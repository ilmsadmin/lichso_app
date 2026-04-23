package com.lichso.app.notification

import android.content.Context
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.screen.profile.ProfileKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Helper xây dựng nội dung tử vi cá nhân hoá cho một ngày cụ thể,
 * dựa trên profile của user (tên, ngày/tháng/năm sinh, giới tính).
 *
 * Dùng cho notification "Tử Vi AI" buổi tối — thay vì nội dung generic,
 * nếu user đã setup đầy đủ profile thì hiển thị tử vi cho ngày mai dành
 * riêng cho tuổi của họ (quan hệ Địa Chi năm sinh × Địa Chi ngày).
 */
object PersonalHoroscopeHelper {

    /** Snapshot profile cần thiết để tính tử vi — null nếu chưa setup đủ. */
    data class UserProfile(
        val displayName: String,
        val birthDay: Int,
        val birthMonth: Int,
        val birthYear: Int,
        val gender: String,
        val lunarYear: Int,
        val yearChiIndex: Int,      // 0..11
        val yearCanChi: String,     // "Giáp Tý"
        val conGiap: String,        // "Tý"
        val conGiapEmoji: String    // "🐭"
    )

    data class Horoscope(
        val title: String,
        val subtitle: String,
        val shortBody: String,
        val lines: List<String>
    )

