package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteDbModel(
    @PrimaryKey val id: Int,
    val name: String,
    val country: String,
    val region1: String,
    val region2: String,
    val latitude: Double,
    val longitude: Double
)