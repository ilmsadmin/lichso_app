package com.lichso.app.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Chuyển đổi exception kỹ thuật thành thông báo thân thiện với người dùng.
 * Tránh hiển thị địa chỉ IP, stack trace hay thông tin kỹ thuật ra ngoài UI.
 */
object ErrorMessageUtil {

    /**
     * Trả về chuỗi lỗi tiếng Việt phù hợp hiển thị cho người dùng.
     *
     * @param throwable Exception cần chuyển đổi (nullable — trả về fallback khi null)
     * @param fallback  Thông báo mặc định nếu không khớp pattern nào (tuỳ chọn)
     */
    fun friendlyMessage(
        throwable: Throwable?,
        fallback: String = "Đã xảy ra lỗi, vui lòng thử lại",
    ): String {
        if (throwable == null) return fallback
        val msg = throwable.message ?: ""
        return when {
            // Không có kết nối mạng / không phân giải được tên miền
            throwable is UnknownHostException
                || throwable.cause is UnknownHostException
                || msg.contains("Unable to resolve", ignoreCase = true)
                || msg.contains("UnknownHost", ignoreCase = true) ->
                "Không có kết nối internet. Vui lòng kiểm tra WiFi hoặc dữ liệu di động."

            // Kết nối bị từ chối / máy chủ không đến được
            throwable is ConnectException
                || throwable.cause is ConnectException
                || msg.contains("Failed to connect", ignoreCase = true)
                || msg.contains("Connection refused", ignoreCase = true) ->
                "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."

            // Timeout
            throwable is SocketTimeoutException
                || throwable.cause is SocketTimeoutException
                || msg.contains("timeout", ignoreCase = true)
                || msg.contains("timed out", ignoreCase = true) ->
                "Kết nối quá chậm, vui lòng thử lại."

            // HTTP 401 / 403 – xác thực
            msg.contains("401") || msg.contains("403") || msg.contains("Unauthorized") ->
                "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại."

            // HTTP 5xx – lỗi máy chủ
            msg.contains("500") || msg.contains("502") || msg.contains("503") || msg.contains("504") ->
                "Máy chủ đang bận, vui lòng thử lại sau."

            // Lỗi mạng chung (OkHttp / Retrofit)
            msg.contains("network", ignoreCase = true)
                || msg.contains("socket", ignoreCase = true)
                || msg.contains("IO") ->
                "Lỗi kết nối mạng. Vui lòng kiểm tra và thử lại."

            else -> fallback
        }
    }
}
