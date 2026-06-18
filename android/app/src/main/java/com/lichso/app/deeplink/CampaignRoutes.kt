package com.lichso.app.deeplink

import android.net.Uri

/**
 * Ánh xạ "đích" trong link/quảng cáo → route nội bộ của app, dùng chung cho:
 *  - Direct deep link `lichso://...` (MainActivity, khi app đã cài).
 *  - Deferred deep link từ Google Play Install Referrer (install mới từ ads).
 *
 * Team marketing chỉ cần gắn 1 trong các dạng:
 *  - URL scheme:  lichso://chat · lichso://quiz · lichso://open?screen=home
 *  - Play referrer: ...&referrer=screen%3Dchat  (hoặc deep_link=lichso%3A%2F%2Fchat)
 */
object CampaignRoutes {

    /** Mọi route hợp lệ có thể điều hướng tới từ deep link / push / widget. */
    val valid = setOf(
        "home", "calendar", "gooddays", "knowledge_feed", "quiz_home", "chat",
        "tools", "prayers", "history", "profile", "bookmarks", "tasks", "countdown",
        "familytree", "oracle_draw", "daily_store", "ledger", "zodiac_collection",
        "date_picker", "tiet_khi", "date_math", "birth_planner", "cycle_tracker",
        "world_clock", "widget_manager", "leaderboard", "notifications", "search",
        "settings", "bat_trach", "feng_shui_compass", "lo_ban",
    )

    /**
     * Ánh xạ tên thân thiện (từ ad) → route nội bộ, rồi lọc theo [valid].
     * Trả về null nếu không khớp route hợp lệ.
     */
    fun resolve(raw: String?): String? {
        val key = raw?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
        val mapped = when (key) {
            // AI tử vi / phong thủy
            "ai", "ai_chat", "tuvi", "tu-vi", "tu_vi", "phongthuy", "phong-thuy" -> "chat"
            // Lịch vạn niên (lịch ngày — almanac) vs lịch tháng
            "lich", "lichvannien", "lich-van-nien", "almanac" -> "home"
            "thang", "lichthang", "month" -> "calendar"
            // Đố vui
            "quiz", "dovui", "do-vui", "trac-nghiem" -> "quiz_home"
            // Văn khấn
            "vankhan", "van-khan", "khan" -> "prayers"
            // Ngày tốt xấu
            "ngaytot", "ngay-tot", "xemngay" -> "gooddays"
            else -> key
        }
        return mapped.takeIf { it in valid }
    }

    /**
     * Suy ra route từ chuỗi referrer của Play Store (đã được API giải mã).
     * Định dạng giống query string, ví dụ:
     *   "utm_source=facebook&utm_campaign=tuvi&screen=chat"
     *   "deep_link=lichso://chat"
     * Ưu tiên `deep_link` (URL đầy đủ) → `screen` → `utm_content`/`utm_campaign`.
     */
    fun fromReferrer(referrer: String?): String? {
        val ref = referrer?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        // Bọc thành URI giả để tái dùng parser query chuẩn, tránh tự tách chuỗi.
        val q = runCatching { Uri.parse("https://x/?$ref") }.getOrNull() ?: return null

        // 1) deep_link=lichso://<đích>
        q.getQueryParameter("deep_link")?.let { dl ->
            val u = runCatching { Uri.parse(dl) }.getOrNull()
            val target = u?.getQueryParameter("screen") ?: u?.host ?: dl
            resolve(target)?.let { return it }
        }
        // 2) screen=<đích>  3) fallback utm_content / utm_campaign
        return resolve(q.getQueryParameter("screen"))
            ?: resolve(q.getQueryParameter("utm_content"))
            ?: resolve(q.getQueryParameter("utm_campaign"))
    }
}
