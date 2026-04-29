package com.lichso.app.notification

import com.lichso.app.domain.model.DayInfo

/**
 * Sinh "gợi ý theo ngữ cảnh" cho ngày hiện tại dựa trên lunar info.
 * Ưu tiên (chỉ trả 1 hint cho daily notification để không quá dài):
 *  1. Mùng 1, Rằm — nhắc sửa lễ thắp hương
 *  2. Đêm trước Mùng 1 / Rằm (29-30 / 14)
 *  3. Ngày Tam nương / Nguyệt kỵ — nhắc tránh xuất hành quan trọng
 *  4. Ông Công ông Táo (23 tháng Chạp âm)
 *  5. Tết Nguyên Đán (mùng 1 tháng 1 âm)
 *  6. Lễ âm có sẵn từ dayInfo.lunarHoliday
 */
object SmartReminderProvider {

    fun contextualHintFor(dayInfo: DayInfo): String? {
        val lunarDay = dayInfo.lunar.day
        val lunarMonth = dayInfo.lunar.month

        // Tết
        if (lunarMonth == 1 && lunarDay == 1) {
            return "Chúc mừng năm mới — sáng mùng 1 nhớ thắp hương gia tiên đầu năm."
        }
        // Ông Công ông Táo
        if (lunarMonth == 12 && lunarDay == 23) {
            return "Hôm nay 23 tháng Chạp — tiễn ông Công ông Táo về trời, chuẩn bị mâm cá chép."
        }
        // Rằm tháng Bảy (Vu Lan) — special prompt
        if (lunarMonth == 7 && lunarDay == 15) {
            return "Rằm tháng Bảy — Vu Lan báo hiếu, chuẩn bị lễ chay cúng cô hồn."
        }
        // Mùng 1
        if (dayInfo.isMung1) {
            return "Mùng 1 đầu tháng — sáng nay nhớ thắp hương gia tiên cầu bình an."
        }
        // Rằm
        if (dayInfo.isRam) {
            return "Hôm nay là Rằm — chiều 5h thắp hương lễ Phật, đặt hoa quả ban thờ."
        }
        // Đêm trước Mùng 1
        if (lunarDay == 30 || lunarDay == 29) {
            return "Mai là Mùng 1 — chuẩn bị hoa, trà, oản trước cho buổi sáng đầu tháng."
        }
        // Đêm trước Rằm
        if (lunarDay == 14) {
            return "Mai là ngày Rằm — sửa soạn mâm cúng, hoa quả tươi cho buổi chiều."
        }
        // Tam nương
        if (dayInfo.activities.isTamNuong) {
            return "Hôm nay Tam Nương — tránh khởi sự lớn (cưới hỏi, khai trương, xuất hành xa)."
        }
        // Nguyệt kỵ
        if (dayInfo.activities.isNguyetKy) {
            return "Hôm nay Nguyệt Kỵ — nên thư thái, lùi việc trọng đại sang ngày khác."
        }
        // Lunar holiday
        dayInfo.lunarHoliday?.let { hol ->
            return "Lễ âm hôm nay: $hol — tham khảo bài văn khấn phù hợp trong app."
        }
        return null
    }
}
