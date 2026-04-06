package com.lichso.app.ui.screen.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.BookmarkDao
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// ═══════════════════════════════════════
// Models
// ═══════════════════════════════════════

enum class BookmarkCategory(val label: String, val emoji: String) {
    ALL("Tất cả", "📅"),
    GOOD_DAY("Ngày tốt", "✨"),
    HOLIDAY("Lễ / Giỗ", "🎉"),
    PERSONAL("Cá nhân", "💼")
}

enum class BookmarkFilterChip(val label: String, val emoji: String) {
    ALL("Tất cả", "📑"),
    WEDDING("Cưới hỏi", "💒"),
    BUSINESS("Kinh doanh", "🏢"),
    MEMORIAL("Giỗ chạp", "🕯️"),
    TRAVEL("Du lịch", "✈️"),
    HEALTH("Sức khỏe", "🏥")
}

enum class BookmarkSortMode {
    DATE_DESC, DATE_ASC, NAME
}

data class BookmarkDisplayItem(
    val entity: BookmarkEntity,
    val dayOfWeek: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarYear: Int,
    val lunarMonthName: String,
    val dayCanChi: String,
    val dayRating: String,       // "Rất tốt", "Tốt", "Trung bình", "Xấu"
    val solarHoliday: String?,
    val lunarHoliday: String?,
    val daysUntil: Long,         // negative = past, 0 = today, positive = upcoming
    val isUpcoming: Boolean,
    val isPast: Boolean,
    val category: BookmarkCategory
)

data class BookmarksUiState(
    val bookmarks: List<BookmarkDisplayItem> = emptyList(),
    val filteredBookmarks: List<BookmarkDisplayItem> = emptyList(),
    val totalCount: Int = 0,
    val goodDayCount: Int = 0,
    val holidayCount: Int = 0,
    val personalCount: Int = 0,
    val upcomingCount: Int = 0,
    val selectedCategory: BookmarkCategory = BookmarkCategory.ALL,
    val selectedChip: BookmarkFilterChip = BookmarkFilterChip.ALL,
    val searchQuery: String = "",
    val sortMode: BookmarkSortMode = BookmarkSortMode.DATE_DESC,
    val showMoreSheet: Boolean = false,
    val toastMessage: String? = null
)

