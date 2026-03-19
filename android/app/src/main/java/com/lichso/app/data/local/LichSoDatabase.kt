package com.lichso.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        @Volatile private var INSTANCE: LichSoDatabase? = null

        fun getInstance(context: Context): LichSoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LichSoDatabase::class.java,
                    "lichso.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
