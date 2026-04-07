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
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Widget provider for AI Horoscope widget showing daily fortune predictions
 */
class AiHoroscopeWidgetProvider : AppWidgetProvider() {

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
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
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
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_ai_horoscope)

            // Set date
            views.setTextViewText(R.id.widget_ai_date, today.format(dateFormatter))

            // Load user profile from settings
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val prefs = context.settingsDataStore.data.first()
                    val displayName = prefs[SettingsKeys.DISPLAY_NAME] ?: "Chưa đặt tên"
                    val birthYear = prefs[SettingsKeys.BIRTH_YEAR] ?: 2000
                    val birthMonth = prefs[SettingsKeys.BIRTH_MONTH] ?: 1
                    val birthDay = prefs[SettingsKeys.BIRTH_DAY] ?: 1

                    // Calculate zodiac and Can Chi
                    val dayInfoProvider = DayInfoProvider()
                    val birthDayInfo = try {
                        dayInfoProvider.getDayInfo(birthDay, birthMonth, birthYear)
                    } catch (e: Exception) {
                        null
                    }

                    val todayInfo = try {
                        dayInfoProvider.getDayInfo(
                            today.dayOfMonth,
                            today.monthValue,
                            today.year
                        )
                    } catch (e: Exception) {
                        null
                    }

                    if (birthDayInfo != null) {
                        views.setTextViewText(R.id.widget_ai_user_name, displayName)
                        views.setTextViewText(
                            R.id.widget_ai_user_zodiac,
                            "Tuổi: ${getConGiap(birthYear)}"
                        )
                        views.setTextViewText(
                            R.id.widget_ai_user_canchi,
                            "Can Chi: ${birthDayInfo.yearCanChi}"
                        )

                        // Generate simple fortune prediction based on day rating
                        val prediction = if (todayInfo != null) {
                            generateDailyPrediction(todayInfo.dayRating.score, todayInfo.dayCanChi)
                        } else {
                            "Chạm để xem lời khuyên chi tiết từ AI Tử Vi..."
                        }
                        views.setTextViewText(R.id.widget_ai_prediction, prediction)
                    } else {
                        views.setTextViewText(
                            R.id.widget_ai_user_name,
                            "Chưa có thông tin người dùng"
                        )
                        views.setTextViewText(R.id.widget_ai_user_zodiac, "Vui lòng cập nhật hồ sơ")
                        views.setTextViewText(R.id.widget_ai_user_canchi, "trong ứng dụng")
                        views.setTextViewText(
                            R.id.widget_ai_prediction,
                            "Chạm để mở ứng dụng và cập nhật thông tin cá nhân để nhận lời khuyên tử vi hàng ngày."
                        )
                    }

                    // Set up click listener to open the app (navigate to chat screen)
                    val intent = Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", "chat")
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_ai_user_section, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_ai_footer, pendingIntent)

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun getConGiap(year: Int): String {
            val conGiapList = arrayOf(
                "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tị",
                "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
            )
            val yearOffset = (year - 4) % 12
            return conGiapList[yearOffset]
        }

        private fun generateDailyPrediction(dayScore: Int, dayCanChi: String): String {
            return when {
                dayScore >= 8 -> "⭐⭐⭐ Hôm nay ($dayCanChi) là ngày rất tốt! Vận may đang ủng hộ bạn. Hãy tận dụng cơ hội để thực hiện các kế hoạch quan trọng. Sự nghiệp thuận lợi, tài lộc hanh thông."
                dayScore >= 6 -> "⭐⭐ Ngày ($dayCanChi) có vận tốt. Phù hợp cho công việc thường ngày và gặp gỡ bạn bè. Hãy giữ thái độ tích cực và hành động thận trọng."
                dayScore >= 4 -> "⭐ Ngày ($dayCanChi) trung bình. Nên tránh các quyết định lớn, tập trung vào việc hoàn thiện công việc đang làm. Tâm trạng có thể thay đổi, hãy bình tĩnh."
                else -> "⚠️ Ngày ($dayCanChi) không thuận lợi. Nên hạn chế ra quyết định quan trọng, tránh tranh cãi. Hãy kiên nhẫn và chờ đợi thời cơ tốt hơn."
            }
        }

        /**
         * Request update for all widget instances
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, AiHoroscopeWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, AiHoroscopeWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
