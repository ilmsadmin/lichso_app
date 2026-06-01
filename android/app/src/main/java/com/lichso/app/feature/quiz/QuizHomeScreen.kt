package com.lichso.app.feature.quiz

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LoginNudgeBanner
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Category data ──

private data class QuizCategory(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val gradient: List<Color>,
)

private val quizCategories = listOf(
    QuizCategory("history_vn", "Lịch sử VN", Icons.Filled.HistoryEdu, listOf(Color(0xFF8B0000), Color(0xFFD32F2F))),
    QuizCategory("history_world", "Thế giới", Icons.Filled.Public, listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
    QuizCategory("culture", "Văn hóa", Icons.AutoMirrored.Filled.MenuBook, listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))),
    QuizCategory("geography", "Địa lý", Icons.Filled.Place, listOf(Color(0xFFE65100), Color(0xFFFFB74D))),
    QuizCategory("general", "Tổng hợp", Icons.Filled.Lightbulb, listOf(Color(0xFF00695C), Color(0xFF4DB6AC))),
)

@Composable
fun QuizHomeScreen(
    onBackClick: () -> Unit = {},
    onStartDaily: () -> Unit = {},
    onStartTopic: (String) -> Unit = {},
    onLeaderboard: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val homeState by viewModel.homeState.collectAsState()
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    LaunchedEffect(Unit) {
        viewModel.loadHomeOverview(quizCategories.map { it.id })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground(c.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Đố Vui",
                subtitle = "Thi đấu và tích điểm thưởng",
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Daily Quiz card ──
                DailyQuizCard(
                    date = today.format(formatter),
                    questionCount = homeState.dailyQuestionCount,
                    onStart = onStartDaily,
                )

                GameplayRulesCard()

                // ── Category grid ──
                SectionHeader(title = "Chọn chủ đề", icon = Icons.Filled.Category)
                CategoryGrid(
                    categories = quizCategories,
                    counts = homeState.categoryQuestionCounts,
                    onCategoryClick = onStartTopic,
                )

                // ── Leaderboard button ──
                SectionHeader(title = "Bảng xếp hạng", icon = Icons.Filled.Leaderboard)
                LeaderboardButton(onClick = onLeaderboard)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        LoginNudgeBanner(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Daily Quiz card ──

@Composable
private fun DailyQuizCard(date: String, questionCount: Int?, onStart: () -> Unit) {
    val c = LichSoThemeColors.current
    val countText = questionCount?.let { "$it câu" } ?: "Đang tải"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (c.isDark) listOf(Color(0xFF7F1D1D), Color(0xFF4A1010))
                    else listOf(c.primary, c.deepRed),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .clickable(onClick = onStart)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Filled.Today,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        "Bộ đề hôm nay",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    )
                }
                Text(
                    "$countText • $date",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        "Trả lời đúng để leo hạng",
                        style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Bắt đầu",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

// ── Category grid ──

@Composable
private fun CategoryGrid(
    categories: List<QuizCategory>,
    counts: Map<String, Int>,
    onCategoryClick: (String) -> Unit,
) {
    val rows = categories.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { cat ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            category = cat,
                            count = counts[cat.id],
                            onClick = { onCategoryClick(cat.id) },
                        )
                    }
                }
                repeat(3 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: QuizCategory, count: Int?, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    val enabled = count == null || count > 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = category.gradient,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                category.icon,
                contentDescription = category.label,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.45f),
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            category.label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) c.textPrimary else c.textTertiary,
            ),
            maxLines = 1,
        )
        Text(
            count?.let { "$it câu" } ?: "...",
            style = TextStyle(
                fontSize = 10.sp,
                color = if (enabled) c.textSecondary else c.textTertiary,
            ),
            maxLines = 1,
        )
    }
}

// ── Leaderboard button ──

@Composable
private fun LeaderboardButton(onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFD4A017), Color(0xFFEF6C00)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Leaderboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    "Bảng xếp hạng",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                )
                Text(
                    "Top người chơi tuần này",
                    style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                )
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = c.outline,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Section header ──

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = c.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
        )
    }
}

@Composable
private fun GameplayRulesCard() {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Rule,
                contentDescription = null,
                tint = c.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Luật chơi nhanh",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
            )
        }
        Text(
            "Mỗi câu có 30 giây. Đúng được tính điểm leo bảng xếp hạng; điểm ngày dùng để mua trợ giúp khi bí.",
            style = TextStyle(fontSize = 12.sp, color = c.textSecondary, lineHeight = 18.sp),
        )
    }
}
