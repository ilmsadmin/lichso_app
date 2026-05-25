// Package phongthuy implements Vietnamese Feng Shui calculations.
package phongthuy

import (
	"math"
	"time"
)

// ============================================
// Types
// ============================================

// TrucNgay represents one of the 12 Trực (Day Officers).
type TrucNgay struct {
	Name    string `json:"name"`     // e.g. "Kiến"
	DanhGia string `json:"danh_gia"` // "Tốt" / "Xấu" / "Bình thường"
	MoTa    string `json:"mo_ta"`    // Description
}

// HuongXuatHanh represents directional feng shui info.
type HuongXuatHanh struct {
	TaiThan  string   `json:"tai_than"`  // God of Wealth direction
	HyThan   string   `json:"hy_than"`   // God of Happiness direction
	HacThan  string   `json:"hac_than"`  // Malevolent direction
	HuongTot []string `json:"huong_tot"` // Good directions
	HuongXau []string `json:"huong_xau"` // Bad directions
}

// SaoChieuMenh represents the star governing the day.
type SaoChieuMenh struct {
	Name   string `json:"name"`    // Star name
	TotXau string `json:"tot_xau"` // "Tốt" or "Xấu"
	MoTa   string `json:"mo_ta"`   // Description
}

// MoonPhase represents the moon phase info.
type MoonPhase struct {
	Phase string `json:"phase"` // Internal phase name
	Emoji string `json:"emoji"` // Moon emoji
	Desc  string `json:"desc"`  // Vietnamese description
}

// DayInfo represents the complete feng shui information for a day.
type DayInfo struct {
	ChiSoNgay     int           `json:"chi_so_ngay"`     // Day quality score (0-100)
	DanhGia       string        `json:"danh_gia"`        // Overall assessment
	TrucNgay      TrucNgay      `json:"truc_ngay"`       // Day Officer
	HuongXuatHanh HuongXuatHanh `json:"huong_xuat_hanh"` // Directions
	SaoChieu      SaoChieuMenh  `json:"sao_chieu"`       // Governing star
	MoonPhase     MoonPhase     `json:"moon_phase"`      // Moon phase
	ViecNen       []string      `json:"viec_nen"`        // Things to do
	ViecKhong     []string      `json:"viec_khong"`      // Things to avoid
}

// ============================================
// 12 Trực (Day Officers)
// ============================================

var trucNgayList = [12]TrucNgay{
	{"Kiến", "Tốt", "Ngày khởi đầu, thích hợp khai trương, khởi công"},
	{"Trừ", "Tốt", "Ngày trừ bỏ, tốt cho dọn dẹp, trị bệnh, tẩy trần"},
	{"Mãn", "Tốt", "Ngày đầy đủ, tốt cho cầu tài, khai trương, giao dịch"},
	{"Bình", "Bình thường", "Ngày bình thường, nên bình tĩnh xử lý việc"},
	{"Định", "Tốt", "Ngày ổn định, tốt cho ký kết, hôn nhân, nhập trạch"},
	{"Chấp", "Bình thường", "Ngày nắm giữ, tốt cho xây dựng, trồng trọt"},
	{"Phá", "Xấu", "Ngày phá hoại, không nên khởi đầu việc lớn"},
	{"Nguy", "Bình thường", "Ngày nguy hiểm, cẩn thận trong mọi việc"},
	{"Thành", "Tốt", "Ngày thành tựu, tốt cho mọi việc lớn"},
	{"Thu", "Bình thường", "Ngày thu nạp, tốt cho thu hoạch, nhận tiền"},
	{"Khai", "Tốt", "Ngày khai thông, tốt cho khai trương, xuất hành"},
	{"Bế", "Xấu", "Ngày đóng cửa, không nên xuất hành, khởi công"},
}

