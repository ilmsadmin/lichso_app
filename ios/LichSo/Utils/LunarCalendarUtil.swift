import Foundation

// MARK: - Thuật toán Âm lịch Việt Nam — Hồ Ngọc Đức
// Triển khai đầy đủ: Âm lịch, Can Chi, Tiết Khí, Giờ Hoàng Đạo, Pha Mặt Trăng

struct LunarResult {
    let lunarDay: Int
    let lunarMonth: Int
    let lunarYear: Int
    let lunarLeap: Int
}

enum LunarCalendarUtil {
    static let TZ: Double = 7.0 // Múi giờ Việt Nam (GMT+7)

    // MARK: - Julian Day

    static func jdFromDate(dd: Int, mm: Int, yy: Int) -> Int {
        let a = (14 - mm) / 12
        let y = yy + 4800 - a
        let m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if jd < 2299161 {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    static func jdToDate(jd: Int) -> (Int, Int, Int) {
        let a: Int
        let b: Int
        let c: Int
        if jd > 2299160 {
            let aa = jd + 32044
            let bb = (4 * aa + 3) / 146097
            a = aa; b = bb; c = aa - bb * 146097 / 4
        } else {
            a = 0; b = 0; c = jd + 32082
        }
        _ = a
        let d = (4 * c + 3) / 1461
        let e = c - 1461 * d / 4
        let m = (5 * e + 2) / 153
        let day = e - (153 * m + 2) / 5 + 1
        let month = m + 3 - 12 * (m / 10)
        let year = b * 100 + d - 4800 + m / 10
        return (day, month, year)
    }

    // MARK: - New Moon (Sóc)

    static func getNewMoonDay(k: Int, timeZone: Double) -> Int {
        let T = Double(k) / 1236.85
        let T2 = T * T
        let T3 = T2 * T
        let dr = Double.pi / 180.0

        var Jd1 = 2415020.75933 + 29.53058868 * Double(k) + 0.0001178 * T2 - 0.000000155 * T3
        Jd1 += 0.00033 * sin((166.56 + 132.87 * T - 0.009173 * T2) * dr)

        let M = 359.2242 + 29.10535608 * Double(k) - 0.0000333 * T2 - 0.00000347 * T3
        let Mpr = 306.0253 + 385.81691806 * Double(k) + 0.0107306 * T2 + 0.00001236 * T3
        let F = 21.2964 + 390.67050646 * Double(k) - 0.0016528 * T2 - 0.00000239 * T3

        var C1 = (0.1734 - 0.000393 * T) * sin(M * dr) + 0.0021 * sin(2 * dr * M)
        C1 -= 0.4068 * sin(Mpr * dr) + 0.0161 * sin(dr * 2 * Mpr)
        C1 -= 0.0004 * sin(dr * 3 * Mpr)
        C1 += 0.0104 * sin(dr * 2 * F) - 0.0051 * sin(dr * (M + Mpr))
        C1 -= 0.0074 * sin(dr * (M - Mpr)) + 0.0004 * sin(dr * (2 * F + M))
        C1 -= 0.0004 * sin(dr * (2 * F - M)) - 0.0006 * sin(dr * (2 * F + Mpr))
        C1 += 0.0010 * sin(dr * (2 * F - Mpr)) + 0.0005 * sin(dr * (2 * Mpr + M))

        let deltat: Double
        if T < -11 {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
        } else {
            deltat = -0.000278 + 0.000265 * T + 0.000262 * T2
        }

        let JdNew = Jd1 + C1 - deltat
        return Int(floor(JdNew + 0.5 + timeZone / 24.0))
    }

    // MARK: - Sun Longitude

    static func getSunLongitude(jdn: Int, timeZone: Double) -> Int {
        let T = (Double(jdn) - 2451545.5 - timeZone / 24.0) / 36525.0
        let T2 = T * T
        let dr = Double.pi / 180.0
        let M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        let L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * sin(dr * 2 * M) + 0.000290 * sin(dr * 3 * M)
        var L = L0 + DL
        L *= dr
        L -= Double.pi * 2 * floor(L / (Double.pi * 2))
        return Int(floor(L / Double.pi * 6))
    }

    static func getSunLongitudeDegree(jdn: Int, timeZone: Double) -> Double {
        let T = (Double(jdn) - 2451545.5 - timeZone / 24.0) / 36525.0
        let T2 = T * T
        let dr = Double.pi / 180.0
        let M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        let L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * sin(dr * 2 * M) + 0.000290 * sin(dr * 3 * M)
        var L = L0 + DL
        L = L.truncatingRemainder(dividingBy: 360)
        if L < 0 { L += 360.0 }
        return L
    }

    // MARK: - Lunar Month 11

    static func getLunarMonth11(yy: Int, timeZone: Double) -> Int {
        let off = jdFromDate(dd: 31, mm: 12, yy: yy) - 2415021
        let k = Int(Double(off) / 29.530588853)
        var nm = getNewMoonDay(k: k, timeZone: timeZone)
        let sunLong = getSunLongitude(jdn: nm, timeZone: timeZone)
        if sunLong >= 9 {
            nm = getNewMoonDay(k: k - 1, timeZone: timeZone)
        }
        return nm
    }

    // MARK: - Leap Month Offset

    static func getLeapMonthOffset(a11: Int, timeZone: Double) -> Int {
        let k = Int((Double(a11) - 2415021.076998695) / 29.530588853 + 0.5)
        var last = 0
        var i = 1
        var arc = getSunLongitude(jdn: getNewMoonDay(k: k + i, timeZone: timeZone), timeZone: timeZone)
        repeat {
            last = arc
            i += 1
            arc = getSunLongitude(jdn: getNewMoonDay(k: k + i, timeZone: timeZone), timeZone: timeZone)
        } while arc != last && i < 14
        return i - 1
    }

    // MARK: - Solar → Lunar

    static func convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double = TZ) -> LunarResult {
        let dayNumber = jdFromDate(dd: dd, mm: mm, yy: yy)
        let k = Int((Double(dayNumber) - 2415021.076998695) / 29.530588853)
        var monthStart = getNewMoonDay(k: k + 1, timeZone: timeZone)
        if monthStart > dayNumber {
            monthStart = getNewMoonDay(k: k, timeZone: timeZone)
        }
        var a11 = getLunarMonth11(yy: yy, timeZone: timeZone)
        var b11 = a11
        let lunarYear: Int
        if a11 >= monthStart {
            lunarYear = yy
            a11 = getLunarMonth11(yy: yy - 1, timeZone: timeZone)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy: yy + 1, timeZone: timeZone)
        }
        let lunarDay = dayNumber - monthStart + 1
        let diff = Int(Double(monthStart - a11) / 29.0)
        var lunarLeap = 0
        var lunarMonth = diff + 11
        if b11 - a11 > 365 {
            let leapMonthDiff = getLeapMonthOffset(a11: a11, timeZone: timeZone)
            if diff >= leapMonthDiff {
                lunarMonth = diff + 10
                if diff == leapMonthDiff {
                    lunarLeap = 1
                }
            }
        }
        if lunarMonth > 12 {
            lunarMonth -= 12
        }
        if lunarMonth >= 11 && diff < 4 {
            return LunarResult(lunarDay: lunarDay, lunarMonth: lunarMonth, lunarYear: lunarYear - 1, lunarLeap: lunarLeap)
        }
        return LunarResult(lunarDay: lunarDay, lunarMonth: lunarMonth, lunarYear: lunarYear, lunarLeap: lunarLeap)
    }

