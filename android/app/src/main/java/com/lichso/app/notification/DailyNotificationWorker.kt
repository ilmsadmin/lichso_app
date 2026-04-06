package com.lichso.app.notification

import android.content.Context
import androidx.work.*
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker chạy hàng ngày theo giờ nhắc nhở người dùng cấu hình,
 * gửi notification tóm tắt ngày mới: can chi, giờ hoàng đạo, ngày lễ.
 */
class DailyNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.settingsDataStore.data.first()
        val enabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
        if (!enabled) return Result.success()

        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)

        val lunarStr = "${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch"
        val canChi = dayInfo.dayCanChi
        val gioText = dayInfo.gioHoangDao
            .take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }
        val rating = dayInfo.dayRating.label

        val parts = mutableListOf<String>()
        parts.add("📅 $lunarStr · $canChi")
        parts.add("⭐ Đánh giá: $rating")
        parts.add("🕐 Giờ tốt: $gioText")

        dayInfo.solarHoliday?.let { parts.add("🎉 $it") }
        dayInfo.lunarHoliday?.let { parts.add("🏮 $it") }

        val body = parts.joinToString("\n")

        NotificationHelper.sendDailyNotification(applicationContext, body)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_notification"

        /**
         * Lên lịch worker chạy hàng ngày vào giờ người dùng chọn.
         */
        fun schedule(context: Context, hour: Int = 7, minute: Int = 0) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DATE, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
