package com.lichso.app.ui.screen.home

import androidx.lifecycle.ViewModel
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val currentYear: Int = LocalDate.now().year,
    val currentMonth: Int = LocalDate.now().monthValue,
    val selectedDate: LocalDate = LocalDate.now(),
    val dayInfo: DayInfo? = null,
    val calendarDays: List<CalendarDay> = emptyList(),
    val upcomingEvents: List<UpcomingEvent> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dayInfoProvider: DayInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentDate()
    }

    private fun loadCurrentDate() {
        val today = LocalDate.now()
        updateState(today.year, today.monthValue, today)
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

    private fun updateState(year: Int, month: Int, selectedDate: LocalDate) {
        val dd = selectedDate.dayOfMonth
        val mm = selectedDate.monthValue
        val yy = selectedDate.year
        _uiState.value = HomeUiState(
            currentYear = year,
            currentMonth = month,
            selectedDate = selectedDate,
            dayInfo = dayInfoProvider.getDayInfo(dd, mm, yy),
            calendarDays = dayInfoProvider.getCalendarDays(year, month),
            upcomingEvents = dayInfoProvider.getUpcomingEvents(dd, mm, yy)
        )
    }
}
