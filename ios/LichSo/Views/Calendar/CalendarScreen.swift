import SwiftUI

struct CalendarScreen: View {
    @ObservedObject var viewModel: HomeViewModel
    @Environment(\.lichSoColors) private var c
    var onGoodDaysClick: () -> Void = {}
    var onSearchClick: () -> Void = {}
    var onMenuClick: () -> Void = {}
    var onAskAiClick: (Int, Int, Int) -> Void = { _, _, _ in }

    private let weekDays = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
    private let weekDaysSunday = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]

    var body: some View {
        let state = viewModel.uiState
        let headers = AppSettings.shared.weekStartSunday ? weekDaysSunday : weekDays
        let cal = Calendar.current
        let selectedDay = cal.component(.day, from: state.selectedDate)
        let selectedMonth = cal.component(.month, from: state.selectedDate)
        let selectedYear = cal.component(.year, from: state.selectedDate)

        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onMenuClick) {
                    Image(systemName: "line.3.horizontal")
                        .font(.system(size: 20))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("LỊCH THÁNG")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                HStack(spacing: 12) {
                    Button(action: onSearchClick) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 18))
                            .foregroundColor(c.textSecondary)
                    }
                    Button(action: onGoodDaysClick) {
                        Image(systemName: "checkmark.circle")
                            .font(.system(size: 18))
                            .foregroundColor(c.textSecondary)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Month/Year selector
            HStack {
                Button(action: { viewModel.prevMonth() }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(c.textSecondary)
                }
                Spacer()
                Text("Tháng \(state.currentMonth) / \(String(state.currentYear))")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(c.textPrimary)
                Spacer()
                Button(action: { viewModel.nextMonth() }) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(c.textSecondary)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 8)

            // Week headers
            HStack(spacing: 0) {
                ForEach(headers, id: \.self) { header in
                    Text(header)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(header == "CN" ? c.red : c.textTertiary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 8)
            .padding(.bottom, 4)

            // Calendar grid
            let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(state.calendarDays, id: \.id) { day in
                    HomeScreen.CalendarDayCell(
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

            Divider().padding(.vertical, 8)

            // Day detail overlay
            ScrollView {
                if let info = state.dayInfo {
                    DayDetailOverlay(info: info, c: c, onAskAi: {
                        onAskAiClick(selectedDay, selectedMonth, selectedYear)
                    })
                    .padding(.horizontal, 16)
                }
            }
        }
        .background(c.bg)
    }
}

// MARK: - Day Detail Overlay
struct DayDetailOverlay: View {
    let info: DayInfo
    let c: LichSoColors
    var onAskAi: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Ngày \(info.solarDate.day)/\(info.solarDate.month)/\(info.solarDate.year)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(c.textPrimary)
                    Text("Âm lịch: \(info.lunarDate.day)/\(info.lunarDate.month)\(info.lunarDate.isLeapMonth ? " nhuận" : "")")
                        .font(.system(size: 13))
                        .foregroundColor(c.textSecondary)
                }
                Spacer()
                Button(action: onAskAi) {
                    HStack(spacing: 4) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 12))
                        Text("Hỏi AI")
                            .font(.system(size: 12, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(c.fabGradient)
                    .cornerRadius(16)
                }
            }

            // Can Chi
            Text("Ngày \(info.dayCanChi) • Tháng \(info.monthCanChi) • Năm \(info.yearCanChi)")
                .font(.system(size: 13))
                .foregroundColor(c.textSecondary)

            // Holiday
            if let holiday = info.solarHoliday ?? info.lunarHoliday, !holiday.isEmpty {
                HStack(spacing: 6) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 12))
                        .foregroundColor(c.noteOrange)
                    Text(holiday)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(c.noteOrange)
                }
            }

            // Trực + Sao
            HStack(spacing: 16) {
                Label {
                    Text(info.trucNgay)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(c.textPrimary)
                } icon: {
                    Image(systemName: "star.fill")
                        .font(.system(size: 11))
                        .foregroundColor(c.gold)
                }
                if let sao = info.saoChieu {
                    Label {
                        Text(sao)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(c.textPrimary)
                    } icon: {
                        Image(systemName: "sparkle")
                            .font(.system(size: 11))
                            .foregroundColor(c.teal)
                    }
                }
            }

            // Nên làm
            if !info.nenLam.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(c.goodGreen)
                            .font(.system(size: 13))
                        Text("Nên làm")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(c.goodGreen)
                    }
                    Text(info.nenLam.joined(separator: ", "))
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                }
            }

            // Không nên
            if !info.khongNen.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(c.badRed)
                            .font(.system(size: 13))
                        Text("Không nên")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(c.badRed)
                    }
                    Text(info.khongNen.joined(separator: ", "))
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                }
            }

            // Giờ hoàng đạo
            if !info.gioHoangDao.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Image(systemName: "clock.fill")
                            .foregroundColor(c.gold)
                            .font(.system(size: 13))
                        Text("Giờ hoàng đạo")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(c.gold)
                    }
                    Text(info.gioHoangDao.map { "\($0.name) (\($0.time))" }.joined(separator: " • "))
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(c.surface)
        )
    }
}
