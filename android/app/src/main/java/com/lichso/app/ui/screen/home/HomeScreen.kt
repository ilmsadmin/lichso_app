package com.lichso.app.ui.screen.home

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.domain.model.*
import com.lichso.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Live clock
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Decorative sparkle (top-left)
        Box(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = c.gold.copy(alpha = 0.35f), modifier = Modifier.size(14.dp))
        }

        uiState.dayInfo?.let { info ->
            // ═══ HERO DAY CARD — fills viewport ═══
            DayHeroSection(
                info = info,
                onSettingsClick = onSettingsClick,
                onPrevDay = viewModel::prevDay,
                onNextDay = viewModel::nextDay,
            )

            // ═══ LIVE CLOCK ═══
            LiveClockSection(
                currentTime = currentTime,
                gioHoangDao = info.gioHoangDao,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ═══ TIẾT KHÍ ═══
            TietKhiBar(tietKhi = info.tietKhi)

            Spacer(modifier = Modifier.height(14.dp))

            // ═══ DAY ACTIVITIES ═══
            SectionLabel(text = "THÔNG TIN NGÀY ${info.solar.dd}/${info.solar.mm}")
            Spacer(modifier = Modifier.height(7.dp))
            ActivityGrid(info = info)

            Spacer(modifier = Modifier.height(14.dp))

            // ═══ EVENTS ═══
            if (uiState.upcomingEvents.isNotEmpty()) {
                SectionLabel(text = "SỰ KIỆN SẮP TỚI")
                Spacer(modifier = Modifier.height(7.dp))
                EventList(events = uiState.upcomingEvents)
            }
        }

        Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
    }
}

// ══════════════════════════════════════════
// HERO DAY SECTION — Big date display matching screenshot
// ══════════════════════════════════════════

@Composable
private fun DayHeroSection(
    info: DayInfo,
    onSettingsClick: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val cardBg = if (c.isDark) {
        Brush.verticalGradient(listOf(Color(0xFF1A1710), Color(0xFF1E1C14), c.bg))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFCFAF5), Color(0xFFF8F5ED), c.bg))
    }
    val cardBorder = if (c.isDark) Color(0x20E8C84A) else Color(0x18A08520)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(brush = cardBg, shape = RoundedCornerShape(24.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
    ) {
        // Settings gear top-right (no circle background)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = "Cài đặt", tint = c.textTertiary, modifier = Modifier.size(18.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Big Solar Day Number ──
            Text(
                text = "${info.solar.dd}",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 96.sp,
                    color = c.textPrimary,
                    letterSpacing = (-3).sp,
                    lineHeight = 96.sp,
                )
            )

            // ── Day of Week ──
            Text(
                text = info.dayOfWeek.uppercase().split("").filter { it.isNotBlank() }.joinToString("  "),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textTertiary,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Month · Year ──
            Text(
                text = "Tháng ${info.solar.mm} · ${info.solar.yy}",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    color = c.textSecondary,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Day Navigator: < ✦ > ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = onPrevDay,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
                }

                // Decorative line with diamond
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(180.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(c.border))
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.StarOutline, null, tint = c.gold.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(c.border))
                }

                IconButton(
                    onClick = onNextDay,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══ LUNAR SECTION ═══
            Text(
                "Â M   L Ị C H",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textTertiary,
                    letterSpacing = 3.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Big lunar day
            Text(
                text = "${info.lunar.day}",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    color = c.teal,
                    letterSpacing = (-2).sp,
                    lineHeight = 52.sp,
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Lunar month + year Can Chi
            Text(
                text = "${info.lunar.monthName} · Năm ${info.yearCanChi}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = c.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Can Chi Chips ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                CanChiChip(info.dayCanChi, c.red2, c.red.copy(alpha = 0.12f), c.red.copy(alpha = 0.3f))
                CanChiChip(info.monthCanChi, c.textSecondary, c.surface, c.border)
                CanChiChip(info.yearCanChi.takeLast(info.yearCanChi.length.coerceAtMost(6)), c.textSecondary, c.surface, c.border)
            }

            // ── Holiday / special day ──
            val holiday = info.solarHoliday ?: info.lunarHoliday
            if (holiday != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .background(c.goldDim, RoundedCornerShape(20.dp))
                        .border(1.dp, c.gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Celebration, contentDescription = null, tint = c.gold2, modifier = Modifier.size(14.dp))
                        Text(
                            holiday,
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.gold2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CanChiChip(text: String, textColor: Color, bgColor: Color, borderColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(text, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor))
    }
}

// ══════════════════════════════════════════
// LIVE CLOCK SECTION
// ══════════════════════════════════════════

@Composable
private fun LiveClockSection(
    currentTime: LocalTime,
    gioHoangDao: List<GioHoangDaoInfo>,
) {
    val c = LichSoThemeColors.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

    // Determine current Giờ
    val currentHour = currentTime.hour
    val gioName = getGioName(currentHour)
    val isHoangDao = gioHoangDao.any { it.name == gioName }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big clock
        Text(
            text = currentTime.format(timeFormatter),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = c.textPrimary,
                letterSpacing = 2.sp,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Giờ label
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Giờ $gioName",
                style = TextStyle(fontSize = 13.sp, color = c.textSecondary)
            )
            if (isHoangDao) {
                Text("·", style = TextStyle(color = c.textTertiary))
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = c.teal2, modifier = Modifier.size(13.dp))
                Text(
                    "Hoàng Đạo",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.teal2,
                    )
                )
            }
        }
    }
}

private fun getGioName(hour: Int): String = when (hour) {
    23, 0 -> "Tý"
    1, 2 -> "Sửu"
    3, 4 -> "Dần"
    5, 6 -> "Mão"
    7, 8 -> "Thìn"
    9, 10 -> "Tỵ"
    11, 12 -> "Ngọ"
    13, 14 -> "Mùi"
    15, 16 -> "Thân"
    17, 18 -> "Dậu"
    19, 20 -> "Tuất"
    21, 22 -> "Hợi"
    else -> ""
}

// ══════════════════════════════════════════
// TIẾT KHÍ BAR
// ══════════════════════════════════════════

@Composable
private fun TietKhiBar(tietKhi: TietKhiInfo) {
    val c = LichSoThemeColors.current
    val barBg = if (c.isDark) {
        Brush.horizontalGradient(colors = listOf(Color(0x12E8C84A), Color(0x05E8C84A)))
    } else {
        Brush.horizontalGradient(colors = listOf(Color(0x14C4A020), Color(0x06C4A020)))
    }
    val barBorder = if (c.isDark) Color(0x29E8C84A) else Color(0x22C4A020)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(brush = barBg, shape = RoundedCornerShape(10.dp))
            .border(1.dp, barBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = c.teal.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val text = if (tietKhi.daysUntilNext == 0) {
                "Hôm nay: ${tietKhi.currentName ?: ""}"
            } else {
                "Tiết khí: ${tietKhi.nextName ?: ""} — ${tietKhi.nextDd ?: ""}/${tietKhi.nextMm ?: ""} (còn ${tietKhi.daysUntilNext} ngày)"
            }
            Text(text = text, style = TextStyle(fontSize = 12.sp, color = c.textSecondary))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(14.dp))
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text,
        style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.sp),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ══════════════════════════════════════════
// ACTIVITY GRID
// ══════════════════════════════════════════

@Composable
private fun ActivityGrid(info: DayInfo) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
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

// ══════════════════════════════════════════
// EVENT LIST
// ══════════════════════════════════════════

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
