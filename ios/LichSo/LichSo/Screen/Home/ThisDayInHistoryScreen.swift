import SwiftUI

// ═══════════════════════════════════════════
// ThisDayInHistoryScreen — Ngày này năm xưa
// Timeline of historical events on a given day
// Supports swiping to prev/next day
// ═══════════════════════════════════════════

// MARK: - Design Tokens

private var HistGreen:      Color { Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "388E3C") : UIColor(hex: "2E7D32") }) }
private var HistGreenDeep:  Color { Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "1B5E20") : UIColor(hex: "1B5E20") }) }
private var SurfBg:         Color { LSTheme.bg }
private var SurfCard:       Color { LSTheme.surfaceContainer }
private var TextMain:       Color { LSTheme.textPrimary }
private var TextSub:        Color { LSTheme.textSecondary }
private var TextDim:        Color { LSTheme.textTertiary }
private var OutlineVar:     Color { LSTheme.outlineVariant }
private var VietRed:        Color { LSTheme.primary }
private var WorldBlue:      Color { Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "90CAF9") : UIColor(hex: "1565C0") }) }
private var ScienceOrange:  Color { Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "FFCC80") : UIColor(hex: "E65100") }) }
private var CultureGold:    Color { LSTheme.gold }

// MARK: - Screen

struct ThisDayInHistoryScreen: View {
    /// Initial date — defaults to today if not provided
    var initialDay: Int = Calendar.current.component(.day, from: Date())
    var initialMonth: Int = Calendar.current.component(.month, from: Date())
    var initialYear: Int = Calendar.current.component(.year, from: Date())

    @Environment(\.dismiss) private var dismiss
    @State private var currentDate: Date

    private var cal: Calendar { Calendar.current }
    private var day:   Int { cal.component(.day,   from: currentDate) }
    private var month: Int { cal.component(.month, from: currentDate) }
    private var year:  Int { cal.component(.year,  from: currentDate) }

    private var events: [HistoricalEvent] {
        HistoricalEventProvider.getEvents(day: day, month: month)
    }

    private var formattedDate: String {
        let df = DateFormatter()
        df.locale = Locale(identifier: "vi_VN")
        df.dateFormat = "EEEE, dd/MM/yyyy"
        let s = df.string(from: currentDate)
        return s.prefix(1).uppercased() + s.dropFirst()
    }

    init(initialDay: Int? = nil, initialMonth: Int? = nil, initialYear: Int? = nil) {
        let cal = Calendar.current
        let today = Date()
        let d = initialDay   ?? cal.component(.day,   from: today)
        let m = initialMonth ?? cal.component(.month, from: today)
        let y = initialYear  ?? cal.component(.year,  from: today)
        let date = cal.date(from: DateComponents(year: y, month: m, day: d)) ?? today
        _currentDate = State(initialValue: date)
    }

    var body: some View {
        VStack(spacing: 0) {
            headerSection
            datePicker
            contentSection
        }
        .background(SurfBg.ignoresSafeArea())
        .navigationBarHidden(true)
    }

    // MARK: - Header

    private var headerSection: some View {
        ZStack(alignment: .bottom) {
            // Decorative glow
            Circle()
                .fill(
                    RadialGradient(
                        colors: [Color(hex: "FFD700").opacity(0.12), .clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 90
                    )
                )
                .frame(width: 180, height: 180)
                .offset(x: 100, y: -30)

            VStack(alignment: .leading, spacing: 0) {
                // Top bar
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                    Spacer()
                    Text("Ngày này năm xưa")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                    Spacer()
                    Button {
                        shareContent()
                    } label: {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)

                Spacer().frame(height: 6)

                // Date display
                HStack(alignment: .lastTextBaseline, spacing: 8) {
                    Text(String(format: "%02d", day))
                        .font(.system(size: 40, weight: .bold))
                        .foregroundColor(.white)
                    Text("Tháng \(month)")
                        .font(.system(size: 17, weight: .medium))
                        .foregroundColor(.white.opacity(0.85))
                }
                .padding(.leading, 68)
                .padding(.bottom, 14)
            }
        }
        .background(
            LinearGradient(
                colors: [
                    Color(hex: "1B5E20"),
                    Color(hex: "2E7D32"),
                    Color(hex: "388E3C")
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }

    // MARK: - Date Picker Row

    private var datePicker: some View {
        HStack(spacing: 16) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    currentDate = cal.date(byAdding: .day, value: -1, to: currentDate) ?? currentDate
                }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                    .frame(width: 36, height: 36)
                    .overlay(
                        Circle().stroke(OutlineVar, lineWidth: 1)
                    )
            }

            Text(formattedDate)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(TextMain)
                .frame(minWidth: 180)
                .multilineTextAlignment(.center)

            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    currentDate = cal.date(byAdding: .day, value: 1, to: currentDate) ?? currentDate
                }
            } label: {
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(TextMain)
                    .frame(width: 36, height: 36)
                    .overlay(
                        Circle().stroke(OutlineVar, lineWidth: 1)
                    )
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 20)
        .background(SurfBg)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(OutlineVar)
                .frame(height: 0.5)
        }
    }

    // MARK: - Content

    @ViewBuilder
    private var contentSection: some View {
        if events.isEmpty {
            emptyState
        } else {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    timelineView
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 40)
            }
        }
    }

    // MARK: - Empty State

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 52))
                .foregroundColor(HistGreen.opacity(0.4))
            Text("Chưa có dữ liệu")
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(TextMain)
            Text("Ngày \(String(format: "%02d", day))/\(String(format: "%02d", month)) chưa có sự kiện lịch sử nào được ghi nhận.")
                .font(.system(size: 14))
                .foregroundColor(TextSub)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Timeline

    private var timelineView: some View {
        let currentYear = cal.component(.year, from: Date())

        return ZStack(alignment: .topLeading) {
            // Vertical line
            Rectangle()
                .fill(OutlineVar)
                .frame(width: 2)
                .padding(.leading, 10)
                .padding(.top, 4)
                .padding(.bottom, 0)

            VStack(alignment: .leading, spacing: 20) {
                ForEach(events) { event in
                    TimelineItemView(
                        event: event,
                        currentYear: currentYear,
                        vietRedColor: VietRed,
                        worldBlueColor: WorldBlue,
                        scienceOrangeColor: ScienceOrange,
                        cultureGoldColor: CultureGold,
                        surfCardColor: SurfCard,
                        outlineVarColor: OutlineVar,
                        textMainColor: TextMain,
                        textSubColor: TextSub,
                        textDimColor: TextDim
                    )
                }
            }
        }
    }

    // MARK: - Share

    private func shareContent() {
        let currentYear = cal.component(.year, from: Date())
        var text = "📅 Ngày này năm xưa — \(String(format: "%02d", day))/\(String(format: "%02d", month))\n\n"
        for event in events {
            let ago = currentYear - event.year
            text += "• \(event.year) (\(ago) năm trước): \(event.title)\n"
        }
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = scene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }
}

