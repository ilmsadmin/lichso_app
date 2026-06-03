package com.lichso.app.util

import android.content.Context
import com.lichso.app.data.auth.TokenManager
import com.lichso.app.data.remote.LichSoApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private const val REVIEW_FLOW_LOW_RATING = "low_rating_feedback"
private const val REVIEW_FLOW_HIGH_RATING = "high_rating_prompt"

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppReviewReporterEntryPoint {
    fun lichSoApi(): LichSoApi
    fun tokenManager(): TokenManager
}

object AppReviewReporter {
    suspend fun submitLowRatingFeedback(
        context: Context,
        stars: Int,
        reviewText: String,
    ): Result<Unit> = submit(context, stars, reviewText, REVIEW_FLOW_LOW_RATING)

    suspend fun submitHighRatingReview(
        context: Context,
        stars: Int,
    ): Result<Unit> = submit(context, stars, "", REVIEW_FLOW_HIGH_RATING)

    private suspend fun submit(
        context: Context,
        stars: Int,
        reviewText: String,
        reviewFlow: String,
    ): Result<Unit> {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppReviewReporterEntryPoint::class.java
        )
        val token = entryPoint.tokenManager().getAccessToken()
        return entryPoint.lichSoApi().submitAppReview(
            token = token,
            stars = stars,
            reviewText = reviewText,
            reviewFlow = reviewFlow,
        )
    }
}
