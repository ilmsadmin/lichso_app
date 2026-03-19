package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver nhận AlarmManager intent và fire notification nhắc nhở.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, 0L).toInt()
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Nhắc nhở"
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        NotificationHelper.sendReminderNotification(context, id, title, body)
    }
}
