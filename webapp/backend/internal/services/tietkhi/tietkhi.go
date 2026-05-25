// Package tietKhi implements the 24 Solar Terms (Tiết Khí) calculation.
package tietkhi

import (
	"math"
	"time"
)

// SolarTerm represents a single solar term (tiết khí).
type SolarTerm struct {
	Index   int    `json:"index"`    // 0-23
	Name    string `json:"name"`     // Vietnamese name
	HanTu   string `json:"han_tu"`   // Chinese characters
	Date    string `json:"date"`     // Approximate date "dd/mm"
	SunLong int    `json:"sun_long"` // Sun's longitude in degrees (0-360)
}

// SolarTermDate is a computed solar term with exact date.
type SolarTermDate struct {
	SolarTerm
	Day   int `json:"day"`
	Month int `json:"month"`
	Year  int `json:"year"`
}

// CurrentSolarTermInfo contains info about the current solar term.
type CurrentSolarTermInfo struct {
	Current  SolarTermDate `json:"current"`
	Next     SolarTermDate `json:"next"`
	Progress float64       `json:"progress"` // 0.0 - 1.0
	DaysLeft int           `json:"days_left"`
	Desc     string        `json:"description"`
}

// ============================================
// 24 Solar Terms Definition
// ============================================

var SolarTerms = [24]SolarTerm{
	{0, "Tiểu Hàn", "小寒", "~06/01", 285},
	{1, "Đại Hàn", "大寒", "~20/01", 300},
	{2, "Lập Xuân", "立春", "~04/02", 315},
	{3, "Vũ Thuỷ", "雨水", "~19/02", 330},
	{4, "Kinh Trập", "驚蟄", "~06/03", 345},
	{5, "Xuân Phân", "春分", "~21/03", 0},
	{6, "Thanh Minh", "清明", "~05/04", 15},
	{7, "Cốc Vũ", "穀雨", "~20/04", 30},
	{8, "Lập Hạ", "立夏", "~06/05", 45},
	{9, "Tiểu Mãn", "小滿", "~21/05", 60},
	{10, "Mang Chủng", "芒種", "~06/06", 75},
	{11, "Hạ Chí", "夏至", "~21/06", 90},
	{12, "Tiểu Thử", "小暑", "~07/07", 105},
	{13, "Đại Thử", "大暑", "~23/07", 120},
	{14, "Lập Thu", "立秋", "~07/08", 135},
	{15, "Xử Thử", "處暑", "~23/08", 150},
	{16, "Bạch Lộ", "白露", "~08/09", 165},
	{17, "Thu Phân", "秋分", "~23/09", 180},
	{18, "Hàn Lộ", "寒露", "~08/10", 195},
	{19, "Sương Giáng", "霜降", "~23/10", 210},
	{20, "Lập Đông", "立冬", "~07/11", 225},
	{21, "Tiểu Tuyết", "小雪", "~22/11", 240},
	{22, "Đại Tuyết", "大雪", "~07/12", 255},
	{23, "Đông Chí", "冬至", "~22/12", 270},
}

// Descriptions for each solar term.
var SolarTermDescs = [24]string{
	"Tiết trời lạnh nhẹ, bắt đầu mùa rét",
	"Rét đậm nhất trong năm, đất đông cứng",
	"Xuân sang, vạn vật bắt đầu sinh sôi",
	"Mưa xuân tưới mát, đất trời giao hoà",
	"Sấm vang, côn trùng tỉnh giấc ngủ đông",
	"Ngày đêm bằng nhau, xuân giữa mùa",
	"Trời quang mây tạnh, trong sáng dịu mát",
	"Mưa nuôi ngũ cốc, cây cối xanh tươi",
	"Hạ bắt đầu, thời tiết ấm nóng",
	"Lúa bắt đầu chín, cây cối sum suê",
	"Lúa chín rộ, vụ mùa bận rộn",
	"Ngày dài nhất năm, nắng cao nhất",
	"Nắng nóng bắt đầu, tiết trời oi ả",
	"Nóng nhất trong năm, hè rực lửa",
	"Thu sang, gió se se lạnh",
	"Nắng nóng lui dần, heo may về",
	"Sương trắng trên lá, thu mát dịu",
	"Ngày đêm bằng nhau, thu giữa mùa",
	"Sương lạnh bắt đầu, lá vàng rơi",
	"Sương giá xuất hiện, đông gần kề",
	"Đông bắt đầu, rét mướt dần",
	"Tuyết nhỏ bắt đầu rơi, trời lạnh",
	"Tuyết rơi nhiều, đất phủ trắng",
	"Đêm dài nhất năm, đông chính giữa",
}

// ============================================
// Astronomical Computation
// ============================================

// sunLongitude computes approximate sun longitude at a Julian Day Number.
func sunLongitude(jdn float64) float64 {
	T := (jdn - 2451545.0) / 36525.0
	T2 := T * T
	dr := math.Pi / 180.0

	M := 357.52910 + 35999.05030*T - 0.0001559*T2 - 0.00000048*T*T2
	L0 := 280.46645 + 36000.76983*T + 0.0003032*T2
	DL := (1.914600 - 0.004817*T - 0.000014*T2) * math.Sin(M*dr)
	DL += (0.019993 - 0.000101*T) * math.Sin(2*M*dr)
	DL += 0.000290 * math.Sin(3*M*dr)
	L := L0 + DL

	omega := 125.04 - 1934.136*T
	L = L - 0.00569 - 0.00478*math.Sin(omega*dr)
	L = math.Mod(L, 360)
	if L < 0 {
		L += 360
	}
	return L
}

