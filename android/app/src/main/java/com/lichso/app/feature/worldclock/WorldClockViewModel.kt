package com.lichso.app.feature.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.WorldClockCityDao
import com.lichso.app.data.local.entity.WorldClockCityEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ── Available cities catalogue ────────────────────────────────────────────────

val ALL_AVAILABLE_CITIES: List<WorldClockCityEntity> = listOf(
    WorldClockCityEntity(cityName = "Hồ Chí Minh",  timezone = "Asia/Ho_Chi_Minh",      country = "Việt Nam"),
    WorldClockCityEntity(cityName = "Hà Nội",        timezone = "Asia/Ho_Chi_Minh",      country = "Việt Nam"),
    WorldClockCityEntity(cityName = "Tokyo",          timezone = "Asia/Tokyo",            country = "Nhật Bản"),
    WorldClockCityEntity(cityName = "Seoul",          timezone = "Asia/Seoul",            country = "Hàn Quốc"),
    WorldClockCityEntity(cityName = "Singapore",      timezone = "Asia/Singapore",        country = "Singapore"),
    WorldClockCityEntity(cityName = "Bangkok",        timezone = "Asia/Bangkok",          country = "Thái Lan"),
    WorldClockCityEntity(cityName = "Bắc Kinh",      timezone = "Asia/Shanghai",         country = "Trung Quốc"),
    WorldClockCityEntity(cityName = "Đài Bắc",       timezone = "Asia/Taipei",           country = "Đài Loan"),
    WorldClockCityEntity(cityName = "Mumbai",         timezone = "Asia/Kolkata",          country = "Ấn Độ"),
    WorldClockCityEntity(cityName = "Delhi",          timezone = "Asia/Kolkata",          country = "Ấn Độ"),
    WorldClockCityEntity(cityName = "Dubai",          timezone = "Asia/Dubai",            country = "UAE"),
    WorldClockCityEntity(cityName = "Riyadh",         timezone = "Asia/Riyadh",           country = "Ả Rập Xê Út"),
    WorldClockCityEntity(cityName = "Istanbul",       timezone = "Europe/Istanbul",       country = "Thổ Nhĩ Kỳ"),
    WorldClockCityEntity(cityName = "Moscow",         timezone = "Europe/Moscow",         country = "Nga"),
    WorldClockCityEntity(cityName = "London",         timezone = "Europe/London",         country = "Anh"),
    WorldClockCityEntity(cityName = "Paris",          timezone = "Europe/Paris",          country = "Pháp"),
    WorldClockCityEntity(cityName = "Berlin",         timezone = "Europe/Berlin",         country = "Đức"),
    WorldClockCityEntity(cityName = "Rome",           timezone = "Europe/Rome",           country = "Ý"),
    WorldClockCityEntity(cityName = "Amsterdam",      timezone = "Europe/Amsterdam",      country = "Hà Lan"),
    WorldClockCityEntity(cityName = "Sydney",         timezone = "Australia/Sydney",      country = "Úc"),
    WorldClockCityEntity(cityName = "Melbourne",      timezone = "Australia/Melbourne",   country = "Úc"),
    WorldClockCityEntity(cityName = "New York",       timezone = "America/New_York",      country = "Mỹ"),
    WorldClockCityEntity(cityName = "Los Angeles",    timezone = "America/Los_Angeles",   country = "Mỹ"),
    WorldClockCityEntity(cityName = "Chicago",        timezone = "America/Chicago",       country = "Mỹ"),
    WorldClockCityEntity(cityName = "Toronto",        timezone = "America/Toronto",       country = "Canada"),
    WorldClockCityEntity(cityName = "São Paulo",      timezone = "America/Sao_Paulo",     country = "Brazil"),
    WorldClockCityEntity(cityName = "Mexico City",    timezone = "America/Mexico_City",   country = "Mexico"),
    WorldClockCityEntity(cityName = "Johannesburg",   timezone = "Africa/Johannesburg",   country = "Nam Phi"),
    WorldClockCityEntity(cityName = "Cairo",          timezone = "Africa/Cairo",          country = "Ai Cập"),
    WorldClockCityEntity(cityName = "Nairobi",        timezone = "Africa/Nairobi",        country = "Kenya"),
)

