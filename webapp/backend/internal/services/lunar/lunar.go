// Package lunar implements Vietnamese Lunar Calendar (Âm Lịch) algorithms.
// Based on Ho Ngoc Duc's algorithm: https://www.informatik.uni-leipzig.de/~duc/amlich/
package lunar

import "math"

// ============================================
// Core Astronomical Functions
// ============================================

// jdFromDate converts a date (dd/mm/yyyy) to Julian Day Number.
func jdFromDate(dd, mm, yy int) int {
	a := (14 - mm) / 12
	y := yy + 4800 - a
	m := mm + 12*a - 3
	jd := dd + (153*m+2)/5 + 365*y + y/4 - y/100 + y/400 - 32045
	if jd < 2299161 {
		jd = dd + (153*m+2)/5 + 365*y + y/4 - 32083
	}
	return jd
}

// jdToDate converts a Julian Day Number to date (dd, mm, yyyy).
func jdToDate(jd int) (int, int, int) {
	var a, b, c, d, e, m, day, month, year int
	if jd > 2299160 {
		a = jd + 32044
		b = (4*a + 3) / 146097
		c = a - (146097*b)/4
	} else {
		b = 0
		c = jd + 32082
	}
	d = (4*c + 3) / 1461
	e = c - (1461*d)/4
	m = (5*e + 2) / 153
	day = e - (153*m+2)/5 + 1
	month = m + 3 - 12*(m/10)
	year = 100*b + d - 4800 + m/10
	return day, month, year
}

// newMoon computes the Julian Day Number of the kth new moon after (or before)
// the New Moon of January 6, 1900 13:52 UCT (k=0).
// Returns a float64 for precision.
func newMoon(k int) float64 {
	T := float64(k) / 1236.85
	T2 := T * T
	T3 := T2 * T
	dr := math.Pi / 180.0

	Jd1 := 2415020.75933 + 29.53058868*float64(k) + 0.0001178*T2 - 0.000000155*T3
	Jd1 += 0.00033 * math.Sin((166.56+132.87*T-0.009173*T2)*dr)

	M := 359.2242 + 29.10535608*float64(k) - 0.0000333*T2 - 0.00000347*T3
	Mpr := 306.0253 + 385.81691806*float64(k) + 0.0107306*T2 + 0.00001236*T3
	F := 21.2964 + 390.67050646*float64(k) - 0.0016528*T2 - 0.00000239*T3

	C1 := (0.1734-0.000393*T)*math.Sin(M*dr) + 0.0021*math.Sin(2*M*dr)
	C1 = C1 - 0.4068*math.Sin(Mpr*dr) + 0.0161*math.Sin(2*Mpr*dr)
	C1 = C1 - 0.0004*math.Sin(3*Mpr*dr) + 0.0104*math.Sin(2*F*dr)
	C1 = C1 - 0.0051*math.Sin((M+Mpr)*dr) - 0.0074*math.Sin((M-Mpr)*dr)
	C1 = C1 + 0.0004*math.Sin((2*F+M)*dr) - 0.0004*math.Sin((2*F-M)*dr)
	C1 = C1 - 0.0006*math.Sin((2*F+Mpr)*dr) + 0.0010*math.Sin((2*F-Mpr)*dr)
	C1 = C1 + 0.0005*math.Sin((M+2*Mpr)*dr)

	var deltat float64
	if T < -11 {
		deltat = 0.001 + 0.000839*T + 0.0002261*T2 - 0.00000845*T3 - 0.000000081*T*T3
	} else {
		deltat = -0.000278 + 0.000265*T + 0.000262*T2
	}

	return Jd1 + C1 - deltat
}

// sunLongitude computes the sun's longitude (in degrees) at Julian Day Number jdn.
// Accuracy ~ 1 arc minute.
func sunLongitude(jdn float64) float64 {
	T := (jdn - 2451545.0) / 36525.0
	T2 := T * T
	dr := math.Pi / 180.0

	M := 357.52910 + 35999.05030*T - 0.0001559*T2 - 0.00000048*T*T2
	L0 := 280.46645 + 36000.76983*T + 0.0003032*T2
	DL := (1.914600 - 0.004817*T - 0.000014*T2) * math.Sin(M*dr)
	DL = DL + (0.019993-0.000101*T)*math.Sin(2*M*dr) + 0.000290*math.Sin(3*M*dr)
	L := L0 + DL

	omega := 125.04 - 1934.136*T
	L = L - 0.00569 - 0.00478*math.Sin(omega*dr)
	L = math.Mod(L, 360)
	if L < 0 {
		L += 360
	}
	return L
}

// getSunLongitude returns the sun longitude at the start of a given Julian Day.
func getSunLongitude(dayNumber int, timeZone float64) float64 {
	return sunLongitude(float64(dayNumber) - 0.5 - timeZone/24.0)
}

