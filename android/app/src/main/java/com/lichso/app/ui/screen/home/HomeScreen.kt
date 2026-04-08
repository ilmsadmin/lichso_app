package com.lichso.app.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.remote.WeatherState
import com.lichso.app.domain.model.*
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.CalendarPatternBackground
import com.lichso.app.ui.components.HeaderIconButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // ── Weather detail sheet ──
    var showWeatherSheet by remember { mutableStateOf(false) }

    // ── Page flip state ──
    // flipProgress: 0 = page flat, → 1 = page fully flipped (next day)
    //                                → -1 = page fully flipped (prev day)
    val flipProgress = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isAnimatingFlip by remember { mutableStateOf(false) }
    val flipCommitThreshold = 0.18f  // 18% rotation = commit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        uiState.dayInfo?.let { info ->
            // ═══ RED GRADIENT HEADER ═══
            RedHeader(
                info = info,
                weatherState = uiState.weatherState,
                tempUnit = uiState.tempUnit,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick,
                onWeatherRefresh = { viewModel.refreshWeather() },
                onWeatherClick = { showWeatherSheet = true },
                notificationUnreadCount = uiState.notificationUnreadCount
            )

            // ═══ TEAR LINE (perforation) ═══
            TearLine()

            // ═══ MINI WEEK STRIP ═══
            MiniCalendarStrip(
                selectedDate = uiState.selectedDate,
                calendarDays = uiState.calendarDays,
                weekStartSunday = uiState.weekStartSunday,
                onDayClick = { day ->
                    viewModel.selectDay(day.solarDay, day.solarMonth, day.solarYear)
                }
            )

            // ═══ PAGE FLIP CALENDAR AREA ═══
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                if (!isAnimatingFlip) isDragging = true
                            },
                            onDragEnd = {
                                if (!isDragging) return@detectVerticalDragGestures
                                isDragging = false
                                val current = flipProgress.value

                                if (kotlin.math.abs(current) >= flipCommitThreshold) {
                                    // Commit flip
                                    val goNext = current > 0
                                    isAnimatingFlip = true
                                    coroutineScope.launch {
                                        // Animate page to fully flipped
                                        flipProgress.animateTo(
                                            if (goNext) 1f else -1f,
                                            tween(180, easing = FastOutSlowInEasing)
                                        )
                                        // Change day
                                        if (goNext) viewModel.nextDay() else viewModel.prevDay()
                                        // New page enters from opposite side
                                        flipProgress.snapTo(if (goNext) -0.25f else 0.25f)
                                        flipProgress.animateTo(
                                            0f,
                                            tween(200, easing = FastOutSlowInEasing)
                                        )
                                        isAnimatingFlip = false
                                    }
                                } else {
                                    // Snap back
                                    isAnimatingFlip = true
                                    coroutineScope.launch {
                                        flipProgress.animateTo(
                                            0f,
                                            spring(dampingRatio = 0.8f, stiffness = 600f)
                                        )
                                        isAnimatingFlip = false
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                coroutineScope.launch {
                                    flipProgress.animateTo(0f, spring(stiffness = 600f))
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                if (isDragging && !isAnimatingFlip) {
                                    change.consume()
                                    // Swipe up → positive (next), swipe down → negative (prev)
                                    val delta = -dragAmount / 450f
                                    val newVal = (flipProgress.value + delta).coerceIn(-1f, 1f)
                                    coroutineScope.launch {
                                        flipProgress.snapTo(newVal)
                                    }
                                }
                            }
                        )
                    }
            ) {
                val progress = flipProgress.value
                val absProgress = kotlin.math.abs(progress)

                // ── Single layer: 3D page flip ──
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // 3D perspective
                            cameraDistance = 16f * density

                            // Rotate around top/bottom edge like a calendar page
                            rotationX = progress * -80f

                            // Pivot at top edge (swipe up) or bottom edge (swipe down)
                            transformOrigin = if (progress >= 0)
                                TransformOrigin(0.5f, 0f)
                            else
                                TransformOrigin(0.5f, 1f)

                            // Fade as page flips past ~50°
                            alpha = (1f - absProgress * 0.9f).coerceIn(0f, 1f)
                        }
                        .background(c.bg)
                ) {
                    // ═══ HOA VĂN NỀN & VIỀN TỜ LỊCH ═══
                    CalendarPatternBackground(
                        day = info.solar.dd,
                        month = info.solar.mm,
                        year = info.solar.yy
                    )

                    // ═══ NỘI DUNG CHÍNH ═══
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ═══ BIG DATE DISPLAY ═══
                        BigDateSection(info = info)

                        Spacer(modifier = Modifier.height(8.dp))

                        // ═══ SWIPE HINT ═══
                        SwipeHint()

                        Spacer(modifier = Modifier.height(8.dp))

                        // ═══ QUOTE SECTION ═══
                        if (uiState.showQuote) {
                            QuoteSection(selectedDate = uiState.selectedDate)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // ═══ EVENT CHIPS ═══
                        EventChips(
                            info = info,
                            showFestival = uiState.showFestival,
                            onHistoryClick = onHistoryClick
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // ═══ WEATHER DETAIL BOTTOM SHEET ═══
    if (showWeatherSheet) {
        val ws = uiState.weatherState
        if (ws is WeatherState.Success) {
            WeatherDetailSheet(
                weather = ws.weather,
                tempUnit = uiState.tempUnit,
                onDismiss = { showWeatherSheet = false },
                onRefresh = {
                    viewModel.refreshWeather()
                },
                onCityChange = { cityName ->
                    viewModel.changeCity(cityName)
                }
            )
        } else {
            showWeatherSheet = false
        }
    }
}

// ══════════════════════════════════════════
// SWIPE HINT INDICATOR
// ══════════════════════════════════════════

@Composable
private fun SwipeHint() {
    val c = LichSoThemeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintOffset"
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Filled.SwipeVertical,
            contentDescription = null,
            tint = c.textTertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            "Vuốt để lật lịch",
            style = TextStyle(fontSize = 10.sp, color = c.textTertiary)
        )
    }
}

