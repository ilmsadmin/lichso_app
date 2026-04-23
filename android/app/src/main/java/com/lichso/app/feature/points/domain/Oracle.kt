package com.lichso.app.feature.points.domain

import kotlin.random.Random

/**
 * Kinh Dịch / xăm oracle model — 64 quẻ simplified set.
 * For v2.0 we hard-code a small curated set (~20 quẻ) and pick deterministically
 * by epochDay so the user sees the same quẻ if they revisit the result screen
 * the same day.
 */
enum class OracleTier(val display: String, val colorHex: Long) {
    THUONG_CAT     ("★ Thượng Thượng Cát", 0xFF2E7D32),
    THUONG_TRUNG   ("★ Thượng Trung Cát", 0xFF388E3C),
    TRUNG_CAT      ("• Trung Cát",         0xFFF9A825),
    TRUNG_BINH     ("• Trung Bình",        0xFFB08D57),
    HA_CAT         ("◯ Hạ Cát",           0xFFEF6C00),
    HUNG           ("✗ Hung",              0xFFC62828),
}

data class OracleQue(
    val index: Int,
    val name: String,
    val han: String,
    val subtitle: String,       // "Thiên Thủy Tụng · Nạp âm Sa Trung Kim"
    val tier: OracleTier,
    val poem: String,           // 4 câu thất ngôn
    val interpretation: String, // Lời giải
    val career: String,         // Công danh
    val wealth: String,         // Tài lộc
    val love: String,           // Tình duyên
    val health: String,         // Sức khỏe
    val direction: String,      // Hướng tốt
    val luckyHour: String,      // Giờ tốt
    val suggestion: String,     // Lời khuyên
)

