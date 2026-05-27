import SwiftUI
import SwiftData

// ═══════════════════════════════════════════════════════════
// Calendar Screen — Full month grid + day detail
// Matches screen-calendar.html mock design
// Uses real lunar calendar data via DayInfoProvider
// ═══════════════════════════════════════════════════════════

private var PrimaryRed           : Color { LSTheme.primary }
private var DeepRed              : Color { LSTheme.deepRed }
private var GoldAccent           : Color { LSTheme.gold }
private var SurfaceBg            : Color { LSTheme.bg }
private var SurfaceContainer     : Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh : Color { LSTheme.surfaceContainerHigh }
private var TextMain             : Color { LSTheme.textPrimary }
private var TextSub              : Color { LSTheme.textSecondary }
private var TextDim              : Color { LSTheme.textTertiary }
private var OutlineVar           : Color { LSTheme.outlineVariant }
private var GoodGreen            : Color { LSTheme.goodGreen }

struct CalendarScreen: View {
    @StateObject private var vm = CalendarViewModel()
    @State private var detailDayInfo: DayInfo?
    @State private var featuredArticle: Article?
    @State private var articlePool: [Article] = []
    @State private var isLoadingFeaturedArticle = false
    @State private var featuredArticleError: String?
    @State private var showFeaturedArticleDetail = false
    @State private var showSearch = false
    @State private var showMonthYearPicker = false
    @Environment(\.modelContext) private var modelContext

    // ── Paged swipe ──
    @State private var tabPage = 1

    // ── Display settings ──
    @AppStorage("setting_show_lunar")     private var showLunar: Bool = true
    @AppStorage("setting_show_hoang_dao") private var showHoangDao: Bool = true
    @AppStorage("setting_show_festivals") private var showFestivals: Bool = true
    @AppStorage("setting_week_start")     private var weekStart: String = "Thứ 2"

    // Query bookmarks for the current month
    @Query private var allBookmarks: [BookmarkEntity]

