package com.lichso.app.feature.quiz

import com.lichso.app.data.remote.LeaderboardEntry
import com.lichso.app.data.remote.LeaderboardResponse
import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.data.remote.MyRankResponse
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.QuizSession
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val api: LichSoApi,
) {

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

        return api.startQuizSession(token, sessionType, category).map { response ->
            Pair(response.session, response.questions)
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

    suspend fun getMyRank(
        token: String,
        period: String,
    ): Result<MyRankResponse> =
        api.getMyRank(token, period)
}
