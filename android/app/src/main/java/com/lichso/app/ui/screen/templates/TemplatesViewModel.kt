package com.lichso.app.ui.screen.templates

import androidx.lifecycle.ViewModel
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.domain.model.DayInfo
import com.lichso.app.ui.screen.chat.ChatIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

data class TemplateItem(
    val id: Int,
    val iconName: String,
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String>,
    val queryKeyword: String // Used to generate response from DayInfoProvider
)

data class TemplateDetailResult(
    val template: TemplateItem,
    val info: DayInfo,
    val summary: String
)

data class TemplatesUiState(
    val selectedTab: TemplateTab = TemplateTab.ALL,
    val detailResult: TemplateDetailResult? = null,
    val showDetail: Boolean = false
)

enum class TemplateTab { ALL, CEREMONY, EVENTS, FENG_SHUI }

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val dayInfoProvider: DayInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    val allTemplates = listOf(
        TemplateItem(1, "favorite", "Ngày cưới hỏi", "Chọn ngày tốt cho lễ cưới, ăn hỏi, dạm ngõ theo tuổi cô dâu chú rể", "Lễ nghi", listOf("Cưới hỏi", "Phong thủy"), "cuoi"),
        TemplateItem(2, "home", "Động thổ xây nhà", "Xem ngày khởi công, đổ mái, cất nóc theo phong thủy", "Phong thủy", listOf("Xây dựng", "Nhà ở"), "dongTho"),
        TemplateItem(3, "store", "Khai trương cửa hàng", "Ngày tốt khai trương, chọn hướng tài lộc, giờ hoàng đạo", "Kinh doanh", listOf("Khai trương", "Kinh doanh"), "khaiTruong"),
        TemplateItem(4, "directions_car", "Mua xe · Nhận xe", "Ngày tốt mua xe, đăng ký, nhận xe mới theo phong thủy", "Phong thủy", listOf("Mua bán", "Phương tiện"), "muaXe"),
        TemplateItem(5, "flight", "Xuất hành · Du lịch", "Ngày tốt xuất hành, hướng đi tốt, giờ khởi hành", "Kinh doanh", listOf("Xuất hành", "Du lịch"), "xuatHanh"),
        TemplateItem(6, "school", "Nhập học · Thi cử", "Ngày tốt khai giảng, thi cử, nộp đơn xin học", "Lễ nghi", listOf("Học hành", "Thi cử"), "thiCu"),
        TemplateItem(7, "child_care", "Đầy tháng · Thôi nôi", "Chọn ngày lễ đầy tháng, thôi nôi cho bé", "Lễ nghi", listOf("Gia đình", "Lễ nghi"), "leNghi"),
        TemplateItem(8, "temple_buddhist", "Cúng giỗ · Lễ Tết", "Ngày cúng giỗ, lễ Tết, rằm, mùng 1 theo âm lịch", "Lễ nghi", listOf("Cúng lễ", "Truyền thống"), "cungGio"),
    )

    fun selectTab(tab: TemplateTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun getFilteredTemplates(): List<TemplateItem> {
        return when (_uiState.value.selectedTab) {
            TemplateTab.ALL -> allTemplates
            TemplateTab.CEREMONY -> allTemplates.filter { it.category == "Lễ nghi" }
            TemplateTab.EVENTS -> allTemplates.filter { it.category == "Kinh doanh" }
            TemplateTab.FENG_SHUI -> allTemplates.filter { it.category == "Phong thủy" }
        }
    }

    fun openTemplateDetail(template: TemplateItem) {
        val today = LocalDate.now()
        val info = dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
        val summary = generateTemplateSummary(template, info)
        _uiState.update {
            it.copy(
                detailResult = TemplateDetailResult(template, info, summary),
                showDetail = true
            )
        }
    }

    fun closeDetail() {
        _uiState.update { it.copy(showDetail = false) }
    }

    /**
     * Scan the next 30 days to find good days for this template category
     */
    fun findGoodDays(template: TemplateItem): List<GoodDayResult> {
        val results = mutableListOf<GoodDayResult>()
        val today = LocalDate.now()

        for (i in 0..29) {
            val date = today.plusDays(i.toLong())
            val info = dayInfoProvider.getDayInfo(date.dayOfMonth, date.monthValue, date.year)

            if (info.activities.isXauDay) continue

            val isGood = when (template.queryKeyword) {
                "cuoi" -> info.activities.nenLam.any { it.contains("Cưới") || it.contains("Hôn") || it.contains("hỏi") }
                "dongTho" -> info.activities.nenLam.any { it.contains("Động thổ") || it.contains("Xây") || it.contains("Sửa chữa") }
                "khaiTruong" -> info.activities.nenLam.any { it.contains("Khai trương") || it.contains("Mở cửa") || it.contains("Giao dịch") }
                "muaXe" -> info.activities.nenLam.any { it.contains("Mua") || it.contains("Giao dịch") || it.contains("Nhận") }
                "xuatHanh" -> info.activities.nenLam.any { it.contains("Xuất hành") || it.contains("Du lịch") || it.contains("Di chuyển") }
                "thiCu" -> info.activities.nenLam.any { it.contains("Học") || it.contains("Nhập") || it.contains("Khai") }
                "leNghi" -> info.activities.nenLam.any { it.contains("Lễ") || it.contains("Cầu phúc") || it.contains("Gia tiên") }
                "cungGio" -> info.activities.nenLam.any { it.contains("Cúng") || it.contains("Lễ") || it.contains("Cầu phúc") }
                else -> true
            }

            if (isGood) {
                results.add(
                    GoodDayResult(
                        date = date,
                        solarStr = "${date.dayOfMonth}/${date.monthValue}/${date.year}",
                        lunarStr = "${info.lunar.day}/${info.lunar.month} âm",
                        dayCanChi = info.dayCanChi,
                        dayOfWeek = info.dayOfWeek,
                        gioHoangDao = info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" },
                        daysFromNow = i
                    )
                )
            }
        }
        return results.take(10)
    }

    private fun generateTemplateSummary(template: TemplateItem, info: DayInfo): String {
        val sb = StringBuilder()
        val dd = info.solar.dd
        val mm = info.solar.mm
        val yy = info.solar.yy
        val i = ChatIcons

        sb.appendLine("${i.CALENDAR} Hôm nay: $dd/$mm/$yy (${info.dayOfWeek})")
        sb.appendLine("${i.LUNAR} Âm lịch: ${info.lunar.day}/${info.lunar.month} · ${info.yearCanChi}")
        sb.appendLine("${i.CANCHI} Ngày: ${info.dayCanChi}")
        sb.appendLine()

        when (template.queryKeyword) {
            "cuoi" -> {
                val ok = info.activities.nenLam.any { it.contains("Cưới") || it.contains("Hôn") || it.contains("hỏi") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP cho việc cưới hỏi, ăn hỏi, dạm ngõ.")
                } else {
                    sb.appendLine("${i.WARNING} Hôm nay KHÔNG LÝ TƯỞNG cho cưới hỏi.")
                }
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ đẹp: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.COMPASS} Hướng Hỷ Thần: ${info.huong.hyThan}")
                sb.appendLine("${i.SPARKLE} Nên đón dâu/rước lễ theo hướng ${info.huong.hyThan}.")
            }
            "dongTho" -> {
                val ok = info.activities.nenLam.any { it.contains("Động thổ") || it.contains("Xây") || it.contains("Sửa chữa") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP để động thổ, khởi công, sửa chữa nhà.")
                } else {
                    sb.appendLine("${i.WARNING} Hôm nay KHÔNG NÊN động thổ.")
                }
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.COMPASS} Hướng tốt: ${info.huong.thanTai}")
                sb.appendLine("${i.WARNING} Tránh hướng: ${info.huong.hungThan}")
            }
            "khaiTruong" -> {
                val ok = info.activities.nenLam.any { it.contains("Khai trương") || it.contains("Mở cửa") || it.contains("Giao dịch") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP để khai trương!")
                } else {
                    sb.appendLine("${i.WARNING} Hôm nay KHÔNG LÝ TƯỞNG để khai trương.")
                }
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(4).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.FORTUNE} Hướng tài lộc: ${info.huong.thanTai}")
                sb.appendLine("${i.SPARKLE} Đặt quầy thu ngân hướng ${info.huong.thanTai} để đón vượng khí.")
            }
            "muaXe" -> {
                val ok = info.activities.nenLam.any { it.contains("Mua") || it.contains("Giao dịch") || it.contains("Nhận") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP để mua xe, nhận xe mới!")
                } else {
                    sb.appendLine("${i.WARNING} Hôm nay chưa lý tưởng để mua xe.")
                }
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ tốt: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.FORTUNE} Hướng Thần Tài: ${info.huong.thanTai}")
            }
            "xuatHanh" -> {
                val ok = info.activities.nenLam.any { it.contains("Xuất hành") || it.contains("Du lịch") || it.contains("Di chuyển") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP để xuất hành, du lịch!")
                } else {
                    sb.appendLine("${i.WARNING} Cần cân nhắc khi xuất hành hôm nay.")
                }
                sb.appendLine()
                sb.appendLine("${i.COMPASS} Hướng xuất hành tốt: ${info.huong.thanTai}")
                sb.appendLine("${i.CLOCK} Giờ khởi hành: ${info.gioHoangDao.firstOrNull()?.let { "${it.name} (${it.time})" } ?: "N/A"}")
                sb.appendLine("${i.WARNING} Tránh hướng: ${info.huong.hungThan}")
            }
            "thiCu" -> {
                val ok = info.activities.nenLam.any { it.contains("Học") || it.contains("Nhập") || it.contains("Khai") }
                if (ok && !info.activities.isXauDay) {
                    sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP cho việc học, thi cử, nhập học!")
                } else {
                    sb.appendLine("${i.WARNING} Hôm nay chưa lý tưởng cho thi cử.")
                }
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.SPARKLE} Hướng tốt cho văn xương: ${info.huong.hyThan}")
            }
            "leNghi", "cungGio" -> {
                sb.appendLine("${i.LUNAR} Âm lịch: ${info.lunar.day}/${info.lunar.month} ${info.yearCanChi}")
                if (info.isRam) sb.appendLine("${i.STAR} Hôm nay Rằm — ngày lễ lớn")
                if (info.isMung1) sb.appendLine("${i.STAR} Hôm nay Mùng 1 — ngày lễ đầu tháng")
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ tốt cúng lễ: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.COMPASS} Hướng đặt bàn thờ tốt: ${info.huong.hyThan}")
                if (info.lunarHoliday != null) sb.appendLine("${i.JOY} Lễ: ${info.lunarHoliday}")
            }
            else -> {
                sb.appendLine("${i.CHECK} Nên: ${info.activities.nenLam.take(5).joinToString(", ")}")
                sb.appendLine("${i.CROSS} Không nên: ${info.activities.khongNen.take(5).joinToString(", ")}")
            }
        }

        if (info.activities.isXauDay) {
            sb.appendLine()
            sb.appendLine("${i.WARNING} LƯU Ý: Hôm nay là ngày xấu${if (info.activities.isNguyetKy) " (Nguyệt Kỵ)" else ""}${if (info.activities.isTamNuong) " (Tam Nương)" else ""}.")
        }

        return sb.toString().trim()
    }
}

data class GoodDayResult(
    val date: LocalDate,
    val solarStr: String,
    val lunarStr: String,
    val dayCanChi: String,
    val dayOfWeek: String,
    val gioHoangDao: String,
    val daysFromNow: Int
)
