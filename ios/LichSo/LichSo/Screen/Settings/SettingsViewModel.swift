import SwiftUI
import UserNotifications
import Foundation
import Combine

// ═══════════════════════════════════════════
// Settings ViewModel
// Persists all settings via @AppStorage (UserDefaults).
// iCloud sync mirrors key settings to NSUbiquitousKeyValueStore
// so data stays in sync across devices.
// ═══════════════════════════════════════════

@MainActor
class SettingsViewModel: ObservableObject {

    // ── Chung ──
    @AppStorage("setting_week_start") var weekStart: String = "Thứ 2"
    @AppStorage("setting_theme")      var theme: String     = "Theo hệ thống"

    // ── Hiển thị ──
    @AppStorage("setting_show_lunar")     var showLunar:     Bool = true
    @AppStorage("setting_show_hoang_dao") var showHoangDao:  Bool = true
    @AppStorage("setting_show_festivals") var showFestivals: Bool = true
    @AppStorage("setting_show_quote")     var showQuote:     Bool = true

    // ── Thông báo ──
    @AppStorage("setting_daily_reminder") var dailyReminder: Bool = true {
        didSet { Task { await onDailyReminderChanged() } }
    }
    @AppStorage("setting_reminder_hour")   var reminderHour:   Int = 7
    @AppStorage("setting_reminder_minute") var reminderMinute: Int = 0
    @AppStorage("setting_festival_reminder") var festivalReminder: Bool = true {
        didSet { onFestivalReminderChanged() }
    }

    // ── Đồng bộ & Lưu trữ ──
    @AppStorage("setting_icloud_sync") var iCloudSync: Bool = true {
        didSet { onICloudSyncChanged() }
    }
    @AppStorage("setting_location")  var location: String = "Hà Nội"
    @AppStorage("setting_temp_unit") var tempUnit: String = "°C"

    // ── Notification permission state ──
    @Published var notificationPermission: UNAuthorizationStatus = .notDetermined

    // ── iCloud sync status ──
    @Published var iCloudAvailable: Bool = false

    // ── Options ──
    let weekStartOptions = ["Thứ 2", "Chủ nhật"]
    let themeOptions     = ["Sáng", "Tối", "Theo hệ thống"]
    let tempUnitOptions  = ["°C", "°F"]

    let locationOptions: [String] = [
        // ── Thành phố trực thuộc Trung ương ──
        "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
        // ── Miền Bắc ──
        "Hà Giang", "Cao Bằng", "Bắc Kạn", "Tuyên Quang", "Lào Cai",
        "Điện Biên", "Lai Châu", "Sơn La", "Yên Bái", "Hoà Bình",
        "Thái Nguyên", "Lạng Sơn", "Quảng Ninh", "Hạ Long", "Bắc Giang",
        "Phú Thọ", "Vĩnh Phúc", "Bắc Ninh", "Hải Dương", "Hưng Yên",
        "Thái Bình", "Hà Nam", "Nam Định", "Ninh Bình",
        // ── Miền Trung ──
        "Thanh Hoá", "Nghệ An", "Vinh", "Hà Tĩnh", "Quảng Bình", "Đồng Hới",
        "Quảng Trị", "Huế", "Quảng Nam", "Hội An", "Quảng Ngãi",
        "Bình Định", "Quy Nhơn", "Phú Yên", "Tuy Hoà",
        "Khánh Hoà", "Nha Trang", "Ninh Thuận", "Phan Rang",
        "Bình Thuận", "Phan Thiết",
        // ── Tây Nguyên ──
        "Kon Tum", "Gia Lai", "Pleiku", "Đắk Lắk", "Buôn Ma Thuột",
        "Đắk Nông", "Gia Nghĩa", "Lâm Đồng", "Đà Lạt", "Bảo Lộc",
        // ── Đông Nam Bộ ──
        "Bình Phước", "Đồng Xoài", "Tây Ninh",
        "Bình Dương", "Thủ Dầu Một", "Đồng Nai", "Biên Hoà",
        "Bà Rịa - Vũng Tàu", "Vũng Tàu",
        // ── Đồng bằng Sông Cửu Long ──
        "Long An", "Tân An", "Tiền Giang", "Mỹ Tho",
        "Bến Tre", "Trà Vinh", "Vĩnh Long",
        "Đồng Tháp", "Cao Lãnh", "An Giang", "Long Xuyên", "Châu Đốc",
        "Kiên Giang", "Rạch Giá", "Phú Quốc",
        "Hậu Giang", "Vị Thanh", "Sóc Trăng", "Bạc Liêu", "Cà Mau"
    ]

