package com.lichso.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.lichso.app.MainActivity
import com.lichso.app.R
import com.lichso.app.domain.DayInfoProvider
import java.time.LocalDate

/**
 * Widget 4×2 — Giờ & Lịch + Thời tiết.
 * Left: TextClock (auto), solar/lunar date, day rating badge.
 * Right: Weather icon, temperature, description.
 */
class ClockWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try {
            CalendarWidgetScheduler.scheduleWidgetUpdates(context.applicationContext)
            // Trigger immediate weather fetch + widget update
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling updates", e)
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        Log.d(TAG, "onUpdate ids=${ids.toList()}")
        var needsWeatherFetch = false
        for (id in ids) {
            try {
                updateWidget(context, mgr, id)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $id", e)
            }
        }
        // If no cached weather, trigger immediate fetch in background
        if (!WidgetWeatherHelper.isCacheFresh(context)) {
            Log.d(TAG, "Weather cache empty/stale, triggering immediate fetch")
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in DATE_ACTIONS) {
            Log.d(TAG, "Received date action: ${intent.action}")
            val mgr = AppWidgetManager.getInstance(context)

            // Update own widgets
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidget::class.java))
            onUpdate(context, mgr, ids)

            // Also directly update ClockWidget2 (in case its broadcast is delayed)
            try {
                val clock2Ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidget2::class.java))
                for (id in clock2Ids) {
                    ClockWidget2.updateWidget(context, mgr, id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating ClockWidget2 from ClockWidget", e)
            }

            // Also trigger worker to update all other widgets
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        }
    }

    companion object {
        private const val TAG = "ClockWidget"
        private val DATE_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        private val DAY_OF_WEEK_LABELS = listOf(
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm",
            "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
        )

        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val today = LocalDate.now()
            val dd = today.dayOfMonth
            val mm = today.monthValue
            val yy = today.year

            val views = RemoteViews(context.packageName, R.layout.widget_clock)

            // === LEFT SECTION ===
            // Solar date line
            val dowIndex = today.dayOfWeek.value - 1
            views.setTextViewText(
                R.id.tv_clock_solar,
                "${DAY_OF_WEEK_LABELS[dowIndex]}, %02d/%02d/%d".format(dd, mm, yy)
            )

            // Lunar + can chi
            try {
                val dayInfo = DayInfoProvider().getDayInfo(dd, mm, yy)
                val lunar = dayInfo.lunar
                val lunarText = "Âm lịch ${"%02d".format(lunar.day)}/${"%02d".format(lunar.month)} - ${dayInfo.dayCanChi}"

                views.setTextViewText(
                    R.id.tv_clock_lunar,
                    lunarText
                )

                // Rating badge
                if (!dayInfo.activities.isXauDay) {
                    views.setTextViewText(R.id.tv_clock_rating_icon, "✦")
                    views.setTextColor(R.id.tv_clock_rating_icon, 0xFF81C784.toInt())
                    views.setTextViewText(R.id.tv_clock_rating, "Ngày Hoàng Đạo")
                } else {
                    views.setTextViewText(R.id.tv_clock_rating_icon, "✗")
                    views.setTextColor(R.id.tv_clock_rating_icon, 0xFFEF5350.toInt())
                    views.setTextViewText(R.id.tv_clock_rating, "Ngày Hắc Đạo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading day data", e)
                views.setTextViewText(R.id.tv_clock_lunar, "Đang tải...")
                views.setTextViewText(R.id.tv_clock_rating, "")
            }

            // === RIGHT SECTION — Weather ===
            val weather = WidgetWeatherHelper.getCachedWeather(context)
            if (weather != null) {
                views.setTextViewText(R.id.tv_weather_icon, weather.icon)
                views.setTextViewText(R.id.tv_weather_temp, "${weather.temperature.toInt()}°")
                views.setTextViewText(R.id.tv_weather_desc, weather.description)
                // Show daily min/max temperature
                views.setTextViewText(
                    R.id.tv_weather_range,
                    "↓ ${weather.tempMin.toInt()}°  ↑ ${weather.tempMax.toInt()}°"
                )
            } else {
                views.setTextViewText(R.id.tv_weather_icon, "🌡️")
                views.setTextViewText(R.id.tv_weather_temp, "--°")
                views.setTextViewText(R.id.tv_weather_desc, "Đang tải...")
                views.setTextViewText(R.id.tv_weather_range, "")
            }

            // Tap → open app
            val pi = PendingIntent.getActivity(
                context, 10,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            mgr.updateAppWidget(id, views)
        }
    }
}
