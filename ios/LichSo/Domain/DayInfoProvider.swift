import Foundation

// MARK: - Provides complete day information by composing all calculators

class DayInfoProvider {
    static let shared = DayInfoProvider()

    func getDayInfo(dd: Int, mm: Int, yy: Int) -> DayInfo {
        let jd = LunarCalendarUtil.jdFromDate(dd: dd, mm: mm, yy: yy)
        let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: dd, mm: mm, yy: yy)

        let yearCanChi = CanChiCalculator.getYearCanChi(lunarYear: lunar.lunarYear)
        let monthCanChi = CanChiCalculator.getMonthCanChi(lunarMonth: lunar.lunarMonth, lunarYear: lunar.lunarYear)
        let dayCanChi = CanChiCalculator.getDayCanChi(jd: jd)
        let dayOfWeek = CanChiCalculator.getDayOfWeek(jd: jd)
        let dayOfWeekIndex = CanChiCalculator.getDayOfWeekIndex(jd: jd)

        let moonPhase = MoonPhaseCalculator.getMoonPhase(dd: dd, mm: mm, yy: yy)
        let gioHD = GioHoangDaoCalculator.getGioHoangDao(jd: jd)
        let activities = DayActivityCalculator.getDayActivities(jd: jd, lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth)
        let huong = DayActivityCalculator.getHuongTot(jd: jd)
        let solarHoliday = HolidayUtil.getSolarHoliday(dd: dd, mm: mm)
        let lunarHoliday = HolidayUtil.getLunarHoliday(lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth)
        let tietKhi = TietKhiCalculator.getCurrentSolarTerm(dd: dd, mm: mm, yy: yy)
        let trucNgay = TrucNgayCalculator.getTrucNgay(jd: jd, lunarMonth: lunar.lunarMonth)
        let saoChieu = SaoChieuCalculator.getSaoChieu(jd: jd)

        // Hour Can Chi (current hour)
        let currentHour = Calendar.current.component(.hour, from: Date())
        let hourCanChi = getHourCanChi(jd: jd, hour: currentHour)

        // Day rating
        let dayRating = calculateDayRating(truc: trucNgay, sao: saoChieu, activities: activities)

        let lunarMonthName = lunar.lunarLeap == 1 ? "Tháng \(lunar.lunarMonth) nhuận" : "Tháng \(lunar.lunarMonth)"

