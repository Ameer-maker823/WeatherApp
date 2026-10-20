package com.group4.weatherapp.data

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(val results: List<GeoResult>?)

data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)

data class ForecastResponse(val current: Current, val hourly: Hourly)

data class Current(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("is_day") val isDay: Int
)

data class Hourly(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)