import Foundation
import SwiftData
import UIKit

// ═══════════════════════════════════════════
// App Backup Manager
// Full app backup / restore — matches Android's AppBackupManager
//
// Backs up ALL user data:
//  - App settings (UserDefaults)
//  - Tasks, Notes, Reminders, Bookmarks, Notifications, Chat messages (SwiftData)
//  - Family tree: members, memorials, checklist, settings, photos (SwiftData)
//  - Profile avatar (Base64)
//  - Family member avatars & photos (Base64)
// ═══════════════════════════════════════════

struct AppBackupManager {

    // MARK: - Backup Data Models

    struct AppBackupData: Codable {
        var version: Int = 1
        var exportDate: String = ""
        var appId: String = "com.lichso.app"
        var type: String = "full_backup"

        // App settings
        var appSettings: [String: AnyCodable] = [:]

        // Profile avatar
        var profileAvatarBase64: String?

        // Room DB tables
        var tasks: [BackupTask] = []
        var notes: [BackupNote] = []
        var reminders: [BackupReminder] = []
        var bookmarks: [BackupBookmark] = []
        var notifications: [BackupNotification] = []
        var chatMessages: [BackupChatMessage] = []

        // Family tree
        var familySettings: BackupFamilySettings?
        var familyMembers: [BackupFamilyMember] = []
        var memorialDays: [BackupMemorialDay] = []
        var memorialChecklist: [BackupMemorialChecklist] = []
        var memberPhotos: [BackupMemberPhoto] = []
    }

    struct BackupTask: Codable {
        var title: String
        var description: String = ""
        var dueDate: Int64?
        var dueTime: String?
        var priority: Int = 1
        var isDone: Bool = false
        var labels: String = ""
        var hasReminder: Bool = false
        var createdAt: Int64 = 0
        var updatedAt: Int64 = 0
    }

    struct BackupNote: Codable {
        var title: String
        var content: String = ""
        var colorIndex: Int = 0
        var isPinned: Bool = false
        var labels: String = ""
        var createdAt: Int64 = 0
        var updatedAt: Int64 = 0
    }

    struct BackupReminder: Codable {
        var title: String
        var subtitle: String = ""
        var triggerTime: Int64 = 0
        var repeatType: Int = 0
        var isEnabled: Bool = true
        var useLunar: Bool = false
        var advanceDays: Int = 0
        var category: Int = 0
        var labels: String = ""
        var createdAt: Int64 = 0
    }

    struct BackupBookmark: Codable {
        var solarDay: Int
        var solarMonth: Int
        var solarYear: Int
        var label: String = ""
        var note: String = ""
        var colorIndex: Int = 0
        var createdAt: Int64 = 0
    }

    struct BackupNotification: Codable {
        var title: String
        var description: String = ""
        var type: String = "system"
        var isRead: Bool = false
        var createdAt: Int64 = 0
    }

    struct BackupChatMessage: Codable {
        var content: String
        var isUser: Bool
        var timestamp: Int64 = 0
    }

    struct BackupFamilySettings: Codable {
        var familyName: String
        var familyCrest: String
        var hometown: String
        var treeDisplayMode: String
        var treeTheme: String
        var showAvatar: Bool
        var showYears: Bool
        var remindMemorial: Bool
        var remindBirthday: Bool
        var remindDaysBefore: Int
    }

    struct BackupFamilyMember: Codable {
        var id: String
        var name: String
        var role: String
        var gender: String
        var generation: Int
        var birthYear: Int?
        var deathYear: Int?
        var birthDateLunar: String?
        var deathDateLunar: String?
        var canChi: String?
        var menh: String?
        var zodiacEmoji: String?
        var menhEmoji: String?
        var hanhEmoji: String?
        var menhDetail: String?
        var zodiacName: String?
        var menhName: String?
        var hometown: String?
        var occupation: String?
        var isSelf: Bool = false
        var isElder: Bool = false
        var emoji: String = "👤"
        var spouseIds: String = ""
        var spouseOrder: Int = 0
        var parentIds: String = ""
        var note: String?
        var avatarBase64: String?
    }