private val DEFAULT_SEED: List<WorldClockCityEntity> = listOf(
    WorldClockCityEntity(cityName = "Hồ Chí Minh", timezone = "Asia/Ho_Chi_Minh", country = "Việt Nam",   sortOrder = 0),
    WorldClockCityEntity(cityName = "Tokyo",         timezone = "Asia/Tokyo",       country = "Nhật Bản",   sortOrder = 1),
    WorldClockCityEntity(cityName = "Seoul",         timezone = "Asia/Seoul",       country = "Hàn Quốc",   sortOrder = 2),
    WorldClockCityEntity(cityName = "Singapore",     timezone = "Asia/Singapore",   country = "Singapore",  sortOrder = 3),
    WorldClockCityEntity(cityName = "Dubai",         timezone = "Asia/Dubai",       country = "UAE",        sortOrder = 4),
    WorldClockCityEntity(cityName = "London",        timezone = "Europe/London",    country = "Anh",        sortOrder = 5),
    WorldClockCityEntity(cityName = "New York",      timezone = "America/New_York", country = "Mỹ",         sortOrder = 6),
)

// ── UI models ─────────────────────────────────────────────────────────────────

data class CityTimeUi(
    val entity: WorldClockCityEntity,
    val timeStr: String,        // "14:35"
    val dateStr: String,        // "05/05"
    val offsetLabel: String,    // "UTC+7"
    val is24hAhead: Boolean,    // date is tomorrow relative to base
    val is24hBehind: Boolean,   // date is yesterday relative to base
)

data class WorldClockUiState(
    val cities: List<CityTimeUi> = emptyList(),
    val baseCity: WorldClockCityEntity? = null,
    val targetCity: WorldClockCityEntity? = null,
    val showAddDialog: Boolean = false,
    val searchQuery: String = "",
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

private fun tickerFlow(): Flow<Unit> = flow {
    while (true) { emit(Unit); delay(30_000L) }
}

@HiltViewModel
class WorldClockViewModel @Inject constructor(
    private val dao: WorldClockCityDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldClockUiState())
    val uiState: StateFlow<WorldClockUiState> = _uiState.asStateFlow()

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt = DateTimeFormatter.ofPattern("dd/MM")

    init {
        viewModelScope.launch {
            if (dao.count() == 0) {
                DEFAULT_SEED.forEach { dao.insert(it) }
            }
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(dao.observeAll(), tickerFlow()) { cities, _ -> cities }
                .collect { cities ->
                    val now = Instant.now()
                    val cityTimes = cities.map { c -> c.toUi(now, timeFmt, dateFmt) }
                    _uiState.update { state ->
                        val base = state.baseCity?.let { b -> cities.find { it.id == b.id } }
                            ?: cities.firstOrNull()
                        val target = state.targetCity?.let { t -> cities.find { it.id == t.id } }
                            ?: cities.getOrNull(6) ?: cities.lastOrNull()
                        state.copy(cities = cityTimes, baseCity = base, targetCity = target)
                    }
                }
        }
    }

    fun setBase(city: WorldClockCityEntity) = _uiState.update { it.copy(baseCity = city) }
    fun setTarget(city: WorldClockCityEntity) = _uiState.update { it.copy(targetCity = city) }

    fun openAddDialog() = _uiState.update { it.copy(showAddDialog = true, searchQuery = "") }
    fun closeAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun setSearch(q: String) = _uiState.update { it.copy(searchQuery = q) }

    fun addCity(proto: WorldClockCityEntity) {
        viewModelScope.launch {
            val order = _uiState.value.cities.size
            dao.insert(proto.copy(id = 0, sortOrder = order))
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun removeCity(city: WorldClockCityEntity) {
        viewModelScope.launch { dao.delete(city) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun WorldClockCityEntity.toUi(
        now: Instant,
        timeFmt: DateTimeFormatter,
        dateFmt: DateTimeFormatter,
    ): CityTimeUi {
        val zid = ZoneId.of(timezone)
        val zdt = now.atZone(zid)
        val offsetSecs = zdt.offset.totalSeconds
        val absH = Math.abs(offsetSecs / 3600)
        val absM = Math.abs((offsetSecs % 3600) / 60)
        val sign = if (offsetSecs >= 0) "+" else "-"
        val offsetLabel = "UTC$sign$absH${if (absM > 0) ":${absM.toString().padStart(2, '0')}" else ""}"
        return CityTimeUi(
            entity = this,
            timeStr = timeFmt.format(zdt),
            dateStr = dateFmt.format(zdt),
            offsetLabel = offsetLabel,
            is24hAhead = false,
            is24hBehind = false,
        )
    }
}
