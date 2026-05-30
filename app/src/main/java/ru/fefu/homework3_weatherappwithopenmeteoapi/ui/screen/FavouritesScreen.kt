package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.components.CityListItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.components.EmptyFavouritesPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    favourites: List<CityItem>,
    removeFromFavourite: (CityItem) -> Unit,
    onBack: () -> Unit,
    onCityClick: (CityItem) -> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Любимые места") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (favourites.isEmpty()) {
                EmptyFavouritesPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favourites, key = { it.city.id }) { item ->
                        CityListItem(
                            cityItem = item,
                            onClick = { onCityClick(item) },
                            onFavouriteClick = removeFromFavourite
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}