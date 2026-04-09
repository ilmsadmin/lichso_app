import Foundation

// ═══════════════════════════════════════════
// Domain Models — Port từ Android
// ═══════════════════════════════════════════

struct SolarDate {
    let dd: Int
    let mm: Int
    let yy: Int
}

struct LunarDate {
    let day: Int
    let month: Int
    let year: Int
    let leap: Int
    let monthName: String
}

struct MoonPhaseInfo {
    let icon: String
    let name: String
    let age: Double
}

struct GioHoangDaoInfo {
    let name: String
    let time: String
}

struct DayActivitiesInfo {
    let nenLam: [String]
    let khongNen: [String]
    let isXauDay: Bool
    let isNguyetKy: Bool
    let isTamNuong: Bool
}

struct HuongTotInfo {
    let thanTai: String
    let hyThan: String
    let hungThan: String
}

struct TietKhiDisplayInfo {
    let currentName: String?
    let nextName: String?
    let nextDd: Int?
    let nextMm: Int?
    let daysUntilNext: Int
}

struct TrucNgayDisplayInfo {
    let name: String
    let rating: String
}

struct SaoChieuDisplayInfo {
    let name: String
    let rating: String
}

struct DayRatingInfo {
    let label: String  // "Rất tốt", "Tốt", "Trung bình", "Xấu"
    let percent: Int   // 0–100
}

struct DayInfo {
    let solar: SolarDate
    let lunar: LunarDate
    let jd: Int
    let dayOfWeek: String
    let dayOfWeekIndex: Int
    let yearCanChi: String
    let monthCanChi: String
    let dayCanChi: String
    let hourCanChi: String
    let moonPhase: MoonPhaseInfo
    let gioHoangDao: [GioHoangDaoInfo]
    let activities: DayActivitiesInfo
    let huong: HuongTotInfo
    let solarHoliday: String?
    let lunarHoliday: String?
    let tietKhi: TietKhiDisplayInfo
    let trucNgay: TrucNgayDisplayInfo
    let saoChieu: SaoChieuDisplayInfo
    let dayRating: DayRatingInfo
    let isRam: Bool
    let isMung1: Bool
}

struct CalendarDay: Identifiable {
    var id: String { "\(solarYear)-\(solarMonth)-\(solarDay)" }
    let solarDay: Int
    let solarMonth: Int
    let solarYear: Int
    let lunarDay: Int
    let lunarMonth: Int
    let isCurrentMonth: Bool
    let isToday: Bool
    let isSunday: Bool
    let isSaturday: Bool
    let isHoliday: Bool
    let hasEvent: Bool
    let lunarDisplayText: String
    let dayRatingLabel: String
}

struct UpcomingEvent: Identifiable {
    let id = UUID()
    let title: String
    let timeLabel: String
    let tag: String
    let colorType: EventColor
}

enum EventColor {
    case gold, teal, red
}
