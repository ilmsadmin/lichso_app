package com.lichso.app.feature.survey

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.auth.TokenManager
import com.lichso.app.data.remote.LichSoApiException
import com.lichso.app.data.remote.Survey
import com.lichso.app.data.remote.UserAnswerPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SurveyUiState {
    object Loading : SurveyUiState
    data class Success(val survey: Survey) : SurveyUiState
    object Empty : SurveyUiState
    data class Error(val message: String) : SurveyUiState
}

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val repository: SurveyRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SurveyUiState>(SurveyUiState.Loading)
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    // Store selected answers: Map of QuestionIndex to List of selected options
    val selectedOptions = mutableStateMapOf<Int, List<String>>()
    // Store text answers: Map of QuestionIndex to input text
    val textAnswers = mutableStateMapOf<Int, String>()

    init {
        loadActiveSurvey()
    }

    fun loadActiveSurvey() {
        viewModelScope.launch {
            _uiState.value = SurveyUiState.Loading
            try {
                val token = tokenManager.getAccessToken()
                repository.getActiveSurvey(token).fold(
                    onSuccess = { survey ->
                        _uiState.value = SurveyUiState.Success(survey)
                    },
                    onFailure = { err ->
                        if (err is LichSoApiException && (err.statusCode == 200 || err.statusCode == 404) &&
                            (err.message.contains("No active survey") || err.message.contains("không tìm thấy"))
                        ) {
                            _uiState.value = SurveyUiState.Empty
                        } else {
                            _uiState.value = SurveyUiState.Error(err.message ?: "Không thể tải khảo sát")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = SurveyUiState.Error(e.message ?: "Lỗi hệ thống khi tải khảo sát")
            }
        }
    }

    fun submitSurvey() {
        val currentState = _uiState.value
        if (currentState !is SurveyUiState.Success) return

        val survey = currentState.survey
        val payloads = mutableListOf<UserAnswerPayload>()

        // Validate and aggregate answers
        for (i in survey.questions.indices) {
            val q = survey.questions[i]
            val opts = selectedOptions[i] ?: emptyList()
            val txt = textAnswers[i] ?: ""

            if (q.required) {
                if (q.type == "text" && txt.trim().isBlank()) {
                    _submitError.value = "Vui lòng trả lời câu hỏi: ${q.title}"
                    return
                }
                if ((q.type == "single_choice" || q.type == "multiple_choice") && opts.isEmpty()) {
                    _submitError.value = "Vui lòng chọn câu trả lời cho câu hỏi: ${q.title}"
                    return
                }
            }

            payloads.add(
                UserAnswerPayload(
                    questionIndex = i,
                    selectedOptions = if (q.type != "text") opts else null,
                    textAnswer = if (q.type == "text") txt else null
                )
            )
        }

        viewModelScope.launch {
            _submitting.value = true
            _submitError.value = null
            try {
                val token = tokenManager.getAccessToken()
                val deviceId = tokenManager.getInstallationId()

                repository.submitSurveyResponse(token, payloads, deviceId).fold(
                    onSuccess = {
                        _submitting.value = false
                        _submitSuccess.value = true
                    },
                    onFailure = { err ->
                        _submitting.value = false
                        _submitError.value = err.message ?: "Không thể gửi câu trả lời"
                    }
                )
            } catch (e: Exception) {
                _submitting.value = false
                _submitError.value = e.message ?: "Lỗi hệ thống khi gửi khảo sát"
            }
        }
    }

    fun clearSubmitError() {
        _submitError.value = null
    }
}
