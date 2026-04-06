package com.lichso.app.ui.screen.bookmarks

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import kotlin.math.abs

// ══════════════════════════════════════════
// Color constants
// ══════════════════════════════════════════
private val GoodDayGreen = Color(0xFF2E7D32)
private val HolidayOrange = Color(0xFFE65100)
private val PersonalBlue = Color(0xFF1565C0)
private val GoldAccent = Color(0xFFD4A017)
private val DeepRed = Color(0xFF8B0000)

// Strip colors
private val StripRed = Color(0xFFB71C1C)
private val StripGold = Color(0xFFD4A017)
private val StripGreen = Color(0xFF2E7D32)
private val StripBlue = Color(0xFF1565C0)
private val StripPurple = Color(0xFF7B1FA2)
private val StripOrange = Color(0xFFE65100)

// ══════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBackClick: () -> Unit = {},
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit = { _, _, _ -> },
    onAddBookmark: () -> Unit = {},
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Toast feedback
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    // More Options Bottom Sheet
    if (state.showMoreSheet) {
        MoreOptionsSheet(
            onDismiss = { viewModel.hideMoreSheet() },
            onSortByDate = {
                viewModel.toggleSort()
                viewModel.hideMoreSheet()
            },
            onDeleteAll = {
                viewModel.deleteAllBookmarks()
                viewModel.hideMoreSheet()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            // ═══ HEADER ═══
            BookmarksHeader(
                totalCount = state.totalCount,
                upcomingCount = state.upcomingCount,
                onBackClick = onBackClick,
                onSortClick = { viewModel.toggleSort() },
                onMoreClick = { viewModel.showMoreSheet() }
            )

            // ═══ STATS ROW ═══
            StatsRow(
                totalCount = state.totalCount,
                goodDayCount = state.goodDayCount,
                holidayCount = state.holidayCount,
                personalCount = state.personalCount,
                selectedCategory = state.selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it) }
            )

            // ═══ FILTER CHIPS ═══
            FilterChipsRow(
                selectedChip = state.selectedChip,
                onChipSelect = { viewModel.selectChip(it) },
                totalCount = state.totalCount
            )

            // ═══ SEARCH BAR ═══
            SearchRow(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onTuneClick = { viewModel.showMoreSheet() }
            )

            // ═══ BOOKMARK LIST ═══
            if (state.filteredBookmarks.isEmpty() && state.bookmarks.isEmpty()) {
                // Empty state
                EmptyBookmarksState(onAddClick = onAddBookmark)
            } else if (state.filteredBookmarks.isEmpty()) {
                // No results for filter
                EmptyFilterState()
            } else {
                BookmarkList(
                    bookmarks = state.filteredBookmarks,
                    onItemClick = { item ->
                        onDateSelected(item.entity.solarYear, item.entity.solarMonth, item.entity.solarDay)
                    },
                    onDeleteItem = { viewModel.deleteBookmark(it) }
                )
            }
        }

        // ═══ FAB ═══
        FloatingActionButton(
            onClick = onAddBookmark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp, 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(c.primary, Color(0xFFC62828))),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BookmarkAdd, contentDescription = "Thêm đánh dấu", modifier = Modifier.size(26.dp))
            }
        }
    }
}

// ══════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════

@Composable
private fun BookmarksHeader(
    totalCount: Int,
    upcomingCount: Int,
    onBackClick: () -> Unit,
    onSortClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    AppTopBar(
        title = "Ngày đã lưu",
        subtitle = "$totalCount ngày đã lưu · $upcomingCount sắp tới",
        onBackClick = onBackClick,
        actions = {
            HeaderIconButton(
                icon = Icons.Filled.SwapVert,
                contentDescription = "Sắp xếp",
                onClick = onSortClick
            )
            HeaderIconButton(
                icon = Icons.Filled.MoreVert,
                contentDescription = "Tùy chọn",
                onClick = onMoreClick
            )
        }
    )
}

// ══════════════════════════════════════════
// STATS ROW
// ══════════════════════════════════════════

