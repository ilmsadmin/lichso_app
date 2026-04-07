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
 * Widget AI Tử Vi — hiển thị can chi hôm nay, đánh giá ngày, hướng tài lộc
 * và mời người dùng nhấn vào để hỏi AI.
 */
class AiWidget : AppWidgetProvider() {

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
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, AiWidget::class.java)
            )
            onUpdate(context, manager, ids)
        }
    }

    companion object {
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

            val views = RemoteViews(context.packageName, R.layout.widget_ai)

            // Date label (e.g. "7/4")
            views.setTextViewText(R.id.tv_ai_date, "$dd/$mm")

            if (dayInfo != null) {
                // Year + day can chi
                views.setTextViewText(
                    R.id.tv_ai_year_can_chi,
                    "Năm ${dayInfo.yearCanChi} · Ngày ${dayInfo.dayCanChi}"
                )

                // Day rating
                val ratingLabel = if (!dayInfo.activities.isXauDay) {
                    "Ngày Hoàng Đạo — ${dayInfo.dayRating.label}"
                } else {
                    "Ngày Hắc Đạo — ${dayInfo.dayRating.label}"
                }
                views.setTextViewText(R.id.tv_ai_rating, ratingLabel)

                // Direction advice
                val huong = dayInfo.huong
                val adviceText = "Tài lộc: ${huong.thanTai} · Hỷ thần: ${huong.hyThan}"
                views.setTextViewText(R.id.tv_ai_advice, adviceText)
            }

            // Tap to open app (to AI chat screen)
            val intent = Intent(context, MainActivity::class.java).apply {
                action = "OPEN_AI_CHAT"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_ai_date, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
