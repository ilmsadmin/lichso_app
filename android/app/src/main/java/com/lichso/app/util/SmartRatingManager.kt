package com.lichso.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.android.play.core.review.ReviewManagerFactory
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * SmartRatingManager — Quản lý logic "xin đánh giá in-app".
 *
 * Triết lý đơn giản:
 *  - Sau N happy action + cooldown đủ lâu → bật dialog 1-5 sao.
 *  - 1-3 sao → mở email feedback (đang hoạt động tốt, giữ nguyên).
 *  - 4-5 sao → gọi [launchInAppReview] để hiển thị Google Play In-App Review,
 *    fallback mở Play Store nếu API không sẵn sàng.
 *
 * Không có "EmotionStep" emoji dư thừa, không có 2 hệ thống cooldown song song.
 */
object SmartRatingManager {

    // ── DataStore keys ──
    private val KEY_HAPPY_ACTION_COUNT = intPreferencesKey("smart_rating_happy_action_count")
    private val KEY_LAST_ASKED_TIME    = longPreferencesKey("smart_rating_last_asked_time")
    private val KEY_TIMES_ASKED        = intPreferencesKey("smart_rating_times_asked")
    // 0 = chưa | 1 = mở Play/in-app review (4-5 sao) | 2 = đã gửi feedback (1-3 sao) | 3 = Google API unavailable
    private val KEY_LAST_OUTCOME       = intPreferencesKey("smart_rating_last_outcome")

    // ── Thresholds ──
    private const val MIN_ACTIONS_BEFORE_ASK    = 3      // tối thiểu 3 happy action mới hỏi
    private const val COOLDOWN_AFTER_SKIP_DAYS  = 7L     // user skip → 7 ngày
    private const val COOLDOWN_AFTER_REVIEW_DAYS = 90L   // đã 4-5 sao → 90 ngày
    private const val COOLDOWN_AFTER_FEEDBACK_DAYS = 30L // đã gửi feedback → 30 ngày
    private const val COOLDOWN_AFTER_API_UNAVAIL_DAYS = 14L // Google API unavailable → 14 ngày (retry sớm hơn)
    private const val MAX_TIMES_TO_ASK          = 5      // tối đa 5 lần auto-trigger

    // ── Observable state ──
    private val _shouldShow = MutableStateFlow(false)
    val shouldShow: StateFlow<Boolean> = _shouldShow.asStateFlow()

    /** True = trigger thủ công (Settings/Drawer); không tính vào quota auto. */
    @Volatile
    var isManualTrigger: Boolean = false
        private set

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Gọi sau khi user hoàn thành 1 hành động "vui vẻ" (lưu bookmark, tạo task,
     * thêm thành viên gia phả, hoàn thành check-in...). Tăng counter và check
     * điều kiện để tự động bật dialog.
     *
     * @param weight trọng số (mặc định 1, action lớn hơn dùng 2)
     */
    suspend fun recordHappyAction(context: Context, weight: Int = 1) {
        val prefs = context.safeSettingsData.first()
        val timesAsked = prefs[KEY_TIMES_ASKED] ?: 0
        if (timesAsked >= MAX_TIMES_TO_ASK) return

        context.settingsDataStore.edit { p ->
            p[KEY_HAPPY_ACTION_COUNT] = (p[KEY_HAPPY_ACTION_COUNT] ?: 0) + weight
        }
        checkAndTriggerAuto(context)
    }

    /** Trigger thủ công từ Settings / Drawer — bypass mọi điều kiện. */
    fun triggerManually() {
        isManualTrigger = true
        _shouldShow.value = true
    }

    /** Đóng dialog không thay đổi state (gọi khi user dismiss bằng back/outside tap). */
    fun dismissNow() {
        isManualTrigger = false
        _shouldShow.value = false
    }

    // ─────────────────────────────────────────────────────────────────────
    // Outcome recorders — gọi từ Dialog sau mỗi nhánh kết thúc
    // ─────────────────────────────────────────────────────────────────────

    /** User chọn 4-5 sao (đã trigger in-app review hoặc Play Store). */
    suspend fun recordReviewIntent(context: Context) {
        writeOutcome(context, outcome = 1)
        dismissNow()
    }

    /** User gửi feedback (1-3 sao). */
    suspend fun recordFeedbackSent(context: Context) {
        writeOutcome(context, outcome = 2)
        dismissNow()
    }

