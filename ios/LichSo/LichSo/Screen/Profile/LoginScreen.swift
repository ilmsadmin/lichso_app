import SwiftUI

// ═══════════════════════════════════════════
// LoginScreen — Google Sign-In for LichSo
// ═══════════════════════════════════════════

struct LoginScreen: View {
    @EnvironmentObject var authService: GoogleAuthService
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [
                    Color(red: 0.773, green: 0.157, blue: 0.157),
                    Color(red: 0.545, green: 0,     blue: 0),
                    Color(red: 0.35,  green: 0,     blue: 0)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            // Decorative circles
            Circle()
                .fill(Color.white.opacity(0.05))
                .frame(width: 300, height: 300)
                .offset(x: 130, y: -180)
            Circle()
                .fill(Color.white.opacity(0.04))
                .frame(width: 200, height: 200)
                .offset(x: -110, y: 260)

            VStack(spacing: 0) {
                // Close button
                HStack {
                    Spacer()
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 26))
                            .foregroundColor(.white.opacity(0.7))
                    }
                    .padding(.trailing, 24)
                    .padding(.top, 16)
                }

                Spacer()

                // Logo + title
                VStack(spacing: 20) {
                    // App logo
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.15))
                            .frame(width: 100, height: 100)
                        Circle()
                            .fill(Color.white.opacity(0.1))
                            .frame(width: 80, height: 80)
                        Image("AppLogo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 78, height: 78)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    }

                    VStack(spacing: 8) {
                        Text("Lịch Số")
                            .font(.system(size: 36, weight: .bold, design: .serif))
                            .foregroundColor(.white)

                        Text("Đăng nhập để đồng bộ dữ liệu\nvà tham gia bảng xếp hạng")
                            .font(.system(size: 15))
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.center)
                            .lineSpacing(4)
                    }
                }

                Spacer().frame(height: 60)

                // Card
                VStack(spacing: 20) {
                    // Error message
                    if let error = authService.errorMessage {
                        HStack(spacing: 10) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.orange)
                            Text(error)
                                .font(.system(size: 13))
                                .foregroundColor(.orange)
                                .multilineTextAlignment(.leading)
                        }
                        .padding(14)
                        .background(Color.orange.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                        )
                    }

                    // Google sign-in button
                    Button(action: {
                        authService.errorMessage = nil
                        Task { await authService.signIn() }
                    }) {
                        HStack {
                            Spacer()
                            HStack(spacing: 14) {
                                if authService.isLoading {
                                    ProgressView()
                                        .tint(Color(red: 0.2, green: 0.2, blue: 0.2))
                                        .scaleEffect(0.9)
                                } else {
                                    GoogleLogoView()
                                        .frame(width: 22, height: 22)
                                }
                                Text(authService.isLoading ? "Đang đăng nhập..." : "Đăng nhập bằng Google")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(Color(red: 0.2, green: 0.2, blue: 0.2))
                            }
                            Spacer()
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.12), radius: 8, y: 4)
                    }
                    .disabled(authService.isLoading)
                    .scaleEffect(authService.isLoading ? 0.97 : 1.0)
                    .animation(.spring(response: 0.3), value: authService.isLoading)

                    Text("Thông tin của bạn được bảo mật\nvà không chia sẻ với bên thứ ba")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.55))
                        .multilineTextAlignment(.center)
                }
                .padding(28)
                .background(Color.white.opacity(0.08))
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .overlay(
                    RoundedRectangle(cornerRadius: 28)
                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
                )
                .padding(.horizontal, 24)

                Spacer()

                // Footer
                Text("© Lịch Số 2025")
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.35))
                    .padding(.bottom, 32)
            }
        }
        .onChange(of: authService.isSignedIn) { _, signedIn in
            if signedIn { dismiss() }
        }
    }
}

// ── Google "G" logo drawn in SwiftUI ──
struct GoogleLogoView: View {
    var body: some View {
        GeometryReader { geo in
            let size = min(geo.size.width, geo.size.height)
            Canvas { ctx, _ in
                let center = CGPoint(x: size / 2, y: size / 2)
                let r = size / 2

                // Blue arc (top-right)
                drawArc(ctx: ctx, center: center, radius: r,
                        start: -0.1, end: 1.1, color: .blue)
                // Red arc (top-left to bottom-left)
                drawArc(ctx: ctx, center: center, radius: r,
                        start: 1.2, end: 2.5, color: Color(red: 0.839, green: 0.153, blue: 0.157))
                // Yellow arc (bottom)
                drawArc(ctx: ctx, center: center, radius: r,
                        start: 2.6, end: 3.4, color: Color(red: 1, green: 0.737, blue: 0))
                // Green arc (right)
                drawArc(ctx: ctx, center: center, radius: r,
                        start: 3.5, end: 4.7, color: Color(red: 0.234, green: 0.659, blue: 0.325))

                // White horizontal bar for "G"
                let barY = size * 0.47
                let barX = size * 0.45
                let barH = size * 0.16
                let barW = size * 0.50
                var bar = Path()
                bar.addRect(CGRect(x: barX, y: barY, width: barW, height: barH))
                ctx.fill(bar, with: .color(.white))

                // White circle to cut out center
                var inner = Path()
                inner.addEllipse(in: CGRect(
                    x: center.x - r * 0.55,
                    y: center.y - r * 0.55,
                    width: r * 1.10,
                    height: r * 1.10
                ))
                ctx.fill(inner, with: .color(.white))
            }
        }
    }

    private func drawArc(ctx: GraphicsContext, center: CGPoint, radius: CGFloat,
                         start: CGFloat, end: CGFloat, color: Color) {
        var path = Path()
        path.addArc(center: center, radius: radius,
                    startAngle: .radians(start), endAngle: .radians(end), clockwise: false)
        path.addArc(center: center, radius: radius * 0.55,
                    startAngle: .radians(end), endAngle: .radians(start), clockwise: true)
        path.closeSubpath()
        ctx.fill(path, with: .color(color))
    }
}

// ── Compact Google Sign-In button (used in ProfileScreen) ──
struct GoogleSignInButton: View {
    @EnvironmentObject var authService: GoogleAuthService
    var compact: Bool = false

    var body: some View {
        Button(action: {
            authService.errorMessage = nil
            Task { await authService.signIn() }
        }) {
            HStack(spacing: 10) {
                if authService.isLoading {
                    ProgressView().tint(.white).scaleEffect(0.8)
                } else {
                    GoogleLogoView().frame(width: 18, height: 18)
                }
                Text(authService.isLoading ? "Đang đăng nhập..." : "Đăng nhập bằng Google")
                    .font(.system(size: compact ? 13 : 15, weight: .semibold))
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
            .frame(height: compact ? 44 : 50)
            .background(
                LinearGradient(
                    colors: [Color(red: 0.9, green: 0.18, blue: 0.18), Color(red: 0.7, green: 0.05, blue: 0.05)],
                    startPoint: .leading, endPoint: .trailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: compact ? 12 : 14))
            .shadow(color: Color(red: 0.7, green: 0.05, blue: 0.05).opacity(0.4), radius: 6, y: 3)
        }
        .disabled(authService.isLoading)
        .scaleEffect(authService.isLoading ? 0.97 : 1.0)
        .animation(.spring(response: 0.3), value: authService.isLoading)
    }
}
