package ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity

data class City(
    val id: Int,
    val name: String,
    val country: String? = "",
    val admin1: String? = "",
    val latitude: Double,
    val longitude: Double
)