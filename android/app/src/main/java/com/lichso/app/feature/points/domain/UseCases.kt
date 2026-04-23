package com.lichso.app.feature.points.domain

import com.lichso.app.feature.points.data.PointsRepository
import kotlin.math.roundToInt
import javax.inject.Inject

/**
 * Award points for a given [ActionType], applying:
 *   1. Daily reset if needed
 *   2. Daily cap check (ActionType.dailyCap)
 *   3. Streak multiplier
 *   4. Detecting rank-up → permanent unlocks
 */
class AwardPointsUseCase @Inject constructor(
    private val repo: PointsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        action: ActionType,
        metadata: String? = null,
    ): AwardResult {
        repo.rolloverIfNeeded()

        // 1. Cap check
        if (action.dailyCap != -1) {
            val count = repo.countActionToday(action.name)
            if (count >= action.dailyCap) return AwardResult.CappedForToday
        }

        // 2. Streak multiplier (read-only — no mutation here)
        val streak = repo.getOrCreateStreak()
        val tier = StreakTier.fromStreak(streak.currentStreak)
        val dailyAwarded = (action.dailyPoints * tier.dailyMultiplier).roundToInt()
        val permanentAwarded = (action.permanentPoints * tier.permanentMultiplier).roundToInt()

        if (dailyAwarded == 0 && permanentAwarded == 0) {
            // Still log a 0-award? Skip.
            return AwardResult.Disabled
        }

        val oldLedger = repo.getOrCreateLedger()
        val oldRank = PermanentRank.fromTotal(oldLedger.permanentPoints)

        val newLedger = repo.creditPoints(
            actionName = action.name,
            dailyAwarded = dailyAwarded,
            permanentAwarded = permanentAwarded,
            multiplierApplied = tier.dailyMultiplier,
            metadata = metadata,
        )

        val newRank = PermanentRank.fromTotal(newLedger.permanentPoints)
        val newUnlocks = mutableListOf<PermanentUnlockKey>()
        var ranked: PermanentRank? = null
        if (newRank != oldRank && newRank.ordinal > oldRank.ordinal) {
            ranked = newRank
            // Grant ALL unlocks between oldRank+1 .. newRank (in case user jumped 2 ranks).
            for (i in (oldRank.ordinal + 1)..newRank.ordinal) {
                val rank = PermanentRank.entries[i]
                rank.unlocks.forEach { key ->
                    repo.grantPermanentUnlock(key.name, rank.name)
                    newUnlocks += key
                }
            }
        }

        return AwardResult.Success(
            action = action,
            dailyAwarded = dailyAwarded,
            permanentAwarded = permanentAwarded.toLong(),
            multiplier = tier.dailyMultiplier,
            newRankUnlocked = ranked,
            newPermanentUnlocks = newUnlocks,
        )
    }
}

/** Spend ⚡ daily points to unlock a feature for today. */
class SpendDailyPointsUseCase @Inject constructor(
    private val repo: PointsRepository,
) {
    suspend operator fun invoke(key: DailyUnlockKey): SpendResult {
        repo.rolloverIfNeeded()
        if (repo.isDailyUnlocked(key.name)) return SpendResult.AlreadyUnlocked
        val ledger = repo.getOrCreateLedger()
        if (ledger.dailyPoints < key.cost) {
            return SpendResult.InsufficientPoints(
                needed = key.cost - ledger.dailyPoints,
                have = ledger.dailyPoints,
            )
        }
        val ok = repo.spendDaily(key.name, key.cost)
        return if (ok) SpendResult.Success
        else SpendResult.InsufficientPoints(key.cost, ledger.dailyPoints)
    }
}

/**
 * Daily check-in — updates streak, grants check-in points, may trigger
 * streak-milestone permanent awards.
 */
