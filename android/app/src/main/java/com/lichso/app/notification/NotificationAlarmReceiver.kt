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
            // Fallback defaults — dùng khi đọc settings fail để vẫn reschedule được
            var reminderHour = 7
            var reminderMinute = 0

            // ── Bước 1: Đọc setting + fire notification (có timeout để tránh ANR) ──
            try {
                withTimeoutOrNull(8_000L) {
                    val prefs = context.settingsDataStore.data.first()
                    val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
                    reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
                    reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

                    when (type) {
                        NotificationAlarmScheduler.TYPE_DAILY -> {
                            if (notifyEnabled) fireDaily(context)
                        }
                        NotificationAlarmScheduler.TYPE_GIO_DAI_CAT -> {
                            val enabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
                            if (notifyEnabled && enabled) fireGioDaiCat(context)
                        }
                        NotificationAlarmScheduler.TYPE_FESTIVAL -> {
                            val enabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
                            if (notifyEnabled && enabled) fireFestival(context)
                        }
                        NotificationAlarmScheduler.TYPE_AI_TUVI -> {
                            if (notifyEnabled) fireAiTuVi(context)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotifAlarm", "Error firing $type: ${e.message}")
            }

            // ── Bước 2: LUÔN reschedule alarm cho lần kế tiếp ──
            // Kể cả khi bước 1 timeout / throw, ta vẫn phải đăng ký alarm
            // tiếp theo, nếu không chuỗi notification sẽ chết vĩnh viễn cho tới
            // khi user mở lại app (LichSoApp.scheduleWorkersFromSettings).
            // Đây chính là root cause khiến thông báo biến mất sau vài ngày.
            try {
                when (type) {
                    NotificationAlarmScheduler.TYPE_DAILY,
                    NotificationAlarmScheduler.TYPE_GIO_DAI_CAT -> {
                        NotificationAlarmScheduler.schedule(
                            context, type, reminderHour, reminderMinute
                        )
                    }
                    NotificationAlarmScheduler.TYPE_FESTIVAL -> {
                        NotificationAlarmScheduler.schedule(context, type, 20, 0)
                    }
                    NotificationAlarmScheduler.TYPE_AI_TUVI -> {
                        NotificationAlarmScheduler.schedule(context, type, 21, 0)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotifAlarm", "Reschedule $type failed: ${e.message}")
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

        // Nhãn ngày kiêng kỵ (nếu có) — ưu tiên Nguyệt kỵ trước Tam nương
        val kyLabel = when {
            dayInfo.activities.isNguyetKy -> "Ngày Nguyệt kỵ"
            dayInfo.activities.isTamNuong -> "Ngày Tam nương"
            else -> null
        }

        val gioText = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }

        val title = "${dayInfo.dayOfWeek}, $dd/$mm — $lunarStr"
        val subtitle = buildString {
            append(canChi)
            append(" | ${dayInfo.dayRating.label}")
            if (kyLabel != null) append(" | $kyLabel")
        }
        val lines = mutableListOf<String>()
        lines.add("Can Chi: $canChi")
        lines.add(
            if (kyLabel != null) "Đánh giá: ${dayInfo.dayRating.label} — $kyLabel"
            else "Đánh giá: ${dayInfo.dayRating.label}"
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

        val kyLabel = when {
            dayInfo.activities.isNguyetKy -> "Ngày Nguyệt kỵ"
            dayInfo.activities.isTamNuong -> "Ngày Tam nương"
            else -> null
        }

        val title = "Giờ Hoàng Đạo — ${dayInfo.dayOfWeek} $dd/$mm"
        val topGio = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }
        val subtitle = if (kyLabel != null) "$kyLabel | $topGio" else "${dayInfo.dayRating.label} | $topGio"
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

    /**
     * Fire AI Tử Vi notification.
     * - Nếu user đã setup đầy đủ profile (tên + ngày sinh hợp lệ) → gửi bản
     *   cá nhân hoá cho NGÀY MAI (21:00 tối là thời điểm người ta xem tử vi
     *   chuẩn bị cho hôm sau).
     * - Nếu chưa đủ profile → fallback về bản generic cũ.
     *
     * Hàm này là `suspend` để có thể đọc DataStore. Được gọi từ coroutine
     * scope trong onReceive.
     */
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
            android.util.Log.e("NotifAlarm", "Personal horoscope failed, fallback: ${e.message}")
        }
        // Fallback: user chưa setup profile → notification generic
        NotificationHelper.sendAiTuViNotification(context)
    }
}
