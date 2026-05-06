package com.lichso.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker dự phòng: định kỳ kiểm tra và dựng lại lịch alarm thông báo.
 *
 * Mục tiêu: tránh trường hợp một số ROM/OEM làm rơi exact alarm sau vài ngày
 * idle khiến user chỉ nhận thông báo khi mở app.
 */
class NotificationWatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.safeSettingsData.first()
            val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
            val gioDaiCatEnabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
            val festivalReminderEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
            val reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
            val reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

            if (notifyEnabled) {
                DailyNotificationWorker.schedule(applicationContext, reminderHour, reminderMinute)
                AiTuViWorker.schedule(applicationContext)

                val db = LichSoDatabase.getInstance(applicationContext)
                val scheduler = ReminderScheduler(applicationContext)
                db.reminderDao().getEnabledReminders().first().forEach { scheduler.schedule(it) }
            } else {
                DailyNotificationWorker.cancel(applicationContext)
                AiTuViWorker.cancel(applicationContext)
            }

            if (gioDaiCatEnabled && notifyEnabled) {
                GioDaiCatWorker.schedule(applicationContext, reminderHour, reminderMinute)
            } else {
                GioDaiCatWorker.cancel(applicationContext)
            }

            if (festivalReminderEnabled && notifyEnabled) {
                FestivalReminderWorker.schedule(applicationContext)
            } else {
                FestivalReminderWorker.cancel(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NotifWatchdog", "Watchdog failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "notification_watchdog"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<NotificationWatchdogWorker>(6, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                req,
            )
        }
    }
}
