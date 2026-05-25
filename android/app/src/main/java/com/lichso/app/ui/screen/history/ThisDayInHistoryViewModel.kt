package com.lichso.app.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.ContentEvent
import com.lichso.app.domain.HistoricalEventProvider
import com.lichso.app.domain.model.EventImportance
import com.lichso.app.domain.model.HistoricalEvent
import com.lichso.app.domain.model.HistoryCategory
import com.lichso.app.feature.content.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ThisDayInHistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<HistoricalEvent> = emptyList(),
    val dayDisplay: String = "",
    val monthDisplay: String = "",
    val fullDateDisplay: String = "",
    val isLoading: Boolean = false,
)

@HiltViewModel
class ThisDayInHistoryViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

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

        val dayDisplay = "%02d".format(date.dayOfMonth)
        val monthDisplay = "Tháng ${date.monthValue}"
        val fullDateDisplay = "$dayOfWeekVi, %02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year)

        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            dayDisplay = dayDisplay,
            monthDisplay = monthDisplay,
            fullDateDisplay = fullDateDisplay,
            isLoading = true,
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = contentRepository.getEventsByDate(date.monthValue, date.dayOfMonth)
            val apiEvents = result.getOrNull()
            val events = if (!apiEvents.isNullOrEmpty()) {
                apiEvents.map { it.toHistoricalEvent() }
            } else {
                HistoricalEventProvider.getEvents(date.dayOfMonth, date.monthValue)
            }
            _uiState.value = _uiState.value.copy(
                events = events,
                isLoading = false,
            )
        }
    }

    fun yearsAgo(eventYear: Int): Int {
        return _uiState.value.selectedDate.year - eventYear
    }
}

private fun ContentEvent.toHistoricalEvent() = HistoricalEvent(
    year = eventYear ?: 0,
    title = title,
    description = shortDescription ?: description ?: "",
    category = HistoryCategory.VIETNAM,
    importance = EventImportance.MAJOR,
)
