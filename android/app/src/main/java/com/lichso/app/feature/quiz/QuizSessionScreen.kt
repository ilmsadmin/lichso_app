package com.lichso.app.feature.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.SubmitAnswerResult
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun QuizSessionScreen(
    onBackClick: () -> Unit = {},
    onFinished: () -> Unit = {},
    onAskAi: (String) -> Unit = {},
    initialCategory: String? = null,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val quizState by viewModel.quizState.collectAsState()

    // Observe finished state and navigate out
    LaunchedEffect(quizState) {
        if (quizState is QuizState.Finished) {
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Quiz",
            onBackClick = {
                viewModel.resetToIdle()
                onBackClick()
            },
        )

        when (val state = quizState) {
            is QuizState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
                }
            }
            is QuizState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = {
                        if (initialCategory.isNullOrBlank()) {
                            viewModel.loadDailyQuiz()
                        } else {
                            viewModel.loadTopicQuiz(initialCategory)
                        }
                    },
                )
            }
            is QuizState.Question -> {
                QuizSessionContent(
                    state = state,
                    onAnswerSelected = { chosen ->
                        val timeMs = (30 - state.timeRemaining) * 1000
                        viewModel.submitAnswer(chosen, timeMs)
                    },
                    onNext = { viewModel.nextQuestion() },
                    onAskAi = onAskAi,
                    isGuest = true, // TODO: wire real auth
                )
            }
            is QuizState.Idle -> {
                LaunchedEffect(initialCategory) {
                    if (initialCategory.isNullOrBlank()) {
                        viewModel.loadDailyQuiz()
                    } else {
                        viewModel.loadTopicQuiz(initialCategory)
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary)
                }
            }
            is QuizState.Finished -> {
                // Handled by LaunchedEffect above
            }
        }
    }
}

@Composable
private fun QuizSessionContent(
    state: QuizState.Question,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
    onAskAi: (String) -> Unit,
    isGuest: Boolean,
) {
    val c = LichSoThemeColors.current
    val totalQuestions = state.questions.size
    val currentQ = state.questions.getOrNull(state.currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress + timer row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Progress bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${state.currentIndex + 1} / $totalQuestions",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textSecondary,
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (state.currentIndex + 1).toFloat() / totalQuestions.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = c.primary,
                    trackColor = c.outlineVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Timer circle
            TimerCircle(timeRemaining = state.timeRemaining)
        }

        // Guest notice
        if (isGuest) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.goldDim)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = c.gold,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Đăng nhập để lưu điểm",
                    style = TextStyle(fontSize = 12.sp, color = c.gold),
                )
            }
        }

        // Question card
        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()).togetherWith(
                    slideOutHorizontally { -it } + fadeOut()
                )
            },
            label = "question_transition",
        ) {
            QuestionCard(question = currentQ)
        }

        // Answer options
        val answerLabels = listOf("A", "B", "C", "D")
        val answerTexts = listOf(currentQ.optionA, currentQ.optionB, currentQ.optionC, currentQ.optionD)

        answerLabels.forEachIndexed { index, label ->
            AnswerButton(
                label = label,
                text = answerTexts.getOrElse(index) { "" },
                isAnswered = state.showingResult,
                chosen = state.lastResult?.chosen,
                correct = state.lastResult?.correct,
                thisOption = label,
                onSelect = { onAnswerSelected(label) },
            )
        }

        // Explanation card (slides up after answer)
        AnimatedVisibility(
            visible = state.showingResult,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            state.lastResult?.let { result ->
                ExplanationCard(
                    result = result,
                    question = currentQ,
                    onAskAi = onAskAi,
                )
            }
        }

        // "Next" button
        AnimatedVisibility(visible = state.showingResult) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
            ) {
                Text(
                    if (state.currentIndex + 1 >= totalQuestions) "Xem kết quả" else "Tiếp theo",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Timer circle ──

@Composable
private fun TimerCircle(timeRemaining: Int) {
    val timerColor = when {
        timeRemaining > 15 -> Color(0xFF2E7D32)
        timeRemaining > 7 -> Color(0xFFFF8F00)
        else -> Color(0xFFD32F2F)
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(timerColor.copy(alpha = 0.12f), CircleShape)
            .border(2.dp, timerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$timeRemaining",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor,
            )
        )
    }
}

// ── Question card ──

@Composable
private fun QuestionCard(question: QuizQuestion) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    question.category,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = c.primary,
                        fontWeight = FontWeight.Medium,
                    )
                )
                Text("•", style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                Text(
                    question.difficulty,
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                )
            }
            Text(
                question.content,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 24.sp,
                )
            )
        }
    }
}

