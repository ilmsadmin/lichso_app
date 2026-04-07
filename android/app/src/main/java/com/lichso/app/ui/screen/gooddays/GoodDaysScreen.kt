package com.lichso.app.ui.screen.gooddays

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.domain.model.DayInfo
import com.lichso.app.ui.screen.home.HomeViewModel
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import java.time.LocalDate

enum class DayQuality { GOOD, BAD, NEUTRAL }

data class GoodDayItem(
    val dayNum: String,
    val weekday: String,
    val lunarDate: String,
    val quality: DayQuality,
    val qualityLabel: String,
    val canChi: String,
    val gioTot: String,
    val tags: List<GoodDayTag>,
    val nenLam: List<String>,
    val khongNen: List<String>
)

data class GoodDayTag(
    val text: String,
    val type: TagType
)

enum class TagType { GOOD, AVOID, EVENT }

data class FilterChipData(
    val icon: ImageVector,
    val label: String,
    val filterKey: String // keyword to match in nenLam/khongNen
)

@Composable
fun GoodDaysScreen(onBackClick: () -> Unit = {}, viewModel: HomeViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableIntStateOf(0) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var qualityFilter by remember { mutableStateOf<DayQuality?>(null) } // null = all

    val filters = listOf(
        FilterChipData(Icons.Outlined.CalendarToday, "Tháng này", "all"),
        FilterChipData(Icons.Outlined.Store, "Khai trương", "khaiTruong"),
        FilterChipData(Icons.Outlined.Home, "Xây nhà", "xayNha"),
        FilterChipData(Icons.Outlined.Favorite, "Cưới hỏi", "cuoiHoi"),
        FilterChipData(Icons.Outlined.Flight, "Xuất hành", "xuatHanh"),
    )

    // Build real good days from DayInfoProvider
    val allDays = remember(uiState.currentMonth, uiState.currentYear) {
        buildGoodDaysFromReal(uiState.currentYear, uiState.currentMonth)
    }

    // Apply filter
    val filteredDays = remember(allDays, selectedFilter, qualityFilter) {
        val key = filters[selectedFilter].filterKey
        var result = if (key == "all") {
            allDays
        } else {
            allDays.filter { day ->
                when (key) {
                    "khaiTruong" -> day.nenLam.any { it.contains("Khai trương") || it.contains("Mở cửa") || it.contains("Giao dịch") }
                    "xayNha" -> day.nenLam.any { it.contains("Động thổ") || it.contains("Xây") || it.contains("Dựng cột") || it.contains("Nhập trạch") }
                    "cuoiHoi" -> day.nenLam.any { it.contains("Cưới") || it.contains("hỏi") || it.contains("Hôn") }
                    "xuatHanh" -> day.nenLam.any { it.contains("Xuất hành") || it.contains("Du lịch") || it.contains("Di chuyển") }
                    else -> true
                }
            }
        }
        // Apply quality filter
        if (qualityFilter != null) {
            result = result.filter { it.quality == qualityFilter }
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        AppTopBar(
            title = "Ngày tốt / xấu",
            onBackClick = onBackClick,
            actions = {
                HeaderIconButton(
                    icon = Icons.Filled.FilterList,
                    contentDescription = "Lọc",
                    onClick = { showFilterSheet = true }
                )
            }
        )

        // ═══ FILTER CHIPS ═══
        LazyRow(
            modifier = Modifier.padding(top = 12.dp, bottom = 14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters.size) { index ->
                val isActive = selectedFilter == index
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isActive) c.primary else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isActive) c.primary else c.outlineVariant,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        filters[index].icon,
                        contentDescription = null,
                        tint = if (isActive) Color.White else c.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        filters[index].label,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) Color.White else c.textTertiary
                        )
                    )
                }
            }
        }

        // ═══ DAY LIST ═══
        if (filteredDays.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EventBusy,
                        contentDescription = null,
                        tint = c.outlineVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Không có ngày phù hợp trong tháng này",
                        style = TextStyle(fontSize = 14.sp, color = c.textTertiary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredDays) { day ->
                    DayCard(day = day)
                }
            }
        }
    }

    // ═══ FILTER BOTTOM SHEET ═══
    if (showFilterSheet) {
        GoodDayFilterSheet(
            currentQuality = qualityFilter,
            currentActivityFilter = selectedFilter,
            filters = filters,
            goodCount = allDays.count { it.quality == DayQuality.GOOD },
            badCount = allDays.count { it.quality == DayQuality.BAD },
            neutralCount = allDays.count { it.quality == DayQuality.NEUTRAL },
            onApply = { quality, activityIdx ->
                qualityFilter = quality
                selectedFilter = activityIdx
                showFilterSheet = false
            },
            onReset = {
                qualityFilter = null
                selectedFilter = 0
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun DayCard(day: GoodDayItem) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Date column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                day.dayNum,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (day.quality) {
                        DayQuality.GOOD -> c.goodGreen
                        DayQuality.BAD -> c.primary
                        DayQuality.NEUTRAL -> c.gold
                    },
                    lineHeight = 28.sp
                )
            )
            Text(day.weekday, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.textTertiary))
            Text(day.lunarDate, style = TextStyle(fontSize = 10.sp, color = c.outline))
        }

        // Info column
        Column(modifier = Modifier.weight(1f)) {
            // Quality badge
            QualityBadge(quality = day.quality, label = day.qualityLabel)

            Spacer(modifier = Modifier.height(3.dp))

            // Can Chi + Giờ tốt
            Text(
                "${day.canChi} · ${day.gioTot}",
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                day.tags.forEach { tag ->
                    val (tagBg, tagColor) = when (tag.type) {
                        TagType.GOOD -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        TagType.AVOID -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                        TagType.EVENT -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                    }
                    Box(
                        modifier = Modifier
                            .background(tagBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            tag.text,
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tagColor)
                        )
                    }
                }
            }
        }

        // Arrow
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = c.outlineVariant,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(20.dp)
        )
    }
}

