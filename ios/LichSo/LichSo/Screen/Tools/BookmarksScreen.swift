import SwiftUI
import SwiftData

// ══════════════════════════════════════════
// Models (ported from Android BookmarksViewModel)
// ══════════════════════════════════════════

enum BookmarkCategory: String, CaseIterable {
    case all, goodDay, holiday, personal

    var label: String {
        switch self {
        case .all: return "TẤT CẢ"
        case .goodDay: return "NGÀY TỐT"
        case .holiday: return "LỄ / GIỖ"
        case .personal: return "CÁ NHÂN"
        }
    }

    var icon: String {
        switch self {
        case .all: return "calendar"
        case .goodDay: return "sparkles"
        case .holiday: return "party.popper"
        case .personal: return "briefcase"
        }
    }

    var color: Color {
        switch self {
        case .all: return LSTheme.primary
        case .goodDay: return Color(hex: "2E7D32")
        case .holiday: return Color(hex: "E65100")
        case .personal: return Color(hex: "1565C0")
        }
    }
}

enum BookmarkFilterChip: String, CaseIterable {
    case all, wedding, business, memorial, travel, health

    var label: String {
        switch self {
        case .all: return "Tất cả"
        case .wedding: return "Cưới hỏi"
        case .business: return "Kinh doanh"
        case .memorial: return "Giỗ chạp"
        case .travel: return "Du lịch"
        case .health: return "Sức khỏe"
        }
    }

    var icon: String {
        switch self {
        case .all: return "bookmark"
        case .wedding: return "heart.fill"
        case .business: return "building.2"
        case .memorial: return "flame"
        case .travel: return "airplane"
        case .health: return "cross.case"
        }
    }

    var keywords: [String] {
        switch self {
        case .all: return []
        case .wedding: return ["cưới", "hỏi", "đính hôn", "wedding"]
        case .business: return ["khai trương", "công ty", "kinh doanh", "business"]
        case .memorial: return ["giỗ", "cúng", "tưởng niệm", "memorial"]
        case .travel: return ["du lịch", "travel", "nghỉ"]
        case .health: return ["khám", "sức khỏe", "bệnh viện", "health"]
        }
    }
}

enum BookmarkSortMode {
    case dateDesc, dateAsc, name

    var label: String {
        switch self {
        case .dateDesc: return "Ngày (mới → cũ)"
        case .dateAsc: return "Ngày (cũ → mới)"
        case .name: return "Tên nhãn"
        }
    }
}

struct BookmarkDisplayItem: Identifiable {
    let entity: BookmarkEntity
    let dayOfWeek: String
    let lunarDay: Int
    let lunarMonth: Int
    let lunarMonthName: String
    let dayCanChi: String
    let dayRating: String
    let solarHoliday: String?
    let lunarHoliday: String?
    let daysUntil: Int
    let category: BookmarkCategory

    var id: Int64 { entity.id }
    var isUpcoming: Bool { daysUntil > 0 }
    var isToday: Bool { daysUntil == 0 }
    var isPast: Bool { daysUntil < 0 }
}

// ══════════════════════════════════════════
// Screen
// ══════════════════════════════════════════