        return DayInfo(
            solar: SolarDate(dd: dd, mm: mm, yy: yy),
            lunar: LunarDate(day: lunar.lunarDay, month: lunar.lunarMonth, year: lunar.lunarYear, leap: lunar.lunarLeap, monthName: lunarMonthName),
            jd: jd,
            dayOfWeek: dayOfWeek,
            dayOfWeekIndex: dayOfWeekIndex,
            yearCanChi: yearCanChi,
            monthCanChi: monthCanChi,
            dayCanChi: dayCanChi,
            hourCanChi: hourCanChi,
            moonPhase: MoonPhaseInfo(icon: moonPhase.icon, name: moonPhase.name, age: moonPhase.age),
            gioHoangDao: gioHD.map { GioHoangDaoInfo(name: $0.name, time: $0.time) },
            activities: DayActivitiesInfo(nenLam: activities.nenLam, khongNen: activities.khongNen, isXauDay: activities.isXauDay, isNguyetKy: activities.isNguyetKy, isTamNuong: activities.isTamNuong),
            huong: HuongTotInfo(thanTai: huong.thanTai, hyThan: huong.hyThan, hungThan: huong.hungThan),
            solarHoliday: solarHoliday,
            lunarHoliday: lunarHoliday,
            tietKhiInfo: TietKhiInfoModel(
                currentName: tietKhi.current?.name,
                nextName: tietKhi.next?.name,
                nextDd: tietKhi.next?.dd,
                nextMm: tietKhi.next?.mm,
                daysUntilNext: tietKhi.daysUntilNext
            ),
            trucNgayInfo: TrucNgayInfoModel(name: trucNgay.name, rating: trucNgay.rating),
            saoChieuInfo: SaoChieuInfoModel(name: saoChieu.name, rating: saoChieu.rating),
            dayRating: dayRating,
            isRam: lunar.lunarDay == 15,
            isMung1: lunar.lunarDay == 1
        )
    }

    func getCalendarDays(year: Int, month: Int, weekStartSunday: Bool = false) -> [CalendarDay] {
        let today = Date()
        let calendar = Calendar.current
        let todayComponents = calendar.dateComponents([.year, .month, .day], from: today)

        guard let firstDay = calendar.date(from: DateComponents(year: year, month: month, day: 1)) else { return [] }
        let weekday = calendar.component(.weekday, from: firstDay) // 1=Sun..7=Sat

        let offset: Int
        if weekStartSunday {
            offset = weekday - 1
        } else {
            offset = (weekday + 5) % 7 // Mon=0..Sun=6
        }

        let daysInMonth = calendar.range(of: .day, in: .month, for: firstDay)!.count
        let prevMonth = calendar.date(byAdding: .month, value: -1, to: firstDay)!
        let daysInPrevMonth = calendar.range(of: .day, in: .month, for: prevMonth)!.count
        let prevMonthValue = calendar.component(.month, from: prevMonth)
        let prevYearValue = calendar.component(.year, from: prevMonth)

        var days: [CalendarDay] = []

        // Previous month days
        for i in stride(from: offset - 1, through: 0, by: -1) {
            let d = daysInPrevMonth - i
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: d, mm: prevMonthValue, yy: prevYearValue)
            let lunStr = lunar.lunarDay == 1 ? "\(lunar.lunarDay)/\(lunar.lunarMonth)" : "\(lunar.lunarDay)"
            let date = calendar.date(from: DateComponents(year: prevYearValue, month: prevMonthValue, day: d))!
            let wd = calendar.component(.weekday, from: date)
            days.append(CalendarDay(
                solarDay: d, solarMonth: prevMonthValue, solarYear: prevYearValue,
                lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth,
                isCurrentMonth: false, isToday: false,
                isSunday: wd == 1, isSaturday: wd == 7,
                isHoliday: false, hasEvent: false, lunarDisplayText: lunStr
            ))
        }

        // Current month days
        for d in 1...daysInMonth {
            let date = calendar.date(from: DateComponents(year: year, month: month, day: d))!
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: d, mm: month, yy: year)
            let lunStr = lunar.lunarDay == 1 ? "\(lunar.lunarDay)/\(lunar.lunarMonth)" : "\(lunar.lunarDay)"
            let wd = calendar.component(.weekday, from: date)
            let sHol = HolidayUtil.getSolarHoliday(dd: d, mm: month)
            let lHol = HolidayUtil.getLunarHoliday(lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth)
            let hasEvent = lunar.lunarDay == 1 || lunar.lunarDay == 15 || sHol != nil || lHol != nil
            let isToday = todayComponents.year == year && todayComponents.month == month && todayComponents.day == d

            let jd = LunarCalendarUtil.jdFromDate(dd: d, mm: month, yy: year)
            let truc = TrucNgayCalculator.getTrucNgay(jd: jd, lunarMonth: lunar.lunarMonth)
            let sao = SaoChieuCalculator.getSaoChieu(jd: jd)
            let act = DayActivityCalculator.getDayActivities(jd: jd, lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth)
            let rating = calculateDayRating(truc: truc, sao: sao, activities: act)

            days.append(CalendarDay(
                solarDay: d, solarMonth: month, solarYear: year,
                lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth,
                isCurrentMonth: true,
                isToday: isToday,
                isSunday: wd == 1,
                isSaturday: wd == 7,
                isHoliday: sHol != nil || lHol != nil,
                hasEvent: hasEvent,
                lunarDisplayText: lunStr,
                dayRatingLabel: rating.label
            ))
        }

        // Next month days
        let totalCells = offset + daysInMonth
        let remaining = totalCells % 7 == 0 ? 0 : 7 - (totalCells % 7)
        let nextMonth = calendar.date(byAdding: .month, value: 1, to: firstDay)!
        let nextMonthValue = calendar.component(.month, from: nextMonth)
        let nextYearValue = calendar.component(.year, from: nextMonth)

        for d in 1...max(remaining, 1) {
            if d > remaining { break }
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: d, mm: nextMonthValue, yy: nextYearValue)
            let lunStr = lunar.lunarDay == 1 ? "\(lunar.lunarDay)/\(lunar.lunarMonth)" : "\(lunar.lunarDay)"
            let date = calendar.date(from: DateComponents(year: nextYearValue, month: nextMonthValue, day: d))!
            let wd = calendar.component(.weekday, from: date)
            days.append(CalendarDay(
                solarDay: d, solarMonth: nextMonthValue, solarYear: nextYearValue,
                lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth,
                isCurrentMonth: false, isToday: false,
                isSunday: wd == 1, isSaturday: wd == 7,
                isHoliday: false, hasEvent: false, lunarDisplayText: lunStr
            ))
        }

        return days
    }

    func getUpcomingEvents(dd: Int, mm: Int, yy: Int) -> [UpcomingEvent] {
        var events: [UpcomingEvent] = []
        let todayJd = LunarCalendarUtil.jdFromDate(dd: dd, mm: mm, yy: yy)

        for i in 0..<14 {
            let checkJd = todayJd + i
            let (cd, cm, _) = LunarCalendarUtil.jdToDate(jd: checkJd)
            let lunar = LunarCalendarUtil.convertSolar2Lunar(dd: cd, mm: cm, yy: yy)
            let timeStr: String
            switch i {
            case 0: timeStr = "Hôm nay"
            case 1: timeStr = "Ngày mai · \(cd)/\(cm)"
            default: timeStr = "\(cd)/\(cm)"
            }

            if lunar.lunarDay == 15 {
                events.append(UpcomingEvent(title: "Rằm tháng \(lunar.lunarMonth) Âm lịch", timeLabel: timeStr, tag: "Âm lịch", colorType: .teal))
            }
            if lunar.lunarDay == 1 {
                events.append(UpcomingEvent(title: "Mùng 1 tháng \(lunar.lunarMonth) Âm lịch", timeLabel: timeStr, tag: "Âm lịch", colorType: .teal))
            }
            if let sHol = HolidayUtil.getSolarHoliday(dd: cd, mm: cm) {
                events.append(UpcomingEvent(title: sHol, timeLabel: timeStr, tag: "Ngày lễ", colorType: .red))
            }
            if let lHol = HolidayUtil.getLunarHoliday(lunarDay: lunar.lunarDay, lunarMonth: lunar.lunarMonth),
               lunar.lunarDay != 15, lunar.lunarDay != 1 {
                events.append(UpcomingEvent(title: lHol, timeLabel: timeStr, tag: "Âm lịch", colorType: .gold))
            }

            if events.count >= 5 { break }
        }

        // Add upcoming solar term
        let tkInfo = TietKhiCalculator.getCurrentSolarTerm(dd: dd, mm: mm, yy: yy)
        if let nd = tkInfo.next, tkInfo.daysUntilNext <= 14 {
            let timeStr: String
            switch tkInfo.daysUntilNext {
            case 0: timeStr = "Hôm nay"
            case 1: timeStr = "Ngày mai · \(nd.dd)/\(nd.mm)"
            default: timeStr = "\(nd.dd)/\(nd.mm) · Bắt đầu tiết khí mới"
            }
            events.append(UpcomingEvent(title: "Tiết \(nd.name)", timeLabel: timeStr, tag: "Tiết khí", colorType: .red))
        }

        return Array(events.prefix(4))
    }

    // MARK: - Private Helpers

    private func getHourCanChi(jd: Int, hour: Int) -> String {
        let can = CanChiCalculator.THIEN_CAN
        let chi = CanChiCalculator.DIA_CHI
        let chiIdx: Int
        switch hour {
        case 23, 0: chiIdx = 0
        case 1, 2: chiIdx = 1
        case 3, 4: chiIdx = 2
        case 5, 6: chiIdx = 3
        case 7, 8: chiIdx = 4
        case 9, 10: chiIdx = 5
        case 11, 12: chiIdx = 6
        case 13, 14: chiIdx = 7
        case 15, 16: chiIdx = 8
        case 17, 18: chiIdx = 9
        case 19, 20: chiIdx = 10
        case 21, 22: chiIdx = 11
        default: chiIdx = 0
        }
        let dayCan = ((jd + 9) % 10 + 10) % 10
        let canIdx = (dayCan * 2 + chiIdx) % 10
        return "\(can[canIdx]) \(chi[chiIdx])"
    }

    private func calculateDayRating(truc: TrucNgayInfo, sao: SaoChieuInfo, activities: DayActivities) -> DayRatingInfo {
        var score = 50

        switch truc.rating {
        case "Tốt": score += 20
        case "Xấu": score -= 15
        default: break
        }

        switch sao.rating {
        case "Tốt": score += 20
        case "Xấu": score -= 15
        default: break
        }

        if activities.isNguyetKy { score -= 15 }
        if activities.isTamNuong { score -= 10 }
        score += min(activities.nenLam.count * 2, 10)
        score = max(10, min(100, score))

        let label: String
        switch score {
        case 80...: label = "Rất tốt"
        case 60...: label = "Tốt"
        case 40...: label = "Trung bình"
        default: label = "Xấu"
        }

        return DayRatingInfo(label: label, percent: score)
    }
}