// getNewMoonDay returns the Julian Day Number of the new moon closest to a
// given Julian Day.
func getNewMoonDay(k int, timeZone float64) int {
	return int(newMoon(k) + 0.5 + timeZone/24.0)
}

// getLunarMonth11 returns the Julian Day Number of the lunar month 11's new moon
// that starts the lunar year containing the given solar year.
func getLunarMonth11(yy int, timeZone float64) int {
	off := jdFromDate(31, 12, yy) - 2415021
	k := int(float64(off) / 29.530588853)
	nm := getNewMoonDay(k, timeZone)
	sunLong := int(getSunLongitude(nm, timeZone) / 30.0)
	if sunLong >= 9 {
		nm = getNewMoonDay(k-1, timeZone)
	}
	return nm
}

// getLeapMonthOffset returns 0 if no leap month, or the index of the leap month
// after lunar month 11 for the year that has lunar month 11 starting on a11.
func getLeapMonthOffset(a11 int, timeZone float64) int {
	k := int(0.5 + (float64(a11)-2415021.076998695)/29.530588853)
	last := 0
	i := 1
	arc := int(getSunLongitude(getNewMoonDay(k+i, timeZone), timeZone) / 30.0)
	for {
		last = arc
		i++
		arc = int(getSunLongitude(getNewMoonDay(k+i, timeZone), timeZone) / 30.0)
		if !(arc != last && i < 14) {
			break
		}
	}
	return i - 1
}

// ============================================
// Public Types
// ============================================

// LunarDate represents a Vietnamese lunar calendar date.
type LunarDate struct {
	Day       int  `json:"day"`
	Month     int  `json:"month"`
	Year      int  `json:"year"`
	LeapMonth bool `json:"leap_month"`
}

// SolarDate represents a Gregorian calendar date.
type SolarDate struct {
	Day   int `json:"day"`
	Month int `json:"month"`
	Year  int `json:"year"`
}

// ============================================
// Public API — Conversion
// ============================================

// SolarToLunar converts a Gregorian date to Vietnamese Lunar date.
// timeZone is typically 7 for Vietnam (UTC+7).
func SolarToLunar(dd, mm, yy int, timeZone float64) LunarDate {
	dayNumber := jdFromDate(dd, mm, yy)
	k := int(float64(dayNumber-2415021) / 29.530588853)
	monthStart := getNewMoonDay(k+1, timeZone)
	if monthStart > dayNumber {
		monthStart = getNewMoonDay(k, timeZone)
	}

	a11 := getLunarMonth11(yy, timeZone)
	b11 := a11
	var lunarYear int
	if a11 >= monthStart {
		lunarYear = yy
		a11 = getLunarMonth11(yy-1, timeZone)
	} else {
		lunarYear = yy + 1
		b11 = getLunarMonth11(yy+1, timeZone)
	}

	lunarDay := dayNumber - monthStart + 1
	diff := int(float64(monthStart-a11) / 29.0)
	lunarLeap := false
	lunarMonth := diff + 11

	if b11-a11 > 365 {
		leapMonthDiff := getLeapMonthOffset(a11, timeZone)
		if diff >= leapMonthDiff {
			lunarMonth = diff + 10
			if diff == leapMonthDiff {
				lunarLeap = true
			}
		}
	}
	if lunarMonth > 12 {
		lunarMonth -= 12
	}
	if lunarMonth >= 11 && diff < 4 {
		lunarYear--
	}

	return LunarDate{
		Day:       lunarDay,
		Month:     lunarMonth,
		Year:      lunarYear,
		LeapMonth: lunarLeap,
	}
}

// LunarToSolar converts a Vietnamese Lunar date to Gregorian date.
func LunarToSolar(lunarDay, lunarMonth, lunarYear int, lunarLeap bool, timeZone float64) SolarDate {
	var a11, b11 int
	if lunarMonth < 11 {
		a11 = getLunarMonth11(lunarYear-1, timeZone)
		b11 = getLunarMonth11(lunarYear, timeZone)
	} else {
		a11 = getLunarMonth11(lunarYear, timeZone)
		b11 = getLunarMonth11(lunarYear+1, timeZone)
	}

	k := int(0.5 + (float64(a11)-2415021.076998695)/29.530588853)
	off := lunarMonth - 11
	if off < 0 {
		off += 12
	}

	if b11-a11 > 365 {
		leapOff := getLeapMonthOffset(a11, timeZone)
		leapMonth := leapOff - 2
		if leapMonth < 0 {
			leapMonth += 12
		}
		if lunarLeap && lunarMonth != leapMonth {
			// Invalid leap: return 0,0,0
			return SolarDate{0, 0, 0}
		} else if lunarLeap || (off >= leapOff) {
			off++
		}
	}

	monthStart := getNewMoonDay(k+off, timeZone)
	dd, mm, yy := jdToDate(monthStart + lunarDay - 1)
	return SolarDate{Day: dd, Month: mm, Year: yy}
}
