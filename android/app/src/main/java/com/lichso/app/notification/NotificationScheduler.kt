package com.lichso.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lichso.app.MainActivity
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Scheduler DUY NHẤT cho mọi notification trong app.
 *
 * Triết lý thiết kế:
 *  - Một API duy nhất, không có worker/scheduler/receiver chồng chéo.
 *  - Dùng [AlarmManager.setAlarmClock] cho TẤT CẢ thông báo định giờ.
 *    Đây là API duy nhất KHÔNG bị Doze/Battery Saver trì hoãn và sống sót
 *    được trên các ROM tích cực kill background (Xiaomi MIUI, Vivo, Oppo,
 *    Realme...). Đánh đổi: status bar có icon "alarm sắp tới" — chấp nhận
 *    được với app calendar/reminder.
 *  - Self-healing: mỗi lần [NotificationReceiver] fire xong, nó gọi
 *    [rescheduleAll] để dựng lại toàn bộ chuỗi alarm — nếu OEM có drop
 *    alarm nào thì lần fire kế tiếp sẽ tự khôi phục.
 *
 * System notification định kỳ:
 *  - DAILY        : Chào buổi sáng tổng hợp thời tiết + ngày + giờ hoàng đạo
 *                   (giờ user cấu hình, mặc định 07:00)
 *  - FESTIVAL     : Nhắc ngày lễ ngày mai (20:00)
 *  - AI_TUVI      : Gợi ý AI Tử Vi tối (21:00)
 *
 * Per-row reminder: ReminderEntity từ Room (task, sinh nhật, giỗ...).
 */
object NotificationScheduler {

    // Alarm types
    const val TYPE_DAILY = "daily"
    const val TYPE_WEATHER = "weather"
    const val TYPE_GIO_DAI_CAT = "gio_dai_cat"
    const val TYPE_FESTIVAL = "festival"
    const val TYPE_AI_TUVI = "ai_tuvi"
    const val TYPE_REMINDER = "reminder"

    const val EXTRA_TYPE = "notif_type"
    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_BODY = "body"

    // PendingIntent request codes — chọn dải cao tránh trùng với reminder.id
    private const val REQ_BASE_SYSTEM = 0x10000000
    private const val REQ_DAILY = REQ_BASE_SYSTEM + 1
    private const val REQ_WEATHER = REQ_BASE_SYSTEM + 2
    private const val REQ_GIO_DAI_CAT = REQ_BASE_SYSTEM + 3
    private const val REQ_FESTIVAL = REQ_BASE_SYSTEM + 4
    private const val REQ_AI_TUVI = REQ_BASE_SYSTEM + 5

    private const val TAG = "NotifScheduler"
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Dựng lại TẤT CẢ alarm từ settings + DB. Idempotent.
     * Được gọi từ [com.lichso.app.LichSoApp.onCreate], [BootReceiver], và sau khi
     * [NotificationReceiver] fire xong (self-healing).
     */
    suspend fun rescheduleAll(context: Context) {
        val prefs = context.safeSettingsData.first()
        val notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true
        val festivalEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
        val hour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7
        val minute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0

        if (notifyEnabled) {
            scheduleDailyAt(context, hour, minute)
            // Morning weather and auspicious-hour content is now merged into DAILY.
            cancelWeatherMorning(context)
            cancelGioDaiCat(context)
            scheduleAiTuVi(context)
            if (festivalEnabled) scheduleFestival(context) else cancelFestival(context)
        } else {
            cancelDaily(context)
            cancelWeatherMorning(context)
            cancelAiTuVi(context)
            cancelGioDaiCat(context)
            cancelFestival(context)
        }

        // Per-row reminders
        try {
            val db = LichSoDatabase.getInstance(context)
            val all = db.reminderDao().getAllRemindersOnce()
            all.forEach { r ->
                if (notifyEnabled && r.isEnabled) scheduleReminder(context, r)
                else cancelReminder(context, r.id)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "rescheduleAll: DB read failed: ${e.message}")
        }
    }

    /** Bật/tắt master switch — gọi từ Settings sau khi đã save pref. */
    suspend fun applySettingsChange(context: Context) = rescheduleAll(context)

    // ── System notifications (4 loại định kỳ) ──

    fun scheduleDailyAt(context: Context, hour: Int, minute: Int) {
        scheduleAt(context, TYPE_DAILY, hour, minute, REQ_DAILY)
    }

    fun cancelDaily(context: Context) = cancelByReq(context, TYPE_DAILY, REQ_DAILY)

    fun scheduleWeatherMorning(context: Context) {
        // Cố định 07:00 như yêu cầu UX
        scheduleAt(context, TYPE_WEATHER, 7, 0, REQ_WEATHER)
    }

    fun cancelWeatherMorning(context: Context) = cancelByReq(context, TYPE_WEATHER, REQ_WEATHER)

