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
        ChatMessageEntity::class,
        BookmarkEntity::class,
        NotificationEntity::class,
        FamilyMemberEntity::class,
        MemorialDayEntity::class,
        MemorialChecklistEntity::class,
        FamilySettingsEntity::class,
        MemberPhotoEntity::class,
    ],
    version = 9,
    exportSchema = false
)
abstract class LichSoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun notificationDao(): NotificationDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun memorialDayDao(): MemorialDayDao
    abstract fun memorialChecklistDao(): MemorialChecklistDao
    abstract fun familySettingsDao(): FamilySettingsDao
    abstract fun memberPhotoDao(): MemberPhotoDao

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
