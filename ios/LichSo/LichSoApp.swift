import SwiftUI

// MARK: - Root App
@main
struct LichSoApp: App {
    @StateObject private var settingsVM = SettingsViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(settingsVM)
                .preferredColorScheme(settingsVM.darkModeEnabled ? .dark : .light)
        }
    }
}

// MARK: - Content View (Tab Navigation)
struct ContentView: View {
    @EnvironmentObject var settingsVM: SettingsViewModel
    @StateObject private var homeVM = HomeViewModel()
    @StateObject private var calVM = CalendarViewModel()
    @StateObject private var tasksVM = TasksViewModel()
    @StateObject private var chatVM = ChatViewModel()

    @State private var selectedTab: AppTab = .home
    @State private var showChat: Bool = false
    @State private var showSettings: Bool = false
    @State private var fabOffset: CGSize = .zero
    @State private var fabPosition: CGPoint = .zero

    var colors: LichSoColors { settingsVM.darkModeEnabled ? .dark : .light }

    enum AppTab: String, CaseIterable {
        case home = "home"
        case calendar = "calendar"
        case tasks = "tasks"
        case templates = "templates"

        var title: String {
            switch self {
            case .home: return "Trang chủ"
            case .calendar: return "Lịch"
            case .tasks: return "Công việc"
            case .templates: return "Template"
            }
        }

        var icon: String {
            switch self {
            case .home: return "house"
            case .calendar: return "calendar"
            case .tasks: return "checklist"
            case .templates: return "doc.text"
            }
        }

        // SF Symbols filled variant
        var iconSelected: String {
            switch self {
            case .home: return "house.fill"
            case .calendar: return "calendar"
            case .tasks: return "checklist"
            case .templates: return "doc.text.fill"
            }
        }
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            // MARK: Main Content
            Group {
                switch selectedTab {
                case .home:
                    HomeScreen(viewModel: homeVM, onSettings: { showSettings = true })
                case .calendar:
                    CalendarScreen(viewModel: calVM)
                case .tasks:
                    TasksScreen(viewModel: tasksVM)
                case .templates:
                    TemplatesScreen()
                        .environmentObject(tasksVM)
                }
            }
            .environment(\.lichSoColors, colors)
            .padding(.bottom, showChat ? 0 : 80)
            .sheet(isPresented: $showSettings) {
                SettingsScreen()
                    .environmentObject(settingsVM)
                    .environment(\.lichSoColors, colors)
            }

            // MARK: Bottom Nav Bar
            if !showChat {
                VStack(spacing: 0) {
                    Divider().overlay(colors.border)
                    HStack(spacing: 0) {
                        ForEach(AppTab.allCases, id: \.self) { tab in
                            TabBarItem(
                                tab: tab,
                                isSelected: selectedTab == tab,
                                colors: colors
                            )
                            .onTapGesture { selectedTab = tab }
                        }
                    }
                    .frame(height: 56)
                    .padding(.bottom, 8)
                }
                .background(colors.bg2.ignoresSafeArea(edges: .bottom))
                .transition(.move(edge: .bottom))
            }

            // MARK: Robot FAB
            if !showChat {
                GeometryReader { geo in
                    RobotFAB(colors: colors)
                        .frame(width: 56, height: 56)
                        .position(
                            x: fabPosition.x == 0 ? geo.size.width - 46 : fabPosition.x,
                            y: fabPosition.y == 0 ? geo.size.height - 120 : fabPosition.y
                        )
                        .offset(fabOffset)
                        .gesture(
                            DragGesture()
                                .onChanged { value in fabOffset = value.translation }
                                .onEnded { value in
                                    fabPosition = CGPoint(
                                        x: (fabPosition.x == 0 ? geo.size.width - 46 : fabPosition.x) + value.translation.width,
                                        y: (fabPosition.y == 0 ? geo.size.height - 120 : fabPosition.y) + value.translation.height
                                    )
                                    fabOffset = .zero
                                }
                        )
                        .onTapGesture { showChat = true }
                }
                .ignoresSafeArea(edges: .bottom)
            }

            // MARK: Chat Overlay
            if showChat {
                ChatScreen(viewModel: chatVM, onClose: { showChat = false })
                    .environment(\.lichSoColors, colors)
                    .transition(.move(edge: .trailing).combined(with: .opacity))
                    .zIndex(10)
            }
        }
        .animation(.easeInOut(duration: 0.25), value: showChat)
        .ignoresSafeArea(edges: .bottom)
        .environment(\.lichSoColors, colors)
        .background(colors.bg.ignoresSafeArea())
    }
}

// MARK: - Tab Bar Item
struct TabBarItem: View {
    let tab: ContentView.AppTab
    let isSelected: Bool
    let colors: LichSoColors

    var body: some View {
        VStack(spacing: 3) {
            Image(systemName: isSelected ? tab.iconSelected : tab.icon)
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(isSelected ? colors.gold2 : colors.textTertiary)
            Text(tab.title)
                .font(.system(size: 9, weight: isSelected ? .semibold : .medium))
                .foregroundColor(isSelected ? colors.gold2 : colors.textTertiary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 4)
        .overlay(alignment: .top) {
            if isSelected {
                RoundedRectangle(cornerRadius: 2)
                    .fill(colors.gold2)
                    .frame(width: 24, height: 3)
                    .offset(y: -4)
            }
        }
    }
}
