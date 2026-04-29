package com.lichso.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * PointsEngine v2 entities.
 * See `docs/POINTS_ENGINE_DATA_MODEL.md` for full spec.
 */

/** Balance hiện tại — 1 row duy nhất (id = 1). */
@Entity(tableName = "points_ledger")
data class PointsLedgerEntity(
    @PrimaryKey val id: Int = 1,
    val dailyPoints: Int = 0,
    val spentDailyPoints: Int = 0,
    val permanentPoints: Long = 0L,
    val lastResetEpochDay: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Mỗi action trao điểm được ghi 1 row (phục vụ daily cap + analytics). */
@Entity(
    tableName = "action_log",
    indices = [Index(value = ["actionType", "epochDay"])]
)
data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,
    val dailyPointsAwarded: Int,
    val permanentPointsAwarded: Int,
    val streakMultiplierApplied: Float,
    val epochDay: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null,
)

/** Tính năng đã mở trong NGÀY — compound key (unlockKey + epochDay). */
@Entity(
    tableName = "daily_unlock",
    primaryKeys = ["unlockKey", "epochDay"]
)
data class DailyUnlockEntity(
    val unlockKey: String,
    val epochDay: Long,
    val cost: Int,
    val unlockedAt: Long = System.currentTimeMillis(),
)

/** Tính năng đã mở VĨNH VIỄN. */
@Entity(tableName = "permanent_unlock")
data class PermanentUnlockEntity(
    @PrimaryKey val unlockKey: String,
    val rank: String,
    val unlockedAt: Long = System.currentTimeMillis(),
)

/** Streak singleton (id = 1). */
@Entity(tableName = "streak_record")
data class StreakRecordEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckInEpochDay: Long = 0L,
    val freezeTokens: Int = 0,
    val lastFreezeGrantedMonth: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)
