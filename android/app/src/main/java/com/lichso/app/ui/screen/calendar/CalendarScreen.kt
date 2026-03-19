package com.lichso.app.ui.screen.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.domain.model.*
import com.lichso.app.ui.screen.home.HomeViewModel
import com.lichso.app.ui.theme.*

@Composable
fun CalendarScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Track which day's detail overlay is shown
    var showDayDetail by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .verticalScroll(rememberScrollState())
        ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lịch Tháng",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    color = c.gold2,
                    letterSpacing = 0.3.sp
                )
            )
            // Today button
            Box(
                modifier = Modifier
                    .background(c.goldDim, RoundedCornerShape(20.dp))
                    .border(1.dp, c.gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.goToToday() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    "Hôm nay",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.gold2)
                )
            }
        }

        // Month Navigation
        MonthNavigation(
            month = uiState.currentMonth,
            year = uiState.currentYear,
            onPrev = viewModel::prevMonth,
            onNext = viewModel::nextMonth
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        CalendarGrid(
            days = uiState.calendarDays,
            selectedDate = uiState.selectedDate,
            showLunarBadge = uiState.showLunarBadge,
            onDayClick = { day ->
                viewModel.selectDay(day.solarDay, day.solarMonth, day.solarYear)
                showDayDetail = true
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selected day info
        uiState.dayInfo?.let { info ->
            SectionLabel(text = "THÔNG TIN NGÀY ${info.solar.dd}/${info.solar.mm}")
            Spacer(modifier = Modifier.height(7.dp))
            ActivityGrid(info = info)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Events
        if (uiState.upcomingEvents.isNotEmpty()) {
            SectionLabel(text = "SỰ KIỆN SẮP TỚI")
            Spacer(modifier = Modifier.height(7.dp))
            EventList(events = uiState.upcomingEvents)
        }

        Spacer(modifier = Modifier.height(96.dp))
        }

        // ═══ Day Detail Overlay ═══
        AnimatedVisibility(
            visible = showDayDetail && uiState.dayInfo != null,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300, easing = EaseOutCubic)
            ),
            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                targetOffsetY = { it / 4 },
                animationSpec = tween(200)
            )
        ) {
            uiState.dayInfo?.let { info ->
                DayDetailOverlay(
                    dayInfo = info,
                    onDismiss = { showDayDetail = false }
                )
            }
        }
    }
}

// ═══ Month Navigation ═══

@Composable
private fun MonthNavigation(month: Int, year: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = c.textSecondary, modifier = Modifier.size(18.dp))
        }
        Text(
            text = "Tháng $month · $year",
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
        )
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = c.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══ Calendar Grid ═══

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDate: java.time.LocalDate,
    showLunarBadge: Boolean,
    onDayClick: (CalendarDay) -> Unit
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(c.bg2, RoundedCornerShape(16.dp))
            .border(1.dp, c.border, RoundedCornerShape(16.dp))
            .padding(bottom = 7.dp)
    ) {
        val weekDays = listOf("CN" to true, "T2" to false, "T3" to false, "T4" to false, "T5" to false, "T6" to false, "T7" to false)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 5.dp)
        ) {
            weekDays.forEachIndexed { index, (label, isSunday) ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSunday -> c.red2
                            index == 6 -> c.teal2
                            else -> c.textTertiary
                        },
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                week.forEach { day ->
                    val isSelected = day.isCurrentMonth &&
                            day.solarDay == selectedDate.dayOfMonth &&
                            day.solarMonth == selectedDate.monthValue &&
                            day.solarYear == selectedDate.year &&
                            !day.isToday

                    DayCell(
                        day = day,
                        isSelected = isSelected,
                        showLunarBadge = showLunarBadge,
                        onClick = { if (day.isCurrentMonth) onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    showLunarBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    val bgColor = when {
        day.isToday -> c.goldDim
        isSelected -> c.tealDim
        else -> Color.Transparent
    }
    val borderColor = if (isSelected) c.teal.copy(alpha = 0.3f) else Color.Transparent

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .then(if (isSelected) Modifier.border(1.dp, borderColor, RoundedCornerShape(9.dp)) else Modifier)
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(1.dp)
        ) {
            val textColor = when {
                !day.isCurrentMonth -> c.textPrimary.copy(alpha = 0.28f)
                day.isToday -> c.gold2
                day.isHoliday || day.isSunday -> c.red2
                isSelected -> c.teal2
                day.isSaturday -> c.teal2
                else -> c.textPrimary
            }
            val fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Medium

            Text(
                text = "${day.solarDay}",
                style = TextStyle(fontSize = 14.sp, fontWeight = fontWeight, color = textColor, lineHeight = 14.sp)
            )
            if (showLunarBadge) {
                Text(
                    text = day.lunarDisplayText,
                    style = TextStyle(fontSize = 9.sp, color = if (day.isCurrentMonth) c.textTertiary else c.textTertiary.copy(alpha = 0.28f), lineHeight = 9.sp)
                )
            }
        }

        if (day.isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .background(c.gold, CircleShape)
            )
        }

        if (day.hasEvent && day.isCurrentMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 5.dp)
                    .size(4.dp)
                    .background(c.teal2, CircleShape)
            )
        }
    }
}

// ═══ Info Sections ═══

@Composable
private fun SectionLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text,
        style = TextStyle(
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = c.textTertiary,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun ActivityGrid(info: DayInfo) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityCard("Nên làm", Icons.Outlined.CheckCircle, c.teal2, info.activities.nenLam, c.teal2, Modifier.weight(1f))
            ActivityCard("Không nên", Icons.Outlined.Cancel, c.red2, info.activities.khongNen, c.red2, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityCard(
                "Giờ hoàng đạo", Icons.Outlined.Star, c.gold2,
                info.gioHoangDao.map { "${it.name} (${it.time})" },
                c.gold2, Modifier.weight(1f)
            )
            ActivityCard(
                "Hướng tốt", Icons.Outlined.Explore, c.gold2,
                listOf("Thần tài: ${info.huong.thanTai}", "Hỷ thần: ${info.huong.hyThan}", "Hung thần: ${info.huong.hungThan}"),
                c.gold2, Modifier.weight(1f), specialLastItemRed = true
            )
        }
    }
}

@Composable
private fun ActivityCard(
    title: String, icon: ImageVector, titleColor: Color,
    items: List<String>, itemColor: Color,
    modifier: Modifier = Modifier, specialLastItemRed: Boolean = false
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .padding(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(14.dp))
            Text(title.uppercase(), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = titleColor, letterSpacing = 0.8.sp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        items.forEachIndexed { index, item ->
            val color = if (specialLastItemRed && index == items.size - 1) c.red2 else itemColor
            Text("• $item", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 12.sp, color = color, lineHeight = 18.sp))
        }
    }
}

@Composable
private fun EventList(events: List<UpcomingEvent>) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        events.forEach { event ->
            val (dotColor, tagBg, tagColor) = when (event.colorType) {
                EventColor.GOLD -> Triple(c.gold, c.goldDim, c.gold)
                EventColor.TEAL -> Triple(c.teal, c.tealDim, c.teal2)
                EventColor.RED -> Triple(c.red, c.red.copy(alpha = 0.12f), c.red2)
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(c.bg2, RoundedCornerShape(10.dp))
                    .border(1.dp, c.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.textPrimary))
                    Text(event.timeLabel, style = TextStyle(fontSize = 11.sp, color = c.textTertiary), modifier = Modifier.padding(top = 2.dp))
                }
                Box(
                    modifier = Modifier.background(tagBg, RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(event.tag, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tagColor))
                }
            }
        }
    }
}
