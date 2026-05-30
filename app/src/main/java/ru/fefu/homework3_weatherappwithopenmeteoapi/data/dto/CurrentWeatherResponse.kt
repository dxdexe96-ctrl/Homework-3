package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather

data class CurrentWeatherResponse(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
) {
    fun toCurrentWeather() = CurrentWeather(
        temperature = temperature,
        windspeed = windspeed,
        weathercode = weathercode,
        time = time
    )
}