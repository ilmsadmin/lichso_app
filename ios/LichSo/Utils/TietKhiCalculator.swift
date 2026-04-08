import Foundation

// MARK: - Tính Tiết Khí (24 Solar Terms)

struct SolarTerm {
    let name: String
    let longitude: Int
    let jd: Int
    let dd: Int
    let mm: Int
    let yy: Int
}

struct SolarTermInfo {
    let current: SolarTerm?
    let next: SolarTerm?
    let daysUntilNext: Int
}

enum TietKhiCalculator {
    private static let TERM_NAMES: [(String, Int)] = [
        ("Xuân Phân", 0), ("Thanh Minh", 15), ("Cốc Vũ", 30),
        ("Lập Hạ", 45), ("Tiểu Mãn", 60), ("Mang Chủng", 75),
        ("Hạ Chí", 90), ("Tiểu Thử", 105), ("Đại Thử", 120),
        ("Lập Thu", 135), ("Xử Thử", 150), ("Bạch Lộ", 165),
        ("Thu Phân", 180), ("Hàn Lộ", 195), ("Sương Giáng", 210),
        ("Lập Đông", 225), ("Tiểu Tuyết", 240), ("Đại Tuyết", 255),
        ("Đông Chí", 270), ("Tiểu Hàn", 285), ("Đại Hàn", 300),
        ("Lập Xuân", 315), ("Vũ Thủy", 330), ("Kinh Trập", 345)
    ]

    private static func findSolarTermDate(year: Int, termLongitude: Int) -> Int {
        let daysPerDegree = 365.25 / 360.0
        let marchEquinox = LunarCalendarUtil.jdFromDate(dd: 20, mm: 3, yy: year)
        var diff = Double(termLongitude)
        if diff < 0 { diff += 360.0 }
        let estJd = marchEquinox + Int(floor(diff * daysPerDegree))

        for jd in (estJd - 5)...(estJd + 5) {
            let l1 = LunarCalendarUtil.getSunLongitudeDegree(jdn: jd, timeZone: LunarCalendarUtil.TZ)
            var l2 = LunarCalendarUtil.getSunLongitudeDegree(jdn: jd + 1, timeZone: LunarCalendarUtil.TZ)
            if l2 < l1 { l2 += 360.0 }
            let target = Double(termLongitude)
            if l1 <= target && target < l2 { return jd }
            if l1 <= target + 360 && target + 360 < l2 { return jd }
        }
        return estJd
    }

    static func getAllSolarTerms(year: Int) -> [SolarTerm] {
        var terms: [SolarTerm] = []
        for (name, lon) in TERM_NAMES {
            let jd = findSolarTermDate(year: year, termLongitude: lon)
            let (dd, mm, yy) = LunarCalendarUtil.jdToDate(jd: jd)
            terms.append(SolarTerm(name: name, longitude: lon, jd: jd, dd: dd, mm: mm, yy: yy))
        }
        terms.sort { $0.jd < $1.jd }
        return terms
    }

    static func getCurrentSolarTerm(dd: Int, mm: Int, yy: Int) -> SolarTermInfo {
        let todayJd = LunarCalendarUtil.jdFromDate(dd: dd, mm: mm, yy: yy)
        let prevTerms = getAllSolarTerms(year: yy - 1)
        let terms = getAllSolarTerms(year: yy)
        let nextTerms = getAllSolarTerms(year: yy + 1)
        let allTerms = (prevTerms + terms + nextTerms).sorted { $0.jd < $1.jd }

        var current: SolarTerm?
        var next: SolarTerm?

        for i in 0..<(allTerms.count - 1) {
            if todayJd >= allTerms[i].jd && todayJd < allTerms[i + 1].jd {
                current = allTerms[i]
                next = allTerms[i + 1]
                break
            }
        }

        return SolarTermInfo(
            current: current,
            next: next,
            daysUntilNext: next != nil ? next!.jd - todayJd : 0
        )
    }
}
