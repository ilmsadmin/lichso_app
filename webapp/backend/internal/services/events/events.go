// Package events provides Vietnamese holidays and events data.
package events

// ============================================
// Types
// ============================================

// Event represents a holiday or special event.
type Event struct {
	Name     string `json:"name"`     // Event name in Vietnamese
	Type     string `json:"type"`     // "national", "traditional", "international", "memorial"
	IsOff    bool   `json:"is_off"`   // Whether it's a day off
	IsLunar  bool   `json:"is_lunar"` // Whether date follows lunar calendar
	Day      int    `json:"day"`      // Day of the event
	Month    int    `json:"month"`    // Month of the event
	Emoji    string `json:"emoji"`    // Representative emoji
	Desc     string `json:"desc"`     // Brief description
	Category string `json:"category"` // "tet", "quoc-le", "truyen-thong", "quoc-te", "gio"
}

// ============================================
// Solar (Dương lịch) Holidays & Events
// ============================================

var solarEvents = []Event{
	// === Quốc lễ / Ngày nghỉ chính thức ===
	{Name: "Tết Dương lịch", Type: "national", IsOff: true, IsLunar: false, Day: 1, Month: 1, Emoji: "🎆", Desc: "Ngày đầu năm mới Dương lịch", Category: "quoc-le"},
	{Name: "Ngày Giải phóng miền Nam", Type: "national", IsOff: true, IsLunar: false, Day: 30, Month: 4, Emoji: "🇻🇳", Desc: "Thống nhất đất nước (30/4/1975)", Category: "quoc-le"},
	{Name: "Ngày Quốc tế Lao động", Type: "national", IsOff: true, IsLunar: false, Day: 1, Month: 5, Emoji: "⚒️", Desc: "Ngày nghỉ lễ Quốc tế Lao động", Category: "quoc-le"},
	{Name: "Quốc Khánh", Type: "national", IsOff: true, IsLunar: false, Day: 2, Month: 9, Emoji: "🇻🇳", Desc: "Ngày Quốc Khánh nước CHXHCN Việt Nam (2/9/1945)", Category: "quoc-le"},

	// === Ngày truyền thống & kỷ niệm ===
	{Name: "Ngày thành lập Đảng", Type: "memorial", IsOff: false, IsLunar: false, Day: 3, Month: 2, Emoji: "⭐", Desc: "Thành lập Đảng Cộng sản Việt Nam (3/2/1930)", Category: "truyen-thong"},
	{Name: "Ngày Thầy thuốc Việt Nam", Type: "traditional", IsOff: false, IsLunar: false, Day: 27, Month: 2, Emoji: "🩺", Desc: "Tri ân ngành y tế Việt Nam", Category: "truyen-thong"},
	{Name: "Ngày Quốc tế Phụ nữ", Type: "international", IsOff: false, IsLunar: false, Day: 8, Month: 3, Emoji: "🌷", Desc: "Ngày Quốc tế Phụ nữ 8/3", Category: "quoc-te"},
	{Name: "Ngày thành lập Đoàn TNCS", Type: "memorial", IsOff: false, IsLunar: false, Day: 26, Month: 3, Emoji: "🌟", Desc: "Thành lập Đoàn Thanh niên Cộng sản Hồ Chí Minh", Category: "truyen-thong"},
	{Name: "Ngày Sách Việt Nam", Type: "traditional", IsOff: false, IsLunar: false, Day: 21, Month: 4, Emoji: "📖", Desc: "Ngày Sách và Văn hóa đọc Việt Nam", Category: "truyen-thong"},
	{Name: "Chiến thắng Điện Biên Phủ", Type: "memorial", IsOff: false, IsLunar: false, Day: 7, Month: 5, Emoji: "🏆", Desc: "Chiến thắng Điện Biên Phủ (7/5/1954)", Category: "truyen-thong"},
	{Name: "Ngày sinh Chủ tịch Hồ Chí Minh", Type: "memorial", IsOff: false, IsLunar: false, Day: 19, Month: 5, Emoji: "🌻", Desc: "Sinh nhật Bác Hồ (19/5/1890)", Category: "truyen-thong"},
	{Name: "Quốc tế Thiếu nhi", Type: "international", IsOff: false, IsLunar: false, Day: 1, Month: 6, Emoji: "🧒", Desc: "Ngày Quốc tế Thiếu nhi 1/6", Category: "quoc-te"},
	{Name: "Ngày Gia đình Việt Nam", Type: "traditional", IsOff: false, IsLunar: false, Day: 28, Month: 6, Emoji: "👨‍👩‍👧‍👦", Desc: "Ngày Gia đình Việt Nam", Category: "truyen-thong"},
	{Name: "Ngày Thương binh Liệt sĩ", Type: "memorial", IsOff: false, IsLunar: false, Day: 27, Month: 7, Emoji: "🕊️", Desc: "Ngày Thương binh Liệt sĩ (27/7/1947)", Category: "truyen-thong"},
	{Name: "Ngày Cách mạng tháng Tám", Type: "memorial", IsOff: false, IsLunar: false, Day: 19, Month: 8, Emoji: "✊", Desc: "Cách mạng tháng Tám thành công (19/8/1945)", Category: "truyen-thong"},
	{Name: "Ngày Quốc Khánh (nghỉ bù)", Type: "national", IsOff: true, IsLunar: false, Day: 1, Month: 9, Emoji: "🇻🇳", Desc: "Ngày nghỉ bù liền kề Quốc Khánh", Category: "quoc-le"},
	{Name: "Ngày Phụ nữ Việt Nam", Type: "traditional", IsOff: false, IsLunar: false, Day: 20, Month: 10, Emoji: "💐", Desc: "Ngày Phụ nữ Việt Nam 20/10", Category: "truyen-thong"},
	{Name: "Ngày Nhà giáo Việt Nam", Type: "traditional", IsOff: false, IsLunar: false, Day: 20, Month: 11, Emoji: "📚", Desc: "Ngày Nhà giáo Việt Nam 20/11", Category: "truyen-thong"},
	{Name: "Ngày thành lập QĐND Việt Nam", Type: "memorial", IsOff: false, IsLunar: false, Day: 22, Month: 12, Emoji: "🎖️", Desc: "Thành lập Quân đội Nhân dân Việt Nam (22/12/1944)", Category: "truyen-thong"},
	{Name: "Giáng Sinh", Type: "international", IsOff: false, IsLunar: false, Day: 25, Month: 12, Emoji: "🎄", Desc: "Lễ Giáng Sinh — Noel", Category: "quoc-te"},
	{Name: "Valentine", Type: "international", IsOff: false, IsLunar: false, Day: 14, Month: 2, Emoji: "💝", Desc: "Ngày lễ Tình nhân", Category: "quoc-te"},
	{Name: "Halloween", Type: "international", IsOff: false, IsLunar: false, Day: 31, Month: 10, Emoji: "🎃", Desc: "Lễ hội Halloween", Category: "quoc-te"},
}

