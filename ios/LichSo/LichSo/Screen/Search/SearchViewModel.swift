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
}

@MainActor
class SearchViewModel: ObservableObject {
    @Published var query = ""
    @Published var results: [SearchResult] = []
    @Published var recentSearches: [String] = []
    @Published var lunarInput = ""
    @Published var lunarOutput = ""
    @Published var lunarSummary = ""

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
                found.append(SearchResult(
                    title: "\(day)/\(month)/\(year) Dương lịch",
                    subtitle: "Âm lịch: \(lunar.lunarDay)/\(lunar.lunarMonth)/\(lunar.lunarYear)",
                    type: .date,
                    highlightTerm: q
                ))
            }
        }

        results = found
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
