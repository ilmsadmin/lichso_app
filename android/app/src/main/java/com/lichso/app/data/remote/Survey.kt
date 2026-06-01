package com.lichso.app.data.remote

import com.google.gson.annotations.SerializedName

data class SurveyQuestion(
    val title: String,
    val type: String, // "single_choice", "multiple_choice", "text"
    val options: List<String>?,
    val required: Boolean
)

data class Survey(
    val id: String,
    val title: String,
    val description: String?,
    val questions: List<SurveyQuestion>,
    @SerializedName("is_active") val isActive: Boolean
)

data class UserAnswerPayload(
    @SerializedName("question_index") val questionIndex: Int,
    @SerializedName("selected_options") val selectedOptions: List<String>?,
    @SerializedName("text_answer") val textAnswer: String?
)

data class SubmitSurveyResponseRequest(
    val answers: List<UserAnswerPayload>,
    @SerializedName("device_id") val deviceId: String?
)
