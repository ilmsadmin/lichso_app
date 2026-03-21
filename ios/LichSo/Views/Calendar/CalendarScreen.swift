import SwiftUI

// MARK: - Calendar Screen (Modern Redesign)
struct CalendarScreen: View {
    @ObservedObject var viewModel: CalendarViewModel
    @Environment(\.lichSoColors) var c
    @Environment(\.dismiss) var dismiss
    @State private var showDayDetail = false
    @AppStorage("lunarBadgeEnabled") private var lunarBadgeEnabled: Bool = true

    var body: some View {
        NavigationView {
            ZStack {
                c.bg.ignoresSafeArea()

                ScrollView(showsIndicators: false) {
                    VStack(spacing: 16) {
                        // ── Month Navigation ──
                        CalendarMonthNav(
                            month: viewModel.currentMonth,
                            year: viewModel.currentYear,
                            onPrev: viewModel.prevMonth,
                            onNext: viewModel.nextMonth,
                            onToday: viewModel.goToToday
                        )

                        // ── Calendar Grid Card ──
                        CalendarGridCard(
                            days: viewModel.calendarDays,
                            selectedDate: viewModel.selectedDate,
                            showLunar: lunarBadgeEnabled,
                            onDayTap: { day in
                                viewModel.selectDay(day)
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                    showDayDetail = true
                                }
                            }
                        )

                        // ── Selected Day Quick Info ──
                        if let info = viewModel.selectedDayInfo {
                            CalendarDayQuickInfo(info: info)
                                .transition(.asymmetric(
                                    insertion: .move(edge: .bottom).combined(with: .opacity),
                                    removal: .opacity
                                ))
                        }

                        Spacer(minLength: 40)
                    }
                    .padding(.top, 8)
                }

                // ── Day Detail Sheet ──
                if showDayDetail, let info = viewModel.selectedDayInfo {
                    CalendarDayDetailSheet(info: info, onDismiss: {
                        withAnimation(.easeOut(duration: 0.25)) { showDayDetail = false }
                    })
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .zIndex(10)
                }
            }
            .navigationTitle("Lịch Tháng")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 20))
                            .foregroundColor(c.textTertiary)
                            .symbolRenderingMode(.hierarchical)
                    }
                }
                ToolbarItem(placement: .navigationBarLeading) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(c.cyan)
                        Toggle("", isOn: $lunarBadgeEnabled)
                            .labelsHidden()
                            .tint(c.cyan)
                            .scaleEffect(0.7)
                    }
                }
            }
        }
    }
}

// MARK: - Month Navigation
struct CalendarMonthNav: View {
    let month: Int
    let year: Int
    let onPrev: () -> Void
    let onNext: () -> Void
    let onToday: () -> Void
    @Environment(\.lichSoColors) var c

    private var monthName: String {
        let names = ["", "Tháng Giêng", "Tháng Hai", "Tháng Ba", "Tháng Tư", "Tháng Năm",
                     "Tháng Sáu", "Tháng Bảy", "Tháng Tám", "Tháng Chín", "Tháng Mười",
                     "Tháng Mười Một", "Tháng Chạp"]
        return month >= 1 && month <= 12 ? names[month] : "Tháng \(month)"
    }

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onPrev) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(c.textSecondary)
                    .frame(width: 36, height: 36)
                    .background(
                        Circle()
                            .fill(c.surface.opacity(0.6))
                            .overlay(Circle().stroke(c.border, lineWidth: 1))
                    )
            }

            Spacer()

            VStack(spacing: 2) {
                Text(monthName)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(c.textPrimary)
                Text("\(year)")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(c.textTertiary)
            }
            .onTapGesture { onToday() }

            Spacer()

            Button(action: onNext) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(c.textSecondary)
                    .frame(width: 36, height: 36)
                    .background(
                        Circle()
                            .fill(c.surface.opacity(0.6))
                            .overlay(Circle().stroke(c.border, lineWidth: 1))
                    )
            }
        }
        .padding(.horizontal, 20)
    }
}

// MARK: - Calendar Grid Card
struct CalendarGridCard: View {
    let days: [CalendarDay]
    let selectedDate: Date
    var showLunar: Bool = true
    let onDayTap: (CalendarDay) -> Void
    @Environment(\.lichSoColors) var c

