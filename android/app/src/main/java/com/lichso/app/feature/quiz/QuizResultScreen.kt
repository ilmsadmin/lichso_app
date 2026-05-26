package com.lichso.app.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun QuizResultScreen(
    onBackClick: () -> Unit = {},
    onAskAi: (String) -> Unit = {},
    onPlayAgain: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val quizState by viewModel.quizState.collectAsState()
    val finishedState = quizState as? QuizState.Finished

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppTopBar(
                title = "Kết quả",
                onBackClick = {
                    viewModel.resetToIdle()
                    onBackClick()
                },
            )

            if (finishedState == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có kết quả", style = TextStyle(color = c.textSecondary))
                }
                return@Column
            }

            val correct = finishedState.answers.count { it?.isCorrect == true }
            val total = finishedState.answers.size.takeIf { it > 0 } ?: (finishedState.result?.total ?: 0)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Score hero
                ScoreHero(
                    result = finishedState.result,
                    answers = finishedState.answers,
                )

                // Points chips
                finishedState.result?.let { result ->
                    PointsChips(result = result)
                }

                // Answer list
                AnswerReviewList(
                    answers = finishedState.answers,
                    onAskAi = onAskAi,
                )

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val wrongQuestions = finishedState.questions.mapIndexedNotNull { index, question ->
                        val answer = finishedState.answers.getOrNull(index)
                        if (answer?.isCorrect == false) {
                            question.copy(
                                correct = answer.correct.ifBlank { question.correct },
                                explanation = answer.explanation ?: question.explanation,
                            )
                        } else {
                            null
                        }
                    }
                    val wrongQuestionAnswers = finishedState.questions.mapIndexedNotNull { index, question ->
                        val answer = finishedState.answers.getOrNull(index)
                        val correct = answer?.correct.orEmpty()
                        if (answer?.isCorrect == false && correct.isNotBlank()) {
                            question.id to correct
                        } else {
                            null
                        }
                    }.toMap()

                    if (wrongQuestions.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.loadWrongQuestionsSession(
                                    wrongQuestions = wrongQuestions,
                                    reviewCorrectAnswers = wrongQuestionAnswers,
                                )
                                onPlayAgain()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                        ) {
                            Icon(
                                Icons.Filled.Book,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ôn lại câu sai",
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetToIdle()
                                onBackClick()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                width = 1.5.dp,
                            ),
                        ) {
                            Text(
                                "Đóng",
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
                            )
                        }

                        Button(
                            onClick = {
                                finishedState.result?.let { res ->
                                    shareResult(context, res, correct, total)
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = c.primaryContainer),
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = null,
                                tint = c.onPrimaryContainer,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Chia sẻ",
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.onPrimaryContainer),
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.resetToIdle()
                            viewModel.loadDailyQuiz()
                            onPlayAgain()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = c.outline.copy(alpha = 0.15f)),
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = c.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Chơi lại Daily",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Confetti overlay when completed chặng 15 (daily quiz)
        if (finishedState != null && finishedState.answers.size == 15) {
            ConfettiEffect()
        }
    }
}

// ── Score hero ──

@Composable
private fun ScoreHero(result: SessionResult?, answers: List<SubmitAnswerResult?>) {
    val c = LichSoThemeColors.current
    val correct = answers.count { it?.isCorrect == true }
    val total = answers.size.takeIf { it > 0 } ?: (result?.total ?: 0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF7F1D1D))
                    else listOf(c.primary, c.deepRed),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Kết quả của bạn",
                style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f)),
            )
            Text(
                "$correct / $total",
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            )
            if (result != null && result.sessionTitle.isNotBlank()) {
                Text(
                    result.sessionTitle,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center
                    )
                )
            } else {
                val grade = when {
                    total == 0 -> ""
                    correct.toFloat() / total >= 0.8f -> "Xuất sắc!"
                    correct.toFloat() / total >= 0.6f -> "Khá!"
                    correct.toFloat() / total >= 0.4f -> "Trung bình"
                    else -> "Cố gắng hơn nhé!"
                }
                if (grade.isNotBlank()) {
                    Text(
                        grade,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD700),
                        )
                    )
                }
            }
        }
    }
}

// ── Points chips ──

