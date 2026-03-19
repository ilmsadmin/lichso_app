package com.lichso.app.util

/**
 * Tính Giờ Hoàng Đạo dựa trên Chi của ngày
 */
object GioHoangDaoCalculator {

    data class GioHoangDao(val name: String, val time: String)

    private val GIO_LABELS = listOf(
        GioHoangDao("Tý", "23h–1h"), GioHoangDao("Sửu", "1h–3h"),
        GioHoangDao("Dần", "3h–5h"), GioHoangDao("Mão", "5h–7h"),
        GioHoangDao("Thìn", "7h–9h"), GioHoangDao("Tỵ", "9h–11h"),
        GioHoangDao("Ngọ", "11h–13h"), GioHoangDao("Mùi", "13h–15h"),
        GioHoangDao("Thân", "15h–17h"), GioHoangDao("Dậu", "17h–19h"),
        GioHoangDao("Tuất", "19h–21h"), GioHoangDao("Hợi", "21h–23h")
    )

    // Bảng tra giờ hoàng đạo theo Chi ngày
    private val HOANG_DAO = mapOf(
        0 to listOf(0, 1, 4, 5, 8, 9),     // Ngày Tý
        1 to listOf(2, 3, 6, 7, 10, 11),   // Ngày Sửu
        2 to listOf(0, 1, 4, 5, 8, 9),     // Ngày Dần
        3 to listOf(2, 3, 6, 7, 10, 11),   // Ngày Mão
        4 to listOf(0, 1, 4, 5, 8, 9),     // Ngày Thìn
        5 to listOf(2, 3, 6, 7, 10, 11),   // Ngày Tỵ
        6 to listOf(0, 1, 4, 5, 8, 9),     // Ngày Ngọ
        7 to listOf(2, 3, 6, 7, 10, 11),   // Ngày Mùi
        8 to listOf(0, 1, 4, 5, 8, 9),     // Ngày Thân
        9 to listOf(2, 3, 6, 7, 10, 11),   // Ngày Dậu
        10 to listOf(0, 1, 4, 5, 8, 9),    // Ngày Tuất
        11 to listOf(2, 3, 6, 7, 10, 11)   // Ngày Hợi
    )

    fun getGioHoangDao(jd: Int): List<GioHoangDao> {
        val chiOfDay = (jd + 1) % 12
        val gioTot = HOANG_DAO[chiOfDay] ?: listOf(0, 1, 4, 5, 8, 9)
        return gioTot.map { GIO_LABELS[it] }
    }
}