    // ── Zodiac Chi relationships ──
    // Bộ dữ liệu chuẩn — đồng bộ với SearchScreen ZodiacCompat
    private val CHI_NAMES = listOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
        "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    private val TAM_HOP = listOf(
        setOf(0, 4, 8), setOf(1, 5, 9), setOf(2, 6, 10), setOf(3, 7, 11)
    )
    private val LUC_HOP = listOf(
        setOf(0, 1), setOf(2, 11), setOf(3, 10),
        setOf(4, 9), setOf(5, 8), setOf(6, 7)
    )
    private val LUC_XUNG = listOf(
        setOf(0, 6), setOf(1, 7), setOf(2, 8),
        setOf(3, 9), setOf(4, 10), setOf(5, 11)
    )
    private val LUC_HAI = listOf(
        setOf(0, 7), setOf(1, 6), setOf(2, 5),
        setOf(3, 4), setOf(8, 11), setOf(9, 10)
    )

    private data class Relation(
        val label: String,       // "Tam hợp", "Lục xung"...
        val rating: Int,         // 1..5 (5=tốt nhất, 1=xấu nhất)
        val oneLiner: String     // Mô tả ngắn
    )

    private fun getChiRelation(userChi: Int, dayChi: Int): Relation {
        if (userChi == dayChi) return Relation(
            "Tương đồng", 3,
            "Cùng chi — năng lượng hoà hợp nhưng dễ cạnh tranh."
        )
        val pair = setOf(userChi, dayChi)
        if (LUC_HOP.any { it == pair }) return Relation(
            "Lục hợp ⭐", 5,
            "Ngày lục hợp — rất hanh thông, thuận lợi cho mọi việc lớn."
        )
        if (TAM_HOP.any { it.containsAll(pair) }) return Relation(
            "Tam hợp ✨", 4,
            "Ngày tam hợp — quý nhân phù trợ, hợp ký kết và khởi sự."
        )
        if (LUC_XUNG.any { it == pair }) return Relation(
            "Lục xung ⚠️", 1,
            "Ngày xung tuổi — nên tránh việc trọng đại, giữ tâm tĩnh lặng."
        )
        if (LUC_HAI.any { it == pair }) return Relation(
            "Lục hại ⚡", 2,
            "Ngày hại tuổi — cẩn trọng lời nói, tránh tranh chấp."
        )
        return Relation(
            "Bình hoà", 3,
            "Ngày bình thường — thuận theo đánh giá chung của ngày."
        )
    }

    /**
     * Đọc profile từ DataStore. Trả về null nếu user chưa điền đủ
     * ngày/tháng/năm sinh (các giá trị 0 hoặc < 1900).
     */
    suspend fun loadProfile(context: Context): UserProfile? {
        return try {
            val prefs = context.settingsDataStore.data.first()
            val rawName = prefs[ProfileKeys.DISPLAY_NAME]?.trim().orEmpty()
            val bd = prefs[ProfileKeys.BIRTH_DAY] ?: 0
            val bm = prefs[ProfileKeys.BIRTH_MONTH] ?: 0
            val by = prefs[ProfileKeys.BIRTH_YEAR] ?: 0
            val gender = prefs[ProfileKeys.GENDER] ?: "Nam"

            // Yêu cầu: có tên (khác default) + ngày tháng năm sinh hợp lệ
            if (rawName.isEmpty() || rawName == "Người dùng") return null
            if (bd !in 1..31 || bm !in 1..12 || by !in 1900..2100) return null

            val lunar = LunarCalendarUtil.convertSolar2Lunar(bd, bm, by)
            val yearChiIdx = (lunar.lunarYear + 8) % 12
            val conGiap = CHI_NAMES[yearChiIdx]
            val conGiapEmoji = listOf(
                "🐭", "🐮", "🐯", "🐱", "🐲", "🐍",
                "🐴", "🐐", "🐒", "🐔", "🐶", "🐷"
            )[yearChiIdx]
            val yearCanChi = CanChiCalculator.getYearCanChi(lunar.lunarYear)

            UserProfile(
                displayName = rawName,
                birthDay = bd, birthMonth = bm, birthYear = by,
                gender = gender,
                lunarYear = lunar.lunarYear,
                yearChiIndex = yearChiIdx,
                yearCanChi = yearCanChi,
                conGiap = conGiap,
                conGiapEmoji = conGiapEmoji
            )
        } catch (e: Exception) {
            android.util.Log.e("PersonalHoroscope", "loadProfile failed", e)
            null
        }
    }

    /**
     * Build horoscope cho [targetDate] dành riêng cho [profile].
     */
    fun buildHoroscope(profile: UserProfile, targetDate: LocalDate): Horoscope {
        val dayInfo = DayInfoProvider().getDayInfo(
            targetDate.dayOfMonth, targetDate.monthValue, targetDate.year
        )
        val dayChiName = dayInfo.dayCanChi.substringAfterLast(' ')
        val dayChiIdx = CHI_NAMES.indexOf(dayChiName).coerceAtLeast(0)
        val rel = getChiRelation(profile.yearChiIndex, dayChiIdx)

        val dd = "%02d".format(targetDate.dayOfMonth)
        val mm = "%02d".format(targetDate.monthValue)
        val isTomorrow = targetDate == LocalDate.now().plusDays(1)
        val dateLabel = if (isTomorrow) "ngày mai ($dd/$mm)" else "$dd/$mm"

        // Tổng hợp rating: kết hợp dayRating + relation
        val baseScore = dayInfo.dayRating.score
        val adjScore = when (rel.rating) {
            5 -> (baseScore + 15).coerceAtMost(100)
            4 -> (baseScore + 8).coerceAtMost(100)
            2 -> (baseScore - 10).coerceAtLeast(10)
            1 -> (baseScore - 18).coerceAtLeast(10)
            else -> baseScore
        }
        val personalLabel = when {
            dayInfo.activities.isXauDay -> if (adjScore >= 50) "Trung bình" else "Xấu"
            adjScore >= 80 -> "Rất tốt"
            adjScore >= 60 -> "Tốt"
            adjScore >= 40 -> "Trung bình"
            else -> "Xấu"
        }

        val title = "Tử Vi ${profile.displayName.substringBefore(' ').take(20)} — $dateLabel"
        val subtitle = "${profile.conGiapEmoji} Tuổi ${profile.conGiap} · ${rel.label} · $personalLabel"

        val topGio = dayInfo.gioHoangDao.take(3)
            .joinToString(", ") { "${it.name} (${it.time})" }

        val adviceLines = mutableListOf<String>()
        adviceLines.add("Tuổi ${profile.conGiap} × ngày ${dayChiName}: ${rel.label}")
        adviceLines.add(rel.oneLiner)
        adviceLines.add("Tổng quan: $personalLabel · Trực ${dayInfo.trucNgay.name} · Sao ${dayInfo.saoChieu.name}")
        if (topGio.isNotEmpty()) adviceLines.add("Giờ tốt: $topGio")
        adviceLines.add("Hướng Thần Tài: ${dayInfo.huong.thanTai} · Hỷ Thần: ${dayInfo.huong.hyThan}")

        when (rel.rating) {
            5, 4 -> {
                if (dayInfo.activities.nenLam.isNotEmpty()) {
                    adviceLines.add("Nên: ${dayInfo.activities.nenLam.take(3).joinToString(", ")}")
                }
            }
            2, 1 -> {
                adviceLines.add("Lời khuyên: giữ bình tĩnh, tránh quyết định lớn, cẩn trọng giao tiếp.")
                if (dayInfo.activities.khongNen.isNotEmpty()) {
                    adviceLines.add("Tránh: ${dayInfo.activities.khongNen.take(3).joinToString(", ")}")
                }
            }
            else -> {
                if (dayInfo.activities.nenLam.isNotEmpty()) {
                    adviceLines.add("Nên: ${dayInfo.activities.nenLam.take(2).joinToString(", ")}")
                }
            }
        }
        if (dayInfo.activities.isNguyetKy) adviceLines.add("⚠️ Ngày Nguyệt kỵ — kiêng việc lớn.")
        if (dayInfo.activities.isTamNuong) adviceLines.add("⚠️ Ngày Tam nương — nên thận trọng.")

        val shortBody = "${rel.label} với ngày $dayChiName · $personalLabel. " + rel.oneLiner

        return Horoscope(
            title = title,
            subtitle = subtitle,
            shortBody = shortBody,
            lines = adviceLines
        )
    }
}