// ============================================
// Lunar (Âm lịch) Holidays & Events
// ============================================

var lunarEvents = []Event{
	// === Tết Nguyên Đán ===
	{Name: "Tất Niên (29 Tết)", Type: "traditional", IsOff: true, IsLunar: true, Day: 29, Month: 12, Emoji: "🧧", Desc: "Ngày cuối cùng năm cũ, cúng Tất niên", Category: "tet"},
	{Name: "Giao Thừa (30 Tết)", Type: "national", IsOff: true, IsLunar: true, Day: 30, Month: 12, Emoji: "🎆", Desc: "Đêm Giao thừa, đón Tết Nguyên Đán", Category: "tet"},
	{Name: "Tết Nguyên Đán — Mồng 1", Type: "national", IsOff: true, IsLunar: true, Day: 1, Month: 1, Emoji: "🧧", Desc: "Mồng 1 Tết — Ngày đầu tiên năm mới Âm lịch", Category: "tet"},
	{Name: "Tết Nguyên Đán — Mồng 2", Type: "national", IsOff: true, IsLunar: true, Day: 2, Month: 1, Emoji: "🧧", Desc: "Mồng 2 Tết — Cúng gia tiên", Category: "tet"},
	{Name: "Tết Nguyên Đán — Mồng 3", Type: "national", IsOff: true, IsLunar: true, Day: 3, Month: 1, Emoji: "🧧", Desc: "Mồng 3 Tết — Hóa vàng", Category: "tet"},
	{Name: "Tết Nguyên Đán — Mồng 4", Type: "national", IsOff: true, IsLunar: true, Day: 4, Month: 1, Emoji: "🧧", Desc: "Mồng 4 Tết — Khai hạ", Category: "tet"},
	{Name: "Tết Nguyên Đán — Mồng 5", Type: "national", IsOff: true, IsLunar: true, Day: 5, Month: 1, Emoji: "🧧", Desc: "Mồng 5 Tết — Ngày cuối kỳ nghỉ Tết", Category: "tet"},

	// === Ngày lễ Âm lịch khác ===
	{Name: "Tết Nguyên Tiêu (Rằm tháng Giêng)", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 1, Emoji: "🏮", Desc: "Rằm tháng Giêng — Lễ Thượng Nguyên", Category: "truyen-thong"},
	{Name: "Tết Hàn Thực", Type: "traditional", IsOff: false, IsLunar: true, Day: 3, Month: 3, Emoji: "🍡", Desc: "Mồng 3 tháng 3 — Tết Hàn Thực, làm bánh trôi nước", Category: "truyen-thong"},
	{Name: "Giỗ Tổ Hùng Vương", Type: "national", IsOff: true, IsLunar: true, Day: 10, Month: 3, Emoji: "🏛️", Desc: "Mồng 10 tháng 3 — Giỗ Tổ Hùng Vương", Category: "quoc-le"},
	{Name: "Lễ Phật Đản", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 4, Emoji: "🪷", Desc: "Rằm tháng 4 — Lễ Phật Đản sanh", Category: "truyen-thong"},
	{Name: "Tết Đoan Ngọ", Type: "traditional", IsOff: false, IsLunar: true, Day: 5, Month: 5, Emoji: "🍊", Desc: "Mồng 5 tháng 5 — Tết Đoan Ngọ, diệt sâu bọ", Category: "truyen-thong"},
	{Name: "Lễ Vu Lan", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 7, Emoji: "🌸", Desc: "Rằm tháng 7 — Lễ Vu Lan báo hiếu, Xá tội vong nhân", Category: "truyen-thong"},
	{Name: "Tết Trung Thu", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 8, Emoji: "🥮", Desc: "Rằm tháng 8 — Tết Trung Thu, Tết Thiếu nhi", Category: "truyen-thong"},
	{Name: "Tết Trùng Cửu", Type: "traditional", IsOff: false, IsLunar: true, Day: 9, Month: 9, Emoji: "🍊", Desc: "Mồng 9 tháng 9 — Tết Trùng Cửu, thờ cúng ông bà", Category: "truyen-thong"},
	{Name: "Tết Hạ Nguyên", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 10, Emoji: "🪔", Desc: "Rằm tháng 10 — Tết Hạ Nguyên, cúng cô hồn", Category: "truyen-thong"},
	{Name: "Ông Công Ông Táo", Type: "traditional", IsOff: false, IsLunar: true, Day: 23, Month: 12, Emoji: "🐟", Desc: "23 tháng Chạp — Tiễn Ông Công Ông Táo về trời", Category: "truyen-thong"},

	// === Ngày Rằm và Mùng 1 (hàng tháng) ===
	{Name: "Ngày Rằm", Type: "traditional", IsOff: false, IsLunar: true, Day: 15, Month: 0, Emoji: "🌕", Desc: "Ngày Rằm — cúng lễ, ăn chay", Category: "truyen-thong"},
	{Name: "Ngày Mùng 1", Type: "traditional", IsOff: false, IsLunar: true, Day: 1, Month: 0, Emoji: "🌑", Desc: "Ngày Mùng 1 — cúng lễ đầu tháng", Category: "truyen-thong"},
}

