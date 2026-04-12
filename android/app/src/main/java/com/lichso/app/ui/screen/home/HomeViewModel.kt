package com.lichso.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.NotificationDao
import com.lichso.app.data.remote.WeatherRepository
import com.lichso.app.data.remote.WeatherState
import com.lichso.app.data.settings.AppSettingsRepository
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.domain.model.*
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val currentYear: Int = LocalDate.now().year,
    val currentMonth: Int = LocalDate.now().monthValue,
    val selectedDate: LocalDate = LocalDate.now(),
    val dayInfo: DayInfo? = null,
    val calendarDays: List<CalendarDay> = emptyList(),
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val showLunarBadge: Boolean = true,
    val showQuote: Boolean = true,
    val showFestival: Boolean = true,
    val showHoangDao: Boolean = false,
    val weekStartSunday: Boolean = false,
    val tempUnit: String = "°C",
    val weatherState: WeatherState = WeatherState.Loading,
    val notificationUnreadCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dayInfoProvider: DayInfoProvider,
    private val appSettings: AppSettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentDate()
        // Lắng nghe số thông báo chưa đọc
        notificationDao.getUnreadCount()
            .onEach { count -> _uiState.update { it.copy(notificationUnreadCount = count) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting lịch âm
        appSettings.lunarBadgeEnabled
            .onEach { enabled -> _uiState.update { it.copy(showLunarBadge = enabled) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting câu danh ngôn
        appSettings.quoteEnabled
            .onEach { enabled -> _uiState.update { it.copy(showQuote = enabled) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting ngày lễ / sự kiện
        appSettings.festivalEnabled
            .onEach { enabled -> _uiState.update { it.copy(showFestival = enabled) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi đơn vị nhiệt độ
        appSettings.tempUnit
            .onEach { unit -> _uiState.update { it.copy(tempUnit = unit) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi thời tiết
        weatherRepository.weatherState
            .onEach { state -> _uiState.update { it.copy(weatherState = state) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi ngày bắt đầu tuần
        appSettings.weekStart
            .onEach { ws ->
                val sunday = ws == "Chủ Nhật"
                _uiState.update { it.copy(weekStartSunday = sunday) }
                val s = _uiState.value
                val calDays = withContext(Dispatchers.Default) {
                    dayInfoProvider.getCalendarDays(s.currentYear, s.currentMonth, sunday)
                }
                _uiState.update { it.copy(calendarDays = calDays) }
            }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting ngày hoàng đạo
        appSettings.gioDaiCatEnabled
            .onEach { enabled -> _uiState.update { it.copy(showHoangDao = enabled) } }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi vị trí → tải lại thời tiết
        appSettings.locationName
            .onEach { _ ->
                // Khi vị trí thay đổi, gọi lại fetchWeather (sẽ đọc city mới từ settings)
                weatherRepository.fetchWeather(forceRefresh = true)
            }
            .launchIn(viewModelScope)
        // Tải thời tiết
        loadWeather()
    }

    private fun loadCurrentDate() {
        val today = LocalDate.now()
        viewModelScope.launch { updateState(today.year, today.monthValue, today) }
    }

    fun loadWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            weatherRepository.fetchWeather(forceRefresh)
        }
    }

    fun refreshWeather() {
        loadWeather(forceRefresh = true)
    }

    /**
     * Đổi thành phố: lưu vào DataStore → WeatherRepository sẽ tự fetch lại
     */
    fun changeCity(cityName: String) {
        viewModelScope.launch {
            context.settingsDataStore.edit { prefs ->
                prefs[SettingsKeys.LOCATION_NAME] = cityName
            }
            // fetchWeather sẽ được gọi tự động nhờ listener locationName ở init{}
        }
    }

    fun selectDay(day: Int, month: Int, year: Int) {
        val date = LocalDate.of(year, month, day)
        viewModelScope.launch {
            val info = withContext(Dispatchers.Default) { dayInfoProvider.getDayInfo(day, month, year) }
            val events = withContext(Dispatchers.Default) { dayInfoProvider.getUpcomingEvents(day, month, year) }
            _uiState.update { it.copy(selectedDate = date, dayInfo = info, upcomingEvents = events) }
        }
    }

    fun prevDay() {
        val newDate = _uiState.value.selectedDate.minusDays(1)
        viewModelScope.launch { updateState(newDate.year, newDate.monthValue, newDate) }
    }

    fun nextDay() {
        val newDate = _uiState.value.selectedDate.plusDays(1)
        viewModelScope.launch { updateState(newDate.year, newDate.monthValue, newDate) }
    }

    fun prevMonth() {
        val current = _uiState.value
        var newMonth = current.currentMonth - 1
        var newYear = current.currentYear
        if (newMonth < 1) { newMonth = 12; newYear-- }
        viewModelScope.launch { updateState(newYear, newMonth, current.selectedDate) }
    }

    fun nextMonth() {
        val current = _uiState.value
        var newMonth = current.currentMonth + 1
        var newYear = current.currentYear
        if (newMonth > 12) { newMonth = 1; newYear++ }
        viewModelScope.launch { updateState(newYear, newMonth, current.selectedDate) }
    }

    fun goToToday() {
        val today = LocalDate.now()
        viewModelScope.launch { updateState(today.year, today.monthValue, today) }
    }

    fun goToDate(year: Int, month: Int, day: Int) {
        val date = LocalDate.of(year, month, day)
        viewModelScope.launch { updateState(date.year, date.monthValue, date) }
    }

    fun goToMonth(year: Int, month: Int) {
        val current = _uiState.value
        val day = current.selectedDate.dayOfMonth.coerceAtMost(
            java.time.YearMonth.of(year, month).lengthOfMonth()
        )
        val date = LocalDate.of(year, month, day)
        viewModelScope.launch { updateState(year, month, date) }
    }

    private suspend fun updateState(year: Int, month: Int, selectedDate: LocalDate) {
        val dd = selectedDate.dayOfMonth
        val mm = selectedDate.monthValue
        val yy = selectedDate.year
        val (dayInfo, calDays, events) = withContext(Dispatchers.Default) {
            Triple(
                dayInfoProvider.getDayInfo(dd, mm, yy),
                dayInfoProvider.getCalendarDays(year, month, _uiState.value.weekStartSunday),
                dayInfoProvider.getUpcomingEvents(dd, mm, yy)
            )
        }
        _uiState.update { current ->
            current.copy(
                currentYear = year,
                currentMonth = month,
                selectedDate = selectedDate,
                dayInfo = dayInfo,
                calendarDays = calDays,
                upcomingEvents = events,
            )
        }
    }
}
