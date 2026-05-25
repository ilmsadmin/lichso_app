package com.lichso.app.feature.datemath

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoNullableDatePickerField
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DateMathScreen(
    onBackClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tuổi", "Khoảng cách", "Cộng / Trừ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Máy tính ngày",
            subtitle = "Tuổi · Khoảng cách · Cộng/Trừ",
            onBackClick = onBackClick,
        )

        // ── Tabs ──
        TabRow(
            selectedTabIndex = tab,
            containerColor = c.bg,
            contentColor = c.primary,
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = tab == idx,
                    onClick = { tab = idx },
                    text = {
                        Text(
                            label,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = if (tab == idx) FontWeight.Bold else FontWeight.Medium,
                                color = if (tab == idx) c.primary else c.textTertiary,
                            )
                        )
                    },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (tab) {
                0 -> AgeTab()
                1 -> DiffTab()
                2 -> AddTab()
            }
        }
    }
}

// ══════════════════════════════════════════
// COMMON UI HELPERS
// ══════════════════════════════════════════

@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Chọn ngày",
) {
    LichSoNullableDatePickerField(
        label = label,
        date = date,
        onDateChange = onChange,
        modifier = modifier,
        placeholder = placeholder,
        formatter = DATE_FMT,
        icon = Icons.Filled.CalendarToday,
    )
}

@Composable
private fun ResultCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgGradient: List<Color>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(bgGradient))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(
                title,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = iconTint),
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color? = null) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
        )
        Text(
            value,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: c.textPrimary,
            )
        )
    }
}

// ══════════════════════════════════════════
// TAB 1 — TUỔI
// ══════════════════════════════════════════

@Composable
private fun AgeTab() {
    val c = LichSoThemeColors.current
    var birth by remember { mutableStateOf<LocalDate?>(null) }
    val today = remember { LocalDate.now() }

    val result = remember(birth) { birth?.let { DateMathLogic.computeAge(it, today) } }
    val fates = remember(birth) {
        birth?.let { b ->
            val lunarBirth = com.lichso.app.util.LunarCalendarUtil
                .convertSolar2Lunar(b.dayOfMonth, b.monthValue, b.year)
            DateMathLogic.upcomingYearFates(lunarBirth.lunarYear, today, count = 6)
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DatePickerField(
            label = "Ngày sinh (dương lịch)",
            date = birth,
            onChange = { birth = it },
        )

        if (result != null) {
            ResultCard(
                title = "Tuổi & quá khứ",
                icon = Icons.Filled.Cake,
                iconTint = Color(0xFFD4A017),
                bgGradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7)),
            ) {
                StatRow("Tuổi tròn", "${result.age} tuổi")
                StatRow("Tuổi mụ", "${result.ageMu} tuổi")
                StatRow(
                    "Năm sinh âm",
                    "${result.canChiYear} (${result.lunarBirthLabel})"
                )
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFFFE082).copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                val p = result.period
                StatRow("Đã sống", "${p.years}n ${p.months}th ${p.days}n")
                StatRow("Tổng ngày", "%,d".format(result.totalDays))
                StatRow("Tổng tuần", "%,d".format(result.totalWeeks))
                StatRow("Tổng giờ", "%,d".format(result.totalHours))
            }

            ResultCard(
                title = "Sinh nhật kế tiếp",
                icon = Icons.Filled.Celebration,
                iconTint = Color(0xFFC2185B),
                bgGradient = listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0).copy(alpha = 0.4f)),
            ) {
                StatRow(
                    "Sinh nhật dương",
                    "${result.nextSolarBirthday.format(DATE_FMT)} (còn ${result.daysToSolarBirthday} ngày)",
                    valueColor = Color(0xFFC2185B),
                )
                StatRow(
                    "Sinh nhật âm",
                    "${result.nextLunarBirthday.format(DATE_FMT)} (còn ${result.daysToLunarBirthday} ngày)",
                    valueColor = Color(0xFFC2185B),
                )
            }

            // Năm hạn
            if (fates.isNotEmpty()) {
                ResultCard(
                    title = "Năm hạn 6 năm tới",
                    icon = Icons.Filled.Warning,
                    iconTint = Color(0xFFE65100),
                    bgGradient = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2).copy(alpha = 0.5f)),
                ) {
                    fates.forEach { f ->
                        FateRow(f)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tham khảo phong thuỷ truyền thống: Kim Lâu hại bản thân/gia đình, Tam Tai 3 năm liên tiếp xui, Hoang Ốc cảnh báo làm nhà.",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                    )
                }
            }
        } else {
            EmptyHint("Nhập ngày sinh để xem tuổi và năm hạn sắp tới.")
        }
    }
}