class DailyCheckInUseCase @Inject constructor(
    private val repo: PointsRepository,
    private val awardPoints: AwardPointsUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(): CheckInResult {
        repo.rolloverIfNeeded()
        val today = clock.todayEpochDay()
        val streak = repo.getOrCreateStreak()

        if (streak.lastCheckInEpochDay == today) return CheckInResult.AlreadyCheckedIn

        val daysSinceLast = today - streak.lastCheckInEpochDay
        val continuing = streak.lastCheckInEpochDay > 0 && daysSinceLast == 1L
        val canFreeze = !continuing && streak.lastCheckInEpochDay > 0 &&
                daysSinceLast in 2..2 && streak.freezeTokens > 0

        val oldTier = StreakTier.fromStreak(streak.currentStreak)
        val newStreakCount = when {
            continuing -> streak.currentStreak + 1
            canFreeze -> streak.currentStreak + 1          // freeze saved it
            else -> 1                                       // reset
        }
        val usedFreeze = canFreeze

        val newTier = StreakTier.fromStreak(newStreakCount)
        val tierUp = newTier.ordinal > oldTier.ordinal

        // Free freeze token: 1 per calendar month, max 3.
        val ym = clock.yearMonth()
        val grantedFreeze = ym > streak.lastFreezeGrantedMonth && streak.freezeTokens < 3
        val newFreeze = (streak.freezeTokens + (if (grantedFreeze) 1 else 0) - (if (usedFreeze) 1 else 0))
            .coerceAtLeast(0)

        val updated = streak.copy(
            currentStreak = newStreakCount,
            longestStreak = maxOf(streak.longestStreak, newStreakCount),
            lastCheckInEpochDay = today,
            freezeTokens = newFreeze,
            lastFreezeGrantedMonth = if (grantedFreeze) ym else streak.lastFreezeGrantedMonth,
            updatedAt = clock.nowEpochMillis(),
        )
        repo.upsertStreak(updated)

        // Award base check-in points
        val award = awardPoints(ActionType.DAILY_CHECK_IN)

        // Milestone actions — one-shot per streak
        when (newStreakCount) {
            7 -> awardPoints(ActionType.STREAK_7_DAYS)
            30 -> awardPoints(ActionType.STREAK_30_DAYS)
            100 -> awardPoints(ActionType.STREAK_100_DAYS)
            365 -> awardPoints(ActionType.STREAK_365_DAYS)
        }

        return CheckInResult.Success(
            newStreak = newStreakCount,
            tier = newTier,
            tierUp = tierUp,
            award = award,
        )
    }
}

/** Build PointsBalance UI model from a ledger row. */
fun PointsBalanceMapper(ledger: com.lichso.app.data.local.entity.PointsLedgerEntity?): PointsBalance {
    val daily = ledger?.dailyPoints ?: 0
    val spent = ledger?.spentDailyPoints ?: 0
    val perm = ledger?.permanentPoints ?: 0L
    val rank = PermanentRank.fromTotal(perm)
    val next = PermanentRank.nextOf(rank)
    val progress = if (next != null) {
        val span = (next.threshold - rank.threshold).coerceAtLeast(1)
        ((perm - rank.threshold).toFloat() / span).coerceIn(0f, 1f)
    } else 1f
    val toNext = next?.let { it.threshold - perm } ?: 0L
    return PointsBalance(
        daily = daily,
        spentDaily = spent,
        permanent = perm,
        rank = rank,
        nextRank = next,
        progressToNextRank = progress,
        pointsToNextRank = toNext,
    )
}

/** Build StreakState UI model. */
fun StreakStateMapper(
    entity: com.lichso.app.data.local.entity.StreakRecordEntity?,
    today: Long,
): StreakState {
    val e = entity
    val cur = e?.currentStreak ?: 0
    val tier = StreakTier.fromStreak(cur)
    val next = StreakTier.nextOf(tier)
    val daysToNext = next?.let { (it.minDays - cur).coerceAtLeast(0) }
    val missed = (e?.lastCheckInEpochDay ?: 0L) < today
    return StreakState(
        current = cur,
        longest = e?.longestStreak ?: 0,
        tier = tier,
        nextTier = next,
        freezeTokens = e?.freezeTokens ?: 0,
        missedToday = missed,
        daysToNextTier = daysToNext,
    )
}
