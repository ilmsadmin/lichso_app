package services

import (
	"crypto/sha256"
	"encoding/binary"
	"fmt"
	"math"
	"time"

	"github.com/zplus/lichso/internal/services/canchi"
	"go.uber.org/zap"
)

// ============================================
// Types
// ============================================

// ZodiacSign represents a Chinese zodiac animal
type ZodiacSign struct {
	Index     int    `json:"index"`     // 0-11
	Name      string `json:"name"`      // e.g. "Tý"
	Animal    string `json:"animal"`    // e.g. "Chuột"
	Emoji     string `json:"emoji"`     // e.g. "🐀"
	Character string `json:"character"` // Tính cách
}

// HoroscopeRating represents a 1-5 star rating for a category
type HoroscopeRating struct {
	Category string `json:"category"` // e.g. "Tài lộc"
	Stars    int    `json:"stars"`    // 1-5
	Advice   string `json:"advice"`   // Brief description
	Emoji    string `json:"emoji"`    // Category icon
}

// DailyHoroscope represents the complete daily horoscope for a zodiac sign
type DailyHoroscope struct {
	Date          string            `json:"date"`          // YYYY-MM-DD
	Zodiac        ZodiacSign        `json:"zodiac"`        // The zodiac sign
	Overall       int               `json:"overall"`       // Overall rating 1-5
	OverallText   string            `json:"overall_text"`  // Overall assessment
	Ratings       []HoroscopeRating `json:"ratings"`       // Category ratings
	LuckyColor    []string          `json:"lucky_color"`   // Lucky colors
	LuckyNumber   []int             `json:"lucky_number"`  // Lucky numbers
	LuckyHour     []string          `json:"lucky_hour"`    // Lucky hours
	Direction     string            `json:"direction"`     // Good direction
	Advice        string            `json:"advice"`        // Daily advice
	Compatibility string            `json:"compatibility"` // Best compatible sign today
}

// AllZodiacHoroscope is the horoscope for all 12 signs on a given date
type AllZodiacHoroscope struct {
	Date       string           `json:"date"`
	DayCanChi  string           `json:"day_can_chi"`
	Horoscopes []DailyHoroscope `json:"horoscopes"`
}

// LunarAge represents lunar age calculation result
type LunarAge struct {
	BirthYear    int    `json:"birth_year"`
	CurrentYear  int    `json:"current_year"`
	TuoiDuong    int    `json:"tuoi_duong"` // Solar age
	TuoiAm       int    `json:"tuoi_am"`    // Lunar age
	TuoiMu       int    `json:"tuoi_mu"`    // Vietnamese traditional age (tuổi mụ)
	ConGiap      string `json:"con_giap"`   // Chinese zodiac
	ConGiapEmoji string `json:"con_giap_emoji"`
	CanChi       string `json:"can_chi"`  // Can Chi of birth year
	NguHanh      string `json:"ngu_hanh"` // Five elements (Ngũ Hành)
	Menh         string `json:"menh"`     // Destiny element (Mệnh)
}

// GoodDayRequest represents a request for finding good days for a specific purpose
type GoodDayRequest struct {
	Year       int    `json:"year" validate:"required,min=1900,max=2100"`
	Month      int    `json:"month" validate:"required,min=1,max=12"`
	Purpose    string `json:"purpose" validate:"required"` // "cuoi_hoi", "dong_tho", "nhap_trach", "khai_truong", "xuat_hanh"
	BirthYear  int    `json:"birth_year,omitempty"`        // Optional: for compatibility check
	SpouseYear int    `json:"spouse_year,omitempty"`       // For wedding: spouse's birth year
}

// GoodDayResult represents a good day result for a specific purpose
type GoodDayResult struct {
	SolarDay   int      `json:"solar_day"`
	SolarMonth int      `json:"solar_month"`
	SolarYear  int      `json:"solar_year"`
	LunarDay   int      `json:"lunar_day"`
	LunarMonth int      `json:"lunar_month"`
	DayOfWeek  string   `json:"day_of_week"`
	DayCanChi  string   `json:"day_can_chi"`
	TrucNgay   string   `json:"truc_ngay"`
	Score      int      `json:"score"`    // 0-100
	Reasons    []string `json:"reasons"`  // Why this day is good
	ViecNen    []string `json:"viec_nen"` // Recommended activities
	GioTot     []string `json:"gio_tot"`  // Good hours
}

