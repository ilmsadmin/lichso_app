import SwiftUI
import SwiftData

// ═══════════════════════════════════════════════════════════
// Calendar Screen — Full month grid + day detail
// Matches screen-calendar.html mock design
// Uses real lunar calendar data via DayInfoProvider
// ═══════════════════════════════════════════════════════════

private var PrimaryRed           : Color { LSTheme.primary }
private var DeepRed              : Color { LSTheme.deepRed }
private var GoldAccent           : Color { LSTheme.gold }
private var SurfaceBg            : Color { LSTheme.bg }
private var SurfaceContainer     : Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh : Color { LSTheme.surfaceContainerHigh }
private var TextMain             : Color { LSTheme.textPrimary }
private var TextSub              : Color { LSTheme.textSecondary }
private var TextDim              : Color { LSTheme.textTertiary }
private var OutlineVar           : Color { LSTheme.outlineVariant }
private var GoodGreen            : Color { LSTheme.goodGreen }

struct CalendarScreen: View {
    @StateObject private var vm = CalendarViewModel()
    @State private var showDayDetail = false
    @State private var showSearch = false
    @Environment(\.modelContext) private var modelContext

    // ── Display settings ──
    @AppStorage("setting_show_lunar")     private var showLunar: Bool = true
    @AppStorage("setting_show_hoang_dao") private var showHoangDao: Bool = true
    @AppStorage("setting_show_festivals") private var showFestivals: Bool = true

    // Query bookmarks for the current month
    @Query private var allBookmarks: [BookmarkEntity]

    var body: some View {
        ZStack {
            SurfaceBg.ignoresSafeArea()

            VStack(spacing: 0) {
                // ═══ TOP BAR ═══
                CalendarTopBar(
                    onSearchTap: { showSearch = true }
                )

                // ═══ MONTH SELECTOR ═══
                MonthSelector(
                    year: vm.state.currentYear,
                    month: vm.state.currentMonth,
                    lunarLabel: vm.state.lunarMonthLabel,
                    onPrev: { vm.previousMonth() },
                    onNext: { vm.nextMonth() }
                )

                // ═══ CALENDAR GRID ═══
                CalendarGrid(
                    days: vm.state.calendarDays,
                    selectedDay: vm.state.selectedDay,
                    selectedMonth: vm.state.selectedMonth,
                    selectedYear: vm.state.selectedYear,
                    bookmarkedDates: bookmarkedDatesSet,
                    showLunar: showLunar,
                    showHoangDao: showHoangDao,
                    onDayTap: { day in
                        vm.selectDay(day.solarDay, month: day.solarMonth, year: day.solarYear)
                    },
                    onDayDoubleTap: { day in
                        vm.selectDay(day.solarDay, month: day.solarMonth, year: day.solarYear)
                        showDayDetail = true
                    }
                )

                // ═══ SELECTED DAY DETAIL (bottom bar) ═══
                if let info = vm.state.dayInfo {
                    SelectedDayBar(
                        dayInfo: info,
                        onTap: { showDayDetail = true }
                    )
                }

                Spacer()
            }
        }
        .fullScreenCover(isPresented: $showDayDetail) {
            if let info = vm.state.dayInfo {
                DayDetailScreen(dayInfo: info, onDismiss: { showDayDetail = false })
            }
        }
        .sheet(isPresented: $showSearch) {
            NavigationStack {
                SearchScreen()
            }
        }
    }

    private var bookmarkedDatesSet: Set<String> {
        Set(allBookmarks.map { "\($0.solarYear)-\($0.solarMonth)-\($0.solarDay)" })
    }
}

// ══════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════

private struct CalendarTopBar: View {
    let onSearchTap: () -> Void

