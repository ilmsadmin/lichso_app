import SwiftUI

// ══════════════════════════════════════════
// Models (ported from Android TemplatesViewModel)
// ══════════════════════════════════════════

enum TemplateTab: CaseIterable {
    case all, ceremony, events, fengShui
    var label: String {
        switch self {
        case .all: return "Tất cả"
        case .ceremony: return "Lễ nghi"
        case .events: return "Sự kiện"
        case .fengShui: return "Phong thủy"
        }
    }
}

struct TemplateItem: Identifiable {
    let id: Int
    let sfIcon: String
    let title: String
    let description: String
    let category: String        // "Lễ nghi" | "Kinh doanh" | "Phong thủy"
    let tags: [String]
    let queryKeyword: String
}

struct TemplateGoodDay: Identifiable {
    let id = UUID()
    let solarStr: String
    let lunarStr: String
    let dayCanChi: String
    let dayOfWeek: String
    let gioHoangDao: String
    let daysFromNow: Int
}

// Glyphs mirror Android ChatIcons so the generated summary text is identical.
private enum TIcon {
    static let calendar = "◈", lunar = "☽", clock = "◷", star = "✦"
    static let canchi = "⊕", compass = "◎", fortune = "⊛", joy = "❖"
    static let sparkle = "⟡", check = "▪", cross = "▫", warning = "△"
}

enum TemplateData {
    static let all: [TemplateItem] = [
        TemplateItem(id: 1, sfIcon: "heart.fill", title: "Ngày cưới hỏi", description: "Chọn ngày tốt cho lễ cưới, ăn hỏi, dạm ngõ theo tuổi cô dâu chú rể", category: "Lễ nghi", tags: ["Cưới hỏi", "Phong thủy"], queryKeyword: "cuoi"),
        TemplateItem(id: 2, sfIcon: "house.fill", title: "Động thổ xây nhà", description: "Xem ngày khởi công, đổ mái, cất nóc theo phong thủy", category: "Phong thủy", tags: ["Xây dựng", "Nhà ở"], queryKeyword: "dongTho"),
        TemplateItem(id: 3, sfIcon: "storefront.fill", title: "Khai trương cửa hàng", description: "Ngày tốt khai trương, chọn hướng tài lộc, giờ hoàng đạo", category: "Kinh doanh", tags: ["Khai trương", "Kinh doanh"], queryKeyword: "khaiTruong"),
        TemplateItem(id: 4, sfIcon: "car.fill", title: "Mua xe · Nhận xe", description: "Ngày tốt mua xe, đăng ký, nhận xe mới theo phong thủy", category: "Phong thủy", tags: ["Mua bán", "Phương tiện"], queryKeyword: "muaXe"),
        TemplateItem(id: 5, sfIcon: "airplane", title: "Xuất hành · Du lịch", description: "Ngày tốt xuất hành, hướng đi tốt, giờ khởi hành", category: "Kinh doanh", tags: ["Xuất hành", "Du lịch"], queryKeyword: "xuatHanh"),
        TemplateItem(id: 6, sfIcon: "graduationcap.fill", title: "Nhập học · Thi cử", description: "Ngày tốt khai giảng, thi cử, nộp đơn xin học", category: "Lễ nghi", tags: ["Học hành", "Thi cử"], queryKeyword: "thiCu"),
        TemplateItem(id: 7, sfIcon: "figure.and.child.holdinghands", title: "Đầy tháng · Thôi nôi", description: "Chọn ngày lễ đầy tháng, thôi nôi cho bé", category: "Lễ nghi", tags: ["Gia đình", "Lễ nghi"], queryKeyword: "leNghi"),
        TemplateItem(id: 8, sfIcon: "building.columns.fill", title: "Cúng giỗ · Lễ Tết", description: "Ngày cúng giỗ, lễ Tết, rằm, mùng 1 theo âm lịch", category: "Lễ nghi", tags: ["Cúng lễ", "Truyền thống"], queryKeyword: "cungGio"),
    ]

    static func filtered(_ tab: TemplateTab) -> [TemplateItem] {
        switch tab {
        case .all: return all
        case .ceremony: return all.filter { $0.category == "Lễ nghi" }
        case .events: return all.filter { $0.category == "Kinh doanh" }
        case .fengShui: return all.filter { $0.category == "Phong thủy" }
        }
    }

