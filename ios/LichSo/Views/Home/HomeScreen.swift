import SwiftUI

// MARK: - Home Screen
struct HomeScreen: View {
    @ObservedObject var viewModel: HomeViewModel
    @Environment(\.lichSoColors) var c
    var onSettings: () -> Void = {}
    @State private var currentTime = Date()
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    // Đọc từ UserDefaults trực tiếp — sync với SettingsViewModel
    @AppStorage("lunarBadgeEnabled") private var lunarBadgeEnabled: Bool = true

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Sparkle
                HStack {
                    Image(systemName: "sparkles")
                        .font(.system(size: 14))
                        .foregroundColor(c.gold.opacity(0.35))
                        .padding(.leading, 16)
                        .padding(.top, 8)
                    Spacer()
                }

                if let info = viewModel.dayInfo {
                    // Hero Card
                    DayHeroCard(info: info,
                                onPrev: viewModel.prevDay,
                                onNext: viewModel.nextDay,
                                onSettings: onSettings,
                                showLunar: lunarBadgeEnabled)
                        .padding(.top, 4)

                    // Clock
                    LiveClockSection(currentTime: currentTime, gioHoangDao: info.gioHoangDao)
                        .padding(.top, 20)

                    Spacer(minLength: 18)

                    // Tiết Khí
                    TietKhiBar(tietKhi: info.tietKhi)

                    Spacer(minLength: 14)

                    // Day Info
                    SectionLabel("THÔNG TIN NGÀY \(info.solar.dd)/\(info.solar.mm)")
                    Spacer(minLength: 7)
                    ActivityGrid(info: info)

                    Spacer(minLength: 14)

                    // Events
                    if !viewModel.upcomingEvents.isEmpty {
                        SectionLabel("SỰ KIỆN SẮP TỚI")
                        Spacer(minLength: 7)
                        VStack(spacing: 7) {
                            ForEach(viewModel.upcomingEvents) { event in
                                EventRow(event: event)
                                    .padding(.horizontal, 20)
                            }
                        }
                    }
                }

                Spacer(minLength: 32)
            }
        }
        .background(c.bg.ignoresSafeArea())
        .onReceive(timer) { currentTime = $0 }
    }
}

// MARK: - Day Hero Card
struct DayHeroCard: View {
    let info: DayInfo
    let onPrev: () -> Void
    let onNext: () -> Void
    let onSettings: () -> Void
    var showLunar: Bool = true
    @Environment(\.lichSoColors) var c

