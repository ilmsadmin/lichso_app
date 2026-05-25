package canchi

import "testing"

func TestYearCanChi(t *testing.T) {
	tests := []struct {
		year int
		want string
	}{
		{1984, "Giáp Tý"},
		{2024, "Giáp Thìn"},
		{2025, "Ất Tỵ"},
		{2026, "Bính Ngọ"},
		{2000, "Canh Thìn"},
	}
	for _, tt := range tests {
		got := YearCanChi(tt.year)
		if got.CanChi != tt.want {
			t.Errorf("YearCanChi(%d) = %q, want %q", tt.year, got.CanChi, tt.want)
		}
	}
}

func TestDayCanChi(t *testing.T) {
	// Verify a known date: 05/03/2026
	got := DayCanChi(5, 3, 2026)
	t.Logf("05/03/2026 = %s", got.CanChi)
	// Just ensure it returns a valid Can Chi
	if got.Can == "" || got.Chi == "" {
		t.Error("DayCanChi returned empty values")
	}
}

func TestGetGioHoangDao(t *testing.T) {
	hours := GetGioHoangDao(5, 3, 2026)
	if len(hours) != 12 {
		t.Errorf("Expected 12 hours, got %d", len(hours))
	}

	// Check that at least some are Hoang Dao and some are not
	hdCount := 0
	for _, h := range hours {
		if h.IsHoangDao {
			hdCount++
		}
		if h.Name == "" || h.Range == "" || h.CanChi == "" {
			t.Errorf("Empty field in GioCanChi: %+v", h)
		}
	}
	if hdCount == 0 || hdCount == 12 {
		t.Errorf("Unexpected Hoang Dao count: %d (should be between 1 and 11)", hdCount)
	}
	t.Logf("Hoang Dao hours: %d/12", hdCount)
}

func TestHourIndex(t *testing.T) {
	tests := []struct {
		hour int
		want int // expected Dia Chi index
	}{
		{23, 0}, // Tý
		{0, 0},
		{1, 1}, // Sửu
		{2, 1},
		{11, 6}, // Ngọ
		{14, 7}, // Mùi
	}
	for _, tt := range tests {
		got := HourIndex(tt.hour)
		if got != tt.want {
			t.Errorf("HourIndex(%d) = %d, want %d (%s)", tt.hour, got, tt.want, DiaChi[tt.want])
		}
	}
}
