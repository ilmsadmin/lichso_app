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

    /**
     * @param reviewSource nguồn đánh giá:
     *   - `"in_app_review"` — Google In-App Review API sẵn sàng (dialog có thể hiện)
     *   - `"play_store_fallback"` — API unavailable, user được đưa đến Play Store
     *   - `"play_store_manual"` — user tự nhấn nút mở Play Store từ màn thanks
     */
    suspend fun submitHighRatingReview(
        context: Context,
        stars: Int,
        reviewSource: String = "in_app_review",
    ): Result<Unit> = submit(context, stars, "", REVIEW_FLOW_HIGH_RATING, reviewSource)

    private suspend fun submit(
        context: Context,
        stars: Int,
        reviewText: String,
        reviewFlow: String,
        reviewSource: String? = null,
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
            reviewSource = reviewSource,
        )
	    }
	}
