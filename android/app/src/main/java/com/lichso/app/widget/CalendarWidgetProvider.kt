package com.lichso.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lichso.app.MainActivity
import com.lichso.app.R
import com.lichso.app.domain.DayInfoProvider
import java.time.LocalDate

/**
 * Widget provider for calendar widget showing today's lunar calendar information
 */
class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Get today's date
            val today = LocalDate.now()
            val dayInfoProvider = DayInfoProvider()

            // Get day information
            val dayInfo = try {
                dayInfoProvider.getDayInfo(
                    today.dayOfMonth,
                    today.monthValue,
                    today.year
                )
            } catch (e: Exception) {
                null
            }

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            if (dayInfo != null) {
                // Header section - Month and Year
                val monthNames = arrayOf(
                    "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
                )
                views.setTextViewText(
                    R.id.widget_month_year,
                    "${monthNames[today.monthValue - 1]}, ${today.year}"
                )
                views.setTextViewText(R.id.widget_can_chi, "Năm ${dayInfo.yearCanChi}")

                // Today's date section
                views.setTextViewText(R.id.widget_solar_day, today.dayOfMonth.toString())
                views.setTextViewText(R.id.widget_day_of_week, dayInfo.dayOfWeek)
                views.setTextViewText(
                    R.id.widget_lunar_date,
                    "Ngày ${dayInfo.lunar.lunarDay} ${dayInfo.lunar.lunarMonthName}"
                )
                views.setTextViewText(R.id.widget_day_can_chi, dayInfo.dayCanChi)

                // Day rating
                val ratingText = when {
                    dayInfo.dayRating.score >= 8 -> "Ngày rất tốt"
                    dayInfo.dayRating.score >= 6 -> "Ngày tốt"
                    dayInfo.dayRating.score >= 4 -> "Ngày trung bình"
                    else -> "Ngày xấu"
                }
                views.setTextViewText(R.id.widget_day_rating, ratingText)

                // Day rating background color
                val ratingBgColor = when {
                    dayInfo.dayRating.score >= 8 -> android.R.color.holo_green_light
                    dayInfo.dayRating.score >= 6 -> android.R.color.holo_green_light
                    dayInfo.dayRating.score >= 4 -> android.R.color.holo_orange_light
                    else -> android.R.color.holo_red_light
                }
                views.setInt(
                    R.id.widget_day_rating,
                    "setBackgroundResource",
                    ratingBgColor
                )

                // Moon phase
                val moonEmoji = when (dayInfo.moonPhase.emoji) {
                    "🌑" -> "🌑 Trăng mới"
                    "🌒" -> "🌒 Trăng non"
                    "🌓" -> "🌓 Trăng lưỡi liềm"
                    "🌔" -> "🌔 Trăng thượng huyền"
                    "🌕" -> "🌕 Trăng tròn"
                    "🌖" -> "🌖 Trăng khuyết"
                    "🌗" -> "🌗 Trăng hạ huyền"
                    "🌘" -> "🌘 Trăng tàn"
                    else -> dayInfo.moonPhase.emoji + " " + dayInfo.moonPhase.name
                }
                views.setTextViewText(R.id.widget_moon_phase, moonEmoji)

                // Solar term (Tiết khí)
                views.setTextViewText(R.id.widget_tiet_khi, dayInfo.tietKhi.name)
            } else {
                // Fallback if day info cannot be loaded
                views.setTextViewText(R.id.widget_month_year, "Lịch Vạn Niên")
                views.setTextViewText(R.id.widget_solar_day, today.dayOfMonth.toString())
                views.setTextViewText(R.id.widget_day_of_week, "")
                views.setTextViewText(R.id.widget_lunar_date, "Đang tải...")
            }

            // Set up click listener to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.today_section, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Request update for all widget instances
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, CalendarWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
