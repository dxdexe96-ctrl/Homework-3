package ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.GeocodingResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.WeatherResponse


interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "ru"
    ): GeocodingResponse
}

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse
}
