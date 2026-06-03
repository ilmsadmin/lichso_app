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
    @StateObject private var googleAuth = GoogleAuthService.shared

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
            ItemEntity.self,
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
            // Nếu database bị hỏng, thử xóa và tạo lại với in-memory
            print("⚠️ Could not create ModelContainer: \(error). Falling back to in-memory store.")
            let fallbackConfig = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
            do {
                return try ModelContainer(for: schema, configurations: [fallbackConfig])
            } catch {
                // Trường hợp cuối cùng — tạo schema tối thiểu với in-memory (không crash)
                print("❌ Critical: Could not create any ModelContainer: \(error). Using minimal in-memory store.")
                let minimalSchema = Schema([])
                let minimalConfig = ModelConfiguration(schema: minimalSchema, isStoredInMemoryOnly: true)
                // Force try — nếu ngay cả empty schema cũng fail thì hệ thống có vấn đề nghiêm trọng
                return try! ModelContainer(for: minimalSchema, configurations: [minimalConfig])
            }
        }
    }()

    var body: some Scene {
        WindowGroup {
            // Không còn màn splash in-app (giống Android): chỉ còn LaunchScreen native
            // (nền đỏ) hiện chớp nhoáng → vào thẳng Onboarding (nếu chưa xong) hoặc Main.
            Group {
                if !hasCompletedOnboarding {
                    OnboardingScreen {
                        hasCompletedOnboarding = true
                    }
                } else {
                    MainTabView()
                        .environmentObject(appState)
                        .environmentObject(googleAuth)
                        .onAppear {
                            migrateToUnifiedItems()
                            setupNotifications()
                            // Track app open as a lightweight happy action
                            SmartRatingManager.shared.recordHappyAction(weight: 1)
                            // Attempt token refresh on launch
                            Task { await googleAuth.refreshTokenIfNeeded() }
                            // Làm nóng bàn phím để lần focus đầu tiên không bị chậm (cold-start)
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                                KeyboardWarmer.warmUp()
                            }
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
        guard dailyReminder else {
            NotificationScheduler.shared.cancelMorningSummaryNotifications()
            return
        }
        NotificationScheduler.shared.setupAllNotifications(
            dailyHour: reminderHour,
            dailyMinute: reminderMinute
        )
    }

    /// Di trú một lần: gộp NoteEntity/TaskEntity/ReminderEntity cũ → ItemEntity hợp nhất.
    /// Chạy đúng 1 lần (cờ UserDefaults). Sau khi gộp, xoá bản ghi cũ + đặt lại lịch nhắc.
    @MainActor private func migrateToUnifiedItems() {
        let key = "items_migrated_v1"
        guard !UserDefaults.standard.bool(forKey: key) else { return }
        let ctx = sharedModelContainer.mainContext
        var nextId = Int64(Date().timeIntervalSince1970 * 1000)
        func newId() -> Int64 { defer { nextId += 1 }; return nextId }
        var migrated: [ItemEntity] = []

        // Notes → ghi chú thuần
        if let notes = try? ctx.fetch(FetchDescriptor<NoteEntity>()) {
            for n in notes {
                let item = ItemEntity(
                    id: newId(), title: n.title, itemDescription: n.content, tags: n.labels,
                    isPinned: n.isPinned, colorIndex: n.colorIndex,
                    createdAt: n.createdAt, updatedAt: n.updatedAt
                )
                ctx.insert(item); migrated.append(item)
            }
            notes.forEach { ctx.delete($0) }
        }
        // Tasks → việc cần làm
        if let tasks = try? ctx.fetch(FetchDescriptor<TaskEntity>()) {
            for t in tasks {
                let item = ItemEntity(
                    id: newId(), title: t.title, itemDescription: t.taskDescription, tags: t.labels,
                    isTask: true, isDone: t.isDone, priority: t.priority, dueDate: t.dueDate,
                    dueTime: t.dueTime, createdAt: t.createdAt, updatedAt: t.updatedAt
                )
                ctx.insert(item); migrated.append(item)
            }
            tasks.forEach { ctx.delete($0) }
        }
        // Reminders → có nhắc nhở
        if let reminders = try? ctx.fetch(FetchDescriptor<ReminderEntity>()) {
            for r in reminders {
                let item = ItemEntity(
                    id: newId(), title: r.title, itemDescription: r.subtitle, tags: r.labels,
                    hasReminder: true, reminderAt: r.triggerTime, repeatType: r.repeatType,
                    useLunar: r.useLunar, advanceDays: r.advanceDays, reminderEnabled: r.isEnabled,
                    createdAt: r.createdAt, updatedAt: r.createdAt
                )
                ctx.insert(item); migrated.append(item)
            }
            reminders.forEach { ctx.delete($0) }
        }

        try? ctx.save()
        ItemReminderScheduler.rescheduleAll(migrated)
        UserDefaults.standard.set(true, forKey: key)
    }
}

class AppState: ObservableObject {
    // (Trước đây giữ cờ splash; splash in-app đã bỏ để giống Android.)
}
