import SwiftUI

// MARK: - Navigation Route
enum AppRoute: String, CaseIterable {
    case home
    case calendar
    case tasks
    case prayers
    case profile
    case chat
    case familytree
    case settings
    case history
    case notifications
    case search
    case bookmarks
    case gooddays
}

// MARK: - ContentView (Main Navigation)
struct ContentView: View {
    @Environment(\.lichSoColors) private var c
    @EnvironmentObject private var settings: AppSettings
    @StateObject private var homeViewModel = HomeViewModel()
    @State private var currentRoute: AppRoute = .home
    @State private var showDrawer = false

    var body: some View {
        let hideBottomBar = [.chat, .familytree, .settings, .history, .notifications, .search, .bookmarks, .gooddays].contains(currentRoute)

        ZStack {
            c.bg.ignoresSafeArea()
            mainContent
        }
        .overlay(alignment: .bottomTrailing) {
                // ── AI FAB ──
                if !hideBottomBar {
                    Button(action: { currentRoute = .chat }) {
                        ZStack(alignment: .topTrailing) {
                            Circle()
                                .fill(c.fabGradient)
                                .frame(width: 56, height: 56)
                                .shadow(color: c.primary.opacity(0.4), radius: 8, y: 4)
                                .overlay(
                                    Image(systemName: "sparkles")
                                        .font(.system(size: 24, weight: .medium))
                                        .foregroundColor(.white)
                                )
                            Text("AI")
                                .font(.system(size: 8, weight: .bold))
                                .foregroundColor(.black)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(c.gold)
                                .cornerRadius(8)
                                .offset(x: 4, y: -4)
                        }
                    }
                    .padding(.trailing, 16)
                    .padding(.bottom, 16)
                }
            }
            .safeAreaInset(edge: .bottom) {
                if !hideBottomBar {
                    BottomNavBar(
                        currentRoute: currentRoute,
                        onRouteSelected: { currentRoute = $0 },
                        onCenterClick: {
                            homeViewModel.goToToday()
                            currentRoute = .home
                        }
                    )
                }
            }
            .sheet(isPresented: $showDrawer) {
            DrawerMenuView(
                currentRoute: currentRoute,
                onItemClick: { route in
                    showDrawer = false
                    currentRoute = route
                }
            )
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
        }
    }

    @ViewBuilder
    private var mainContent: some View {
        switch currentRoute {
        case .home:
            HomeScreen(
                viewModel: homeViewModel,
                onSettingsClick: { currentRoute = .settings },
                onMenuClick: { showDrawer.toggle() },
                onProfileClick: { currentRoute = .profile },
                onHistoryClick: { currentRoute = .history },
                onNotificationClick: { currentRoute = .notifications }
            )
        case .calendar:
            CalendarScreen(
                viewModel: homeViewModel,
                onGoodDaysClick: { currentRoute = .gooddays },
                onSearchClick: { currentRoute = .search },
                onMenuClick: { showDrawer.toggle() },
                onAskAiClick: { day, month, year in
                    currentRoute = .chat
                }
            )
        case .tasks:
            TasksScreen(
                onBackClick: { currentRoute = .home },
                onMenuClick: { showDrawer.toggle() }
            )
        case .prayers:
            PrayersScreen(
                onBackClick: { currentRoute = .home },
                onMenuClick: { showDrawer.toggle() }
            )
        case .profile:
            ProfileScreen(
                onSettingsClick: { currentRoute = .settings },
                onFamilyTreeClick: { currentRoute = .familytree },
                onBackClick: { currentRoute = .home },
                onTasksClick: { currentRoute = .tasks },
                onBookmarksClick: { currentRoute = .bookmarks }
            )
        case .chat:
            AIChatScreen(
                onBackClick: { currentRoute = .home },
                onNavigateToProfile: { currentRoute = .profile }
            )
        case .familytree:
            FamilyTreeScreen(
                onBackClick: { currentRoute = .profile }
            )
        case .settings:
            SettingsScreen(onBackClick: { currentRoute = .home })
        case .history:
            ThisDayInHistoryScreen(onBackClick: { currentRoute = .home })
        case .notifications:
            NotificationScreen(onBackClick: { currentRoute = .home })
        case .search:
            SearchScreen(
                onBackClick: { currentRoute = .calendar },
                onDateSelected: { year, month, day in
                    homeViewModel.goToDate(year: year, month: month, day: day)
                    currentRoute = .calendar
                }
            )
        case .bookmarks:
            BookmarksScreen(
                onBackClick: { currentRoute = .profile },
                onDateSelected: { year, month, day in
                    homeViewModel.goToDate(year: year, month: month, day: day)
                    currentRoute = .calendar
                }
            )
        case .gooddays:
            GoodDaysScreen(onBackClick: { currentRoute = .home })
        }
    }
}

// MARK: - Bottom Navigation Bar
struct BottomNavBar: View {
    @Environment(\.lichSoColors) private var c
    let currentRoute: AppRoute
    let onRouteSelected: (AppRoute) -> Void
    let onCenterClick: () -> Void

    private struct NavItem {
        let route: AppRoute
        let title: String
        let icon: String
        let iconFilled: String
    }