object OracleDeck {
    private val all = listOf(
        OracleQue(
            1, "Càn Nguyên", "乾元",
            "Thuần Càn · Nạp âm Đại Khê Thủy",
            OracleTier.THUONG_CAT,
            "Rồng bay chín tầng mây biếc,\nVận hội quân tử lên cao.\nĐức dày gánh vác trọng trách,\nCông thành danh toại một mai.",
            "Quẻ này tượng trưng cho người quân tử gặp thời, mọi việc hanh thông. Cần giữ vững đạo chính, không kiêu ngạo thì phúc lộc dài lâu.",
            "Rất tốt — quý nhân đưa đường", "Dồi dào — đầu tư có lãi",
            "Hòa hợp — người trong mộng", "Tốt — khí huyết lưu thông",
            "Đông", "Tỵ (9–11h)",
            "Chủ động đề xuất, mở rộng quan hệ, tránh bảo thủ.",
        ),
        OracleQue(
            8, "Tỉ", "比",
            "Thủy Địa Tỉ · Nạp âm Tuyền Trung Thủy",
            OracleTier.THUONG_TRUNG,
            "Hợp quần giúp đỡ lẫn nhau,\nTrên dưới một lòng thuận thảo.\nTránh đi lẻ bóng dễ sai,\nĐồng đội ắt có tương lai.",
            "Là quẻ của sự liên kết. Nên tìm bạn đồng hành, đối tác tin cậy. Đơn thương độc mã dễ thất bại.",
            "Khá — cộng tác thành công", "Ổn — chia lãi cùng bạn",
            "Tốt — người cùng chí hướng", "Bình thường",
            "Tây Bắc", "Thân (15–17h)",
            "Kêu gọi hỗ trợ từ gia đình, nhóm bạn lâu năm.",
        ),
        OracleQue(
            11, "Thái", "泰",
            "Địa Thiên Thái · Nạp âm Bạch Lạp Kim",
            OracleTier.THUONG_CAT,
            "Trời đất giao hòa thuận lợi,\nÂm dương dung hợp an vui.\nCông việc tự nhiên hanh thông,\nTài danh sớm gặt rỡ ràng.",
            "Quẻ đại cát — thời kỳ thuận lợi hiếm có. Tranh thủ hành động, gieo hạt cho tương lai.",
            "Rất tốt — thăng tiến bất ngờ", "Rất tốt — tiền vào nhiều cửa",
            "Tuyệt vời — hỉ sự ập đến", "Tốt",
            "Nam", "Ngọ (11–13h)",
            "Đừng trì hoãn — cơ hội đang ở ngay trước mắt.",
        ),
        OracleQue(
            14, "Đại Hữu", "大有",
            "Hỏa Thiên Đại Hữu",
            OracleTier.THUONG_CAT,
            "Kho báu đầy nhà phúc lộc,\nĐại hanh đại lợi muôn nơi.\nChớ tham lam mà rước họa,\nSan sẻ thì phúc càng dày.",
            "Phú quý đến nơi, nhưng cần biết chia sẻ. Tích đức mới giữ được lâu dài.",
            "Tốt — cấp trên tin cậy", "Dồi dào — công đức càng lớn",
            "Khá", "Tốt",
            "Đông Nam", "Tị (9–11h)",
            "Bố thí, làm từ thiện một ít — phúc trở lại gấp nhiều.",
        ),
        OracleQue(
            20, "Quan", "觀",
            "Phong Địa Quan",
            OracleTier.TRUNG_CAT,
            "Đứng trên cao nhìn rộng khắp,\nLắng nghe trước khi ra lời.\nTrau dồi đạo đức làm gốc,\nViệc đời tự đến đúng thời.",
            "Thời kỳ quan sát và học hỏi. Chưa phải lúc hành động lớn.",
            "Chờ cơ hội — học thêm kỹ năng", "Trung bình — giữ tiền", 
            "Bình thường — tìm hiểu kỹ", "Ổn",
            "Bắc", "Tí (23–01h)",
            "Tạm gác quyết định quan trọng sang tuần sau.",
        ),
        OracleQue(
            24, "Phục", "復",
            "Địa Lôi Phục",
            OracleTier.TRUNG_CAT,
            "Một dương quay trở về,\nKhởi đầu mới chờ đợi bạn.\nKiên trì đi đúng đường,\nKhó khăn tự nhiên hóa thông.",
            "Quẻ của sự phục hồi. Sau giai đoạn khó, mọi thứ đang tốt dần lên.",
            "Đang đi lên — nhận lại vị trí cũ", "Hồi phục nhẹ",
            "Nối lại duyên xưa", "Đang hồi phục",
            "Đông Bắc", "Dần (3–5h)",
            "Quay lại kế hoạch cũ từng bỏ dở, lần này sẽ thành.",
        ),
        OracleQue(
            30, "Ly", "離",
            "Thuần Ly · Lửa",
            OracleTier.THUONG_TRUNG,
            "Lửa sáng chiếu rọi bốn phương,\nTrí tuệ sáng suốt hơn người.\nNương vào người hiền giữ đức,\nChớ để lòng bốc như lửa thiêu.",
            "Quẻ của ánh sáng, trí tuệ. Thành công đến từ sự minh mẫn.",
            "Tốt — tỏa sáng trong công việc", "Khá — có lợi nhuận trí tuệ",
            "Nồng nhiệt — chú ý bộc phát", "Tránh viêm nhiệt",
            "Nam", "Ngọ (11–13h)",
            "Học một kỹ năng mới, đọc sách cổ — gặp được quý nhân.",
        ),
        OracleQue(
            34, "Đại Tráng", "大壯",
            "Lôi Thiên Đại Tráng",
            OracleTier.TRUNG_CAT,
            "Sức mạnh lớn nhưng khéo dùng,\nTiến lui có chừng có mực.\nCương nhu biết phân biệt rõ,\nViệc khó hóa dễ một khi.",
            "Có sức, có thế. Nhưng hãy tránh dùng lực quá đà mà gãy đổ.",
            "Mạnh mẽ — nhưng đừng ép cấp dưới", "Ổn",
            "Tốt nhưng cần mềm mỏng", "Tốt — chú ý gân cốt",
            "Tây", "Dậu (17–19h)",
            "Mềm mỏng với người thân. Quyết đoán với đối thủ.",
        ),
        OracleQue(
            38, "Vấn Sư", "問師",
            "Thiên Thủy Tụng · Nạp âm Sa Trung Kim",
            OracleTier.THUONG_TRUNG,
            "Hỏi thầy chỉ lối đường đi,\nLòng ngay ý thẳng ắt khi đến bờ.\nViệc làm chớ có phân bì,\nKiên trì chờ đợi vận thì hanh thông.",
            "Quẻ của người đang tìm phương hướng. Hỏi người có kinh nghiệm sẽ bước tiến rõ. Tránh nóng vội, giữ tâm ngay thẳng.",
            "Khá — có quý nhân phù trợ", "Trung bình — đừng vay mượn",
            "Tốt — cơ hội nói thẳng thắn", "Chú ý hô hấp",
            "Đông Nam", "Mùi (13–15h)",
            "Hỏi ý kiến người lớn tuổi trước khi quyết định lớn.",
        ),
        OracleQue(
            42, "Ích", "益",
            "Phong Lôi Ích",
            OracleTier.THUONG_TRUNG,
            "Trên giảm dưới được tăng thêm,\nThuận lòng trời đất dựng xây.\nNhân đức gieo trồng nhiều buổi,\nCủa cải tự khắc đầy nhà.",
            "Quẻ gia tăng lợi ích. Giúp người thì được nhận lại gấp nhiều.",
            "Tốt — đồng nghiệp giúp", "Rất tốt — có nguồn thu mới",
            "Khá — cho đi nhiều hơn", "Ổn",
            "Đông", "Mão (5–7h)",
            "Giúp một người hôm nay — công đức quay lại trong tuần.",
        ),
        OracleQue(
            50, "Đỉnh", "鼎",
            "Hỏa Phong Đỉnh",
            OracleTier.THUONG_TRUNG,
            "Ba chân vạc vững chắc đứng,\nCơ nghiệp xây dựng bền vững.\nTân trang đổi mới hợp thời,\nHanh thông khắp chốn mọi nơi.",
            "Quẻ của sự ổn định và đổi mới đúng lúc. Cơ nghiệp vững chãi.",
            "Tốt — được thăng chức", "Dồi dào — có nguồn chính",
            "Ổn — nên cưới hỏi", "Tốt",
            "Nam", "Tị (9–11h)",
            "Đầu tư vào nền tảng — kết quả lâu dài sẽ rất đẹp.",
        ),
        OracleQue(
            58, "Đoái", "兌",
            "Thuần Đoái · Đầm",
            OracleTier.TRUNG_CAT,
            "Đầm nước phản chiếu trời xanh,\nVui vẻ hòa hợp cùng bạn thân.\nLời nói phải lòng chớ nịnh,\nTâm thành ắt được toại lòng.",
            "Quẻ của niềm vui và giao tiếp. Chú ý lời nói phải chân thành.",
            "Tốt — thương thuyết thuận", "Khá — nhờ ngoại giao",
            "Rất tốt — hỉ sự", "Ổn — chú ý miệng răng",
            "Tây", "Dậu (17–19h)",
            "Tụ họp bạn bè — một cuộc gặp sẽ mở ra cơ hội.",
        ),
        OracleQue(
            61, "Trung Phu", "中孚",
            "Phong Trạch Trung Phu",
            OracleTier.THUONG_TRUNG,
            "Lòng thành cảm cả đất trời,\nViệc khó đến đâu cũng xong.\nChân thật là gốc làm người,\nPhúc dày lộc lớn tự nhiên đến.",
            "Quẻ của lòng thành tín. Giữ chữ tín thì trăm việc đều thành.",
            "Tốt — được tín nhiệm", "Ổn — tránh gian lận",
            "Rất tốt — thành tín cảm động", "Tốt",
            "Tây Nam", "Thìn (7–9h)",
            "Thực hiện đúng lời đã hứa — uy tín sẽ mang lại tất cả.",
        ),
        OracleQue(
            29, "Khảm", "坎",
            "Thuần Khảm · Hiểm",
            OracleTier.TRUNG_BINH,
            "Nước cuộn sâu lòng nguy hiểm,\nGiữ vững lòng tin vượt qua.\nBước đi cẩn thận từng chút,\nHiểm cảnh tự khắc hóa an.",
            "Quẻ cảnh báo có khó khăn. Giữ bình tĩnh, cẩn trọng thì qua được.",
            "Khó — đừng đổi việc lúc này", "Thận trọng — đừng đầu tư mới",
            "Cần kiên nhẫn", "Chú ý thận, bàng quang",
            "Bắc", "Tí (23–01h)",
            "Chậm lại, giữ tiền mặt — tuần sau tình hình rõ hơn.",
        ),
        OracleQue(
            6, "Tụng", "訟",
            "Thiên Thủy Tụng",
            OracleTier.TRUNG_BINH,
            "Tranh cãi chẳng lợi cho ai,\nNhường nhịn một bước rộng lối.\nCầu thầy phân xử công minh,\nViệc xong hai bên đều toại.",
            "Quẻ có tranh chấp. Nên hòa giải, tránh đối đầu trực diện.",
            "Có tranh chấp — nhờ trung gian", "Giữ giấy tờ cẩn thận",
            "Có hiểu lầm — nói rõ", "Chú ý stress",
            "Tây Bắc", "Tuất (19–21h)",
            "Viết rõ hợp đồng, ghi chép cẩn thận — tránh miệng hứa.",
        ),
        OracleQue(
            47, "Khốn", "困",
            "Trạch Thủy Khốn",
            OracleTier.HA_CAT,
            "Cây khô đầm cạn khó hoa,\nKiên trì chờ đợi mưa về.\nQuân tử giữ được chí cao,\nKhó khăn rồi cũng vượt qua.",
            "Quẻ khó khăn. Đừng tuyệt vọng, giữ chí khí thì ngày mai tươi sáng.",
            "Tạm bị bó tay — tìm hướng mới", "Khó — tiết kiệm là chính",
            "Cô đơn tạm thời", "Cần nghỉ ngơi",
            "Tây", "Dậu (17–19h)",
            "Đọc sách, học online — tích lũy cho cơ hội sau.",
        ),
        OracleQue(
            3, "Truân", "屯",
            "Thủy Lôi Truân",
            OracleTier.TRUNG_BINH,
            "Khởi đầu khó như mầm nảy,\nGặp đất cứng cần thêm công.\nNhẫn nại từng ngày một chút,\nMai sau ắt được trổ bông.",
            "Quẻ khởi sự khó. Đừng nản — mọi điều lớn đều bắt đầu khó.",
            "Chậm — cần người chỉ dẫn", "Mới đầu tư cần thời gian",
            "Gian nan ban đầu", "Ổn",
            "Bắc", "Sửu (01–03h)",
            "Tìm một người thầy (mentor) — tiết kiệm nhiều năm mò mẫm.",
        ),
        OracleQue(
            63, "Ký Tế", "既濟",
            "Thủy Hỏa Ký Tế",
            OracleTier.TRUNG_CAT,
            "Việc tạm hoàn thành đúng lúc,\nChớ chủ quan mà sinh loạn.\nGiữ thành quả mới là khó,\nPhòng xa mới được dài lâu.",
            "Quẻ vừa thành công. Chú ý giữ gìn, đừng chủ quan đánh mất.",
            "Vừa đạt mục tiêu — giữ vững", "Ổn — lập quỹ dự phòng",
            "Ổn — tránh lơ là", "Tốt",
            "Nam", "Ngọ (11–13h)",
            "Ghi chép lại bài học — chu kỳ mới đang đến.",
        ),
        OracleQue(
            36, "Minh Di", "明夷",
            "Địa Hỏa Minh Di",
            OracleTier.HUNG,
            "Mặt trời khuất bóng chân trời,\nQuân tử ẩn mình chờ ngày.\nBáo đức rèn mình trong tối,\nMai sáng rồi sẽ bừng oai.",
            "Quẻ ẩn nhẫn. Thời không thuận — giữ mình, đừng khoe khoang.",
            "Tạm lùi một bước", "Giữ chặt hầu bao",
            "Nên giữ khoảng cách tạm", "Chú ý gan mật",
            "Đông Nam", "Tị (9–11h)",
            "Im lặng làm việc — tránh khoe khoang trên mạng xã hội.",
        ),
        OracleQue(
            55, "Phong", "豐",
            "Lôi Hỏa Phong",
            OracleTier.THUONG_TRUNG,
            "Phong phú dồi dào hưng thịnh,\nTrăng tròn rồi ắt sẽ khuyết.\nThời thịnh phải biết phòng suy,\nMới giữ được đỉnh đài vinh quang.",
            "Quẻ thịnh vượng. Đỉnh cao rồi — cần chuẩn bị cho chu kỳ tiếp.",
            "Đang ở đỉnh — chuẩn bị người kế nhiệm", "Rất tốt — đa dạng hóa",
            "Tốt — kỷ niệm sự kiện lớn", "Tốt — đừng ăn nhậu quá",
            "Nam", "Ngọ (11–13h)",
            "Đa dạng nguồn thu — đừng bỏ hết trứng vào một rổ.",
        ),
    )

    fun size(): Int = all.size

    /** Same epochDay → same quẻ (deterministic for a calendar day). */
    fun pickForDay(epochDay: Long, salt: Long = 0L): OracleQue {
        val seed = epochDay * 2654435761L + salt
        val idx = (Random(seed).nextInt() and Int.MAX_VALUE) % all.size
        return all[idx]
    }
}
