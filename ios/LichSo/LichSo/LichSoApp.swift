import SwiftUI
import SwiftData
import UserNotifications

@main
struct LichSoApp: App {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @AppStorage("setting_daily_reminder") private var dailyReminder = true
    @AppStorage("setting_reminder_hour") private var reminderHour = 7
    @AppStorage("setting_reminder_minute") private var reminderMinute = 0
    @StateObject private var appState = AppState()

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
                    .preferredColorScheme(appState.preferredColorScheme)
                    .onAppear {
                        setupNotifications()
                    }
            }
        }
        .modelContainer(sharedModelContainer)
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

    // Published so any change triggers a re-render of views observing AppState
    @Published var themePreference: String = UserDefaults.standard.string(forKey: "setting_theme") ?? "Theo hệ thống"

    private var defaultsObserver: NSObjectProtocol?

    init() {
        // Watch UserDefaults so SettingsViewModel writes propagate here automatically
        defaultsObserver = NotificationCenter.default.addObserver(
            forName: UserDefaults.didChangeNotification,
            object: UserDefaults.standard,
            queue: .main
        ) { [weak self] _ in
            let newValue = UserDefaults.standard.string(forKey: "setting_theme") ?? "Theo hệ thống"
            if self?.themePreference != newValue {
                self?.themePreference = newValue
            }
        }
    }

    var preferredColorScheme: ColorScheme? {
        switch themePreference {
        case "Sáng": return .light
        case "Tối":  return .dark
        default:     return nil  // follows system
        }
    }
}