    static func summary(for t: TemplateItem, info: DayInfo) -> String {
        var lines: [String] = []
        let gio3 = info.gioHoangDao.prefix(3).map { "\($0.name) (\($0.time))" }.joined(separator: ", ")
        let gio4 = info.gioHoangDao.prefix(4).map { "\($0.name) (\($0.time))" }.joined(separator: ", ")

        lines.append("\(TIcon.calendar) Hôm nay: \(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy) (\(info.dayOfWeek))")
        lines.append("\(TIcon.lunar) Âm lịch: \(info.lunar.day)/\(info.lunar.month) · \(info.yearCanChi)")
        lines.append("\(TIcon.canchi) Ngày: \(info.dayCanChi)")
        lines.append("")

        let nen = info.activities.nenLam
        func has(_ kws: [String]) -> Bool { nen.contains { n in kws.contains { n.contains($0) } } }

        switch t.queryKeyword {
        case "cuoi":
            let ok = has(["Cưới", "Hôn", "hỏi"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP cho việc cưới hỏi, ăn hỏi, dạm ngõ."
                : "\(TIcon.warning) Hôm nay KHÔNG LÝ TƯỞNG cho cưới hỏi.")
            lines.append("")
            lines.append("\(TIcon.clock) Giờ đẹp: \(gio3)")
            lines.append("\(TIcon.compass) Hướng Hỷ Thần: \(info.huong.hyThan)")
            lines.append("\(TIcon.sparkle) Nên đón dâu/rước lễ theo hướng \(info.huong.hyThan).")
        case "dongTho":
            let ok = has(["Động thổ", "Xây", "Sửa chữa"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP để động thổ, khởi công, sửa chữa nhà."
                : "\(TIcon.warning) Hôm nay KHÔNG NÊN động thổ.")
            lines.append("")
            lines.append("\(TIcon.clock) Giờ hoàng đạo: \(gio3)")
            lines.append("\(TIcon.compass) Hướng tốt: \(info.huong.thanTai)")
            lines.append("\(TIcon.warning) Tránh hướng: \(info.huong.hungThan)")
        case "khaiTruong":
            let ok = has(["Khai trương", "Mở cửa", "Giao dịch"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP để khai trương!"
                : "\(TIcon.warning) Hôm nay KHÔNG LÝ TƯỞNG để khai trương.")
            lines.append("")
            lines.append("\(TIcon.clock) Giờ hoàng đạo: \(gio4)")
            lines.append("\(TIcon.fortune) Hướng tài lộc: \(info.huong.thanTai)")
            lines.append("\(TIcon.sparkle) Đặt quầy thu ngân hướng \(info.huong.thanTai) để đón vượng khí.")
        case "muaXe":
            let ok = has(["Mua", "Giao dịch", "Nhận"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP để mua xe, nhận xe mới!"
                : "\(TIcon.warning) Hôm nay chưa lý tưởng để mua xe.")
            lines.append("")
            lines.append("\(TIcon.clock) Giờ tốt: \(gio3)")
            lines.append("\(TIcon.fortune) Hướng Thần Tài: \(info.huong.thanTai)")
        case "xuatHanh":
            let ok = has(["Xuất hành", "Du lịch", "Di chuyển"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP để xuất hành, du lịch!"
                : "\(TIcon.warning) Cần cân nhắc khi xuất hành hôm nay.")
            lines.append("")
            lines.append("\(TIcon.compass) Hướng xuất hành tốt: \(info.huong.thanTai)")
            let first = info.gioHoangDao.first.map { "\($0.name) (\($0.time))" } ?? "N/A"
            lines.append("\(TIcon.clock) Giờ khởi hành: \(first)")
            lines.append("\(TIcon.warning) Tránh hướng: \(info.huong.hungThan)")
        case "thiCu":
            let ok = has(["Học", "Nhập", "Khai"])
            lines.append(ok && !info.activities.isXauDay
                ? "\(TIcon.check) Hôm nay PHÙ HỢP cho việc học, thi cử, nhập học!"
                : "\(TIcon.warning) Hôm nay chưa lý tưởng cho thi cử.")
            lines.append("")
            lines.append("\(TIcon.clock) Giờ hoàng đạo: \(gio3)")
            lines.append("\(TIcon.sparkle) Hướng tốt cho văn xương: \(info.huong.hyThan)")
        case "leNghi", "cungGio":
            lines.append("\(TIcon.lunar) Âm lịch: \(info.lunar.day)/\(info.lunar.month) \(info.yearCanChi)")
            if info.isRam { lines.append("\(TIcon.star) Hôm nay Rằm — ngày lễ lớn") }
            if info.isMung1 { lines.append("\(TIcon.star) Hôm nay Mùng 1 — ngày lễ đầu tháng") }
            lines.append("")
            lines.append("\(TIcon.clock) Giờ tốt cúng lễ: \(gio3)")
            lines.append("\(TIcon.compass) Hướng đặt bàn thờ tốt: \(info.huong.hyThan)")
            if let h = info.lunarHoliday { lines.append("\(TIcon.joy) Lễ: \(h)") }
        default:
            lines.append("\(TIcon.check) Nên: \(info.activities.nenLam.prefix(5).joined(separator: ", "))")
            lines.append("\(TIcon.cross) Không nên: \(info.activities.khongNen.prefix(5).joined(separator: ", "))")
        }

        if info.activities.isXauDay {
            lines.append("")
            let suffix = (info.activities.isNguyetKy ? " (Nguyệt Kỵ)" : "") + (info.activities.isTamNuong ? " (Tam Nương)" : "")
            lines.append("\(TIcon.warning) LƯU Ý: Hôm nay là ngày xấu\(suffix).")
        }
        return lines.joined(separator: "\n").trimmingCharacters(in: .whitespacesAndNewlines)
    }

    static func findGoodDays(for t: TemplateItem) -> [TemplateGoodDay] {
        var results: [TemplateGoodDay] = []
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())

        for i in 0...29 {
            guard let date = cal.date(byAdding: .day, value: i, to: today) else { continue }
            let comps = cal.dateComponents([.day, .month, .year], from: date)
            guard let d = comps.day, let m = comps.month, let y = comps.year else { continue }
            let info = DayInfoProvider.shared.getDayInfo(dd: d, mm: m, yy: y)
            if info.activities.isXauDay { continue }

            let nen = info.activities.nenLam
            func has(_ kws: [String]) -> Bool { nen.contains { n in kws.contains { n.contains($0) } } }

            let isGood: Bool
            switch t.queryKeyword {
            case "cuoi": isGood = has(["Cưới", "Hôn", "hỏi"])
            case "dongTho": isGood = has(["Động thổ", "Xây", "Sửa chữa"])
            case "khaiTruong": isGood = has(["Khai trương", "Mở cửa", "Giao dịch"])
            case "muaXe": isGood = has(["Mua", "Giao dịch", "Nhận"])
            case "xuatHanh": isGood = has(["Xuất hành", "Du lịch", "Di chuyển"])
            case "thiCu": isGood = has(["Học", "Nhập", "Khai"])
            case "leNghi": isGood = has(["Lễ", "Cầu phúc", "Gia tiên"])
            case "cungGio": isGood = has(["Cúng", "Lễ", "Cầu phúc"])
            default: isGood = true
            }

            if isGood {
                results.append(TemplateGoodDay(
                    solarStr: "\(d)/\(m)/\(y)",
                    lunarStr: "\(info.lunar.day)/\(info.lunar.month) âm",
                    dayCanChi: info.dayCanChi,
                    dayOfWeek: info.dayOfWeek,
                    gioHoangDao: info.gioHoangDao.prefix(3).map { "\($0.name) (\($0.time))" }.joined(separator: ", "),
                    daysFromNow: i
                ))
            }
        }
        return Array(results.prefix(10))
    }
}

// ══════════════════════════════════════════
// Screen
// ══════════════════════════════════════════

struct TemplatesScreen: View {
    @State private var selectedTab: TemplateTab = .all
    @State private var detail: TemplateItem? = nil

    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Text("Mẫu tra cứu")
                        .font(.system(size: 22, weight: .bold, design: .serif))
                        .foregroundColor(LSTheme.gold2)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(TemplateTab.allCases, id: \.self) { tab in
                                tabChip(tab)
                            }
                        }
                        .padding(.horizontal, 20)
                    }

                    VStack(spacing: 10) {
                        ForEach(TemplateData.filtered(selectedTab)) { item in
                            templateCard(item)
                                .onTapGesture { detail = item }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 14)
                    .padding(.bottom, 24)
                }
            }
        }
        .sheet(item: $detail) { item in
            TemplateDetailView(template: item)
        }
    }

    private func tabChip(_ tab: TemplateTab) -> some View {
        let active = tab == selectedTab
        return Text(tab.label)
            .font(.system(size: 13, weight: .semibold))
            .foregroundColor(active ? .white : LSTheme.textSecondary)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(active ? LSTheme.primary : LSTheme.surfaceContainer)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(active ? LSTheme.primary : LSTheme.outlineVariant, lineWidth: 1))
            .onTapGesture { selectedTab = tab }
    }

    private func templateCard(_ item: TemplateItem) -> some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(LSTheme.primary.opacity(0.12))
                    .frame(width: 50, height: 50)
                Image(systemName: item.sfIcon)
                    .font(.system(size: 22))
                    .foregroundColor(LSTheme.primary)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(item.title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(item.description)
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineLimit(2)
                HStack(spacing: 6) {
                    ForEach(item.tags, id: \.self) { tag in
                        Text(tag)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(LSTheme.gold2)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(LSTheme.gold2.opacity(0.12))
                            .clipShape(Capsule())
                    }
                }
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textTertiary)
        }
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(LSTheme.outlineVariant, lineWidth: 1))
        .contentShape(Rectangle())
    }
}

