package com.lichso.app.feature.points.domain

import java.time.LocalDate
import java.time.ZoneId

/**
 * Thin Clock abstraction to make PointsEngine testable (inject a FakeClock in unit tests).
 */
interface Clock {
    fun todayEpochDay(): Long
    fun nowEpochMillis(): Long
    fun yearMonth(): Int   // YYYYMM
}

class SystemClock : Clock {
    override fun todayEpochDay(): Long =
        LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    override fun nowEpochMillis(): Long = System.currentTimeMillis()

    override fun yearMonth(): Int {
        val d = LocalDate.now(ZoneId.systemDefault())
        return d.year * 100 + d.monthValue
    }
}
