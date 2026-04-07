package com.lichso.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lichso.app.MainActivity
import com.lichso.app.R
import com.lichso.app.domain.DayInfoProvider
import java.time.LocalDate

/**
 * Widget lịch vạn niên — hiển thị ngày dương, âm lịch, can chi, và đánh giá ngày.
 * Cập nhật mỗi 30 phút và khi màn hình bật (ACTION_DATE_CHANGED, ACTION_TIMEZONE_CHANGED).
 */
class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Refresh all widgets when date or timezone changes
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
            "tháng 1", "tháng 2", "tháng 3", "tháng 4",
            "tháng 5", "tháng 6", "tháng 7", "tháng 8",
            "tháng 9", "tháng 10", "tháng 11", "tháng 12"
        )

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val today = LocalDate.now()
            val dd = today.dayOfMonth
            val mm = today.monthValue
            val yy = today.year

            val dayInfo = try {
                DayInfoProvider().getDayInfo(dd, mm, yy)
            } catch (e: Exception) {
                null
            }

            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            // Solar date
            views.setTextViewText(R.id.tv_solar_day, dd.toString())

            // Day of week (LocalDate dayOfWeek: 1=Mon..7=Sun → index 0..6)
            val dowIndex = today.dayOfWeek.value - 1
            views.setTextViewText(R.id.tv_day_of_week, DAY_OF_WEEK_LABELS[dowIndex])

            // Month + year
            views.setTextViewText(
                R.id.tv_solar_month_year,
                "${MONTH_LABELS[mm - 1]} · $yy"
            )

            if (dayInfo != null) {
                val lunar = dayInfo.lunar
                val leapLabel = if (lunar.leap == 1) " nhuận" else ""
                val lunarText = "Mùng ${lunar.day} · ${lunar.monthName}$leapLabel Âm lịch"
                views.setTextViewText(R.id.tv_lunar, lunarText)

                views.setTextViewText(
                    R.id.tv_can_chi,
                    "Ngày ${dayInfo.dayCanChi} · Năm ${dayInfo.yearCanChi}"
                )

                val ratingText = if (!dayInfo.activities.isXauDay) {
                    "✦ Ngày Hoàng Đạo — ${dayInfo.dayRating.label}"
                } else {
                    "✗ Ngày Hắc Đạo"
                }
                views.setTextViewText(R.id.tv_day_rating, ratingText)

                // Color: green for good day, red for bad
                val ratingColor = if (!dayInfo.activities.isXauDay) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                views.setTextColor(R.id.tv_day_rating, ratingColor)
            }

            // Tap to open app — attached to root for full-widget tap target
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
