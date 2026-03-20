import SwiftUI

// MARK: - Shared UI Components

// MARK: - Section Label
struct SectionLabel: View {
    let text: String
    @Environment(\.lichSoColors) var c

    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text)
            .font(.system(size: 10.5, weight: .bold))
            .foregroundColor(c.textTertiary)
            .kerning(1.0)
    }
}

// MARK: - Can Chi Chip
struct CanChiChip: View {
    let text: String
    let textColor: Color
    let bgColor: Color
    let borderColor: Color

    var body: some View {
        Text(text)
            .font(.system(size: 13, weight: .medium))
            .foregroundColor(textColor)
            .padding(.horizontal, 14)
            .padding(.vertical, 5)
            .background(bgColor)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(borderColor, lineWidth: 1))
    }
}

// MARK: - Activity Card
struct ActivityCard: View {
    let title: String
    let icon: String
    let titleColor: Color
    let items: [String]
    let itemColor: Color
    var specialLastItemRed: Bool = false
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                    .foregroundColor(titleColor)
                Text(title.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(titleColor)
                    .kerning(0.8)
            }
            ForEach(Array(items.enumerated()), id: \.offset) { idx, item in
                let color = (specialLastItemRed && idx == items.count - 1) ? c.red2 : itemColor
                Text("• \(item)")
                    .font(.system(size: 12))
                    .foregroundColor(color)
                    .lineSpacing(4)
            }
        }
        .padding(11)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
    }
}

// MARK: - Event Row
struct EventRow: View {
    let event: UpcomingEvent
    @Environment(\.lichSoColors) var c

    var body: some View {
        let (dotColor, tagBg, tagColor): (Color, Color, Color) = {
            switch event.colorType {
            case .gold: return (c.gold, c.goldDim, c.gold)
            case .teal: return (c.teal, c.tealDim, c.teal2)
            case .red:  return (c.red, c.red.opacity(0.12), c.red2)
            }
        }()

        HStack(spacing: 11) {
            Circle()
                .fill(dotColor)
                .frame(width: 8, height: 8)
            VStack(alignment: .leading, spacing: 2) {
                Text(event.title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(c.textPrimary)
                Text(event.timeLabel)
                    .font(.system(size: 11))
                    .foregroundColor(c.textTertiary)
            }
            Spacer()
            Text(event.tag)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(tagColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(tagBg)
                .clipShape(Capsule())
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 11)
        .background(c.bg2)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(c.border, lineWidth: 1))
    }
}

// MARK: - Activity Grid
struct ActivityGrid: View {
    let info: DayInfo
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                ActivityCard(
                    title: "Nên làm",
                    icon: "checkmark.circle",
                    titleColor: c.teal2,
                    items: info.activities.nenLam,
                    itemColor: c.teal2
                )
                ActivityCard(
                    title: "Không nên",
                    icon: "xmark.circle",
                    titleColor: c.red2,
                    items: info.activities.khongNen,
                    itemColor: c.red2
                )
            }
            HStack(spacing: 8) {
                ActivityCard(
                    title: "Giờ hoàng đạo",
                    icon: "star",
                    titleColor: c.gold2,
                    items: info.gioHoangDao.map { "\($0.name) (\($0.time))" },
                    itemColor: c.gold2
                )
                ActivityCard(
                    title: "Hướng tốt",
                    icon: "location",
                    titleColor: c.gold2,
                    items: [
                        "Thần tài: \(info.huong.thanTai)",
                        "Hỷ thần: \(info.huong.hyThan)",
                        "Hung thần: \(info.huong.hungThan)"
                    ],
                    itemColor: c.gold2,
                    specialLastItemRed: true
                )
            }
        }
        .padding(.horizontal, 20)
    }
}

// MARK: - Tiết Khí Bar
struct TietKhiBar: View {
    let tietKhi: TietKhiInfo
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "sun.max")
                .font(.system(size: 14))
                .foregroundColor(c.teal.opacity(0.8))
            Text(tietKhiText)
                .font(.system(size: 12))
                .foregroundColor(c.textSecondary)
            Spacer()
            Image(systemName: "chevron.right")
                .font(.system(size: 11))
                .foregroundColor(c.textTertiary)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(
            c.isDark
            ? LinearGradient(colors: [Color(hex: 0xE8C84A).opacity(0.07), Color(hex: 0xE8C84A).opacity(0.02)], startPoint: .leading, endPoint: .trailing)
            : LinearGradient(colors: [Color(hex: 0xC4A020).opacity(0.08), Color(hex: 0xC4A020).opacity(0.03)], startPoint: .leading, endPoint: .trailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(c.isDark ? Color(hex: 0xE8C84A).opacity(0.16) : Color(hex: 0xC4A020).opacity(0.13), lineWidth: 1)
        )
        .padding(.horizontal, 20)
    }

    var tietKhiText: String {
        if tietKhi.daysUntilNext == 0 {
            return "Hôm nay: \(tietKhi.currentName ?? "")"
        } else {
            return "Tiết khí: \(tietKhi.nextName ?? "") — \(tietKhi.nextDd ?? 0)/\(tietKhi.nextMm ?? 0) (còn \(tietKhi.daysUntilNext) ngày)"
        }
    }
}

