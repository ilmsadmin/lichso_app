import SwiftUI

// MARK: - Home Screen (Mock L-shape Design)
struct HomeScreen: View {
    @ObservedObject var viewModel: HomeViewModel
    @ObservedObject var chatViewModel: ChatViewModel
    @Environment(\.lichSoColors) var c
    var onSettings: () -> Void = {}
    var onCalendar: () -> Void = {}
    var onTasks: () -> Void = {}
    @State private var currentTime = Date()
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    @AppStorage("lunarBadgeEnabled") private var lunarBadgeEnabled: Bool = true

    var body: some View {
        VStack(spacing: 0) {
            // MARK: App Header
            AppHeaderView(info: viewModel.dayInfo, onSettings: onSettings)

            if let info = viewModel.dayInfo {
                // MARK: Main L-shape Grid (fills available height)
                LShapeGridView(
                    info: info,
                    currentTime: currentTime,
                    chatViewModel: chatViewModel,
                    onPrev: viewModel.prevDay,
                    onNext: viewModel.nextDay,
                    onCalendar: onCalendar,
                    onTasks: onTasks
                )
                .frame(maxHeight: .infinity)
            } else {
                Spacer()
            }

            // MARK: Chat Input Bar
            HomeChatInputBar(chatViewModel: chatViewModel)
                .padding(.horizontal, 16)
                .padding(.bottom, 8)
        }
        .background(c.bg.ignoresSafeArea())
        .onReceive(timer) { currentTime = $0 }
    }
}

// MARK: - App Header (Brand title + Date pill)
struct AppHeaderView: View {
    let info: DayInfo?
    let onSettings: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 3) {
                Text("Lịch Số AI")
                    .font(.custom("Nunito", size: 22).weight(.black))
                    .foregroundStyle(c.brandGradient)
                Text("Trợ lý vạn niên thông minh")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(c.textTertiary)
                    .tracking(0.3)
            }

            Spacer()

            if let info = info {
                DatePillView(info: info)
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 2)
        .padding(.bottom, 12)
    }
}

// MARK: - Date Pill
struct DatePillView: View {
    let info: DayInfo
    @Environment(\.lichSoColors) var c

    private var solarDateString: String {
        let weekday = info.dayOfWeek
        return "\(weekday), \(String(format: "%02d", info.solar.dd))/\(String(format: "%02d", info.solar.mm))/\(info.solar.yy)"
    }

    private var lunarDateString: String {
        "月 \(info.lunar.day) \(info.lunar.monthName) \(info.yearCanChi)"
    }

    var body: some View {
        VStack(alignment: .trailing, spacing: 2) {
            Text(solarDateString)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(c.cyan)
                .tracking(0.2)
            Text(lunarDateString)
                .font(.system(size: 9.5, weight: .medium))
                .foregroundColor(c.gold)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 6)
        .background(c.cyan.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(c.cyan.opacity(0.16), lineWidth: 1)
        )
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(solarDateString), Âm lịch \(lunarDateString)")
    }
}

// MARK: - L-Shape Grid Layout
struct LShapeGridView: View {
    let info: DayInfo
    let currentTime: Date
    @ObservedObject var chatViewModel: ChatViewModel
    let onPrev: () -> Void
    let onNext: () -> Void
    var onCalendar: () -> Void = {}
    var onTasks: () -> Void = {}
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 0) {
            // LEFT: Chat Feed Panel (full height)
            ChatFeedPanel(info: info, chatViewModel: chatViewModel, onPrev: onPrev, onNext: onNext)

            // RIGHT: Robot (top) + Extension cards (bottom) — match left height
            VStack(spacing: 0) {
                // Robot Cell (fixed size)
                RobotCellView(info: info)

                // Subtle divider between robot and extension cards
                Rectangle()
                    .fill(c.borderSubtle)
                    .frame(height: 0.5)
                    .padding(.horizontal, 6)

                // Extension Cards — expands to fill remaining height
                ExtensionCardsView(info: info, onCalendar: onCalendar, onTasks: onTasks)
            }
            .frame(width: 118)
        }
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(c.border, lineWidth: 1)
                )
                .shadow(color: c.isDark ? Color.black.opacity(0.3) : Color.black.opacity(0.08), radius: 12, x: 0, y: 4)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .padding(.horizontal, 16)
        .padding(.bottom, 12)
    }
}

// MARK: - Chat Feed Panel (Left Column — replaces NotePanel)
struct ChatFeedPanel: View {
    let info: DayInfo
    @ObservedObject var chatViewModel: ChatViewModel
    let onPrev: () -> Void
    let onNext: () -> Void
    @Environment(\.lichSoColors) var c

    /// Show only the recent messages (last N)
    private var recentMessages: [ChatMessage] {
        Array(chatViewModel.messages.suffix(20))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 6) {
                // AI status dot
                Circle()
                    .fill(c.green)
                    .frame(width: 6, height: 6)

                Text("TRỢ LÝ AI")
                    .font(.system(size: 10.5, weight: .semibold))
                    .foregroundColor(c.textTertiary)
                    .tracking(1.1)

                Spacer()

                // Typing indicator in header
                if chatViewModel.isTyping {
                    HStack(spacing: 2) {
                        ForEach(0..<3, id: \.self) { i in
                            Circle()
                                .fill(c.cyan)
                                .frame(width: 3, height: 3)
                                .opacity(0.7)
                        }
                    }
                    .padding(.trailing, 4)
                }

                // Clear chat button (only show when there are messages)
                if !chatViewModel.messages.isEmpty {
                    Button(action: { chatViewModel.clearChat() }) {
                        Image(systemName: "arrow.counterclockwise")
                            .font(.system(size: 9, weight: .semibold))
                            .foregroundColor(c.textTertiary)
                            .frame(width: 22, height: 22)
                            .background(
                                Circle()
                                    .fill(c.isDark ? Color.white.opacity(0.05) : Color.black.opacity(0.04))
                                    .overlay(Circle().stroke(c.border, lineWidth: 1))
                            )
                    }
                }
            }
            .padding(.horizontal, 15)
            .padding(.vertical, 10)

