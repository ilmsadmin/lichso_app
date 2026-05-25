package phongthuy

import "testing"

func TestGetTrucNgay(t *testing.T) {
	// Test all 12 Trực for a given lunar month
	for dayChiIdx := 0; dayChiIdx < 12; dayChiIdx++ {
		truc := GetTrucNgay(1, dayChiIdx)
		if truc.Name == "" || truc.DanhGia == "" || truc.MoTa == "" {
			t.Errorf("GetTrucNgay(1, %d) returned empty fields: %+v", dayChiIdx, truc)
		}
	}

	// Trực Kiến starts at the month's Dia Chi
	// Lunar month 1 (Giêng) = Dần (index 2)
	truc := GetTrucNgay(1, 2) // dayChiIdx = 2 (Dần) → Kiến
	if truc.Name != "Kiến" {
		t.Errorf("Expected Truc Kien for month 1, dayChiIdx 2, got %s", truc.Name)
	}
}

func TestGetSaoChieuMenh(t *testing.T) {
	sao := GetSaoChieuMenh(5, 3, 2026)
	if sao.Name == "" {
		t.Error("GetSaoChieuMenh returned empty name")
	}
	if sao.TotXau != "Tốt" && sao.TotXau != "Xấu" {
		t.Errorf("Unexpected TotXau value: %s", sao.TotXau)
	}
	t.Logf("05/03/2026 sao: %s (%s)", sao.Name, sao.TotXau)
}

func TestGetHuongXuatHanh(t *testing.T) {
	huong := GetHuongXuatHanh(0, 0)
	if huong.TaiThan == "" || huong.HyThan == "" {
		t.Error("GetHuongXuatHanh returned empty directions")
	}
	if len(huong.HuongTot) == 0 {
		t.Error("HuongTot should not be empty")
	}
	if len(huong.HuongXau) == 0 {
		t.Error("HuongXau should not be empty")
	}

	// Test all 10 Can x 12 Chi combinations
	for can := 0; can < 10; can++ {
		for chi := 0; chi < 12; chi++ {
			h := GetHuongXuatHanh(can, chi)
			if h.TaiThan == "" || h.HyThan == "" {
				t.Errorf("Empty direction for can=%d chi=%d", can, chi)
			}
		}
	}
}

func TestGetMoonPhase(t *testing.T) {
	tests := []struct {
		lunarDay int
		phase    string
	}{
		{1, "new_moon"},
		{3, "waxing_crescent"},
		{7, "first_quarter"},
		{11, "waxing_gibbous"},
		{15, "full_moon"},
		{18, "waning_gibbous"},
		{22, "last_quarter"},
		{28, "waning_crescent"},
	}

	for _, tt := range tests {
		got := GetMoonPhase(tt.lunarDay)
		if got.Phase != tt.phase {
			t.Errorf("GetMoonPhase(%d) = %s, want %s", tt.lunarDay, got.Phase, tt.phase)
		}
		if got.Emoji == "" || got.Desc == "" {
			t.Errorf("GetMoonPhase(%d) has empty emoji or desc", tt.lunarDay)
		}
	}
}

func TestGetViecNenKhong(t *testing.T) {
	// Good day (Kiến = index 0)
	nen, khong := GetViecNenKhong(0, "Tốt")
	if len(nen) == 0 {
		t.Error("Good day should have viec nen")
	}
	if len(khong) == 0 {
		t.Error("Good day should have some viec khong")
	}

	// Bad day (Phá = index 6)
	nen, khong = GetViecNenKhong(6, "Xấu")
	if len(khong) == 0 {
		t.Error("Bad day should have viec khong")
	}
	t.Logf("Bad day: nen=%d, khong=%d", len(nen), len(khong))
}

func TestCalculateDayScore(t *testing.T) {
	// Good Truc + Good Star + Hoang Dao hour → high score
	score := CalculateDayScore(0, "Tốt", true) // Kiến, Tốt, HoangDao
	if score < 70 {
		t.Errorf("Expected high score for good day, got %d", score)
	}

	// Bad Truc + Bad Star → low score
	score = CalculateDayScore(6, "Xấu", false) // Phá, Xấu
	if score > 50 {
		t.Errorf("Expected low score for bad day, got %d", score)
	}

	// Score should be clamped to 0-100
	for i := 0; i < 12; i++ {
		for _, star := range []string{"Tốt", "Xấu"} {
			for _, hd := range []bool{true, false} {
				s := CalculateDayScore(i, star, hd)
				if s < 0 || s > 100 {
					t.Errorf("Score out of range: trucIdx=%d star=%s hd=%v → %d", i, star, hd, s)
				}
			}
		}
	}
}

func TestGetDayInfo(t *testing.T) {
	info := GetDayInfo(5, 3, 2026, 17, 1, 2026)

	if info.ChiSoNgay < 0 || info.ChiSoNgay > 100 {
		t.Errorf("ChiSoNgay out of range: %d", info.ChiSoNgay)
	}
	if info.DanhGia == "" {
		t.Error("DanhGia is empty")
	}
	if info.TrucNgay.Name == "" {
		t.Error("TrucNgay.Name is empty")
	}
	if info.SaoChieu.Name == "" {
		t.Error("SaoChieu.Name is empty")
	}
	if info.MoonPhase.Phase == "" {
		t.Error("MoonPhase.Phase is empty")
	}
	if len(info.ViecNen) == 0 {
		t.Error("ViecNen should not be empty")
	}

	t.Logf("DayInfo 05/03/2026: score=%d%% (%s), truc=%s, sao=%s, moon=%s",
		info.ChiSoNgay, info.DanhGia, info.TrucNgay.Name, info.SaoChieu.Name, info.MoonPhase.Emoji)
}
