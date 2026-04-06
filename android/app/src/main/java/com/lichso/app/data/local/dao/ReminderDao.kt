package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    suspend fun getAllRemindersOnce(): List<ReminderEntity>

    @Query("SELECT * FROM reminders ORDER BY triggerTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY triggerTime ASC")
    fun getEnabledReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders WHERE isEnabled = 1")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT * FROM reminders WHERE triggerTime >= :startOfDay AND triggerTime < :endOfDay ORDER BY triggerTime ASC")
    fun getRemindersForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
