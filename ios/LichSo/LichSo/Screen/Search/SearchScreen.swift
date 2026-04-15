import SwiftUI

// ═══════════════════════════════════════════
// Search Screen — Holiday, date, lunar search
// Matches screen-search.html design
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var PrimaryContainer: Color { LSTheme.primaryContainer }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }
private var GoldAccent: Color { LSTheme.gold }

struct SearchScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = SearchViewModel()
    @FocusState private var isSearchFocused: Bool

    // ── Quick lookup sheets ──
    @State private var showLunarConverter = false
    @State private var showGoodDays = false
    @State private var showTuoiHop = false
    @State private var showGotoDate = false

    // ── Day detail navigation ──
    @State private var selectedDayInfo: DayInfo? = nil

    var body: some View {
        VStack(spacing: 0) {
            // ═══ SEARCH BAR ═══
            HStack(spacing: 8) {
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 20))
                        .foregroundColor(.white)

                    TextField("Tìm ngày, sự kiện, ngày lễ...", text: $vm.query)
                        .font(.system(size: 15))
                        .foregroundColor(.white)
                        .tint(.white)
                        .focused($isSearchFocused)
                        .onSubmit {
                            vm.performSearch()
                            vm.saveToRecent(vm.query)
                        }
                        .onChange(of: vm.query) { _, _ in
                            vm.performSearch()
                        }

                    if !vm.query.isEmpty {
                        Button {
                            vm.query = ""
                            vm.results = []
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 18))
                                .foregroundColor(.white.opacity(0.7))
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color.white.opacity(0.2))
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .overlay(RoundedRectangle(cornerRadius: 28).stroke(Color.white.opacity(0.4), lineWidth: 1.5))

                Button { dismiss() } label: {
                    Text("Hủy")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 14)
            .background(
                LinearGradient(
                    colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                             Color(red: 0.545, green: 0, blue: 0)],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
            )

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // ═══ QUICK LOOKUP ═══
                    Text("TRA CỨU NHANH")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(TextDim)
                        .tracking(0.5)
                        .padding(.horizontal, 20)
                        .padding(.top, 16)
                        .padding(.bottom, 10)

                    QuickGrid(
                        onLunarConverter: { showLunarConverter = true },
                        onGoodDays: {
                            vm.loadGoodDaysThisWeek()
                            showGoodDays = true
                        },
                        onTuoiHop: { showTuoiHop = true },
                        onGotoDate: {
                            vm.loadGotoDay()
                            showGotoDate = true
                        }
                    )
                    .padding(.horizontal, 20)
                    .padding(.bottom, 16)

                    // ═══ RESULTS ═══
                    if !vm.results.isEmpty {
                        Text("KẾT QUẢ TÌM KIẾM")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(PrimaryRed)
                            .tracking(0.5)
                            .padding(.horizontal, 20)
                            .padding(.bottom, 10)

                        ForEach(vm.results) { result in
                            ResultCard(result: result) {
                                if let info = result.dayInfo {
                                    selectedDayInfo = info
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                    }

                    // ═══ LUNAR CONVERTER ═══
                    LunarConverterCard(vm: vm)
                        .padding(.horizontal, 16)
                        .padding(.top, 10)

                    // ═══ RECENT SEARCHES ═══
                    if !vm.recentSearches.isEmpty {
                        Text("TÌM GẦN ĐÂY")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(PrimaryRed)
                            .tracking(0.5)
                            .padding(.horizontal, 20)
                            .padding(.top, 16)
                            .padding(.bottom, 6)

                        ForEach(vm.recentSearches, id: \.self) { text in
                            RecentRow(text: text, onTap: {
                                vm.query = text
                                vm.performSearch()
                            }, onRemove: {
                                vm.removeRecent(text)
                            })
                        }
                        .padding(.horizontal, 20)
                    }

                    Spacer().frame(height: 60)
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear { isSearchFocused = true }
        // ── Sheets ──
        .sheet(isPresented: $showLunarConverter) {
            LunarConverterSheet(vm: vm)
        }
        .sheet(isPresented: $showGoodDays) {
            GoodDaysThisWeekSheet(vm: vm) { info in
                showGoodDays = false
                selectedDayInfo = info
            }
        }
        .sheet(isPresented: $showTuoiHop) {
            TuoiHopSheet(vm: vm)
        }
        .sheet(isPresented: $showGotoDate) {
            GotoDateSheet(vm: vm) { info in
                showGotoDate = false
                selectedDayInfo = info
            }
        }
        .fullScreenCover(item: $selectedDayInfo) { (info: DayInfo) in
            DayDetailScreen(dayInfo: info, onDismiss: { selectedDayInfo = nil })
        }
    }
}

// ══════════════════════════════════════════
// QUICK GRID (4 items — all tappable)
// ══════════════════════════════════════════

private struct QuickGrid: View {
    let onLunarConverter: () -> Void
    let onGoodDays: () -> Void
    let onTuoiHop: () -> Void
    let onGotoDate: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            QuickItem(icon: "arrow.left.arrow.right", label: "Đổi Âm\n→ Dương", color: Color(hex: "C62828"), action: onLunarConverter)
            QuickItem(icon: "calendar.badge.checkmark", label: "Ngày tốt\ntuần này", color: Color(hex: "F57F17"), action: onGoodDays)
            QuickItem(icon: "sparkles", label: "Tuổi hợp", color: Color(hex: "2E7D32"), action: onTuoiHop)
            QuickItem(icon: "calendar", label: "Đi đến\nngày", color: Color(hex: "1565C0"), action: onGotoDate)
        }
    }
}

