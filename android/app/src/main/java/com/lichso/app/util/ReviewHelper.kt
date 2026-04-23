package com.lichso.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.google.android.play.core.review.ReviewManagerFactory
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first

/**
 * Helper for Google Play In-App Review API.
 *
 * Rules:
 * - Show review prompt after user has opened the app at least [MIN_OPENS_BEFORE_REVIEW] times
 * - Don't show more than once every [MIN_DAYS_BETWEEN_REVIEWS] days
 * - Can always be triggered manually from Settings (bypasses conditions)
 */
object ReviewHelper {

    private const val TAG = "ReviewHelper"
    private const val MIN_OPENS_BEFORE_REVIEW = 3
    private const val MIN_DAYS_BETWEEN_REVIEWS = 15

    /**
     * Increment app open count in DataStore. Call this every time the main screen appears.
     */
    suspend fun incrementAppOpenCount(context: Context) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[SettingsKeys.APP_OPEN_COUNT] ?: 0
            prefs[SettingsKeys.APP_OPEN_COUNT] = current + 1
        }
    }

    /**
     * Check if conditions are met to show the review prompt automatically.
     */
    suspend fun shouldShowReview(context: Context): Boolean {
        val prefs = context.settingsDataStore.data.first()
        val openCount = prefs[SettingsKeys.APP_OPEN_COUNT] ?: 0
        val lastPromptTime = prefs[SettingsKeys.LAST_REVIEW_PROMPT_TIME] ?: 0L

        if (openCount < MIN_OPENS_BEFORE_REVIEW) {
            Log.d(TAG, "Not enough opens: $openCount < $MIN_OPENS_BEFORE_REVIEW")
            return false
        }

        val daysSinceLastPrompt = if (lastPromptTime == 0L) {
            Long.MAX_VALUE // Never prompted before
        } else {
            (System.currentTimeMillis() - lastPromptTime) / (1000 * 60 * 60 * 24)
        }

        if (daysSinceLastPrompt < MIN_DAYS_BETWEEN_REVIEWS) {
            Log.d(TAG, "Too soon: $daysSinceLastPrompt days < $MIN_DAYS_BETWEEN_REVIEWS")
            return false
        }

        return true
    }

    /**
     * Record that we showed the review prompt.
     */
    private suspend fun recordReviewPrompt(context: Context) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.LAST_REVIEW_PROMPT_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Launch the Google Play In-App Review flow.
     *
     * ⚠️ KHÔNG dùng trực tiếp cho auto-trigger. Mỗi lần gọi đều tiêu quota
     * In-App Review của Google (rất hạn chế). Hãy dùng [SmartRatingDialog]
     * (có fallback Play Store khi quota hết) thay vì gọi trực tiếp hàm này.
     *
     * @param activity The current Activity (required by the Review API)
     * @param onComplete Called when the flow finishes (success or failure)
     */
    @Deprecated(
        "Dùng SmartRatingDialog / SmartRatingManager.triggerManually() thay thế " +
        "để có fallback Play Store khi quota In-App Review đã hết.",
        level = DeprecationLevel.WARNING
    )
    fun launchReviewFlow(activity: Activity, onComplete: () -> Unit = {}) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val requestFlow = reviewManager.requestReviewFlow()

        requestFlow.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.d(TAG, "Review flow completed")
                    onComplete()
                }
            } else {
                Log.e(TAG, "Failed to request review flow", task.exception)
                onComplete()
            }
        }
    }

    /**
     * Check conditions and launch review if appropriate. Used for automatic triggering.
     *
     * ⚠️ KHÔNG gọi hàm này trong main screen / LaunchedEffect. Mỗi call đều
     * đốt quota In-App Review của Google (ngay cả khi user không bấm gì).
     * Sau vài lần, mọi request sau đó sẽ fail silently — user bấm "Đánh giá"
     * thật cũng không thấy dialog.
     *
     * Dùng [SmartRatingManager.recordHappyAction] để trigger theo happy-path
     * thay vì tự động mỗi lần mở app.
     */
    @Deprecated(
        "Auto-trigger mỗi lần mở app sẽ đốt quota In-App Review. " +
        "Dùng SmartRatingManager thay thế.",
        level = DeprecationLevel.WARNING
    )
    @Suppress("DEPRECATION")
    suspend fun tryShowReview(activity: Activity) {
        val context = activity.applicationContext
        if (shouldShowReview(context)) {
            recordReviewPrompt(context)
            launchReviewFlow(activity)
        }
    }

    /**
     * Trigger smart rating dialog sau khi user hoàn thành một "happy action".
     *
     * Gọi hàm này sau các tác vụ mang lại cảm xúc tích cực:
     * - Lưu bookmark ngày tốt
     * - Thêm thành viên gia phả
     * - Lưu văn khấn yêu thích
     * - Hoàn thành tra cứu ngày tốt cho sự kiện
     *
     * @param context Android context
     * @param weight Trọng số của action (mặc định 1, action quan trọng hơn dùng 2)
     */
    suspend fun triggerAfterAction(context: Context, weight: Int = 1) {
        SmartRatingManager.recordHappyAction(context, weight)
    }
}
