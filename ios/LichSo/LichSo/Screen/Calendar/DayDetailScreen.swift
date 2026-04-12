import SwiftUI

// ═══════════════════════════════════════════════════════════
// Day Detail Screen — Full-screen day information
// Matches screen-day-detail.html mock design
// Uses real data from DayInfoProvider
// ═══════════════════════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVar: Color { LSTheme.outlineVariant }
private var GoodGreen: Color { LSTheme.goodGreen }
private var BadRed: Color { LSTheme.badRed }

struct DayDetailScreen: View {
    let dayInfo: DayInfo
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HERO HEADER ═══
            DayHero(
                dayInfo: dayInfo,
                onBack: onDismiss,
                onShare: { shareDay() }
            )

            // ═══ SCROLLABLE CONTENT ═══
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 16) {
                    // ── Info Grid 2×2 ──
                    DetailInfoGrid(dayInfo: dayInfo)

                    // ── Giờ Hoàng Đạo ──
                    DetailSection(icon: "clock.fill", title: "Giờ hoàng đạo") {
                        HoursGrid(hours: dayInfo.gioHoangDao, jd: dayInfo.jd)
                    }

                    // ── Việc nên / kỵ ──
                    DetailSection(icon: "checklist", title: "Việc nên / kỵ") {
                        ActivitiesSection(activities: dayInfo.activities)
                    }

                    // ── Sự kiện & Ghi chú ──
                    DetailSection(icon: "calendar.badge.clock", title: "Sự kiện & Ghi chú") {
                        EventsSection(dayInfo: dayInfo)
                    }

                    // ── Quote ──
                    DayQuoteCard(day: dayInfo.solar.dd, month: dayInfo.solar.mm)

                    // ── Ask AI Button ──
                    AskAiButton()

                    Spacer().frame(height: 20)
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
            }
        }
        .background(SurfaceBg)
        .ignoresSafeArea(edges: .top)
    }

    private func shareDay() {
        var text = "📅 \(dayInfo.dayOfWeek), \(dayInfo.solar.dd)/\(dayInfo.solar.mm)/\(dayInfo.solar.yy)\n"
        text += "🌙 Âm lịch: \(dayInfo.lunar.day)/\(dayInfo.lunar.month)/\(dayInfo.lunar.year)"
        if dayInfo.lunar.leap == 1 { text += " (Nhuận)" }
        text += "\n🔮 Ngày \(dayInfo.dayCanChi)\n"
        text += "📆 Tháng \(dayInfo.monthCanChi), Năm \(dayInfo.yearCanChi)\n\n"
        text += "⭐ Đánh giá: \(dayInfo.dayRating.label) (\(dayInfo.dayRating.percent)%)\n"
        if let hol = dayInfo.solarHoliday { text += "🎉 \(hol)\n" }
        if let hol = dayInfo.lunarHoliday { text += "🏮 \(hol)\n" }
        if !dayInfo.activities.nenLam.isEmpty {
            text += "\n✅ Nên làm: \(dayInfo.activities.nenLam.prefix(5).joined(separator: ", "))\n"
        }
        if !dayInfo.activities.khongNen.isEmpty {
            text += "❌ Không nên: \(dayInfo.activities.khongNen.prefix(5).joined(separator: ", "))\n"
        }
        text += "\n💰 Thần Tài: \(dayInfo.huong.thanTai)\n"
        text += "😊 Hỷ Thần: \(dayInfo.huong.hyThan)\n\n"
        text += "— Lịch Số · Lịch Vạn Niên"

        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = windowScene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }
}

// ══════════════════════════════════════════
// HERO HEADER
// ══════════════════════════════════════════

private struct DayHero: View {
    let dayInfo: DayInfo
    let onBack: () -> Void
    let onShare: () -> Void

    var body: some View {
        ZStack(alignment: .top) {
            // Decorative glow
            Circle()
                .fill(RadialGradient(
                    colors: [Color.yellow.opacity(0.1), .clear],
                    center: .center, startRadius: 0, endRadius: 90
                ))
                .frame(width: 180, height: 180)
                .offset(x: 80, y: -40)

            VStack(spacing: 0) {
                Spacer().frame(height: 56) // Status bar space

                // ── Nav row ──
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(Color.white.opacity(0.12))
                            .clipShape(Circle())
                    }

                    Spacer()

                    Button(action: onShare) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 17))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(Color.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 24)

