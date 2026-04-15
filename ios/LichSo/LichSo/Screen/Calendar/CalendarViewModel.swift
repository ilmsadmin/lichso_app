import SwiftUI
import Combine

// ═══════════════════════════════════════════
// CalendarViewModel — Month grid + day detail
// Uses DayInfoProvider, real lunar data
// ═══════════════════════════════════════════

struct CalendarUiState {
    var currentYear: Int
    var currentMonth: Int
    var selectedDay: Int
    var selectedMonth: Int
    var selectedYear: Int
    var dayInfo: DayInfo?
    var calendarDays: [CalendarDay]
    var lunarMonthLabel: String

    init() {
        let now = Date()
        let cal = Calendar.current
        currentYear = cal.component(.year, from: now)
        currentMonth = cal.component(.month, from: now)
        selectedDay = cal.component(.day, from: now)
        selectedMonth = cal.component(.month, from: now)
        selectedYear = cal.component(.year, from: now)
        dayInfo = nil
        calendarDays = []
        lunarMonthLabel = ""
    }
}

@MainActor
class CalendarViewModel: ObservableObject {
    @Published var state = CalendarUiState()

    private let provider = DayInfoProvider.shared
    @AppStorage("setting_week_start") private var weekStart: String = "Thứ 2" {
        didSet { reloadGrid() }
    }

    init() {
        loadCurrentMonth()
    }

    // ── Load current month + select today ──
    func loadCurrentMonth() {
        let now = Date()
        let cal = Calendar.current
        let y = cal.component(.year, from: now)
        let m = cal.component(.month, from: now)
        let d = cal.component(.day, from: now)
        state.currentYear = y
        state.currentMonth = m
        state.selectedDay = d
        state.selectedMonth = m
        state.selectedYear = y
        reloadGrid()
        state.dayInfo = provider.getDayInfo(dd: d, mm: m, yy: y)
    }

    // ── Navigate months ──
    func previousMonth() {
        var m = state.currentMonth - 1
        var y = state.currentYear
        if m < 1 { m = 12; y -= 1 }
        state.currentMonth = m
        state.currentYear = y
        reloadGrid()
        // Select first day of month
        selectDay(1, month: m, year: y)
    }

    func nextMonth() {
        var m = state.currentMonth + 1
        var y = state.currentYear
        if m > 12 { m = 1; y += 1 }
        state.currentMonth = m
        state.currentYear = y
        reloadGrid()
        selectDay(1, month: m, year: y)
    }

    // ── Select a day ──
    func selectDay(_ day: Int, month: Int, year: Int) {
        state.selectedDay = day
        state.selectedMonth = month
        state.selectedYear = year
        state.dayInfo = provider.getDayInfo(dd: day, mm: month, yy: year)
    }

    // ── Go to today ──
    func goToToday() {
        loadCurrentMonth()
    }

    // ── Go to specific month ──
    func goToMonth(year: Int, month: Int) {
        state.currentYear = year
        state.currentMonth = month
        reloadGrid()
        // Select 1st day, or today if it's the current month
        let now = Date()
        let cal = Calendar.current
        let todayY = cal.component(.year, from: now)
        let todayM = cal.component(.month, from: now)
        let todayD = cal.component(.day, from: now)
        if year == todayY && month == todayM {
            selectDay(todayD, month: month, year: year)
        } else {
            selectDay(1, month: month, year: year)
        }
    }

    // ── Go to specific date ──
    func goToDate(year: Int, month: Int, day: Int) {
        state.currentYear = year
        state.currentMonth = month
        reloadGrid()
        selectDay(day, month: month, year: year)
    }

    // ── Reload calendar grid ──
    private func reloadGrid() {
        let weekStartSunday = weekStart == "Chủ nhật"
        state.calendarDays = provider.getCalendarDays(
            year: state.currentYear,
            month: state.currentMonth,
            weekStartSunday: weekStartSunday
        )
        // Compute lunar month label
        let lunar = LunarCalendarUtil.convertSolar2Lunar(
            dd: 15, mm: state.currentMonth, yy: state.currentYear
        )
        state.lunarMonthLabel = "Tháng \(lunar.lunarMonth) Âm lịch"
    }
}
