import Foundation
@preconcurrency import UserNotifications
import SwiftData

// ═══════════════════════════════════════════
// NotificationScheduler — Schedule iOS local notifications
// Tương tự WorkManager trên Android
// ═══════════════════════════════════════════

@MainActor
class NotificationScheduler: ObservableObject {
    static let shared = NotificationScheduler()

    private let notificationCenter = UNUserNotificationCenter.current()
    private let service = NotificationService.shared

    // MARK: - Permission

    func requestPermission() async -> Bool {
        do {
            let granted = try await notificationCenter.requestAuthorization(options: [.alert, .sound, .badge])
            return granted
        } catch {
            #if DEBUG
            print("Notification permission error: \(error)")
            #endif
            return false
        }
    }

    // MARK: - Schedule daily notification

    /// Lên lịch thông báo hàng ngày (tương tự DailyNotificationWorker)
    func scheduleDailyNotification(hour: Int = 7, minute: Int = 0) {
        // Xóa thông báo daily cũ
        notificationCenter.removePendingNotificationRequests(withIdentifiers: ["daily_morning"])

        let daily = service.generateDailyNotification()

        let content = UNMutableNotificationContent()
        content.title = daily.title
        content.body = daily.description
        content.sound = .default
        content.categoryIdentifier = "daily"

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: "daily_morning", content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            #if DEBUG
            if let error { print("Schedule daily error: \(error)") }
            #endif
        }
    }

    /// Lên lịch thông báo giờ hoàng đạo (tương tự GioDaiCatWorker)
    func scheduleGioHoangDaoNotification(hour: Int = 6, minute: Int = 30) {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: ["gio_hoang_dao"])

        let gio = service.generateGioHoangDaoNotification()

        let content = UNMutableNotificationContent()
        content.title = gio.title
        // Chỉ lấy 2 dòng đầu cho push notification
        let lines = gio.description.components(separatedBy: "\n")
        content.body = lines.prefix(3).joined(separator: "\n")
        content.sound = .default
        content.categoryIdentifier = "good_day"

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: "gio_hoang_dao", content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            #if DEBUG
            if let error { print("Schedule gio hoang dao error: \(error)") }
            #endif
        }
    }

    /// Lên lịch thông báo nhắc lễ buổi tối (tương tự FestivalReminderWorker)
    func scheduleFestivalReminder() {
        notificationCenter.removePendingNotificationRequests(withIdentifiers: ["festival_evening"])

        // Festival check runs at 20:00 every day
        let content = UNMutableNotificationContent()
        content.title = "Kiểm tra ngày lễ"
        content.body = "Kiểm tra xem ngày mai có sự kiện đặc biệt không"
        content.sound = .default
        content.categoryIdentifier = "holiday"

        var dateComponents = DateComponents()
        dateComponents.hour = 20
        dateComponents.minute = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: "festival_evening", content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            #if DEBUG
            if let error { print("Schedule festival error: \(error)") }
            #endif
        }
    }

    // MARK: - Schedule upcoming festival-specific notifications

    /// Lên lịch thông báo cụ thể cho các ngày lễ sắp tới (trong 30 ngày)
    func scheduleUpcomingFestivalNotifications() {
        let cal = Calendar.current
        let today = Date()

        // Xóa tất cả thông báo festival cũ
        let center = notificationCenter
        center.getPendingNotificationRequests { requests in
            let festivalIds = requests.filter { $0.identifier.hasPrefix("festival_") }.map(\.identifier)
            center.removePendingNotificationRequests(withIdentifiers: festivalIds)
        }

        // Scan 30 ngày tới
        for i in 1...30 {
            guard let checkDate = cal.date(byAdding: .day, value: i, to: today) else { continue }
            let dd = cal.component(.day, from: checkDate)
            let mm = cal.component(.month, from: checkDate)
            let yy = cal.component(.year, from: checkDate)

            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)
            var festivals: [String] = []

            if let sHol = HolidayUtil.getSolarHoliday(dd: dd, mm: mm) {
                festivals.append(sHol)
            }
            if let lHol = HolidayUtil.getLunarHoliday(lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth) {
                festivals.append(lHol)
            }
            if lunar.lunarDay == 1 || lunar.lunarDay == 15 {
                let name = lunar.lunarDay == 1 ? "Mùng 1" : "Rằm"
                festivals.append("\(name) tháng \(lunar.lunarMonth) Âm lịch")
            }

            guard !festivals.isEmpty else { continue }

            // Tạo thông báo cho tối hôm trước (20h)
            guard let reminderDate = cal.date(byAdding: .day, value: -1, to: checkDate) else { continue }

            let content = UNMutableNotificationContent()
            content.title = "📅 Ngày lễ ngày mai — \(dd)/\(mm)"
            content.body = festivals.joined(separator: " | ") + "\nĐừng quên chuẩn bị nhé!"
            content.sound = .default
            content.categoryIdentifier = "holiday"

            var dateComponents = cal.dateComponents([.year, .month, .day], from: reminderDate)
            dateComponents.hour = 20
            dateComponents.minute = 0

            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
            let request = UNNotificationRequest(
                identifier: "festival_\(dd)_\(mm)_\(yy)",
                content: content,
                trigger: trigger
            )

            notificationCenter.add(request) { error in
                #if DEBUG
                if let error { print("Schedule festival \(dd)/\(mm) error: \(error)") }
                #endif
            }
        }
    }

    // MARK: - Schedule Rằm/Mùng 1 specific

    /// Lên lịch nhắc Rằm & Mùng 1 trong 60 ngày tới
    func scheduleRamMung1Reminders() {
        let cal = Calendar.current
        let today = Date()

        for i in 1...60 {
            guard let checkDate = cal.date(byAdding: .day, value: i, to: today) else { continue }
            let dd = cal.component(.day, from: checkDate)
            let mm = cal.component(.month, from: checkDate)
            let yy = cal.component(.year, from: checkDate)

            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)

            guard lunar.lunarDay == 1 || lunar.lunarDay == 15 else { continue }

            // Nhắc trước 1 ngày, buổi tối
            guard let reminderDate = cal.date(byAdding: .day, value: -1, to: checkDate) else { continue }

            let isRam = lunar.lunarDay == 15
            let label = isRam ? "Rằm" : "Mùng 1"
            let emoji = isRam ? "🌕" : "🌑"

            let content = UNMutableNotificationContent()
            content.title = "\(emoji) Ngày mai là \(label) tháng \(lunar.lunarMonth)"
            content.body = "Chuẩn bị hương hoa, lễ vật cúng gia tiên.\nXem văn khấn trong mục Văn khấn."
            content.sound = .default
            content.categoryIdentifier = "reminder"

            var dateComponents = cal.dateComponents([.year, .month, .day], from: reminderDate)
            dateComponents.hour = 19
            dateComponents.minute = 0

            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
            let request = UNNotificationRequest(
                identifier: "lunar_\(label)_\(lunar.lunarMonth)_\(yy)",
                content: content,
                trigger: trigger
            )

            notificationCenter.add(request) { error in
                #if DEBUG
                if let error { print("Schedule \(label) error: \(error)") }
                #endif
            }
        }
    }

    // MARK: - Setup all

    /// Thiết lập tất cả thông báo (gọi khi app launch)
    func setupAllNotifications(dailyHour: Int = 7, dailyMinute: Int = 0) {
        Task {
            let granted = await requestPermission()
            guard granted else { return }

            scheduleDailyNotification(hour: dailyHour, minute: dailyMinute)
            scheduleGioHoangDaoNotification(hour: max(dailyHour - 1, 5), minute: 30)
            scheduleUpcomingFestivalNotifications()
            scheduleRamMung1Reminders()
        }
    }

    // MARK: - Cancel all

    func cancelAllNotifications() {
        notificationCenter.removeAllPendingNotificationRequests()
    }
}
