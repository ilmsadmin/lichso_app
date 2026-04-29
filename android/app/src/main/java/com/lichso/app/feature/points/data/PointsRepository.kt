package com.lichso.app.feature.points.data

import com.lichso.app.data.local.dao.PointsDao
import com.lichso.app.data.local.dao.StreakDao
import com.lichso.app.data.local.dao.UnlockDao
import com.lichso.app.data.local.entity.ActionLogEntity
import com.lichso.app.data.local.entity.DailyUnlockEntity
import com.lichso.app.data.local.entity.PermanentUnlockEntity
import com.lichso.app.data.local.entity.PointsLedgerEntity
import com.lichso.app.data.local.entity.StreakRecordEntity
import com.lichso.app.feature.points.domain.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for PointsEngine data.
 *
 * All mutating calls go through [mutex] to avoid race conditions when multiple
 * actions come in simultaneously (e.g. user taps "xem thẻ" twice quickly).
 */
@Singleton
class PointsRepository @Inject constructor(
    private val pointsDao: PointsDao,
    private val unlockDao: UnlockDao,
    private val streakDao: StreakDao,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    // ── Observers ─────────────────────────────────────────────
    fun observeLedger(): Flow<PointsLedgerEntity?> = pointsDao.observeLedger()
    fun observeStreak(): Flow<StreakRecordEntity?> = streakDao.observeStreak()
    fun observeTodayUnlocks() = unlockDao.observeTodayUnlocks(clock.todayEpochDay())
    fun observePermanentUnlocks() = unlockDao.observeAllPermanent()
    fun observeDailyUnlocked(key: String) =
        unlockDao.observeDailyUnlocked(key, clock.todayEpochDay())
    fun observePermanentUnlocked(key: String) =
        unlockDao.observePermanentUnlocked(key)
    fun observeRecentLogs(days: Int = 30) =
        pointsDao.observeRecentLogs(clock.todayEpochDay() - days + 1)

    // ── Queries ───────────────────────────────────────────────
    suspend fun getOrCreateLedger(): PointsLedgerEntity = mutex.withLock {
        getOrCreateLedgerInternal()
    }

    suspend fun getOrCreateStreak(): StreakRecordEntity = mutex.withLock {
        getOrCreateStreakInternal()
    }

    suspend fun countActionToday(actionName: String): Int =
        pointsDao.countActionToday(actionName, clock.todayEpochDay())

    suspend fun isDailyUnlocked(key: String): Boolean =
        unlockDao.isDailyUnlocked(key, clock.todayEpochDay())

    suspend fun getPermanentUnlocks(): List<PermanentUnlockEntity> =
        unlockDao.getAllPermanent()

    // ── Mutations (always locked) ─────────────────────────────

    /** Rollover lifecycle: reset daily points at 00:00 if needed. Idempotent. */
    suspend fun rolloverIfNeeded(): PointsLedgerEntity = mutex.withLock {
        val ledger = getOrCreateLedgerInternal()
        val today = clock.todayEpochDay()
        if (ledger.lastResetEpochDay < today) {
            val updated = ledger.copy(
                dailyPoints = 0,
                spentDailyPoints = 0,
                lastResetEpochDay = today,
                updatedAt = clock.nowEpochMillis(),
            )
            pointsDao.upsertLedger(updated)
            // Prune very old logs (keep 90 days)
            pointsDao.pruneOldLogs(today - 90)
            updated
        } else ledger
    }

    /** Credit points + log the action. Assumes cap already checked by use case. */
    suspend fun creditPoints(
        actionName: String,
        dailyAwarded: Int,
        permanentAwarded: Int,
        multiplierApplied: Float,
        metadata: String? = null,
    ): PointsLedgerEntity = mutex.withLock {
        val ledger = getOrCreateLedgerInternal()
        val updated = ledger.copy(
            dailyPoints = ledger.dailyPoints + dailyAwarded,
            permanentPoints = ledger.permanentPoints + permanentAwarded,
            updatedAt = clock.nowEpochMillis(),
        )
        pointsDao.upsertLedger(updated)
        pointsDao.logAction(
            ActionLogEntity(
                actionType = actionName,
                dailyPointsAwarded = dailyAwarded,
                permanentPointsAwarded = permanentAwarded,
                streakMultiplierApplied = multiplierApplied,
                epochDay = clock.todayEpochDay(),
                timestamp = clock.nowEpochMillis(),
                metadata = metadata,
            )
        )
        updated
    }

    /** Debit daily points to unlock a daily feature. Returns false if not enough. */
    suspend fun spendDaily(key: String, cost: Int): Boolean = mutex.withLock {
        val ledger = getOrCreateLedgerInternal()
        if (ledger.dailyPoints < cost) return@withLock false
        if (unlockDao.isDailyUnlocked(key, clock.todayEpochDay())) return@withLock true
        val updated = ledger.copy(
            dailyPoints = ledger.dailyPoints - cost,
            spentDailyPoints = ledger.spentDailyPoints + cost,
            updatedAt = clock.nowEpochMillis(),
        )
        pointsDao.upsertLedger(updated)
        unlockDao.insertDailyUnlock(
            DailyUnlockEntity(
                unlockKey = key,
                epochDay = clock.todayEpochDay(),
                cost = cost,
                unlockedAt = clock.nowEpochMillis(),
            )
        )
        true
    }

    /** Record a permanent unlock (idempotent via REPLACE). */
    suspend fun grantPermanentUnlock(key: String, rankName: String) = mutex.withLock {
        unlockDao.insertPermanentUnlock(
            PermanentUnlockEntity(
                unlockKey = key,
                rank = rankName,
                unlockedAt = clock.nowEpochMillis(),
            )
        )
    }

    /** Update streak record (use by StreakUseCase). */
    suspend fun upsertStreak(entity: StreakRecordEntity) = mutex.withLock {
        streakDao.upsertStreak(entity)
    }

    // ── Internals (NO lock — callers already hold it) ─────────
    private suspend fun getOrCreateLedgerInternal(): PointsLedgerEntity {
        pointsDao.getLedger()?.let { return it }
        val fresh = PointsLedgerEntity(
            id = 1,
            dailyPoints = 0,
            spentDailyPoints = 0,
            permanentPoints = 0L,
            lastResetEpochDay = clock.todayEpochDay(),
            updatedAt = clock.nowEpochMillis(),
        )
        pointsDao.upsertLedger(fresh)
        return fresh
    }

    private suspend fun getOrCreateStreakInternal(): StreakRecordEntity {
        streakDao.getStreak()?.let { return it }
        val fresh = StreakRecordEntity(
            id = 1,
            currentStreak = 0,
            longestStreak = 0,
            lastCheckInEpochDay = 0L,
            freezeTokens = 0,
            lastFreezeGrantedMonth = 0,
            updatedAt = clock.nowEpochMillis(),
        )
        streakDao.upsertStreak(fresh)
        return fresh
    }
}

/** Map DB entity -> UI LedgerEntry. */
fun Flow<List<ActionLogEntity>>.mapToLedgerEntries():
    Flow<List<com.lichso.app.feature.points.domain.LedgerEntry>> =
    map { list ->
        list.map { e ->
            val action = runCatching {
                com.lichso.app.feature.points.domain.ActionType.valueOf(e.actionType)
            }.getOrNull()
            com.lichso.app.feature.points.domain.LedgerEntry(
                id = e.id,
                action = action,
                rawActionType = e.actionType,
                dailyPointsAwarded = e.dailyPointsAwarded,
                permanentPointsAwarded = e.permanentPointsAwarded,
                streakMultiplierApplied = e.streakMultiplierApplied,
                epochDay = e.epochDay,
                timestamp = e.timestamp,
                metadata = e.metadata,
            )
        }
    }
