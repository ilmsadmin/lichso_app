package com.lichso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object LichSoSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object LichSoRadius {
    val chip = 18.dp
    val field = 12.dp
    val card = 16.dp
    val dialog = 20.dp
}

object LichSoTextSize {
    val label = 12.sp
    val body = 14.sp
    val title = 15.sp
    val heading = 17.sp
}

@Composable
fun LichSoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LichSoThemeColors.current.surfaceContainer,
    borderColor: Color = LichSoThemeColors.current.outlineVariant,
    radius: Dp = LichSoRadius.card,
    contentPadding: PaddingValues = PaddingValues(LichSoSpacing.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(radius))
            .border(1.dp, borderColor, RoundedCornerShape(radius))
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun LichSoGradientCard(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    radius: Dp = LichSoRadius.dialog,
    contentPadding: PaddingValues = PaddingValues(LichSoSpacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(colors), RoundedCornerShape(radius))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun LichSoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(trailing, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
        }
    }
}

@Composable
fun LichSoHeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .background(Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun LichSoDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Xoá",
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .background(Color(0xFFFFEBEE), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = contentDescription,
            tint = Color(0xFFC62828),
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
fun LichSoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val c = LichSoThemeColors.current
    val enabledContentColor = if (c.primary.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    val disabledContentColor = c.textTertiary
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = enabledContentColor,
            disabledContainerColor = c.surfaceContainerHigh,
            disabledContentColor = disabledContentColor,
        ),
    ) {
        val contentColor = if (enabled) enabledContentColor else disabledContentColor
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = contentColor, style = TextStyle(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun LichSoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text)
    }
}

@Composable
fun LichSoTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
    }
}

@Composable
fun LichSoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    suffix: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        suffix = suffix,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(LichSoRadius.field),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun LichSoDatePickerField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    icon: ImageVector = Icons.Filled.CalendarToday,
) {
    val c = LichSoThemeColors.current
    var showPicker by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            label,
            style = TextStyle(fontSize = LichSoTextSize.label, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(LichSoRadius.field))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(LichSoRadius.field))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(17.dp))
            Text(date.format(formatter), style = TextStyle(fontSize = 14.sp, color = c.textPrimary, fontWeight = FontWeight.Medium))
        }
    }

    if (showPicker) {
        LichSoDatePickerDialog(
            initialDate = date,
            onDismiss = { showPicker = false },
            onDateSelected = {
                onDateChange(it)
                showPicker = false
            },
        )
    }
}

@Composable
fun LichSoNullableDatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Chọn ngày",
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    icon: ImageVector = Icons.Filled.CalendarToday,
) {
    val c = LichSoThemeColors.current
    var showPicker by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            label,
            style = TextStyle(fontSize = LichSoTextSize.label, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(LichSoRadius.field))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(LichSoRadius.field))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(17.dp))
            Text(
                date?.format(formatter) ?: placeholder,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (date != null) c.textPrimary else c.textTertiary,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }

    if (showPicker) {
        LichSoDatePickerDialog(
            initialDate = date ?: LocalDate.now(),
            onDismiss = { showPicker = false },
            onDateSelected = {
                onDateChange(it)
                showPicker = false
            },
        )
    }
}

private enum class LichSoDatePickerPanel { Day, Month, Year }

