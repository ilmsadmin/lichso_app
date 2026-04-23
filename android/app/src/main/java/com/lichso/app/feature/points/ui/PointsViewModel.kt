package com.lichso.app.feature.points.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.feature.points.data.PointsRepository
import com.lichso.app.feature.points.data.mapToLedgerEntries
import com.lichso.app.feature.points.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for the entire PointsEngine surface:
 *  - HomePointsPill
 *  - DailyUnlockStoreScreen
 *  - LedgerScreen
 *  - OracleDrawScreen
 *  - RankUpDialog
 *
 * Scoped with @HiltViewModel so every screen obtaining it via hiltViewModel()
 * shares state within the Activity.
 */
@HiltViewModel
class PointsViewModel @Inject constructor(
    private val repo: PointsRepository,
    private val clock: Clock,
    private val awardPoints: AwardPointsUseCase,
    private val spendDailyPoints: SpendDailyPointsUseCase,
    private val dailyCheckIn: DailyCheckInUseCase,
) : ViewModel() {

    val balance: StateFlow<PointsBalance> = repo.observeLedger()
        .map { PointsBalanceMapper(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PointsBalanceMapper(null))

    val streak: StateFlow<StreakState> = repo.observeStreak()
        .map { StreakStateMapper(it, clock.todayEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StreakStateMapper(null, clock.todayEpochDay()))

    val todayUnlocks: StateFlow<Set<String>> = repo.observeTodayUnlocks()
        .map { list -> list.map { it.unlockKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val permanentUnlocks: StateFlow<Set<String>> = repo.observePermanentUnlocks()
        .map { list -> list.map { it.unlockKey }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val recentLogs: StateFlow<List<LedgerEntry>> = repo.observeRecentLogs(30)
        .mapToLedgerEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── One-shot events (rank-up, insufficient points, etc) ──
    private val _events = MutableSharedFlow<PointsEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<PointsEvent> = _events

    init {
        viewModelScope.launch { repo.rolloverIfNeeded() }
    }

    // ── Intents ──
    fun award(action: ActionType, metadata: String? = null) {
        viewModelScope.launch {
            val r = awardPoints(action, metadata)
            if (r is AwardResult.Success && r.newRankUnlocked != null) {
                _events.emit(PointsEvent.RankUp(r.newRankUnlocked, r.newPermanentUnlocks))
            }
        }
    }

    fun checkInToday() {
        viewModelScope.launch {
            val r = dailyCheckIn()
            if (r is CheckInResult.Success) {
                val award = r.award
                if (award is AwardResult.Success && award.newRankUnlocked != null) {
                    _events.emit(PointsEvent.RankUp(award.newRankUnlocked, award.newPermanentUnlocks))
                }
                _events.emit(PointsEvent.CheckedIn(r.newStreak, r.tier, r.tierUp))
            }
        }
    }

    fun spendDaily(key: DailyUnlockKey, onResult: (SpendResult) -> Unit = {}) {
        viewModelScope.launch {
            val r = spendDailyPoints(key)
            onResult(r)
            when (r) {
                SpendResult.Success -> _events.emit(PointsEvent.Unlocked(key))
                is SpendResult.InsufficientPoints ->
                    _events.emit(PointsEvent.NeedMorePoints(key, r.needed))
                SpendResult.AlreadyUnlocked -> Unit
            }
        }
    }
}

sealed interface PointsEvent {
    data class RankUp(
        val rank: PermanentRank,
        val unlocks: List<PermanentUnlockKey>,
    ) : PointsEvent
    data class CheckedIn(val streak: Int, val tier: StreakTier, val tierUp: Boolean) : PointsEvent
    data class Unlocked(val key: DailyUnlockKey) : PointsEvent
    data class NeedMorePoints(val key: DailyUnlockKey, val short: Int) : PointsEvent
}
