import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Home Screen — Vietnamese Calendar Day Page
// Matches Android design: Red header → tear line → week strip → calendar page
// Swipe up/down to flip calendar pages (3D rotation)
// ═══════════════════════════════════════════

// MARK: - Design Tokens (adaptive — respond to light/dark theme)

private var PrimaryRed:    Color { LSTheme.primary }
private var DeepRed:       Color { LSTheme.deepRed }
private var GoldAccent:    Color { LSTheme.gold }
private var SurfaceBg:     Color { LSTheme.bg }
private var SurfaceCard:   Color { LSTheme.surfaceContainer }
private var TextPrimary:   Color { LSTheme.textPrimary }
private var TextSecondary: Color { LSTheme.textSecondary }
private var TextTertiary:  Color { LSTheme.textTertiary }
private var OutlineColor:  Color { LSTheme.outlineVariant }
private var GoodGreen:     Color { LSTheme.goodGreen }
private var BadRed:        Color { LSTheme.badRed }
private var GoldChip:      Color { LSTheme.gold2 }
private var DatePink:      Color { LSTheme.primary.opacity(0.7) }

private let HeaderGradient: [Color] = [
    Color(hex: "5D1212"),
    Color(hex: "7F1D1D"),
    Color(hex: "4A1010")
]

// MARK: - HomeScreen

struct HomeScreen: View {
    var onMenuClick: () -> Void = {}

