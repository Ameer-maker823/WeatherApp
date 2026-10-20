package com.group4.weatherapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1
    ): GeocodingResponse

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,is_day",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("forecast_days") days: Int = 2,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse
}

object RetrofitClient {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}