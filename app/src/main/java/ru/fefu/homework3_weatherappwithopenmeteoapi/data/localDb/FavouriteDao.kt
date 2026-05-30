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
    fun getAll(): Flow<List<FavouriteDbModel>>

    @Query("SELECT * FROM favourites WHERE id = :id")
    suspend fun getById(id: Int): FavouriteDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteDbModel)

    @Query("SELECT EXISTS (SELECT * FROM favourites WHERE id = :id)")
    fun existById(id: Int): Flow<Boolean>

    @Delete
    suspend fun delete(entity: FavouriteDbModel)
}