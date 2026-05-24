package ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather

interface WeatherRepository {
    suspend fun searchCities(name: String): List<City>
    suspend fun getWeather(lat: Double, lon: Double): CurrentWeather
    fun getFavourites(): Flow<List<City>>
    suspend fun getCityById(id: Int): City?
    suspend fun addFavourite(city: City)
    suspend fun removeFavourite(city: City)
}