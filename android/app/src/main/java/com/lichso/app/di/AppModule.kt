package com.lichso.app.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.Room
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.data.remote.ScreenBackgroundRepository
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
import com.lichso.app.data.auth.TokenManager
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        tokenManager: TokenManager
    ): OkHttpClient {
        val deviceName = listOfNotNull(Build.MANUFACTURER, Build.MODEL)
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val userAgent = "LichSo-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; $deviceName)"
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
                .header("User-Agent", userAgent)
                .header("X-Client-Platform", "android")
                .header("X-App-Version", BuildConfig.VERSION_NAME)
                .header("X-Device-Name", deviceName)
                .header("X-OS-Version", Build.VERSION.RELEASE ?: "")

            // Read from in-memory cache — no runBlocking, no ANR risk
            val deviceId = tokenManager.cachedDeviceId
            if (deviceId.isNotEmpty()) {
                builder.header("X-Device-ID", deviceId)
            }

            // Read from in-memory cache — always up-to-date via TokenManager.saveTokens()
            val token = tokenManager.cachedAccessToken
            if (!token.isNullOrEmpty() &&
                request.header("Authorization") == null &&
                !request.isPublicContentRequest()
            ) {
                builder.header("Authorization", "Bearer $token")
            }

            chain.proceed(builder.build())
        }

        val offlineCacheInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (request.isPublicContentRequest() && !context.hasNetworkConnection()) {
                request = request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(7, TimeUnit.DAYS)
                            .build()
                    )
                    .build()
            }
            chain.proceed(request)
        }

        val cacheInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val request = chain.request()
            val maxAgeSeconds = request.publicContentMaxAgeSeconds()
            if (maxAgeSeconds != null && request.header("Authorization") == null) {
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAgeSeconds")
                    .build()
            } else {
                response
            }
        }

        val cache = Cache(File(context.cacheDir, "http_cache"), 100L * 1024L * 1024L)

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineCacheInterceptor)
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
    fun provideItemDao(db: LichSoDatabase): ItemDao = db.itemDao()

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

    @Provides
    @Singleton
    fun provideScreenBackgroundRepository(
        @ApplicationContext context: Context,
        api: LichSoApi,
    ): ScreenBackgroundRepository = ScreenBackgroundRepository(context, api)
}

private fun okhttp3.Request.isPublicContentRequest(): Boolean =
    method == "GET" && publicContentMaxAgeSeconds() != null

private fun okhttp3.Request.publicContentMaxAgeSeconds(): Int? {
    val path = url.encodedPath
    if (!url.host.endsWith("lichso.vn")) return null
    return when {
        path.startsWith("/api/uploads/") -> 30 * 24 * 60 * 60
        path == "/api/articles" || path == "/api/articles/" -> 10 * 60
        path.startsWith("/api/articles/slug/") -> 60 * 60
        path.matches(Regex("^/api/articles/[^/]+$")) -> 60 * 60
        path == "/api/categories" || path == "/api/categories/" -> 10 * 60
        path.startsWith("/api/categories/slug/") -> 30 * 60
        path.matches(Regex("^/api/categories/[^/]+$")) -> 30 * 60
        path.startsWith("/api/day-content/") -> 10 * 60
        path.startsWith("/api/events/") -> 30 * 60
        path.startsWith("/api/famous-people/") -> 30 * 60
        path.startsWith("/api/festivals/") -> 30 * 60
        path.startsWith("/api/quotes/") -> 10 * 60
        path.startsWith("/api/banners") -> 5 * 60
        path.startsWith("/api/popups") -> 5 * 60
        path.startsWith("/api/screen-backgrounds") -> null
        else -> null
    }
}

private fun Context.hasNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
