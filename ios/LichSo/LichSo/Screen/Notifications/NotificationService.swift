import Foundation
import SwiftData

// ═══════════════════════════════════════════
// NotificationService — Tạo thông báo thật từ dữ liệu lịch
// Port logic từ Android DailyNotificationWorker,
// FestivalReminderWorker, GioDaiCatWorker
// ═══════════════════════════════════════════

@MainActor
class NotificationService {
    static let shared = NotificationService()
    private let provider = DayInfoProvider.shared

    // MARK: - Generate daily morning notification

    /// Tạo thông báo tóm tắt ngày mới (tương tự DailyNotificationWorker Android)
    func generateDailyNotification(for date: Date = Date()) -> (title: String, description: String, type: String) {
        generateMorningSummaryNotification(for: date)
    }

    /// Tạo thông báo buổi sáng đã gộp: thời tiết nếu có cache + thông tin ngày +
    /// giờ hoàng đạo + nên/tránh. Mirror Android `fireMorningSummary`.
    func generateMorningSummaryNotification(for date: Date = Date()) -> (title: String, description: String, type: String) {
        let cal = Calendar.current
        let dd = cal.component(.day, from: date)
        let mm = cal.component(.month, from: date)
        let yy = cal.component(.year, from: date)

        let dayInfo = provider.getDayInfo(dd: dd, mm: mm, yy: yy)

        let ddStr = String(format: "%02d", dd)
        let mmStr = String(format: "%02d", mm)
        let lunarStr = "\(dayInfo.lunar.day)/\(dayInfo.lunar.month) Âm lịch"
        let title = "Chào buổi sáng — \(ddStr)/\(mmStr)"
        var lines: [String] = []

        if cal.isDateInToday(date), let weather = WeatherService.shared.weather {
            let unit = UserDefaults.standard.string(forKey: "setting_temp_unit") ?? "°C"
            let city = UserDefaults.standard.string(forKey: "setting_location") ?? weather.cityName
            let tempNow = Int(weather.temperature.rounded())
            let tempMin = Int((weather.dailyForecast.first?.tempMin ?? weather.temperature).rounded())
            let tempMax = Int((weather.dailyForecast.first?.tempMax ?? weather.temperature).rounded())
            lines.append("\(city): \(tempNow)\(unit), \(tempMin)\(unit)-\(tempMax)\(unit), \(weather.conditionText)")
            lines.append(weatherAdvice(weather))
        }

        lines.append("Ngày: \(dayInfo.dayOfWeek) \(ddStr)/\(mmStr) - \(lunarStr)")
        lines.append("Can Chi: \(dayInfo.dayCanChi) - \(dayInfo.dayRating.label)")

        if dayInfo.activities.isNguyetKy {
            lines.append("Lưu ý: Ngày Nguyệt kỵ")
        }
        if dayInfo.activities.isTamNuong {
            lines.append("Lưu ý: Ngày Tam nương")
        }

        let gioText = dayInfo.gioHoangDao.prefix(3)
            .map { "\($0.name) (\($0.time))" }
            .joined(separator: ", ")
        if !gioText.isEmpty {
            lines.append("Giờ hoàng đạo: \(gioText)")
        }
        if !dayInfo.activities.nenLam.isEmpty {
            lines.append("Nên: \(dayInfo.activities.nenLam.prefix(2).joined(separator: ", "))")
        }
        if !dayInfo.activities.khongNen.isEmpty {
            lines.append("Tránh: \(dayInfo.activities.khongNen.prefix(2).joined(separator: ", "))")
        }
        if let sHol = dayInfo.solarHoliday {
            lines.append("Ngày lễ: \(sHol)")
        }
        if let lHol = dayInfo.lunarHoliday {
            lines.append("Âm lịch: \(lHol)")
        }

        return (title, lines.joined(separator: "\n"), "daily")
    }

    private func weatherAdvice(_ weather: WeatherData) -> String {
        let maxTempC = weather.dailyForecast.first?.tempMax ?? weather.temperature
        let minTempC = weather.dailyForecast.first?.tempMin ?? weather.temperature
        if maxTempC >= 34 {
            return "Nắng khá gắt, nhớ mang nước và che nắng khi ra ngoài."
        }
        if maxTempC >= 30 {
            return "Trưa có thể nắng nóng, nên mang ô hoặc áo khoác mỏng."
        }
        if minTempC <= 18 {
            return "Sáng sớm khá mát, nên mặc thêm áo khoác nhẹ."
        }
        if weather.humidity >= 85 {
            return "Độ ẩm cao, có thể oi nhẹ. Uống đủ nước để giữ sức."
        }
        return "Thời tiết tương đối dễ chịu, chúc bạn một ngày nhiều năng lượng."
    }

