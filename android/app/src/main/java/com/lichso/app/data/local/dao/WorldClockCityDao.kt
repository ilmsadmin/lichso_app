package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.WorldClockCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockCityDao {

    @Query("SELECT * FROM world_clock_cities ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<WorldClockCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: WorldClockCityEntity): Long

    @Delete
    suspend fun delete(city: WorldClockCityEntity)

    @Query("SELECT COUNT(*) FROM world_clock_cities")
    suspend fun count(): Int
}
