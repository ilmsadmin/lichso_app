import SwiftUI

struct HomeScreen: View {
    @ObservedObject var viewModel: HomeViewModel
    @Environment(\.lichSoColors) private var c
    var onSettingsClick: () -> Void = {}
    var onMenuClick: () -> Void = {}
    var onProfileClick: () -> Void = {}
    var onHistoryClick: () -> Void = {}
    var onNotificationClick: () -> Void = {}

    private let weekDays = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
    private let weekDaysSunday = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]

    var body: some View {
        let state = viewModel.uiState

        VStack(spacing: 0) {
            // ═══ RED GRADIENT HEADER (outside ScrollView so it can extend into status bar) ═══
            headerSection(info: state.dayInfo)

            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // ═══ TEAR LINE ═══
                    tearLine

                    // ═══ MINI WEEK STRIP ═══
                    miniCalendarStrip(state: state)

                    // ═══ DAY DETAIL CARD ═══
                    if let info = state.dayInfo {
                        dayDetailCard(info: info, state: state)
                    }

                    // ═══ QUOTE OF THE DAY ═══
                    if AppSettings.shared.quoteEnabled {
                        quoteCard(quote: state.quote)
                    }
                }
            }
        }
        .background(c.bg)
        .gesture(
            DragGesture(minimumDistance: 50)
                .onEnded { value in
                    if value.translation.height < -50 {
                        viewModel.nextDay()
                    } else if value.translation.height > 50 {
                        viewModel.prevDay()
                    }
                }
        )
        .onAppear {
            viewModel.loadWeather()
        }
    }

    // MARK: - Header
    @ViewBuilder
    private func headerSection(info: DayInfo?) -> some View {
        VStack(spacing: 8) {
            // Top bar
            HStack {
                Button(action: onMenuClick) {
                    Image(systemName: "line.3.horizontal")
                        .font(.system(size: 22))
                        .foregroundColor(.white)
                }
                Spacer()
                Text("LỊCH SỐ")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .tracking(2)
                Spacer()
                HStack(spacing: 16) {
                    Button(action: onNotificationClick) {
                        Image(systemName: "bell")
                            .font(.system(size: 20))
                            .foregroundColor(.white)
                    }
                    Button(action: onProfileClick) {
                        Image(systemName: "person.circle")
                            .font(.system(size: 22))
                            .foregroundColor(.white)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            if let info = info {
                // Date display
                VStack(spacing: 4) {
                    Text(info.dayOfWeek)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.white.opacity(0.8))

                    Text("\(info.solarDate.day)")
                        .font(.system(size: 72, weight: .bold, design: .rounded))
                        .foregroundColor(.white)

                    Text("Tháng \(info.solarDate.month), \(String(info.solarDate.year))")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white.opacity(0.9))

                    // Lunar date
                    HStack(spacing: 4) {
                        Image(systemName: "moon.fill")
                            .font(.system(size: 12))
                            .foregroundColor(c.gold)
                        Text("\(info.lunarDate.day)/\(info.lunarDate.month)\(info.lunarDate.isLeapMonth ? " nhuận" : "") Âm lịch")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(c.gold)
                    }

                    // Can Chi
                    Text("Ngày \(info.dayCanChi) • \(info.monthCanChi) • \(info.yearCanChi)")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.75))
                        .padding(.top, 2)

                    // Holiday
                    if let holiday = info.solarHoliday ?? info.lunarHoliday, !holiday.isEmpty {
                        Text(holiday)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(c.gold)
                            .multilineTextAlignment(.center)
                            .padding(.top, 4)
                    }

                    // Weather
                    if let weather = viewModel.uiState.weatherInfo {
                        HStack(spacing: 8) {
                            Text(weather.icon)
                                .font(.system(size: 16))
                            Text("\(Int(weather.temperature))°C")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white)
                            Text(weather.description)
                                .font(.system(size: 12))
                                .foregroundColor(.white.opacity(0.8))
                        }
                        .padding(.top, 4)
                    }
                }
                .padding(.bottom, 20)
            }
        }
        .frame(maxWidth: .infinity)
        .background {
            c.headerGradient
                .ignoresSafeArea(.container, edges: .top)
        }
    }

    // MARK: - Tear Line
    private var tearLine: some View {
        HStack(spacing: 6) {
            ForEach(0..<40, id: \.self) { _ in
                Circle()
                    .fill(c.textQuaternary)
                    .frame(width: 3, height: 3)
            }
        }
        .padding(.vertical, 6)
    }

    // MARK: - Mini Calendar Strip
    @ViewBuilder
    private func miniCalendarStrip(state: HomeUiState) -> some View {
        let headers = AppSettings.shared.weekStartSunday ? weekDaysSunday : weekDays
        let cal = Calendar.current
        let selectedDay = cal.component(.day, from: state.selectedDate)
        let selectedMonth = cal.component(.month, from: state.selectedDate)

        VStack(spacing: 4) {
            // Month/Year header
            HStack {
                Button(action: { viewModel.prevMonth() }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(c.textSecondary)
                }
                Spacer()
                Text("Tháng \(state.currentMonth) / \(String(state.currentYear))")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(c.textPrimary)
                Spacer()
                Button(action: { viewModel.nextMonth() }) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(c.textSecondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)

            // Week day headers
            HStack(spacing: 0) {
                ForEach(headers, id: \.self) { header in
                    Text(header)
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(header == "CN" ? c.red : c.textTertiary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 8)

            // Calendar grid
            let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(state.calendarDays, id: \.id) { day in
                    CalendarDayCell(
                        day: day,
                        isSelected: day.solarDay == selectedDay && day.solarMonth == selectedMonth,
                        isToday: day.isToday,
                        c: c,
                        onTap: {
                            viewModel.selectDay(day: day.solarDay, month: day.solarMonth, year: day.solarYear)
                        }
                    )
                }
            }
            .padding(.horizontal, 8)
        }
        .padding(.bottom, 8)
    }

    // MARK: - Calendar Day Cell
    struct CalendarDayCell: View {
        let day: CalendarDay
        let isSelected: Bool
        let isToday: Bool
        let c: LichSoColors
        let onTap: () -> Void

        var body: some View {
            Button(action: onTap) {
                VStack(spacing: 1) {
                    Text("\(day.solarDay)")
                        .font(.system(size: 14, weight: isSelected || isToday ? .bold : .regular))
                        .foregroundColor(dayColor)
                    if day.lunarDay > 0 {
                        Text("\(day.lunarDay)")
                            .font(.system(size: 9))
                            .foregroundColor(c.textTertiary)
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 38)
                .background(
                    Group {
                        if isSelected {
                            RoundedRectangle(cornerRadius: 10)
                                .fill(c.primary)
                        } else if isToday {
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(c.primary, lineWidth: 1.5)
                        }
                    }
                )
            }
            .opacity(day.isCurrentMonth ? 1.0 : 0.35)
        }

        private var dayColor: Color {
            if isSelected { return .white }
            if day.isSunday { return c.red }
            if day.isHoliday { return c.red }
            return c.textPrimary
        }
    }

    // MARK: - Day Detail Card
    @ViewBuilder
    private func dayDetailCard(info: DayInfo, state: HomeUiState) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            // Trực ngày & Sao
            HStack(spacing: 12) {
                infoChip(icon: "star.fill", title: "Trực", value: info.trucNgay, color: c.gold)
                if let sao = info.saoChieu {
                    infoChip(icon: "sparkle", title: "Sao", value: sao, color: c.teal)
                }
            }

            // Tiết khí
            if let tietKhi = info.tietKhi, !tietKhi.isEmpty {
                HStack(spacing: 6) {
                    Image(systemName: "leaf.fill")
                        .font(.system(size: 12))
                        .foregroundColor(c.teal)
                    Text("Tiết khí: \(tietKhi)")
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                }
            }

            // Nên làm
            if !info.nenLam.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 6) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(c.goodGreen)
                            .font(.system(size: 14))
                        Text("Nên làm")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(c.goodGreen)
                    }
                    Text(info.nenLam.joined(separator: ", "))
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                        .lineLimit(3)
                }
            }

            // Không nên
            if !info.khongNen.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 6) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(c.badRed)
                            .font(.system(size: 14))
                        Text("Không nên")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(c.badRed)
                    }
                    Text(info.khongNen.joined(separator: ", "))
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                        .lineLimit(3)
                }
            }

            // Giờ hoàng đạo
            if !info.gioHoangDao.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 6) {
                        Image(systemName: "clock.fill")
                            .foregroundColor(c.gold)
                            .font(.system(size: 14))
                        Text("Giờ hoàng đạo")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(c.gold)
                    }
                    Text(info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: " • "))
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                }
            }

            // Hướng tốt
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 6) {
                    Image(systemName: "safari.fill")
                        .foregroundColor(c.teal)
                        .font(.system(size: 14))
                    Text("Hướng tốt")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(c.teal)
                }
                Text("• Thần Tài: \(info.huong.thanTai)")
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)
                Text("• Hỷ Thần: \(info.huong.hyThan)")
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)
                Text("• Hướng Hung: \(info.huong.hungThan)")
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)
            }

            // Upcoming events
            if !state.upcomingEvents.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar.badge.exclamationmark")
                            .foregroundColor(c.noteOrange)
                            .font(.system(size: 14))
                        Text("Sự kiện sắp tới")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(c.noteOrange)
                    }
                    ForEach(Array(state.upcomingEvents.prefix(5))) { event in
                        HStack(spacing: 8) {
                            Text(event.tag)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(c.noteOrange)
                                .cornerRadius(8)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(event.title)
                                    .font(.system(size: 13))
                                    .foregroundColor(c.textPrimary)
                                Text(event.timeLabel)
                                    .font(.system(size: 11))
                                    .foregroundColor(c.textTertiary)
                            }
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(c.surface)
        )
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    // MARK: - Info Chip
    @ViewBuilder
    private func infoChip(icon: String, title: String, value: String, color: Color) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(color)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(c.textTertiary)
                Text(value)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(c.textPrimary)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(c.bg2)
        )
    }

    // MARK: - Quote Card
    @ViewBuilder
    private func quoteCard(quote: (quote: String, author: String)) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                Image(systemName: "quote.opening")
                    .font(.system(size: 14))
                    .foregroundColor(c.gold)
                Text("Câu nói hôm nay")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(c.gold)
            }
            Text(quote.quote)
                .font(.system(size: 14))
                .foregroundColor(c.textPrimary)
                .italic()
            Text("— \(quote.author)")
                .font(.system(size: 12))
                .foregroundColor(c.textTertiary)
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(c.surface)
        )
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    // MARK: - Helpers
    private func dayOfWeekString(_ dow: Int) -> String {
        switch dow {
        case 1: return "Chủ Nhật"
        case 2: return "Thứ Hai"
        case 3: return "Thứ Ba"
        case 4: return "Thứ Tư"
        case 5: return "Thứ Năm"
        case 6: return "Thứ Sáu"
        case 7: return "Thứ Bảy"
        default: return ""
        }
    }

    private func weatherIcon(_ condition: String) -> String {
        let lower = condition.lowercased()
        if lower.contains("clear") || lower.contains("sunny") || lower.contains("nắng") { return "sun.max.fill" }
        if lower.contains("cloud") || lower.contains("mây") { return "cloud.fill" }
        if lower.contains("rain") || lower.contains("mưa") { return "cloud.rain.fill" }
        if lower.contains("thunder") || lower.contains("sấm") { return "cloud.bolt.fill" }
        if lower.contains("snow") || lower.contains("tuyết") { return "snowflake" }
        if lower.contains("fog") || lower.contains("sương") { return "cloud.fog.fill" }
        return "cloud.sun.fill"
    }
}
