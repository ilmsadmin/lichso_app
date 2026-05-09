package com.lichso.app

import android.app.Application
import com.lichso.app.analytics.Analytics
import com.lichso.app.data.local.LichSoDatabase
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

@HiltAndroidApp
class LichSoApp : Application() {

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
    }
}
