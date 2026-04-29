package com.lichso.app.di

import android.content.Context
import androidx.room.Room
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.settings.AppSettingsRepository
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.feature.points.domain.Clock
import com.lichso.app.feature.points.domain.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // Security: cleartext traffic is blocked via network_security_config.xml
        // Certificate pinning is NOT used here because:
        //   1. This client is shared between OpenRouter, Open-Meteo, and wttr.in
        //   2. Cloud services (Cloudflare CDN) rotate certificates frequently
        //   3. A stale pin would brick the app for ALL users until an update is released
        //   4. The proper fix is proxying API calls through our own backend
        .build()

    @Provides
    @Singleton
    fun provideDayInfoProvider(): DayInfoProvider = DayInfoProvider()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LichSoDatabase =
        LichSoDatabase.getInstance(context)

    @Provides
    fun provideTaskDao(db: LichSoDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideNoteDao(db: LichSoDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideReminderDao(db: LichSoDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideChatMessageDao(db: LichSoDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides
    fun provideBookmarkDao(db: LichSoDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideNotificationDao(db: LichSoDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideFamilyMemberDao(db: LichSoDatabase): FamilyMemberDao = db.familyMemberDao()

    @Provides
    fun provideMemorialDayDao(db: LichSoDatabase): MemorialDayDao = db.memorialDayDao()

    @Provides
    fun provideMemorialChecklistDao(db: LichSoDatabase): MemorialChecklistDao = db.memorialChecklistDao()

    @Provides
    fun provideFamilySettingsDao(db: LichSoDatabase): FamilySettingsDao = db.familySettingsDao()

    @Provides
    fun provideMemberPhotoDao(db: LichSoDatabase): MemberPhotoDao = db.memberPhotoDao()

    // ── v2 PointsEngine ──
    @Provides
    fun providePointsDao(db: LichSoDatabase): PointsDao = db.pointsDao()

    @Provides
    fun provideUnlockDao(db: LichSoDatabase): UnlockDao = db.unlockDao()

    @Provides
    fun provideStreakDao(db: LichSoDatabase): StreakDao = db.streakDao()

    @Provides
    @Singleton
    fun providePointsClock(): Clock = SystemClock()

    @Provides
    @Singleton
    fun provideFamilyTreeRepository(
        memberDao: FamilyMemberDao,
        memorialDao: MemorialDayDao,
        checklistDao: MemorialChecklistDao,
        settingsDao: FamilySettingsDao,
        photoDao: MemberPhotoDao,
    ): FamilyTreeRepository = FamilyTreeRepository(memberDao, memorialDao, checklistDao, settingsDao, photoDao)

    @Provides
    @Singleton
    fun provideAppSettingsRepository(@ApplicationContext context: Context): AppSettingsRepository =
        AppSettingsRepository(context)
}
