package com.lichso.app.notification

import android.content.Context
import androidx.work.*
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker chạy hàng ngày, kiểm tra ngày mai có phải ngày lễ không.
 * Nếu có → gửi notification nhắc trước 1 ngày.
 */
class FestivalReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.settingsDataStore.data.first()
        val enabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true
        if (!enabled) return Result.success()

        val tomorrow = LocalDate.now().plusDays(1)
        val dd = tomorrow.dayOfMonth
        val mm = tomorrow.monthValue
        val yy = tomorrow.year

        val festivals = mutableListOf<String>()

        // Check solar holiday
        HolidayUtil.getSolarHoliday(dd, mm)?.let { festivals.add(it) }

        // Check lunar holiday
        val lunar = LunarCalendarUtil.convertSolar2Lunar(dd, mm, yy)
        HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)?.let { festivals.add(it) }

        // Check Rằm / Mùng 1
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
            NotificationHelper.sendFestivalReminderNotification(applicationContext, title, subtitle, lines)
        }

        // Tự reschedule cho ngày mai để đảm bảo đúng giờ
        scheduleNext(applicationContext)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "festival_reminder_daily"

        /**
         * ⚠️ Đã chuyển sang AlarmManager ([NotificationAlarmScheduler]).
         * Fire 20:00 mỗi ngày, chỉ hiển thị notification khi ngày mai có lễ.
         */
        fun schedule(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.schedule(
                context, NotificationAlarmScheduler.TYPE_FESTIVAL, 20, 0
            )
        }

        internal fun scheduleNext(context: Context) {
            schedule(context)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.cancel(context, NotificationAlarmScheduler.TYPE_FESTIVAL)
        }
    }
}
