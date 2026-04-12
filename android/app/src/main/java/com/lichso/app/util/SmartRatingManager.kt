package com.lichso.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * SmartRatingManager — Singleton quản lý logic "xin đánh giá thông minh"
 *
 * Chiến lược:
 * - Trigger sau khi user hoàn thành một "happy action" (bookmark, lưu gia phả, lưu văn khấn, v.v.)
 * - Chỉ hỏi sau khi user đã thực hiện đủ [MIN_ACTIONS_BEFORE_ASK] happy actions
 * - Không hỏi lại trong vòng [MIN_DAYS_BETWEEN_ASKS] ngày
 * - Tối đa [MAX_TIMES_TO_ASK] lần, sau đó dừng hẳn (tránh làm phiền)
 *
 * Luồng:
 * - Hài lòng (👍) → Google Play In-App Review
 * - Không hài lòng (👎) → Mở mailto gửi feedback tới zenixhq.com@gmail.com
 */
object SmartRatingManager {

    // ── Prefs keys ──
    private val KEY_HAPPY_ACTION_COUNT = intPreferencesKey("smart_rating_happy_action_count")
    private val KEY_LAST_ASKED_TIME    = longPreferencesKey("smart_rating_last_asked_time")
    private val KEY_TIMES_ASKED        = intPreferencesKey("smart_rating_times_asked")
    private val KEY_USER_RATED         = intPreferencesKey("smart_rating_user_rated") // 0=not yet, 1=rated, -1=declined

    // ── Thresholds ──
    private const val MIN_ACTIONS_BEFORE_ASK  = 3    // số happy actions tối thiểu trước khi hỏi
    private const val MIN_DAYS_BETWEEN_ASKS   = 14   // chờ ít nhất 14 ngày giữa 2 lần hỏi
    private const val MAX_TIMES_TO_ASK        = 3    // hỏi tối đa 3 lần tổng cộng

    // ── Observable state for Compose ──
    private val _shouldShow = MutableStateFlow(false)
    val shouldShow: StateFlow<Boolean> = _shouldShow.asStateFlow()

    /**
     * Gọi sau khi user hoàn thành một "happy action" — ứng dụng làm người dùng vui.
     * Ví dụ: lưu bookmark, lưu thành viên gia phả, lưu văn khấn yêu thích, v.v.
     *
     * @param context Android context
     * @param actionWeight số điểm tăng thêm (mặc định 1, actions quan trọng hơn có thể dùng 2)
     */
    suspend fun recordHappyAction(context: Context, actionWeight: Int = 1) {
        val prefs = context.settingsDataStore.data.first()
        val timesAsked = prefs[KEY_TIMES_ASKED] ?: 0
        val userRated = prefs[KEY_USER_RATED] ?: 0

        // Đừng hỏi nữa nếu đã hỏi đủ lần hoặc user đã đánh giá/từ chối dứt khoát
        if (timesAsked >= MAX_TIMES_TO_ASK || userRated == 1) return

        context.settingsDataStore.edit { p ->
            val current = p[KEY_HAPPY_ACTION_COUNT] ?: 0
            p[KEY_HAPPY_ACTION_COUNT] = current + actionWeight
        }

        checkAndTrigger(context)
    }

    /**
     * Kiểm tra điều kiện và trigger dialog nếu đủ.
     */
    suspend fun checkAndTrigger(context: Context) {
        val prefs = context.settingsDataStore.data.first()
        val actionCount = prefs[KEY_HAPPY_ACTION_COUNT] ?: 0
        val lastAskedTime = prefs[KEY_LAST_ASKED_TIME] ?: 0L
        val timesAsked = prefs[KEY_TIMES_ASKED] ?: 0
        val userRated = prefs[KEY_USER_RATED] ?: 0

        // Đã đánh giá → không hỏi nữa
        if (userRated == 1 || timesAsked >= MAX_TIMES_TO_ASK) return

        // Chưa đủ happy actions
        if (actionCount < MIN_ACTIONS_BEFORE_ASK) return

        // Quá gần lần hỏi trước
        val daysSinceLastAsked = if (lastAskedTime == 0L) Long.MAX_VALUE
        else (System.currentTimeMillis() - lastAskedTime) / (1000L * 60 * 60 * 24)

        if (daysSinceLastAsked < MIN_DAYS_BETWEEN_ASKS) return

        // Đủ điều kiện → show dialog
        _shouldShow.value = true
    }

    /**
     * Trigger thủ công — dùng khi user bấm "Đánh giá ứng dụng" trong sidebar.
     * Bỏ qua mọi điều kiện.
     */
    fun triggerManually() {
        _shouldShow.value = true
    }

    /**
     * Ghi nhận đã hỏi lần này (trước khi show dialog).
     * Chỉ lưu prefs để tính cooldown — KHÔNG set shouldShow = false ở đây.
     */
    suspend fun recordShown(context: Context) {
        context.settingsDataStore.edit { p ->
            val current = p[KEY_TIMES_ASKED] ?: 0
            p[KEY_TIMES_ASKED] = current + 1
            p[KEY_LAST_ASKED_TIME] = System.currentTimeMillis()
            // Reset action count để không trigger lại ngay sau này
            p[KEY_HAPPY_ACTION_COUNT] = 0
        }
        // Không set _shouldShow.value = false ở đây — dialog tự quản lý việc đóng
    }

    /**
     * User chọn "Hài lòng" → ghi nhận rated
     */
    suspend fun recordRated(context: Context) {
        context.settingsDataStore.edit { p ->
            p[KEY_USER_RATED] = 1
        }
        _shouldShow.value = false
    }

    /**
     * User đã gửi feedback / bỏ qua → chỉ dismiss, có thể hỏi lại sau
     */
    fun dismiss() {
        _shouldShow.value = false
    }
}
