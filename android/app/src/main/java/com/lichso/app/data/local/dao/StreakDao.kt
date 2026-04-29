package com.lichso.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lichso.app.data.local.entity.StreakRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_record WHERE id = 1")
    fun observeStreak(): Flow<StreakRecordEntity?>

    @Query("SELECT * FROM streak_record WHERE id = 1")
    suspend fun getStreak(): StreakRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(entity: StreakRecordEntity)
}
