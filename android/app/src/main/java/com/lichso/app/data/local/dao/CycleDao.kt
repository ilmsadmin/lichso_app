package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.CycleLogEntity
import com.lichso.app.data.local.entity.CycleSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {

    // ── Settings (singleton row id=1) ────────────────────────────────────────
    @Query("SELECT * FROM cycle_settings WHERE id = 1")
    fun observeSettings(): Flow<CycleSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: CycleSettingsEntity)

    // ── Cycle log ────────────────────────────────────────────────────────────
    @Query("SELECT * FROM cycle_logs ORDER BY startEpochDay DESC")
    fun observeLogs(): Flow<List<CycleLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CycleLogEntity): Long

    @Delete
    suspend fun deleteLog(log: CycleLogEntity)

    @Query("SELECT * FROM cycle_logs ORDER BY startEpochDay DESC LIMIT 1")
    suspend fun getLatestLog(): CycleLogEntity?
}