// ============================================
// Zodiac Data
// ============================================

var zodiacSigns = [12]ZodiacSign{
	{0, "Tý", "Chuột", "🐀", "Thông minh, nhanh nhẹn, lanh lợi"},
	{1, "Sửu", "Trâu", "🐂", "Kiên nhẫn, chăm chỉ, đáng tin cậy"},
	{2, "Dần", "Hổ", "🐅", "Dũng cảm, mạnh mẽ, quyết đoán"},
	{3, "Mão", "Mèo", "🐇", "Hiền hoà, khéo léo, tinh tế"},
	{4, "Thìn", "Rồng", "🐉", "Quyền lực, may mắn, tham vọng"},
	{5, "Tỵ", "Rắn", "🐍", "Khôn ngoan, bí ẩn, sâu sắc"},
	{6, "Ngọ", "Ngựa", "🐴", "Năng động, tự do, nhiệt tình"},
	{7, "Mùi", "Dê", "🐐", "Hiền lành, nghệ sĩ, nhạy cảm"},
	{8, "Thân", "Khỉ", "🐒", "Thông minh, linh hoạt, hài hước"},
	{9, "Dậu", "Gà", "🐓", "Chăm chỉ, trung thực, ngăn nắp"},
	{10, "Tuất", "Chó", "🐕", "Trung thành, chính trực, can đảm"},
	{11, "Hợi", "Heo", "🐖", "Rộng lượng, chân thành, hào phóng"},
}

// Tam Hợp (Triple Harmony) groups
var tamHop = [4][3]int{
	{0, 4, 8},  // Tý - Thìn - Thân (Thuỷ)
	{1, 5, 9},  // Sửu - Tỵ - Dậu (Kim)
	{2, 6, 10}, // Dần - Ngọ - Tuất (Hoả)
	{3, 7, 11}, // Mão - Mùi - Hợi (Mộc)
}

// Tương Xung (Clash) pairs
var tuongXung = [6][2]int{
	{0, 6},  // Tý - Ngọ
	{1, 7},  // Sửu - Mùi
	{2, 8},  // Dần - Thân
	{3, 9},  // Mão - Dậu
	{4, 10}, // Thìn - Tuất
	{5, 11}, // Tỵ - Hợi
}

// Ngũ Hành Nạp Âm (Destiny element by Can Chi)
var nguHanhNapAm = map[string]string{
	"Giáp Tý": "Kim", "Ất Sửu": "Kim",
	"Bính Dần": "Hoả", "Đinh Mão": "Hoả",
	"Mậu Thìn": "Mộc", "Kỷ Tỵ": "Mộc",
	"Canh Ngọ": "Thổ", "Tân Mùi": "Thổ",
	"Nhâm Thân": "Kim", "Quý Dậu": "Kim",
	"Giáp Tuất": "Hoả", "Ất Hợi": "Hoả",
	"Bính Tý": "Thuỷ", "Đinh Sửu": "Thuỷ",
	"Mậu Dần": "Thổ", "Kỷ Mão": "Thổ",
	"Canh Thìn": "Kim", "Tân Tỵ": "Kim",
	"Nhâm Ngọ": "Mộc", "Quý Mùi": "Mộc",
	"Giáp Thân": "Thuỷ", "Ất Dậu": "Thuỷ",
	"Bính Tuất": "Thổ", "Đinh Hợi": "Thổ",
	"Mậu Tý": "Hoả", "Kỷ Sửu": "Hoả",
	"Canh Dần": "Mộc", "Tân Mão": "Mộc",
	"Nhâm Thìn": "Thuỷ", "Quý Tỵ": "Thuỷ",
	"Giáp Ngọ": "Kim", "Ất Mùi": "Kim",
	"Bính Thân": "Hoả", "Đinh Dậu": "Hoả",
	"Mậu Tuất": "Mộc", "Kỷ Hợi": "Mộc",
	"Canh Tý": "Thổ", "Tân Sửu": "Thổ",
	"Nhâm Dần": "Kim", "Quý Mão": "Kim",
	"Giáp Thìn": "Hoả", "Ất Tỵ": "Hoả",
	"Bính Ngọ": "Thuỷ", "Đinh Mùi": "Thuỷ",
	"Mậu Thân": "Thổ", "Kỷ Dậu": "Thổ",
	"Canh Tuất": "Kim", "Tân Hợi": "Kim",
	"Nhâm Tý": "Mộc", "Quý Sửu": "Mộc",
	"Giáp Dần": "Thuỷ", "Ất Mão": "Thuỷ",
	"Bính Thìn": "Thổ", "Đinh Tỵ": "Thổ",
	"Mậu Ngọ": "Hoả", "Kỷ Mùi": "Hoả",
	"Canh Thân": "Mộc", "Tân Dậu": "Mộc",
	"Nhâm Tuất": "Thuỷ", "Quý Hợi": "Thuỷ",
}

