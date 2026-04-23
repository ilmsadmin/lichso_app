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
    const val CHANNEL_AI_TUVI = "channel_ai_tuvi"

    // Removed group key — grouping without summary notification causes
    // Android to bundle & sometimes hide individual notifications.

    /**
     * Kiểm tra xem app có quyền gửi notification không.
     * Trên Android 13+ (API 33) cần runtime permission POST_NOTIFICATIONS.
     * Trên Android 12 trở xuống luôn trả về true (trừ khi user tắt trong settings).
     */
    fun canPostNotifications(context: Context): Boolean {
        // Trên Android 13+ kiểm tra runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        // Kiểm tra thêm xem user có tắt notification trong system settings không
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

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

        // Channel AI Tử Vi buổi tối
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_AI_TUVI,
                "Tử Vi AI buổi tối",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gợi ý xem tử vi AI mỗi tối"
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
        // Luôn lưu vào DB trước (in-app notification)
        saveToDatabase(context, title, body, "reminder")

        // Kiểm tra permission trước khi gửi system notification
        if (!canPostNotifications(context)) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted, skipping system notification")
            return
        }

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

            .setSubText("Nhắc nhở")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(id, notification)
    }

    fun sendGioDaiCatNotification(context: Context, title: String, subtitle: String, lines: List<String>) {
        // Luôn lưu vào DB trước (in-app notification)
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "good_day")

        // Kiểm tra permission trước khi gửi system notification
        if (!canPostNotifications(context)) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted, skipping system notification")
            return
        }

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

            .setSubText("Giờ Hoàng Đạo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9999, notification)
    }

    fun sendDailyNotification(context: Context, title: String, subtitle: String, lines: List<String>) {
        // Luôn lưu vào DB trước (in-app notification)
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "daily")

        // Kiểm tra permission trước khi gửi system notification
        if (!canPostNotifications(context)) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted, skipping system notification")
            return
        }

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

            .setSubText("Thông báo ngày mới")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9998, notification)
    }

    fun sendFestivalReminderNotification(context: Context, title: String, subtitle: String, lines: List<String>) {
        // Luôn lưu vào DB trước (in-app notification)
        val fullBody = lines.joinToString("\n")
        saveToDatabase(context, title, fullBody, "holiday")

        // Kiểm tra permission trước khi gửi system notification
        if (!canPostNotifications(context)) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted, skipping system notification")
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

            .setSubText("Nhắc ngày lễ")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9997, notification)
    }

    /**
     * Gửi notification gợi ý xem Tử Vi AI buổi tối.
     * Nội dung ngẫu nhiên mỗi ngày để tạo sự tò mò.
     */
    fun sendAiTuViNotification(context: Context) {
        val messages = listOf(
            "🌙 Tối nay sao chiếu mệnh bạn có điều thú vị — hỏi AI Tử Vi xem sao!",
            "✨ Ngày mai có biến động gì không? AI Tử Vi đã sẵn sàng giải đáp.",
            "🔮 Vận trình tình cảm tuần này thế nào? Để AI Tử Vi mách bạn nhé!",
            "🌟 Bạn có biết sao nào đang chiếu mệnh mình không? Hỏi AI Tử Vi ngay!",
            "🎋 Tài lộc, sức khỏe, tình duyên — AI Tử Vi phân tích chi tiết cho bạn.",
            "🏮 Đêm nay bình an — nhưng ngày mai thì sao? AI Tử Vi biết đấy!",
            "💫 Mệnh Kim, Mộc, Thuỷ, Hoả, Thổ — AI Tử Vi giải mã vận số riêng bạn.",
            "🌕 Trăng sáng đêm nay, vận may đang gõ cửa? Xem AI Tử Vi nhé!",
            "🐉 Long mạch phong thuỷ hôm nay có gì đặc biệt? AI Tử Vi chờ bạn!",
            "⭐ Cuối ngày rồi — dành 1 phút xem AI Tử Vi phân tích ngày mai nhé!",
            "🎯 Công danh, sự nghiệp có thuận lợi? AI Tử Vi có câu trả lời.",
            "🌸 Nhân duyên tháng này ra sao? AI Tử Vi đọc sao giúp bạn!",
            "🧧 Hỏi AI Tử Vi: Ngày nào tốt để khởi sự, ký kết, xuất hành?",
            "🔥 Hoả tinh đang vượng — ảnh hưởng gì đến bạn? Hỏi AI Tử Vi!",
            "🍀 May mắn đến từ đâu? AI Tử Vi phân tích dựa trên tử vi của bạn."
        )

        val randomIndex = (System.currentTimeMillis() / 86400000).toInt() // Đổi theo ngày
        val body = messages[randomIndex % messages.size]
        val title = "Tử Vi AI — Gợi ý buổi tối"

        // Lưu DB
        saveToDatabase(context, title, body, "ai_tuvi")

        if (!canPostNotifications(context)) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "ai_chat") // Deep link to AI chat
        }
        val pi = PendingIntent.getActivity(
            context, 9995, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_AI_TUVI)
            .setSmallIcon(R.drawable.ic_notif_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setSubText("Tử Vi AI")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9995, notification)
    }

    /**
     * Gửi notification tử vi CÁ NHÂN HOÁ dựa trên profile user (tên + ngày sinh).
     * Khác với [sendAiTuViNotification] generic, bản này phân tích quan hệ Địa
     * Chi tuổi user × ngày mục tiêu (thường là ngày mai) để cho lời khuyên
     * riêng: tam hợp / lục hợp / lục xung / lục hại...
     */
    fun sendPersonalHoroscopeNotification(
        context: Context,
        title: String,
        subtitle: String,
        shortBody: String,
        lines: List<String>
    ) {
        // Lưu DB cho in-app notification screen
        saveToDatabase(context, title, shortBody, "ai_tuvi")

        if (!canPostNotifications(context)) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "ai_chat")
        }
        val pi = PendingIntent.getActivity(
            context, 9995, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
        lines.take(7).forEach { inboxStyle.addLine(it) }

        val notification = NotificationCompat.Builder(context, CHANNEL_AI_TUVI)
            .setSmallIcon(R.drawable.ic_notif_calendar)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setSubText("Tử Vi cá nhân")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(9995, notification)
    }

    /**
     * Gửi system notification thông báo có bản cập nhật mới.
     * Khi nhấn vào sẽ mở Google Play trang Lịch Số để người dùng cập nhật.
     *
     * @param versionName Tên phiên bản mới, VD "1.7.0"
     */
    fun sendAppUpdateNotification(context: Context, versionName: String) {
        val title = "Lịch Số $versionName — Cập nhật mới!"
        val body = "Phiên bản mới đã có trên Google Play. Nhấn để cập nhật ngay."

        // Luôn lưu vào DB trước (in-app notification)
        saveToDatabase(context, title, body, "update")

        // Kiểm tra permission trước khi gửi system notification
        if (!canPostNotifications(context)) {
            android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted, skipping system notification")
            return
        }

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

        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATE)
            .setSmallIcon(R.drawable.ic_notif_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)

            .setSubText("Cập nhật ứng dụng")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(9996, notification)
    }
}