// GetTrucNgay returns the Trực of the day based on lunar month and day's Dia Chi.
func GetTrucNgay(lunarMonth int, dayChiIdx int) TrucNgay {
	// Trực Kiến starts at the month's Dia Chi.
	// Lunar month 1 (Giêng) = Dần (index 2)
	monthChiIdx := (lunarMonth + 1) % 12
	trucIdx := (dayChiIdx - monthChiIdx + 12) % 12
	return trucNgayList[trucIdx]
}

// ============================================
// 28 Sao (28 Mansions / Star Constellations)
// ============================================

type Sao28 struct {
	Name   string
	TotXau string
	MoTa   string
}

var sao28List = [28]Sao28{
	{"Giác", "Tốt", "Sao Giác - Cát tinh, tốt cho xây dựng, cưới hỏi"},
	{"Cang", "Xấu", "Sao Cang - Hung tinh, không nên khởi công"},
	{"Đê", "Xấu", "Sao Đê - Hung tinh, không nên xuất hành"},
	{"Phòng", "Tốt", "Sao Phòng - Cát tinh, tốt cho hôn lễ, nhập trạch"},
	{"Tâm", "Xấu", "Sao Tâm - Hung tinh, cẩn thận tai nạn"},
	{"Vĩ", "Tốt", "Sao Vĩ - Cát tinh, tốt cho cưới hỏi, xây dựng"},
	{"Cơ", "Tốt", "Sao Cơ - Cát tinh, tốt cho cầu tài, giao dịch"},
	{"Đẩu", "Tốt", "Sao Đẩu - Cát tinh, tốt cho khởi công, động thổ"},
	{"Ngưu", "Xấu", "Sao Ngưu - Hung tinh, không nên cưới hỏi"},
	{"Nữ", "Xấu", "Sao Nữ - Hung tinh, không nên khởi sự"},
	{"Hư", "Xấu", "Sao Hư - Hung tinh, không nên giao dịch"},
	{"Nguy", "Xấu", "Sao Nguy - Hung tinh, cẩn thận mọi việc"},
	{"Thất", "Tốt", "Sao Thất - Cát tinh, tốt cho xây dựng, sửa chữa"},
	{"Bích", "Tốt", "Sao Bích - Cát tinh, tốt cho khai trương, nhập trạch"},
	{"Khuê", "Xấu", "Sao Khuê - Hung tinh, không nên xuất hành"},
	{"Lâu", "Tốt", "Sao Lâu - Cát tinh, tốt cho cưới hỏi, khai trương"},
	{"Vị", "Tốt", "Sao Vị - Cát tinh, tốt cho xây dựng, giao dịch"},
	{"Mão", "Xấu", "Sao Mão - Hung tinh, không nên khởi công"},
	{"Tất", "Tốt", "Sao Tất - Cát tinh, tốt cho cầu tài, xuất hành"},
	{"Chủy", "Xấu", "Sao Chủy - Hung tinh, cẩn thận kiện tụng"},
	{"Sâm", "Xấu", "Sao Sâm - Hung tinh, không nên cưới hỏi"},
	{"Tỉnh", "Tốt", "Sao Tỉnh - Cát tinh, tốt cho xây dựng, nhập trạch"},
	{"Quỷ", "Xấu", "Sao Quỷ - Hung tinh, không nên xuất hành"},
	{"Liễu", "Xấu", "Sao Liễu - Hung tinh, không nên khởi sự"},
	{"Tinh", "Xấu", "Sao Tinh - Hung tinh, cẩn thận hỏa hoạn"},
	{"Trương", "Tốt", "Sao Trương - Cát tinh, tốt cho cưới hỏi, khai trương"},
	{"Dực", "Xấu", "Sao Dực - Hung tinh, không nên xuất hành"},
	{"Chẩn", "Tốt", "Sao Chẩn - Cát tinh, tốt cho mọi việc"},
}

// GetSaoChieuMenh returns the governing star for a given solar date.
func GetSaoChieuMenh(dd, mm, yy int) SaoChieuMenh {
	jd := jdFromDate(dd, mm, yy)
	idx := (jd + 15) % 28 // offset for alignment
	if idx < 0 {
		idx += 28
	}
	sao := sao28List[idx]
	return SaoChieuMenh{
		Name:   sao.Name,
		TotXau: sao.TotXau,
		MoTa:   sao.MoTa,
	}
}

