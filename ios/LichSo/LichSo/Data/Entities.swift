import Foundation
import SwiftData

// ═══════════════════════════════════════════
// Database Entities — Giữ nguyên cấu trúc từ Android Room
// KHÔNG ĐƯỢC THAY ĐỔI CẤU TRÚC DATABASE
// ═══════════════════════════════════════════

@Model
final class TaskEntity {
    @Attribute(.unique) var id: Int64
    var title: String
    var taskDescription: String
    var dueDate: Int64?
    var dueTime: String?
    var priority: Int
    var isDone: Bool
    var labels: String
    var hasReminder: Bool
    var createdAt: Int64
    var updatedAt: Int64

    init(id: Int64 = 0, title: String, taskDescription: String = "", dueDate: Int64? = nil, dueTime: String? = nil, priority: Int = 1, isDone: Bool = false, labels: String = "", hasReminder: Bool = false) {
        self.id = id
        self.title = title
        self.taskDescription = taskDescription
        self.dueDate = dueDate
        self.dueTime = dueTime
        self.priority = priority
        self.isDone = isDone
        self.labels = labels
        self.hasReminder = hasReminder
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        self.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class NoteEntity {
    @Attribute(.unique) var id: Int64
    var title: String
    var content: String
    var colorIndex: Int
    var isPinned: Bool
    var labels: String
    var createdAt: Int64
    var updatedAt: Int64

    init(id: Int64 = 0, title: String, content: String = "", colorIndex: Int = 0, isPinned: Bool = false, labels: String = "") {
        self.id = id
        self.title = title
        self.content = content
        self.colorIndex = colorIndex
        self.isPinned = isPinned
        self.labels = labels
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        self.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class ReminderEntity {
    @Attribute(.unique) var id: Int64
    var title: String
    var subtitle: String
    var triggerTime: Int64
    var repeatType: Int     // 0=Once, 1=Daily, 2=Weekly, 3=Monthly, 4=MonthlyLunar, 5=Yearly
    var isEnabled: Bool
    var useLunar: Bool
    var advanceDays: Int
    var category: Int       // 0=Holiday, 1=Birthday, 2=Lunar, 3=Personal, 4=Memorial
    var labels: String
    var createdAt: Int64

    init(id: Int64 = 0, title: String, subtitle: String = "", triggerTime: Int64, repeatType: Int = 0, isEnabled: Bool = true, useLunar: Bool = false, advanceDays: Int = 0, category: Int = 0, labels: String = "") {
        self.id = id
        self.title = title
        self.subtitle = subtitle
        self.triggerTime = triggerTime
        self.repeatType = repeatType
        self.isEnabled = isEnabled
        self.useLunar = useLunar
        self.advanceDays = advanceDays
        self.category = category
        self.labels = labels
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class BookmarkEntity {
    @Attribute(.unique) var id: Int64
    var solarDay: Int
    var solarMonth: Int
    var solarYear: Int
    var label: String
    var note: String
    var colorIndex: Int
    var createdAt: Int64

    init(id: Int64 = 0, solarDay: Int, solarMonth: Int, solarYear: Int, label: String = "", note: String = "", colorIndex: Int = 0) {
        self.id = id
        self.solarDay = solarDay
        self.solarMonth = solarMonth
        self.solarYear = solarYear
        self.label = label
        self.note = note
        self.colorIndex = colorIndex
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class NotificationEntity {
    @Attribute(.unique) var id: Int64
    var title: String
    var notificationDescription: String
    var type: String    // daily, holiday, ai, reminder, system, good_day
    var isRead: Bool
    var createdAt: Int64

    init(id: Int64 = 0, title: String, notificationDescription: String = "", type: String = "system", isRead: Bool = false) {
        self.id = id
        self.title = title
        self.notificationDescription = notificationDescription
        self.type = type
        self.isRead = isRead
        self.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
    }
}

@Model
final class ChatMessageEntity {
    @Attribute(.unique) var id: Int64
    var content: String
    var isUser: Bool
    var timestamp: Int64

    init(id: Int64 = 0, content: String, isUser: Bool) {
        self.id = id
        self.content = content
        self.isUser = isUser
        self.timestamp = Int64(Date().timeIntervalSince1970 * 1000)
    }
}
