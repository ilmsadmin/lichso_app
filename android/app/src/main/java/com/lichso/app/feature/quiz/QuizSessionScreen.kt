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
import kotlinx.coroutines.delay

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
            title = "Đố Vui",
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
                val isGuest by viewModel.isGuest.collectAsState()
                if (state.isMilestonePaused) {
                    MilestonePauseScreen(
                        state = state,
                        onDismiss = { viewModel.dismissMilestone() }
                    )
                } else {
                    QuizSessionContent(
                        state = state,
                        onAnswerSelected = { chosen ->
                            val timeMs = ((30 - state.timeRemaining).coerceAtLeast(0)) * 1000
                            viewModel.submitAnswer(chosen, timeMs)
                        },
                        onUseAssist = { viewModel.useAssist(it) },
                        onAssistMessageShown = { viewModel.clearAssistMessage() },
                        onNext = { viewModel.nextQuestion() },
                        onAskAi = onAskAi,
                        isGuest = isGuest,
                    )
                }
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
    onUseAssist: (QuizAssistType) -> Unit,
    onAssistMessageShown: () -> Unit,
    onNext: () -> Unit,
    onAskAi: (String) -> Unit,
    isGuest: Boolean,
) {
    val c = LichSoThemeColors.current
    val totalQuestions = state.questions.size
    val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
    val hiddenOptions = state.hiddenOptions[currentQ.id].orEmpty()
    val hint = state.hints[currentQ.id]
    val usedAssists = state.usedAssists[currentQ.id].orEmpty()

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
                Spacer(modifier = Modifier.height(6.dp))
                if (totalQuestions == 15) {
                    SegmentedProgressBar(
                        currentIndex = state.currentIndex,
                        totalQuestions = totalQuestions,
                        answers = state.answers,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
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
        state.modeNotice?.let { notice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.surfaceContainer)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = c.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    notice,
                    style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                )
            }
        }

        AssistPanel(
            dailyPoints = state.dailyPoints,
            usedAssists = usedAssists,
            enabled = !state.showingResult,
            onUseAssist = onUseAssist,
        )

        state.assistMessage?.let { message ->
            LaunchedEffect(message) {
                delay(2200L)
                onAssistMessageShown()
            }
            AssistMessageCard(message = message)
        }

        if (!hint.isNullOrBlank()) {
            HintCard(hint = hint)
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
        ) { targetIndex ->
            QuestionCard(question = state.questions[targetIndex])
        }

        // Answer options
        val answerLabels = listOf("A", "B", "C", "D")
        val answerTexts = listOf(currentQ.optionA, currentQ.optionB, currentQ.optionC, currentQ.optionD)
        val lastResult = state.lastResult
        val revealAnswerNow = state.reviewMode || lastResult?.isCorrect == true
        val displayedCorrect = lastResult?.correct.takeIf { revealAnswerNow }

        answerLabels.forEachIndexed { index, label ->
            val optionKey = label.lowercase()
            AnswerButton(
                label = label,
                text = answerTexts.getOrElse(index) { "" },
                isAnswered = state.showingResult,
                isHidden = optionKey in hiddenOptions,
                chosen = lastResult?.chosen,
                correct = displayedCorrect,
                isAnswerCorrect = lastResult?.isCorrect,
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
                    isGuest = isGuest,
                    revealAnswer = state.reviewMode || result.isCorrect,
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

// ── Assists ──

@Composable
private fun AssistPanel(
    dailyPoints: Int,
    usedAssists: Set<QuizAssistType>,
    enabled: Boolean,
    onUseAssist: (QuizAssistType) -> Unit,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = c.gold, modifier = Modifier.size(18.dp))
                Text(
                    "Trợ giúp",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                )
            }
            Text(
                "$dailyPoints điểm",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.gold),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuizAssistType.entries.forEach { type ->
                AssistButton(
                    type = type,
                    used = type in usedAssists,
                    canAfford = dailyPoints >= type.cost,
                    enabled = enabled,
                    onClick = { onUseAssist(type) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AssistButton(
    type: QuizAssistType,
    used: Boolean,
    canAfford: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    val active = enabled && !used && canAfford
    val tint = when {
        used -> c.textTertiary
        canAfford -> c.primary
        else -> c.textSecondary
    }
    val icon = when (type) {
        QuizAssistType.FiftyFifty -> Icons.Filled.FilterAlt
        QuizAssistType.Hint -> Icons.Filled.Lightbulb
        QuizAssistType.ExtraTime -> Icons.Filled.Timer
    }
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) c.primary.copy(alpha = 0.08f) else c.outlineVariant.copy(alpha = 0.35f))
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .then(if (enabled && !used) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (used) "Đã dùng" else type.label,
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tint),
            maxLines = 1,
        )
        Text(
            "${type.cost} điểm",
            style = TextStyle(fontSize = 10.sp, color = c.textTertiary),
            maxLines = 1,
        )
    }
}

