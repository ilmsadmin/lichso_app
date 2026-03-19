package com.lichso.app.util

import kotlin.math.floor

/**
 * Can Chi Calculator — Tính Can Chi cho năm, tháng, ngày
 */
object CanChiCalculator {

    val THIEN_CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    val DIA_CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    val THU = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")

    fun getYearCanChi(lunarYear: Int): String {
        val can = THIEN_CAN[(lunarYear + 6) % 10]
        val chi = DIA_CHI[(lunarYear + 8) % 12]
        return "$can $chi"
    }

    fun getMonthCanChi(lunarMonth: Int, lunarYear: Int): String {
        val chiIdx = (lunarMonth + 1) % 12
        val chi = DIA_CHI[chiIdx]
        val canIdx = (lunarYear * 12 + lunarMonth + 3) % 10
        val can = THIEN_CAN[canIdx]
        return "$can $chi"
    }

    fun getDayCanChi(jd: Int): String {
        val can = THIEN_CAN[(jd + 9) % 10]
        val chi = DIA_CHI[(jd + 1) % 12]
        return "$can $chi"
    }

    fun getDayOfWeek(jd: Int): String {
        return THU[jd % 7]
    }

    fun getDayOfWeekIndex(jd: Int): Int {
        return jd % 7 // 0=Mon ... 6=Sun
    }
}
