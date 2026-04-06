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

        val gioText = dayInfo.gioHoangDao
            .take(6)
            .joinToString(" · ") { "${it.name} (${it.time})" }

        val body = "Giờ tốt hôm nay (${today.dayOfMonth}/${today.monthValue}): $gioText"
        NotificationHelper.sendGioDaiCatNotification(applicationContext, body)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "gio_dai_cat_daily"

        /**
         * Lên lịch worker chạy hàng ngày vào giờ được cấu hình (mặc định 6h sáng).
         */
        fun schedule(context: Context, hour: Int = 6, minute: Int = 0) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DATE, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<GioDaiCatWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
