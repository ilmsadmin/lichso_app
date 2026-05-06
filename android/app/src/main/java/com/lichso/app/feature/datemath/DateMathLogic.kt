package com.lichso.app.feature.datemath

import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.HolidayUtil
import com.lichso.app.util.LunarCalendarUtil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

// ══════════════════════════════════════════════════════════
// DATE MATH — Pure business logic (no Compose deps)
// ══════════════════════════════════════════════════════════

object DateMathLogic {

    // ── Tab Tuổi ─────────────────────────────────────────

    data class AgeResult(
        val age: Int,                     // tuổi tròn dương
        val ageMu: Int,                   // tuổi mụ (~ tuổi âm theo VN: chênh +1 hoặc +2)
        val period: Period,               // năm/tháng/ngày tròn
        val totalDays: Long,
        val totalWeeks: Long,
        val totalHours: Long,
        val nextSolarBirthday: LocalDate, // sinh nhật dương kế tiếp
        val daysToSolarBirthday: Long,
        val nextLunarBirthday: LocalDate, // sinh nhật âm kế tiếp (đã đổi ra dương)
        val daysToLunarBirthday: Long,
        val lunarBirthLabel: String,      // VD: 25/3 Âm
        val canChiYear: String,           // VD: Bính Ngọ
        val conGiap: String,              // VD: Ngọ
    )

    fun computeAge(birth: LocalDate, today: LocalDate = LocalDate.now()): AgeResult {
        val period = Period.between(birth, today)
        val totalDays = ChronoUnit.DAYS.between(birth, today)
        val totalHours = totalDays * 24

        // Lunar info
        val lunarBirth = LunarCalendarUtil.convertSolar2Lunar(
            birth.dayOfMonth, birth.monthValue, birth.year
        )
        val lunarToday = LunarCalendarUtil.convertSolar2Lunar(
            today.dayOfMonth, today.monthValue, today.year
        )

        // Tuổi mụ = lunarYear hiện tại - lunarYear sinh + 1
        val ageMu = lunarToday.lunarYear - lunarBirth.lunarYear + 1

        // Sinh nhật dương kế tiếp
        val solarBd = nextAnniversary(birth, today)
        // Sinh nhật âm kế tiếp (chuyển âm → dương cho năm âm hiện tại / kế tiếp)
        val lunarBd = nextLunarAnniversary(
            lunarBirth.lunarDay, lunarBirth.lunarMonth, today
        )

        return AgeResult(
            age = period.years,
            ageMu = ageMu,
            period = period,
            totalDays = totalDays,
            totalWeeks = totalDays / 7,
            totalHours = totalHours,
            nextSolarBirthday = solarBd,
            daysToSolarBirthday = ChronoUnit.DAYS.between(today, solarBd),
            nextLunarBirthday = lunarBd,
            daysToLunarBirthday = ChronoUnit.DAYS.between(today, lunarBd),
            lunarBirthLabel = "${lunarBirth.lunarDay}/${lunarBirth.lunarMonth} Âm",
            canChiYear = CanChiCalculator.getYearCanChi(lunarBirth.lunarYear),
            conGiap = CanChiCalculator.DIA_CHI[(lunarBirth.lunarYear + 8) % 12],
        )
    }

    private fun nextAnniversary(birth: LocalDate, today: LocalDate): LocalDate {
        val thisYear = try {
            birth.withYear(today.year)
        } catch (_: Exception) {
            // 29/2 → 28/2
            LocalDate.of(today.year, birth.monthValue, 28)
        }
        return if (!thisYear.isBefore(today)) thisYear else {
            try { birth.withYear(today.year + 1) }
            catch (_: Exception) { LocalDate.of(today.year + 1, birth.monthValue, 28) }
        }
    }

    private fun nextLunarAnniversary(
        lunarDay: Int, lunarMonth: Int, today: LocalDate
    ): LocalDate {
        // Try this lunar year then next
        val lunarToday = LunarCalendarUtil.convertSolar2Lunar(
            today.dayOfMonth, today.monthValue, today.year
        )
        for (year in listOf(lunarToday.lunarYear, lunarToday.lunarYear + 1)) {
            val (d, m, y) = LunarCalendarUtil.convertLunar2Solar(
                lunarDay, lunarMonth, year, 0
            )
            if (d == 0) continue
            val solar = LocalDate.of(y, m, d)
            if (!solar.isBefore(today)) return solar
        }
        // Fallback: lunarYear+2
        val (d, m, y) = LunarCalendarUtil.convertLunar2Solar(
            lunarDay, lunarMonth, lunarToday.lunarYear + 2, 0
        )
        return LocalDate.of(y, m, d)
    }

    // ── Năm hạn (Kim Lâu / Tam Tai / Hoang Ốc) ──────────

    data class YearFate(
        val lunarYear: Int,
        val solarYear: Int,
        val ageMu: Int,
        val canChi: String,
        val isKimLau: Boolean,
        val kimLauName: String?,         // KL Thân/Thê/Tử/Lục Súc
        val isTamTai: Boolean,
        val tamTaiNote: String?,         // năm Tam Tai thứ mấy: đầu/giữa/cuối
        val hoangOcName: String,         // Nhất Cát / Nhì Nghi / ...
        val isHoangOcBad: Boolean,
    )

