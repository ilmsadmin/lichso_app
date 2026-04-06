package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.data.local.LichSoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver nhận AlarmManager intent và fire notification nhắc nhở.
 * Sau khi fire, nếu reminder có lặp lại → reschedule alarm tiếp theo.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Nhắc nhở"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""

        // Fire notification ngay lập tức
        NotificationHelper.sendReminderNotification(context, id.toInt(), title, body)

        // Reschedule nếu là reminder lặp lại (Daily/Weekly/Monthly)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = LichSoDatabase.getInstance(context)
                val reminder = db.reminderDao().getAllReminders().first().find { it.id == id }
                if (reminder != null && reminder.isEnabled && reminder.repeatType != 0) {
                    // repeatType != 0 → lặp lại → schedule alarm tiếp theo
                    val scheduler = ReminderScheduler(context)
                    scheduler.schedule(reminder)
                }
            } catch (_: Exception) {
                // Ignore errors — notification đã được gửi rồi
            } finally {
                pendingResult.finish()
            }
        }
    }
}
