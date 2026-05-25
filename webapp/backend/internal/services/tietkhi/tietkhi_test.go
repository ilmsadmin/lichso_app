package tietkhi

import "testing"

func TestGetSolarTermsForYear(t *testing.T) {
	terms := GetSolarTermsForYear(2026)

	if len(terms) != 24 {
		t.Fatalf("Expected 24 solar terms, got %d", len(terms))
	}

	// Verify all terms have valid dates
	for i, term := range terms {
		if term.Day < 1 || term.Day > 31 {
			t.Errorf("Term %d (%s): invalid day %d", i, term.Name, term.Day)
		}
		if term.Month < 1 || term.Month > 12 {
			t.Errorf("Term %d (%s): invalid month %d", i, term.Name, term.Month)
		}
		if term.Year != 2026 {
			t.Errorf("Term %d (%s): expected year 2026, got %d", i, term.Name, term.Year)
		}
		t.Logf("%s (%s): %02d/%02d/%d [%d°]", term.Name, term.HanTu, term.Day, term.Month, term.Year, term.SunLong)
	}

	// Xuân Phân (Spring Equinox) should be around March 20-21
	xuanPhan := terms[5]
	if xuanPhan.Name != "Xuân Phân" {
		t.Errorf("Expected index 5 to be Xuan Phan, got %s", xuanPhan.Name)
	}
	if xuanPhan.Month != 3 || xuanPhan.Day < 19 || xuanPhan.Day > 22 {
		t.Errorf("Xuan Phan should be around 20-21/3, got %d/%d", xuanPhan.Day, xuanPhan.Month)
	}

	// Hạ Chí (Summer Solstice) should be around June 20-22
	haChi := terms[11]
	if haChi.Name != "Hạ Chí" {
		t.Errorf("Expected index 11 to be Ha Chi, got %s", haChi.Name)
	}
	if haChi.Month != 6 || haChi.Day < 19 || haChi.Day > 23 {
		t.Errorf("Ha Chi should be around 20-22/6, got %d/%d", haChi.Day, haChi.Month)
	}

	// Đông Chí (Winter Solstice) should be around December 21-22
	dongChi := terms[23]
	if dongChi.Name != "Đông Chí" {
		t.Errorf("Expected index 23 to be Dong Chi, got %s", dongChi.Name)
	}
	if dongChi.Month != 12 || dongChi.Day < 20 || dongChi.Day > 23 {
		t.Errorf("Dong Chi should be around 21-22/12, got %d/%d", dongChi.Day, dongChi.Month)
	}
}

func TestGetCurrentSolarTerm(t *testing.T) {
	// Test March 5, 2026 — should be after Vũ Thuỷ (rain water, ~Feb 19) and before Kinh Trập (~Mar 6)
	info := GetCurrentSolarTerm(5, 3, 2026)

	if info.Current.Name == "" {
		t.Error("Current solar term name is empty")
	}
	if info.Next.Name == "" {
		t.Error("Next solar term name is empty")
	}
	if info.Progress < 0 || info.Progress > 1 {
		t.Errorf("Progress should be 0-1, got %f", info.Progress)
	}
	if info.DaysLeft < 0 {
		t.Errorf("DaysLeft should be >= 0, got %d", info.DaysLeft)
	}

	t.Logf("Current: %s (%s), Next: %s, Progress: %.1f%%, DaysLeft: %d",
		info.Current.Name, info.Current.HanTu, info.Next.Name, info.Progress*100, info.DaysLeft)
}

func TestGetSolarTermForDate(t *testing.T) {
	tests := []struct {
		dd, mm, yy int
	}{
		{1, 1, 2026},
		{5, 3, 2026},
		{21, 6, 2026},
		{22, 12, 2026},
	}

	for _, tt := range tests {
		name := GetCurrentSolarTerm(tt.dd, tt.mm, tt.yy).Current.Name
		if name == "" {
			t.Errorf("GetCurrentSolarTerm(%d/%d/%d) returned empty name", tt.dd, tt.mm, tt.yy)
		}
		t.Logf("%02d/%02d/%d → %s", tt.dd, tt.mm, tt.yy, name)
	}
}

func TestSolarTermsChronologicalOrder(t *testing.T) {
	terms := GetSolarTermsForYear(2026)
	for i := 1; i < len(terms); i++ {
		prevJD := jdFromDate(terms[i-1].Day, terms[i-1].Month, terms[i-1].Year)
		currJD := jdFromDate(terms[i].Day, terms[i].Month, terms[i].Year)
		if currJD <= prevJD {
			t.Errorf("Solar terms not in order: %s (%d/%d) >= %s (%d/%d)",
				terms[i-1].Name, terms[i-1].Day, terms[i-1].Month,
				terms[i].Name, terms[i].Day, terms[i].Month)
		}
	}
}
