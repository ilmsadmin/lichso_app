package com.lichso.app.util

import kotlin.math.floor

/**
 * Pha Mặt Trăng
 */
object MoonPhaseCalculator {

    data class MoonPhase(val icon: String, val name: String, val age: Double)

    fun getMoonPhase(dd: Int, mm: Int, yy: Int): MoonPhase {
        val jd = LunarCalendarUtil.jdFromDate(dd, mm, yy)
        var k = ((jd - 2415021.076998695) / 29.530588853).toInt()
        var nmJd = LunarCalendarUtil.getNewMoonDay(k, LunarCalendarUtil.TZ)
        if (nmJd > jd) {
            k--
            nmJd = LunarCalendarUtil.getNewMoonDay(k, LunarCalendarUtil.TZ)
        }
        val moonAge = (jd - nmJd).toDouble()

        return when {
            moonAge < 1.5 -> MoonPhase("●", "Trăng mới", moonAge)
            moonAge < 7.4 -> MoonPhase("◑", "Trăng lưỡi liềm đầu", moonAge)
            moonAge < 8.4 -> MoonPhase("◑", "Bán nguyệt đầu", moonAge)
            moonAge < 14.4 -> MoonPhase("◕", "Trăng khuyết đầu", moonAge)
            moonAge < 15.8 -> MoonPhase("○", "Trăng tròn", moonAge)
            moonAge < 21.8 -> MoonPhase("◔", "Trăng khuyết cuối", moonAge)
            moonAge < 22.8 -> MoonPhase("◐", "Bán nguyệt cuối", moonAge)
            moonAge < 28.5 -> MoonPhase("◐", "Trăng lưỡi liềm cuối", moonAge)
            else -> MoonPhase("●", "Trăng mới", moonAge)
        }
    }
}
