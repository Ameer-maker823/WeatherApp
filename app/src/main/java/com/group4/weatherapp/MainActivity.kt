package com.group4.weatherapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group4.weatherapp.databinding.ActivityMainBinding
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.core.content.ContextCompat
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.group4.weatherapp.data.ForecastResponse
import com.group4.weatherapp.data.GeoResult
import com.group4.weatherapp.data.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val hourAdapter = HourAdapter()
    private val loopAnimators = mutableListOf<Animator>()
    private var currentMood = Mood.SUNNY
    private var currentBg: Drawable? = null
    private var currentGlow = 0
    private var shownTemp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val pad = (20 * resources.displayMetrics.density).toInt()
            binding.content.setPadding(
                bars.left + pad, bars.top + pad, bars.right + pad, bars.bottom + pad
            )
            insets
        }

        binding.rvHourly.adapter = hourAdapter
        playEntrance()
        startGlowPulse()
        startIconFloat()

        currentBg = ContextCompat.getDrawable(this, Mood.SUNNY.background)
        currentGlow = ContextCompat.getColor(this, Mood.SUNNY.glow)

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard()
                    loadWeather(query)
                }
                true
            } else {
                false
            }
        }

        loadWeather(savedInstanceState?.getString("city") ?: "Colombo")
    }
    private fun startGlowPulse() {
        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            binding.glow,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.18f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.18f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.55f, 0.9f)
        ).apply {
            duration = 2200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        loopAnimators.add(pulse)
        pulse.start()
    }

    private fun startIconFloat() {
        val rise = -8 * resources.displayMetrics.density
        val float = ObjectAnimator.ofFloat(binding.ivWeather, View.TRANSLATION_Y, 0f, rise).apply {
            duration = 2600
            startDelay = 900
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        loopAnimators.add(float)
        float.start()
    }

    private fun animateTemperature(target: Int) {
        ValueAnimator.ofInt(shownTemp, target).apply {
            duration = 900
            addUpdateListener {
                shownTemp = it.animatedValue as Int
                binding.tvTemp.text = "$shownTemp°C"
            }
            start()
        }
    }



    private fun applyMood(
        mood: Mood,
        temp: Int = mood.temp,
        desc: String = mood.desc,
        wind: String = mood.wind,
        humidity: String = mood.humidity,
        icon: Pair<Int, Int> = mood.icon to mood.iconTint
    ) {
        currentMood = mood

        val next = ContextCompat.getDrawable(this, mood.background)!!
        val transition = TransitionDrawable(arrayOf(currentBg ?: next, next))
        binding.main.background = transition
        transition.startTransition(700)
        currentBg = next

        val toGlow = ContextCompat.getColor(this, mood.glow)
        ValueAnimator.ofObject(ArgbEvaluator(), currentGlow, toGlow).apply {
            duration = 700
            addUpdateListener {
                binding.glow.imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
            }
            start()
        }
        currentGlow = toGlow

        binding.ivWeather.animate().alpha(0f).setDuration(250).withEndAction {
            binding.ivWeather.setImageResource(icon.first)
            binding.ivWeather.imageTintList = ColorStateList.valueOf(icon.second)
            binding.ivWeather.animate().alpha(1f).setDuration(350).start()
        }.start()

        binding.tvDesc.text = desc
        binding.tvWind.text = wind
        binding.tvHumidity.text = humidity
        animateTemperature(temp)
    }

    private val sunTint = 0xFFFFB84D.toInt()
    private val coolTint = 0xFFBFD7FF.toInt()
    private val moonTint = 0xFFC9C3FF.toInt()
    private var currentCity = "Colombo"

    private fun loadWeather(city: String) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val geo = RetrofitClient.api.searchCity(city).results?.firstOrNull()
                if (geo == null) {
                    showError(getString(R.string.error_city_not_found))
                    return@launch
                }
                val forecast = RetrofitClient.api.getForecast(geo.latitude, geo.longitude)
                currentCity = city
                showWeather(geo, forecast)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                showError(getString(R.string.error_network))
            } catch (e: Exception) {
                showError(getString(R.string.error_generic))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.content.alpha = if (loading) 0.4f else 1f
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun showWeather(geo: GeoResult, forecast: ForecastResponse) {
        binding.tvError.visibility = View.GONE
        binding.tvCity.text = listOfNotNull(geo.name, geo.country).joinToString(", ")

        val c = forecast.current
        val isDay = c.isDay == 1
        applyMood(
            mood = moodFor(c.weatherCode, isDay),
            temp = c.temperature.roundToInt(),
            desc = describe(c.weatherCode),
            wind = "${c.windSpeed.roundToInt()} km/h",
            humidity = "${c.humidity}%",
            icon = iconFor(c.weatherCode, isDay)
        )
        hourAdapter.submitList(buildHours(forecast))
    }

    private fun isRain(code: Int) = code in 51..67 || code in 80..82 || code in 95..99

    private fun moodFor(code: Int, isDay: Boolean) = when {
        isRain(code) -> Mood.RAIN
        !isDay -> Mood.NIGHT
        else -> Mood.SUNNY
    }

    private fun iconFor(code: Int, isDay: Boolean): Pair<Int, Int> = when {
        isRain(code) || code >= 2 -> R.drawable.ic_cloud to coolTint
        isDay -> R.drawable.ic_sun to sunTint
        else -> R.drawable.ic_moon to moonTint
    }

    private fun describe(code: Int) = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        in 51..57 -> "Drizzle"
        in 61..67 -> "Rain"
        in 71..77 -> "Snow"
        in 80..82 -> "Rain showers"
        in 85..86 -> "Snow showers"
        in 95..99 -> "Thunderstorm"
        else -> "Unknown"
    }

    private fun buildHours(forecast: ForecastResponse): List<HourForecast> {
        val h = forecast.hourly
        val nowHour = forecast.current.time.substring(0, 13)
        val start = h.time.indexOfFirst { it.substring(0, 13) >= nowHour }.coerceAtLeast(0)
        val end = minOf(start + 8, h.time.size)
        return (start until end).map { i ->
            val hour = h.time[i].substring(11, 13).toInt()
            val (icon, tint) = iconFor(h.weatherCode[i], hour in 6..18)
            HourForecast(
                label = if (i == start) "Now" else hourLabel(hour),
                temp = h.temperature[i].roundToInt(),
                iconRes = icon,
                tint = tint
            )
        }
    }

    private fun hourLabel(hour: Int): String {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        return "$h12${if (hour < 12) "am" else "pm"}"
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("city", currentCity)
    }

    private fun playEntrance() {
        val offset = 32 * resources.displayMetrics.density
        for (i in 0 until binding.content.childCount) {
            val v = binding.content.getChildAt(i)
            v.alpha = 0f
            v.translationY = offset
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i * 80L)
                .setDuration(500)
                .start()
        }
    }

    override fun onDestroy() {
        loopAnimators.forEach { it.cancel() }
        super.onDestroy()
    }
}