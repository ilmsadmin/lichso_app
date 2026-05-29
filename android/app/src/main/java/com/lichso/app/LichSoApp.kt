package com.lichso.app

import android.app.Application
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.lichso.app.analytics.Analytics
import com.lichso.app.data.auth.TokenManager
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.notification.AppIconBadgeManager
import com.lichso.app.notification.AppUpdateChecker
import com.lichso.app.notification.NotificationHelper
import com.lichso.app.notification.NotificationScheduler
import com.lichso.app.widget.CalendarWidgetScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltAndroidApp
class LichSoApp : Application() {

    @Inject lateinit var api: LichSoApi
    @Inject lateinit var tokenManager: TokenManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createChannels(this)
        Analytics.init(this)

        // Reschedule toàn bộ notification alarm từ settings + DB.
        // Idempotent — gọi lại an toàn ở mỗi cold start.
        appScope.launch {
            try {
                NotificationScheduler.rescheduleAll(this@LichSoApp)
            } catch (e: Exception) {
                android.util.Log.e("LichSoApp", "rescheduleAll failed: ${e.message}")
            }
        }

        // Keep launcher badge synced with unread notification count.
        appScope.launch {
            try {
                LichSoDatabase.getInstance(this@LichSoApp)
                    .notificationDao()
                    .getUnreadCount()
                    .collect { unreadCount ->
                        AppIconBadgeManager.applyCount(this@LichSoApp, unreadCount)
                    }
            } catch (e: Exception) {
                android.util.Log.e("LichSoApp", "badge sync failed: ${e.message}")
            }
        }

        CalendarWidgetScheduler.scheduleWidgetUpdates(this)
        AppUpdateChecker.schedule(this)

        // Đăng ký FCM token với backend mỗi lần khởi động.
        // onNewToken trong LichSoFirebaseMessagingService xử lý khi token đổi mới;
        // đây là fallback đảm bảo token luôn được sync dù service chưa chạy.
        appScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                tokenManager.saveFcmToken(fcmToken)
                val authToken = tokenManager.getAccessToken()
                val deviceId = Settings.Secure.getString(
                    contentResolver, Settings.Secure.ANDROID_ID
                ) ?: ""
                api.registerDeviceToken(
                    fcmToken = fcmToken,
                    appVersion = com.lichso.app.BuildConfig.VERSION_NAME,
                    deviceId = deviceId,
                    authToken = authToken,
                )
            } catch (e: Exception) {
                android.util.Log.w("LichSoApp", "FCM token registration failed: ${e.message}")
            }
        }
    }
}
