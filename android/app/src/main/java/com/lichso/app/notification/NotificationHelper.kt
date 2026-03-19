package com.lichso.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lichso.app.MainActivity
import com.lichso.app.R

object NotificationHelper {

    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_GIO_DAI_CAT = "channel_gio_dai_cat"

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
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(id, notification)
    }

    fun sendGioDaiCatNotification(context: Context, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_GIO_DAI_CAT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("☽ Giờ Hoàng Đạo Hôm Nay")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9999, notification)
    }
}
