package com.lichso.app.domain

import com.lichso.app.domain.model.*
import com.lichso.app.util.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides complete day information by composing all calculators
 */
@Singleton
class DayInfoProvider @Inject constructor() {

    fun getDayInfo(dd: Int, mm: Int, yy: Int): DayInfo {
        val jd = LunarCalendarUtil.jdFromDate(dd, mm, yy)
        val lunar = LunarCalendarUtil.convertSolar2Lunar(dd, mm, yy)

        val yearCanChi = CanChiCalculator.getYearCanChi(lunar.lunarYear)
        val monthCanChi = CanChiCalculator.getMonthCanChi(lunar.lunarMonth, lunar.lunarYear)
        val dayCanChi = CanChiCalculator.getDayCanChi(jd)
        val dayOfWeek = CanChiCalculator.getDayOfWeek(jd)
        val dayOfWeekIndex = CanChiCalculator.getDayOfWeekIndex(jd)

        val moonPhase = MoonPhaseCalculator.getMoonPhase(dd, mm, yy)
        val gioHD = GioHoangDaoCalculator.getGioHoangDao(jd)
        val activities = DayActivityCalculator.getDayActivities(jd, lunar.lunarDay, lunar.lunarMonth)
        val huong = DayActivityCalculator.getHuongTot(jd)
        val solarHoliday = HolidayUtil.getSolarHoliday(dd, mm)
        val lunarHoliday = HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)
        val tietKhi = TietKhiCalculator.getCurrentSolarTerm(dd, mm, yy)

        val lunarMonthName = if (lunar.lunarLeap == 1) "tháng ${lunar.lunarMonth} nhuận" else "tháng ${lunar.lunarMonth}"