// ══════════════════════════════════════════
// RED GRADIENT HEADER (matching index.html)
// ══════════════════════════════════════════

@Composable
private fun RedHeader(
    info: DayInfo,
    weatherState: WeatherState,
    tempUnit: String = "°C",
    onMenuClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onWeatherRefresh: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    notificationUnreadCount: Int = 0
) {
    val c = LichSoThemeColors.current
    val colors = if (c.isDark) {
        listOf(Color(0xFF5D1212), Color(0xFF7F1D1D), Color(0xFF4A1010))
    } else {
        listOf(c.primary, Color(0xFFD32F2F), c.deepRed)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column {
            // ── Row 1: Menu | Weather chip | Notification ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    icon = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    onClick = onMenuClick
                )

                WeatherChip(
                    weatherState = weatherState,
                    tempUnit = tempUnit,
                    onRefresh = onWeatherRefresh,
                    onClick = onWeatherClick
                )

                HeaderIconButton(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNotificationClick,
                    badgeCount = notificationUnreadCount
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Row 2: Year + Month + Weekday (year-month-row from HTML) ──
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Year — small, semi-transparent
                Text(
                    "${info.solar.yy}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                )
                // Month — bold, prominent
                Text(
                    "Tháng ${info.solar.mm}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
                // Weekday — lighter
                Text(
                    "• ${info.dayOfWeek}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// WEATHER CHIP (hiển thị thời tiết thật)
// ══════════════════════════════════════════

@Composable
private fun WeatherChip(
    weatherState: WeatherState,
    tempUnit: String = "°C",
    onRefresh: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                when (weatherState) {
                    is WeatherState.Success -> onClick()
                    is WeatherState.Error -> onRefresh()
                    else -> {}
                }
            }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (weatherState) {
            is WeatherState.Loading -> {
                // Loading shimmer
                Icon(
                    Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F).copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "...",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                )
            }

            is WeatherState.Success -> {
                val weather = weatherState.weather
                val displayTemp = if (tempUnit == "°F") {
                    (weather.temperature * 9 / 5 + 32).toInt()
                } else {
                    weather.temperature.toInt()
                }
                Text(
                    weather.icon,
                    style = TextStyle(fontSize = 18.sp)
                )
                Text(
                    "${displayTemp}${tempUnit}",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    weather.cityName,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                )
            }

            is WeatherState.Error -> {
                Icon(
                    Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Thử lại",
                    style = TextStyle(fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// TEAR LINE (dashed perforation)
// ══════════════════════════════════════════

@Composable
private fun TearLine() {
    val c = LichSoThemeColors.current
    val dashColor = c.outlineVariant
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .drawBehind {
                val dashWidth = 8.dp.toPx()
                val gapWidth = 6.dp.toPx()
                drawLine(
                    color = dashColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f)
                )
            }
    )
}

// ══════════════════════════════════════════
// MINI CALENDAR WEEK STRIP
// ══════════════════════════════════════════

@Composable
private fun MiniCalendarStrip(
    selectedDate: java.time.LocalDate,
    calendarDays: List<CalendarDay>,
    weekStartSunday: Boolean = false,
    onDayClick: (CalendarDay) -> Unit
) {
    val c = LichSoThemeColors.current

    // Get the week containing the selected date, respecting week start setting
    val dayOfWeek = selectedDate.dayOfWeek.value // 1=Mon..7=Sun
    val weekStart = if (weekStartSunday) {
        // Sunday start: offset from Sunday
        val offset = dayOfWeek % 7 // Sun=0, Mon=1..Sat=6
        selectedDate.minusDays(offset.toLong())
    } else {
        // Monday start (default)
        selectedDate.minusDays((dayOfWeek - 1).toLong())
    }

    val weekDayLabels = if (weekStartSunday) {
        listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    } else {
        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        (0..6).forEach { i ->
            val date = weekStart.plusDays(i.toLong())
            val isToday = date == java.time.LocalDate.now()
            val isSelected = date == selectedDate
            val isWeekend = if (weekStartSunday) {
                i == 0 || i == 6  // CN at 0, T7 at 6
            } else {
                i >= 5  // T7 at 5, CN at 6
            }
            val lunarText = calendarDays.find {
                it.solarDay == date.dayOfMonth && it.solarMonth == date.monthValue && it.solarYear == date.year
            }?.lunarDisplayText ?: ""

            Column(
                modifier = Modifier
                    .width(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isToday -> if (c.isDark) Color(0xFF7F1D1D) else c.primary
                            else -> Color.Transparent
                        }
                    )
                    .clickable {
                        val matchDay = calendarDays.find {
                            it.solarDay == date.dayOfMonth && it.solarMonth == date.monthValue && it.solarYear == date.year
                        }
                        matchDay?.let { onDayClick(it) }
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    weekDayLabels[i],
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isToday) Color.White.copy(alpha = 0.8f)
                        else c.textTertiary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${date.dayOfMonth}",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isToday -> Color.White
                            isWeekend -> if (c.isDark) Color(0xFFEF9A9A) else c.primary
                            else -> c.textPrimary
                        }
                    )
                )
                Text(
                    lunarText,
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = if (isToday) Color.White.copy(alpha = 0.7f)
                        else c.textSecondary
                    )
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// BIG DATE DISPLAY (matching index.html)
// ══════════════════════════════════════════

@Composable
private fun BigDateSection(info: DayInfo) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big solar date number
        Text(
            text = "%02d".format(info.solar.dd),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 140.sp,
                color = if (c.isDark) Color(0xFFEF9A9A) else c.primary,
                lineHeight = 140.sp,
                letterSpacing = (-2).sp
            )
        )

        // Decorative gold line under date
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, c.gold, Color.Transparent)
                    ),
                    RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Weekday label
        Text(
            text = info.dayOfWeek.uppercase(),
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lunar date info card
        Row(
            modifier = Modifier
                .background(c.surfaceContainer, RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    if (c.isDark) c.gold.copy(alpha = 0.25f) else c.outlineVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Moon icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🌙", style = TextStyle(fontSize = 18.sp))
            }

            Column {
                Text(
                    "Mùng ${info.lunar.day} tháng ${info.lunar.month}",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
                Text(
                    "Năm ${info.yearCanChi}",
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
                )
            }

            Text(
                "Ngày ${info.dayCanChi}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (c.isDark) Color(0xFFEF9A9A) else c.deepRed)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day quality indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isGood = !info.activities.isXauDay
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isGood) c.goodGreen else c.badRed, CircleShape)
            )
            Text(
                text = if (isGood) "Ngày Hoàng Đạo ✦" else "Ngày Hắc Đạo",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGood) c.goodGreen else c.badRed
                )
            )
        }
    }
}