            Divider().overlay(c.borderSubtle)

            // Chat feed or empty state
            if recentMessages.isEmpty {
                ChatFeedEmptyState(info: info)
            } else {
                // Scrollable chat messages
                ScrollViewReader { proxy in
                    ScrollView(.vertical, showsIndicators: false) {
                        LazyVStack(spacing: 6) {
                            ForEach(recentMessages) { msg in
                                MiniChatBubble(message: msg, chatViewModel: chatViewModel)
                                    .id(msg.id)
                            }
                            if chatViewModel.isTyping {
                                MiniTypingBubble()
                                    .id("mini-typing")
                            }
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 8)
                    }
                    .onChange(of: chatViewModel.messages.count) { _ in
                        withAnimation(.easeOut(duration: 0.2)) {
                            if let last = recentMessages.last {
                                proxy.scrollTo(last.id, anchor: .bottom)
                            }
                        }
                    }
                    .onChange(of: chatViewModel.isTyping) { isTyping in
                        if isTyping {
                            withAnimation(.easeOut(duration: 0.2)) {
                                proxy.scrollTo("mini-typing", anchor: .bottom)
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Chat Feed Empty State
struct ChatFeedEmptyState: View {
    let info: DayInfo
    @Environment(\.lichSoColors) var c
    @State private var pulseScale: CGFloat = 1

    var body: some View {
        VStack(spacing: 12) {
            Spacer()

            // Animated AI icon
            ZStack {
                Circle()
                    .fill(c.cyan.opacity(0.06))
                    .frame(width: 56, height: 56)
                    .scaleEffect(pulseScale)

                Circle()
                    .fill(
                        LinearGradient(
                            colors: [c.cyan.opacity(0.15), c.cyan2.opacity(0.1)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 40, height: 40)

                Image(systemName: "sparkles")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(
                        LinearGradient(colors: [c.cyan, c.cyan2], startPoint: .top, endPoint: .bottom)
                    )
            }
            .onAppear {
                withAnimation(.easeInOut(duration: 2.5).repeatForever(autoreverses: true)) {
                    pulseScale = 1.3
                }
            }

            VStack(spacing: 4) {
                Text("Hỏi tôi bất cứ điều gì")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(c.textPrimary)

                Text("Phong thuỷ · Ngày tốt · Nhắc việc")
                    .font(.system(size: 9.5))
                    .foregroundColor(c.textTertiary)
                    .tracking(0.3)
            }

            // Quick day summary card
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 5) {
                    Image(systemName: "calendar")
                        .font(.system(size: 9))
                        .foregroundColor(c.cyan)
                    Text("Hôm nay")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(c.cyan)
                        .tracking(0.5)
                }

                Text(buildDaySummary())
                    .font(.system(size: 9.5))
                    .foregroundColor(c.textSecondary)
                    .lineSpacing(3)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(c.cyan.opacity(0.04))
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(c.cyan.opacity(0.12), lineWidth: 1)
                    )
            )
            .padding(.horizontal, 10)

            Spacer()
        }
    }

    private func buildDaySummary() -> String {
        let nen = info.activities.nenLam.prefix(2).joined(separator: ", ")
        let gio = info.gioHoangDao.first.map { "\($0.name) (\($0.time))" } ?? ""
        return "✅ \(nen)\n⏰ Giờ tốt: \(gio)"
    }
}

// MARK: - Mini Chat Bubble (compact for home feed)
struct MiniChatBubble: View {
    let message: ChatMessage
    let chatViewModel: ChatViewModel
    @Environment(\.lichSoColors) var c

    var isUser: Bool { message.isUser }

    var body: some View {
        HStack(alignment: .bottom, spacing: 5) {
            if isUser { Spacer(minLength: 20) }

            // AI avatar (only for AI messages)
            if !isUser {
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(colors: [c.cyan.opacity(0.2), c.cyan2.opacity(0.15)],
                                           startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                        .frame(width: 18, height: 18)
                    Image(systemName: "sparkles")
                        .font(.system(size: 8, weight: .bold))
                        .foregroundColor(c.cyan)
                }
            }

            VStack(alignment: isUser ? .trailing : .leading, spacing: 2) {
                Text(message.content)
                    .font(.system(size: 10.5))
                    .foregroundColor(isUser ? (c.isDark ? c.teal2 : Color(hex: 0x1A6B5A)) : c.textPrimary)
                    .lineSpacing(2)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 7)
                    .background(
                        isUser
                        ? (c.isDark ? Color(hex: 0x1A2A25).opacity(0.8) : Color(hex: 0xE4F5F0))
                        : c.bg2
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(
                                isUser
                                ? Color(hex: 0x4ABEAA).opacity(0.15)
                                : c.borderSubtle,
                                lineWidth: 0.5
                            )
                    )

                Text(chatViewModel.formatTime(message.timestamp))
                    .font(.system(size: 7.5))
                    .foregroundColor(c.textQuaternary)
                    .padding(.horizontal, 4)
            }

            if !isUser { Spacer(minLength: 20) }
        }
    }
}

// MARK: - Mini Typing Indicator
struct MiniTypingBubble: View {
    @Environment(\.lichSoColors) var c
    @State private var dotPhase: [CGFloat] = [0, 0, 0]

    var body: some View {
        HStack(alignment: .bottom, spacing: 5) {
            // AI avatar
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(colors: [c.cyan.opacity(0.2), c.cyan2.opacity(0.15)],
                                       startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .frame(width: 18, height: 18)
                Image(systemName: "sparkles")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(c.cyan)
            }

            HStack(spacing: 4) {
                ForEach(0..<3, id: \.self) { i in
                    Circle()
                        .fill(c.cyan.opacity(0.6))
                        .frame(width: 4, height: 4)
                        .offset(y: dotPhase[i])
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(c.bg2)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(c.borderSubtle, lineWidth: 0.5)
            )
            .onAppear {
                for i in 0..<3 {
                    withAnimation(.easeInOut(duration: 0.45).repeatForever(autoreverses: true).delay(Double(i) * 0.12)) {
                        dotPhase[i] = -3
                    }
                }
            }

            Spacer(minLength: 20)
        }
    }
}

// MARK: - Robot Cell (Top Right)
struct RobotCellView: View {
    let info: DayInfo
    @Environment(\.lichSoColors) var c
    @State private var floatOffset: CGFloat = 0
    @State private var glowScale: CGFloat = 1
    @State private var blinkPhase: Double = 0
    @State private var antennaPulse: Double = 1

    // Typewriter animation states
    private let speechMessages = [
        "Hỏi tôi về lịch âm,\nngày tốt, nhắc việc",
        "Tôi giúp bạn xem\nngày giờ hoàng đạo",
        "Đặt nhắc việc,\nghi chú nhanh nhé!"
    ]
    @State private var displayedText: String = ""
    @State private var currentMessageIndex: Int = 0
    @State private var charIndex: Int = 0
    @State private var isTyping: Bool = true
    @State private var bubbleOpacity: Double = 1
    @State private var typewriterTimer: Timer?

    var body: some View {
        VStack(spacing: 0) {
            // Speech bubble with typewriter effect
            VStack(spacing: 0) {
                Text(displayedText)
                    .font(.system(size: 8.5))
                    .foregroundColor(c.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(2)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 6)
                    .frame(minHeight: 34)
            }
            .opacity(bubbleOpacity)
            .background(
                RoundedRectangle(cornerRadius: 11)
                    .fill(c.speechBg)
                    .overlay(
                        RoundedRectangle(cornerRadius: 11)
                            .stroke(c.border, lineWidth: 1)
                    )
                    .shadow(color: c.isDark ? Color.black.opacity(0.15) : Color.black.opacity(0.05), radius: 4, y: 2)
            )
            .padding(.horizontal, 6)

            // Triangle
            Triangle()
                .fill(c.speechBg)
                .frame(width: 10, height: 6)

            // Robot Body
            ZStack {
                // Glow
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [c.isDark ? Color.white.opacity(0.06) : c.cyan.opacity(0.1), Color.clear],
                            center: .center,
                            startRadius: 0,
                            endRadius: 44
                        )
                    )
                    .frame(width: 88, height: 88)
                    .scaleEffect(glowScale)

                // Robot SVG-like drawing
                RobotAvatar(colors: c, blinkPhase: blinkPhase, antennaPulse: antennaPulse)
                    .frame(width: 80, height: 96)
                    .offset(y: floatOffset)
                    .shadow(color: c.isDark ? Color.black.opacity(0.5) : Color.black.opacity(0.15), radius: 12, x: 0, y: 5)
            }

            // Status
            HStack(spacing: 4) {
                Circle()
                    .fill(c.green)
                    .frame(width: 5, height: 5)
                Text("Sẵn sàng")
                    .font(.system(size: 8, weight: .medium))
                    .foregroundColor(c.textTertiary)
            }
            .padding(.top, 2)
        }
        .padding(.top, 4)
        .padding(.bottom, 3)
        .onAppear {
            withAnimation(.easeInOut(duration: 4).repeatForever(autoreverses: true)) {
                floatOffset = -7
            }
            withAnimation(.easeInOut(duration: 3.5).repeatForever(autoreverses: true)) {
                glowScale = 1.5
            }
            // Blink
            Timer.scheduledTimer(withTimeInterval: 4.8, repeats: true) { _ in
                withAnimation(.linear(duration: 0.12)) { blinkPhase = 1 }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.12) {
                    withAnimation(.linear(duration: 0.12)) { blinkPhase = 0 }
                }
            }
            withAnimation(.easeInOut(duration: 2.2).repeatForever(autoreverses: true)) {
                antennaPulse = 0.35
            }
            // Start typewriter
            startTypewriter()
        }
        .onDisappear {
            typewriterTimer?.invalidate()
            typewriterTimer = nil
        }
    }

    // MARK: Typewriter Animation
    private func startTypewriter() {
        let message = speechMessages[currentMessageIndex]
        let characters = Array(message)
        charIndex = 0
        displayedText = ""
        bubbleOpacity = 1

        typewriterTimer = Timer.scheduledTimer(withTimeInterval: 0.06, repeats: true) { timer in
            if charIndex < characters.count {
                displayedText.append(characters[charIndex])
                charIndex += 1
            } else {
                // Done typing — pause, then fade out and move to next message
                timer.invalidate()
                typewriterTimer = nil

                // Hold the full text for a moment
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    // Fade out
                    withAnimation(.easeInOut(duration: 0.6)) {
                        bubbleOpacity = 0
                    }
                    // After fade out, switch message and restart
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) {
                        currentMessageIndex = (currentMessageIndex + 1) % speechMessages.count
                        displayedText = ""
                        withAnimation(.easeInOut(duration: 0.3)) {
                            bubbleOpacity = 1
                        }
                        // Small delay before typing starts again
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                            startTypewriter()
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Triangle Shape
struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.closeSubpath()
        return path
    }
}

// MARK: - L-Shape Border (Selective Edge + Corner)
/// Draws a continuous border path on selected edges with rounded corners where two bordered edges meet.
struct LShapeBorder: Shape {
    let cornerRadius: CGFloat
    let lineWidth: CGFloat
    let edges: Set<Edge>

    enum Edge: Hashable {
        case top, trailing, bottom, leading
    }

    func path(in rect: CGRect) -> Path {
        let r = cornerRadius
        let hw = lineWidth / 2
        let minX = rect.minX + hw
        let maxX = rect.maxX - hw
        let minY = rect.minY + hw
        let maxY = rect.maxY - hw

        let rTL = edges.contains(.top) && edges.contains(.leading)
        let rTR = edges.contains(.top) && edges.contains(.trailing)
        let rBR = edges.contains(.bottom) && edges.contains(.trailing)
        let rBL = edges.contains(.bottom) && edges.contains(.leading)

        // Build segments clockwise: top → trailing → bottom → leading
        // Track whether previous segment's end connects to next segment's start
        var path = Path()
        var started = false

        // Helper: continue drawing or start new sub-path
        func goTo(_ p: CGPoint) {
            if started { path.addLine(to: p) }
            else { path.move(to: p); started = true }
        }

        // ── TOP edge ──
        if edges.contains(.top) {
            let sx = rTL ? minX + r : minX
            let ex = rTR ? maxX - r : maxX
            // If leading edge was just drawn and ended with TL arc, pen is already at (sx, minY)
            // Otherwise start fresh
            if !(edges.contains(.leading) && rTL && started) {
                path.move(to: CGPoint(x: sx, y: minY))
                started = true
            }
            goTo(CGPoint(x: ex, y: minY))
            if rTR {
                path.addArc(center: CGPoint(x: maxX - r, y: minY + r),
                            radius: r, startAngle: .degrees(-90), endAngle: .degrees(0), clockwise: false)
            }
        } else {
            started = false
        }

        // ── TRAILING edge ──
        if edges.contains(.trailing) {
            let sy = rTR ? minY + r : minY
            let ey = rBR ? maxY - r : maxY
            if !(edges.contains(.top) && rTR && started) {
                path.move(to: CGPoint(x: maxX, y: sy))
                started = true
            }
            goTo(CGPoint(x: maxX, y: ey))
            if rBR {
                path.addArc(center: CGPoint(x: maxX - r, y: maxY - r),
                            radius: r, startAngle: .degrees(0), endAngle: .degrees(90), clockwise: false)
            }
        } else {
            started = false
        }

        // ── BOTTOM edge ──
        if edges.contains(.bottom) {
            let sx = rBR ? maxX - r : maxX
            let ex = rBL ? minX + r : minX
            if !(edges.contains(.trailing) && rBR && started) {
                path.move(to: CGPoint(x: sx, y: maxY))
                started = true
            }
            goTo(CGPoint(x: ex, y: maxY))
            if rBL {
                path.addArc(center: CGPoint(x: minX + r, y: maxY - r),
                            radius: r, startAngle: .degrees(90), endAngle: .degrees(180), clockwise: false)
            }
        } else {
            started = false
        }

        // ── LEADING edge ──
        if edges.contains(.leading) {
            let sy = rBL ? maxY - r : maxY
            let ey = rTL ? minY + r : minY
            if !(edges.contains(.bottom) && rBL && started) {
                path.move(to: CGPoint(x: minX, y: sy))
                started = true
            }
            goTo(CGPoint(x: minX, y: ey))
            if rTL {
                path.addArc(center: CGPoint(x: minX + r, y: minY + r),
                            radius: r, startAngle: .degrees(180), endAngle: .degrees(270), clockwise: false)
            }
        }

        return path
    }
}

// MARK: - Robot Avatar (Canvas drawing)
struct RobotAvatar: View {
    let colors: LichSoColors
    let blinkPhase: Double
    let antennaPulse: Double

    var body: some View {
        Canvas { ctx, size in
            let w = size.width
            let h = size.height
            let cx = w / 2
            let sc = min(w / 100, h / 128)

            // Colors
            let bodyFill = colors.isDark
                ? Color(hex: 0xE0E8F5)
                : Color(hex: 0xD8E0EE)
            let bodyStroke = colors.isDark
                ? Color(hex: 0xC0C8D6)
                : Color(hex: 0xAAB2C0)
            let eyeColor = Color(hex: 0x4ECDC4)
            let chestLineGreen = Color(hex: 0x4ECDC4).opacity(0.55)
            let chestLineBlue = Color(hex: 0x38BDF8).opacity(0.42)
            let chestLineGold = Color(hex: 0xF5C842).opacity(0.35)
            let chestDot = Color(hex: 0x34D399).opacity(0.55)

            // Antenna Left
            var antLPath = Path()
            antLPath.move(to: CGPoint(x: 33*sc, y: 17*sc))
            antLPath.addLine(to: CGPoint(x: 27*sc, y: 6*sc))
            ctx.stroke(antLPath, with: .color(bodyStroke.opacity(antennaPulse)), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 22.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyFill.opacity(antennaPulse)))
            ctx.stroke(Path(ellipseIn: CGRect(x: 22.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyStroke.opacity(antennaPulse)), style: StrokeStyle(lineWidth: 1*sc))

            // Antenna Right
            var antRPath = Path()
            antRPath.move(to: CGPoint(x: 67*sc, y: 17*sc))
            antRPath.addLine(to: CGPoint(x: 73*sc, y: 6*sc))
            ctx.stroke(antRPath, with: .color(bodyStroke.opacity(antennaPulse)), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 70.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyFill.opacity(antennaPulse)))
            ctx.stroke(Path(ellipseIn: CGRect(x: 70.5*sc, y: 1*sc, width: 7*sc, height: 7*sc)), with: .color(bodyStroke.opacity(antennaPulse)), style: StrokeStyle(lineWidth: 1*sc))

            // Head
            ctx.fill(Path(ellipseIn: CGRect(x: 20*sc, y: 8*sc, width: 60*sc, height: 52*sc)), with: .color(bodyFill))
            // Head highlight
            ctx.fill(Path(ellipseIn: CGRect(x: 31*sc, y: 17.5*sc, width: 24*sc, height: 11*sc)), with: .color(Color.white.opacity(0.5)))

            // Visor
            ctx.fill(Path(ellipseIn: CGRect(x: 28*sc, y: 21*sc, width: 44*sc, height: 30*sc)), with: .color(Color(hex: 0xB4C4DC).opacity(0.22)))

            // Eyes (with blink)
            let eyeH = 18.0 * sc * (1.0 - blinkPhase)
            if eyeH > 0.5 {
                // Left eye
                ctx.fill(Path(ellipseIn: CGRect(x: 31*sc, y: 27*sc + (18*sc - eyeH)/2, width: 16*sc, height: eyeH)), with: .color(Color(hex: 0x141E30)))
                ctx.fill(Path(ellipseIn: CGRect(x: 32.8*sc, y: 27*sc + (18*sc - eyeH)/2, width: 12.4*sc, height: eyeH * 0.8)), with: .color(eyeColor.opacity(0.92)))
                ctx.fill(Path(ellipseIn: CGRect(x: 36.2*sc, y: 29.8*sc + (18*sc - eyeH)/4, width: 5.6*sc, height: eyeH * 0.35)), with: .color(Color.white.opacity(0.9)))
                // Right eye
                ctx.fill(Path(ellipseIn: CGRect(x: 53*sc, y: 27*sc + (18*sc - eyeH)/2, width: 16*sc, height: eyeH)), with: .color(Color(hex: 0x141E30)))
                ctx.fill(Path(ellipseIn: CGRect(x: 54.8*sc, y: 27*sc + (18*sc - eyeH)/2, width: 12.4*sc, height: eyeH * 0.8)), with: .color(eyeColor.opacity(0.92)))
                ctx.fill(Path(ellipseIn: CGRect(x: 58.2*sc, y: 29.8*sc + (18*sc - eyeH)/4, width: 5.6*sc, height: eyeH * 0.35)), with: .color(Color.white.opacity(0.9)))
            }

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

            // Body highlight
            ctx.fill(Path(ellipseIn: CGRect(x: 24*sc, y: 62*sc, width: 28*sc, height: 10*sc)), with: .color(Color.white.opacity(0.45)))

            // Arms
            ctx.fill(Path(ellipseIn: CGRect(x: 3*sc, y: 72.5*sc, width: 14*sc, height: 21*sc)), with: .color(bodyFill))
            ctx.fill(Path(ellipseIn: CGRect(x: 83*sc, y: 72.5*sc, width: 14*sc, height: 21*sc)), with: .color(bodyFill))
            // Arm highlights
            ctx.fill(Path(ellipseIn: CGRect(x: 5*sc, y: 74*sc, width: 6*sc, height: 8*sc)), with: .color(Color.white.opacity(0.40)))
            ctx.fill(Path(ellipseIn: CGRect(x: 89*sc, y: 74*sc, width: 6*sc, height: 8*sc)), with: .color(Color.white.opacity(0.40)))

            // Chest mini screen
            ctx.fill(Path(roundedRect: CGRect(x: 35*sc, y: 76*sc, width: 30*sc, height: 22*sc), cornerRadius: 7*sc), with: .color(Color(hex: 0xB4C4DC).opacity(0.22)))
            // Chest lines
            var l1 = Path(); l1.move(to: CGPoint(x: 38*sc, y: 82*sc)); l1.addLine(to: CGPoint(x: 56*sc, y: 82*sc))
            ctx.stroke(l1, with: .color(chestLineGreen), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            var l2 = Path(); l2.move(to: CGPoint(x: 38*sc, y: 87*sc)); l2.addLine(to: CGPoint(x: 50*sc, y: 87*sc))
            ctx.stroke(l2, with: .color(chestLineBlue), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            var l3 = Path(); l3.move(to: CGPoint(x: 38*sc, y: 92*sc)); l3.addLine(to: CGPoint(x: 53*sc, y: 92*sc))
            ctx.stroke(l3, with: .color(chestLineGold), style: StrokeStyle(lineWidth: 2*sc, lineCap: .round))
            ctx.fill(Path(ellipseIn: CGRect(x: 57*sc, y: 84*sc, width: 6*sc, height: 6*sc)), with: .color(chestDot))
        }
    }
}

// MARK: - Extension Cards (Bottom Right)
struct ExtensionCardsView: View {
    let info: DayInfo
    var onCalendar: () -> Void = {}
    var onTasks: () -> Void = {}
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 6) {
            // ─── Quick Nav Buttons ───
            HStack(spacing: 5) {
                QuickNavButton(icon: "calendar", label: "Lịch", color: c.cyan, action: onCalendar)
                QuickNavButton(icon: "checklist", label: "Việc", color: c.gold, action: onTasks)
            }

            // Giờ tốt card
            ExtCard(
                icon: "sun.max",
                iconBg: c.cyan.opacity(0.12),
                iconColor: c.cyan,
                title: "Giờ tốt",
                text: info.gioHoangDao.prefix(2).map { "\($0.name) \($0.time)" }.joined(separator: "\n")
            )

            // Tránh giờ card
            ExtCard(
                icon: "exclamationmark.triangle",
                iconBg: c.gold.opacity(0.12),
                iconColor: c.gold,
                title: "Tránh giờ",
                text: "Hung thần: \(info.huong.hungThan)"
            )

            // Hướng xuất hành card
            ExtCard(
                icon: "location.north.line",
                iconBg: c.cyan.opacity(0.12),
                iconColor: c.cyan,
                title: "Hướng xuất",
                text: "\(info.huong.thanTai) cát tường"
            )

            Spacer(minLength: 0)
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 9)
        .frame(maxHeight: .infinity)
    }
}

// MARK: - Extension Card
struct ExtCard: View {
    let icon: String
    let iconBg: Color
    let iconColor: Color
    let title: String
    let text: String
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(alignment: .top, spacing: 7) {
            ZStack {
                RoundedRectangle(cornerRadius: 6)
                    .fill(iconBg)
                    .frame(width: 22, height: 22)
                Image(systemName: icon)
                    .font(.system(size: 10))
                    .foregroundColor(iconColor)
            }
            .padding(.top, 1)

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 9, weight: .bold))
                    .foregroundColor(c.textPrimary)
                Text(text)
                    .font(.system(size: 8.5))
                    .foregroundColor(c.textSecondary)
                    .lineSpacing(2)
            }
        }
        .padding(7)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(c.isDark ? Color.white.opacity(0.03) : Color.black.opacity(0.03))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(c.borderSubtle, lineWidth: 1)
                )
        )
    }
}

