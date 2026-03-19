package com.lichso.app.util

/**
 * Ngày lễ Dương lịch & Âm lịch Việt Nam
 */
object HolidayUtil {

    val SOLAR_HOLIDAYS = mapOf(
        "1/1" to "Tết Dương lịch",
        "14/2" to "Valentine",
        "8/3" to "Quốc tế Phụ nữ",
        "30/4" to "Giải phóng miền Nam",
        "1/5" to "Quốc tế Lao động",
        "19/5" to "Sinh nhật Bác Hồ",
        "1/6" to "Quốc tế Thiếu nhi",
        "27/7" to "Thương binh Liệt sĩ",
        "2/9" to "Quốc khánh",
        "20/10" to "Phụ nữ Việt Nam",
        "20/11" to "Nhà giáo Việt Nam",
        "22/12" to "Quân đội Nhân dân VN",
        "24/12" to "Giáng sinh",
        "25/12" to "Giáng sinh"
    )

    val LUNAR_HOLIDAYS = mapOf(
        "1/1" to "Tết Nguyên Đán",
        "2/1" to "Mùng 2 Tết",
        "3/1" to "Mùng 3 Tết",
        "15/1" to "Rằm tháng Giêng",
        "3/3" to "Tết Hàn thực",
        "10/3" to "Giỗ Tổ Hùng Vương",
        "15/4" to "Phật Đản",
        "5/5" to "Tết Đoan Ngọ",
        "15/7" to "Rằm tháng Bảy (Vu Lan)",
        "15/8" to "Tết Trung Thu",
        "9/9" to "Tết Trùng Dương",
        "10/10" to "Tết cơm mới",
        "15/10" to "Rằm tháng Mười",
        "23/12" to "Ông Táo về trời",
        "30/12" to "Tất niên"
    )

    fun getSolarHoliday(dd: Int, mm: Int): String? = SOLAR_HOLIDAYS["$dd/$mm"]

    fun getLunarHoliday(lunarDay: Int, lunarMonth: Int): String? = LUNAR_HOLIDAYS["$lunarDay/$lunarMonth"]
}
