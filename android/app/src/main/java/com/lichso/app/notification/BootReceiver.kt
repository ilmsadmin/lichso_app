package com.lichso.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lichso.app.widget.CalendarWidgetScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reschedule toàn bộ alarm notification + widget sau khi:
 *  - Device reboot (BOOT_COMPLETED, LOCKED_BOOT_COMPLETED)
 *  - App được update (MY_PACKAGE_REPLACED)
 *  - User đổi giờ / múi giờ (TIME_SET, TIMEZONE_CHANGED)
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val handle = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        if (!handle) return

        // LOCKED_BOOT_COMPLETED: credential-protected DataStore/Room có thể chưa sẵn sàng,
        // bỏ qua — sẽ được handle ở BOOT_COMPLETED kế tiếp.
        val isLockedBoot = action == Intent.ACTION_LOCKED_BOOT_COMPLETED

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!isLockedBoot) {
                    withTimeoutOrNull(10_000L) {
                        NotificationScheduler.rescheduleAll(appContext)
                    }
                }
                CalendarWidgetScheduler.scheduleWidgetUpdates(appContext)
                CalendarWidgetScheduler.triggerImmediateUpdate(appContext)
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Reschedule failed: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
