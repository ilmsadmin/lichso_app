package com.lichso.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

/**
 * Helper để hướng dẫn người dùng thêm widget vào màn hình chính.
 *
 * Android 8.0+ hỗ trợ [AppWidgetManager.requestPinAppWidget] — sẽ hiện dialog
 * xác nhận rồi tự ghim widget vào launcher. Với launcher không hỗ trợ (Samsung
 * One UI cũ, MIUI...) thì fallback về hướng dẫn thủ công.
 */
object WidgetPinHelper {

    data class WidgetInfo(
        val label: String,
        val description: String,
        val providerClass: Class<*>
    )

    /** Danh sách tất cả widget của ứng dụng */
    val allWidgets = listOf(
        WidgetInfo(
            label = "Lịch Vạn Niên",
            description = "Hiển thị ngày dương, âm lịch, can chi và đánh giá ngày",
            providerClass = CalendarWidget::class.java
        ),
        WidgetInfo(
            label = "Đồng Hồ Lịch Số",
            description = "Đồng hồ + thời tiết + lịch âm dương (4×2)",
            providerClass = ClockWidget::class.java
        ),
        WidgetInfo(
            label = "Đồng Hồ Sáng/Tối",
            description = "Đồng hồ giao diện sáng/tối tự động (4×2)",
            providerClass = ClockWidget2::class.java
        ),
        WidgetInfo(
            label = "Lịch Tháng",
            description = "Lịch tháng đầy đủ với âm lịch (4×4)",
            providerClass = MonthCalendarWidget::class.java
        ),
        WidgetInfo(
            label = "AI Tử Vi",
            description = "Xem tử vi & hỏi AI về ngày hôm nay",
            providerClass = AiWidget::class.java
        ),
    )

    /**
     * Kiểm tra launcher hiện tại có hỗ trợ pin widget hay không.
     * Trả về true nếu API được hỗ trợ (Android 8+) và launcher cho phép.
     */
    fun isPinSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val mgr = AppWidgetManager.getInstance(context)
        return mgr.isRequestPinAppWidgetSupported
    }

    /**
     * Yêu cầu ghim một widget vào màn hình chính.
     * Launcher sẽ hiện dialog xác nhận cho người dùng.
     *
     * @return true nếu yêu cầu được gửi thành công
     */
    fun requestPin(context: Context, widgetInfo: WidgetInfo): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            val mgr = AppWidgetManager.getInstance(context)
            if (!mgr.isRequestPinAppWidgetSupported) return false
            val provider = ComponentName(context, widgetInfo.providerClass)
            mgr.requestPinAppWidget(provider, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }
}
