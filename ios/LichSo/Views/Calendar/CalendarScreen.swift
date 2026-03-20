import SwiftUI

// MARK: - Calendar Screen
struct CalendarScreen: View {
    @ObservedObject var viewModel: CalendarViewModel
    @Environment(\.lichSoColors) var c
    @State private var showDayDetail = false
    @AppStorage("lunarBadgeEnabled") private var lunarBadgeEnabled: Bool = true

    var body: some View {
        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // Header
                    HStack {
                        Text("Lịch Tháng")
                            .font(.system(size: 22, weight: .bold, design: .serif))
                            .foregroundColor(c.gold2)
                        Spacer()
                        Button("Hôm nay") { viewModel.goToToday() }
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(c.gold2)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 5)
                            .background(c.goldDim)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(c.gold.opacity(0.3), lineWidth: 1))
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 14)

                    // Month Navigation
                    MonthNavBar(month: viewModel.currentMonth, year: viewModel.currentYear,
                                onPrev: viewModel.prevMonth,
                                onNext: viewModel.nextMonth)

                    Spacer(minLength: 8)

                    // Calendar Grid
                    CalendarGridView(
                        days: viewModel.calendarDays,
                        selectedDate: viewModel.selectedDate,
                        showLunar: lunarBadgeEnabled,
                        onDayTap: { day in
                            viewModel.selectDay(day)
                            withAnimation(.spring(response: 0.35)) { showDayDetail = true }
                        }
                    )

                    Spacer(minLength: 14)

                    // Selected day info
                    if let info = viewModel.selectedDayInfo {
                        SectionLabel("THÔNG TIN NGÀY \(info.solar.dd)/\(info.solar.mm)")
                        Spacer(minLength: 7)
                        ActivityGrid(info: info)
                        Spacer(minLength: 96)
                    }
                }
            }
            .background(c.bg.ignoresSafeArea())

            // Day Detail Overlay
            if showDayDetail, let info = viewModel.selectedDayInfo {
                DayDetailOverlay(info: info, onDismiss: {
                    withAnimation(.easeOut(duration: 0.2)) { showDayDetail = false }
                })
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .zIndex(10)
            }
        }
    }
}

// MARK: - Month Nav Bar
struct MonthNavBar: View {
    let month: Int
    let year: Int
    let onPrev: () -> Void
    let onNext: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack {
            Button(action: onPrev) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(c.textSecondary)
            }
            .frame(width: 30, height: 30)

            Spacer()
            Text("Tháng \(month) · \(year)")
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(c.textPrimary)
            Spacer()

            Button(action: onNext) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(c.textSecondary)
            }
            .frame(width: 30, height: 30)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 6)
    }
}

// MARK: - Calendar Grid View
struct CalendarGridView: View {
    let days: [CalendarDay]
    let selectedDate: Date
    var showLunar: Bool = true
    let onDayTap: (CalendarDay) -> Void
    @Environment(\.lichSoColors) var c

    let weekDays = [("CN", true), ("T2", false), ("T3", false), ("T4", false), ("T5", false), ("T6", false), ("T7", false)]

    var body: some View {
        VStack(spacing: 0) {
            // Week headers
            HStack(spacing: 0) {
                ForEach(Array(weekDays.enumerated()), id: \.offset) { _, item in
                    Text(item.0)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(item.1 ? c.red2 : c.textTertiary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.top, 8)
            .padding(.bottom, 5)

            // Days grid
            let chunks = days.chunked(into: 7)
            ForEach(Array(chunks.enumerated()), id: \.offset) { _, week in
                HStack(spacing: 0) {
                    ForEach(week) { day in
                        CalendarDayCell(day: day, selectedDate: selectedDate, showLunar: showLunar)
                            .onTapGesture { if day.isCurrentMonth { onDayTap(day) } }
                    }
                }
                .padding(.vertical, 1)
            }
        }
        .padding(.horizontal, 20)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(c.border, lineWidth: 1))
        .padding(.horizontal, 20)
    }
}

// MARK: - Calendar Day Cell
struct CalendarDayCell: View {
    let day: CalendarDay
    let selectedDate: Date
    var showLunar: Bool = true
    @Environment(\.lichSoColors) var c

