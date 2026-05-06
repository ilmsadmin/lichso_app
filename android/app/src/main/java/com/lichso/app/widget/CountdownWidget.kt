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
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CountdownWidget::class.java))
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        private val FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            val today = LocalDate.now()

            val event = runBlocking {
                LichSoDatabase.getInstance(context)
                    .countdownEventDao()
                    .getPrimaryForWidget(today.toEpochDay())
            }

            if (event != null) {
                val targetDate = LocalDate.ofEpochDay(event.targetEpochDay)
                val daysLeft = event.targetEpochDay - today.toEpochDay()
                val lunar = LunarCalendarUtil.convertSolar2Lunar(
                    targetDate.dayOfMonth,
                    targetDate.monthValue,
                    targetDate.year,
                )

                views.setTextViewText(R.id.tv_countdown_days, daysLeft.toString())
                views.setTextViewText(R.id.tv_countdown_title, event.title)
                views.setTextViewText(
                    R.id.tv_countdown_date,
                    "${targetDate.format(FMT)} · ${lunar.lunarDay}/${lunar.lunarMonth} Âm"
                )
                views.setTextViewText(R.id.tv_countdown_sub, "ngày tới sự kiện")
            } else {
                views.setTextViewText(R.id.tv_countdown_days, "--")
                views.setTextViewText(R.id.tv_countdown_title, "Chưa có sự kiện")
                views.setTextViewText(R.id.tv_countdown_date, "Thêm trong mục Đếm ngược")
                views.setTextViewText(R.id.tv_countdown_sub, "")
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pi = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_countdown_root, pi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
