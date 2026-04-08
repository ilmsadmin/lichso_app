import Foundation
import SwiftData

// MARK: - Task Entity
@Model
final class TaskEntity {
    @Attribute(.unique) var id: UUID
    var title: String
    var taskDescription: String
    var dueDate: Date?
    var dueTime: String?
    var priority: Int // 0=Low, 1=Medium, 2=High
    var isDone: Bool
    var labels: String // comma-separated labels
    var hasReminder: Bool
    var createdAt: Date
    var updatedAt: Date

    init(title: String, taskDescription: String = "", dueDate: Date? = nil, dueTime: String? = nil,
         priority: Int = 1, isDone: Bool = false, labels: String = "", hasReminder: Bool = false) {
        self.id = UUID()
        self.title = title
        self.taskDescription = taskDescription
        self.dueDate = dueDate
        self.dueTime = dueTime
        self.priority = priority
        self.isDone = isDone
        self.labels = labels
        self.hasReminder = hasReminder
        self.createdAt = Date()
        self.updatedAt = Date()
    }
}

// MARK: - Note Entity
@Model
final class NoteEntity {
    @Attribute(.unique) var id: UUID
    var title: String
    var content: String
    var colorIndex: Int
    var isPinned: Bool
    var labels: String
    var createdAt: Date
    var updatedAt: Date

    init(title: String, content: String = "", colorIndex: Int = 0, isPinned: Bool = false, labels: String = "") {
        self.id = UUID()
        self.title = title
        self.content = content
        self.colorIndex = colorIndex
        self.isPinned = isPinned
        self.labels = labels
        self.createdAt = Date()
        self.updatedAt = Date()
    }
}

// MARK: - Reminder Entity
@Model
final class ReminderEntity {
    @Attribute(.unique) var id: UUID
    var title: String
    var subtitle: String
    var triggerTime: Date
    var repeatType: Int // 0=Once, 1=Daily, 2=Weekly, 3=Monthly, 4=MonthlyLunar, 5=Yearly
    var isEnabled: Bool
    var useLunar: Bool
    var advanceDays: Int
    var category: Int // 0=Holiday, 1=Birthday, 2=Lunar, 3=Personal, 4=Memorial
    var labels: String
    var createdAt: Date

    init(title: String, subtitle: String = "", triggerTime: Date, repeatType: Int = 0,
         isEnabled: Bool = true, useLunar: Bool = false, advanceDays: Int = 0,
         category: Int = 0, labels: String = "") {
        self.id = UUID()
        self.title = title
        self.subtitle = subtitle
        self.triggerTime = triggerTime
        self.repeatType = repeatType
        self.isEnabled = isEnabled
        self.useLunar = useLunar
        self.advanceDays = advanceDays
        self.category = category
        self.labels = labels
        self.createdAt = Date()
    }
}

// MARK: - Bookmark Entity
@Model
final class BookmarkEntity {
    @Attribute(.unique) var id: UUID
    var solarDay: Int
    var solarMonth: Int
    var solarYear: Int
    var label: String
    var note: String
    var colorIndex: Int
    var createdAt: Date

    init(solarDay: Int, solarMonth: Int, solarYear: Int, label: String = "", note: String = "", colorIndex: Int = 0) {
        self.id = UUID()
        self.solarDay = solarDay
        self.solarMonth = solarMonth
        self.solarYear = solarYear
        self.label = label
        self.note = note
        self.colorIndex = colorIndex
        self.createdAt = Date()
    }
}

// MARK: - Notification Entity
@Model
final class NotificationEntity {
    @Attribute(.unique) var id: UUID
    var title: String
    var notificationDescription: String
    var type: String // daily, holiday, ai, reminder, system, good_day
    var isRead: Bool
    var createdAt: Date

    init(title: String, notificationDescription: String = "", type: String = "system", isRead: Bool = false) {
        self.id = UUID()
        self.title = title
        self.notificationDescription = notificationDescription
        self.type = type
        self.isRead = isRead
        self.createdAt = Date()
    }
}

// MARK: - Chat Message Entity
@Model
final class ChatMessageEntity {
    @Attribute(.unique) var id: UUID
    var content: String
    var isUser: Bool
    var timestamp: Date

    init(content: String, isUser: Bool) {
        self.id = UUID()
        self.content = content
        self.isUser = isUser
        self.timestamp = Date()
    }
}
