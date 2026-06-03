import SwiftUI

// ═══════════════════════════════════════════
// LoginNudgeBanner — "blade" nhắc đăng nhập Google khi user còn là khách.
// Port từ Android LoginNudgeBanner.kt: hiện 1 lần/phiên app, sau 3s, trượt lên từ đáy;
// có nút "Đăng nhập" (Google) + nút đóng. Tự ẩn khi đăng nhập thành công.
// ═══════════════════════════════════════════

private enum LoginNudgeSession {
    static var shown = false   // chỉ hiện 1 lần mỗi vòng đời tiến trình app
}

struct LoginNudgeBanner: View {
    @ObservedObject private var auth = GoogleAuthService.shared
    @State private var visible = false
    @State private var showLogin = false

    var body: some View {
        Group {
            if visible && !auth.isSignedIn {
                card
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.35), value: visible)
        .onChange(of: auth.isSignedIn) { _, signedIn in
            if signedIn { visible = false }
        }
        .task {
            guard !auth.isSignedIn, !LoginNudgeSession.shown else { return }
            LoginNudgeSession.shown = true
            try? await Task.sleep(nanoseconds: 3_000_000_000)
            if !auth.isSignedIn { visible = true }
        }
        .fullScreenCover(isPresented: $showLogin) {
            LoginScreen().environmentObject(auth)
        }
    }

    private var redGradient: LinearGradient {
        LinearGradient(
            colors: [LSTheme.primary, Color(hex: "C62828")],
            startPoint: .topLeading, endPoint: .bottomTrailing
        )
    }

    private var card: some View {
        HStack(spacing: 10) {
            // Icon
            ZStack {
                RoundedRectangle(cornerRadius: 10).fill(redGradient)
                    .frame(width: 38, height: 38)
                Image(systemName: "person.crop.circle.badge.plus")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
            }

            // Text
            VStack(alignment: .leading, spacing: 2) {
                Text("Đăng nhập để lưu tiến trình")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .lineLimit(1)
                Text("Điểm thưởng & bảng xếp hạng")
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // Sign-in button
            Button {
                showLogin = true
            } label: {
                Text("Đăng nhập")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(minWidth: 56)
                    .padding(.horizontal, 10).padding(.vertical, 6)
                    .background(redGradient)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .buttonStyle(.plain)

            // Close
            Button {
                visible = false
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(LSTheme.textTertiary)
                    .frame(width: 24, height: 24)
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 12).padding(.vertical, 10)
        .background(LSTheme.surfaceContainerHigh)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.18), radius: 6, y: 2)
        .padding(.horizontal, 16).padding(.vertical, 12)
    }
}
