package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.widget.CalendarWidgetScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reschedule tất cả reminders và workers đang enabled sau khi device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            // Fallback defaults nếu đọc DataStore fail/timeout
            var notifyEnabled = true
            var gioDaiCatEnabled = false
            var festivalReminderEnabled = true
            var reminderHour = 7
            var reminderMinute = 0
            var settingsLoaded = false

            try {
                withTimeoutOrNull(5_000L) {
                    val prefs = context.settingsDataStore.data.first()
                    notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                    gioDaiCatEnabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
                    festivalReminderEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
                    reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
                    reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0
                    settingsLoaded = true
                }
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Load settings failed: ${e.message}")
            }

            // LUÔN reschedule các alarm với giá trị settings đã đọc được
            // (hoặc defaults). Không nằm trong cùng timeout/try với phần đọc DB
            // để tránh bị "kẹt" do timeout.
            try {
                if (notifyEnabled) {
                    // Reschedule daily notification alarm
                    DailyNotificationWorker.schedule(context, reminderHour, reminderMinute)
                    AiTuViWorker.schedule(context)
                } else {
                    DailyNotificationWorker.cancel(context)
                    AiTuViWorker.cancel(context)
                }

                if (gioDaiCatEnabled && notifyEnabled) {
                    GioDaiCatWorker.schedule(context, reminderHour, reminderMinute)
                } else {
                    GioDaiCatWorker.cancel(context)
                }

                if (festivalReminderEnabled && notifyEnabled) {
                    FestivalReminderWorker.schedule(context)
                } else {
                    FestivalReminderWorker.cancel(context)
                }

                // Widget updates + midnight alarm
                CalendarWidgetScheduler.scheduleWidgetUpdates(context)
                CalendarWidgetScheduler.triggerImmediateUpdate(context)
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Reschedule failed: ${e.message}")
            }

            // Reschedule individual task reminders (cần DB, tách timeout riêng)
            if (settingsLoaded && notifyEnabled) {
                try {
                    withTimeoutOrNull(5_000L) {
                        val db = LichSoDatabase.getInstance(context)
                        val reminders = db.reminderDao().getEnabledReminders().first()
                        val scheduler = ReminderScheduler(context)
                        reminders.forEach { scheduler.schedule(it) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BootReceiver", "Reschedule reminders failed: ${e.message}")
                }
            }

            pendingResult.finish()
        }
    }
}
