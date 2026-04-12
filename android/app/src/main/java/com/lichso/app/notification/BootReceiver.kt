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
            try {
                // Limit to 8 seconds to stay within BroadcastReceiver 10s limit
                withTimeoutOrNull(8_000L) {
                    val prefs = context.settingsDataStore.data.first()
                val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                val gioDaiCatEnabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
                val festivalReminderEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
                val reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
                val reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

                if (notifyEnabled) {
                    // Reschedule individual task reminders
                    val db = LichSoDatabase.getInstance(context)
                    val reminders = db.reminderDao().getEnabledReminders().first()
                    val scheduler = ReminderScheduler(context)
                    reminders.forEach { scheduler.schedule(it) }

                    // Reschedule daily notification worker
                    DailyNotificationWorker.schedule(context, reminderHour, reminderMinute)
                }

                // Reschedule giờ đại cát worker
                if (gioDaiCatEnabled) {
                    GioDaiCatWorker.schedule(context, reminderHour, reminderMinute)
                }

                // Reschedule festival reminder worker
                if (festivalReminderEnabled) {
                    FestivalReminderWorker.schedule(context)
                }

                // Reschedule widget updates + midnight alarm
                CalendarWidgetScheduler.scheduleWidgetUpdates(context)
                CalendarWidgetScheduler.triggerImmediateUpdate(context)
                } // end withTimeoutOrNull
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Error rescheduling: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
