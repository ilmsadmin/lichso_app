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

    @Query("SELECT * FROM cycle_settings WHERE id = 1")
    suspend fun getSettingsOnce(): CycleSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: CycleSettingsEntity)

    // ── Cycle log ────────────────────────────────────────────────────────────
    @Query("SELECT * FROM cycle_logs ORDER BY startEpochDay DESC")
    fun observeLogs(): Flow<List<CycleLogEntity>>

    @Query("SELECT * FROM cycle_logs ORDER BY startEpochDay DESC")
    suspend fun getAllLogsOnce(): List<CycleLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CycleLogEntity): Long

    @Delete
    suspend fun deleteLog(log: CycleLogEntity)

    @Query("DELETE FROM cycle_settings")
    suspend fun clearSettings()

    @Query("DELETE FROM cycle_logs")
    suspend fun clearLogs()

    suspend fun deleteAll() { clearSettings(); clearLogs() }

    @Query("SELECT * FROM cycle_logs ORDER BY startEpochDay DESC LIMIT 1")
    suspend fun getLatestLog(): CycleLogEntity?
}
