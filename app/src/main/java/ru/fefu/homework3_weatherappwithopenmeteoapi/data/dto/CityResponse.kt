package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City

data class CityResponse(
    val id: Int,
    val name: String,
    val country: String? = "",
    val admin1: String? = "",
    val admin2: String? = "",
    val latitude: Double,
    val longitude: Double
) {
    fun toCity() = City(
        id,
        name,
        country,
        admin1,
        admin2,
        latitude,
        longitude
    )
}