// ============================================
// Public Functions
// ============================================

// GetSolarEvents returns all solar calendar events for a specific day/month.
func GetSolarEvents(day, month int) []Event {
	var result []Event
	for _, e := range solarEvents {
		if e.Day == day && e.Month == month {
			result = append(result, e)
		}
	}
	return result
}

// GetLunarEvents returns all lunar calendar events for a specific lunar day/month.
// Month=0 events (like Rằm, Mùng 1) match every month.
func GetLunarEvents(lunarDay, lunarMonth int) []Event {
	var result []Event
	for _, e := range lunarEvents {
		if e.Day == lunarDay && (e.Month == lunarMonth || e.Month == 0) {
			result = append(result, e)
		}
	}
	return result
}

// GetAllEvents returns both solar and lunar events for a given date.
func GetAllEvents(solarDay, solarMonth, lunarDay, lunarMonth int) []Event {
	var result []Event
	result = append(result, GetSolarEvents(solarDay, solarMonth)...)
	result = append(result, GetLunarEvents(lunarDay, lunarMonth)...)
	return result
}

// GetMonthSolarEvents returns all solar events in a given month.
func GetMonthSolarEvents(month int) []Event {
	var result []Event
	for _, e := range solarEvents {
		if e.Month == month {
			result = append(result, e)
		}
	}
	return result
}

// GetMonthLunarEvents returns all lunar events in a given lunar month.
// Includes Month=0 events (recurring Rằm/Mùng 1).
func GetMonthLunarEvents(lunarMonth int) []Event {
	var result []Event
	for _, e := range lunarEvents {
		if e.Month == lunarMonth || e.Month == 0 {
			result = append(result, e)
		}
	}
	return result
}

// IsHoliday checks if a solar date is a public holiday.
func IsHoliday(solarDay, solarMonth int) bool {
	for _, e := range solarEvents {
		if e.Day == solarDay && e.Month == solarMonth && e.IsOff {
			return true
		}
	}
	return false
}

// IsLunarHoliday checks if a lunar date is a public holiday.
func IsLunarHoliday(lunarDay, lunarMonth int) bool {
	for _, e := range lunarEvents {
		if e.Day == lunarDay && e.Month == lunarMonth && e.IsOff {
			return true
		}
	}
	return false
}