@Composable
private fun StatsRow(
    totalCount: Int,
    goodDayCount: Int,
    holidayCount: Int,
    personalCount: Int,
    selectedCategory: BookmarkCategory,
    onCategorySelect: (BookmarkCategory) -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            emoji = "📅", count = totalCount, label = "TẤT CẢ",
            isActive = selectedCategory == BookmarkCategory.ALL,
            valueColor = c.primary,
            onClick = { onCategorySelect(BookmarkCategory.ALL) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "✨", count = goodDayCount, label = "NGÀY TỐT",
            isActive = selectedCategory == BookmarkCategory.GOOD_DAY,
            valueColor = GoodDayGreen,
            onClick = { onCategorySelect(BookmarkCategory.GOOD_DAY) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "🎉", count = holidayCount, label = "LỄ / GIỖ",
            isActive = selectedCategory == BookmarkCategory.HOLIDAY,
            valueColor = HolidayOrange,
            onClick = { onCategorySelect(BookmarkCategory.HOLIDAY) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "💼", count = personalCount, label = "CÁ NHÂN",
            isActive = selectedCategory == BookmarkCategory.PERSONAL,
            valueColor = PersonalBlue,
            onClick = { onCategorySelect(BookmarkCategory.PERSONAL) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    emoji: String,
    count: Int,
    label: String,
    isActive: Boolean,
    valueColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(
                if (isActive) c.primaryContainer else c.surfaceContainer,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isActive) c.primary else c.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            "$count",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) c.primary else valueColor
            )
        )
        Text(
            label,
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.outline,
                letterSpacing = 0.3.sp
            ),
            maxLines = 1
        )
    }
}

// ══════════════════════════════════════════
// FILTER CHIPS ROW
// ══════════════════════════════════════════

