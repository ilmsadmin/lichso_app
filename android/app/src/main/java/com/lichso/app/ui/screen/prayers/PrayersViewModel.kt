package com.lichso.app.ui.screen.prayers

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ══════════════════════════════════════════
// Data Models
// ══════════════════════════════════════════

data class PrayerCategory(
    val id: String,
    val emoji: String,
    val label: String
)

data class PrayerItem(
    val id: Int,
    val emoji: String,
    val emojiStyle: String, // maps to color style
    val name: String,
    val description: String,
    val categoryId: String,
    val tags: List<PrayerTag>,
    val isPopular: Boolean = false,
    val content: String, // full prayer text (HTML-like with emphasis markers)
    val note: String = ""
)

data class PrayerTag(
    val label: String,
    val type: PrayerTagType = PrayerTagType.NORMAL
)

enum class PrayerTagType { NORMAL, HOT, NEW }

data class PrayersUiState(
    val selectedCategoryId: String = "all",
    val searchQuery: String = "",
    val selectedPrayer: PrayerItem? = null,
    val showDetail: Boolean = false
)

// ══════════════════════════════════════════
// ViewModel
// ══════════════════════════════════════════

@HiltViewModel
class PrayersViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PrayersUiState())
    val uiState: StateFlow<PrayersUiState> = _uiState.asStateFlow()

    val categories = listOf(
        PrayerCategory("all", "📋", "Tất cả"),
        PrayerCategory("gio", "🕯️", "Cúng giỗ"),
        PrayerCategory("ram", "🌕", "Rằm & Mùng 1"),
        PrayerCategory("tet", "🏮", "Tết"),
        PrayerCategory("nhap", "🏠", "Nhập trạch"),
        PrayerCategory("khai", "🏪", "Khai trương"),
        PrayerCategory("chua", "⛩️", "Đi chùa"),
        PrayerCategory("xe", "🚗", "Xe mới"),
    )

    val featuredPrayer = PrayerItem(
        id = 0,
        emoji = "🙏",
        emojiStyle = "gio",
        name = "Văn khấn Rằm tháng Bảy",
        description = "Bài cúng Vu Lan — Xá tội vong nhân, báo hiếu cha mẹ, cúng chúng sinh",
        categoryId = "ram",
        tags = listOf(PrayerTag("🔥 Phổ biến", PrayerTagType.HOT)),
        isPopular = true,
        content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Địa Tạng Vương Bồ Tát, Đức Mục Kiền Liên Tôn Giả.

Hôm nay là ngày Rằm tháng Bảy, nhằm tiết Vu Lan, ngày xá tội vong nhân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Thành tâm sắm sửa hương hoa, lễ vật, trà quả và các thứ cúng dâng, bày lên trước án.

Chúng con thành tâm kính mời các vong linh, cô hồn không nơi nương tựa, không ai thờ cúng.

Cúi xin về đây thụ hưởng lễ vật, phù hộ cho gia đình chúng con bình an, mạnh khỏe.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
        note = "• Lễ cúng Rằm tháng 7 gồm: cúng Phật, cúng Gia tiên, cúng Chúng sinh\n• Cúng chúng sinh nên đặt ngoài trời hoặc trước cửa nhà\n• Thời gian cúng tốt nhất: chiều tối ngày 14 hoặc 15 tháng 7 Âm lịch"
    )

    val allPrayers = listOf(
        // ── Cúng giỗ ──
        PrayerItem(
            id = 1, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng giỗ (ngày giỗ thường)",
            description = "Bài khấn cúng giỗ Ông Bà, Cha Mẹ, người thân đã khuất vào ngày giỗ hàng năm",
            categoryId = "gio",
            tags = listOf(PrayerTag("🔥 Phổ biến", PrayerTagType.HOT), PrayerTag("Giỗ")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ, chư vị Tôn Thần.

Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.

Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, Thúc Bá Huynh Đệ, Cô Di Tỷ Muội họ nội họ ngoại.

Tín chủ (chúng) con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch)
Nhằm ngày giỗ của …(quan hệ: Ông/Bà/Cha/Mẹ…)…
Hiệu là: …(tên người mất)…

Chúng con cùng toàn thể gia quyến, thành tâm sắm lễ, hương hoa trà quả, thắp nén tâm hương dâng trước án.

Chúng con kính mời: …(tên người mất)…
Đồng kính mời Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Cúi xin thương xót con cháu, linh thiêng hiện về, chứng giám lòng thành, thụ hưởng lễ vật.

Phù hộ độ trì cho toàn gia an khang thịnh vượng, già trẻ bình an, mọi việc hanh thông, vạn sự tốt lành.

Chúng con lễ bạc tâm thành, trước án kính lễ, cúi xin được phù hộ độ trì.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ vật gồm: hương, hoa, nước, quả, trầu cau, rượu, đèn nến\n• Cúng giỗ nên làm trước ngày giỗ chính 1 ngày (ngày Tiên thường)\n• Thay phần in đậm bằng thông tin thực tế\n• Đọc bài khấn với lòng thành kính, trang nghiêm"
        ),
        PrayerItem(
            id = 2, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng 49 ngày",
            description = "Bài khấn cúng 49 ngày cho người mới mất, cầu siêu thoát cho vong linh",
            categoryId = "gio",
            tags = listOf(PrayerTag("Tang lễ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Địa Tạng Vương Bồ Tát.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … nhằm ngày chung thất (49 ngày) của …(quan hệ)… hiệu là …(tên người mất)…

Chúng con thành tâm sắm lễ, hương hoa trà quả dâng cúng, kính mời hương linh …(tên)… về thụ hưởng.

Cúi xin Chư Phật, Chư vị Bồ Tát từ bi phù hộ, dẫn dắt hương linh sớm được siêu thoát, vãng sinh Cực Lạc.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Nên mời nhà sư hoặc thầy cúng đến làm lễ\n• Cúng trước bàn thờ vong, lễ vật đầy đủ\n• 49 ngày là mốc quan trọng theo Phật giáo"
        ),
        PrayerItem(
            id = 3, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng 100 ngày",
            description = "Bài khấn lễ tốt khốc (100 ngày) cho người mới mất",
            categoryId = "gio",
            tags = listOf(PrayerTag("Tang lễ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … nhằm ngày tốt khốc (100 ngày) của …(quan hệ)… hiệu là …(tên người mất)…

Chúng con thành tâm kính lễ, dâng hương hoa lễ vật, cầu xin Chư Phật tiếp dẫn hương linh.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ 100 ngày còn gọi là lễ Tốt khốc (thôi khóc)\n• Sau lễ này, tang gia bớt kiêng kỵ hơn"
        ),

        // ── Rằm & Mùng 1 ──
        PrayerItem(
            id = 4, emoji = "🌕", emojiStyle = "ram",
            name = "Văn khấn cúng Rằm hàng tháng",
            description = "Bài khấn ngày rằm (15 Âm lịch) tại gia, cúng Phật, Thần Tài, Thổ Công",
            categoryId = "ram",
            tags = listOf(PrayerTag("🔥 Phổ biến", PrayerTagType.HOT), PrayerTag("Rằm")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Hoàng Thiên Hậu Thổ chư vị Tôn thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày Rằm, tháng … năm … (Âm lịch).

Tín chủ con thành tâm sắm lễ, hương hoa trà quả, thắp nén tâm hương dâng lên trước án.

Kính mời các ngài giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Cúi xin phù hộ độ trì cho gia đình chúng con an khang, mạnh khỏe, vạn sự như ý.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chuẩn bị: hương, hoa tươi, quả, nước sạch, đèn/nến\n• Cúng vào buổi sáng hoặc chiều, tránh cúng tối muộn\n• Ăn chay ngày Rằm là tốt"
        ),
        PrayerItem(
            id = 5, emoji = "🌑", emojiStyle = "ram",
            name = "Văn khấn cúng Mùng 1 hàng tháng",
            description = "Bài khấn ngày mùng 1 (1 Âm lịch) tại gia — sóc vọng đầu tháng",
            categoryId = "ram",
            tags = listOf(PrayerTag("Mùng 1")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 1, tháng … năm … (Âm lịch), ngày sóc đầu tháng.

Tín chủ con thành tâm sắm lễ, dâng lên trước án, cúi xin các ngài chứng giám, phù hộ gia đình bình an, thuận lợi cả tháng.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Mùng 1 cúng đơn giản hơn ngày Rằm\n• Lễ vật: hương, hoa, quả, nước"
        ),

        // ── Tết ──
        PrayerItem(
            id = 6, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn cúng Giao Thừa (ngoài trời)",
            description = "Bài khấn đêm Giao Thừa ngoài trời — tiễn Ông Hành Khiển cũ, đón Ông Hành Khiển mới",
            categoryId = "tet",
            tags = listOf(PrayerTag("🔥 Phổ biến", PrayerTagType.HOT), PrayerTag("Tết")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Đức Đương Lai Hạ Sinh Di Lặc Tôn Phật.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Nay là phút Giao Thừa năm … chuyển sang năm …

Chúng con thành tâm, sửa biện hương hoa lễ vật, kính cáo Thiên Địa Tôn Thần.

Kính tiễn Ông Hành Khiển cũ cùng chư vị Phán Quan.
Kính đón Ông Hành Khiển mới cùng chư vị Tôn thần năm mới.

Cúi xin phù hộ cho toàn gia an khang, thịnh vượng, vạn sự tốt lành.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng ngoài trời, trước cửa nhà\n• Thời gian: đúng phút Giao Thừa (23:00 – 0:00)\n• Lễ vật: mâm cỗ mặn/chay, hương, hoa, đèn, vàng mã"
        ),
        PrayerItem(
            id = 7, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn cúng Tất Niên (30 Tết)",
            description = "Bài khấn chiều 30 Tết, mời ông bà tổ tiên về ăn Tết cùng con cháu",
            categoryId = "tet",
            tags = listOf(PrayerTag("Tết")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày 30 tháng Chạp, ngày cuối năm.

Chúng con kính mời Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, các vị Hương linh Gia tiên nội ngoại về sum họp với con cháu, ăn Tết cùng gia đình.

Chúng con thành tâm kính lễ, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng vào chiều hoặc tối ngày 30 Tết\n• Mâm cỗ đầy đủ, có thể mặn hoặc chay\n• Mời ông bà gia tiên về ăn Tết"
        ),
        PrayerItem(
            id = 8, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn Mùng 1 Tết",
            description = "Bài khấn sáng mùng 1 Tết Nguyên Đán — nguyện cầu năm mới bình an",
            categoryId = "tet",
            tags = listOf(PrayerTag("Tết")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 1 Tết, năm …

Chúng con thành tâm dâng lễ đầu năm mới, kính cáo Thiên Địa, kính lạy Gia tiên.

Cầu xin năm mới bình an, sức khỏe dồi dào, tài lộc hanh thông, vạn sự như ý.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng buổi sáng sớm mùng 1\n• Trang phục chỉnh tề, lịch sự\n• Lòng thành kính, hoan hỷ đón năm mới"
        ),

        // ── Nhà cửa & Công việc ──
        PrayerItem(
            id = 9, emoji = "🏠", emojiStyle = "nhap",
            name = "Văn khấn nhập trạch (về nhà mới)",
            description = "Bài khấn khi dọn về nhà mới, xin phép Thổ Công, Thổ Địa cho gia đình vào ở",
            categoryId = "nhap",
            tags = listOf(PrayerTag("✨ Mới", PrayerTagType.NEW), PrayerTag("Nhà mới")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.

Tín chủ con là: …(họ tên)…

Hôm nay là ngày … tháng … năm …, chúng con dọn đến nhà mới tại: …(địa chỉ mới)…

Chúng con thành tâm sắm lễ, xin phép các ngài cho gia đình chúng con được nhập trạch, an cư lạc nghiệp.

Cúi xin các ngài phù hộ cho gia đình bình an, hòa thuận, làm ăn phát đạt.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chuẩn bị: bếp lửa hoặc bếp ga nhỏ mang vào đầu tiên\n• Chọn ngày tốt, giờ hoàng đạo\n• Vào nhà mới nên mang theo gạo, muối, nước"
        ),
        PrayerItem(
            id = 10, emoji = "🏪", emojiStyle = "khai",
            name = "Văn khấn khai trương",
            description = "Bài khấn khai trương cửa hàng, công ty, đầu năm mới — cầu tài lộc hanh thông",
            categoryId = "khai",
            tags = listOf(PrayerTag("Kinh doanh")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Ông Thần Tài, Ông Thổ Địa.

Tín chủ con là: …(họ tên)…
Cửa hàng/Công ty: …(tên)…
Tại địa chỉ: …(địa chỉ)…

Hôm nay ngày … tháng … năm …, chúng con khai trương cửa hàng.

Thành tâm dâng lễ, kính xin các ngài phù hộ cho việc kinh doanh thuận buồm xuôi gió, tài lộc dồi dào, khách hàng đông đúc.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo để khai trương\n• Lễ vật: hoa quả, hương, nến, mâm cúng\n• Hướng bàn thờ Thần Tài theo hướng tốt của năm"
        ),
        PrayerItem(
            id = 11, emoji = "🚗", emojiStyle = "xe",
            name = "Văn khấn cúng xe mới",
            description = "Bài khấn khi mua xe ô tô / xe máy mới — cầu đi đường bình an, thuận lợi",
            categoryId = "xe",
            tags = listOf(PrayerTag("Xe cộ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay ngày … tháng … năm …, con vừa tậu xe mới.

Con thành tâm sắm lễ cúng xe, xin Chư Phật và các vị Tôn Thần phù hộ cho con đi đường bình an, tránh mọi tai ương, thuận lợi mọi chuyến đi.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chọn ngày tốt để nhận xe và cúng xe\n• Cúng trước xe, dùng hương, hoa quả\n• Có thể rải gạo muối quanh xe"
        ),

        // ── Đi chùa ──
        PrayerItem(
            id = 12, emoji = "⛩️", emojiStyle = "chua",
            name = "Văn khấn ban Tam Bảo (chùa)",
            description = "Bài khấn khi đi chùa lễ Phật — khấn trước ban Tam Bảo chính điện",
            categoryId = "chua",
            tags = listOf(PrayerTag("🔥 Phổ biến", PrayerTagType.HOT), PrayerTag("Đi chùa")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy Đức Phật Thích Ca Mâu Ni.
Con lạy Đức Phật A Di Đà.
Con lạy Đức Quan Thế Âm Bồ Tát.
Con lạy Đức Đại Thế Chí Bồ Tát.
Con lạy mười phương chư Phật, chư Đại Bồ Tát, chư Hiền Thánh Tăng.

Đệ tử con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm …, con đến chùa …(tên chùa)…

Thành tâm kính lễ trước ban Tam Bảo, nguyện cầu Chư Phật từ bi gia hộ cho con cùng gia quyến thân tâm an lạc, nghiệp chướng tiêu trừ, bồ đề tâm tăng trưởng.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn trước ban thờ chính (Tam Bảo) đầu tiên\n• Trang phục kín đáo, lịch sự khi vào chùa\n• Không nên xin tài lộc tại ban Tam Bảo, chỉ xin bình an, trí tuệ"
        ),
        PrayerItem(
            id = 13, emoji = "🙏", emojiStyle = "chua",
            name = "Văn khấn ban Đức Ông",
            description = "Bài khấn trước ban Đức Ông (hộ pháp) khi đi chùa",
            categoryId = "chua",
            tags = listOf(PrayerTag("Đi chùa")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Ông Trưởng Giả, ngài là đệ tử Phật, hộ trì Tam Bảo.

Đệ tử con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay con đến chùa lễ Phật, thành tâm kính lễ trước ban Đức Ông, xin ngài chứng giám lòng thành.

Cúi xin ngài gia hộ cho con và gia đình bình an, mọi sự hanh thông.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn ban Đức Ông sau khi khấn ban Tam Bảo\n• Có thể đặt lễ mặn tại ban Đức Ông (khác với ban Tam Bảo chỉ đặt chay)"
        ),
        PrayerItem(
            id = 14, emoji = "⛩️", emojiStyle = "than",
            name = "Văn khấn Thần Tài — Thổ Địa",
            description = "Bài khấn cúng Ông Thần Tài, Ông Thổ Địa tại nhà hoặc nơi kinh doanh",
            categoryId = "chua",
            tags = listOf(PrayerTag("Tài lộc")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Ông Thần Tài.
Con kính lạy Ông Thổ Địa.

Tín chủ con là: …(họ tên)…
Ngụ tại / Kinh doanh tại: …(địa chỉ)…

Hôm nay ngày … tháng … năm …

Con thành tâm sắm sửa hương hoa lễ vật, dâng lên trước ban thờ Thần Tài, Thổ Địa.

Kính xin các ngài phù hộ cho con tài lộc hanh thông, buôn may bán đắt, công việc thuận lợi, gia đạo bình an.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng Thần Tài vào ngày mùng 1, ngày Rằm, hoặc mùng 10 hàng tháng\n• Lễ vật: hoa tươi, quả, nước, hương, nến\n• Ban thờ Thần Tài đặt dưới đất, hướng ra cửa"
        ),
    )

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openPrayerDetail(prayer: PrayerItem) {
        _uiState.update { it.copy(selectedPrayer = prayer, showDetail = true) }
    }

    fun openPrayerById(id: Int) {
        val prayer = allPrayers.find { it.id == id } ?: return
        openPrayerDetail(prayer)
    }

    fun closeDetail() {
        _uiState.update { it.copy(showDetail = false) }
    }

    fun getFilteredPrayers(): List<PrayerItem> {
        val state = _uiState.value
        var list = allPrayers

        if (state.selectedCategoryId != "all") {
            list = list.filter { it.categoryId == state.selectedCategoryId }
        }

        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
            }
        }

        return list
    }

    /** Group prayers by categoryId for section display */
    fun getGroupedPrayers(): Map<String, List<PrayerItem>> {
        return getFilteredPrayers().groupBy { it.categoryId }
    }

    fun getCategoryLabel(categoryId: String): Pair<String, String> = when (categoryId) {
        "gio" -> "🔥" to "Cúng giỗ & Tâm linh"
        "ram" -> "🌙" to "Rằm & Mùng 1"
        "tet" -> "🎊" to "Tết & Lễ lớn"
        "nhap" -> "🏠" to "Nhà cửa & Công việc"
        "khai" -> "🏪" to "Nhà cửa & Công việc"
        "xe" -> "🚗" to "Nhà cửa & Công việc"
        "chua" -> "⛩️" to "Đi chùa & Đi lễ"
        else -> "📋" to "Khác"
    }
}