private struct QuickItem: View {
    let icon: String
    let label: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 24))
                    .foregroundColor(color)
                Text(label)
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(TextSub)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// RESULT CARD
// ══════════════════════════════════════════

private struct ResultCard: View {
    let result: SearchResult
    let onTap: () -> Void

    private var iconInfo: (String, Color, Color) {
        switch result.type {
        case .holiday: return ("party.popper.fill", Color(hex: "E65100"), Color(hex: "FFF3E0"))
        case .date:    return ("info.circle.fill", PrimaryRed, PrimaryContainer)
        case .lunar:   return ("moon.fill", Color(hex: "F57F17"), Color(hex: "FFF8E1"))
        }
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: iconInfo.0)
                    .font(.system(size: 20))
                    .foregroundColor(iconInfo.1)
                    .frame(width: 40, height: 40)
                    .background(iconInfo.2)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                VStack(alignment: .leading, spacing: 2) {
                    Text(result.title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(TextMain)
                        .lineLimit(1)
                    Text(result.subtitle)
                        .font(.system(size: 11))
                        .foregroundColor(TextSub)
                        .lineLimit(1)
                }

                Spacer()

                Image(systemName: result.dayInfo != nil ? "chevron.right" : "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVariant)
            }
            .padding(12)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// LUNAR CONVERTER CARD (inline in scroll)
// ══════════════════════════════════════════

private struct LunarConverterCard: View {
    @ObservedObject var vm: SearchViewModel

    var body: some View {
        VStack(spacing: 10) {
            HStack(spacing: 6) {
                Image(systemName: "arrow.left.arrow.right")
                    .font(.system(size: 16))
                    .foregroundColor(GoldAccent)
                Text("Chuyển đổi Âm ↔ Dương")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(GoldAccent)
                Spacer()
            }

            HStack(spacing: 8) {
                TextField("Âm lịch (dd/MM)", text: $vm.lunarInput)
                    .font(.system(size: 14))
                    .multilineTextAlignment(.center)
                    .padding(10)
                    .background(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "FFE082"), lineWidth: 1))
                    .onSubmit { vm.convertLunarToSolar() }

                Image(systemName: "arrow.right")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(GoldAccent)

                Text(vm.lunarOutput.isEmpty ? "Dương lịch" : vm.lunarOutput)
                    .font(.system(size: 14))
                    .foregroundColor(vm.lunarOutput.isEmpty ? TextDim : TextMain)
                    .frame(maxWidth: .infinity)
                    .padding(10)
                    .background(Color(hex: "FFF8E1"))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "FFE082"), lineWidth: 1))
            }

            if !vm.lunarSummary.isEmpty {
                Text(vm.lunarSummary)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
            }

            Button {
                vm.convertLunarToSolar()
            } label: {
                Text("Chuyển đổi")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(GoldAccent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .padding(16)
        .background(
            LinearGradient(colors: [Color(hex: "FFF8E1"), Color(hex: "FFFDE7")], startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color(hex: "FFE082"), lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// RECENT SEARCH ROW
// ══════════════════════════════════════════

private struct RecentRow: View {
    let text: String
    let onTap: () -> Void
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 18))
                .foregroundColor(TextDim)

            Button(action: onTap) {
                Text(text)
                    .font(.system(size: 14))
                    .foregroundColor(TextSub)
                    .lineLimit(1)
            }

            Spacer()

            Button(action: onRemove) {
                Image(systemName: "xmark")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVariant)
            }
        }
        .padding(.vertical, 10)
        .overlay(alignment: .bottom) {
            Rectangle().fill(OutlineVariant).frame(height: 0.5)
        }
    }
}

