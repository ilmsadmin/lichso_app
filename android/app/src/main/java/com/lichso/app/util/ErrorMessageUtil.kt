package com.lichso.app.util

import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.lichso.app.data.remote.LichSoApiException
import java.net.ConnectException
import java.net.HttpURLConnection
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
            // Google Sign-In / Firebase Auth
            throwable is NoCredentialException
                || msg.contains("No credentials available", ignoreCase = true)
                || msg.contains("Cannot find a matching credential", ignoreCase = true) ->
                "Không tìm thấy tài khoản Google khả dụng trên thiết bị."

            throwable is FirebaseNetworkException ->
                "Không thể kết nối tới Google. Vui lòng kiểm tra internet và thử lại."

            throwable is FirebaseAuthInvalidCredentialsException
                || msg.contains("malformed", ignoreCase = true)
                || msg.contains("expired", ignoreCase = true)
                || msg.contains("Invalid Google ID token", ignoreCase = true)
                || msg.contains("audience mismatch", ignoreCase = true) ->
                "Cấu hình Google Sign-In chưa khớp giữa ứng dụng và máy chủ."

            msg.contains("GOOGLE_WEB_CLIENT_ID", ignoreCase = true) ->
                "Cấu hình Google Sign-In trong ứng dụng chưa đầy đủ."

            msg.contains("Google login is not configured", ignoreCase = true) ->
                "Máy chủ chưa bật đăng nhập Google."

            throwable is GetCredentialException
                || throwable is FirebaseAuthException ->
                "Không thể đăng nhập bằng Google. Vui lòng thử lại hoặc dùng tài khoản Google khác."

            throwable is LichSoApiException -> when (throwable.statusCode) {
                HttpURLConnection.HTTP_BAD_REQUEST ->
                    "Thông tin gửi lên chưa hợp lệ. Vui lòng kiểm tra lại."
                HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN ->
                    "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại."
                HttpURLConnection.HTTP_NOT_FOUND ->
                    "Không tìm thấy dữ liệu cần thiết. Vui lòng thử lại sau."
                408 ->
                    "Kết nối quá chậm, vui lòng thử lại."
                409 ->
                    "Dữ liệu đã thay đổi. Vui lòng tải lại và thử lại."
                422 ->
                    "Một số thông tin chưa đúng định dạng. Vui lòng kiểm tra lại."
                429 ->
                    "Bạn thao tác hơi nhanh. Vui lòng chờ một lát rồi thử lại."
                in 500..599 ->
                    "Máy chủ đang bận, vui lòng thử lại sau."
                else ->
                    fallback
            }

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
            msg.contains("AI proxy", ignoreCase = true)
                || msg.contains("AI_PROXY", ignoreCase = true) ->
                "Tính năng AI hiện chưa sẵn sàng. Vui lòng thử lại sau."

            msg.contains("network", ignoreCase = true)
                || msg.contains("socket", ignoreCase = true)
                || msg.contains("IO") ->
                "Lỗi kết nối mạng. Vui lòng kiểm tra và thử lại."

            else -> fallback
        }
    }
}
