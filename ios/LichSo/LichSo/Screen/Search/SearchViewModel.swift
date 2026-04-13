import SwiftUI

// ═══════════════════════════════════════════
// SearchViewModel — Holiday search, lunar conversion
// Uses HolidayUtil + LunarCalendarUtil
// ═══════════════════════════════════════════

enum SearchResultType { case holiday, date, lunar }

struct SearchResult: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let type: SearchResultType
    let highlightTerm: String
    // Optional day info for tap-through to DayDetailScreen
    var dayInfo: DayInfo? = nil
}

// ── Good day result for weekly display ──
struct GoodDayItem: Identifiable {
    let id = UUID()
    let dd: Int
    let mm: Int
    let yy: Int
    let dayOfWeek: String
    let ratingLabel: String
    let ratingScore: Int
    let solarHoliday: String?
    let lunarHoliday: String?
    let lunarDay: Int
    let lunarMonth: Int
    var dayInfo: DayInfo? = nil
}

// ── Zodiac compatibility item ──
struct ZodiacCompatItem: Identifiable {
    let id = UUID()
    let zodiac: String           // "Tý", "Sửu", ...
    let zodiacEmoji: String
    let compatScore: Int         // 0-100
    let compatLabel: String      // "Rất hợp", "Hợp", "Bình thường", "Khắc"
    let reason: String
}

@MainActor
class SearchViewModel: ObservableObject {
    @Published var query = ""
    @Published var results: [SearchResult] = []
    @Published var recentSearches: [String] = []
    @Published var lunarInput = ""
    @Published var lunarOutput = ""
    @Published var lunarSummary = ""

    // ── Quick lookup: Tuổi hợp ──
    @Published var tuoiHopInput = ""              // birth year text
    @Published var tuoiHopResults: [ZodiacCompatItem] = []
    @Published var tuoiHopUserZodiac = ""
    @Published var tuoiHopUserCanChi = ""
    @Published var tuoiHopError = ""

    // ── Quick lookup: Ngày tốt tuần này ──
    @Published var goodDaysThisWeek: [GoodDayItem] = []

    // ── Quick lookup: Đi đến ngày ──
    @Published var gotoDate = Date()
    @Published var gotoDayInfo: DayInfo? = nil

    // ── Zodiac data ──
    static let zodiacNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
    static let zodiacEmojis = ["🐭","🐂","🐯","🐰","🐲","🐍","🐴","🐑","🐵","🐓","🐕","🐗"]
    // Compatibility matrix [baseZodiac(0=Tý)][targetZodiac(0=Tý)] — score 0-100
    // Based on traditional tam hợp / tứ hành xung / lục hợp
    static let compatMatrix: [[Int]] = [
        // Tý  Sửu  Dần  Mão  Thìn  Tỵ  Ngọ  Mùi  Thân  Dậu  Tuất  Hợi
        [100,  70,  50,  70,  80,   75,  30,  40,  90,   60,   45,   85], // Tý
        [ 70, 100,  45,  55,  75,   85,  50,  80,  60,   45,   65,   50], // Sửu
        [ 50,  45, 100,  35,  55,   40,  85,  65,  75,   50,   90,   70], // Dần
        [ 70,  55,  35, 100,  50,   65,  55,  80,  40,   85,   55,   75], // Mão
        [ 80,  75,  55,  50, 100,   50,  45,  40,  85,   70,   75,   60], // Thìn
        [ 75,  85,  40,  65,  50,  100,  55,  70,  50,   90,   45,   80], // Tỵ
        [ 30,  50,  85,  55,  45,   55, 100,  35,  65,   75,   80,   50], // Ngọ
        [ 40,  80,  65,  80,  40,   70,  35, 100,  50,   55,   70,   85], // Mùi
        [ 90,  60,  75,  40,  85,   50,  65,  50, 100,   80,   40,   55], // Thân
        [ 60,  45,  50,  85,  70,   90,  75,  55,  80,  100,   50,   65], // Dậu
        [ 45,  65,  90,  55,  75,   45,  80,  70,  40,   50,  100,   75], // Tuất
        [ 85,  50,  70,  75,  60,   80,  50,  85,  55,   65,   75,  100], // Hợi
    ]

