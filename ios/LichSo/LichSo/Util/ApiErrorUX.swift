import Foundation

/// Ghi log chỉ trong bản DEBUG — no-op khi build Release để không lộ thông tin nội bộ.
@inline(__always)
func debugLog(_ items: Any..., separator: String = " ", terminator: String = "\n") {
    #if DEBUG
    print(items.map { "\($0)" }.joined(separator: separator), terminator: terminator)
    #endif
}

enum ApiErrorUX {
    static func userMessage(
        from error: Error,
        fallback: String = "Đã có lỗi xảy ra. Vui lòng thử lại."
    ) -> String {
        let nsError = error as NSError
        let raw = nsError.localizedDescription.lowercased()

        if nsError.domain == NSURLErrorDomain {
            switch nsError.code {
            case NSURLErrorNotConnectedToInternet, NSURLErrorNetworkConnectionLost, NSURLErrorCannotFindHost, NSURLErrorCannotConnectToHost:
                return "Không có kết nối internet. Vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động."
            case NSURLErrorTimedOut:
                return "Kết nối đang chậm. Vui lòng thử lại sau ít phút."
            default:
                break
            }
        }

        if raw.contains("timeout") || raw.contains("timed out") {
            return "Kết nối đang chậm. Vui lòng thử lại sau ít phút."
        }
        if raw.contains("offline")
            || raw.contains("network")
            || raw.contains("not connected")
            || raw.contains("cannot find host")
            || raw.contains("cannot connect to host")
            || raw.contains("failed to connect")
            || raw.contains("api.lichso.vn") {
            return "Không thể kết nối tới dịch vụ Lịch Số. Vui lòng thử lại sau."
        }
        if raw.contains("500") || raw.contains("502") || raw.contains("503") || raw.contains("504") {
            return "Dịch vụ Lịch Số đang bận. Vui lòng thử lại sau."
        }
        if raw.contains("401") || raw.contains("403") || raw.contains("unauthorized") {
            return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        }

        return fallback
    }
}