    var body: some View {
        HStack {
            Text("Lịch")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            Spacer()
            Button(action: onSearchTap) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 36, height: 36)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
        .padding(.bottom, 12)
        .background(
            LinearGradient(
                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                         Color(red: 0.545, green: 0, blue: 0)],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// MONTH SELECTOR
// ══════════════════════════════════════════

private struct MonthSelector: View {
    let year: Int
    let month: Int
    let lunarLabel: String
    let onPrev: () -> Void
    let onNext: () -> Void

    private let monthNames = [
        "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    ]

    var body: some View {
        HStack(spacing: 16) {
            Button(action: onPrev) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(TextMain)
                    .frame(width: 32, height: 32)
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
            }

            VStack(spacing: 2) {
                Text("\(monthNames[month]), \(String(year))")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(PrimaryRed)
                Text(lunarLabel)
                    .font(.system(size: 11))
                    .foregroundColor(TextSub)
            }
            .frame(minWidth: 160)

            Button(action: onNext) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(TextMain)
                    .frame(width: 32, height: 32)
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
            }
        }
        .padding(.vertical, 8)
    }
}

// ══════════════════════════════════════════
// CALENDAR GRID
// ══════════════════════════════════════════

private struct CalendarGrid: View {
    let days: [CalendarDay]
    let selectedDay: Int
    let selectedMonth: Int
    let selectedYear: Int
    let bookmarkedDates: Set<String>
    var showLunar: Bool = true
    var showHoangDao: Bool = true
    let onDayTap: (CalendarDay) -> Void
    let onDayDoubleTap: (CalendarDay) -> Void

    @AppStorage("setting_week_start") private var weekStart: String = "Thứ 2"

    private var weekdayHeaders: [String] {
        if weekStart == "Chủ nhật" {
            return ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]
        } else {
            return ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
        }
    }

    private var weekendIndices: [Int] {
        if weekStart == "Chủ nhật" {
            return [0, 6]  // CN, T7
        } else {
            return [5, 6]  // T7, CN
        }
    }

    var body: some View {
        VStack(spacing: 2) {
            // Weekday header
            HStack(spacing: 0) {
                ForEach(0..<7, id: \.self) { i in
                    Text(weekdayHeaders[i])
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(weekendIndices.contains(i) ? PrimaryRed : TextDim)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.bottom, 8)

            // Grid of days
            let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 7)
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(days) { day in
                    CalendarDayCell(
                        day: day,
                        isSelected: day.solarDay == selectedDay
                            && day.solarMonth == selectedMonth
                            && day.solarYear == selectedYear,
                        isBookmarked: bookmarkedDates.contains("\(day.solarYear)-\(day.solarMonth)-\(day.solarDay)"),
                        showLunar: showLunar,
                        showHoangDao: showHoangDao,
                        onTap: { onDayTap(day) },
                        onDoubleTap: { onDayDoubleTap(day) }
                    )
                }
            }
        }
        .padding(.horizontal, 14)
    }
}

// ══════════════════════════════════════════
// CALENDAR DAY CELL
// ══════════════════════════════════════════

private struct CalendarDayCell: View {
    let day: CalendarDay
    let isSelected: Bool
    let isBookmarked: Bool
    var showLunar: Bool = true
    var showHoangDao: Bool = true
    let onTap: () -> Void
    let onDoubleTap: () -> Void

