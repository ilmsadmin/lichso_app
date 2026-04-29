package com.lichso.app.feature.points.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.feature.points.data.ZodiacCollectionStore
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.feature.points.domain.AwardPointsUseCase
import com.lichso.app.feature.points.domain.Clock
import com.lichso.app.feature.points.domain.DailyUnlockKey
import com.lichso.app.feature.points.domain.SpendDailyPointsUseCase
import com.lichso.app.feature.points.domain.SpendResult
import com.lichso.app.feature.points.domain.ZodiacCard
import com.lichso.app.feature.points.domain.ZodiacDeck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn Bộ sưu tập 12 con giáp.
 *  - Tiêu ⚡ DAILY_ZODIAC_CARD (20⚡) → rút 1 thẻ/ngày.
 *  - Đủ bộ 12 sign → award CHECKIN_TEMPLE (50☯️).
 */
@HiltViewModel
class ZodiacCollectionViewModel @Inject constructor(
    private val store: ZodiacCollectionStore,
    private val spendDailyPoints: SpendDailyPointsUseCase,
    private val awardPoints: AwardPointsUseCase,
    private val clock: Clock,
) : ViewModel() {

    val collected: StateFlow<Set<String>> = store.collectedIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val totalDraws: StateFlow<Int> = store.totalDraws
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val lastDrawEpochDay: StateFlow<Long> = store.lastDrawEpochDay
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    private val _lastDrawn = MutableStateFlow<ZodiacCard?>(null)
    val lastDrawn: StateFlow<ZodiacCard?> = _lastDrawn.asStateFlow()

    private val _events = MutableSharedFlow<ZodiacEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<ZodiacEvent> = _events

    fun tryDraw() {
        viewModelScope.launch {
            val today = clock.todayEpochDay()
            val lastDrawDay = store.snapshotLastDrawEpochDay()
            if (lastDrawDay >= today) {
                _events.emit(ZodiacEvent.AlreadyDrawnToday)
                return@launch
            }
            val r = spendDailyPoints(DailyUnlockKey.DAILY_ZODIAC_CARD)
            when (r) {
                SpendResult.Success, SpendResult.AlreadyUnlocked -> {
                    val seed = today * 31L + (clock.nowEpochMillis() and 0xFFFF)
                    val card = ZodiacDeck.drawRandom(seed = seed)
                    store.recordDraw(card.id, today)
                    _lastDrawn.value = card
                    _events.emit(ZodiacEvent.Drawn(card))

                    val newSet = store.snapshotCollected()
                    if (ZodiacDeck.isFullSet(newSet) && !store.snapshotFullSetAwarded()) {
                        store.markFullSetBonusAwarded()
                        awardPoints(ActionType.CHECKIN_TEMPLE, "zodiac_full_set_bonus")
                        _events.emit(ZodiacEvent.FullSetCompleted)
                    }
                }
                is SpendResult.InsufficientPoints -> {
                    _events.emit(ZodiacEvent.NotEnoughPoints(needed = r.needed))
                }
            }
        }
    }
}

sealed interface ZodiacEvent {
    data object AlreadyDrawnToday : ZodiacEvent
    data class Drawn(val card: ZodiacCard) : ZodiacEvent
    data class NotEnoughPoints(val needed: Int) : ZodiacEvent
    data object FullSetCompleted : ZodiacEvent
}
