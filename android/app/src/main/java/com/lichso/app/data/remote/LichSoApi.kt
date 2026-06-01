package com.lichso.app.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val BASE_URL = "https://api.lichso.vn/api"

// ── Generic API response wrapper ──

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
)

class LichSoApiException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)

// ── Content data classes ──

data class ContentEvent(
    val id: Long,
    val title: String,
    val description: String?,
    @SerializedName("event_day") val eventDay: Int,
    @SerializedName("event_month") val eventMonth: Int,
    @SerializedName("event_year") val eventYear: Int?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("short_description") val shortDescription: String?,
)

data class FamousPerson(
    val id: String,
    val name: String,
    val biography: String?,
    @SerializedName("birth_year") val birthYear: Int?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val description: String?,
)

data class Article(
    val id: String,
    val title: String,
    val slug: String,
    val excerpt: String?,
    val content: String?,
    @SerializedName("featured_image") val featuredImage: String?,
    @SerializedName("published_at") val publishedAt: String?,
    val category: ArticleCategory?,
    @SerializedName("reading_time") val readingTime: Int? = null,
)

data class ArticleCategory(
    val id: String,
    val name: String,
    val slug: String,
)

data class Festival(
    val id: String,
    val name: String,
    @SerializedName("lunar_month") val lunarMonth: Int?,
    @SerializedName("lunar_day") val lunarDay: Int?,
    val description: String?,
    val location: String?,
    @SerializedName("image_url") val imageUrl: String?,
)

data class Quote(
    val id: String,
    val quote: String,
    val author: String?,
)

data class Banner(
    val id: String,
    val title: String,
    val subtitle: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("icon_key") val iconKey: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("cta_type") val ctaType: String?,
    @SerializedName("cta_route") val ctaRoute: String?,
    @SerializedName("bg_color") val bgColor: String?,
    val locations: List<String>?,
    @SerializedName("is_active") val active: Boolean = true,
    @SerializedName("sort_order") val sortOrder: Int = 0,
)

data class Popup(
    val id: String,
    val title: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("cta_type") val ctaType: String?,
    @SerializedName("cta_route") val ctaRoute: String?,
    @SerializedName("is_active") val active: Boolean = true,
    val position: String?,
)

// Screen background set by admin for each app screen
data class ScreenBackground(
    @SerializedName("screen_key") val screenKey: String,
    @SerializedName("screen_name") val screenName: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean = true,
)


data class DayContentResponse(val date: String, val data: DayContentData?)
data class DayContentData(
    val quote: Quote?,
    val events: List<ContentEvent>?,
    @SerializedName("famous_people") val famousPeople: List<FamousPerson>?,
    val festivals: List<Festival>?,
    val articles: List<Article>?,
)

// ── Quiz data classes ──

data class QuizQuestion(
    val id: Long,
    val content: String,
    @SerializedName("option_a") val optionA: String,
    @SerializedName("option_b") val optionB: String,
    @SerializedName("option_c") val optionC: String,
    @SerializedName("option_d") val optionD: String,
    val category: String,
    val difficulty: String,
    @SerializedName("article_id") val articleId: Long? = null,
    val correct: String? = null,
    @SerializedName("correct_answer") val correctAnswer: String? = null,
    val hint: String? = null,
    val explanation: String? = null,
)

data class SubmitAnswerResult(
    @SerializedName("question_id") val questionId: Long,
    val chosen: String,
    val correct: String,
    @SerializedName("is_correct") val isCorrect: Boolean,
    val explanation: String?,
    @SerializedName("article_id") val articleId: Long?,
    @SerializedName("points_earned") val pointsEarned: Int,
    @SerializedName("time_ms") val timeMs: Int = 0,
)

data class SessionResult(
    @SerializedName("session_id") val sessionId: String,
    val score: Int,
    @SerializedName("score_v2") val scoreV2: Int = 0,
    val total: Int,
    @SerializedName("points_earned") val pointsEarned: Int,
    @SerializedName("bonus_points") val bonusPoints: Int,
    @SerializedName("new_week_score") val newWeekScore: Int,
    val rank: Int,
    @SerializedName("app_points_earned") val appPointsEarned: Int = 0,
    @SerializedName("xp_earned") val xpEarned: Int = 0,
    @SerializedName("session_title") val sessionTitle: String = "",
    @SerializedName("unlocked_mastery_titles") val unlockedMasteryTitles: List<String> = emptyList(),
    @SerializedName("unlocked_badges") val unlockedBadges: List<String> = emptyList(),
)