// MARK: - Timeline Item View

private struct TimelineItemView: View {
    let event: HistoricalEvent
    let currentYear: Int
    let vietRedColor: Color
    let worldBlueColor: Color
    let scienceOrangeColor: Color
    let cultureGoldColor: Color
    let surfCardColor: Color
    let outlineVarColor: Color
    let textMainColor: Color
    let textSubColor: Color
    let textDimColor: Color

    @State private var expanded = false

    private var yearsAgo: Int { currentYear - event.year }

    private var dotColor: Color {
        switch event.category {
        case .vietnam:  return vietRedColor
        case .world:    return worldBlueColor
        case .science:  return scienceOrangeColor
        case .culture:  return cultureGoldColor
        }
    }

    private var dotIcon: String {
        switch event.category {
        case .vietnam:  return "flag.fill"
        case .world:    return "globe"
        case .science:  return "atom"
        case .culture:  return "music.note"
        }
    }

    private var categoryLabel: String {
        switch event.category {
        case .vietnam:  return "🇻🇳 Lịch sử Việt Nam"
        case .world:    return "🌍 Thế giới"
        case .science:  return "🔬 Khoa học"
        case .culture:  return "🎭 Văn hoá"
        }
    }

    private var categoryBg: Color {
        switch event.category {
        case .vietnam:  return Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "3D1515") : UIColor(hex: "FFEBEE") })
        case .world:    return Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "0D2B4E") : UIColor(hex: "E3F2FD") })
        case .science:  return Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "3E2000") : UIColor(hex: "FFF3E0") })
        case .culture:  return Color(UIColor { t in t.userInterfaceStyle == .dark ? UIColor(hex: "2E2300") : UIColor(hex: "FFFDE7") })
        }
    }

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            // Dot
            ZStack {
                if event.importance == .major {
                    Circle()
                        .fill(dotColor)
                        .frame(width: 22, height: 22)
                    Image(systemName: dotIcon)
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                } else {
                    Circle()
                        .fill(surfCardColor)
                        .frame(width: 22, height: 22)
                        .overlay(Circle().stroke(outlineVarColor, lineWidth: 2))
                    Image(systemName: dotIcon)
                        .font(.system(size: 9))
                        .foregroundColor(dotColor)
                }
            }
            .offset(y: 2)

            // Card
            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                    expanded.toggle()
                }
            } label: {
                VStack(alignment: .leading, spacing: 6) {
                    // Year row
                    HStack(spacing: 6) {
                        Text(String(event.year))
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(dotColor)
                        Text("· \(yearsAgo) năm trước")
                            .font(.system(size: 11))
                            .foregroundColor(textDimColor)
                        Spacer()
                        Image(systemName: expanded ? "chevron.up" : "chevron.down")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(textDimColor)
                    }

                    // Title
                    Text(event.title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(textMainColor)
                        .multilineTextAlignment(.leading)
                        .fixedSize(horizontal: false, vertical: true)

                    // Description (expandable)
                    if expanded {
                        Text(event.description)
                            .font(.system(size: 12))
                            .foregroundColor(textSubColor)
                            .multilineTextAlignment(.leading)
                            .fixedSize(horizontal: false, vertical: true)
                            .transition(.opacity.combined(with: .move(edge: .top)))
                    }

                    // Category tag
                    Text(categoryLabel)
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(dotColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(categoryBg)
                        .clipShape(RoundedRectangle(cornerRadius: 6))

                    // Image placeholder for hasImage events
                    if expanded && event.hasImage {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(outlineVarColor.opacity(0.3))
                            .frame(height: 100)
                            .overlay(
                                Image(systemName: "photo")
                                    .font(.system(size: 24))
                                    .foregroundColor(outlineVarColor)
                            )
                            .transition(.opacity)
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(surfCardColor)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(outlineVarColor, lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            .buttonStyle(.plain)
        }
    }
}
