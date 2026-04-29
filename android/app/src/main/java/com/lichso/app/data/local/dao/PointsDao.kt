package com.lichso.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lichso.app.data.local.entity.ActionLogEntity
import com.lichso.app.data.local.entity.PointsLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsDao {

    @Query("SELECT * FROM points_ledger WHERE id = 1")
    fun observeLedger(): Flow<PointsLedgerEntity?>

    @Query("SELECT * FROM points_ledger WHERE id = 1")
    suspend fun getLedger(): PointsLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLedger(entity: PointsLedgerEntity)

    @Insert
    suspend fun logAction(log: ActionLogEntity): Long

    @Query("""
        SELECT COUNT(*) FROM action_log
        WHERE actionType = :type AND epochDay = :day
    """)
    suspend fun countActionToday(type: String, day: Long): Int

    @Query("SELECT * FROM action_log WHERE epochDay >= :fromDay ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentLogs(fromDay: Long, limit: Int = 200): Flow<List<ActionLogEntity>>

    @Query("DELETE FROM action_log WHERE epochDay < :beforeDay")
    suspend fun pruneOldLogs(beforeDay: Long)
}
