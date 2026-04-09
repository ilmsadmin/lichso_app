import SwiftUI

// ═══════════════════════════════════════════
// Settings ViewModel
// Persists all settings via @AppStorage
// ═══════════════════════════════════════════

class SettingsViewModel: ObservableObject {
    // ── Chung ──
    @AppStorage("setting_week_start") var weekStart: String = "Thứ 2"
    @AppStorage("setting_language") var language: String = "Tiếng Việt"
    @AppStorage("setting_theme") var theme: String = "Sáng"

    // ── Hiển thị ──
    @AppStorage("setting_show_lunar") var showLunar: Bool = true
    @AppStorage("setting_show_hoang_dao") var showHoangDao: Bool = true
    @AppStorage("setting_show_festivals") var showFestivals: Bool = true
    @AppStorage("setting_show_quote") var showQuote: Bool = true

    // ── Thông báo ──
    @AppStorage("setting_daily_reminder") var dailyReminder: Bool = true
    @AppStorage("setting_reminder_hour") var reminderHour: Int = 7
    @AppStorage("setting_reminder_minute") var reminderMinute: Int = 0
    @AppStorage("setting_festival_reminder") var festivalReminder: Bool = true

    // ── Vị trí & Thời tiết ──
    @AppStorage("setting_location") var location: String = "Hà Nội"
    @AppStorage("setting_temp_unit") var tempUnit: String = "°C"

    // ── Week start options ──
    let weekStartOptions = ["Thứ 2", "Chủ nhật"]
    let themeOptions = ["Sáng", "Tối", "Theo hệ thống"]
    let tempUnitOptions = ["°C", "°F"]

    var reminderTimeString: String {
        String(format: "%02d:%02d", reminderHour, reminderMinute)
    }
}
