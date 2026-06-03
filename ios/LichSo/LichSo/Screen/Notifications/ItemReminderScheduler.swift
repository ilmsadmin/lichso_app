import Foundation
import UserNotifications

// ═══════════════════════════════════════════
// ItemReminderScheduler — lập lịch local notification cho ItemEntity có nhắc nhở.
// Port hành vi từ Android NotificationScheduler.scheduleReminder / computeNextReminderTrigger.
//
// iOS dùng UNUserNotificationCenter (không có AlarmManager):
//   • Lặp đơn giản (daily/weekly/monthly-solar/yearly-solar, không âm lịch, advance=0)
//     → UNCalendarNotificationTrigger repeats:true → fire kể cả khi app đóng.
//   • Once / âm lịch / nhắc-trước N ngày → tính ngày dương cụ thể kế tiếp rồi đặt
//     trigger một lần (one-shot). Cần re-arm bằng rescheduleAll(...) khi mở app.
//
// repeatType: 0=Once, 1=Daily, 2=Weekly, 3=MonthlySolar, 4=MonthlyLunar, 5=Yearly
// ═══════════════════════════════════════════
enum ItemReminderScheduler {
    private static let dayMs: Int64 = 24 * 60 * 60 * 1000

    static func identifier(_ id: Int64) -> String { "item_reminder_\(id)" }

    /// Đặt (hoặc đặt lại) thông báo cho 1 item. Tự huỷ lịch cũ trước.
    static func schedule(_ item: ItemEntity) {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: [identifier(item.id)])

        guard item.hasReminder, item.reminderEnabled, let reminderAt = item.reminderAt else { return }

        let content = UNMutableNotificationContent()
        content.title = item.title.isEmpty ? "Nhắc nhở" : item.title
        content.body = item.itemDescription.isEmpty ? "Đã đến giờ nhắc nhở!" : item.itemDescription
        content.sound = .default

        let cal = Calendar.current
        let baseDate = Date(timeIntervalSince1970: Double(reminderAt) / 1000)
        let useSimpleRepeat = item.advanceDays <= 0 && !item.useLunar
            && [1, 2, 3, 5].contains(item.repeatType)

