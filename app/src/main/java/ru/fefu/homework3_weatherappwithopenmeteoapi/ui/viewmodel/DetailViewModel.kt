package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
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
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val repository: WeatherRepository,
    @Assisted private val cityId: Int
) : ViewModel() {


    @AssistedFactory
    interface Factory {
        fun create(cityId: Int): DetailViewModel
    }

    private val trigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        trigger.tryEmit(Unit)
    }

    val state: StateFlow<DetailUiState> = trigger
        .flatMapLatest {
            val city = repository.getCityById(cityId) ?: error("Нет такого города")

            val weather = repository.getWeather(city.latitude, city.longitude)

            repository.existFavById(cityId).map<Boolean, DetailUiState> { isFavourite ->
                DetailUiState.Success(
                    weather = weather,
                    cityItem = CityItem(city, isFavourite)
                )
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