package com.lichso.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lichso.app.data.local.entity.CountdownEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownEventDao {
    @Query("SELECT * FROM countdown_events ORDER BY targetEpochDay ASC, createdAt DESC")
    fun observeAll(): Flow<List<CountdownEventEntity>>

    @Query("SELECT * FROM countdown_events WHERE showOnHome = 1 AND targetEpochDay >= :todayEpochDay ORDER BY targetEpochDay ASC LIMIT :limit")
    fun observeUpcomingForHome(todayEpochDay: Long, limit: Int = 3): Flow<List<CountdownEventEntity>>

    @Query("SELECT * FROM countdown_events WHERE showOnWidget = 1 AND targetEpochDay >= :todayEpochDay ORDER BY targetEpochDay ASC LIMIT 1")
    suspend fun getPrimaryForWidget(todayEpochDay: Long): CountdownEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CountdownEventEntity): Long

    @Update
    suspend fun update(entity: CountdownEventEntity)

    @Delete
    suspend fun delete(entity: CountdownEventEntity)
}
