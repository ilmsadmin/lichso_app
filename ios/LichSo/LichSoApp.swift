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

// MARK: - Content View
struct ContentView: View {
    @EnvironmentObject var settingsVM: SettingsViewModel
    @StateObject private var homeVM = HomeViewModel()
    @StateObject private var chatVM = ChatViewModel()
    @StateObject private var calendarVM = CalendarViewModel()
    @StateObject private var tasksVM = TasksViewModel()

    @State private var showSettings: Bool = false
    @State private var showCalendar: Bool = false
    @State private var showTasks: Bool = false
    @State private var splashFinished: Bool = false
    @State private var splashFadeOut: Bool = false

    var colors: LichSoColors { settingsVM.darkModeEnabled ? .dark : .light }

    var body: some View {
        ZStack {
            // MARK: Main Content (always present, hidden behind splash initially)
            HomeScreen(
                viewModel: homeVM,
                chatViewModel: chatVM,
                onSettings: { showSettings = true },
                onCalendar: { showCalendar = true },
                onTasks: { showTasks = true }
            )
                .environment(\.lichSoColors, colors)
                .sheet(isPresented: $showSettings) {
                    SettingsScreen()
                        .environmentObject(settingsVM)
                        .environment(\.lichSoColors, colors)
                }
                .sheet(isPresented: $showCalendar) {
                    CalendarScreen(viewModel: calendarVM)
                        .environment(\.lichSoColors, colors)
                }
                .sheet(isPresented: $showTasks) {
                    TasksScreen(viewModel: tasksVM)
                        .environment(\.lichSoColors, colors)
                }

            // MARK: Splash Overlay
            if !splashFinished {
                SplashScreen()
                    .environment(\.lichSoColors, colors)
                    .zIndex(100)
                    .opacity(splashFadeOut ? 0 : 1)
                    .scaleEffect(splashFadeOut ? 1.08 : 1)
            }
        }
        .ignoresSafeArea(edges: .bottom)
        .environment(\.lichSoColors, colors)
        .background(colors.bg.ignoresSafeArea())
        .onAppear {
            // After 1.5 seconds, start fade-out transition
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                withAnimation(.easeInOut(duration: 0.5)) {
                    splashFadeOut = true
                }
                // Remove splash from view tree after animation completes
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                    splashFinished = true
                }
            }
        }
    }
}
