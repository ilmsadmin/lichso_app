package com.lichso.app.util

/**
 * Tính Sao chiếu (28 sao - Nhị thập bát tú) của ngày
 */
object SaoChieuCalculator {

    data class SaoChieuInfo(
        val name: String,
        val rating: String, // "Tốt", "Xấu", "Trung bình"
    )

    // 28 sao (Nhị thập bát tú)
    private val SAO_NAMES = listOf(
        "Giác", "Cang", "Đê", "Phòng", "Tâm", "Vĩ", "Cơ",
        "Đẩu", "Ngưu", "Nữ", "Hư", "Nguy", "Thất", "Bích",
        "Khuê", "Lâu", "Vị", "Mão", "Tất", "Chủy", "Sâm",
        "Tỉnh", "Quỷ", "Liễu", "Tinh", "Trương", "Dực", "Chẩn"
    )

    private val SAO_RATING = mapOf(
        "Giác" to "Tốt", "Cang" to "Xấu", "Đê" to "Xấu", "Phòng" to "Tốt",
        "Tâm" to "Xấu", "Vĩ" to "Tốt", "Cơ" to "Tốt",
        "Đẩu" to "Tốt", "Ngưu" to "Xấu", "Nữ" to "Xấu", "Hư" to "Xấu",
        "Nguy" to "Trung bình", "Thất" to "Tốt", "Bích" to "Tốt",
        "Khuê" to "Xấu", "Lâu" to "Tốt", "Vị" to "Tốt", "Mão" to "Xấu",
        "Tất" to "Tốt", "Chủy" to "Xấu", "Sâm" to "Tốt",
        "Tỉnh" to "Tốt", "Quỷ" to "Xấu", "Liễu" to "Xấu",
        "Tinh" to "Tốt", "Trương" to "Tốt", "Dực" to "Tốt", "Chẩn" to "Tốt"
    )

    /**
     * Tính sao chiếu dựa trên Julian Day
     * Sao chiếu = (jd + 15) % 28
     */
    fun getSaoChieu(jd: Int): SaoChieuInfo {
        val saoIdx = (jd + 15) % 28
        val name = SAO_NAMES[saoIdx.coerceIn(0, 27)]
        val rating = SAO_RATING[name] ?: "Trung bình"
        return SaoChieuInfo(name, rating)
    }
}
