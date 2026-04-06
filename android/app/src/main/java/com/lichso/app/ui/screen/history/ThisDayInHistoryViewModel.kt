package com.lichso.app.ui.screen.history

import androidx.lifecycle.ViewModel
import com.lichso.app.domain.HistoricalEventProvider
import com.lichso.app.domain.model.HistoricalEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class ThisDayInHistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<HistoricalEvent> = emptyList(),
    val dayDisplay: String = "",       // "05"
    val monthDisplay: String = "",     // "Tháng 4"
    val fullDateDisplay: String = "",  // "Chủ Nhật, 05/04/2026"
)

@HiltViewModel
class ThisDayInHistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ThisDayInHistoryUiState())
    val uiState: StateFlow<ThisDayInHistoryUiState> = _uiState.asStateFlow()

    init {
        loadDate(LocalDate.now())
    }

    fun prevDay() {
        loadDate(_uiState.value.selectedDate.minusDays(1))
    }

    fun nextDay() {
        loadDate(_uiState.value.selectedDate.plusDays(1))
    }

    fun selectDate(date: LocalDate) {
        loadDate(date)
    }

    private fun loadDate(date: LocalDate) {
        val events = HistoricalEventProvider.getEvents(date.dayOfMonth, date.monthValue)

        val dayOfWeekVi = when (date.dayOfWeek.value) {
            1 -> "Thứ Hai"
            2 -> "Thứ Ba"
            3 -> "Thứ Tư"
            4 -> "Thứ Năm"
            5 -> "Thứ Sáu"
            6 -> "Thứ Bảy"
            7 -> "Chủ Nhật"
            else -> ""
        }

        _uiState.value = ThisDayInHistoryUiState(
            selectedDate = date,
            events = events,
            dayDisplay = "%02d".format(date.dayOfMonth),
            monthDisplay = "Tháng ${date.monthValue}",
            fullDateDisplay = "$dayOfWeekVi, %02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year)
        )
    }

    /**
     * Tính "X năm trước" dựa trên năm hiện tại.
     */
    fun yearsAgo(eventYear: Int): Int {
        return _uiState.value.selectedDate.year - eventYear
    }
}
