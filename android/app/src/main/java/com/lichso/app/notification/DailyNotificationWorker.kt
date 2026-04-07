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

        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val lunarStr = "${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch"
        val canChi = dayInfo.dayCanChi
        val isGoodDay = !dayInfo.activities.isXauDay
        val dayQuality = if (isGoodDay) "Hoàng Đạo" else "Hắc Đạo"

        val gioText = dayInfo.gioHoangDao
            .take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }

        // Title: clear, concise, no emoji
        val title = "${dayInfo.dayOfWeek}, $dd/$mm — $lunarStr"

        // Subtitle (contentText): brief summary
        val subtitle = buildString {
            append("$canChi")
            append(if (isGoodDay) " | Ngày Hoàng Đạo" else " | Ngày Hắc Đạo")
            append(" | ${dayInfo.dayRating.label}")
        }

        // Expanded lines for InboxStyle
        val lines = mutableListOf<String>()
        lines.add("Can Chi: $canChi")
        lines.add(if (isGoodDay) "Đánh giá: ${dayInfo.dayRating.label} — Ngày Hoàng Đạo" else "Đánh giá: ${dayInfo.dayRating.label} — Ngày Hắc Đạo")
        lines.add("Giờ tốt: $gioText")
        lines.add("Trực ngày: ${dayInfo.trucNgay.name} | Sao: ${dayInfo.saoChieu.name}")
        lines.add("Hướng Thần Tài: ${dayInfo.huong.thanTai}")
        if (dayInfo.activities.nenLam.isNotEmpty()) {
            lines.add("Nên: ${dayInfo.activities.nenLam.take(3).joinToString(", ")}")
        }
        if (dayInfo.activities.khongNen.isNotEmpty()) {
            lines.add("Tránh: ${dayInfo.activities.khongNen.take(3).joinToString(", ")}")
        }
        dayInfo.solarHoliday?.let { lines.add("Ngày lễ: $it") }
        dayInfo.lunarHoliday?.let { lines.add("Âm lịch: $it") }

        NotificationHelper.sendDailyNotification(applicationContext, title, subtitle, lines)

        // Tự reschedule cho ngày mai để đảm bảo đúng giờ
        val hour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
        val minute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0
        scheduleNext(applicationContext, hour, minute)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_notification"

        /**
         * Lên lịch worker chạy 1 lần vào đúng giờ người dùng chọn.
         * Sau khi chạy xong, worker sẽ tự reschedule cho ngày hôm sau.
         * Dùng OneTimeWorkRequest thay vì PeriodicWork để đảm bảo đúng giờ —
         * PeriodicWork có flex window khiến notification bị lệch giờ.
         */
        fun schedule(context: Context, hour: Int = 7, minute: Int = 0) {
            scheduleNext(context, hour, minute)
        }

        internal fun scheduleNext(context: Context, hour: Int, minute: Int) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                // Luôn schedule cho lần chạy tiếp theo (nếu đã qua giờ hôm nay → ngày mai)
                if (!after(now)) add(java.util.Calendar.DATE, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