    // MARK: - Generate festival reminder (ngày mai)

    /// Kiểm tra ngày mai có lễ/sự kiện không → tạo thông báo nhắc trước
    func generateFestivalReminder(for date: Date = Date()) -> (title: String, description: String, type: String)? {
        let cal = Calendar.current
        guard let tomorrow = cal.date(byAdding: .day, value: 1, to: date) else { return nil }

        let dd = cal.component(.day, from: tomorrow)
        let mm = cal.component(.month, from: tomorrow)
        let yy = cal.component(.year, from: tomorrow)

        let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)
        var festivals: [String] = []

        // Solar holiday
        if let sHol = HolidayUtil.getSolarHoliday(dd: dd, mm: mm) {
            festivals.append(sHol)
        }

        // Lunar holiday
        if let lHol = HolidayUtil.getLunarHoliday(lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth) {
            festivals.append(lHol)
        }

        // Rằm / Mùng 1
        if lunar.lunarDay == 1 && !festivals.contains(where: { $0.contains("Mùng 1") || $0.contains("Tết") }) {
            festivals.append("Mùng 1 tháng \(lunar.lunarMonth) Âm lịch")
        } else if lunar.lunarDay == 15 && !festivals.contains(where: { $0.contains("Rằm") }) {
            festivals.append("Rằm tháng \(lunar.lunarMonth) Âm lịch")
        }

        guard !festivals.isEmpty else { return nil }

        let title = "📅 Ngày lễ ngày mai — \(dd)/\(mm)/\(yy)"
        var desc = "Ngày \(dd)/\(mm)/\(yy) (\(lunar.lunarDay)/\(lunar.lunarMonth) Âm lịch)\n"
        desc += festivals.joined(separator: " | ")
        desc += "\nHãy chuẩn bị lễ vật và sắp xếp công việc phù hợp."

