import Foundation
import SwiftUI

// MARK: - Lunar Calendar Engine (Vietnamese)

struct LunarCalendarEngine {

    // Can Chi arrays
    static let canNames = ["Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"]
    static let chiNames = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"]
    static let chiHour  = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"]
    static let monthNames = ["Giêng","Hai","Ba","Tư","Năm","Sáu","Bảy","Tám","Chín","Mười","Một","Chạp"]

    static func canChiYear(_ year: Int) -> String {
        let can = canNames[(year + 6) % 10]
        let chi = chiNames[(year + 8) % 12]
        return "\(can) \(chi)"
    }

    static func canChiMonth(_ month: Int, _ year: Int) -> String {
        let can = canNames[(year * 12 + month + 3) % 10]
        let chi = chiNames[(month + 1) % 12]
        return "\(can) \(chi)"
    }

    static func canChiDay(_ jd: Int) -> String {
        let can = canNames[(jd + 9) % 10]
        let chi = chiNames[(jd + 1) % 12]
        return "\(can) \(chi)"
    }

    static func canChiHour(_ h: Int) -> String {
        let gioIndex = h / 2
        let can = canNames[gioIndex % 10]
        let chi = chiHour[gioIndex % 12]
        return "\(can) \(chi)"
    }

    static func gioName(_ hour: Int) -> String {
        switch hour {
        case 23, 0: return "Tý"
        case 1, 2:  return "Sửu"
        case 3, 4:  return "Dần"
        case 5, 6:  return "Mão"
        case 7, 8:  return "Thìn"
        case 9, 10: return "Tỵ"
        case 11, 12: return "Ngọ"
        case 13, 14: return "Mùi"
        case 15, 16: return "Thân"
        case 17, 18: return "Dậu"
        case 19, 20: return "Tuất"
        case 21, 22: return "Hợi"
        default: return ""
        }
    }

    // Julian Day Number
    static func jdFromDate(_ dd: Int, _ mm: Int, _ yy: Int) -> Int {
        var y = yy; var m = mm; let d = dd
        if m < 3 { y -= 1; m += 12 }
        let a = y / 100
        let b = 2 - a + a / 4
        return Int(365.25 * Double(y + 4716)) + Int(30.6001 * Double(m + 1)) + d + b - 1524
    }

    static func jdToDate(_ jd: Int) -> (d: Int, m: Int, y: Int) {
        var l = jd + 68569
        let n = 4 * l / 146097
        l = l - (146097 * n + 3) / 4
        let i = 4000 * (l + 1) / 1461001
        l = l - 1461 * i / 4 + 31
        let j = 80 * l / 2447
        let d = l - 2447 * j / 80
        l = j / 11
        let m = j + 2 - 12 * l
        let y = 100 * (n - 49) + i + l
        return (d, m, y)
    }

    // New moon calculation (simplified)
    static func newMoon(_ k: Int) -> Int {
        let T = Double(k) / 1236.85
        let T2 = T * T; let T3 = T2 * T
        var Jd = 2415020.75933 + 29.53058868 * Double(k)
            + 0.0001178 * T2 - 0.000000155 * T3
            + 0.00033 * sin((166.56 + 132.87 * T - 0.009173 * T2) * .pi / 180)
        let M = 359.2242 + 29.10535608 * Double(k) - 0.0000333 * T2 - 0.00000347 * T3
        let Mpr = 306.0253 + 385.81691806 * Double(k) + 0.0107306 * T2 + 0.00001236 * T3
        let F = 21.2964 + 390.67050646 * Double(k) - 0.0016528 * T2 - 0.00000239 * T3
        let deg = Double.pi / 180
        let E1 = (0.1734 - 0.000393 * T) * sin(M * deg) + 0.0021 * sin(2 * M * deg)
                - 0.4068 * sin(Mpr * deg) + 0.0161 * sin(2 * Mpr * deg) - 0.0004 * sin(3 * Mpr * deg)
                + 0.0104 * sin(2 * F * deg) - 0.0051 * sin((M + Mpr) * deg)
                - 0.0074 * sin((M - Mpr) * deg) + 0.0004 * sin((2 * F + M) * deg)
                - 0.0004 * sin((2 * F - M) * deg) - 0.0006 * sin((2 * F + Mpr) * deg)
                + 0.0010 * sin((2 * F - Mpr) * deg) + 0.0005 * sin((M + 2 * Mpr) * deg)
        Jd += E1
        return Int(Jd + 0.5 + 7.0 / 24.0)
    }