@Composable
private fun AssistMessageCard(message: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.goldDim)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = c.gold, modifier = Modifier.size(16.dp))
        Text(message, style = TextStyle(fontSize = 12.sp, color = c.gold))
    }
}

@Composable
private fun HintCard(hint: String) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.primary.copy(alpha = 0.08f))
            .border(1.dp, c.primary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
            Text("Gợi ý đã mở", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.primary))
        }
        Text(hint, style = TextStyle(fontSize = 12.sp, color = c.textPrimary, lineHeight = 18.sp))
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
                    mapCategoryToVietnamese(question.category),
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = c.primary,
                        fontWeight = FontWeight.Medium,
                    )
                )
                Text("•", style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                Text(
                    mapDifficultyToVietnamese(question.difficulty),
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
    isHidden: Boolean,
    chosen: String?,
    correct: String?,
    isAnswerCorrect: Boolean?,
    thisOption: String,
    onSelect: () -> Unit,
) {
    val c = LichSoThemeColors.current

    val correctKnown = isAnswered && !correct.isNullOrBlank()
    val (bgColor, borderColor, textColor) = when {
        isHidden -> Triple(c.outlineVariant.copy(alpha = 0.35f), c.outlineVariant, c.textTertiary)
        !isAnswered -> Triple(c.surfaceContainer, c.outlineVariant, c.textPrimary)
        correctKnown && thisOption.equals(correct, ignoreCase = true) ->
            Triple(Color(0xFF1B5E20).copy(alpha = if (c.isDark) 0.3f else 0.12f), Color(0xFF2E7D32), Color(0xFF2E7D32))
        correctKnown && thisOption.equals(chosen, ignoreCase = true) ->
            Triple(Color(0xFFB71C1C).copy(alpha = if (c.isDark) 0.3f else 0.12f), Color(0xFFB71C1C), Color(0xFFB71C1C))
        isAnswered && isAnswerCorrect == false && thisOption.equals(chosen, ignoreCase = true) ->
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
            .then(if (!isAnswered && !isHidden) Modifier.clickable(onClick = onSelect) else Modifier)
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
            if (isHidden) "Đã loại bởi 50/50" else text,
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
                isAnswerCorrect == false && thisOption.equals(chosen, ignoreCase = true) ->
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
    isGuest: Boolean,
    revealAnswer: Boolean,
) {
    val c = LichSoThemeColors.current
    val isCorrect = result.isCorrect
    val correctKnown = revealAnswer && result.correct.isNotBlank()
    // When correct answer is unknown (guest mode, public API), show neutral state
    val accentColor = when {
        isCorrect -> Color(0xFF2E7D32)
        !isCorrect -> Color(0xFFB71C1C)
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
                    !isCorrect -> Icons.Filled.Cancel
                    else -> Icons.Filled.Info
                },
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                when {
                    isCorrect -> "Chính xác!"
                    !isCorrect && revealAnswer -> "Sai rồi!"
                    !isCorrect -> "Chưa chính xác"
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

        if (revealAnswer && !result.explanation.isNullOrBlank()) {
            Text(
                result.explanation,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = c.textPrimary,
                    lineHeight = 20.sp,
                )
            )
        }

        if (!isCorrect && !revealAnswer) {
            Text(
                if (isGuest) {
                    "Đăng nhập để lưu điểm. Đáp án và lời giải sẽ có ở phần ôn lại."
                } else {
                    "Đáp án và lời giải sẽ mở ở phần ôn lại câu sai."
                },
                style = TextStyle(fontSize = 12.sp, color = accentColor),
            )
        }

        if (!isCorrect && revealAnswer) {
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

private fun mapCategoryToVietnamese(category: String): String {
    return when (category.trim().lowercase()) {
        "history_vn" -> "Lịch sử Việt Nam"
        "history_world" -> "Lịch sử Thế giới"
        "culture" -> "Văn hóa"
        "geography" -> "Địa lý"
        "general" -> "Tổng hợp"
        else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun mapDifficultyToVietnamese(difficulty: String): String {
    return when (difficulty.trim().lowercase()) {
        "easy" -> "Dễ"
        "medium" -> "Trung bình"
        "hard" -> "Khó"
        else -> difficulty.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Composable
private fun SegmentedProgressBar(
    currentIndex: Int,
    totalQuestions: Int,
    answers: List<SubmitAnswerResult?>,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val segments = if (totalQuestions == 15) 3 else 1
        val questionsPerSegment = totalQuestions / segments

        for (s in 0 until segments) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val startIdx = s * questionsPerSegment
                val endIdx = startIdx + questionsPerSegment
                for (i in startIdx until endIdx) {
                    val isCurrent = i == currentIndex
                    val answer = answers.getOrNull(i)
                    val color = when {
                        answer != null && answer.isCorrect -> Color(0xFF2E7D32)
                        answer != null && !answer.isCorrect -> Color(0xFFD32F2F)
                        isCurrent -> c.primary
                        else -> c.outlineVariant.copy(alpha = 0.6f)
                    }
                    val size = if (isCurrent) 10.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            if (s < segments - 1) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(14.dp)
                        .background(c.textTertiary.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun MilestonePauseScreen(
    state: QuizState.Question,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val chặngNumber = if (state.currentIndex == 5) 1 else 2
    val startIndex = if (chặngNumber == 1) 0 else 5
    val endIndex = if (chặngNumber == 1) 5 else 10

    val chặngAnswers = state.answers.subList(startIndex, endIndex)
    val correctCount = chặngAnswers.count { it?.isCorrect == true }
    val pointsEarned = chặngAnswers.sumOf { it?.pointsEarned ?: 0 }

    var countdown by remember { mutableStateOf(5) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg.copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(c.primary.copy(alpha = 0.15f), c.surfaceContainer)
                    )
                )
                .border(1.5.dp, c.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Flag,
                contentDescription = null,
                tint = c.gold,
                modifier = Modifier.size(64.dp)
            )

            Text(
                "CHẶNG $chặngNumber HOÀN THÀNH!",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    letterSpacing = 1.5.sp
                )
            )

            Text(
                "Bạn đã vượt qua chặng đường chông gai! Hãy nghỉ ngơi một chút trước khi tiếp tục.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            HorizontalDivider(color = c.outlineVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$correctCount / 5",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    )
                    Text(
                        "Chính xác",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "+$pointsEarned",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.gold)
                    )
                    Text(
                        "Điểm chặng",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                    )
                }
            }

            HorizontalDivider(color = c.outlineVariant, thickness = 1.dp)

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary)
            ) {
                Text(
                    "Tiếp tục cuộc thi ($countdown)",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }
    }
}
