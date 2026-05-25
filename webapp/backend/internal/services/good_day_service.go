package services

import (
	"fmt"
	"time"

	"github.com/zplus/lichso/internal/services/canchi"
	"github.com/zplus/lichso/internal/services/lunar"
	"github.com/zplus/lichso/internal/services/phongthuy"
	"go.uber.org/zap"
)

// ============================================
// Types
// ============================================

// PurposeGoodDay represents a good day for a specific purpose
type PurposeGoodDay struct {
	SolarDay   int      `json:"solar_day"`
	SolarMonth int      `json:"solar_month"`
	SolarYear  int      `json:"solar_year"`
	LunarDay   int      `json:"lunar_day"`
	LunarMonth int      `json:"lunar_month"`
	DayOfWeek  string   `json:"day_of_week"`
	DayCanChi  string   `json:"day_can_chi"`
	TrucNgay   string   `json:"truc_ngay"`
	Score      int      `json:"score"`
	Reasons    []string `json:"reasons"`
	ViecNen    []string `json:"viec_nen"`
	GioTot     []string `json:"gio_tot"`
}

// PurposeGoodDaysResult contains all good days for a purpose in a given month
type PurposeGoodDaysResult struct {
	Year        int              `json:"year"`
	Month       int              `json:"month"`
	Purpose     string           `json:"purpose"`
	PurposeName string           `json:"purpose_name"`
	GoodDays    []PurposeGoodDay `json:"good_days"`
	Total       int              `json:"total"`
}

// PurposeInfo describes a purpose for looking up good days
type PurposeInfo struct {
	Key          string   `json:"key"`
	Name         string   `json:"name"`
	Emoji        string   `json:"emoji"`
	GoodTruc     []string // Trực ngày tốt cho mục đích này
	BadTruc      []string // Trực ngày xấu
	MinChiSoNgay int      // Minimum day quality score
}

// ============================================
// Purpose Definitions
// ============================================

var purposes = map[string]PurposeInfo{
	"cuoi_hoi": {
		Key:          "cuoi_hoi",
		Name:         "Cưới hỏi",
		Emoji:        "💍",
		GoodTruc:     []string{"Thành", "Khai", "Mãn"},
		BadTruc:      []string{"Phá", "Nguy", "Bế"},
		MinChiSoNgay: 60,
	},
	"dong_tho": {
		Key:          "dong_tho",
		Name:         "Động thổ / Xây dựng",
		Emoji:        "🏗️",
		GoodTruc:     []string{"Khai", "Kiến", "Thành"},
		BadTruc:      []string{"Phá", "Nguy", "Thu"},
		MinChiSoNgay: 55,
	},
	"nhap_trach": {
		Key:          "nhap_trach",
		Name:         "Nhập trạch (Dọn về nhà mới)",
		Emoji:        "🏠",
		GoodTruc:     []string{"Mãn", "Thành", "Khai"},
		BadTruc:      []string{"Phá", "Nguy", "Bế"},
		MinChiSoNgay: 60,
	},
	"khai_truong": {
		Key:          "khai_truong",
		Name:         "Khai trương",
		Emoji:        "🏪",
		GoodTruc:     []string{"Khai", "Thành", "Mãn"},
		BadTruc:      []string{"Bế", "Phá", "Nguy"},
		MinChiSoNgay: 60,
	},
	"xuat_hanh": {
		Key:          "xuat_hanh",
		Name:         "Xuất hành",
		Emoji:        "🚗",
		GoodTruc:     []string{"Khai", "Thành", "Kiến"},
		BadTruc:      []string{"Phá", "Nguy"},
		MinChiSoNgay: 50,
	},
	"ky_hop_dong": {
		Key:          "ky_hop_dong",
		Name:         "Ký hợp đồng",
		Emoji:        "📋",
		GoodTruc:     []string{"Thành", "Bình", "Khai"},
		BadTruc:      []string{"Phá", "Nguy"},
		MinChiSoNgay: 55,
	},
	"du_lich": {
		Key:          "du_lich",
		Name:         "Du lịch",
		Emoji:        "✈️",
		GoodTruc:     []string{"Khai", "Thành", "Mãn"},
		BadTruc:      []string{"Phá", "Nguy"},
		MinChiSoNgay: 50,
	},
}

var dayOfWeekVN = [7]string{"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"}

// Vietnam timezone
var vnTZ = 7.0

// ============================================
// Service
// ============================================

// GoodDayService provides enhanced good day lookups for specific purposes
type GoodDayService struct {
	horoscopeService *HoroscopeService
	logger           *zap.Logger
}

// NewGoodDayService creates a new GoodDayService
func NewGoodDayService(horoscopeService *HoroscopeService, logger *zap.Logger) *GoodDayService {
	return &GoodDayService{
		horoscopeService: horoscopeService,
		logger:           logger,
	}
}

// GetPurposes returns all available purpose types
func (s *GoodDayService) GetPurposes() []PurposeInfo {
	result := make([]PurposeInfo, 0, len(purposes))
	for _, p := range purposes {
		result = append(result, p)
	}
	return result
}