    var body: some View {
        ZStack {
            // Background
            RoundedRectangle(cornerRadius: 14)
                .fill(cellBackground)

            VStack(spacing: 1) {
                // Solar day number
                Text("\(day.solarDay)")
                    .font(.system(size: 16, weight: day.isToday || day.isHoliday ? .bold : .semibold))
                    .foregroundColor(solarColor)
                    .lineLimit(1)

                // Lunar day — controlled by setting
                if showLunar {
                    Text(day.lunarDisplayText)
                        .font(.system(size: 9))
                        .foregroundColor(lunarColor)
                        .lineLimit(1)
                }
            }

            // Event dot
            if day.hasEvent && day.isCurrentMonth {
                VStack {
                    Spacer()
                    Circle()
                        .fill(GoldAccent)
                        .frame(width: 4, height: 4)
                        .padding(.bottom, 5)
                }
            }

            // Good day indicator — controlled by setting
            if showHoangDao && day.dayRatingLabel == "Rất tốt" && day.isCurrentMonth {
                VStack {
                    HStack {
                        Spacer()
                        Circle()
                            .fill(GoodGreen)
                            .frame(width: 5, height: 5)
                            .padding(.top, 3)
                            .padding(.trailing, 3)
                    }
                    Spacer()
                }
            }

            // Bookmark indicator
            if isBookmarked {
                VStack {
                    HStack {
                        Image(systemName: "bookmark.fill")
                            .font(.system(size: 6))
                            .foregroundColor(PrimaryRed)
                            .padding(.top, 2)
                            .padding(.leading, 2)
                        Spacer()
                    }
                    Spacer()
                }
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .onTapGesture(count: 2) { onDoubleTap() }
        .onTapGesture { onTap() }
    }

    private var cellBackground: Color {
        if day.isToday {
            return PrimaryRed
        } else if isSelected {
            return Color(hex: "5D1212")
        } else {
            return .clear
        }
    }

    private var solarColor: Color {
        if day.isToday {
            return .white
        }
        if !day.isCurrentMonth {
            return OutlineVar
        }
        if day.isHoliday || day.isSunday {
            return PrimaryRed
        }
        if day.isSaturday {
            return PrimaryRed.opacity(0.7)
        }
        return TextMain
    }

    private var lunarColor: Color {
        if day.isToday {
            return .white.opacity(0.7)
        }
        if !day.isCurrentMonth {
            return OutlineVar
        }
        return TextDim
    }
}

// ══════════════════════════════════════════
// SELECTED DAY BAR (bottom info)
// ══════════════════════════════════════════

private struct SelectedDayBar: View {
    let dayInfo: DayInfo
    let onTap: () -> Void

    var body: some View {
        VStack(spacing: 6) {
            Divider().foregroundColor(OutlineVar)

            Button(action: onTap) {
                VStack(spacing: 8) {
                    // Date header row
                    HStack {
                        Text("\(dayInfo.dayOfWeek), \(String(format: "%02d/%02d/%d", dayInfo.solar.dd, dayInfo.solar.mm, dayInfo.solar.yy))")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(TextMain)

                        Spacer()

                        Text("\(dayInfo.lunar.day)/\(dayInfo.lunar.month) Âm")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(PrimaryRed)
                    }

                    // Quick info chips
                    HStack(spacing: 8) {
                        // Day rating
                        let isGood = dayInfo.dayRating.label == "Rất tốt" || dayInfo.dayRating.label == "Tốt"
                        HStack(spacing: 4) {
                            Image(systemName: isGood ? "checkmark.seal.fill" : "exclamationmark.triangle.fill")
                                .font(.system(size: 14))
                            Text(dayInfo.dayRating.label)
                                .font(.system(size: 12, weight: .semibold))
                        }
                        .foregroundColor(isGood ? Color(hex: "2E7D32") : Color(hex: "C62828"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(isGood ? Color(hex: "E8F5E9") : Color(hex: "FFEBEE"))
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                        // Holiday if present
                        if let holiday = dayInfo.solarHoliday ?? dayInfo.lunarHoliday {
                            HStack(spacing: 4) {
                                Image(systemName: "party.popper.fill")
                                    .font(.system(size: 14))
                                Text(holiday)
                                    .font(.system(size: 12, weight: .semibold))
                                    .lineLimit(1)
                            }
                            .foregroundColor(Color(hex: "E65100"))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(Color(hex: "FFF3E0"))
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 14))
                            .foregroundColor(OutlineVar)
                    }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 10)
            }
            .buttonStyle(.plain)
        }
    }
}

// ══════════════════════════════════════════
#Preview {
    CalendarScreen()
}
