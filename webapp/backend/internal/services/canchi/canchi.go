// Package canchi implements the Vietnamese Can Chi (Heavenly Stems & Earthly Branches) system.
package canchi

import (
	"math"
	"time"
)

// ============================================
// Thiên Can (10 Heavenly Stems)
// ============================================

var ThienCan = [10]string{"Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"}
var ThienCanNguHanh = [10]string{"Mộc", "Mộc", "Hoả", "Hoả", "Thổ", "Thổ", "Kim", "Kim", "Thuỷ", "Thuỷ"}
var ThienCanAmDuong = [10]string{"Dương", "Âm", "Dương", "Âm", "Dương", "Âm", "Dương", "Âm", "Dương", "Âm"}

// ============================================
// Địa Chi (12 Earthly Branches)
// ============================================

var DiaChi = [12]string{"Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"}
var DiaChiConGiap = [12]string{"Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Heo"}
var DiaChiGio = [12]string{"23–1h", "1–3h", "3–5h", "5–7h", "7–9h", "9–11h", "11–13h", "13–15h", "15–17h", "17–19h", "19–21h", "21–23h"}

// ============================================
// Result Types
// ============================================

// CanChi represents a Can-Chi pair.
type CanChi struct {
	Can     string `json:"can"`      // Heavenly Stem
	Chi     string `json:"chi"`      // Earthly Branch
	CanChi  string `json:"can_chi"`  // Combined "Giáp Tý"
	NguHanh string `json:"ngu_hanh"` // Five Elements of the Can
	AmDuong string `json:"am_duong"` // Yin/Yang of the Can
	ConGiap string `json:"con_giap"` // Chinese zodiac animal
}

// TuTru represents the Four Pillars (Năm/Tháng/Ngày/Giờ).
type TuTru struct {
	Nam   CanChi `json:"nam"`   // Year pillar
	Thang CanChi `json:"thang"` // Month pillar
	Ngay  CanChi `json:"ngay"`  // Day pillar
	Gio   CanChi `json:"gio"`   // Hour pillar
}

// GioCanChi represents a two-hour period with its Can Chi info.
type GioCanChi struct {
	Name       string `json:"name"`    // e.g. "Tý"
	Range      string `json:"range"`   // e.g. "23–1h"
	CanChi     string `json:"can_chi"` // e.g. "Giáp Tý"
	IsHoangDao bool   `json:"is_hoang_dao"`
}

// ============================================
// Computation Functions
// ============================================

// makeCanChi builds a CanChi from indices.
func makeCanChi(canIdx, chiIdx int) CanChi {
	canIdx = ((canIdx % 10) + 10) % 10
	chiIdx = ((chiIdx % 12) + 12) % 12
	return CanChi{
		Can:     ThienCan[canIdx],
		Chi:     DiaChi[chiIdx],
		CanChi:  ThienCan[canIdx] + " " + DiaChi[chiIdx],
		NguHanh: ThienCanNguHanh[canIdx],
		AmDuong: ThienCanAmDuong[canIdx],
		ConGiap: DiaChiConGiap[chiIdx],
	}
}

// jdFromDate computes Julian Day Number (same as lunar package, duplicated for independence).
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

// YearCanChi returns the Can Chi of a lunar year.
// Lunar year cycle: 1984 = Giáp Tý (index 0).
func YearCanChi(lunarYear int) CanChi {
	canIdx := (lunarYear + 6) % 10
	chiIdx := (lunarYear + 8) % 12
	return makeCanChi(canIdx, chiIdx)
}

// MonthCanChi returns the Can Chi of a lunar month.
// The Can of month depends on the year's Can.
func MonthCanChi(lunarMonth, lunarYear int) CanChi {
	yearCan := (lunarYear + 6) % 10
	// Công thức: Can tháng = (YearCan * 2 + lunarMonth) % 10
	monthCan := (yearCan*2 + lunarMonth) % 10
	monthChi := (lunarMonth + 1) % 12 // Tháng Giêng = Dần (index 2): (1+1)%12 = 2
	return makeCanChi(monthCan, monthChi)
}

