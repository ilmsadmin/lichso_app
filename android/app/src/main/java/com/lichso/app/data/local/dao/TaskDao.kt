package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND dueDate = :dateMillis ORDER BY priority DESC")
    fun getTasksForDate(dateMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 0")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleDone(id: Long, isDone: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
