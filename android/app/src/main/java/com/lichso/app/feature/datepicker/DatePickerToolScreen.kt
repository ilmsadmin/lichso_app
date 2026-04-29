package com.lichso.app.feature.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.feature.points.domain.DailyUnlockKey
import com.lichso.app.feature.points.domain.PermanentRank
import com.lichso.app.feature.points.domain.PermanentUnlockKey
import com.lichso.app.feature.points.domain.SpendResult
import com.lichso.app.feature.points.ui.PointsViewModel
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DatePickerToolScreen(
    onBackClick: () -> Unit = {},
    viewModel: DatePickerToolViewModel = hiltViewModel(),
    pointsVm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val balance by pointsVm.balance.collectAsState()
    val permUnlocks by pointsVm.permanentUnlocks.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hasPermUnlock = PermanentUnlockKey.CHOOSE_DAY_TOOL_PERM.name in permUnlocks ||
        balance.rank.ordinal >= PermanentRank.DAO_SI.ordinal
    val cost = DailyUnlockKey.CHOOSE_DAY_TOOL.cost
    val canUnlockToday = balance.daily >= cost

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Công cụ chọn ngày", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.textPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = c.bg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // ── Hero ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B0000), Color(0xFFD4A017)),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        "💎 Chọn ngày đẹp",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "So sánh top 5 ngày đẹp nhất theo mục đích, hợp tuổi, không phạm sát.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFFFFEEDB)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            hasPermUnlock -> "✓ Bạn đã đạt cấp Đạo sĩ — dùng không giới hạn"
                            canUnlockToday -> "Tiêu $cost ⚡ điểm ngày để mở 1 lần"
                            else -> "Cần $cost ⚡ (đang có ${balance.daily} ⚡)"
                        },
                        style = TextStyle(fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Mục đích ──
            SectionLabel("Mục đích")
            Spacer(Modifier.height(8.dp))
            FlowRow {
                DatePurpose.entries.forEach { p ->
                    val selected = state.purpose == p
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, bottom = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) c.primary else c.surfaceContainer)
                            .border(
                                1.dp,
                                if (selected) c.primary else c.outlineVariant,
                                RoundedCornerShape(20.dp),
                            )
                            .clickable { viewModel.setPurpose(p) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "${p.emoji} ${p.displayName}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else c.textPrimary,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Tuổi (năm sinh) ──
            SectionLabel("Tuổi của người chính (năm sinh dương lịch)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.birthYearText,
                onValueChange = viewModel::setBirthYearText,
                placeholder = { Text("Ví dụ: 1990 (để trống = bỏ qua xung tuổi)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // ── Khoảng thời gian ──
            SectionLabel("Khoảng thời gian tìm kiếm")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90, 180).forEach { days ->
                    val selected = state.rangeDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) c.primary else c.surfaceContainer)
                            .border(
                                1.dp,
                                if (selected) c.primary else c.outlineVariant,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { viewModel.setRangeDays(days) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$days ngày",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else c.textPrimary,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CTA ──
            Button(
                onClick = {
                    if (hasPermUnlock) {
                        viewModel.runScoring()
                    } else {
                        pointsVm.spendDaily(DailyUnlockKey.CHOOSE_DAY_TOOL) { result ->
                            when (result) {
                                is SpendResult.Success, SpendResult.AlreadyUnlocked -> viewModel.runScoring()
                                is SpendResult.InsufficientPoints -> scope.launch {
                                    snackbarHostState.showSnackbar("Còn thiếu ${result.needed} ⚡")
                                }
                            }
                        }
                    }
                },
                enabled = !state.isLoading && (hasPermUnlock || canUnlockToday),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isLoading) "Đang tìm..." else "Tìm top 5 ngày đẹp",
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Results ──
            if (state.results.isNotEmpty()) {
                SectionLabel("Top ${state.results.size} ngày đẹp nhất")
                Spacer(Modifier.height(10.dp))
                state.results.forEachIndexed { idx, candidate ->
                    CandidateDayCard(rank = idx + 1, candidate = candidate)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text,
        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
    )
}

@Composable
private fun CandidateDayCard(rank: Int, candidate: CandidateDay) {
    val c = LichSoThemeColors.current
    val info = candidate.info
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "  "
    }
    val ratingColor = when {
        candidate.score >= 130 -> Color(0xFF2E7D32)
        candidate.score >= 100 -> Color(0xFF558B2F)
        candidate.score >= 70 -> Color(0xFFF9A825)
        else -> Color(0xFFE65100)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.5.dp, ratingColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$medal #${rank}",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = ratingColor),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${"%02d".format(info.solar.dd)}/${"%02d".format(info.solar.mm)}/${info.solar.yy} · ${info.dayOfWeek}",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                    )
                    Text(
                        "Âm: ${info.lunar.day}/${info.lunar.month} · ${info.dayCanChi} · ${info.dayRating.label}",
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ratingColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "${candidate.score}đ",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }

            if (candidate.reasons.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                candidate.reasons.forEach { reason ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            reason,
                            style = TextStyle(fontSize = 12.sp, color = c.textPrimary),
                        )
                    }
                }
            }
            if (candidate.warnings.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                candidate.warnings.forEach { w ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            w,
                            style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                        )
                    }
                }
            }

            if (info.gioHoangDao.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Giờ hoàng đạo: " + info.gioHoangDao.joinToString(" · ") { "${it.name} (${it.time})" },
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                )
            }
        }
    }
}

/** Simple flow-row replacement for Compose without dependency on Material accompanist. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}