// ============================================
// Hướng Xuất Hành (Travel Direction)
// ============================================

var directions = []string{"Bắc", "Đông Bắc", "Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc"}

// GetHuongXuatHanh calculates feng shui directions for a day based on day's Can Chi.
func GetHuongXuatHanh(dayCanIdx, dayChiIdx int) HuongXuatHanh {
	// Tài thần (God of Wealth) direction based on day's Can
	taiThanMap := [10]string{"Đông Nam", "Đông", "Bắc", "Bắc", "Đông Bắc", "Nam", "Tây Nam", "Tây", "Tây Bắc", "Nam"}
	// Hỷ thần (God of Happiness) direction based on day's Can
	hyThanMap := [10]string{"Đông Bắc", "Tây Bắc", "Tây Nam", "Nam", "Đông Nam", "Đông Bắc", "Tây Bắc", "Tây Nam", "Nam", "Đông Nam"}
	// Hắc thần (Malevolent) direction based on day's Chi
	hacThanMap := [12]string{"Nam", "Đông", "Bắc", "Tây", "Nam", "Đông", "Bắc", "Tây", "Nam", "Đông", "Bắc", "Tây"}

	taiThan := taiThanMap[dayCanIdx]
	hyThan := hyThanMap[dayCanIdx]
	hacThan := hacThanMap[dayChiIdx]

	// Good directions = unique set of TaiThan + HyThan directions
	huongTot := []string{taiThan}
	if hyThan != taiThan {
		huongTot = append(huongTot, hyThan)
	}

	// Bad direction(s) = HacThan + opposite of HacThan
	huongXau := []string{hacThan}

	return HuongXuatHanh{
		TaiThan:  taiThan,
		HyThan:   hyThan,
		HacThan:  hacThan,
		HuongTot: huongTot,
		HuongXau: huongXau,
	}
}

// ============================================
// Pha Trăng (Moon Phase)
// ============================================

// GetMoonPhase returns the moon phase based on the lunar day.
func GetMoonPhase(lunarDay int) MoonPhase {
	switch {
	case lunarDay == 1:
		return MoonPhase{"new_moon", "🌑", "Trăng non (Sóc)"}
	case lunarDay <= 3:
		return MoonPhase{"waxing_crescent", "🌒", "Trăng lưỡi liềm đầu tháng"}
	case lunarDay <= 7:
		return MoonPhase{"first_quarter", "🌓", "Trăng bán nguyệt đầu tháng"}
	case lunarDay <= 11:
		return MoonPhase{"waxing_gibbous", "🌔", "Trăng khuyết đầu tháng"}
	case lunarDay <= 16:
		return MoonPhase{"full_moon", "🌕", "Trăng tròn (Vọng)"}
	case lunarDay <= 19:
		return MoonPhase{"waning_gibbous", "🌖", "Trăng khuyết cuối tháng"}
	case lunarDay <= 23:
		return MoonPhase{"last_quarter", "🌗", "Trăng bán nguyệt cuối tháng"}
	default:
		return MoonPhase{"waning_crescent", "🌘", "Trăng lưỡi liềm cuối tháng"}
	}
}

// ============================================
// Việc Nên / Không Nên
// ============================================

var viecNenOptions = []string{
	"Xuất hành, đi xa",
	"Giao thương, ký kết",
	"Học tập, thi cử",
	"Cầu tài, cầu lộc",
	"Cưới hỏi, ăn hỏi",
	"Nhập trạch, dọn nhà",
	"Khai trương, mở hàng",
	"Động thổ, xây dựng",
	"Cầu an, giải hạn",
	"Thăm bệnh, chữa bệnh",
	"Trồng trọt, gieo hạt",
	"Sửa chữa nhà cửa",
}