@Composable
private fun QualityBadge(quality: DayQuality, label: String) {
    val (bg, textColor) = when (quality) {
        DayQuality.GOOD -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        DayQuality.BAD -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        DayQuality.NEUTRAL -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
    }
    val symbol = when (quality) {
        DayQuality.GOOD -> "✦"
        DayQuality.BAD -> "✕"
        DayQuality.NEUTRAL -> "◈"
    }
    val prefix = when (quality) {
        DayQuality.GOOD -> "Hoàng Đạo"
        DayQuality.BAD -> "Hắc Đạo"
        DayQuality.NEUTRAL -> "Bình thường"
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            "$symbol $prefix",
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
        )
    }
}

// Build real good days data using DayInfoProvider
private fun buildGoodDaysFromReal(year: Int, month: Int): List<GoodDayItem> {
    val provider = DayInfoProvider()
    val weekdayNames = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật")
    val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()

    return (1..daysInMonth).map { d ->
        val info = provider.getDayInfo(d, month, year)
        val dow = LocalDate.of(year, month, d).dayOfWeek
        val weekdayIdx = dow.value - 1 // 0=Mon..6=Sun

        // Determine quality from dayRating and trucNgay/saoChieu
        val quality = when {
            info.dayRating.label == "Xấu" || info.activities.isXauDay -> DayQuality.BAD
            info.dayRating.label == "Rất tốt" || info.dayRating.label == "Tốt" -> DayQuality.GOOD
            else -> DayQuality.NEUTRAL
        }

        // Build tags from real nenLam/khongNen
        val tags = mutableListOf<GoodDayTag>()

        // Add holidays as EVENT tags
        info.solarHoliday?.let { tags.add(GoodDayTag(it, TagType.EVENT)) }
        info.lunarHoliday?.let { tags.add(GoodDayTag(it, TagType.EVENT)) }

        // Add "nên làm" as GOOD tags
        info.activities.nenLam.take(3).forEach { activity ->
            tags.add(GoodDayTag(activity.replace(", ", " · "), TagType.GOOD))
        }

        // Add "không nên" as AVOID tags (show "Kỵ ..." prefix)
        info.activities.khongNen
            .filter { !it.startsWith("Nguyệt kỵ") && !it.startsWith("Tam nương") }
            .take(2)
            .forEach { activity ->
                tags.add(GoodDayTag("Kỵ $activity", TagType.AVOID))
            }

        // Special warnings
        if (info.activities.isNguyetKy) {
            tags.add(0, GoodDayTag("Nguyệt kỵ", TagType.AVOID))
        }
        if (info.activities.isTamNuong) {
            tags.add(0, GoodDayTag("Tam nương", TagType.AVOID))
        }

        // Giờ hoàng đạo text
        val gioTot = "Giờ tốt: " + info.gioHoangDao.take(3).joinToString(", ") { it.name }

        GoodDayItem(
            dayNum = "%02d".format(d),
            weekday = weekdayNames[weekdayIdx],
            lunarDate = "${info.lunar.day}/${info.lunar.month} ÂL",
            quality = quality,
            qualityLabel = when (quality) {
                DayQuality.GOOD -> "Hoàng Đạo"
                DayQuality.BAD -> "Hắc Đạo"
                DayQuality.NEUTRAL -> "Bình thường"
            },
            canChi = info.dayCanChi,
            gioTot = gioTot,
            tags = tags,
            nenLam = info.activities.nenLam,
            khongNen = info.activities.khongNen
        )
    }
}

