import SwiftUI

// ═══════════════════════════════════════════
// Splash Screen — Port from Android
// Traditional Red & Gold theme
// ═══════════════════════════════════════════

private let BgDeepRed = Color(hex: "6D0B0B")
private let BgRed = Color(hex: "9B1B1B")
private var BgCrimson: Color { LSTheme.badRed }

private let Gold50 = Color(hex: "FFF8E1")
private let Gold200 = Color(hex: "E6C361")
private var Gold400: Color { LSTheme.gold }
private let Gold600 = Color(hex: "B8860B")
private let Gold800 = Color(hex: "8B6914")

private let ZenixStart = Color(hex: "64FFDA")
private let ZenixMid = Color(hex: "448AFF")
private let ZenixEnd = Color(hex: "B388FF")

struct SplashScreen: View {
    let onSplashFinished: () -> Void

    @State private var started = false
    @State private var logoScale: CGFloat = 0
    @State private var logoOpacity: Double = 0
    @State private var titleOpacity: Double = 0
    @State private var titleOffset: CGFloat = 24
    @State private var subOpacity: Double = 0
    @State private var subOffset: CGFloat = 20
    @State private var bottomOpacity: Double = 0
    @State private var ringRotation: Double = 0
    @State private var glowPulse: Double = 0.06

    var body: some View {
        ZStack {
            // ── Background gradient ──
            LinearGradient(
                colors: [BgDeepRed, BgRed, BgCrimson],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            // ── Ambient glow top-right ──
            Circle()
                .fill(
                    RadialGradient(
                        colors: [Gold400, Color.clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 170
                    )
                )
                .frame(width: 340, height: 340)
                .opacity(glowPulse)
                .offset(x: 100, y: -340)

            // ── Ambient glow bottom-left ──
            Circle()
                .fill(
                    RadialGradient(
                        colors: [Gold200, Color.clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 140
                    )
                )
                .frame(width: 280, height: 280)
                .opacity(glowPulse * 0.6)
                .offset(x: -70, y: 340)

            // ═══ Center content ═══
            VStack(spacing: 0) {
                // ── Logo + ring ──
                ZStack {
                    // Rotating gold arc ring
                    Circle()
                        .trim(from: 0, to: 0.75)
                        .stroke(
                            AngularGradient(
                                colors: [Color.clear, Gold200, Gold50, Color.clear],
                                center: .center
                            ),
                            lineWidth: 2
                        )
                        .frame(width: 210, height: 210)
                        .rotationEffect(.degrees(ringRotation))
                        .opacity(0.35)

                    // Soft glow behind logo
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [Gold200, Color.clear],
                                center: .center,
                                startRadius: 0,
                                endRadius: 90
                            )
                        )
                        .frame(width: 180, height: 180)
                        .opacity(0.12)

                    // App logo image
                    Image("AppLogo")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 140, height: 140)
                        .shadow(color: Color.black.opacity(0.35), radius: 24, x: 0, y: 12)
                        .scaleEffect(logoScale)
                        .opacity(logoOpacity)
                }
                .frame(width: 210, height: 210)

                // ── App name ──
                Text("Lịch Số")
                    .font(.system(size: 28, weight: .bold, design: .serif))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [Gold600, Gold200, Gold50, Gold200, Gold600],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .tracking(4)
                    .opacity(titleOpacity)
                    .offset(y: titleOffset)

                Spacer().frame(height: 10)

                // ── Thin divider ──
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [Color.clear, Gold400, Color.clear],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: 56, height: 2)
                    .opacity(titleOpacity)

                Spacer().frame(height: 14)

                // ── Tagline ──
                Text("Lịch Vạn Niên  ·  Gia Phả  ·  AI Tử Vi")
                    .font(.system(size: 13, weight: .regular))
                    .foregroundColor(.white.opacity(0.55))
                    .tracking(0.8)
                    .opacity(subOpacity)
                    .offset(y: subOffset)

                Spacer().frame(height: 4)

                Text("v1.0.0")
                    .font(.system(size: 11))
                    .foregroundColor(.white.opacity(0.25))
                    .opacity(subOpacity)
                    .offset(y: subOffset)
            }
            .offset(y: -28)

            // ═══ Bottom branding ═══
            VStack(spacing: 10) {
                Spacer()

                Circle()
                    .fill(Gold400.opacity(0.4))
                    .frame(width: 3, height: 3)

                HStack(spacing: 4) {
                    Text("Phát triển bởi  ")
                        .font(.system(size: 12))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [Gold600, Gold200, Gold50, Gold200, Gold600],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .tracking(0.5)

                    Text("Zenix Labs")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [ZenixStart, ZenixMid, ZenixEnd],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                }
                .padding(.bottom, 52)
            }
            .opacity(bottomOpacity)
        }
        .onAppear {
            startAnimations()
        }
    }

    private func startAnimations() {
        // Logo spring bounce
        withAnimation(.spring(response: 0.5, dampingFraction: 0.5, blendDuration: 0)) {
            logoScale = 1.0
            logoOpacity = 1.0
        }

        // Title fade in (delayed 350ms)
        withAnimation(.easeOut(duration: 0.5).delay(0.35)) {
            titleOpacity = 1.0
            titleOffset = 0
        }

        // Subtitle (delayed 650ms)
        withAnimation(.easeOut(duration: 0.4).delay(0.65)) {
            subOpacity = 1.0
            subOffset = 0
        }

        // Bottom (delayed 900ms)
        withAnimation(.easeOut(duration: 0.5).delay(0.9)) {
            bottomOpacity = 1.0
        }

        // Infinite ring rotation
        withAnimation(.linear(duration: 20).repeatForever(autoreverses: false)) {
            ringRotation = 360
        }

        // Glow pulse
        withAnimation(.easeInOut(duration: 3).repeatForever(autoreverses: true)) {
            glowPulse = 0.14
        }

        // Navigate after 2.4s
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.4) {
            onSplashFinished()
        }
    }
}

#Preview {
    SplashScreen(onSplashFinished: {})
}