// GetGoodDaysForPurpose returns good days for a specific purpose in a given month
func (s *GoodDayService) GetGoodDaysForPurpose(year, month int, purpose string, birthYear, spouseYear int) PurposeGoodDaysResult {
	purposeInfo, ok := purposes[purpose]
	if !ok {
		purposeInfo = purposes["xuat_hanh"] // default fallback
	}

	daysInMonth := time.Date(year, time.Month(month+1), 0, 0, 0, 0, 0, time.UTC).Day()
	var goodDays []PurposeGoodDay

	for d := 1; d <= daysInMonth; d++ {
		t := time.Date(year, time.Month(month), d, 12, 0, 0, 0, time.UTC)
		dow := int(t.Weekday())
		lunarDate := lunar.SolarToLunar(d, month, year, vnTZ)
		phongThuyInfo := phongthuy.GetDayInfo(d, month, year, lunarDate.Day, lunarDate.Month, lunarDate.Year)
		dayCC := canchi.DayCanChi(d, month, year)

		score, reasons := s.evaluateDay(purposeInfo, phongThuyInfo, dayCC, dow, lunarDate, birthYear, spouseYear)

		if score >= purposeInfo.MinChiSoNgay {
			// Get good hours
			gioHD := canchi.GetGioHoangDao(d, month, year)
			var gioTot []string
			for _, g := range gioHD {
				if g.IsHoangDao {
					gioTot = append(gioTot, fmt.Sprintf("%s (%s)", g.Name, g.Range))
				}
			}

			goodDays = append(goodDays, PurposeGoodDay{
				SolarDay:   d,
				SolarMonth: month,
				SolarYear:  year,
				LunarDay:   lunarDate.Day,
				LunarMonth: lunarDate.Month,
				DayOfWeek:  dayOfWeekVN[dow],
				DayCanChi:  dayCC.CanChi,
				TrucNgay:   phongThuyInfo.TrucNgay.Name,
				Score:      score,
				Reasons:    reasons,
				ViecNen:    phongThuyInfo.ViecNen,
				GioTot:     gioTot,
			})
		}
	}

	return PurposeGoodDaysResult{
		Year:        year,
		Month:       month,
		Purpose:     purpose,
		PurposeName: purposeInfo.Emoji + " " + purposeInfo.Name,
		GoodDays:    goodDays,
		Total:       len(goodDays),
	}
}

// evaluateDay scores a day for a specific purpose (0-100)
func (s *GoodDayService) evaluateDay(purpose PurposeInfo, dayInfo phongthuy.DayInfo, dayCC canchi.CanChi, dow int, lunarDate lunar.LunarDate, birthYear, spouseYear int) (int, []string) {
	score := 0
	var reasons []string

	// 1. Base score from Chỉ Số Ngày (phong thuỷ)
	baseScore := dayInfo.ChiSoNgay * 40 / 100 // 0-40 points
	score += baseScore

	// 2. Trực ngày compatibility
	trucName := dayInfo.TrucNgay.Name
	for _, good := range purpose.GoodTruc {
		if trucName == good {
			score += 25
			reasons = append(reasons, fmt.Sprintf("Trực %s — rất phù hợp cho %s", trucName, purpose.Name))
			break
		}
	}
	for _, bad := range purpose.BadTruc {
		if trucName == bad {
			score -= 20
			reasons = append(reasons, fmt.Sprintf("Trực %s — không thuận lợi", trucName))
			break
		}
	}

	// 3. Sao chiếu is good
	if dayInfo.SaoChieu.TotXau == "Tốt" {
		score += 10
		reasons = append(reasons, fmt.Sprintf("Sao %s chiếu — %s", dayInfo.SaoChieu.Name, "sao tốt"))
	}

	// 4. Day of week bonus (weekdays often preferred for ceremonies)
	if purpose.Key == "cuoi_hoi" || purpose.Key == "khai_truong" {
		if dow == 0 || dow == 6 { // Weekend
			score += 5
			reasons = append(reasons, "Cuối tuần — thuận tiện tổ chức")
		}
	}

	// 5. Birth year compatibility (for wedding)
	if birthYear > 0 && (purpose.Key == "cuoi_hoi" || purpose.Key == "nhap_trach") {
		zodiacIdx := ((birthYear+8)%12 + 12) % 12
		dayChiIdx := diaChiIndex(dayCC.Chi)
		compat := calculateCompatibility(dayChiIdx, zodiacIdx)
		compatScore := compat * 15 / 100 // 0-15 points
		score += compatScore
		if compat >= 70 {
			reasons = append(reasons, "Ngày hợp tuổi chủ nhà/chú rể")
		}
	}

	if spouseYear > 0 && purpose.Key == "cuoi_hoi" {
		zodiacIdx := ((spouseYear+8)%12 + 12) % 12
		dayChiIdx := diaChiIndex(dayCC.Chi)
		compat := calculateCompatibility(dayChiIdx, zodiacIdx)
		compatScore := compat * 10 / 100 // 0-10 points
		score += compatScore
		if compat >= 70 {
			reasons = append(reasons, "Ngày hợp tuổi cô dâu")
		}
	}

	// Clamp score
	if score < 0 {
		score = 0
	}
	if score > 100 {
		score = 100
	}

	if len(reasons) == 0 && score >= purpose.MinChiSoNgay {
		reasons = append(reasons, fmt.Sprintf("Chỉ số ngày %d/100 — %s", dayInfo.ChiSoNgay, dayInfo.DanhGia))
	}

	return score, reasons
}
