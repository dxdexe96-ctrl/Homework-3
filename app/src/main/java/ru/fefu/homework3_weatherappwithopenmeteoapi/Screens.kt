package ru.fefu.homework3_weatherappwithopenmeteoapi

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: WeatherViewModel,
    onCityClick: (City) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Поиск погоды") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                label = { Text("Название города") },
                placeholder = { Text("Например: Moscov") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.searchCities() }, modifier = Modifier.fillMaxWidth()) {
                Text("Найти")
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = viewModel.searchState) {
                is SearchUiState.Idle -> Text("Введите название города и нажмите «Найти»")
                is SearchUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is SearchUiState.Empty -> Text("Ничего не найдено. Попробуйте другое название.")
                is SearchUiState.Error -> {
                    Text("Ошибка: ${state.message}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.searchCities() }) { Text("Повторить") }
                }
                is SearchUiState.Success -> {
                    LazyColumn {
                        items(state.cities) { city ->
                            CityListItem(
                                city = city,
                                isFavourite = viewModel.isFavourite(city),
                                onClick = { onCityClick(city) },
                                onFavouriteClick = { viewModel.toggleFavourite(city) }
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

//  Экран деталей
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit
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
            when (val state = viewModel.weatherState)
            {
                null, WeatherUiState.Loading -> CircularProgressIndicator()
                is WeatherUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${state.message}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBack) { Text("Назад") }
                    }
                }
                is WeatherUiState.Success -> {
                    WeatherDetail(
                        city = state.city,
                        weather = state.weather,
                        isFavourite = viewModel.isFavourite(state.city),
                        onFavouriteClick = { viewModel.toggleFavourite(state.city) }
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
    onFavouriteClick: () -> Unit
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
        Button(onClick = onFavouriteClick) {
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