package com.lichso.app.util

/**
 * Ngày tốt/xấu — Nên làm / Không nên — Hướng tốt
 */
object DayActivityCalculator {

    data class DayActivities(
        val nenLam: List<String>,
        val khongNen: List<String>,
        val isXauDay: Boolean,
        val isNguyetKy: Boolean,
        val isTamNuong: Boolean
    )

    data class HuongTot(
        val thanTai: String,
        val hyThan: String,
        val hungThan: String
    )

    private val NEN_LAM_LIST = listOf(
        listOf("Giao dịch, ký kết", "Xuất hành", "Cưới hỏi", "Khai trương"),
        listOf("Cầu tài, cầu phúc", "Nhập học", "An sàng", "Dựng cột"),
        listOf("Động thổ, xây dựng", "Nhập trạch", "Giao dịch", "Cầu tài"),
        listOf("Cầu an, giải hạn", "Tu sửa", "Nhập học", "Cưới hỏi"),
        listOf("Xuất hành", "Khai trương", "Giao dịch", "Ký kết"),
        listOf("Cầu phúc, cúng tế", "An táng", "Nhập trạch", "Tu sửa"),
        listOf("Giao dịch, ký kết", "Xuất hành", "Khai trương", "Nhập học"),
        listOf("Cầu tài", "An sàng", "Tu sửa", "Nhập trạch"),
        listOf("Động thổ", "Dựng cột", "Giao dịch", "Xuất hành"),
        listOf("Cầu an", "Tu sửa", "Nhập học", "Cầu tài"),
        listOf("Cưới hỏi", "Xuất hành", "Ký kết", "Khai trương"),
        listOf("Cầu phúc", "An sàng", "Nhập trạch", "Giao dịch")
    )

    private val KHONG_NEN_LIST = listOf(
        listOf("An táng", "Kiện cáo"),
        listOf("Xuất hành xa", "Phá thổ"),
        listOf("Cưới hỏi", "Kiện cáo"),
        listOf("Khai trương", "An táng"),
        listOf("An táng", "Tu sửa"),
        listOf("Xuất hành", "Kiện cáo"),
        listOf("An táng", "Phá thổ"),
        listOf("Khai trương", "Kiện cáo"),
        listOf("Cưới hỏi", "An táng"),
        listOf("Xuất hành xa", "Phá thổ"),
        listOf("An táng", "Kiện cáo"),
        listOf("Xuất hành", "Tu sửa")
    )

    private val THAN_TAI = listOf(
        "Đông-Nam", "Đông", "Bắc", "Bắc", "Đông-Bắc",
        "Đông", "Tây-Nam", "Tây", "Bắc", "Nam"
    )

    private val HY_THAN = listOf(
        "Đông-Bắc", "Tây-Bắc", "Tây-Nam", "Nam", "Đông-Nam",
        "Đông-Bắc", "Tây-Bắc", "Tây-Nam", "Nam", "Đông-Nam"
    )

    private val HUNG_THAN = listOf(
        "Nam", "Đông", "Bắc", "Tây", "Nam", "Đông",
        "Bắc", "Tây", "Nam", "Đông", "Bắc", "Tây"
    )

    fun getDayActivities(jd: Int, lunarDay: Int, lunarMonth: Int): DayActivities {
        val chiIdx = (jd + 1) % 12

        val isNguyetKy = lunarDay == 5 || lunarDay == 14 || lunarDay == 23
        val isTamNuong = lunarDay in listOf(3, 7, 13, 18, 22, 27)

        val nenLam = NEN_LAM_LIST.getOrElse(chiIdx) { listOf("Giao dịch", "Xuất hành") }
        val khongNen = KHONG_NEN_LIST.getOrElse(chiIdx) { listOf("An táng") }.toMutableList()

        val isXauDay = isNguyetKy || isTamNuong
        if (isNguyetKy) {
            khongNen.add(0, "Nguyệt kỵ — kiêng mọi việc lớn")
        }
        if (isTamNuong) {
            khongNen.add(0, "Tam nương sát — không cưới hỏi")
        }

        return DayActivities(nenLam, khongNen, isXauDay, isNguyetKy, isTamNuong)
    }

    fun getHuongTot(jd: Int): HuongTot {
        val canIdx = (jd + 9) % 10
        val chiIdx = (jd + 1) % 12
        return HuongTot(
            thanTai = THAN_TAI[canIdx],
            hyThan = HY_THAN[canIdx],
            hungThan = HUNG_THAN[chiIdx]
        )
    }
}
