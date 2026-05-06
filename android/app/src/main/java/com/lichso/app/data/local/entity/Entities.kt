package com.lichso.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,       // epoch millis
    val dueTime: String? = null,     // "HH:mm"
    val priority: Int = 1,           // 0=Low, 1=Medium, 2=High
    val isDone: Boolean = false,
    val labels: String = "",         // comma-separated labels
    val hasReminder: Boolean = false, // whether to create a reminder for this task
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val colorIndex: Int = 0,         // index into predefined colors
    val isPinned: Boolean = false,   // pinned to top
    val labels: String = "",         // comma-separated labels
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val triggerTime: Long,           // epoch millis
    val repeatType: Int = 0,         // 0=Once, 1=Daily, 2=Weekly, 3=Monthly, 4=MonthlyLunar, 5=Yearly
    val isEnabled: Boolean = true,
    val useLunar: Boolean = false,   // whether this is a lunar calendar reminder
    val advanceDays: Int = 0,        // remind N days before (0=same day, 1, 3, 7)
    val category: Int = 0,           // 0=Holiday, 1=Birthday, 2=Lunar, 3=Personal, 4=Memorial
    val labels: String = "",         // comma-separated labels
    val createdAt: Long = System.currentTimeMillis()
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
