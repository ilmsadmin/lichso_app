import SwiftUI

// MARK: - Splash / Welcome Screen
struct SplashScreen: View {
    @Environment(\.lichSoColors) var c

    // Animation states
    @State private var logoScale: CGFloat = 0.3
    @State private var logoOpacity: Double = 0
    @State private var ringScale: CGFloat = 0.5
    @State private var ringOpacity: Double = 0
    @State private var ringRotation: Double = 0
    @State private var titleOpacity: Double = 0
    @State private var titleOffset: CGFloat = 20
    @State private var subtitleOpacity: Double = 0
    @State private var subtitleOffset: CGFloat = 15
    @State private var particlesOpacity: Double = 0
    @State private var shimmerOffset: CGFloat = -200
    @State private var pulseScale: CGFloat = 1
    @State private var bottomTextOpacity: Double = 0

    var body: some View {
        ZStack {
            // Background
            c.bg.ignoresSafeArea()

            // Radial glow background
            RadialGradient(
                colors: [
                    c.cyan.opacity(0.08),
                    c.cyan2.opacity(0.04),
                    Color.clear
                ],
                center: .center,
                startRadius: 20,
                endRadius: 300
            )
            .ignoresSafeArea()
            .opacity(particlesOpacity)

            // Floating particles
            FloatingParticlesView(colors: c)
                .opacity(particlesOpacity)
                .ignoresSafeArea()

            // Main content
            VStack(spacing: 0) {
                Spacer()

                // Animated ring + Logo
                ZStack {
                    // Outer rotating ring
                    Circle()
                        .stroke(
                            AngularGradient(
                                colors: [c.cyan, c.cyan2, c.gold, c.green, c.cyan],
                                center: .center
                            ),
                            lineWidth: 2
                        )
                        .frame(width: 140, height: 140)
                        .scaleEffect(ringScale)
                        .opacity(ringOpacity * 0.6)
                        .rotationEffect(.degrees(ringRotation))

                    // Inner glow ring
                    Circle()
                        .stroke(c.cyan.opacity(0.3), lineWidth: 1)
                        .frame(width: 110, height: 110)
                        .scaleEffect(ringScale)
                        .opacity(ringOpacity * 0.4)
                        .rotationEffect(.degrees(-ringRotation * 0.7))

                    // Pulse circle
                    Circle()
                        .fill(c.cyan.opacity(0.06))
                        .frame(width: 120, height: 120)
                        .scaleEffect(pulseScale)
                        .opacity(ringOpacity * 0.5)

                    // Robot logo
                    SplashRobotLogo(colors: c)
                        .frame(width: 90, height: 108)
                        .scaleEffect(logoScale)
                        .opacity(logoOpacity)
                }

                Spacer().frame(height: 36)

                // App title
                VStack(spacing: 6) {
                    Text("Lịch Số AI")
                        .font(.custom("Nunito", size: 34).weight(.black))
                        .foregroundStyle(c.brandGradient)
                        .opacity(titleOpacity)
                        .offset(y: titleOffset)

                    // Shimmer line
                    Rectangle()
                        .fill(
                            LinearGradient(
                                colors: [Color.clear, c.cyan.opacity(0.5), c.cyan2.opacity(0.5), Color.clear],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: 120, height: 1.5)
                        .offset(x: shimmerOffset)
                        .mask(Rectangle().frame(width: 120))
                        .opacity(titleOpacity)
                }

                Spacer().frame(height: 14)

                // Subtitle
                Text("Trợ lý vạn niên thông minh")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(c.textSecondary)
                    .tracking(1.2)
                    .opacity(subtitleOpacity)
                    .offset(y: subtitleOffset)

                Spacer().frame(height: 8)

                // Version tagline
                Text("Lịch âm · Ngày tốt · Nhắc việc · AI")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(c.textTertiary)
                    .tracking(0.5)
                    .opacity(subtitleOpacity * 0.7)
                    .offset(y: subtitleOffset)

                Spacer()

                // Bottom loading indicator
                VStack(spacing: 12) {
                    // Loading dots
                    SplashLoadingDots(color: c.cyan)

                    Text("Đang khởi tạo...")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(c.textTertiary)
                }
                .opacity(bottomTextOpacity)
                .padding(.bottom, 60)
            }
        }
        .onAppear {
            startAnimations()
        }
    }

    private func startAnimations() {
        // 1. Logo appears with spring
        withAnimation(.spring(response: 0.7, dampingFraction: 0.65).delay(0.2)) {
            logoScale = 1.0
            logoOpacity = 1.0
        }

        // 2. Ring appears
        withAnimation(.easeOut(duration: 0.8).delay(0.3)) {
            ringScale = 1.0
            ringOpacity = 1.0
        }

        // 3. Ring starts rotating
        withAnimation(.linear(duration: 8).repeatForever(autoreverses: false).delay(0.3)) {
            ringRotation = 360
        }

        // 4. Pulse
        withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true).delay(0.5)) {
            pulseScale = 1.25
        }