    @StateObject private var viewModel = HomeViewModel()
    @ObservedObject private var weatherService = WeatherService.shared
    @Query(filter: #Predicate<NotificationEntity> { !$0.isRead })
    private var unreadNotifications: [NotificationEntity]
    @State private var showNotifications = false
    @State private var showGoodDays = false
    @State private var showHistory = false
    @State private var showWeather = false
    @State private var flipProgress: CGFloat = 0
    @State private var isDragging = false
    @State private var isAnimatingFlip = false

    // ── Settings ──
    @AppStorage("setting_show_quote")     private var showQuoteSetting: Bool = true
    @AppStorage("setting_show_festivals") private var showFestivalsSetting: Bool = true
    @AppStorage("setting_show_lunar")     private var showLunarSetting: Bool = true
    @AppStorage("setting_show_hoang_dao") private var showHoangDaoSetting: Bool = true
    @AppStorage("setting_location")       private var locationSetting: String = "Hà Nội"
    @AppStorage("setting_temp_unit")      private var tempUnitSetting: String = "°C"
    @AppStorage("setting_week_start")     private var weekStart: String = "Thứ 2"

    private let flipThreshold: CGFloat = 0.18

    var body: some View {
        ZStack {
            // Full-screen dark background
            SurfaceBg.ignoresSafeArea()

            // Red gradient behind status bar
            VStack {
                LinearGradient(
                    colors: HeaderGradient,
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .frame(height: 120)
                Spacer()
            }
            .ignoresSafeArea(edges: .top)

            // Main content
            if let info = viewModel.state.dayInfo {
                VStack(spacing: 0) {
                    headerSection(info: info)
                    tearLine
                    weekStrip
                    calendarPage(info: info)
                }
            }
        }
        .sheet(isPresented: $showNotifications) {
            NavigationStack {
                NotificationsScreen()
            }
        }
        .fullScreenCover(isPresented: $showGoodDays) {
            NavigationStack {
                GoodDaysScreen()
            }
        }
        .fullScreenCover(isPresented: $showHistory) {
            NavigationStack {
                ThisDayInHistoryScreen(
                    initialDay: viewModel.state.selectedDay,
                    initialMonth: viewModel.state.selectedMonth,
                    initialYear: viewModel.state.selectedYear
                )
            }
        }
        .task {
            await weatherService.fetchWeather(for: locationSetting, unit: tempUnitSetting)
        }
    }

    // MARK: - Header

    private func headerSection(info: DayInfo) -> some View {
        VStack(spacing: 0) {
            HStack {
                circleButton(icon: "line.3.horizontal") { onMenuClick() }
                Spacer()
                weatherChip
                Spacer()
                ZStack(alignment: .topTrailing) {
                    circleButton(icon: "bell") { showNotifications = true }
                    if !unreadNotifications.isEmpty {
                        Circle()
                            .fill(Color(hex: "FF6B6B"))
                            .frame(width: 7, height: 7)
                            .offset(x: -6, y: 6)
                    }
                }
            }
            .padding(.horizontal, 24)

            Spacer().frame(height: 4)

            HStack(spacing: 0) {
                Text(String(info.solar.yy))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white.opacity(0.7))
                    .padding(.trailing, 8)
                Text("Tháng \(info.solar.mm)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white.opacity(0.9))
                Text(" · ")
                    .font(.system(size: 13, weight: .light))
                    .foregroundColor(.white.opacity(0.35))
                Text(info.dayOfWeek)
                    .font(.system(size: 13))
                    .foregroundColor(.white.opacity(0.6))
                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .padding(.bottom, 12)
        .background(
            LinearGradient(
                colors: HeaderGradient,
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }

    private func circleButton(icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Circle()
                .fill(Color.white.opacity(0.12))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundColor(.white)
                )
        }
    }

    private var weatherChip: some View {
        Button {
            showWeather = true
        } label: {
            HStack(spacing: 6) {
                Text(WeatherService.weatherEmoji(for: weatherService.weather?.weatherCode ?? 2))
                    .font(.system(size: 18))
                Text(weatherService.weather.map { "\(Int($0.temperature.rounded()))\(tempUnitSetting)" } ?? "31\(tempUnitSetting)")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                Text(locationSetting)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .background(Color.white.opacity(0.15))
            .clipShape(Capsule())
        }
        .sheet(isPresented: $showWeather) {
            WeatherDetailSheet(city: locationSetting, unit: tempUnitSetting)
                .presentationDetents([.large])
                .presentationDragIndicator(.visible)
        }
    }

    // MARK: - Tear Line

    private var tearLine: some View {
        Canvas { ctx, size in
            var x: CGFloat = 0
            while x < size.width {
                let rect = CGRect(x: x, y: size.height / 2 - 0.5, width: 8, height: 1)
                ctx.fill(Path(rect), with: .color(OutlineColor))
                x += 14
            }
        }
        .frame(height: 3)
        .background(SurfaceBg)
    }

    // MARK: - Week Strip

    private var weekStrip: some View {
        let state = viewModel.state
        let selectedDate = makeDate(state.selectedDay, state.selectedMonth, state.selectedYear)
        let weekDates = weekDatesFor(selectedDate)
        let labels = weekStart == "Chủ nhật"
            ? ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]
            : ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
        let weekendIndices = weekStart == "Chủ nhật" ? [0, 6] : [5, 6]

        return HStack(spacing: 0) {
            ForEach(0..<7, id: \.self) { i in
                let date = weekDates[i]
                let cal = Calendar.current
                let dayNum = cal.component(.day, from: date)
                let month = cal.component(.month, from: date)
                let year = cal.component(.year, from: date)
                let isToday = cal.isDateInToday(date)
                let isWeekend = weekendIndices.contains(i)

                let lunarText = state.calendarDays.first {
                    $0.solarDay == dayNum && $0.solarMonth == month && $0.solarYear == year
                }?.lunarDisplayText ?? ""

                Button {
                    if let match = state.calendarDays.first(where: {
                        $0.solarDay == dayNum && $0.solarMonth == month && $0.solarYear == year
                    }) {
                        viewModel.selectDay(match.solarDay, month: match.solarMonth, year: match.solarYear)
                    }
                } label: {
                    VStack(spacing: 2) {
                        Text(labels[i])
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(isToday ? .white.opacity(0.8) : TextTertiary)
                        Text("\(dayNum)")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(isToday ? .white : isWeekend ? DatePink : TextPrimary)
                        Text(lunarText)
                            .font(.system(size: 8))
                            .foregroundColor(isToday ? .white.opacity(0.7) : TextSecondary)
                    }
                    .frame(width: 44)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(isToday ? Color(hex: "7F1D1D") : .clear)
                    )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(SurfaceBg)
    }

    // MARK: - Calendar Page (Flip Area)

    private func calendarPage(info: DayInfo) -> some View {
        GeometryReader { _ in
            let progress = flipProgress
            let absP = abs(progress)

            ZStack {
                CalendarPageBody(
                    info: info,
                    showQuote: showQuoteSetting,
                    showFestivals: showFestivalsSetting,
                    showLunar: showLunarSetting,
                    showHoangDao: showHoangDaoSetting,
                    selectedDay: viewModel.state.selectedDay,
                    selectedMonth: viewModel.state.selectedMonth,
                    onGoodDaysTap: { showGoodDays = true },
                    onHistoryTap: { showHistory = true }
                )
                .rotation3DEffect(
                    .degrees(Double(progress) * -80),
                    axis: (x: 1, y: 0, z: 0),
                    anchor: progress >= 0 ? .top : .bottom,
                    perspective: 0.3
                )
                .opacity(Double(1 - absP * 0.9))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .contentShape(Rectangle()) // ensure entire area responds to drag
            .clipped()
            .gesture(flipGesture)
        }
        .background(SurfaceBg)
    }

    private var flipGesture: some Gesture {
        DragGesture(minimumDistance: 10)
            .onChanged { value in
                guard !isAnimatingFlip else { return }
                isDragging = true
                flipProgress = min(max(-value.translation.height / 450, -1), 1)
            }
            .onEnded { _ in
                guard isDragging else { return }
                isDragging = false

                if abs(flipProgress) >= flipThreshold {
                    let goNext = flipProgress > 0
                    isAnimatingFlip = true
                    withAnimation(.easeInOut(duration: 0.18)) {
                        flipProgress = goNext ? 1 : -1
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
                        if goNext { viewModel.nextDay() } else { viewModel.prevDay() }
                        flipProgress = goNext ? -0.25 : 0.25
                        withAnimation(.easeOut(duration: 0.2)) {
                            flipProgress = 0
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                            isAnimatingFlip = false
                        }
                    }
                } else {
                    isAnimatingFlip = true
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                        flipProgress = 0
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        isAnimatingFlip = false
                    }
                }
            }
    }

    // MARK: - Helpers

    private func makeDate(_ d: Int, _ m: Int, _ y: Int) -> Date {
        Calendar.current.date(from: DateComponents(year: y, month: m, day: d)) ?? Date()
    }

    private func weekDatesFor(_ date: Date) -> [Date] {
        let cal = Calendar.current
        let wd = cal.component(.weekday, from: date)
        // weekday: 1=Sun, 2=Mon, ..., 7=Sat
        let offset: Int
        if weekStart == "Chủ nhật" {
            offset = -(wd - 1) // shift to Sunday
        } else {
            offset = -((wd + 5) % 7) // shift to Monday
        }
        let startDay = cal.date(byAdding: .day, value: offset, to: date)!
        return (0..<7).map { cal.date(byAdding: .day, value: $0, to: startDay)! }
    }
}

// MARK: - Calendar Page Body

private struct CalendarPageBody: View {
    let info: DayInfo
    let showQuote: Bool
    let showFestivals: Bool
    let showLunar: Bool
    let showHoangDao: Bool
    let selectedDay: Int
    let selectedMonth: Int
    var onGoodDaysTap: () -> Void = {}
    var onHistoryTap: () -> Void = {}

    var body: some View {
        ZStack(alignment: .top) {
            // Decorative gold border (fills entire page area)
            CalendarBorder()

            // Content — aligned to top, pushed close to border edge
            VStack(spacing: 0) {
                // Big date number — negative vertical padding to reduce built-in font space
                Text(String(format: "%02d", info.solar.dd))
                    .font(.system(size: 140, weight: .bold, design: .serif))
                    .foregroundColor(DatePink)
                    .tracking(-2)
                    .shadow(color: PrimaryRed.opacity(0.3), radius: 12, y: 2)
                    .padding(.top, -16)

                // Gold decorative line
                Capsule()
                    .fill(
                        LinearGradient(
                            colors: [.clear, GoldChip, .clear],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: 80, height: 3)
                    .padding(.top, -8)

                Spacer().frame(height: 10)

                // Weekday label
                Text(info.dayOfWeek.uppercased())
                    .font(.system(size: 22, weight: .medium))
                    .foregroundColor(TextPrimary)
                    .tracking(1)

                Spacer().frame(height: 12)

                // Lunar date card — controlled by setting
                if showLunar {
                    lunarCard
                    Spacer().frame(height: 8)
                }

                // Day quality — controlled by setting
                if showHoangDao {
                    dayQuality
                    Spacer().frame(height: 8)
                }

                // Swipe hint
                swipeHint

                Spacer().frame(height: 8)

                // Quote — controlled by setting
                if showQuote {
                    quoteSection
                    Spacer().frame(height: 8)
                }

                // Event chips — controlled by setting
                if showFestivals {
                    let hasHistory = HistoricalEventProvider.hasEvents(day: info.solar.dd, month: info.solar.mm)
                    let holiday = info.solarHoliday ?? info.lunarHoliday
                    if holiday != nil || hasHistory {
                        eventChips
                    }
                }

                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: Lunar Card

    private var lunarCard: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "FFF9C4"), Color(hex: "FFD54F")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 32, height: 32)
                Text("🌙").font(.system(size: 18))
            }

            VStack(alignment: .leading, spacing: 1) {
                Text("Mùng \(info.lunar.day) tháng \(info.lunar.month)")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(TextPrimary)
                Text("Năm \(info.yearCanChi)")
                    .font(.system(size: 12))
                    .foregroundColor(TextTertiary)
            }

            Text("Ngày \(info.dayCanChi)")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(DatePink)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
        .background(SurfaceCard)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(GoldChip.opacity(0.25), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: Day Quality

    private var dayQuality: some View {
        let isGood = !info.activities.isXauDay
        return Button(action: onGoodDaysTap) {
            HStack(spacing: 6) {
                Circle()
                    .fill(isGood ? GoodGreen : BadRed)
                    .frame(width: 8, height: 8)
                Text(isGood ? "Ngày Hoàng Đạo ✦" : "Ngày Hắc Đạo")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(isGood ? GoodGreen : BadRed)
                Image(systemName: "chevron.right")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(isGood ? GoodGreen.opacity(0.6) : BadRed.opacity(0.6))
            }
        }
        .buttonStyle(.plain)
    }

    // MARK: Swipe Hint

    private var swipeHint: some View {
        HStack(spacing: 4) {
            Image(systemName: "hand.draw")
                .font(.system(size: 14))
            Text("Vuốt để lật lịch")
                .font(.system(size: 10))
        }
        .foregroundColor(TextTertiary)
        .opacity(0.35)
    }

    // MARK: Quote

    private var quoteSection: some View {
        let dayOfYear = (selectedMonth - 1) * 30 + selectedDay
        let (text, author) = VietnameseQuotes.ofDay(dayOfYear)

        return VStack(spacing: 4) {
            Text("\u{201C} \(text) \u{201D}")
                .font(.system(size: 14, weight: .regular, design: .serif))
                .italic()
                .foregroundColor(TextSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(7)
            Text("— \(author)")
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(TextTertiary)
        }
        .padding(.horizontal, 4)
    }

    // MARK: Event Chips

    // Adaptive chip colors — dark & light
    private var holidayChipBg: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "3D2A10") : UIColor(hex: "FFF3E0") })
    }
    private var holidayChipFg: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "E8A06A") : UIColor(hex: "BF360C") })
    }
    private var holidayChipBorder: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "5C3D1A") : UIColor(hex: "FFCC80") })
    }
    private var historyChipBg: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "1A2E1A") : UIColor(hex: "E8F5E9") })
    }
    private var historyChipFg: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "81C784") : UIColor(hex: "2E7D32") })
    }
    private var historyChipBorder: Color {
        Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "2E4A2E") : UIColor(hex: "A5D6A7") })
    }

    private var eventChips: some View {
        let holiday = info.solarHoliday ?? info.lunarHoliday
        let hasHistory = HistoricalEventProvider.hasEvents(day: info.solar.dd, month: info.solar.mm)

        return HStack(spacing: 8) {
            if let holiday = holiday {
                chipView(
                    icon: "party.popper",
                    text: holiday,
                    bg: holidayChipBg,
                    fg: holidayChipFg,
                    border: holidayChipBorder
                )
            }
            if hasHistory {
                Button(action: onHistoryTap) {
                    chipView(
                        icon: "book.closed",
                        text: "Ngày này năm xưa",
                        bg: historyChipBg,
                        fg: historyChipFg,
                        border: historyChipBorder
                    )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func chipView(icon: String, text: String, bg: Color, fg: Color, border: Color) -> some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(fg)
            Text(text)
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(fg)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(bg)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(border, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// MARK: - Calendar Border (Decorative gold frame)

private struct CalendarBorder: View {
    var body: some View {
        GeometryReader { geo in
            let m: CGFloat = 16

            RoundedRectangle(cornerRadius: 12)
                .stroke(
                    LinearGradient(
                        colors: [
                            GoldChip.opacity(0.3),
                            GoldChip.opacity(0.1),
                            GoldChip.opacity(0.3)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1.5
                )
                .padding(m)

            // Four corner ornaments
            let positions: [(CGFloat, CGFloat)] = [
                (m + 8, m + 8),
                (geo.size.width - m - 8, m + 8),
                (m + 8, geo.size.height - m - 8),
                (geo.size.width - m - 8, geo.size.height - m - 8)
            ]

            ForEach(0..<4, id: \.self) { i in
                Text("✦")
                    .font(.system(size: 8))
                    .foregroundColor(GoldChip.opacity(0.4))
                    .position(x: positions[i].0, y: positions[i].1)
            }
        }
    }
}

// MARK: - Preview

#Preview {
    HomeScreen()
}
