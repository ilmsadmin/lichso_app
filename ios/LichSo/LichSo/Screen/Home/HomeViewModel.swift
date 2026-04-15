import SwiftUI
import Combine

// ═══════════════════════════════════════════
// HomeViewModel — ported from Android MVVM
// ═══════════════════════════════════════════

struct HomeUiState {
    var currentYear: Int
    var currentMonth: Int
    var selectedDay: Int
    var selectedMonth: Int
    var selectedYear: Int
    var dayInfo: DayInfo?
    var calendarDays: [CalendarDay]
    var upcomingEvents: [UpcomingEvent]
    var notificationUnreadCount: Int

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
        upcomingEvents = []
        notificationUnreadCount = 0
    }
}

@MainActor
class HomeViewModel: ObservableObject {
    @Published var state = HomeUiState()
    private let provider = DayInfoProvider.shared
    @AppStorage("setting_week_start") private var weekStart: String = "Thứ 2" {
        didSet { reloadCalendarDays() }
    }

    init() {
        loadCurrentDate()
    }

    func loadCurrentDate() {
        let now = Date()
        let cal = Calendar.current
        let y = cal.component(.year, from: now)
        let m = cal.component(.month, from: now)
        let d = cal.component(.day, from: now)
        updateState(year: y, month: m, day: d)
    }

    func selectDay(_ day: Int, month: Int, year: Int) {
        updateState(year: year, month: month, day: day)
    }

    func prevDay() {
        guard let current = dateFromState() else { return }
        let cal = Calendar.current
        guard let prev = cal.date(byAdding: .day, value: -1, to: current) else { return }
        let c = cal.dateComponents([.year, .month, .day], from: prev)
        updateState(year: c.year!, month: c.month!, day: c.day!)
    }

    func nextDay() {
        guard let current = dateFromState() else { return }
        let cal = Calendar.current
        guard let next = cal.date(byAdding: .day, value: 1, to: current) else { return }
        let c = cal.dateComponents([.year, .month, .day], from: next)
        updateState(year: c.year!, month: c.month!, day: c.day!)
    }

    func goToToday() {
        loadCurrentDate()
    }

    private func dateFromState() -> Date? {
        var comps = DateComponents()
        comps.year = state.selectedYear
        comps.month = state.selectedMonth
        comps.day = state.selectedDay
        return Calendar.current.date(from: comps)
    }

    private func updateState(year: Int, month: Int, day: Int) {
        state.currentYear = year
        state.currentMonth = month
        state.selectedDay = day
        state.selectedMonth = month
        state.selectedYear = year
        state.dayInfo = provider.getDayInfo(dd: day, mm: month, yy: year)
        state.calendarDays = provider.getCalendarDays(
            year: year, month: month,
            weekStartSunday: weekStart == "Chủ nhật"
        )
        state.upcomingEvents = provider.getUpcomingEvents(dd: day, mm: month, yy: year)
    }

    private func reloadCalendarDays() {
        state.calendarDays = provider.getCalendarDays(
            year: state.currentYear, month: state.currentMonth,
            weekStartSunday: weekStart == "Chủ nhật"
        )
    }
}