        return DayInfo(
            solar = SolarDate(dd, mm, yy),
            lunar = LunarDate(lunar.lunarDay, lunar.lunarMonth, lunar.lunarYear, lunar.lunarLeap, lunarMonthName),
            jd = jd,
            dayOfWeek = dayOfWeek,
            dayOfWeekIndex = dayOfWeekIndex,
            yearCanChi = yearCanChi,
            monthCanChi = monthCanChi,
            dayCanChi = dayCanChi,
            moonPhase = MoonPhaseInfo(moonPhase.icon, moonPhase.name, moonPhase.age),
            gioHoangDao = gioHD.map { GioHoangDaoInfo(it.name, it.time) },
            activities = DayActivitiesInfo(activities.nenLam, activities.khongNen, activities.isXauDay, activities.isNguyetKy, activities.isTamNuong),
            huong = HuongTotInfo(huong.thanTai, huong.hyThan, huong.hungThan),
            solarHoliday = solarHoliday,
            lunarHoliday = lunarHoliday,
            tietKhi = TietKhiInfo(
                currentName = tietKhi.current?.name,
                nextName = tietKhi.next?.name,
                nextDd = tietKhi.next?.dd,
                nextMm = tietKhi.next?.mm,
                daysUntilNext = tietKhi.daysUntilNext
            ),
            isRam = lunar.lunarDay == 15,
            isMung1 = lunar.lunarDay == 1
        )
    }

    fun getCalendarDays(year: Int, month: Int): List<CalendarDay> {
        // month is 1-indexed
        val today = LocalDate.now()
        val firstDay = LocalDate.of(year, month, 1)
        val dayOfWeekFirst = firstDay.dayOfWeek.value % 7 // 0=Sun, 1=Mon..6=Sat
        val daysInMonth = firstDay.lengthOfMonth()
        val prevMonth = firstDay.minusMonths(1)
        val daysInPrevMonth = prevMonth.lengthOfMonth()

        val days = mutableListOf<CalendarDay>()

        // Previous month days
        for (i in dayOfWeekFirst - 1 downTo 0) {
            val d = daysInPrevMonth - i
            val pm = prevMonth.monthValue
            val py = prevMonth.year
            val lunar = LunarCalendarUtil.convertSolar2Lunar(d, pm, py)
            val lunStr = if (lunar.lunarDay == 1) "${lunar.lunarDay}/${lunar.lunarMonth}" else "${lunar.lunarDay}"
            val dow = LocalDate.of(py, pm, d).dayOfWeek
            days.add(
                CalendarDay(
                    solarDay = d, solarMonth = pm, solarYear = py,
                    lunarDay = lunar.lunarDay, lunarMonth = lunar.lunarMonth,
                    isCurrentMonth = false, isToday = false,
                    isSunday = dow.value == 7, isSaturday = dow.value == 6,
                    isHoliday = false, hasEvent = false, lunarDisplayText = lunStr
                )
            )
        }

        // Current month days
        for (d in 1..daysInMonth) {
            val date = LocalDate.of(year, month, d)
            val lunar = LunarCalendarUtil.convertSolar2Lunar(d, month, year)
            val lunStr = if (lunar.lunarDay == 1) "${lunar.lunarDay}/${lunar.lunarMonth}" else "${lunar.lunarDay}"
            val dow = date.dayOfWeek
            val sHol = HolidayUtil.getSolarHoliday(d, month)
            val lHol = HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)
            val hasEvent = lunar.lunarDay == 1 || lunar.lunarDay == 15 || sHol != null || lHol != null

            days.add(
                CalendarDay(
                    solarDay = d, solarMonth = month, solarYear = year,
                    lunarDay = lunar.lunarDay, lunarMonth = lunar.lunarMonth,
                    isCurrentMonth = true,
                    isToday = date == today,
                    isSunday = dow.value == 7,
                    isSaturday = dow.value == 6,
                    isHoliday = sHol != null || lHol != null,
                    hasEvent = hasEvent,
                    lunarDisplayText = lunStr
                )
            )
        }

        // Next month days
        val totalCells = dayOfWeekFirst + daysInMonth
        val remaining = if (totalCells % 7 == 0) 0 else 7 - (totalCells % 7)
        val nextMonth = firstDay.plusMonths(1)
        for (d in 1..remaining) {
            val nm = nextMonth.monthValue
            val ny = nextMonth.year
            val lunar = LunarCalendarUtil.convertSolar2Lunar(d, nm, ny)
            val lunStr = if (lunar.lunarDay == 1) "${lunar.lunarDay}/${lunar.lunarMonth}" else "${lunar.lunarDay}"
            val dow = LocalDate.of(ny, nm, d).dayOfWeek
            days.add(
                CalendarDay(
                    solarDay = d, solarMonth = nm, solarYear = ny,
                    lunarDay = lunar.lunarDay, lunarMonth = lunar.lunarMonth,
                    isCurrentMonth = false, isToday = false,
                    isSunday = dow.value == 7, isSaturday = dow.value == 6,
                    isHoliday = false, hasEvent = false, lunarDisplayText = lunStr
                )
            )
        }

        return days
    }

    fun getUpcomingEvents(dd: Int, mm: Int, yy: Int): List<UpcomingEvent> {
        val events = mutableListOf<UpcomingEvent>()
        val todayJd = LunarCalendarUtil.jdFromDate(dd, mm, yy)

        for (i in 0 until 14) {
            val checkJd = todayJd + i
            val (cd, cm, cy) = LunarCalendarUtil.jdToDate(checkJd)
            val lunar = LunarCalendarUtil.convertSolar2Lunar(cd, cm, cy)
            val timeStr = when (i) {
                0 -> "Hôm nay"
                1 -> "Ngày mai · $cd/$cm"
                else -> "$cd/$cm"
            }

            if (lunar.lunarDay == 15) {
                events.add(UpcomingEvent("Rằm tháng ${lunar.lunarMonth} Âm lịch", timeStr, "Âm lịch", EventColor.TEAL))
            }
            if (lunar.lunarDay == 1) {
                events.add(UpcomingEvent("Mùng 1 tháng ${lunar.lunarMonth} Âm lịch", timeStr, "Âm lịch", EventColor.TEAL))
            }
            val sHol = HolidayUtil.getSolarHoliday(cd, cm)
            if (sHol != null) {
                events.add(UpcomingEvent(sHol, timeStr, "Ngày lễ", EventColor.RED))
            }
            val lHol = HolidayUtil.getLunarHoliday(lunar.lunarDay, lunar.lunarMonth)
            if (lHol != null && lunar.lunarDay != 15 && lunar.lunarDay != 1) {
                events.add(UpcomingEvent(lHol, timeStr, "Âm lịch", EventColor.GOLD))
            }

            if (events.size >= 5) break
        }

        // Add upcoming solar term
        val tkInfo = TietKhiCalculator.getCurrentSolarTerm(dd, mm, yy)
        if (tkInfo.next != null && tkInfo.daysUntilNext <= 14) {
            val nd = tkInfo.next
            val timeStr = when (tkInfo.daysUntilNext) {
                0 -> "Hôm nay"
                1 -> "Ngày mai · ${nd.dd}/${nd.mm}"
                else -> "${nd.dd}/${nd.mm} · Bắt đầu tiết khí mới"
            }
            events.add(UpcomingEvent("Tiết ${nd.name}", timeStr, "Tiết khí", EventColor.RED))
        }

        return events.take(4)
    }
}
