package com.lichso.app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lichso.app.data.auth.AuthRepository
import com.lichso.app.data.remote.LeaderboardResponse
import com.lichso.app.data.remote.MyRankResponse
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import com.lichso.app.feature.points.data.PointsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

// ── Quiz state machine ──

sealed interface QuizState {
    object Idle : QuizState
    object Loading : QuizState
    data class Question(
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val sessionId: String?,
        val answers: List<SubmitAnswerResult?>,
        val timeRemaining: Int,
        val lastResult: SubmitAnswerResult?,
        val showingResult: Boolean,
        val hiddenOptions: Map<Long, Set<String>>,
        val hints: Map<Long, String>,
        val usedAssists: Map<Long, Set<QuizAssistType>>,
        val dailyPoints: Int,
        val assistMessage: String?,
        val isMilestonePaused: Boolean = false,
        val reviewMode: Boolean = false,
        val reviewCorrectAnswers: Map<Long, String> = emptyMap(),
    ) : QuizState
    data class Finished(
        val result: SessionResult?,
        val answers: List<SubmitAnswerResult?>,
        val questions: List<QuizQuestion> = emptyList()
    ) : QuizState
    data class Error(val message: String) : QuizState
}

data class QuizHomeUiState(
    val dailyQuestionCount: Int? = null,
    val categoryQuestionCounts: Map<String, Int> = emptyMap(),
)

enum class QuizAssistType(val cost: Int, val label: String) {
    FiftyFifty(12, "50/50"),
    Hint(8, "Gợi ý"),
    ExtraTime(10, "+15 giây"),
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: QuizRepository,
    private val authRepository: AuthRepository,
    private val pointsRepository: PointsRepository,
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var autoAdvanceJob: Job? = null

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _leaderboard = MutableStateFlow<LeaderboardResponse?>(null)
    val leaderboard: StateFlow<LeaderboardResponse?> = _leaderboard.asStateFlow()

    private val _myRank = MutableStateFlow<MyRankResponse?>(null)
    val myRank: StateFlow<MyRankResponse?> = _myRank.asStateFlow()

    private val _homeState = MutableStateFlow(QuizHomeUiState())
    val homeState: StateFlow<QuizHomeUiState> = _homeState.asStateFlow()

    private val _isGuest = MutableStateFlow(true)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    // JWT token — will be null for guest users
    private var authToken: String? = null
    private var currentSessionType: String = "daily"
    private var currentSessionCategory: String? = null
    private var currentSessionStartedAtMs: Long = 0L
    private var currentLeaderboardPeriod: String = "weekly"

    private var timerJob: Job? = null
    private var leaderboardJob: Job? = null

    private val QUESTION_TIME_SECONDS = 30

    init {
        viewModelScope.launch {
            pointsRepository.observeLedger().collectLatest { ledger ->
                val daily = ledger?.dailyPoints ?: 0
                val state = _quizState.value
                if (state is QuizState.Question && state.dailyPoints != daily) {
                    _quizState.value = state.copy(dailyPoints = daily)
                }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _isGuest.value = user == null
                if (user != null) {
                    syncPendingGuestSessions()
                }
            }
        }
    }

