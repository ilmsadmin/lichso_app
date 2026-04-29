package com.lichso.app.feature.points.domain

import kotlin.random.Random

/**
 * Bộ sưu tập 12 con giáp + biến thể hiếm (rare).
 * Dùng cho tính năng "Mở thẻ 12 con giáp" — tiêu ⚡ DailyUnlockKey.DAILY_ZODIAC_CARD
 * để rút 1 thẻ ngẫu nhiên/ngày, đủ bộ thưởng badge.
 */
enum class ZodiacRarity(val display: String, val colorHex: Long, val weight: Int) {
    COMMON   ("Thường",       0xFF8D6E63, 70),   // 70% chance
    RARE     ("Hiếm",         0xFF1976D2, 22),   // 22%
    EPIC     ("Cực hiếm",     0xFF8E24AA, 7),    // 7%
    LEGENDARY("Huyền thoại",  0xFFD4A017, 1),    // <1%
}

data class ZodiacCard(
    val id: String,           // "TY_COMMON" / "RONG_LEGENDARY"
    val name: String,         // "Tý"
    val displayName: String,  // "Chuột Bạch"
    val han: String,          // "子"
    val emoji: String,        // "🐭"
    val rarity: ZodiacRarity,
    val description: String,
)

object ZodiacDeck {
    /** 12 thẻ chính (mỗi con giáp có 1 thẻ COMMON, 1 RARE; vài con thêm EPIC/LEGENDARY). */
    val all: List<ZodiacCard> = listOf(
        // Tý
        ZodiacCard("TY_COMMON",    "Tý",   "Chuột Bạch",      "子", "🐭", ZodiacRarity.COMMON,
            "Thông minh, nhanh nhẹn, khéo léo trong giao tiếp."),
        ZodiacCard("TY_RARE",      "Tý",   "Chuột Vàng",      "子", "🐭", ZodiacRarity.RARE,
            "Chuột Vàng — biểu tượng thịnh vượng, chiêu tài lộc."),
        // Sửu
        ZodiacCard("SUU_COMMON",   "Sửu",  "Trâu Đồng",       "丑", "🐂", ZodiacRarity.COMMON,
            "Cần cù, kiên định, sức mạnh bền bỉ."),
        ZodiacCard("SUU_RARE",     "Sửu",  "Trâu Bạc",        "丑", "🐂", ZodiacRarity.RARE,
            "Trâu Bạc — sức bền vĩnh hằng, khắc chế nghịch cảnh."),
        // Dần
        ZodiacCard("DAN_COMMON",   "Dần",  "Hổ Vàng",         "寅", "🐅", ZodiacRarity.COMMON,
            "Dũng mãnh, uy quyền, thủ lĩnh trong rừng xanh."),
        ZodiacCard("DAN_EPIC",     "Dần",  "Bạch Hổ",         "寅", "🐅", ZodiacRarity.EPIC,
            "Bạch Hổ — thần thú phương Tây, trừ tà hộ mệnh."),
        // Mão
        ZodiacCard("MAO_COMMON",   "Mão",  "Mèo Tam Thể",     "卯", "🐈", ZodiacRarity.COMMON,
            "Tinh tế, linh hoạt, biết chọn thời cơ."),
        ZodiacCard("MAO_RARE",     "Mão",  "Mèo Ngọc",        "卯", "🐈", ZodiacRarity.RARE,
            "Mèo Ngọc — biểu tượng may mắn, đem điều lành đến nhà."),
        // Thìn
        ZodiacCard("THIN_COMMON",  "Thìn", "Rồng Xanh",       "辰", "🐲", ZodiacRarity.COMMON,
            "Tôn quý, đầy năng lượng, sáng tạo phi thường."),
        ZodiacCard("THIN_LEGEND",  "Thìn", "Hoàng Long",      "辰", "🐉", ZodiacRarity.LEGENDARY,
            "Hoàng Long Vàng — vua của muôn loài, huyền thoại trăm năm."),
        // Tỵ
        ZodiacCard("TI_COMMON",    "Tỵ",   "Rắn Hoa",         "巳", "🐍", ZodiacRarity.COMMON,
            "Trí tuệ, bí ẩn, biết im lặng đúng lúc."),
        ZodiacCard("TI_RARE",      "Tỵ",   "Bạch Xà",         "巳", "🐍", ZodiacRarity.RARE,
            "Bạch Xà — linh thú trường thọ, khôn ngoan."),
        // Ngọ
        ZodiacCard("NGO_COMMON",   "Ngọ",  "Ngựa Ô",          "午", "🐎", ZodiacRarity.COMMON,
            "Tự do, năng động, không ngại thử thách."),
        ZodiacCard("NGO_EPIC",     "Ngọ",  "Xích Thố",        "午", "🐎", ZodiacRarity.EPIC,
            "Xích Thố — thần mã ngàn dặm, tốc độ vô địch."),
        // Mùi
        ZodiacCard("MUI_COMMON",   "Mùi",  "Dê Núi",          "未", "🐐", ZodiacRarity.COMMON,
            "Hiền hòa, nghệ thuật, tâm hồn lãng mạn."),
        ZodiacCard("MUI_RARE",     "Mùi",  "Dê Vàng",         "未", "🐐", ZodiacRarity.RARE,
            "Dê Vàng — phú quý, dồi dào tài lộc."),
        // Thân
        ZodiacCard("THAN_COMMON",  "Thân", "Khỉ Núi",         "申", "🐒", ZodiacRarity.COMMON,
            "Tinh ranh, nhanh trí, đa tài."),
        ZodiacCard("THAN_EPIC",    "Thân", "Tôn Ngộ Không",   "申", "🐵", ZodiacRarity.EPIC,
            "Tôn Ngộ Không — Tề Thiên Đại Thánh, hỗn thiên đại thánh."),
        // Dậu
        ZodiacCard("DAU_COMMON",   "Dậu",  "Gà Trống",        "酉", "🐓", ZodiacRarity.COMMON,
            "Tự tin, tỉ mỉ, nguyên tắc."),
        ZodiacCard("DAU_LEGEND",   "Dậu",  "Phụng Hoàng Đỏ",  "酉", "🦅", ZodiacRarity.LEGENDARY,
            "Phụng Hoàng Đỏ — tái sinh từ tro tàn, biểu tượng vĩnh hằng."),
        // Tuất
        ZodiacCard("TUAT_COMMON",  "Tuất", "Chó Cỏ",          "戌", "🐕", ZodiacRarity.COMMON,
            "Trung thành, tốt bụng, đáng tin cậy."),
        ZodiacCard("TUAT_RARE",    "Tuất", "Hắc Khuyển",      "戌", "🐕‍🦺", ZodiacRarity.RARE,
            "Hắc Khuyển — chó đen canh cổng, trừ tà ma."),
        // Hợi
        ZodiacCard("HOI_COMMON",   "Hợi",  "Heo Đất",         "亥", "🐖", ZodiacRarity.COMMON,
            "Phúc hậu, hào sảng, biết hưởng thụ."),
        ZodiacCard("HOI_RARE",     "Hợi",  "Kim Trư",         "亥", "🐗", ZodiacRarity.RARE,
            "Kim Trư — heo vàng, thịnh vượng và sung túc."),
    )

    /** 12 nhánh con giáp gốc theo thứ tự. */
    val zodiacSigns = listOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
        "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    /** Rút 1 thẻ ngẫu nhiên dựa trên trọng số rarity. */
    fun drawRandom(seed: Long? = null): ZodiacCard {
        val rng = if (seed != null) Random(seed) else Random.Default
        val totalWeight = all.sumOf { it.rarity.weight }
        var roll = rng.nextInt(totalWeight)
        for (card in all) {
            roll -= card.rarity.weight
            if (roll < 0) return card
        }
        return all.last()
    }

    fun byId(id: String): ZodiacCard? = all.firstOrNull { it.id == id }

    /** Trả về có đủ "bộ 12 con giáp" chưa (mỗi sign có ít nhất 1 thẻ). */
    fun isFullSet(collected: Set<String>): Boolean {
        val signsHeld = collected.mapNotNull { byId(it)?.name }.toSet()
        return zodiacSigns.all { it in signsHeld }
    }
}
