package com.lichso.app.util

/**
 * Tính Trực của ngày (Kiến Trừ 12 Trực)
 * 12 Trực: Kiến, Trừ, Mãn, Bình, Định, Chấp, Phá, Nguy, Thành, Thu, Khai, Bế
 */
object TrucNgayCalculator {

    data class TrucNgayInfo(
        val name: String,
        val rating: String, // "Tốt", "Xấu", "Trung bình"
    )

    private val TRUC_NAMES = listOf(
        "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp",
        "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế"
    )

    private val TRUC_RATING = mapOf(
        "Kiến" to "Trung bình",
        "Trừ" to "Tốt",
        "Mãn" to "Tốt",
        "Bình" to "Tốt",
        "Định" to "Tốt",
        "Chấp" to "Trung bình",
        "Phá" to "Xấu",
        "Nguy" to "Xấu",
        "Thành" to "Tốt",
        "Thu" to "Trung bình",
        "Khai" to "Tốt",
        "Bế" to "Xấu",
    )

    /**
     * Tính trực của ngày dựa trên Chi ngày và tháng âm lịch
     */
    fun getTrucNgay(jd: Int, lunarMonth: Int): TrucNgayInfo {
        val chiOfDay = (jd + 1) % 12
        // Công thức: Trực = (Chi ngày - tháng âm lịch + 2) mod 12
        val trucIdx = ((chiOfDay - (lunarMonth - 1) + 12) % 12)
        val name = TRUC_NAMES[trucIdx]
        val rating = TRUC_RATING[name] ?: "Trung bình"
        return TrucNgayInfo(name, rating)
    }
}
