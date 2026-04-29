package com.lichso.app.feature.datepicker

import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.domain.model.DayInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mục đích chọn ngày — Phase 3 "Công cụ chọn ngày".
 */
enum class DatePurpose(
    val displayName: String,
    val emoji: String,
    /** Từ khoá kiểm tra trong DayInfo.activities.nenLam (cộng điểm). */
    val nenLamKeywords: List<String>,
    /** Từ khoá kiểm tra trong DayInfo.activities.khongNen (trừ điểm). */
    val khongNenKeywords: List<String>,
) {
    CUOI_HOI(
        "Cưới hỏi", "💍",
        nenLamKeywords = listOf("Cưới", "Hôn", "Đính"),
        khongNenKeywords = listOf("An táng"),
    ),
    DONG_THO(
        "Động thổ", "🏗️",
        nenLamKeywords = listOf("Động thổ", "Khởi công", "Sửa chữa"),
        khongNenKeywords = listOf("An táng", "Kiện cáo"),
    ),
    KHAI_TRUONG(
        "Khai trương", "🎊",
        nenLamKeywords = listOf("Khai trương", "Khai trang", "Khởi sự", "Cầu tài"),
        khongNenKeywords = listOf("An táng"),
    ),
    NHAP_TRACH(
        "Nhập trạch", "🏠",
        nenLamKeywords = listOf("Nhập trạch", "Dọn nhà", "Nhập học"),
        khongNenKeywords = emptyList(),
    ),
    XUAT_HANH(
        "Xuất hành", "🧳",
        nenLamKeywords = listOf("Xuất hành", "Cầu tài", "Giao dịch"),
        khongNenKeywords = listOf("Kiện cáo"),
    ),
    AN_TANG(
        "An táng", "⚱️",
        nenLamKeywords = listOf("An táng", "Cải táng"),
        khongNenKeywords = listOf("Cưới", "Khai trương"),
    ),
    KY_KET(
        "Ký kết hợp đồng", "📝",
        nenLamKeywords = listOf("Ký kết", "Giao dịch", "Cầu tài"),
        khongNenKeywords = listOf("Kiện cáo"),
    ),
}

/** Lục xung — các cặp địa chi xung khắc nhau (mất 25 điểm khi gặp). */
private val LUC_XUNG = mapOf(
    "Tý" to "Ngọ", "Ngọ" to "Tý",
    "Sửu" to "Mùi", "Mùi" to "Sửu",
    "Dần" to "Thân", "Thân" to "Dần",
    "Mão" to "Dậu", "Dậu" to "Mão",
    "Thìn" to "Tuất", "Tuất" to "Thìn",
    "Tỵ" to "Hợi", "Hợi" to "Tỵ",
)

/** Tam hợp — bộ ba địa chi tương hợp (cộng 10 điểm khi gặp). */
private val TAM_HOP = listOf(
    setOf("Thân", "Tý", "Thìn"),
    setOf("Hợi", "Mão", "Mùi"),
    setOf("Dần", "Ngọ", "Tuất"),
    setOf("Tỵ", "Dậu", "Sửu"),
)

data class CandidateDay(
    val info: DayInfo,
    val score: Int,
    val reasons: List<String>,
    val warnings: List<String>,
)

@Singleton
class DatePickerScorer @Inject constructor(
    private val dayInfoProvider: DayInfoProvider,
) {

    /**
     * Chấm điểm các ngày trong khoảng [fromJd, toJd] và trả về top [topN].
     *
     * @param userBirthChi địa chi tuổi của user (ví dụ "Sửu"); null = bỏ qua xung tuổi.
     */
    fun scoreRange(
        fromJd: Int,
        toJd: Int,
        purpose: DatePurpose,
        userBirthChi: String? = null,
        topN: Int = 5,
    ): List<CandidateDay> {
        require(toJd >= fromJd) { "Invalid range" }
        val results = mutableListOf<CandidateDay>()

        for (jd in fromJd..toJd) {
            val (dd, mm, yy) = com.lichso.app.util.LunarCalendarUtil.jdToDate(jd)
            val info = dayInfoProvider.getDayInfo(dd, mm, yy)
            val (score, reasons, warnings) = computeScore(info, purpose, userBirthChi)
            results.add(CandidateDay(info, score, reasons, warnings))
        }

        return results.sortedByDescending { it.score }.take(topN)
    }

    private fun computeScore(
        info: DayInfo,
        purpose: DatePurpose,
        userBirthChi: String?,
    ): Triple<Int, List<String>, List<String>> {
        var score = info.dayRating.percent // 10..100
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // ── Mục đích nên làm / không nên ──
        val nenLamHit = purpose.nenLamKeywords.any { kw ->
            info.activities.nenLam.any { it.contains(kw, ignoreCase = true) }
        }
        if (nenLamHit) {
            score += 25
            reasons.add("Hợp việc ${purpose.displayName.lowercase()}")
        }
        val khongNenHit = purpose.khongNenKeywords.any { kw ->
            info.activities.khongNen.any { it.contains(kw, ignoreCase = true) }
        }
        if (khongNenHit) {
            score -= 30
            warnings.add("Không nên ${purpose.displayName.lowercase()}")
        }

        // ── Trực ngày & Sao chiếu ──
        if (info.trucNgay.rating == "Tốt") reasons.add("Trực ${info.trucNgay.name} (tốt)")
        else if (info.trucNgay.rating == "Xấu") warnings.add("Trực ${info.trucNgay.name} (xấu)")
        if (info.saoChieu.rating == "Tốt") reasons.add("${info.saoChieu.name} chiếu mệnh")
        else if (info.saoChieu.rating == "Xấu") warnings.add("Sao ${info.saoChieu.name} (xấu)")

        // ── Ngày kiêng kỵ ──
        if (info.activities.isNguyetKy) {
            score -= 15
            warnings.add("Nguyệt kỵ (mùng 5/14/23)")
        }
        if (info.activities.isTamNuong) {
            score -= 12
            warnings.add("Tam nương sát")
        }

        // ── Xung tuổi ──
        if (!userBirthChi.isNullOrBlank()) {
            val dayChi = info.dayCanChi.split(" ").lastOrNull()
            if (dayChi != null) {
                if (LUC_XUNG[userBirthChi] == dayChi) {
                    score -= 25
                    warnings.add("Xung tuổi $userBirthChi")
                } else if (TAM_HOP.any { it.containsAll(listOf(userBirthChi, dayChi)) }) {
                    score += 10
                    reasons.add("Tam hợp với tuổi $userBirthChi")
                }
            }
        }

        // ── Hoàng đạo bonus ──
        val hasHoangDao = info.gioHoangDao.isNotEmpty()
        if (hasHoangDao && info.gioHoangDao.size >= 4) {
            reasons.add("Nhiều giờ hoàng đạo")
        }

        score = score.coerceIn(0, 200)
        return Triple(score, reasons, warnings)
    }
}