var viecKhongOptions = []string{
	"Khai trương, khởi công",
	"Động thổ, xây dựng",
	"Nhập trạch, dọn nhà mới",
	"Cưới hỏi, ăn hỏi",
	"Xuất hành, đi xa",
	"Giao dịch, ký kết",
	"Kiện tụng, tranh chấp",
	"Mổ xẻ, phẫu thuật",
	"Mai táng, cải táng",
	"Đào giếng, đào ao",
}

// GetViecNenKhong returns lists of recommended and discouraged activities.
func GetViecNenKhong(trucIdx int, saoTotXau string) ([]string, []string) {
	truc := trucNgayList[trucIdx]
	var nen, khong []string

	switch truc.DanhGia {
	case "Tốt":
		// Good day: many activities allowed
		nen = viecNenOptions[:6]
		khong = viecKhongOptions[6:8]
	case "Bình thường":
		nen = viecNenOptions[:4]
		khong = viecKhongOptions[:3]
	case "Xấu":
		nen = viecNenOptions[8:10]
		khong = viecKhongOptions[:6]
	}

	// Adjust based on star
	if saoTotXau == "Xấu" && len(nen) > 2 {
		nen = nen[:len(nen)-1]
		khong = append(khong, "Khởi sự việc lớn")
	}

	return nen, khong
}

// ============================================
// Chỉ Số Ngày (Day Quality Score)
// ============================================

// CalculateDayScore computes an overall day quality score (0-100).
func CalculateDayScore(trucIdx int, saoTotXau string, isHoangDaoHour bool) int {
	score := 50

	// Trực ngày contribution
	truc := trucNgayList[trucIdx]
	switch truc.DanhGia {
	case "Tốt":
		score += 25
	case "Bình thường":
		score += 10
	case "Xấu":
		score -= 15
	}

	// Star contribution
	if saoTotXau == "Tốt" {
		score += 15
	} else {
		score -= 10
	}

	// Hoang Dao hour bonus
	if isHoangDaoHour {
		score += 8
	}

	// Clamp to 0-100
	if score > 100 {
		score = 100
	}
	if score < 0 {
		score = 0
	}

	return score
}

// ============================================
// Utility
// ============================================

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

// ============================================
// Comprehensive DayInfo
// ============================================

// GetDayInfo returns complete feng shui information for a specific date.
func GetDayInfo(dd, mm, yy, lunarDay, lunarMonth, lunarYear int) DayInfo {
	jd := jdFromDate(dd, mm, yy)
	dayCanIdx := (jd + 9) % 10
	dayChiIdx := (jd + 1) % 12

	truc := GetTrucNgay(lunarMonth, dayChiIdx)
	huong := GetHuongXuatHanh(dayCanIdx, dayChiIdx)
	sao := GetSaoChieuMenh(dd, mm, yy)
	moon := GetMoonPhase(lunarDay)

	// Get Trực index for score and activity calculation
	monthChiIdx := (lunarMonth + 1) % 12
	trucIdx := (dayChiIdx - monthChiIdx + 12) % 12

	nen, khong := GetViecNenKhong(trucIdx, sao.TotXau)
	score := CalculateDayScore(trucIdx, sao.TotXau, false)

	danhGia := "Bình thường"
	if score >= 85 {
		danhGia = "Rất tốt"
	} else if score >= 70 {
		danhGia = "Tốt"
	} else if score < 40 {
		danhGia = "Xấu"
	}

	return DayInfo{
		ChiSoNgay:     score,
		DanhGia:       danhGia,
		TrucNgay:      truc,
		HuongXuatHanh: huong,
		SaoChieu:      sao,
		MoonPhase:     moon,
		ViecNen:       nen,
		ViecKhong:     khong,
	}
}

// GetDayInfoFromTime is a convenience wrapper.
func GetDayInfoFromTime(t time.Time, lunarDay, lunarMonth, lunarYear int) DayInfo {
	return GetDayInfo(t.Day(), int(t.Month()), t.Year(), lunarDay, lunarMonth, lunarYear)
}

// unused but required by the package — suppress lint
var _ = math.Pi