    struct BackupMemorialDay: Codable {
        var id: String
        var memberId: String
        var memberName: String
        var relation: String
        var lunarDay: Int
        var lunarMonth: Int
        var lunarLeap: Int = 0
        var note: String?
        var remindBefore3Days: Bool = true
        var remindBefore1Day: Bool = true
    }

    struct BackupMemorialChecklist: Codable {
        var memorialId: String
        var text: String
        var isDone: Bool = false
        var sortOrder: Int = 0
    }

    struct BackupMemberPhoto: Codable {
        var memberId: String
        var caption: String?
        var sortOrder: Int = 0
        var photoBase64: String?
    }

    // MARK: - Generate file name

    static func generateFileName() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMdd_HHmmss"
        let dateStr = formatter.string(from: Date())
        return "lichso_backup_\(dateStr).json"
    }

    // MARK: - Build Backup JSON

    static func buildBackupData(context: ModelContext) -> AppBackupData {
        var data = AppBackupData()
        let now = Date()
        let formatter = ISO8601DateFormatter()
        data.exportDate = formatter.string(from: now)

        // ── 1. App settings (UserDefaults) ──
        let settingKeys = [
            "setting_week_start", "setting_theme",
            "setting_show_lunar", "setting_show_hoang_dao",
            "setting_show_festivals", "setting_show_quote",
            "setting_daily_reminder", "setting_reminder_hour", "setting_reminder_minute",
            "setting_festival_reminder", "setting_icloud_sync",
            "setting_location", "setting_temp_unit",
            "displayName", "profile_email", "gender",
            "birthDay", "birthMonth", "birthYear",
            "birthHour", "birthMinute"
        ]
        var settings: [String: AnyCodable] = [:]
        for key in settingKeys {
            if let value = UserDefaults.standard.object(forKey: key) {
                settings[key] = AnyCodable(value)
            }
        }
        data.appSettings = settings

        // ── 2. Profile avatar ──
        let avatarPath = UserDefaults.standard.string(forKey: "profile_avatar_path") ?? ""
        if !avatarPath.isEmpty, FileManager.default.fileExists(atPath: avatarPath),
           let imgData = try? Data(contentsOf: URL(fileURLWithPath: avatarPath)) {
            data.profileAvatarBase64 = imgData.base64EncodedString()
        }

        // ── 3. Tasks ──
        if let tasks = try? context.fetch(FetchDescriptor<TaskEntity>()) {
            data.tasks = tasks.map { t in
                BackupTask(title: t.title, description: t.taskDescription,
                           dueDate: t.dueDate, dueTime: t.dueTime,
                           priority: t.priority, isDone: t.isDone,
                           labels: t.labels, hasReminder: t.hasReminder,
                           createdAt: t.createdAt, updatedAt: t.updatedAt)
            }
        }

        // ── 4. Notes ──
        if let notes = try? context.fetch(FetchDescriptor<NoteEntity>()) {
            data.notes = notes.map { n in
                BackupNote(title: n.title, content: n.content,
                           colorIndex: n.colorIndex, isPinned: n.isPinned,
                           labels: n.labels, createdAt: n.createdAt, updatedAt: n.updatedAt)
            }
        }

        // ── 5. Reminders ──
        if let reminders = try? context.fetch(FetchDescriptor<ReminderEntity>()) {
            data.reminders = reminders.map { r in
                BackupReminder(title: r.title, subtitle: r.subtitle,
                               triggerTime: r.triggerTime, repeatType: r.repeatType,
                               isEnabled: r.isEnabled, useLunar: r.useLunar,
                               advanceDays: r.advanceDays, category: r.category,
                               labels: r.labels, createdAt: r.createdAt)
            }
        }

        // ── 6. Bookmarks ──
        if let bookmarks = try? context.fetch(FetchDescriptor<BookmarkEntity>()) {
            data.bookmarks = bookmarks.map { b in
                BackupBookmark(solarDay: b.solarDay, solarMonth: b.solarMonth,
                               solarYear: b.solarYear, label: b.label,
                               note: b.note, colorIndex: b.colorIndex,
                               createdAt: b.createdAt)
            }
        }

        // ── 7. Notifications ──
        if let notifications = try? context.fetch(FetchDescriptor<NotificationEntity>()) {
            data.notifications = notifications.map { n in
                BackupNotification(title: n.title, description: n.notificationDescription,
                                   type: n.type, isRead: n.isRead, createdAt: n.createdAt)
            }
        }

        // ── 8. Chat messages ──
        if let messages = try? context.fetch(FetchDescriptor<ChatMessageEntity>()) {
            data.chatMessages = messages.map { m in
                BackupChatMessage(content: m.content, isUser: m.isUser, timestamp: m.timestamp)
            }
        }

        // ── 9. Family settings ──
        if let fs = try? context.fetch(FetchDescriptor<FamilySettingsEntity>()).first {
            data.familySettings = BackupFamilySettings(
                familyName: fs.familyName, familyCrest: fs.familyCrest,
                hometown: fs.hometown, treeDisplayMode: fs.treeDisplayMode,
                treeTheme: fs.treeTheme, showAvatar: fs.showAvatar,
                showYears: fs.showYears, remindMemorial: fs.remindMemorial,
                remindBirthday: fs.remindBirthday, remindDaysBefore: fs.remindDaysBefore)
        }

        // ── 10. Family members ──
        if let members = try? context.fetch(FetchDescriptor<FamilyMemberEntity>()) {
            data.familyMembers = members.map { m in
                var avatarBase64: String? = nil
                if let path = m.avatarPath, FileManager.default.fileExists(atPath: path),
                   let imgData = try? Data(contentsOf: URL(fileURLWithPath: path)) {
                    // Limit to ~500KB
                    if imgData.count <= 500_000 {
                        avatarBase64 = imgData.base64EncodedString()
                    } else if let uiImg = UIImage(data: imgData),
                              let compressed = uiImg.jpegData(compressionQuality: 0.5) {
                        avatarBase64 = compressed.base64EncodedString()
                    }
                }
                return BackupFamilyMember(
                    id: m.id, name: m.name, role: m.role, gender: m.gender,
                    generation: m.generation, birthYear: m.birthYear, deathYear: m.deathYear,
                    birthDateLunar: m.birthDateLunar, deathDateLunar: m.deathDateLunar,
                    canChi: m.canChi, menh: m.menh, zodiacEmoji: m.zodiacEmoji,
                    menhEmoji: m.menhEmoji, hanhEmoji: m.hanhEmoji, menhDetail: m.menhDetail,
                    zodiacName: m.zodiacName, menhName: m.menhName,
                    hometown: m.hometown, occupation: m.occupation,
                    isSelf: m.isSelf, isElder: m.isElder, emoji: m.emoji,
                    spouseIds: m.spouseIds, spouseOrder: m.spouseOrder,
                    parentIds: m.parentIds, note: m.note,
                    avatarBase64: avatarBase64)
            }
        }

        // ── 11. Memorial days ──
        if let memorials = try? context.fetch(FetchDescriptor<MemorialDayEntity>()) {
            data.memorialDays = memorials.map { m in
                BackupMemorialDay(id: m.id, memberId: m.memberId, memberName: m.memberName,
                                  relation: m.relation, lunarDay: m.lunarDay,
                                  lunarMonth: m.lunarMonth, lunarLeap: m.lunarLeap,
                                  note: m.note, remindBefore3Days: m.remindBefore3Days,
                                  remindBefore1Day: m.remindBefore1Day)
            }
        }

        // ── 12. Memorial checklist ──
        if let items = try? context.fetch(FetchDescriptor<MemorialChecklistEntity>()) {
            data.memorialChecklist = items.map { c in
                BackupMemorialChecklist(memorialId: c.memorialId, text: c.text,
                                        isDone: c.isDone, sortOrder: c.sortOrder)
            }
        }

        // ── 13. Member photos ──
        if let photos = try? context.fetch(FetchDescriptor<MemberPhotoEntity>()) {
            data.memberPhotos = photos.compactMap { p in
                var photoBase64: String? = nil
                if FileManager.default.fileExists(atPath: p.filePath),
                   let imgData = try? Data(contentsOf: URL(fileURLWithPath: p.filePath)) {
                    if imgData.count <= 500_000 {
                        photoBase64 = imgData.base64EncodedString()
                    } else if let uiImg = UIImage(data: imgData),
                              let compressed = uiImg.jpegData(compressionQuality: 0.5) {
                        photoBase64 = compressed.base64EncodedString()
                    }
                }
                return BackupMemberPhoto(memberId: p.memberId, caption: p.caption,
                                         sortOrder: p.sortOrder, photoBase64: photoBase64)
            }
        }

        return data
    }

    static func buildBackupJSON(context: ModelContext) -> String? {
        let data = buildBackupData(context: context)
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        guard let jsonData = try? encoder.encode(data) else { return nil }
        return String(data: jsonData, encoding: .utf8)
    }

    // MARK: - Parse Backup JSON

    static func parseBackupJSON(_ json: String) -> AppBackupData? {
        guard let jsonData = json.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(AppBackupData.self, from: jsonData)
    }

    // MARK: - Get Backup Summary

    static func getBackupSummary(_ data: AppBackupData) -> String {
        var parts: [String] = []
        if !data.tasks.isEmpty      { parts.append("\(data.tasks.count) công việc") }
        if !data.notes.isEmpty      { parts.append("\(data.notes.count) ghi chú") }
        if !data.reminders.isEmpty  { parts.append("\(data.reminders.count) nhắc nhở") }
        if !data.bookmarks.isEmpty  { parts.append("\(data.bookmarks.count) ngày đã lưu") }
        if !data.chatMessages.isEmpty { parts.append("\(data.chatMessages.count) tin nhắn AI") }
        if !data.familyMembers.isEmpty { parts.append("\(data.familyMembers.count) thành viên gia phả") }
        if !data.memorialDays.isEmpty { parts.append("\(data.memorialDays.count) ngày giỗ") }
        if parts.isEmpty { return "Bản sao lưu trống" }
        return parts.joined(separator: "\n")
    }

    // MARK: - Restore from Backup

    static func restoreFromBackup(_ data: AppBackupData, context: ModelContext) {
        let now = Int64(Date().timeIntervalSince1970 * 1000)

        // ── 1. Restore app settings ──
        for (key, value) in data.appSettings {
            UserDefaults.standard.set(value.value, forKey: key)
        }

        // ── 2. Restore profile avatar ──
        if let base64 = data.profileAvatarBase64, let imgData = Data(base64Encoded: base64) {
            let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            let avatarPath = dir.appendingPathComponent("profile_avatar.jpg").path
            try? imgData.write(to: URL(fileURLWithPath: avatarPath))
            UserDefaults.standard.set(avatarPath, forKey: "profile_avatar_path")
        }

        // ── 3. Restore tasks ──
        if let existing = try? context.fetch(FetchDescriptor<TaskEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for t in data.tasks {
            let entity = TaskEntity(id: now + Int64.random(in: 1...999999),
                                    title: t.title, taskDescription: t.description,
                                    dueDate: t.dueDate, dueTime: t.dueTime,
                                    priority: t.priority, isDone: t.isDone,
                                    labels: t.labels, hasReminder: t.hasReminder)
            entity.createdAt = t.createdAt
            entity.updatedAt = t.updatedAt
            context.insert(entity)
        }

        // ── 4. Restore notes ──
        if let existing = try? context.fetch(FetchDescriptor<NoteEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for n in data.notes {
            let entity = NoteEntity(id: now + Int64.random(in: 1...999999),
                                    title: n.title, content: n.content,
                                    colorIndex: n.colorIndex, isPinned: n.isPinned,
                                    labels: n.labels)
            entity.createdAt = n.createdAt
            entity.updatedAt = n.updatedAt
            context.insert(entity)
        }

        // ── 5. Restore reminders ──
        if let existing = try? context.fetch(FetchDescriptor<ReminderEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for r in data.reminders {
            let entity = ReminderEntity(id: now + Int64.random(in: 1...999999),
                                        title: r.title, subtitle: r.subtitle,
                                        triggerTime: r.triggerTime, repeatType: r.repeatType,
                                        isEnabled: r.isEnabled, useLunar: r.useLunar,
                                        advanceDays: r.advanceDays, category: r.category,
                                        labels: r.labels)
            entity.createdAt = r.createdAt
            context.insert(entity)
        }

        // ── 6. Restore bookmarks ──
        if let existing = try? context.fetch(FetchDescriptor<BookmarkEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for b in data.bookmarks {
            let entity = BookmarkEntity(id: now + Int64.random(in: 1...999999),
                                        solarDay: b.solarDay, solarMonth: b.solarMonth,
                                        solarYear: b.solarYear, label: b.label,
                                        note: b.note, colorIndex: b.colorIndex)
            entity.createdAt = b.createdAt
            context.insert(entity)
        }

        // ── 7. Restore notifications ──
        if let existing = try? context.fetch(FetchDescriptor<NotificationEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for n in data.notifications {
            let entity = NotificationEntity(id: now + Int64.random(in: 1...999999),
                                            title: n.title,
                                            notificationDescription: n.description,
                                            type: n.type, isRead: n.isRead)
            entity.createdAt = n.createdAt
            context.insert(entity)
        }

        // ── 8. Restore chat messages ──
        if let existing = try? context.fetch(FetchDescriptor<ChatMessageEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for m in data.chatMessages {
            let entity = ChatMessageEntity(id: now + Int64.random(in: 1...999999),
                                           content: m.content, isUser: m.isUser)
            entity.timestamp = m.timestamp
            context.insert(entity)
        }

        // ── 9. Restore family settings ──
        if let fs = data.familySettings {
            if let existing = try? context.fetch(FetchDescriptor<FamilySettingsEntity>()) {
                existing.forEach { context.delete($0) }
            }
            let entity = FamilySettingsEntity(
                familyName: fs.familyName, familyCrest: fs.familyCrest,
                hometown: fs.hometown, treeDisplayMode: fs.treeDisplayMode,
                treeTheme: fs.treeTheme, showAvatar: fs.showAvatar,
                showYears: fs.showYears, remindMemorial: fs.remindMemorial,
                remindBirthday: fs.remindBirthday, remindDaysBefore: fs.remindDaysBefore)
            context.insert(entity)
        }

        // ── 10. Restore family members ──
        if let existing = try? context.fetch(FetchDescriptor<FamilyMemberEntity>()) {
            existing.forEach { context.delete($0) }
        }
        let avatarDir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            .appendingPathComponent("member_avatars")
        try? FileManager.default.createDirectory(at: avatarDir, withIntermediateDirectories: true)

        for m in data.familyMembers {
            var avatarPath: String? = nil
            if let base64 = m.avatarBase64, let imgData = Data(base64Encoded: base64) {
                let filePath = avatarDir.appendingPathComponent("\(m.id).jpg")
                try? imgData.write(to: filePath)
                avatarPath = filePath.path
            }
            let entity = FamilyMemberEntity(
                id: m.id, name: m.name, role: m.role, gender: m.gender,
                generation: m.generation, birthYear: m.birthYear, deathYear: m.deathYear,
                birthDateLunar: m.birthDateLunar, deathDateLunar: m.deathDateLunar,
                canChi: m.canChi, menh: m.menh, zodiacEmoji: m.zodiacEmoji,
                menhEmoji: m.menhEmoji, hanhEmoji: m.hanhEmoji, menhDetail: m.menhDetail,
                zodiacName: m.zodiacName, menhName: m.menhName,
                hometown: m.hometown, occupation: m.occupation,
                isSelf: m.isSelf, isElder: m.isElder, emoji: m.emoji,
                spouseIds: m.spouseIds, spouseOrder: m.spouseOrder,
                parentIds: m.parentIds, note: m.note, avatarPath: avatarPath)
            context.insert(entity)
        }

        // ── 11. Restore memorial days ──
        if let existing = try? context.fetch(FetchDescriptor<MemorialDayEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for m in data.memorialDays {
            let entity = MemorialDayEntity(
                id: m.id, memberId: m.memberId, memberName: m.memberName,
                relation: m.relation, lunarDay: m.lunarDay, lunarMonth: m.lunarMonth,
                lunarLeap: m.lunarLeap, note: m.note,
                remindBefore3Days: m.remindBefore3Days, remindBefore1Day: m.remindBefore1Day)
            context.insert(entity)
        }

        // ── 12. Restore memorial checklist ──
        if let existing = try? context.fetch(FetchDescriptor<MemorialChecklistEntity>()) {
            existing.forEach { context.delete($0) }
        }
        for c in data.memorialChecklist {
            let entity = MemorialChecklistEntity(
                memorialId: c.memorialId, text: c.text,
                isDone: c.isDone, sortOrder: c.sortOrder)
            context.insert(entity)
        }

        // ── 13. Restore member photos ──
        if let existing = try? context.fetch(FetchDescriptor<MemberPhotoEntity>()) {
            existing.forEach { context.delete($0) }
        }
        let photosDir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
            .appendingPathComponent("member_photos")
        try? FileManager.default.createDirectory(at: photosDir, withIntermediateDirectories: true)

        for p in data.memberPhotos {
            var filePath = ""
            if let base64 = p.photoBase64, let imgData = Data(base64Encoded: base64) {
                let fileName = "\(p.memberId)_\(Int64.random(in: 1...999999)).jpg"
                let fileURL = photosDir.appendingPathComponent(fileName)
                try? imgData.write(to: fileURL)
                filePath = fileURL.path
            }
            if !filePath.isEmpty {
                let entity = MemberPhotoEntity(
                    memberId: p.memberId, filePath: filePath,
                    caption: p.caption, sortOrder: p.sortOrder)
                context.insert(entity)
            }
        }

        // Save all
        try? context.save()
    }
}

// MARK: - AnyCodable helper for encoding arbitrary UserDefaults values

struct AnyCodable: Codable {
    let value: Any

    init(_ value: Any) {
        self.value = value
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let v = try? container.decode(Bool.self)   { value = v; return }
        if let v = try? container.decode(Int.self)    { value = v; return }
        if let v = try? container.decode(Double.self) { value = v; return }
        if let v = try? container.decode(String.self) { value = v; return }
        if let v = try? container.decode([String: AnyCodable].self) {
            value = v.mapValues { $0.value }; return
        }
        if let v = try? container.decode([AnyCodable].self) {
            value = v.map { $0.value }; return
        }
        value = ""
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        if let v = value as? Bool   { try container.encode(v); return }
        if let v = value as? Int    { try container.encode(v); return }
        if let v = value as? Double { try container.encode(v); return }
        if let v = value as? String { try container.encode(v); return }
        if let v = value as? [String: Any] {
            try container.encode(v.mapValues { AnyCodable($0) }); return
        }
        if let v = value as? [Any] {
            try container.encode(v.map { AnyCodable($0) }); return
        }
        try container.encode("\(value)")
    }
}
