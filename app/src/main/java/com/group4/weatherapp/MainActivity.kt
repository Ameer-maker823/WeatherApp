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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val hourAdapter = HourAdapter()
    private val loopAnimators = mutableListOf<Animator>()

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
        ValueAnimator.ofInt(0, target).apply {
            duration = 900
            addUpdateListener { binding.tvTemp.text = "${it.animatedValue}°C" }
            start()
        }
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