// MARK: - Quick Nav Button (Calendar / Tasks)
struct QuickNavButton: View {
    let icon: String
    let label: String
    let color: Color
    let action: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        Button(action: action) {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(color)
                Text(label)
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(color)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 7)
            .background(
                RoundedRectangle(cornerRadius: 9)
                    .fill(color.opacity(0.08))
                    .overlay(
                        RoundedRectangle(cornerRadius: 9)
                            .stroke(color.opacity(0.2), lineWidth: 1)
                    )
            )
        }
    }
}

// MARK: - Text lineHeight modifier
extension Text {
    func lineHeight(_ lineHeight: CGFloat) -> some View {
        self.lineSpacing(lineHeight * 3)
    }
}

// MARK: - Live Clock (kept for other screens)
struct LiveClockSection: View {
    let currentTime: Date
    let gioHoangDao: [GioHoangDaoInfo]
    @Environment(\.lichSoColors) var c

    private var timeString: String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss"
        return f.string(from: currentTime)
    }

    private var gioName: String {
        let h = Calendar.current.component(.hour, from: currentTime)
        return LunarCalendarEngine.gioName(h)
    }

    private var isHoangDao: Bool {
        gioHoangDao.contains { $0.name == gioName }
    }

    var body: some View {
        VStack(spacing: 4) {
            Text(timeString)
                .font(.system(size: 36, weight: .bold, design: .serif))
                .foregroundColor(c.textPrimary)
                .monospacedDigit()
                .tracking(2)

            HStack(spacing: 6) {
                Text("Giờ \(gioName)")
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)

                if isHoangDao {
                    Text("·")
                        .foregroundColor(c.textTertiary)
                    Image(systemName: "sparkles")
                        .font(.system(size: 11))
                        .foregroundColor(c.teal2)
                    Text("Hoàng Đạo")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(c.teal2)
                }
            }
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Prompt Template Model
struct PromptTemplate: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let prompt: String
    let category: PromptCategory
    let color: Color

    enum PromptCategory: String, CaseIterable {
        case calendar = "Lịch & Ngày"
        case horoscope = "Tử Vi & Phong Thuỷ"
        case productivity = "Ghi Chú & Nhắc Nhở"
    }
}

