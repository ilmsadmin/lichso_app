import Foundation
import SwiftUI
import Combine

// MARK: - Home UI State
struct HomeUiState {
    var currentYear: Int = Calendar.current.component(.year, from: Date())
    var currentMonth: Int = Calendar.current.component(.month, from: Date())
    var selectedDate: Date = Date()
    var dayInfo: DayInfo? = nil
    var calendarDays: [CalendarDay] = []
    var upcomingEvents: [UpcomingEvent] = []
    var weatherInfo: WeatherInfo? = nil
    var isLoadingWeather: Bool = false
    var quote: (quote: String, author: String) = VietnameseQuotes.ofDay(Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1)
}

// MARK: - HomeViewModel
@MainActor
class HomeViewModel: ObservableObject {
    @Published var uiState = HomeUiState()

    private let dayInfoProvider = DayInfoProvider.shared
    private let settings = AppSettings.shared

    init() {
        loadCurrentDate()
        loadWeather()
    }

    func loadCurrentDate() {
        let today = Date()
        let cal = Calendar.current
        let year = cal.component(.year, from: today)
        let month = cal.component(.month, from: today)
        let day = cal.component(.day, from: today)
        updateState(year: year, month: month, selectedDate: today)
        // Quote of the day
        let dayOfYear = cal.ordinality(of: .day, in: .year, for: today) ?? 1
        uiState.quote = VietnameseQuotes.ofDay(dayOfYear)
    }

    func loadWeather() {
        uiState.isLoadingWeather = true
        Task {
            let location = CityCoordinates.toLocationInfo(settings.locationName)
            do {
                let weather = try await WeatherAPI.shared.getCurrentWeather(location: location)
                uiState.weatherInfo = weather
            } catch {
                print("Weather load error: \(error)")
            }
            uiState.isLoadingWeather = false
        }
    }

    func selectDay(day: Int, month: Int, year: Int) {
        var components = DateComponents()
        components.year = year
        components.month = month
        components.day = day
        if let date = Calendar.current.date(from: components) {
            uiState.selectedDate = date
            uiState.dayInfo = dayInfoProvider.getDayInfo(dd: day, mm: month, yy: year)
            uiState.upcomingEvents = dayInfoProvider.getUpcomingEvents(dd: day, mm: month, yy: year)
        }
    }

    func prevDay() {
        if let newDate = Calendar.current.date(byAdding: .day, value: -1, to: uiState.selectedDate) {
            let cal = Calendar.current
            updateState(year: cal.component(.year, from: newDate),
                       month: cal.component(.month, from: newDate),
                       selectedDate: newDate)
        }
    }

    func nextDay() {
        if let newDate = Calendar.current.date(byAdding: .day, value: 1, to: uiState.selectedDate) {
            let cal = Calendar.current
            updateState(year: cal.component(.year, from: newDate),
                       month: cal.component(.month, from: newDate),
                       selectedDate: newDate)
        }
    }

    func prevMonth() {
        var newMonth = uiState.currentMonth - 1
        var newYear = uiState.currentYear
        if newMonth < 1 { newMonth = 12; newYear -= 1 }
        updateState(year: newYear, month: newMonth, selectedDate: uiState.selectedDate)
    }

    func nextMonth() {
        var newMonth = uiState.currentMonth + 1
        var newYear = uiState.currentYear
        if newMonth > 12 { newMonth = 1; newYear += 1 }
        updateState(year: newYear, month: newMonth, selectedDate: uiState.selectedDate)
    }

    func goToToday() {
        let today = Date()
        let cal = Calendar.current
        updateState(year: cal.component(.year, from: today),
                   month: cal.component(.month, from: today),
                   selectedDate: today)
    }

    func goToDate(year: Int, month: Int, day: Int) {
        var components = DateComponents()
        components.year = year
        components.month = month
        components.day = day
        if let date = Calendar.current.date(from: components) {
            updateState(year: year, month: month, selectedDate: date)
        }
    }

    private func updateState(year: Int, month: Int, selectedDate: Date) {
        let cal = Calendar.current
        let dd = cal.component(.day, from: selectedDate)
        let mm = cal.component(.month, from: selectedDate)
        let yy = cal.component(.year, from: selectedDate)
        let weekStartSunday = settings.weekStartSunday

        uiState.currentYear = year
        uiState.currentMonth = month
        uiState.selectedDate = selectedDate
        uiState.dayInfo = dayInfoProvider.getDayInfo(dd: dd, mm: mm, yy: yy)
        uiState.calendarDays = dayInfoProvider.getCalendarDays(year: year, month: month, weekStartSunday: weekStartSunday)
        uiState.upcomingEvents = dayInfoProvider.getUpcomingEvents(dd: dd, mm: mm, yy: yy)
    }
}