    /**
     * Tính 6 năm âm lịch sắp tới (từ năm âm hiện tại) cho tuổi của user.
     */
    fun upcomingYearFates(
        birthLunarYear: Int,
        from: LocalDate = LocalDate.now(),
        count: Int = 6
    ): List<YearFate> {
        val lunarToday = LunarCalendarUtil.convertSolar2Lunar(
            from.dayOfMonth, from.monthValue, from.year
        )
        val results = mutableListOf<YearFate>()
        for (i in 0 until count) {
            val yLunar = lunarToday.lunarYear + i
            val ageMu = yLunar - birthLunarYear + 1
            if (ageMu <= 0) continue
            results.add(yearFateFor(yLunar, ageMu, from.year + i))
        }
        return results
    }

    private fun yearFateFor(lunarYear: Int, ageMu: Int, solarYear: Int): YearFate {
        // Kim Lâu: tuổi mụ % 9 ∈ {1, 3, 6, 8}
        val klMod = ageMu % 9
        val klName = when (klMod) {
            1 -> "Kim Lâu Thân (hại bản thân)"
            3 -> "Kim Lâu Thê (hại vợ/chồng)"
            6 -> "Kim Lâu Tử (hại con cái)"
            8 -> "Kim Lâu Lục Súc (hại gia súc/tài sản)"
            else -> null
        }

        // Tam Tai: dựa vào tam hợp nhóm tuổi
        // Thân-Tý-Thìn → Tam Tai năm Dần, Mão, Thìn
        // Tỵ-Dậu-Sửu → Tam Tai năm Hợi, Tý, Sửu
        // Dần-Ngọ-Tuất → Tam Tai năm Thân, Dậu, Tuất
        // Hợi-Mão-Mùi → Tam Tai năm Tỵ, Ngọ, Mùi
        val birthChiIdx = (lunarYear - ageMu + 1 + 8) % 12
        val currentChiIdx = (lunarYear + 8) % 12
        val tamTaiYears: List<Int> = when (birthChiIdx) {
            8, 0, 4 -> listOf(2, 3, 4)  // Thân, Tý, Thìn → Dần, Mão, Thìn
            5, 9, 1 -> listOf(11, 0, 1) // Tỵ, Dậu, Sửu → Hợi, Tý, Sửu
            2, 6, 10 -> listOf(8, 9, 10) // Dần, Ngọ, Tuất → Thân, Dậu, Tuất
            11, 3, 7 -> listOf(5, 6, 7)  // Hợi, Mão, Mùi → Tỵ, Ngọ, Mùi
            else -> emptyList()
        }
        val ttIdx = tamTaiYears.indexOf(currentChiIdx)
        val ttNote = when (ttIdx) {
            0 -> "Tam Tai năm đầu"
            1 -> "Tam Tai năm giữa (nặng nhất)"
            2 -> "Tam Tai năm cuối"
            else -> null
        }

        // Hoang Ốc: chu kỳ 6, bắt đầu từ tuổi 10 = Nhất Cát
        // (ageMu - 10) mod 6: 0=Nhất Cát, 1=Nhì Nghi, 2=Tam Địa Sát, 3=Tứ Tấn Tài, 4=Ngũ Thọ Tử, 5=Lục Hoang Ốc
        val ho = ((ageMu - 10) % 6 + 6) % 6
        val hoNames = listOf(
            "Nhất Cát" to false,
            "Nhì Nghi" to false,
            "Tam Địa Sát" to true,
            "Tứ Tấn Tài" to false,
            "Ngũ Thọ Tử" to true,
            "Lục Hoang Ốc" to true,
        )
        val (hoName, hoBad) = hoNames[ho]

        return YearFate(
            lunarYear = lunarYear,
            solarYear = solarYear,
            ageMu = ageMu,
            canChi = CanChiCalculator.getYearCanChi(lunarYear),
            isKimLau = klName != null,
            kimLauName = klName,
            isTamTai = ttNote != null,
            tamTaiNote = ttNote,
            hoangOcName = hoName,
            isHoangOcBad = hoBad,
        )
    }

    // ── Tab Khoảng cách ─────────────────────────────────

    data class DiffResult(
        val from: LocalDate,
        val to: LocalDate,
        val totalDays: Long,
        val totalWeeks: Long,
        val period: Period,
        val workdays: Long,           // không tính T7/CN + lễ Tết VN
        val weekendDays: Long,
        val holidayCount: Int,        // số ngày lễ trong khoảng
    )

