// Package calendar provides the unified calendar service combining
// lunar conversion, can chi, solar terms, and feng shui.
package calendar

import (
	"fmt"
	"time"

	"github.com/zplus/lichso/internal/services/canchi"
	"github.com/zplus/lichso/internal/services/events"
	"github.com/zplus/lichso/internal/services/lunar"
	"github.com/zplus/lichso/internal/services/phongthuy"
	"github.com/zplus/lichso/internal/services/tietkhi"
)

// TimeZone for Vietnam (UTC+7)
const VietnamTZ = 7.0

// ============================================
// Day Names
// ============================================

var lunarDayNames = []string{
	"", "Mồng 1", "Mồng 2", "Mồng 3", "Mồng 4", "Mồng 5",
	"Mồng 6", "Mồng 7", "Mồng 8", "Mồng 9", "Mồng 10",
	"11", "12", "13", "14", "Rằm",
	"16", "17", "18", "19", "20",
	"21", "22", "23", "24", "25",
	"26", "27", "28", "29", "30",
}

var lunarMonthNames = []string{
	"", "Tháng Giêng", "Tháng Hai", "Tháng Ba", "Tháng Tư", "Tháng Năm",
	"Tháng Sáu", "Tháng Bảy", "Tháng Tám", "Tháng Chín", "Tháng Mười",
	"Tháng Mười Một", "Tháng Chạp",
}

var dayOfWeekNames = []string{"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"}

// ============================================
// Response Types
// ============================================

// DayResponse is the full data for a single date.
type DayResponse struct {
	// Solar
	SolarDay   int    `json:"solar_day"`
	SolarMonth int    `json:"solar_month"`
	SolarYear  int    `json:"solar_year"`
	DayOfWeek  string `json:"day_of_week"`
	DayOfWeekN int    `json:"day_of_week_n"` // 0=Sunday

	// Lunar
	LunarDay       int    `json:"lunar_day"`
	LunarDayName   string `json:"lunar_day_name"`
	LunarMonth     int    `json:"lunar_month"`
	LunarMonthName string `json:"lunar_month_name"`
	LunarYear      int    `json:"lunar_year"`
	IsLeapMonth    bool   `json:"is_leap_month"`

	// Can Chi (Four Pillars)
	TuTru canchi.TuTru `json:"tu_tru"`

	// Giờ Hoàng Đạo
	GioHoangDao []canchi.GioCanChi `json:"gio_hoang_dao"`

	// Tiết Khí
	TietKhi tietkhi.CurrentSolarTermInfo `json:"tiet_khi"`

	// Phong Thuỷ
	PhongThuy phongthuy.DayInfo `json:"phong_thuy"`

	// Sự kiện / Ngày lễ
	Events []events.Event `json:"events"`
}

// MonthDayBrief is a brief day info for calendar grid.
type MonthDayBrief struct {
	SolarDay     int            `json:"solar_day"`
	LunarDay     int            `json:"lunar_day"`
	LunarDayName string         `json:"lunar_day_name"`
	DayOfWeek    int            `json:"day_of_week"` // 0=Sunday
	IsToday      bool           `json:"is_today"`
	IsGoodDay    bool           `json:"is_good_day"`
	ChiSoNgay    int            `json:"chi_so_ngay"`
	IsHoliday    bool           `json:"is_holiday"`
	Events       []events.Event `json:"events,omitempty"`
}

// MonthResponse contains a full month's data.
type MonthResponse struct {
	Year      int             `json:"year"`
	Month     int             `json:"month"`
	LunarInfo string          `json:"lunar_info"` // e.g. "Tháng Hai Ất Tỵ"
	Days      []MonthDayBrief `json:"days"`
}

// ConvertResult is the result of a calendar conversion.
type ConvertResult struct {
	Solar lunar.SolarDate `json:"solar"`
	Lunar lunar.LunarDate `json:"lunar"`
}

