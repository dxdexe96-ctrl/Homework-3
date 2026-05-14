package ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity

data class City(
    val id: Int = 0,
    val name: String,
    val country: String? = "",
    val admin1: String? = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)