    init() {
        loadRecent()
    }

    // ── Search Logic ──
    func performSearch() {
        let q = query.trimmingCharacters(in: .whitespaces)
        guard !q.isEmpty else { results = []; return }

        var found: [SearchResult] = []

        // Search solar holidays
        for (key, name) in HolidayUtil.SOLAR_HOLIDAYS {
            if name.localizedCaseInsensitiveContains(q) || key.contains(q) {
                let parts = key.split(separator: "/")
                if parts.count == 2, let day = Int(parts[0]), let month = Int(parts[1]) {
                    let year = Calendar.current.component(.year, from: Date())
                    let dateStr = String(format: "%02d/%02d/%d", day, month, year)
                    found.append(SearchResult(
                        title: name,
                        subtitle: "\(dateStr) · Dương lịch",
                        type: .holiday,
                        highlightTerm: q
                    ))
                }
            }
        }

        // Search lunar holidays
        for (key, name) in HolidayUtil.LUNAR_HOLIDAYS {
            if name.localizedCaseInsensitiveContains(q) || key.contains(q) {
                let parts = key.split(separator: "/")
                if parts.count == 2, let day = Int(parts[0]), let month = Int(parts[1]) {
                    let year = Calendar.current.component(.year, from: Date())
                    let solar = LunarCalendarUtil.convertLunar2Solar(
                        lunarDay: day, lunarMonth: month, lunarYear: year, lunarLeap: 0, timeZone: 7.0
                    )
                    let solarStr = String(format: "%02d/%02d/%d", solar.0, solar.1, solar.2)
                    found.append(SearchResult(
                        title: name,
                        subtitle: "\(day)/\(month) Âm lịch — \(solarStr) · Nghỉ lễ",
                        type: .lunar,
                        highlightTerm: q
                    ))
                }
            }
        }

        // Try to parse as date dd/MM
        let dateParts = q.split(separator: "/").compactMap { Int($0) }
        if dateParts.count >= 2 {
            let day = dateParts[0], month = dateParts[1]
            let year = dateParts.count >= 3 ? dateParts[2] : Calendar.current.component(.year, from: Date())
            if day >= 1 && day <= 31 && month >= 1 && month <= 12 {
                // Solar → Lunar
                let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: day, mm: month, yy: year, timeZone: 7.0)
                let info = DayInfoProvider.shared.getDayInfo(dd: day, mm: month, yy: year)
                found.append(SearchResult(
                    title: "\(day)/\(month)/\(year) Dương lịch",
                    subtitle: "Âm lịch: \(lunar.lunarDay)/\(lunar.lunarMonth)/\(lunar.lunarYear)",
                    type: .date,
                    highlightTerm: q,
                    dayInfo: info
                ))
            }
        }

