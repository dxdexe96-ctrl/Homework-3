package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.FavouritesScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.FavouritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesRoute(
    vm: FavouritesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCityClick: (CityItem) -> Unit,
) {
    val favourites by vm.state.collectAsStateWithLifecycle()

    FavouritesScreen(
        favourites = favourites,
        removeFromFavourite = vm::removeFromFavourite,
        onBack = onBack,
        onCityClick = onCityClick
    )
}