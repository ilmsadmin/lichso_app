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
        private const val TAG = "AiWidget"

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
            Log.d(TAG, "updateWidget id=$appWidgetId forceDark=$forceDark")
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
                if (isDark) R.layout.widget_ai_dark
                else R.layout.widget_ai
            } else {
                // Let system auto-resolve via layout/ vs layout-night/
                R.layout.widget_ai
            }

            val views = RemoteViews(context.packageName, layoutId)

            // Date badge in header
            views.setTextViewText(R.id.tv_ai_date, "$dd/$mm")

            try {
                val dayInfo = DayInfoProvider().getDayInfo(dd, mm, yy)

                // Status line
                views.setTextViewText(R.id.tv_ai_status, "● Đang hoạt động")

                // AI bubble — summary of today
                val lunar = dayInfo.lunar
                val lunarDayLabel = if (lunar.day <= 10) "Mùng ${lunar.day}" else "${lunar.day}"
                val ratingText = if (!dayInfo.activities.isXauDay) {
                    "Ngày Hoàng Đạo"
                } else {
                    "Ngày Hắc Đạo"
                }
                val huong = dayInfo.huong
                val bubbleText = "✦ Hôm nay $ratingText, $lunarDayLabel/${lunar.monthName} Âm. " +
                    "Hướng tài lộc ${huong.thanTai}"
                views.setTextViewText(R.id.tv_ai_bubble, bubbleText)

                // User prompt bubble
                views.setTextViewText(R.id.tv_ai_user_bubble, "Hỏi AI tử vi ›")

                Log.d(TAG, "Data loaded: $dd/$mm/$yy, canchi=${dayInfo.dayCanChi}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading day info", e)
                views.setTextViewText(R.id.tv_ai_bubble, "✦ Nhấn để xem tử vi hôm nay")
                views.setTextViewText(R.id.tv_ai_user_bubble, "Hỏi AI tử vi ›")
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
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "updateAppWidget done for id=$appWidgetId isDark=$isDark")
        }
    }
}