// MARK: - Robot FAB
struct RobotFAB: View {
    let colors: LichSoColors

    var body: some View {
        ZStack {
            Circle()
                .fill(
                    LinearGradient(
                        colors: [colors.teal, Color(hex: 0x237A6A)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
            RobotIcon(color: .white)
                .padding(12)
        }
        .frame(width: 56, height: 56)
        .shadow(color: colors.teal.opacity(0.4), radius: 8, x: 0, y: 4)
    }
}

// MARK: - Robot Icon (Canvas)
struct RobotIcon: View {
    let color: Color
    @State private var headTilt: Double = 0
    @State private var blinkPhase: Double = 0
    @State private var antennaGlow: Double = 0.5
    @State private var antennaBounce: Double = 0

    var body: some View {
        Canvas { ctx, size in
            let w = size.width
            let h = size.height
            let cx = w / 2
            let cy = h / 2
            let sc = min(w, h) / 24.0
            let strokeW = 1.6 * sc

            ctx.translateBy(x: cx, y: cy)
            ctx.rotate(by: .degrees(headTilt))
            ctx.translateBy(x: -cx, y: -cy)

            // Body
            let bodyRect = CGRect(x: 5*sc, y: 11*sc, width: 14*sc, height: 10*sc)
            var bodyPath = Path(roundedRect: bodyRect, cornerRadius: 2*sc)
            ctx.stroke(bodyPath, with: .color(color), style: StrokeStyle(lineWidth: strokeW, lineCap: .round, lineJoin: .round))

            // Antenna
            var antPath = Path()
            antPath.move(to: CGPoint(x: 12*sc, y: 11*sc))
            antPath.addLine(to: CGPoint(x: 12*sc, y: (7 + antennaBounce)*sc))
            ctx.stroke(antPath, with: .color(color), style: StrokeStyle(lineWidth: strokeW, lineCap: .round))

            // Antenna ball glow
            ctx.fill(
                Path(ellipseIn: CGRect(x: (12-1.5)*sc, y: (5-1.5 + antennaBounce)*sc, width: 3*sc, height: 3*sc)),
                with: .color(color.opacity(antennaGlow * 0.3))
            )
            ctx.stroke(
                Path(ellipseIn: CGRect(x: (12-2)*sc, y: (5-2 + antennaBounce)*sc, width: 4*sc, height: 4*sc)),
                with: .color(color),
                style: StrokeStyle(lineWidth: strokeW)
            )
            ctx.fill(
                Path(ellipseIn: CGRect(x: (12-0.35)*sc, y: (5-0.35 + antennaBounce)*sc, width: 0.7*sc, height: 0.7*sc)),
                with: .color(color.opacity(antennaGlow))
            )

            // Eyes
            let eyeH = 2 * sc * (1 - blinkPhase)
            if eyeH > 0.1 {
                ctx.fill(
                    Path(roundedRect: CGRect(x: 7*sc, y: 14*sc - eyeH/2, width: 2.5*sc, height: eyeH), cornerRadius: 0.5*sc),
                    with: .color(color)
                )
                ctx.fill(
                    Path(roundedRect: CGRect(x: 14.5*sc, y: 14*sc - eyeH/2, width: 2.5*sc, height: eyeH), cornerRadius: 0.5*sc),
                    with: .color(color)
                )
            }

            // Smile
            var smilePath = Path()
            smilePath.addArc(center: CGPoint(x: 12*sc, y: 17.25*sc), radius: 2.5*sc, startAngle: .degrees(20), endAngle: .degrees(160), clockwise: false)
            ctx.stroke(smilePath, with: .color(color), style: StrokeStyle(lineWidth: strokeW * 0.7, lineCap: .round))
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 2.2).repeatForever(autoreverses: true)) { headTilt = 6 }
            withAnimation(.easeInOut(duration: 1.2).repeatForever(autoreverses: true)) { antennaGlow = 1.0 }
            withAnimation(.easeInOut(duration: 0.9).repeatForever(autoreverses: true)) { antennaBounce = -1.2 }
            // Blink every ~3.5s
            Timer.scheduledTimer(withTimeInterval: 3.5, repeats: true) { _ in
                withAnimation(.linear(duration: 0.15)) { blinkPhase = 1.0 }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                    withAnimation(.linear(duration: 0.15)) { blinkPhase = 0.0 }
                }
            }
        }
    }
}
