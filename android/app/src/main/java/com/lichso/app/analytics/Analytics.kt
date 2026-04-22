package com.lichso.app.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Wrapper thống nhất cho Firebase / Google Analytics (GA4).
 *
 * Usage:
 *   Analytics.init(applicationContext)
 *   Analytics.logScreen("calendar")
 *   Analytics.logEvent("reminder_created", mapOf("repeat_type" to 1))
 *
 * Muốn tắt analytics (opt-out của user):
 *   Analytics.setEnabled(false)
 */
object Analytics {

    private var fa: FirebaseAnalytics? = null

    fun init(context: Context) {
        fa = Firebase.analytics.also {
            it.setAnalyticsCollectionEnabled(true)
        }
    }

    /** Bật / tắt toàn bộ việc gửi data (ví dụ khi user từ chối ở màn onboarding). */
    fun setEnabled(enabled: Boolean) {
        fa?.setAnalyticsCollectionEnabled(enabled)
    }

    /** Log màn hình – dùng cho navigation. */
    fun logScreen(screenName: String, screenClass: String? = null) {
        fa?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
    }

    /** Log custom event với key-value bất kỳ (String / Long / Double / Int / Boolean). */
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (k, v) ->
                when (v) {
                    is String -> putString(k, v)
                    is Int -> putLong(k, v.toLong())
                    is Long -> putLong(k, v)
                    is Double -> putDouble(k, v)
                    is Float -> putDouble(k, v.toDouble())
                    is Boolean -> putLong(k, if (v) 1L else 0L)
                    null -> {}
                    else -> putString(k, v.toString())
                }
            }
        }
        fa?.logEvent(name, bundle)
    }

    /** Gán user ID (ví dụ Firebase uid sau khi login). Truyền null để xoá. */
    fun setUserId(id: String?) {
        fa?.setUserId(id)
    }

    /** Gán user property (tối đa 25 property, key ≤ 24 ký tự). */
    fun setUserProperty(key: String, value: String?) {
        fa?.setUserProperty(key, value)
    }
}
