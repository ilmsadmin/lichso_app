package com.lichso.app.feature.points.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.R
import com.lichso.app.feature.points.domain.*
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * LedgerScreen — minh bạch mọi giao dịch ⚡/☯️ của user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    onBackClick: () -> Unit,
    vm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val balance by vm.balance.collectAsState()
    val streak by vm.streak.collectAsState()
    val logs by vm.recentLogs.collectAsState()
    var filter by remember { mutableStateOf(LedgerFilter.ALL) }

    val filtered = remember(logs, filter) {
        when (filter) {
            LedgerFilter.ALL -> logs
            LedgerFilter.EARN -> logs.filter { it.dailyPointsAwarded > 0 }
            LedgerFilter.KARMA -> logs.filter { it.permanentPointsAwarded > 0 }
            LedgerFilter.STREAK -> logs.filter { it.action?.category == ActionCategory.ENGAGEMENT || it.action?.category == ActionCategory.MILESTONE }
        }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.epochDay }.toSortedMap(compareByDescending { it }) }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF4A1010))
                            else listOf(c.primary, c.deepRed)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Nhật ký điểm",
                            style = TextStyle(color = Color.White, fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        )
                        Text(
                            "Công đức · Streak · Chi tiêu",
                            style = TextStyle(color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PointsPillContent(balance = balance, onClick = {})
                    Spacer(Modifier.weight(1f))
                    StreakBadgeContent(streak = streak)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                SummaryRow(
                    daily = balance.daily,
                    spent = balance.spentDaily,
                    perm = balance.permanent,
                )
            }
            item {
                FilterRow(current = filter, onChange = { filter = it })
            }
            grouped.forEach { (epochDay, dayLogs) ->
                item {
                    DayHeader(epochDay = epochDay, logs = dayLogs)
                }
                items(dayLogs) { entry ->
                    LedgerEntryRow(entry = entry)
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Chưa có giao dịch nào.\nTích cực hoạt động để kiếm điểm nhé!",
                            style = TextStyle(fontSize = 13.sp, color = c.textTertiary),
                        )
                    }
                }
            }
        }
    }
}

private enum class LedgerFilter(val label: String) {
    ALL("Tất cả"), EARN("Kiếm ⚡"), KARMA("Nghiệp ☯️"), STREAK("Streak 🔥");
}

@Composable
private fun SummaryRow(daily: Int, spent: Int, perm: Long) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SummaryTile(
            value = "$daily",
            label = "⚡ HÔM NAY",
            iconRes = R.drawable.ic_bolt,
            tint = c.gold,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            value = "-$spent",
            label = "⚡ CHI TIÊU",
            iconRes = R.drawable.ic_lock_open,
            tint = c.primary,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            value = formatCompact(perm),
            label = "☯️ TÍCH LUỸ",
            iconRes = R.drawable.ic_yin_yang,
            tint = c.goodGreen,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(
    value: String,
    label: String,
    iconRes: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = tint))
        Text(label, style = TextStyle(fontSize = 9.sp, color = c.textTertiary, letterSpacing = 0.5.sp))
    }
}

@Composable
private fun FilterRow(current: LedgerFilter, onChange: (LedgerFilter) -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScrollSafe()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LedgerFilter.entries.forEach { f ->
            val active = f == current
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (active) c.primary else c.surface)
                    .border(1.dp, if (active) Color.Transparent else c.outlineVariant, CircleShape)
                    .clickable { onChange(f) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    f.label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else c.textSecondary,
                    ),
                )
            }
        }
    }
}

private fun Modifier.horizontalScrollSafe() = this   // placeholder; keep row simple

@Composable
private fun DayHeader(epochDay: Long, logs: List<LedgerEntry>) {
    val c = LichSoThemeColors.current
    val date = LocalDate.ofEpochDay(epochDay)
    val today = LocalDate.now()
    val label = when (epochDay) {
        today.toEpochDay() -> "Hôm nay"
        today.minusDays(1).toEpochDay() -> "Hôm qua"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, d/M"))
    }
    val dailySum = logs.sumOf { it.dailyPointsAwarded }
    val permSum = logs.sumOf { it.permanentPointsAwarded }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
        )
        Text(
            "+$dailySum⚡  +$permSum☯️",
            style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
        )
    }
}

@Composable
private fun LedgerEntryRow(entry: LedgerEntry) {
    val c = LichSoThemeColors.current
    val iconRes = when (entry.action?.category) {
        ActionCategory.ENGAGEMENT -> R.drawable.ic_flame
        ActionCategory.NAVIGATION -> R.drawable.ic_sparkle
        ActionCategory.DEEP -> R.drawable.ic_check_circle
        ActionCategory.VIRAL -> R.drawable.ic_trophy
        ActionCategory.AD -> R.drawable.ic_bolt
        ActionCategory.LOCATION -> R.drawable.ic_scroll
        ActionCategory.MILESTONE -> R.drawable.ic_crown
        null -> R.drawable.ic_ledger
    }
    val bgColor = when (entry.action?.category) {
        ActionCategory.MILESTONE -> c.gold
        ActionCategory.ENGAGEMENT -> c.primary
        ActionCategory.VIRAL -> c.goodGreen
        else -> c.teal
    }
    val timeStr = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = bgColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                entry.action?.label ?: entry.rawActionType,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(timeStr, style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                if (entry.streakMultiplierApplied > 1f) {
                    Text(
                        "x${"%.1f".format(entry.streakMultiplierApplied)}",
                        style = TextStyle(fontSize = 10.sp, color = c.gold, fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (entry.dailyPointsAwarded > 0) {
                Text(
                    "+${entry.dailyPointsAwarded}⚡",
                    style = TextStyle(fontSize = 13.sp, color = c.gold, fontWeight = FontWeight.Bold),
                )
            }
            if (entry.permanentPointsAwarded > 0) {
                Text(
                    "+${entry.permanentPointsAwarded}☯️",
                    style = TextStyle(fontSize = 12.sp, color = c.goodGreen, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}