    var body: some View {
        ZStack {
            SurfaceBg.ignoresSafeArea()

            VStack(spacing: 0) {
                // ═══ TOP BAR ═══
                CalendarTopBar(
                    onSearchTap: { showSearch = true }
                )

                // ═══ MONTH SELECTOR ═══
                MonthSelector(
                    year: vm.state.currentYear,
                    month: vm.state.currentMonth,
                    lunarLabel: vm.state.lunarMonthLabel,
                    onPrev: { vm.previousMonth() },
                    onNext: { vm.nextMonth() },
                    onTitleTap: { showMonthYearPicker = true }
                )

                // ═══ CALENDAR GRID (native paged swipe) ═══
                TabView(selection: $tabPage) {
                    calendarGridPage(delta: -1).tag(0)
                    calendarGridPage(delta:  0).tag(1)
                    calendarGridPage(delta: +1).tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .onChange(of: tabPage) { _, new in
                    guard new != 1 else { return }
                    if new == 0 { vm.previousMonth() } else { vm.nextMonth() }
                    // Reset to center instantly so next swipe works in both directions
                    var t = Transaction(animation: .none)
                    t.disablesAnimations = true
                    withTransaction(t) { tabPage = 1 }
                }
                .clipped()

                Group {
                    if let article = featuredArticle {
                        RandomArticleCard(article: article) {
                            showFeaturedArticleDetail = true
                        }
                    } else if isLoadingFeaturedArticle {
                        ProgressView()
                            .tint(PrimaryRed)
                            .padding(.vertical, 8)
                    } else if let featuredArticleError {
                        Text(featuredArticleError)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(TextSub)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                    }
                }

                // ═══ SELECTED DAY DETAIL (bottom bar) ═══
                if let info = vm.state.dayInfo {
                    SelectedDayBar(
                        dayInfo: info,
                        onTap: { detailDayInfo = info }
                    )
                }

                Spacer()
            }
        }
        .fullScreenCover(item: $detailDayInfo) { (info: DayInfo) in
            DayDetailScreen(dayInfo: info, onDismiss: { detailDayInfo = nil })
        }
        .sheet(isPresented: $showSearch) {
            NavigationStack {
                SearchScreen()
            }
        }
        .sheet(isPresented: $showFeaturedArticleDetail) {
            if let article = featuredArticle {
                NavigationStack {
                    ArticleDetailScreen(articleId: article.id)
                }
            }
        }
        .sheet(isPresented: $showMonthYearPicker) {
            MonthYearPickerSheet(
                currentYear: vm.state.currentYear,
                currentMonth: vm.state.currentMonth,
                onSelect: { year, month in
                    showMonthYearPicker = false
                    vm.goToMonth(year: year, month: month)
                }
            )
            .presentationDetents([.medium, .large])
        }
        .task {
            await refreshFeaturedArticle()
        }
        .onChange(of: vm.state.selectedDay) { _, _ in
            Task { await refreshFeaturedArticle() }
        }
        .onChange(of: vm.state.selectedMonth) { _, _ in
            Task { await refreshFeaturedArticle() }
        }
        .onChange(of: vm.state.selectedYear) { _, _ in
            Task { await refreshFeaturedArticle() }
        }
    }

    // Returns (year, month) offset from the current displayed month
    private func adjacentMonth(delta: Int) -> (year: Int, month: Int) {
        var m = vm.state.currentMonth + delta
        var y = vm.state.currentYear
        while m < 1  { m += 12; y -= 1 }
        while m > 12 { m -= 12; y += 1 }
        return (y, m)
    }

    // Builds a CalendarGrid page for a given month delta (−1, 0, +1)
    @ViewBuilder
    private func calendarGridPage(delta: Int) -> some View {
        let (y, m) = adjacentMonth(delta: delta)
        let days: [CalendarDay] = delta == 0
            ? vm.state.calendarDays
            : DayInfoProvider.shared.getCalendarDays(
                year: y, month: m,
                weekStartSunday: weekStart == "Chủ nhật"
              )
        CalendarGrid(
            days: days,
            selectedDay: vm.state.selectedDay,
            selectedMonth: vm.state.selectedMonth,
            selectedYear: vm.state.selectedYear,
            bookmarkedDates: bookmarkedDatesSet,
            showLunar: showLunar,
            showHoangDao: showHoangDao,
            onDayTap: { day in
                vm.selectDay(day.solarDay, month: day.solarMonth, year: day.solarYear)
            },
            onDayDoubleTap: { day in
                vm.selectDay(day.solarDay, month: day.solarMonth, year: day.solarYear)
                detailDayInfo = vm.state.dayInfo
            }
        )
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }

    private var bookmarkedDatesSet: Set<String> {
        Set(allBookmarks.map { "\($0.solarYear)-\($0.solarMonth)-\($0.solarDay)" })
    }

    @MainActor
    private func refreshFeaturedArticle() async {
        if articlePool.isEmpty {
            isLoadingFeaturedArticle = true
            featuredArticleError = nil
            do {
                articlePool = try await QuizService.shared.fetchArticles(page: 1, limit: 60)
            } catch {
                featuredArticle = nil
                featuredArticleError = "Không tải được bài viết gợi ý."
                isLoadingFeaturedArticle = false
                return
            }
            isLoadingFeaturedArticle = false
        }

        guard !articlePool.isEmpty else {
            featuredArticle = nil
            featuredArticleError = "Chưa có bài viết để hiển thị."
            return
        }

        let idx = daySeedIndex(
            year: vm.state.selectedYear,
            month: vm.state.selectedMonth,
            day: vm.state.selectedDay,
            count: articlePool.count
        )
        featuredArticle = articlePool[idx]
        featuredArticleError = nil
    }

    private func daySeedIndex(year: Int, month: Int, day: Int, count: Int) -> Int {
        guard count > 0 else { return 0 }
        var seed = UInt64(max(0, year * 10000 + month * 100 + day))
        seed ^= 0x9E3779B97F4A7C15
        seed = (seed ^ (seed >> 30)) &* 0xBF58476D1CE4E5B9
        seed = (seed ^ (seed >> 27)) &* 0x94D049BB133111EB
        seed = seed ^ (seed >> 31)
        return Int(seed % UInt64(count))
    }
}

// ══════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════

private struct CalendarTopBar: View {
    let onSearchTap: () -> Void

