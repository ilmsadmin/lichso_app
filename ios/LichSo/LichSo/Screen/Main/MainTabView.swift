import SwiftUI

// ═══════════════════════════════════════════
// Main Tab View — iOS Tab Bar (5 tabs)
// Matches iOS HTML mock tab bar design
// ═══════════════════════════════════════════

private var PrimaryRed  : Color { LSTheme.primary }
private var SurfaceBg   : Color { LSTheme.bg }
private var TabInactive : Color { LSTheme.textTertiary }

enum MainTab: Int, CaseIterable {
    case calendar = 0
    case notes = 1
    case today = 2
    case prayers = 3
    case profile = 4

    var label: String {
        switch self {
        case .calendar: return "Lịch tháng"
        case .notes: return "Ghi chú"
        case .today: return "Hôm nay"
        case .prayers: return "Văn Khấn"
        case .profile: return "Cá nhân"
        }
    }

    var icon: String {
        switch self {
        case .calendar: return "calendar"
        case .notes: return "note.text"
        case .today: return "sun.min.fill"
        case .prayers: return "book.fill"
        case .profile: return "person"
        }
    }

    var iconFilled: String {
        switch self {
        case .calendar: return "calendar"
        case .notes: return "note.text"
        case .today: return "sun.min.fill"
        case .prayers: return "book.fill"
        case .profile: return "person.fill"
        }
    }
}

struct MainTabView: View {
    @EnvironmentObject private var appState: AppState
    @State private var selectedTab: MainTab = .today
    @State private var showAIChat = false
    @State private var showSidebar = false
    @State private var showSettings = false
    @State private var showFamilyTree = false
    @State private var showGoodDays = false
    @State private var showHistory = false
    @State private var showBookmarks = false

    var body: some View {
        ZStack {
            // Main content with tabs
            mainContent

            // Sidebar overlay
            SidebarView(isOpen: $showSidebar) { route in
                handleSidebarNavigation(route)
            }
        }
    }

    // MARK: - Main Content

    private var mainContent: some View {
        GeometryReader { geometry in
            let safeBottom = geometry.safeAreaInsets.bottom // home indicator height
            let tabBarHeight: CGFloat = 54 // HStack row content height
            let tabBarTopPad: CGFloat = 6
            let tabBarBottomPad: CGFloat = safeBottom > 0 ? safeBottom : 20
            let totalTabBarHeight = tabBarTopPad + tabBarHeight + tabBarBottomPad
            // Content bottom = aligns border bottom with top edge of tab bar
            let contentBottom = totalTabBarHeight
            // FAB: right edge 5px inside border right, bottom edge 5px above border bottom
            // Calendar border margin = 16px from content edge
            let borderMargin: CGFloat = 16
            let fabInset: CGFloat = 5
            let fabTrailing = borderMargin + fabInset + 3 // +3 for border stroke width
            let fabBottomOffset = contentBottom + borderMargin + fabInset + 3

            ZStack(alignment: .bottom) {
                // Content
                Group {
                    switch selectedTab {
                    case .calendar:
                        CalendarScreen()
                    case .notes:
                        NotesScreen()
                    case .today:
                        HomeScreen(onMenuClick: { showSidebar = true })
                    case .prayers:
                        PrayersScreen()
                    case .profile:
                        ProfileScreen()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.bottom, contentBottom)

                // ═══ AI FAB (floating above tab bar) ═══
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        AiFab { showAIChat = true }
                    }
                    .padding(.trailing, fabTrailing)
                    .padding(.bottom, fabBottomOffset)
                }

                // Custom Tab Bar
                CustomTabBar(selectedTab: $selectedTab, bottomInset: tabBarBottomPad)
            }
        }
        .edgesIgnoringSafeArea(.bottom)
        .fullScreenCover(isPresented: $showAIChat) {
            AIChatScreen()
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                SettingsScreen()
            }
        }
        .fullScreenCover(isPresented: $showFamilyTree) {
            NavigationStack {
                FamilyTreeScreen()
            }
        }
        .fullScreenCover(isPresented: $showGoodDays) {
            NavigationStack {
                GoodDaysScreen()
            }
        }
        .fullScreenCover(isPresented: $showHistory) {
            NavigationStack {
                ThisDayInHistoryScreen()
            }
        }
        .sheet(isPresented: $showBookmarks) {
            NavigationStack {
                SearchScreen()
            }
        }
    }

    // MARK: - Sidebar Navigation

    private func handleSidebarNavigation(_ route: String) {
        switch route {
        case "home":
            selectedTab = .today
        case "calendar":
            selectedTab = .calendar
        case "prayers":
            selectedTab = .prayers
        case "settings":
            showSettings = true
        case "gooddays":
            showGoodDays = true
        case "bookmarks":
            showBookmarks = true
        case "history":
            showHistory = true
        case "familytree":
            showFamilyTree = true
        default:
            break
        }
    }
}

// ══════════════════════════════════════════
// CUSTOM TAB BAR (matching iOS mock)
// ══════════════════════════════════════════

struct CustomTabBar: View {
    @Binding var selectedTab: MainTab
    var bottomInset: CGFloat = 30

    var body: some View {
        HStack(spacing: 0) {
            ForEach(MainTab.allCases, id: \.rawValue) { tab in
                if tab == .today {
                    // Center raised circle button
                    CenterTabButton(
                        isSelected: selectedTab == .today,
                        onTap: { selectedTab = .today }
                    )
                } else {
                    // Regular tab
                    RegularTabButton(
                        tab: tab,
                        isSelected: selectedTab == tab,
                        onTap: { selectedTab = tab }
                    )
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 6)
        .padding(.bottom, bottomInset)
        .background(SurfaceBg.opacity(0.97))
        .background(.ultraThinMaterial)
        .shadow(color: Color.black.opacity(0.15), radius: 1, y: -0.5)
    }
}

private struct RegularTabButton: View {
    let tab: MainTab
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 2) {
                Image(systemName: isSelected ? tab.iconFilled : tab.icon)
                    .font(.system(size: 22))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)

                Text(tab.label)
                    .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 4)
        }
        .buttonStyle(.plain)
    }
}

private struct CenterTabButton: View {
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 0) {
                // Raised circle with today's date
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "D32F2F"), PrimaryRed],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 56, height: 56)
                        .shadow(color: PrimaryRed.opacity(0.25), radius: 8, y: 4)
                        .overlay(
                            Circle()
                                .stroke(SurfaceBg, lineWidth: 3)
                        )

                    let today = Calendar.current.component(.day, from: Date())
                    Text("\(today)")
                        .font(.system(size: 23, weight: .heavy))
                        .foregroundColor(.white)
                        .monospacedDigit()
                }
                .offset(y: -20)

                Text("Hôm nay")
                    .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                    .foregroundColor(isSelected ? PrimaryRed : TabInactive)
                    .offset(y: -16)
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// PLACEHOLDER (for tabs not yet implemented)
// ══════════════════════════════════════════

struct PlaceholderScreen: View {
    let title: String
    let icon: String

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(PrimaryRed.opacity(0.4))
            Text(title)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(Color(hex: "F0E8D0"))
            Text("Đang phát triển...")
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "8A7E62"))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SurfaceBg)
    }
}

#Preview {
    MainTabView()
}