@Composable
private fun FateRow(f: DateMathLogic.YearFate) {
    val c = LichSoThemeColors.current
    val hasBad = f.isKimLau || f.isTamTai || f.isHoangOcBad
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                if (hasBad) Color(0xFFFFEBEE) else Color.White.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${f.solarYear} · ${f.canChi}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
            )
            Text(
                "Tuổi mụ ${f.ageMu}",
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
            )
        }
        Spacer(Modifier.height(4.dp))
        if (f.isKimLau) Tag("• ${f.kimLauName}", Color(0xFFC62828))
        if (f.isTamTai) Tag("• ${f.tamTaiNote}", Color(0xFFE65100))
        Tag(
            "• Hoang Ốc: ${f.hoangOcName}",
            if (f.isHoangOcBad) Color(0xFFC62828) else Color(0xFF2E7D32)
        )
        if (!hasBad) Tag("✓ Năm bình thường", Color(0xFF2E7D32))
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(
        text,
        style = TextStyle(fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium),
    )
}

// ══════════════════════════════════════════
// TAB 2 — KHOẢNG CÁCH
// ══════════════════════════════════════════

@Composable
private fun DiffTab() {
    val c = LichSoThemeColors.current
    var d1 by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var d2 by remember { mutableStateOf<LocalDate?>(null) }

    val result = remember(d1, d2) {
        val from = d1
        val to = d2
        if (from != null && to != null) DateMathLogic.computeDiff(from, to) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(
                label = "Từ ngày",
                date = d1,
                onChange = { d1 = it },
                modifier = Modifier.weight(1f),
            )
            DatePickerField(
                label = "Đến ngày",
                date = d2,
                onChange = { d2 = it },
                modifier = Modifier.weight(1f),
            )
        }

        if (result != null) {
            ResultCard(
                title = "Khoảng cách",
                icon = Icons.Filled.SwapVert,
                iconTint = Color(0xFF1565C0),
                bgGradient = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB).copy(alpha = 0.4f)),
            ) {
                val p = result.period
                StatRow("Số ngày", "%,d ngày".format(result.totalDays))
                StatRow("Số tuần", "%,d tuần".format(result.totalWeeks))
                StatRow(
                    "Quy đổi",
                    "${p.years}n ${p.months}th ${p.days}ng"
                )
                StatRow("Từ", result.from.format(DATE_FMT))
                StatRow("Đến", result.to.format(DATE_FMT))
            }

            if (result.workdays >= 0) {
                ResultCard(
                    title = "Phân loại ngày",
                    icon = Icons.Filled.WorkOutline,
                    iconTint = Color(0xFF6A1B9A),
                    bgGradient = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7).copy(alpha = 0.4f)),
                ) {
                    StatRow(
                        "Ngày làm việc",
                        "%,d ngày".format(result.workdays),
                        valueColor = Color(0xFF2E7D32),
                    )
                    StatRow(
                        "Cuối tuần",
                        "%,d ngày".format(result.weekendDays),
                        valueColor = Color(0xFFE65100),
                    )
                    StatRow(
                        "Lễ Tết VN",
                        "${result.holidayCount} ngày",
                        valueColor = Color(0xFFC62828),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Loại trừ T7/CN và lễ chính: 1/1, 30/4, 1/5, 2/9, Tết Nguyên Đán (5 ngày), Giỗ Tổ.",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                    )
                }
            } else {
                Text(
                    "Khoảng cách quá lớn (>5 năm) — không hiển thị chi tiết ngày làm việc.",
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                )
            }
        } else {
            EmptyHint("Chọn 2 ngày để tính khoảng cách.")
        }
    }
}

