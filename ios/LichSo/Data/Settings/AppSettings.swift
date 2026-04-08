import Foundation
import SwiftUI

// MARK: - App Settings Repository (UserDefaults wrapper)

class AppSettings: ObservableObject {
    static let shared = AppSettings()

    @AppStorage("lunar_badge_enabled") var lunarBadgeEnabled: Bool = true
    @AppStorage("notify_enabled") var notifyEnabled: Bool = true
    @AppStorage("gio_dai_cat_enabled") var gioDaiCatEnabled: Bool = false
    @AppStorage("theme_mode") var themeMode: String = "system" // "light", "dark", "system"
    @AppStorage("festival_enabled") var festivalEnabled: Bool = true
    @AppStorage("quote_enabled") var quoteEnabled: Bool = true
    @AppStorage("festival_reminder") var festivalReminderEnabled: Bool = true
    @AppStorage("temp_unit") var tempUnit: String = "°C"
    @AppStorage("location_name") var locationName: String = "Hà Nội"
    @AppStorage("reminder_hour") var reminderHour: Int = 7
    @AppStorage("reminder_minute") var reminderMinute: Int = 0
    @AppStorage("week_start") var weekStart: String = "Thứ Hai"
    @AppStorage("app_open_count") var appOpenCount: Int = 0

    // Profile settings
    @AppStorage("user_name") var userName: String = ""
    @AppStorage("user_gender") var userGender: String = ""
    @AppStorage("user_birth_date") var userBirthDate: String = ""
    @AppStorage("user_birth_hour") var userBirthHour: String = ""

    var weekStartSunday: Bool {
        weekStart == "Chủ Nhật"
    }

    var colorScheme: ColorScheme? {
        switch themeMode {
        case "light": return .light
        case "dark": return .dark
        default: return nil
        }
    }
}
