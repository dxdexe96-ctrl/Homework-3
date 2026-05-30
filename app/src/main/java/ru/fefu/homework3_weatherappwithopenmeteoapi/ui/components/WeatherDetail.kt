package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CityItem
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather

@Composable
fun WeatherDetail(
    cityItem: CityItem,
    weather: CurrentWeather,
    onFavouriteClick: (CityItem) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(cityItem.city.name, style = MaterialTheme.typography.headlineMedium)
        if (!cityItem.city.admin1.isNullOrBlank()) {
            Text(cityItem.city.admin1, style = MaterialTheme.typography.bodyMedium)
        }
        if (!cityItem.city.admin2.isNullOrBlank()) {
            Text(cityItem.city.admin2, style = MaterialTheme.typography.bodyMedium)
        }
        if (!cityItem.city.country.isNullOrBlank()) {
            Text(cityItem.city.country, style = MaterialTheme.typography.bodyMedium)
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
                WeatherRow("Широта", "${cityItem.city.latitude}°")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Долгота", "${cityItem.city.longitude}°")
                Spacer(modifier = Modifier.height(8.dp))
                WeatherRow("Время замера", weather.time)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onFavouriteClick(cityItem) }) {
            Icon(
                imageVector = if (cityItem.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (cityItem.isFavourite) "Убрать из избранного" else "В избранное")
        }
    }
}

fun weatherDescription(code: Int): String = when (code) {
    0 -> "Ясно"
    in 1..3 -> "Переменная облачность"
    in 45..48 -> "Туман"
    in 51..55 -> "Морось"
    in 61..67 -> "Дождь"
    in 71..77 -> "Снег"
    in 80..82 -> "Ливень"
    in 95..99 -> "Гроза"
    else -> "Неизвестно"
}