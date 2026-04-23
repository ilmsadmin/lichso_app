package com.lichso.app.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker chạy mỗi tối ~21h, gửi notification gợi ý
 * người dùng sử dụng chức năng Tử Vi AI.
 * Nội dung ngẫu nhiên mỗi ngày để tạo sự mới mẻ.
 */
class AiTuViWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val profile = PersonalHoroscopeHelper.loadProfile(applicationContext)
            if (profile != null) {
                val horoscope = PersonalHoroscopeHelper.buildHoroscope(
                    profile, java.time.LocalDate.now().plusDays(1)
                )
                NotificationHelper.sendPersonalHoroscopeNotification(
                    applicationContext,
                    title = horoscope.title,
                    subtitle = horoscope.subtitle,
                    shortBody = horoscope.shortBody,
                    lines = horoscope.lines
                )
            } else {
                NotificationHelper.sendAiTuViNotification(applicationContext)
            }
        } catch (e: Exception) {
            NotificationHelper.sendAiTuViNotification(applicationContext)
        }

        // Tự reschedule cho ngày mai 21h
        scheduleNext(applicationContext)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "ai_tuvi_nightly"

        /**
         * ⚠️ Đã chuyển sang AlarmManager ([NotificationAlarmScheduler]).
         * Fire 21:00 mỗi tối.
         */
        fun schedule(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.schedule(
                context, NotificationAlarmScheduler.TYPE_AI_TUVI, 21, 0
            )
        }

        internal fun scheduleNext(context: Context) {
            schedule(context)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationAlarmScheduler.cancel(context, NotificationAlarmScheduler.TYPE_AI_TUVI)
        }
    }
}