@Composable
private fun PointsChips(result: SessionResult) {
    val c = LichSoThemeColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (result.scoreV2 > 0) {
                PointChip(
                    label = "${result.scoreV2} điểm thành tích",
                    color = c.primary,
                    icon = Icons.Filled.Star,
                    modifier = Modifier.weight(1f),
                )
            }
            if (result.appPointsEarned > 0) {
                PointChip(
                    label = "+${result.appPointsEarned} App Points",
                    color = c.gold,
                    icon = Icons.Filled.AutoAwesome,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (result.xpEarned > 0 || (result.scoreV2 <= 0 && result.appPointsEarned <= 0 && result.pointsEarned > 0)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (result.xpEarned > 0) {
                    PointChip(
                        label = "+${result.xpEarned} XP",
                        color = c.teal,
                        icon = Icons.Filled.ElectricBolt,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (result.scoreV2 <= 0 && result.appPointsEarned <= 0 && result.pointsEarned > 0) {
                    PointChip(
                        label = "+${result.pointsEarned} điểm",
                        color = c.gold,
                        icon = Icons.Filled.AutoAwesome,
                        modifier = Modifier.weight(1f),
                    )
                } else if (result.xpEarned > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PointChip(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
            )
        }
    }
}

// ── Answer review list ──

@Composable
private fun AnswerReviewList(
    answers: List<SubmitAnswerResult?>,
    onAskAi: (String) -> Unit,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Chi tiết từng câu",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
        )
        answers.forEachIndexed { index, result ->
            AnswerReviewRow(
                index = index + 1,
                result = result,
                onAskAi = onAskAi,
            )
        }
    }
}

@Composable
private fun AnswerReviewRow(
    index: Int,
    result: SubmitAnswerResult?,
    onAskAi: (String) -> Unit,
) {
    val c = LichSoThemeColors.current
    val isCorrect = result?.isCorrect == true
    val iconColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFB71C1C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$index",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                )
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Câu $index",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.textPrimary),
            )
            if (result != null) {
                Text(
                    "Đã chọn: ${result.chosen}" + if (!isCorrect && result.correct.isNotBlank()) " • Đúng: ${result.correct}" else "",
                    style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                )
            }
        }

        Icon(
            if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun shareResult(
    context: android.content.Context,
    result: SessionResult,
    correct: Int,
    total: Int
) {
    val shareBody = buildString {
        append("🏆 KẾT QUẢ THI ĐẤU LỊCH SỐ 🏆\n")
        if (result.sessionTitle.isNotBlank()) {
            append("Danh hiệu: ${result.sessionTitle}\n")
        }
        append("Số câu đúng: $correct/$total\n")
        append("Điểm thành tích: ${result.scoreV2}\n")
        append("XP nhận được: +${result.xpEarned}\n")
        append("Ví điểm App Points: +${result.appPointsEarned}\n")
        append("\nChơi ngay ứng dụng Lịch Số để thi tài kiến thức lịch sử!")
    }
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Kết quả thi tài Lịch Số")
        putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Chia sẻ thành tích"))
}

@Composable
private fun ConfettiEffect() {
    val transition = rememberInfiniteTransition(label = "confetti")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = Math.random().toFloat(),
                y = -Math.random().toFloat() * 0.5f,
                speedY = 0.3f + Math.random().toFloat() * 0.8f,
                speedX = -0.2f + Math.random().toFloat() * 0.4f,
                size = 6 + (Math.random() * 8).toInt(),
                color = Color(
                    red = (0.4 + Math.random() * 0.6).toFloat(),
                    green = (0.4 + Math.random() * 0.6).toFloat(),
                    blue = (0.4 + Math.random() * 0.6).toFloat(),
                    alpha = 0.9f
                ),
                rotation = (Math.random() * 360).toFloat(),
                rotationSpeed = -3f + Math.random().toFloat() * 6f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val progressY = (p.y + time * p.speedY) % 1.1f
            val progressX = (p.x + time * p.speedX) % 1.0f
            val actualX = progressX * size.width
            val actualY = progressY * size.height
            val currentRotation = p.rotation + time * p.rotationSpeed * 360f

            rotate(currentRotation, pivot = androidx.compose.ui.geometry.Offset(actualX, actualY)) {
                drawRect(
                    color = p.color,
                    topLeft = androidx.compose.ui.geometry.Offset(actualX - p.size / 2, actualY - p.size / 2),
                    size = androidx.compose.ui.geometry.Size(p.size.toFloat(), p.size.toFloat())
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Int,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float
)
