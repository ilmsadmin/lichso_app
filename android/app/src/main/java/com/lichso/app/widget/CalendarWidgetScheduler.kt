package com.lichso.app.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Scheduler for calendar widget updates
 */
object CalendarWidgetScheduler {

    private const val WIDGET_UPDATE_WORK_NAME = "calendar_widget_update"

    /**
     * Schedule periodic widget updates at midnight and every hour
     */
    fun scheduleWidgetUpdates(context: Context) {
        val appContext = context.applicationContext
        // Schedule periodic update every hour
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
}