// ═══════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val dayInfoProvider: DayInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val dayOfWeekNames = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")

    init {
        viewModelScope.launch {
            bookmarkDao.getAllBookmarks().collect { entities ->
                val today = LocalDate.now()
                val items = entities.map { entity -> enrichBookmark(entity, today) }
                val totalCount = items.size
                val goodDayCount = items.count { it.category == BookmarkCategory.GOOD_DAY }
                val holidayCount = items.count { it.category == BookmarkCategory.HOLIDAY }
                val personalCount = items.count { it.category == BookmarkCategory.PERSONAL }
                val upcomingCount = items.count { it.isUpcoming }

                _uiState.update { state ->
                    state.copy(
                        bookmarks = items,
                        totalCount = totalCount,
                        goodDayCount = goodDayCount,
                        holidayCount = holidayCount,
                        personalCount = personalCount,
                        upcomingCount = upcomingCount
                    )
                }
                applyFilters()
            }
        }
    }

    private fun enrichBookmark(entity: BookmarkEntity, today: LocalDate): BookmarkDisplayItem {
        val info = try {
            dayInfoProvider.getDayInfo(entity.solarDay, entity.solarMonth, entity.solarYear)
        } catch (_: Exception) { null }

        val lunar = info?.lunar
        val lunarDay = lunar?.day ?: 0
        val lunarMonth = lunar?.month ?: 0
        val lunarYear = lunar?.year ?: 0
        val lunarMonthName = lunar?.monthName ?: ""
        val dayCanChi = info?.dayCanChi ?: ""
        val dayRating = info?.dayRating?.label ?: "Trung bình"
        val dayOfWeek = if (info != null) dayOfWeekNames.getOrElse(info.dayOfWeekIndex) { "" } else ""
        val solarHoliday = info?.solarHoliday
        val lunarHoliday = info?.lunarHoliday
        val hasHoliday = solarHoliday != null || lunarHoliday != null

        val bookmarkDate = try {
            LocalDate.of(entity.solarYear, entity.solarMonth, entity.solarDay)
        } catch (_: Exception) { today }
        val daysUntil = ChronoUnit.DAYS.between(today, bookmarkDate)

        val category = when {
            hasHoliday -> BookmarkCategory.HOLIDAY
            dayRating in listOf("Rất tốt", "Tốt") -> BookmarkCategory.GOOD_DAY
            else -> BookmarkCategory.PERSONAL
        }

        return BookmarkDisplayItem(
            entity = entity,
            dayOfWeek = dayOfWeek,
            lunarDay = lunarDay,
            lunarMonth = lunarMonth,
            lunarYear = lunarYear,
            lunarMonthName = lunarMonthName,
            dayCanChi = dayCanChi,
            dayRating = dayRating,
            solarHoliday = solarHoliday,
            lunarHoliday = lunarHoliday,
            daysUntil = daysUntil,
            isUpcoming = daysUntil > 0,
            isPast = daysUntil < 0,
            category = category
        )
    }

    // ═══ Filter & Sort ═══

    fun selectCategory(category: BookmarkCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun selectChip(chip: BookmarkFilterChip) {
        _uiState.update { it.copy(selectedChip = chip) }
        applyFilters()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun toggleSort() {
        val next = when (_uiState.value.sortMode) {
            BookmarkSortMode.DATE_DESC -> BookmarkSortMode.DATE_ASC
            BookmarkSortMode.DATE_ASC -> BookmarkSortMode.NAME
            BookmarkSortMode.NAME -> BookmarkSortMode.DATE_DESC
        }
        _uiState.update { it.copy(sortMode = next) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var list = state.bookmarks

        // Category filter
        if (state.selectedCategory != BookmarkCategory.ALL) {
            list = list.filter { it.category == state.selectedCategory }
        }

        // Chip filter (by label keywords)
        if (state.selectedChip != BookmarkFilterChip.ALL) {
            val keywords = when (state.selectedChip) {
                BookmarkFilterChip.WEDDING -> listOf("cưới", "hỏi", "đính hôn", "wedding")
                BookmarkFilterChip.BUSINESS -> listOf("khai trương", "công ty", "kinh doanh", "business")
                BookmarkFilterChip.MEMORIAL -> listOf("giỗ", "cúng", "tưởng niệm", "memorial")
                BookmarkFilterChip.TRAVEL -> listOf("du lịch", "travel", "nghỉ")
                BookmarkFilterChip.HEALTH -> listOf("khám", "sức khỏe", "bệnh viện", "health")
                else -> emptyList()
            }
            if (keywords.isNotEmpty()) {
                list = list.filter { item ->
                    val text = "${item.entity.label} ${item.entity.note} ${item.solarHoliday ?: ""} ${item.lunarHoliday ?: ""}".lowercase()
                    keywords.any { text.contains(it) }
                }
            }
        }

        // Search filter
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase()
            list = list.filter { item ->
                val text = "${item.entity.label} ${item.entity.note} ${item.solarHoliday ?: ""} ${item.lunarHoliday ?: ""} " +
                        "${item.dayOfWeek} ${item.dayCanChi} ${item.entity.solarDay}/${item.entity.solarMonth}/${item.entity.solarYear}"
                text.lowercase().contains(q)
            }
        }

        // Sort
        list = when (state.sortMode) {
            BookmarkSortMode.DATE_DESC -> list.sortedWith(
                compareByDescending<BookmarkDisplayItem> { it.entity.solarYear }
                    .thenByDescending { it.entity.solarMonth }
                    .thenByDescending { it.entity.solarDay }
            )
            BookmarkSortMode.DATE_ASC -> list.sortedWith(
                compareBy<BookmarkDisplayItem> { it.entity.solarYear }
                    .thenBy { it.entity.solarMonth }
                    .thenBy { it.entity.solarDay }
            )
            BookmarkSortMode.NAME -> list.sortedBy { it.entity.label.lowercase() }
        }

        _uiState.update { it.copy(filteredBookmarks = list) }
    }

    // ═══ Actions ═══

    fun deleteBookmark(item: BookmarkDisplayItem) {
        viewModelScope.launch {
            bookmarkDao.delete(item.entity)
            _uiState.update { it.copy(toastMessage = "Đã xóa đánh dấu") }
        }
    }

    fun deleteAllBookmarks() {
        viewModelScope.launch {
            _uiState.value.bookmarks.forEach { item ->
                bookmarkDao.delete(item.entity)
            }
            _uiState.update { it.copy(toastMessage = "Đã xóa tất cả đánh dấu") }
        }
    }

    fun showMoreSheet() = _uiState.update { it.copy(showMoreSheet = true) }
    fun hideMoreSheet() = _uiState.update { it.copy(showMoreSheet = false) }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
