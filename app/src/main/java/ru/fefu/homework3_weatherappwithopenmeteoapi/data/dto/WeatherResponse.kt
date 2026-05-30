package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

data class WeatherResponse(
    val current_weather: CurrentWeatherResponse
) {
    fun toCurrentWeather() = current_weather.toCurrentWeather()
}