    var body: some View {
        HStack {
            Text("Lịch")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            Spacer()
            Button(action: onSearchTap) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 36, height: 36)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
        .padding(.bottom, 12)
        .background(
            LinearGradient(
                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                         Color(red: 0.545, green: 0, blue: 0)],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
        )
    }
}

// ══════════════════════════════════════════
// MONTH SELECTOR
// ══════════════════════════════════════════

private struct MonthSelector: View {
    let year: Int
    let month: Int
    let lunarLabel: String
    let onPrev: () -> Void
    let onNext: () -> Void
    var onTitleTap: (() -> Void)? = nil

    private let monthNames = [
        "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    ]

    var body: some View {
        HStack(spacing: 16) {
            Button(action: onPrev) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(TextMain)
                    .frame(width: 32, height: 32)
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
            }

            Button {
                onTitleTap?()
            } label: {
                VStack(spacing: 2) {
                    HStack(spacing: 4) {
                        Text("\(monthNames[month]), \(String(year))")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(PrimaryRed)
                        Image(systemName: "chevron.down")
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(PrimaryRed.opacity(0.6))
                    }
                    Text(lunarLabel)
                        .font(.system(size: 11))
                        .foregroundColor(TextSub)
                }
            }
            .buttonStyle(.plain)
            .frame(minWidth: 160)

            Button(action: onNext) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(TextMain)
                    .frame(width: 32, height: 32)
                    .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
            }
        }
        .padding(.vertical, 8)
    }
}

// ══════════════════════════════════════════
// CALENDAR GRID
// ══════════════════════════════════════════

private struct CalendarGrid: View {
    let days: [CalendarDay]
    let selectedDay: Int
    let selectedMonth: Int
    let selectedYear: Int
    let bookmarkedDates: Set<String>
    var showLunar: Bool = true
    var showHoangDao: Bool = true
    let onDayTap: (CalendarDay) -> Void
    let onDayDoubleTap: (CalendarDay) -> Void

    @AppStorage("setting_week_start") private var weekStart: String = "Thứ 2"

    private var weekdayHeaders: [String] {
        if weekStart == "Chủ nhật" {
            return ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]
        } else {
            return ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
        }
    }

    private var weekendIndices: [Int] {
        if weekStart == "Chủ nhật" {
            return [0, 6]  // CN, T7
        } else {
            return [5, 6]  // T7, CN
        }
    }

    var body: some View {
        VStack(spacing: 2) {
            // Weekday header
            HStack(spacing: 0) {
                ForEach(0..<7, id: \.self) { i in
                    Text(weekdayHeaders[i])
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(weekendIndices.contains(i) ? PrimaryRed : TextDim)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.bottom, 8)

            // Grid of days
            let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 7)
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(days) { day in
                    CalendarDayCell(
                        day: day,
                        isSelected: day.solarDay == selectedDay
                            && day.solarMonth == selectedMonth
                            && day.solarYear == selectedYear,
                        isBookmarked: bookmarkedDates.contains("\(day.solarYear)-\(day.solarMonth)-\(day.solarDay)"),
                        showLunar: showLunar,
                        showHoangDao: showHoangDao,
                        onTap: { onDayTap(day) },
                        onDoubleTap: { onDayDoubleTap(day) }
                    )
                }
            }
        }
        .padding(.horizontal, 14)
    }
}

