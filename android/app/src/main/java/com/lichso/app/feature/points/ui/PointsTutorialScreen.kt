package com.lichso.app.feature.points.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.feature.points.domain.ActionCategory
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Màn hình hướng dẫn tân thủ cách kiếm điểm.
 * Hiển thị toàn bộ ActionType có sẵn, gom nhóm theo độ dễ:
 *   - 🟢 Hằng ngày dễ (ENGAGEMENT + NAVIGATION cap nhỏ)
 *   - 🟡 Hoạt động sâu (DEEP)
 *   - 🔴 Phần thưởng lớn (VIRAL + MILESTONE)
 *
 * Mỗi card click → deeplink tương ứng (gọi onAction).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsTutorialScreen(
    onBack: () -> Unit,
    onAction: (ActionType) -> Unit,
    vm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val balance by vm.balance.collectAsState()
    var rewardClaimed by remember { mutableStateOf(false) }

    // Trao 100☯ khi user lần đầu mở tutorial (cap 1/day, COMPLETE_TUTORIAL)
    LaunchedEffect(Unit) {
        if (!rewardClaimed) {
            vm.award(ActionType.COMPLETE_TUTORIAL)
            rewardClaimed = true
        }
    }

    val easyActions = remember {
        listOf(
            ActionType.DAILY_CHECK_IN,
            ActionType.VIEW_FORTUNE_CARD,
            ActionType.VIEW_DAY_DETAIL,
            ActionType.VISIT_LUNAR_CALENDAR,
            ActionType.VISIT_VAN_KHAN,
            ActionType.VISIT_TU_VI,
            ActionType.VISIT_TOOLS,
            ActionType.USE_LUNAR_CONVERTER,
            ActionType.USE_ZODIAC_COMPAT,
            ActionType.VIEW_TIET_KHI,
            ActionType.VIEW_HISTORY_TODAY,
            ActionType.VIEW_LEDGER,
            ActionType.VIEW_PROFILE,
            ActionType.VIEW_DAILY_STORE,
        )
    }
    val mediumActions = remember {
        listOf(
            ActionType.DRAW_KINH_DICH,
            ActionType.CHAT_AI_MESSAGE,
            ActionType.CREATE_REMINDER,
            ActionType.COMPLETE_REMINDER,
            ActionType.ADD_BOOKMARK,
            ActionType.READ_VAN_KHAN_FULL,
            ActionType.USE_DATE_PICKER,
        )
    }
    val bigActions = remember {
        ActionType.entries.filter {
            it.category == ActionCategory.VIRAL || it.category == ActionCategory.MILESTONE
        }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hướng dẫn kiếm điểm",
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = c.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Hero ──
            HeroCard(
                ledgerDaily = balance.daily,
                ledgerPermanent = balance.permanent,
            )

            // ── Giải thích ⚡ vs ☯️ ──
            ExplainCard()

            // ── Hằng ngày dễ ──
            SectionHeader("Hằng ngày — Dễ dàng", "Điểm danh + lướt app cũng kiếm 50–80 điểm/ngày", Color(0xFF66BB6A))
            ActionListCard(easyActions, onAction)

            // ── Hoạt động sâu ──
            SectionHeader("Hoạt động sâu", "Tương tác với app, dùng tính năng nâng cao", Color(0xFFFFCA28))
            ActionListCard(mediumActions, onAction)

            // ── Phần thưởng lớn ──
            SectionHeader("Phần thưởng lớn", "Mời bạn bè, đánh giá app, mốc streak...", Color(0xFFEF5350))
            ActionListCard(bigActions, onAction)

            // ── Tip cuối ──
            TipCard()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroCard(ledgerDaily: Int, ledgerPermanent: Long) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(c.gold.copy(alpha = 0.15f), c.teal.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, c.border, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = c.gold,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Cách kiếm điểm Lịch Số",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
            }
            Text(
                "Mỗi lần điểm danh, lướt lịch hay rút quẻ — bạn đều nhận điểm thưởng. " +
                        "Tích đủ điểm để mở khoá tính năng & lên cấp đạo sĩ!",
                fontSize = 14.sp,
                color = c.textSecondary,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("$ledgerDaily", "Điểm hôm nay", c.gold, iconRes = com.lichso.app.R.drawable.ic_bolt)
                StatPill("$ledgerPermanent", "Điểm vĩnh viễn", c.teal, iconRes = com.lichso.app.R.drawable.ic_yin_yang)
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, accent: Color, iconRes: Int? = null) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        if (iconRes != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accent)
            }
        } else {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accent)
        }
        Text(label, fontSize = 11.sp, color = c.textTertiary)
    }
}

@Composable
private fun ExplainCard() {
    val c = LichSoThemeColors.current
    Surface(
        color = c.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, c.border),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = c.gold)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Điểm ngày — reset 00:00 mỗi ngày",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                )
            }
            Text(
                "Dùng để mở khoá tính năng trong ngày: bộ lọc lịch, chủ đề, AI chat...",
                fontSize = 13.sp,
                color = c.textSecondary,
                lineHeight = 18.sp,
            )
            HorizontalDivider(color = c.border)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = c.teal)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Điểm vĩnh viễn — không bao giờ mất",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                )
            }
            Text(
                "Tích để lên cấp Đạo Sĩ → Pháp Sư → Đại Sư → Thiên Sư, mở khoá vĩnh viễn các tính năng cao cấp.",
                fontSize = 13.sp,
                color = c.textSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, dotColor: Color = Color.Transparent) {
    val c = LichSoThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (dotColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(dotColor),
                )
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        }
        Text(subtitle, fontSize = 12.sp, color = c.textTertiary)
    }
}

@Composable
private fun ActionListCard(actions: List<ActionType>, onAction: (ActionType) -> Unit) {
    val c = LichSoThemeColors.current
    Surface(
        color = c.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, c.border),
    ) {
        Column {
            actions.forEachIndexed { index, action ->
                ActionRow(action, onClick = { onAction(action) })
                if (index < actions.lastIndex) HorizontalDivider(color = c.border)
            }
        }
    }
}

@Composable
private fun ActionRow(action: ActionType, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(action.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            val capText = if (action.dailyCap == -1) "Không giới hạn" else "Tối đa ${action.dailyCap}× / ngày"
            Text(capText, fontSize = 11.sp, color = c.textTertiary)
        }
        Spacer(Modifier.width(8.dp))
        if (action.dailyPoints > 0) {
            BoltRewardChip(action.dailyPoints, c.gold)
        }
        if (action.permanentPoints > 0) {
            Spacer(Modifier.width(6.dp))
            KarmaRewardChip(action.permanentPoints, c.teal)
        }
    }
}

@Composable
private fun BoltRewardChip(points: Int, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text("+$points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = accent)
        Icon(
            painter = painterResource(com.lichso.app.R.drawable.ic_bolt),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun KarmaRewardChip(points: Int, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text("+$points", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = accent)
        Icon(
            painter = painterResource(com.lichso.app.R.drawable.ic_yin_yang),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun TipCard() {
    val c = LichSoThemeColors.current
    Surface(
        color = c.gold.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, c.gold.copy(alpha = 0.3f)),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = c.gold,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Streak nhân hệ số!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
                Text(
                    "Điểm danh đều mỗi ngày để được hệ số nhân điểm × tăng dần (đến ×3 ở 30 ngày).",
                    fontSize = 12.sp,
                    color = c.textSecondary,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