// GoodDayInfo is a brief good day entry.
type GoodDayInfo struct {
	SolarDay   int    `json:"solar_day"`
	SolarMonth int    `json:"solar_month"`
	LunarDay   int    `json:"lunar_day"`
	DayOfWeek  string `json:"day_of_week"`
	ChiSoNgay  int    `json:"chi_so_ngay"`
	DanhGia    string `json:"danh_gia"`
	DayCanChi  string `json:"day_can_chi"`
	TrucNgay   string `json:"truc_ngay"`
}

// ============================================
// Service
// ============================================

// Service provides calendar operations.
type Service struct{}

// NewService creates a new Calendar Service.
func NewService() *Service {
	return &Service{}
}

// GetToday returns full day info for today (Vietnam timezone).
func (s *Service) GetToday() DayResponse {
	loc, _ := time.LoadLocation("Asia/Ho_Chi_Minh")
	now := time.Now().In(loc)
	return s.GetDate(now.Day(), int(now.Month()), now.Year(), now.Hour())
}

// GetDate returns full day info for a specific date.
func (s *Service) GetDate(dd, mm, yy, hour int) DayResponse {
	// Determine day of week
	t := time.Date(yy, time.Month(mm), dd, hour, 0, 0, 0, time.UTC)
	dow := int(t.Weekday())

	// Lunar conversion
	lunarDate := lunar.SolarToLunar(dd, mm, yy, VietnamTZ)

	// Can Chi Four Pillars
	tuTru := canchi.GetTuTru(dd, mm, yy, hour, lunarDate.Month, lunarDate.Year)

	// Giờ Hoàng Đạo
	gioHD := canchi.GetGioHoangDao(dd, mm, yy)

	// Tiết Khí
	tietKhiInfo := tietkhi.GetCurrentSolarTerm(dd, mm, yy)

	// Phong Thuỷ
	phongThuyInfo := phongthuy.GetDayInfo(dd, mm, yy, lunarDate.Day, lunarDate.Month, lunarDate.Year)

	// Sự kiện / Ngày lễ
	dayEvents := events.GetAllEvents(dd, mm, lunarDate.Day, lunarDate.Month)
	lunarDayName := ""
	if lunarDate.Day >= 1 && lunarDate.Day <= 30 {
		lunarDayName = lunarDayNames[lunarDate.Day]
	}
	lunarMonthName := ""
	if lunarDate.Month >= 1 && lunarDate.Month <= 12 {
		lunarMonthName = lunarMonthNames[lunarDate.Month]
	}
	if lunarDate.LeapMonth {
		lunarMonthName = "Nhuận " + lunarMonthName
	}

	return DayResponse{
		SolarDay:       dd,
		SolarMonth:     mm,
		SolarYear:      yy,
		DayOfWeek:      dayOfWeekNames[dow],
		DayOfWeekN:     dow,
		LunarDay:       lunarDate.Day,
		LunarDayName:   lunarDayName,
		LunarMonth:     lunarDate.Month,
		LunarMonthName: lunarMonthName,
		LunarYear:      lunarDate.Year,
		IsLeapMonth:    lunarDate.LeapMonth,
		TuTru:          tuTru,
		GioHoangDao:    gioHD,
		TietKhi:        tietKhiInfo,
		PhongThuy:      phongThuyInfo,
		Events:         dayEvents,
	}
}

