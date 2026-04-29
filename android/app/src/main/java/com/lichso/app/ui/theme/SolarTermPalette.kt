package com.lichso.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.lichso.app.util.TietKhiCalculator
import java.time.LocalDate

/**
 * Phase 4 — Theme động theo 24 tiết khí.
 *
 * Mỗi tiết khí (15 ngày) có một palette riêng dựa theo mùa + đặc tính khí hậu Việt Nam.
 * Trả về [LichSoColors] (light variant); fallback về [LightColors] nếu không khớp tiết khí nào.
 */

private data class TermPalette(
    val primary: Color,
    val gold: Color,
    val teal: Color,
    val accent: Color, // dùng cho red/highlight
    val displayLabel: String,
)

private val TERM_PALETTES: Map<String, TermPalette> = mapOf(
    // ── Xuân (Lập Xuân → Cốc Vũ) ──
    "Lập Xuân"   to TermPalette(Color(0xFF388E3C), Color(0xFFFFB300), Color(0xFF00897B), Color(0xFFE91E63), "Hoa đào nở"),
    "Vũ Thủy"    to TermPalette(Color(0xFF26A69A), Color(0xFFFFC107), Color(0xFF00796B), Color(0xFF42A5F5), "Mưa xuân"),
    "Kinh Trập"  to TermPalette(Color(0xFF66BB6A), Color(0xFFAED581), Color(0xFF26A69A), Color(0xFF7CB342), "Sâu thức giấc"),
    "Xuân Phân"  to TermPalette(Color(0xFF43A047), Color(0xFFFFD54F), Color(0xFF00897B), Color(0xFFEC407A), "Cân bằng ngày đêm"),
    "Thanh Minh" to TermPalette(Color(0xFF558B2F), Color(0xFFAEEA00), Color(0xFF00897B), Color(0xFF8BC34A), "Tảo mộ"),
    "Cốc Vũ"     to TermPalette(Color(0xFF689F38), Color(0xFFFFB300), Color(0xFF00796B), Color(0xFF7CB342), "Mưa hạt thóc"),

    // ── Hạ (Lập Hạ → Đại Thử) ──
    "Lập Hạ"     to TermPalette(Color(0xFF00897B), Color(0xFFFFB300), Color(0xFF00695C), Color(0xFFFF7043), "Hè về"),
    "Tiểu Mãn"   to TermPalette(Color(0xFF00796B), Color(0xFFFF8F00), Color(0xFF004D40), Color(0xFFFF5722), "Hạt thóc đầy"),
    "Mang Chủng" to TermPalette(Color(0xFF00695C), Color(0xFFE65100), Color(0xFF004D40), Color(0xFFFF6F00), "Mùa gặt đầu"),
    "Hạ Chí"     to TermPalette(Color(0xFFD84315), Color(0xFFFF8F00), Color(0xFF00695C), Color(0xFFFF5722), "Ngày dài nhất"),
    "Tiểu Thử"   to TermPalette(Color(0xFFE65100), Color(0xFFFFB300), Color(0xFF00897B), Color(0xFFD84315), "Nắng nóng nhẹ"),
    "Đại Thử"    to TermPalette(Color(0xFFBF360C), Color(0xFFFF6F00), Color(0xFF006064), Color(0xFFD84315), "Cực nóng"),

    // ── Thu (Lập Thu → Sương Giáng) ──
    "Lập Thu"    to TermPalette(Color(0xFFEF6C00), Color(0xFFE65100), Color(0xFF00695C), Color(0xFFBF360C), "Thu sang"),
    "Xử Thử"     to TermPalette(Color(0xFFD84315), Color(0xFFFF8F00), Color(0xFF558B2F), Color(0xFFBF360C), "Hết nóng"),
    "Bạch Lộ"    to TermPalette(Color(0xFFC0CA33), Color(0xFFFFD54F), Color(0xFF00897B), Color(0xFFEF6C00), "Sương trắng"),
    "Thu Phân"   to TermPalette(Color(0xFFE65100), Color(0xFFFFAB00), Color(0xFF00695C), Color(0xFFD84315), "Cân bằng thu"),
    "Hàn Lộ"     to TermPalette(Color(0xFFEF6C00), Color(0xFFFF8F00), Color(0xFF00838F), Color(0xFFBF360C), "Sương lạnh"),
    "Sương Giáng" to TermPalette(Color(0xFF8D6E63), Color(0xFFE65100), Color(0xFF455A64), Color(0xFF6D4C41), "Sương sa"),

    // ── Đông (Lập Đông → Đại Hàn) ──
    "Lập Đông"   to TermPalette(Color(0xFF455A64), Color(0xFF607D8B), Color(0xFF1565C0), Color(0xFF37474F), "Đông về"),
    "Tiểu Tuyết" to TermPalette(Color(0xFF546E7A), Color(0xFF78909C), Color(0xFF1976D2), Color(0xFF455A64), "Tuyết nhỏ"),
    "Đại Tuyết"  to TermPalette(Color(0xFF37474F), Color(0xFF607D8B), Color(0xFF0D47A1), Color(0xFF263238), "Tuyết lớn"),
    "Đông Chí"   to TermPalette(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF0D47A1), Color(0xFF283593), "Đêm dài nhất"),
    "Tiểu Hàn"   to TermPalette(Color(0xFF283593), Color(0xFF5C6BC0), Color(0xFF1565C0), Color(0xFF1A237E), "Lạnh nhẹ"),
    "Đại Hàn"   to TermPalette(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF01579B), Color(0xFF1A237E), "Cực lạnh"),
)

/**
 * Lấy tên tiết khí hiện tại + nhãn miêu tả ngắn gọn (ví dụ "Lập Xuân — Hoa đào nở").
 * Trả null nếu không xác định được.
 */
fun currentSolarTermLabel(date: LocalDate = LocalDate.now()): Pair<String, String>? {
    val info = TietKhiCalculator.getCurrentSolarTerm(date.dayOfMonth, date.monthValue, date.year)
    val name = info.current?.name ?: return null
    val palette = TERM_PALETTES[name] ?: return name to ""
    return name to palette.displayLabel
}

/**
 * Tạo palette LichSoColors theo tiết khí hiện tại; fallback về [seasonalPaletteForMonth] nếu không match.
 */
fun solarTermPalette(date: LocalDate = LocalDate.now()): LichSoColors {
    val info = TietKhiCalculator.getCurrentSolarTerm(date.dayOfMonth, date.monthValue, date.year)
    val name = info.current?.name
    val pal = name?.let { TERM_PALETTES[it] }
        ?: return seasonalPaletteForMonth(date.monthValue)

    return LightColors.copy(
        primary = pal.primary,
        primaryContainer = pal.primary.copy(alpha = 0.12f),
        gold = pal.gold,
        gold2 = pal.gold,
        goldDim = pal.gold.copy(alpha = 0.18f),
        teal = pal.teal,
        teal2 = pal.teal,
        tealDim = pal.teal.copy(alpha = 0.12f),
        red = pal.accent,
        red2 = pal.accent,
        noteGold = pal.gold,
        noteTeal = pal.teal,
        noteOrange = pal.accent,
        deepRed = pal.accent,
        neutralAmber = pal.gold,
    )
}
