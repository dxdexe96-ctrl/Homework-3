package ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.WeatherResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toCity
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toDbModel
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather
import javax.inject.Inject
import kotlin.collections.map

interface WeatherRepository {
    suspend fun searchCities(name: String): Flow<List<City>>
    suspend fun getWeather(lat: Double, lon: Double): CurrentWeather
    fun getFavourites(): Flow<List<City>>
    suspend fun getCityById(id: Int): City?
    suspend fun addFavourite(city: City)
    suspend fun removeFavourite(city: City)
}

class WeatherRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val favouriteDao: FavouriteDao
) : WeatherRepository {
    override suspend fun searchCities(name: String) = flow {
        emit(geocodingApi.searchCity(name).results ?: emptyList())
    }.flowOn(Dispatchers.IO)

    override suspend fun getWeather(lat: Double, lon: Double) =
        weatherApi.getWeather(lat, lon).current_weather

    override fun getFavourites(): Flow<List<City>> =
        favouriteDao.getAll().map { list -> list.map { it.toCity() } }

    override suspend fun getCityById(id: Int): City? =
        favouriteDao.getById(id)?.toCity()

    override suspend fun addFavourite(city: City) = favouriteDao.insert(city.toDbModel())

    override suspend fun removeFavourite(city: City) = favouriteDao.delete(city.toDbModel())
}