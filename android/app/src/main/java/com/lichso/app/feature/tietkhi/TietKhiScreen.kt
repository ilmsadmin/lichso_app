package com.lichso.app.feature.tietkhi

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.util.TietKhiCalculator
import java.util.Calendar

/**
 * Màn hình "24 Tiết Khí" — hiển thị toàn bộ vòng năm Á Đông:
 * - Hero card: tiết khí hiện tại + đếm ngược tới tiết kế tiếp
 * - 4 nhóm theo mùa (Xuân/Hạ/Thu/Đông), mỗi nhóm 6 tiết
 * - Click 1 tiết → bottom dialog hiện chi tiết: ý nghĩa, nên làm, kiêng kỵ, ẩm thực
 */
@Composable
fun TietKhiScreen(
    onBackClick: () -> Unit = {},
) {
    val c = LichSoThemeColors.current

    // Tính tiết khí hiện tại từ ngày hôm nay + exact dates cho năm hiện tại
    val cal = remember { Calendar.getInstance() }
    val currentYear = cal.get(Calendar.YEAR)

    data class TermInfo(
        val currentTermName: String?,
        val daysUntilNext: Int,
        val nextTermName: String?,
        val nextTermDate: String?,          // "dd/mm" chính xác của tiết kế tiếp
        val exactDates: Map<String, String> // tên tiết → "dd/mm" chính xác năm nay
    )

    val termInfo = remember(currentYear) {
        val today = cal
        val info = TietKhiCalculator.getCurrentSolarTerm(
            today.get(Calendar.DAY_OF_MONTH),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.YEAR),
        )
        // Tính ngày chính xác cho tất cả tiết khí năm nay
        val allTermsThisYear = TietKhiCalculator.getAllSolarTerms(currentYear)
        val exactDates = allTermsThisYear.associate { term ->
            term.name to "%02d/%02d".format(term.dd, term.mm)
        }
        val nextDate = info.next?.let { "%02d/%02d".format(it.dd, it.mm) }
        TermInfo(info.current?.name, info.daysUntilNext, info.next?.name, nextDate, exactDates)
    }

    var selected by remember { mutableStateOf<TietKhiDetail?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground(c.bg),
    ) {
        AppTopBar(
            title = "24 Tiết Khí",
            subtitle = "Vòng năm Á Đông",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Hero — tiết khí hiện tại
            HeroCurrentTermCard(
                currentTermName = termInfo.currentTermName,
                daysUntilNext = termInfo.daysUntilNext,
                nextTermName = termInfo.nextTermName,
                nextTermDate = termInfo.nextTermDate,
                onClickDetail = { name ->
                    TietKhiCatalog.byName(name)?.let { selected = it }
                },
            )

            // 4 mùa
            Season.entries.forEach { season ->
                SeasonSection(
                    season = season,
                    items = TietKhiCatalog.bySeason(season),
                    currentName = termInfo.currentTermName,
                    exactDates = termInfo.exactDates,
                    onItemClick = { selected = it },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    selected?.let { detail ->
        TietKhiDetailDialog(detail = detail, onDismiss = { selected = null })
    }
}

@Composable
private fun HeroCurrentTermCard(
    currentTermName: String?,
    daysUntilNext: Int,
    nextTermName: String?,
    nextTermDate: String?,
    onClickDetail: (String) -> Unit,
) {
    val c = LichSoThemeColors.current
    val current = currentTermName?.let { TietKhiCatalog.byName(it) }

    val bg1 = current?.season?.let { seasonGradientStart(it) } ?: c.gold
    val bg2 = current?.season?.let { seasonGradientEnd(it) } ?: c.red

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(bg1, bg2)))
            .clickable(enabled = currentTermName != null) {
                currentTermName?.let(onClickDetail)
            }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "TIẾT KHÍ HÔM NAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f),
            )
            Text(
                currentTermName ?: "—",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            current?.let {
                Text(
                    "${it.season.emoji} ${it.season.displayName} · ${it.pinyin}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    it.meaning,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 19.sp,
                )
            }
            if (nextTermName != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val dateStr = if (nextTermDate != null) " ($nextTermDate)" else ""
                    Text(
                        "Còn $daysUntilNext ngày → $nextTermName$dateStr",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonSection(
    season: Season,
    items: List<TietKhiDetail>,
    currentName: String?,
    exactDates: Map<String, String>,
    onItemClick: (TietKhiDetail) -> Unit,
) {
    val c = LichSoThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(season.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "Mùa ${season.displayName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            )
        }
        Surface(
            color = c.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, c.border),
        ) {
            Column {
                items.forEachIndexed { idx, item ->
                    TietKhiRow(
                        detail = item,
                        isCurrent = item.name == currentName,
                        exactDate = exactDates[item.name],
                        onClick = { onItemClick(item) },
                    )
                    if (idx < items.lastIndex) {
                        HorizontalDivider(color = c.border)
                    }
                }
            }
        }
    }
}

@Composable
private fun TietKhiRow(
    detail: TietKhiDetail,
    isCurrent: Boolean,
    exactDate: String?,
    onClick: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val accent = seasonGradientStart(detail.season)
    val bg by animateColorAsState(
        if (isCurrent) accent.copy(alpha = 0.10f) else Color.Transparent,
        label = "rowbg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f))
                .border(
                    width = if (isCurrent) 2.dp else 0.dp,
                    color = if (isCurrent) accent else Color.Transparent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Hiển thị ngày chính xác (lấy dd từ exactDate "dd/mm") hoặc ngày xấp xỉ
            val displayDay = exactDate?.substringBefore("/")?.trimStart('0') ?: "%02d".format(detail.approxDay)
            Text(
                displayDay,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    detail.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                )
                if (isCurrent) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accent)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "Hôm nay",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
            Text(
                if (exactDate != null) "$exactDate dương · ${detail.pinyin}"
                else "≈ ${detail.approxDay}/${detail.month} dương · ${detail.pinyin}",
                fontSize = 11.sp,
                color = c.textTertiary,
            )
        }
        Text("›", fontSize = 18.sp, color = c.textTertiary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TietKhiDetailDialog(detail: TietKhiDetail, onDismiss: () -> Unit) {
    val c = LichSoThemeColors.current
    val accent = seasonGradientStart(detail.season)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = c.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f))
                            .border(2.dp, accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(detail.season.emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            detail.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        Text(
                            "${detail.pinyin} · ≈ ${detail.approxDay}/${detail.month} dương",
                            fontSize = 12.sp,
                            color = c.textTertiary,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = c.textSecondary)
                    }
                }

                // Meaning
                Surface(
                    color = accent.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                ) {
                    Text(
                        detail.meaning,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 14.sp,
                        color = c.textPrimary,
                        lineHeight = 20.sp,
                    )
                }

                // Recommended
                SectionList(
                    title = "Nên làm",
                    items = detail.recommended,
                    icon = Icons.Filled.CheckCircle,
                    accent = c.goodGreen,
                )

                // Avoid
                SectionList(
                    title = "Kiêng kỵ",
                    items = detail.avoid,
                    icon = Icons.Filled.Close,
                    accent = c.badRed,
                )

                // Food
                Surface(
                    color = c.gold.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, c.gold.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = c.gold,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Thực phẩm theo mùa",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textPrimary,
                            )
                            Text(
                                detail.seasonalFood,
                                fontSize = 13.sp,
                                color = c.textSecondary,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionList(
    title: String,
    items: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
) {
    val c = LichSoThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
            )
        }
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(start = 24.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("•", fontSize = 14.sp, color = accent, modifier = Modifier.padding(end = 6.dp))
                Text(
                    item,
                    fontSize = 13.sp,
                    color = c.textSecondary,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}

private fun seasonGradientStart(season: Season): Color = when (season) {
    Season.SPRING -> Color(0xFF43A047)
    Season.SUMMER -> Color(0xFFD84315)
    Season.AUTUMN -> Color(0xFFE65100)
    Season.WINTER -> Color(0xFF1565C0)
}

private fun seasonGradientEnd(season: Season): Color = when (season) {
    Season.SPRING -> Color(0xFFFFB300)
    Season.SUMMER -> Color(0xFFFF6F00)
    Season.AUTUMN -> Color(0xFF8D6E63)
    Season.WINTER -> Color(0xFF1A237E)
}
