package com.lichso.app.ui.screen.prayers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.util.SmartRatingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class PrayersViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayersUiState())
    val uiState: StateFlow<PrayersUiState> = _uiState.asStateFlow()

    // Đếm số lần user mở chi tiết văn khấn trong session — chỉ trigger từ lần thứ 2
    // (lần đầu có thể chỉ là tò mò; lần thứ 2 cho thấy engagement thật).
    private var prayerOpenCount = 0

    val categories = listOf(
        PrayerCategory("all", "📋", "Tất cả"),
        PrayerCategory("gio", "🕯️", "Cúng giỗ"),
        PrayerCategory("ram", "🌕", "Rằm & Mùng 1"),
        PrayerCategory("tet", "🏮", "Tết"),
        PrayerCategory("nhap", "🏠", "Nhập trạch"),
        PrayerCategory("khai", "🏪", "Khai trương"),
        PrayerCategory("chua", "⛩️", "Đi chùa"),
        PrayerCategory("xe", "🚗", "Xe mới"),
        PrayerCategory("gia", "👶", "Gia đình"),
        PrayerCategory("dat", "🌾", "Cúng đất"),
    )

    val featuredPrayer = PrayerItem(
        id = 0,
        emoji = "🙏",
        emojiStyle = "gio",
        name = "Văn khấn Rằm tháng Bảy",
        description = "Bài cúng Vu Lan — Xá tội vong nhân, báo hiếu cha mẹ, cúng chúng sinh",
        categoryId = "ram",
        tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT)),
        isPopular = true,
        content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Đức Địa Tạng Vương Bồ Tát, Đức Mục Kiền Liên Tôn Giả.

Hôm nay là ngày Rằm tháng Bảy, nhằm tiết Vu Lan, ngày xá tội vong nhân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Nhân ngày Vu Lan báo hiếu, ngày xá tội vong nhân, chúng con thành tâm sắm sửa hương hoa, lễ vật, trà quả và các thứ cúng dâng, bày lên trước án.

Chúng con thành tâm kính mời các vong linh cô hồn, những linh hồn không nơi nương tựa, không ai thờ cúng, không mồ không mả, kẻ sa sông lạc chợ, người chết đường chết chợ, người chết vì chiến tranh, thiên tai, dịch bệnh.

Cúi xin tất cả các vong linh hãy về đây thụ hưởng lễ vật mà chúng con đã thành tâm sắm sửa.

Chúng con cũng thành tâm hướng về Đức Phật, cầu nguyện cho cha mẹ hiện tiền được sức khỏe dồi dào, phước thọ tăng long. Cha mẹ quá vãng được siêu sinh tịnh độ, thoát ly khổ ách, vãng sinh Cực Lạc.

Nguyện cầu cho muôn loài chúng sinh đều được an lạc, thoát khỏi khổ đau luân hồi.

