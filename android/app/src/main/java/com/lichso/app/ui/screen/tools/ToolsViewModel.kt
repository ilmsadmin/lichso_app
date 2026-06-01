package com.lichso.app.ui.screen.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.Banner
import com.lichso.app.feature.content.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ToolsUiState(
    val banners: List<Banner> = emptyList(),
)

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init {
        loadBanners()
    }

    private fun loadBanners() {
        viewModelScope.launch {
            contentRepository.getBanners("tools,tools_calendar,tools_feng_shui,tools_utility,tools_collection")
                .onSuccess { list ->
                    val active = list.filter { it.active }.sortedBy { it.sortOrder }
                    _uiState.update { it.copy(banners = active) }
                }
                .onFailure {
                    _uiState.update { it.copy(banners = emptyList()) }
                }
        }
    }
}

