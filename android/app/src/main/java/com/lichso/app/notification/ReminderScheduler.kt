package com.lichso.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lichso.app.data.local.entity.ReminderEntity

/**
 * Lên lịch / huỷ AlarmManager cho từng ReminderEntity.
 *
 * repeatType:
 *   0 = Once
 *   1 = Daily
 *   2 = Weekly
 *   3 = Monthly
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }

        val triggerAt = reminder.triggerTime
        val now = System.currentTimeMillis()

        // Nếu thời gian đã qua và là lịch Once → bỏ qua
        if (triggerAt < now && reminder.repeatType == 0) return

        // Tính thời gian trigger tiếp theo nếu đã qua
        val nextTrigger = when (reminder.repeatType) {
            1 -> nextRepeat(triggerAt, now, 24 * 60 * 60 * 1000L)           // Daily
            2 -> nextRepeat(triggerAt, now, 7 * 24 * 60 * 60 * 1000L)       // Weekly
            3 -> nextMonthly(triggerAt, now)                                  // Monthly
            else -> if (triggerAt >= now) triggerAt else return               // Once
        }

        val pi = buildPendingIntent(reminder)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                // Fallback: inexact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pi
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pi
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pi)
        }
    }

    fun cancel(reminderId: Long) {
        val pi = buildPendingIntent(
            ReminderEntity(id = reminderId, title = "", triggerTime = 0)
        )
        alarmManager.cancel(pi)
    }

    private fun buildPendingIntent(reminder: ReminderEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderReceiver.EXTRA_BODY, reminder.subtitle.ifBlank { "Đã đến giờ nhắc nhở!" })
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextRepeat(base: Long, now: Long, interval: Long): Long {
        var t = base
        while (t < now) t += interval
        return t
    }

    private fun nextMonthly(base: Long, now: Long): Long {
        var cal = java.util.Calendar.getInstance().apply { timeInMillis = base }
        val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        while (!cal.after(nowCal)) {
            cal.add(java.util.Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }
}
