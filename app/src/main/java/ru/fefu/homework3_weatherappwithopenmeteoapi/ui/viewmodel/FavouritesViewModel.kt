package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    val state: StateFlow<List<CityItem>> = repository.getFavourites()
        .map { list ->
            list.map { CityItem(it, isFavourite = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromFavourite(cityItem: CityItem) {
        viewModelScope.launch {
            repository.removeFavourite(cityItem.city)
        }
    }
}