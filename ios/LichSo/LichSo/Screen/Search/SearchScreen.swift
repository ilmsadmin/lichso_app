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

    var body: some View {
        VStack(spacing: 0) {
            // ═══ SEARCH BAR ═══
            HStack(spacing: 8) {
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 20))
                        .foregroundColor(PrimaryRed)

                    TextField("Tìm ngày, sự kiện, ngày lễ...", text: $vm.query)
                        .font(.system(size: 15))
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
                                .foregroundColor(TextDim)
                                .frame(width: 32, height: 32)
                                .background(Color(hex: "2A2720"))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(SurfaceContainerHigh)
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .overlay(RoundedRectangle(cornerRadius: 28).stroke(PrimaryRed, lineWidth: 2))

                Button { dismiss() } label: {
                    Text("Hủy")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(PrimaryRed)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 12)

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // ═══ QUICK LOOKUP ═══
                    Text("TRA CỨU NHANH")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(TextDim)
                        .tracking(0.5)
                        .padding(.horizontal, 20)
                        .padding(.bottom, 10)

                    QuickGrid()
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
                            ResultCard(result: result)
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
    }
}

// ══════════════════════════════════════════
// QUICK GRID (4 items)
// ══════════════════════════════════════════

private struct QuickGrid: View {
    private let items: [(icon: String, label: String, color: Color)] = [
        ("arrow.left.arrow.right", "Đổi Âm\n→ Dương", Color(hex: "C62828")),
        ("calendar.badge.checkmark", "Ngày tốt\ntuần này", Color(hex: "F57F17")),
        ("sparkles", "Tuổi hợp", Color(hex: "2E7D32")),
        ("calendar", "Đi đến\nngày", Color(hex: "1565C0")),
    ]

    var body: some View {
        HStack(spacing: 10) {
            ForEach(0..<items.count, id: \.self) { i in
                let item = items[i]
                VStack(spacing: 6) {
                    Image(systemName: item.icon)
                        .font(.system(size: 24))
                        .foregroundColor(item.color)
                    Text(item.label)
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
        }
    }
}

// ══════════════════════════════════════════
// RESULT CARD
// ══════════════════════════════════════════

private struct ResultCard: View {
    let result: SearchResult

    private var iconInfo: (String, Color, Color) {
        switch result.type {
        case .holiday: return ("party.popper.fill", Color(hex: "E65100"), Color(hex: "FFF3E0"))
        case .date:    return ("info.circle.fill", PrimaryRed, PrimaryContainer)
        case .lunar:   return ("moon.fill", Color(hex: "F57F17"), Color(hex: "FFF8E1"))
        }
    }

    var body: some View {
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

            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(OutlineVariant)
        }
        .padding(12)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        .padding(.bottom, 8)
    }
}

// ══════════════════════════════════════════
// LUNAR CONVERTER CARD
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
