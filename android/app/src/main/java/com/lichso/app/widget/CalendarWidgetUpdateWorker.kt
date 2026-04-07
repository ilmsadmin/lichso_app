package com.lichso.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker to update all widgets in background
 */
class CalendarWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)

            // Update Calendar widgets
            val calendarIds = appWidgetManager.getAppWidgetIds(
                ComponentName(applicationContext, CalendarWidget::class.java)
            )
            Log.d("WidgetWorker", "Updating ${calendarIds.size} CalendarWidget(s)")
            for (appWidgetId in calendarIds) {
                CalendarWidget.updateWidget(applicationContext, appWidgetManager, appWidgetId)
            }

            // Update AI widgets
            val aiIds = appWidgetManager.getAppWidgetIds(
                ComponentName(applicationContext, AiWidget::class.java)
            )
            Log.d("WidgetWorker", "Updating ${aiIds.size} AiWidget(s)")
            for (appWidgetId in aiIds) {
                AiWidget.updateWidget(applicationContext, appWidgetManager, appWidgetId)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("WidgetWorker", "Error updating widgets", e)
            Result.failure()
        }
    }
}