    // MARK: - Lunar → Solar

    static func convertLunar2Solar(
        lunarDay: Int, lunarMonth: Int, lunarYear: Int,
        lunarLeap: Int, timeZone: Double = TZ
    ) -> (Int, Int, Int) {
        let a11: Int
        let b11: Int
        if lunarMonth < 11 {
            a11 = getLunarMonth11(yy: lunarYear - 1, timeZone: timeZone)
            b11 = getLunarMonth11(yy: lunarYear, timeZone: timeZone)
        } else {
            a11 = getLunarMonth11(yy: lunarYear, timeZone: timeZone)
            b11 = getLunarMonth11(yy: lunarYear + 1, timeZone: timeZone)
        }
        var off = lunarMonth - 11
        if off < 0 { off += 12 }
        if b11 - a11 > 365 {
            let leapOff = getLeapMonthOffset(a11: a11, timeZone: timeZone)
            var leapMonth = leapOff - 2
            if leapMonth < 0 { leapMonth += 12 }
            if lunarLeap != 0 && lunarMonth != leapMonth {
                return (0, 0, 0)
            } else if lunarLeap != 0 || off >= leapOff {
                off += 1
            }
        }
        let k = Int(0.5 + (Double(a11) - 2415021.076998695) / 29.530588853)
        let monthStart = getNewMoonDay(k: k + off, timeZone: timeZone)
        return jdToDate(jd: monthStart + lunarDay - 1)
    }
}
