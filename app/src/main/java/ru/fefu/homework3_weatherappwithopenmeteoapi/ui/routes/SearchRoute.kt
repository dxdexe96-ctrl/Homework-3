package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.SearchScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoute(
    vm: SearchViewModel = hiltViewModel(),
    onOpenFavourites: () -> Unit,
    onCityClick: (CityItem) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    SearchScreen(
        state = state,
        query = query,
        onOpenFavourites = onOpenFavourites,
        onCityClick = onCityClick,
        onQueryChanged = vm::onQueryChanged,
        onRetryClick = vm::retryClick,
        onToggleFavourite = vm::toggleFavourite
    )
}
