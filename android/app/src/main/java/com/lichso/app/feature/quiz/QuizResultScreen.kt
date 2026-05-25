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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.data.remote.QuizQuestion
import com.lichso.app.data.remote.SessionResult
import com.lichso.app.data.remote.SubmitAnswerResult
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun QuizResultScreen(
    onBackClick: () -> Unit = {},
    onAskAi: (String) -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val quizState by viewModel.quizState.collectAsState()
    val finishedState = quizState as? QuizState.Finished

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        viewModel.resetToIdle()
                        viewModel.loadDailyQuiz()
                        onBackClick()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Chơi lại",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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

// ── Points chips ──

@Composable
private fun PointsChips(result: SessionResult) {
    val c = LichSoThemeColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (result.pointsEarned > 0) {
            PointChip(label = "+${result.pointsEarned} điểm", color = c.gold)
        }
        if (result.bonusPoints > 0) {
            PointChip(label = "+${result.bonusPoints} bonus", color = c.teal)
        }
    }
}

@Composable
private fun PointChip(label: String, color: Color) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        )
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
