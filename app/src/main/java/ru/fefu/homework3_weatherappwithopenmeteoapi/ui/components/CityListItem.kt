package ru.fefu.homework3_weatherappwithopenmeteoapi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.CityItem

@Composable
fun CityListItem(
    cityItem: CityItem,
    onClick: () -> Unit,
    onFavouriteClick: (CityItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(cityItem.city.name, style = MaterialTheme.typography.bodyLarge)
            val subtitle = listOfNotNull(cityItem.city.admin1,cityItem.city.admin2, cityItem.city.country)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = { onFavouriteClick(cityItem) }) {
            Icon(
                imageVector = if (cityItem.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (cityItem.isFavourite) "Убрать из избранного" else "Добавить в избранное"
            )
        }
    }
}