    static func sunLongitude(_ jdn: Int) -> Double {
        let T = (Double(jdn) - 2451545.0) / 36525.0
        let T2 = T * T
        var dr = Double.pi / 180
        var M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T2 * T
        var L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T2
        let DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * sin(dr * M)
                + (0.019993 - 0.000101 * T) * sin(dr * 2 * M)
                + 0.000290 * sin(dr * 3 * M)
        var L = L0 + DL
        L = L - 360 * floor(L / 360)
        return floor(L / 30)
    }

    static func getLunarMonth11(_ yy: Int) -> Int {
        let off = jdFromDate(31, 12, yy) - 2415021
        let k = Int(Double(off) / 29.530588853)
        var nm = newMoon(k)
        let sunLong = sunLongitude(nm)
        if sunLong >= 9 { nm = newMoon(k - 1) }
        return nm
    }

    static func getLeapMonthOffset(_ a11: Int) -> Int {
        let k = Int((Double(a11) - 2415021.076998695) / 29.530588853 + 0.5)
        var last: Double = 0; var i = 1; var arc: Double = sunLongitude(newMoon(k + 1))
        repeat {
            last = arc
            i += 1
            arc = sunLongitude(newMoon(k + i))
        } while arc != last && i < 14
        return i - 1
    }

    static func solar2Lunar(_ dd: Int, _ mm: Int, _ yy: Int) -> LunarDate {
        let dayNumber = jdFromDate(dd, mm, yy)
        let k = Int((Double(dayNumber) - 2415021.076998695) / 29.530588853)
        var monthStart = newMoon(k + 1)
        if monthStart > dayNumber { monthStart = newMoon(k) }

        var a11 = getLunarMonth11(yy)
        var b11 = a11
        var lunarYear: Int
        if a11 >= monthStart {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1)
        }

        let lunarDay = dayNumber - monthStart + 1
        let diff = Int(Double(monthStart - a11) / 29.0)
        var lunarLeap = 0
        var lunarMonth = diff + 11
        if b11 - a11 > 365 {
            let leapMonthDiff = getLeapMonthOffset(a11)
            if diff >= leapMonthDiff {
                lunarMonth = diff + 10
                if diff == leapMonthDiff { lunarLeap = 1 }
            }
        }
        if lunarMonth > 12 { lunarMonth -= 12 }
        if lunarMonth >= 11 && diff < 4 { lunarYear -= 1 }

