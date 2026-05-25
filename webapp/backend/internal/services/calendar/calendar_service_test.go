package calendar

import (
	"testing"
)

func TestGetToday(t *testing.T) {
	svc := NewService()
	result := svc.GetToday()

	if result.SolarDay == 0 || result.SolarMonth == 0 || result.SolarYear == 0 {
		t.Error("GetToday returned zero solar date")
	}
	if result.LunarDay == 0 || result.LunarMonth == 0 || result.LunarYear == 0 {
		t.Error("GetToday returned zero lunar date")
	}
	if result.DayOfWeek == "" {
		t.Error("GetToday returned empty day of week")
	}
	if result.TuTru.Nam.CanChi == "" {
		t.Error("GetToday returned empty year Can Chi")
	}
	if len(result.GioHoangDao) != 12 {
		t.Errorf("Expected 12 gio hoang dao, got %d", len(result.GioHoangDao))
	}

	t.Logf("Today: %d/%d/%d (%s)", result.SolarDay, result.SolarMonth, result.SolarYear, result.DayOfWeek)
	t.Logf("Lunar: %s %s %s", result.LunarDayName, result.LunarMonthName, result.TuTru.Nam.CanChi)
	t.Logf("Day Can Chi: %s", result.TuTru.Ngay.CanChi)
	t.Logf("Tiet Khi: %s (%s)", result.TietKhi.Current.Name, result.TietKhi.Current.HanTu)
	t.Logf("Chi So Ngay: %d%%", result.PhongThuy.ChiSoNgay)
}

func TestGetDate(t *testing.T) {
	svc := NewService()

	// Test Tet Nguyen Dan 2025
	result := svc.GetDate(29, 1, 2025, 8)

	if result.LunarDay != 1 || result.LunarMonth != 1 {
		t.Errorf("Expected Mong 1 thang Gieng, got day=%d month=%d", result.LunarDay, result.LunarMonth)
	}

	if result.TuTru.Nam.CanChi != "Ất Tỵ" {
		t.Errorf("Expected At Ty year, got %s", result.TuTru.Nam.CanChi)
	}

	t.Logf("29/1/2025 = %s %s, năm %s", result.LunarDayName, result.LunarMonthName, result.TuTru.Nam.CanChi)
}

func TestGetMonth(t *testing.T) {
	svc := NewService()
	result := svc.GetMonth(2026, 3)

	if result.Year != 2026 || result.Month != 3 {
		t.Errorf("Expected 3/2026, got %d/%d", result.Month, result.Year)
	}

	if len(result.Days) != 31 {
		t.Errorf("March should have 31 days, got %d", len(result.Days))
	}

	// Check first and last day
	if result.Days[0].SolarDay != 1 {
		t.Errorf("First day should be 1, got %d", result.Days[0].SolarDay)
	}
	if result.Days[30].SolarDay != 31 {
		t.Errorf("Last day should be 31, got %d", result.Days[30].SolarDay)
	}

	t.Logf("Month info: %s", result.LunarInfo)

	goodCount := 0
	for _, d := range result.Days {
		if d.IsGoodDay {
			goodCount++
		}
	}
	t.Logf("Good days in March 2026: %d/%d", goodCount, len(result.Days))
}

func TestConvert(t *testing.T) {
	svc := NewService()

	// Solar to Lunar
	result := svc.ConvertSolarToLunar(29, 1, 2025)
	if result.Lunar.Day != 1 || result.Lunar.Month != 1 || result.Lunar.Year != 2025 {
		t.Errorf("Expected 1/1/2025 lunar, got %+v", result.Lunar)
	}

	// Lunar to Solar (round trip)
	result2 := svc.ConvertLunarToSolar(1, 1, 2025, false)
	if result2.Solar.Day != 29 || result2.Solar.Month != 1 || result2.Solar.Year != 2025 {
		t.Errorf("Expected 29/1/2025 solar, got %+v", result2.Solar)
	}
}

func TestGetGoodDays(t *testing.T) {
	svc := NewService()
	goodDays := svc.GetGoodDays(2026, 3)

	t.Logf("Good days in 3/2026: %d", len(goodDays))
	for _, d := range goodDays {
		t.Logf("  %d/%d (%s) - %s - Score: %d%% - Trực: %s",
			d.SolarDay, d.SolarMonth, d.DayOfWeek, d.DayCanChi, d.ChiSoNgay, d.TrucNgay)
	}
}

func TestGetSolarTerms(t *testing.T) {
	svc := NewService()
	terms := svc.GetSolarTerms(2026)

	if len(terms) != 24 {
		t.Errorf("Expected 24 solar terms, got %d", len(terms))
	}

	for _, st := range terms {
		if st.Day == 0 || st.Month == 0 {
			t.Errorf("Solar term %s has zero date", st.Name)
		}
		t.Logf("  %s (%s) - %d/%d/%d", st.Name, st.HanTu, st.Day, st.Month, st.Year)
	}
}
