package com.lichso.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Scheduler for calendar widget updates
 */
object CalendarWidgetScheduler {

    private const val TAG = "WidgetScheduler"
    private const val WIDGET_UPDATE_WORK_NAME = "calendar_widget_update"
    private const val ACTION_MIDNIGHT_UPDATE = "com.lichso.app.widget.MIDNIGHT_UPDATE"

    /**
     * Schedule periodic widget updates every hour + exact midnight alarm
     */
    fun scheduleWidgetUpdates(context: Context) {
        val appContext = context.applicationContext
        // Schedule periodic update every hour via WorkManager
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<CalendarWidgetUpdateWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )

        // Also schedule exact midnight alarm for date change
        scheduleMidnightAlarm(appContext)
    }

    /**
     * Schedule an exact alarm at midnight to force widget refresh when date changes.
     * WorkManager periodic tasks can be delayed by Android Doze, so this ensures
     * the widget always shows the correct date right after midnight.
     */
    private fun scheduleMidnightAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MidnightUpdateReceiver::class.java).apply {
            action = ACTION_MIDNIGHT_UPDATE
        }
        val pi = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next midnight
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5) // 5 seconds past midnight to be safe
            set(Calendar.MILLISECOND, 0)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi
                    )
                } else {
                    // Fallback: inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi
                )
            }
            Log.d(TAG, "Midnight alarm scheduled for ${midnight.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling midnight alarm", e)
            // Fallback: inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi
            )
        }
    }

    /**
     * Trigger immediate widget update
     */
    fun triggerImmediateUpdate(context: Context) {
        val appContext = context.applicationContext
        val updateRequest = OneTimeWorkRequestBuilder<CalendarWidgetUpdateWorker>()
            .build()

        WorkManager.getInstance(appContext).enqueue(updateRequest)
    }

    /**
     * Cancel all scheduled widget updates
     */
    fun cancelWidgetUpdates(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }

    /**
     * BroadcastReceiver for midnight alarm — updates all widgets and reschedules.
     */
    class MidnightUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Log.d(TAG, "Midnight alarm fired — updating all widgets")
            triggerImmediateUpdate(context.applicationContext)
            // Reschedule next midnight alarm
            scheduleMidnightAlarm(context.applicationContext)
        }
    }
}
