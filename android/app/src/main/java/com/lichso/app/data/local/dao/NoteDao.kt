package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE title LIKE :datePrefix || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForDate(datePrefix: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAllNotesOnce(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
