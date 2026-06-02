package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import com.lichso.app.widget.WidgetWeatherHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate

/**
 * BroadcastReceiver DUY NHẤT cho mọi alarm trong app.
 *
 * Dispatch theo [NotificationScheduler.EXTRA_TYPE]:
 *  - daily / festival / ai_tuvi : system notification định kỳ
 *  - weather / gio_dai_cat : legacy alarm cũ, không gửi riêng nữa
 *  - reminder : per-row ReminderEntity của user
 *
 * Sau khi fire xong → gọi [NotificationScheduler.rescheduleAll] để self-heal:
 * dựng lại toàn bộ chuỗi alarm. Cách này đảm bảo nếu OEM có drop alarm nào
 * khác (xảy ra trên Xiaomi/Vivo/Oppo sau vài ngày idle) thì lần fire kế tiếp
 * sẽ tự khôi phục, KHÔNG cần user mở app.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(NotificationScheduler.EXTRA_TYPE) ?: return
        android.util.Log.i(TAG, "Received alarm: type=$type")

        val pendingResult = goAsync()
        // Capture extras now (intent có thể bị recycle)
        val reminderId = intent.getLongExtra(NotificationScheduler.EXTRA_REMINDER_ID, -1L)
        val reminderTitle = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE)
        val reminderBody = intent.getStringExtra(NotificationScheduler.EXTRA_BODY)
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_000L) {
                    when (type) {
                        NotificationScheduler.TYPE_DAILY -> fireMorningSummary(appContext)
                        NotificationScheduler.TYPE_WEATHER,
                        NotificationScheduler.TYPE_GIO_DAI_CAT -> {
                            android.util.Log.i(TAG, "Skipping legacy morning alarm: type=$type")
                        }
                        NotificationScheduler.TYPE_FESTIVAL -> fireFestivalGuarded(appContext)
                        NotificationScheduler.TYPE_AI_TUVI -> fireAiTuViGuarded(appContext)
                        NotificationScheduler.TYPE_REMINDER -> fireReminder(
                            appContext, reminderId, reminderTitle, reminderBody
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Fire $type failed: ${e.message}", e)
            }

            // Self-healing: dựng lại TOÀN BỘ alarm chain.
            // Nếu type là per-row reminder → reschedule chính nó. Nếu là
            // system → rescheduleAll cũng đặt lại nó cho hôm sau.
            try {
                if (type == NotificationScheduler.TYPE_REMINDER && reminderId > 0) {
                    val r = LichSoDatabase.getInstance(appContext)
                        .itemDao().getById(reminderId)
                    if (r != null && r.hasReminder && r.reminderEnabled && r.repeatType != 0) {
                        NotificationScheduler.scheduleReminder(appContext, r)
                    }
                } else {
                    NotificationScheduler.rescheduleAll(appContext)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Reschedule after $type failed: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Fire handlers
    // ─────────────────────────────────────────────────────────────────────

    private suspend fun fireFestivalGuarded(context: Context) {
        val prefs = context.safeSettingsData.first()
        val notify = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
        val enabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
        if (notify && enabled) fireFestival(context)
    }

    private suspend fun fireAiTuViGuarded(context: Context) {
        val prefs = context.safeSettingsData.first()
        val notify = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
        if (notify) fireAiTuVi(context)
    }

    private fun fireReminder(context: Context, id: Long, title: String?, body: String?) {
        if (id <= 0L) return
        NotificationHelper.sendReminderNotification(
            context, id.toInt(),
            title ?: "Nhắc nhở",
            body ?: ""
        )
    }

    private suspend fun fireMorningSummary(context: Context) {
        val prefs = context.safeSettingsData.first()
        val cityName = prefs[SettingsKeys.LOCATION_NAME] ?: "Hà Nội"
        val tempUnit = prefs[SettingsKeys.TEMP_UNIT] ?: "°C"
        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)

        val weatherLines = buildWeatherLines(context, cityName, tempUnit)
        val dayLines = buildDayLines(dayInfo, today)
        val subtitle = weatherLines.firstOrNull()
            ?: "${dayInfo.dayOfWeek}, ${"%02d".format(today.dayOfMonth)}/${"%02d".format(today.monthValue)}"

        NotificationHelper.sendMorningSummaryNotification(
            context = context,
            subtitle = subtitle,
            lines = weatherLines + dayLines
        )
    }

    private suspend fun buildWeatherLines(
        context: Context,
        cityName: String,
        tempUnit: String
    ): List<String> {
        val weather = WidgetWeatherHelper.fetchAndCacheWeather(context, cityName)
            ?: return listOf("Thời tiết: chưa lấy được dữ liệu tại $cityName")

        val currentTempC = weather.temperature
        val maxTempC = weather.tempMax
        val minTempC = weather.tempMin
        val unitLabel: String
        val currentTemp: Int
        val maxTemp: Int
        val minTemp: Int
        if (tempUnit == "°F") {
            val cToF: (Double) -> Int = { ((it * 9.0 / 5.0) + 32.0).toInt() }
            unitLabel = "°F"
            currentTemp = cToF(currentTempC)
            maxTemp = cToF(maxTempC)
            minTemp = cToF(minTempC)
        } else {
            unitLabel = "°C"
            currentTemp = currentTempC.toInt()
            maxTemp = maxTempC.toInt()
            minTemp = minTempC.toInt()
        }

        val tempRangeText = "$minTemp$unitLabel-$maxTemp$unitLabel"
        val advice = when {
            maxTempC >= 34 -> "Nắng khá gắt, nhớ mang nước và che nắng khi ra ngoài."
            maxTempC >= 30 -> "Trưa có thể nắng nóng, nên mang ô hoặc áo khoác mỏng."
            minTempC <= 18 -> "Sáng sớm khá mát, nên mặc thêm áo khoác nhẹ."
            weather.humidity >= 85 -> "Độ ẩm cao, có thể oi nhẹ. Uống đủ nước để giữ sức."
            else -> "Thời tiết tương đối dễ chịu, chúc bạn một ngày nhiều năng lượng."
        }

        return listOf(
            "${weather.cityName}: $currentTemp$unitLabel, $tempRangeText, ${weather.description}",
            advice
        )
    }

    private fun buildDayLines(dayInfo: com.lichso.app.domain.model.DayInfo, today: LocalDate): List<String> {
        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val kyLabel = when {
            dayInfo.activities.isNguyetKy -> "Ngày Nguyệt kỵ"
            dayInfo.activities.isTamNuong -> "Ngày Tam nương"
            else -> null
        }
        val lunarStr = "${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch"
        val gioText = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }
        val lines = mutableListOf<String>()
        lines.add("Ngày: ${dayInfo.dayOfWeek} $dd/$mm - $lunarStr")
        lines.add("Can Chi: ${dayInfo.dayCanChi} - ${dayInfo.dayRating.label}")
        if (kyLabel != null) lines.add("Lưu ý: $kyLabel")
        lines.add("Giờ hoàng đạo: $gioText")
        if (dayInfo.activities.nenLam.isNotEmpty()) {
            lines.add("Nên: ${dayInfo.activities.nenLam.take(2).joinToString(", ")}")
        }
        if (dayInfo.activities.khongNen.isNotEmpty()) {
            lines.add("Tránh: ${dayInfo.activities.khongNen.take(2).joinToString(", ")}")
        }
        return lines
    }

    private fun fireFestival(context: Context) {
        val tomorrow = LocalDate.now().plusDays(1)
        val dd = tomorrow.dayOfMonth
        val mm = tomorrow.monthValue
        val yy = tomorrow.year
        val festivals = mutableListOf<String>()
        HolidayUtil.getSolarHoliday(dd, mm)?.let { festivals.add(it) }
        val lunar = LunarCalendarUtil.convertSolar2Lunar(dd, mm, yy)
        HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)?.let { festivals.add(it) }
        if (lunar.lunarDay == 1) festivals.add("Mùng 1 tháng ${lunar.lunarMonth} Âm lịch")
        else if (lunar.lunarDay == 15) festivals.add("Rằm tháng ${lunar.lunarMonth} Âm lịch")
        if (festivals.isEmpty()) {
            android.util.Log.d(TAG, "Festival: no holiday tomorrow ($dd/$mm)")
            return
        }
        val title = "Ngày lễ ngày mai — $dd/$mm/$yy"
        val subtitle = festivals.joinToString(" | ")
        val lines = mutableListOf<String>()
        lines.add("Ngày $dd/$mm/$yy (${lunar.lunarDay}/${lunar.lunarMonth} Âm lịch)")
        festivals.forEach { lines.add(it) }
        lines.add("Hãy chuẩn bị lễ vật và sắp xếp công việc phù hợp.")
        NotificationHelper.sendFestivalReminderNotification(context, title, subtitle, lines)
    }

    private suspend fun fireAiTuVi(context: Context) {
        try {
            val profile = PersonalHoroscopeHelper.loadProfile(context)
            if (profile != null) {
                val horoscope = PersonalHoroscopeHelper.buildHoroscope(
                    profile, LocalDate.now().plusDays(1)
                )
                NotificationHelper.sendPersonalHoroscopeNotification(
                    context,
                    title = horoscope.title,
                    subtitle = horoscope.subtitle,
                    shortBody = horoscope.shortBody,
                    lines = horoscope.lines
                )
                return
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Personal horoscope failed, fallback: ${e.message}")
        }
        NotificationHelper.sendAiTuViNotification(context)
    }

    companion object {
        private const val TAG = "NotifReceiver"
    }
}
