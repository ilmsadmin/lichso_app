package com.lichso.app.feature.datepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class DatePickerToolState(
    val purpose: DatePurpose = DatePurpose.CUOI_HOI,
    val birthYearText: String = "",
    val rangeDays: Int = 60,
    val results: List<CandidateDay> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class DatePickerToolViewModel @Inject constructor(
    private val scorer: DatePickerScorer,
) : ViewModel() {

    private val _state = MutableStateFlow(DatePickerToolState())
    val state: StateFlow<DatePickerToolState> = _state.asStateFlow()

    fun setPurpose(p: DatePurpose) = _state.update { it.copy(purpose = p) }
    fun setBirthYearText(text: String) = _state.update {
        it.copy(birthYearText = text.filter { ch -> ch.isDigit() }.take(4))
    }
    fun setRangeDays(days: Int) = _state.update { it.copy(rangeDays = days) }

    fun runScoring() {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val results = withContext(Dispatchers.Default) {
                val today = LocalDate.now()
                val fromJd = LunarCalendarUtil.jdFromDate(today.dayOfMonth, today.monthValue, today.year)
                val toJd = fromJd + s.rangeDays
                val birthChi = s.birthYearText.toIntOrNull()?.let { yr ->
                    // Năm sinh dương lịch ~ Năm âm lịch (sai số <1 năm) — lấy chi của năm âm.
                    CanChiCalculator.getYearCanChi(yr).split(" ").lastOrNull()
                }
                scorer.scoreRange(
                    fromJd = fromJd,
                    toJd = toJd,
                    purpose = s.purpose,
                    userBirthChi = birthChi,
                )
            }
            _state.update { it.copy(isLoading = false, results = results) }
        }
    }
}
