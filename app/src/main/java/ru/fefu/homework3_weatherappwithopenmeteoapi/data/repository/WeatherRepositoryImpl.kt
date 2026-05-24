package ru.fefu.homework3_weatherappwithopenmeteoapi.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toCity
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toDbModel
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val favouriteDao: FavouriteDao
) : WeatherRepository {
    private val searchCache = mutableMapOf<Int, City>()

    override suspend fun searchCities(name: String) =
        (geocodingApi.searchCity(name).toCities() ?: emptyList())
            .also { cities -> cities.forEach { searchCache[it.id] = it } }

    override suspend fun getWeather(lat: Double, lon: Double) =
        weatherApi.getWeather(lat, lon).current_weather

    override fun getFavourites(): Flow<List<City>> =
        favouriteDao.getAll().map { list -> list.map { it.toCity() } }

    override suspend fun getCityById(id: Int): City? =
        searchCache[id] ?: favouriteDao.getById(id)?.toCity()

    override suspend fun addFavourite(city: City) = favouriteDao.insert(city.toDbModel())

    override suspend fun removeFavourite(city: City) = favouriteDao.delete(city.toDbModel())
}