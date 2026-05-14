package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val country: String,
    val region: String,
    val latitude: Double,
    val longitude: Double
)