// ══════════════════════════════════════════
// CALENDAR DAY CELL
// ══════════════════════════════════════════

private struct CalendarDayCell: View {
    let day: CalendarDay
    let isSelected: Bool
    let isBookmarked: Bool
    var showLunar: Bool = true
    var showHoangDao: Bool = true
    let onTap: () -> Void
    let onDoubleTap: () -> Void

    var body: some View {
        ZStack {
            // Background
            RoundedRectangle(cornerRadius: 14)
                .fill(cellBackground)

            // Selected border ring (not today)
            if isSelected && !day.isToday {
                RoundedRectangle(cornerRadius: 14)
                    .stroke(PrimaryRed.opacity(0.4), lineWidth: 1.5)
            }

            VStack(spacing: 1) {
                // Solar day number
                Text("\(day.solarDay)")
                    .font(.system(size: 16, weight: day.isToday || day.isHoliday || isSelected ? .bold : .semibold))
                    .foregroundColor(solarColor)
                    .lineLimit(1)

                // Lunar day — controlled by setting
                if showLunar {
                    Text(day.lunarDisplayText)
                        .font(.system(size: 9))
                        .foregroundColor(lunarColor)
                        .lineLimit(1)
                }
            }

            // Event dot
            if day.hasEvent && day.isCurrentMonth {
                VStack {
                    Spacer()
                    Circle()
                        .fill(GoldAccent)
                        .frame(width: 4, height: 4)
                        .padding(.bottom, 5)
                }
            }

            // Good day indicator — controlled by setting
            if showHoangDao && day.dayRatingLabel == "Rất tốt" && day.isCurrentMonth {
                VStack {
                    HStack {
                        Spacer()
                        Circle()
                            .fill(GoodGreen)
                            .frame(width: 5, height: 5)
                            .padding(.top, 3)
                            .padding(.trailing, 3)
                    }
                    Spacer()
                }
            }

            // Bookmark indicator
            if isBookmarked {
                VStack {
                    HStack {
                        Image(systemName: "bookmark.fill")
                            .font(.system(size: 6))
                            .foregroundColor(PrimaryRed)
                            .padding(.top, 2)
                            .padding(.leading, 2)
                        Spacer()
                    }
                    Spacer()
                }
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .onTapGesture(count: 2) { onDoubleTap() }
        .onTapGesture { onTap() }
    }

    private var cellBackground: Color {
        if day.isToday {
            return PrimaryRed
        } else if isSelected {
            return PrimaryRed.opacity(0.12)
        } else {
            return .clear
        }
    }

    private var solarColor: Color {
        if day.isToday {
            return .white
        }
        if isSelected {
            return PrimaryRed
        }
        if !day.isCurrentMonth {
            return OutlineVar
        }
        if day.isHoliday || day.isSunday {
            return PrimaryRed
        }
        if day.isSaturday {
            return PrimaryRed.opacity(0.7)
        }
        return TextMain
    }

    private var lunarColor: Color {
        if day.isToday {
            return .white.opacity(0.7)
        }
        if isSelected {
            return PrimaryRed.opacity(0.6)
        }
        if !day.isCurrentMonth {
            return OutlineVar
        }
        return TextDim
    }
}

// ══════════════════════════════════════════
// SELECTED DAY BAR (bottom info)
// ══════════════════════════════════════════

private struct SelectedDayBar: View {
    let dayInfo: DayInfo
    let onTap: () -> Void

    var body: some View {
        VStack(spacing: 6) {
            Divider().foregroundColor(OutlineVar)

            Button(action: onTap) {
                VStack(spacing: 8) {
                    // Date header row
                    HStack {
                        Text("\(dayInfo.dayOfWeek), \(String(format: "%02d/%02d/%d", dayInfo.solar.dd, dayInfo.solar.mm, dayInfo.solar.yy))")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(TextMain)

                        Spacer()

                        Text("\(dayInfo.lunar.day)/\(dayInfo.lunar.month) Âm")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(PrimaryRed)
                    }

                    // Quick info chips
                    HStack(spacing: 8) {
                        // Day rating
                        let isGood = dayInfo.dayRating.label == "Rất tốt" || dayInfo.dayRating.label == "Tốt"
                        HStack(spacing: 4) {
                            Image(systemName: isGood ? "checkmark.seal.fill" : "exclamationmark.triangle.fill")
                                .font(.system(size: 14))
                            Text(dayInfo.dayRating.label)
                                .font(.system(size: 12, weight: .semibold))
                        }
                        .foregroundColor(isGood ? Color(hex: "2E7D32") : Color(hex: "C62828"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(isGood ? Color(hex: "E8F5E9") : Color(hex: "FFEBEE"))
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                        // Holiday if present
                        if let holiday = dayInfo.solarHoliday ?? dayInfo.lunarHoliday {
                            HStack(spacing: 4) {
                                Image(systemName: "party.popper.fill")
                                    .font(.system(size: 14))
                                Text(holiday)
                                    .font(.system(size: 12, weight: .semibold))
                                    .lineLimit(1)
                            }
                            .foregroundColor(Color(hex: "E65100"))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(Color(hex: "FFF3E0"))
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 14))
                            .foregroundColor(OutlineVar)
                    }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 10)
            }
            .buttonStyle(.plain)
        }
    }
}

private struct RandomArticleCard: View {
    let article: Article
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 8) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(PrimaryRed)
                        Text("Bài viết hôm nay")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(PrimaryRed)
                    }

                    Text(article.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(TextMain)
                        .lineLimit(2)

                    if let excerpt = article.excerpt ?? article.content {
                        Text(excerpt.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression))
                            .font(.system(size: 12))
                            .foregroundColor(TextSub)
                            .lineLimit(2)
                    }
                }

