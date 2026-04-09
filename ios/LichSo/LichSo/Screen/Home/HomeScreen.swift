import SwiftUI

// ═══════════════════════════════════════════
// Home Screen — follows iOS HTML mock design
// (hero header + scrollable content cards)
// ═══════════════════════════════════════════

private let PrimaryRed = Color(hex: "B71C1C")
private let DeepRed = Color(hex: "8B0000")
private let GoldAccent = Color(hex: "D4A017")
private let SurfaceBg = Color(hex: "FFFBF5")
private let SurfaceContainer = Color(hex: "FFF8F0")
private let TextMain = Color(hex: "1C1B1F")
private let TextSub = Color(hex: "534340")
private let TextDim = Color(hex: "857371")
private let Outline = Color(hex: "D8C2BF")
private let GoodGreen = Color(hex: "2E7D32")
private let BadRed = Color(hex: "C62828")
private let GoldChipBg = Color(hex: "FFF8E1")

struct HomeScreen: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var showAIChat = false
    @State private var showNotifications = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                // ═══ HERO HEADER ═══
                HeroHeader(info: viewModel.state.dayInfo, showNotifications: $showNotifications)

                // ═══ CONTENT ═══
                ScrollView(.vertical, showsIndicators: false) {
                    VStack(spacing: 0) {
                        if let info = viewModel.state.dayInfo {
                            // Can Chi Chips Row
                            CanChiChipsRow(info: info)

                            // Tiết Khí Bar
                            TietKhiBar(info: info)

                            // Info Grid 2x2
                            InfoGrid(info: info)

                            // Upcoming Events
                            if !viewModel.state.upcomingEvents.isEmpty {
                                EventsSection(events: viewModel.state.upcomingEvents)
                            }

                            // Quote
                            if viewModel.state.showQuote {
                                QuoteCard(day: viewModel.state.selectedDay, month: viewModel.state.selectedMonth)
                            }
                        }

                        Spacer().frame(height: 16)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                }
                .background(SurfaceBg)
            }
            .background(DeepRed.ignoresSafeArea(edges: .top)) // Ensures status bar area has red background

            // ═══ AI FAB ═══
            AiFab { showAIChat = true }
                .padding(.trailing, 16)
                .padding(.bottom, 16)
        }
        .fullScreenCover(isPresented: $showAIChat) {
            AIChatScreen()
        }
        .sheet(isPresented: $showNotifications) {
            NavigationStack {
                NotificationsScreen()
            }
        }
    }
}

// ══════════════════════════════════════════
// HERO HEADER
// ══════════════════════════════════════════

private struct HeroHeader: View {
    let info: DayInfo?
    @Binding var showNotifications: Bool