// ══════════════════════════════════════════
// SHEET: Lunar Converter (full dedicated sheet)
// ══════════════════════════════════════════

private struct LunarConverterSheet: View {
    @ObservedObject var vm: SearchViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var solarInput = ""
    @State private var solarToLunarResult = ""

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {

                    // ── Âm → Dương ──
                    GroupBox {
                        VStack(spacing: 12) {
                            HStack(spacing: 6) {
                                Image(systemName: "moon.fill")
                                    .foregroundColor(GoldAccent)
                                Text("Âm lịch → Dương lịch")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(GoldAccent)
                                Spacer()
                            }

                            HStack(spacing: 8) {
                                TextField("dd/MM hoặc dd/MM/yyyy", text: $vm.lunarInput)
                                    .font(.system(size: 14))
                                    .padding(10)
                                    .background(SurfaceContainerHigh)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                                    .keyboardType(.numbersAndPunctuation)

                                Button {
                                    vm.convertLunarToSolar()
                                } label: {
                                    Image(systemName: "arrow.right.circle.fill")
                                        .font(.system(size: 28))
                                        .foregroundColor(GoldAccent)
                                }
                            }

                            if !vm.lunarOutput.isEmpty {
                                HStack {
                                    Image(systemName: "sun.max.fill")
                                        .foregroundColor(Color(hex: "F57F17"))
                                    Text(vm.lunarSummary)
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(TextMain)
                                    Spacer()
                                }
                                .padding(12)
                                .background(Color(hex: "FFF8E1"))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }
                        .padding(4)
                    }
                    .groupBoxStyle(CardGroupBoxStyle(accent: GoldAccent))

                    // ── Dương → Âm ──
                    GroupBox {
                        VStack(spacing: 12) {
                            HStack(spacing: 6) {
                                Image(systemName: "sun.max.fill")
                                    .foregroundColor(Color(hex: "1565C0"))
                                Text("Dương lịch → Âm lịch")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(Color(hex: "1565C0"))
                                Spacer()
                            }

                            HStack(spacing: 8) {
                                TextField("dd/MM hoặc dd/MM/yyyy", text: $solarInput)
                                    .font(.system(size: 14))
                                    .padding(10)
                                    .background(SurfaceContainerHigh)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                                    .keyboardType(.numbersAndPunctuation)

                                Button {
                                    convertSolarToLunar()
                                } label: {
                                    Image(systemName: "arrow.right.circle.fill")
                                        .font(.system(size: 28))
                                        .foregroundColor(Color(hex: "1565C0"))
                                }
                            }

                            if !solarToLunarResult.isEmpty {
                                HStack {
                                    Image(systemName: "moon.fill")
                                        .foregroundColor(GoldAccent)
                                    Text(solarToLunarResult)
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(TextMain)
                                    Spacer()
                                }
                                .padding(12)
                                .background(Color(hex: "FFF8E1"))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }
                        .padding(4)
                    }
                    .groupBoxStyle(CardGroupBoxStyle(accent: Color(hex: "1565C0")))

                    // ── Help note ──
                    HStack(spacing: 8) {
                        Image(systemName: "info.circle")
                            .font(.system(size: 14))
                            .foregroundColor(TextDim)
                        Text("Nhập định dạng: 15/01 hoặc 15/01/2025")
                            .font(.system(size: 12))
                            .foregroundColor(TextDim)
                        Spacer()
                    }
                    .padding(.horizontal, 4)
                }
                .padding(16)
            }
            .background(SurfaceBg)
            .navigationTitle("Đổi Âm ↔ Dương")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark")
                            .foregroundColor(PrimaryRed)
                    }
                }
            }
        }
    }

    private func convertSolarToLunar() {
        let parts = solarInput.split(separator: "/").compactMap { Int($0) }
        guard parts.count >= 2 else { solarToLunarResult = "Không hợp lệ"; return }
        let dd = parts[0], mm = parts[1]
        let yy = parts.count >= 3 ? parts[2] : Calendar.current.component(.year, from: Date())
        guard dd >= 1 && dd <= 31 && mm >= 1 && mm <= 12 else {
            solarToLunarResult = "Không hợp lệ"
            return
        }
        let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy, timeZone: 7.0)
        solarToLunarResult = "\(String(format: "%02d/%02d/%d", dd, mm, yy)) Dương → \(lunar.lunarDay)/\(lunar.lunarMonth)/\(lunar.lunarYear) Âm lịch"
    }
}

