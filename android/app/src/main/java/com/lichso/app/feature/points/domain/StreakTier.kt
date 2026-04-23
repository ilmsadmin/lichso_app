package com.lichso.app.feature.points.domain

/**
 * Streak tier — nhân hệ số điểm theo chuỗi ngày liên tiếp.
 */
enum class StreakTier(
    val minDays: Int,
    val dailyMultiplier: Float,
    val permanentMultiplier: Float,
    val displayName: String
) {
    BEGINNER (0,   1.0f, 1.0f, "Tân thủ"),
    WEEK     (7,   1.5f, 1.2f, "Tu tập"),
    MONTH    (30,  2.0f, 1.5f, "Kiên tâm"),
    CENTURY  (100, 3.0f, 2.0f, "Đại định"),
    YEAR     (365, 5.0f, 3.0f, "Thiên mệnh");

    companion object {
        fun fromStreak(days: Int): StreakTier =
            entries.last { days >= it.minDays }

        fun nextOf(current: StreakTier): StreakTier? =
            entries.getOrNull(current.ordinal + 1)
    }
}
