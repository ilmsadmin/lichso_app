package com.lichso.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.*

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        ReminderEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LichSoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun chatMessageDao(): ChatMessageDao
}
