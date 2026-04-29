package com.lichso.app.feature.points.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.zodiacStore: DataStore<Preferences> by preferencesDataStore(name = "zodiac_collection")

/**
 * DataStore-backed persistence cho bộ sưu tập 12 con giáp.
 * - Lưu danh sách card id đã sưu tầm (CSV).
 * - Lưu count mỗi card (đếm số lần đã rút trùng).
 * - Lưu epochDay rút lần cuối để gate "1 lần/ngày".
 */
@Singleton
class ZodiacCollectionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_COLLECTED_IDS = stringPreferencesKey("collected_ids")        // "TY_COMMON,SUU_RARE"
        private val KEY_TOTAL_DRAWS = intPreferencesKey("total_draws")
        private val KEY_LAST_DRAW_EPOCH_DAY = longPreferencesKey("last_draw_epoch_day")
        private val KEY_FULL_SET_BONUS_AWARDED = intPreferencesKey("full_set_bonus_awarded") // 0/1
    }

    val collectedIds: Flow<Set<String>> = context.zodiacStore.data.map {
        (it[KEY_COLLECTED_IDS] ?: "")
            .split(",")
            .filter { id -> id.isNotBlank() }
            .toSet()
    }

    val totalDraws: Flow<Int> = context.zodiacStore.data.map { it[KEY_TOTAL_DRAWS] ?: 0 }

    val lastDrawEpochDay: Flow<Long> = context.zodiacStore.data.map { it[KEY_LAST_DRAW_EPOCH_DAY] ?: 0L }

    val fullSetBonusAwarded: Flow<Boolean> = context.zodiacStore.data.map {
        (it[KEY_FULL_SET_BONUS_AWARDED] ?: 0) == 1
    }

    suspend fun snapshotCollected(): Set<String> = collectedIds.first()
    suspend fun snapshotLastDrawEpochDay(): Long = lastDrawEpochDay.first()
    suspend fun snapshotTotalDraws(): Int = totalDraws.first()
    suspend fun snapshotFullSetAwarded(): Boolean = fullSetBonusAwarded.first()

    suspend fun recordDraw(cardId: String, epochDay: Long) {
        context.zodiacStore.edit { prefs ->
            val current = (prefs[KEY_COLLECTED_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            current += cardId
            prefs[KEY_COLLECTED_IDS] = current.joinToString(",")
            prefs[KEY_TOTAL_DRAWS] = (prefs[KEY_TOTAL_DRAWS] ?: 0) + 1
            prefs[KEY_LAST_DRAW_EPOCH_DAY] = epochDay
        }
    }

    suspend fun markFullSetBonusAwarded() {
        context.zodiacStore.edit { it[KEY_FULL_SET_BONUS_AWARDED] = 1 }
    }
}
