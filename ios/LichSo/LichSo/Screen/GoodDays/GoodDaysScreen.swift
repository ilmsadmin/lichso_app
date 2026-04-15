import SwiftUI

// ═══════════════════════════════════════════
// Good Days Screen — Ngày tốt / xấu
// Shows list of days in a month with quality rating
// Filter by activity type (Khai trương, Cưới hỏi, etc.)
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVar: Color { LSTheme.outlineVariant }
private var GoodGreen: Color { LSTheme.goodGreen }
private var BadRed: Color { LSTheme.badRed }
private var NeutralGold: Color { LSTheme.gold }

struct GoodDaysScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = GoodDaysViewModel()
    @State private var selectedDayInfo: DayInfo?

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            headerBar

            // ═══ MONTH SELECTOR ═══
            monthSelector

            // ═══ FILTER CHIPS ═══
            filterChips

            // ═══ DAY LIST ═══
            if vm.state.isLoading {
                Spacer()
                ProgressView()
                    .tint(PrimaryRed)
                Spacer()
            } else if vm.state.filteredDays.isEmpty {
                emptyState
            } else {
                dayList
            }
        }
        .background(SurfaceBg)
        .fullScreenCover(item: $selectedDayInfo) { (info: DayInfo) in
            DayDetailScreen(dayInfo: info, onDismiss: { selectedDayInfo = nil })
        }
    }

    // MARK: - Header

    private var headerBar: some View {
        HStack {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
                    .background(.white.opacity(0.12))
                    .clipShape(Circle())
            }

            Spacer()

            Text("Ngày tốt / xấu")
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(.white)

            Spacer()

            // Invisible spacer for centering
            Color.clear.frame(width: 40, height: 40)
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 12)
        .background(
            LinearGradient(
                colors: [DeepRed, PrimaryRed, Color(hex: "D32F2F")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }

    // MARK: - Month Selector

    private var monthSelector: some View {
        HStack {
            Button { vm.prevMonth() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextSub)
                    .frame(width: 36, height: 36)
                    .background(SurfaceContainer)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 0.5))
            }

            Spacer()

            VStack(spacing: 2) {
                Text("Tháng \(vm.state.month)")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)
                Text("Năm \(String(vm.state.year))")
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
            }

            Spacer()

            Button { vm.nextMonth() } label: {
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextSub)
                    .frame(width: 36, height: 36)
                    .background(SurfaceContainer)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 0.5))
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(SurfaceBg)
    }

    // MARK: - Filter Chips

    private var filterChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(DayFilter.allCases, id: \.rawValue) { filter in
                    FilterChipView(
                        title: filter.rawValue,
                        icon: filter.icon,
                        isActive: vm.state.selectedFilter == filter,
                        onTap: { vm.selectFilter(filter) }
                    )
                }
            }
            .padding(.horizontal, 20)
        }
        .padding(.bottom, 12)
    }

    // MARK: - Day List

    private var dayList: some View {
        ScrollView(.vertical, showsIndicators: true) {
            LazyVStack(spacing: 10) {
                ForEach(vm.state.filteredDays) { item in
                    DayCardView(item: item) {
                        selectedDayInfo = item.dayInfo
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 20)
        }
    }

    // MARK: - Empty State

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "calendar.badge.exclamationmark")
                .font(.system(size: 48))
                .foregroundColor(TextDim.opacity(0.4))
            Text("Không có ngày phù hợp")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(TextSub)
            Text("Thử chọn bộ lọc khác hoặc tháng khác")
                .font(.system(size: 13))
                .foregroundColor(TextDim)
            Spacer()
        }
    }
}

// ══════════════════════════════════════════
// FILTER CHIP
// ══════════════════════════════════════════

