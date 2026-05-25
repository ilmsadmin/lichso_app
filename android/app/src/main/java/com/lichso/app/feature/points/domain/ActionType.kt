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
    // dailyPoints (⚡ reset 00:00) giữ nguyên để không ảnh hưởng Daily Store
    // permanentPoints (☯) giảm mạnh — chỉ action thực sự có ý nghĩa mới cho ☯
    DAILY_CHECK_IN         (10, 2,   1,  ActionCategory.ENGAGEMENT, "Điểm danh hôm nay", "lichso://home"),
    VIEW_FORTUNE_CARD      (5,  1,   1,  ActionCategory.ENGAGEMENT, "Xem thẻ vận mệnh", "lichso://home"),
    DRAW_KINH_DICH         (15, 2,   1,  ActionCategory.ENGAGEMENT, "Rút quẻ Kinh Dịch", "lichso://oracle"),
    VIEW_DAY_DETAIL        (3,  0,   8,  ActionCategory.ENGAGEMENT, "Xem chi tiết 1 ngày", "lichso://calendar"),
    VIEW_TIET_KHI          (5,  1,   1,  ActionCategory.ENGAGEMENT, "Xem tiết khí hôm nay", "lichso://tiet_khi"),
    VIEW_LEDGER            (2,  0,   1,  ActionCategory.ENGAGEMENT, "Mở nhật ký điểm",      "lichso://ledger"),
    VIEW_PROFILE           (2,  0,   1,  ActionCategory.ENGAGEMENT, "Mở hồ sơ cá nhân",     "lichso://profile"),
    VIEW_DAILY_STORE       (3,  0,   1,  ActionCategory.ENGAGEMENT, "Mở kho mở khoá",       "lichso://store"),
    VIEW_HISTORY_TODAY     (5,  1,   1,  ActionCategory.ENGAGEMENT, "Ngày này năm xưa",     "lichso://history"),

    // ── Screen visits ─────────────────────────────────────────────
    VISIT_LUNAR_CALENDAR   (3,  0,   3,  ActionCategory.NAVIGATION, "Xem lịch vạn niên", "lichso://calendar"),
    VISIT_VAN_KHAN         (5,  1,   1,  ActionCategory.NAVIGATION, "Mở văn khấn", "lichso://prayers"),
    VISIT_TU_VI            (4,  1,   1,  ActionCategory.NAVIGATION, "Xem tử vi", "lichso://tools"),
    VISIT_TOOLS            (3,  0,   3,  ActionCategory.NAVIGATION, "Khám phá tiện ích", "lichso://tools"),
    USE_LUNAR_CONVERTER    (4,  0,   3,  ActionCategory.NAVIGATION, "Đổi ngày Âm/Dương",  "lichso://search"),
    USE_ZODIAC_COMPAT      (5,  1,   2,  ActionCategory.NAVIGATION, "Xem tuổi hợp",        "lichso://search"),
    USE_DATE_PICKER        (8,  1,   2,  ActionCategory.NAVIGATION, "Chọn ngày tốt",       "lichso://date_picker"),

    // ── Deep engagement ───────────────────────────────────────────
    READ_VAN_KHAN_FULL     (20, 2,   3,  ActionCategory.DEEP,       "Đọc hết 1 bài văn khấn", "lichso://prayers"),
    CHAT_AI_MESSAGE        (2,  0,  10,  ActionCategory.DEEP,       "Chat với Thầy Số",       "lichso://chat"),
    CREATE_REMINDER        (5,  1,   3,  ActionCategory.DEEP,       "Tạo nhắc nhở mới",       "lichso://tasks"),
    COMPLETE_REMINDER      (10, 1,  -1,  ActionCategory.DEEP,       "Hoàn thành nhắc nhở",    "lichso://tasks"),
    ADD_BOOKMARK           (5,  1,   3,  ActionCategory.DEEP,       "Đánh dấu 1 ngày",       "lichso://bookmarks"),
    COMPLETE_TUTORIAL      (0,  20,  1,  ActionCategory.DEEP,       "Hoàn thành hướng dẫn",  "lichso://tutorial"),
    OPEN_APP_FROM_WIDGET   (3,  0,   3,  ActionCategory.DEEP,       "Mở app từ widget",       "lichso://home"),

    // ── Viral / Share ─────────────────────────────────────────────
    SHARE_TO_SOCIAL        (30, 3,   1,  ActionCategory.VIRAL,      "Chia sẻ lên mạng xã hội","lichso://home"),
    INVITE_FRIEND_SENT     (0,  15, -1,  ActionCategory.VIRAL,      "Mời bạn cài app",        "lichso://profile"),
    INVITE_FRIEND_INSTALLED(0,  50, -1,  ActionCategory.VIRAL,      "Bạn đã cài app",         "lichso://profile"),
    RATE_APP_5_STAR        (0,  100, 1,  ActionCategory.VIRAL,      "Đánh giá 5★",           "lichso://profile"),

    // ── Rewarded ads ──────────────────────────────────────────────
    WATCH_REWARDED_AD      (20, 0,   5,  ActionCategory.AD,         "Xem quảng cáo thưởng",   "lichso://store"),

    // ── Location / Spiritual ──────────────────────────────────────
    CHECKIN_TEMPLE         (0,  5,   1,  ActionCategory.LOCATION,   "Check-in chùa/đền",      "lichso://tools"),

    // ── Streak milestones (auto-awarded) ──────────────────────────
    STREAK_7_DAYS          (0,  5,   1,  ActionCategory.MILESTONE,  "Chuỗi 7 ngày",           "lichso://profile"),
    STREAK_30_DAYS         (0,  30,  1,  ActionCategory.MILESTONE,  "Chuỗi 30 ngày",          "lichso://profile"),
    STREAK_100_DAYS        (0,  150, 1,  ActionCategory.MILESTONE,  "Chuỗi 100 ngày",         "lichso://profile"),
    STREAK_365_DAYS        (0,  500, 1,  ActionCategory.MILESTONE,  "Chuỗi 365 ngày",         "lichso://profile"),
}