var colorNames = []string{"Đỏ", "Cam", "Vàng", "Xanh lá", "Xanh dương", "Tím", "Hồng", "Trắng", "Nâu", "Đen"}
var directionNames = []string{"Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc", "Bắc", "Đông Bắc"}

var financeAdvice = []string{
	"Cơ hội tài chính tốt, nên đầu tư",
	"Thu nhập ổn định, tiết kiệm hợp lý",
	"Cẩn thận chi tiêu, tránh lãng phí",
	"Tài lộc hanh thông, gặp nhiều may mắn",
	"Nên thận trọng với các quyết định tài chính",
}

var loveAdvice = []string{
	"Quan hệ hài hoà, tránh cãi vã",
	"Tình cảm thăng hoa, hẹn hò vui vẻ",
	"Chú ý lắng nghe đối phương",
	"Tình duyên tốt, dễ gặp người hợp ý",
	"Cần kiên nhẫn trong chuyện tình cảm",
}

var healthAdvice = []string{
	"Chú ý giấc ngủ, tập thể dục đều",
	"Sức khoẻ tốt, năng lượng dồi dào",
	"Nên nghỉ ngơi, tránh làm việc quá sức",
	"Ăn uống lành mạnh, bổ sung vitamin",
	"Cẩn thận tai nạn nhỏ, chú ý an toàn",
}

var careerAdvice = []string{
	"Được cấp trên ủng hộ, thuận lợi công việc",
	"Nên chủ động đề xuất ý tưởng mới",
	"Hợp tác tốt với đồng nghiệp",
	"Thận trọng trong giao tiếp công sở",
	"Cơ hội thăng tiến, nắm bắt kịp thời",
}

var overallTexts = []string{
	"Ngày không thuận lợi, cần thận trọng",
	"Ngày bình thường, giữ tâm an tĩnh",
	"Ngày tương đối tốt, thuận lợi việc nhỏ",
	"Ngày khá thuận lợi, nhiều may mắn",
	"Ngày rất tốt, vạn sự như ý",
}

var dailyAdvicePhrases = []string{
	"Hôm nay thích hợp cho việc ký kết hợp đồng, bắt đầu dự án mới.",
	"Nên dành thời gian cho gia đình và người thân yêu.",
	"Tập trung vào công việc chính, tránh phân tán năng lượng.",
	"Ngày tốt để học hỏi kiến thức mới, mở mang tầm nhìn.",
	"Cẩn thận lời nói, tránh tranh cãi không cần thiết.",
	"Nên thiền định, giữ tâm thanh tịnh để đón nhận năng lượng tốt.",
	"Thời điểm tốt để bắt đầu những kế hoạch dài hạn.",
	"Chú ý sức khoẻ, dành thời gian tập luyện thể thao.",
}

// ============================================
// Service
// ============================================

// HoroscopeService computes daily horoscopes based on Can Chi system
type HoroscopeService struct {
	logger *zap.Logger
}

// NewHoroscopeService creates a new HoroscopeService
func NewHoroscopeService(logger *zap.Logger) *HoroscopeService {
	return &HoroscopeService{logger: logger}
}