    private let leftItems = [
        NavItem(route: .calendar, title: "Lịch tháng", icon: "calendar", iconFilled: "calendar"),
        NavItem(route: .tasks, title: "Ghi chú", icon: "square.and.pencil", iconFilled: "square.and.pencil"),
    ]
    private let rightItems = [
        NavItem(route: .prayers, title: "Văn Khấn", icon: "book", iconFilled: "book.fill"),
        NavItem(route: .profile, title: "Cá nhân", icon: "person", iconFilled: "person.fill"),
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Top divider
            Rectangle()
                .fill(c.border)
                .frame(height: 0.5)

            ZStack(alignment: .top) {
                // Background bar with nav items
                HStack {
                    ForEach(leftItems, id: \.route) { item in
                        navButton(item: item)
                    }
                    Spacer().frame(width: 72)
                    ForEach(rightItems, id: \.route) { item in
                        navButton(item: item)
                    }
                }
                .frame(height: 56)
                .padding(.horizontal, 8)
                .padding(.top, 8)

                // Center raised button
                VStack(spacing: 2) {
                    Button(action: onCenterClick) {
                        ZStack {
                            Circle()
                                .fill(
                                    currentRoute == .home
                                        ? c.fabGradient
                                        : LinearGradient(colors: [c.primary.opacity(0.85), c.primary], startPoint: .topLeading, endPoint: .bottomTrailing)
                                )
                                .frame(width: 60, height: 60)
                                .shadow(color: c.primary.opacity(0.3), radius: 6, y: 2)
                                .overlay(
                                    Circle().stroke(c.bg, lineWidth: 3)
                                )
                            Text("\(Calendar.current.component(.day, from: Date()))")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                    .offset(y: -20)

                    Text("Hôm nay")
                        .font(.system(size: 10, weight: currentRoute == .home ? .semibold : .medium))
                        .foregroundColor(currentRoute == .home ? c.primary : c.outline)
                        .offset(y: -18)
                }
            }
        }
        .background(c.bg)
    }

    @ViewBuilder
    private func navButton(item: NavItem) -> some View {
        let isSelected = currentRoute == item.route
        let tint = isSelected ? c.primary : c.outline

        Button(action: { onRouteSelected(item.route) }) {
            VStack(spacing: 3) {
                ZStack {
                    if isSelected {
                        RoundedRectangle(cornerRadius: 15)
                            .fill(c.primaryContainer)
                            .frame(width: 56, height: 30)
                    }
                    Image(systemName: isSelected ? item.iconFilled : item.icon)
                        .font(.system(size: 22))
                        .foregroundColor(tint)
                }
                Text(item.title)
                    .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(tint)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

// MARK: - Drawer Menu View
struct DrawerMenuView: View {
    @Environment(\.lichSoColors) private var c
    let currentRoute: AppRoute
    let onItemClick: (AppRoute) -> Void

    private struct MenuItem {
        let route: AppRoute
        let title: String
        let icon: String
        let iconFilled: String
    }

    private let mainItems: [MenuItem] = [
        MenuItem(route: .home, title: "Trang chủ", icon: "calendar.badge.clock", iconFilled: "calendar.badge.clock"),
        MenuItem(route: .calendar, title: "Lịch tháng", icon: "calendar", iconFilled: "calendar"),
        MenuItem(route: .gooddays, title: "Ngày tốt / xấu", icon: "checkmark.circle", iconFilled: "checkmark.circle.fill"),
        MenuItem(route: .bookmarks, title: "Ngày đã lưu", icon: "bookmark", iconFilled: "bookmark.fill"),
    ]

    private let exploreItems: [MenuItem] = [
        MenuItem(route: .history, title: "Ngày này năm xưa", icon: "clock.arrow.circlepath", iconFilled: "clock.arrow.circlepath"),
        MenuItem(route: .familytree, title: "Cây gia phả", icon: "person.3.sequence", iconFilled: "person.3.sequence.fill"),
        MenuItem(route: .prayers, title: "Các bài văn khấn", icon: "book", iconFilled: "book.fill"),
    ]

    var body: some View {
        NavigationView {
            List {
                // Header
                Section {
                    HStack(spacing: 16) {
                        Image(systemName: "calendar.badge.plus")
                            .font(.system(size: 32))
                            .foregroundColor(.white)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Lịch Số")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(.white)
                            Text("Lịch vạn niên số 1 Việt Nam")
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(.white.opacity(0.8))
                        }
                    }
                    .listRowBackground(
                        c.headerGradient
                    )
                    .padding(.vertical, 8)
                }

                // Main items
                Section {
                    ForEach(mainItems, id: \.route) { item in
                        drawerButton(item: item)
                    }
                }

                // Explore
                Section(header: Text("KHÁM PHÁ").font(.system(size: 11, weight: .semibold)).foregroundColor(c.textTertiary)) {
                    ForEach(exploreItems, id: \.route) { item in
                        drawerButton(item: item)
                    }
                }

                // Settings
                Section {
                    drawerButton(item: MenuItem(route: .settings, title: "Cài đặt", icon: "gearshape", iconFilled: "gearshape.fill"))
                }

                // Footer
                Section {
                    VStack(spacing: 4) {
                        Text("Lịch Số v1.0")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(c.textTertiary)
                        HStack(spacing: 3) {
                            Text("Phát triển bởi")
                                .font(.system(size: 10))
                                .foregroundColor(c.textTertiary.opacity(0.7))
                            Text("Zenix Labs")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(c.teal)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .listRowBackground(Color.clear)
                }
            }
            .listStyle(.insetGrouped)
            .navigationBarHidden(true)
            .background(c.bg)
        }
    }

    @ViewBuilder
    private func drawerButton(item: MenuItem) -> some View {
        let isSelected = currentRoute == item.route
        Button(action: { onItemClick(item.route) }) {
            HStack(spacing: 16) {
                Image(systemName: isSelected ? item.iconFilled : item.icon)
                    .font(.system(size: 20))
                    .foregroundColor(isSelected ? c.onPrimaryContainer : c.textPrimary)
                    .frame(width: 24)
                Text(item.title)
                    .font(.system(size: 14, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(isSelected ? c.onPrimaryContainer : c.textPrimary)
                Spacer()
            }
            .padding(.vertical, 4)
        }
        .listRowBackground(isSelected ? c.primaryContainer : Color.clear)
    }
}