struct BookmarksScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \BookmarkEntity.createdAt, order: .reverse) private var allBookmarks: [BookmarkEntity]

    @State private var selectedCategory: BookmarkCategory = .all
    @State private var selectedChip: BookmarkFilterChip = .all
    @State private var searchQuery = ""
    @State private var sortMode: BookmarkSortMode = .dateDesc
    @State private var showDeleteAllConfirm = false

    private let dayOfWeekNames = ["Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"]

    // ── Derived data ──

    private var enriched: [BookmarkDisplayItem] {
        let today = Calendar.current.startOfDay(for: Date())
        return allBookmarks.map { entity in enrich(entity, today: today) }
    }

    private var totalCount: Int { enriched.count }
    private var goodDayCount: Int { enriched.filter { $0.category == .goodDay }.count }
    private var holidayCount: Int { enriched.filter { $0.category == .holiday }.count }
    private var personalCount: Int { enriched.filter { $0.category == .personal }.count }
    private var upcomingCount: Int { enriched.filter { $0.isUpcoming }.count }

    private var filtered: [BookmarkDisplayItem] {
        var list = enriched

        if selectedCategory != .all {
            list = list.filter { $0.category == selectedCategory }
        }

        if selectedChip != .all {
            let kw = selectedChip.keywords
            if !kw.isEmpty {
                list = list.filter { item in
                    let text = "\(item.entity.label) \(item.entity.note) \(item.solarHoliday ?? "") \(item.lunarHoliday ?? "")".lowercased()
                    return kw.contains { text.contains($0) }
                }
            }
        }

        let q = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
        if !q.isEmpty {
            list = list.filter { item in
                let text = "\(item.entity.label) \(item.entity.note) \(item.solarHoliday ?? "") \(item.lunarHoliday ?? "") \(item.dayOfWeek) \(item.dayCanChi) \(item.entity.solarDay)/\(item.entity.solarMonth)/\(item.entity.solarYear)".lowercased()
                return text.contains(q)
            }
        }

        switch sortMode {
        case .dateDesc:
            list.sort { ($0.entity.solarYear, $0.entity.solarMonth, $0.entity.solarDay) > ($1.entity.solarYear, $1.entity.solarMonth, $1.entity.solarDay) }
        case .dateAsc:
            list.sort { ($0.entity.solarYear, $0.entity.solarMonth, $0.entity.solarDay) < ($1.entity.solarYear, $1.entity.solarMonth, $1.entity.solarDay) }
        case .name:
            list.sort { $0.entity.label.lowercased() < $1.entity.label.lowercased() }
        }
        return list
    }

    private func enrich(_ entity: BookmarkEntity, today: Date) -> BookmarkDisplayItem {
        let info = DayInfoProvider.shared.getDayInfo(dd: entity.solarDay, mm: entity.solarMonth, yy: entity.solarYear)
        let hasHoliday = info.solarHoliday != nil || info.lunarHoliday != nil
        let rating = info.dayRating.label

        let category: BookmarkCategory
        if hasHoliday {
            category = .holiday
        } else if rating == "Rất tốt" || rating == "Tốt" {
            category = .goodDay
        } else {
            category = .personal
        }

        var comps = DateComponents()
        comps.year = entity.solarYear
        comps.month = entity.solarMonth
        comps.day = entity.solarDay
        let bookmarkDate = Calendar.current.date(from: comps) ?? today
        let daysUntil = Calendar.current.dateComponents([.day], from: today, to: Calendar.current.startOfDay(for: bookmarkDate)).day ?? 0

        return BookmarkDisplayItem(
            entity: entity,
            dayOfWeek: dayOfWeekNames.indices.contains(info.dayOfWeekIndex) ? dayOfWeekNames[info.dayOfWeekIndex] : info.dayOfWeek,
            lunarDay: info.lunar.day,
            lunarMonth: info.lunar.month,
            lunarMonthName: info.lunar.monthName,
            dayCanChi: info.dayCanChi,
            dayRating: rating,
            solarHoliday: info.solarHoliday,
            lunarHoliday: info.lunarHoliday,
            daysUntil: daysUntil,
            category: category
        )
    }

    private var shareText: String {
        var lines = ["Ngày đã lưu — Lịch Sổ", "Tổng: \(filtered.count) ngày", ""]
        for (i, item) in filtered.enumerated() {
            let d = String(format: "%02d/%02d/%d", item.entity.solarDay, item.entity.solarMonth, item.entity.solarYear)
            let label = item.entity.label.isEmpty ? (item.solarHoliday ?? item.lunarHoliday ?? "Ngày đã lưu") : item.entity.label
            lines.append("\(i + 1). \(d) — \(label)")
            lines.append("   \(item.lunarDay)/\(item.lunarMonth) ÂL · \(item.dayCanChi) · \(item.dayRating)")
            if !item.entity.note.isEmpty { lines.append("   Ghi chú: \(item.entity.note)") }
        }
        lines.append("")
        lines.append("— Lịch Sổ App")
        return lines.joined(separator: "\n")
    }

    // ── Body ──

    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()

            VStack(spacing: 0) {
                header
                statsRow
                filterChipsRow
                searchRow

                if enriched.isEmpty {
                    emptyState
                } else if filtered.isEmpty {
                    emptyFilterState
                } else {
                    bookmarkList
                }
            }
        }
        .navigationBarHidden(true)
        .confirmationDialog("Xóa tất cả ngày đã lưu?", isPresented: $showDeleteAllConfirm, titleVisibility: .visible) {
            Button("Xóa tất cả", role: .destructive) { deleteAll() }
            Button("Hủy", role: .cancel) {}
        }
    }

    // ── Header ──

    private var header: some View {
        HStack(spacing: 4) {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Quay lại")

            VStack(alignment: .leading, spacing: 2) {
                Text("Ngày đã lưu")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                Text("\(totalCount) ngày đã lưu · \(upcomingCount) sắp tới")
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textTertiary)
            }

            Spacer()

            Button {
                Haptics.selection()
                sortMode = sortMode == .dateDesc ? .dateAsc : (sortMode == .dateAsc ? .name : .dateDesc)
            } label: {
                Image(systemName: "arrow.up.arrow.down")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Sắp xếp")

            Menu {
                Picker("Sắp xếp", selection: $sortMode) {
                    Text(BookmarkSortMode.dateDesc.label).tag(BookmarkSortMode.dateDesc)
                    Text(BookmarkSortMode.dateAsc.label).tag(BookmarkSortMode.dateAsc)
                    Text(BookmarkSortMode.name.label).tag(BookmarkSortMode.name)
                }
                if !filtered.isEmpty {
                    ShareLink(item: shareText) {
                        Label("Chia sẻ danh sách", systemImage: "square.and.arrow.up")
                    }
                }
                Button(role: .destructive) { showDeleteAllConfirm = true } label: {
                    Label("Xóa tất cả", systemImage: "trash")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Tùy chọn")
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 8)
        .background(LSTheme.surfaceContainer)
    }

    // ── Stats row ──

    private var statsRow: some View {
        HStack(spacing: 8) {
            statCard(.all, count: totalCount)
            statCard(.goodDay, count: goodDayCount)
            statCard(.holiday, count: holidayCount)
            statCard(.personal, count: personalCount)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }

    private func statCard(_ cat: BookmarkCategory, count: Int) -> some View {
        let isActive = selectedCategory == cat
        return VStack(spacing: 2) {
            Image(systemName: cat.icon)
                .font(.system(size: 20))
                .foregroundColor(isActive ? LSTheme.primary : cat.color)
            Text("\(count)")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(isActive ? LSTheme.primary : cat.color)
            Text(cat.label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(LSTheme.outline)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .padding(.horizontal, 8)
        .background(isActive ? LSTheme.primaryContainer : LSTheme.surfaceContainer)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isActive ? LSTheme.primary : LSTheme.outlineVariant, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .contentShape(Rectangle())
        .onTapGesture { selectedCategory = cat }
    }

    // ── Filter chips ──

    private var filterChipsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(BookmarkFilterChip.allCases, id: \.self) { chip in
                    let isActive = chip == selectedChip
                    HStack(spacing: 5) {
                        Image(systemName: chip.icon)
                            .font(.system(size: 13))
                            .foregroundColor(isActive ? .white : LSTheme.textSecondary)
                        Text(chip.label)
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(isActive ? .white : LSTheme.textSecondary)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 7)
                    .background(isActive ? LSTheme.primary : LSTheme.surfaceContainer)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(isActive ? LSTheme.primary : LSTheme.outlineVariant, lineWidth: 1.5)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .contentShape(Rectangle())
                    .onTapGesture { selectedChip = chip }
                }
            }
            .padding(.horizontal, 16)
        }
        .padding(.bottom, 10)
    }

    // ── Search ──

    private var searchRow: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .foregroundColor(LSTheme.textTertiary)
            TextField("Tìm theo nhãn, ngày, can chi...", text: $searchQuery)
                .font(.system(size: 14))
                .foregroundColor(LSTheme.textPrimary)
            if !searchQuery.isEmpty {
                Button { searchQuery = "" } label: {
                    Image(systemName: "xmark.circle.fill").foregroundColor(LSTheme.textTertiary)
                }
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .padding(.horizontal, 16)
        .padding(.bottom, 10)
    }

    // ── List ──

    private var bookmarkList: some View {
        let upcoming = filtered.filter { $0.isUpcoming }
        let todayItems = filtered.filter { $0.isToday }
        let past = filtered.filter { $0.isPast }
        let featured = upcoming.min { $0.daysUntil < $1.daysUntil }

        return ScrollView {
            LazyVStack(spacing: 10) {
                if let featured {
                    featuredCard(featured)
                }
                if !upcoming.isEmpty {
                    sectionDivider("Sắp tới", icon: "pin")
                    ForEach(upcoming) { card(for: $0) }
                }
                if !todayItems.isEmpty {
                    sectionDivider("Hôm nay", icon: "mappin.and.ellipse")
                    ForEach(todayItems) { card(for: $0) }
                }
                if !past.isEmpty {
                    sectionDivider("Đã qua", icon: "clock.arrow.circlepath")
                    ForEach(past) { card(for: $0, dimmed: true) }
                }
                Spacer().frame(height: 40)
            }
            .padding(.horizontal, 16)
        }
    }

    private func sectionDivider(_ text: String, icon: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(LSTheme.outline)
            Text(text)
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(LSTheme.outline)
            Rectangle().fill(LSTheme.outlineVariant).frame(height: 1)
        }
        .padding(.vertical, 8)
    }

    private func featuredCard(_ item: BookmarkDisplayItem) -> some View {
        let title = item.solarHoliday ?? item.lunarHoliday ?? (item.entity.label.isEmpty
            ? String(format: "%02d/%02d/%d", item.entity.solarDay, item.entity.solarMonth, item.entity.solarYear)
            : item.entity.label)
        return HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(LinearGradient(colors: [LSTheme.gold, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 52, height: 52)
                Image(systemName: "star.fill").foregroundColor(.white).font(.system(size: 22))
            }
            VStack(alignment: .leading, spacing: 4) {
                Text("SẮP TỚI GẦN NHẤT")
                    .font(.system(size: 9, weight: .bold))
                    .foregroundColor(Color(hex: "B8860B"))
                Text(title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(hex: "5D4037"))
                    .lineLimit(1)
                Text(daysUntilText(item.daysUntil) + " · " + String(format: "%02d/%02d", item.entity.solarDay, item.entity.solarMonth))
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "8D6E63"))
            }
            Spacer()
        }
        .padding(16)
        .background(LinearGradient(colors: [Color(hex: "FFF8E1"), Color(hex: "FFECB3")], startPoint: .leading, endPoint: .trailing))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color(hex: "FFE082"), lineWidth: 1.5))
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func card(for item: BookmarkDisplayItem, dimmed: Bool = false) -> some View {
        let title = item.entity.label.isEmpty
            ? (item.solarHoliday ?? item.lunarHoliday ?? "Ngày đã lưu")
            : item.entity.label
        return HStack(spacing: 12) {
            // Date block
            VStack(spacing: 0) {
                Text("\(item.entity.solarDay)")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(item.category.color)
                Text("Th\(item.entity.solarMonth)")
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(LSTheme.textTertiary)
            }
            .frame(width: 48)

            Rectangle().fill(item.category.color).frame(width: 3).clipShape(Capsule())

            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .lineLimit(1)
                Text("\(item.dayOfWeek) · \(item.lunarDay)/\(item.lunarMonth) ÂL · \(item.dayCanChi)")
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineLimit(1)
                HStack(spacing: 6) {
                    tagChip(item.dayRating, color: item.category.color)
                    if let h = item.solarHoliday ?? item.lunarHoliday {
                        tagChip(h, color: Color(hex: "E65100"))
                    }
                }
            }
            Spacer()
            if item.isUpcoming {
                Text(daysUntilText(item.daysUntil))
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(LSTheme.primary)
            }
        }
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .opacity(dimmed ? 0.6 : 1)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(LSTheme.outlineVariant, lineWidth: 1))
        .contextMenu {
            Button(role: .destructive) { delete(item) } label: {
                Label("Xóa đánh dấu", systemImage: "trash")
            }
        }
    }

    private func tagChip(_ text: String, color: Color) -> some View {
        Text(text)
            .font(.system(size: 10, weight: .semibold))
            .foregroundColor(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color.opacity(0.12))
            .clipShape(Capsule())
    }

    private func daysUntilText(_ days: Int) -> String {
        if days == 0 { return "Hôm nay" }
        if days == 1 { return "Ngày mai" }
        if days > 0 { return "Còn \(days) ngày" }
        return "\(abs(days)) ngày trước"
    }

    // ── Empty states ──

    private var emptyState: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "bookmark.slash")
                .font(.system(size: 48))
                .foregroundColor(LSTheme.textTertiary)
            Text("Chưa có ngày nào được lưu")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
            Text("Mở lịch và đánh dấu những ngày quan trọng để xem lại tại đây.")
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var emptyFilterState: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "line.3.horizontal.decrease.circle")
                .font(.system(size: 44))
                .foregroundColor(LSTheme.textTertiary)
            Text("Không có kết quả phù hợp")
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
            Button("Xóa bộ lọc") {
                selectedCategory = .all
                selectedChip = .all
                searchQuery = ""
            }
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(LSTheme.primary)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // ── Actions ──

    private func delete(_ item: BookmarkDisplayItem) {
        modelContext.delete(item.entity)
        try? modelContext.save()
    }

    private func deleteAll() {
        for item in allBookmarks { modelContext.delete(item) }
        try? modelContext.save()
    }
}