    fun loadHomeOverview(categoryIds: List<String>) {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val dailyCount = repository.fetchDailyQuestions(date).getOrNull()?.size
            val categoryCounts = categoryIds.associateWith { category ->
                repository.fetchQuestions(category = category, limit = 50).getOrNull()?.size ?: 0
            }
            _homeState.value = QuizHomeUiState(
                dailyQuestionCount = dailyCount,
                categoryQuestionCounts = categoryCounts,
            )
        }
    }

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private suspend fun refreshToken() {
        authToken = try {
            authRepository.refreshBackendTokenIfNeeded()
        } catch (e: Exception) {
            null
        }
    }

    private fun saveActiveSessionState(state: QuizState.Question) {
        sharedPreferences.edit().apply {
            putString("questions", gson.toJson(state.questions))
            putInt("currentIndex", state.currentIndex)
            putString("sessionId", state.sessionId)
            putString("answers", gson.toJson(state.answers))
            putInt("timeRemaining", state.timeRemaining)
            putString("lastResult", gson.toJson(state.lastResult))
            putBoolean("showingResult", state.showingResult)
            putString("hiddenOptions", gson.toJson(state.hiddenOptions))
            putString("hints", gson.toJson(state.hints))
            putString("usedAssists", gson.toJson(state.usedAssists))
            putString("reviewCorrectAnswers", gson.toJson(state.reviewCorrectAnswers))
            putInt("dailyPoints", state.dailyPoints)
            putString("assistMessage", state.assistMessage)
            putBoolean("isMilestonePaused", state.isMilestonePaused)
            putBoolean("reviewMode", state.reviewMode)
            putString("sessionType", currentSessionType)
            putString("category", currentSessionCategory)
            putLong("startedAtMs", currentSessionStartedAtMs)
            putBoolean("hasActiveSession", true)
            apply()
        }
    }

    private fun loadActiveSessionState(sessionType: String, category: String?): QuizState.Question? {
        val hasActiveSession = sharedPreferences.getBoolean("hasActiveSession", false)
        if (!hasActiveSession) return null

        val savedSessionType = sharedPreferences.getString("sessionType", null)
        val savedCategory = sharedPreferences.getString("category", null)
        if (savedSessionType != sessionType || savedCategory != category) {
            return null
        }

        val startedAtMs = sharedPreferences.getLong("startedAtMs", 0L)
        if (sessionType == "daily") {
            try {
                val savedDate = Instant.ofEpochMilli(startedAtMs).atZone(ZoneId.systemDefault()).toLocalDate()
                if (savedDate != LocalDate.now()) {
                    clearActiveSessionState()
                    return null
                }
            } catch (e: Exception) {
                clearActiveSessionState()
                return null
            }
        }

        return try {
            val questionsJson = sharedPreferences.getString("questions", null) ?: return null
            val questionsType = object : TypeToken<List<QuizQuestion>>() {}.type
            val questions: List<QuizQuestion> = gson.fromJson(questionsJson, questionsType)

            val currentIndex = sharedPreferences.getInt("currentIndex", 0)
            val sessionId = sharedPreferences.getString("sessionId", null)

            val answersJson = sharedPreferences.getString("answers", null) ?: return null
            val answersType = object : TypeToken<List<SubmitAnswerResult?>>() {}.type
            val answers: List<SubmitAnswerResult?> = gson.fromJson(answersJson, answersType)

            val timeRemaining = sharedPreferences.getInt("timeRemaining", QUESTION_TIME_SECONDS)

            val lastResultJson = sharedPreferences.getString("lastResult", null)
            val lastResult: SubmitAnswerResult? = if (lastResultJson != null) {
                gson.fromJson(lastResultJson, SubmitAnswerResult::class.java)
            } else null

            val showingResult = sharedPreferences.getBoolean("showingResult", false)

            val hiddenOptionsJson = sharedPreferences.getString("hiddenOptions", null)
            val hiddenOptionsType = object : TypeToken<Map<Long, Set<String>>>() {}.type
            val hiddenOptions: Map<Long, Set<String>> = if (hiddenOptionsJson != null) {
                gson.fromJson(hiddenOptionsJson, hiddenOptionsType)
            } else emptyMap()

            val hintsJson = sharedPreferences.getString("hints", null)
            val hintsType = object : TypeToken<Map<Long, String>>() {}.type
            val hints: Map<Long, String> = if (hintsJson != null) {
                gson.fromJson(hintsJson, hintsType)
            } else emptyMap()

            val usedAssistsJson = sharedPreferences.getString("usedAssists", null)
            val usedAssistsType = object : TypeToken<Map<Long, Set<QuizAssistType>>>() {}.type
            val usedAssists: Map<Long, Set<QuizAssistType>> = if (usedAssistsJson != null) {
                gson.fromJson(usedAssistsJson, usedAssistsType)
            } else emptyMap()

            val reviewCorrectAnswersJson = sharedPreferences.getString("reviewCorrectAnswers", null)
            val reviewCorrectAnswersType = object : TypeToken<Map<Long, String>>() {}.type
            val reviewCorrectAnswers: Map<Long, String> = if (reviewCorrectAnswersJson != null) {
                gson.fromJson(reviewCorrectAnswersJson, reviewCorrectAnswersType)
            } else emptyMap()

            val dailyPoints = sharedPreferences.getInt("dailyPoints", 0)
            val assistMessage = sharedPreferences.getString("assistMessage", null)
            val isMilestonePaused = sharedPreferences.getBoolean("isMilestonePaused", false)
            val reviewMode = sharedPreferences.getBoolean("reviewMode", false)

            currentSessionStartedAtMs = startedAtMs

            QuizState.Question(
                questions = questions,
                currentIndex = currentIndex,
                sessionId = sessionId,
                answers = answers,
                timeRemaining = timeRemaining,
                lastResult = lastResult,
                showingResult = showingResult,
                hiddenOptions = hiddenOptions,
                hints = hints,
                usedAssists = usedAssists,
                dailyPoints = dailyPoints,
                assistMessage = assistMessage,
                isMilestonePaused = isMilestonePaused,
                reviewMode = reviewMode,
                reviewCorrectAnswers = reviewCorrectAnswers,
            )
        } catch (e: Exception) {
            clearActiveSessionState()
            null
        }
    }

    private fun clearActiveSessionState() {
        sharedPreferences.edit().apply {
            remove("questions")
            remove("currentIndex")
            remove("sessionId")
            remove("answers")
            remove("timeRemaining")
            remove("lastResult")
            remove("showingResult")
            remove("hiddenOptions")
            remove("hints")
            remove("usedAssists")
            remove("reviewCorrectAnswers")
            remove("dailyPoints")
            remove("assistMessage")
            remove("isMilestonePaused")
            remove("reviewMode")
            remove("sessionType")
            remove("category")
            remove("startedAtMs")
            putBoolean("hasActiveSession", false)
            apply()
        }
    }

    private suspend fun getQuizPointsBalance(): Int {
        val token = authToken
        if (token != null) {
            val walletResult = repository.getPointWallet(token)
            if (walletResult.isSuccess) {
                return walletResult.getOrThrow().balance
            }
        }
        return currentDailyPoints()
    }

    fun dismissMilestone() {
        val state = _quizState.value as? QuizState.Question ?: return
        if (state.isMilestonePaused) {
            val newState = state.copy(isMilestonePaused = false)
            _quizState.value = newState
            saveActiveSessionState(newState)
            startTimer()
        }
    }

    fun loadWrongQuestionsSession(
        wrongQuestions: List<QuizQuestion>,
        reviewCorrectAnswers: Map<Long, String> = emptyMap(),
    ) {
        currentSessionType = "practice_review"
        currentSessionCategory = null
        _quizState.value = QuizState.Loading
        viewModelScope.launch {
            val points = getQuizPointsBalance()
            val newState = QuizState.Question(
                questions = wrongQuestions,
                currentIndex = 0,
                sessionId = null,
                answers = List(wrongQuestions.size) { null },
                timeRemaining = QUESTION_TIME_SECONDS,
                lastResult = null,
                showingResult = false,
                hiddenOptions = emptyMap(),
                hints = emptyMap(),
                usedAssists = emptyMap(),
                dailyPoints = points,
                assistMessage = null,
                isMilestonePaused = false,
                reviewMode = true,
                reviewCorrectAnswers = reviewCorrectAnswers,
            )
            _quizState.value = newState
            saveActiveSessionState(newState)
            startTimer()
        }
    }

    fun loadDailyQuiz() {
        currentSessionType = "daily"
        currentSessionCategory = null
        val restoredState = loadActiveSessionState("daily", null)
        if (restoredState != null) {
            _quizState.value = restoredState
            if (!restoredState.showingResult && !restoredState.isMilestonePaused) {
                startTimer()
            }
            return
        }
        currentSessionStartedAtMs = System.currentTimeMillis()
        viewModelScope.launch {
            refreshToken()
            _quizState.value = QuizState.Loading
            val dailyPoints = getQuizPointsBalance()
            repository.startSession(
                token = authToken,
                sessionType = "daily",
                category = null,
            ).fold(
                onSuccess = { (session, questions) ->
                    if (questions.isEmpty()) {
                        _quizState.value = QuizState.Error("Không có câu hỏi hôm nay")
                        return@launch
                    }
                    val sessionId = session.id.takeIf { it.isNotBlank() }
                    val limitedQuestions = questions.take(15)
                    val newState = QuizState.Question(
                        questions = limitedQuestions,
                        currentIndex = 0,
                        sessionId = sessionId,
                        answers = List(limitedQuestions.size) { null },
                        timeRemaining = QUESTION_TIME_SECONDS,
                        lastResult = null,
                        showingResult = false,
                        hiddenOptions = emptyMap(),
                        hints = emptyMap(),
                        usedAssists = emptyMap(),
                        dailyPoints = dailyPoints,
                        assistMessage = null,
                        isMilestonePaused = false,
                        reviewMode = false,
                        reviewCorrectAnswers = emptyMap(),
                    )
                    _quizState.value = newState
                    saveActiveSessionState(newState)
                    startTimer()
                },
                onFailure = { _ ->
                    _quizState.value = QuizState.Error("Không thể tải câu hỏi. Vui lòng thử lại.")
                },
            )
        }
    }

    fun loadTopicQuiz(category: String) {
        currentSessionType = "topic"
        currentSessionCategory = category
        val restoredState = loadActiveSessionState("topic", category)
        if (restoredState != null) {
            _quizState.value = restoredState
            if (!restoredState.showingResult && !restoredState.isMilestonePaused) {
                startTimer()
            }
            return
        }
        currentSessionStartedAtMs = System.currentTimeMillis()
        viewModelScope.launch {
            refreshToken()
            _quizState.value = QuizState.Loading
            val dailyPoints = getQuizPointsBalance()
            repository.startSession(
                token = authToken,
                sessionType = "topic",
                category = category,
            ).fold(
                onSuccess = { (session, questions) ->
                    if (questions.isEmpty()) {
                        _quizState.value = QuizState.Error("Không có câu hỏi cho chủ đề này")
                        return@launch
                    }
                    val sessionId = session.id.takeIf { it.isNotBlank() }
                    val newState = QuizState.Question(
                        questions = questions,
                        currentIndex = 0,
                        sessionId = sessionId,
                        answers = List(questions.size) { null },
                        timeRemaining = QUESTION_TIME_SECONDS,
                        lastResult = null,
                        showingResult = false,
                        hiddenOptions = emptyMap(),
                        hints = emptyMap(),
                        usedAssists = emptyMap(),
                        dailyPoints = dailyPoints,
                        assistMessage = null,
                        isMilestonePaused = false,
                        reviewMode = false,
                        reviewCorrectAnswers = emptyMap(),
                    )
                    _quizState.value = newState
                    saveActiveSessionState(newState)
                    startTimer()
                },
                onFailure = { _ ->
                    _quizState.value = QuizState.Error("Không thể tải câu hỏi. Vui lòng thử lại.")
                },
            )
        }
    }

    fun submitAnswer(chosen: String, timeMs: Int) {
        val state = _quizState.value as? QuizState.Question ?: return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        timerJob?.cancel()

        val chosenLower = normalizeChoice(chosen)
        val token = authToken
        val sessionId = state.sessionId

        if (sessionId != null) {
            // Server sessions hide correct answers, so every logged-in session must be checked by backend.
            _quizState.value = state.copy(showingResult = true)
            viewModelScope.launch {
                val submitToken = token ?: refreshBackendTokenForSync()?.also { authToken = it }
                if (submitToken == null) {
                    val latestState = _quizState.value as? QuizState.Question ?: return@launch
                    _quizState.value = latestState.copy(
                        showingResult = false,
                        assistMessage = "Không thể xác thực phiên đăng nhập. Vui lòng thử lại."
                    )
                    startTimer()
                    return@launch
                }
                repository.submitAnswer(
                    token = submitToken,
                    sessionId = sessionId,
                    questionId = question.id,
                    chosen = chosenLower,
                    timeMs = timeMs,
                ).fold(
                    onSuccess = { serverResult ->
                        val latestState = _quizState.value as? QuizState.Question ?: return@launch
                        updateWithAnswer(latestState, serverResult)

                        autoAdvanceJob?.cancel()
                        autoAdvanceJob = viewModelScope.launch {
                            delay(3500L)
                            val currentState = _quizState.value as? QuizState.Question
                            if (currentState != null && currentState.currentIndex == latestState.currentIndex && currentState.showingResult) {
                                nextQuestion()
                            }
                        }
                    },
                    onFailure = { error ->
                        val latestState = _quizState.value as? QuizState.Question ?: return@launch
                        _quizState.value = latestState.copy(
                            showingResult = false,
                            assistMessage = "Lỗi kết nối: ${error.localizedMessage}. Vui lòng chọn lại."
                        )
                        startTimer()
                    }
                )
            }
        } else {
            // Local evaluation (guest / practice / fallback)
            val localResult = buildLocalResult(
                question = question,
                chosen = chosenLower,
                timeMs = timeMs,
                correctOverride = state.reviewCorrectAnswers[question.id],
            )
            updateWithAnswer(state, localResult)

            autoAdvanceJob?.cancel()
            autoAdvanceJob = viewModelScope.launch {
                delay(3500L)
                val currentState = _quizState.value as? QuizState.Question
                if (currentState != null && currentState.currentIndex == state.currentIndex && currentState.showingResult) {
                    nextQuestion()
                }
            }
        }
    }

    private fun buildLocalResult(
        question: QuizQuestion,
        chosen: String,
        timeMs: Int,
        correctOverride: String? = null,
    ): SubmitAnswerResult {
        val normalizedOverride = normalizeChoice(correctOverride)
        val correctAnswer = if (normalizedOverride in setOf("a", "b", "c", "d")) {
            normalizedOverride
        } else {
            question.correctOptionKey()
        }
        val isCorrect = correctAnswer.isNotBlank() && chosen == correctAnswer
        return SubmitAnswerResult(
            questionId = question.id,
            chosen = chosen,
            correct = correctAnswer,
            isCorrect = isCorrect,
            explanation = question.explanation,
            articleId = question.articleId,
            pointsEarned = if (isCorrect) 1 else 0,
            timeMs = timeMs,
        )
    }

    private fun normalizeChoice(value: String?): String =
        value?.trim()?.lowercase().orEmpty()

    private fun QuizQuestion.correctOptionKey(): String {
        val normalizedCorrect = normalizeChoice(correct)
        if (normalizedCorrect in setOf("a", "b", "c", "d")) {
            return normalizedCorrect
        }

        val normalizedAnswer = normalizeChoice(correctAnswer ?: correct)
        return listOf(optionA, optionB, optionC, optionD)
            .map(::normalizeChoice)
            .indexOf(normalizedAnswer)
            .takeIf { it >= 0 }
            ?.let { listOf("a", "b", "c", "d")[it] }
            .orEmpty()
    }

    fun useAssist(type: QuizAssistType) {
        val state = _quizState.value as? QuizState.Question ?: return
        if (state.showingResult) return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val usedForQuestion = state.usedAssists[question.id].orEmpty()
        if (type in usedForQuestion) {
            _quizState.value = state.copy(assistMessage = "${type.label} đã dùng cho câu này")
            return
        }

        val correct = question.correctOptionKey()
        if (type == QuizAssistType.Hint && question.hint.isNullOrBlank()) {
            _quizState.value = state.copy(assistMessage = "Câu này chưa có gợi ý từ người soạn")
            return
        }
        if (type == QuizAssistType.FiftyFifty && correct.isBlank() && authToken == null) {
            // Guest mode checks locally, online mode can check even if correct is hidden initially
            _quizState.value = state.copy(assistMessage = "Câu này chưa đủ dữ liệu để dùng trợ giúp")
            return
        }

        val token = authToken
        val sessionId = state.sessionId

        viewModelScope.launch {
            if (token != null) {
                // Online mode: Spend points from GORM wallet via API
                val assistKey = when (type) {
                    QuizAssistType.FiftyFifty -> "fifty_fifty"
                    QuizAssistType.Hint -> "hint"
                    QuizAssistType.ExtraTime -> "extra_time"
                }
                val source = "quiz_assist_$assistKey"
                val metadata = mapOf(
                    "session_id" to (sessionId ?: ""),
                    "question_id" to question.id
                )

                repository.spendPoints(
                    token = token,
                    amount = type.cost,
                    source = source,
                    sourceId = sessionId,
                    idempotencyKey = "quiz_assist_${assistKey}_${question.id}_${sessionId}",
                    metadata = metadata
                ).fold(
                    onSuccess = { updatedWallet ->
                        val latest = _quizState.value as? QuizState.Question ?: return@launch
                        val latestUsed = latest.usedAssists[question.id].orEmpty() + type
                        val usedMap = latest.usedAssists + (question.id to latestUsed)

                        val updated = when (type) {
                            QuizAssistType.FiftyFifty -> {
                                val hidden = buildFiftyFiftyHiddenOptions(question, correct)
                                latest.copy(
                                    hiddenOptions = latest.hiddenOptions + (question.id to hidden),
                                    usedAssists = usedMap,
                                    dailyPoints = updatedWallet.balance,
                                    assistMessage = "Đã loại 2 đáp án sai",
                                )
                            }
                            QuizAssistType.Hint -> latest.copy(
                                hints = latest.hints + (question.id to question.hint.orEmpty().trim()),
                                usedAssists = usedMap,
                                dailyPoints = updatedWallet.balance,
                                assistMessage = "Đã mở gợi ý",
                            )
                            QuizAssistType.ExtraTime -> latest.copy(
                                timeRemaining = (latest.timeRemaining + 15).coerceAtMost(QUESTION_TIME_SECONDS + 15),
                                usedAssists = usedMap,
                                dailyPoints = updatedWallet.balance,
                                assistMessage = "Đã cộng thêm 15 giây",
                            )
                        }
                        _quizState.value = updated
                        saveActiveSessionState(updated)
                    },
                    onFailure = { error ->
                        updateAssistMessage("Lỗi giao dịch điểm: ${error.localizedMessage}")
                    }
                )
            } else {
                // Offline/Guest mode
                pointsRepository.rolloverIfNeeded()
                val ledger = pointsRepository.getOrCreateLedger()
                if (ledger.dailyPoints < type.cost) {
                    val short = type.cost - ledger.dailyPoints
                    updateAssistMessage("Cần thêm $short điểm ngày để dùng ${type.label}")
                    return@launch
                }

                val spendKey = "QUIZ_${type.name}_${question.id}_${currentSessionStartedAtMs}"
                val spent = pointsRepository.spendDaily(spendKey, type.cost)
                if (!spent) {
                    updateAssistMessage("Không thể trừ điểm lúc này, thử lại sau nhé")
                    return@launch
                }

                val latest = _quizState.value as? QuizState.Question ?: return@launch
                val latestUsed = latest.usedAssists[question.id].orEmpty() + type
                val usedMap = latest.usedAssists + (question.id to latestUsed)
                val dailyAfterSpend = (ledger.dailyPoints - type.cost).coerceAtLeast(0)

                val updated = when (type) {
                    QuizAssistType.FiftyFifty -> {
                        val hidden = buildFiftyFiftyHiddenOptions(question, correct)
                        latest.copy(
                            hiddenOptions = latest.hiddenOptions + (question.id to hidden),
                            usedAssists = usedMap,
                            dailyPoints = dailyAfterSpend,
                            assistMessage = "Đã loại 2 đáp án sai",
                        )
                    }
                    QuizAssistType.Hint -> latest.copy(
                        hints = latest.hints + (question.id to question.hint.orEmpty().trim()),
                        usedAssists = usedMap,
                        dailyPoints = dailyAfterSpend,
                        assistMessage = "Đã mở gợi ý",
                    )
                    QuizAssistType.ExtraTime -> latest.copy(
                        timeRemaining = (latest.timeRemaining + 15).coerceAtMost(QUESTION_TIME_SECONDS + 15),
                        usedAssists = usedMap,
                        dailyPoints = dailyAfterSpend,
                        assistMessage = "Đã cộng thêm 15 giây",
                    )
                }
                _quizState.value = updated
                saveActiveSessionState(updated)
            }
        }
    }

    fun clearAssistMessage() {
        val state = _quizState.value as? QuizState.Question ?: return
        if (state.assistMessage != null) {
            val newState = state.copy(assistMessage = null)
            _quizState.value = newState
            saveActiveSessionState(newState)
        }
    }

    private suspend fun currentDailyPoints(): Int {
        pointsRepository.rolloverIfNeeded()
        return pointsRepository.getOrCreateLedger().dailyPoints
    }

    private fun updateAssistMessage(message: String) {
        val state = _quizState.value as? QuizState.Question ?: return
        _quizState.value = state.copy(assistMessage = message)
    }

    private fun buildFiftyFiftyHiddenOptions(question: QuizQuestion, correct: String): Set<String> {
        val options = listOf("a", "b", "c", "d")
        val seed = question.id + stateSeed(question.content)
        return options
            .filterNot { it == correct }
            .sortedBy { option -> ((seed + option.first().code) % 97).toInt() }
            .take(2)
            .toSet()
    }

    private fun stateSeed(value: String): Long =
        value.fold(0L) { acc, char -> (acc * 31 + char.code).and(0x7fffffffL) }

    private fun updateWithAnswer(state: QuizState.Question, result: SubmitAnswerResult) {
        val newAnswers = state.answers.toMutableList()
        newAnswers[state.currentIndex] = result
        val newState = state.copy(
            answers = newAnswers,
            lastResult = result,
            showingResult = true,
        )
        _quizState.value = newState
        saveActiveSessionState(newState)
    }

    fun nextQuestion() {
        autoAdvanceJob?.cancel()
        val state = _quizState.value as? QuizState.Question ?: return
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            finishQuiz()
            return
        }

        // Daily quiz milestones pausing after Q5 (index 5) and Q10 (index 10)
        val shouldPause = currentSessionType == "daily" && (nextIndex == 5 || nextIndex == 10)

        val newState = state.copy(
            currentIndex = nextIndex,
            timeRemaining = QUESTION_TIME_SECONDS,
            lastResult = null,
            showingResult = false,
            assistMessage = null,
            isMilestonePaused = shouldPause,
        )
        _quizState.value = newState
        saveActiveSessionState(newState)
        if (!shouldPause) {
            startTimer()
        }
    }

    fun finishQuiz() {
        autoAdvanceJob?.cancel()
        clearActiveSessionState()
        val state = _quizState.value as? QuizState.Question ?: return
        timerJob?.cancel()
        val token = authToken
        val sessionId = state.sessionId

        if (token != null && sessionId != null) {
            viewModelScope.launch {
                repository.finishSession(token, sessionId).fold(
                    onSuccess = { result ->
                        _quizState.value = QuizState.Finished(result, state.answers, state.questions)
                    },
                    onFailure = {
                        _quizState.value = QuizState.Finished(null, state.answers, state.questions)
                    },
                )
            }
        } else {
            viewModelScope.launch {
                repository.saveGuestSession(
                    sessionType = currentSessionType,
                    category = currentSessionCategory,
                    questionIds = state.questions.map { it.id },
                    answers = state.answers.filterNotNull(),
                    startedAtMs = currentSessionStartedAtMs.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    finishedAtMs = System.currentTimeMillis(),
                )
            }
            _quizState.value = QuizState.Finished(null, state.answers, state.questions)
        }
    }

    fun resetToIdle() {
        timerJob?.cancel()
        autoAdvanceJob?.cancel()
        _quizState.value = QuizState.Idle
    }

    fun loadLeaderboard(period: String = "weekly") {
        currentLeaderboardPeriod = period
        leaderboardJob?.cancel()
        leaderboardJob = viewModelScope.launch {
            repository.getLeaderboard(period).collectLatest { response ->
                _leaderboard.value = response
            }
        }
        val token = authToken
        if (token != null) {
            viewModelScope.launch {
                repository.getMyRank(token, period).getOrNull()?.let {
                    _myRank.value = it
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _quizState.value as? QuizState.Question ?: break
                if (state.showingResult) break
                if (state.isMilestonePaused) continue
                val newTime = state.timeRemaining - 1
                if (newTime <= 0) {
                    submitAnswer("", 30_000)
                    break
                }
                _quizState.value = state.copy(timeRemaining = newTime)
            }
        }
    }

    private suspend fun syncPendingGuestSessions() {
        val token = refreshBackendTokenForSync() ?: return
        val syncedCount = repository.syncPendingGuestSessions(token).getOrNull() ?: 0
        if (syncedCount > 0) {
            loadLeaderboard(currentLeaderboardPeriod)
        }
        syncGuestPointsOffline(token)
    }

    private suspend fun syncGuestPointsOffline(token: String) {
        try {
            val logs = pointsRepository.getAllActionLogsOnce()
            val unlocks = pointsRepository.getAllDailyUnlocksOnce()

            if (logs.isEmpty() && unlocks.isEmpty()) return

            val transactions = mutableListOf<Map<String, Any?>>()

            logs.forEach { e ->
                val amount = e.dailyPointsAwarded + e.permanentPointsAwarded
                if (amount > 0) {
                    transactions.add(
                        mapOf(
                            "amount" to amount,
                            "direction" to "earn",
                            "source" to e.actionType,
                            "idempotency_key" to "guest_earn_${e.actionType}_${e.timestamp}_${e.id}",
                            "created_at" to java.time.Instant.ofEpochMilli(e.timestamp).toString()
                        )
                    )
                }
            }

            unlocks.forEach { u ->
                if (u.cost > 0) {
                    transactions.add(
                        mapOf(
                            "amount" to u.cost,
                            "direction" to "spend",
                            "source" to u.unlockKey,
                            "idempotency_key" to "guest_spend_${u.unlockKey}",
                            "created_at" to java.time.Instant.ofEpochMilli(u.unlockedAt).toString()
                        )
                    )
                }
            }

            if (transactions.isNotEmpty()) {
                repository.syncGuestPoints(token, transactions).fold(
                    onSuccess = { _ ->
                        pointsRepository.clearActionLog()
                        pointsRepository.clearDailyUnlocks()
                    },
                    onFailure = { error ->
                        android.util.Log.e("QuizViewModel", "Failed to sync guest points", error)
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("QuizViewModel", "Error in syncGuestPointsOffline", e)
        }
    }

    private suspend fun refreshBackendTokenForSync(): String? {
        return try {
            authRepository.refreshBackendTokenIfNeeded()
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        autoAdvanceJob?.cancel()
        leaderboardJob?.cancel()
    }
}