    let weekDays = [("CN", true), ("T2", false), ("T3", false), ("T4", false), ("T5", false), ("T6", false), ("T7", false)]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                ForEach(Array(weekDays.enumerated()), id: \.offset) { _, item in
                    Text(item.0)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(item.1 ? c.red2 : c.textTertiary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.top, 14)
            .padding(.bottom, 8)

            Rectangle()
                .fill(c.borderSubtle)
                .frame(height: 0.5)
                .padding(.horizontal, 12)

            let chunks = days.chunked(into: 7)
            ForEach(Array(chunks.enumerated()), id: \.offset) { _, week in
                HStack(spacing: 0) {
                    ForEach(week) { day in
                        ModernCalendarDayCell(
                            day: day,
                            selectedDate: selectedDate,
                            showLunar: showLunar
                        )
                        .onTapGesture {
                            if day.isCurrentMonth { onDayTap(day) }
                        }
                    }
                }
                .padding(.vertical, 1)
            }

            Spacer(minLength: 8)
        }
        .background(
            RoundedRectangle(cornerRadius: 18)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(c.border, lineWidth: 1)
                )
                .shadow(color: c.isDark ? Color.black.opacity(0.25) : Color.black.opacity(0.06), radius: 12, x: 0, y: 4)
        )
        .padding(.horizontal, 16)
    }
}

// MARK: - Modern Calendar Day Cell
struct ModernCalendarDayCell: View {
    let day: CalendarDay
    let selectedDate: Date
    var showLunar: Bool = true
    @Environment(\.lichSoColors) var c

    private var isSelected: Bool {
        guard let d = Calendar.current.date(from: DateComponents(year: day.solarYear, month: day.solarMonth, day: day.solarDay)) else { return false }
        return Calendar.current.isDate(d, inSameDayAs: selectedDate) && day.isCurrentMonth
    }

    var body: some View {
        VStack(spacing: 2) {
            ZStack {
                if isSelected {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(
                            LinearGradient(
                                colors: [c.cyan, c.cyan2],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 34, height: 34)
                        .shadow(color: c.cyan.opacity(0.3), radius: 6, x: 0, y: 2)
                } else if day.isToday {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(c.cyan, lineWidth: 1.5)
                        .frame(width: 34, height: 34)
                }

                Text("\(day.solarDay)")
                    .font(.system(size: 15, weight: isSelected ? .bold : day.isToday ? .semibold : .regular))
                    .foregroundColor(
                        isSelected ? .white
                        : !day.isCurrentMonth ? c.textQuaternary
                        : day.isSunday || day.isHoliday ? c.red2
                        : day.isSaturday ? c.teal2
                        : c.textPrimary
                    )
            }
            .frame(height: 36)

            if showLunar {
                Text(day.lunarDisplayText)
                    .font(.system(size: 8.5, weight: .medium))
                    .foregroundColor(
                        isSelected ? c.cyan
                        : day.lunarDay == 1 || day.lunarDay == 15
                            ? c.gold
                            : c.textQuaternary
                    )
                    .frame(height: 11)
            } else {
                Color.clear.frame(height: 11)
            }

            Circle()
                .fill(day.hasEvent ? c.teal : Color.clear)
                .frame(width: 4, height: 4)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 2)
        .contentShape(Rectangle())
    }
}

// MARK: - Day Quick Info
struct CalendarDayQuickInfo: View {
    let info: DayInfo
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 10) {
            HStack(spacing: 10) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(
                            LinearGradient(
                                colors: [c.cyan.opacity(0.15), c.cyan2.opacity(0.08)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 48, height: 48)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(c.cyan.opacity(0.2), lineWidth: 1)
                        )
                    VStack(spacing: 0) {
                        Text("\(info.solar.dd)")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(c.cyan)
                        Text("T\(info.solar.mm)")
                            .font(.system(size: 9, weight: .semibold))
                            .foregroundColor(c.cyan.opacity(0.7))
                    }
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text("\(info.dayOfWeek), \(info.solar.dd)/\(info.solar.mm)/\(info.solar.yy)")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                    Text("\(info.lunar.day)/\(info.lunar.month) Âm · \(info.dayCanChi)")
                        .font(.system(size: 12))
                        .foregroundColor(c.textSecondary)
                }

                Spacer()

                VStack(spacing: 2) {
                    HStack(spacing: 2) {
                        ForEach(0..<5, id: \.self) { i in
                            Image(systemName: i < info.dayRating.score ? "star.fill" : "star")
                                .font(.system(size: 8))
                                .foregroundColor(i < info.dayRating.score ? c.gold : c.textQuaternary)
                        }
                    }
                    Text(info.dayRating.label)
                        .font(.system(size: 9, weight: .medium))
                        .foregroundColor(c.gold)
                }
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    CalendarInfoChip(icon: "moon.stars", text: info.yearCanChi, color: c.gold)
                    CalendarInfoChip(icon: "calendar.day.timeline.left", text: "Trực \(info.trucNgay.name)", color: c.cyan)
                    CalendarInfoChip(icon: "sparkle", text: info.saoChieu.name, color: c.teal)
                    if let tietKhi = info.tietKhi.currentName {
                        CalendarInfoChip(icon: "leaf", text: tietKhi, color: c.green)
                    }
                }
            }

            HStack(spacing: 8) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 10))
                            .foregroundColor(c.teal)
                        Text("NÊN LÀM")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(c.teal)
                            .tracking(0.5)
                    }
                    ForEach(info.activities.nenLam.prefix(3), id: \.self) { item in
                        Text("• \(item)")
                            .font(.system(size: 11))
                            .foregroundColor(c.textSecondary)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 10))
                            .foregroundColor(c.red2)
                        Text("KHÔNG NÊN")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(c.red2)
                            .tracking(0.5)
                    }
                    ForEach(info.activities.khongNen.prefix(3), id: \.self) { item in
                        Text("• \(item)")
                            .font(.system(size: 11))
                            .foregroundColor(c.textSecondary)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(c.panelBg)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(c.border, lineWidth: 1)
                )
                .shadow(color: c.isDark ? Color.black.opacity(0.2) : Color.black.opacity(0.05), radius: 8, x: 0, y: 3)
        )
        .padding(.horizontal, 16)
    }
}

