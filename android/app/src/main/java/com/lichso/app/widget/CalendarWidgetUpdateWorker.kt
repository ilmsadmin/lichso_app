package com.lichso.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker to update calendar widget in background
 */
class CalendarWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get the app widget manager
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)

            // Get all widget IDs for our widget
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(
                    applicationContext,
                    CalendarWidget::class.java
                )
            )

            // Update each widget
            for (appWidgetId in appWidgetIds) {
                CalendarWidget.updateWidget(
                    applicationContext,
                    appWidgetManager,
                    appWidgetId
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
