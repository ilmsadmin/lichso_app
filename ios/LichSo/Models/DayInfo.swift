import Foundation

// MARK: - Domain Models

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

struct GioHoangDaoInfo: Identifiable {
    let id = UUID()
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

struct TietKhiInfo {
    let currentName: String?
    let nextName: String?
    let nextDd: Int?
    let nextMm: Int?
    let daysUntilNext: Int
}

struct TrucNgayInfo {
    let name: String
    let rating: String // "Tốt", "Xấu", "Trung bình"
}

struct SaoChieuInfo {
    let name: String
    let description: String
}

struct DayRatingInfo {
    let score: Int // 1-5
    let label: String
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
    let tietKhi: TietKhiInfo
    let trucNgay: TrucNgayInfo
    let saoChieu: SaoChieuInfo
    let dayRating: DayRatingInfo
    let isRam: Bool
    let isMung1: Bool
}

struct CalendarDay: Identifiable {
    let id = UUID()
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
}

enum EventColor { case gold, teal, red }

struct UpcomingEvent: Identifiable {
    let id = UUID()
    let title: String
    let timeLabel: String
    let tag: String
    let colorType: EventColor
}

// MARK: - Task / Note / Reminder
struct TaskItem: Identifiable {
    var id = UUID()
    var title: String
    var isDone: Bool
    var priority: Int // 0=low,1=normal,2=high
    var deadline: Date? = nil
    var createdAt: Date = Date()
}

struct NoteItem: Identifiable {
    var id = UUID()
    var title: String
    var content: String
    var color: String // "gold","teal","orange","purple","green","red"
    var createdAt: Date = Date()
}

struct ReminderItem: Identifiable {
    var id = UUID()
    var title: String
    var time: Date
    var isActive: Bool
    var note: String = ""
    var createdAt: Date = Date()
}

// MARK: - Chat
struct ChatMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let timestamp: Date
}
