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
 *   Analytics.logScreen("calendar")          // tự map sang event "screen_calendar"
 *   Analytics.logEvent("reminder_created", mapOf("repeat_type" to 1))
 *
 * Mỗi màn hình fire 2 event:
 *   1. screen_view  (GA4 chuẩn — xuất hiện trong "Screens and views" report)
 *   2. screen_<name> (custom event — dễ lọc, tạo funnel, so sánh trong Explorer)
 *
 * Muốn tắt analytics (opt-out của user):
 *   Analytics.setEnabled(false)
 */
object Analytics {

    private var fa: FirebaseAnalytics? = null

    // ── Ánh xạ route → custom event name ──────────────────────────────────────
    // GA4 event name: chữ thường, dưới gạch, tối đa 40 ký tự.
    // Các route không có trong map sẽ fallback về screen_view thuần.
    private val ROUTE_TO_EVENT: Map<String, String> = mapOf(
        // Bottom nav chính
        "home"                 to "screen_home",
        "calendar"             to "screen_calendar",
        "tasks"                to "screen_tasks",
        "prayers"              to "screen_prayers",
        "tools"                to "screen_tools",

        // Tính năng AI & Chat
        "chat"                 to "screen_ai_chat",
        "templates"            to "screen_ai_templates",

        // Tính năng lịch & ngày
        "good_days"            to "screen_good_days",
        "date_picker_tool"     to "screen_date_picker",
        "tiet_khi"             to "screen_tiet_khi",
        "date_math"            to "screen_date_math",
        "countdown"            to "screen_countdown",
        "birth_planner"        to "screen_birth_planner",
        "cycle_tracker"        to "screen_cycle_tracker",
        "world_clock"          to "screen_world_clock",

        // Phong thuỷ
        "compass"              to "screen_compass",
        "lo_ban"               to "screen_lo_ban",
        "bat_trach"            to "screen_bat_trach",

        // Người dùng & xã hội
        "profile"              to "screen_profile",
        "family_tree"          to "screen_family_tree",
        "bookmarks"            to "screen_bookmarks",
        "search"               to "screen_search",
        "history"              to "screen_history",
        "notifications"        to "screen_notifications",
        "settings"             to "screen_settings",
        "widget_manager"       to "screen_widget_manager",

        // Points & gamification
        "ledger"               to "screen_ledger",
        "oracle"               to "screen_oracle",
        "zodiac_collection"    to "screen_zodiac_collection",
        "daily_store"          to "screen_daily_store",
        "streak_freeze"        to "screen_streak_freeze",
        "points_tutorial"      to "screen_points_tutorial",
    )

    fun init(context: Context) {
        fa = Firebase.analytics.also {
            it.setAnalyticsCollectionEnabled(true)
        }
    }

    /** Bật / tắt toàn bộ việc gửi data (ví dụ khi user từ chối ở màn onboarding). */
    fun setEnabled(enabled: Boolean) {
        fa?.setAnalyticsCollectionEnabled(enabled)
    }

    /**
     * Log màn hình khi navigation route thay đổi.
     *
     * Fire 2 event:
     * - `screen_view` chuẩn GA4 (param screen_name + screen_class)
     * - Custom event riêng theo route (vd: "screen_calendar") để dễ thống kê
     */
    fun logScreen(route: String, screenClass: String? = null) {
        val displayName = ROUTE_TO_EVENT[route] ?: route

        // 1. GA4 chuẩn — xuất hiện trong "Screens and views"
        fa?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, displayName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }

        // 2. Custom event riêng — xuất hiện trong "Events" report, dễ lọc
        ROUTE_TO_EVENT[route]?.let { eventName ->
            fa?.logEvent(eventName, null)
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
