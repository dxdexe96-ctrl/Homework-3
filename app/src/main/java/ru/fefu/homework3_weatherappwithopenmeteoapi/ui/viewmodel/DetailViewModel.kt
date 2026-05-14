package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val weather: CurrentWeather,
        val cityItem: CityItem,
    ) : DetailUiState()

    data class Error(val message: String) : DetailUiState()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val repository: WeatherRepository,
    @Assisted private val cityId: Int
) : ViewModel() {


    @AssistedFactory
    interface Factory {
        fun create(cityId: Int): DetailViewModel
    }

    val state: StateFlow<DetailUiState> = flow<DetailUiState> {
        val city = repository.getCityById(cityId) ?: throw Exception("Нет такого города")
        val weather = repository.getWeather(city.latitude, city.longitude)
        repository.getFavourites().map { favs ->
            DetailUiState.Success(
                weather = weather,
                cityItem = CityItem(
                    city = city,
                    isFavourite = favs.any { it.id == cityId }
                )
            )
        }.collect { emit(it) }
    }
        .onStart { emit(DetailUiState.Loading) }
        .catch { emit(DetailUiState.Error(it.message ?: " Ошибка")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailUiState.Loading)

    fun toggleFavourite(item: CityItem) {
        viewModelScope.launch {
            if (item.isFavourite) repository.removeFavourite(item.city)
            else repository.addFavourite(item.city)
        }
    }
}