                Spacer(minLength: 8)

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(OutlineVar)
                    .padding(.top, 4)
            }
            .padding(12)
            .background(SurfaceContainer, in: RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(OutlineVar.opacity(0.35), lineWidth: 1)
            )
            .padding(.horizontal, 14)
            .padding(.top, 6)
            .padding(.bottom, 8)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// MONTH / YEAR PICKER SHEET
// Level 1: 12 months grid → tap year header → Level 2: 16-year grid
// ══════════════════════════════════════════

private struct MonthYearPickerSheet: View {
    let currentYear: Int
    let currentMonth: Int
    let onSelect: (Int, Int) -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var displayYear: Int
    @State private var showYearGrid = false

    private let monthNames = [
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    ]

    init(currentYear: Int, currentMonth: Int, onSelect: @escaping (Int, Int) -> Void) {
        self.currentYear = currentYear
        self.currentMonth = currentMonth
        self.onSelect = onSelect
        self._displayYear = State(initialValue: currentYear)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if showYearGrid {
                    yearGridView
                } else {
                    monthGridView
                }
            }
            .background(SurfaceBg)
            .navigationTitle(showYearGrid ? "Chọn năm" : "Chọn tháng")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        if showYearGrid {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                showYearGrid = false
                            }
                        } else {
                            dismiss()
                        }
                    } label: {
                        Image(systemName: showYearGrid ? "chevron.left" : "xmark")
                            .foregroundColor(PrimaryRed)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        let now = Date()
                        let cal = Calendar.current
                        onSelect(cal.component(.year, from: now), cal.component(.month, from: now))
                    } label: {
                        Text("Hôm nay")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(PrimaryRed)
                    }
                }
            }
        }
    }

    // ── Level 1: Month Grid ──
    private var monthGridView: some View {
        VStack(spacing: 16) {
            // Year header — tappable to go to year grid
            HStack {
                Button {
                    displayYear -= 1
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                        .frame(width: 32, height: 32)
                        .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                }

                Spacer()

                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        showYearGrid = true
                    }
                } label: {
                    HStack(spacing: 4) {
                        Text("Năm \(String(displayYear))")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(PrimaryRed)
                        Image(systemName: "chevron.down")
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(PrimaryRed.opacity(0.6))
                    }
                }
                .buttonStyle(.plain)

                Spacer()

                Button {
                    displayYear += 1
                } label: {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                        .frame(width: 32, height: 32)
                        .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)

            // 12 months in 3×4 grid
            let columns = Array(repeating: GridItem(.flexible(), spacing: 12), count: 3)
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(0..<12, id: \.self) { index in
                    let m = index + 1
                    let isSelected = displayYear == currentYear && m == currentMonth
                    let isCurrentMonth = isThisMonth(month: m, year: displayYear)

                    Button {
                        onSelect(displayYear, m)
                    } label: {
                        VStack(spacing: 4) {
                            Text(monthNames[index])
                                .font(.system(size: 14, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? .white : (isCurrentMonth ? PrimaryRed : TextMain))

                            // Lunar month label
                            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: 15, mm: m, yy: displayYear)
                            Text("Tháng \(lunar.lunarMonth) Âm")
                                .font(.system(size: 10))
                                .foregroundColor(isSelected ? .white.opacity(0.7) : TextDim)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(isSelected ? PrimaryRed : (isCurrentMonth ? PrimaryRed.opacity(0.08) : SurfaceContainer))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(isSelected ? PrimaryRed : (isCurrentMonth ? PrimaryRed.opacity(0.3) : OutlineVar), lineWidth: isSelected ? 2 : 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)

            Spacer()
        }
    }

    // ── Level 2: Year Grid (16 years) ──
    private var yearGridView: some View {
        let startYear = displayYear - 7
        let years = Array(startYear...(startYear + 15))

        return VStack(spacing: 16) {
            // Range header with navigation
            HStack {
                Button {
                    displayYear -= 16
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                        .frame(width: 32, height: 32)
                        .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                }

                Spacer()

                Text("\(String(years.first!)) – \(String(years.last!))")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(PrimaryRed)

                Spacer()

                Button {
                    displayYear += 16
                } label: {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                        .frame(width: 32, height: 32)
                        .overlay(Circle().stroke(OutlineVar, lineWidth: 1))
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)

            // 4×4 grid
            let columns = Array(repeating: GridItem(.flexible(), spacing: 12), count: 4)
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(years, id: \.self) { year in
                    let isSelected = year == currentYear
                    let isThisYear = year == Calendar.current.component(.year, from: Date())

                    Button {
                        displayYear = year
                        withAnimation(.easeInOut(duration: 0.2)) {
                            showYearGrid = false
                        }
                    } label: {
                        VStack(spacing: 2) {
                            Text(String(year))
                                .font(.system(size: 15, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? .white : (isThisYear ? PrimaryRed : TextMain))

                            // Can chi of year
                            Text(canChiOfYear(year))
                                .font(.system(size: 10))
                                .foregroundColor(isSelected ? .white.opacity(0.7) : TextDim)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(isSelected ? PrimaryRed : (isThisYear ? PrimaryRed.opacity(0.08) : SurfaceContainer))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(isSelected ? PrimaryRed : (isThisYear ? PrimaryRed.opacity(0.3) : OutlineVar), lineWidth: isSelected ? 2 : 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)

            Spacer()
        }
    }

    // ── Helpers ──
    private func isThisMonth(month: Int, year: Int) -> Bool {
        let now = Date()
        let cal = Calendar.current
        return cal.component(.year, from: now) == year && cal.component(.month, from: now) == month
    }

    private func canChiOfYear(_ year: Int) -> String {
        let thienCan = ["Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"]
        let diaChi = ["Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi"]
        return "\(thienCan[year % 10]) \(diaChi[year % 12])"
    }
}

// ══════════════════════════════════════════
#Preview {
    CalendarScreen()
}
