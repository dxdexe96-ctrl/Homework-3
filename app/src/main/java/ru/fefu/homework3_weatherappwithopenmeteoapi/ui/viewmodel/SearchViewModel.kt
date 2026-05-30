@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import javax.inject.Inject

private sealed interface SearchResult {
    data object Idle : SearchResult
    data object Loading : SearchResult
    data object Empty : SearchResult
    data class Success(val cities: List<City>) : SearchResult
    data class Error(val message: String) : SearchResult
}

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
    private val trigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val query = searchQuery.asStateFlow()

    private val searchResults: Flow<SearchResult> = combine(
        searchQuery,
        trigger.onStart { emit(Unit) }
    ) { query, _ -> query }
        .debounce { query -> if (query.isBlank()) 0L else 500L }
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(SearchResult.Idle)
            else {
                flow {
                    emit(SearchResult.Loading)
                    val cities = repository.searchCities(query)

                    if (cities.isEmpty()) {
                        emit(SearchResult.Empty)
                    } else {
                        repository.saveCities(cities)
                        emit(SearchResult.Success(cities))
                    }
                }.catch { e -> emit(SearchResult.Error(e.message ?: "Ошибка")) }
            }
        }

    val state: StateFlow<SearchUiState> = combine(
        searchResults,
        repository.getFavourites()
    ) { result, favourites ->
        when (result) {
            is SearchResult.Idle -> SearchUiState.Idle
            is SearchResult.Loading -> SearchUiState.Loading
            is SearchResult.Empty -> SearchUiState.Empty
            is SearchResult.Error -> SearchUiState.Error(result.message)
            is SearchResult.Success -> {
                val items = result.cities.map { city ->
                    CityItem(
                        city = city,
                        isFavourite = favourites.any { it.id == city.id }
                    )
                }
                SearchUiState.Success(items)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Idle
    )

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