package com.lichso.app.ui.screen.search

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// ═══════════════════════════════════════
// Search Result Model
// ═══════════════════════════════════════

enum class SearchResultType { HOLIDAY, DATE, LUNAR, ZODIAC }

data class SearchResult(
    val title: String,
    val titleHighlight: String = "",   // phần cần highlight
    val description: String,
    val type: SearchResultType,
    val solarDay: Int = 0,
    val solarMonth: Int = 0,
    val solarYear: Int = 0
)

data class LunarConversion(
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarYear: Int,
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val dayOfWeek: String
)

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val lunarConversion: LunarConversion? = null,
    val isSearching: Boolean = false
)

// ═══════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dayInfoProvider: DayInfoProvider
) : ViewModel() {

    companion object {
        private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")
        private const val MAX_RECENT = 10
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val dayOfWeekNames = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")

    init {
        loadRecentSearches()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.isNotBlank()) {
            viewModelScope.launch {
                withContext(Dispatchers.Default) { performSearch(query.trim()) }
            }
        } else {
            _uiState.update { it.copy(results = emptyList(), lunarConversion = null) }
        }
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList(), lunarConversion = null) }
    }

    fun saveSearchToRecent(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            val current = _uiState.value.recentSearches.toMutableList()
            current.remove(query)
            current.add(0, query)
            if (current.size > MAX_RECENT) current.removeAt(current.size - 1)
            _uiState.update { it.copy(recentSearches = current) }
            persistRecentSearches(current)
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            val current = _uiState.value.recentSearches.toMutableList()
            current.remove(query)
            _uiState.update { it.copy(recentSearches = current) }
            persistRecentSearches(current)
        }
    }

    fun clearAllRecent() {
        viewModelScope.launch {
            _uiState.update { it.copy(recentSearches = emptyList()) }
            persistRecentSearches(emptyList())
        }
    }

    // ═══ Search Logic ═══

    private fun performSearch(query: String) {
        val results = mutableListOf<SearchResult>()
        var lunarConversion: LunarConversion? = null

        // 1. Search holidays by name
        results.addAll(searchHolidays(query))

        // 2. Parse date input: dd/mm, dd/mm/yyyy
        val dateMatch = Regex("""^(\d{1,2})/(\d{1,2})(?:/(\d{2,4}))?$""").find(query)
        if (dateMatch != null) {
            val day = dateMatch.groupValues[1].toIntOrNull() ?: 0
            val month = dateMatch.groupValues[2].toIntOrNull() ?: 0
            val yearStr = dateMatch.groupValues[3]
            val year = when {
                yearStr.isEmpty() -> LocalDate.now().year
                yearStr.length == 2 -> 2000 + (yearStr.toIntOrNull() ?: 0)
                else -> yearStr.toIntOrNull() ?: LocalDate.now().year
            }

            if (day in 1..31 && month in 1..12 && year in 1900..2100) {
                // Solar date info
                try {
                    val info = dayInfoProvider.getDayInfo(day, month, year)
                    val dayOfWeek = dayOfWeekNames.getOrElse(info.dayOfWeekIndex) { "" }
                    results.add(
                        SearchResult(
                            title = String.format("%02d/%02d/%d", day, month, year),
                            description = "$dayOfWeek · Âm lịch: ${info.lunar.day}/${info.lunar.month}/${info.lunar.year} · ${info.dayCanChi}",
                            type = SearchResultType.DATE,
                            solarDay = day,
                            solarMonth = month,
                            solarYear = year
                        )
                    )
                    // Holiday on that date?
                    info.solarHoliday?.let { hol ->
                        results.add(
                            SearchResult(
                                title = hol,
                                description = String.format("%02d/%02d/%d Dương lịch", day, month, year),
                                type = SearchResultType.HOLIDAY,
                                solarDay = day, solarMonth = month, solarYear = year
                            )
                        )
                    }
                    info.lunarHoliday?.let { hol ->
                        results.add(
                            SearchResult(
                                title = hol,
                                description = "${info.lunar.day}/${info.lunar.month} Âm lịch",
                                type = SearchResultType.HOLIDAY,
                                solarDay = day, solarMonth = month, solarYear = year
                            )
                        )
                    }
                } catch (_: Exception) { }
            }
        }

        // 3. Check for "âm lịch" / "lunar" conversion keywords
        val queryLower = query.lowercase()
        if (queryLower.contains("âm lịch") || queryLower.contains("am lich") || queryLower.contains("âm")) {
            val lunarDateMatch = Regex("""(\d{1,2})/(\d{1,2})""").find(query)
            if (lunarDateMatch != null) {
                val lunarDay = lunarDateMatch.groupValues[1].toIntOrNull() ?: 0
                val lunarMonth = lunarDateMatch.groupValues[2].toIntOrNull() ?: 0
                if (lunarDay in 1..30 && lunarMonth in 1..12) {
                    val year = LocalDate.now().year
                    val (sDay, sMonth, sYear) = LunarCalendarUtil.convertLunar2Solar(
                        lunarDay, lunarMonth, year, 0
                    )
                    if (sDay > 0) {
                        val sDate = LocalDate.of(sYear, sMonth, sDay)
                        val dow = dayOfWeekNames.getOrElse(sDate.dayOfWeek.value - 1) { "" }
                        results.add(
                            SearchResult(
                                title = "${lunarDay}/${lunarMonth} Âm lịch năm $year",
                                description = "Dương lịch: ${String.format("%02d/%02d/%d", sDay, sMonth, sYear)} · $dow",
                                type = SearchResultType.LUNAR,
                                solarDay = sDay, solarMonth = sMonth, solarYear = sYear
                            )
                        )
                        lunarConversion = LunarConversion(lunarDay, lunarMonth, year, sDay, sMonth, sYear, dow)
                    }
                }
            }
        }

        // 4. Keywords: "ngày tốt", "ngày cưới", etc.
        if (queryLower.contains("ngày tốt") || queryLower.contains("ngay tot")) {
            results.add(
                SearchResult(
                    title = "Ngày tốt sắp tới",
                    description = "Xem danh sách ngày hoàng đạo trong 30 ngày tới",
                    type = SearchResultType.ZODIAC
                )
            )
        }
        if (queryLower.contains("cưới") || queryLower.contains("cuoi")) {
            results.add(
                SearchResult(
                    title = "Ngày cưới tốt",
                    description = "Tra cứu ngày tốt cho lễ cưới hỏi",
                    type = SearchResultType.ZODIAC
                )
            )
        }
        if (queryLower.contains("khai trương") || queryLower.contains("khai truong")) {
            results.add(
                SearchResult(
                    title = "Ngày khai trương",
                    description = "Tìm ngày giờ tốt để khai trương",
                    type = SearchResultType.ZODIAC
                )
            )
        }

        _uiState.update { it.copy(results = results, lunarConversion = lunarConversion) }
    }

    private fun searchHolidays(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val queryLower = query.lowercase()
        val currentYear = LocalDate.now().year

        // Search solar holidays
        HolidayUtil.SOLAR_HOLIDAYS.forEach { (dateKey, name) ->
            if (name.lowercase().contains(queryLower)) {
                val parts = dateKey.split("/")
                val day = parts[0].toIntOrNull() ?: return@forEach
                val month = parts[1].toIntOrNull() ?: return@forEach
                val dow = try {
                    val date = LocalDate.of(currentYear, month, day)
                    dayOfWeekNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                } catch (_: Exception) { "" }

                results.add(
                    SearchResult(
                        title = name,
                        titleHighlight = query,
                        description = "${String.format("%02d/%02d/%d", day, month, currentYear)} · $dow · Dương lịch",
                        type = SearchResultType.HOLIDAY,
                        solarDay = day, solarMonth = month, solarYear = currentYear
                    )
                )
            }
        }

        // Search lunar holidays
        HolidayUtil.LUNAR_HOLIDAYS.forEach { (dateKey, name) ->
            if (name.lowercase().contains(queryLower)) {
                val parts = dateKey.split("/")
                val lunarDay = parts[0].toIntOrNull() ?: return@forEach
                val lunarMonth = parts[1].toIntOrNull() ?: return@forEach
                val (sDay, sMonth, sYear) = LunarCalendarUtil.convertLunar2Solar(
                    lunarDay, lunarMonth, currentYear, 0
                )
                val dow = if (sDay > 0) {
                    try {
                        val date = LocalDate.of(sYear, sMonth, sDay)
                        dayOfWeekNames.getOrElse(date.dayOfWeek.value - 1) { "" }
                    } catch (_: Exception) { "" }
                } else ""

                results.add(
                    SearchResult(
                        title = name,
                        titleHighlight = query,
                        description = if (sDay > 0)
                            "${lunarDay}/${lunarMonth} Âm lịch — ${String.format("%02d/%02d/%d", sDay, sMonth, sYear)} · $dow"
                        else
                            "${lunarDay}/${lunarMonth} Âm lịch",
                        type = SearchResultType.HOLIDAY,
                        solarDay = sDay, solarMonth = sMonth, solarYear = sYear
                    )
                )
            }
        }

        return results
    }

    /**
     * Quick action: Chuyển đổi Âm → Dương
     */
    fun convertLunarToSolar(lunarDay: Int, lunarMonth: Int, year: Int = LocalDate.now().year): LunarConversion? {
        if (lunarDay !in 1..30 || lunarMonth !in 1..12) return null
        val (sDay, sMonth, sYear) = LunarCalendarUtil.convertLunar2Solar(lunarDay, lunarMonth, year, 0)
        if (sDay == 0) return null
        val dow = try {
            val date = LocalDate.of(sYear, sMonth, sDay)
            dayOfWeekNames.getOrElse(date.dayOfWeek.value - 1) { "" }
        } catch (_: Exception) { "" }
        return LunarConversion(lunarDay, lunarMonth, year, sDay, sMonth, sYear, dow)
    }

    /**
     * Quick action: Chuyển đổi Dương → Âm
     */
    fun convertSolarToLunar(solarDay: Int, solarMonth: Int, year: Int = LocalDate.now().year): LunarConversion? {
        if (solarDay !in 1..31 || solarMonth !in 1..12) return null
        return try {
            val lunar = LunarCalendarUtil.convertSolar2Lunar(solarDay, solarMonth, year)
            val dow = try {
                val date = LocalDate.of(year, solarMonth, solarDay)
                dayOfWeekNames.getOrElse(date.dayOfWeek.value - 1) { "" }
            } catch (_: Exception) { "" }
            LunarConversion(lunar.lunarDay, lunar.lunarMonth, lunar.lunarYear, solarDay, solarMonth, year, dow)
        } catch (_: Exception) { null }
    }

    // ═══ Recent Searches Persistence ═══

    private fun loadRecentSearches() {
        viewModelScope.launch {
            context.settingsDataStore.data.first().let { prefs ->
                val raw = prefs[RECENT_SEARCHES_KEY] ?: ""
                val list = if (raw.isBlank()) emptyList() else raw.split("|||")
                _uiState.update { it.copy(recentSearches = list) }
            }
        }
    }

    private suspend fun persistRecentSearches(list: List<String>) {
        context.settingsDataStore.edit { prefs ->
            prefs[RECENT_SEARCHES_KEY] = list.joinToString("|||")
        }
    }
}
