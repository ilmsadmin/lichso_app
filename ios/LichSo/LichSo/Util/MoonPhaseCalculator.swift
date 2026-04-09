import Foundation

// ═══════════════════════════════════════════
// Pha Mặt Trăng
// Port chính xác từ Android Kotlin
// KHÔNG ĐƯỢC THAY ĐỔI THUẬT TOÁN
// ═══════════════════════════════════════════

struct MoonPhase {
    let icon: String
    let name: String
    let age: Double
}

enum MoonPhaseCalculator {

    static func getMoonPhase(dd: Int, mm: Int, yy: Int) -> MoonPhase {
        let jd = LunarCalendarUtil.jdFromDate(dd: dd, mm: mm, yy: yy)
        var k = Int((Double(jd) - 2415021.076998695) / 29.530588853)
        var nmJd = LunarCalendarUtil.getNewMoonDay(k: k, timeZone: LunarCalendarUtil.TZ)
        if nmJd > jd {
            k -= 1
            nmJd = LunarCalendarUtil.getNewMoonDay(k: k, timeZone: LunarCalendarUtil.TZ)
        }
        let moonAge = Double(jd - nmJd)

        switch moonAge {
        case ..<1.5:   return MoonPhase(icon: "●", name: "Trăng mới", age: moonAge)
        case ..<7.4:   return MoonPhase(icon: "◑", name: "Trăng lưỡi liềm đầu", age: moonAge)
        case ..<8.4:   return MoonPhase(icon: "◑", name: "Bán nguyệt đầu", age: moonAge)
        case ..<14.4:  return MoonPhase(icon: "◕", name: "Trăng khuyết đầu", age: moonAge)
        case ..<15.8:  return MoonPhase(icon: "○", name: "Trăng tròn", age: moonAge)
        case ..<21.8:  return MoonPhase(icon: "◔", name: "Trăng khuyết cuối", age: moonAge)
        case ..<22.8:  return MoonPhase(icon: "◐", name: "Bán nguyệt cuối", age: moonAge)
        case ..<28.5:  return MoonPhase(icon: "◐", name: "Trăng lưỡi liềm cuối", age: moonAge)
        default:       return MoonPhase(icon: "●", name: "Trăng mới", age: moonAge)
        }
    }
}