// GetDailyHoroscope returns the horoscope for a specific zodiac sign on a given date
func (s *HoroscopeService) GetDailyHoroscope(dd, mm, yy int, zodiacIdx int) DailyHoroscope {
	if zodiacIdx < 0 || zodiacIdx > 11 {
		zodiacIdx = 0
	}

	dateStr := fmt.Sprintf("%04d-%02d-%02d", yy, mm, dd)
	sign := zodiacSigns[zodiacIdx]
	dayCC := canchi.DayCanChi(dd, mm, yy)

	// Deterministic seed based on date + zodiac
	seed := hashSeed(dateStr, zodiacIdx)

	// Calculate compatibility score between day's DiaChi and zodiac
	dayChiIdx := diaChiIndex(dayCC.Chi)
	compat := calculateCompatibility(dayChiIdx, zodiacIdx)

	// Overall rating (1-5) based on compatibility
	overall := 1 + int(math.Round(float64(compat)*4.0/100.0))
	if overall > 5 {
		overall = 5
	}
	if overall < 1 {
		overall = 1
	}

	// Generate category ratings
	ratings := []HoroscopeRating{
		{Category: "Tài lộc", Stars: clampStars(seededRating(seed, 0, compat)), Advice: financeAdvice[seededIdx(seed, 1, len(financeAdvice))], Emoji: "💰"},
		{Category: "Tình cảm", Stars: clampStars(seededRating(seed, 2, compat)), Advice: loveAdvice[seededIdx(seed, 3, len(loveAdvice))], Emoji: "💕"},
		{Category: "Sức khoẻ", Stars: clampStars(seededRating(seed, 4, compat)), Advice: healthAdvice[seededIdx(seed, 5, len(healthAdvice))], Emoji: "💪"},
		{Category: "Sự nghiệp", Stars: clampStars(seededRating(seed, 6, compat)), Advice: careerAdvice[seededIdx(seed, 7, len(careerAdvice))], Emoji: "💼"},
	}

	// Lucky colors (2 colors)
	c1 := colorNames[seededIdx(seed, 10, len(colorNames))]
	c2 := colorNames[(seededIdx(seed, 11, len(colorNames))+1)%len(colorNames)]
	if c1 == c2 {
		c2 = colorNames[(seededIdx(seed, 11, len(colorNames))+2)%len(colorNames)]
	}

	// Lucky numbers (3 numbers 1-99)
	n1 := 1 + seededIdx(seed, 20, 99)
	n2 := 1 + seededIdx(seed, 21, 99)
	n3 := 1 + seededIdx(seed, 22, 99)

	// Lucky hours from Giờ Hoàng Đạo
	gioHD := canchi.GetGioHoangDao(dd, mm, yy)
	var luckyHours []string
	for _, g := range gioHD {
		if g.IsHoangDao {
			luckyHours = append(luckyHours, fmt.Sprintf("%s (%s)", g.Name, g.Range))
			if len(luckyHours) >= 3 {
				break
			}
		}
	}

	// Direction
	direction := directionNames[seededIdx(seed, 30, len(directionNames))]

	// Find best compatible sign
	bestCompat := findBestCompatible(zodiacIdx)

	return DailyHoroscope{
		Date:          dateStr,
		Zodiac:        sign,
		Overall:       overall,
		OverallText:   overallTexts[overall-1],
		Ratings:       ratings,
		LuckyColor:    []string{c1, c2},
		LuckyNumber:   []int{n1, n2, n3},
		LuckyHour:     luckyHours,
		Direction:     direction,
		Advice:        dailyAdvicePhrases[seededIdx(seed, 40, len(dailyAdvicePhrases))],
		Compatibility: zodiacSigns[bestCompat].Name + " (" + zodiacSigns[bestCompat].Emoji + ")",
	}
}

// GetAllZodiacHoroscope returns horoscopes for all 12 signs on a given date
func (s *HoroscopeService) GetAllZodiacHoroscope(dd, mm, yy int) AllZodiacHoroscope {
	dateStr := fmt.Sprintf("%04d-%02d-%02d", yy, mm, dd)
	dayCC := canchi.DayCanChi(dd, mm, yy)

	horoscopes := make([]DailyHoroscope, 12)
	for i := 0; i < 12; i++ {
		horoscopes[i] = s.GetDailyHoroscope(dd, mm, yy, i)
	}

	return AllZodiacHoroscope{
		Date:       dateStr,
		DayCanChi:  dayCC.CanChi,
		Horoscopes: horoscopes,
	}
}

// GetZodiacFromYear returns the zodiac index for a given birth year (lunar year)
func (s *HoroscopeService) GetZodiacFromYear(year int) int {
	return ((year+8)%12 + 12) % 12
}

