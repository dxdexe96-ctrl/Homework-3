package ru.fefu.homework3_weatherappwithopenmeteoapi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.local.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.local.FavouriteEntity

@Database(
    entities = [FavouriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
}