    var body: some View {
        ZStack(alignment: .top) {
            // Decorative gold radial glow
            Circle()
                .fill(
                    RadialGradient(
                        colors: [Color.yellow.opacity(0.08), Color.clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 130
                    )
                )
                .frame(width: 260, height: 260)
                .offset(x: 100, y: -80)

            if let info = info {
                VStack(spacing: 0) {
                    // ── Top row: Weather + Notification ──
                    HStack {
                        Spacer()
                        WeatherChipView()
                        Spacer()

                        Button(action: { showNotifications = true }) {
                            ZStack(alignment: .topTrailing) {
                                Circle()
                                    .fill(Color.white.opacity(0.12))
                                    .frame(width: 40, height: 40)
                                    .overlay(
                                        Image(systemName: "bell")
                                            .font(.system(size: 20))
                                            .foregroundColor(.white)
                                    )
                                Circle()
                                    .fill(Color(hex: "FF6B6B"))
                                    .frame(width: 7, height: 7)
                                    .offset(x: -6, y: 6)
                            }
                        }
                    }
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 12)

                    // ── Year / Month / Weekday row ──
                    HStack(spacing: 0) {
                        Text("\(info.solar.yy)")
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(.white.opacity(0.5))
                            .padding(.trailing, 8)

                        Text("Tháng \(info.solar.mm)")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)

                        Text(" · ")
                            .font(.system(size: 14, weight: .light))
                            .foregroundColor(.white.opacity(0.35))

                        Text(info.dayOfWeek)
                            .font(.system(size: 15))
                            .foregroundColor(.white.opacity(0.55))

                        Spacer()
                    }
                    .padding(.horizontal, 24)

                    // ── Big Solar Date ──
                    Text(String(format: "%02d", info.solar.dd))
                        .font(.system(size: 128, weight: .heavy))
                        .foregroundColor(.white)
                        .tracking(-5)
                        .shadow(color: .black.opacity(0.12), radius: 12, y: 2)
                        .padding(.vertical, 4)

                    // ── Lunar info row ──
                    HStack(spacing: 8) {
                        // Lunar chip
                        HStack(spacing: 6) {
                            Text(info.moonPhase.icon)
                                .font(.system(size: 15))
                            Text("\(info.lunar.day) tháng \(info.lunar.month) Âm · \(info.dayCanChi)")
                                .font(.system(size: 12.5, weight: .medium))
                                .foregroundColor(.white.opacity(0.9))
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(Color.white.opacity(0.08), lineWidth: 1))

                        // Quality chip
                        let isGood = !info.activities.isXauDay
                        HStack(spacing: 4) {
                            Text("✦")
                                .font(.system(size: 10))
                            Text(isGood ? "Hoàng Đạo" : "Hắc Đạo")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(isGood ? Color(hex: "A5D6A7") : Color(hex: "EF9A9A"))
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(isGood ? Color(hex: "4CAF50").opacity(0.25) : Color(hex: "C62828").opacity(0.25))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.08), lineWidth: 1))
                    }

                    Spacer().frame(height: 4)
                }
                .padding(.top, 8)
                .padding(.bottom, 20)
            }
        }
        .frame(maxWidth: .infinity)
        .background(
            // Gradient background — extends behind status bar
            LinearGradient(
                colors: [
                    Color(hex: "C62828"),
                    PrimaryRed,
                    Color(hex: "9B1B1B"),
                    DeepRed
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }
}

// ══════════════════════════════════════════
// WEATHER CHIP (Placeholder)
// ══════════════════════════════════════════

private struct WeatherChipView: View {
    var body: some View {
        HStack(spacing: 7) {
            Text("⛅")
                .font(.system(size: 18))
            Text("31°C")
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(.white)
            Text("Hà Nội")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.white.opacity(0.75))
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 8)
        .background(Color.white.opacity(0.14))
        .clipShape(Capsule())
        .overlay(Capsule().stroke(Color.white.opacity(0.1), lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// CAN CHI CHIPS ROW
// ══════════════════════════════════════════

private struct CanChiChipsRow: View {
    let info: DayInfo

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                CanChiChip(icon: "calendar", text: "Năm \(info.yearCanChi)")
                CanChiChip(icon: "moon.fill", text: "Tháng \(info.monthCanChi)")
                CanChiChip(icon: "sun.min.fill", text: "Ngày \(info.dayCanChi)")

                let goodHours = info.gioHoangDao.prefix(2).map { $0.name }.joined(separator: ", ")
                CanChiChip(icon: "clock.fill", text: "Giờ tốt: \(goodHours)")
            }
        }
        .padding(.bottom, 16)
    }
}

private struct CanChiChip: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
            Text(text)
                .font(.system(size: 11.5, weight: .semibold))
                .foregroundColor(TextSub)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(SurfaceContainer)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Outline, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// ══════════════════════════════════════════
// TIẾT KHÍ BAR
// ══════════════════════════════════════════

private struct TietKhiBar: View {
    let info: DayInfo

    var body: some View {
        let tietKhiName = info.tietKhi.currentName ?? info.tietKhi.nextName ?? "—"
        let dateRange: String = {
            if let nextName = info.tietKhi.nextName, let dd = info.tietKhi.nextDd, let mm = info.tietKhi.nextMm {
                if info.tietKhi.daysUntilNext == 0 {
                    return "Hôm nay · Đang trong tiết khí"
                }
                return "\(String(format: "%02d/%02d", dd, mm)) · Còn \(info.tietKhi.daysUntilNext) ngày"
            }
            return ""
        }()

        HStack(spacing: 12) {
            Text("☀️")
                .font(.system(size: 24))

            VStack(alignment: .leading, spacing: 1) {
                Text(tietKhiName)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(TextMain)
                Text(dateRange)
                    .font(.system(size: 11.5))
                    .foregroundColor(TextSub)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(Outline)
        }
        .padding(16)
        .background(
            LinearGradient(
                colors: [Color(hex: "FFF8E1"), Color(hex: "FFFDE7")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "FFE082"), lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.bottom, 16)
    }
}

// ══════════════════════════════════════════
// INFO GRID (2x2)
// ══════════════════════════════════════════

private struct InfoGrid: View {
    let info: DayInfo

    var body: some View {
        LazyVGrid(columns: [
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10)
        ], spacing: 10) {
            // Nên làm
            InfoCard(
                icon: "checkmark.circle.fill",
                title: "NÊN LÀM",
                headerColor: GoodGreen,
                content: info.activities.nenLam.joined(separator: ", ")
            )

            // Không nên
            InfoCard(
                icon: "xmark.circle.fill",
                title: "KHÔNG NÊN",
                headerColor: BadRed,
                content: info.activities.khongNen.joined(separator: ", ")
            )

            // Giờ hoàng đạo
            InfoCard(
                icon: "star.fill",
                title: "GIỜ HOÀNG ĐẠO",
                headerColor: GoldAccent,
                content: info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: ", ")
            )

            // Hướng tốt
            InfoCard(
                icon: "safari.fill",
                title: "HƯỚNG TỐT",
                headerColor: Color(hex: "1565C0"),
                content: "Thần Tài: \(info.huong.thanTai)\nHỷ Thần: \(info.huong.hyThan)"
            )
        }
        .padding(.bottom, 16)
    }
}

private struct InfoCard: View {
    let icon: String
    let title: String
    let headerColor: Color
    let content: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 5) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundColor(headerColor)
                Text(title)
                    .font(.system(size: 10.5, weight: .bold))
                    .foregroundColor(headerColor)
                    .tracking(0.6)
            }

            Text(content)
                .font(.system(size: 12))
                .foregroundColor(TextSub)
                .lineSpacing(6)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(SurfaceContainer)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Outline, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ══════════════════════════════════════════