// jdFromDate converts date to Julian Day Number.
func jdFromDate(dd, mm, yy int) float64 {
	a := (14 - mm) / 12
	y := yy + 4800 - a
	m := mm + 12*a - 3
	jd := dd + (153*m+2)/5 + 365*y + y/4 - y/100 + y/400 - 32045
	return float64(jd)
}

// findSolarTermJD finds the Julian Day when sun longitude reaches the given degrees.
// Search starts from startJD.
func findSolarTermJD(targetLong float64, startJD float64) float64 {
	jd := startJD
	for i := 0; i < 400; i++ {
		l := sunLongitude(jd)
		diff := targetLong - l
		if diff > 180 {
			diff -= 360
		}
		if diff < -180 {
			diff += 360
		}
		if math.Abs(diff) < 0.01 {
			return jd
		}
		// Approximate: sun moves ~1 degree per day
		jd += diff
	}
	return jd
}

// jdToDate converts Julian Day Number to date.
func jdToDate(jd float64) (int, int, int) {
	z := int(jd + 0.5)
	var a int
	if z < 2299161 {
		a = z
	} else {
		alpha := int((float64(z) - 1867216.25) / 36524.25)
		a = z + 1 + alpha - alpha/4
	}
	b := a + 1524
	c := int((float64(b) - 122.1) / 365.25)
	d := int(365.25 * float64(c))
	e := int(float64(b-d) / 30.6001)
	day := b - d - int(30.6001*float64(e))
	var month int
	if e < 14 {
		month = e - 1
	} else {
		month = e - 13
	}
	var year int
	if month > 2 {
		year = c - 4716
	} else {
		year = c - 4715
	}
	return day, month, year
}

// ============================================
// Public API
// ============================================

// GetSolarTermsForYear computes all 24 solar term dates for a given year.
func GetSolarTermsForYear(year int) []SolarTermDate {
	results := make([]SolarTermDate, 24)
	for i := 0; i < 24; i++ {
		// Approximate starting JD: beginning of the month when this term typically occurs
		var approxMonth int
		switch {
		case i <= 1:
			approxMonth = 1
		case i <= 3:
			approxMonth = 2
		case i <= 5:
			approxMonth = 3
		case i <= 7:
			approxMonth = 4
		case i <= 9:
			approxMonth = 5
		case i <= 11:
			approxMonth = 6
		case i <= 13:
			approxMonth = 7
		case i <= 15:
			approxMonth = 8
		case i <= 17:
			approxMonth = 9
		case i <= 19:
			approxMonth = 10
		case i <= 21:
			approxMonth = 11
		default:
			approxMonth = 12
		}

		startJD := jdFromDate(1, approxMonth, year)
		targetLong := float64(SolarTerms[i].SunLong)
		jd := findSolarTermJD(targetLong, startJD)

		dd, mm, yy := jdToDate(jd)
		results[i] = SolarTermDate{
			SolarTerm: SolarTerms[i],
			Day:       dd,
			Month:     mm,
			Year:      yy,
		}
	}
	return results
}

// GetCurrentSolarTerm returns the current solar term and progress info for a date.
func GetCurrentSolarTerm(dd, mm, yy int) CurrentSolarTermInfo {
	// Get terms for this year and adjacent years for boundary cases
	terms := GetSolarTermsForYear(yy)
	prevTerms := GetSolarTermsForYear(yy - 1)
	nextTerms := GetSolarTermsForYear(yy + 1)

	// Build a flat list for easier search
	allTerms := make([]SolarTermDate, 0, 72)
	allTerms = append(allTerms, prevTerms...)
	allTerms = append(allTerms, terms...)
	allTerms = append(allTerms, nextTerms...)

	targetJD := jdFromDate(dd, mm, yy)

	// Find the current term (latest term whose JD <= targetJD)
	var currentIdx int
	for i := len(allTerms) - 1; i >= 0; i-- {
		termJD := jdFromDate(allTerms[i].Day, allTerms[i].Month, allTerms[i].Year)
		if termJD <= targetJD {
			currentIdx = i
			break
		}
	}

	current := allTerms[currentIdx]
	next := allTerms[currentIdx+1]

	currentJD := jdFromDate(current.Day, current.Month, current.Year)
	nextJD := jdFromDate(next.Day, next.Month, next.Year)

	progress := float64(targetJD-currentJD) / float64(nextJD-currentJD)
	if progress < 0 {
		progress = 0
	}
	if progress > 1 {
		progress = 1
	}

	daysLeft := int(nextJD - targetJD)
	if daysLeft < 0 {
		daysLeft = 0
	}

	desc := ""
	if current.Index >= 0 && current.Index < 24 {
		desc = SolarTermDescs[current.Index]
	}

	return CurrentSolarTermInfo{
		Current:  current,
		Next:     next,
		Progress: progress,
		DaysLeft: daysLeft,
		Desc:     desc,
	}
}

// GetSolarTermForDate returns the solar term name that is active on a given date.
func GetSolarTermForDate(t time.Time) string {
	info := GetCurrentSolarTerm(t.Day(), int(t.Month()), t.Year())
	return info.Current.Name
}
