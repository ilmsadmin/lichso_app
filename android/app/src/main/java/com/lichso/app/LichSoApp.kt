package com.lichso.app

import android.app.Application
import android.os.Build
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

        // Analytics nhẹ — giữ trên main thread để event sớm không bị mất.
        Analytics.init(this)

        // Các tác vụ khởi động KHÔNG cần xong trước frame đầu tiên đều đẩy xuống
        // background để không chặn main thread (giảm thời gian cold start mà
        // Play Console đo — "App startup time"):
        //  - createChannels(): 16 notification channel = 16 lệnh IPC sang system.
        //  - WorkManager init + enqueue (widget update / app-update check).
        //  - rescheduleAll(): đọc settings + DB rồi đặt lại alarm.
        // Tất cả đều idempotent — gọi lại an toàn ở mỗi cold start.
        appScope.launch {
            try {
                NotificationHelper.createChannels(this@LichSoApp)
                CalendarWidgetScheduler.scheduleWidgetUpdates(this@LichSoApp)
                AppUpdateChecker.schedule(this@LichSoApp)
                NotificationScheduler.rescheduleAll(this@LichSoApp)
            } catch (e: Exception) {
                android.util.Log.e("LichSoApp", "startup chores failed: ${e.message}")
            }
        }

        // Deferred deep link: đọc Install Referrer 1 lần ở lần cài đầu để ad mở
        // đúng màn (kết quả lưu DataStore, tiêu thụ khi vào Main). Idempotent.
        appScope.launch {
            try {
                com.lichso.app.deeplink.InstallReferrerManager.fetchAndStoreIfNeeded(this@LichSoApp)
            } catch (e: Exception) {
                android.util.Log.w("LichSoApp", "install referrer fetch failed: ${e.message}")
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

        // Mỗi lần khởi động:
        //  1. Đảm bảo luôn có backend token — nếu chưa đăng nhập Google thì tạo
        //     phiên guest ẩn danh (theo device_id) để user nào cũng có bản ghi ở
        //     bảng users và mọi request đều được gắn vào user đó.
        //  2. Đăng ký FCM device token với token hiện tại (guest hoặc user) nên
        //     thiết bị luôn được link đúng người dùng.
        appScope.launch {
            try {
                val deviceId = tokenManager.getInstallationId()
                val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

                // 1. Ensure a backend session (guest fallback).
                if (tokenManager.getAccessToken() == null && deviceId.isNotEmpty()) {
                    api.guestLogin(deviceId = deviceId, deviceName = deviceName)
                        .getOrNull()?.let { resp ->
                            tokenManager.saveTokens(resp.accessToken, resp.refreshToken)
                        }
                }

                // 2. Register FCM device token under the current session.
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                tokenManager.saveFcmToken(fcmToken)
                api.registerDeviceToken(
                    fcmToken = fcmToken,
                    appVersion = com.lichso.app.BuildConfig.VERSION_NAME,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    authToken = tokenManager.getAccessToken(),
                )
            } catch (e: Exception) {
                android.util.Log.w("LichSoApp", "Startup session/FCM registration failed: ${e.message}")
            }
        }
    }
}
