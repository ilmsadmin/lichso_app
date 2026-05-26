package com.lichso.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lichso.app.data.local.entity.QuizOfflineSessionEntity

@Dao
interface QuizOfflineSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuizOfflineSessionEntity)

    @Query("SELECT * FROM quiz_offline_sessions ORDER BY finishedAtMs ASC")
    suspend fun getAllOnce(): List<QuizOfflineSessionEntity>

    @Query("DELETE FROM quiz_offline_sessions WHERE clientSessionId IN (:clientSessionIds)")
    suspend fun deleteByClientSessionIds(clientSessionIds: List<String>)

    @Delete
    suspend fun delete(entity: QuizOfflineSessionEntity)
}
