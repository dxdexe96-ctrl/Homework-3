package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites")
    fun getAll(): Flow<List<FavouriteEntity>>

    @Query("SELECT * FROM favourites WHERE id = :id")
    suspend fun getById(id: Int): FavouriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteEntity)

    @Delete
    suspend fun delete(entity: FavouriteEntity)
}