// MARK: - Calendar Info Chip
struct CalendarInfoChip: View {
    let icon: String
    let text: String
    let color: Color
    @Environment(\.lichSoColors) var c

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(color)
            Text(text)
                .font(.system(size: 10.5, weight: .medium))
                .foregroundColor(color)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(
            Capsule()
                .fill(color.opacity(0.1))
                .overlay(
                    Capsule()
                        .stroke(color.opacity(0.2), lineWidth: 1)
                )
        )
    }
}

// MARK: - Day Detail Sheet
struct CalendarDayDetailSheet: View {
    let info: DayInfo
    let onDismiss: () -> Void
    @Environment(\.lichSoColors) var c

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.black.opacity(0.45).ignoresSafeArea()
                .onTapGesture { onDismiss() }

            VStack(spacing: 0) {
                Capsule()
                    .fill(c.textTertiary.opacity(0.4))
                    .frame(width: 36, height: 4)
                    .padding(.top, 12)
                    .padding(.bottom, 10)

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 16) {
                        // ── Header ──
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(info.dayOfWeek)")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(c.textTertiary)
                                Text("\(String(format: "%02d", info.solar.dd))/\(String(format: "%02d", info.solar.mm))/\(info.solar.yy)")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(c.textPrimary)
                                HStack(spacing: 6) {
                                    Text("Âm: \(info.lunar.day)/\(info.lunar.month)")
                                        .font(.system(size: 13, weight: .medium))
                                        .foregroundColor(c.gold)
                                    Text("·")
                                        .foregroundColor(c.textQuaternary)
                                    Text(info.dayCanChi)
                                        .font(.system(size: 13, weight: .semibold))
                                        .foregroundColor(c.cyan)
                                }
                            }
                            Spacer()
                            Button(action: onDismiss) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 26))
                                    .foregroundColor(c.textTertiary)
                                    .symbolRenderingMode(.hierarchical)
                            }
                        }
                        .padding(.horizontal, 20)

                        // ── Info Cards ──
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            DetailInfoCard(icon: "moon.stars", title: "Can Chi", value: info.dayCanChi, subtitle: info.yearCanChi, color: c.gold)
                            DetailInfoCard(icon: "clock.arrow.2.circlepath", title: "Trực Ngày", value: info.trucNgay.name, subtitle: info.trucNgay.rating, color: detailRatingColor(info.trucNgay.rating))
                            DetailInfoCard(icon: "sparkle", title: "Sao Chiếu", value: info.saoChieu.name, subtitle: "", color: c.teal)
                            DetailInfoCard(icon: "leaf", title: "Tiết Khí", value: info.tietKhi.currentName ?? "—", subtitle: info.tietKhi.nextName.map { "Tiếp: \($0)" } ?? "", color: c.green)
                        }
                        .padding(.horizontal, 20)

                        // ── Giờ Hoàng Đạo ──
                        VStack(alignment: .leading, spacing: 8) {
                            HStack(spacing: 6) {
                                Image(systemName: "sun.max.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(c.gold)
                                Text("GIỜ HOÀNG ĐẠO")
                                    .font(.system(size: 10.5, weight: .bold))
                                    .foregroundColor(c.textTertiary)
                                    .tracking(0.8)
                            }

                            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 6) {
                                ForEach(info.gioHoangDao) { gio in
                                    HStack(spacing: 4) {
                                        Circle()
                                            .fill(c.gold)
                                            .frame(width: 5, height: 5)
                                        VStack(alignment: .leading, spacing: 0) {
                                            Text(gio.name)
                                                .font(.system(size: 11, weight: .semibold))
                                                .foregroundColor(c.textPrimary)
                                            Text(gio.time)
                                                .font(.system(size: 9))
                                                .foregroundColor(c.textTertiary)
                                        }
                                    }
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 6)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(c.gold.opacity(0.06))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(c.gold.opacity(0.15), lineWidth: 1)
                                            )
                                    )
                                }
                            }
                        }
                        .padding(.horizontal, 20)

                        // ── Hướng Tốt ──
                        VStack(alignment: .leading, spacing: 8) {
                            HStack(spacing: 6) {
                                Image(systemName: "location.north.line.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(c.cyan)
                                Text("HƯỚNG XUẤT HÀNH")
                                    .font(.system(size: 10.5, weight: .bold))
                                    .foregroundColor(c.textTertiary)
                                    .tracking(0.8)
                            }

                            HStack(spacing: 8) {
                                DirectionChip(label: "Thần Tài", value: info.huong.thanTai, color: c.gold)
                                DirectionChip(label: "Hỷ Thần", value: info.huong.hyThan, color: c.teal)
                                DirectionChip(label: "Hung Thần", value: info.huong.hungThan, color: c.red2)
                            }
                        }
                        .padding(.horizontal, 20)

                        // ── Activities ──
                        ActivityGrid(info: info)

                        Spacer(minLength: 30)
                    }
                }
            }
            .background(
                RoundedRectangle(cornerRadius: 24)
                    .fill(c.bg)
                    .shadow(color: Color.black.opacity(0.3), radius: 20, x: 0, y: -5)
            )
            .frame(maxHeight: UIScreen.main.bounds.height * 0.78)
        }
        .ignoresSafeArea()
    }

    private func detailRatingColor(_ rating: String) -> Color {
        switch rating {
        case "Tốt": return c.teal
        case "Xấu": return c.red2
        default: return c.gold
        }
    }
}

