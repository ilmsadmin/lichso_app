package com.lichso.app.di

import android.content.Context
import androidx.room.Room
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.data.settings.AppSettingsRepository
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.feature.points.domain.Clock
import com.lichso.app.feature.points.domain.SystemClock
import com.lichso.app.feature.quiz.QuizRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.os.Build
import com.lichso.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val deviceName = listOfNotNull(Build.MANUFACTURER, Build.MODEL)
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val userAgent = "LichSo-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; $deviceName)"
        val userAgentInterceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", userAgent)
                    .header("X-Client-Platform", "android")
                    .header("X-App-Version", BuildConfig.VERSION_NAME)
                    .header("X-Device-Name", deviceName)
                    .header("X-OS-Version", Build.VERSION.RELEASE ?: "")
                    .build()
            )
        }
        
        // Cache configuration: 50MB
        val cacheSize = 50 * 1024 * 1024L
        val cache = okhttp3.Cache(context.cacheDir, cacheSize)

        // Interceptor to cache responses even if server has no cache headers (e.g. 7 days for articles)
        val cacheInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val cacheControl = okhttp3.CacheControl.Builder()
                .maxAge(7, TimeUnit.DAYS)
                .build()
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(userAgentInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

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
    fun provideCountdownEventDao(db: LichSoDatabase): CountdownEventDao = db.countdownEventDao()

    @Provides
    fun provideWorldClockCityDao(db: LichSoDatabase): WorldClockCityDao = db.worldClockCityDao()

    @Provides
    fun provideCycleDao(db: LichSoDatabase): CycleDao = db.cycleDao()

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
    fun provideQuizOfflineSessionDao(db: LichSoDatabase): QuizOfflineSessionDao = db.quizOfflineSessionDao()

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

    // ── v3 LichSo API + Quiz ──
    @Provides
    @Singleton
    fun provideLichSoApi(client: OkHttpClient): LichSoApi = LichSoApi(client)

    @Provides
    @Singleton
    fun provideQuizRepository(api: LichSoApi, quizOfflineSessionDao: QuizOfflineSessionDao): QuizRepository =
        QuizRepository(api, quizOfflineSessionDao)

    @Provides
    @Singleton
    fun provideContentRepository(api: LichSoApi): com.lichso.app.feature.content.ContentRepository =
        com.lichso.app.feature.content.ContentRepository(api)
}
