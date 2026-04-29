package com.lichso.app.feature.points.domain

/**
 * Categories for grouping ActionTypes in analytics & UI suggestions.
 */
enum class ActionCategory {
    ENGAGEMENT, NAVIGATION, DEEP, VIRAL, AD, LOCATION, MILESTONE
}

/**
 * Every action a user can perform that awards points.
 *
 * - [dailyPoints]     → ⚡ "Điểm ngày" (reset 00:00)
 * - [permanentPoints] → ☯️ "Điểm vĩnh viễn" (cumulative, never reset)
 * - [dailyCap]        → maximum times per day this action rewards points (-1 = unlimited)
 * - [category]        → for analytics & UnlockSuggester
 * - [label]           → short VN label shown in suggestions
 * - [deeplink]        → internal deeplink used by ActionSuggestion
 */
enum class ActionType(
    val dailyPoints: Int,
    val permanentPoints: Int,
    val dailyCap: Int,
    val category: ActionCategory,
    val label: String,
    val deeplink: String
) {
    // ── Daily engagement ──────────────────────────────────────────
    DAILY_CHECK_IN         (10, 5,   1,  ActionCategory.ENGAGEMENT, "Điểm danh hôm nay", "lichso://home"),
    VIEW_FORTUNE_CARD      (5,  2,   1,  ActionCategory.ENGAGEMENT, "Xem thẻ vận mệnh", "lichso://home"),
    DRAW_KINH_DICH         (15, 5,   1,  ActionCategory.ENGAGEMENT, "Rút quẻ Kinh Dịch", "lichso://oracle"),

    // ── Screen visits ─────────────────────────────────────────────
    VISIT_LUNAR_CALENDAR   (3,  1,   3,  ActionCategory.NAVIGATION, "Xem lịch vạn niên", "lichso://calendar"),
    VISIT_VAN_KHAN         (5,  2,   3,  ActionCategory.NAVIGATION, "Mở văn khấn", "lichso://prayers"),
    VISIT_TU_VI            (4,  2,   3,  ActionCategory.NAVIGATION, "Xem tử vi", "lichso://tools"),

    // ── Deep engagement ───────────────────────────────────────────
    READ_VAN_KHAN_FULL     (20, 10,  5,  ActionCategory.DEEP,       "Đọc hết 1 bài văn khấn", "lichso://prayers"),
    CHAT_AI_MESSAGE        (2,  1,  10,  ActionCategory.DEEP,       "Chat với Thầy Số",       "lichso://chat"),
    CREATE_REMINDER        (5,  3,   5,  ActionCategory.DEEP,       "Tạo nhắc nhở mới",       "lichso://tasks"),
    COMPLETE_REMINDER      (10, 5,  -1,  ActionCategory.DEEP,       "Hoàn thành nhắc nhở",    "lichso://tasks"),

    // ── Viral / Share ─────────────────────────────────────────────
    SHARE_TO_SOCIAL        (30, 20,  3,  ActionCategory.VIRAL,      "Chia sẻ lên mạng xã hội","lichso://home"),
    INVITE_FRIEND_SENT     (0,  200, -1, ActionCategory.VIRAL,      "Mời bạn cài app",        "lichso://profile"),
    INVITE_FRIEND_INSTALLED(0,  500, -1, ActionCategory.VIRAL,      "Bạn đã cài app",         "lichso://profile"),
    RATE_APP_5_STAR        (0,  1000, 1, ActionCategory.VIRAL,      "Đánh giá 5★",           "lichso://profile"),

    // ── Rewarded ads ──────────────────────────────────────────────
    WATCH_REWARDED_AD      (20, 0,   5,  ActionCategory.AD,         "Xem quảng cáo thưởng",   "lichso://store"),

    // ── Location / Spiritual ──────────────────────────────────────
    CHECKIN_TEMPLE         (0,  50,  3,  ActionCategory.LOCATION,   "Check-in chùa/đền",      "lichso://tools"),

    // ── Streak milestones (auto-awarded) ──────────────────────────
    STREAK_7_DAYS          (0,  300,  1, ActionCategory.MILESTONE,  "Chuỗi 7 ngày",           "lichso://profile"),
    STREAK_30_DAYS         (0,  1500, 1, ActionCategory.MILESTONE,  "Chuỗi 30 ngày",          "lichso://profile"),
    STREAK_100_DAYS        (0,  10_000, 1, ActionCategory.MILESTONE, "Chuỗi 100 ngày",        "lichso://profile"),
    STREAK_365_DAYS        (0,  50_000, 1, ActionCategory.MILESTONE, "Chuỗi 365 ngày",        "lichso://profile"),
}