@Composable
private fun FilterChipsRow(
    selectedChip: BookmarkFilterChip,
    onChipSelect: (BookmarkFilterChip) -> Unit,
    totalCount: Int
) {
    val c = LichSoThemeColors.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        items(BookmarkFilterChip.entries) { chip ->
            val isActive = chip == selectedChip
            Row(
                modifier = Modifier
                    .background(
                        if (isActive) c.primary else c.surfaceContainer,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.5.dp,
                        if (isActive) c.primary else c.outlineVariant,
                        RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onChipSelect(chip) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(chip.emoji, fontSize = 13.sp)
                Text(
                    chip.label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) Color.White else c.textSecondary
                    )
                )
                if (chip == BookmarkFilterChip.ALL && totalCount > 0) {
                    Text(
                        "$totalCount",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (isActive) Color.White.copy(alpha = 0.85f) else c.outline,
                        modifier = Modifier
                            .background(
                                if (isActive) Color.White.copy(alpha = 0.25f) else c.outlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// SEARCH ROW
// ══════════════════════════════════════════

@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onTuneClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search box
        Row(
            modifier = Modifier
                .weight(1f)
                .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                .border(1.5.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Search, "Tìm kiếm", tint = c.outline, modifier = Modifier.size(20.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                "Tìm ngày đã lưu...",
                                style = TextStyle(fontSize = 14.sp, color = c.outline)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close, "Xóa",
                    tint = c.outline,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") }
                )
            }
        }

        // Tune button
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(c.surfaceContainer, RoundedCornerShape(12.dp))
                .border(1.5.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTuneClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Tune, "Lọc", tint = c.textSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// ══════════════════════════════════════════
// BOOKMARK LIST
// ══════════════════════════════════════════

@Composable
private fun BookmarkList(
    bookmarks: List<BookmarkDisplayItem>,
    onItemClick: (BookmarkDisplayItem) -> Unit,
    onDeleteItem: (BookmarkDisplayItem) -> Unit
) {
    val upcoming = bookmarks.filter { it.isUpcoming }
    val today = bookmarks.filter { it.daysUntil == 0L }
    val past = bookmarks.filter { it.isPast }

    // Find featured item (nearest upcoming)
    val featured = upcoming.minByOrNull { it.daysUntil }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Featured card
        if (featured != null) {
            item(key = "featured") {
                FeaturedSavedCard(item = featured, onClick = { onItemClick(featured) })
            }
        }

        // Upcoming section
        if (upcoming.isNotEmpty()) {
            item(key = "section_upcoming") {
                SectionDivider("📌 Sắp tới")
            }
            items(upcoming, key = { "up_${it.entity.id}" }) { item ->
                BookmarkCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onDelete = { onDeleteItem(item) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // Today section
        if (today.isNotEmpty()) {
            item(key = "section_today") {
                SectionDivider("📍 Hôm nay")
            }
            items(today, key = { "td_${it.entity.id}" }) { item ->
                BookmarkCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onDelete = { onDeleteItem(item) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // Past section
        if (past.isNotEmpty()) {
            item(key = "section_past") {
                SectionDivider("🕐 Đã qua")
            }
            items(past, key = { "past_${it.entity.id}" }) { item ->
                BookmarkCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onDelete = { onDeleteItem(item) },
                    dimmed = true
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // Bottom spacing for FAB
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ══════════════════════════════════════════
// SECTION DIVIDER
// ══════════════════════════════════════════

@Composable
private fun SectionDivider(text: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = c.outline,
                letterSpacing = 0.5.sp
            )
        )
        HorizontalDivider(color = c.outlineVariant, modifier = Modifier.weight(1f))
    }
}

// ══════════════════════════════════════════
// FEATURED SAVED CARD
// ══════════════════════════════════════════

@Composable
private fun FeaturedSavedCard(
    item: BookmarkDisplayItem,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    val displayTitle = item.solarHoliday ?: item.lunarHoliday ?: item.entity.label.ifEmpty {
        "${"%02d".format(item.entity.solarDay)}/${"%02d".format(item.entity.solarMonth)}/${item.entity.solarYear}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))),
                RoundedCornerShape(20.dp)
            )
            .border(1.5.dp, Color(0xFFFFE082), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(GoldAccent, Color(0xFFB8860B))),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("⭐", fontSize = 26.sp)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "SẮP TỚI GẦN NHẤT",
                style = TextStyle(
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = HolidayOrange, letterSpacing = 0.5.sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                displayTitle,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "${"%02d".format(item.entity.solarDay)}/${"%02d".format(item.entity.solarMonth)}/${item.entity.solarYear} · ${item.dayOfWeek}",
                style = TextStyle(fontSize = 11.sp, color = c.textSecondary)
            )
        }

        // Countdown
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${item.daysUntil}",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HolidayOrange)
            )
            Text(
                "ngày",
                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = HolidayOrange)
            )
        }
    }
}

// ══════════════════════════════════════════
// BOOKMARK CARD
// ══════════════════════════════════════════

@Composable
private fun BookmarkCard(
    item: BookmarkDisplayItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    dimmed: Boolean = false
) {
    val c = LichSoThemeColors.current
    val alpha = if (dimmed) 0.65f else 1f

    val stripColor = when (item.category) {
        BookmarkCategory.HOLIDAY -> StripOrange
        BookmarkCategory.GOOD_DAY -> StripGreen
        BookmarkCategory.PERSONAL -> StripBlue
        BookmarkCategory.ALL -> StripRed
    }

    val badgeBg = when {
        item.solarHoliday != null || item.lunarHoliday != null ->
            Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
        item.dayRating in listOf("Rất tốt", "Tốt") ->
            Brush.linearGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))
        item.dayRating == "Xấu" ->
            Brush.linearGradient(listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)))
        else -> Brush.linearGradient(listOf(c.primaryContainer, c.primaryContainer))
    }

    val dayColor = when {
        item.solarHoliday != null || item.lunarHoliday != null -> HolidayOrange
        item.dayRating in listOf("Rất tốt", "Tốt") -> GoodDayGreen
        item.dayRating == "Xấu" -> Color(0xFFC62828)
        else -> c.primary
    }

    val qualityDotColor = when (item.dayRating) {
        "Rất tốt", "Tốt" -> Color(0xFF4CAF50)
        "Xấu" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(20.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
                .padding(start = 4.dp)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(stripColor, RoundedCornerShape(2.dp))
            )

            // Date badge
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .background(badgeBg, RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Quality dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(6.dp)
                            .background(qualityDotColor, CircleShape)
                    )

                    Text(
                        "${item.entity.solarDay}",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = dayColor)
                    )
                    Text(
                        "Th${item.entity.solarMonth}",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
                    )
                    Text(
                        item.dayOfWeek.take(4).uppercase(),
                        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = c.outline)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    item.entity.label.ifEmpty {
                        item.solarHoliday ?: item.lunarHoliday
                        ?: "${"%02d".format(item.entity.solarDay)}/${"%02d".format(item.entity.solarMonth)}/${item.entity.solarYear}"
                    },
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )

                // Lunar info
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.NightsStay, null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                    Text(
                        "${item.lunarDay}/${item.lunarMonth} Âm · ${item.dayCanChi}",
                        style = TextStyle(fontSize = 11.sp, color = c.textSecondary)
                    )
                }

                // Note
                if (item.entity.note.isNotEmpty()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        item.entity.note,
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary, lineHeight = 17.sp),
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }

                // Tags
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Day rating tag
                    TagChip(
                        text = item.dayRating,
                        bgColor = when (item.dayRating) {
                            "Rất tốt", "Tốt" -> Color(0xFFE8F5E9)
                            "Xấu" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFFFF8E1)
                        },
                        textColor = when (item.dayRating) {
                            "Rất tốt", "Tốt" -> GoodDayGreen
                            "Xấu" -> Color(0xFFC62828)
                            else -> HolidayOrange
                        }
                    )

                    // Can Chi tag
                    if (item.dayCanChi.isNotEmpty()) {
                        TagChip(
                            text = item.dayCanChi,
                            bgColor = c.primary.copy(alpha = 0.08f),
                            textColor = c.primary
                        )
                    }

                    // Holiday tags
                    item.solarHoliday?.let {
                        TagChip(
                            text = it,
                            bgColor = Color(0xFFFFF3E0),
                            textColor = HolidayOrange
                        )
                    }
                    item.lunarHoliday?.let {
                        TagChip(
                            text = it,
                            bgColor = Color(0xFFF3E5F5),
                            textColor = Color(0xFF7B1FA2)
                        )
                    }
                }
            }

            // Right actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                // Time label
                val timeText = when {
                    item.daysUntil == 0L -> "Hôm nay"
                    item.daysUntil > 0 -> "Còn ${item.daysUntil} ngày"
                    else -> "${abs(item.daysUntil)} ngày trước"
                }
                Text(
                    timeText,
                    style = TextStyle(fontSize = 10.sp, color = c.outline),
                    maxLines = 1
                )

                // Bookmark icon (remove)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Bookmark, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                }

                // Arrow
                Icon(Icons.Filled.ChevronRight, null, tint = c.outlineVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TagChip(text: String, bgColor: Color, textColor: Color) {
    Text(
        text,
        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor),
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

// ══════════════════════════════════════════
// EMPTY STATES
// ══════════════════════════════════════════

@Composable
private fun EmptyBookmarksState(onAddClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(c.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.BookmarkBorder, null, tint = c.outline, modifier = Modifier.size(36.dp))
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Chưa có ngày nào được lưu",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Lưu những ngày quan trọng khi xem chi tiết ngày trên lịch để dễ dàng tra cứu lại",
            style = TextStyle(fontSize = 13.sp, color = c.outline, lineHeight = 20.sp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .background(c.primary, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { onAddClick() }
                .padding(horizontal = 28.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(
                "Mở lịch",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            )
        }
    }
}

@Composable
private fun EmptyFilterState() {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.SearchOff, null, tint = c.outline, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            "Không tìm thấy kết quả",
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
        )
        Text(
            "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm",
            style = TextStyle(fontSize = 13.sp, color = c.outline)
        )
    }
}

// ══════════════════════════════════════════
// MORE OPTIONS BOTTOM SHEET
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreOptionsSheet(
    onDismiss: () -> Unit,
    onSortByDate: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val c = LichSoThemeColors.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 0.dp)) {
            Text(
                "Tùy chọn",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SheetOption(
                icon = Icons.Filled.SwapVert,
                text = "Sắp xếp theo ngày",
                onClick = onSortByDate
            )
            SheetOption(
                icon = Icons.Filled.FilterList,
                text = "Lọc theo danh mục",
                onClick = onDismiss
            )
            SheetOption(
                icon = Icons.Filled.FileDownload,
                text = "Xuất danh sách",
                onClick = onDismiss
            )

            HorizontalDivider(
                color = c.outlineVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            SheetOption(
                icon = Icons.Filled.SelectAll,
                text = "Chọn nhiều ngày",
                onClick = onDismiss
            )
            SheetOption(
                icon = Icons.Filled.Share,
                text = "Chia sẻ danh sách",
                onClick = onDismiss
            )

            HorizontalDivider(
                color = c.outlineVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            SheetOption(
                icon = Icons.Filled.DeleteSweep,
                text = "Xóa tất cả đánh dấu",
                isDanger = true,
                onClick = onDeleteAll
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SheetOption(
    icon: ImageVector,
    text: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    val color = if (isDanger) Color(0xFFC62828) else c.textPrimary
    val iconColor = if (isDanger) Color(0xFFC62828) else c.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        Text(
            text,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
        )
    }
}