// ══════════════════════════════════════════
// SHEET: Ngày tốt tuần này
// ══════════════════════════════════════════

private struct GoodDaysThisWeekSheet: View {
    @ObservedObject var vm: SearchViewModel
    let onSelectDay: (DayInfo) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var expandedId: UUID? = nil

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    // ── Week summary banner ──
                    weekSummaryBanner

                    // ── Legend ──
                    legendRow

                    // ── Day cards ──
                    ForEach(vm.goodDaysThisWeek) { item in
                        GoodDayDetailCard(
                            item: item,
                            isExpanded: expandedId == item.id,
                            onToggle: {
                                withAnimation(.easeInOut(duration: 0.25)) {
                                    expandedId = expandedId == item.id ? nil : item.id
                                }
                            },
                            onViewDetail: {
                                guard let info = item.dayInfo else { return }
                                onSelectDay(info)
                            }
                        )
                    }
                }
                .padding(16)
            }
            .background(SurfaceBg)
            .navigationTitle("Ngày tốt tuần này")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark").foregroundColor(PrimaryRed)
                    }
                }
            }
        }
    }

    private var weekSummaryBanner: some View {
        let goodCount = vm.goodDaysThisWeek.filter { $0.ratingLabel == "Rất tốt" || $0.ratingLabel == "Tốt" }.count
        let avgScore = vm.goodDaysThisWeek.isEmpty ? 0 : vm.goodDaysThisWeek.map { $0.ratingScore }.reduce(0, +) / vm.goodDaysThisWeek.count
        let bestDay = vm.goodDaysThisWeek.max(by: { $0.ratingScore < $1.ratingScore })

        return VStack(spacing: 12) {
            HStack(spacing: 12) {
                // Left: Score circle
                ZStack {
                    Circle()
                        .stroke(Color(hex: "E8F5E9"), lineWidth: 6)
                        .frame(width: 56, height: 56)
                    Circle()
                        .trim(from: 0, to: CGFloat(avgScore) / 100)
                        .stroke(
                            goodCount > 3 ? Color(hex: "2E7D32") : Color(hex: "F57F17"),
                            style: StrokeStyle(lineWidth: 6, lineCap: .round)
                        )
                        .frame(width: 56, height: 56)
                        .rotationEffect(.degrees(-90))
                    Text("\(avgScore)")
                        .font(.system(size: 16, weight: .heavy))
                        .foregroundColor(goodCount > 3 ? Color(hex: "2E7D32") : Color(hex: "F57F17"))
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Tuần này có **\(goodCount)/7** ngày tốt")
                        .font(.system(size: 14))
                        .foregroundColor(TextMain)
                    if let best = bestDay {
                        Text("Ngày tốt nhất: \(best.dayOfWeek) \(best.dd)/\(best.mm)")
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "2E7D32"))
                    }
                    Text("Nhấn vào ngày để mở rộng chi tiết")
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                Image(systemName: "calendar.badge.checkmark")
                    .font(.system(size: 32))
                    .foregroundColor(Color(hex: "F57F17"))
            }
        }
        .padding(16)
        .background(
            LinearGradient(
                colors: [Color(hex: "F1F8E9"), Color(hex: "FFFDE7")],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color(hex: "C8E6C9"), lineWidth: 1))
    }

    private var legendRow: some View {
        HStack(spacing: 16) {
            legendDot(color: Color(hex: "2E7D32"), label: "Rất tốt / Tốt")
            legendDot(color: Color(hex: "F57F17"), label: "Trung bình")
            legendDot(color: Color(hex: "C62828"), label: "Xấu")
            Spacer()
        }
        .padding(.horizontal, 4)
    }

    private func legendDot(color: Color, label: String) -> some View {
        HStack(spacing: 4) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(label).font(.system(size: 10)).foregroundColor(TextDim)
        }
    }
}

// ── Detailed Good Day Card with expandable content ──
private struct GoodDayDetailCard: View {
    let item: WeekGoodDayItem
    let isExpanded: Bool
    let onToggle: () -> Void
    let onViewDetail: () -> Void

