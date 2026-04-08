import Foundation

// MARK: - Domain Models — Tổng hợp thông tin ngày

struct DayInfo: Identifiable {
    let id = UUID()
    let solar: SolarDate
    let lunar: LunarDate
    let jd: Int
    let dayOfWeek: String
    let dayOfWeekIndex: Int // 0=Mon..6=Sun
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
    let tietKhiInfo: TietKhiInfoModel
    let trucNgayInfo: TrucNgayInfoModel
    let saoChieuInfo: SaoChieuInfoModel
    let dayRating: DayRatingInfo
    let isRam: Bool
    let isMung1: Bool

    // MARK: - Convenience aliases for flat access (matching view usage)
    var lunarDate: LunarDate { lunar }
    var nenLam: [String] { activities.nenLam }
    var khongNen: [String] { activities.khongNen }
    var trucNgay: String { trucNgayInfo.name }
    var saoChieu: String? { saoChieuInfo.name.isEmpty ? nil : saoChieuInfo.name }
    var tietKhi: String? { tietKhiInfo.currentName }
    var solarDate: SolarDate { solar }
}

struct SolarDate: Hashable {
    let dd: Int
    let mm: Int
    let yy: Int

    var day: Int { dd }
    var month: Int { mm }
    var year: Int { yy }
}

struct LunarDate {
    let day: Int
    let month: Int
    let year: Int
    let leap: Int
    let monthName: String

    var isLeapMonth: Bool { leap != 0 }
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

struct TietKhiInfoModel {
    let currentName: String?
    let nextName: String?
    let nextDd: Int?
    let nextMm: Int?
    let daysUntilNext: Int
}

struct TrucNgayInfoModel {
    let name: String
    let rating: String
}

struct SaoChieuInfoModel {
    let name: String
    let rating: String
}

struct DayRatingInfo {
    let label: String // "Rất tốt", "Tốt", "Trung bình", "Xấu"
    let percent: Int  // 0–100
}

// MARK: - Calendar Day Cell

struct CalendarDay: Identifiable, Hashable {
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
    var dayRatingLabel: String = ""
}

// MARK: - Upcoming Event

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

// MARK: - Weather

struct WeatherInfo {
    let temperature: Double
    let weatherCode: Int
    let humidity: Int
    let windSpeed: Double
    let cityName: String
    let description: String
    let icon: String
    let isDay: Bool
    let feelsLike: Double?
    let uvIndex: Double?
    let timestamp: Date

    static func fromWeatherCode(_ code: Int, isDay: Bool) -> (String, String) {
        switch code {
        case 0: return ("Trời quang", isDay ? "☀️" : "🌙")
        case 1: return ("Quang đãng", isDay ? "🌤️" : "🌙")
        case 2: return ("Có mây", "⛅")
        case 3: return ("Nhiều mây", "☁️")
        case 45, 48: return ("Sương mù", "🌫️")
        case 51, 53, 55: return ("Mưa phùn", "🌦️")
        case 56, 57: return ("Mưa phùn đóng băng", "🌧️")
        case 61, 63, 65: return ("Mưa", "🌧️")
        case 66, 67: return ("Mưa đóng băng", "🌧️")
        case 71, 73, 75: return ("Tuyết rơi", "🌨️")
        case 77: return ("Mưa đá nhỏ", "🌨️")
        case 80, 81, 82: return ("Mưa rào", "🌧️")
        case 85, 86: return ("Tuyết rào", "🌨️")
        case 95: return ("Dông", "⛈️")
        case 96, 99: return ("Dông kèm mưa đá", "⛈️")
        default: return ("Không rõ", "🌡️")
        }
    }
}

struct LocationInfo {
    let latitude: Double
    let longitude: Double
    let cityName: String
}

// MARK: - Historical Event

struct HistoricalEvent: Identifiable {
    let id = UUID()
    let year: Int
    let title: String
    let description: String
    let category: HistoryCategory
    let importance: EventImportance
    let hasImage: Bool

    init(year: Int, title: String, description: String, category: HistoryCategory, importance: EventImportance = .minor, hasImage: Bool = false) {
        self.year = year
        self.title = title
        self.description = description
        self.category = category
        self.importance = importance
        self.hasImage = hasImage
    }
}

enum HistoryCategory: String {
    case vietnam = "Lịch sử Việt Nam"
    case world = "Thế giới"
    case culture = "Văn hóa"
    case science = "Khoa học"

    var emoji: String {
        switch self {
        case .vietnam: return "🇻🇳"
        case .world: return "🌍"
        case .culture: return "🎨"
        case .science: return "🔬"
        }
    }
}

enum EventImportance {
    case major
    case minor
}

// MARK: - City Coordinates

struct CityInfo {
    let name: String
    let latitude: Double
    let longitude: Double
}

enum CityCoordinates {
    static let cities: [CityInfo] = [
        CityInfo(name: "Hà Nội", latitude: 21.0285, longitude: 105.8542),
        CityInfo(name: "TP. Hồ Chí Minh", latitude: 10.8231, longitude: 106.6297),
        CityInfo(name: "Đà Nẵng", latitude: 16.0544, longitude: 108.2022),
        CityInfo(name: "Hải Phòng", latitude: 20.8449, longitude: 106.6881),
        CityInfo(name: "Cần Thơ", latitude: 10.0452, longitude: 105.7469),
        CityInfo(name: "Huế", latitude: 16.4637, longitude: 107.5909),
        CityInfo(name: "Nha Trang", latitude: 12.2388, longitude: 109.1967),
        CityInfo(name: "Đà Lạt", latitude: 11.9404, longitude: 108.4583),
        CityInfo(name: "Vũng Tàu", latitude: 10.4114, longitude: 107.1362),
        CityInfo(name: "Quy Nhơn", latitude: 13.7830, longitude: 109.2197),
        CityInfo(name: "Vinh", latitude: 18.6796, longitude: 105.6813),
        CityInfo(name: "Buôn Ma Thuột", latitude: 12.6680, longitude: 108.0378),
        CityInfo(name: "Thái Nguyên", latitude: 21.5928, longitude: 105.8442),
        CityInfo(name: "Nam Định", latitude: 20.4388, longitude: 106.1621),
        CityInfo(name: "Hạ Long", latitude: 20.9711, longitude: 107.0452),
    ]

    static var cityNames: [String] { cities.map { $0.name } }

    static func getCoordinates(_ cityName: String) -> CityInfo {
        cities.first { $0.name == cityName } ?? cities[0]
    }

    static func toLocationInfo(_ cityName: String) -> LocationInfo {
        let city = getCoordinates(cityName)
        return LocationInfo(latitude: city.latitude, longitude: city.longitude, cityName: city.name)
    }
}
