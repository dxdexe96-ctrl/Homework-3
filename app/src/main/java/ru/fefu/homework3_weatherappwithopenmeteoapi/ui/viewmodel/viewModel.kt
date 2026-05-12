package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.local.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.local.toCity
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.local.toEntity
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.CurrentWeather
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import javax.inject.Inject


class WeatherRepository @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val favouriteDao: FavouriteDao
) {
    suspend fun searchCities(name: String) =
        geocodingApi.searchCity(name).results ?: emptyList()

    suspend fun getWeather(lat: Double, lon: Double) =
        weatherApi.getWeather(lat, lon)

    fun getFavourites(): Flow<List<City>> =
        favouriteDao.getAll().map { list -> list.map { it.toCity() } }

    suspend fun getCityById(id: Int): City? =
        favouriteDao.getById(id)?.toCity()

    suspend fun addFavourite(city: City) = favouriteDao.insert(city.toEntity())

    suspend fun removeFavourite(city: City) = favouriteDao.delete(city.toEntity())
}


sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val weather: CurrentWeather,
        val city: City,
        val isFavourite: Boolean
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

data class CityItem(val city: City, val isFavourite: Boolean)

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val items: List<CityItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    val searchState = mutableStateOf<SearchUiState>(SearchUiState.Idle)
    val detailState = mutableStateOf<DetailUiState>(DetailUiState.Loading)
    val searchQuery = mutableStateOf("")
    val favourites = mutableStateOf<List<City>>(emptyList())

    private val citiesCache = mutableMapOf<Int, City>()

    init {
        viewModelScope.launch {
            repository.getFavourites().collect {
                favourites.value = it
                updateAllFavouriteFlags()
            }
        }
    }

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun searchCities() {
        if (searchQuery.value.isBlank()) return
        viewModelScope.launch {
            searchState.value = SearchUiState.Loading
            try {
                val cities = repository.searchCities(searchQuery.value)
                cities.forEach { citiesCache[it.id] = it }
                searchState.value = if (cities.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    val items = cities.map { city ->
                        CityItem(city, favourites.value.any { it.id == city.id })
                    }
                    SearchUiState.Success(items)
                }
            } catch (e: Exception) {
                searchState.value = SearchUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun loadWeather(cityId: Int) {
        viewModelScope.launch {
            detailState.value = DetailUiState.Loading
            val city = citiesCache[cityId] ?: repository.getCityById(cityId)
            if (city == null) {
                detailState.value = DetailUiState.Error("Город не найден")
                return@launch
            }
            try {
                val response = repository.getWeather(city.latitude, city.longitude)
                val isFav = favourites.value.any { it.id == city.id }
                detailState.value = DetailUiState.Success(response.current_weather, city, isFav)
            } catch (e: Exception) {
                detailState.value = DetailUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun toggleFavourite(city: City) {
        viewModelScope.launch {
            if (favourites.value.any { it.id == city.id }) {
                repository.removeFavourite(city)
            } else {
                repository.addFavourite(city)
            }
        }
    }

    private fun updateAllFavouriteFlags() {
        val current = detailState.value
        if (current is DetailUiState.Success) {
            detailState.value = current.copy(
                isFavourite = favourites.value.any { it.id == current.city.id }
            )
        }
        updateSearchStateFavourite()
    }

    private fun updateSearchStateFavourite() {
        val current = searchState.value as? SearchUiState.Success ?: return
        searchState.value = current.copy(
            items = current.items.map { item ->
                item.copy(isFavourite = favourites.value.any { it.id == item.city.id })
            }
        )
    }
}

