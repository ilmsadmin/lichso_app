package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate

/**
 * Broadcast receiver nhận alarm từ [NotificationAlarmScheduler].
 *
 * Xử lý cho cả 4 loại notification:
 *  - daily         : Tóm tắt ngày mới (fire theo giờ user cấu hình, mặc định 07:00)
 *  - gio_dai_cat   : Giờ Hoàng Đạo (cùng giờ với daily)
 *  - festival      : Nhắc ngày lễ ngày mai (20:00)
 *  - ai_tuvi       : Gợi ý AI Tử Vi (21:00)
 *
 * Sau khi fire xong tự reschedule alarm cho ngày hôm sau.
 */
class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(NotificationAlarmScheduler.EXTRA_TYPE) ?: return
        android.util.Log.i("NotifAlarm", "Received alarm for $type")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_000L) {
                    val prefs = context.settingsDataStore.data.first()
                    val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                    val reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
                    val reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

                    when (type) {
                        NotificationAlarmScheduler.TYPE_DAILY -> {
                            if (notifyEnabled) fireDaily(context)
                            // reschedule for tomorrow
                            NotificationAlarmScheduler.schedule(
                                context, type, reminderHour, reminderMinute
                            )
                        }
                        NotificationAlarmScheduler.TYPE_GIO_DAI_CAT -> {
                            val enabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
                            if (notifyEnabled && enabled) fireGioDaiCat(context)
                            NotificationAlarmScheduler.schedule(
                                context, type, reminderHour, reminderMinute
                            )
                        }
                        NotificationAlarmScheduler.TYPE_FESTIVAL -> {
                            val enabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
                            if (notifyEnabled && enabled) fireFestival(context)
                            NotificationAlarmScheduler.schedule(context, type, 20, 0)
                        }
                        NotificationAlarmScheduler.TYPE_AI_TUVI -> {
                            if (notifyEnabled) fireAiTuVi(context)
                            NotificationAlarmScheduler.schedule(context, type, 21, 0)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotifAlarm", "Error handling $type: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun fireDaily(context: Context) {
        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)

        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val lunarStr = "${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch"
        val canChi = dayInfo.dayCanChi
        val isGoodDay = !dayInfo.activities.isXauDay

        val gioText = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }

        val title = "${dayInfo.dayOfWeek}, $dd/$mm — $lunarStr"
        val subtitle = buildString {
            append(canChi)
            append(if (isGoodDay) " | Ngày Hoàng Đạo" else " | Ngày Hắc Đạo")
            append(" | ${dayInfo.dayRating.label}")
        }
        val lines = mutableListOf<String>()
        lines.add("Can Chi: $canChi")
        lines.add(
            if (isGoodDay) "Đánh giá: ${dayInfo.dayRating.label} — Ngày Hoàng Đạo"
            else "Đánh giá: ${dayInfo.dayRating.label} — Ngày Hắc Đạo"
        )
        lines.add("Giờ tốt: $gioText")
        lines.add("Trực ngày: ${dayInfo.trucNgay.name} | Sao: ${dayInfo.saoChieu.name}")
        lines.add("Hướng Thần Tài: ${dayInfo.huong.thanTai}")
        if (dayInfo.activities.nenLam.isNotEmpty()) {
            lines.add("Nên: ${dayInfo.activities.nenLam.take(3).joinToString(", ")}")
        }
        if (dayInfo.activities.khongNen.isNotEmpty()) {
            lines.add("Tránh: ${dayInfo.activities.khongNen.take(3).joinToString(", ")}")
        }
        dayInfo.solarHoliday?.let { lines.add("Ngày lễ: $it") }
        dayInfo.lunarHoliday?.let { lines.add("Âm lịch: $it") }

        NotificationHelper.sendDailyNotification(context, title, subtitle, lines)
    }

    private fun fireGioDaiCat(context: Context) {
        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)
        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val isGoodDay = !dayInfo.activities.isXauDay
        val title = "Giờ Hoàng Đạo — ${dayInfo.dayOfWeek} $dd/$mm"
        val topGio = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }
        val subtitle = if (isGoodDay) "Ngày Hoàng Đạo | $topGio" else "Ngày Hắc Đạo | $topGio"
        val lines = mutableListOf<String>()
        lines.add("${dayInfo.dayCanChi} — ${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch")
        dayInfo.gioHoangDao.forEach { gio -> lines.add("${gio.name}  ${gio.time}") }
        lines.add("Hướng Thần Tài: ${dayInfo.huong.thanTai}")
        lines.add("Hướng Hỷ Thần: ${dayInfo.huong.hyThan}")
        NotificationHelper.sendGioDaiCatNotification(context, title, subtitle, lines)
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
        if (lunar.lunarDay == 1) {
            festivals.add("Mùng 1 tháng ${lunar.lunarMonth} Âm lịch")
        } else if (lunar.lunarDay == 15) {
            festivals.add("Rằm tháng ${lunar.lunarMonth} Âm lịch")
        }
        if (festivals.isNotEmpty()) {
            val title = "Ngày lễ ngày mai — $dd/$mm/$yy"
            val subtitle = festivals.joinToString(" | ")
            val lines = mutableListOf<String>()
            lines.add("Ngày $dd/$mm/$yy (${lunar.lunarDay}/${lunar.lunarMonth} Âm lịch)")
            festivals.forEach { lines.add(it) }
            lines.add("Hãy chuẩn bị lễ vật và sắp xếp công việc phù hợp.")
            NotificationHelper.sendFestivalReminderNotification(context, title, subtitle, lines)
        } else {
            android.util.Log.d("NotifAlarm", "Festival: no holiday tomorrow ($dd/$mm)")
        }
    }

    private fun fireAiTuVi(context: Context) {
        NotificationHelper.sendAiTuViNotification(context)
    }
}
