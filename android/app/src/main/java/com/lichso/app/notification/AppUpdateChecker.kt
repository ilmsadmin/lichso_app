package com.lichso.app.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Worker chạy nền mỗi ngày một lần để kiểm tra có bản cập nhật mới trên
 * Google Play hay không (dùng Play In-App Update API).
 *
 * Nếu phát hiện bản mới:
 *  - Gửi system notification dẫn tới Google Play
 *  - Lưu vào in-app Notification Screen
 *
 * Worker tự tránh gửi trùng bằng cách ghi lại versionCode đã thông báo vào
 * SharedPreferences.
 */
class AppUpdateChecker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkForUpdate(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed: ${e.message}")
            // Retry lần sau nếu lỗi mạng
            Result.retry()
        }
    }

    private suspend fun checkForUpdate(context: Context) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfo = appUpdateManager.appUpdateInfo.await()

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val availableVersionCode = appUpdateInfo.availableVersionCode()
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastNotifiedCode = prefs.getInt(KEY_LAST_NOTIFIED_VERSION_CODE, 0)

            // Chỉ thông báo nếu chưa thông báo phiên bản này rồi
            if (availableVersionCode > lastNotifiedCode) {
                // Lấy tên phiên bản từ stalenessDays hint hoặc dùng mã version
                // Play API không cung cấp versionName trực tiếp nên ta dùng versionCode
                val versionLabel = inputData.getString(KEY_VERSION_NAME)
                    ?: "v${availableVersionCode}"

                Log.i(TAG, "New update available: $versionLabel (code=$availableVersionCode)")
                NotificationHelper.sendAppUpdateNotification(context, versionLabel)

                prefs.edit()
                    .putInt(KEY_LAST_NOTIFIED_VERSION_CODE, availableVersionCode)
                    .apply()
            } else {
                Log.d(TAG, "Update already notified for versionCode=$availableVersionCode")
            }
        } else {
            Log.d(TAG, "No update available (status=${appUpdateInfo.updateAvailability()})")
        }
    }

    companion object {
        private const val TAG = "AppUpdateChecker"
        private const val WORK_NAME = "app_update_check"
        private const val PREFS_NAME = "app_update_prefs"
        private const val KEY_LAST_NOTIFIED_VERSION_CODE = "last_notified_version_code"
        const val KEY_VERSION_NAME = "version_name"

        /**
         * Lên lịch kiểm tra update mỗi ngày một lần.
         * Dùng [ExistingPeriodicWorkPolicy.KEEP] để tránh schedule trùng lặp.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AppUpdateChecker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "AppUpdateChecker scheduled (daily, requires network)")
        }

        /** Huỷ lịch nếu cần (ví dụ người dùng tắt toàn bộ thông báo). */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
