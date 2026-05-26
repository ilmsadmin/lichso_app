package com.lichso.app.feature.quiz

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lichso.app.data.local.dao.QuizOfflineSessionDao
import com.lichso.app.data.local.entity.QuizOfflineSessionEntity
import com.lichso.app.data.remote.LeaderboardEntry
import com.lichso.app.data.remote.LeaderboardResponse
import com.lichso.app.data.remote.OfflineQuizAnswerPayload
import com.lichso.app.data.remote.OfflineQuizSessionPayload
import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.data.remote.MyRankResponse
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.QuizSession
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import com.lichso.app.data.remote.PointWallet
import com.lichso.app.data.remote.SyncGuestPointsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val api: LichSoApi,
    private val offlineSessionDao: QuizOfflineSessionDao,
) {
    private val gson = Gson()

    fun getDailyQuestions(date: String): Flow<List<QuizQuestion>> = flow {
        fetchDailyQuestions(date).getOrNull()?.let { emit(it) }
    }

    fun getQuestions(
        category: String?,
        difficulty: String?,
        limit: Int,
    ): Flow<List<QuizQuestion>> = flow {
        fetchQuestions(category, difficulty, limit).getOrNull()?.let { emit(it) }
    }

    suspend fun fetchDailyQuestions(date: String): Result<List<QuizQuestion>> =
        api.getDailyQuizQuestions(date)

    suspend fun fetchQuestions(
        category: String?,
        difficulty: String? = null,
        limit: Int = 10,
    ): Result<List<QuizQuestion>> =
        api.getQuizQuestions(category, difficulty, limit)

    fun getLeaderboard(period: String): Flow<LeaderboardResponse> = flow {
        api.getLeaderboard(period).getOrNull()?.let { entries ->
            emit(LeaderboardResponse(entries = entries.mapIndexed { i, e -> e.copy(rank = i + 1) }))
        }
    }

    /**
     * Start a quiz session.
     * If no token (guest mode): fetch public questions and return without a server session.
     */
    suspend fun startSession(
        token: String?,
        sessionType: String,
        category: String?,
    ): Result<Pair<QuizSession, List<QuizQuestion>>> {
        if (token.isNullOrBlank()) {
            // Guest mode — fetch questions without creating a server session
            val date = LocalDate.now().toString()
            val questionsResult = if (sessionType == "daily") {
                fetchDailyQuestions(date)
            } else {
                fetchQuestions(category = category, limit = 10)
            }
            return questionsResult.map { questions ->
                val guestSession = QuizSession(
                    id = "",
                    sessionType = sessionType,
                    category = category,
                    questionIds = questions.map { it.id },
                    questions = questions,
                )
                Pair(guestSession, questions)
            }
        }

        val serverResult = api.startQuizSession(token, sessionType, category)
        if (serverResult.isSuccess) {
            return serverResult.map { response ->
                Pair(response.session, response.questions)
            }
        }

        // Any server error → fall back to guest mode so the quiz always works.
        val date = LocalDate.now().toString()
        val questionsResult = if (sessionType == "daily") {
            fetchDailyQuestions(date)
        } else {
            fetchQuestions(category = category, limit = 10)
        }
        return questionsResult.map { questions ->
            val guestSession = QuizSession(
                id = "",
                sessionType = sessionType,
                category = category,
                questionIds = questions.map { it.id },
                questions = questions,
            )
            Pair(guestSession, questions)
        }
    }

    suspend fun submitAnswer(
        token: String,
        sessionId: String,
        questionId: Long,
        chosen: String,
        timeMs: Int,
    ): Result<SubmitAnswerResult> =
        api.submitAnswer(token, sessionId, questionId, chosen, timeMs)

    suspend fun finishSession(
        token: String,
        sessionId: String,
    ): Result<SessionResult> =
        api.finishQuizSession(token, sessionId)

    suspend fun saveGuestSession(
        sessionType: String,
        category: String?,
        questionIds: List<Long>,
        answers: List<SubmitAnswerResult>,
        startedAtMs: Long,
        finishedAtMs: Long,
    ) {
        val clientSessionId = java.util.UUID.randomUUID().toString()
        val entity = QuizOfflineSessionEntity(
            clientSessionId = clientSessionId,
            sessionType = sessionType,
            category = category,
            questionIdsJson = gson.toJson(questionIds),
            answersJson = gson.toJson(answers.map {
                OfflineQuizAnswerPayload(
                    questionId = it.questionId,
                    chosen = it.chosen,
                    timeMs = it.timeMs,
                )
            }),
            startedAtMs = startedAtMs,
            finishedAtMs = finishedAtMs,
        )
        offlineSessionDao.upsert(entity)
    }

    suspend fun syncPendingGuestSessions(token: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pending = offlineSessionDao.getAllOnce()
            if (pending.isEmpty()) {
                return@withContext Result.success(0)
            }

            val questionIdsType = object : TypeToken<List<Long>>() {}.type
            val answersType = object : TypeToken<List<OfflineQuizAnswerPayload>>() {}.type

            val payload = pending.map { entity ->
                OfflineQuizSessionPayload(
                    clientSessionId = entity.clientSessionId,
                    sessionType = entity.sessionType,
                    category = entity.category,
                    questionIds = gson.fromJson(entity.questionIdsJson, questionIdsType),
                    answers = gson.fromJson(entity.answersJson, answersType),
                    startedAtMs = entity.startedAtMs,
                    finishedAtMs = entity.finishedAtMs,
                )
            }

            val responseResult = api.syncOfflineQuizSessions(token, payload)
            val response = responseResult.fold(
                onSuccess = { it },
                onFailure = { return@withContext Result.failure(it) },
            )
            val syncedIds = response.syncedSessionIds
            if (syncedIds.isNotEmpty()) {
                offlineSessionDao.deleteByClientSessionIds(syncedIds)
            }
            Result.success(syncedIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyRank(
        token: String,
        period: String,
    ): Result<MyRankResponse> =
        api.getMyRank(token, period)

    suspend fun getPointWallet(token: String): Result<PointWallet> =
        api.getPointWallet(token)

    suspend fun spendPoints(
        token: String,
        amount: Int,
        source: String,
        sourceId: String?,
        idempotencyKey: String?,
        metadata: Map<String, Any?> = emptyMap(),
    ): Result<PointWallet> =
        api.spendPoints(token, amount, source, sourceId, idempotencyKey, metadata)

    suspend fun syncGuestPoints(
        token: String,
        transactions: List<Map<String, Any?>>,
    ): Result<SyncGuestPointsResponse> =
        api.syncGuestPoints(token, transactions)
}
