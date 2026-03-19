package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reschedule tất cả reminders đang enabled sau khi device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.settingsDataStore.data.first()
                val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                if (notifyEnabled) {
                    val db = LichSoDatabase.getInstance(context)
                    val reminders = db.reminderDao().getEnabledReminders().first()
                    val scheduler = ReminderScheduler(context)
                    reminders.forEach { scheduler.schedule(it) }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