    fun computeDiff(d1: LocalDate, d2: LocalDate): DiffResult {
        val from = if (d1.isAfter(d2)) d2 else d1
        val to = if (d1.isAfter(d2)) d1 else d2
        val totalDays = ChronoUnit.DAYS.between(from, to)
        val period = Period.between(from, to)

        var workdays = 0L
        var weekend = 0L
        var holidays = 0
        var cur = from
        // For perf cap loop: limit detailed walk to <= 5 years (~1827 days)
        val walkable = totalDays in 0..(366 * 5)
        if (walkable) {
            while (cur.isBefore(to)) {
                val dow = cur.dayOfWeek
                val isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                val isHoliday = isVnPublicHoliday(cur)
                if (isWeekend) weekend++
                if (isHoliday) holidays++
                if (!isWeekend && !isHoliday) workdays++
                cur = cur.plusDays(1)
            }
        }

        return DiffResult(
            from = from, to = to,
            totalDays = totalDays,
            totalWeeks = totalDays / 7,
            period = period,
            workdays = if (walkable) workdays else -1,
            weekendDays = if (walkable) weekend else -1,
            holidayCount = if (walkable) holidays else -1,
        )
    }

    /**
     * Public holiday check: dương lịch trong HolidayUtil + 5 ngày Tết âm (mùng 1-5/1 âm).
     */
    fun isVnPublicHoliday(date: LocalDate): Boolean {
        val solarKey = "${date.dayOfMonth}/${date.monthValue}"
        if (solarKey in HolidayUtil.SOLAR_HOLIDAYS) {
            // Filter: chỉ tính ngày được nghỉ chính thức
            return solarKey in OFFICIAL_VN_PUBLIC_HOLIDAYS_SOLAR
        }
        val lunar = LunarCalendarUtil.convertSolar2Lunar(
            date.dayOfMonth, date.monthValue, date.year
        )
        // Tết: 30/12 âm năm trước hoặc 1-5/1 âm
        if (lunar.lunarMonth == 1 && lunar.lunarDay in 1..5) return true
        if (lunar.lunarMonth == 12 && lunar.lunarDay == 30) return true
        // Giỗ Tổ Hùng Vương 10/3 âm
        if (lunar.lunarMonth == 3 && lunar.lunarDay == 10) return true
        return false
    }

    // Subset: chỉ những ngày dương lịch được pháp luật VN cho nghỉ
    private val OFFICIAL_VN_PUBLIC_HOLIDAYS_SOLAR = setOf(
        "1/1",   // Tết Dương lịch
        "30/4",  // Giải phóng miền Nam
        "1/5",   // Quốc tế Lao động
        "2/9",   // Quốc khánh
    )

    // ── Tab Cộng/Trừ ────────────────────────────────────

    enum class DateUnit(val label: String) {
        DAY("Ngày"), WEEK("Tuần"), MONTH("Tháng"), YEAR("Năm")
    }

    data class AddResult(
        val base: LocalDate,
        val result: LocalDate,
        val days: Long,
        val dayOfWeek: String,
        val lunarLabel: String,
        val dayCanChi: String,
        val holiday: String?,
    )

    fun computeAdd(base: LocalDate, amount: Long, unit: DateUnit, subtract: Boolean): AddResult {
        val signed = if (subtract) -amount else amount
        val result: LocalDate = when (unit) {
            DateUnit.DAY -> base.plusDays(signed)
            DateUnit.WEEK -> base.plusWeeks(signed)
            DateUnit.MONTH -> base.plusMonths(signed)
            DateUnit.YEAR -> base.plusYears(signed)
        }
        val lunar = LunarCalendarUtil.convertSolar2Lunar(
            result.dayOfMonth, result.monthValue, result.year
        )
        val jd = LunarCalendarUtil.jdFromDate(
            result.dayOfMonth, result.monthValue, result.year
        )
        val holiday = HolidayUtil.getSolarHoliday(result.dayOfMonth, result.monthValue)
            ?: HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)
        return AddResult(
            base = base,
            result = result,
            days = ChronoUnit.DAYS.between(base, result),
            dayOfWeek = vnDayOfWeek(result.dayOfWeek),
            lunarLabel = "${lunar.lunarDay}/${lunar.lunarMonth} Âm",
            dayCanChi = CanChiCalculator.getDayCanChi(jd),
            holiday = holiday,
        )
    }

    /**
     * Preset cúng tang lễ phổ biến trong văn hoá Việt — cộng N ngày từ ngày mất.
     */
    data class FuneralPreset(val label: String, val days: Long, val note: String)

    val FUNERAL_PRESETS = listOf(
        FuneralPreset("Cúng 7 ngày", 7, "Chung thất thứ 1"),
        FuneralPreset("49 ngày", 49, "Chung thất (cúng 7 lần × 7 ngày)"),
        FuneralPreset("100 ngày", 100, "Tốt khốc (thôi khóc)"),
        FuneralPreset("Giỗ đầu", 365, "Tiểu Tường"),
        FuneralPreset("Giỗ hết", 365L * 2, "Đại Tường"),
        FuneralPreset("Mãn tang", 365L * 3, "Trừ phục"),
    )

    fun vnDayOfWeek(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.MONDAY -> "Thứ Hai"
        DayOfWeek.TUESDAY -> "Thứ Ba"
        DayOfWeek.WEDNESDAY -> "Thứ Tư"
        DayOfWeek.THURSDAY -> "Thứ Năm"
        DayOfWeek.FRIDAY -> "Thứ Sáu"
        DayOfWeek.SATURDAY -> "Thứ Bảy"
        DayOfWeek.SUNDAY -> "Chủ Nhật"
    }
}
