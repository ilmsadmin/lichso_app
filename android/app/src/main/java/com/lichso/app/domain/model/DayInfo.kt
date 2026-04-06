package com.lichso.app.domain.model

/**
 * Tổng hợp thông tin ngày — Domain Model
 */
data class DayInfo(
    val solar: SolarDate,
    val lunar: LunarDate,
    val jd: Int,
    val dayOfWeek: String,
    val dayOfWeekIndex: Int, // 0=Mon..6=Sun
    val yearCanChi: String,
    val monthCanChi: String,
    val dayCanChi: String,
    val hourCanChi: String,
    val moonPhase: MoonPhaseInfo,
    val gioHoangDao: List<GioHoangDaoInfo>,
    val activities: DayActivitiesInfo,
    val huong: HuongTotInfo,
    val solarHoliday: String?,
    val lunarHoliday: String?,
    val tietKhi: TietKhiInfo,
    val trucNgay: TrucNgayInfo,
    val saoChieu: SaoChieuInfo,
    val dayRating: DayRatingInfo,
    val isRam: Boolean,
    val isMung1: Boolean
)

data class SolarDate(val dd: Int, val mm: Int, val yy: Int)

data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val leap: Int,
    val monthName: String
)

data class MoonPhaseInfo(val icon: String, val name: String, val age: Double)

data class GioHoangDaoInfo(val name: String, val time: String)

data class DayActivitiesInfo(
    val nenLam: List<String>,
    val khongNen: List<String>,
    val isXauDay: Boolean,
    val isNguyetKy: Boolean,
    val isTamNuong: Boolean
)

data class HuongTotInfo(
    val thanTai: String,
    val hyThan: String,
    val hungThan: String
)

data class TietKhiInfo(
    val currentName: String?,
    val nextName: String?,
    val nextDd: Int?,
    val nextMm: Int?,
    val daysUntilNext: Int
)

/**
 * Calendar day cell for the grid
 */
data class CalendarDay(
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val lunarDay: Int,
    val lunarMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSunday: Boolean,
    val isSaturday: Boolean,
    val isHoliday: Boolean,
    val hasEvent: Boolean,
    val lunarDisplayText: String, // "15" or "1/2" for first day of lunar month
    val dayRatingLabel: String = "" // "Rất tốt", "Tốt", "Trung bình", "Xấu"
)

/**
 * Upcoming event
 */
data class UpcomingEvent(
    val title: String,
    val timeLabel: String,
    val tag: String,
    val colorType: EventColor
)

enum class EventColor { GOLD, TEAL, RED }

data class TrucNgayInfo(
    val name: String,
    val rating: String // "Tốt", "Xấu", "Trung bình"
)

data class SaoChieuInfo(
    val name: String,
    val rating: String // "Tốt", "Xấu", "Trung bình"
)

data class DayRatingInfo(
    val label: String, // "Rất tốt", "Tốt", "Trung bình", "Xấu"
    val percent: Int   // 0–100
)
