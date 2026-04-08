import Foundation
import SwiftData

// MARK: - LichSo Database Container

enum LichSoDatabase {
    static let schema = Schema([
        TaskEntity.self,
        NoteEntity.self,
        ReminderEntity.self,
        ChatMessageEntity.self,
        BookmarkEntity.self,
        NotificationEntity.self,
        FamilyMemberEntity.self,
        MemorialDayEntity.self,
        MemorialChecklistEntity.self,
        FamilySettingsEntity.self,
        MemberPhotoEntity.self,
    ])

    static var modelConfiguration: ModelConfiguration {
        ModelConfiguration(
            "lichso",
            schema: schema,
            isStoredInMemoryOnly: false,
            allowsSave: true
        )
    }

    @MainActor
    static var sharedModelContainer: ModelContainer = {
        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()
}