// DayCanChi returns the Can Chi of a solar date.
// Based on Julian Day Number.
func DayCanChi(dd, mm, yy int) CanChi {
	jd := jdFromDate(dd, mm, yy)
	canIdx := (jd + 9) % 10
	chiIdx := (jd + 1) % 12
	return makeCanChi(canIdx, chiIdx)
}

// HourCanChi returns the Can Chi of the current hour.
// dayCan is the Can index of the day.
func HourCanChi(hour int, dayCan int) CanChi {
	hourIdx := int(math.Floor(float64((hour+1)%24) / 2.0))
	// Can of Tý hour = (dayCan * 2) % 10
	hourCan := (dayCan*2 + hourIdx) % 10
	return makeCanChi(hourCan, hourIdx)
}

// HourIndex returns the Dia Chi index for a given hour (0-23).
func HourIndex(hour int) int {
	return int(math.Floor(float64((hour+1)%24) / 2.0))
}

// ============================================
// Four Pillars (Tứ Trụ)
// ============================================

// GetTuTru calculates the Four Pillars for a given date and time.
// lunarMonth, lunarYear are from the lunar calendar.
func GetTuTru(dd, mm, yy int, hour int, lunarMonth, lunarYear int) TuTru {
	jd := jdFromDate(dd, mm, yy)
	dayCan := (jd + 9) % 10

	return TuTru{
		Nam:   YearCanChi(lunarYear),
		Thang: MonthCanChi(lunarMonth, lunarYear),
		Ngay:  DayCanChi(dd, mm, yy),
		Gio:   HourCanChi(hour, dayCan),
	}
}

// ============================================
// 12 Giờ Hoàng Đạo / Hắc Đạo
// ============================================

// Hoàng Đạo hours depend on the day's Dia Chi.
// Pattern for each day Dia Chi index:
var hoangDaoPatterns = map[int][]bool{
	0:  {true, true, false, false, true, false, true, true, false, true, false, false}, // Tý
	1:  {false, false, true, true, false, true, false, false, true, false, true, true}, // Sửu
	2:  {true, true, false, false, true, false, true, true, false, true, false, false}, // Dần
	3:  {false, false, true, true, false, true, false, false, true, false, true, true}, // Mão
	4:  {true, true, false, false, true, false, true, true, false, true, false, false}, // Thìn
	5:  {false, false, true, true, false, true, false, false, true, false, true, true}, // Tỵ
	6:  {true, true, false, false, true, false, true, true, false, true, false, false}, // Ngọ
	7:  {false, false, true, true, false, true, false, false, true, false, true, true}, // Mùi
	8:  {true, true, false, false, true, false, true, true, false, true, false, false}, // Thân
	9:  {false, false, true, true, false, true, false, false, true, false, true, true}, // Dậu
	10: {true, true, false, false, true, false, true, true, false, true, false, false}, // Tuất
	11: {false, false, true, true, false, true, false, false, true, false, true, true}, // Hợi
}

// GetGioHoangDao returns the 12 two-hour periods with Hoang Dao info for a given date.
func GetGioHoangDao(dd, mm, yy int) []GioCanChi {
	jd := jdFromDate(dd, mm, yy)
	dayCan := (jd + 9) % 10
	dayChi := (jd + 1) % 12

	pattern, ok := hoangDaoPatterns[dayChi]
	if !ok {
		pattern = hoangDaoPatterns[0]
	}

	result := make([]GioCanChi, 12)
	for i := 0; i < 12; i++ {
		hourCan := (dayCan*2 + i) % 10
		canChiStr := ThienCan[hourCan] + " " + DiaChi[i]
		result[i] = GioCanChi{
			Name:       DiaChi[i],
			Range:      DiaChiGio[i],
			CanChi:     canChiStr,
			IsHoangDao: pattern[i],
		}
	}
	return result
}

// GetCurrentGioCanChi returns the current hour's Can Chi information.
func GetCurrentGioCanChi(t time.Time) GioCanChi {
	dd, mm, yy := t.Day(), int(t.Month()), t.Year()
	hours := GetGioHoangDao(dd, mm, yy)
	idx := HourIndex(t.Hour())
	return hours[idx]
}