                Spacer().frame(height: 8)

                // ── Big Date ──
                Text(String(format: "%02d", dayInfo.solar.dd))
                    .font(.system(size: 80, weight: .heavy))
                    .foregroundColor(.white)
                    .tracking(-2)

                // ── Weekday ──
                Text(dayInfo.dayOfWeek)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white.opacity(0.85))
                    .tracking(1)

                // ── Month / Year ──
                Text("Tháng \(dayInfo.solar.mm), \(String(dayInfo.solar.yy))")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.6))
                    .padding(.top, 2)

                // ── Lunar chips ──
                HStack(spacing: 8) {
                    // Lunar chip
                    HStack(spacing: 6) {
                        Text(dayInfo.moonPhase.icon)
                            .font(.system(size: 16))
                        Text("\(dayInfo.lunar.day) tháng \(dayInfo.lunar.month) Âm · \(dayInfo.dayCanChi)")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.white.opacity(0.95))
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Capsule())

                    // Quality chip
                    let isGood = !dayInfo.activities.isXauDay
                    HStack(spacing: 4) {
                        Text("✦")
                            .font(.system(size: 10))
                        Text(isGood ? "Hoàng Đạo" : "Hắc Đạo")
                            .font(.system(size: 11, weight: .bold))
                    }
                    .foregroundColor(isGood ? Color(hex: "A5D6A7") : Color(hex: "EF9A9A"))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 5)
                    .background(isGood ? Color(hex: "4CAF50").opacity(0.3) : Color(hex: "C62828").opacity(0.3))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.top, 12)

                Spacer().frame(height: 20)
            }
        }
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                colors: [PrimaryRed, Color(hex: "D32F2F"), DeepRed],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// INFO GRID 2×2
// ══════════════════════════════════════════

private struct DetailInfoGrid: View {
    let dayInfo: DayInfo

    var body: some View {
        LazyVGrid(columns: [
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10)
        ], spacing: 10) {
            InfoGridCard(
                icon: "sparkles", label: "Can chi",
                value: "Ngày \(dayInfo.dayCanChi)",
                valueColor: PrimaryRed
            )
            InfoGridCard(
                icon: "star.fill", label: "Trực",
                value: "Trực \(dayInfo.trucNgay.name)",
                valueColor: dayInfo.trucNgay.rating == "Tốt" ? GoodGreen : (dayInfo.trucNgay.rating == "Xấu" ? BadRed : TextMain)
            )
            InfoGridCard(
                icon: "shield.fill", label: "Sao tốt",
                value: dayInfo.saoChieu.name,
                valueColor: dayInfo.saoChieu.rating == "Tốt" ? GoodGreen : (dayInfo.saoChieu.rating == "Xấu" ? BadRed : TextMain)
            )
            InfoGridCard(
                icon: "safari.fill", label: "Hướng tốt",
                value: "Thần Tài: \(dayInfo.huong.thanTai)"
            )
        }
    }
}

private struct InfoGridCard: View {
    let icon: String
    let label: String
    let value: String
    var valueColor: Color = TextMain

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
                Text(label)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(TextDim)
            }
            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(valueColor)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(SurfaceContainer)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVar, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ══════════════════════════════════════════
// SECTION HEADER
// ══════════════════════════════════════════

private struct DetailSection<Content: View>: View {
    let icon: String
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(PrimaryRed)
                Text(title)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(TextMain)
            }
            content()
        }
    }
}

// ══════════════════════════════════════════
// HOURS GRID (Giờ hoàng đạo)
// ══════════════════════════════════════════

private struct HoursGrid: View {
    let hours: [GioHoangDaoInfo]
    let jd: Int

    private let allHours: [(String, String)] = [
        ("Tý", "23h-1h"), ("Sửu", "1h-3h"), ("Dần", "3h-5h"),
        ("Mão", "5h-7h"), ("Thìn", "7h-9h"), ("Tỵ", "9h-11h"),
        ("Ngọ", "11h-13h"), ("Mùi", "13h-15h"), ("Thân", "15h-17h"),
        ("Dậu", "17h-19h"), ("Tuất", "19h-21h"), ("Hợi", "21h-23h")
    ]

