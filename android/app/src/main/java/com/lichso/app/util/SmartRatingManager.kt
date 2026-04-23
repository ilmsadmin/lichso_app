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
    // 0=auto trigger gần nhất là skip, 1=user gửi feedback (cooldown dài hơn)
    private val KEY_LAST_OUTCOME       = intPreferencesKey("smart_rating_last_outcome")

    // ── Thresholds ──
    // Giảm ngưỡng để dễ trigger hơn (trước đây quá khắt khe → không user nào đạt).
    private const val MIN_ACTIONS_BEFORE_ASK  = 2     // chỉ cần 2 happy actions
    private const val MIN_DAYS_FIRST_ASK      = 1L    // mở app ≥ 1 ngày sau cài là đủ
    private const val MIN_DAYS_AFTER_SKIP     = 5L    // user skip → hỏi lại sau 5 ngày
    private const val MIN_DAYS_AFTER_FEEDBACK = 30L   // user gửi feedback → 30 ngày mới hỏi lại
    private const val MAX_TIMES_TO_ASK        = 6     // tối đa 6 lần (~1 năm với cooldown 5 ngày)

    // ── Observable state for Compose ──
    private val _shouldShow = MutableStateFlow(false)
    val shouldShow: StateFlow<Boolean> = _shouldShow.asStateFlow()

    /**
     * True khi dialog đang được mở thủ công từ Settings — KHÔNG ghi recordShown
     * (không tăng timesAsked, không reset action count). Tránh việc user bấm thử
     * vài lần trong Settings là cạn quota auto-trigger vĩnh viễn.
     */
    @Volatile
    var isManualTrigger: Boolean = false
        private set

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
        val lastOutcome = prefs[KEY_LAST_OUTCOME] ?: 0

        // Đã đánh giá (chọn 4-5 sao + xác nhận) → KHÔNG BAO GIỜ hỏi nữa
        if (userRated == 1) return
        // Đã hỏi quá nhiều lần (kể cả khi user toàn skip) → dừng để không làm phiền
        if (timesAsked >= MAX_TIMES_TO_ASK) return

        // Chưa đủ happy actions
        if (actionCount < MIN_ACTIONS_BEFORE_ASK) return

        // Cooldown:
        //  - Lần đầu (lastAskedTime = 0): chỉ cần đủ MIN_DAYS_FIRST_ASK ngày
        //    (tính từ lần đầu happy action — gần như là install date thực tế)
        //  - Sau khi user gửi feedback: chờ MIN_DAYS_AFTER_FEEDBACK (lâu hơn)
        //  - Sau khi user skip: chờ MIN_DAYS_AFTER_SKIP (ngắn hơn — vì có thể họ
        //    chỉ đang bận, không có nghĩa là không thích app)
        if (lastAskedTime > 0L) {
            val daysSinceLastAsked =
                (System.currentTimeMillis() - lastAskedTime) / (1000L * 60 * 60 * 24)
            val cooldown = if (lastOutcome == 1) MIN_DAYS_AFTER_FEEDBACK else MIN_DAYS_AFTER_SKIP
            if (daysSinceLastAsked < cooldown) return
        }
        // Lần đầu: không có lastAskedTime, dùng MIN_DAYS_FIRST_ASK gián tiếp qua
        // số lượng happy actions đã tích luỹ (≥ MIN_ACTIONS_BEFORE_ASK đã check ở trên).

        // Đủ điều kiện → show dialog
        triggerAuto()
    }

    /**
     * Trigger thủ công — dùng khi user bấm "Đánh giá ứng dụng" trong sidebar.
     * Bỏ qua mọi điều kiện và KHÔNG đếm vào quota auto-trigger.
     */
    fun triggerManually() {
        isManualTrigger = true
        _shouldShow.value = true
    }

    /**
     * Auto-trigger từ checkAndTrigger (sau happy action). Đếm vào quota.
     */
    private fun triggerAuto() {
        isManualTrigger = false
        _shouldShow.value = true
    }

    /**
     * Ghi nhận đã hỏi lần này (trước khi show dialog).
     * Chỉ lưu prefs để tính cooldown — KHÔNG set shouldShow = false ở đây.
     * Skip nếu là manual trigger (bấm từ Settings).
     */
    suspend fun recordShown(context: Context) {
        if (isManualTrigger) return
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
     * User chọn "Hài lòng" → ghi nhận rated. Sau đó sẽ KHÔNG BAO GIỜ hỏi lại.
     */
    suspend fun recordRated(context: Context) {
        context.settingsDataStore.edit { p ->
            p[KEY_USER_RATED] = 1
        }
        isManualTrigger = false
        _shouldShow.value = false
    }

    /**
     * User đã gửi feedback (1-3 sao hoặc "Chưa hài lòng") — đánh dấu cooldown DÀI
     * (30 ngày) để không làm phiền user vừa phàn nàn.
     */
    suspend fun recordFeedbackSent(context: Context) {
        context.settingsDataStore.edit { p ->
            p[KEY_LAST_OUTCOME] = 1
            p[KEY_LAST_ASKED_TIME] = System.currentTimeMillis()
        }
        isManualTrigger = false
        _shouldShow.value = false
    }

    /**
     * User bỏ qua / đóng dialog mà không tương tác — cooldown NGẮN (5 ngày)
     * vì có thể chỉ là họ đang bận, không có nghĩa là ghét app.
     */
    suspend fun recordSkipped(context: Context) {
        context.settingsDataStore.edit { p ->
            p[KEY_LAST_OUTCOME] = 0
            // Nếu đây là auto-trigger (không phải manual từ Settings), recordShown
            // đã set lastAskedTime rồi, nhưng set lại cho chắc chắn.
            p[KEY_LAST_ASKED_TIME] = System.currentTimeMillis()
        }
        isManualTrigger = false
        _shouldShow.value = false
    }

    /**
     * @deprecated Dùng [recordSkipped] (suspend, có context) hoặc [dismissNoCooldown] thay thế.
     * Hàm này chỉ ẩn dialog ngay lập tức KHÔNG cập nhật cooldown — gọi từ chỗ
     * không có CoroutineScope tiện. Vẫn giữ để tương thích code cũ.
     */
    fun dismiss() {
        isManualTrigger = false
        _shouldShow.value = false
    }
}
