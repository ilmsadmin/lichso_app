package com.lichso.app.notification

import android.content.Context
import androidx.work.*
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker chạy mỗi sáng, gửi notification danh sách giờ hoàng đạo hôm nay.
 */
class GioDaiCatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Kiểm tra setting giờ đại cát có bật không
        val prefs = applicationContext.settingsDataStore.data.first()
        val enabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false
        if (!enabled) return Result.success()

        val today = LocalDate.now()
        val dayInfo = DayInfoProvider().getDayInfo(today.dayOfMonth, today.monthValue, today.year)

        val dd = "%02d".format(today.dayOfMonth)
        val mm = "%02d".format(today.monthValue)
        val isGoodDay = !dayInfo.activities.isXauDay

        // Title: professional, no emoji
        val title = "Giờ Hoàng Đạo — ${dayInfo.dayOfWeek} $dd/$mm"

        // Subtitle for collapsed view
        val topGio = dayInfo.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }
        val subtitle = if (isGoodDay) "Ngày Hoàng Đạo | $topGio" else "Ngày Hắc Đạo | $topGio"

        // Expanded lines for InboxStyle
        val lines = mutableListOf<String>()
        lines.add("${dayInfo.dayCanChi} — ${dayInfo.lunar.day}/${dayInfo.lunar.month} Âm lịch")
        dayInfo.gioHoangDao.forEach { gio ->
            lines.add("${gio.name}  ${gio.time}")
        }
        lines.add("Hướng Thần Tài: ${dayInfo.huong.thanTai}")
        lines.add("Hướng Hỷ Thần: ${dayInfo.huong.hyThan}")

        NotificationHelper.sendGioDaiCatNotification(applicationContext, title, subtitle, lines)

        // Tự reschedule cho ngày mai để đảm bảo đúng giờ
        val hour = prefs[SettingsKeys.REMINDER_HOUR] ?: 6
        val minute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0
        scheduleNext(applicationContext, hour, minute)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "gio_dai_cat_daily"

        /**
         * ⚠️ Đã chuyển từ WorkManager sang AlarmManager (xem [NotificationAlarmScheduler])
         * để fire đúng giờ kể cả khi máy ở Doze mode. Hàm giữ signature cũ.
         */
        fun schedule(context: Context, hour: Int = 6, minute: Int = 0) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.schedule(
                context, NotificationAlarmScheduler.TYPE_GIO_DAI_CAT, hour, minute
            )
        }

        internal fun scheduleNext(context: Context, hour: Int, minute: Int) {
            schedule(context, hour, minute)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.cancel(context, NotificationAlarmScheduler.TYPE_GIO_DAI_CAT)
        }
    }
}
