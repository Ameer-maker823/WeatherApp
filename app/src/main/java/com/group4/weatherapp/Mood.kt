package com.group4.weatherapp


import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

enum class Mood(
    @DrawableRes val background: Int,
    @ColorRes val glow: Int,
    @DrawableRes val icon: Int,
    val iconTint: Int,
    val temp: Int,
    val desc: String,
    val wind: String,
    val humidity: String
) {
    SUNNY(R.drawable.bg_sunny, R.color.glow_sunny, R.drawable.ic_sun, 0xFFFFB84D.toInt(),
        31, "Clear skies, high UV today.", "12 km/h", "58%"),
    RAIN(R.drawable.bg_rain, R.color.glow_rain, R.drawable.ic_cloud, 0xFFBFD7FF.toInt(),
        27, "Expect heavy rain today.", "18 km/h", "91%"),
    NIGHT(R.drawable.bg_night, R.color.glow_night, R.drawable.ic_moon, 0xFFC9C3FF.toInt(),
        25, "Clear and calm tonight.", "8 km/h", "72%")
}