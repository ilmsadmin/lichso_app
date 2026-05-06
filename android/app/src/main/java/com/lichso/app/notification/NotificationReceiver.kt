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
 *  - daily / gio_dai_cat / festival / ai_tuvi : 4 system notification định kỳ
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
                        NotificationScheduler.TYPE_DAILY -> fireDaily(appContext)
                        NotificationScheduler.TYPE_GIO_DAI_CAT -> fireGioDaiCatGuarded(appContext)
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
                        .reminderDao().getAllRemindersOnce()
                        .find { it.id == reminderId }
                    if (r != null && r.isEnabled && r.repeatType != 0) {
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

    private suspend fun fireGioDaiCatGuarded(context: Context) {
        val prefs = context.safeSettingsData.first()
        val notify = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
        val enabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
        if (notify && enabled) fireGioDaiCat(context)
    }

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

    private fun fireDaily(context: Context) {
        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)
        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val lunarStr = "${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch"
        val canChi = dayInfo.dayCanChi
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
        SmartReminderProvider.contextualHintFor(dayInfo)?.let { hint -> lines.add("💡 $hint") }
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
