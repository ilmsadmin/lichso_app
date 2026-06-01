package com.lichso.app.feature.survey

import com.lichso.app.data.remote.LichSoApi
import com.lichso.app.data.remote.Survey
import com.lichso.app.data.remote.UserAnswerPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(
    private val api: LichSoApi
) {
    suspend fun getActiveSurvey(token: String?): Result<Survey> {
        return api.getActiveSurvey(token)
    }

    suspend fun submitSurveyResponse(
        token: String?,
        answers: List<UserAnswerPayload>,
        deviceId: String?
    ): Result<Unit> {
        return api.submitSurveyResponse(token, answers, deviceId)
    }
}