    private var ratingColor: Color {
        switch item.ratingLabel {
        case "Rất tốt": return Color(hex: "2E7D32")
        case "Tốt":     return Color(hex: "388E3C")
        case "Xấu":     return Color(hex: "C62828")
        default:        return Color(hex: "F57F17")
        }
    }

    private var ratingBg: Color {
        switch item.ratingLabel {
        case "Rất tốt", "Tốt": return Color(hex: "E8F5E9")
        case "Xấu":             return Color(hex: "FFEBEE")
        default:                return Color(hex: "FFF3E0")
        }
    }

    private var ratingIcon: String {
        switch item.ratingLabel {
        case "Rất tốt": return "checkmark.seal.fill"
        case "Tốt":     return "checkmark.circle.fill"
        case "Xấu":     return "xmark.circle.fill"
        default:        return "minus.circle.fill"
        }
    }

    private var borderColor: Color {
        switch item.ratingLabel {
        case "Rất tốt": return Color(hex: "A5D6A7")
        case "Tốt":     return Color(hex: "C8E6C9")
        case "Xấu":     return Color(hex: "FFCDD2")
        default:        return OutlineVariant
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // ── Header row (always visible, tappable) ──
            Button(action: onToggle) {
                HStack(spacing: 12) {
                    // Date badge
                    VStack(spacing: 2) {
                        Text("\(item.dd)")
                            .font(.system(size: 24, weight: .heavy))
                            .foregroundColor(ratingColor)
                        Text(String(format: "/%02d", item.mm))
                            .font(.system(size: 11))
                            .foregroundColor(TextDim)
                    }
                    .frame(width: 48)

                    VStack(alignment: .leading, spacing: 3) {
                        HStack(spacing: 6) {
                            Text(item.dayOfWeek)
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(TextMain)
                            Text("• \(item.lunarDay)/\(item.lunarMonth) Âm")
                                .font(.system(size: 11))
                                .foregroundColor(TextDim)
                        }
                        if let info = item.dayInfo {
                            Text(info.dayCanChi)
                                .font(.system(size: 11))
                                .foregroundColor(TextSub)
                        }
                        if let hol = item.solarHoliday ?? item.lunarHoliday {
                            HStack(spacing: 4) {
                                Image(systemName: "party.popper.fill")
                                    .font(.system(size: 10))
                                Text(hol)
                                    .font(.system(size: 11))
                            }
                            .foregroundColor(Color(hex: "E65100"))
                        }
                    }

                    Spacer()

                    // Rating badge
                    HStack(spacing: 4) {
                        Image(systemName: ratingIcon)
                            .font(.system(size: 12))
                        Text(item.ratingLabel)
                            .font(.system(size: 11, weight: .semibold))
                    }
                    .foregroundColor(ratingColor)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(ratingBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(OutlineVariant)
                }
                .padding(14)
            }
            .buttonStyle(.plain)

            // ── Expanded detail ──
            if isExpanded, let info = item.dayInfo {
                Divider().padding(.horizontal, 14)

                VStack(alignment: .leading, spacing: 12) {

                    // ── Rating bar ──
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text("Đánh giá ngày")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(TextSub)
                            Spacer()
                            Text("\(info.dayRating.percent)%")
                                .font(.system(size: 12, weight: .heavy))
                                .foregroundColor(ratingColor)
                        }
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(ratingBg)
                                    .frame(height: 8)
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(ratingColor)
                                    .frame(width: geo.size.width * CGFloat(info.dayRating.percent) / 100, height: 8)
                            }
                        }
                        .frame(height: 8)
                    }

                    // ── Can Chi / Trực / Sao row ──
                    HStack(spacing: 8) {
                        infoChip(icon: "sun.max.fill", label: info.dayCanChi, color: Color(hex: "E65100"))
                        infoChip(icon: "arrow.triangle.branch", label: info.trucNgay.name, color: trucColor(info.trucNgay.rating))
                        infoChip(icon: "sparkle", label: info.saoChieu.name, color: saoColor(info.saoChieu.rating))
                    }

                    // ── Giờ hoàng đạo ──
                    if !info.gioHoangDao.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            HStack(spacing: 4) {
                                Image(systemName: "clock.fill")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: "F57F17"))
                                Text("Giờ hoàng đạo")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(TextSub)
                            }
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 6) {
                                ForEach(info.gioHoangDao, id: \.name) { gio in
                                    HStack(spacing: 4) {
                                        Text(gio.name)
                                            .font(.system(size: 11, weight: .semibold))
                                            .foregroundColor(Color(hex: "F57F17"))
                                        Text(gio.time)
                                            .font(.system(size: 10))
                                            .foregroundColor(TextDim)
                                    }
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 5)
                                    .background(Color(hex: "FFF8E1"))
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                            }
                        }
                    }

                    // ── Nên làm ──
                    if !info.activities.nenLam.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            HStack(spacing: 4) {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: "2E7D32"))
                                Text("Nên làm")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color(hex: "2E7D32"))
                            }
                            WeekGoodDayTagFlow(tags: info.activities.nenLam, style: .good)
                        }
                    }

                    // ── Không nên ──
                    if !info.activities.khongNen.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            HStack(spacing: 4) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: "C62828"))
                                Text("Không nên")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color(hex: "C62828"))
                            }
                            WeekGoodDayTagFlow(tags: info.activities.khongNen, style: .bad)
                        }
                    }

                    // ── Hướng tốt ──
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 4) {
                            Image(systemName: "safari.fill")
                                .font(.system(size: 11))
                                .foregroundColor(Color(hex: "1565C0"))
                            Text("Hướng tốt")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(TextSub)
                        }
                        HStack(spacing: 8) {
                            huongBadge(label: "Thần Tài", value: info.huong.thanTai, color: Color(hex: "F57F17"))
                            huongBadge(label: "Hỷ Thần", value: info.huong.hyThan, color: Color(hex: "C62828"))
                        }
                    }

                    // ── Warnings ──
                    if info.activities.isNguyetKy || info.activities.isTamNuong {
                        HStack(spacing: 6) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(Color(hex: "C62828"))
                            VStack(alignment: .leading, spacing: 2) {
                                if info.activities.isNguyetKy {
                                    Text("⚠ Ngày Nguyệt Kỵ — kiêng mọi việc lớn")
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(Color(hex: "C62828"))
                                }
                                if info.activities.isTamNuong {
                                    Text("⚠ Ngày Tam Nương — không nên cưới hỏi")
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(Color(hex: "C62828"))
                                }
                            }
                            Spacer()
                        }
                        .padding(10)
                        .background(Color(hex: "FFEBEE"))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    // ── View full detail button ──
                    Button(action: onViewDetail) {
                        HStack(spacing: 6) {
                            Image(systemName: "arrow.up.right.square")
                                .font(.system(size: 13))
                            Text("Xem chi tiết đầy đủ")
                                .font(.system(size: 13, weight: .semibold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(
                            LinearGradient(
                                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                                         Color(red: 0.545, green: 0, blue: 0)],
                                startPoint: .leading, endPoint: .trailing
                            )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(.horizontal, 14)
                .padding(.top, 10)
                .padding(.bottom, 14)
            }
        }
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(borderColor, lineWidth: isExpanded ? 1.5 : 1))
        .shadow(color: isExpanded ? ratingColor.opacity(0.08) : .clear, radius: 6, y: 2)
    }

    // ── Helper views ──

    private func infoChip(icon: String, label: String, color: Color) -> some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 10))
            Text(label)
                .font(.system(size: 10, weight: .semibold))
        }
        .foregroundColor(color)
        .padding(.horizontal, 8)
        .padding(.vertical, 5)
        .background(color.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    private func huongBadge(label: String, value: String, color: Color) -> some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(TextDim)
            Text(value)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .background(color.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    private func trucColor(_ rating: String) -> Color {
        switch rating {
        case "tốt": return Color(hex: "2E7D32")
        case "xấu": return Color(hex: "C62828")
        default:    return Color(hex: "F57F17")
        }
    }

    private func saoColor(_ rating: String) -> Color {
        switch rating {
        case "tốt": return Color(hex: "1565C0")
        case "xấu": return Color(hex: "C62828")
        default:    return Color(hex: "F57F17")
        }
    }
}

// ── Tag flow for activities ──
private enum WeekGoodDayTagStyle {
    case good, bad

    var bgColor: Color {
        switch self {
        case .good: return Color(hex: "E8F5E9")
        case .bad:  return Color(hex: "FFF3E0")
        }
    }
    var fgColor: Color {
        switch self {
        case .good: return Color(hex: "2E7D32")
        case .bad:  return Color(hex: "E65100")
        }
    }
}

private struct WeekGoodDayTagFlow: View {
    let tags: [String]
    let style: WeekGoodDayTagStyle

    var body: some View {
        // Simple wrapping using HStack + wrapping approach
        WrappingHStack(tags: tags) { tag in
            Text(tag)
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(style.fgColor)
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .background(style.bgColor)
                .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }
}

private struct WrappingHStack<Content: View>: View {
    let tags: [String]
    let content: (String) -> Content

    @State private var totalHeight: CGFloat = .zero

    var body: some View {
        GeometryReader { geo in
            generateContent(in: geo)
        }
        .frame(height: totalHeight)
    }

    private func generateContent(in geo: GeometryProxy) -> some View {
        var width = CGFloat.zero
        var height = CGFloat.zero

        return ZStack(alignment: .topLeading) {
            ForEach(tags, id: \.self) { tag in
                content(tag)
                    .padding(.trailing, 4)
                    .padding(.bottom, 4)
                    .alignmentGuide(.leading) { d in
                        if abs(width - d.width) > geo.size.width {
                            width = 0
                            height -= d.height + 4
                        }
                        let result = width
                        if tag == tags.last {
                            width = 0
                        } else {
                            width -= d.width
                        }
                        return result
                    }
                    .alignmentGuide(.top) { _ in
                        let result = height
                        if tag == tags.last {
                            height = 0
                        }
                        return result
                    }
            }
        }
        .background(viewHeightReader($totalHeight))
    }

    private func viewHeightReader(_ binding: Binding<CGFloat>) -> some View {
        GeometryReader { geo -> Color in
            DispatchQueue.main.async {
                binding.wrappedValue = geo.size.height
            }
            return Color.clear
        }
    }
}

// ══════════════════════════════════════════
// SHEET: Tuổi hợp
// ══════════════════════════════════════════

private struct TuoiHopSheet: View {
    @ObservedObject var vm: SearchViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Input row
                    VStack(spacing: 10) {
                        Text("Nhập năm sinh (Dương lịch) để xem các tuổi hợp:")
                            .font(.system(size: 13))
                            .foregroundColor(TextSub)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        HStack(spacing: 10) {
                            TextField("VD: 1990", text: $vm.tuoiHopInput)
                                .font(.system(size: 16))
                                .keyboardType(.numberPad)
                                .padding(12)
                                .background(SurfaceContainerHigh)
                                .clipShape(RoundedRectangle(cornerRadius: 12))

                            Button {
                                vm.calculateTuoiHop()
                            } label: {
                                Text("Tính")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 12)
                                    .background(Color(hex: "2E7D32"))
                                    .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }

                        if !vm.tuoiHopError.isEmpty {
                            HStack(spacing: 6) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(Color(hex: "C62828"))
                                Text(vm.tuoiHopError)
                                    .font(.system(size: 13))
                                    .foregroundColor(Color(hex: "C62828"))
                                Spacer()
                            }
                        }
                    }
                    .padding(16)
                    .background(SurfaceContainer)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))

                    // User zodiac info
                    if !vm.tuoiHopUserZodiac.isEmpty {
                        HStack(spacing: 12) {
                            Text(SearchViewModel.zodiacEmojis[SearchViewModel.zodiacNames.firstIndex(of: vm.tuoiHopUserZodiac) ?? 0])
                                .font(.system(size: 36))
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Tuổi \(vm.tuoiHopUserZodiac)")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(TextMain)
                                Text("Năm \(vm.tuoiHopInput) · \(vm.tuoiHopUserCanChi)")
                                    .font(.system(size: 12))
                                    .foregroundColor(TextDim)
                            }
                            Spacer()
                        }
                        .padding(16)
                        .background(
                            LinearGradient(colors: [Color(hex: "E8F5E9"), Color(hex: "F1F8E9")], startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "A5D6A7"), lineWidth: 1))

                        // Compatibility list
                        Text("Mức độ tương hợp")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(TextDim)
                            .tracking(0.5)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        ForEach(vm.tuoiHopResults) { item in
                            ZodiacCompatRow(item: item, userZodiac: vm.tuoiHopUserZodiac)
                        }
                    }
                }
                .padding(16)
            }
            .background(SurfaceBg)
            .navigationTitle("Tra cứu tuổi hợp")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark").foregroundColor(PrimaryRed)
                    }
                }
            }
        }
    }
}