        // 5. Title slides up
        withAnimation(.spring(response: 0.6, dampingFraction: 0.75).delay(0.6)) {
            titleOpacity = 1.0
            titleOffset = 0
        }

        // 6. Shimmer across title
        withAnimation(.easeInOut(duration: 1.2).delay(0.8)) {
            shimmerOffset = 200
        }

        // 7. Subtitle slides up
        withAnimation(.spring(response: 0.6, dampingFraction: 0.75).delay(0.9)) {
            subtitleOpacity = 1.0
            subtitleOffset = 0
        }

        // 8. Particles fade in
        withAnimation(.easeIn(duration: 1.0).delay(0.5)) {
            particlesOpacity = 1.0
        }

        // 9. Bottom loading text
        withAnimation(.easeIn(duration: 0.5).delay(1.2)) {
            bottomTextOpacity = 1.0
        }
    }
}

// MARK: - Splash Robot Logo (simplified Canvas robot)
struct SplashRobotLogo: View {
    let colors: LichSoColors

    var body: some View {
        Canvas { ctx, size in
            let w = size.width
            let h = size.height
            let sc = min(w / 100, h / 128)

            let bodyFill = colors.isDark
                ? Color(hex: 0xE0E8F5)
                : Color(hex: 0xD8E0EE)
            let bodyStroke = colors.isDark
                ? Color(hex: 0xC0C8D6)
                : Color(hex: 0xAAB2C0)
            let eyeColor = Color(hex: 0x4ECDC4)

            // Antenna Left
            var antLPath = Path()
            antLPath.move(to: CGPoint(x: 33*sc, y: 17*sc))
            antLPath.addLine(to: CGPoint(x: 27*sc, y: 6*sc))
            ctx.stroke(antLPath, with: .color(bodyStroke), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 22.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyFill))

            // Antenna Right
            var antRPath = Path()
            antRPath.move(to: CGPoint(x: 67*sc, y: 17*sc))
            antRPath.addLine(to: CGPoint(x: 73*sc, y: 6*sc))
            ctx.stroke(antRPath, with: .color(bodyStroke), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 70.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyFill))

            // Head
            ctx.fill(Path(ellipseIn: CGRect(x: 20*sc, y: 8*sc, width: 60*sc, height: 52*sc)), with: .color(bodyFill))
            ctx.fill(Path(ellipseIn: CGRect(x: 31*sc, y: 17.5*sc, width: 24*sc, height: 11*sc)), with: .color(Color.white.opacity(0.5)))

            // Eyes
            ctx.fill(Path(ellipseIn: CGRect(x: 31*sc, y: 27*sc, width: 16*sc, height: 18*sc)), with: .color(Color(hex: 0x141E30)))
            ctx.fill(Path(ellipseIn: CGRect(x: 32.8*sc, y: 27*sc, width: 12.4*sc, height: 14.4*sc)), with: .color(eyeColor.opacity(0.92)))
            ctx.fill(Path(ellipseIn: CGRect(x: 36.2*sc, y: 29.8*sc, width: 5.6*sc, height: 6.3*sc)), with: .color(Color.white.opacity(0.9)))

            ctx.fill(Path(ellipseIn: CGRect(x: 53*sc, y: 27*sc, width: 16*sc, height: 18*sc)), with: .color(Color(hex: 0x141E30)))
            ctx.fill(Path(ellipseIn: CGRect(x: 54.8*sc, y: 27*sc, width: 12.4*sc, height: 14.4*sc)), with: .color(eyeColor.opacity(0.92)))
            ctx.fill(Path(ellipseIn: CGRect(x: 58.2*sc, y: 29.8*sc, width: 5.6*sc, height: 6.3*sc)), with: .color(Color.white.opacity(0.9)))

            // Neck
            ctx.fill(Path(roundedRect: CGRect(x: 43*sc, y: 58*sc, width: 14*sc, height: 8*sc), cornerRadius: 4*sc), with: .color(Color(hex: 0xDDE3EF)))