    var body: some View {
        ZStack(alignment: .topTrailing) {
            // Card background
            RoundedRectangle(cornerRadius: 24)
                .fill(
                    LinearGradient(
                        colors: c.isDark
                        ? [Color(hex: 0x1A1710), Color(hex: 0x1E1C14), c.bg]
                        : [Color(hex: 0xFCFAF5), Color(hex: 0xF8F5ED), c.bg],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 24)
                        .stroke(c.isDark ? Color(hex: 0xE8C84A).opacity(0.13) : Color(hex: 0xA08520).opacity(0.11), lineWidth: 1)
                )

            VStack(spacing: 0) {
                Spacer(minLength: 24)

                // Big Solar Day
                Text("\(info.solar.dd)")
                    .font(.system(size: 96, weight: .bold, design: .serif))
                    .foregroundColor(c.textPrimary)
                    .tracking(-3)

                // Day of week
                Text(info.dayOfWeek.uppercased().map { String($0) }.joined(separator: "  "))
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(c.textTertiary)
                    .kerning(4)

                Spacer(minLength: 4)

                // Month · Year
                Text("Tháng \(info.solar.mm) · \(info.solar.yy)")
                    .font(.system(size: 18, design: .serif))
                    .foregroundColor(c.textSecondary)

                Spacer(minLength: 20)

                // Navigator
                HStack(spacing: 16) {
                    Button(action: onPrev) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(c.textTertiary)
                    }
                    .frame(width: 32, height: 32)

                    // Decorative line
                    HStack(spacing: 10) {
                        Rectangle().fill(c.border).frame(width: 60, height: 1)
                        Image(systemName: "star")
                            .font(.system(size: 12))
                            .foregroundColor(c.gold.opacity(0.6))
                        Rectangle().fill(c.border).frame(width: 60, height: 1)
                    }

                    Button(action: onNext) {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(c.textTertiary)
                    }
                    .frame(width: 32, height: 32)
                }

                Spacer(minLength: 24)

                // Lunar section
                if showLunar {
                    Text("Â M   L Ị C H")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(c.textTertiary)
                        .kerning(3)

                    Spacer(minLength: 6)

                    Text("\(info.lunar.day)")
                        .font(.system(size: 52, weight: .bold, design: .serif))
                        .foregroundColor(c.teal)
                        .tracking(-2)

                    Spacer(minLength: 4)

                    Text("\(info.lunar.monthName) · Năm \(info.yearCanChi)")
                        .font(.system(size: 14))
                        .foregroundColor(c.textSecondary)

                    Spacer(minLength: 14)

                    // Can Chi chips
                    HStack(spacing: 8) {
                        CanChiChip(text: info.dayCanChi, textColor: c.red2, bgColor: c.red.opacity(0.12), borderColor: c.red.opacity(0.3))
                        CanChiChip(text: info.monthCanChi, textColor: c.textSecondary, bgColor: c.surface, borderColor: c.border)
                        CanChiChip(
                            text: String(info.yearCanChi.suffix(min(info.yearCanChi.count, 6))),
                            textColor: c.textSecondary, bgColor: c.surface, borderColor: c.border
                        )
                    }
                    .padding(.horizontal, 20)

                    // Holiday badge
                    if let holiday = info.solarHoliday ?? info.lunarHoliday {
                        Spacer(minLength: 14)
                        HStack(spacing: 6) {
                            Image(systemName: "party.popper")
                                .font(.system(size: 12))
                                .foregroundColor(c.gold2)
                            Text(holiday)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(c.gold2)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 5)
                        .background(c.goldDim)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(c.gold.opacity(0.3), lineWidth: 1))
                    }
                } else {
                    // Khi tắt lịch âm — chỉ hiện Can Chi năm nhỏ gọn
                    HStack(spacing: 8) {
                        CanChiChip(text: info.dayCanChi, textColor: c.red2, bgColor: c.red.opacity(0.12), borderColor: c.red.opacity(0.3))
                        CanChiChip(text: info.monthCanChi, textColor: c.textSecondary, bgColor: c.surface, borderColor: c.border)
                        CanChiChip(
                            text: String(info.yearCanChi.suffix(min(info.yearCanChi.count, 6))),
                            textColor: c.textSecondary, bgColor: c.surface, borderColor: c.border
                        )
                    }
                    .padding(.horizontal, 20)

                    if let holiday = info.solarHoliday ?? info.lunarHoliday {
                        Spacer(minLength: 10)
                        HStack(spacing: 6) {
                            Image(systemName: "party.popper")
                                .font(.system(size: 12))
                                .foregroundColor(c.gold2)
                            Text(holiday)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(c.gold2)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 5)
                        .background(c.goldDim)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(c.gold.opacity(0.3), lineWidth: 1))
                    }
                }

                Spacer(minLength: 28)
            }
            .frame(maxWidth: .infinity)

            // Settings button top-right
            Button(action: onSettings) {
                Image(systemName: "gearshape")
                    .font(.system(size: 16))
                    .foregroundColor(c.textTertiary)
            }
            .padding(18)
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - Live Clock
struct LiveClockSection: View {
    let currentTime: Date
    let gioHoangDao: [GioHoangDaoInfo]
    @Environment(\.lichSoColors) var c

    private var timeString: String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss"
        return f.string(from: currentTime)
    }

    private var gioName: String {
        let h = Calendar.current.component(.hour, from: currentTime)
        return LunarCalendarEngine.gioName(h)
    }

    private var isHoangDao: Bool {
        gioHoangDao.contains { $0.name == gioName }
    }

    var body: some View {
        VStack(spacing: 4) {
            Text(timeString)
                .font(.system(size: 36, weight: .bold, design: .serif))
                .foregroundColor(c.textPrimary)
                .monospacedDigit()
                .tracking(2)

            HStack(spacing: 6) {
                Text("Giờ \(gioName)")
                    .font(.system(size: 13))
                    .foregroundColor(c.textSecondary)

                if isHoangDao {
                    Text("·")
                        .foregroundColor(c.textTertiary)
                    Image(systemName: "sparkles")
                        .font(.system(size: 11))
                        .foregroundColor(c.teal2)
                    Text("Hoàng Đạo")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(c.teal2)
                }
            }
        }
        .frame(maxWidth: .infinity)
    }
}
