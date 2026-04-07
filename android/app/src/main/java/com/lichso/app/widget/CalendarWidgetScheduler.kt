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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }

    /**
     * Trigger immediate widget update
     */
    fun triggerImmediateUpdate(context: Context) {
        val updateRequest = OneTimeWorkRequestBuilder<CalendarWidgetUpdateWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(updateRequest)
    }

    /**
     * Cancel all scheduled widget updates
     */
    fun cancelWidgetUpdates(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }
}
