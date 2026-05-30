package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
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
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: WeatherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cityId: Int = savedStateHandle["cityId"]
        ?: error("cityId argument is missing")

    private val trigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        trigger.tryEmit(Unit)
    }

    val state: StateFlow<DetailUiState> = trigger
        .flatMapLatest {
            flow {
                emit(DetailUiState.Loading)

                val city = repository.getCityById(cityId) ?: error("Нет такого города")
                val weather = repository.getWeather(city.latitude, city.longitude)

                val favouritesFlow = repository.existFavById(cityId).map { isFavourite ->
                    DetailUiState.Success(
                        weather = weather,
                        cityItem = CityItem(city, isFavourite)
                    )
                }
                emitAll(favouritesFlow)

            }.catch { e -> emit(DetailUiState.Error(e.message ?: "Ошибка")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailUiState.Loading)

    fun retryClick() {
        trigger.tryEmit(Unit)
    }

    fun toggleFavourite(item: CityItem) {
        viewModelScope.launch {
            if (item.isFavourite) repository.removeFavourite(item.city)
            else repository.addFavourite(item.city)
        }
    }
}