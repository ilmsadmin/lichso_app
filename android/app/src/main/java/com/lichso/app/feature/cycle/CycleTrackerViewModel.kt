package com.lichso.app.feature.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.CycleDao
import com.lichso.app.data.local.entity.CycleLogEntity
import com.lichso.app.data.local.entity.CycleSettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CycleUiState(
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val lastPeriodStart: LocalDate = LocalDate.now().minusDays(3),
    val logs: List<CycleLogEntity> = emptyList(),
    // input fields (not overwritten by DB after initial load)
    val cycleLengthInput: String = "28",
    val periodLengthInput: String = "5",
    // add-log dialog
    val showAddLogDialog: Boolean = false,
    val addLogDateInput: LocalDate = LocalDate.now(),
    val addLogNotesInput: String = "",
    val settingsSaved: Boolean = false,
)

@HiltViewModel
class CycleTrackerViewModel @Inject constructor(
    private val dao: CycleDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleUiState())
    val uiState: StateFlow<CycleUiState> = _uiState.asStateFlow()

    private var settingsLoaded = false

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(dao.observeSettings(), dao.observeLogs()) { settings, logs ->
                Pair(settings, logs)
            }.collect { (settings, logs) ->
                val cycleLength = settings?.cycleLength ?: 28
                val periodLength = settings?.periodLength ?: 5
                val lastStart = logs.firstOrNull()
                    ?.let { LocalDate.ofEpochDay(it.startEpochDay) }
                    ?: LocalDate.now().minusDays(3)
                _uiState.update { state ->
                    state.copy(
                        cycleLength = cycleLength,
                        periodLength = periodLength,
                        lastPeriodStart = lastStart,
                        logs = logs,
                        // Only init input fields on first load; don't overwrite user typing
                        cycleLengthInput = if (!settingsLoaded) cycleLength.toString() else state.cycleLengthInput,
                        periodLengthInput = if (!settingsLoaded) periodLength.toString() else state.periodLengthInput,
                    )
                }
                settingsLoaded = true
            }
        }
    }

    fun setCycleLengthInput(v: String) =
        _uiState.update { it.copy(cycleLengthInput = v.filter(Char::isDigit).take(2), settingsSaved = false) }

    fun setPeriodLengthInput(v: String) =
        _uiState.update { it.copy(periodLengthInput = v.filter(Char::isDigit).take(2), settingsSaved = false) }

    fun saveSettings() {
        viewModelScope.launch {
            val cl = _uiState.value.cycleLengthInput.toIntOrNull()?.coerceIn(21, 40) ?: 28
            val pl = _uiState.value.periodLengthInput.toIntOrNull()?.coerceIn(2, 10) ?: 5
            dao.saveSettings(CycleSettingsEntity(cycleLength = cl, periodLength = pl))
            _uiState.update { it.copy(settingsSaved = true) }
        }
    }

    // ── Add log dialog ──────────────────────────────────────────────────────

    fun openAddLogDialog() = _uiState.update {
        it.copy(showAddLogDialog = true, addLogDateInput = LocalDate.now(), addLogNotesInput = "")
    }

    fun closeAddLogDialog() = _uiState.update { it.copy(showAddLogDialog = false) }

    fun setAddLogDate(d: LocalDate) = _uiState.update { it.copy(addLogDateInput = d) }
    fun setAddLogNotes(v: String) = _uiState.update { it.copy(addLogNotesInput = v.take(200)) }

    fun confirmAddLog() {
        viewModelScope.launch {
            dao.insertLog(
                CycleLogEntity(
                    startEpochDay = _uiState.value.addLogDateInput.toEpochDay(),
                    notes = _uiState.value.addLogNotesInput.trim(),
                )
            )
            _uiState.update { it.copy(showAddLogDialog = false) }
        }
    }

    fun deleteLog(log: CycleLogEntity) {
        viewModelScope.launch { dao.deleteLog(log) }
    }
}
