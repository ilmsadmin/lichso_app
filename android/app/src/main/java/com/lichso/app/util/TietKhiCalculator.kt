package com.lichso.app.util

import kotlin.math.floor

/**
 * Tính Tiết Khí (24 Solar Terms)
 */
object TietKhiCalculator {

    data class SolarTerm(
        val name: String,
        val longitude: Int,
        val jd: Int,
        val dd: Int,
        val mm: Int,
        val yy: Int
    )

    data class SolarTermInfo(
        val current: SolarTerm?,
        val next: SolarTerm?,
        val daysUntilNext: Int
    )

    private val TERM_NAMES = listOf(
        "Xuân Phân" to 0, "Thanh Minh" to 15, "Cốc Vũ" to 30,
        "Lập Hạ" to 45, "Tiểu Mãn" to 60, "Mang Chủng" to 75,
        "Hạ Chí" to 90, "Tiểu Thử" to 105, "Đại Thử" to 120,
        "Lập Thu" to 135, "Xử Thử" to 150, "Bạch Lộ" to 165,
        "Thu Phân" to 180, "Hàn Lộ" to 195, "Sương Giáng" to 210,
        "Lập Đông" to 225, "Tiểu Tuyết" to 240, "Đại Tuyết" to 255,
        "Đông Chí" to 270, "Tiểu Hàn" to 285, "Đại Hàn" to 300,
        "Lập Xuân" to 315, "Vũ Thủy" to 330, "Kinh Trập" to 345
    )

    private fun findSolarTermDate(year: Int, termLongitude: Int): Int {
        val daysPerDegree = 365.25 / 360.0
        val marchEquinox = LunarCalendarUtil.jdFromDate(20, 3, year)
        var diff = termLongitude.toDouble()
        if (diff < 0) diff += 360.0
        val estJd = marchEquinox + floor(diff * daysPerDegree).toInt()

        for (jd in (estJd - 5)..(estJd + 5)) {
            val l1 = LunarCalendarUtil.getSunLongitudeDegree(jd, LunarCalendarUtil.TZ)
            var l2 = LunarCalendarUtil.getSunLongitudeDegree(jd + 1, LunarCalendarUtil.TZ)
            if (l2 < l1) l2 += 360.0
            val target = termLongitude.toDouble()
            if (l1 <= target && target < l2) return jd
            if (l1 <= target + 360 && target + 360 < l2) return jd
        }
        return estJd
    }

    fun getAllSolarTerms(year: Int): List<SolarTerm> {
        val terms = mutableListOf<SolarTerm>()

        for ((name, lon) in TERM_NAMES) {
            val jd = findSolarTermDate(year, lon)
            val (dd, mm, yy) = LunarCalendarUtil.jdToDate(jd)
            terms.add(SolarTerm(name, lon, jd, dd, mm, yy))
        }

        terms.sortBy { it.jd }
        return terms
    }

    fun getCurrentSolarTerm(dd: Int, mm: Int, yy: Int): SolarTermInfo {
        val todayJd = LunarCalendarUtil.jdFromDate(dd, mm, yy)
        val prevTerms = getAllSolarTerms(yy - 1)
        val terms = getAllSolarTerms(yy)
        val nextTerms = getAllSolarTerms(yy + 1)
        val allTerms = (prevTerms + terms + nextTerms).sortedBy { it.jd }

        var current: SolarTerm? = null
        var next: SolarTerm? = null

        for (i in 0 until allTerms.size - 1) {
            if (todayJd >= allTerms[i].jd && todayJd < allTerms[i + 1].jd) {
                current = allTerms[i]
                next = allTerms[i + 1]
                break
            }
        }

        return SolarTermInfo(
            current = current,
            next = next,
            daysUntilNext = if (next != null) next.jd - todayJd else 0
        )
    }
}
