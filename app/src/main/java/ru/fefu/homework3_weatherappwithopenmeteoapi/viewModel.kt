package ru.fefu.homework3_weatherappwithopenmeteoapi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class WeatherRepository {
    suspend fun searchCities(name: String): List<City> {
        return RetrofitClient.geocodingApi.searchCity(name).results ?: emptyList()
    }
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return RetrofitClient.weatherApi.getWeather(lat, lon)
    }
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weather: CurrentWeather,
        val city: City
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
    }
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
    data class Success(val items: List<CityItem>) : SearchUiState()   // ← изменено
    data class Error(val message: String) : SearchUiState()
}

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    var searchState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    var detailState: DetailUiState by mutableStateOf(DetailUiState.Loading) // новое поле
        private set

    var searchQuery: String by mutableStateOf("")
        private set

    var favourites: List<City> by mutableStateOf(emptyList())
        private set

    fun onQueryChange(query: String) {
        searchQuery = query
    }

    fun searchCities() {
        if (searchQuery.isBlank()) return
        viewModelScope.launch {
            searchState = SearchUiState.Loading
            try {
                val cities = repository.searchCities(searchQuery)
                searchState = if (cities.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    // Создаём CityItem с флагом isFavourite
                    val items = cities.map { city ->
                        CityItem(city, favourites.any { it.id == city.id })
                    }
                    SearchUiState.Success(items)
                }
            } catch (e: Exception) {
                searchState = SearchUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun loadWeather(city: City) {
        viewModelScope.launch {
            detailState = DetailUiState.Loading
            try {
                val response = repository.getWeather(city.latitude, city.longitude)
                val isFav = favourites.any { it.id == city.id }
                detailState = DetailUiState.Success(response.current_weather, city, isFav)
            } catch (e: Exception) {
                detailState = DetailUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun toggleFavourite(city: City) {
        favourites = if (favourites.any { it.id == city.id }) {
            favourites.filter { it.id != city.id }
        } else {
            favourites + city
        }
        // Если на детальном экране сейчас отображается этот город, обновляем его isFavourite
        if (detailState is DetailUiState.Success && (detailState as DetailUiState.Success).city.id == city.id) {
            val success = detailState as DetailUiState.Success
            detailState = success.copy(isFavourite = favourites.any { it.id == city.id })
        }
        // Также обновляем searchState, если он содержит этот город
        updateSearchStateFavourite(city)
    }

    private fun updateSearchStateFavourite(city: City) {
        if (searchState !is SearchUiState.Success) return
        val success = searchState as SearchUiState.Success
        val updatedItems = success.items.map { item ->
            if (item.city.id == city.id) item.copy(isFavourite = favourites.any { it.id == city.id })
            else item
        }
        searchState = success.copy(items = updatedItems)
    }
}
