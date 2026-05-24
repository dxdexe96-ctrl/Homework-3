package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.components.CityListItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel = hiltViewModel(),
    onOpenFavourites: () -> Unit,
    onCityClick: (City) -> Unit
) {

    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск погоды") },
                actions = {
                    IconButton(onClick = onOpenFavourites) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Открыть избранное"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChanged,
                label = { Text("Название города") },
                placeholder = { Text("Например: Moscow") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = state) {
                is SearchUiState.Idle -> Text("Введите название города")
                is SearchUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is SearchUiState.Empty -> Text("Ничего не найдено. Попробуйте другое название.")
                is SearchUiState.Error -> {
                    Text("Ошибка: ${state.message}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {vm.retryClick()}) { Text("Повторить") }
                }

                is SearchUiState.Success -> {
                    LazyColumn {
                        items(state.items, key = {it.city.id}) { item ->
                            CityListItem(
                                cityItem = item,
                                onClick = { onCityClick(item.city) },
                                onFavouriteClick = vm::toggleFavourite
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