@Composable
fun LichSoDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val c = LichSoThemeColors.current
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var visibleMonth by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }
    var panel by remember { mutableStateOf(LichSoDatePickerPanel.Day) }
    var yearBlockStart by remember(initialDate) { mutableStateOf(initialDate.year - 7) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(12.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Chọn ngày",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                    modifier = Modifier.weight(1f),
                )
                LichSoIconCircleButton(icon = Icons.Filled.Close, contentDescription = "Đóng", onClick = onDismiss)
            }

            Spacer(Modifier.height(18.dp))

            DatePickerHeader(
                panel = panel,
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                yearBlockStart = yearBlockStart,
                onPrevious = {
                    when (panel) {
                        LichSoDatePickerPanel.Day -> visibleMonth = visibleMonth.minusMonths(1)
                        LichSoDatePickerPanel.Month -> visibleMonth = visibleMonth.minusYears(1)
                        LichSoDatePickerPanel.Year -> yearBlockStart -= 16
                    }
                },
                onNext = {
                    when (panel) {
                        LichSoDatePickerPanel.Day -> visibleMonth = visibleMonth.plusMonths(1)
                        LichSoDatePickerPanel.Month -> visibleMonth = visibleMonth.plusYears(1)
                        LichSoDatePickerPanel.Year -> yearBlockStart += 16
                    }
                },
                onTitleClick = {
                    panel = when (panel) {
                        LichSoDatePickerPanel.Day -> LichSoDatePickerPanel.Month
                        LichSoDatePickerPanel.Month -> {
                            yearBlockStart = visibleMonth.year - 7
                            LichSoDatePickerPanel.Year
                        }
                        LichSoDatePickerPanel.Year -> LichSoDatePickerPanel.Month
                    }
                },
            )

            Spacer(Modifier.height(16.dp))

            when (panel) {
                LichSoDatePickerPanel.Day -> DayPickerGrid(
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    onSelect = {
                        selectedDate = it
                        visibleMonth = YearMonth.from(it)
                    },
                )
                LichSoDatePickerPanel.Month -> MonthPickerGrid(
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    onSelect = { month ->
                        visibleMonth = YearMonth.of(visibleMonth.year, month)
                        panel = LichSoDatePickerPanel.Day
                    },
                )
                LichSoDatePickerPanel.Year -> YearPickerGrid(
                    yearBlockStart = yearBlockStart,
                    visibleYear = visibleMonth.year,
                    onSelect = { year ->
                        visibleMonth = visibleMonth.withYear(year)
                        panel = LichSoDatePickerPanel.Month
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            DatePickerSelectedSummary(selectedDate)
            Spacer(Modifier.height(14.dp))
            LichSoPrimaryButton(
                text = "Chọn ngày này",
                onClick = { onDateSelected(selectedDate) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DatePickerHeader(
    panel: LichSoDatePickerPanel,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    yearBlockStart: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val lunar = LunarCalendarUtil.convertSolar2Lunar(selectedDate.dayOfMonth, selectedDate.monthValue, selectedDate.year)
    val title = when (panel) {
        LichSoDatePickerPanel.Day -> "Tháng ${visibleMonth.monthValue} ${visibleMonth.year}"
        LichSoDatePickerPanel.Month -> "${visibleMonth.year}"
        LichSoDatePickerPanel.Year -> "$yearBlockStart - ${yearBlockStart + 15}"
    }
    val subtitle = when (panel) {
        LichSoDatePickerPanel.Day -> "Tháng ${lunar.lunarMonth} âm · ${CanChiCalculator.getYearCanChi(lunar.lunarYear)}"
        LichSoDatePickerPanel.Month -> "Nhấn năm để chọn nhanh 16 năm xung quanh"
        LichSoDatePickerPanel.Year -> "Chọn năm rồi chọn tháng"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LichSoIconCircleButton(icon = Icons.Filled.ChevronLeft, contentDescription = "Trước", onClick = onPrevious)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onTitleClick)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                textAlign = TextAlign.Center,
            )
            Text(
                subtitle,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.gold),
                textAlign = TextAlign.Center,
            )
        }
        LichSoIconCircleButton(icon = Icons.Filled.ChevronRight, contentDescription = "Sau", onClick = onNext)
    }
}

@Composable
private fun DayPickerGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
) {
    val c = LichSoThemeColors.current
    val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEachIndexed { index, label ->
            Text(
                label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (index == 6) c.badRed.copy(alpha = 0.75f) else c.textTertiary,
                ),
            )
        }
    }
    Spacer(Modifier.height(8.dp))

    val firstDay = visibleMonth.atDay(1)
    val firstGridDay = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())
    val days = List(42) { firstGridDay.plusDays(it.toLong()) }
    days.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                DayPickerCell(
                    date = day,
                    inVisibleMonth = day.monthValue == visibleMonth.monthValue,
                    selected = day == selectedDate,
                    onClick = {
                        onSelect(day)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DayPickerCell(
    date: LocalDate,
    inVisibleMonth: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    val lunar = LunarCalendarUtil.convertSolar2Lunar(date.dayOfMonth, date.monthValue, date.year)
    val isSunday = date.dayOfWeek.value == 7
    val solarColor = when {
        selected -> if (c.isDark) Color(0xFF0F0E0C) else Color(0xFF1C1B1F)
        !inVisibleMonth -> c.textQuaternary
        isSunday -> c.badRed.copy(alpha = 0.7f)
        else -> c.textPrimary
    }
    val lunarColor = when {
        selected -> c.textSecondary
        lunar.lunarDay == 1 || lunar.lunarDay == 15 -> c.gold
        !inVisibleMonth -> c.textQuaternary.copy(alpha = 0.65f)
        else -> c.textTertiary.copy(alpha = 0.75f)
    }
    val lunarText = when (lunar.lunarDay) {
        1 -> "1/${lunar.lunarMonth}"
        15 -> "Rằm"
        else -> lunar.lunarDay.toString()
    }

    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (c.isDark) Color(0xFFEFECE4) else c.primaryContainer, CircleShape),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                style = TextStyle(fontSize = 17.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = solarColor),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                lunarText,
                style = TextStyle(fontSize = 10.sp, fontWeight = if (lunar.lunarDay == 1 || lunar.lunarDay == 15) FontWeight.Bold else FontWeight.Medium, color = lunarColor),
            )
        }
    }
}