    /**
     * Google In-App Review API không sẵn sàng (quota hết, thiết bị không hỗ trợ...).
     * Cooldown ngắn hơn (14 ngày) và KHÔNG tính vào quota MAX_TIMES_TO_ASK
     * để user có cơ hội review lần sau.
     */
    suspend fun recordReviewApiUnavailable(context: Context) {
        writeOutcome(context, outcome = 3)
        // Hoàn lại quota: trừ đi 1 lần timesAsked đã tăng ở recordShown()
        context.settingsDataStore.edit { p ->
            val current = p[KEY_TIMES_ASKED] ?: 0
            if (current > 0) p[KEY_TIMES_ASKED] = current - 1
        }
        dismissNow()
    }

    /** User skip / đóng dialog mà không tương tác. */
    suspend fun recordSkipped(context: Context) {
        writeOutcome(context, outcome = 0)
        dismissNow()
    }

    /**
     * Ghi nhận đã hiển thị dialog auto: tăng timesAsked, reset action count.
     * Skip nếu là manual trigger để không cạn quota auto.
     */
    suspend fun recordShown(context: Context) {
        if (isManualTrigger) return
        context.settingsDataStore.edit { p ->
            p[KEY_TIMES_ASKED] = (p[KEY_TIMES_ASKED] ?: 0) + 1
            p[KEY_LAST_ASKED_TIME] = System.currentTimeMillis()
            p[KEY_HAPPY_ACTION_COUNT] = 0
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Play Store / In-App Review launchers
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Hiển thị Google Play In-App Review dialog.
     *
     * Bắt buộc truyền Activity (API yêu cầu).
     *
     * @param onResult callback trả về `true` nếu `requestReviewFlow` thành công
     *   (Google có thể hiện dialog), `false` nếu API fail / không sẵn sàng
     *   (quota, thiết bị không có Play Store...). Khi `false`, caller nên
     *   hiển thị fallback (ví dụ nút mở Play Store).
     *
     * Lưu ý: `true` chỉ có nghĩa là Google **có thể** hiện dialog,
     *   KHÔNG đảm bảo dialog thực sự xuất hiện (Google tự quyết định).
     */
    fun launchInAppReview(activity: Activity, onResult: (apiReady: Boolean) -> Unit = {}) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { req ->
                if (req.isSuccessful) {
                    manager.launchReviewFlow(activity, req.result)
                        .addOnCompleteListener { onResult(true) }
                } else {
                    // API fail → báo cho caller biết để xử lý fallback
                    onResult(false)
                }
            }
        } catch (_: Exception) {
            onResult(false)
        }
    }

    /**
     * Mở trang Lịch Số trên Google Play (market:// → fallback https://).
     * Trả về true nếu mở được activity nào đó.
     */
    fun openPlayStoreListing(context: Context): Boolean {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            setPackage("com.android.vending")
        }
        try {
            context.startActivity(market)
            return true
        } catch (_: android.content.ActivityNotFoundException) { /* fallback web */ }

        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return try {
            context.startActivity(web)
            true
        } catch (_: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(
                context,
                "Không mở được Google Play. Vui lòng tìm \"Lịch Số\" trên Play Store.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────────────────────────

    private suspend fun checkAndTriggerAuto(context: Context) {
        val prefs = context.safeSettingsData.first()
        val actionCount = prefs[KEY_HAPPY_ACTION_COUNT] ?: 0
        val lastAsked = prefs[KEY_LAST_ASKED_TIME] ?: 0L
        val timesAsked = prefs[KEY_TIMES_ASKED] ?: 0
        val lastOutcome = prefs[KEY_LAST_OUTCOME] ?: 0

        if (timesAsked >= MAX_TIMES_TO_ASK) return
        if (actionCount < MIN_ACTIONS_BEFORE_ASK) return

        if (lastAsked > 0L) {
            val daysSince = (System.currentTimeMillis() - lastAsked) / (1000L * 60 * 60 * 24)
            val cooldown = when (lastOutcome) {
                1 -> COOLDOWN_AFTER_REVIEW_DAYS
                2 -> COOLDOWN_AFTER_FEEDBACK_DAYS
                3 -> COOLDOWN_AFTER_API_UNAVAIL_DAYS
                else -> COOLDOWN_AFTER_SKIP_DAYS
            }
            if (daysSince < cooldown) return
        }

        isManualTrigger = false
        _shouldShow.value = true
    }

    private suspend fun writeOutcome(context: Context, outcome: Int) {
        context.settingsDataStore.edit { p ->
            p[KEY_LAST_OUTCOME] = outcome
            p[KEY_LAST_ASKED_TIME] = System.currentTimeMillis()
        }
    }
}
