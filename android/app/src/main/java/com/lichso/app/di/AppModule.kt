package com.lichso.app.di

import android.content.Context
import androidx.room.Room
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.domain.DayInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDayInfoProvider(): DayInfoProvider = DayInfoProvider()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LichSoDatabase =
        Room.databaseBuilder(context, LichSoDatabase::class.java, "lichso.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTaskDao(db: LichSoDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideNoteDao(db: LichSoDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideReminderDao(db: LichSoDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideChatMessageDao(db: LichSoDatabase): ChatMessageDao = db.chatMessageDao()
}