        let trigger: UNCalendarNotificationTrigger
        if useSimpleRepeat {
            var comps = DateComponents()
            comps.hour = cal.component(.hour, from: baseDate)
            comps.minute = cal.component(.minute, from: baseDate)
            switch item.repeatType {
            case 2: comps.weekday = cal.component(.weekday, from: baseDate)               // weekly
            case 3: comps.day = cal.component(.day, from: baseDate)                       // monthly (solar)
            case 5:                                                                        // yearly (solar)
                comps.month = cal.component(.month, from: baseDate)
                comps.day = cal.component(.day, from: baseDate)
            default: break                                                                // daily
            }
            trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: true)
        } else {
            guard let next = computeNextTrigger(item) else { return }   // Once đã qua → bỏ
            let comps = cal.dateComponents([.year, .month, .day, .hour, .minute, .second], from: next)
            trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)
        }

        let request = UNNotificationRequest(identifier: identifier(item.id), content: content, trigger: trigger)
        center.add(request)
    }

    static func cancel(_ id: Int64) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier(id)])
    }

    /// Re-arm toàn bộ — gọi khi app mở / sau migration để các one-shot (âm lịch, nhắc-trước) luôn có lịch kế tiếp.
    static func rescheduleAll(_ items: [ItemEntity]) {
        for item in items where item.hasReminder && item.reminderEnabled {
            schedule(item)
        }
    }

    // ── Tính thời điểm fire kế tiếp (trừ advanceDays). nil nếu Once đã qua. ──
    static func computeNextTrigger(_ item: ItemEntity) -> Date? {
        guard let reminderAt = item.reminderAt else { return nil }
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let advanceMs = Int64(max(0, item.advanceDays)) * dayMs
        let nextEvent: Int64
        switch item.repeatType {
        case 1: nextEvent = nextRepeat(base: reminderAt, after: now + advanceMs, interval: dayMs)
        case 2: nextEvent = nextRepeat(base: reminderAt, after: now + advanceMs, interval: 7 * dayMs)
        case 3: nextEvent = nextMonthlySolar(base: reminderAt, after: now + advanceMs)
        case 4: nextEvent = nextMonthlyLunar(base: reminderAt, after: now + advanceMs)
        case 5: nextEvent = nextYearly(base: reminderAt, after: now + advanceMs, useLunar: item.useLunar)
        default:                                                            // Once
            if reminderAt - advanceMs < now { return nil }
            nextEvent = reminderAt
        }
        let trigger = nextEvent - advanceMs
        let final = trigger < now ? now + 5_000 : trigger
        return Date(timeIntervalSince1970: Double(final) / 1000)
    }

    // ── Helpers (port từ NotificationScheduler.kt) ──
    private static func nextRepeat(base: Int64, after: Int64, interval: Int64) -> Int64 {
        var t = base
        while t <= after { t += interval }
        return t
    }

    private static func nextMonthlySolar(base: Int64, after: Int64) -> Int64 {
        let cal = Calendar.current
        var date = Date(timeIntervalSince1970: Double(base) / 1000)
        let limit = Date(timeIntervalSince1970: Double(after) / 1000)
        while date <= limit {
            date = cal.date(byAdding: .month, value: 1, to: date) ?? date.addingTimeInterval(2_592_000)
        }
        return Int64(date.timeIntervalSince1970 * 1000)
    }

    private static func nextYearly(base: Int64, after: Int64, useLunar: Bool) -> Int64 {
        let cal = Calendar.current
        let baseDate = Date(timeIntervalSince1970: Double(base) / 1000)
        let hh = cal.component(.hour, from: baseDate)
        let mi = cal.component(.minute, from: baseDate)

        if !useLunar {
            var date = baseDate
            let limit = Date(timeIntervalSince1970: Double(after) / 1000)
            while date <= limit {
                date = cal.date(byAdding: .year, value: 1, to: date) ?? date.addingTimeInterval(31_536_000)
            }
            return Int64(date.timeIntervalSince1970 * 1000)
        }

        let lunar = LunarCalendarUtil.convertSolar2Lunar(
            dd: cal.component(.day, from: baseDate),
            mm: cal.component(.month, from: baseDate),
            yy: cal.component(.year, from: baseDate)
        )
        let nowYear = cal.component(.year, from: Date(timeIntervalSince1970: Double(after) / 1000))
        for y in (nowYear - 1)...(nowYear + 5) {
            let (sd, sm, sy) = LunarCalendarUtil.convertLunar2Solar(
                lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth, lunarYear: y, lunarLeap: 0
            )
            if sd == 0 { continue }
            if let cand = makeDate(sy, sm, sd, hh, mi), cand.timeIntervalSince1970 * 1000 > Double(after) {
                return Int64(cand.timeIntervalSince1970 * 1000)
            }
        }
        return nextYearly(base: base, after: after, useLunar: false)
    }

    private static func nextMonthlyLunar(base: Int64, after: Int64) -> Int64 {
        let cal = Calendar.current
        let baseDate = Date(timeIntervalSince1970: Double(base) / 1000)
        let hh = cal.component(.hour, from: baseDate)
        let mi = cal.component(.minute, from: baseDate)
        let lunar = LunarCalendarUtil.convertSolar2Lunar(
            dd: cal.component(.day, from: baseDate),
            mm: cal.component(.month, from: baseDate),
            yy: cal.component(.year, from: baseDate)
        )
        var ly = lunar.lunarYear
        var lm = lunar.lunarMonth
        for _ in 0..<15 {
            let (sd, sm, sy) = LunarCalendarUtil.convertLunar2Solar(
                lunarDay: lunar.lunarDay, lunarMonth: lm, lunarYear: ly, lunarLeap: 0
            )
            if sd != 0, let cand = makeDate(sy, sm, sd, hh, mi),
               cand.timeIntervalSince1970 * 1000 > Double(after) {
                return Int64(cand.timeIntervalSince1970 * 1000)
            }
            lm += 1
            if lm > 12 { lm = 1; ly += 1 }
        }
        return nextMonthlySolar(base: base, after: after)
    }

    private static func makeDate(_ y: Int, _ m: Int, _ d: Int, _ hh: Int, _ mi: Int) -> Date? {
        var comps = DateComponents()
        comps.year = y; comps.month = m; comps.day = d
        comps.hour = hh; comps.minute = mi; comps.second = 0
        return Calendar.current.date(from: comps)
    }
}
