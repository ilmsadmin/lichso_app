package com.lichso.app.feature.points.domain

/**
 * Tính năng mở theo NGÀY — tiêu [cost] ⚡ điểm ngày để unlock, khoá lại sau 00:00.
 */
enum class DailyUnlockKey(val cost: Int, val label: String, val description: String) {
    DETAILED_HOROSCOPE (20,  "Tử vi chi tiết",      "Luận giải sao chiếu mệnh, cát hung hôm nay"),
    VAN_KHAN_FULL      (30,  "Văn khấn đầy đủ",     "Mở khoá toàn bộ 1 bài văn khấn"),
    AI_MASTER_10_MSG   (40,  "AI Thầy Số (10 tin)", "Chat với Thầy Số 10 tin nhắn trong ngày"),
    SEASONAL_THEME     (25,  "Theme hôm nay",       "Giao diện đặc biệt theo tiết khí"),
    LUCKY_HOURS_FULL   (15,  "Giờ hoàng đạo chi tiết", "Xem đầy đủ 12 canh giờ với luận giải"),
    WEEK_FORTUNE_CHART (50,  "Biểu đồ vận hạn tuần", "Theo dõi vận hạn 7 ngày tới"),
    LA_SO_TU_VI        (60,  "Lá số tử vi hôm nay", "Lập lá số tử vi tức thời"),
    DAILY_ZODIAC_CARD  (20,  "Mở thẻ 12 con giáp",  "Rút 1 thẻ con giáp ngẫu nhiên"),
    CHOOSE_DAY_TOOL    (150, "Chọn ngày tốt (1 lần)","Công cụ chọn ngày cưới/động thổ/khai trương"),
}
