package com.lichso.app.ui.screen.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.domain.model.*
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Compact floating overlay — day detail when tapping a calendar day.
 */
@Composable
fun DayDetailOverlay(
    dayInfo: DayInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    // Scrim
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        val cardBg = if (c.isDark) {
            Brush.verticalGradient(listOf(Color(0xFF1E1C14), Color(0xFF161410)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFFFFDF8), Color(0xFFF6F3EB)))
        }
        val cardBorder = if (c.isDark) Color(0x28E8C84A) else Color(0x1AA08520)

        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(brush = cardBg, shape = RoundedCornerShape(22.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                .clickable(enabled = false) {}
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 20.dp),
        ) {
            // ─── Header: Solar + Close ───
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "DƯƠNG LỊCH",
                        style = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.5.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${dayInfo.solar.dd}",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                                fontSize = 52.sp, color = c.gold2,
                                letterSpacing = (-2).sp, lineHeight = 52.sp,
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.padding(bottom = 7.dp)) {
                            Text(
                                "Tháng ${dayInfo.solar.mm} · ${dayInfo.solar.yy}",
                                style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 14.sp, color = c.textSecondary)
                            )
                            Text(dayInfo.dayOfWeek, style = TextStyle(fontSize = 12.5.sp, color = c.textTertiary))
                        }
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                ) {
                    Icon(Icons.Default.Close, "Đóng", tint = c.textTertiary, modifier = Modifier.size(18.dp))
                }
            }

            // ─── Thin divider ───
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(c.border))
            Spacer(modifier = Modifier.height(10.dp))

            // ─── Âm lịch row ───
            Text(
                "ÂM LỊCH",
                style = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.5.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                val lunarDayText = if (dayInfo.lunar.day <= 10) "Mồng ${dayInfo.lunar.day}" else "${dayInfo.lunar.day}"
                Text(
                    lunarDayText,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                        fontSize = 28.sp, color = c.teal, lineHeight = 28.sp,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tháng ${dayInfo.lunar.monthName.replaceFirst("tháng ", "")} · Năm ${dayInfo.yearCanChi}",
                    style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Moon phase — compact inline
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${dayInfo.moonPhase.icon}  ${dayInfo.moonPhase.name}",
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
            )

            // ─── Can Chi chips ───
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MiniChip("Ngày ${dayInfo.dayCanChi}", c.red2, c.red.copy(alpha = 0.10f))
                MiniChip("Tháng ${dayInfo.monthCanChi}", c.teal2, c.teal.copy(alpha = 0.10f))
                MiniChip("Năm ${dayInfo.yearCanChi}", c.textSecondary, c.surface)
            }

            // ─── Holiday (if any) ───
            val holiday = dayInfo.solarHoliday ?: dayInfo.lunarHoliday
            if (holiday != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.goldDim.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("�", fontSize = 14.sp)
                    Text(holiday, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.gold2))
                }
            }

            // ─── Rating bar ───
            Spacer(modifier = Modifier.height(14.dp))
            DayRatingBar(dayInfo.dayRating)

            // ─── 2×2 Info Grid (compact) ───
            Spacer(modifier = Modifier.height(14.dp))
            CompactInfoGrid(dayInfo)

            // ─── Giờ hoàng đạo (compact) ───
            Spacer(modifier = Modifier.height(14.dp))
            CompactGioHoangDao(dayInfo)
        }
    }
}

// ═══ Small chip ═══
@Composable
private fun MiniChip(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor))
    }
}

// ═══ Rating bar ═══
@Composable
private fun DayRatingBar(rating: DayRatingInfo) {
    val c = LichSoThemeColors.current
    val ratingColor = when {
        rating.percent >= 80 -> c.teal
        rating.percent >= 60 -> c.teal2
        rating.percent >= 40 -> c.gold
        else -> c.red
    }
    val trackColor = if (c.isDark) Color(0xFF2A2720) else Color(0xFFE5E1D8)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "ĐÁNH GIÁ NGÀY",
            style = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.2.sp)
        )
        Text(
            "${rating.label} · ${rating.percent}%",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ratingColor)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
        modifier = Modifier.fillMaxWidth().height(7.dp)
            .clip(RoundedCornerShape(3.5.dp)).background(trackColor)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(rating.percent / 100f).fillMaxHeight()
                .clip(RoundedCornerShape(3.5.dp))
                .background(Brush.horizontalGradient(listOf(ratingColor, ratingColor.copy(alpha = 0.65f))))
        )
    }
}

// ═══ Compact 2×2 info grid ═══
@Composable
private fun CompactInfoGrid(info: DayInfo) {
    val c = LichSoThemeColors.current
    val cardBg = if (c.isDark) Color(0xFF1A1810) else Color(0xFFF2EFE6)
    val cardBorder = if (c.isDark) Color(0x14E8C84A) else Color(0x12A08520)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallInfoCard(
                label = "TRỰC NGÀY", value = info.trucNgay.name, sub = info.trucNgay.rating,
                subColor = when (info.trucNgay.rating) { "Tốt" -> c.teal2; "Xấu" -> c.red2; else -> c.gold2 },
                bg = cardBg, border = cardBorder, modifier = Modifier.weight(1f)
            )
            SmallInfoCard(
                label = "SAO CHIẾU", value = info.saoChieu.name, sub = info.saoChieu.rating,
                subColor = when (info.saoChieu.rating) { "Tốt" -> c.teal2; "Xấu" -> c.red2; else -> c.gold2 },
                bg = cardBg, border = cardBorder, modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallInfoCard(
                label = "HƯỚNG TỐT", value = info.huong.thanTai, sub = "Tài thần",
                subColor = c.textTertiary, bg = cardBg, border = cardBorder, modifier = Modifier.weight(1f)
            )
            val tkName = info.tietKhi.currentName ?: "—"
            val tkSub = if (info.tietKhi.nextName != null) "→ ${info.tietKhi.nextName}" else ""
            SmallInfoCard(
                label = "TIẾT KHÍ", value = tkName, sub = tkSub,
                subColor = c.teal2, bg = cardBg, border = cardBorder, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmallInfoCard(
    label: String, value: String, sub: String,
    subColor: Color, bg: Color, border: Color,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 0.8.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
        if (sub.isNotEmpty()) {
            Text(sub, style = TextStyle(fontSize = 10.5.sp, color = subColor))
        }
    }
}

// ═══ Compact Giờ Hoàng Đạo ═══
@Composable
private fun CompactGioHoangDao(info: DayInfo) {
    val c = LichSoThemeColors.current
    val hoangDaoNames = info.gioHoangDao.map { it.name }.toSet()

    Text(
        "GIỜ HOÀNG ĐẠO",
        style = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.2.sp)
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Only show hoàng đạo hours as compact chips in a FlowRow-style wrap
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        info.gioHoangDao.forEach { gio ->
            val chipBg = if (c.isDark) Color(0x264ABEAA) else Color(0x182E9A88)
            val chipBorder = c.teal.copy(alpha = 0.30f)
            Box(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(8.dp))
                    .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    "${gio.name} ${gio.time}",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.teal2)
                )
            }
        }
    }
}
