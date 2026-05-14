package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City

data class GeocodingResponse(
    val results: List<City>? = null
)