private struct FilterChipView: View {
    let title: String
    let icon: String
    let isActive: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 5) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                Text(title)
                    .font(.system(size: 13, weight: .medium))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .foregroundColor(isActive ? .white : TextSub)
            .background(isActive ? PrimaryRed : Color.clear)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(isActive ? PrimaryRed : OutlineVar, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// DAY CARD
// ══════════════════════════════════════════

private struct DayCardView: View {
    let item: GoodDayItem
    let onTap: () -> Void

    private var qualityColor: Color {
        switch item.quality {
        case .good: return GoodGreen
        case .bad: return BadRed
        case .neutral: return NeutralGold
        }
    }

    private var qualityBg: Color {
        switch item.quality {
        case .good: return Color(hex: "E8F5E9")
        case .bad: return Color(hex: "FFEBEE")
        case .neutral: return Color(hex: "FFF8E1")
        }
    }

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 14) {
                // Date column
                dateColumn

                // Info column
                infoColumn

                // Arrow
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundColor(OutlineVar)
                    .padding(.top, 16)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(OutlineVar, lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
    }

    private var dateColumn: some View {
        VStack(spacing: 2) {
            Text(String(format: "%02d", item.dayInfo.solar.dd))
                .font(.system(size: 28, weight: .heavy))
                .foregroundColor(qualityColor)
                .tracking(-1)
            Text(item.dayInfo.dayOfWeek)
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(TextSub)
            Text("\(item.dayInfo.lunar.day)/\(item.dayInfo.lunar.month) ÂL")
                .font(.system(size: 10))
                .foregroundColor(TextDim)
        }
        .frame(minWidth: 52)
    }

    private var infoColumn: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Quality badge
            HStack(spacing: 4) {
                Text(item.quality.icon)
                    .font(.system(size: 10, weight: .bold))
                Text(item.quality.label)
                    .font(.system(size: 10, weight: .bold))
            }
            .foregroundColor(qualityColor)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(qualityBg)
            .clipShape(RoundedRectangle(cornerRadius: 6))

            // Can chi
            Text("\(item.dayInfo.dayCanChi)")
                .font(.system(size: 12))
                .foregroundColor(TextSub)
            + Text(goodHoursText)
                .font(.system(size: 12))
                .foregroundColor(TextSub)

            // Activity tags
            activityTags
        }
    }

    private var goodHoursText: AttributedString {
        if item.dayInfo.gioHoangDao.isEmpty { return "" }
        let hours = item.dayInfo.gioHoangDao.prefix(3).map { $0.name }.joined(separator: ", ")
        return AttributedString(" · Giờ tốt: \(hours)")
    }

    private var activityTags: some View {
        let good = item.goodActivities.prefix(3)
        let bad = item.badActivities.prefix(2)

        return FlowLayout(spacing: 4) {
            ForEach(Array(good.enumerated()), id: \.offset) { _, activity in
                TagView(text: activity, style: .good)
            }
            ForEach(Array(bad.enumerated()), id: \.offset) { _, activity in
                TagView(text: activity, style: .avoid)
            }
            // Holiday tag
            if let holiday = item.dayInfo.solarHoliday ?? item.dayInfo.lunarHoliday {
                TagView(text: holiday, style: .event)
            }
        }
    }
}

// ══════════════════════════════════════════
// TAG VIEW
// ══════════════════════════════════════════

private enum TagStyle {
    case good, avoid, event

    var bgColor: Color {
        switch self {
        case .good: return Color(hex: "E8F5E9")
        case .avoid: return Color(hex: "FFF3E0")
        case .event: return Color(hex: "E3F2FD")
        }
    }

    var fgColor: Color {
        switch self {
        case .good: return Color(hex: "2E7D32")
        case .avoid: return Color(hex: "E65100")
        case .event: return Color(hex: "1565C0")
        }
    }
}

private struct TagView: View {
    let text: String
    let style: TagStyle

    var body: some View {
        Text(text)
            .font(.system(size: 10, weight: .semibold))
            .foregroundColor(style.fgColor)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(style.bgColor)
            .clipShape(RoundedRectangle(cornerRadius: 6))
    }
}

// ══════════════════════════════════════════
// FLOW LAYOUT (wrapping tags)
// ══════════════════════════════════════════

private struct FlowLayout: Layout {
    var spacing: CGFloat = 4

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = computeLayout(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = computeLayout(proposal: proposal, subviews: subviews)
        for (index, offset) in result.offsets.enumerated() {
            subviews[index].place(
                at: CGPoint(x: bounds.minX + offset.x, y: bounds.minY + offset.y),
                proposal: .unspecified
            )
        }
    }

    private struct LayoutResult {
        var offsets: [CGPoint]
        var size: CGSize
    }

    private func computeLayout(proposal: ProposedViewSize, subviews: Subviews) -> LayoutResult {
        let maxWidth = proposal.width ?? .infinity
        var offsets: [CGPoint] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var totalWidth: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }
            offsets.append(CGPoint(x: currentX, y: currentY))
            currentX += size.width + spacing
            lineHeight = max(lineHeight, size.height)
            totalWidth = max(totalWidth, currentX - spacing)
        }

        return LayoutResult(
            offsets: offsets,
            size: CGSize(width: totalWidth, height: currentY + lineHeight)
        )
    }
}

// ══════════════════════════════════════════
// STATS SUMMARY (top of list)
// ══════════════════════════════════════════

// ══════════════════════════════════════════
// PREVIEW
// ══════════════════════════════════════════

#Preview {
    NavigationStack {
        GoodDaysScreen()
    }
}