        let idx = (lunarMonth - 1 + 12) % 12
        let mName = monthNames[idx]
        return LunarDate(day: lunarDay, month: lunarMonth, year: lunarYear, leap: lunarLeap, monthName: "Tháng \(mName)")
    }

    // MARK: - Giờ Hoàng Đạo
    static let hoangDaoTable: [[Int]] = [
        [0,0,1,0,1,1,0,1,0,1,1,0],
        [1,0,0,1,0,1,0,1,1,0,1,0],
        [1,1,0,0,1,0,1,0,1,0,1,1],
        [0,1,0,1,0,0,1,0,1,1,0,1],
        [1,0,1,1,0,1,0,0,1,0,1,0],
        [0,1,0,1,1,0,1,1,0,0,1,0],
    ]

    static let gioNames = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"]
    static let gioTimes = ["23-01","01-03","03-05","05-07","07-09","09-11","11-13","13-15","15-17","17-19","19-21","21-23"]

    static func gioHoangDao(jd: Int) -> [GioHoangDaoInfo] {
        let chiIdx = (jd + 1) % 12
        let row = hoangDaoTable[chiIdx % 6]
        var result: [GioHoangDaoInfo] = []
        for (i, val) in row.enumerated() {
            if val == 1 {
                result.append(GioHoangDaoInfo(name: gioNames[i], time: gioTimes[i]))
            }
        }
        return result
    }

    // MARK: - Day Activities
    static let nenLamByDay: [[String]] = [
        ["Cúng tế", "Gặp bạn bè", "Đi du lịch"],
        ["Xuất hành", "Mua sắm", "Ký kết hợp đồng"],
        ["Cưới hỏi", "Khai trương", "Động thổ"],
        ["Học hành", "Hội họp", "Làm việc lớn"],
        ["Cầu an", "Sửa nhà", "Trồng cây"],
        ["Xuất hành", "Gặp đối tác", "Đi xa"],
        ["Cúng tế", "Thăm người thân", "Nghỉ ngơi"],
    ]
    static let khongNenByDay: [[String]] = [
        ["Khai trương", "Ký kết", "Đầu tư"],
        ["Cưới hỏi", "Chuyển nhà", "Phẫu thuật"],
        ["Xuất hành", "Đi xa", "Vay tiền"],
        ["Cúng tế", "Chôn cất", "Phá thổ"],
        ["Khai trương", "Ký kết", "Mở cửa hàng"],
        ["Cưới hỏi", "Tiệc tùng", "Phẫu thuật"],
        ["Đi xa", "Vay mượn", "Đầu tư mạo hiểm"],
    ]

    // MARK: - Hướng tốt
    static let huongTot = [
        HuongTotInfo(thanTai: "Đông Bắc", hyThan: "Tây Nam", hungThan: "Tây Bắc"),
        HuongTotInfo(thanTai: "Đông", hyThan: "Tây", hungThan: "Nam"),
        HuongTotInfo(thanTai: "Nam", hyThan: "Bắc", hungThan: "Đông"),
        HuongTotInfo(thanTai: "Tây Nam", hyThan: "Đông Bắc", hungThan: "Đông Nam"),
        HuongTotInfo(thanTai: "Tây", hyThan: "Đông", hungThan: "Bắc"),
        HuongTotInfo(thanTai: "Tây Bắc", hyThan: "Đông Nam", hungThan: "Tây"),
        HuongTotInfo(thanTai: "Bắc", hyThan: "Nam", hungThan: "Đông Bắc"),
    ]

    // MARK: - Tiết Khí (simplified)
    static let tietKhiNames = [
        "Xuân Phân","Thanh Minh","Cốc Vũ","Lập Hạ","Tiểu Mãn","Mang Chủng",
        "Hạ Chí","Tiểu Thử","Đại Thử","Lập Thu","Xử Thử","Bạch Lộ",
        "Thu Phân","Hàn Lộ","Sương Giáng","Lập Đông","Tiểu Tuyết","Đại Tuyết",
        "Đông Chí","Tiểu Hàn","Đại Hàn","Lập Xuân","Vũ Thủy","Kinh Trập"
    ]

    static func tietKhiInfo(_ dd: Int, _ mm: Int) -> TietKhiInfo {
        let idx = ((mm - 1) * 2) % 24
        let name = tietKhiNames[idx]
        let nextIdx = (idx + 1) % 24
        let nextName = tietKhiNames[nextIdx]
        let nextDay = dd < 15 ? 15 : (mm < 12 ? 1 : 1)
        let nextMonth = dd < 15 ? mm : (mm < 12 ? mm + 1 : 1)
        let daysLeft = max(0, (dd < 15 ? 15 - dd : 30 - dd))
        return TietKhiInfo(currentName: dd == 5 || dd == 20 ? name : nil,
                           nextName: nextName, nextDd: nextDay,
                           nextMm: nextMonth, daysUntilNext: daysLeft)
    }

    // MARK: - Moon phase
    static func moonPhase(lunarDay: Int) -> MoonPhaseInfo {
        switch lunarDay {
        case 1:  return MoonPhaseInfo(icon: "🌑", name: "Sóc (Trăng Mới)", age: 0)
        case 2...6:  return MoonPhaseInfo(icon: "🌒", name: "Trăng lưỡi liềm", age: Double(lunarDay))
        case 7...9:  return MoonPhaseInfo(icon: "🌓", name: "Thượng huyền", age: Double(lunarDay))
        case 10...14: return MoonPhaseInfo(icon: "🌔", name: "Trăng khuyết tròn", age: Double(lunarDay))
        case 15: return MoonPhaseInfo(icon: "🌕", name: "Vọng (Trăng Tròn)", age: 15)
        case 16...20: return MoonPhaseInfo(icon: "🌖", name: "Trăng khuyết xế", age: Double(lunarDay))
        case 21...23: return MoonPhaseInfo(icon: "🌗", name: "Hạ huyền", age: Double(lunarDay))
        default: return MoonPhaseInfo(icon: "🌘", name: "Trăng hạ huyền", age: Double(lunarDay))
        }
    }

    // MARK: - Truc Ngay
    static let trucNgayNames = ["Kiến","Trừ","Mãn","Bình","Định","Chấp","Phá","Nguy","Thành","Thu","Khai","Bế"]
    static let trucNgayRatings = ["Tốt","Xấu","Tốt","Tốt","Tốt","Tốt","Xấu","Xấu","Tốt","Tốt","Tốt","Xấu"]

    // MARK: - Solar Holidays
    static func solarHoliday(_ dd: Int, _ mm: Int) -> String? {
        switch (dd, mm) {
        case (1, 1): return "Tết Dương Lịch"
        case (30, 4): return "Ngày Giải Phóng"
        case (1, 5): return "Quốc Tế Lao Động"
        case (2, 9): return "Quốc Khánh"
        case (20, 11): return "Ngày Nhà Giáo"
        case (8, 3): return "Quốc Tế Phụ Nữ"
        case (1, 6): return "Quốc Tế Thiếu Nhi"
        default: return nil
        }
    }

    static func lunarHoliday(_ ld: Int, _ lm: Int) -> String? {
        switch (ld, lm) {
        case (1, 1): return "Mùng 1 Tết Nguyên Đán"
        case (2, 1): return "Mùng 2 Tết"
        case (3, 1): return "Mùng 3 Tết"
        case (15, 1): return "Rằm Tháng Giêng (Tết Nguyên Tiêu)"
        case (10, 3): return "Giỗ Tổ Hùng Vương"
        case (15, 4): return "Phật Đản"
        case (15, 7): return "Lễ Vu Lan"
        case (15, 8): return "Rằm Tháng Tám (Tết Trung Thu)"
        case (23, 12): return "Táo Quân Chầu Trời"
        default: return nil
        }
    }

    // MARK: - Build Full DayInfo
    static func buildDayInfo(dd: Int, mm: Int, yy: Int) -> DayInfo {
        let jd = jdFromDate(dd, mm, yy)
        let lunar = solar2Lunar(dd, mm, yy)
        let dow = ["Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy","Chủ Nhật"]
        let dowIdx = ((jd + 1) % 7 + 7) % 7 // 0=Mon..6=Sun
        let dayCanChiStr = canChiDay(jd)
        let yearCanChiStr = canChiYear(lunar.year)
        let monthCanChiStr = canChiMonth(lunar.month, lunar.year)
        let hourCanChiStr = canChiHour(Calendar.current.component(.hour, from: Date()))
        let gioHD = gioHoangDao(jd: jd)
        let actIdx = (jd + dowIdx) % 7
        let activities = DayActivitiesInfo(
            nenLam: nenLamByDay[actIdx],
            khongNen: khongNenByDay[actIdx],
            isXauDay: false, isNguyetKy: false, isTamNuong: false
        )
        let huong = huongTot[actIdx]
        let tk = tietKhiInfo(dd, mm)
        let moon = moonPhase(lunarDay: lunar.day)
        let trucIdx = (jd + 1) % 12
        let truc = TrucNgayInfo(name: trucNgayNames[trucIdx], rating: trucNgayRatings[trucIdx])
        let sao = SaoChieuInfo(name: "Thiên Đức", description: "Đại cát")
        let rating = DayRatingInfo(score: 4, label: "Ngày tốt")
        let sHoliday = solarHoliday(dd, mm)
        let lHoliday = lunarHoliday(lunar.day, lunar.month)

        return DayInfo(
            solar: SolarDate(dd: dd, mm: mm, yy: yy),
            lunar: lunar,
            jd: jd,
            dayOfWeek: dow[min(dowIdx, 6)],
            dayOfWeekIndex: dowIdx,
            yearCanChi: yearCanChiStr,
            monthCanChi: monthCanChiStr,
            dayCanChi: dayCanChiStr,
            hourCanChi: hourCanChiStr,
            moonPhase: moon,
            gioHoangDao: gioHD,
            activities: activities,
            huong: huong,
            solarHoliday: sHoliday,
            lunarHoliday: lHoliday,
            tietKhi: tk,
            trucNgay: truc,
            saoChieu: sao,
            dayRating: rating,
            isRam: lunar.day == 15,
            isMung1: lunar.day == 1
        )
    }

    // MARK: - Calendar Grid
    static func calendarDays(month: Int, year: Int) -> [CalendarDay] {
        var days: [CalendarDay] = []
        let cal = Calendar.current
        var comps = DateComponents(year: year, month: month, day: 1)
        guard let firstDay = cal.date(from: comps) else { return [] }
        let firstWeekday = (cal.component(.weekday, from: firstDay) + 5) % 7 // Mon=0
        let daysInMonth = cal.range(of: .day, in: .month, for: firstDay)?.count ?? 30

        // Previous month filler
        if firstWeekday > 0 {
            let prevMonth = month == 1 ? 12 : month - 1
            let prevYear = month == 1 ? year - 1 : year
            comps.month = prevMonth; comps.year = prevYear
            let prevMonthDays = cal.range(of: .day, in: .month, for: cal.date(from: comps)!)?.count ?? 30
            for d in (prevMonthDays - firstWeekday + 1)...prevMonthDays {
                let lunar = solar2Lunar(d, prevMonth, prevYear)
                let jd = jdFromDate(d, prevMonth, prevYear)
                let dow = ((jd + 1) % 7 + 7) % 7
                days.append(CalendarDay(
                    solarDay: d, solarMonth: prevMonth, solarYear: prevYear,
                    lunarDay: lunar.day, lunarMonth: lunar.month,
                    isCurrentMonth: false, isToday: false,
                    isSunday: dow == 6, isSaturday: dow == 5,
                    isHoliday: false, hasEvent: false,
                    lunarDisplayText: "\(lunar.day)"
                ))
            }
        }

        // Current month
        let today = cal.dateComponents([.day, .month, .year], from: Date())
        for d in 1...daysInMonth {
            let lunar = solar2Lunar(d, month, year)
            let jd = jdFromDate(d, month, year)
            let dow = ((jd + 1) % 7 + 7) % 7
            let isToday = d == today.day && month == today.month && year == today.year
            let lunarText = lunar.day == 1 ? "1/\(lunar.month)" : "\(lunar.day)"
            days.append(CalendarDay(
                solarDay: d, solarMonth: month, solarYear: year,
                lunarDay: lunar.day, lunarMonth: lunar.month,
                isCurrentMonth: true, isToday: isToday,
                isSunday: dow == 6, isSaturday: dow == 5,
                isHoliday: solarHoliday(d, month) != nil || lunarHoliday(lunar.day, lunar.month) != nil,
                hasEvent: false,
                lunarDisplayText: lunarText
            ))
        }

        // Next month filler
        let remaining = 42 - days.count
        let nextMonth = month == 12 ? 1 : month + 1
        let nextYear = month == 12 ? year + 1 : year
        for d in 1...max(1, remaining) {
            let lunar = solar2Lunar(d, nextMonth, nextYear)
            let jd = jdFromDate(d, nextMonth, nextYear)
            let dow = ((jd + 1) % 7 + 7) % 7
            days.append(CalendarDay(
                solarDay: d, solarMonth: nextMonth, solarYear: nextYear,
                lunarDay: lunar.day, lunarMonth: lunar.month,
                isCurrentMonth: false, isToday: false,
                isSunday: dow == 6, isSaturday: dow == 5,
                isHoliday: false, hasEvent: false,
                lunarDisplayText: "\(lunar.day)"
            ))
        }
        return days
    }
}
