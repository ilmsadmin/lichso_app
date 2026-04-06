package com.lichso.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
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
    val weatherState: WeatherState = WeatherState.Loading
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dayInfoProvider: DayInfoProvider,
    private val appSettings: AppSettingsRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentDate()
        // Lắng nghe thay đổi setting lịch âm
        appSettings.lunarBadgeEnabled
            .onEach { enabled -> _uiState.value = _uiState.value.copy(showLunarBadge = enabled) }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting câu danh ngôn
        appSettings.quoteEnabled
            .onEach { enabled -> _uiState.value = _uiState.value.copy(showQuote = enabled) }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting ngày lễ / sự kiện
        appSettings.festivalEnabled
            .onEach { enabled -> _uiState.value = _uiState.value.copy(showFestival = enabled) }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi đơn vị nhiệt độ
        appSettings.tempUnit
            .onEach { unit -> _uiState.value = _uiState.value.copy(tempUnit = unit) }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi thời tiết
        weatherRepository.weatherState
            .onEach { state -> _uiState.value = _uiState.value.copy(weatherState = state) }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi ngày bắt đầu tuần
        appSettings.weekStart
            .onEach { ws ->
                val sunday = ws == "Chủ Nhật"
                _uiState.value = _uiState.value.copy(weekStartSunday = sunday)
                // Rebuild calendar grid with new week start
                val s = _uiState.value
                _uiState.value = s.copy(
                    calendarDays = dayInfoProvider.getCalendarDays(s.currentYear, s.currentMonth, sunday)
                )
            }
            .launchIn(viewModelScope)
        // Lắng nghe thay đổi setting ngày hoàng đạo
        appSettings.gioDaiCatEnabled
            .onEach { enabled -> _uiState.value = _uiState.value.copy(showHoangDao = enabled) }
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
        updateState(today.year, today.monthValue, today)
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
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            dayInfo = dayInfoProvider.getDayInfo(day, month, year),
            upcomingEvents = dayInfoProvider.getUpcomingEvents(day, month, year)
        )
    }

    fun prevDay() {
        val newDate = _uiState.value.selectedDate.minusDays(1)
        updateState(newDate.year, newDate.monthValue, newDate)
    }

    fun nextDay() {
        val newDate = _uiState.value.selectedDate.plusDays(1)
        updateState(newDate.year, newDate.monthValue, newDate)
    }

    fun prevMonth() {
        val current = _uiState.value
        var newMonth = current.currentMonth - 1
        var newYear = current.currentYear
        if (newMonth < 1) { newMonth = 12; newYear-- }
        updateState(newYear, newMonth, current.selectedDate)
    }

    fun nextMonth() {
        val current = _uiState.value
        var newMonth = current.currentMonth + 1
        var newYear = current.currentYear
        if (newMonth > 12) { newMonth = 1; newYear++ }
        updateState(newYear, newMonth, current.selectedDate)
    }

    fun goToToday() {
        val today = LocalDate.now()
        updateState(today.year, today.monthValue, today)
    }

    fun goToDate(year: Int, month: Int, day: Int) {
        val date = LocalDate.of(year, month, day)
        updateState(date.year, date.monthValue, date)
    }

    private fun updateState(year: Int, month: Int, selectedDate: LocalDate) {
        val dd = selectedDate.dayOfMonth
        val mm = selectedDate.monthValue
        val yy = selectedDate.year
        val weekStartSunday = _uiState.value.weekStartSunday
        _uiState.value = HomeUiState(
            currentYear = year,
            currentMonth = month,
            selectedDate = selectedDate,
            dayInfo = dayInfoProvider.getDayInfo(dd, mm, yy),
            calendarDays = dayInfoProvider.getCalendarDays(year, month, weekStartSunday),
            upcomingEvents = dayInfoProvider.getUpcomingEvents(dd, mm, yy),
            showLunarBadge = _uiState.value.showLunarBadge,
            showQuote = _uiState.value.showQuote,
            showFestival = _uiState.value.showFestival,
            showHoangDao = _uiState.value.showHoangDao,
            weekStartSunday = weekStartSunday,
            tempUnit = _uiState.value.tempUnit,
            weatherState = _uiState.value.weatherState
        )
    }
}
