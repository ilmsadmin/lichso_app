package com.lichso.app.feature.points.domain

/**
 * Tính năng mở VĨNH VIỄN khi đạt cấp bậc công đức tương ứng.
 */
enum class PermanentUnlockKey(val label: String) {
    REMOVE_ADS           ("Bỏ quảng cáo vĩnh viễn"),
    THEME_TRANG_RAM      ("Theme Trăng Rằm (Dark Mode xịn)"),
    AI_20_MSG_PER_DAY    ("AI Thầy Số 20 tin/ngày"),
    WATERMARKED_EXPORT   ("Export ảnh quẻ có watermark tên bạn"),
    LUCKY_HOURS_PERMANENT("Giờ hoàng đạo chi tiết vĩnh viễn"),
    PREMIUM_THEMES       ("Bộ 5 theme Premium"),
    CHOOSE_DAY_TOOL_PERM ("Công cụ chọn ngày cưới / động thổ"),
    AI_UNLIMITED         ("AI Thầy Số không giới hạn"),
    BABY_NAME_TOOL       ("Đặt tên con / Thương hiệu"),
    PDF_EXPORT           ("Export PDF đẹp"),
    ALL_PREMIUM          ("Tất cả tính năng Premium"),
    BADGE_CROWN          ("Huy hiệu vương miện cạnh tên"),
    GOLD_FRAME           ("Avatar frame rồng vàng"),
}

/**
 * Cấp bậc tích luỹ.
 */
enum class PermanentRank(
    val threshold: Long,
    val displayName: String,
    val unlocks: List<PermanentUnlockKey>
) {
    // Ngưỡng thiết kế: người dùng casual (~6 ☯/ngày) đạt Thiên sư sau ~2 năm (~800 ngày)
    // Người dùng tích cực (~15 ☯/ngày) đạt sau ~1 năm. Max hiển thị: ≤ 9.999 (không cần chữ K)
    NOVICE     (0L,    "Vô danh",    emptyList()),
    SO_CO      (30L,   "Sơ cơ",      listOf(
        PermanentUnlockKey.REMOVE_ADS,
        PermanentUnlockKey.THEME_TRANG_RAM
    )),
    TU_TAP     (100L,  "Tu tập",     listOf(
        PermanentUnlockKey.AI_20_MSG_PER_DAY,
        PermanentUnlockKey.WATERMARKED_EXPORT
    )),
    THONG_THAO (300L,  "Thông thạo", listOf(
        PermanentUnlockKey.LUCKY_HOURS_PERMANENT,
        PermanentUnlockKey.PREMIUM_THEMES
    )),
    DAO_SI     (800L,  "Đạo sĩ",     listOf(
        PermanentUnlockKey.CHOOSE_DAY_TOOL_PERM
    )),
    CHAN_NHAN  (2_000L,"Chân nhân",  listOf(
        PermanentUnlockKey.AI_UNLIMITED,
        PermanentUnlockKey.BABY_NAME_TOOL,
        PermanentUnlockKey.PDF_EXPORT
    )),
    THIEN_SU   (5_000L,"Thiên sư",   listOf(
        PermanentUnlockKey.ALL_PREMIUM,
        PermanentUnlockKey.BADGE_CROWN,
        PermanentUnlockKey.GOLD_FRAME
    ));

    companion object {
        fun fromTotal(total: Long): PermanentRank =
            entries.last { total >= it.threshold }

        fun nextOf(current: PermanentRank): PermanentRank? =
            entries.getOrNull(current.ordinal + 1)
    }
}
