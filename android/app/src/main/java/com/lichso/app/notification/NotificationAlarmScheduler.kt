package com.lichso.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Scheduler duy nhất cho các notification lặp lại hàng ngày.
 * Dùng [AlarmManager.setAndAllowWhileIdle] thay vì WorkManager để đảm bảo
 * notification fire đúng giờ ngay cả khi thiết bị ở Doze mode (buổi đêm,
 * máy idle) — WorkManager sẽ bị hoãn đến maintenance window (thường trễ
 * 1–6 giờ hoặc bị skip luôn trên ROM Xiaomi/Oppo/Vivo).
 *
 * Khi alarm fire, [NotificationAlarmReceiver] sẽ xử lý logic và tự động
 * reschedule cho ngày kế tiếp.
 */
object NotificationAlarmScheduler {

    const val TYPE_DAILY = "daily"
    const val TYPE_GIO_DAI_CAT = "gio_dai_cat"
    const val TYPE_FESTIVAL = "festival"
    const val TYPE_AI_TUVI = "ai_tuvi"

    const val EXTRA_TYPE = "notif_type"

    // PendingIntent request codes (unique per type)
    private const val REQ_DAILY = 20001
    private const val REQ_GIO_DAI_CAT = 20002
    private const val REQ_FESTIVAL = 20003
    private const val REQ_AI_TUVI = 20004

    /**
     * Lên lịch alarm cho một trong 4 loại notification.
     * Tự động tính thời điểm tiếp theo (hôm nay hoặc ngày mai nếu đã qua).
     */
    fun schedule(context: Context, type: String, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DATE, 1)
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, type)

        // Ưu tiên exact alarm để notification fire đúng giờ. Trên Android 12+
        // cần quyền SCHEDULE_EXACT_ALARM; nếu user/OEM từ chối thì fallback
        // về setAndAllowWhileIdle (inexact nhưng vẫn bypass Doze).
        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                am.canScheduleExactAlarms()
            } else true

            if (canExact) {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, target.timeInMillis, pi
                )
            } else {
                am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, target.timeInMillis, pi
                )
            }
        } catch (se: SecurityException) {
            // Một số ROM ném SecurityException dù canScheduleExactAlarms trả true
            am.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, target.timeInMillis, pi
            )
        }
        android.util.Log.i(
            "NotifAlarm",
            "Scheduled $type at ${target.time} (in ${(target.timeInMillis - now.timeInMillis) / 1000 / 60} min)"
        )
    }

    fun cancel(context: Context, type: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, type))
        android.util.Log.i("NotifAlarm", "Cancelled $type alarm")
    }

    private fun buildPendingIntent(context: Context, type: String): PendingIntent {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            action = "com.lichso.app.notification.ALARM_$type"
            putExtra(EXTRA_TYPE, type)
        }
        val reqCode = when (type) {
            TYPE_DAILY -> REQ_DAILY
            TYPE_GIO_DAI_CAT -> REQ_GIO_DAI_CAT
            TYPE_FESTIVAL -> REQ_FESTIVAL
            TYPE_AI_TUVI -> REQ_AI_TUVI
            else -> type.hashCode()
        }
        return PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