    var body: some View {
        let goodNames = Set(hours.map { $0.name })
        // Bad hours are specific: Thìn, Tị, Ngọ could be bad etc - we mark non-good as neutral, specific bad hours from calculator
        let layout = [GridItem(.adaptive(minimum: 72), spacing: 6)]

        LazyVGrid(columns: layout, spacing: 6) {
            ForEach(allHours, id: \.0) { name, time in
                let isGood = goodNames.contains(name)
                HourChip(name: name, time: time, type: isGood ? .good : .neutral)
            }
        }
    }
}

private enum HourType { case good, bad, neutral }

private struct HourChip: View {
    let name: String
    let time: String
    let type: HourType

    var body: some View {
        VStack(spacing: 1) {
            Text(name)
                .font(.system(size: 11, weight: .bold))
            Text(time)
                .font(.system(size: 10))
                .opacity(0.7)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .padding(.horizontal, 8)
        .background(chipBg)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(chipBorder, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .foregroundColor(chipFg)
    }

    private var chipBg: Color {
        switch type {
        case .good: return Color(hex: "E8F5E9")
        case .bad: return Color(hex: "FFEBEE")
        case .neutral: return SurfaceContainer
        }
    }

    private var chipBorder: Color {
        switch type {
        case .good: return Color(hex: "81C784")
        case .bad: return Color(hex: "EF9A9A")
        case .neutral: return OutlineVar
        }
    }

    private var chipFg: Color {
        switch type {
        case .good: return GoodGreen
        case .bad: return BadRed
        case .neutral: return TextMain
        }
    }
}

// ══════════════════════════════════════════
// ACTIVITIES (Nên / Kỵ)
// ══════════════════════════════════════════

private struct ActivitiesSection: View {
    let activities: DayActivitiesInfo

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Nên làm
            if !activities.nenLam.isEmpty {
                HStack(spacing: 4) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(GoodGreen)
                    Text("Việc nên làm")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(GoodGreen)
                }

                FlowLayout(spacing: 6) {
                    ForEach(activities.nenLam, id: \.self) { item in
                        ActivityTag(text: item, isGood: true)
                    }
                }
            }

            // Không nên
            if !activities.khongNen.isEmpty {
                HStack(spacing: 4) {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(BadRed)
                    Text("Việc nên tránh")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(BadRed)
                }

                FlowLayout(spacing: 6) {
                    ForEach(activities.khongNen, id: \.self) { item in
                        ActivityTag(text: item, isGood: false)
                    }
                }
            }
        }
    }
}

private struct ActivityTag: View {
    let text: String
    let isGood: Bool

    var body: some View {
        Text(text)
            .font(.system(size: 12, weight: .medium))
            .foregroundColor(isGood ? GoodGreen : Color(hex: "E65100"))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isGood ? Color(hex: "E8F5E9") : Color(hex: "FFF3E0"))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isGood ? Color(hex: "C8E6C9") : Color(hex: "FFCC80"), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// ══════════════════════════════════════════
// FLOW LAYOUT (for wrapping tags)
// ══════════════════════════════════════════

private struct FlowLayout: Layout {
    var spacing: CGFloat = 6

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = flowLayout(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = flowLayout(proposal: ProposedViewSize(width: bounds.width, height: bounds.height), subviews: subviews)
        for (index, offset) in result.offsets.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + offset.x, y: bounds.minY + offset.y), proposal: .unspecified)
        }
    }

    private func flowLayout(proposal: ProposedViewSize, subviews: Subviews) -> (offsets: [CGPoint], size: CGSize) {
        let maxWidth = proposal.width ?? .infinity
        var offsets: [CGPoint] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }
            offsets.append(CGPoint(x: currentX, y: currentY))
            lineHeight = max(lineHeight, size.height)
            currentX += size.width + spacing
            maxX = max(maxX, currentX)
        }

        return (offsets, CGSize(width: maxX, height: currentY + lineHeight))
    }
}