private struct ZodiacCompatRow: View {
    let item: ZodiacCompatItem
    let userZodiac: String

    private var accentColor: Color {
        switch item.compatLabel {
        case "Rất hợp":    return Color(hex: "2E7D32")
        case "Hợp":        return Color(hex: "388E3C")
        case "Bình thường": return Color(hex: "F57F17")
        default:           return Color(hex: "C62828")
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            Text(item.zodiacEmoji)
                .font(.system(size: 28))

            VStack(alignment: .leading, spacing: 2) {
                Text("Tuổi \(item.zodiac)")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                Text(item.reason)
                    .font(.system(size: 11))
                    .foregroundColor(TextDim)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 3) {
                Text(item.compatLabel)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(accentColor)

                // Score bar
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(accentColor.opacity(0.12))
                        .frame(width: 60, height: 5)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(accentColor)
                        .frame(width: CGFloat(item.compatScore) / 100.0 * 60, height: 5)
                }
            }
        }
        .padding(12)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(
            item.compatLabel == "Rất hợp" ? Color(hex: "A5D6A7") : OutlineVariant, lineWidth: 1
        ))
    }
}

// ══════════════════════════════════════════
// SHEET: Đi đến ngày
// ══════════════════════════════════════════

private struct GotoDateSheet: View {
    @ObservedObject var vm: SearchViewModel
    let onSelectDay: (DayInfo) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Date picker
                DatePicker(
                    "Chọn ngày",
                    selection: $vm.gotoDate,
                    displayedComponents: [.date]
                )
                .datePickerStyle(.graphical)
                .tint(PrimaryRed)
                .padding(.horizontal, 16)
                .onChange(of: vm.gotoDate) { _, _ in
                    vm.loadGotoDay()
                }