// MARK: - Predefined Templates
extension PromptTemplate {
    static let allTemplates: [PromptTemplate] = [
        // ── Lịch & Ngày ──
        PromptTemplate(
            icon: "calendar.badge.clock",
            title: "Chi tiết ngày hôm nay",
            prompt: "Cho tôi xem chi tiết đầy đủ về ngày hôm nay: thông tin âm lịch, dương lịch, ngày can chi, giờ hoàng đạo, sao chiếu, trực ngày, tiết khí, nên làm và không nên làm.",
            category: .calendar,
            color: Color(hex: 0x5BC0EB)
        ),
        PromptTemplate(
            icon: "calendar",
            title: "Xem lịch tháng này",
            prompt: "Cho tôi xem lịch tháng này với các ngày tốt xấu, ngày lễ âm lịch và dương lịch đáng chú ý.",
            category: .calendar,
            color: Color(hex: 0x5BC0EB)
        ),
        PromptTemplate(
            icon: "arrow.left.arrow.right",
            title: "Đổi ngày Âm ↔ Dương",
            prompt: "Đổi ngày âm lịch sang dương lịch: [nhập ngày âm]. Hoặc đổi dương lịch sang âm: [nhập ngày dương].",
            category: .calendar,
            color: Color(hex: 0x5BC0EB)
        ),
        PromptTemplate(
            icon: "moon.stars",
            title: "Giờ Hoàng Đạo hôm nay",
            prompt: "Cho tôi xem các giờ hoàng đạo hôm nay và giải thích giờ nào tốt cho việc gì.",
            category: .calendar,
            color: Color(hex: 0x5BC0EB)
        ),
        PromptTemplate(
            icon: "checkmark.seal",
            title: "Ngày tốt sắp tới",
            prompt: "Trong 7 ngày tới, ngày nào là ngày tốt để làm việc lớn (khai trương, cưới hỏi, ký hợp đồng, xuất hành)?",
            category: .calendar,
            color: Color(hex: 0x5BC0EB)
        ),

        // ── Tử Vi & Phong Thuỷ ──
        PromptTemplate(
            icon: "sparkles",
            title: "Tử vi hôm nay",
            prompt: "Xem tử vi ngày hôm nay cho tuổi [nhập tuổi/năm sinh] về công việc, tài lộc, tình duyên, sức khoẻ.",
            category: .horoscope,
            color: Color(hex: 0xF5C842)
        ),
        PromptTemplate(
            icon: "tornado",
            title: "Ngũ hành & Mệnh",
            prompt: "Phân tích ngũ hành và mệnh của người sinh năm [nhập năm sinh]. Cho biết hợp/kỵ màu sắc, hướng, số.",
            category: .horoscope,
            color: Color(hex: 0xF5C842)
        ),
        PromptTemplate(
            icon: "location.north",
            title: "Hướng xuất hành tốt",
            prompt: "Hôm nay xuất hành hướng nào tốt? Hướng nào cần tránh? Thần Tài, Hỷ Thần, Hắc Thần ở đâu?",
            category: .horoscope,
            color: Color(hex: 0xF5C842)
        ),
        PromptTemplate(
            icon: "house",
            title: "Phong thuỷ nhà cửa",
            prompt: "Tư vấn phong thuỷ cơ bản cho nhà ở: bố trí bàn thờ, bếp, giường ngủ, bàn làm việc hợp hướng tốt.",
            category: .horoscope,
            color: Color(hex: 0xF5C842)
        ),

        // ── Ghi Chú & Nhắc Nhở ──
        PromptTemplate(
            icon: "note.text.badge.plus",
            title: "Tạo ghi chú",
            prompt: "Tạo ghi chú mới với tiêu đề: [nhập tiêu đề] và nội dung: [nhập nội dung].",
            category: .productivity,
            color: Color(hex: 0x38D9A9)
        ),
        PromptTemplate(
            icon: "checklist",
            title: "Tạo danh sách việc",
            prompt: "Tạo danh sách công việc cần làm hôm nay: [liệt kê các việc, cách nhau bằng dấu phẩy].",
            category: .productivity,
            color: Color(hex: 0x38D9A9)
        ),
        PromptTemplate(
            icon: "bell.badge",
            title: "Đặt nhắc nhở",
            prompt: "Nhắc tôi [nội dung nhắc nhở] vào lúc [giờ] ngày [ngày tháng].",
            category: .productivity,
            color: Color(hex: 0x38D9A9)
        ),
        PromptTemplate(
            icon: "gift",
            title: "Chọn ngày tổ chức sự kiện",
            prompt: "Giúp tôi chọn ngày tốt để [cưới hỏi/khai trương/động thổ/nhập trạch] trong tháng [nhập tháng năm].",
            category: .productivity,
            color: Color(hex: 0x38D9A9)
        ),
        PromptTemplate(
            icon: "text.badge.star",
            title: "Tóm tắt ngày",
            prompt: "Tóm tắt toàn bộ thông tin quan trọng về ngày hôm nay: lịch, tử vi, nhắc nhở, giờ tốt — trong 1 đoạn ngắn gọn.",
            category: .productivity,
            color: Color(hex: 0x38D9A9)
        ),
    ]
}

