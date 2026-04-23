package com.lichso.app.feature.points.domain

/** UI-facing balance snapshot. */
data class PointsBalance(
    val daily: Int,
    val spentDaily: Int,
    val permanent: Long,
    val rank: PermanentRank,
    val nextRank: PermanentRank?,
    val progressToNextRank: Float,            // 0f..1f
    val pointsToNextRank: Long,
)

/** UI-facing streak state. */
data class StreakState(
    val current: Int,
    val longest: Int,
    val tier: StreakTier,
    val nextTier: StreakTier?,
    val freezeTokens: Int,
    val missedToday: Boolean,
    val daysToNextTier: Int?,
)

/** Daily unlock UI state. */
data class DailyUnlockState(
    val key: DailyUnlockKey,
    val isUnlocked: Boolean,
    val cost: Int,
    val currentDaily: Int,
    val canAfford: Boolean,
    val shortestPath: List<ActionSuggestion>,
)

/** One action suggestion — "rút quẻ +15⚡ → lichso://oracle". */
data class ActionSuggestion(
    val action: ActionType,
    val potentialGain: Int,
    val label: String,
    val deeplink: String,
)

/** Immutable log entry shown in Ledger screen. */
data class LedgerEntry(
    val id: Long,
    val action: ActionType?,
    val rawActionType: String,
    val dailyPointsAwarded: Int,
    val permanentPointsAwarded: Int,
    val streakMultiplierApplied: Float,
    val epochDay: Long,
    val timestamp: Long,
    val metadata: String?,
)

/** Result of awarding points for an action. */
sealed interface AwardResult {
    data class Success(
        val action: ActionType,
        val dailyAwarded: Int,
        val permanentAwarded: Long,
        val multiplier: Float,
        val newRankUnlocked: PermanentRank?,
        val newPermanentUnlocks: List<PermanentUnlockKey>,
    ) : AwardResult
    data object CappedForToday : AwardResult
    data object Disabled : AwardResult
}

/** Result of spending daily points to unlock a feature for today. */
sealed interface SpendResult {
    data object Success : SpendResult
    data class InsufficientPoints(val needed: Int, val have: Int) : SpendResult
    data object AlreadyUnlocked : SpendResult
}

/** Result of daily check-in. */
sealed interface CheckInResult {
    data class Success(
        val newStreak: Int,
        val tier: StreakTier,
        val tierUp: Boolean,
        val award: AwardResult,
    ) : CheckInResult
    data object AlreadyCheckedIn : CheckInResult
}
