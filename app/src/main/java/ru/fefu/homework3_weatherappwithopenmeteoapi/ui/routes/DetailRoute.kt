package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.DetailScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRoute(
    vm: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    DetailScreen(
        state = state,
        onBack = onBack,
        onRetry = vm::retryClick,
        onToggleFavourite = vm::toggleFavourite
    )
}