        results = found
    }

    // ── Quick Lookup: Ngày tốt tuần này ──
    func loadGoodDaysThisWeek() {
        let calendar = Calendar.current
        let today = Date()
        // Start from Monday of this week
        var comps = calendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: today)
        comps.weekday = 2 // Monday
        guard let monday = calendar.date(from: comps) else { return }

        var items: [GoodDayItem] = []
        for offset in 0..<7 {
            guard let date = calendar.date(byAdding: .day, value: offset, to: monday) else { continue }
            let c = calendar.dateComponents([.year, .month, .day], from: date)
            guard let dd = c.day, let mm = c.month, let yy = c.year else { continue }
            let info = DayInfoProvider.shared.getDayInfo(dd: dd, mm: mm, yy: yy)
            items.append(GoodDayItem(
                dd: dd, mm: mm, yy: yy,
                dayOfWeek: info.dayOfWeek,
                ratingLabel: info.dayRating.label,
                ratingScore: info.dayRating.percent,
                solarHoliday: info.solarHoliday,
                lunarHoliday: info.lunarHoliday,
                lunarDay: info.lunar.day,
                lunarMonth: info.lunar.month,
                dayInfo: info
            ))
        }
        goodDaysThisWeek = items
    }

    // ── Quick Lookup: Đi đến ngày ──
    func loadGotoDay() {
        let c = Calendar.current.dateComponents([.year, .month, .day], from: gotoDate)
        guard let dd = c.day, let mm = c.month, let yy = c.year else { return }
        gotoDayInfo = DayInfoProvider.shared.getDayInfo(dd: dd, mm: mm, yy: yy)
    }

    // ── Quick Lookup: Tuổi hợp ──
    func calculateTuoiHop() {
        tuoiHopError = ""
        tuoiHopResults = []
        tuoiHopUserZodiac = ""
        tuoiHopUserCanChi = ""

        let trimmed = tuoiHopInput.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else {
            tuoiHopError = "Vui lòng nhập năm sinh"
            return
        }
        guard let year = Int(trimmed), year >= 1900 && year <= 2100 else {
            tuoiHopError = "Năm sinh không hợp lệ (1900–2100)"
            return
        }

        // Convert solar birth year to lunar year
        let lunarYear = year // Approx — lunar year shifts around Feb
        let zodiacIdx = (lunarYear + 8) % 12  // 0=Tý
        let canIdx = (lunarYear + 6) % 10
        tuoiHopUserZodiac = Self.zodiacNames[zodiacIdx]
        tuoiHopUserCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunarYear)

        var items: [ZodiacCompatItem] = []
        for i in 0..<12 {
            let score = Self.compatMatrix[zodiacIdx][i]
            let label: String
            switch score {
            case 85...100: label = "Rất hợp"
            case 70..<85:  label = "Hợp"
            case 50..<70:  label = "Bình thường"
            default:       label = "Khắc"
            }
            let reason: String
            switch score {
            case 85...100: reason = "Tam hợp / Lục hợp — rất tốt"
            case 70..<85:  reason = "Tương sinh — hợp nhau"
            case 50..<70:  reason = "Bình hoà — ổn định"
            default:       reason = "Tứ hành xung / Tương khắc"
            }
            items.append(ZodiacCompatItem(
                zodiac: Self.zodiacNames[i],
                zodiacEmoji: Self.zodiacEmojis[i],
                compatScore: score,
                compatLabel: label,
                reason: reason
            ))
        }
        // Sort: best compatibility first
        tuoiHopResults = items.sorted { $0.compatScore > $1.compatScore }
    }

    // ── Lunar Conversion ──
    func convertLunarToSolar() {
        let parts = lunarInput.split(separator: "/").compactMap { Int($0) }
        guard parts.count >= 2 else {
            lunarOutput = ""
            lunarSummary = ""
            return
        }
        let day = parts[0], month = parts[1]
        let year = parts.count >= 3 ? parts[2] : Calendar.current.component(.year, from: Date())

        let solar = LunarCalendarUtil.convertLunar2Solar(
            lunarDay: day, lunarMonth: month, lunarYear: year, lunarLeap: 0, timeZone: 7.0
        )
        if solar.0 > 0 && solar.1 > 0 {
            lunarOutput = String(format: "%02d/%02d", solar.0, solar.1)
            lunarSummary = "\(day)/\(month) Âm → \(String(format: "%02d/%02d/%d", solar.0, solar.1, solar.2)) Dương lịch"
        } else {
            lunarOutput = "Không hợp lệ"
            lunarSummary = ""
        }
    }

    // ── Recent searches (persisted in UserDefaults) ──
    private let recentKey = "lichso_recent_searches"

    func loadRecent() {
        recentSearches = UserDefaults.standard.stringArray(forKey: recentKey) ?? []
    }

    func saveToRecent(_ text: String) {
        let t = text.trimmingCharacters(in: .whitespaces)
        guard !t.isEmpty else { return }
        recentSearches.removeAll { $0 == t }
        recentSearches.insert(t, at: 0)
        if recentSearches.count > 10 { recentSearches = Array(recentSearches.prefix(10)) }
        UserDefaults.standard.set(recentSearches, forKey: recentKey)
    }

    func removeRecent(_ text: String) {
        recentSearches.removeAll { $0 == text }
        UserDefaults.standard.set(recentSearches, forKey: recentKey)
    }

    func clearRecent() {
        recentSearches = []
        UserDefaults.standard.removeObject(forKey: recentKey)
    }
}