// ── Answer button ──

@Composable
private fun AnswerButton(
    label: String,
    text: String,
    isAnswered: Boolean,
    chosen: String?,
    correct: String?,
    thisOption: String,
    onSelect: () -> Unit,
) {
    val c = LichSoThemeColors.current

    val correctKnown = isAnswered && !correct.isNullOrBlank()
    val (bgColor, borderColor, textColor) = when {
        !isAnswered -> Triple(c.surfaceContainer, c.outlineVariant, c.textPrimary)
        correctKnown && thisOption.equals(correct, ignoreCase = true) ->
            Triple(Color(0xFF1B5E20).copy(alpha = if (c.isDark) 0.3f else 0.12f), Color(0xFF2E7D32), Color(0xFF2E7D32))
        correctKnown && thisOption.equals(chosen, ignoreCase = true) ->
            Triple(Color(0xFFB71C1C).copy(alpha = if (c.isDark) 0.3f else 0.12f), Color(0xFFB71C1C), Color(0xFFB71C1C))
        !correctKnown && thisOption.equals(chosen, ignoreCase = true) ->
            Triple(Color(0xFF1565C0).copy(alpha = if (c.isDark) 0.3f else 0.12f), Color(0xFF1565C0), Color(0xFF1565C0))
        else -> Triple(c.surfaceContainer, c.outlineVariant, c.textSecondary)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .then(if (!isAnswered) Modifier.clickable(onClick = onSelect) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(borderColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor,
                )
            )
        }
        Text(
            text,
            style = TextStyle(
                fontSize = 14.sp,
                color = textColor,
                lineHeight = 20.sp,
            ),
            modifier = Modifier.weight(1f),
        )
        if (isAnswered) {
            when {
                correctKnown && thisOption.equals(correct, ignoreCase = true) ->
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                correctKnown && thisOption.equals(chosen, ignoreCase = true) ->
                    Icon(Icons.Filled.Cancel, contentDescription = null, tint = Color(0xFFB71C1C), modifier = Modifier.size(20.dp))
                !correctKnown && thisOption.equals(chosen, ignoreCase = true) ->
                    Icon(Icons.Filled.RadioButtonChecked, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Explanation card ──

@Composable
private fun ExplanationCard(
    result: SubmitAnswerResult,
    question: QuizQuestion,
    onAskAi: (String) -> Unit,
) {
    val c = LichSoThemeColors.current
    val isCorrect = result.isCorrect
    val correctKnown = result.correct.isNotBlank()
    // When correct answer is unknown (guest mode, public API), show neutral state
    val accentColor = when {
        isCorrect -> Color(0xFF2E7D32)
        correctKnown -> Color(0xFFB71C1C)
        else -> Color(0xFF1565C0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = if (c.isDark) 0.15f else 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                when {
                    isCorrect -> Icons.Filled.CheckCircle
                    correctKnown -> Icons.Filled.Cancel
                    else -> Icons.Filled.Info
                },
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                when {
                    isCorrect -> "Chính xác!"
                    correctKnown -> "Sai rồi!"
                    else -> "Đã ghi nhận"
                },
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            )
            if (!isCorrect && correctKnown) {
                Text(
                    "Đáp án: ${result.correct}",
                    style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                )
            }
        }

        if (!result.explanation.isNullOrBlank()) {
            Text(
                result.explanation,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = c.textPrimary,
                    lineHeight = 20.sp,
                )
            )
        }

        if (!isCorrect && !correctKnown) {
            Text(
                "Đăng nhập để xem đáp án và tích điểm",
                style = TextStyle(fontSize = 12.sp, color = accentColor),
            )
        }

        if (!isCorrect) {
            val aiPrompt = "Giải thích chi tiết: ${question.content}"
            TextButton(
                onClick = { onAskAi(aiPrompt) },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Xem AI giải thích",
                    style = TextStyle(fontSize = 13.sp, color = c.primary),
                )
            }
        }
    }
}

// ── Error content ──

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                message,
                style = TextStyle(fontSize = 14.sp, color = c.textSecondary, textAlign = TextAlign.Center),
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
            ) {
                Text("Thử lại", color = Color.White)
            }
        }
    }
}
