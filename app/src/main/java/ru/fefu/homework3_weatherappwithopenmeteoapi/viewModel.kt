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
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val cities: List<City>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}



sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weather: CurrentWeather,
        val city: City
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}


class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()
        var searchState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set
    var weatherState: WeatherUiState? by mutableStateOf(null) // ДОБАВИТЬ БАЗОВЫй стиль
        private set

    // Текст в поисковой строке
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
                    SearchUiState.Success(cities)
                }
            } catch (e: Exception) {
                searchState = SearchUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }


    fun loadWeather(city: City) {
        viewModelScope.launch {
            weatherState = WeatherUiState.Loading
            try {
                val response = repository.getWeather(city.latitude, city.longitude)
                weatherState = WeatherUiState.Success(response.current_weather, city)
            } catch (e: Exception) {
                weatherState = WeatherUiState.Error(e.message ?: "Ошибка сети")
            }
        }
    }


    fun toggleFavourite(city: City) {
        favourites = if (favourites.any { it.id == city.id }) {
            favourites.filter { it.id != city.id }  // убрать
        } else {
            favourites + city  // добавить
        }
    }


    fun isFavourite(city: City): Boolean = favourites.any { it.id == city.id }
}
