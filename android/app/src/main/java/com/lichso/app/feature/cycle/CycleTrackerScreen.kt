package com.lichso.app.feature.cycle

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.CycleLogEntity
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoCard
import com.lichso.app.ui.components.LichSoDatePickerField
import com.lichso.app.ui.components.LichSoDeleteButton
import com.lichso.app.ui.components.LichSoDialogSurface
import com.lichso.app.ui.components.LichSoEmptyState
import com.lichso.app.ui.components.LichSoHeaderActionButton
import com.lichso.app.ui.components.LichSoPrimaryButton
import com.lichso.app.ui.components.LichSoSecondaryButton
import com.lichso.app.ui.components.LichSoSectionHeader
import com.lichso.app.ui.components.LichSoTextField
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DATE_F = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun CycleTrackerScreen(
    onBackClick: () -> Unit = {},
    viewModel: CycleTrackerViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val today = LocalDate.now()
    val nextPeriodStart = state.lastPeriodStart.plusDays(state.cycleLength.toLong())
    val ovulationDay = nextPeriodStart.minusDays(14)
    val fertileStart = ovulationDay.minusDays(4)
    val fertileEnd = ovulationDay.plusDays(1)
    val daysToNext = ChronoUnit.DAYS.between(today, nextPeriodStart)

    Column(modifier = Modifier.fillMaxSize().screenBackground(c.bg)) {
        AppTopBar(
            title = "Chu kỳ kinh nguyệt",
            subtitle = "Dự đoán · Rụng trứng · Lịch sử",
            onBackClick = onBackClick,
            actions = {
                LichSoHeaderActionButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Ghi chu kỳ mới",
                    onClick = viewModel::openAddLogDialog,
                )
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Prediction banner ────────────────────────────────────────────
            item {
                PredictionBanner(
                    daysToNext = daysToNext,
                    nextPeriodStart = nextPeriodStart,
                    ovulationDay = ovulationDay,
                )
            }

            // ── Detail cards ─────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Rụng trứng",
                        value = ovulationDay.format(DATE_F),
                        note = "Ước tính",
                        accentColor = Color(0xFFAD1457),
                        bgColor = Color(0xFFFCE4EC),
                        borderColor = Color(0xFFF48FB1),
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Cửa sổ thụ thai",
                        value = "${fertileStart.format(DateTimeFormatter.ofPattern("dd/MM"))} – ${fertileEnd.format(DateTimeFormatter.ofPattern("dd/MM"))}",
                        note = "Dễ thụ thai nhất",
                        accentColor = Color(0xFF6A1B9A),
                        bgColor = Color(0xFFF3E5F5),
                        borderColor = Color(0xFFCE93D8),
                    )
                }
            }

            // ── Settings card ─────────────────────────────────────────────────
            item {
                SettingsCard(
                    state = state,
                    onCycleLength = viewModel::setCycleLengthInput,
                    onPeriodLength = viewModel::setPeriodLengthInput,
                    onSave = viewModel::saveSettings,
                )
            }

            // ── History ───────────────────────────────────────────────────────
            item {
                LichSoSectionHeader(
                    title = "Lịch sử chu kỳ",
                    trailing = "${state.logs.size} kỳ đã ghi",
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }

            if (state.logs.isEmpty()) {
                item {
                    EmptyHistoryCard { viewModel.openAddLogDialog() }
                }
            } else {
                items(state.logs, key = { it.id }) { log ->
                    CycleLogRow(
                        log = log,
                        cycleLength = state.cycleLength,
                        onDelete = { viewModel.deleteLog(log) },
                    )
                }
            }

            item {
                Text(
                    "⚠ Đây là ước tính tham khảo, không thay thế tư vấn y khoa.",
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                )
            }
        }
    }

    if (state.showAddLogDialog) {
        AddLogDialog(
            state = state,
            onDismiss = viewModel::closeAddLogDialog,
            onDateChange = viewModel::setAddLogDate,
            onNotesChange = viewModel::setAddLogNotes,
            onConfirm = viewModel::confirmAddLog,
        )
    }
}

// ── Prediction banner ──────────────────────────────────────────────────────────

