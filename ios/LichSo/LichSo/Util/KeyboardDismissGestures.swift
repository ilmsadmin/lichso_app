import SwiftUI
import UIKit

// ══════════════════════════════════════════
// Tiện ích ẩn bàn phím bằng chạm/vuốt vào VÙNG NỘI DUNG (không bọc ô nhập/nút).
// Cố ý KHÔNG gắn gesture ở cấp UIWindow để tránh chặn tap của các nút SwiftUI
// (vd nút menu mở sidebar) và tránh trễ bật bàn phím.
// ══════════════════════════════════════════

/// Ẩn bàn phím hiện hành (resign first responder).
func dismissKeyboardNow() {
    UIApplication.shared.sendAction(
        #selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil
    )
}

// ══════════════════════════════════════════
// KeyboardWarmer — "làm nóng" bàn phím 1 lần lúc khởi động để lần focus thật đầu tiên
// không phải chờ iOS nạp bàn phím (cold-start rất chậm, nhất là simulator + tiếng Việt).
// Tạo 1 UITextField ẩn off-screen, become + resign first responder trong cùng vòng lặp
// → khởi tạo bộ máy text-input mà không hiện bàn phím (không nháy).
// ══════════════════════════════════════════
enum KeyboardWarmer {
    private static var warmed = false

    static func warmUp() {
        guard !warmed else { return }
        guard let window = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .flatMap({ $0.windows })
            .first(where: { $0.isKeyWindow }) else { return }
        warmed = true
        let field = UITextField(frame: CGRect(x: 0, y: -1000, width: 1, height: 1))
        window.addSubview(field)
        field.becomeFirstResponder()
        field.resignFirstResponder()
        field.removeFromSuperview()
    }
}

extension View {
    /// Chạm HOẶC vuốt vào view này (vùng trống) → ẩn bàn phím.
    /// Chỉ áp cho vùng nội dung KHÔNG chứa ô nhập (empty state…) để không gây trễ.
    func dismissKeyboardOnTapAndDrag() -> some View {
        self
            .contentShape(Rectangle())
            .onTapGesture { dismissKeyboardNow() }
            .simultaneousGesture(
                DragGesture(minimumDistance: 12).onChanged { _ in dismissKeyboardNow() }
            )
    }

    /// Chạm vào view này → ẩn bàn phím (chạy song song, không chặn thao tác con).
    /// Dùng cho ScrollView/List (vuốt-để-ẩn đã có scrollDismissesKeyboard lo).
    func dismissKeyboardOnTap() -> some View {
        self.simultaneousGesture(
            TapGesture().onEnded { dismissKeyboardNow() }
        )
    }
}
