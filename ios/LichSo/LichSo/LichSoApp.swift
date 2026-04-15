import SwiftUI
import SwiftData
import UserNotifications

@main
struct LichSoApp: App {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @AppStorage("setting_daily_reminder") private var dailyReminder = true
    @AppStorage("setting_reminder_hour") private var reminderHour = 7
    @AppStorage("setting_reminder_minute") private var reminderMinute = 0
    @AppStorage("setting_theme") private var themePreference: String = "Theo hệ thống"
    @StateObject private var appState = AppState()

    /// Map setting string → UIKit interface style
    private var uiStyle: UIUserInterfaceStyle {
        switch themePreference {
        case "Sáng": return .light
        case "Tối":  return .dark
        default:     return .unspecified   // follow system
        }
    }

    var sharedModelContainer: ModelContainer = {
        let schema = Schema([
            ChatMessageEntity.self,
            TaskEntity.self,
            NoteEntity.self,
            ReminderEntity.self,
            BookmarkEntity.self,
            NotificationEntity.self,
            FamilyMemberEntity.self,
            MemorialDayEntity.self,
            MemorialChecklistEntity.self,
            FamilySettingsEntity.self,
            MemberPhotoEntity.self
        ])
        let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            Group {
                if appState.isSplashActive {
                    SplashScreen {
                        withAnimation(.easeInOut(duration: 0.5)) {
                            appState.isSplashActive = false
                        }
                    }
                } else if !hasCompletedOnboarding {
                    OnboardingScreen {
                        hasCompletedOnboarding = true
                    }
                } else {
                    MainTabView()
                        .environmentObject(appState)
                        .onAppear {
                            setupNotifications()
                        }
                }
            }
            .onAppear { applyTheme() }
            .onChange(of: themePreference) { _, _ in applyTheme() }
        }
        .modelContainer(sharedModelContainer)
    }

    /// Apply theme via UIKit window style — instant, no view recreation
    private func applyTheme() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
        for window in windowScene.windows {
            window.overrideUserInterfaceStyle = uiStyle
        }
    }

    private func setupNotifications() {
        guard dailyReminder else { return }
        NotificationScheduler.shared.setupAllNotifications(
            dailyHour: reminderHour,
            dailyMinute: reminderMinute
        )
    }
}

class AppState: ObservableObject {
    @Published var isSplashActive = true
}
