package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

data class GeocodingResponse(
    val results: List<CityResponse>? = null
) {
    fun toCities() =
        results?.map { it.toCity() }
}