// MARK: - Detail Info Card
struct DetailInfoCard: View {
    let icon: String
    let title: String
    let value: String
    let subtitle: String
    let color: Color
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 5) {
                Image(systemName: icon)
                    .font(.system(size: 10))
                    .foregroundColor(color)
                Text(title.uppercased())
                    .font(.system(size: 9, weight: .bold))
                    .foregroundColor(c.textTertiary)
                    .tracking(0.5)
            }
            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(c.textPrimary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.system(size: 10))
                    .foregroundColor(color)
                    .lineLimit(1)
            }
        }
        .padding(11)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(color.opacity(0.06))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(color.opacity(0.15), lineWidth: 1)
                )
        )
    }
}

// MARK: - Direction Chip
struct DirectionChip: View {
    let label: String
    let value: String
    let color: Color
    @Environment(\.lichSoColors) var c

    var body: some View {
        VStack(spacing: 3) {
            Text(label)
                .font(.system(size: 9, weight: .bold))
                .foregroundColor(c.textTertiary)
                .tracking(0.3)
            Text(value)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(color)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .frame(maxWidth: .infinity)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(color.opacity(0.08))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(color.opacity(0.18), lineWidth: 1)
                )
        )
    }
}

// MARK: - Array Chunk Helper
extension Array {
    func chunked(into size: Int) -> [[Element]] {
        var chunks: [[Element]] = []
        var i = 0
        while i < count {
            chunks.append(Array(self[i..<Swift.min(i + size, count)]))
            i += size
        }
        return chunks
    }
}
