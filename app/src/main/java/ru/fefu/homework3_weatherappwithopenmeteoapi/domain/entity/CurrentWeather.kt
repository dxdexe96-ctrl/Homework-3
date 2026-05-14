package ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)