    private var isSelected: Bool {
        let cal = Calendar.current
        guard let d = Calendar.current.date(from: DateComponents(year: day.solarYear, month: day.solarMonth, day: day.solarDay)) else { return false }
        return cal.isDate(d, inSameDayAs: selectedDate) && day.isCurrentMonth
    }

    var body: some View {
        VStack(spacing: 1) {
            ZStack {
                if isSelected {
                    Circle()
                        .fill(c.gold)
                        .frame(width: 30, height: 30)
                } else if day.isToday {
                    Circle()
                        .stroke(c.gold, lineWidth: 1.5)
                        .frame(width: 30, height: 30)
                }

                Text("\(day.solarDay)")
                    .font(.system(size: 14, weight: isSelected ? .bold : day.isToday ? .semibold : .regular))
                    .foregroundColor(
                        isSelected ? (c.isDark ? Color(hex: 0x1A1500) : .white)
                        : !day.isCurrentMonth ? c.textQuaternary
                        : day.isSunday || day.isHoliday ? c.red2
                        : day.isSaturday ? c.teal2
                        : c.textPrimary
                    )
            }
            .frame(height: 32)

            if showLunar {
                Text(day.lunarDisplayText)
                    .font(.system(size: 9))
                    .foregroundColor(
                        day.lunarDay == 1 || day.lunarDay == 15
                        ? (c.isDark ? c.gold.opacity(0.9) : c.gold2)
                        : c.textQuaternary
                    )
                    .frame(height: 12)
            } else {
                Color.clear.frame(height: 12)
            }

            // Event dot
            Circle()
                .fill(day.hasEvent ? c.teal : Color.clear)
                .frame(width: 4, height: 4)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 3)
    }
}

// MARK: - Day Detail Overlay
struct DayDetailOverlay: View {
    let info: DayInfo
    let onDismiss: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.black.opacity(0.4).ignoresSafeArea()
                .onTapGesture { onDismiss() }

            VStack(spacing: 0) {
                // Handle
                Capsule()
                    .fill(c.textTertiary.opacity(0.4))
                    .frame(width: 36, height: 4)
                    .padding(.top, 12)
                    .padding(.bottom, 8)

                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        // Date title
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("\(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy)")
                                    .font(.system(size: 20, weight: .bold, design: .serif))
                                    .foregroundColor(c.textPrimary)
                                Text("\(info.lunar.day)/\(info.lunar.month) Âm lịch · \(info.dayCanChi)")
                                    .font(.system(size: 13))
                                    .foregroundColor(c.textSecondary)
                            }
                            Spacer()
                            Button(action: onDismiss) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(c.textTertiary)
                            }
                        }
                        .padding(.horizontal, 20)

                        Divider().overlay(c.border)

                        // Info rows
                        Group {
                            InfoRow(icon: "moon.stars", label: "Can Chi Ngày", value: info.dayCanChi, color: c.gold2)
                            InfoRow(icon: "calendar", label: "Tháng Can Chi", value: info.monthCanChi, color: c.textSecondary)
                            InfoRow(icon: "clock", label: "Trực Ngày", value: "\(info.trucNgay.name) — \(info.trucNgay.rating)", color: c.teal2)
                            InfoRow(icon: "sun.max", label: "Tiết Khí", value: info.tietKhi.nextName ?? "—", color: c.textSecondary)
                        }
                        .padding(.horizontal, 20)

                        Divider().overlay(c.border)

                        ActivityGrid(info: info)
                            .padding(.bottom, 20)
                    }
                }
            }
            .background(c.bg2.ignoresSafeArea(edges: .bottom))
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .frame(maxHeight: UIScreen.main.bounds.height * 0.75)
        }
        .ignoresSafeArea()
    }
}

struct InfoRow: View {
    let icon: String
    let label: String
    let value: String
    let color: Color
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(color)
                .frame(width: 20)
            Text(label)
                .font(.system(size: 13))
                .foregroundColor(c.textSecondary)
            Spacer()
            Text(value)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(color)
        }
        .padding(.vertical, 3)
    }
}

// MARK: - Array Chunk Helper
extension Array {
    func chunked(into size: Int) -> [[Element]] {
        var chunks: [[Element]] = []
        var i = 0
        while i < count {
            chunks.append(Array(self[i..<Swift.min(i + size, count)]))
            i += size
        }
        return chunks
    }
}
