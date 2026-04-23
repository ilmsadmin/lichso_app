package com.lichso.app.feature.points.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.R
import com.lichso.app.feature.points.domain.PermanentRank
import com.lichso.app.feature.points.domain.PointsBalance
import com.lichso.app.feature.points.domain.StreakState
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Compact pill showing ⚡ daily / ☯️ permanent + rank.
 * Designed to slot into the red Home header or any toolbar row.
 * Tap → opens onClick (typically LedgerScreen).
 */
@Composable
fun PointsPill(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    vm: PointsViewModel = hiltViewModel(),
) {
    val balance by vm.balance.collectAsState()
    PointsPillContent(balance = balance, onClick = onClick, modifier = modifier)
}

@Composable
fun PointsPillContent(
    balance: PointsBalance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ⚡ daily
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_bolt),
                contentDescription = "Điểm ngày",
                tint = c.gold,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = balance.daily.toString(),
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(12.dp)
                .background(Color.White.copy(alpha = 0.28f))
        )

        // ☯️ permanent
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_yin_yang),
                contentDescription = "Điểm vĩnh viễn",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = formatCompact(balance.permanent),
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            )
        }
    }
}

/**
 * Streak badge — chuỗi ngày + tier + freeze tokens.
 * Used in Profile & Home.
 */
@Composable
fun StreakBadge(
    modifier: Modifier = Modifier,
    vm: PointsViewModel = hiltViewModel(),
) {
    val streak by vm.streak.collectAsState()
    StreakBadgeContent(streak = streak, modifier = modifier)
}

@Composable
fun StreakBadgeContent(streak: StreakState, modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current
    val bg = if (streak.current > 0)
        Brush.linearGradient(listOf(Color(0xFFF57F17), Color(0xFFB71C1C)))
    else
        Brush.linearGradient(listOf(c.outline, c.outline))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_streak),
            contentDescription = "Streak",
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Text(
            "${streak.current}",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp),
        )
        Text(
            streak.tier.displayName,
            style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp),
        )
        if (streak.freezeTokens > 0) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_snowflake),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(streak.freezeTokens.toString(),
                        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * Big rank progress — shown on Profile / Home hero card.
 */
@Composable
fun RankProgressCard(
    balance: PointsBalance,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
            .clickable { onTap() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_yin_yang),
                contentDescription = null,
                tint = c.gold,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Bậc công đức",
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                )
                Text(
                    balance.rank.displayName,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                )
            }
            Text(
                formatCompact(balance.permanent),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.gold),
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { balance.progressToNextRank },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = c.gold,
            trackColor = c.outlineVariant.copy(alpha = 0.4f),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Spacer(Modifier.height(6.dp))
        val nextLabel = balance.nextRank?.let {
            "Còn ${formatCompact(balance.pointsToNextRank)} → ${it.displayName}"
        } ?: "Đã đạt bậc cao nhất"
        Text(nextLabel, style = TextStyle(fontSize = 11.sp, color = c.textSecondary))
    }
}

internal fun formatCompact(value: Long): String = when {
    value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0).removeSuffix(".0M") + "M"
    value >= 1_000 -> String.format("%.1fK", value / 1_000.0).removeSuffix(".0K") + "K"
    else -> value.toString()
}

internal fun formatCompact(value: Int): String = formatCompact(value.toLong())
