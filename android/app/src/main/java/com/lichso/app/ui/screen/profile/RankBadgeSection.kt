package com.lichso.app.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.feature.points.domain.PermanentRank
import com.lichso.app.feature.points.domain.PermanentUnlockKey
import com.lichso.app.feature.points.ui.PointsViewModel
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Hiển thị cấp bậc, streak tier, và bộ huy hiệu vĩnh viễn user đã đạt được.
 * Phase 2 — "Milestone vĩnh viễn + Badges + Avatar frames".
 */
@Composable
internal fun RankBadgeSection(
    pointsVm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val balance by pointsVm.balance.collectAsState()
    val streak by pointsVm.streak.collectAsState()
    val permUnlocks by pointsVm.permanentUnlocks.collectAsState()

    val rank = balance.rank
    val nextRank = balance.nextRank
    val gradient = rankGradient(rank)

    // ── Header card ──
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient))
            .padding(16.dp),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cấp bậc: ${rank.displayName}",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                    Text(
                        "${balance.permanent} ☯️ tích lũy" +
                            (nextRank?.let { " · cần ${balance.pointsToNextRank} ☯️ → ${it.displayName}" } ?: " · cấp tối đa"),
                        style = TextStyle(fontSize = 12.sp, color = Color(0xFFFFEEDB)),
                    )
                }
            }
            if (nextRank != null) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { balance.progressToNextRank },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Streak tier chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Streak ${streak.current} ngày · ${streak.tier.displayName} (x${streak.tier.dailyMultiplier})",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // ── Permanent unlocks (Badges) ──
    Text(
        "Huy hiệu đã mở (${permUnlocks.size}/${PermanentUnlockKey.entries.size})",
        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
    )
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PermanentUnlockKey.entries.toList()) { key ->
            BadgeChip(
                key = key,
                owned = key.name in permUnlocks,
            )
        }
    }
}

@Composable
private fun BadgeChip(key: PermanentUnlockKey, owned: Boolean) {
    val c = LichSoThemeColors.current
    val accent = if (owned) Color(0xFFD4A017) else c.outline.copy(alpha = 0.4f)
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(
                width = if (owned) 1.5.dp else 1.dp,
                color = accent,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (owned) accent.copy(alpha = 0.15f) else c.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (owned) Icons.Filled.Verified else Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = if (owned) accent else c.textTertiary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            key.label,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (owned) c.textPrimary else c.textTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
            ),
            maxLines = 3,
        )
    }
}

/** Trả về gradient phù hợp với rank — dùng cho avatar frame & header. */
internal fun rankGradient(rank: PermanentRank): List<Color> = when (rank) {
    PermanentRank.NOVICE     -> listOf(Color(0xFF607D8B), Color(0xFF455A64))
    PermanentRank.SO_CO      -> listOf(Color(0xFF8D6E63), Color(0xFF5D4037)) // bronze
    PermanentRank.TU_TAP     -> listOf(Color(0xFFB0BEC5), Color(0xFF78909C)) // silver
    PermanentRank.THONG_THAO -> listOf(Color(0xFFFFB300), Color(0xFFFF6F00)) // gold
    PermanentRank.DAO_SI     -> listOf(Color(0xFF26A69A), Color(0xFF00695C)) // teal
    PermanentRank.CHAN_NHAN  -> listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)) // purple
    PermanentRank.THIEN_SU   -> listOf(Color(0xFFFFD700), Color(0xFFD32F2F)) // legendary
}