// ══════════════════════════════════════════
// QUOTE SECTION
// ══════════════════════════════════════════

@Composable
private fun QuoteSection(selectedDate: java.time.LocalDate) {
    val c = LichSoThemeColors.current

    // Mỗi tờ lịch (ngày khác nhau) hiển thị câu khác nhau
    val (quoteText, quoteAuthor) = remember(selectedDate) {
        val dayOfYear = selectedDate.dayOfYear
        com.lichso.app.data.VietnameseQuotes.ofDay(dayOfYear)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u201C $quoteText \u201D",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = if (c.isDark) c.textSecondary else c.textTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "— $quoteAuthor",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (c.isDark) c.textTertiary else c.outline)
        )
    }
}

// ══════════════════════════════════════════
// EVENT CHIPS
// ══════════════════════════════════════════

@Composable
private fun EventChips(info: DayInfo, showFestival: Boolean = true, onHistoryClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    val holiday = info.solarHoliday ?: info.lunarHoliday

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (showFestival && holiday != null) {
            EventChip(
                icon = Icons.Filled.Celebration,
                text = holiday,
                bgColor = if (c.isDark) Color(0xFF3D2A10) else Color(0xFFFFF3E0),
                textColor = if (c.isDark) Color(0xFFE8A06A) else Color(0xFFE65100),
                borderColor = if (c.isDark) Color(0xFF5C3D1A) else Color(0xFFFFB74D)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        EventChip(
            icon = Icons.Filled.HistoryEdu,
            text = "Ngày này năm xưa",
            bgColor = if (c.isDark) Color(0xFF1A2E1A) else Color(0xFFE8F5E9),
            textColor = if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
            borderColor = if (c.isDark) Color(0xFF2E4A2E) else Color(0xFF81C784),
            onClick = onHistoryClick
        )
    }
}

@Composable
private fun EventChip(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor))
    }
}
