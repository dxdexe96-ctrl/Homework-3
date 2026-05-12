package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.CurrentWeather
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.DetailUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.weatherDescription


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchState: SearchUiState,
    onCityClick: (City) -> Unit,
    onFavouriteClick: (City) -> Unit,
    onOpenFavourites: () -> Unit
) {
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
                value = searchQuery,
                onValueChange = onQueryChange,
                label = { Text("Название города") },
                placeholder = { Text("Например: Moscow") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSearch, modifier = Modifier.fillMaxWidth()) {
                Text("Найти")
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (searchState) {
                is SearchUiState.Idle -> Text("Введите название города и нажмите «Найти»")
                is SearchUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is SearchUiState.Empty -> Text("Ничего не найдено. Попробуйте другое название.")
                is SearchUiState.Error -> {
                    Text("Ошибка: ${searchState.message}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onSearch) { Text("Повторить") }
                }
                is SearchUiState.Success -> {
                    LazyColumn {
                        items(searchState.items) { item ->
                            CityListItem(
                                city = item.city,
                                isFavourite = item.isFavourite,
                                onClick = { onCityClick(item.city) },
                                onFavouriteClick = { onFavouriteClick(item.city) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityListItem(
    city: City,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onFavouriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(city.name, style = MaterialTheme.typography.bodyLarge)
            val subtitle = listOfNotNull(city.admin1, city.country)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = onFavouriteClick) {
            Icon(
                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavourite) "Убрать из избранного" else "Добавить в избранное"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    detailState: DetailUiState,
    onBack: () -> Unit,
    onFavouriteClick: (City) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Погода") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (detailState) {
                is DetailUiState.Loading -> CircularProgressIndicator()
                is DetailUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${detailState.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBack) { Text("Назад") }
                    }
                }
                is DetailUiState.Success -> {
                    WeatherDetail(
                        city = detailState.city,
                        weather = detailState.weather,
                        isFavourite = detailState.isFavourite,
                        onFavouriteClick = onFavouriteClick
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetail(
    city: City,
    weather: CurrentWeather,
    isFavourite: Boolean,
    onFavouriteClick: (City) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(city.name, style = MaterialTheme.typography.headlineMedium)
        if (!city.country.isNullOrBlank()) {
            Text(city.country, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(weatherDescription(weather.weathercode), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                WeatherRow("Температура", "${weather.temperature} °C")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Скорость ветра", "${weather.windspeed} км/ч")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Широта", "${city.latitude}°")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Долгота", "${city.longitude}°")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Время замера", weather.time)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onFavouriteClick(city) }) {
            Icon(
                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFavourite) "Убрать из избранного" else "В избранное")
        }
    }
}

@Composable
fun WeatherRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    favourites: List<City>,
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    onFavouriteClick: (City) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Любимые места") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Назад")
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Список избранного пока пуст")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(favourites) { city ->
                        CityListItem(
                            city = city,
                            isFavourite = true,
                            onClick = { onCityClick(city) },
                            onFavouriteClick = { onFavouriteClick(city) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}