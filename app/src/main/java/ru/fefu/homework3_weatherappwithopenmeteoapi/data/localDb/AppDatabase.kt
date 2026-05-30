package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CityDbModel::class, FavouriteDbModel::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citiesDao(): CitiesDao
    abstract fun favouriteDao(): FavouriteDao
}