        return (title, desc, "holiday")
    }

    // MARK: - Generate giờ hoàng đạo notification

    /// Tạo thông báo giờ hoàng đạo (tương tự GioDaiCatWorker Android)
    func generateGioHoangDaoNotification(for date: Date = Date()) -> (title: String, description: String, type: String) {
        let cal = Calendar.current
        let dd = cal.component(.day, from: date)
        let mm = cal.component(.month, from: date)
        let yy = cal.component(.year, from: date)

        let dayInfo = provider.getDayInfo(dd: dd, mm: mm, yy: yy)
        let isGoodDay = !dayInfo.activities.isXauDay

        let ddStr = String(format: "%02d", dd)
        let mmStr = String(format: "%02d", mm)
        let title = "Giờ Hoàng Đạo — \(dayInfo.dayOfWeek) \(ddStr)/\(mmStr)"

        var descParts: [String] = []
        descParts.append("\(dayInfo.dayCanChi) — \(dayInfo.lunar.day)/\(dayInfo.lunar.month) Âm lịch")

        if isGoodDay {
            descParts.append("Ngày Hoàng Đạo — thuận lợi cho nhiều việc")
        } else {
            descParts.append("Ngày Hắc Đạo — nên chọn giờ tốt để hành sự")
        }

        for gio in dayInfo.gioHoangDao {
            descParts.append("• \(gio.name): \(gio.time)")
        }

        descParts.append("Hướng Thần Tài: \(dayInfo.huong.thanTai)")
        descParts.append("Hướng Hỷ Thần: \(dayInfo.huong.hyThan)")

        return (title, descParts.joined(separator: "\n"), "good_day")
    }

    // MARK: - Generate good day suggestion

    /// Thông báo ngày tốt sắp tới (scan 7 ngày)
    func generateGoodDaySuggestion(for date: Date = Date()) -> (title: String, description: String, type: String)? {
        let cal = Calendar.current

        var goodDays: [(date: Date, dayInfo: DayInfo)] = []

        // Scan 7 ngày tiếp theo
        for i in 1...7 {
            guard let checkDate = cal.date(byAdding: .day, value: i, to: date) else { continue }
            let dd = cal.component(.day, from: checkDate)
            let mm = cal.component(.month, from: checkDate)
            let yy = cal.component(.year, from: checkDate)
            let dayInfo = provider.getDayInfo(dd: dd, mm: mm, yy: yy)

            if dayInfo.dayRating.percent >= 80 {
                goodDays.append((checkDate, dayInfo))
            }
        }

        guard !goodDays.isEmpty else { return nil }

        let title = "🌟 Ngày tốt sắp tới trong tuần"
        var desc: [String] = []
        for (gDate, gInfo) in goodDays.prefix(3) {
            let dd = cal.component(.day, from: gDate)
            let mm = cal.component(.month, from: gDate)
            desc.append("• \(gInfo.dayOfWeek) \(dd)/\(mm): \(gInfo.dayCanChi) — \(gInfo.dayRating.label) (\(gInfo.dayRating.percent)%)")
        }
        desc.append("Phù hợp cho khai trương, xuất hành, cưới hỏi, nhập trạch.")

        return (title, desc.joined(separator: "\n"), "good_day")
    }

    // MARK: - Generate Rằm/Mùng 1 reminder

    /// Nhắc nhở cúng Rằm/Mùng 1 (2 ngày trước)
    func generateLunarDateReminder(for date: Date = Date()) -> (title: String, description: String, type: String)? {
        let cal = Calendar.current

        // Check 2 ngày tới
        for offset in 1...2 {
            guard let checkDate = cal.date(byAdding: .day, value: offset, to: date) else { continue }
            let dd = cal.component(.day, from: checkDate)
            let mm = cal.component(.month, from: checkDate)
            let yy = cal.component(.year, from: checkDate)
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)

            if lunar.lunarDay == 15 {
                let dayStr = offset == 1 ? "Ngày mai" : "Ngày \(dd)/\(mm)"
                let title = "🌕 \(dayStr) là Rằm tháng \(lunar.lunarMonth)"
                let desc = "Rằm tháng \(lunar.lunarMonth) Âm lịch — \(dd)/\(mm)/\(yy)\nĐừng quên chuẩn bị hương hoa, lễ vật cúng gia tiên.\nXem bài văn khấn cúng Rằm trong mục Văn khấn."
                return (title, desc, "reminder")
            }

            if lunar.lunarDay == 1 {
                let dayStr = offset == 1 ? "Ngày mai" : "Ngày \(dd)/\(mm)"
                let title = "🌑 \(dayStr) là Mùng 1 tháng \(lunar.lunarMonth)"
                let desc = "Mùng 1 tháng \(lunar.lunarMonth) Âm lịch — \(dd)/\(mm)/\(yy)\nĐừng quên chuẩn bị hương hoa cúng đầu tháng.\nXem bài văn khấn Mùng 1 trong mục Văn khấn."
                return (title, desc, "reminder")
            }
        }

        return nil
    }

    // MARK: - Generate activities summary

    /// Thông báo nên/không nên làm hôm nay
    func generateActivitiesSummary(for date: Date = Date()) -> (title: String, description: String, type: String) {
        let cal = Calendar.current
        let dd = cal.component(.day, from: date)
        let mm = cal.component(.month, from: date)
        let yy = cal.component(.year, from: date)

        let dayInfo = provider.getDayInfo(dd: dd, mm: mm, yy: yy)

        let title = "📋 Nên & Không nên — \(dayInfo.dayOfWeek) \(dd)/\(mm)"

        var desc: [String] = []
        desc.append("Trực ngày: \(dayInfo.trucNgay.name) | Sao: \(dayInfo.saoChieu.name)")

        if !dayInfo.activities.nenLam.isEmpty {
            let nenLam = dayInfo.activities.nenLam.prefix(5).joined(separator: ", ")
            desc.append("✅ Nên: \(nenLam)")
        }
        if !dayInfo.activities.khongNen.isEmpty {
            let khongNen = dayInfo.activities.khongNen.prefix(5).joined(separator: ", ")
            desc.append("❌ Tránh: \(khongNen)")
        }

        if dayInfo.activities.isNguyetKy {
            desc.append("⚠️ Ngày Nguyệt Kỵ — không nên làm việc lớn")
        }
        if dayInfo.activities.isTamNuong {
            desc.append("⚠️ Ngày Tam Nương — cẩn thận trong mọi việc")
        }

        return (title, desc.joined(separator: "\n"), "daily")
    }

    // MARK: - Batch generate all notifications for today

    /// Sinh tất cả thông báo thật cho ngày hiện tại và lưu vào SwiftData
    func generateAllNotifications(context: ModelContext, for date: Date = Date()) {
        let now = Int64(date.timeIntervalSince1970 * 1000)
        // Kiểm tra đã tạo thông báo hôm nay chưa (tránh trùng lặp)
        let todayStart = Calendar.current.startOfDay(for: date)
        let todayStartMs = Int64(todayStart.timeIntervalSince1970 * 1000)
        let todayEndMs = todayStartMs + 86_400_000

        let descriptor = FetchDescriptor<NotificationEntity>(
            predicate: #Predicate { $0.createdAt >= todayStartMs && $0.createdAt < todayEndMs }
        )
        let existingCount = (try? context.fetchCount(descriptor)) ?? 0
        guard existingCount == 0 else { return }

        // 1. Thông báo buổi sáng — đã gộp ngày + giờ hoàng đạo + nên/tránh
        let morning = generateMorningSummaryNotification(for: date)
        let n1 = NotificationEntity(id: now, title: morning.title, notificationDescription: morning.description, type: morning.type)
        n1.createdAt = now
        context.insert(n1)

        if let festival = generateFestivalReminder(for: date) {
            let n4 = NotificationEntity(id: now - 1, title: festival.title, notificationDescription: festival.description, type: festival.type)
            n4.createdAt = now - 1
            context.insert(n4)
        }

        if let lunarReminder = generateLunarDateReminder(for: date) {
            let n5 = NotificationEntity(id: now - 2, title: lunarReminder.title, notificationDescription: lunarReminder.description, type: lunarReminder.type)
            n5.createdAt = now - 2
            context.insert(n5)
        }

        let weekday = Calendar.current.component(.weekday, from: date)
        if weekday == 2, let goodDay = generateGoodDaySuggestion(for: date) {
            let n6 = NotificationEntity(id: now - 3, title: goodDay.title, notificationDescription: goodDay.description, type: goodDay.type)
            n6.createdAt = now - 3
            context.insert(n6)
        }

        try? context.save()
    }

    // MARK: - Seed initial notifications (first launch with real data)

    /// Tạo thông báo lịch sử cho lần đầu mở app (dùng dữ liệu thật)
    func seedInitialNotifications(context: ModelContext) {
        let now = Date()
        let cal = Calendar.current
        let nowMs = Int64(now.timeIntervalSince1970 * 1000)
        let hour: Int64 = 3_600_000

        // Kiểm tra đã có thông báo nào chưa
        let descriptor = FetchDescriptor<NotificationEntity>()
        let count = (try? context.fetchCount(descriptor)) ?? 0
        guard count == 0 else { return }

        // === Thông báo HÔM NAY ===
        // 1. Tóm tắt buổi sáng đã gộp
        let daily = generateMorningSummaryNotification(for: now)
        let n1 = NotificationEntity(id: nowMs, title: daily.title, notificationDescription: daily.description, type: daily.type)
        n1.createdAt = nowMs
        context.insert(n1)

        if let festival = generateFestivalReminder(for: now) {
            let n3 = NotificationEntity(id: nowMs - hour / 2, title: festival.title, notificationDescription: festival.description, type: festival.type)
            n3.createdAt = nowMs - hour / 2
            context.insert(n3)
        }

        // === Thông báo HÔM QUA (đã đọc) ===
        if let yesterday = cal.date(byAdding: .day, value: -1, to: now) {
            let yMs = Int64(yesterday.timeIntervalSince1970 * 1000)

            let yDaily = generateMorningSummaryNotification(for: yesterday)
            let ny1 = NotificationEntity(id: yMs, title: yDaily.title, notificationDescription: yDaily.description, type: yDaily.type)
            ny1.createdAt = yMs
            ny1.isRead = true
            context.insert(ny1)

            if let yFestival = generateFestivalReminder(for: yesterday) {
                let ny2 = NotificationEntity(id: yMs - hour, title: yFestival.title, notificationDescription: yFestival.description, type: yFestival.type)
                ny2.createdAt = yMs - hour
                ny2.isRead = true
                context.insert(ny2)
            }
        }

        // === Thông báo 2 NGÀY TRƯỚC (đã đọc) ===
        if let twoDaysAgo = cal.date(byAdding: .day, value: -2, to: now) {
            let tMs = Int64(twoDaysAgo.timeIntervalSince1970 * 1000)

            let tDaily = generateMorningSummaryNotification(for: twoDaysAgo)
            let nt1 = NotificationEntity(id: tMs, title: tDaily.title, notificationDescription: tDaily.description, type: tDaily.type)
            nt1.createdAt = tMs
            nt1.isRead = true
            context.insert(nt1)

            if let goodDay = generateGoodDaySuggestion(for: twoDaysAgo) {
                let nt2 = NotificationEntity(id: tMs - hour, title: goodDay.title, notificationDescription: goodDay.description, type: goodDay.type)
                nt2.createdAt = tMs - hour
                nt2.isRead = true
                context.insert(nt2)
            }
        }

        try? context.save()
    }
}
