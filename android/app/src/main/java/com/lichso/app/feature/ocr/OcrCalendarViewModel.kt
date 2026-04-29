package com.lichso.app.feature.ocr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrCalendarState(
    val imageUri: Uri? = null,
    val isProcessing: Boolean = false,
    val rawText: String = "",
    val parsedDates: List<VietnameseDateParser.ParsedDate> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class OcrCalendarViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(OcrCalendarState())
    val state: StateFlow<OcrCalendarState> = _state.asStateFlow()

    fun setImageUri(uri: Uri?) = _state.update {
        it.copy(imageUri = uri, rawText = "", parsedDates = emptyList(), errorMessage = null)
    }

    fun runOcr() {
        val uri = _state.value.imageUri ?: return
        if (_state.value.isProcessing) return
        _state.update { it.copy(isProcessing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val text = OcrEngine.recognizeText(appContext, uri)
                val parsed = VietnameseDateParser.parse(text)
                _state.update {
                    it.copy(isProcessing = false, rawText = text, parsedDates = parsed)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isProcessing = false, errorMessage = e.message ?: "Không đọc được ảnh")
                }
            }
        }
    }
}
