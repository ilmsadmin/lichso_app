package com.lichso.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lichso.app.MainActivity
import com.lichso.app.R
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_GIO_DAI_CAT = "channel_gio_dai_cat"
    const val CHANNEL_DAILY = "channel_daily_summary"
    const val CHANNEL_FESTIVAL = "channel_festival_reminder"
    const val CHANNEL_UPDATE = "channel_app_update"

    private const val GROUP_KEY_LICHSO = "com.lichso.app.NOTIFICATION_GROUP"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel nhắc nhở task/reminder
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                "Nhắc nhở",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở task và lịch hẹn"
                enableVibration(true)
            }
        )

        // Channel giờ đại cát
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GIO_DAI_CAT,
                "Giờ Đại Cát",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo giờ hoàng đạo trong ngày"
            }
        )

        // Channel nhắc nhở hàng ngày
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DAILY,
                "Nhắc nhở hàng ngày",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo tóm tắt ngày mới mỗi sáng"
            }
        )

        // Channel nhắc ngày lễ
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_FESTIVAL,
                "Nhắc ngày lễ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc trước 1 ngày khi có ngày lễ sắp tới"
            }
        )

        // Channel thông báo cập nhật ứng dụng
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_UPDATE,
                "Cập nhật ứng dụng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi có phiên bản Lịch Số mới"
                enableVibration(true)
            }
        )
    }

    /**
     * Persist a notification into Room DB so that the in-app NotificationScreen can display it.
     */
    private fun saveToDatabase(context: Context, title: String, description: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = LichSoDatabase.getInstance(context).notificationDao()
                dao.insert(
                    NotificationEntity(
                        title = title,
                        description = description,
                        type = type,
                        isRead = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) { /* best-effort */ }
        }
    }

    fun sendReminderNotification(context: Context, id: Int, title: String, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notif_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_KEY_LICHSO)
            .setSubText("Nhắc nhở")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(id, notification)
        saveToDatabase(context, title, body, "reminder")
    }

    fun sendGioDaiCatNotification(context: Context, title: String, subtitle: String, lines: List<String>) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("Lịch Số — Giờ Hoàng Đạo")
        lines.forEach { inboxStyle.addLine(it) }

        val notification = NotificationCompat.Builder(context, CHANNEL_GIO_DAI_CAT)
            .setSmallIcon(R.drawable.ic_notif_clock)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_KEY_LICHSO)
            .setSubText("Giờ Hoàng Đạo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9999, notification)
        // Save full body for in-app notification screen
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "good_day")
    }

    fun sendDailyNotification(context: Context, title: String, subtitle: String, lines: List<String>) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 9998, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("Lịch Số — Thông tin ngày")
        lines.forEach { inboxStyle.addLine(it) }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_notif_calendar)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_KEY_LICHSO)
            .setSubText("Thông báo ngày mới")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9998, notification)
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "daily")
    }

    fun sendFestivalReminderNotification(context: Context, title: String, subtitle: String, lines: List<String>) {        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 9997, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("Lịch Số — Ngày lễ")
        lines.forEach { inboxStyle.addLine(it) }

        val notification = NotificationCompat.Builder(context, CHANNEL_FESTIVAL)
            .setSmallIcon(R.drawable.ic_notif_festival)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_KEY_LICHSO)
            .setSubText("Nhắc ngày lễ")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9997, notification)
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "holiday")
    }

    /**
     * Gửi system notification thông báo có bản cập nhật mới.
     * Khi nhấn vào sẽ mở Google Play trang Lịch Số để người dùng cập nhật.
     *
     * @param versionName Tên phiên bản mới, VD "1.7.0"
     */
    fun sendAppUpdateNotification(context: Context, versionName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deep-link tới Google Play để cập nhật
        val playIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.lichso.app")
            setPackage("com.android.vending")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 9996, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Lịch Số $versionName — Cập nhật mới!"
        val body = "Phiên bản mới đã có trên Google Play. Nhấn để cập nhật ngay."

        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATE)
            .setSmallIcon(R.drawable.ic_notif_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setGroup(GROUP_KEY_LICHSO)
            .setSubText("Cập nhật ứng dụng")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(9996, notification)

        // Lưu vào in-app notification screen
        saveToDatabase(context, title, body, "update")
    }
}