// ══════════════════════════════════════════
// TAB 3 — CỘNG / TRỪ
// ══════════════════════════════════════════

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AddTab() {
    val c = LichSoThemeColors.current
    var base by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var amountText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(DateMathLogic.DateUnit.DAY) }
    var subtract by remember { mutableStateOf(false) }

    val amount = amountText.toLongOrNull()?.coerceIn(0, 100_000) ?: 0L
    val result = remember(base, amount, unit, subtract) {
        val selectedBase = base
        if (selectedBase != null && amount > 0) {
            DateMathLogic.computeAdd(selectedBase, amount, unit, subtract)
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DatePickerField(
            label = "Ngày mốc",
            date = base,
            onChange = { base = it },
        )

        // Amount + unit
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ± toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surfaceContainer)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                listOf(false to "+", true to "−").forEach { (isSub, lbl) ->
                    val sel = subtract == isSub
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) c.primary else Color.Transparent)
                            .clickable { subtract = isSub }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            lbl,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sel) Color.White else c.textPrimary,
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> amountText = v.filter { ch -> ch.isDigit() }.take(6) },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontSize = 14.sp),
            )
        }

        // Unit selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateMathLogic.DateUnit.entries.forEach { u ->
                val sel = unit == u
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) c.primary else c.surfaceContainer)
                        .border(
                            1.dp,
                            if (sel) c.primary else c.outlineVariant,
                            RoundedCornerShape(10.dp),
                        )
                        .clickable { unit = u }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        u.label,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sel) Color.White else c.textPrimary,
                        )
                    )
                }
            }
        }

        // Funeral / cúng presets
        Text(
            "Mốc cúng giỗ phổ biến (cộng từ ngày mốc)",
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
        )
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateMathLogic.FUNERAL_PRESETS.forEach { p ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF3E0))
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(20.dp))
                        .clickable {
                            amountText = p.days.toString()
                            unit = DateMathLogic.DateUnit.DAY
                            subtract = false
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        p.label,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100),
                        )
                    )
                }
            }
        }

        AnimatedVisibility(visible = result != null) {
            result?.let { r ->
                ResultCard(
                    title = if (subtract) "Trước ${amount} ${unit.label.lowercase()}" else "Sau ${amount} ${unit.label.lowercase()}",
                    icon = Icons.Filled.EventAvailable,
                    iconTint = Color(0xFF2E7D32),
                    bgGradient = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9).copy(alpha = 0.4f)),
                ) {
                    val c2 = LichSoThemeColors.current
                    Text(
                        r.result.format(DATE_FMT),
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B5E20),
                        )
                    )
                    Text(
                        "${r.dayOfWeek} · ${r.lunarLabel} · ${r.dayCanChi}",
                        style = TextStyle(fontSize = 13.sp, color = c2.textSecondary),
                    )
                    if (r.holiday != null) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFE0B2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "📅 ${r.holiday}",
                                style = TextStyle(fontSize = 12.sp, color = Color(0xFFE65100)),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    StatRow("Chênh lệch ngày", "${if (r.days >= 0) "+" else ""}${"%,d".format(r.days)}")
                }
            }
        }

        if (result == null) {
            EmptyHint("Chọn ngày mốc và nhập số để tính.")
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text,
                style = TextStyle(fontSize = 13.sp, color = c.textTertiary),
            )
        }
    }
}
