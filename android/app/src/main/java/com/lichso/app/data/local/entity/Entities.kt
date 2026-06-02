package com.lichso.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ── Đối tượng hợp nhất: Ghi chú + Công việc + Nhắc nhở (1 object duy nhất) ──
 *
 * Trước đây là 3 entity riêng (TaskEntity / NoteEntity / ReminderEntity). Giờ gộp
 * thành một "item" linh hoạt: luôn có title + description (mô tả) + tags. Các năng
 * lực dưới đây BẬT/TẮT độc lập nên một item có thể vừa là ghi chú, vừa có checkbox
 * việc cần làm, vừa có nhắc nhở:
 *   • Task     : isTask + priority + isDone + dueDate/dueTime
 *   • Reminder : hasReminder + reminderAt + repeatType + useLunar + advanceDays + reminderEnabled
 *   • Note/UI  : isPinned + colorIndex
 */
@Entity(
    tableName = "items",
    indices = [Index(value = ["dueDate"]), Index(value = ["reminderAt"])]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val tags: String = "",            // comma-separated tags (gộp từ labels cũ)

    // ── Task capability ──
    val isTask: Boolean = false,
    val isDone: Boolean = false,
    val priority: Int = 1,            // 0=Low, 1=Medium, 2=High
    val dueDate: Long? = null,        // epoch millis (ngày đến hạn)
    val dueTime: String? = null,      // "HH:mm"

    // ── Reminder capability ──
    val hasReminder: Boolean = false,
    val reminderAt: Long? = null,     // epoch millis trigger
    val repeatType: Int = 0,          // 0=Once,1=Daily,2=Weekly,3=Monthly,4=MonthlyLunar,5=Yearly
    val useLunar: Boolean = false,
    val advanceDays: Int = 0,         // nhắc trước N ngày (0=đúng ngày)
    val reminderEnabled: Boolean = true,

    // ── Note / visual ──
    val isPinned: Boolean = false,
    val colorIndex: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val label: String = "",              // optional user label
    val note: String = "",               // optional note content
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: String = "system",       // daily, holiday, ai, reminder, system, good_day
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "countdown_events", indices = [Index(value = ["targetEpochDay"])])
data class CountdownEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetEpochDay: Long,
    @ColumnInfo(defaultValue = "") val note: String = "",
    @ColumnInfo(defaultValue = "1") val showOnHome: Boolean = true,
    @ColumnInfo(defaultValue = "1") val showOnWidget: Boolean = true,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = System.currentTimeMillis()
)

// ── World Clock ──────────────────────────────────────────────────────────────
@Entity(tableName = "world_clock_cities")
data class WorldClockCityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val timezone: String,
    @ColumnInfo(defaultValue = "") val country: String = "",
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
)

// ── Cycle Tracker ────────────────────────────────────────────────────────────
@Entity(tableName = "cycle_settings")
data class CycleSettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(defaultValue = "28") val cycleLength: Int = 28,
    @ColumnInfo(defaultValue = "5") val periodLength: Int = 5,
)

@Entity(tableName = "cycle_logs", indices = [Index(value = ["startEpochDay"])])
data class CycleLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startEpochDay: Long,
    @ColumnInfo(defaultValue = "-1") val endEpochDay: Long = -1L,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "0") val createdAt: Long = System.currentTimeMillis(),
)
