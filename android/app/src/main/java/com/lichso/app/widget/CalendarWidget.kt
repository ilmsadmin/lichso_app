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
 * Widget lịch vạn niên — hiển thị ngày dương, âm lịch, can chi, và đánh giá ngày.
 * Supports light/dark mode via RemoteViews(light, dark) on API 31+.
 */
class CalendarWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try {
            CalendarWidgetScheduler.scheduleWidgetUpdates(context.applicationContext)
        } catch (e: Exception) {
            Log.e("CalendarWidget", "Error scheduling updates", e)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("CalendarWidget", "onUpdate called, ids=${appWidgetIds.toList()}")
        for (appWidgetId in appWidgetIds) {
            try {
                updateWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e("CalendarWidget", "Error updating widget $appWidgetId", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CalendarWidget::class.java)
            )
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        private val DAY_OF_WEEK_LABELS = listOf(
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm",
            "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
        )

        private val MONTH_LABELS = listOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
            "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
            "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )

        /**
         * Update widget.
         * @param forceDark if non-null, overrides auto-detection of dark mode
         *                  (used by DarkModeWidgetObserver which knows the new state
         *                  before context.resources.configuration is updated).
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            forceDark: Boolean? = null
        ) {
            Log.d("CalendarWidget", "updateWidget id=$appWidgetId forceDark=$forceDark")
            val today = LocalDate.now()
            val dd = today.dayOfMonth
            val mm = today.monthValue
            val yy = today.year

            // Use forceDark if provided (from DarkModeWidgetObserver), otherwise auto-detect
            val isDark = forceDark ?: run {
                val nightMode = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

            // Choose layout explicitly when forceDark is provided,
            // because the system resource qualifier may not have updated yet.
            val layoutId = if (forceDark != null) {
                if (isDark) R.layout.widget_calendar_dark
                else R.layout.widget_calendar
            } else {
                // Let system auto-resolve via layout/ vs layout-night/
                R.layout.widget_calendar
            }

            val views = RemoteViews(context.packageName, layoutId)
            populateViews(views, today, dd, mm, yy, isDark)

            // Tap to open app
            val pendingIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("CalendarWidget", "updateAppWidget done for id=$appWidgetId isDark=$isDark")
        }

        private fun populateViews(
            views: RemoteViews,
            today: LocalDate,
            dd: Int, mm: Int, yy: Int,
            isDark: Boolean
        ) {
            views.setTextViewText(R.id.tv_solar_day, dd.toString())

            val dowIndex = today.dayOfWeek.value - 1
            views.setTextViewText(R.id.tv_day_of_week, DAY_OF_WEEK_LABELS[dowIndex])
            views.setTextViewText(R.id.tv_solar_month_year, "${MONTH_LABELS[mm - 1]} · $yy")

            try {
                val dayInfo = DayInfoProvider().getDayInfo(dd, mm, yy)
                val lunar = dayInfo.lunar
                val leapLabel = if (lunar.leap == 1) " nhuận" else ""
                val lunarDayLabel = if (lunar.day <= 10) "Mùng ${lunar.day}" else "${lunar.day}"
                val lunarText = "$lunarDayLabel · ${lunar.monthName}$leapLabel Âm lịch"
                views.setTextViewText(R.id.tv_lunar, lunarText)

                views.setTextViewText(
                    R.id.tv_can_chi,
                    "Ngày ${dayInfo.dayCanChi} · Năm ${dayInfo.yearCanChi}"
                )

                val isGoodDay = !dayInfo.activities.isXauDay
                val ratingText = if (isGoodDay) {
                    "✦ Ngày Hoàng Đạo"
                } else {
                    "✗ Ngày Hắc Đạo"
                }
                views.setTextViewText(R.id.tv_day_rating, ratingText)

                val ratingColor = if (isGoodDay) {
                    if (isDark) 0xFF81C784.toInt() else 0xFF2E7D32.toInt()
                } else {
                    if (isDark) 0xFFEF5350.toInt() else 0xFFC62828.toInt()
                }
                views.setTextColor(R.id.tv_day_rating, ratingColor)
            } catch (e: Exception) {
                Log.e("CalendarWidget", "Error loading day info", e)
                views.setTextViewText(R.id.tv_lunar, "Đang tải...")
                views.setTextViewText(R.id.tv_can_chi, "")
                views.setTextViewText(R.id.tv_day_rating, "")
            }
        }
    }
}
