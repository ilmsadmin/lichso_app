package com.lichso.app.util

import kotlin.math.*

/**
 * Thuật toán Âm lịch Việt Nam — Hồ Ngọc Đức
 * Triển khai đầy đủ: Âm lịch, Can Chi, Tiết Khí, Giờ Hoàng Đạo, Pha Mặt Trăng
 */
object LunarCalendarUtil {

    private const val PI = Math.PI
    const val TZ = 7.0 // Múi giờ Việt Nam (GMT+7)

    // ═══ JULIAN DAY ═══

    fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    fun jdToDate(jd: Int): Triple<Int, Int, Int> {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) {
            a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - b * 146097 / 4
        } else {
            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3) / 1461
        val e = c - 1461 * d / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return Triple(day, month, year)
    }

    // ═══ NEW MOON (SÓC) ═══

    fun getNewMoonDay(k: Int, timeZone: Double): Int {
        val T = k / 1236.85
        val T2 = T * T
        val T3 = T2 * T
        val dr = PI / 180

        var Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3
        Jd1 += 0.00033 * sin((166.56 + 132.87 * T - 0.009173 * T2) * dr)

        val M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3
        val Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3
        val F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3

        var C1 = (0.1734 - 0.000393 * T) * sin(M * dr) + 0.0021 * sin(2 * dr * M)
        C1 -= 0.4068 * sin(Mpr * dr) + 0.0161 * sin(dr * 2 * Mpr)
        C1 -= 0.0004 * sin(dr * 3 * Mpr)
        C1 += 0.0104 * sin(dr * 2 * F) - 0.0051 * sin(dr * (M + Mpr))
        C1 -= 0.0074 * sin(dr * (M - Mpr)) + 0.0004 * sin(dr * (2 * F + M))
        C1 -= 0.0004 * sin(dr * (2 * F - M)) - 0.0006 * sin(dr * (2 * F + Mpr))
        C1 += 0.0010 * sin(dr * (2 * F - Mpr)) + 0.0005 * sin(dr * (2 * Mpr + M))

        val deltat = if (T < -11) {
            0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
        } else {
            -0.000278 + 0.000265 * T + 0.000262 * T2
        }

        val JdNew = Jd1 + C1 - deltat
        return floor(JdNew + 0.5 + timeZone / 24).toInt()
    }

    // ═══ SUN LONGITUDE ═══

    fun getSunLongitude(jdn: Int, timeZone: Double): Int {
        val T = (jdn - 2451545.5 - timeZone / 24) / 36525
        val T2 = T * T
        val dr = PI / 180
        val M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        val L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * sin(dr * 2 * M) + 0.000290 * sin(dr * 3 * M)
        var L = L0 + DL
        L *= dr
        L -= PI * 2 * floor(L / (PI * 2))
        return floor(L / PI * 6).toInt()
    }

    fun getSunLongitudeDegree(jdn: Int, timeZone: Double): Double {
        val T = (jdn - 2451545.5 - timeZone / 24) / 36525
        val T2 = T * T
        val dr = PI / 180
        val M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        val L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * sin(dr * 2 * M) + 0.000290 * sin(dr * 3 * M)
        var L = L0 + DL
        L %= 360
        if (L < 0) L += 360.0
        return L
    }

    // ═══ LUNAR MONTH 11 ═══

    fun getLunarMonth11(yy: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = (off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone)
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    // ═══ LEAP MONTH OFFSET ═══

    fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k = ((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last = 0
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    // ═══ SOLAR → LUNAR ═══

    data class LunarResult(
        val lunarDay: Int,
        val lunarMonth: Int,
        val lunarYear: Int,
        val lunarLeap: Int
    )

    fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double = TZ): LunarResult {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = ((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        val lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = ((monthStart - a11) / 29.0).toInt()
        var lunarLeap = 0
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = 1
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            return LunarResult(lunarDay, lunarMonth, lunarYear - 1, lunarLeap)
        }
        return LunarResult(lunarDay, lunarMonth, lunarYear, lunarLeap)
    }

    // ═══ LUNAR → SOLAR ═══

    fun convertLunar2Solar(
        lunarDay: Int, lunarMonth: Int, lunarYear: Int,
        lunarLeap: Int, timeZone: Double = TZ
    ): Triple<Int, Int, Int> {
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1, timeZone)
            b11 = getLunarMonth11(lunarYear, timeZone)
        } else {
            a11 = getLunarMonth11(lunarYear, timeZone)
            b11 = getLunarMonth11(lunarYear + 1, timeZone)
        }
        var off = lunarMonth - 11
        if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11, timeZone)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) leapMonth += 12
            if (lunarLeap != 0 && lunarMonth != leapMonth) {
                return Triple(0, 0, 0)
            } else if (lunarLeap != 0 || off >= leapOff) {
                off += 1
            }
        }
        val k = (0.5 + (a11 - 2415021.076998695) / 29.530588853).toInt()
        val monthStart = getNewMoonDay(k + off, timeZone)
        return jdToDate(monthStart + lunarDay - 1)
    }
}