            // Body
            var bodyPath = Path()
            bodyPath.move(to: CGPoint(x: 16*sc, y: 68*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 22*sc, y: 106*sc), control: CGPoint(x: 14*sc, y: 92*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 50*sc, y: 124*sc), control: CGPoint(x: 30*sc, y: 120*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 78*sc, y: 106*sc), control: CGPoint(x: 70*sc, y: 120*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 84*sc, y: 68*sc), control: CGPoint(x: 86*sc, y: 92*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 50*sc, y: 61*sc), control: CGPoint(x: 84*sc, y: 63*sc))
            bodyPath.addQuadCurve(to: CGPoint(x: 16*sc, y: 68*sc), control: CGPoint(x: 16*sc, y: 63*sc))
            bodyPath.closeSubpath()
            ctx.fill(bodyPath, with: .color(bodyFill))
            ctx.fill(Path(ellipseIn: CGRect(x: 24*sc, y: 62*sc, width: 28*sc, height: 10*sc)), with: .color(Color.white.opacity(0.45)))

            // Arms
            ctx.fill(Path(ellipseIn: CGRect(x: 3*sc, y: 72.5*sc, width: 14*sc, height: 21*sc)), with: .color(bodyFill))
            ctx.fill(Path(ellipseIn: CGRect(x: 83*sc, y: 72.5*sc, width: 14*sc, height: 21*sc)), with: .color(bodyFill))

            // Chest screen
            ctx.fill(Path(roundedRect: CGRect(x: 35*sc, y: 76*sc, width: 30*sc, height: 22*sc), cornerRadius: 7*sc), with: .color(Color(hex: 0xB4C4DC).opacity(0.22)))
            var l1 = Path(); l1.move(to: CGPoint(x: 38*sc, y: 82*sc)); l1.addLine(to: CGPoint(x: 56*sc, y: 82*sc))
            ctx.stroke(l1, with: .color(Color(hex: 0x4ECDC4).opacity(0.55)), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            var l2 = Path(); l2.move(to: CGPoint(x: 38*sc, y: 87*sc)); l2.addLine(to: CGPoint(x: 50*sc, y: 87*sc))
            ctx.stroke(l2, with: .color(Color(hex: 0x38BDF8).opacity(0.42)), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            var l3 = Path(); l3.move(to: CGPoint(x: 38*sc, y: 92*sc)); l3.addLine(to: CGPoint(x: 53*sc, y: 92*sc))
            ctx.stroke(l3, with: .color(Color(hex: 0xF5C842).opacity(0.35)), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 57*sc, y: 84*sc, width: 6*sc, height: 6*sc)), with: .color(Color(hex: 0x34D399).opacity(0.55)))
        }
    }
}

// MARK: - Floating Particles
struct FloatingParticlesView: View {
    let colors: LichSoColors

    struct Particle: Identifiable {
        let id = UUID()
        let x: CGFloat
        let y: CGFloat
        let size: CGFloat
        let color: Color
        let duration: Double
        let delay: Double
    }

    @State private var animate = false

    private var particles: [Particle] {
        let particleColors = [colors.cyan, colors.cyan2, colors.gold, colors.green]
        return (0..<18).map { i in
            Particle(
                x: CGFloat.random(in: 0.05...0.95),
                y: CGFloat.random(in: 0.1...0.9),
                size: CGFloat.random(in: 2...5),
                color: particleColors[i % particleColors.count],
                duration: Double.random(in: 2.5...4.5),
                delay: Double.random(in: 0...1.5)
            )
        }
    }

    var body: some View {
        GeometryReader { geo in
            ForEach(particles) { p in
                Circle()
                    .fill(p.color.opacity(0.3))
                    .frame(width: p.size, height: p.size)
                    .position(
                        x: geo.size.width * p.x,
                        y: geo.size.height * p.y + (animate ? -30 : 30)
                    )
                    .opacity(animate ? 0.6 : 0.1)
                    .animation(
                        .easeInOut(duration: p.duration)
                        .repeatForever(autoreverses: true)
                        .delay(p.delay),
                        value: animate
                    )
            }
        }
        .onAppear { animate = true }
    }
}

// MARK: - Loading Dots Animation
struct SplashLoadingDots: View {
    let color: Color
    @State private var activeIndex: Int = 0
    let timer = Timer.publish(every: 0.4, on: .main, in: .common).autoconnect()

    var body: some View {
        HStack(spacing: 6) {
            ForEach(0..<3) { i in
                Circle()
                    .fill(color.opacity(i == activeIndex ? 1.0 : 0.25))
                    .frame(width: 6, height: 6)
                    .scaleEffect(i == activeIndex ? 1.3 : 1.0)
                    .animation(.easeInOut(duration: 0.3), value: activeIndex)
            }
        }
        .onReceive(timer) { _ in
            activeIndex = (activeIndex + 1) % 3
        }
    }
}
