package com.lichso.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.util.LunarCalendarUtil

/**
 * Lên lịch / huỷ AlarmManager cho từng ReminderEntity.
 *
 * repeatType:
 *   0 = Once
 *   1 = Daily
 *   2 = Weekly
 *   3 = Monthly (solar)
 *   4 = MonthlyLunar — cùng ngày âm lịch mỗi tháng
 *   5 = Yearly — sinh nhật / giỗ; kết hợp với [ReminderEntity.useLunar] để
 *       quyết định dùng dương lịch hay âm lịch.
 *
 * [ReminderEntity.advanceDays] : nhắc trước N ngày so với ngày sự kiện.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }

        val now = System.currentTimeMillis()
        val advanceMs = reminder.advanceDays.coerceAtLeast(0) * DAY_MS

        // Tính thời điểm "event gốc" tiếp theo (chưa trừ advanceDays).
        // Với Once: nếu event đã qua → bỏ qua.
        val nextEvent: Long = when (reminder.repeatType) {
            1 -> nextRepeat(reminder.triggerTime, now + advanceMs, DAY_MS)       // Daily
            2 -> nextRepeat(reminder.triggerTime, now + advanceMs, 7 * DAY_MS)   // Weekly
            3 -> nextMonthly(reminder.triggerTime, now + advanceMs)              // Monthly solar
            4 -> nextMonthlyLunar(reminder.triggerTime, now + advanceMs)         // Monthly lunar
            5 -> nextYearly(reminder.triggerTime, now + advanceMs, reminder.useLunar)
            else -> {
                // Once: nếu đã qua (so với now + advance) → không schedule
                val t = reminder.triggerTime
                if (t - advanceMs < now) return else t
            }
        }

        // Trừ advanceDays để fire NHẮC TRƯỚC
        val nextTrigger = (nextEvent - advanceMs).let {
            // Safety: nếu sau khi trừ mà đã qua (chỉ có thể xảy ra với Once)
            // thì fire ngay lập tức (+5s) để vẫn thông báo được.
            if (it < now) now + 5_000L else it
        }

        val pi = buildPendingIntent(reminder)

        // Ưu tiên exact alarm để nhắc nhở fire đúng phút user đặt. Nếu thiết bị
        // không cho phép exact alarm (quyền SCHEDULE_EXACT_ALARM bị từ chối
        // trên Android 12+) thì fallback sang setAndAllowWhileIdle (inexact
        // nhưng vẫn bypass Doze).
        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true

            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextTrigger, pi
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextTrigger, pi
                )
            }
        } catch (se: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, nextTrigger, pi
            )
        }
        android.util.Log.i(
            "ReminderSched",
            "Scheduled #${reminder.id} '${reminder.title}' at ${java.util.Date(nextTrigger)} " +
                "(repeat=${reminder.repeatType}, lunar=${reminder.useLunar}, advance=${reminder.advanceDays}d)"
        )
    }

    fun cancel(reminderId: Long) {
        val pi = buildPendingIntent(
            ReminderEntity(id = reminderId, title = "", triggerTime = 0)
        )
        alarmManager.cancel(pi)
    }

    private fun buildPendingIntent(reminder: ReminderEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderReceiver.EXTRA_BODY, reminder.subtitle.ifBlank { "Đã đến giờ nhắc nhở!" })
        }
        return PendingIntent.getBroadcast(
            context,
            (reminder.id and 0x7FFFFFFF).toInt(), // Safe conversion: mask to positive int range
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextRepeat(base: Long, now: Long, interval: Long): Long {
        var t = base
        while (t < now) t += interval
        return t
    }

    private fun nextMonthly(base: Long, now: Long): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = base }
        val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        while (!cal.after(nowCal)) {
            cal.add(java.util.Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    /**
     * Tính lần fire tiếp theo cho reminder lặp hàng năm.
     * - Nếu [useLunar] = false: giữ nguyên dd/mm dương lịch, tăng năm cho đến
     *   khi > [now].
     * - Nếu [useLunar] = true: chuyển trigger sang âm lịch, rồi convert ngược
     *   lại dương lịch cho các năm kế tiếp cho đến khi > [now]. Cách này đảm
     *   bảo sinh nhật / giỗ âm lịch rơi đúng ngày âm lịch mỗi năm (thường
     *   lệch ~11 ngày so với dương lịch).
     */
    private fun nextYearly(base: Long, now: Long, useLunar: Boolean): Long {
        val baseCal = java.util.Calendar.getInstance().apply { timeInMillis = base }
        val hh = baseCal.get(java.util.Calendar.HOUR_OF_DAY)
        val mi = baseCal.get(java.util.Calendar.MINUTE)

        if (!useLunar) {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = base }
            val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }
            while (!cal.after(nowCal)) {
                cal.add(java.util.Calendar.YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Lunar yearly: convert base → lunar, rồi thử từng năm dương lịch
        // hiện tại trở đi để tìm ngày dương tương ứng (dd/mm/yy âm cố định).
        val baseDd = baseCal.get(java.util.Calendar.DAY_OF_MONTH)
        val baseMm = baseCal.get(java.util.Calendar.MONTH) + 1
        val baseYy = baseCal.get(java.util.Calendar.YEAR)
        val lunar = try {
            LunarCalendarUtil.convertSolar2Lunar(baseDd, baseMm, baseYy)
        } catch (e: Exception) {
            // Fallback sang dương lịch nếu convert fail
            return nextYearly(base, now, useLunar = false)
        }

        val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val startYear = nowCal.get(java.util.Calendar.YEAR) - 1
        for (y in startYear..startYear + 5) {
            val (sd, sm, sy) = try {
                LunarCalendarUtil.convertLunar2Solar(
                    lunar.lunarDay, lunar.lunarMonth, y, 0
                )
            } catch (e: Exception) {
                continue
            }
            if (sd == 0) continue // convert thất bại (tháng nhuận không có)
            val cand = java.util.Calendar.getInstance().apply {
                set(sy, sm - 1, sd, hh, mi, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (cand.timeInMillis > now) return cand.timeInMillis
        }
        // Fallback: trả về dương lịch cùng ngày năm tới
        return nextYearly(base, now, useLunar = false)
    }

    /**
     * Tính lần fire tiếp theo cho reminder lặp MỖI THÁNG ÂM LỊCH (repeatType=4).
     * Ví dụ: mùng 1, rằm (15) âm — fire mỗi tháng âm lịch.
     */
    private fun nextMonthlyLunar(base: Long, now: Long): Long {
        val baseCal = java.util.Calendar.getInstance().apply { timeInMillis = base }
        val hh = baseCal.get(java.util.Calendar.HOUR_OF_DAY)
        val mi = baseCal.get(java.util.Calendar.MINUTE)
        val baseDd = baseCal.get(java.util.Calendar.DAY_OF_MONTH)
        val baseMm = baseCal.get(java.util.Calendar.MONTH) + 1
        val baseYy = baseCal.get(java.util.Calendar.YEAR)
        val lunar = try {
            LunarCalendarUtil.convertSolar2Lunar(baseDd, baseMm, baseYy)
        } catch (e: Exception) {
            return nextMonthly(base, now) // fallback solar monthly
        }

        // Duyệt 15 tháng âm lịch tới, tìm tháng có lunarDay tương ứng > now
        var ly = lunar.lunarYear
        var lm = lunar.lunarMonth
        for (i in 0 until 15) {
            val (sd, sm, sy) = try {
                LunarCalendarUtil.convertLunar2Solar(lunar.lunarDay, lm, ly, 0)
            } catch (e: Exception) {
                Triple(0, 0, 0)
            }
            if (sd != 0) {
                val cand = java.util.Calendar.getInstance().apply {
                    set(sy, sm - 1, sd, hh, mi, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                if (cand.timeInMillis > now) return cand.timeInMillis
            }
            // tăng 1 tháng âm
            lm += 1
            if (lm > 12) { lm = 1; ly += 1 }
        }
        return nextMonthly(base, now)
    }

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
