package ru.fefu.homework3_weatherappwithopenmeteoapi.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.CitiesDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toCity
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toDbModel
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.toFavDbModel
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val citiesDao: CitiesDao,
    private val favouriteDao: FavouriteDao,
) : WeatherRepository {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expirationTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                citiesDao.clearOldCache(expirationTime)
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Не удалось очистить кэш", e)
            }
        }
    }

    override suspend fun searchCities(name: String) =
        (geocodingApi.searchCity(name).toCities() ?: emptyList())

    override suspend fun getWeather(lat: Double, lon: Double) =
        weatherApi.getWeather(lat, lon).toCurrentWeather()

    override fun getFavourites(): Flow<List<City>> =
        favouriteDao.getAll().map { list -> list.map { it.toCity() } }

    override fun existFavById(id: Int): Flow<Boolean> =
        favouriteDao.existById(id)

    override suspend fun getCityById(id: Int): City? =
        citiesDao.getById(id)?.toCity()

    override suspend fun saveCities(cities: List<City>) =
        cities.forEach { citiesDao.insert(it.toDbModel()) }

    override suspend fun addFavourite(city: City) = favouriteDao.insert(city.toFavDbModel())
    override suspend fun removeFavourite(city: City) = favouriteDao.delete(city.toFavDbModel())
}