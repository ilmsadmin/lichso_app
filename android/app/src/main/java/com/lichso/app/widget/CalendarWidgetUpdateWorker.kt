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

            // Only fetch weather if app hasn't synced recently (cache stale > 30 min)
            if (!WidgetWeatherHelper.isCacheFresh(applicationContext)) {
                try {
                    WidgetWeatherHelper.fetchAndCacheWeather(applicationContext)
                    Log.d("WidgetWorker", "Weather data refreshed by worker (cache was stale)")
                } catch (e: Exception) {
                    Log.w("WidgetWorker", "Weather fetch failed (non-fatal)", e)
                }
            } else {
                Log.d("WidgetWorker", "Weather cache is fresh (synced from app), skipping fetch")
            }

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

            // Update Clock widgets
            val clockIds = appWidgetManager.getAppWidgetIds(
                ComponentName(applicationContext, ClockWidget::class.java)
            )
            Log.d("WidgetWorker", "Updating ${clockIds.size} ClockWidget(s)")
            for (appWidgetId in clockIds) {
                ClockWidget.updateWidget(applicationContext, appWidgetManager, appWidgetId)
            }

            // Update Month Calendar widgets
            val monthIds = appWidgetManager.getAppWidgetIds(
                ComponentName(applicationContext, MonthCalendarWidget::class.java)
            )
            Log.d("WidgetWorker", "Updating ${monthIds.size} MonthCalendarWidget(s)")
            for (appWidgetId in monthIds) {
                MonthCalendarWidget.updateWidget(applicationContext, appWidgetManager, appWidgetId)
            }

            // Update Clock2 widgets (light/dark adaptive)
            val clock2Ids = appWidgetManager.getAppWidgetIds(
                ComponentName(applicationContext, ClockWidget2::class.java)
            )
            Log.d("WidgetWorker", "Updating ${clock2Ids.size} ClockWidget2(s)")
            for (appWidgetId in clock2Ids) {
                ClockWidget2.updateWidget(applicationContext, appWidgetManager, appWidgetId)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("WidgetWorker", "Error updating widgets", e)
            Result.failure()
        }
    }
}
