package com.lichso.app.feature.countdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.CountdownEventDao
import com.lichso.app.data.local.entity.CountdownEventEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CountdownItemUi(
    val entity: CountdownEventEntity,
    val daysLeft: Long,
)

data class CountdownUiState(
    val items: List<CountdownItemUi> = emptyList(),
    val showAddDialog: Boolean = false,
    val titleInput: String = "",
    val dateInput: LocalDate = LocalDate.now().plusDays(1),
    val noteInput: String = "",
    val showOnHome: Boolean = true,
    val showOnWidget: Boolean = true,
)

@HiltViewModel
class CountdownViewModel @Inject constructor(
    private val dao: CountdownEventDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val todayEpoch = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            combine(
                dao.observeAll(),
                _uiState,
            ) { entities, state ->
                val mapped = entities.map { e ->
                    CountdownItemUi(entity = e, daysLeft = e.targetEpochDay - todayEpoch)
                }
                state.copy(items = mapped)
            }.collect { _uiState.value = it }
        }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                titleInput = "",
                dateInput = LocalDate.now().plusDays(1),
                noteInput = "",
                showOnHome = true,
                showOnWidget = true,
            )
        }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun setTitle(value: String) {
        _uiState.update { it.copy(titleInput = value.take(80)) }
    }

    fun setDate(value: LocalDate) {
        _uiState.update { it.copy(dateInput = value) }
    }

    fun setNote(value: String) {
        _uiState.update { it.copy(noteInput = value.take(200)) }
    }

    fun setShowOnHome(value: Boolean) {
        _uiState.update { it.copy(showOnHome = value) }
    }

    fun setShowOnWidget(value: Boolean) {
        _uiState.update { it.copy(showOnWidget = value) }
    }

    fun saveNewEvent() {
        val s = _uiState.value
        if (s.titleInput.isBlank()) return
        viewModelScope.launch {
            dao.insert(
                CountdownEventEntity(
                    title = s.titleInput.trim(),
                    targetEpochDay = s.dateInput.toEpochDay(),
                    note = s.noteInput.trim(),
                    showOnHome = s.showOnHome,
                    showOnWidget = s.showOnWidget,
                )
            )
            closeAddDialog()
        }
    }

    fun toggleHome(item: CountdownEventEntity) {
        viewModelScope.launch {
            dao.update(item.copy(showOnHome = !item.showOnHome))
        }
    }

    fun toggleWidget(item: CountdownEventEntity) {
        viewModelScope.launch {
            dao.update(item.copy(showOnWidget = !item.showOnWidget))
        }
    }

    fun delete(item: CountdownEventEntity) {
        viewModelScope.launch { dao.delete(item) }
    }
}
