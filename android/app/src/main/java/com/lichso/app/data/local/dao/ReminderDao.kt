package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY triggerTime ASC")
    fun getEnabledReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders WHERE isEnabled = 1")
    fun getActiveCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, isEnabled: Boolean)
}
