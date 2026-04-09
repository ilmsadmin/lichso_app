import Foundation

// ═══════════════════════════════════════════
// Can Chi Calculator — Tính Can Chi cho năm, tháng, ngày
// Port chính xác từ Android Kotlin
// KHÔNG ĐƯỢC THAY ĐỔI THUẬT TOÁN
// ═══════════════════════════════════════════

enum CanChiCalculator {

    static let THIEN_CAN = ["Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"]
    static let DIA_CHI = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
    static let THU = ["Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"]

    static func getYearCanChi(lunarYear: Int) -> String {
        let can = THIEN_CAN[(lunarYear + 6) % 10]
        let chi = DIA_CHI[(lunarYear + 8) % 12]
        return "\(can) \(chi)"
    }

    static func getMonthCanChi(lunarMonth: Int, lunarYear: Int) -> String {
        let chiIdx = (lunarMonth + 1) % 12
        let chi = DIA_CHI[chiIdx]
        let canIdx = (lunarYear * 12 + lunarMonth + 3) % 10
        let can = THIEN_CAN[canIdx]
        return "\(can) \(chi)"
    }

    static func getDayCanChi(jd: Int) -> String {
        let can = THIEN_CAN[(jd + 9) % 10]
        let chi = DIA_CHI[(jd + 1) % 12]
        return "\(can) \(chi)"
    }

    static func getDayOfWeek(jd: Int) -> String {
        return THU[jd % 7]
    }

    static func getDayOfWeekIndex(jd: Int) -> Int {
        return jd % 7 // 0=Mon ... 6=Sun
    }
}