// GetMonth returns brief day info for each day of a given month.
func (s *Service) GetMonth(yy, mm int) MonthResponse {
	loc, _ := time.LoadLocation("Asia/Ho_Chi_Minh")
	now := time.Now().In(loc)
	daysInMonth := time.Date(yy, time.Month(mm+1), 0, 0, 0, 0, 0, time.UTC).Day()

	// Get lunar info for the first day of the month
	firstLunar := lunar.SolarToLunar(1, mm, yy, VietnamTZ)
	yearCC := canchi.YearCanChi(firstLunar.Year)
	lunarInfo := fmt.Sprintf("%s %s", lunarMonthNames[firstLunar.Month], yearCC.CanChi)

	days := make([]MonthDayBrief, daysInMonth)
	for d := 1; d <= daysInMonth; d++ {
		t := time.Date(yy, time.Month(mm), d, 12, 0, 0, 0, time.UTC)
		dow := int(t.Weekday())
		lunarDate := lunar.SolarToLunar(d, mm, yy, VietnamTZ)
		phongThuyInfo := phongthuy.GetDayInfo(d, mm, yy, lunarDate.Day, lunarDate.Month, lunarDate.Year)

		ldName := ""
		if lunarDate.Day >= 1 && lunarDate.Day <= 30 {
			ldName = lunarDayNames[lunarDate.Day]
		}

		isToday := now.Year() == yy && int(now.Month()) == mm && now.Day() == d

		dayEvents := events.GetAllEvents(d, mm, lunarDate.Day, lunarDate.Month)
		isHoliday := events.IsHoliday(d, mm) || events.IsLunarHoliday(lunarDate.Day, lunarDate.Month)

		days[d-1] = MonthDayBrief{
			SolarDay:     d,
			LunarDay:     lunarDate.Day,
			LunarDayName: ldName,
			DayOfWeek:    dow,
			IsToday:      isToday,
			IsGoodDay:    phongThuyInfo.ChiSoNgay >= 70,
			ChiSoNgay:    phongThuyInfo.ChiSoNgay,
			IsHoliday:    isHoliday,
			Events:       dayEvents,
		}
	}

	return MonthResponse{
		Year:      yy,
		Month:     mm,
		LunarInfo: lunarInfo,
		Days:      days,
	}
}

// ConvertSolarToLunar converts a solar date to lunar.
func (s *Service) ConvertSolarToLunar(dd, mm, yy int) ConvertResult {
	lunarDate := lunar.SolarToLunar(dd, mm, yy, VietnamTZ)
	return ConvertResult{
		Solar: lunar.SolarDate{Day: dd, Month: mm, Year: yy},
		Lunar: lunarDate,
	}
}

// ConvertLunarToSolar converts a lunar date to solar.
func (s *Service) ConvertLunarToSolar(dd, mm, yy int, leap bool) ConvertResult {
	solarDate := lunar.LunarToSolar(dd, mm, yy, leap, VietnamTZ)
	return ConvertResult{
		Solar: solarDate,
		Lunar: lunar.LunarDate{Day: dd, Month: mm, Year: yy, LeapMonth: leap},
	}
}

// GetGoodDays returns all good days in a given month.
func (s *Service) GetGoodDays(yy, mm int) []GoodDayInfo {
	daysInMonth := time.Date(yy, time.Month(mm+1), 0, 0, 0, 0, 0, time.UTC).Day()
	var goodDays []GoodDayInfo

	for d := 1; d <= daysInMonth; d++ {
		t := time.Date(yy, time.Month(mm), d, 12, 0, 0, 0, time.UTC)
		dow := int(t.Weekday())
		lunarDate := lunar.SolarToLunar(d, mm, yy, VietnamTZ)
		phongThuyInfo := phongthuy.GetDayInfo(d, mm, yy, lunarDate.Day, lunarDate.Month, lunarDate.Year)
		dayCC := canchi.DayCanChi(d, mm, yy)

		if phongThuyInfo.ChiSoNgay >= 65 {
			goodDays = append(goodDays, GoodDayInfo{
				SolarDay:   d,
				SolarMonth: mm,
				LunarDay:   lunarDate.Day,
				DayOfWeek:  dayOfWeekNames[dow],
				ChiSoNgay:  phongThuyInfo.ChiSoNgay,
				DanhGia:    phongThuyInfo.DanhGia,
				DayCanChi:  dayCC.CanChi,
				TrucNgay:   phongThuyInfo.TrucNgay.Name,
			})
		}
	}

	return goodDays
}

// GetSolarTerms returns all 24 solar terms for a year.
func (s *Service) GetSolarTerms(yy int) []tietkhi.SolarTermDate {
	return tietkhi.GetSolarTermsForYear(yy)
}
