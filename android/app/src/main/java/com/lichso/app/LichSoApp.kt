package com.lichso.app

import android.app.Application
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.notification.DailyNotificationWorker
import com.lichso.app.notification.FestivalReminderWorker
import com.lichso.app.notification.GioDaiCatWorker
import com.lichso.app.notification.NotificationHelper
import com.lichso.app.notification.ReminderScheduler
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.widget.CalendarWidgetScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class LichSoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        scheduleWorkersFromSettings()
        // Schedule widget updates
        CalendarWidgetScheduler.scheduleWidgetUpdates(this)
    }

    /**
     * Đọc settings và lên lịch các workers nếu đã được bật.
     * Chạy lúc app khởi động để đảm bảo workers luôn active.
     */
    private fun scheduleWorkersFromSettings() {
        val context = this
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.settingsDataStore.data.first()
                val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                val gioDaiCatEnabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
                val festivalReminderEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
                val reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
                val reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

                if (notifyEnabled) {
                    DailyNotificationWorker.schedule(context, reminderHour, reminderMinute)
                    // Schedule tất cả individual reminders đang enabled
                    val db = LichSoDatabase.getInstance(context)
                    val scheduler = ReminderScheduler(context)
                    db.reminderDao().getEnabledReminders().first().forEach { scheduler.schedule(it) }
                }
                if (gioDaiCatEnabled) {
                    GioDaiCatWorker.schedule(context, reminderHour, reminderMinute)
                }
                if (festivalReminderEnabled) {
                    FestivalReminderWorker.schedule(context)
                }
            } catch (_: Exception) {
                // Ignore errors during initial scheduling
            }
        }
    }
}
