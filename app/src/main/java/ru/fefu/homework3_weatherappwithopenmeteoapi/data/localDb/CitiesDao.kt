package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CitiesDao {
    @Query("SELECT * FROM cities")
    fun getAll(): Flow<List<CityDbModel>>

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getById(id: Int): CityDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CityDbModel)

    @Delete
    suspend fun delete(entity: CityDbModel)

    @Query("""
        DELETE FROM cities
        WHERE lastAccessed < :expirationTime
    """)
    suspend fun clearOldCache(expirationTime: Long)
}