// MARK: - Home Chat Input Bar (Real AI Input)
struct HomeChatInputBar: View {
    @ObservedObject var chatViewModel: ChatViewModel
    @Environment(\.lichSoColors) var c
    @State private var inputText: String = ""
    @FocusState private var isFocused: Bool
    @State private var showTemplates: Bool = false

    private var canSend: Bool {
        !inputText.trimmingCharacters(in: .whitespaces).isEmpty && !chatViewModel.isTyping
    }

    var body: some View {
        HStack(spacing: 8) {
            // Template button
            Button(action: { showTemplates = true }) {
                ZStack {
                    Circle()
                        .fill(showTemplates ? c.cyan.opacity(0.15) : c.surface.opacity(0.6))
                        .frame(width: 36, height: 36)
                        .overlay(
                            Circle()
                                .stroke(showTemplates ? c.cyan.opacity(0.3) : c.border, lineWidth: 1)
                        )
                    Image(systemName: "text.badge.star")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(showTemplates ? c.cyan : c.textTertiary)
                }
            }

            // Text field
            TextField("Hỏi lịch, đặt nhắc việc, ra lệnh AI...", text: $inputText, axis: .vertical)
                .font(.system(size: 13))
                .foregroundColor(c.textPrimary)
                .focused($isFocused)
                .lineLimit(1...3)
                .submitLabel(.send)
                .onSubmit {
                    sendMessage()
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 22)
                        .fill(c.inputBg)
                        .overlay(
                            RoundedRectangle(cornerRadius: 22)
                                .stroke(isFocused ? c.cyan.opacity(0.4) : c.border, lineWidth: 1)
                        )
                )

            // Send button
            Button(action: sendMessage) {
                ZStack {
                    Circle()
                        .fill(
                            canSend
                            ? LinearGradient(colors: [c.cyan, c.cyan2], startPoint: .topLeading, endPoint: .bottomTrailing)
                            : LinearGradient(colors: [c.surface, c.surface], startPoint: .top, endPoint: .bottom)
                        )
                        .frame(width: 36, height: 36)
                        .shadow(color: canSend ? c.cyan.opacity(0.25) : Color.clear, radius: 6, x: 0, y: 2)

                    if chatViewModel.isTyping {
                        ProgressView()
                            .scaleEffect(0.7)
                            .tint(.white)
                    } else {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 14))
                            .foregroundColor(canSend ? .white : c.textQuaternary)
                    }
                }
            }
            .disabled(!canSend)
        }
        .sheet(isPresented: $showTemplates) {
            PromptTemplateSheet(
                onSelect: { template in
                    inputText = template.prompt
                    showTemplates = false
                    isFocused = true
                },
                onSend: { text in
                    showTemplates = false
                    chatViewModel.sendMessage(text)
                }
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
    }

    private func sendMessage() {
        let trimmed = inputText.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        chatViewModel.sendMessage(trimmed)
        inputText = ""
        isFocused = false
    }
}