// CalculateLunarAge calculates the lunar age and related information
func (s *HoroscopeService) CalculateLunarAge(birthYear int) LunarAge {
	now := time.Now()
	currentYear := now.Year()

	yearCC := canchi.YearCanChi(birthYear)
	zodiacIdx := ((birthYear+8)%12 + 12) % 12

	// Ngũ Hành Nạp Âm
	menh := nguHanhNapAm[yearCC.CanChi]
	if menh == "" {
		menh = yearCC.NguHanh // Fallback to Thiên Can's Ngũ Hành
	}

	tuoiDuong := currentYear - birthYear
	tuoiAm := tuoiDuong + 1 // Lunar age = solar age + 1 (Vietnamese tradition)
	tuoiMu := tuoiAm        // Tuổi mụ = tuổi âm (in most cases)

	return LunarAge{
		BirthYear:    birthYear,
		CurrentYear:  currentYear,
		TuoiDuong:    tuoiDuong,
		TuoiAm:       tuoiAm,
		TuoiMu:       tuoiMu,
		ConGiap:      zodiacSigns[zodiacIdx].Animal,
		ConGiapEmoji: zodiacSigns[zodiacIdx].Emoji,
		CanChi:       yearCC.CanChi,
		NguHanh:      yearCC.NguHanh,
		Menh:         menh,
	}
}

// ============================================
// Helpers
// ============================================

func hashSeed(dateStr string, zodiacIdx int) uint64 {
	h := sha256.New()
	h.Write([]byte(fmt.Sprintf("%s:%d", dateStr, zodiacIdx)))
	sum := h.Sum(nil)
	return binary.BigEndian.Uint64(sum[:8])
}

func seededIdx(seed uint64, offset int, max int) int {
	if max <= 0 {
		return 0
	}
	return int((seed/uint64(offset+1) + uint64(offset*17)) % uint64(max))
}

func seededRating(seed uint64, offset int, baseCompat int) int {
	// Base rating from compatibility (0-100 → 1-5)
	base := 1 + baseCompat*4/100
	// Add small variation
	variation := int(seed/uint64(offset+1))%3 - 1 // -1, 0, or 1
	return base + variation
}

func clampStars(v int) int {
	if v < 1 {
		return 1
	}
	if v > 5 {
		return 5
	}
	return v
}

func diaChiIndex(chi string) int {
	for i, dc := range canchi.DiaChi {
		if dc == chi {
			return i
		}
	}
	return 0
}

func calculateCompatibility(dayChiIdx, zodiacIdx int) int {
	score := 50 // Base score

	// Check Tam Hợp (Triple Harmony) — very good
	for _, group := range tamHop {
		inGroup := false
		zodiacInGroup := false
		for _, g := range group {
			if g == dayChiIdx {
				inGroup = true
			}
			if g == zodiacIdx {
				zodiacInGroup = true
			}
		}
		if inGroup && zodiacInGroup {
			score += 30
		}
	}

	// Check Tương Xung (Clash) — bad
	for _, pair := range tuongXung {
		if (pair[0] == dayChiIdx && pair[1] == zodiacIdx) ||
			(pair[1] == dayChiIdx && pair[0] == zodiacIdx) {
			score -= 30
		}
	}

	// Check Lục Hợp (Six Harmony) — good
	lucHop := [][2]int{{0, 1}, {2, 11}, {3, 10}, {4, 9}, {5, 8}, {6, 7}}
	for _, pair := range lucHop {
		if (pair[0] == dayChiIdx && pair[1] == zodiacIdx) ||
			(pair[1] == dayChiIdx && pair[0] == zodiacIdx) {
			score += 20
		}
	}

	// Clamp to 0-100
	if score < 0 {
		score = 0
	}
	if score > 100 {
		score = 100
	}

	return score
}

func findBestCompatible(zodiacIdx int) int {
	// Find from Tam Hợp group
	for _, group := range tamHop {
		for _, g := range group {
			if g == zodiacIdx {
				// Return another member of the same Tam Hợp
				for _, g2 := range group {
					if g2 != zodiacIdx {
						return g2
					}
				}
			}
		}
	}
	return (zodiacIdx + 4) % 12 // Fallback
}
