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
 * Widget AI Tử Vi — hiển thị can chi hôm nay, đánh giá ngày, hướng tài lộc
 * và mời người dùng nhấn vào để hỏi AI.
 */
class AiWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("AiWidget", "onUpdate called, ids=${appWidgetIds.toList()}")
        for (appWidgetId in appWidgetIds) {
            try {
                updateWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e("AiWidget", "Error updating widget $appWidgetId", e)
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
            Log.d("AiWidget", "updateWidget id=$appWidgetId")
            val today = LocalDate.now()
            val dd = today.dayOfMonth
            val mm = today.monthValue
            val yy = today.year

            val views = RemoteViews(context.packageName, R.layout.widget_ai)

            // Date label — always set
            views.setTextViewText(R.id.tv_ai_date, "$dd/$mm")

            try {
                val dayInfo = DayInfoProvider().getDayInfo(dd, mm, yy)

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

                Log.d("AiWidget", "Data loaded: $dd/$mm/$yy, canchi=${dayInfo.dayCanChi}")
            } catch (e: Exception) {
                Log.e("AiWidget", "Error loading day info", e)
                views.setTextViewText(R.id.tv_ai_year_can_chi, "Đang tải...")
                views.setTextViewText(R.id.tv_ai_rating, "")
                views.setTextViewText(R.id.tv_ai_advice, "Nhấn để mở ứng dụng")
            }

            // Tap to open app (to AI chat screen) — attached to root for full-widget tap target
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
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("AiWidget", "updateAppWidget done for id=$appWidgetId")
        }
    }
}
