import Foundation

// ═══════════════════════════════════════════
// Ngày lễ Dương lịch & Âm lịch Việt Nam
// Port chính xác từ Android Kotlin
// KHÔNG ĐƯỢC THAY ĐỔI
// ═══════════════════════════════════════════

enum HolidayUtil {

    static let SOLAR_HOLIDAYS: [String: String] = [
        "1/1": "Tết Dương lịch",
        "14/2": "Valentine",
        "8/3": "Quốc tế Phụ nữ",
        "30/4": "Giải phóng miền Nam",
        "1/5": "Quốc tế Lao động",
        "19/5": "Sinh nhật Bác Hồ",
        "1/6": "Quốc tế Thiếu nhi",
        "27/7": "Thương binh Liệt sĩ",
        "2/9": "Quốc khánh",
        "20/10": "Phụ nữ Việt Nam",
        "20/11": "Nhà giáo Việt Nam",
        "22/12": "Quân đội Nhân dân VN",
        "24/12": "Giáng sinh",
        "25/12": "Giáng sinh"
    ]

    static let LUNAR_HOLIDAYS: [String: String] = [
        "1/1": "Tết Nguyên Đán",
        "2/1": "Mùng 2 Tết",
        "3/1": "Mùng 3 Tết",
        "15/1": "Rằm tháng Giêng",
        "3/3": "Tết Hàn thực",
        "10/3": "Giỗ Tổ Hùng Vương",
        "15/4": "Phật Đản",
        "5/5": "Tết Đoan Ngọ",
        "15/7": "Rằm tháng Bảy (Vu Lan)",
        "15/8": "Tết Trung Thu",
        "9/9": "Tết Trùng Dương",
        "10/10": "Tết cơm mới",
        "15/10": "Rằm tháng Mười",
        "23/12": "Ông Táo về trời",
        "30/12": "Tất niên"
    ]

    static func getSolarHoliday(dd: Int, mm: Int) -> String? {
        return SOLAR_HOLIDAYS["\(dd)/\(mm)"]
    }

    static func getLunarHoliday(lunarDay: Int, lunarMonth: Int) -> String? {
        return LUNAR_HOLIDAYS["\(lunarDay)/\(lunarMonth)"]
    }
}
