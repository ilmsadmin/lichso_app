import SwiftUI
import SwiftData

@main
struct LichSoApp: App {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @StateObject private var appState = AppState()

    var sharedModelContainer: ModelContainer = {
        let schema = Schema([
            ChatMessageEntity.self,
            TaskEntity.self,
            NoteEntity.self,
            ReminderEntity.self,
            BookmarkEntity.self,
            NotificationEntity.self
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
            }
        }
        .modelContainer(sharedModelContainer)
    }
}

class AppState: ObservableObject {
    @Published var isSplashActive = true
}