    var reminderTimeString: String {
        String(format: "%02d:%02d", reminderHour, reminderMinute)
    }

    var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }

    // MARK: - Init

    init() {
        Task {
            await checkNotificationPermission()
            checkICloudAvailability()
            if iCloudSync { pullFromICloud() }
        }
        // Listen for iCloud remote changes
        NotificationCenter.default.addObserver(
            forName: NSUbiquitousKeyValueStore.didChangeExternallyNotification,
            object: NSUbiquitousKeyValueStore.default,
            queue: .main
        ) { [weak self] _ in
            Task { @MainActor in
                self?.pullFromICloud()
            }
        }
    }

    // MARK: - Notifications

    func checkNotificationPermission() async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        notificationPermission = settings.authorizationStatus
    }

    func requestNotificationPermission() async -> Bool {
        let granted = await NotificationScheduler.shared.requestPermission()
        await checkNotificationPermission()
        return granted
    }

    func rescheduleNotifications() {
        guard dailyReminder, notificationPermission == .authorized else {
            UNUserNotificationCenter.current()
                .removePendingNotificationRequests(withIdentifiers: ["daily_morning"])
            return
        }
        NotificationScheduler.shared.scheduleDailyNotification(
            hour: reminderHour, minute: reminderMinute
        )
    }

    private func onDailyReminderChanged() async {
        if dailyReminder && notificationPermission == .notDetermined {
            _ = await requestNotificationPermission()
        }
        rescheduleNotifications()
    }

    private func onFestivalReminderChanged() {
        guard notificationPermission == .authorized else { return }
        if festivalReminder {
            NotificationScheduler.shared.scheduleUpcomingFestivalNotifications()
            NotificationScheduler.shared.scheduleRamMung1Reminders()
        } else {
            // Remove festival-related pending notifications
            UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
                let ids = requests
                    .filter { $0.identifier.hasPrefix("festival_") || $0.identifier.hasPrefix("lunar_") }
                    .map(\.identifier)
                UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ids)
            }
        }
    }

    // MARK: - iCloud Sync

    private let iCloudStore = NSUbiquitousKeyValueStore.default

    /// Keys to sync to/from iCloud (lightweight display/preference settings only — not user content)
    private let iCloudKeys: [String] = [
        "setting_week_start",
        "setting_theme",
        "setting_show_lunar",
        "setting_show_hoang_dao",
        "setting_show_festivals",
        "setting_show_quote",
        "setting_location",
        "setting_temp_unit",
        "setting_daily_reminder",
        "setting_reminder_hour",
        "setting_reminder_minute",
        "setting_festival_reminder"
    ]

    func checkICloudAvailability() {
        iCloudAvailable = FileManager.default.ubiquityIdentityToken != nil
    }

    /// Push all settings → iCloud KV store
    func pushToICloud() {
        guard iCloudSync, iCloudAvailable else { return }
        let ud = UserDefaults.standard
        for key in iCloudKeys {
            iCloudStore.set(ud.object(forKey: key), forKey: key)
        }
        iCloudStore.synchronize()
    }

    /// Pull settings from iCloud → UserDefaults (only if iCloud value exists)
    func pullFromICloud() {
        guard iCloudSync, iCloudAvailable else { return }
        let ud = UserDefaults.standard
        for key in iCloudKeys {
            if let val = iCloudStore.object(forKey: key) {
                ud.set(val, forKey: key)
            }
        }
        // Refresh published view by re-reading AppStorage keys
        objectWillChange.send()
    }

    private func onICloudSyncChanged() {
        if iCloudSync {
            checkICloudAvailability()
            pushToICloud()
        }
    }
}
