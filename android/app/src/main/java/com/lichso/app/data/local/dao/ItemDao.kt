package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO hợp nhất cho [ItemEntity] (ghi chú + việc cần làm + nhắc nhở).
 * Thay thế TaskDao / NoteDao / ReminderDao cũ.
 */
@Dao
interface ItemDao {

    // ── Danh sách chính: ghim lên đầu, việc chưa xong trước, mới cập nhật trước ──
    @Query("SELECT * FROM items ORDER BY isPinned DESC, isDone ASC, updatedAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    suspend fun getAllItemsOnce(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): ItemEntity?

    // ── Theo ngày (dueDate trong [start,end)) — dùng cho lịch ngày ──
    @Query("SELECT * FROM items WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY isDone ASC, dueTime ASC")
    fun getItemsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<ItemEntity>>

    // ── Nhắc nhở đang bật (để lên lịch alarm) ──
    @Query("SELECT * FROM items WHERE hasReminder = 1 AND reminderEnabled = 1 ORDER BY reminderAt ASC")
    suspend fun getActiveRemindersOnce(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE hasReminder = 1 AND reminderEnabled = 1 AND reminderAt >= :startOfDay AND reminderAt < :endOfDay ORDER BY reminderAt ASC")
    fun getRemindersForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<ItemEntity>>

    // ── Đếm cho badge / hồ sơ ──
    @Query("SELECT COUNT(*) FROM items WHERE isTask = 1 AND isDone = 0")
    fun getPendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM items WHERE hasReminder = 1 AND reminderEnabled = 1")
    fun getActiveReminderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM items")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("UPDATE items SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleDone(id: Long, isDone: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE items SET reminderEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleReminderEnabled(id: Long, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
