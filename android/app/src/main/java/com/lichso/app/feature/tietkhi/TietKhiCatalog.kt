package com.lichso.app.feature.tietkhi

/**
 * Mô tả 24 tiết khí: ý nghĩa, hoạt động phong thuỷ phù hợp, kiêng kỵ.
 * Dùng trong [TietKhiScreen] để hiển thị chi tiết khi user click vào 1 tiết khí.
 */
data class TietKhiDetail(
    val name: String,
    val pinyin: String,
    val month: Int,                 // tháng dương lịch xấp xỉ tiết khí bắt đầu
    val approxDay: Int,             // ngày dương lịch xấp xỉ tiết khí bắt đầu
    val season: Season,
    val meaning: String,            // mô tả ngắn về khí hậu / nông nghiệp
    val recommended: List<String>,  // việc nên làm (phong thuỷ + sức khoẻ)
    val avoid: List<String>,        // việc kiêng kỵ
    val seasonalFood: String,       // món ăn theo mùa (Đông y)
)

enum class Season(val displayName: String, val emoji: String) {
    SPRING("Xuân", "🌸"),
    SUMMER("Hạ", "☀️"),
    AUTUMN("Thu", "🍂"),
    WINTER("Đông", "❄️"),
}

object TietKhiCatalog {

    val all: List<TietKhiDetail> = listOf(
        TietKhiDetail(
            name = "Lập Xuân", pinyin = "Lìchūn", month = 2, approxDay = 4, season = Season.SPRING,
            meaning = "Bắt đầu mùa xuân — vạn vật bừng tỉnh sau giấc ngủ đông. Khí dương bắt đầu sinh.",
            recommended = listOf("Khai trương — đầu tư việc mới", "Trồng cây, gieo hạt", "Tập thể dục buổi sáng", "Mặc đồ ấm phần thân dưới"),
            avoid = listOf("Ăn quá nhiều đồ chua", "Để cơ thể nhiễm lạnh đột ngột", "Cắt tóc quá ngắn"),
            seasonalFood = "Hành lá, giá đỗ, măng, cải xanh"
        ),
        TietKhiDetail(
            name = "Vũ Thủy", pinyin = "Yǔshuǐ", month = 2, approxDay = 19, season = Season.SPRING,
            meaning = "Mưa xuân bắt đầu rơi — tuyết tan, nước về. Đất đai ẩm, thuận lợi gieo trồng.",
            recommended = listOf("Bón phân ruộng vườn", "Bổ tỳ vị bằng cháo gạo", "Mang ô khi ra ngoài"),
            avoid = listOf("Ăn lạnh, nước đá", "Để chân ướt lâu"),
            seasonalFood = "Cháo gạo lứt, hạt sen, đậu đỏ"
        ),
        TietKhiDetail(
            name = "Kinh Trập", pinyin = "Jīngzhé", month = 3, approxDay = 6, season = Season.SPRING,
            meaning = "Sấm đầu mùa — sâu trùng ngủ đông thức giấc. Vạn vật chuyển động.",
            recommended = listOf("Dọn dẹp nhà cửa, xua tà khí", "Đi bộ ngoài thiên nhiên", "Bắt sâu, làm cỏ vườn"),
            avoid = listOf("Ngủ nướng quá 7 giờ", "Cãi vã, nóng giận"),
            seasonalFood = "Tỏi, gừng, lê, mật ong"
        ),
        TietKhiDetail(
            name = "Xuân Phân", pinyin = "Chūnfēn", month = 3, approxDay = 21, season = Season.SPRING,
            meaning = "Ngày dài bằng đêm — âm dương cân bằng. Là điểm khởi đầu của xuân giữa.",
            recommended = listOf("Cúng tổ tiên", "Cân bằng làm việc & nghỉ ngơi", "Trồng cây cảnh trong nhà"),
            avoid = listOf("Thức quá khuya", "Ăn uống thất thường"),
            seasonalFood = "Rau cải, dâu tây, cà chua"
        ),
        TietKhiDetail(
            name = "Thanh Minh", pinyin = "Qīngmíng", month = 4, approxDay = 5, season = Season.SPRING,
            meaning = "Trời quang đãng, sáng sủa. Tiết tảo mộ — tưởng nhớ tổ tiên.",
            recommended = listOf("Tảo mộ, sửa sang phần mộ", "Du xuân, dã ngoại", "Trồng hoa cúc, hoa mai"),
            avoid = listOf("Cãi nhau ở mộ phần", "Ăn đồ quá béo"),
            seasonalFood = "Bánh trôi, bánh chay, rau má"
        ),
        TietKhiDetail(
            name = "Cốc Vũ", pinyin = "Gǔyǔ", month = 4, approxDay = 20, season = Season.SPRING,
            meaning = "Mưa hạt thóc — mưa nhiều thuận lợi cho hạt giống nảy mầm.",
            recommended = listOf("Cấy lúa mùa", "Uống trà xanh đầu vụ", "Vệ sinh ẩm mốc"),
            avoid = listOf("Ăn quá nhiều thịt nóng", "Ở phòng kín, ẩm thấp"),
            seasonalFood = "Trà mạn, măng tây, cá chép"
        ),

        TietKhiDetail(
            name = "Lập Hạ", pinyin = "Lìxià", month = 5, approxDay = 5, season = Season.SUMMER,
            meaning = "Bắt đầu mùa hè — nhiệt độ tăng nhanh. Khí dương cực thịnh, dưỡng tâm là chính.",
            recommended = listOf("Dậy sớm, ngủ trễ một chút", "Uống nhiều nước", "Ăn nhạt, ít dầu mỡ"),
            avoid = listOf("Tập thể dục lúc nắng gắt", "Ăn quá lạnh đột ngột", "Tức giận"),
            seasonalFood = "Đậu xanh, dưa hấu, bí đao"
        ),
        TietKhiDetail(
            name = "Tiểu Mãn", pinyin = "Xiǎomǎn", month = 5, approxDay = 21, season = Season.SUMMER,
            meaning = "Hạt thóc đầy mẩy nhưng chưa chín hẳn. Trời mưa nhiều, ẩm độ cao.",
            recommended = listOf("Chống ẩm, phơi quần áo", "Ăn món thanh nhiệt", "Tránh đứng gió lùa"),
            avoid = listOf("Ăn đồ chiên rán", "Ngồi lâu trong phòng lạnh"),
            seasonalFood = "Mướp đắng, rau dền, cá rô"
        ),
        TietKhiDetail(
            name = "Mang Chủng", pinyin = "Mángzhòng", month = 6, approxDay = 6, season = Season.SUMMER,
            meaning = "Mùa gặt lúa chiêm — cấy lúa mùa. Bận rộn nhất trong năm với nhà nông.",
            recommended = listOf("Tăng đạm thực vật", "Tắm nước ấm buổi tối", "Ngủ trưa 30 phút"),
            avoid = listOf("Lao động quá sức giữa trưa", "Uống bia rượu nhiều"),
            seasonalFood = "Mơ ngâm, vải thiều, khổ qua"
        ),
        TietKhiDetail(
            name = "Hạ Chí", pinyin = "Xiàzhì", month = 6, approxDay = 21, season = Season.SUMMER,
            meaning = "Ngày dài nhất trong năm — dương cực thịnh. Là điểm chuyển âm dương.",
            recommended = listOf("Cúng tổ tiên (Đoan Ngọ)", "Hái thuốc nam", "Ăn món thanh nhiệt"),
            avoid = listOf("Phơi nắng giữa trưa", "Ăn quá no"),
            seasonalFood = "Cơm rượu nếp, mận, vải, chè đỗ đen"
        ),
        TietKhiDetail(
            name = "Tiểu Thử", pinyin = "Xiǎoshǔ", month = 7, approxDay = 7, season = Season.SUMMER,
            meaning = "Bắt đầu nóng nhẹ — chưa cực điểm. Cần dưỡng tâm, tránh nóng giận.",
            recommended = listOf("Ngủ trưa", "Uống trà sen, trà cúc", "Đi bơi"),
            avoid = listOf("Ăn cay nhiều", "Để bàn chân lạnh"),
            seasonalFood = "Sen, mướp, dưa leo"
        ),
        TietKhiDetail(
            name = "Đại Thử", pinyin = "Dàshǔ", month = 7, approxDay = 23, season = Season.SUMMER,
            meaning = "Nóng cực đỉnh — \"tam phục\" khắc nghiệt nhất. Đề phòng say nắng.",
            recommended = listOf("Ở nơi mát mẻ buổi trưa", "Tăng cường rau xanh", "Tắm nước mát (không đá)"),
            avoid = listOf("Ra ngoài 11–14h", "Tập thể thao quá mức", "Ăn đá lạnh khi mới đi nắng về"),
            seasonalFood = "Đậu xanh, sương sáo, rau câu, dừa"
        ),

        TietKhiDetail(
            name = "Lập Thu", pinyin = "Lìqiū", month = 8, approxDay = 7, season = Season.AUTUMN,
            meaning = "Bắt đầu mùa thu — khí dương suy, khí âm sinh. Thời tiết bắt đầu hanh khô.",
            recommended = listOf("Bổ phổi bằng món hấp", "Đi bộ chiều mát", "Uống nước ép lê"),
            avoid = listOf("Ăn quá nhiều đồ cay", "Để da khô nứt nẻ"),
            seasonalFood = "Lê, củ sen, bí đỏ, hạnh nhân"
        ),
        TietKhiDetail(
            name = "Xử Thử", pinyin = "Chǔshǔ", month = 8, approxDay = 23, season = Season.AUTUMN,
            meaning = "Nóng đã rút lui — sáng mát, trưa còn nóng. Chuyển giao rõ rệt.",
            recommended = listOf("Tăng giờ ngủ buổi tối", "Mặc áo khoác mỏng sáng/tối", "Bổ âm bằng cháo loãng"),
            avoid = listOf("Bật quạt thẳng vào người khi ngủ", "Ăn dưa hấu quá nhiều"),
            seasonalFood = "Cháo bí đỏ, mật ong, nấm"
        ),
        TietKhiDetail(
            name = "Bạch Lộ", pinyin = "Báilù", month = 9, approxDay = 8, season = Season.AUTUMN,
            meaning = "Sương trắng đầu mùa — khí trời lạnh hơn vào ban đêm.",
            recommended = listOf("Đắp chăn mỏng khi ngủ", "Uống trà hoa cúc", "Dưỡng phổi bằng món hấp"),
            avoid = listOf("Đi chân đất buổi sáng sớm", "Ăn món sống lạnh"),
            seasonalFood = "Long nhãn, hạt sen, gà ác hầm"
        ),
        TietKhiDetail(
            name = "Thu Phân", pinyin = "Qiūfēn", month = 9, approxDay = 23, season = Season.AUTUMN,
            meaning = "Ngày bằng đêm lần thứ 2 — âm dương cân bằng. Tiết Trung thu cận kề.",
            recommended = listOf("Sum họp gia đình (Trung thu)", "Dưỡng âm bổ phổi", "Đi bộ chậm dưới trăng"),
            avoid = listOf("Buồn rầu, suy nghĩ quá nhiều", "Thức quá khuya"),
            seasonalFood = "Bánh trung thu, hồng đỏ, lựu, nho"
        ),
        TietKhiDetail(
            name = "Hàn Lộ", pinyin = "Hánlù", month = 10, approxDay = 8, season = Season.AUTUMN,
            meaning = "Sương lạnh — khí trời rõ rệt mát lạnh. Cây cối bắt đầu chuyển vàng.",
            recommended = listOf("Mang tất khi ngủ", "Uống nước ấm sáng sớm", "Ngâm chân nước gừng"),
            avoid = listOf("Để vai gáy bị lạnh", "Ăn cua sống"),
            seasonalFood = "Khoai lang, cua, hạt dẻ, táo đỏ"
        ),
        TietKhiDetail(
            name = "Sương Giáng", pinyin = "Shuāngjiàng", month = 10, approxDay = 23, season = Season.AUTUMN,
            meaning = "Sương sa thành sương muối — báo hiệu mùa đông cận kề.",
            recommended = listOf("Bổ thận bằng món hầm", "Mặc áo che ấm bụng", "Tập khí công"),
            avoid = listOf("Tập thể dục sớm khi sương dày", "Ăn quá nhiều đồ chua"),
            seasonalFood = "Hồng giòn, gừng, củ cải trắng"
        ),

        TietKhiDetail(
            name = "Lập Đông", pinyin = "Lìdōng", month = 11, approxDay = 7, season = Season.WINTER,
            meaning = "Bắt đầu mùa đông — vạn vật ẩn náu, dưỡng tinh khí. Khí âm cực thịnh sinh.",
            recommended = listOf("Bổ thận tinh", "Ăn món hầm ấm", "Đi ngủ sớm 22h", "Tắm nước ấm"),
            avoid = listOf("Tập thể dục quá sớm khi trời tối", "Ra mồ hôi rồi gặp gió"),
            seasonalFood = "Thịt cừu, gừng, quế, đông trùng hạ thảo"
        ),
        TietKhiDetail(
            name = "Tiểu Tuyết", pinyin = "Xiǎoxuě", month = 11, approxDay = 22, season = Season.WINTER,
            meaning = "Tuyết nhỏ rơi (ở miền Bắc TQ) — VN: rét đậm bắt đầu. Khí trời u ám.",
            recommended = listOf("Tăng cường nội tiết", "Uống trà gừng mật ong", "Mặc đồ giữ ấm cổ"),
            avoid = listOf("Ăn quá nhiều đồ ngọt", "Ngồi lì trong phòng kín"),
            seasonalFood = "Gừng, hành, tỏi, mật ong, hạt óc chó"
        ),
        TietKhiDetail(
            name = "Đại Tuyết", pinyin = "Dàxuě", month = 12, approxDay = 7, season = Season.WINTER,
            meaning = "Tuyết lớn — rét đậm rét hại. Cần dưỡng dương khí trong người.",
            recommended = listOf("Phơi nắng buổi trưa", "Ăn cháo nóng", "Sưởi ấm tay chân"),
            avoid = listOf("Tắm nước lạnh", "Để đầu ướt khi ra ngoài"),
            seasonalFood = "Cháo bát bảo, hạt dẻ, khoai môn"
        ),
        TietKhiDetail(
            name = "Đông Chí", pinyin = "Dōngzhì", month = 12, approxDay = 22, season = Season.WINTER,
            meaning = "Đêm dài nhất trong năm — âm cực thịnh, dương khí bắt đầu sinh trở lại.",
            recommended = listOf("Cúng gia tiên cuối năm", "Sum họp gia đình", "Bổ tinh dưỡng huyết", "Ăn bánh trôi/bánh chay"),
            avoid = listOf("Ra ngoài lúc nửa đêm", "Suy nghĩ tiêu cực"),
            seasonalFood = "Bánh trôi, gà ác hầm thuốc bắc, đậu đỏ"
        ),
        TietKhiDetail(
            name = "Tiểu Hàn", pinyin = "Xiǎohán", month = 1, approxDay = 6, season = Season.WINTER,
            meaning = "Lạnh nhỏ — báo hiệu giai đoạn lạnh nhất sắp đến.",
            recommended = listOf("Ngâm chân nước thuốc", "Uống canh xương", "Mặc thêm áo khoác lông"),
            avoid = listOf("Ra ngoài khi gió mạnh", "Ăn rau sống"),
            seasonalFood = "Xương hầm, cải bó xôi, hành tây"
        ),
        TietKhiDetail(
            name = "Đại Hàn", pinyin = "Dàhán", month = 1, approxDay = 20, season = Season.WINTER,
            meaning = "Lạnh cực điểm — chuẩn bị đón Tết. Là tiết khí cuối cùng trong vòng 24 tiết.",
            recommended = listOf("Dọn dẹp nhà chuẩn bị Tết", "Mua sắm Tất niên", "Tổng kết 1 năm", "Bổ thận bằng món hầm"),
            avoid = listOf("Tranh cãi cuối năm", "Cho vay tiền cận Tết (theo dân gian)"),
            seasonalFood = "Bánh chưng (cận Tết), giò chả, dưa hành"
        ),
    )

    fun byName(name: String): TietKhiDetail? = all.firstOrNull { it.name == name }

    fun bySeason(season: Season): List<TietKhiDetail> = all.filter { it.season == season }
}
