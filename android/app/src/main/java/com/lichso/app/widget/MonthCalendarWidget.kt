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
import com.lichso.app.domain.model.CalendarDay
import java.time.LocalDate

/**
 * Widget 4×4 — Lịch Tháng (month calendar grid).
 * Supports ◀ ▶ navigation and "Hôm nay" reset.
 *
 * On API 31+: uses RemoteViews(light, dark) — system auto-switches
 * when dark mode changes, WITHOUT needing app process alive.
 * On API 26–30: falls back to layout-night/ resource qualifier.
 *
 * Grid is filled via partiallyUpdateAppWidget (row-by-row) to stay
 * under Binder transaction size limit.
 */
class MonthCalendarWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try {
            CalendarWidgetScheduler.scheduleWidgetUpdates(context.applicationContext)
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
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        when (action) {
            ACTION_PREV_MONTH, ACTION_NEXT_MONTH, ACTION_TODAY -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val today = LocalDate.now()

                val (year, month) = when (action) {
                    ACTION_TODAY -> Pair(today.year, today.monthValue)
                    else -> {
                        val savedYear = prefs.getInt("${KEY_YEAR}_$appWidgetId", today.year)
                        val savedMonth = prefs.getInt("${KEY_MONTH}_$appWidgetId", today.monthValue)
                        val date = LocalDate.of(savedYear, savedMonth, 1)
                        val newDate = if (action == ACTION_PREV_MONTH) date.minusMonths(1) else date.plusMonths(1)
                        Pair(newDate.year, newDate.monthValue)
                    }
                }

                // Save new state
                prefs.edit()
                    .putInt("${KEY_YEAR}_$appWidgetId", year)
                    .putInt("${KEY_MONTH}_$appWidgetId", month)
                    .apply()

                val mgr = AppWidgetManager.getInstance(context)
                try {
                    updateWidget(context, mgr, appWidgetId, year, month)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating widget $appWidgetId", e)
                }
            }
            in DATE_ACTIONS -> {
                // Date changed → reset all widgets to current month
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, MonthCalendarWidget::class.java))
                onUpdate(context, mgr, ids)
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (id in appWidgetIds) {
            editor.remove("${KEY_YEAR}_$id").remove("${KEY_MONTH}_$id")
        }
        editor.apply()
    }

    companion object {
        private const val TAG = "MonthCalWidget"
        private const val PREFS_NAME = "month_calendar_widget"
        private const val KEY_YEAR = "display_year"
        private const val KEY_MONTH = "display_month"

        const val ACTION_PREV_MONTH = "com.lichso.app.widget.ACTION_PREV_MONTH"
        const val ACTION_NEXT_MONTH = "com.lichso.app.widget.ACTION_NEXT_MONTH"
        const val ACTION_TODAY = "com.lichso.app.widget.ACTION_TODAY"

        private val DATE_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        private val CELL_IDS: Array<IntArray> = arrayOf(
            intArrayOf(R.id.cell_0_0, R.id.cell_0_1, R.id.cell_0_2, R.id.cell_0_3, R.id.cell_0_4, R.id.cell_0_5, R.id.cell_0_6),
            intArrayOf(R.id.cell_1_0, R.id.cell_1_1, R.id.cell_1_2, R.id.cell_1_3, R.id.cell_1_4, R.id.cell_1_5, R.id.cell_1_6),
            intArrayOf(R.id.cell_2_0, R.id.cell_2_1, R.id.cell_2_2, R.id.cell_2_3, R.id.cell_2_4, R.id.cell_2_5, R.id.cell_2_6),
            intArrayOf(R.id.cell_3_0, R.id.cell_3_1, R.id.cell_3_2, R.id.cell_3_3, R.id.cell_3_4, R.id.cell_3_5, R.id.cell_3_6),
            intArrayOf(R.id.cell_4_0, R.id.cell_4_1, R.id.cell_4_2, R.id.cell_4_3, R.id.cell_4_4, R.id.cell_4_5, R.id.cell_4_6),
            intArrayOf(R.id.cell_5_0, R.id.cell_5_1, R.id.cell_5_2, R.id.cell_5_3, R.id.cell_5_4, R.id.cell_5_5, R.id.cell_5_6)
        )

        /**
         * Update widget — uses saved month from prefs, or current month.
         * @param forceDark if non-null, overrides auto-detection of dark mode
         *                  (used by DarkModeWidgetObserver which knows the new state
         *                  before context.resources.configuration is updated).
         */
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int, forceDark: Boolean? = null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val today = LocalDate.now()
            val year = prefs.getInt("${KEY_YEAR}_$id", today.year)
            val month = prefs.getInt("${KEY_MONTH}_$id", today.monthValue)
            updateWidget(context, mgr, id, year, month, forceDark)
        }

        /**
         * Update widget for specific year/month.
         *
         * Dark mode strategy:
         * - Uses R.layout.widget_calendar_month — system auto-picks
         *   layout-night/widget_calendar_month.xml when dark mode is ON.
         * - Grid cell colors are embedded via SpannableString (isDark).
         * - DarkModeWidgetObserver triggers refresh when mode changes (process alive).
         * - On process restart, layout-night/ is already correct + isDark re-detected.
         */
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int, year: Int, month: Int, forceDark: Boolean? = null) {
            // Use forceDark if provided (from DarkModeWidgetObserver), otherwise auto-detect
            val isDark = forceDark ?: run {
                val nightMode = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

            try {
                val provider = DayInfoProvider()
                val days: List<CalendarDay> = provider.getCalendarDays(year, month)

                // Choose layout explicitly when forceDark is provided,
                // because the system resource qualifier may not have updated yet.
                val layoutId = if (forceDark != null) {
                    if (isDark) R.layout.widget_calendar_month_dark
                    else R.layout.widget_calendar_month
                } else {
                    // Let system auto-resolve via layout/ vs layout-night/
                    R.layout.widget_calendar_month
                }

                // === 1) Full update: header + nav buttons ===
                val headerViews = RemoteViews(context.packageName, layoutId)
                populateHeader(headerViews, context, id, year, month, days, provider)
                mgr.updateAppWidget(id, headerViews)

                // === 2) Partial updates: grid row-by-row (avoids Binder limit) ===
                val paddedDays = if (days.size < 42) {
                    days + List(42 - days.size) { null }
                } else {
                    days.take(42)
                }

                for (row in 0 until 6) {
                    val rowViews = RemoteViews(context.packageName, layoutId)
                    fillRow(rowViews, row, paddedDays, isDark)
                    mgr.partiallyUpdateAppWidget(id, rowViews)
                }

                Log.d(TAG, "updateWidget OK id=$id month=$month/$year isDark=$isDark days=${days.size}")
            } catch (e: Exception) {
                Log.e(TAG, "updateWidget FAILED id=$id", e)
                val views = RemoteViews(context.packageName, R.layout.widget_calendar_month)
                views.setTextViewText(R.id.tv_cal_month_title, "Tháng $month, $year")
                views.setTextViewText(R.id.tv_cal_lunar_month, "Lỗi tải lịch")
                mgr.updateAppWidget(id, views)
            }
        }

        private fun populateHeader(
            views: RemoteViews, context: Context, widgetId: Int,
            year: Int, month: Int,
            days: List<CalendarDay>, provider: DayInfoProvider
        ) {
            views.setTextViewText(R.id.tv_cal_month_title, "Tháng $month, $year")

            val firstCurrent = days.firstOrNull { it.isCurrentMonth }
            if (firstCurrent != null) {
                val dayInfo = provider.getDayInfo(1, month, year)
                views.setTextViewText(
                    R.id.tv_cal_lunar_month,
                    "${dayInfo.lunar.monthName} Âm lịch · ${dayInfo.yearCanChi}"
                )
            }

            // ◀ ▶ Hôm nay buttons
            views.setOnClickPendingIntent(R.id.btn_prev_month,
                buildNavIntent(context, widgetId, ACTION_PREV_MONTH, 100 + widgetId))
            views.setOnClickPendingIntent(R.id.btn_next_month,
                buildNavIntent(context, widgetId, ACTION_NEXT_MONTH, 200 + widgetId))
            views.setOnClickPendingIntent(R.id.tv_cal_today_btn,
                buildNavIntent(context, widgetId, ACTION_TODAY, 300 + widgetId))

            // Tap widget body → open app
            val appIntent = PendingIntent.getActivity(
                context, 20,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, appIntent)
        }

        private fun fillRow(
            views: RemoteViews, row: Int,
            paddedDays: List<CalendarDay?>, isDark: Boolean
        ) {
            for (col in 0 until 7) {
                val cellId = CELL_IDS[row][col]
                val idx = row * 7 + col
                val day = paddedDays.getOrNull(idx)

                if (day == null) {
                    views.setTextViewText(cellId, "")
                    continue
                }

                val solarStr = "${day.solarDay}"
                val lunarStr = day.lunarDisplayText
                val text = "$solarStr\n$lunarStr"

                // Solar day color — strong & prominent
                val solarColor = when {
                    day.isToday -> 0xFFFFFFFF.toInt()
                    !day.isCurrentMonth -> if (isDark) 0x44FFFFFF else 0x44857371
                    day.isSunday || day.isSaturday -> if (isDark) 0xFFFF6B6B.toInt() else 0xFFB71C1C.toInt()
                    day.isHoliday -> if (isDark) 0xFFFF6B6B.toInt() else 0xFFB71C1C.toInt()
                    else -> if (isDark) 0xFFFFFFFF.toInt() else 0xFF1C1B1F.toInt()
                }

                // Lunar day color — lighter/faded so it doesn't compete with solar
                val lunarColor = when {
                    day.isToday -> 0xB3FFFFFF.toInt()   // 70% white
                    !day.isCurrentMonth -> if (isDark) 0x33FFFFFF else 0x33857371
                    day.isSunday || day.isSaturday -> if (isDark) 0x99FF6B6B.toInt() else 0x99B71C1C.toInt()
                    day.isHoliday -> if (isDark) 0x99FF6B6B.toInt() else 0x99B71C1C.toInt()
                    else -> if (isDark) 0x80BBBBBB.toInt() else 0x80757575.toInt()
                }

                val spannable = android.text.SpannableString(text)

                // Solar day: bigger, medium weight (weight 500 — between regular and bold)
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(solarColor),
                    0, solarStr.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(1.3f),
                    0, solarStr.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.TypefaceSpan("sans-serif-medium"),
                    0, solarStr.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Lunar day: smaller, faded color, light font
                val lunarStart = solarStr.length + 1 // +1 for \n
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(lunarColor),
                    lunarStart, text.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.7f),
                    lunarStart, text.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.TypefaceSpan("sans-serif-light"),
                    lunarStart, text.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                views.setTextViewText(cellId, spannable)

                if (day.isToday) {
                    views.setInt(cellId, "setBackgroundResource", R.drawable.widget_cal_today_bg)
                }
            }
        }

        private fun buildNavIntent(context: Context, widgetId: Int, action: String, reqCode: Int): PendingIntent {
            val intent = Intent(context, MonthCalendarWidget::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            return PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
