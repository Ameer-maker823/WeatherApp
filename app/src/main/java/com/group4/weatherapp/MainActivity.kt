package com.group4.weatherapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group4.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val hourAdapter = HourAdapter()

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
}