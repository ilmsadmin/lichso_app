import Foundation

/// Contextual one-line hint shown on the Home screen for the selected day.
/// Ported 1:1 from Android `SmartReminderProvider.contextualHintFor`.
enum SmartReminderProvider {
    static func contextualHint(for info: DayInfo) -> String? {
        let lunarDay = info.lunar.day
        let lunarMonth = info.lunar.month

        // Tết
        if lunarMonth == 1 && lunarDay == 1 {
            return "Chúc mừng năm mới — sáng mùng 1 nhớ thắp hương gia tiên đầu năm."
        }
        // Ông Công ông Táo
        if lunarMonth == 12 && lunarDay == 23 {
            return "Hôm nay 23 tháng Chạp — tiễn ông Công ông Táo về trời, chuẩn bị mâm cá chép."
        }
        // Rằm tháng Bảy (Vu Lan)
        if lunarMonth == 7 && lunarDay == 15 {
            return "Rằm tháng Bảy — Vu Lan báo hiếu, chuẩn bị lễ chay cúng cô hồn."
        }
        // Mùng 1
        if info.isMung1 {
            return "Mùng 1 đầu tháng — sáng nay nhớ thắp hương gia tiên cầu bình an."
        }
        // Rằm
        if info.isRam {
            return "Hôm nay là Rằm — chiều 5h thắp hương lễ Phật, đặt hoa quả ban thờ."
        }
        // Đêm trước Mùng 1
        if lunarDay == 30 || lunarDay == 29 {
            return "Mai là Mùng 1 — chuẩn bị hoa, trà, oản trước cho buổi sáng đầu tháng."
        }
        // Đêm trước Rằm
        if lunarDay == 14 {
            return "Mai là ngày Rằm — sửa soạn mâm cúng, hoa quả tươi cho buổi chiều."
        }
        // Tam nương
        if info.activities.isTamNuong {
            return "Hôm nay Tam Nương — tránh khởi sự lớn (cưới hỏi, khai trương, xuất hành xa)."
        }
        // Nguyệt kỵ
        if info.activities.isNguyetKy {
            return "Hôm nay Nguyệt Kỵ — nên thư thái, lùi việc trọng đại sang ngày khác."
        }
        // Lunar holiday
        if let hol = info.lunarHoliday {
            return "Lễ âm hôm nay: \(hol) — tham khảo bài văn khấn phù hợp trong app."
        }
        return nil
    }
}

/// Reads the user's saved countdown events (shared store with CountdownScreen) and
/// returns the nearest upcoming one for the Home countdown card. Parity with Android.
enum HomeCountdownLoader {
    static func nextEvent() -> CountdownEvent? {
        guard let data = UserDefaults.standard.data(forKey: "countdown_events"),
              let events = try? JSONDecoder().decode([CountdownEvent].self, from: data)
        else { return nil }
        return events
            .filter { $0.daysRemaining >= 0 }
            .min { $0.daysRemaining < $1.daysRemaining }
    }
}
