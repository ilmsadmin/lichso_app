import SwiftUI
import SwiftData
import UIKit

@main
struct LichSoApp: App {
    @StateObject private var settings = AppSettings.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(settings)
                .lichSoTheme(isDark: isDarkMode)
                .preferredColorScheme(settings.colorScheme)
                .modelContainer(LichSoDatabase.sharedModelContainer)
        }
    }

    private var isDarkMode: Bool {
        switch settings.themeMode {
        case "dark": return true
        case "light": return false
        default:
            // Follow system
            return UITraitCollection.current.userInterfaceStyle == .dark
        }
    }
}
