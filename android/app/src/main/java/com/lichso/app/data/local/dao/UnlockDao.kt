package com.lichso.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lichso.app.data.local.entity.DailyUnlockEntity
import com.lichso.app.data.local.entity.PermanentUnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {

    // ── Daily ─────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyUnlock(entity: DailyUnlockEntity): Long

    @Query("""
        SELECT EXISTS(SELECT 1 FROM daily_unlock
        WHERE unlockKey = :key AND epochDay = :day)
    """)
    suspend fun isDailyUnlocked(key: String, day: Long): Boolean

    @Query("""
        SELECT EXISTS(SELECT 1 FROM daily_unlock
        WHERE unlockKey = :key AND epochDay = :day)
    """)
    fun observeDailyUnlocked(key: String, day: Long): Flow<Boolean>

    @Query("SELECT * FROM daily_unlock WHERE epochDay = :day")
    fun observeTodayUnlocks(day: Long): Flow<List<DailyUnlockEntity>>

    // ── Permanent ─────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermanentUnlock(entity: PermanentUnlockEntity)

    @Query("SELECT * FROM permanent_unlock")
    fun observeAllPermanent(): Flow<List<PermanentUnlockEntity>>

    @Query("SELECT * FROM permanent_unlock")
    suspend fun getAllPermanent(): List<PermanentUnlockEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM permanent_unlock WHERE unlockKey = :key)")
    fun observePermanentUnlocked(key: String): Flow<Boolean>
}
