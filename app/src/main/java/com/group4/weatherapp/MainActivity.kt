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
        showPlaceholder()
        playEntrance()
        startGlowPulse()
        startIconFloat()
        animateTemperature(31)

        currentBg = ContextCompat.getDrawable(this, Mood.SUNNY.background)
        currentGlow = ContextCompat.getColor(this, Mood.SUNNY.glow)

        binding.ivWeather.setOnClickListener {
            val moods = Mood.entries
            applyMood(moods[(currentMood.ordinal + 1) % moods.size])
        }
    }

    private fun showPlaceholder() {
        binding.tvCity.text = "Colombo, Sri Lanka"
        binding.tvTemp.text = "31°C"
        binding.tvDesc.text = "Clear skies, high UV today."
        binding.tvWind.text = "12 km/h"
        binding.tvHumidity.text = "58%"

        val sun = Color.parseColor("#FFB84D")
        val cloud = Color.parseColor("#BFD7FF")
        hourAdapter.submitList(
            listOf(
                HourForecast("Now", 31, R.drawable.ic_sun, sun),
                HourForecast("2pm", 32, R.drawable.ic_sun, sun),
                HourForecast("3pm", 32, R.drawable.ic_cloud, cloud),
                HourForecast("4pm", 30, R.drawable.ic_cloud, cloud)
            )
        )
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



    private fun applyMood(mood: Mood) {
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
            binding.ivWeather.setImageResource(mood.icon)
            binding.ivWeather.imageTintList = ColorStateList.valueOf(mood.iconTint)
            binding.ivWeather.animate().alpha(1f).setDuration(350).start()
        }.start()

        binding.tvDesc.text = mood.desc
        binding.tvWind.text = mood.wind
        binding.tvHumidity.text = mood.humidity
        animateTemperature(mood.temp)
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