package lunar

import "testing"

// TestSolarToLunar verifies known solar→lunar conversions.
func TestSolarToLunar(t *testing.T) {
	tests := []struct {
		name       string
		dd, mm, yy int
		wantDay    int
		wantMonth  int
		wantYear   int
		wantLeap   bool
	}{
		{
			name: "Tết Nguyên Đán 2025 (29/01/2025 = Mồng 1 tháng Giêng Ất Tỵ)",
			dd:   29, mm: 1, yy: 2025,
			wantDay: 1, wantMonth: 1, wantYear: 2025, wantLeap: false,
		},
		{
			name: "Rằm tháng Giêng 2025 (12/02/2025 = 15 tháng 1)",
			dd:   12, mm: 2, yy: 2025,
			wantDay: 15, wantMonth: 1, wantYear: 2025, wantLeap: false,
		},
		{
			name: "Giỗ Tổ Hùng Vương 2025 (7/4/2025 = 10 tháng 3 Ất Tỵ)",
			dd:   7, mm: 4, yy: 2025,
			wantDay: 10, wantMonth: 3, wantYear: 2025, wantLeap: false,
		},
		{
			name: "05/03/2026 = Mồng 17 tháng Giêng Bính Ngọ",
			dd:   5, mm: 3, yy: 2026,
			wantDay: 17, wantMonth: 1, wantYear: 2026, wantLeap: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := SolarToLunar(tt.dd, tt.mm, tt.yy, 7)
			if got.Day != tt.wantDay || got.Month != tt.wantMonth || got.Year != tt.wantYear || got.LeapMonth != tt.wantLeap {
				t.Errorf("SolarToLunar(%d/%d/%d) = %+v, want Day=%d Month=%d Year=%d Leap=%v",
					tt.dd, tt.mm, tt.yy, got, tt.wantDay, tt.wantMonth, tt.wantYear, tt.wantLeap)
			}
		})
	}
}

// TestLunarToSolar verifies known lunar→solar conversions.
func TestLunarToSolar(t *testing.T) {
	tests := []struct {
		name       string
		ld, lm, ly int
		leap       bool
		wantDay    int
		wantMonth  int
		wantYear   int
	}{
		{
			name: "Mồng 1 tháng Giêng 2025 → 29/01/2025",
			ld:   1, lm: 1, ly: 2025, leap: false,
			wantDay: 29, wantMonth: 1, wantYear: 2025,
		},
		{
			name: "15 tháng 1 năm 2025 → 12/02/2025",
			ld:   15, lm: 1, ly: 2025, leap: false,
			wantDay: 12, wantMonth: 2, wantYear: 2025,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := LunarToSolar(tt.ld, tt.lm, tt.ly, tt.leap, 7)
			if got.Day != tt.wantDay || got.Month != tt.wantMonth || got.Year != tt.wantYear {
				t.Errorf("LunarToSolar(%d/%d/%d) = %+v, want %d/%d/%d",
					tt.ld, tt.lm, tt.ly, got, tt.wantDay, tt.wantMonth, tt.wantYear)
			}
		})
	}
}

// TestRoundTrip verifies solar→lunar→solar produces the original date.
func TestRoundTrip(t *testing.T) {
	dates := []struct{ d, m, y int }{
		{1, 1, 2020},
		{15, 6, 2023},
		{28, 2, 2024},
		{4, 3, 2026},
		{31, 12, 2025},
	}
	for _, dt := range dates {
		lunar := SolarToLunar(dt.d, dt.m, dt.y, 7)
		solar := LunarToSolar(lunar.Day, lunar.Month, lunar.Year, lunar.LeapMonth, 7)
		if solar.Day != dt.d || solar.Month != dt.m || solar.Year != dt.y {
			t.Errorf("RoundTrip(%d/%d/%d): lunar=%+v solar=%+v", dt.d, dt.m, dt.y, lunar, solar)
		}
	}
}