// ══════════════════════════════════════════
// EVENTS SECTION
// ══════════════════════════════════════════

private struct EventsSection: View {
    let dayInfo: DayInfo

    var body: some View {
        VStack(spacing: 10) {
            // Solar holiday
            if let holiday = dayInfo.solarHoliday {
                EventCard(
                    icon: "party.popper.fill",
                    iconBg: Color(hex: "FFF3E0"),
                    iconColor: Color(hex: "E65100"),
                    title: holiday,
                    description: "\(dayInfo.solar.dd)/\(dayInfo.solar.mm) Dương lịch · Nghỉ lễ"
                )
            }

            // Lunar holiday
            if let holiday = dayInfo.lunarHoliday {
                EventCard(
                    icon: "moon.stars.fill",
                    iconBg: Color(hex: "FFF8E1"),
                    iconColor: Color(hex: "F57F17"),
                    title: holiday,
                    description: "\(dayInfo.lunar.day)/\(dayInfo.lunar.month) Âm lịch"
                )
            }

            // Rằm / Mùng 1
            if dayInfo.isRam {
                EventCard(
                    icon: "moon.fill",
                    iconBg: Color(hex: "FFF8E1"),
                    iconColor: GoldAccent,
                    title: "Rằm tháng \(dayInfo.lunar.month)",
                    description: "Ngày rằm Âm lịch — Lễ Phật, cầu an"
                )
            }
            if dayInfo.isMung1 {
                EventCard(
                    icon: "sunrise.fill",
                    iconBg: Color(hex: "E8F5E9"),
                    iconColor: GoodGreen,
                    title: "Mùng 1 tháng \(dayInfo.lunar.month)",
                    description: "Ngày đầu tháng Âm lịch — Lễ Phật, sóc vọng"
                )
            }

            // Day info (Trực + Sao)
            EventCard(
                icon: "info.circle.fill",
                iconBg: Color(hex: "E3F2FD"),
                iconColor: Color(hex: "1565C0"),
                title: "Trực \(dayInfo.trucNgay.name) · Sao \(dayInfo.saoChieu.name)",
                description: "Trực: \(dayInfo.trucNgay.rating) · Sao: \(dayInfo.saoChieu.rating)"
            )
        }
    }
}

private struct EventCard: View {
    let icon: String
    let iconBg: Color
    let iconColor: Color
    let title: String
    let description: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(iconColor)
                .frame(width: 40, height: 40)
                .background(iconBg)
                .clipShape(RoundedRectangle(cornerRadius: 12))

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                Text(description)
                    .font(.system(size: 12))
                    .foregroundColor(TextSub)
                    .lineSpacing(2)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(SurfaceContainer)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVar, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ══════════════════════════════════════════
// QUOTE CARD
// ══════════════════════════════════════════

private struct DayQuoteCard: View {
    let day: Int
    let month: Int

    var body: some View {
        let dayOfYear = (month - 1) * 30 + day
        let (text, author) = VietnameseQuotes.ofDay(dayOfYear)

        VStack(spacing: 8) {
            Text("\u{201C}\(text)\u{201D}")
                .font(.system(size: 14, weight: .regular, design: .serif))
                .italic()
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Text("— \(author)")
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(TextDim)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(colors: [Color(hex: "FFF8E1"), Color(hex: "FFFDE7")],
                           startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "FFE082"), lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// ══════════════════════════════════════════
// ASK AI BUTTON
// ══════════════════════════════════════════

private struct AskAiButton: View {
    var body: some View {
        Button(action: {
            // TODO: Navigate to AI chat with this day context
        }) {
            HStack(spacing: 8) {
                Image(systemName: "sparkles")
                    .font(.system(size: 18))
                    .foregroundColor(GoldAccent)
                Text("Hỏi AI về ngày này")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(
                LinearGradient(colors: [PrimaryRed, Color(hex: "C62828")],
                               startPoint: .leading, endPoint: .trailing)
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
    }
}

// ══════════════════════════════════════════
#Preview {
    let provider = DayInfoProvider.shared
    let info = provider.getDayInfo(dd: 9, mm: 4, yy: 2026)
    DayDetailScreen(dayInfo: info, onDismiss: {})
}