// ══════════════════════════════════════════
// FILTER BOTTOM SHEET
// ══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoodDayFilterSheet(
    currentQuality: DayQuality?,
    currentActivityFilter: Int,
    filters: List<FilterChipData>,
    goodCount: Int,
    badCount: Int,
    neutralCount: Int,
    onApply: (quality: DayQuality?, activityIdx: Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val sheetState = rememberModalBottomSheetState()
    var selectedQuality by remember { mutableStateOf(currentQuality) }
    var selectedActivity by remember { mutableIntStateOf(currentActivityFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(
                "Lọc ngày",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Quality filter
            Text(
                "Loại ngày",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                data class QualityOption(val quality: DayQuality?, val label: String, val count: Int, val color: Color)
                val options = listOf(
                    QualityOption(null, "Tất cả", goodCount + badCount + neutralCount, c.primary),
                    QualityOption(DayQuality.GOOD, "✦ Hoàng Đạo", goodCount, Color(0xFF2E7D32)),
                    QualityOption(DayQuality.BAD, "✕ Hắc Đạo", badCount, Color(0xFFC62828)),
                    QualityOption(DayQuality.NEUTRAL, "◈ Bình thường", neutralCount, Color(0xFFF57F17)),
                )
                options.forEach { opt ->
                    val isSelected = selectedQuality == opt.quality
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) opt.color.copy(alpha = 0.12f) else c.surfaceContainer,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) opt.color else c.outlineVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedQuality = opt.quality }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${opt.count}",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) opt.color else c.textPrimary)
                        )
                        Text(
                            opt.label,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = c.textSecondary),
                            maxLines = 1
                        )
                    }
                }
            }

            // Activity filter
            Text(
                "Mục đích",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                filters.forEachIndexed { index, filter ->
                    val isSelected = selectedActivity == index
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) c.primary else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) c.primary else c.outlineVariant,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedActivity = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            filter.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else c.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            filter.label,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else c.textTertiary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, c.outlineVariant)
                ) {
                    Text("Đặt lại", color = c.textSecondary)
                }
                Button(
                    onClick = { onApply(selectedQuality, selectedActivity) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    Text("Áp dụng", color = Color.White)
                }
            }
        }
    }
}
