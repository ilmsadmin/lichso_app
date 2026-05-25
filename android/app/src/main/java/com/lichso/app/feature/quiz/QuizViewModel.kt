package com.lichso.app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lichso.app.data.remote.LeaderboardResponse
import com.lichso.app.data.remote.MyRankResponse
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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
    ) : QuizState
    data class Finished(val result: SessionResult?, val answers: List<SubmitAnswerResult?>) : QuizState
    data class Error(val message: String) : QuizState
}

data class QuizHomeUiState(
    val dailyQuestionCount: Int? = null,
    val categoryQuestionCounts: Map<String, Int> = emptyMap(),
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
) : ViewModel() {

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _leaderboard = MutableStateFlow<LeaderboardResponse?>(null)
    val leaderboard: StateFlow<LeaderboardResponse?> = _leaderboard.asStateFlow()

    private val _myRank = MutableStateFlow<MyRankResponse?>(null)
    val myRank: StateFlow<MyRankResponse?> = _myRank.asStateFlow()

    private val _homeState = MutableStateFlow(QuizHomeUiState())
    val homeState: StateFlow<QuizHomeUiState> = _homeState.asStateFlow()

    // JWT token — will be null for guest users
    private var authToken: String? = null

    private var timerJob: Job? = null

    private val QUESTION_TIME_SECONDS = 30

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
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    fun loadDailyQuiz() {
        val date = LocalDate.now().toString()
        viewModelScope.launch {
            refreshToken()
            _quizState.value = QuizState.Loading
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
                    val limitedQuestions = questions.take(5)
                    _quizState.value = QuizState.Question(
                        questions = limitedQuestions,
                        currentIndex = 0,
                        sessionId = sessionId,
                        answers = List(limitedQuestions.size) { null },
                        timeRemaining = QUESTION_TIME_SECONDS,
                        lastResult = null,
                        showingResult = false,
                    )
                    startTimer()
                },
                onFailure = { e ->
                    _quizState.value = QuizState.Error(e.message ?: "Lỗi tải câu hỏi")
                },
            )
        }
    }

    fun loadTopicQuiz(category: String) {
        viewModelScope.launch {
            refreshToken()
            _quizState.value = QuizState.Loading
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
                    _quizState.value = QuizState.Question(
                        questions = questions,
                        currentIndex = 0,
                        sessionId = sessionId,
                        answers = List(questions.size) { null },
                        timeRemaining = QUESTION_TIME_SECONDS,
                        lastResult = null,
                        showingResult = false,
                    )
                    startTimer()
                },
                onFailure = { e ->
                    _quizState.value = QuizState.Error(e.message ?: "Lỗi tải câu hỏi")
                },
            )
        }
    }

    fun submitAnswer(chosen: String, timeMs: Int) {
        val state = _quizState.value as? QuizState.Question ?: return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        timerJob?.cancel()

        val chosenLower = normalizeChoice(chosen)

        // Evaluate locally immediately — server now returns correct field with questions
        val localResult = buildLocalResult(question, chosenLower)
        updateWithAnswer(state, localResult)

        // Fire-and-forget to server for leaderboard scoring (doesn't affect UI)
        val token = authToken
        val sessionId = state.sessionId
        if (token != null && sessionId != null) {
            viewModelScope.launch {
                repository.submitAnswer(
                    token = token,
                    sessionId = sessionId,
                    questionId = question.id,
                    chosen = chosenLower,
                    timeMs = timeMs,
                )
            }
        }
    }

    private fun buildLocalResult(question: QuizQuestion, chosen: String): SubmitAnswerResult {
        val correctAnswer = question.correctOptionKey()
        val isCorrect = correctAnswer.isNotBlank() && chosen == correctAnswer
        return SubmitAnswerResult(
            questionId = question.id,
            chosen = chosen,
            correct = correctAnswer,
            isCorrect = isCorrect,
            explanation = question.explanation,
            articleId = question.articleId,
            pointsEarned = if (isCorrect) 1 else 0,
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

    private fun updateWithAnswer(state: QuizState.Question, result: SubmitAnswerResult) {
        val newAnswers = state.answers.toMutableList()
        newAnswers[state.currentIndex] = result
        _quizState.value = state.copy(
            answers = newAnswers,
            lastResult = result,
            showingResult = true,
        )
    }

    fun nextQuestion() {
        val state = _quizState.value as? QuizState.Question ?: return
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            // Auto-finish
            finishQuiz()
            return
        }
        _quizState.value = state.copy(
            currentIndex = nextIndex,
            timeRemaining = QUESTION_TIME_SECONDS,
            lastResult = null,
            showingResult = false,
        )
        startTimer()
    }

    fun finishQuiz() {
        val state = _quizState.value as? QuizState.Question ?: return
        timerJob?.cancel()
        val token = authToken
        val sessionId = state.sessionId

        if (token != null && sessionId != null) {
            viewModelScope.launch {
                repository.finishSession(token, sessionId).fold(
                    onSuccess = { result ->
                        _quizState.value = QuizState.Finished(result, state.answers)
                    },
                    onFailure = {
                        _quizState.value = QuizState.Finished(null, state.answers)
                    },
                )
            }
        } else {
            _quizState.value = QuizState.Finished(null, state.answers)
        }
    }

    fun resetToIdle() {
        timerJob?.cancel()
        _quizState.value = QuizState.Idle
    }

    fun loadLeaderboard(period: String = "weekly") {
        viewModelScope.launch {
            repository.getLeaderboard(period).collectLatest { response ->
                _leaderboard.value = response
            }
        }
        // Also load my rank if logged in
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
                val newTime = state.timeRemaining - 1
                if (newTime <= 0) {
                    // Time up — auto-submit empty
                    submitAnswer("", 30_000)
                    break
                }
                _quizState.value = state.copy(timeRemaining = newTime)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
