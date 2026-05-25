package events

import "testing"

func TestGetSolarEvents(t *testing.T) {
	tests := []struct {
		day, month int
		wantName   string
		wantCount  int
	}{
		{1, 1, "Tết Dương lịch", 1},
		{30, 4, "Ngày Giải phóng miền Nam", 1},
		{1, 5, "Ngày Quốc tế Lao động", 1},
		{2, 9, "Quốc Khánh", 1},
		{20, 11, "Ngày Nhà giáo Việt Nam", 1},
		{25, 12, "Giáng Sinh", 1},
		{15, 6, "", 0}, // No event
	}

	for _, tt := range tests {
		result := GetSolarEvents(tt.day, tt.month)
		if len(result) != tt.wantCount {
			t.Errorf("GetSolarEvents(%d, %d) count = %d, want %d", tt.day, tt.month, len(result), tt.wantCount)
			continue
		}
		if tt.wantCount > 0 && result[0].Name != tt.wantName {
			t.Errorf("GetSolarEvents(%d, %d) name = %q, want %q", tt.day, tt.month, result[0].Name, tt.wantName)
		}
	}
}

func TestGetLunarEvents(t *testing.T) {
	tests := []struct {
		lunarDay, lunarMonth int
		wantMinCount         int
		wantContains         string
	}{
		{1, 1, 2, "Tết Nguyên Đán — Mồng 1"}, // Mồng 1 Tết + Ngày Mùng 1 recurring
		{15, 8, 2, "Tết Trung Thu"},          // Trung Thu + Ngày Rằm recurring
		{10, 3, 1, "Giỗ Tổ Hùng Vương"},      // Hùng Vương only (not Rằm or Mùng 1)
		{15, 7, 2, "Lễ Vu Lan"},              // Vu Lan + Rằm
		{23, 12, 1, "Ông Công Ông Táo"},
		{5, 5, 1, "Tết Đoan Ngọ"},
		{15, 4, 2, "Lễ Phật Đản"}, // Phật Đản + Rằm
	}

	for _, tt := range tests {
		result := GetLunarEvents(tt.lunarDay, tt.lunarMonth)
		if len(result) < tt.wantMinCount {
			t.Errorf("GetLunarEvents(%d, %d) count = %d, want >= %d", tt.lunarDay, tt.lunarMonth, len(result), tt.wantMinCount)
		}
		found := false
		for _, e := range result {
			if e.Name == tt.wantContains {
				found = true
				break
			}
		}
		if !found {
			t.Errorf("GetLunarEvents(%d, %d) missing %q", tt.lunarDay, tt.lunarMonth, tt.wantContains)
		}
	}
}

func TestGetAllEvents(t *testing.T) {
	// 1/1 solar + 1/1 lunar = Tết Dương + Tết Nguyên Đán + Mùng 1
	result := GetAllEvents(1, 1, 1, 1)
	if len(result) < 3 {
		t.Errorf("GetAllEvents(1,1,1,1) count = %d, want >= 3", len(result))
	}
}

func TestRecurringLunarEvents(t *testing.T) {
	// Ngày Rằm should appear for any month
	for m := 1; m <= 12; m++ {
		result := GetLunarEvents(15, m)
		found := false
		for _, e := range result {
			if e.Name == "Ngày Rằm" {
				found = true
				break
			}
		}
		if !found {
			t.Errorf("GetLunarEvents(15, %d) missing 'Ngày Rằm'", m)
		}
	}

	// Ngày Mùng 1 should appear for any month
	for m := 1; m <= 12; m++ {
		result := GetLunarEvents(1, m)
		found := false
		for _, e := range result {
			if e.Name == "Ngày Mùng 1" {
				found = true
				break
			}
		}
		if !found {
			t.Errorf("GetLunarEvents(1, %d) missing 'Ngày Mùng 1'", m)
		}
	}
}

func TestIsHoliday(t *testing.T) {
	tests := []struct {
		day, month int
		want       bool
	}{
		{1, 1, true},    // Tết Dương lịch
		{30, 4, true},   // Giải phóng
		{1, 5, true},    // Quốc tế Lao động
		{2, 9, true},    // Quốc Khánh
		{20, 11, false}, // Nhà giáo — not a day off
		{15, 6, false},  // Normal day
	}

	for _, tt := range tests {
		got := IsHoliday(tt.day, tt.month)
		if got != tt.want {
			t.Errorf("IsHoliday(%d, %d) = %v, want %v", tt.day, tt.month, got, tt.want)
		}
	}
}

func TestIsLunarHoliday(t *testing.T) {
	tests := []struct {
		day, month int
		want       bool
	}{
		{1, 1, true},   // Tết Nguyên Đán
		{10, 3, true},  // Giỗ Tổ Hùng Vương
		{15, 8, false}, // Trung Thu — not an official day off
		{30, 12, true}, // Giao Thừa
	}

	for _, tt := range tests {
		got := IsLunarHoliday(tt.day, tt.month)
		if got != tt.want {
			t.Errorf("IsLunarHoliday(%d, %d) = %v, want %v", tt.day, tt.month, got, tt.want)
		}
	}
}

func TestGetMonthSolarEvents(t *testing.T) {
	jan := GetMonthSolarEvents(1)
	if len(jan) == 0 {
		t.Error("GetMonthSolarEvents(1) should return at least 1 event (Tết Dương lịch)")
	}

	sep := GetMonthSolarEvents(9)
	found := false
	for _, e := range sep {
		if e.Name == "Quốc Khánh" {
			found = true
			break
		}
	}
	if !found {
		t.Error("GetMonthSolarEvents(9) missing 'Quốc Khánh'")
	}
}