data class PointWallet(
    @SerializedName("user_id") val userId: String,
    val balance: Int,
    @SerializedName("lifetime_earned") val lifetimeEarned: Int,
    @SerializedName("lifetime_spent") val lifetimeSpent: Int,
    @SerializedName("updated_at") val updatedAt: String,
)

data class SyncGuestPointsResponse(
    @SerializedName("synced_count") val syncedCount: Int,
    val wallet: PointWallet?,
)

data class LeaderboardEntry(
    val rank: Int = 0,  // not in JSON; populated from list index in repository
    @SerializedName("user_id") val userId: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("week_score") val weekScore: Int,
    @SerializedName("month_score") val monthScore: Int,
    @SerializedName("total_score") val totalScore: Int,
)

data class LeaderboardResponse(val entries: List<LeaderboardEntry>)

// ── Auth data classes ──

data class AuthUser(
    val id: String,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("full_name") val fullName: String,
    val avatar: String?,
    val roles: List<String>,
    val permissions: List<String> = emptyList(),
)

// GET /auth/me wraps user in {user: {...}}
private data class GetMeData(val user: AuthUser)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val user: AuthUser,
)

data class MyRankResponse(

    val rank: Int,
    @SerializedName("week_score") val weekScore: Int,
    @SerializedName("month_score") val monthScore: Int,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("cur_streak") val curStreak: Int,
)

data class QuizSession(
    val id: String,
    @SerializedName("session_type") val sessionType: String,
    val category: String?,
    @SerializedName("question_ids") val questionIds: List<Long>,
    val questions: List<QuizQuestion>?,
)

data class StartSessionResponse(
    val session: QuizSession,
    val questions: List<QuizQuestion>,
)

data class OfflineQuizAnswerPayload(
    @SerializedName("question_id") val questionId: Long,
    val chosen: String,
    @SerializedName("time_ms") val timeMs: Int,
)

data class OfflineQuizSessionPayload(
    @SerializedName("client_session_id") val clientSessionId: String,
    @SerializedName("session_type") val sessionType: String,
    val category: String?,
    @SerializedName("question_ids") val questionIds: List<Long>,
    val answers: List<OfflineQuizAnswerPayload>,
    @SerializedName("started_at_ms") val startedAtMs: Long,
    @SerializedName("finished_at_ms") val finishedAtMs: Long,
)

data class SyncOfflineQuizResponse(
    @SerializedName("synced_session_ids") val syncedSessionIds: List<String>,
    @SerializedName("skipped_session_ids") val skippedSessionIds: List<String>,
)

// ── API Client ──

