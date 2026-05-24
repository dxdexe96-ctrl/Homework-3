@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import javax.inject.Inject

data class CityItem(val city: City, val isFavourite: Boolean)

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val items: List<CityItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val trigger = MutableStateFlow(Unit)
    val query = searchQuery.asStateFlow()

    val state: StateFlow<SearchUiState> = combine(
        searchQuery,
        trigger.onStart { emit(Unit) }
    ) { query, _ ->
        query
    }
        .debounce { query -> if (query.isBlank()) 0L else 500L }
        .flatMapLatest { query ->
            repository.getFavourites().map { favourites ->
                val items = repository.searchCities(query).map { city ->
                    CityItem(city, isFavourite = favourites.any { it.id == city.id })
                }
                if (query.isBlank()) SearchUiState.Idle
                else if (items.isEmpty()) SearchUiState.Empty
                else SearchUiState.Success(items)
            }
                .onStart { emit(SearchUiState.Loading) }
                .catch { e -> emit(SearchUiState.Error(e.message ?: "Ошибка")) }
        }
        .onStart { emit(SearchUiState.Idle) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    fun onQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

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