// MARK: - Prompt Template Sheet
struct PromptTemplateSheet: View {
    let onSelect: (PromptTemplate) -> Void
    let onSend: (String) -> Void
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss
    @State private var selectedTemplate: PromptTemplate? = nil
    @State private var editedPrompt: String = ""
    @State private var searchText: String = ""
    @State private var selectedCategory: PromptTemplate.PromptCategory? = nil

    private var filteredTemplates: [PromptTemplate] {
        var list = PromptTemplate.allTemplates
        if let cat = selectedCategory {
            list = list.filter { $0.category == cat }
        }
        if !searchText.isEmpty {
            let lower = searchText.lowercased()
            list = list.filter {
                $0.title.lowercased().contains(lower) ||
                $0.prompt.lowercased().contains(lower)
            }
        }
        return list
    }

    var body: some View {
        NavigationView {
            ZStack {
                c.bg.ignoresSafeArea()

                VStack(spacing: 0) {
                    if let template = selectedTemplate {
                        // ── Edit Mode ──
                        templateEditView(template)
                    } else {
                        // ── Browse Mode ──
                        templateBrowseView
                    }
                }
            }
            .navigationTitle(selectedTemplate != nil ? "Chỉnh sửa" : "Mẫu lệnh AI")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    if selectedTemplate != nil {
                        Button(action: { withAnimation { selectedTemplate = nil; editedPrompt = "" } }) {
                            HStack(spacing: 4) {
                                Image(systemName: "chevron.left")
                                    .font(.system(size: 13, weight: .semibold))
                                Text("Danh sách")
                                    .font(.system(size: 14))
                            }
                            .foregroundColor(c.cyan)
                        }
                    } else {
                        Button("Đóng") { dismiss() }
                            .foregroundColor(c.textSecondary)
                    }
                }
            }
        }
    }

    // MARK: - Browse View
    @ViewBuilder
    private var templateBrowseView: some View {
        VStack(spacing: 12) {
            // Search bar
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 13))
                    .foregroundColor(c.textTertiary)
                TextField("Tìm mẫu lệnh...", text: $searchText)
                    .font(.system(size: 13))
                    .foregroundColor(c.textPrimary)
                if !searchText.isEmpty {
                    Button(action: { searchText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 13))
                            .foregroundColor(c.textQuaternary)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 9)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(c.inputBg)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(c.border, lineWidth: 1)
                    )
            )
            .padding(.horizontal, 16)
            .padding(.top, 8)

            // Category chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    categoryChip(title: "Tất cả", category: nil)
                    ForEach(PromptTemplate.PromptCategory.allCases, id: \.self) { cat in
                        categoryChip(title: cat.rawValue, category: cat)
                    }
                }
                .padding(.horizontal, 16)
            }

            // Template list
            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(filteredTemplates) { template in
                        templateRow(template)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 20)
            }
        }
    }

    // MARK: - Category Chip
    private func categoryChip(title: String, category: PromptTemplate.PromptCategory?) -> some View {
        let isSelected = selectedCategory == category
        return Button(action: { withAnimation(.easeInOut(duration: 0.2)) { selectedCategory = category } }) {
            Text(title)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(isSelected ? .white : c.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(
                    Capsule()
                        .fill(isSelected ? c.cyan : c.surface)
                )
                .overlay(
                    Capsule()
                        .stroke(isSelected ? Color.clear : c.border, lineWidth: 1)
                )
        }
    }

    // MARK: - Template Row
    private func templateRow(_ template: PromptTemplate) -> some View {
        Button(action: {
            withAnimation(.easeInOut(duration: 0.25)) {
                selectedTemplate = template
                editedPrompt = template.prompt
            }
        }) {
            HStack(spacing: 12) {
                // Icon
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(template.color.opacity(0.12))
                        .frame(width: 40, height: 40)
                    Image(systemName: template.icon)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(template.color)
                }

                // Title + preview
                VStack(alignment: .leading, spacing: 3) {
                    Text(template.title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                        .lineLimit(1)
                    Text(template.prompt)
                        .font(.system(size: 11))
                        .foregroundColor(c.textTertiary)
                        .lineLimit(2)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(c.textQuaternary)
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(c.panelBg)
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(c.border, lineWidth: 1)
                    )
            )
        }
    }

    // MARK: - Edit View
    private func templateEditView(_ template: PromptTemplate) -> some View {
        VStack(spacing: 16) {
            // Template header
            HStack(spacing: 12) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(template.color.opacity(0.12))
                        .frame(width: 44, height: 44)
                    Image(systemName: template.icon)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(template.color)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(template.title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(c.textPrimary)
                    Text(template.category.rawValue)
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(c.textTertiary)
                }
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)

            // Editable prompt
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text("NỘI DUNG LỆNH")
                        .font(.system(size: 10.5, weight: .semibold))
                        .foregroundColor(c.textTertiary)
                        .tracking(0.8)
                    Spacer()
                    // Reset button
                    Button(action: { editedPrompt = template.prompt }) {
                        HStack(spacing: 3) {
                            Image(systemName: "arrow.counterclockwise")
                                .font(.system(size: 9))
                            Text("Đặt lại")
                                .font(.system(size: 10, weight: .medium))
                        }
                        .foregroundColor(c.textTertiary)
                    }
                }
                .padding(.horizontal, 16)

                TextEditor(text: $editedPrompt)
                    .font(.system(size: 14))
                    .foregroundColor(c.textPrimary)
                    .scrollContentBackground(.hidden)
                    .padding(12)
                    .frame(minHeight: 120, maxHeight: 200)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(c.inputBg)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(c.cyan.opacity(0.3), lineWidth: 1)
                            )
                    )
                    .padding(.horizontal, 16)

                // Hint text
                Text("💡 Chỉnh sửa nội dung trong [ngoặc vuông] trước khi gửi")
                    .font(.system(size: 11))
                    .foregroundColor(c.textQuaternary)
                    .padding(.horizontal, 16)
            }

            Spacer()

            // Action buttons
            VStack(spacing: 10) {
                // Send now
                Button(action: {
                    let trimmed = editedPrompt.trimmingCharacters(in: .whitespaces)
                    guard !trimmed.isEmpty else { return }
                    onSend(trimmed)
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 14))
                        Text("Gửi ngay")
                            .font(.system(size: 15, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 13)
                    .background(
                        LinearGradient(colors: [c.cyan, c.cyan2], startPoint: .leading, endPoint: .trailing)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: c.cyan.opacity(0.25), radius: 8, x: 0, y: 3)
                }

                // Copy to input
                Button(action: {
                    onSelect(PromptTemplate(
                        icon: template.icon,
                        title: template.title,
                        prompt: editedPrompt,
                        category: template.category,
                        color: template.color
                    ))
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "pencil.line")
                            .font(.system(size: 14))
                        Text("Chép vào ô nhập")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(c.textSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(c.surface)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(c.border, lineWidth: 1)
                            )
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
        }
    }
}
