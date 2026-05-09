package com.lichso.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lichso.app.MainActivity
import com.lichso.app.R
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * Quản lý badge số thông báo chưa đọc trên icon ứng dụng.
 *
 * Chiến lược hai lớp:
 * 1. ShortcutBadger  — hoạt động trên Samsung/MIUI/OPPO/HTC.
 * 2. Badge-carrier notification — hoạt động trên Pixel/AOSP.
 *    Đây là một notification silent, IMPORTANCE_MIN, có `.setNumber(count)`.
 *    Các launcher AOSP đọc giá trị `number` của notification chưa bị dismiss
 *    để hiển thị badge. Khi count == 0 thì cancel notification này.
 */
object AppIconBadgeManager {

    // v2: đổi channel ID để thoát khỏi IMPORTANCE_MIN cũ (Android không cho nâng cấp importance)
    private const val CHANNEL_BADGE      = "channel_badge_carrier_v2"
    private const val CHANNEL_BADGE_OLD  = "channel_badge_carrier"
    private const val NOTIF_ID_BADGE     = 99_991

    @Volatile private var channelCreated = false

    fun applyCount(context: Context, unreadCount: Int) {
        val count = unreadCount.coerceAtLeast(0)

        // ── Layer 1: ShortcutBadger (Samsung, MIUI, OPPO, HTC …) ──
        runCatching { ShortcutBadger.applyCount(context, count) }

        // ── Layer 2: Badge-carrier notification (Pixel / AOSP) ──
        ensureBadgeChannel(context)
        val nm = NotificationManagerCompat.from(context)

        if (count > 0 && hasPostPermission(context)) {
            val pi = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to", "notifications")
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notif = NotificationCompat.Builder(context, CHANNEL_BADGE)
                .setSmallIcon(R.drawable.ic_notif_bell)
                .setContentTitle(
                    if (count == 1) "1 thông báo chưa đọc"
                    else "$count thông báo chưa đọc"
                )
                .setContentText("Nhấn để xem thông báo")
                .setNumber(count)
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setSilent(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
            runCatching { nm.notify(NOTIF_ID_BADGE, notif) }
        } else {
            // count == 0 hoặc không có quyền → xóa badge carrier
            runCatching { nm.cancel(NOTIF_ID_BADGE) }
        }
    }

    private fun hasPostPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun ensureBadgeChannel(context: Context) {
        if (channelCreated) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Xóa channel cũ (IMPORTANCE_MIN — không hoạt động trên AOSP/Pixel)
        runCatching { nm.deleteNotificationChannel(CHANNEL_BADGE_OLD) }

        // Tạo channel mới với IMPORTANCE_LOW:
        // - không âm thanh, không heads-up, không rung
        // - VẪN hiển thị trong notification shade → AOSP launcher đọc được badge
        val channel = NotificationChannel(
            CHANNEL_BADGE,
            "Badge thông báo",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Hiển thị số thông báo chưa đọc trên icon ứng dụng"
            setShowBadge(true)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }
        nm.createNotificationChannel(channel)
        channelCreated = true
    }
}
