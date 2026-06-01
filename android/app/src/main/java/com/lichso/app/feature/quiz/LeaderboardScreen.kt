package com.lichso.app.feature.quiz

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.data.remote.LeaderboardEntry
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

private val periodLabels = listOf(
    "weekly" to "Tuần này",
    "monthly" to "Tháng này",
    "alltime" to "Toàn thời gian",
)

@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val leaderboard by viewModel.leaderboard.collectAsState()
    val myRank by viewModel.myRank.collectAsState()

    var selectedPeriodIndex by remember { mutableIntStateOf(0) }
    val selectedPeriod = periodLabels[selectedPeriodIndex].first

    LaunchedEffect(selectedPeriod) {
        viewModel.loadLeaderboard(selectedPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground(c.bg)
    ) {
        AppTopBar(
            title = "Bảng xếp hạng",
            subtitle = "Top người chơi quiz",
            onBackClick = onBackClick,
        )

        // Period tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            periodLabels.forEachIndexed { index, (_, label) ->
                val isSelected = index == selectedPeriodIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) c.primary else c.surfaceContainer
                        )
                        .border(
                            1.dp,
                            if (isSelected) c.primary else c.outlineVariant,
                            RoundedCornerShape(20.dp),
                        )
                        .clickable { selectedPeriodIndex = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else c.textSecondary,
                        )
                    )
                }
            }
        }

        // My rank banner (if available)
        myRank?.let { rank ->
            MyRankBanner(rank = rank.rank, score = rank.weekScore, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        val entries = leaderboard?.entries ?: emptyList()
        if (entries.isEmpty() && leaderboard == null) {
            // Loading shimmer
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = c.primary)
            }
        } else if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Filled.Leaderboard,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        "Chưa có dữ liệu",
                        style = TextStyle(fontSize = 14.sp, color = c.textSecondary),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(entries) { _, entry ->
                    LeaderboardRow(entry = entry)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ── My rank banner ──

@Composable
private fun MyRankBanner(rank: Int, score: Int, modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(c.primary.copy(alpha = 0.15f), c.teal.copy(alpha = 0.10f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, 0f),
                )
            )
            .border(1.dp, c.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
            Text(
                "Hạng của bạn: #$rank",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
            )
        }
        Text(
            "$score điểm",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.primary),
        )
    }
}

// ── Leaderboard row ──

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val c = LichSoThemeColors.current
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> c.textTertiary
    }
    val rankIcon = when (entry.rank) {
        1 -> Icons.Filled.EmojiEvents
        2 -> Icons.Filled.EmojiEvents
        3 -> Icons.Filled.EmojiEvents
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            if (rankIcon != null) {
                Icon(rankIcon, contentDescription = null, tint = rankColor, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    "#${entry.rank}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor,
                    )
                )
            }
        }

        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(c.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                entry.displayName.take(1).uppercase(),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.primary,
                )
            )
        }

        // Name + score
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.displayName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                ),
                maxLines = 1,
            )
            Text(
                "${entry.weekScore} điểm tuần",
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
            )
        }

        // Total score chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(rankColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                "${entry.totalScore}",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankColor,
                )
            )
        }
    }
}