                Divider().padding(.horizontal, 16)

                // Day info preview
                if let info = vm.gotoDayInfo {
                    GotoDayPreview(info: info)
                        .padding(.horizontal, 16)
                        .padding(.top, 12)
                }

                Spacer()

                // Go button
                Button {
                    if let info = vm.gotoDayInfo {
                        onSelectDay(info)
                    }
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "calendar")
                        Text("Xem chi tiết ngày này")
                            .font(.system(size: 15, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(
                        LinearGradient(
                            colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                                     Color(red: 0.545, green: 0, blue: 0)],
                            startPoint: .leading, endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 32)
            }
            .background(SurfaceBg)
            .navigationTitle("Đi đến ngày")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark").foregroundColor(PrimaryRed)
                    }
                }
            }
            .onAppear { vm.loadGotoDay() }
        }
    }
}

private struct GotoDayPreview: View {
    let info: DayInfo

    private var ratingColor: Color {
        switch info.dayRating.label {
        case "Rất tốt": return Color(hex: "2E7D32")
        case "Tốt":     return Color(hex: "388E3C")
        case "Xấu":     return Color(hex: "C62828")
        default:        return Color(hex: "F57F17")
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text("\(info.dayOfWeek), \(String(format: "%02d/%02d/%d", info.solar.dd, info.solar.mm, info.solar.yy))")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(TextMain)
                Text("Âm lịch: \(info.lunar.day)/\(info.lunar.month)/\(info.lunar.year) · \(info.dayCanChi)")
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
                if let hol = info.solarHoliday ?? info.lunarHoliday {
                    HStack(spacing: 4) {
                        Image(systemName: "party.popper.fill")
                            .font(.system(size: 10))
                        Text(hol)
                            .font(.system(size: 11))
                    }
                    .foregroundColor(Color(hex: "E65100"))
                }
            }

            Spacer()

            VStack(spacing: 4) {
                Image(systemName: info.dayRating.label == "Rất tốt" ? "checkmark.seal.fill" : info.dayRating.label == "Tốt" ? "checkmark.circle.fill" : "minus.circle.fill")
                    .font(.system(size: 20))
                    .foregroundColor(ratingColor)
                Text(info.dayRating.label)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(ratingColor)
            }
        }
        .padding(14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(OutlineVariant, lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// GroupBox style helper
// ══════════════════════════════════════════

private struct CardGroupBoxStyle: GroupBoxStyle {
    let accent: Color

    func makeBody(configuration: Configuration) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            configuration.content
        }
        .padding(12)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(accent.opacity(0.3), lineWidth: 1.5))
    }
}

// ══════════════════════════════════════════
#Preview {
    SearchScreen()
}
