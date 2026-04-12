import SwiftUI
import Combine

// ═══════════════════════════════════════════
// GoodDaysViewModel — Compute good/bad days for a month
// ═══════════════════════════════════════════

enum DayFilter: String, CaseIterable {
    case all = "Tháng này"
    case khaiTruong = "Khai trương"
    case xayNha = "Xây nhà"
    case cuoiHoi = "Cưới hỏi"
    case xuatHanh = "Xuất hành"
    case nhapTrach = "Nhập trạch"
    case anTang = "An táng"

    var icon: String {
        switch self {
        case .all: return "calendar"
        case .khaiTruong: return "bag"
        case .xayNha: return "house"
        case .cuoiHoi: return "heart.fill"
        case .xuatHanh: return "airplane.departure"
        case .nhapTrach: return "house.circle"
        case .anTang: return "leaf"
        }
    }

    /// Keywords to match in nenLam activities
    var keywords: [String] {
        switch self {
        case .all: return []
        case .khaiTruong: return ["Khai trương"]
        case .xayNha: return ["Động thổ", "Xây dựng", "Dựng cột", "xây dựng", "động thổ"]
        case .cuoiHoi: return ["Cưới hỏi"]
        case .xuatHanh: return ["Xuất hành"]
        case .nhapTrach: return ["Nhập trạch"]
        case .anTang: return ["An táng"]
        }
    }
}

enum DayQuality {
    case good, bad, neutral

    var label: String {
        switch self {
        case .good: return "Hoàng Đạo"
        case .bad: return "Hắc Đạo"
        case .neutral: return "Bình thường"
        }
    }

    var icon: String {
        switch self {
        case .good: return "✦"
        case .bad: return "✕"
        case .neutral: return "◈"
        }
    }
}

struct GoodDayItem: Identifiable {
    let id = UUID()
    let dayInfo: DayInfo
    let quality: DayQuality
    let goodActivities: [String]
    let badActivities: [String]
}

struct GoodDaysUiState {
    var year: Int
    var month: Int
    var days: [GoodDayItem]
    var filteredDays: [GoodDayItem]
    var selectedFilter: DayFilter
    var isLoading: Bool

    init() {
        let now = Date()
        let cal = Calendar.current
        year = cal.component(.year, from: now)
        month = cal.component(.month, from: now)
        days = []
        filteredDays = []
        selectedFilter = .all
        isLoading = true
    }
}

@MainActor
class GoodDaysViewModel: ObservableObject {
    @Published var state = GoodDaysUiState()
    private let provider = DayInfoProvider.shared

    init() {
        loadMonth()
    }

    func loadMonth() {
        state.isLoading = true
        let year = state.year
        let month = state.month
        let cal = Calendar.current

        guard let firstDay = cal.date(from: DateComponents(year: year, month: month, day: 1)),
              let range = cal.range(of: .day, in: .month, for: firstDay) else { return }

        var items: [GoodDayItem] = []

        for day in range {
            let info = provider.getDayInfo(dd: day, mm: month, yy: year)

            let quality: DayQuality
            switch info.dayRating.label {
            case "Rất tốt", "Tốt":
                quality = info.activities.isXauDay ? .neutral : .good
            case "Xấu":
                quality = .bad
            default:
                quality = info.activities.isXauDay ? .bad : .neutral
            }

            items.append(GoodDayItem(
                dayInfo: info,
                quality: quality,
                goodActivities: info.activities.nenLam,
                badActivities: info.activities.khongNen
            ))
        }

        state.days = items
        applyFilter()
        state.isLoading = false
    }

    func selectFilter(_ filter: DayFilter) {
        state.selectedFilter = filter
        applyFilter()
    }

    func nextMonth() {
        if state.month == 12 {
            state.month = 1
            state.year += 1
        } else {
            state.month += 1
        }
        loadMonth()
    }

    func prevMonth() {
        if state.month == 1 {
            state.month = 12
            state.year -= 1
        } else {
            state.month -= 1
        }
        loadMonth()
    }

    private func applyFilter() {
        let filter = state.selectedFilter
        if filter == .all {
            state.filteredDays = state.days
        } else {
            state.filteredDays = state.days.filter { item in
                let combined = item.goodActivities.joined(separator: ", ")
                return filter.keywords.contains { keyword in
                    combined.localizedCaseInsensitiveContains(keyword)
                }
            }
        }
    }
}