@Composable
private fun PredictionBanner(
    daysToNext: Long,
    nextPeriodStart: LocalDate,
    ovulationDay: LocalDate,
) {
    val gradientColors = when {
        daysToNext in 0..3 -> listOf(Color(0xFFC62828), Color(0xFFE53935))
        daysToNext in 4..7 -> listOf(Color(0xFFAD1457), Color(0xFFD81B60))
        else -> listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))
    }
    val countText = when {
        daysToNext > 0 -> "Còn $daysToNext ngày"
        daysToNext == 0L -> "Hôm nay"
        else -> "Đã qua ${-daysToNext} ngày"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(gradientColors), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(countText, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
        Spacer(Modifier.height(4.dp))
        Text(
            "Kỳ tiếp theo: ${nextPeriodStart.format(DATE_F)}",
            style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Rụng trứng dự kiến: ${ovulationDay.format(DATE_F)}",
            style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)),
        )
    }
}

// ── Metric card ────────────────────────────────────────────────────────────────

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    note: String,
    accentColor: Color,
    bgColor: Color,
    borderColor: Color,
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Text(title, style = TextStyle(fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(4.dp))
        Text(value, style = TextStyle(fontSize = 15.sp, color = accentColor, fontWeight = FontWeight.ExtraBold))
        Spacer(Modifier.height(2.dp))
        Text(note, style = TextStyle(fontSize = 10.sp, color = accentColor.copy(alpha = 0.65f)))
    }
}

// ── Settings card ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    state: CycleUiState,
    onCycleLength: (String) -> Unit,
    onPeriodLength: (String) -> Unit,
    onSave: () -> Unit,
) {
    LichSoCard(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val c = LichSoThemeColors.current
        Text("Thông số chu kỳ", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LichSoTextField(
                value = state.cycleLengthInput,
                onValueChange = onCycleLength,
                label = "Độ dài chu kỳ",
                suffix = { Text("ngày") },
                keyboardType = KeyboardType.Number,
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            LichSoTextField(
                value = state.periodLengthInput,
                onValueChange = onPeriodLength,
                label = "Số ngày hành kinh",
                suffix = { Text("ngày") },
                keyboardType = KeyboardType.Number,
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.settingsSaved) Color(0xFF2E7D32) else c.primary
            ),
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (state.settingsSaved) "Đã lưu ✓" else "Lưu thông số")
        }
    }
}

// ── Cycle log row ──────────────────────────────────────────────────────────────

@Composable
private fun CycleLogRow(
    log: CycleLogEntity,
    cycleLength: Int,
    onDelete: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val startDate = LocalDate.ofEpochDay(log.startEpochDay)
    val predictedEnd = startDate.plusDays(cycleLength.toLong())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFCE4EC), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("🩸", style = TextStyle(fontSize = 18.sp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Bắt đầu: ${startDate.format(DATE_F)}",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
            )
            Text(
                "Chu kỳ kế tiếp: ${predictedEnd.format(DATE_F)}",
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
            )
            if (log.notes.isNotBlank()) {
                Text(log.notes, style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
            }
        }

        LichSoDeleteButton(onClick = onDelete)
    }
}

// ── Empty history ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistoryCard(onAdd: () -> Unit) {
    LichSoCard(contentPadding = PaddingValues(8.dp)) {
        LichSoEmptyState(
            title = "Chưa có lịch sử",
            message = "Ghi lại ngày bắt đầu từng chu kỳ để theo dõi chính xác hơn",
            actionText = "Ghi chu kỳ đầu tiên",
            onAction = onAdd,
            icon = Icons.Outlined.Info,
        )
    }
}

// ── Add log dialog ─────────────────────────────────────────────────────────────

@Composable
private fun AddLogDialog(
    state: CycleUiState,
    onDismiss: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    LichSoDialogSurface(onDismiss = onDismiss, title = "Ghi chu kỳ mới") {
            LichSoDatePickerField(
                label = "Ngày bắt đầu",
                date = state.addLogDateInput,
                onDateChange = onDateChange,
                formatter = DATE_F,
                icon = Icons.Filled.CalendarToday,
            )

            LichSoTextField(
                value = state.addLogNotesInput,
                onValueChange = onNotesChange,
                label = "Ghi chú (tuỳ chọn)",
                placeholder = "Ví dụ: chu kỳ đều, đau bụng nhẹ...",
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LichSoSecondaryButton(text = "Huỷ", onClick = onDismiss, modifier = Modifier.weight(1f))
                LichSoPrimaryButton(text = "Lưu", onClick = onConfirm, modifier = Modifier.weight(1f))
            }
    }
}