Phù hộ cho gia đình chúng con bình an, mạnh khỏe, hòa thuận, vạn sự tốt lành.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
        note = "• Lễ cúng Rằm tháng 7 gồm: cúng Phật, cúng Gia tiên, cúng Chúng sinh\n• Cúng chúng sinh nên đặt ngoài trời hoặc trước cửa nhà\n• Lễ vật cúng chúng sinh: cháo loãng, gạo, muối, bỏng ngô, khoai, bắp, bánh, kẹo, tiền vàng mã\n• Thời gian cúng tốt nhất: chiều tối ngày 14 hoặc 15 tháng 7 Âm lịch\n• Nên cúng Phật và gia tiên trước, cúng chúng sinh sau cùng"
    )

    val allPrayers = listOf(
        // ═══════════════════════════════════════
        // ── Cúng giỗ ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 1, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng giỗ (ngày giỗ thường)",
            description = "Bài khấn cúng giỗ Ông Bà, Cha Mẹ, người thân đã khuất vào ngày giỗ hàng năm",
            categoryId = "gio",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Giỗ")),
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

Chúng con cùng toàn thể gia quyến, nhất tâm sắm sửa lễ vật, hương hoa trà quả, trầu cau rượu nước, thắp nén tâm hương dâng trước linh vị.

Trước linh vị của …(tên người mất)… chúng con nghĩ nhớ ân đức sinh thành dưỡng dục, công lao trời biển, nay nhân ngày giỗ, chúng con bày tỏ lòng thành kính, biết ơn sâu sắc.

Chúng con thành tâm kính mời …(tên người mất)… giáng về linh sàng, chứng giám lòng thành, thụ hưởng lễ vật.

Đồng thời kính mời Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, Bá Thúc Huynh Đệ, Cô Di Tỷ Muội, chư vị Hương linh gia tiên nội ngoại đồng lai hâm hưởng.

Kính cáo các vị Tiền Chủ, Hậu Chủ ngụ trong đất này, xin mời đồng lai thụ hưởng.

Cúi xin các vị Tôn thần, gia tiên thương xót con cháu, linh thiêng hiện về, chứng giám lòng thành, phù hộ độ trì cho toàn gia an khang thịnh vượng, già trẻ bình an, mọi việc hanh thông, vạn sự tốt lành.

Chúng con lễ bạc tâm thành, trước án kính lễ, cúi xin được phù hộ độ trì.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ vật gồm: hương, hoa tươi, quả, nước, trầu cau, rượu, đèn nến, mâm cỗ mặn/chay\n• Cúng giỗ nên làm trước ngày giỗ chính 1 ngày (ngày Tiên thường)\n• Ngày giỗ chính gọi là ngày Chính kỵ\n• Thay phần in nghiêng bằng thông tin thực tế\n• Đọc bài khấn với lòng thành kính, trang nghiêm"
        ),
        PrayerItem(
            id = 2, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng 49 ngày",
            description = "Bài khấn cúng 49 ngày (chung thất) cho người mới mất, cầu siêu thoát cho vong linh",
            categoryId = "gio",
            tags = listOf(PrayerTag("Tang lễ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Phật A Di Đà giáo chủ cõi Cực Lạc Tây phương.
Con kính lạy Đức Quan Thế Âm Bồ Tát.
Con kính lạy Đức Đại Thế Chí Bồ Tát.
Con kính lạy Đức Địa Tạng Vương Bồ Tát.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày chung thất (49 ngày) của …(quan hệ: Ông/Bà/Cha/Mẹ)… hiệu là …(tên người mất)…, mất ngày … tháng … năm …

Chúng con nhớ thương …(quan hệ)… vô hạn. Bốn mươi chín ngày qua, chúng con ngày đêm thương nhớ, lòng đau như cắt.

Nay nhân ngày chung thất, chúng con thành tâm sắm sửa lễ vật, hương hoa trà quả, oản phẩm, cơm canh, trầu nước, phẩm vàng, dâng cúng trước linh vị.

Chúng con thành tâm kính mời hương linh …(tên người mất)… giáng về linh sàng, chứng giám lòng thành của con cháu, thụ hưởng lễ vật.

Cúi xin Chư Phật, Chư Đại Bồ Tát, Chư Hiền Thánh Tăng từ bi gia hộ, phóng hào quang tiếp dẫn hương linh …(tên)… được siêu sinh Tịnh Độ, thoát ly khổ nạn, vãng sinh Cực Lạc, hưởng phúc vô biên.

Cũng xin các vị Tôn thần phù hộ cho gia đình chúng con tai qua nạn khỏi, bình an, mọi sự hanh thông.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• 49 ngày là mốc quan trọng nhất theo Phật giáo (thần thức quyết định nơi tái sinh)\n• Nên mời nhà sư hoặc thầy cúng đến tụng kinh cầu siêu\n• Cúng trước bàn thờ vong, lễ vật đầy đủ\n• Nên phóng sinh, làm việc thiện trong 49 ngày\n• Con cháu nên ăn chay, tụng kinh hồi hướng cho người mất"
        ),
        PrayerItem(
            id = 3, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng 100 ngày",
            description = "Bài khấn lễ tốt khốc (100 ngày) — thôi khóc, cầu siêu cho người mới mất",
            categoryId = "gio",
            tags = listOf(PrayerTag("Tang lễ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Phật A Di Đà giáo chủ cõi Cực Lạc Tây phương.
Con kính lạy Đức Quan Thế Âm Bồ Tát.
Con kính lạy Đức Địa Tạng Vương Bồ Tát.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày tốt khốc (Bách nhật — 100 ngày) của …(quan hệ)… hiệu là …(tên người mất)…, mất ngày … tháng … năm …

Một trăm ngày qua, chúng con con cháu ngày đêm thương nhớ …(quan hệ)… khôn nguôi. Nay nhân lễ Bách nhật, chúng con sắm sửa lễ vật, hương hoa trà quả, phẩm oản, cơm canh, vàng bạc, dâng lên trước linh vị.

Chúng con thành tâm kính mời hương linh …(tên người mất)… giáng về linh sàng, chứng giám lòng thành, thụ hưởng lễ vật.

Cúi xin Chư Phật từ bi phóng hào quang tiếp dẫn hương linh sớm được siêu thoát, vãng sinh Cực Lạc, thoát ly khổ ách.

Cúi xin hương linh phù hộ độ trì cho con cháu trong gia đình bình an, khỏe mạnh, hòa thuận, vạn sự hanh thông.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ 100 ngày còn gọi là lễ Tốt khốc (卒哭 — thôi khóc)\n• Sau lễ này, tang gia bắt đầu bớt kiêng kỵ\n• Bàn thờ vong có thể chuyển lên bàn thờ gia tiên\n• Nên cúng cả mâm cỗ đầy đủ, mời họ hàng thân thuộc"
        ),
        PrayerItem(
            id = 15, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng giỗ đầu (Tiểu tường)",
            description = "Bài khấn cúng giỗ đầu tiên — tròn 1 năm ngày mất, lễ Tiểu tường",
            categoryId = "gio",
            tags = listOf(PrayerTag("Giỗ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.
Con kính lạy chư vị Hương linh Gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày giỗ đầu (Tiểu tường) của …(quan hệ)… hiệu là …(tên người mất)…

Tính từ ngày …(quan hệ)… quy tiên đến nay vừa tròn một năm. Con cháu trong nhà ngày đêm thương nhớ, nay nhân ngày giỗ đầu, chúng con nhất tâm sắm sửa lễ vật, hương hoa trà quả, mâm cơm dâng cúng.

Chúng con thành tâm kính mời hương linh …(tên người mất)… giáng về linh sàng, chứng giám lòng thành của con cháu, thụ hưởng lễ vật.

Đồng kính mời Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại đồng lai hâm hưởng.

Cúi xin Chư Phật từ bi gia hộ, tiếp dẫn hương linh siêu sinh Tịnh Độ.

Phù hộ cho con cháu toàn gia an khang, mạnh khỏe, hòa thuận, làm ăn phát đạt, vạn sự tốt lành.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Giỗ đầu (Tiểu tường) là giỗ quan trọng nhất, cần tổ chức long trọng\n• Mời đông đủ họ hàng, bà con thân thuộc\n• Sau giỗ đầu, có thể bỏ bớt khăn tang (giảm tang)"
        ),
        PrayerItem(
            id = 16, emoji = "🕯️", emojiStyle = "gio",
            name = "Văn khấn cúng cơm hàng ngày cho người mới mất",
            description = "Bài khấn cúng cơm sáng tối cho người mới mất trong 49 ngày",
            categoryId = "gio",
            tags = listOf(PrayerTag("Tang lễ"), PrayerTag("Hàng ngày")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Phật A Di Đà.
Con kính lạy Đức Quan Thế Âm Bồ Tát.
Con kính lạy Đức Địa Tạng Vương Bồ Tát.

Con là: …(họ tên)…

Hôm nay là ngày … tháng … năm …

Con thành tâm dâng bát cơm, chén nước lên trước linh vị …(quan hệ)… hiệu là …(tên người mất)…

Kính mời …(quan hệ)… về dùng cơm. Con cháu luôn nhớ thương …(quan hệ)… vô hạn.

Cầu xin Chư Phật từ bi tiếp dẫn hương linh …(tên)… sớm được siêu thoát.

Nam mô A Di Đà Phật! (3 lần)""",
            note = "• Cúng cơm ngày 2 bữa (sáng, tối) trong 49 ngày\n• Cơm mới nấu, canh nóng, đặt trước bàn thờ vong\n• Thắp hương, mời người mất về dùng cơm\n• Sau 49 ngày có thể giảm xuống cúng vào ngày Rằm, Mùng 1"
        ),

        // ═══════════════════════════════════════
        // ── Rằm & Mùng 1 ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 4, emoji = "🌕", emojiStyle = "ram",
            name = "Văn khấn cúng Rằm hàng tháng",
            description = "Bài khấn ngày rằm (15 Âm lịch) tại gia, cúng Phật, Thần Tài, Thổ Công, gia tiên",
            categoryId = "ram",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Rằm")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày Rằm, tháng … năm … (Âm lịch).

Tín chủ con thành tâm sắm sửa hương hoa, lễ vật, trà quả, thắp nén tâm hương dâng lên trước án.

Chúng con kính mời ngài Bản cảnh Thành Hoàng Chư vị Đại Vương, ngài Bản xứ Thần linh Thổ Địa, ngài Bản gia Táo Quân, Ngũ phương Long Mạch, Tài thần giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Chúng con kính mời các cụ Tổ Khảo, Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại họ …(họ)… đồng lai hâm hưởng.

Cúi xin chư vị Tôn Thần, gia tiên phù hộ độ trì cho toàn gia chúng con an khang thịnh vượng, mạnh khỏe, hòa thuận. Con cháu học hành tấn tới, làm ăn phát đạt, vạn sự như ý, mọi việc hanh thông.

Chúng con lễ bạc tâm thành, trước án kính lễ, cúi xin được phù hộ độ trì.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chuẩn bị: hương, hoa tươi, quả, nước sạch, đèn/nến, mâm cỗ chay/mặn\n• Cúng vào buổi sáng hoặc chiều ngày Rằm\n• Nên ăn chay ngày Rằm để tích phước\n• Hoa cúng: hoa cúc, hoa huệ, hoa sen (tránh hoa dại)"
        ),
        PrayerItem(
            id = 5, emoji = "🌑", emojiStyle = "ram",
            name = "Văn khấn cúng Mùng 1 hàng tháng",
            description = "Bài khấn ngày mùng 1 (Sóc) tại gia — sóc vọng đầu tháng, cầu bình an cả tháng",
            categoryId = "ram",
            tags = listOf(PrayerTag("Mùng 1")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 1, tháng … năm … (Âm lịch), ngày sóc đầu tháng.

Tín chủ con thành tâm sắm sửa hương hoa, lễ vật, trà nước dâng lên trước án.

Chúng con kính mời các vị Tôn Thần: ngài Thành Hoàng, ngài Thổ Địa, ngài Táo Quân giáng lâm trước án, chứng giám lòng thành.

Chúng con kính mời chư vị Hương linh gia tiên nội ngoại đồng lai hâm hưởng.

Cúi xin chư vị Tôn thần, gia tiên phù hộ cho gia đình chúng con tháng mới bình an, thuận lợi, sức khỏe dồi dào, công việc hanh thông, cầu tài đắc tài, cầu lộc đắc lộc.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Mùng 1 cúng đơn giản hơn ngày Rằm\n• Lễ vật: hương, hoa, quả, nước, trầu cau\n• Nên cúng vào buổi sáng sớm\n• Ăn chay ngày mùng 1 để tích phước"
        ),
        PrayerItem(
            id = 17, emoji = "🌕", emojiStyle = "ram",
            name = "Văn khấn Rằm tháng Giêng (Tết Nguyên Tiêu)",
            description = "Bài khấn Tết Nguyên Tiêu — Rằm tháng Giêng, lễ lớn đầu năm",
            categoryId = "ram",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Rằm")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Phật Thích Ca Mâu Ni.
Con kính lạy Đức Phật A Di Đà.
Con kính lạy Đức Quan Thế Âm Bồ Tát.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày Rằm tháng Giêng, năm … (Âm lịch), nhằm Tết Nguyên Tiêu, ngày vọng đầu tiên của năm mới.

Tín chủ con thành tâm sắm sửa hương hoa, lễ vật, mâm cỗ chay/mặn, trà quả phẩm oản, dâng lên trước án.

Nhân ngày Rằm tháng Giêng — "Cúng cả năm không bằng Rằm tháng Giêng" — chúng con thành tâm cầu nguyện:

Kính mời chư vị Tôn Thần, chư vị Gia tiên giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Cúi xin phù hộ độ trì cho gia đình chúng con cả năm bình an, sức khỏe dồi dào, tài lộc hanh thông, gia đạo hòa thuận, con cháu học hành tấn tới, vạn sự như ý.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Rằm tháng Giêng là ngày lễ lớn nhất đầu năm\n• Tục ngữ: 'Cúng cả năm không bằng Rằm tháng Giêng'\n• Nên đi chùa cầu an, phóng sinh\n• Cúng cả ở nhà và đi chùa\n• Lễ vật đầy đủ, long trọng hơn Rằm thường"
        ),

        // ═══════════════════════════════════════
        // ── Tết ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 6, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn cúng Giao Thừa (ngoài trời)",
            description = "Bài khấn đêm Giao Thừa ngoài trời — tiễn Ông Hành Khiển cũ, đón Ông Hành Khiển mới",
            categoryId = "tet",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Tết")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.
Con kính lạy Đức Đương Lai Hạ Sinh Di Lặc Tôn Phật.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Nay là phút Giao Thừa năm …(năm cũ)… chuyển sang năm …(năm mới)…

Chúng con thành tâm, sửa biện hương hoa lễ vật, kim ngân trà quả, kính cáo Thiên Địa Tôn Thần.

Năm cũ đã qua, năm mới đã đến, vạn tượng canh tân, muôn vật đổi mới.

Nay kính tiễn ngài Hành Khiển năm …(năm cũ)… là ngài …(tên quan Hành Khiển cũ)… cùng chư vị Phán Quan đã coi sóc nhân gian năm qua. Kính mong ngài nhận lễ, chứng giám lòng thành.

Kính đón ngài Hành Khiển năm …(năm mới)… là ngài …(tên quan Hành Khiển mới)… cùng chư vị Tôn Thần, Phán Quan năm mới.

Kính mời ngài giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Cúi xin phù hộ cho toàn gia chúng con năm mới an khang thịnh vượng, sức khỏe dồi dào, vạn sự tốt lành, gia đạo hưng long, mưa thuận gió hòa, quốc thái dân an.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng ngoài trời, trước cửa nhà, mâm lễ hướng ra ngoài\n• Thời gian: đúng phút Giao Thừa (23:00 – 0:00)\n• Lễ vật: mâm ngũ quả, gà trống luộc/xôi, hương, hoa, đèn, vàng mã, rượu\n• Đốt pháo (nếu cho phép) sau khi khấn\n• Nên ăn mặc chỉnh tề, sạch sẽ"
        ),
        PrayerItem(
            id = 7, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn cúng Tất Niên (30 Tết)",
            description = "Bài khấn chiều 30 Tết, mời ông bà tổ tiên về sum họp ăn Tết cùng con cháu",
            categoryId = "tet",
            tags = listOf(PrayerTag("Tết")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân cùng chư vị Tôn Thần.

Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, Bá Thúc Huynh Đệ, Cô Di Tỷ Muội, chư vị Hương linh gia tiên nội ngoại họ …(họ)…

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày 30 tháng Chạp, năm …, ngày cuối cùng của năm cũ.

Năm cũ sắp qua, năm mới sắp đến. Nhân buổi Tất niên, chúng con cùng toàn gia sắm sửa mâm cỗ, hương hoa trà quả, lễ vật tạ Trời Đất, tạ Tôn Thần.

Chúng con thành tâm kính mời Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại trở về sum họp với con cháu, cùng hưởng lễ Tết, chứng giám lòng thành hiếu kính.

Nhìn lại một năm qua, nhờ ơn Trời Phật, Tổ tiên phù hộ, gia đình chúng con được bình an, khỏe mạnh. Nay chúng con thành tâm tạ ơn.

Cúi xin chư vị Tôn Thần, gia tiên chứng giám, phù hộ cho toàn gia năm mới vạn sự tốt lành, tài lộc hanh thông, gia đạo hưng long.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng vào chiều hoặc tối ngày 30 Tết (hoặc 29 nếu tháng thiếu)\n• Mâm cỗ đầy đủ, truyền thống: bánh chưng, giò chả, xôi, gà, nem…\n• Mời ông bà gia tiên về ăn Tết, ở lại đến hết mùng 3\n• Sau khi cúng, cả gia đình quây quần ăn bữa Tất niên"
        ),
        PrayerItem(
            id = 8, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn Mùng 1 Tết Nguyên Đán",
            description = "Bài khấn sáng mùng 1 Tết — nguyện cầu năm mới bình an, vạn sự như ý",
            categoryId = "tet",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Tết")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Đương niên Hành Khiển năm …(năm mới, ví dụ: Ất Tỵ)…, chư vị Tôn Thần cai quản năm mới.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.

Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 1 tháng Giêng, năm …(tên năm Âm lịch)…, Tết Nguyên Đán — ngày đầu tiên của năm mới.

Chúng con thành tâm sắm sửa mâm cỗ, hương hoa trà quả, trầu cau rượu nước, dâng lên trước án, kính cáo Thiên Địa, kính lạy Gia tiên.

Nhân ngày đầu xuân năm mới, chúng con cung kính dâng lễ, thành tâm cầu nguyện:

Cầu xin chư vị Tôn Thần, gia tiên phù hộ cho toàn gia chúng con năm mới:
— Sức khỏe dồi dào, bách bệnh tiêu trừ
— Tài lộc hanh thông, công việc thuận lợi
— Gia đạo hòa thuận, con cháu hiếu thảo
— Vạn sự như ý, muôn điều tốt lành

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng sáng sớm mùng 1, sau khi đã cúng Giao Thừa đêm trước\n• Trang phục chỉnh tề, lịch sự, vui vẻ\n• Kiêng quét nhà, kiêng nói điều xui mùng 1\n• Sau cúng nên đi chùa lễ Phật đầu năm\n• Hái lộc đầu xuân khi về"
        ),
        PrayerItem(
            id = 18, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn cúng Ông Công Ông Táo (23 tháng Chạp)",
            description = "Bài khấn tiễn Ông Táo về trời ngày 23 tháng Chạp — tục lệ quan trọng trước Tết",
            categoryId = "tet",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Tết")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Đông Trù Tư Mệnh Táo Phủ Thần Quân.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày 23 tháng Chạp, năm …

Tín chủ con thành tâm sắm sửa hương hoa, lễ vật, áo mũ, cá chép (để phóng sinh), kính dâng trước án Táo Quân.

Trải qua một năm, nhờ ơn ngài Táo Quân coi sóc bếp lửa gia đình, phù hộ cho mọi việc trong nhà được bình an, thuận lợi.

Nay đến ngày 23 tháng Chạp, ngài Táo Quân sắp về chầu Ngọc Hoàng Thượng Đế, tâu trình việc trần gian.

Chúng con kính tiễn ngài Táo Quân lên đường. Mong ngài tâu với Ngọc Hoàng những điều tốt đẹp, xin Ngọc Hoàng ban phúc lành cho gia đình chúng con.

Kính xin ngài sớm trở về, tiếp tục phù hộ gia đình chúng con năm mới bình an, thịnh vượng, bếp lửa hồng ấm áp.

Chúng con lễ bạc tâm thành, cúi xin ngài chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng trước 12 giờ trưa ngày 23 tháng Chạp\n• Lễ vật: cá chép sống (phóng sinh), áo mũ giấy, vàng mã, mâm cỗ\n• Phóng sinh cá chép ra sông, hồ (để Ông Táo cưỡi cá chép lên trời)\n• Sau khi cúng, đốt áo mũ vàng mã\n• Dọn dẹp bàn thờ sạch sẽ chuẩn bị Tết"
        ),
        PrayerItem(
            id = 19, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn hóa vàng tiễn Ông Bà (Mùng 3 Tết)",
            description = "Bài khấn hóa vàng ngày mùng 3 Tết — tiễn ông bà trở về cõi âm sau 3 ngày Tết",
            categoryId = "tet",
            tags = listOf(PrayerTag("Tết")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 3 tháng Giêng, năm …

Ba ngày Tết đã qua, Ông Bà gia tiên đã về ăn Tết cùng con cháu. Nay chúng con thành tâm sắm sửa lễ vật, hóa vàng mã, kính tiễn gia tiên trở về cõi âm.

Chúng con kính tiễn Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên lên đường.

Cúi xin gia tiên phù hộ cho con cháu cả năm mới bình an, khỏe mạnh, hòa thuận, làm ăn phát đạt.

Chúng con xin phép hóa vàng, kính tiễn gia tiên.

Nam mô A Di Đà Phật! (3 lần, 3 vái)

(Sau khi khấn xong, mang vàng mã ra hóa)""",
            note = "• Cúng vào sáng hoặc trưa mùng 3 Tết\n• Hóa vàng mã sau khi cúng xong\n• Một số gia đình cúng hóa vàng ngày mùng 4 hoặc mùng 7\n• Sau hóa vàng, Tết chính thức kết thúc, bắt đầu công việc mới"
        ),
        PrayerItem(
            id = 20, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn Tết Đoan Ngọ (Mùng 5 tháng 5)",
            description = "Bài khấn Tết Đoan Ngọ — diệt sâu bọ, cầu mùa màng bội thu",
            categoryId = "tet",
            tags = listOf(PrayerTag("Lễ lớn")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày mùng 5 tháng 5, năm … (Âm lịch), nhằm Tết Đoan Ngọ.

Tín chủ con thành tâm sắm sửa hương hoa, rượu nếp, hoa quả, bánh tro, cơm rượu, dâng lên trước án.

Kính mời chư vị Tôn Thần, gia tiên giáng lâm, chứng giám lòng thành, thụ hưởng lễ vật.

Cúi xin phù hộ cho gia đình chúng con mùa màng bội thu, sâu bọ tiêu trừ, bách bệnh tiêu tán, thân tâm an lạc, gia đạo hòa thuận.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Tết Đoan Ngọ còn gọi là Tết diệt sâu bọ, Tết Giết sâu bọ\n• Lễ vật đặc trưng: cơm rượu nếp, bánh tro (bánh ú), hoa quả mùa hè\n• Sáng mùng 5, ăn cơm rượu và hoa quả để 'diệt sâu bọ' trong người\n• Tắm nước lá mùi, lá xả để trừ tà"
        ),
        PrayerItem(
            id = 21, emoji = "🏮", emojiStyle = "tet",
            name = "Văn khấn Tết Thanh Minh (tảo mộ)",
            description = "Bài khấn Tết Thanh Minh — đi tảo mộ, dọn dẹp mộ phần ông bà tổ tiên",
            categoryId = "tet",
            tags = listOf(PrayerTag("Lễ lớn"), PrayerTag("Tảo mộ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Bản xứ Thổ Địa Chính Thần, ngài Sơn Thần, Hà Bá, Long Mạch, Thổ Thần cai quản khu mộ này.

Con kính lạy hương linh …(tên người mất)…, phần mộ tại …(vị trí)…

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay nhân tiết Thanh Minh, tháng … năm …, chúng con cùng con cháu thành tâm đến mộ phần tảo mộ, dọn dẹp sạch sẽ, sửa sang phần mộ.

Chúng con mang theo hương hoa, lễ vật, trà quả dâng cúng trước mộ phần.

Kính mời hương linh …(tên)… chứng giám lòng thành hiếu kính của con cháu.

Cúi xin Thổ Thần, Sơn Thần phù hộ, giữ gìn mộ phần được yên ổn, linh thiêng.

Cúi xin hương linh gia tiên phù hộ cho con cháu bình an, mạnh khỏe, hòa thuận, làm ăn phát đạt.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Tiết Thanh Minh rơi vào khoảng tháng 3 Âm lịch (tháng 4 Dương lịch)\n• Nên đi tảo mộ cả gia đình, dọn cỏ, sửa mộ, sơn lại mộ\n• Mang theo hương, hoa, quả, nước, vàng mã\n• Sau khi cúng xong mới dọn dẹp mộ\n• Tục ngữ: 'Cây có gốc mới nở ngành xanh ngọn'"
        ),

        // ═══════════════════════════════════════
        // ── Nhà cửa & Công việc ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 9, emoji = "🏠", emojiStyle = "nhap",
            name = "Văn khấn nhập trạch (về nhà mới)",
            description = "Bài khấn khi dọn về nhà mới, xin phép Thổ Công, Thổ Địa cho gia đình vào ở",
            categoryId = "nhap",
            tags = listOf(PrayerTag("✨ Mới", PrayerTagType.NEW), PrayerTag("Nhà mới")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân, ngài Ngũ phương Long Mạch Tôn Thần.
Con kính lạy các vị Tiền Chủ, Hậu Chủ ngôi nhà này.

Tín chủ con là: …(họ tên)…
Nguyên quán tại: …(quê quán)…

Hôm nay là ngày … tháng … năm …(Âm lịch)…, giờ …(giờ hoàng đạo)…

Chúng con dọn đến ở tại ngôi nhà mới địa chỉ: …(địa chỉ mới)…

Chúng con thành tâm sắm sửa hương hoa, lễ vật, mâm cỗ, trà quả, vàng bạc, dâng lên trước án.

Kính cáo chư vị Tôn Thần cai quản khu đất này, kính cáo các vị Tiền Chủ, Hậu Chủ ngôi nhà.

Chúng con xin phép được nhập trạch, dọn về ở tại ngôi nhà mới này.

Cúi xin chư vị Tôn Thần chứng giám lòng thành, cho phép gia đình chúng con được an cư lạc nghiệp nơi đây.

Phù hộ cho gia đình chúng con ở ngôi nhà mới được bình an, hòa thuận, làm ăn phát đạt, vạn sự hanh thông, con cháu hiếu thảo, gia đạo hưng long.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo để nhập trạch\n• Vào nhà mới phải mang theo bếp lửa (hoặc bếp ga cháy) đầu tiên\n• Mang theo gạo, muối, nước vào nhà\n• Bật hết đèn trong nhà cho sáng\n• Đun nồi nước sôi (tượng trưng cho tài lộc cuồn cuộn)\n• Mở vòi nước cho chảy nhẹ (tượng trưng tài lộc)"
        ),
        PrayerItem(
            id = 10, emoji = "🏪", emojiStyle = "khai",
            name = "Văn khấn khai trương",
            description = "Bài khấn khai trương cửa hàng, công ty — cầu tài lộc hanh thông, buôn may bán đắt",
            categoryId = "khai",
            tags = listOf(PrayerTag("Kinh doanh")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy Đức Ông Thần Tài vị tiền.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy ngài Ngũ phương Long Mạch Tài Thần.

Tín chủ con là: …(họ tên)…
Cửa hàng/Công ty: …(tên)…
Tại địa chỉ: …(địa chỉ)…

Hôm nay ngày … tháng … năm … (Âm lịch), giờ …(giờ hoàng đạo)…

Chúng con khai trương cửa hàng/công ty, thành tâm sắm sửa hương hoa, lễ vật, mâm cỗ, trà quả, vàng bạc, dâng lên trước án.

Kính mời Đức Ông Thần Tài, ngài Thổ Địa, chư vị Tôn Thần giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Kính xin các ngài phù hộ cho việc kinh doanh của chúng con:
— Thuận buồm xuôi gió, tài lộc dồi dào
— Khách hàng đông đúc, buôn may bán đắt
— Tiền vào như nước, tiền ra nhỏ giọt
— Công việc suôn sẻ, vạn sự hanh thông
— Nhân viên hòa thuận, đồng lòng phát triển

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo để khai trương\n• Lễ vật: hoa quả, hương, nến, mâm cúng, vàng mã, rượu\n• Hướng bàn thờ Thần Tài theo hướng tốt của năm\n• Cắt băng khai trương sau khi cúng xong\n• Nên mời người tuổi hợp đến khai trương đầu tiên"
        ),
        PrayerItem(
            id = 11, emoji = "🚗", emojiStyle = "xe",
            name = "Văn khấn cúng xe mới",
            description = "Bài khấn khi mua xe ô tô / xe máy mới — cầu đi đường bình an, tránh tai nạn",
            categoryId = "xe",
            tags = listOf(PrayerTag("Xe cộ")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa.
Con kính lạy chư vị Tôn Thần coi sóc đường sá, giao thông.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay ngày … tháng … năm … (Âm lịch), con vừa tậu chiếc xe mới, biển số: …(biển số)…

Con thành tâm sắm sửa hương hoa, lễ vật, trà quả, dâng cúng trước xe mới.

Kính xin Chư Phật, chư vị Tôn Thần từ bi chứng giám, phù hộ cho con và gia đình:
— Đi đường bình an, về nhà mạnh khỏe
— Tránh mọi tai ương, hoạn nạn trên đường
— Xe chạy êm ái, không hư hỏng, không gặp sự cố
— Mọi chuyến đi đều thuận lợi, may mắn

Phù hộ cho xe con luôn bền bỉ, an toàn, mang lại nhiều may mắn cho gia đình.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo để nhận xe và cúng xe\n• Cúng ngay trước xe mới, hương hoa quả đặt trên capô hoặc trước đầu xe\n• Có thể rải gạo muối quanh xe\n• Lau xe sạch sẽ trước khi cúng\n• Một số người còn buộc dải lụa đỏ vào gương xe"
        ),

        // ═══════════════════════════════════════
        // ── Đi chùa ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 12, emoji = "⛩️", emojiStyle = "chua",
            name = "Văn khấn ban Tam Bảo (chùa)",
            description = "Bài khấn khi đi chùa lễ Phật — khấn trước ban Tam Bảo (Phật — Pháp — Tăng)",
            categoryId = "chua",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Đi chùa")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy Đức Phật Thích Ca Mâu Ni — Giáo chủ cõi Ta Bà.
Con lạy Đức Phật A Di Đà — Giáo chủ cõi Cực Lạc Tây Phương.
Con lạy Đức Phật Dược Sư Lưu Ly Quang Vương.
Con lạy Đức Quan Thế Âm Bồ Tát.
Con lạy Đức Đại Thế Chí Bồ Tát.
Con lạy Đức Địa Tạng Vương Bồ Tát.
Con lạy mười phương Chư Phật, Chư Đại Bồ Tát, Chư Hiền Thánh Tăng.

Đệ tử con là: …(họ tên)…
Pháp danh (nếu có): …
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm …(Âm lịch)…
Con thành tâm đến chùa …(tên chùa)… lễ Phật.

Đệ tử con thành tâm kính lễ trước ban Tam Bảo, dâng nén tâm hương, cúi đầu đỉnh lễ.

Nguyện cầu Chư Phật, Chư Bồ Tát từ bi gia hộ cho con cùng toàn thể gia quyến:
— Thân tâm an lạc, trí tuệ sáng suốt
— Nghiệp chướng tiêu trừ, phước huệ tăng trưởng
— Bồ đề tâm kiên cố, tu hành tinh tấn
— Gia đạo bình an, mọi người khỏe mạnh
— Xa lìa khổ ách, hưởng mọi an vui

Con nguyện: quy y Phật, quy y Pháp, quy y Tăng.
Con nguyện: không làm các việc ác, siêng làm các việc lành, giữ tâm ý trong sạch.

Nam mô A Di Đà Phật! (3 lần, 3 lạy)""",
            note = "• Khấn trước ban thờ chính (Tam Bảo) đầu tiên khi vào chùa\n• Trang phục kín đáo, lịch sự, không mặc hở hang\n• Chỉ xin bình an, trí tuệ — KHÔNG xin tài lộc tại ban Tam Bảo\n• Lễ vật: hoa tươi, quả, nước (chỉ đặt đồ chay)\n• Không đặt tiền lẻ, tiền thật lên ban Tam Bảo\n• Bỏ giày dép trước khi vào chính điện"
        ),
        PrayerItem(
            id = 13, emoji = "🙏", emojiStyle = "chua",
            name = "Văn khấn ban Đức Ông",
            description = "Bài khấn trước ban Đức Ông (Cấp Cô Độc) — vị hộ pháp bảo vệ chùa chiền",
            categoryId = "chua",
            tags = listOf(PrayerTag("Đi chùa")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Ông Tu Đạt Đa (Cấp Cô Độc), ngài là đệ tử Phật, đại thí chủ, hộ trì Tam Bảo, bảo vệ chùa chiền.

Con kính lạy Đức Giám Trai Sứ Giả.

Đệ tử con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm …
Con đến chùa …(tên chùa)… lễ Phật, thành tâm kính lễ trước ban Đức Ông.

Đệ tử con dâng lên lễ vật, hương hoa trà quả (có thể dâng lễ mặn nếu chùa cho phép).

Kính xin Đức Ông từ bi chứng giám lòng thành, phù hộ cho con và gia đình:
— Công việc hanh thông, tài lộc dồi dào
— Tai qua nạn khỏi, mọi sự bình an
— Buôn bán thuận lợi, kinh doanh phát đạt
— Gia đạo hòa thuận, con cháu nên người

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn ban Đức Ông SAU khi đã khấn ban Tam Bảo\n• Có thể đặt lễ mặn tại ban Đức Ông (khác với ban Tam Bảo chỉ đặt chay)\n• Đức Ông là vị hộ pháp, có thể cầu xin tài lộc, công danh\n• Lễ vật: hương, hoa, quả, oản, xôi, gà (nếu chùa cho phép)"
        ),
        PrayerItem(
            id = 14, emoji = "⛩️", emojiStyle = "than",
            name = "Văn khấn Thần Tài — Thổ Địa",
            description = "Bài khấn cúng Ông Thần Tài, Ông Thổ Địa tại nhà hoặc nơi kinh doanh",
            categoryId = "chua",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Tài lộc")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Ông Thần Tài vị tiền.
Con kính lạy Ông Địa — Thổ Địa Chính Thần.
Con kính lạy Ngũ Phương Long Mạch Tài Thần.

Tín chủ con là: …(họ tên)…
Ngụ tại / Kinh doanh tại: …(địa chỉ)…

Hôm nay ngày … tháng … năm … (Âm lịch)

Con thành tâm sắm sửa hương hoa, lễ vật, trà nước, trầu cau, dâng lên trước ban thờ Thần Tài, Thổ Địa.

Kính mời Đức Ông Thần Tài, Ông Thổ Địa giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Kính xin các ngài phù hộ cho con:
— Tài lộc hanh thông, tiền tài dồi dào
— Buôn may bán đắt, khách hàng tấp nập
— Công việc thuận lợi, sự nghiệp phát triển
— Gia đạo bình an, mọi người khỏe mạnh
— Vạn sự như ý, cầu gì được nấy

Con xin hứa sẽ làm ăn chân chính, tích đức hành thiện, không gian dối, không lừa lọc.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng Thần Tài vào: mùng 1, Rằm, mùng 10 hàng tháng\n• Lễ vật: hoa tươi (hoa cúc, hoa đồng tiền), quả, nước, hương, nến\n• Ban thờ Thần Tài đặt dưới đất, hướng ra cửa\n• Thay nước thờ mỗi ngày, thắp hương sáng tối\n• Ngày vía Thần Tài: mùng 10 tháng Giêng — cúng lớn, mua vàng"
        ),
        PrayerItem(
            id = 22, emoji = "⛩️", emojiStyle = "chua",
            name = "Văn khấn ban Mẫu (Tứ Phủ)",
            description = "Bài khấn trước ban Mẫu khi đi đền, phủ — cầu sức khỏe, bình an, tài lộc",
            categoryId = "chua",
            tags = listOf(PrayerTag("Đi đền")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Mẫu Thượng Thiên — Mẫu Đệ Nhất.
Con kính lạy Đức Mẫu Thượng Ngàn — Mẫu Đệ Nhị.
Con kính lạy Đức Mẫu Thoải Phủ — Mẫu Đệ Tam.
Con kính lạy Đức Mẫu Địa Phủ — Mẫu Đệ Tứ.

Con kính lạy chư vị Quan Lớn, Chầu Bà, Ông Hoàng, Cô, Cậu trong Tứ Phủ.

Đệ tử con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay ngày … tháng … năm … con đến …(tên đền/phủ)… lễ Mẫu.

Con thành tâm dâng hương hoa, lễ vật, sớ trạng kính dâng trước ban Thánh Mẫu.

Cúi xin Đức Mẫu từ bi thương xót, chứng giám lòng thành, phù hộ cho con cùng gia quyến được bình an, khỏe mạnh, tài lộc dồi dào, mọi sự hanh thông.

Con nguyện tu tâm dưỡng tính, làm việc thiện, tích phước đức.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn ban Mẫu khi đi đền, phủ (không phải chùa Phật)\n• Lễ vật: hương, hoa, quả, oản, xôi, gà, vàng mã, sớ\n• Trang phục lịch sự, trang nghiêm\n• Đền, phủ thờ Mẫu: Phủ Giầy, Phủ Tây Hồ, đền Sòng Sơn…\n• Có thể nhờ thầy viết sớ"
        ),

        // ═══════════════════════════════════════
        // ── Gia đình ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 23, emoji = "👶", emojiStyle = "gia",
            name = "Văn khấn cúng đầy tháng",
            description = "Bài khấn cúng đầy tháng cho bé — lễ quan trọng khi bé tròn 1 tháng tuổi",
            categoryId = "gia",
            tags = listOf(PrayerTag("✨ Mới", PrayerTagType.NEW), PrayerTag("Em bé")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy Đức Bà Chúa Thai Sanh, mười hai bà Mụ đã nặn ra hài nhi.
Con kính lạy ba Đức Ông: Đức Ông Tơ Hồng, Đức Ông Táo Quân, Đức Ông Thổ Địa.
Con kính lạy Đức Ông Bản Mệnh Đương Sinh Thái Tuế.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại họ …(họ cha)… và họ …(họ mẹ)…

Tín chủ con là: …(họ tên cha)…
Cùng vợ (chồng): …(họ tên mẹ)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày cháu …(tên bé)… tròn đầy tháng.

Vợ chồng con sinh được cháu …(trai/gái)…, tên là …(tên bé)…, sinh ngày … tháng … năm … (Dương lịch), nhằm ngày … tháng … năm … (Âm lịch), giờ …

Nay nhân lễ đầy tháng, chúng con thành tâm sắm sửa:
— Hương hoa, đèn nến, trà quả
— Mười hai phần xôi chè (kính dâng mười hai bà Mụ)
— Ba phần xôi chè (kính dâng ba Đức Ông)
— Gà luộc, trứng luộc, chè đậu, xôi gấc
— Trầu cau, rượu nước, vàng mã
Tất cả bày lên trước án, kính dâng chư vị.

Chúng con cúi đầu kính tạ ơn Đức Bà Chúa Thai Sanh, mười hai bà Mụ đã nặn ra cháu, ban cho cháu hình hài khỏe mạnh, ngũ quan đoan chính, chân tay lành lặn.

Kính tạ ơn ba Đức Ông đã bảo hộ sản phụ mẹ tròn con vuông.

Chúng con kính cáo gia tiên nội ngoại: dòng họ nay thêm con cháu, xin Ông Bà chứng giám.

Chúng con kính mời chư vị Tôn Thần, mười hai bà Mụ, ba Đức Ông, gia tiên chứng giám lòng thành, thụ hưởng lễ vật, phù hộ cho cháu …(tên bé)…:
— Hay ăn chóng lớn, khỏe mạnh, ít ốm đau
— Ngủ ngoan, không quấy khóc, dạ yên giấc tốt
— Phúc đức đầy nhà, thông minh sáng láng
— Lớn lên hiếu thảo, nên người thành đạt
— Bình an, mạnh khỏe, mọi sự tốt lành

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng đầy tháng thường tổ chức long trọng, mời họ hàng bạn bè\n• Lễ vật bắt buộc: 12 phần xôi chè (tạ 12 bà Mụ) + 3 phần (tạ 3 Đức Ông)\n• Nghi lễ bẻ miệng trẻ: dùng chè xoa miệng bé (tượng trưng ăn nói dẻo)\n• Nghi lễ khai hoa: bưng bé ra trước bàn thờ, bà ngoại bồng bé đi vòng\n• Đặt tên chính thức cho bé trong lễ đầy tháng\n• Mời người lớn tuổi, có phước đức bế bé đầu tiên\n• Miền Nam thường cúng thêm bánh hỏi, thịt quay"
        ),
        PrayerItem(
            id = 24, emoji = "👶", emojiStyle = "gia",
            name = "Văn khấn cúng thôi nôi (tròn 1 tuổi)",
            description = "Bài khấn cúng thôi nôi — bé tròn 1 tuổi, bốc đồ vật chọn nghề tương lai",
            categoryId = "gia",
            tags = listOf(PrayerTag("✨ Mới", PrayerTagType.NEW), PrayerTag("Em bé")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy Đức Bà Chúa Thai Sanh, mười hai bà Mụ, ba Đức Ông.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên cha/mẹ)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày cháu …(tên bé)… tròn 1 tuổi (thôi nôi).

Vợ chồng con sinh cháu …(trai/gái)…, tên là …(tên bé)…, sinh ngày … tháng … năm …

Nay nhân lễ thôi nôi, chúng con thành tâm sắm sửa:
— Hương hoa, đèn nến, trà quả
— Mười hai phần xôi chè (tạ mười hai bà Mụ)
— Ba phần xôi chè (tạ ba Đức Ông)
— Gà luộc, trứng luộc, mâm cỗ
— Trầu cau, rượu nước, vàng mã
Tất cả bày lên trước án, kính dâng chư vị.

Cháu nhờ ơn trên che chở, nhờ mười hai bà Mụ, ba Đức Ông bảo bọc, đã tròn 1 tuổi, khỏe mạnh, lanh lợi.

Chúng con kính tạ ơn mười hai bà Mụ đã nặn nên cháu, cho cháu tai mắt sáng, chân tay khỏe. Tạ ơn ba Đức Ông đã giữ gìn, bảo hộ cháu suốt một năm qua.

Nay cháu tròn 1 tuổi, chúng con xin phép cho cháu thôi nôi, ra khỏi nôi, bước vào cuộc đời mới.

Chúng con làm lễ bốc đồ (bắt miếu), xin chư vị Tôn Thần, gia tiên chứng giám, dẫn dắt cháu chọn được vật tốt lành, báo hiệu tương lai xán lạn.

Cúi xin chư vị Tôn Thần, gia tiên phù hộ cho cháu …(tên bé)…:
— Khỏe mạnh, hay ăn, chóng lớn
— Thông minh, lanh lợi, sáng dạ
— Phước đức đầy đủ, quý nhân phù trợ
— Lớn lên thành tài, hiếu thảo với cha mẹ
— Gặp nhiều may mắn, vạn sự hanh thông

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ thôi nôi tổ chức khi bé tròn 1 tuổi (tính theo Âm lịch)\n• Nghi thức bốc đồ (bắt miếu): đặt nhiều vật trước bé để bé bốc\n  — Sách/bút: học hành, — Tiền: kinh doanh, — Búa/kìm: kỹ sư\n  — Ống nghe: bác sĩ, — Micro: ca sĩ, — Gương: diễn viên\n• Vật bé bốc đầu tiên tượng trưng cho nghề nghiệp tương lai\n• Lễ vật: 12 chén chè, xôi, gà, trầu cau, hoa quả\n• Mời họ hàng, bạn bè đến chung vui"
        ),
        PrayerItem(
            id = 25, emoji = "💒", emojiStyle = "gia",
            name = "Văn khấn gia tiên ngày cưới (nhà trai)",
            description = "Bài khấn trước bàn thờ gia tiên nhà trai trong ngày cưới — xin phép Ông Bà cho con trai đón vợ",
            categoryId = "gia",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Cưới hỏi")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại họ …

Tín chủ con là: …(họ tên cha chú rể)…
Cùng vợ: …(họ tên mẹ chú rể)…
Ngụ tại: …(địa chỉ nhà trai)…

Hôm nay là ngày … tháng … năm … (Âm lịch), ngày lành tháng tốt.

Con trai chúng con là: …(tên chú rể)…, sinh ngày … tháng … năm …
Nay kết duyên cùng: …(tên cô dâu)…, sinh ngày … tháng … năm …
Con gái ông bà: …(tên bố mẹ cô dâu)…, ngụ tại: …(địa chỉ nhà gái)…

Hai gia đình đã chọn được ngày lành tháng tốt, hôm nay tổ chức lễ thành hôn.

Chúng con thành tâm sắm sửa hương hoa, lễ vật, trầu cau, rượu trà, mâm quả, dâng lên trước linh vị gia tiên.

Kính cáo Ông Bà Tổ tiên: con cháu trong nhà nay đã đến tuổi trưởng thành, nên duyên vợ chồng. Xin Ông Bà Tổ tiên chứng giám, cho phép con trai chúng con …(tên chú rể)… được đón …(tên cô dâu)… về làm dâu trong gia đình.

Xin rước linh hồn Ông Bà về chứng kiến ngày vui của con cháu.

Cúi xin gia tiên phù hộ cho đôi trẻ:
— Phu thê hòa thuận, bách niên giai lão
— Tương kính như tân, đồng cam cộng khổ
— Sớm sinh quý tử, con cháu đầy nhà
— Gia đạo hưng long, hạnh phúc viên mãn
— Vợ chồng đồng lòng, xây dựng cuộc sống ấm no

Chúng con lễ bạc tâm thành, cúi xin gia tiên chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn gia tiên nhà trai: trước khi đoàn đón dâu xuất phát\n• Chú rể thắp hương, vái 3 vái, rồi cùng gia đình đi đón dâu\n• Khi đón dâu về, cô dâu chú rể cùng thắp hương lạy gia tiên nhà trai\n• Lễ vật: trầu cau, rượu, trà, hương, hoa, nến, mâm quả\n• Bố mẹ chú rể đứng cạnh giới thiệu cô dâu với gia tiên"
        ),
        PrayerItem(
            id = 100, emoji = "💒", emojiStyle = "gia",
            name = "Văn khấn gia tiên ngày cưới (nhà gái)",
            description = "Bài khấn trước bàn thờ gia tiên nhà gái — xin phép cho con gái đi lấy chồng",
            categoryId = "gia",
            tags = listOf(PrayerTag("Cưới hỏi")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, chư vị Hương linh gia tiên nội ngoại họ …

Tín chủ con là: …(họ tên cha cô dâu)…
Cùng vợ: …(họ tên mẹ cô dâu)…
Ngụ tại: …(địa chỉ nhà gái)…

Hôm nay là ngày … tháng … năm … (Âm lịch), ngày lành tháng tốt.

Con gái chúng con là: …(tên cô dâu)…, sinh ngày … tháng … năm …
Nay kết duyên cùng: …(tên chú rể)…, sinh ngày … tháng … năm …
Con trai ông bà: …(tên bố mẹ chú rể)…, ngụ tại: …(địa chỉ nhà trai)…

Nay nhà trai đã mang lễ vật đến xin phép rước dâu.

Chúng con thành tâm sắm sửa hương hoa, lễ vật, trầu cau, rượu trà, dâng lên trước linh vị gia tiên.

Kính cáo Ông Bà Tổ tiên: con gái chúng con nay đã trưởng thành, nên duyên vợ chồng. Xin Ông Bà Tổ tiên chứng giám, cho phép con gái chúng con …(tên cô dâu)… được theo chồng về nhà trai.

Con gái đi lấy chồng nhưng vẫn luôn nhớ đến cội nguồn, Ông Bà Tổ tiên bên ngoại.

Cúi xin gia tiên phù hộ cho con gái:
— Về nhà chồng được yêu thương, kính trọng
— Vợ chồng hòa thuận, bách niên giai lão
— Sớm sinh quý tử, gia đạo hưng long
— Hiếu thảo hai bên nội ngoại
— Cuộc sống hạnh phúc, ấm no, viên mãn

Chúng con lễ bạc tâm thành, cúi xin gia tiên chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Khấn gia tiên nhà gái: trước khi cho phép nhà trai vào đón dâu\n• Cô dâu thắp hương, vái 3 vái, lạy cha mẹ trước khi đi\n• Mẹ cô dâu thường không đưa con gái đến nhà trai (tục lệ)\n• Đây là khoảnh khắc xúc động nhất trong ngày cưới\n• Cô dâu bước ra cửa không được ngoái lại (tục lệ)"
        ),
        PrayerItem(
            id = 101, emoji = "🎀", emojiStyle = "gia",
            name = "Văn khấn lễ ăn hỏi (đám hỏi)",
            description = "Bài khấn lễ ăn hỏi — nhà trai mang tráp lễ vật đến nhà gái xin cưới",
            categoryId = "gia",
            tags = listOf(PrayerTag("Cưới hỏi")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại họ …(họ nhà gái)…

Tín chủ con là: …(họ tên cha cô dâu)…
Cùng vợ: …(họ tên mẹ cô dâu)…
Ngụ tại: …(địa chỉ nhà gái)…

Hôm nay là ngày … tháng … năm … (Âm lịch), ngày lành tháng tốt.

Gia đình nhà trai do ông/bà …(tên bố mẹ chú rể)… dẫn đầu, mang tráp lễ vật đến kính dâng gia tiên, xin phép được đính hôn cho con trai …(tên chú rể)… với con gái chúng con là …(tên cô dâu)…

Lễ vật gồm: …(số tráp)… tráp, gồm trầu cau, rượu trà, bánh trái, hoa quả, heo quay…

Chúng con thành tâm đặt lễ vật lên bàn thờ gia tiên, thắp nén nhang thơm.

Kính cáo Ông Bà Tổ tiên: hôm nay là ngày đính hôn (ăn hỏi) của cháu …(tên cô dâu)… với …(tên chú rể)…

Cúi xin Ông Bà Tổ tiên chứng giám cho lễ đính hôn, phù hộ cho đôi trẻ:
— Nhân duyên bền vững, tình cảm son sắt
— Lễ cưới thuận lợi, vạn sự tốt lành
— Sau này vợ chồng hòa thuận, hạnh phúc bền lâu
— Hai gia đình thông gia hòa hợp, quý mến nhau

Chúng con lễ bạc tâm thành, cúi xin gia tiên chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Lễ ăn hỏi (đính hôn) thường trước ngày cưới 1–3 tháng\n• Số tráp thường là số lẻ: 5, 7, 9, 11 tráp\n• Tráp gồm: trầu cau, rượu, trà, bánh, trái cây, heo quay, nữ trang…\n• Nhà gái nhận tráp, chia lại cho nhà trai (lại quả) với số chẵn\n• Bài khấn này do nhà gái đọc tại bàn thờ gia tiên nhà gái"
        ),
        PrayerItem(
            id = 102, emoji = "🤰", emojiStyle = "gia",
            name = "Văn khấn cầu tự (cầu con)",
            description = "Bài khấn cầu tự — vợ chồng hiếm muộn xin được ban cho con cái",
            categoryId = "gia",
            tags = listOf(PrayerTag("Gia đình")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Phật Bà Quan Thế Âm Bồ Tát.
Con kính lạy Đức Bà Chúa Thai Sanh, mười hai bà Mụ.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên chồng)…
Cùng vợ: …(họ tên vợ)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch).

Vợ chồng con kết hôn đã …(số năm)… năm, chưa có phước đức sinh con nối dõi.

Chúng con thành tâm sắm sửa hương hoa, lễ vật, trà quả, dâng lên trước Phật đài và bàn thờ gia tiên.

Chúng con chí thành chí kính, quỳ trước Đức Phật Bà Quan Âm, Đức Bà Chúa Thai Sanh, mười hai bà Mụ, xin ban cho vợ chồng con được:
— Sớm có tin vui, hoài thai thuận lợi
— Mẹ khỏe, con khỏe, thai nhi phát triển bình thường
— Sinh nở mẹ tròn con vuông
— Con cái ngoan hiền, khỏe mạnh, thông minh
— Gia đình hạnh phúc, trọn vẹn thiên chức làm cha mẹ

Chúng con xin hứa ăn ở hiền lành, làm việc thiện, tích phước đức, nuôi dạy con nên người.

Kính xin gia tiên phù hộ, nối dõi tông đường, để dòng họ được hưng vượng.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Có thể cầu tự tại chùa thờ Quan Âm, đền bà Chúa Kho, đền bà Chúa Thai Sanh\n• Chùa Hương, chùa Bà Đanh, Phủ Tây Hồ là nơi cầu tự nổi tiếng\n• Vợ chồng cùng đi lễ, thành tâm cầu nguyện\n• Ăn chay trước ngày đi lễ 1–3 ngày\n• Sau khi có con nên đi tạ lễ (trả lễ)"
        ),
        PrayerItem(
            id = 103, emoji = "🕯️", emojiStyle = "gia",
            name = "Văn khấn cúng Mụ (cúng 12 bà Mụ)",
            description = "Bài khấn cúng Mụ hàng tháng cho bé — tạ ơn 12 bà Mụ bảo hộ trẻ sơ sinh",
            categoryId = "gia",
            tags = listOf(PrayerTag("Em bé")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Bà Chúa Thai Sanh.
Con kính lạy mười hai bà Mụ:
— Bà Mụ Nhất: nặn tay — Bà Mụ Nhị: nặn chân
— Bà Mụ Ba: nặn tai — Bà Mụ Tư: nặn mắt
— Bà Mụ Năm: nặn mũi — Bà Mụ Sáu: nặn miệng
— Bà Mụ Bảy: nặn lưỡi — Bà Mụ Tám: nặn ruột
— Bà Mụ Chín: nặn da — Bà Mụ Mười: nặn tóc
— Bà Mụ Mười Một: nặn xương — Bà Mụ Mười Hai: dạy bé khôn ngoan
Con kính lạy ba Đức Ông.

Tín chủ con là: …(họ tên cha/mẹ)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm …, nhằm ngày cháu …(tên bé)… được …(số tháng)… tháng.

Chúng con thành tâm sắm sửa:
— Mười hai chén chè nhỏ, ba chén chè lớn
— Xôi, cháo, bánh, trứng luộc
— Hương, hoa, trà, nước, vàng mã

Kính dâng mười hai bà Mụ và ba Đức Ông.

Cúi xin các bà Mụ tiếp tục che chở, bảo bọc cháu …(tên bé)…:
— Ăn no, ngủ yên, lớn nhanh
— Không ốm đau, không giật mình, không quấy khóc
— Da dẻ hồng hào, mắt sáng, miệng tươi
— Chân tay cứng cáp, biết lật, biết bò, biết đi đúng lứa

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng Mụ hàng tháng (thường ngày bé đầy tháng mỗi tháng)\n• Cúng đến khi bé tròn 1 tuổi (lễ thôi nôi) thì thôi\n• 12 chén chè nhỏ = 12 bà Mụ, 3 chén lớn = 3 Đức Ông\n• Miền Bắc: chè đậu xanh, chè trôi nước\n• Miền Nam: chè đậu trắng, xôi, cháo\n• Đặt mâm cúng ở phòng bé hoặc bàn thờ gia tiên"
        ),
        PrayerItem(
            id = 104, emoji = "🏠", emojiStyle = "gia",
            name = "Văn khấn cúng giải hạn đầu năm",
            description = "Bài khấn lễ giải hạn, cầu an đầu năm — xin tiêu tai giải nạn cho cả gia đình",
            categoryId = "gia",
            tags = listOf(PrayerTag("Cầu an")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Dược Sư Lưu Ly Quang Vương Phật.
Con kính lạy Đức Phật Bà Quan Thế Âm Bồ Tát.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Đương Niên Hành Khiển, ngài Bản Mệnh Nguyên Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa, ngài Bản gia Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…, tuổi …(tuổi Âm lịch)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch).

Năm nay, …(tuổi con)… phạm vào sao …(tên sao xấu, ví dụ: La Hầu, Kế Đô, Thái Bạch…)…, e có điều bất lợi, tai ương, ốm đau, hao tài tốn của.

Chúng con thành tâm sắm sửa hương hoa, lễ vật, sớ giải hạn, vàng mã, dâng lên trước án.

Cúi xin chư Phật, chư vị Tôn Thần, ngài Đương Niên Hành Khiển, ngài Bản Mệnh Nguyên Thần:
— Giải trừ vận hạn, tiêu tai giải nạn
— Sao xấu lui xa, sao tốt chiếu mệnh
— Ốm đau tiêu trừ, bệnh tật không đến
— Tai qua nạn khỏi, chuyển họa thành phúc
— Gia đình bình an, công việc hanh thông

Con xin nguyện ăn ở hiền lành, làm việc thiện, tích phước đức.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)

(Đọc tên, tuổi từng thành viên trong gia đình cần giải hạn)""",
            note = "• Thường giải hạn đầu năm, tháng Giêng hoặc trước Rằm tháng Giêng\n• Sao xấu cần giải: La Hầu, Kế Đô, Thái Bạch, Thái Âm\n• Có thể nhờ nhà chùa làm lễ giải hạn, cầu an\n• Sớ giải hạn viết tên tuổi, địa chỉ gia chủ\n• Chi phí tùy tâm, đóng góp công đức nhà chùa\n• Nên kết hợp ăn chay, làm từ thiện"
        ),
        PrayerItem(
            id = 105, emoji = "📿", emojiStyle = "gia",
            name = "Văn khấn cầu sức khỏe cho cha mẹ",
            description = "Bài khấn cầu an, cầu sức khỏe cho cha mẹ già — thể hiện lòng hiếu thảo",
            categoryId = "gia",
            tags = listOf(PrayerTag("Cầu an"), PrayerTag("Hiếu thảo")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Đức Dược Sư Lưu Ly Quang Vương Phật.
Con kính lạy Đức Phật Bà Quan Thế Âm Bồ Tát đại từ đại bi.
Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch).

Con có cha/mẹ là: …(họ tên cha/mẹ)…, sinh năm …, nay đã …(số tuổi)… tuổi.

Cha/mẹ con tuổi cao sức yếu, gần đây sức khỏe …(mô tả tình trạng)…

Con thành tâm sắm sửa hương hoa, lễ vật, dâng lên trước Phật đài và bàn thờ gia tiên.

Con chí thành cầu nguyện Đức Phật, Đức Dược Sư, Đức Quan Âm, chư vị Tôn Thần:
— Phù hộ cho cha/mẹ con bệnh tật tiêu trừ, thân thể khỏe mạnh
— Tinh thần minh mẫn, ăn ngon ngủ yên
— Phước thọ tăng long, sống lâu trăm tuổi
— Thoát khỏi khổ đau, hưởng tuổi già an vui
— Con cháu sum vầy, gia đình hạnh phúc

Con nguyện ăn chay, làm việc thiện, phóng sinh, tụng kinh cầu siêu, hồi hướng công đức cho cha/mẹ.

Con nguyện hiếu thảo, phụng dưỡng cha mẹ hết lòng.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô Dược Sư Lưu Ly Quang Vương Phật! (3 lần, 3 vái)""",
            note = "• Có thể cầu tại nhà hoặc đến chùa thờ Phật Dược Sư\n• Tụng kinh Dược Sư hồi hướng cho cha mẹ\n• Nên ăn chay, phóng sinh để tạo phước\n• Nếu cha mẹ bệnh nặng, có thể nhờ nhà chùa cầu an\n• Bài khấn này cũng dùng được cho người thân bị bệnh"
        ),

        // ═══════════════════════════════════════
        // ── Cúng đất ──
        // ═══════════════════════════════════════
        PrayerItem(
            id = 26, emoji = "🌾", emojiStyle = "dat",
            name = "Văn khấn cúng đất (động thổ)",
            description = "Bài khấn lễ động thổ — khởi công xây nhà, xin phép Thổ Thần, Long Mạch",
            categoryId = "dat",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Xây dựng")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa Chính Thần.
Con kính lạy ngài Ngũ Phương, Ngũ Thổ Long Mạch Tôn Thần.
Con kính lạy ngài Kim Niên Thái Tuế Chí Đức Tôn Thần.
Con kính lạy ngài Đương Cảnh Thổ Địa Phúc Đức Chính Thần.
Con kính lạy các ngài Thần linh cai quản khu đất này.
Con kính lạy Tiền Chủ, Hậu Chủ tại khu đất này.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ hiện tại)…

Hôm nay là ngày … tháng … năm … (Âm lịch), giờ …(giờ hoàng đạo)…

Chúng con có thửa đất tại: …(địa chỉ lô đất)…, diện tích …, hướng …

Nay muốn khởi công động thổ xây dựng …(nhà ở/cửa hàng/công trình)…

Chúng con thành tâm sắm sửa:
— Hương hoa, đèn nến, trà quả
— Mâm cỗ mặn: gà luộc, xôi, thịt heo, giò chả
— Trầu cau, rượu nước, gạo, muối
— Vàng mã, giấy tiền
Tất cả bày lên trước án, kính dâng chư vị Tôn Thần.

Kính cáo chư vị Tôn Thần cai quản khu đất này: chúng con mạo muội xin phép được động thổ, đào móng, khởi công xây dựng.

Kính xin Tiền Chủ, Hậu Chủ, các vong linh trước đây ở khu đất này, xin tạm lánh sang một bên, cho chúng con được khởi công, sau này sẽ thờ cúng đàng hoàng.

Cúi xin các ngài chứng giám lòng thành, cho phép chúng con được:
— Động thổ thuận lợi, xây dựng suôn sẻ
— Không gặp trở ngại, không có sự cố
— Thợ thuyền bình an, không tai nạn
— Công trình hoàn thành đúng hạn, đúng dự toán
— Ngôi nhà mới vững chãi, bền đẹp
— Gia đình ở đây an cư lạc nghiệp, gia đạo hưng long

Nếu có điều gì vô ý xúc phạm đến các ngài Thần linh, cúi xin các ngài rộng lòng đại xá.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)

(Gia chủ cuốc nhát cuốc đầu tiên theo hướng tốt, rải gạo muối 4 phương)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo, hợp tuổi gia chủ (nhờ thầy phong thủy)\n• Gia chủ phải tự cuốc nhát cuốc đầu tiên\n• Cuốc theo hướng tốt (thầy phong thủy chỉ dẫn)\n• Lễ vật: hương, hoa, quả, rượu, mâm cỗ, vàng mã, gạo, muối\n• Đặt mâm cúng ở giữa khu đất, quay về hướng tốt\n• Rải gạo muối 4 góc + trung tâm sau khi cúng\n• Đốt vàng mã sau khi hương tàn"
        ),
        PrayerItem(
            id = 27, emoji = "🌾", emojiStyle = "dat",
            name = "Văn khấn cúng cất nóc (thượng lương)",
            description = "Bài khấn lễ cất nóc nhà — dựng nóc, thượng lương, đánh dấu hoàn thành phần thô",
            categoryId = "dat",
            tags = listOf(PrayerTag("Xây dựng")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy Đức Lỗ Ban Tiên Sư, Tổ sư bách nghệ.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa.
Con kính lạy ngài Ngũ Phương Long Mạch Tôn Thần.
Con kính lạy ngài Đương Niên Hành Khiển Thái Tuế Chí Đức Tôn Thần.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), giờ …(giờ hoàng đạo)…

Ngôi nhà mới tại: …(địa chỉ xây)…, do chúng con khởi công xây dựng từ ngày … tháng … năm …

Nay đã hoàn thành phần kết cấu chính, đến phần mái, chúng con xin làm lễ cất nóc (thượng lương).

Chúng con thành tâm sắm sửa:
— Hương hoa, đèn nến, trà quả
— Gà luộc, xôi, mâm cỗ mặn
— Rượu, trầu cau, vàng mã
Tất cả bày lên trước án, kính dâng chư vị Tôn Thần.

Kính tạ ơn Đức Lỗ Ban Tiên Sư, chư vị Tôn Thần đã phù hộ cho quá trình xây dựng thuận lợi, thợ thuyền bình an.

Nay xin phép cất nóc, kính xin chư vị Tôn Thần, gia tiên chứng giám, phù hộ cho:
— Cất nóc thuận lợi, bình an
— Ngôi nhà vững chãi, bền đẹp muôn đời
— Mái nhà che chở, giữ ấm gia đình
— Phần hoàn thiện còn lại suôn sẻ, đúng tiến độ
— Gia đình ở đây được bình an, thịnh vượng
— Phúc lộc đầy nhà, con cháu hưng vượng

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)

(Buộc vải đỏ vào đòn nóc, dựng nóc theo giờ tốt)""",
            note = "• Chọn ngày tốt, giờ hoàng đạo để cất nóc\n• Buộc vải đỏ vào đòn nóc (xà ngang chính) — tượng trưng may mắn\n• Lễ vật: hương, hoa, quả, rượu, gà luộc, xôi, vàng mã\n• Thợ cả (tổ trưởng thợ xây) thường được mời uống rượu\n• Đốt pháo (nếu cho phép) sau khi cất nóc\n• Lễ cất nóc là mốc quan trọng, sau đó đến hoàn thiện nội thất"
        ),
        PrayerItem(
            id = 28, emoji = "🌾", emojiStyle = "dat",
            name = "Văn khấn ngày vía Thần Tài (mùng 10 tháng Giêng)",
            description = "Bài khấn ngày vía Thần Tài — cầu tài lộc đầu năm, mua vàng cầu may",
            categoryId = "dat",
            tags = listOf(PrayerTag("Phổ biến", PrayerTagType.HOT), PrayerTag("Tài lộc")),
            isPopular = true,
            content = """Nam mô A Di Đà Phật! (3 lần)

Con kính lạy Đức Ông Thần Tài vị tiền.
Con kính lạy Ông Địa — Thổ Địa Chính Thần.
Con kính lạy ngài Ngũ Phương Tài Thần, Chiêu Tài Tiến Bảo Thiên Tôn.

Tín chủ con là: …(họ tên)…
Ngụ tại / Kinh doanh tại: …(địa chỉ)…

Hôm nay là ngày mùng 10 tháng Giêng, năm …, nhằm ngày vía Thần Tài.

Nhân ngày vía Đức Ông Thần Tài, con thành tâm sắm sửa:
— Hương, hoa tươi, đèn nến
— Mâm ngũ quả, trà, rượu
— Cá lóc nướng (hoặc heo quay, vịt quay)
— Bộ tam sên: thịt heo luộc, trứng luộc, tôm (cua) luộc
— Vàng thật, vàng mã, giấy tiền
Tất cả dâng lên trước ban thờ Thần Tài Thổ Địa.

Kính mời Đức Ông Thần Tài, Ông Thổ Địa, Ngũ Phương Tài Thần giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Kính xin các ngài ban phước cho con và gia đình:
— Tài lộc dồi dào, tiền vào như nước, của cải đầy kho
— Kinh doanh phát đạt, khách hàng tấp nập, hàng hóa tiêu thụ nhanh
— Năm mới nhiều may mắn, phúc lộc song toàn
— Công việc thuận lợi, vạn sự hanh thông
— Gia đạo bình an, mọi người khỏe mạnh
— Gặp quý nhân phù trợ, tránh tiểu nhân hãm hại

Con xin hứa buôn bán chân chính, tích đức hành thiện, không gian dối.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Ngày vía Thần Tài: mùng 10 tháng Giêng Âm lịch\n• Tục lệ: mua vàng (vàng thật hoặc vàng trang sức) trong ngày này\n• Cúng lớn hơn ngày thường: cá lóc nướng, heo quay, vịt quay\n• Bộ tam sên: thịt heo, trứng, tôm — tượng trưng tam tài\n• Thay bình hoa tươi, lau dọn ban thờ Thần Tài sạch sẽ\n• Thắp đèn sáng cả ngày trên ban thờ Thần Tài\n• Rước vàng về phải đặt lên ban thờ Thần Tài trước"
        ),
        PrayerItem(
            id = 106, emoji = "🏡", emojiStyle = "dat",
            name = "Văn khấn cúng Thổ Công (Thần đất) hàng tháng",
            description = "Bài khấn cúng Thổ Công, Thổ Địa — vị thần cai quản đất đai trong nhà",
            categoryId = "dat",
            tags = listOf(PrayerTag("Hàng tháng")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy ngài Bản gia Đông Trù Tư Mệnh Táo Phủ Thần Quân (Táo Quân).
Con kính lạy ngài Bản xứ Thổ Địa Phúc Đức Chính Thần (Thổ Địa).
Con kính lạy ngài Ngũ Phương, Ngũ Thổ Long Mạch Tôn Thần.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm ngày Rằm (hoặc Mùng 1).

Chúng con thành tâm sắm sửa hương hoa, lễ vật, trà quả, dâng lên trước ban thờ Thổ Công.

Kính mời ngài Bản gia Táo Quân, ngài Thổ Địa, ngài Long Mạch giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.

Chúng con kính cầu:
— Ngài cai quản đất đai trong nhà, giữ cho nền móng vững chãi
— Phù hộ gia đình bình an, mọi người khỏe mạnh
— Ngăn chặn tà khí, âm hồn xâm phạm
— Tài lộc hanh thông, công việc thuận lợi
— Gia đạo hòa thuận, vợ chồng con cái yêu thương nhau

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Thổ Công là vị thần quan trọng nhất trong nhà (Đất đai)\n• Bàn thờ Thổ Công thường đặt dưới đất, gần cửa chính\n• Cúng vào ngày Rằm, Mùng 1, và các ngày lễ lớn\n• Lễ vật: hương, hoa, quả, trà, rượu, xôi, gà (tùy dịp)\n• Không đặt chung đồ cúng Thổ Công với bàn thờ gia tiên"
        ),
        PrayerItem(
            id = 107, emoji = "🔨", emojiStyle = "dat",
            name = "Văn khấn cúng sửa nhà, sửa chữa nhà cửa",
            description = "Bài khấn xin phép trước khi sửa chữa, cải tạo nhà cửa — tránh phạm Thổ Thần",
            categoryId = "dat",
            tags = listOf(PrayerTag("Xây dựng")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa Chính Thần.
Con kính lạy ngài Ngũ Phương Long Mạch Tôn Thần.
Con kính lạy ngài Bản gia Đông Trù Tư Mệnh Táo Quân.
Con kính lạy chư vị Hương linh gia tiên nội ngoại.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ nhà cần sửa)…

Hôm nay là ngày … tháng … năm … (Âm lịch), giờ …

Ngôi nhà tại …(địa chỉ)… đã xây dựng được …(số năm)… năm, nay có phần …(mô tả phần sửa: mái, tường, bếp, phòng…)… xuống cấp, cần sửa chữa.

Chúng con thành tâm sắm sửa hương hoa, lễ vật, dâng lên trước án.

Kính cáo chư vị Tôn Thần, Thổ Địa, Táo Quân: chúng con xin phép được sửa chữa …(phần nào)… của ngôi nhà.

Cúi xin chư vị Tôn Thần, gia tiên chấp thuận, phù hộ cho:
— Việc sửa chữa thuận lợi, suôn sẻ
— Không xâm phạm long mạch, không phạm Thổ Thần
— Thợ thuyền bình an, không tai nạn
— Ngôi nhà sau khi sửa bền đẹp hơn
— Gia đình ở bình an, hạnh phúc

Nếu có điều gì vô ý xúc phạm, cúi xin các ngài rộng lòng đại xá.

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Sửa nhà cũng cần cúng xin phép như xây mới (tránh phạm Thổ Thần)\n• Chọn ngày tốt, tránh ngày Tam Nương, Dương Công Kỵ\n• Đặc biệt cần cúng khi: phá tường, đào đất, thay mái, sửa bếp\n• Nếu sửa nhỏ (sơn, lát gạch): cúng đơn giản với hương hoa\n• Nếu sửa lớn (cơi nới, phá dỡ): cúng đầy đủ như động thổ"
        ),
        PrayerItem(
            id = 108, emoji = "🌳", emojiStyle = "dat",
            name = "Văn khấn cúng Thần Nông (cúng ruộng, cúng vụ mùa)",
            description = "Bài khấn cúng Thần Nông — cầu mùa màng bội thu, mưa thuận gió hòa",
            categoryId = "dat",
            tags = listOf(PrayerTag("Nông nghiệp")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy Đức Thần Nông Viêm Đế.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa.
Con kính lạy ngài Ngũ Cốc Phong Đăng Tôn Thần.
Con kính lạy Hà Bá Thủy Quan, Long Vương cai quản sông nước.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ)…
Canh tác tại: …(khu ruộng/vườn)…

Hôm nay là ngày … tháng … năm … (Âm lịch), nhằm …(dịp lễ/đầu vụ/cuối vụ)…

Chúng con thành tâm sắm sửa hương hoa, lễ vật, mâm cỗ, dâng lên trước án.

Kính tạ ơn Đức Thần Nông đã dạy dân cày cấy, gieo trồng.
Kính tạ ơn Thổ Địa đã ban cho đất đai màu mỡ.
Kính tạ ơn Long Vương đã ban mưa thuận gió hòa.

Cúi xin chư vị Tôn Thần phù hộ cho:
— Mưa thuận gió hòa, không bão lũ hạn hán
— Mùa màng bội thu, lúa tốt cây xanh
— Không sâu bệnh, không mất mùa
— Gia đình no đủ, thu hoạch dồi dào
— Người làm ruộng khỏe mạnh, bình an

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)""",
            note = "• Cúng Thần Nông: đầu vụ (xuống giống) và cuối vụ (thu hoạch)\n• Tết Hạ Điền (xuống đồng), Tết Thượng Điền (lên đồng) — tục lệ cổ truyền\n• Đặt mâm cúng ở bờ ruộng hoặc đình làng\n• Lễ vật: gà, xôi, hoa quả, rượu, hương, vàng mã\n• Nhiều làng tổ chức Lễ Tịch Điền đầu năm cầu mùa màng"
        ),
        PrayerItem(
            id = 109, emoji = "⛳", emojiStyle = "dat",
            name = "Văn khấn cúng đất mới mua (nhận đất)",
            description = "Bài khấn sau khi mua đất — cúng trình diện, xin phép Thổ Thần cho chủ mới",
            categoryId = "dat",
            tags = listOf(PrayerTag("✨ Mới", PrayerTagType.NEW), PrayerTag("Đất đai")),
            content = """Nam mô A Di Đà Phật! (3 lần)

Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.

Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn Thần.
Con kính lạy ngài Bản cảnh Thành Hoàng, ngài Bản xứ Thổ Địa Chính Thần.
Con kính lạy ngài Ngũ Phương, Ngũ Thổ Long Mạch Tôn Thần.
Con kính lạy ngài Kim Niên Thái Tuế Chí Đức Tôn Thần.
Con kính lạy Tiền Chủ, Hậu Chủ, các vị chủ cũ của khu đất này.
Con kính lạy chư vị cô hồn, vong linh tại khu đất này.

Tín chủ con là: …(họ tên)…
Ngụ tại: …(địa chỉ hiện tại)…

Hôm nay là ngày … tháng … năm … (Âm lịch).

Con vừa mua được thửa đất tại: …(địa chỉ lô đất mới)…, diện tích …, hướng …

Con thành tâm sắm sửa hương hoa, lễ vật, mâm cỗ, vàng mã, gạo, muối, rượu, trầu cau, dâng lên trước án.

Kính cáo chư vị Tôn Thần cai quản khu đất này: con là chủ nhân mới, xin được trình diện, ra mắt.

Kính mời Tiền Chủ, Hậu Chủ, các vong linh tại đây: đất đã có chủ mới, xin các vị chứng giám. Con sẽ thờ cúng chu đáo, không để hoang phế.

Cúi xin chư vị Tôn Thần chấp thuận con làm chủ nhân mới, phù hộ cho:
— Đất đai yên ổn, long mạch hanh thông
— Không có tranh chấp, kiện tụng
— Sau này xây dựng thuận lợi
— Gia đình an cư lạc nghiệp trên mảnh đất này
— Phúc lộc dồi dào, vạn sự tốt lành

Chúng con lễ bạc tâm thành, cúi xin chứng giám.

Nam mô A Di Đà Phật! (3 lần, 3 vái)

(Rải gạo muối 4 góc đất sau khi cúng)""",
            note = "• Mua đất mới nên cúng ngay sau khi nhận đất (sang tên xong)\n• Cúng ở giữa khu đất, quay về hướng tốt\n• Rải gạo muối 4 góc: tẩy uế, xua đuổi tà khí\n• Lễ vật: hương, hoa, quả, mâm cỗ, gạo, muối, rượu, vàng mã\n• Nếu đất cũ có mộ phần, cần di dời trước khi cúng\n• Cúng trình đất khác cúng động thổ (chưa xây dựng gì)"
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
        prayerOpenCount++
        if (prayerOpenCount >= 2) {
            viewModelScope.launch {
                SmartRatingManager.recordHappyAction(appContext)
            }
        }
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
        "gio" -> "candle" to "Cúng giỗ & Tâm linh"
        "ram" -> "moon" to "Rằm & Mùng 1"
        "tet" -> "lantern" to "Tết & Lễ lớn"
        "nhap" -> "house" to "Nhà cửa & Công việc"
        "khai" -> "store" to "Nhà cửa & Công việc"
        "xe" -> "car" to "Nhà cửa & Công việc"
        "chua" -> "temple" to "Đi chùa & Đi lễ"
        "gia" -> "baby" to "Gia đình & Hỷ sự"
        "dat" -> "rice" to "Cúng đất & Xây dựng"
        else -> "all" to "Khác"
    }
}