@Singleton
class LichSoApi @Inject constructor(
    private val client: OkHttpClient,
) {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // ── Helpers ──

    private fun get(path: String, token: String? = null): Request {
        val builder = Request.Builder().url("$BASE_URL$path").get()
        if (token != null) builder.header("Authorization", "Bearer $token")
        return builder.build()
    }

    private fun post(path: String, body: Map<String, Any?>, token: String? = null): Request {
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody(jsonMediaType)
        val builder = Request.Builder().url("$BASE_URL$path").post(requestBody)
        if (token != null) builder.header("Authorization", "Bearer $token")
        return builder.build()
    }

    private fun postJson(path: String, body: Any, token: String? = null): Request {
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody(jsonMediaType)
        val builder = Request.Builder().url("$BASE_URL$path").post(requestBody)
        if (token != null) builder.header("Authorization", "Bearer $token")
        return builder.build()
    }

    private fun delete(path: String, token: String? = null): Request {
        val builder = Request.Builder().url("$BASE_URL$path").delete()
        if (token != null) builder.header("Authorization", "Bearer $token")
        return builder.build()
    }

    private inline fun <reified T> execute(request: Request): Result<T> {
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful) {
                    val message = parseErrorMessage(body) ?: "HTTP ${response.code}"
                    return Result.failure(LichSoApiException(response.code, message))
                }
                if (body.isNullOrBlank()) {
                    return Result.failure(LichSoApiException(response.code, "Máy chủ không trả về dữ liệu"))
                }
                val type = object : TypeToken<ApiResponse<T>>() {}.type
                val wrapper: ApiResponse<T> = gson.fromJson(body, type)
                if (!wrapper.success || wrapper.data == null) {
                    return Result.failure(LichSoApiException(response.code, wrapper.message ?: "API error"))
                }
                Result.success(wrapper.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Variant for endpoints that return success with no data body (e.g. register token).
    private fun executeVoid(request: Request): Result<Unit> {
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string()
                    val message = parseErrorMessage(body) ?: "HTTP ${response.code}"
                    return Result.failure(LichSoApiException(response.code, message))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return runCatching {
            val json = com.google.gson.JsonParser.parseString(body).asJsonObject
            json.get("message")?.asString
                ?: json.getAsJsonObject("error")?.get("detail")?.asString
                ?: json.getAsJsonObject("error")?.get("message")?.asString
        }.getOrNull()
    }

    // ── Auth endpoints ──

    suspend fun loginWithGoogle(idToken: String, deviceId: String = ""): Result<LoginResponse> = withContext(Dispatchers.IO) {
        execute(post("/auth/google", mapOf("id_token" to idToken, "device_id" to deviceId)))
    }

    /** Creates or resumes an anonymous guest session keyed by device id. */
    suspend fun guestLogin(
        deviceId: String,
        deviceName: String = "",
        displayName: String = "",
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        execute(post("/auth/guest", mapOf(
            "device_id" to deviceId,
            "device_name" to deviceName,
            "display_name" to displayName,
        )))
    }

    /** Updates the current user's name (works for guest and Google sessions). */
    suspend fun updateProfile(
        token: String,
        firstName: String,
        lastName: String = "",
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        val json = gson.toJson(mapOf("first_name" to firstName, "last_name" to lastName))
        val request = Request.Builder()
            .url("$BASE_URL/auth/me")
            .put(json.toRequestBody(jsonMediaType))
            .header("Authorization", "Bearer $token")
            .build()
        execute<GetMeData>(request).map { it.user }
    }

    suspend fun refreshBackendToken(refreshToken: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        execute(post("/auth/refresh", mapOf("refresh_token" to refreshToken)))
    }

    suspend fun getMe(token: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        execute<GetMeData>(get("/auth/me", token)).map { it.user }
    }

    suspend fun logout(token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.newCall(post("/auth/logout", emptyMap(), token)).execute().use { it.isSuccessful }
        } catch (_: Exception) { false }
    }

    suspend fun deleteAccount(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeVoid(delete("/auth/me", token))
    }

    // ── Content endpoints ──

    suspend fun getBanners(location: String? = null): Result<List<Banner>> = withContext(Dispatchers.IO) {
        val path = buildString {
            append("/banners")
            if (location != null) append("?location=${location.encodeUrl()}")
        }
        execute(get(path))
    }

    suspend fun getPopups(): Result<List<Popup>> = withContext(Dispatchers.IO) {
        execute(get("/popups"))
    }

    suspend fun getScreenBackgrounds(): Result<List<ScreenBackground>> = withContext(Dispatchers.IO) {
        // Bypass disk cache: admin changes must be visible on next app launch
        execute(Request.Builder()
            .url("$BASE_URL/screen-backgrounds")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()
        )
    }


    suspend fun getTodayContent(): Result<DayContentResponse> = withContext(Dispatchers.IO) {
        execute(get("/day-content/today"))
    }

    suspend fun getEventsByDate(month: Int, day: Int): Result<List<ContentEvent>> =
        withContext(Dispatchers.IO) {
            execute(get("/events/date/$month/$day"))
        }

    suspend fun getFamousPeopleByBirthday(month: Int, day: Int): Result<List<FamousPerson>> =
        withContext(Dispatchers.IO) {
            execute(get("/famous-people/birthday/$month/$day"))
        }

    suspend fun getArticles(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
    ): Result<List<Article>> = withContext(Dispatchers.IO) {
        val path = buildString {
            append("/articles/?page=$page&limit=$limit")
            if (category != null) append("&category=${category.encodeUrl()}")
        }
        execute(get(path))
    }

    suspend fun getArticle(id: String): Result<Article> = withContext(Dispatchers.IO) {
        execute(get("/articles/$id"))
    }

    suspend fun getFestivalsByLunarDate(month: Int, day: Int): Result<List<Festival>> =
        withContext(Dispatchers.IO) {
            execute(get("/festivals/lunar/$month/$day"))
        }

    suspend fun getTodayQuote(): Result<Quote> = withContext(Dispatchers.IO) {
        execute(get("/quotes/today"))
    }

    // ── Quiz public endpoints ──

    suspend fun getDailyQuizQuestions(date: String): Result<List<QuizQuestion>> =
        withContext(Dispatchers.IO) {
            execute(get("/quiz/questions/daily?date=$date"))
        }

    suspend fun getQuizQuestions(
        category: String? = null,
        difficulty: String? = null,
        limit: Int = 10,
    ): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        val path = buildString {
            append("/quiz/questions?limit=$limit")
            if (category != null) append("&category=${category.encodeUrl()}")
            if (difficulty != null) append("&difficulty=${difficulty.encodeUrl()}")
        }
        execute(get(path))
    }

    suspend fun getLeaderboard(
        period: String = "weekly",
        limit: Int = 50,
    ): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        // Leaderboard changes immediately after a quiz — always bypass OkHttp disk cache
        execute(Request.Builder()
            .url("$BASE_URL/quiz/leaderboard?period=$period&limit=$limit")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()
        )
    }

    // ── Quiz auth endpoints ──

    suspend fun startQuizSession(
        token: String,
        sessionType: String,
        category: String?,
    ): Result<StartSessionResponse> = withContext(Dispatchers.IO) {
        val body = mapOf("session_type" to sessionType, "category" to category)
        execute(post("/quiz/sessions", body, token))
    }

    suspend fun submitAnswer(
        token: String,
        sessionId: String,
        questionId: Long,
        chosen: String,
        timeMs: Int,
    ): Result<SubmitAnswerResult> = withContext(Dispatchers.IO) {
        val body = mapOf(
            "question_id" to questionId,
            "chosen" to chosen,
            "time_ms" to timeMs,
        )
        execute(post("/quiz/sessions/$sessionId/submit", body, token))
    }

    suspend fun finishQuizSession(
        token: String,
        sessionId: String,
    ): Result<SessionResult> = withContext(Dispatchers.IO) {
        execute(post("/quiz/sessions/$sessionId/finish", emptyMap(), token))
    }

    suspend fun syncOfflineQuizSessions(
        token: String,
        sessions: List<OfflineQuizSessionPayload>,
    ): Result<SyncOfflineQuizResponse> = withContext(Dispatchers.IO) {
        execute(postJson("/quiz/sessions/sync", mapOf("sessions" to sessions), token))
    }

    suspend fun getMyRank(
        token: String,
        period: String = "weekly",
    ): Result<MyRankResponse> = withContext(Dispatchers.IO) {
        execute(Request.Builder()
            .url("$BASE_URL/quiz/leaderboard/me?period=$period")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .header("Authorization", "Bearer $token")
            .build()
        )
    }

    suspend fun getPointWallet(token: String): Result<PointWallet> = withContext(Dispatchers.IO) {
        execute(get("/points/wallet", token))
    }

    suspend fun spendPoints(
        token: String,
        amount: Int,
        source: String,
        sourceId: String?,
        idempotencyKey: String?,
        metadata: Map<String, Any?> = emptyMap(),
    ): Result<PointWallet> = withContext(Dispatchers.IO) {
        val body = mapOf(
            "amount" to amount,
            "source" to source,
            "source_id" to sourceId,
            "idempotency_key" to idempotencyKey,
            "metadata" to metadata,
        )
        execute(post("/points/spend", body, token))
    }

    suspend fun syncGuestPoints(
        token: String,
        transactions: List<Map<String, Any?>>,
    ): Result<SyncGuestPointsResponse> = withContext(Dispatchers.IO) {
        execute(postJson("/points/sync-guest", mapOf("transactions" to transactions), token))
    }

    // ── Push notification endpoints ──

    suspend fun registerDeviceToken(
        fcmToken: String,
        appVersion: String,
        deviceId: String,
        deviceName: String = "",
        authToken: String? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        executeVoid(post(
            "/push/register",
            mapOf(
                "token" to fcmToken,
                "platform" to "android",
                "app_version" to appVersion,
                "device_id" to deviceId,
                "device_name" to deviceName,
            ),
            authToken,
        ))
    }

    suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeVoid(
            Request.Builder()
                .url("$BASE_URL/push/register?token=${fcmToken.encodeUrl()}")
                .delete()
                .build()
        )
    }

    // ── Helpers ──

    private fun String.encodeUrl(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}