@Composable
private fun MonthPickerGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    onSelect: (Int) -> Unit,
) {
    (1..12).chunked(3).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { month ->
                val sample = LocalDate.of(visibleMonth.year, month, 15)
                val lunar = LunarCalendarUtil.convertSolar2Lunar(sample.dayOfMonth, sample.monthValue, sample.year)
                SelectablePickerTile(
                    title = "Tháng $month",
                    subtitle = "Âm ${lunar.lunarMonth}",
                    selected = month == selectedDate.monthValue && visibleMonth.year == selectedDate.year,
                    onClick = { onSelect(month) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun YearPickerGrid(
    yearBlockStart: Int,
    visibleYear: Int,
    onSelect: (Int) -> Unit,
) {
    (yearBlockStart until yearBlockStart + 16).chunked(4).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { year ->
                SelectablePickerTile(
                    title = year.toString(),
                    subtitle = CanChiCalculator.getYearCanChi(year),
                    selected = year == visibleYear,
                    onClick = { onSelect(year) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SelectablePickerTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(if (selected) c.primaryContainer else c.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .border(1.dp, if (selected) c.primary.copy(alpha = 0.45f) else c.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (selected) c.primary else c.textPrimary))
        Spacer(Modifier.height(3.dp))
        Text(subtitle, style = TextStyle(fontSize = 11.sp, color = c.textTertiary), maxLines = 1)
    }
}

@Composable
private fun DatePickerSelectedSummary(date: LocalDate) {
    val c = LichSoThemeColors.current
    val lunar = LunarCalendarUtil.convertSolar2Lunar(date.dayOfMonth, date.monthValue, date.year)
    val jd = LunarCalendarUtil.jdFromDate(date.dayOfMonth, date.monthValue, date.year)
    val leap = if (lunar.lunarLeap == 1) " nhuận" else ""
    Text(
        "${vnDayOfWeek(date)} · ${date.format(DateTimeFormatter.ofPattern("d/M/yyyy"))} — ${lunar.lunarDay} tháng ${lunar.lunarMonth}$leap âm · ${CanChiCalculator.getDayCanChi(jd)}",
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.gold),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun LichSoIconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = c.textTertiary, modifier = Modifier.size(22.dp))
    }
}

private fun vnDayOfWeek(date: LocalDate): String = when (date.dayOfWeek.value) {
    1 -> "Thứ Hai"
    2 -> "Thứ Ba"
    3 -> "Thứ Tư"
    4 -> "Thứ Năm"
    5 -> "Thứ Sáu"
    6 -> "Thứ Bảy"
    else -> "Chủ Nhật"
}

@Composable
fun LichSoToggleChip(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    val bg = if (checked) c.goodGreen.copy(alpha = 0.12f) else c.surfaceContainerHigh
    val border = if (checked) c.goodGreen.copy(alpha = 0.45f) else c.outlineVariant
    val text = if (checked) c.goodGreen else c.textSecondary
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(LichSoRadius.chip))
            .border(1.dp, border, RoundedCornerShape(LichSoRadius.chip))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = text, modifier = Modifier.size(14.dp))
        Text(label, style = TextStyle(fontSize = 12.sp, color = text, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun LichSoBadge(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text(text, style = TextStyle(fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun LichSoEmptyState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(c.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = TextStyle(fontSize = LichSoTextSize.heading, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
        Spacer(Modifier.height(6.dp))
        Text(message, style = TextStyle(fontSize = LichSoTextSize.body, color = c.textSecondary))
        Spacer(Modifier.height(20.dp))
        LichSoPrimaryButton(text = actionText, onClick = onAction, icon = icon)
    }
}

@Composable
fun LichSoDialogSurface(
    onDismiss: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LichSoThemeColors.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(LichSoRadius.dialog))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = TextStyle(fontSize = LichSoTextSize.heading, fontWeight = FontWeight.Bold, color = c.textPrimary))
            content()
        }
    }
}

@Composable
fun LichSoCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = TextStyle(fontSize = 14.sp, color = c.textPrimary))
    }
}