// ══════════════════════════════════════════
// Detail
// ══════════════════════════════════════════

private struct TemplateDetailView: View {
    let template: TemplateItem
    @Environment(\.dismiss) private var dismiss

    private var info: DayInfo {
        let c = Calendar.current.dateComponents([.day, .month, .year], from: Date())
        return DayInfoProvider.shared.getDayInfo(dd: c.day ?? 1, mm: c.month ?? 1, yy: c.year ?? 2026)
    }
    @State private var goodDays: [TemplateGoodDay] = []

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Summary card
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 10) {
                            Image(systemName: template.sfIcon)
                                .font(.system(size: 20))
                                .foregroundColor(LSTheme.primary)
                            Text(template.title)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                        }
                        Divider().padding(.vertical, 4)
                        Text(TemplateData.summary(for: template, info: info))
                            .font(.system(size: 14))
                            .foregroundColor(LSTheme.textPrimary)
                            .lineSpacing(5)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(LSTheme.surfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    // Good days
                    Text("Ngày tốt sắp tới (30 ngày)")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(LSTheme.gold2)

                    if goodDays.isEmpty {
                        Text("Không tìm thấy ngày phù hợp trong 30 ngày tới.")
                            .font(.system(size: 13))
                            .foregroundColor(LSTheme.textSecondary)
                    } else {
                        ForEach(goodDays) { gd in
                            VStack(alignment: .leading, spacing: 3) {
                                HStack {
                                    Text("\(gd.dayOfWeek) · \(gd.solarStr)")
                                        .font(.system(size: 14, weight: .semibold))
                                        .foregroundColor(LSTheme.textPrimary)
                                    Spacer()
                                    Text(gd.daysFromNow == 0 ? "Hôm nay" : "Còn \(gd.daysFromNow) ngày")
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(LSTheme.primary)
                                }
                                Text("\(gd.lunarStr) · \(gd.dayCanChi)")
                                    .font(.system(size: 12))
                                    .foregroundColor(LSTheme.textSecondary)
                                if !gd.gioHoangDao.isEmpty {
                                    Text("Giờ tốt: \(gd.gioHoangDao)")
                                        .font(.system(size: 11))
                                        .foregroundColor(LSTheme.textTertiary)
                                }
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(LSTheme.surfaceContainer)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                }
                .padding(16)
            }
            .background(LSTheme.bg.ignoresSafeArea())
            .navigationTitle("Mẫu tra cứu")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Đóng") { dismiss() }
                }
            }
            .onAppear { goodDays = TemplateData.findGoodDays(for: template) }
        }
    }
}