    fun scheduleGioDaiCat(context: Context, hour: Int, minute: Int) {
        scheduleAt(context, TYPE_GIO_DAI_CAT, hour, minute, REQ_GIO_DAI_CAT)
    }

    fun cancelGioDaiCat(context: Context) = cancelByReq(context, TYPE_GIO_DAI_CAT, REQ_GIO_DAI_CAT)

    fun scheduleFestival(context: Context) {
        // Cố định 20:00
        scheduleAt(context, TYPE_FESTIVAL, 20, 0, REQ_FESTIVAL)
    }

    fun cancelFestival(context: Context) = cancelByReq(context, TYPE_FESTIVAL, REQ_FESTIVAL)

    fun scheduleAiTuVi(context: Context) {
        // Cố định 21:00
        scheduleAt(context, TYPE_AI_TUVI, 21, 0, REQ_AI_TUVI)
    }

    fun cancelAiTuVi(context: Context) = cancelByReq(context, TYPE_AI_TUVI, REQ_AI_TUVI)

    // ── Per-row reminder ──

    /**
     * Lên lịch (hoặc reschedule) cho 1 reminder của user.
     * Tự huỷ alarm cũ (cùng request code) trước khi đặt lại.
     */
    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id)
            return
        }
        val nextTrigger = computeNextReminderTrigger(reminder) ?: run {
            cancelReminder(context, reminder.id)
            android.util.Log.i(TAG, "Reminder #${reminder.id} '${reminder.title}' has no next trigger (once + past)")
            return
        }
        val pi = buildReminderPendingIntent(context, reminder)
        setExact(context, nextTrigger, pi)
        android.util.Log.i(
            TAG,
            "Reminder #${reminder.id} '${reminder.title}' at ${java.util.Date(nextTrigger)} " +
                "(repeat=${reminder.repeatType}, lunar=${reminder.useLunar}, advance=${reminder.advanceDays}d)"
        )
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.lichso.app.notification.REMINDER_$reminderId"
            putExtra(EXTRA_TYPE, TYPE_REMINDER)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            reminderReqCode(reminderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager(context).cancel(pi)
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTERNAL — schedule helpers
    // ─────────────────────────────────────────────────────────────────────

    private fun scheduleAt(context: Context, type: String, hour: Int, minute: Int, reqCode: Int) {
        val now = System.currentTimeMillis()
        val target = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DATE, 1)
        }
        val pi = buildSystemPendingIntent(context, type, reqCode)
        setExact(context, target.timeInMillis, pi)
        android.util.Log.i(
            TAG,
            "Scheduled $type at ${target.time} (in ${(target.timeInMillis - now) / 60_000} min)"
        )
    }

    private fun cancelByReq(context: Context, type: String, reqCode: Int) {
        val pi = buildSystemPendingIntent(context, type, reqCode)
        alarmManager(context).cancel(pi)
        android.util.Log.i(TAG, "Cancelled $type alarm")
    }

    /**
     * Đặt alarm chính xác sống sót Doze + OEM kill bằng [AlarmManager.setAlarmClock].
     * Đây là API duy nhất Google đảm bảo fire đúng giờ kể cả khi:
     *   - Thiết bị đang Doze (idle ban đêm)
     *   - App Standby Bucket = "rare"
     *   - Battery Saver bật
     *   - ROM Trung Quốc (Xiaomi MIUI, Oppo ColorOS, Vivo Funtouch, Realme)
     *
     * Trade-off: status bar có icon "next alarm" — chấp nhận được vì app
     * này về bản chất là calendar/reminder, user mong đợi thấy alarm sắp tới.
     */
    private fun setExact(context: Context, triggerAtMillis: Long, operation: PendingIntent) {
        val am = alarmManager(context)
        try {
            val showIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                operation
            )
        } catch (se: SecurityException) {
            // Hiếm khi xảy ra với setAlarmClock vì không cần quyền SCHEDULE_EXACT_ALARM,
            // nhưng vẫn fallback để chắc chắn alarm được đặt.
            android.util.Log.w(TAG, "setAlarmClock denied, fallback to setExactAndAllowWhileIdle: ${se.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                }
            } catch (_: SecurityException) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        }
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun buildSystemPendingIntent(context: Context, type: String, reqCode: Int): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.lichso.app.notification.SYS_$type"
            putExtra(EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildReminderPendingIntent(context: Context, reminder: ReminderEntity): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.lichso.app.notification.REMINDER_${reminder.id}"
            putExtra(EXTRA_TYPE, TYPE_REMINDER)
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_BODY, reminder.subtitle.ifBlank { "Đã đến giờ nhắc nhở!" })
        }
        return PendingIntent.getBroadcast(
            context, reminderReqCode(reminder.id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Map reminder.id (Long) → request code (Int < REQ_BASE_SYSTEM) tránh trùng system codes. */
    private fun reminderReqCode(reminderId: Long): Int {
        // Giữ trong dải [0, 0x0FFFFFFF] để chắc chắn không đụng REQ_BASE_SYSTEM.
        return (reminderId and 0x0FFFFFFFL).toInt()
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTERNAL — next-trigger calculation cho ReminderEntity
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Tính thời điểm fire kế tiếp cho 1 reminder (trừ advanceDays nếu có).
     * Trả về null nếu là Once và đã qua → không cần schedule.
     *
     * repeatType:
     *   0 = Once, 1 = Daily, 2 = Weekly, 3 = MonthlySolar,
     *   4 = MonthlyLunar, 5 = Yearly (solar hoặc lunar tuỳ useLunar)
     */
    private fun computeNextReminderTrigger(r: ReminderEntity): Long? {
        val now = System.currentTimeMillis()
        val advanceMs = r.advanceDays.coerceAtLeast(0) * DAY_MS
        val nextEvent: Long = when (r.repeatType) {
            1 -> nextRepeat(r.triggerTime, now + advanceMs, DAY_MS)
            2 -> nextRepeat(r.triggerTime, now + advanceMs, 7 * DAY_MS)
            3 -> nextMonthlySolar(r.triggerTime, now + advanceMs)
            4 -> nextMonthlyLunar(r.triggerTime, now + advanceMs)
            5 -> nextYearly(r.triggerTime, now + advanceMs, r.useLunar)
            else -> {
                // Once
                if (r.triggerTime - advanceMs < now) return null
                r.triggerTime
            }
        }
        val trigger = nextEvent - advanceMs
        return if (trigger < now) now + 5_000L else trigger
    }

    private fun nextRepeat(base: Long, now: Long, interval: Long): Long {
        var t = base
        while (t <= now) t += interval
        return t
    }

    private fun nextMonthlySolar(base: Long, now: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = base }
        while (cal.timeInMillis <= now) {
            cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    private fun nextYearly(base: Long, now: Long, useLunar: Boolean): Long {
        val baseCal = Calendar.getInstance().apply { timeInMillis = base }
        val hh = baseCal.get(Calendar.HOUR_OF_DAY)
        val mi = baseCal.get(Calendar.MINUTE)

        if (!useLunar) {
            val cal = Calendar.getInstance().apply { timeInMillis = base }
            while (cal.timeInMillis <= now) {
                cal.add(Calendar.YEAR, 1)
            }
            return cal.timeInMillis
        }

        val baseDd = baseCal.get(Calendar.DAY_OF_MONTH)
        val baseMm = baseCal.get(Calendar.MONTH) + 1
        val baseYy = baseCal.get(Calendar.YEAR)
        val lunar = try {
            LunarCalendarUtil.convertSolar2Lunar(baseDd, baseMm, baseYy)
        } catch (_: Exception) {
            return nextYearly(base, now, useLunar = false)
        }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val startYear = nowCal.get(Calendar.YEAR) - 1
        for (y in startYear..startYear + 5) {
            val (sd, sm, sy) = try {
                LunarCalendarUtil.convertLunar2Solar(lunar.lunarDay, lunar.lunarMonth, y, 0)
            } catch (_: Exception) { continue }
            if (sd == 0) continue
            val cand = Calendar.getInstance().apply {
                set(sy, sm - 1, sd, hh, mi, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cand.timeInMillis > now) return cand.timeInMillis
        }
        return nextYearly(base, now, useLunar = false)
    }

    private fun nextMonthlyLunar(base: Long, now: Long): Long {
        val baseCal = Calendar.getInstance().apply { timeInMillis = base }
        val hh = baseCal.get(Calendar.HOUR_OF_DAY)
        val mi = baseCal.get(Calendar.MINUTE)
        val baseDd = baseCal.get(Calendar.DAY_OF_MONTH)
        val baseMm = baseCal.get(Calendar.MONTH) + 1
        val baseYy = baseCal.get(Calendar.YEAR)
        val lunar = try {
            LunarCalendarUtil.convertSolar2Lunar(baseDd, baseMm, baseYy)
        } catch (_: Exception) {
            return nextMonthlySolar(base, now)
        }
        var ly = lunar.lunarYear
        var lm = lunar.lunarMonth
        for (i in 0 until 15) {
            val (sd, sm, sy) = try {
                LunarCalendarUtil.convertLunar2Solar(lunar.lunarDay, lm, ly, 0)
            } catch (_: Exception) { Triple(0, 0, 0) }
            if (sd != 0) {
                val cand = Calendar.getInstance().apply {
                    set(sy, sm - 1, sd, hh, mi, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cand.timeInMillis > now) return cand.timeInMillis
            }
            lm += 1
            if (lm > 12) { lm = 1; ly += 1 }
        }
        return nextMonthlySolar(base, now)
    }
}
