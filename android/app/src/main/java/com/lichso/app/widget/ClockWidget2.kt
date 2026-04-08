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
 * Widget 4×2 — Giờ & Lịch + Thời tiết (Light/Dark theo hệ thống).
 * Light: nền trắng, chữ tối.
 * Dark : nền đỏ đậm (#7F1D1D), chữ sáng.
 *
 * Dark mode strategy (same as MonthCalendarWidget):
 * - Uses R.layout.widget_clock2 — system auto-picks
 *   layout-night/widget_clock2.xml when dark mode is ON.
 * - DarkModeWidgetObserver triggers refresh with forceDark when mode changes (process alive).
 * - On process restart, layout-night/ is already correct + isDark re-detected.
 */
class ClockWidget2 : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try {
            CalendarWidgetScheduler.scheduleWidgetUpdates(context.applicationContext)
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling updates", e)
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        Log.d(TAG, "onUpdate ids=${ids.toList()}")
        for (id in ids) {
            try {
                updateWidget(context, mgr, id)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $id", e)
            }
        }
        if (!WidgetWeatherHelper.isCacheFresh(context)) {
            Log.d(TAG, "Weather cache stale, triggering immediate fetch")
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in DATE_ACTIONS) {
            Log.d(TAG, "Received date action: ${intent.action}")
            val mgr = AppWidgetManager.getInstance(context)

            // Update own widgets
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidget2::class.java))
            onUpdate(context, mgr, ids)

            // Also directly update ClockWidget (in case its broadcast is delayed)
            try {
                val clockIds = mgr.getAppWidgetIds(ComponentName(context, ClockWidget::class.java))
                for (id in clockIds) {
                    ClockWidget.updateWidget(context, mgr, id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating ClockWidget from ClockWidget2", e)
            }

            // Also trigger worker to update all other widgets
            CalendarWidgetScheduler.triggerImmediateUpdate(context.applicationContext)
        }
    }

    companion object {
        private const val TAG = "ClockWidget2"
        private val DATE_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        private val DAY_OF_WEEK_LABELS = listOf(
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm",
            "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
        )

        /**
         * Update widget.
         * @param forceDark if non-null, overrides auto-detection of dark mode
         *                  (used by DarkModeWidgetObserver which knows the new state
         *                  before context.resources.configuration is updated).
         */
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int, forceDark: Boolean? = null) {
            val today = LocalDate.now()
            val dd = today.dayOfMonth
            val mm = today.monthValue
            val yy = today.year
            Log.d(TAG, "updateWidget id=$id date=$today forceDark=$forceDark")

            // Use forceDark if provided (from DarkModeWidgetObserver), otherwise auto-detect
            val isDark = forceDark ?: run {
                val nightMode = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

            // Choose layout explicitly when forceDark is provided,
            // because the system resource qualifier may not have updated yet.
            val layoutId = if (forceDark != null) {
                if (isDark) R.layout.widget_clock2_dark
                else R.layout.widget_clock2_light
            } else {
                // Let system auto-resolve via layout/ vs layout-night/
                R.layout.widget_clock2
            }

            val views = RemoteViews(context.packageName, layoutId)
            populateViews(views, context, today, dd, mm, yy, isDark)

            // Tap → open app
            val pi = PendingIntent.getActivity(
                context, 20,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            mgr.updateAppWidget(id, views)
            Log.d(TAG, "updateAppWidget done for id=$id isDark=$isDark")
        }

        private fun populateViews(
            views: RemoteViews,
            context: Context,
            today: LocalDate,
            dd: Int, mm: Int, yy: Int,
            isDark: Boolean
        ) {
            // === LEFT SECTION ===
            val dowIndex = today.dayOfWeek.value - 1
            val solarText = "${DAY_OF_WEEK_LABELS[dowIndex]}, %02d/%02d/%d".format(dd, mm, yy)
            views.setTextViewText(R.id.tv_clock_solar, solarText)

            try {
                val dayInfo = DayInfoProvider().getDayInfo(dd, mm, yy)
                val lunar = dayInfo.lunar
                val lunarText = "Âm lịch ${"%02d".format(lunar.day)}/${"%02d".format(lunar.month)} - ${dayInfo.dayCanChi}"

                views.setTextViewText(R.id.tv_clock_lunar, lunarText)

                if (!dayInfo.activities.isXauDay) {
                    views.setTextViewText(R.id.tv_clock_rating_icon, "✦")
                    views.setTextColor(
                        R.id.tv_clock_rating_icon,
                        if (isDark) 0xFF81C784.toInt() else 0xFF2E7D32.toInt()
                    )
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
        }
    }
}