// EVENTS SECTION
// ══════════════════════════════════════════

private struct EventsSection: View {
    let events: [UpcomingEvent]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Section title
            HStack(spacing: 6) {
                Image(systemName: "calendar.badge.clock")
                    .font(.system(size: 16))
                    .foregroundColor(PrimaryRed)
                Text("Sự kiện sắp tới")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(TextMain)
            }
            .padding(.top, 4)

            ForEach(Array(events.prefix(5).enumerated()), id: \.offset) { _, event in
                EventItem(event: event)
            }
        }
        .padding(.bottom, 8)
    }
}

private struct EventItem: View {
    let event: UpcomingEvent

    private var dotColor: Color {
        switch event.colorType {
        case .red: return Color(hex: "E65100")
        case .gold: return GoldAccent
        case .teal: return PrimaryRed
        }
    }

    private var tagBg: Color {
        switch event.colorType {
        case .red: return Color(hex: "FFF3E0")
        case .gold: return Color(hex: "FFF8E1")
        case .teal: return Color(hex: "FFDAD6")
        }
    }

    private var tagText: Color {
        switch event.colorType {
        case .red: return Color(hex: "E65100")
        case .gold: return Color(hex: "F57F17")
        case .teal: return Color(hex: "410002")
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            // Dot
            Circle()
                .fill(dotColor)
                .frame(width: 8, height: 8)
                .shadow(color: dotColor.opacity(0.3), radius: 3)

            // Info
            VStack(alignment: .leading, spacing: 2) {
                Text(event.title)
                    .font(.system(size: 13.5, weight: .semibold))
                    .foregroundColor(TextMain)
                    .lineLimit(1)

                Text(event.timeLabel)
                    .font(.system(size: 11.5))
                    .foregroundColor(TextDim)
            }

            Spacer()

            // Tag
            Text(event.tag)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(tagText)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(tagBg)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 13)
        .background(SurfaceContainer)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Outline, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ══════════════════════════════════════════
// QUOTE CARD
// ══════════════════════════════════════════

private struct QuoteCard: View {
    let day: Int
    let month: Int

    var body: some View {
        let dayOfYear = (month - 1) * 30 + day
        let (text, author) = VietnameseQuotes.ofDay(dayOfYear)

        VStack(spacing: 8) {
            Text("\u{201C}\(text)\u{201D}")
                .font(.system(size: 14.5, weight: .regular, design: .serif))
                .italic()
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(6)

            Text("— \(author)")
                .font(.system(size: 11.5, weight: .medium))
                .foregroundColor(TextDim)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 20)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                colors: [Color(hex: "FFFDF5"), GoldChipBg, Color(hex: "FFFDE7")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "FFE082"), lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.top, 12)
    }
}

// ══════════════════════════════════════════
// AI FAB
// ══════════════════════════════════════════

private struct AiFab: View {
    var onTap: () -> Void = {}
    var body: some View {
        Button(action: onTap) {
            ZStack(alignment: .topTrailing) {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "D32F2F"), PrimaryRed],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 50, height: 50)
                    .shadow(color: PrimaryRed.opacity(0.3), radius: 8, y: 4)
                    .overlay(
                        Image(systemName: "sparkles")
                            .font(.system(size: 22))
                            .foregroundColor(.white)
                    )

                Text("AI")
                    .font(.system(size: 7.5, weight: .heavy))
                    .foregroundColor(.white)
                    .padding(.horizontal, 5)
                    .padding(.vertical, 2)
                    .background(GoldAccent)
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(SurfaceBg, lineWidth: 1.5))
                    .offset(x: 5, y: -3)
            }
        }
    }
}

#Preview {
    HomeScreen()
}
