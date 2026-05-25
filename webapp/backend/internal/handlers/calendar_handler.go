package handlers

import (
	"strconv"
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/zplus/lichso/internal/services/calendar"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// CalendarHandler handles calendar-related HTTP requests.
type CalendarHandler struct {
	calService *calendar.Service
	logger     *zap.Logger
}

// NewCalendarHandler creates a new CalendarHandler.
func NewCalendarHandler(calService *calendar.Service, logger *zap.Logger) *CalendarHandler {
	return &CalendarHandler{
		calService: calService,
		logger:     logger,
	}
}

// GetToday handles GET /api/calendar/today
func (h *CalendarHandler) GetToday(c *fiber.Ctx) error {
	data := h.calService.GetToday()
	return utils.SuccessResponse(c, "Thông tin ngày hôm nay", data)
}

// GetDate handles GET /api/calendar/date/:date
// Date format: YYYY-MM-DD
func (h *CalendarHandler) GetDate(c *fiber.Ctx) error {
	dateStr := c.Params("date")
	if dateStr == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Thiếu tham số ngày (format: YYYY-MM-DD)")
	}

	t, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Định dạng ngày không hợp lệ (format: YYYY-MM-DD)")
	}

	if t.Year() < 1900 || t.Year() > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm phải trong khoảng 1900-2100")
	}

	// Get optional hour parameter
	hour := time.Now().Hour()
	if h := c.Query("hour"); h != "" {
		if parsed, err := strconv.Atoi(h); err == nil && parsed >= 0 && parsed <= 23 {
			hour = parsed
		}
	}

	data := h.calService.GetDate(t.Day(), int(t.Month()), t.Year(), hour)
	return utils.SuccessResponse(c, "Thông tin ngày "+dateStr, data)
}

// GetMonth handles GET /api/calendar/month/:year/:month
func (h *CalendarHandler) GetMonth(c *fiber.Ctx) error {
	yearStr := c.Params("year")
	monthStr := c.Params("month")

	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ (1900-2100)")
	}

	month, err := strconv.Atoi(monthStr)
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ (1-12)")
	}

	data := h.calService.GetMonth(year, month)
	return utils.SuccessResponse(c, "Lịch tháng "+monthStr+"/"+yearStr, data)
}

// Convert handles GET /api/calendar/convert
// Query params: day, month, year, to_lunar (bool), leap_month (bool)
func (h *CalendarHandler) Convert(c *fiber.Ctx) error {
	dayStr := c.Query("day")
	monthStr := c.Query("month")
	yearStr := c.Query("year")
	toLunar := strings.ToLower(c.Query("to_lunar", "true")) == "true"
	leapMonth := strings.ToLower(c.Query("leap_month", "false")) == "true"

	day, err := strconv.Atoi(dayStr)
	if err != nil || day < 1 || day > 31 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Ngày không hợp lệ")
	}

	month, err := strconv.Atoi(monthStr)
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}

	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ (1900-2100)")
	}

	var data calendar.ConvertResult
	if toLunar {
		data = h.calService.ConvertSolarToLunar(day, month, year)
	} else {
		data = h.calService.ConvertLunarToSolar(day, month, year, leapMonth)
	}

	return utils.SuccessResponse(c, "Kết quả chuyển đổi", data)
}

// GetGoodDays handles GET /api/calendar/good-days
// Query params: year, month
func (h *CalendarHandler) GetGoodDays(c *fiber.Ctx) error {
	loc, _ := time.LoadLocation("Asia/Ho_Chi_Minh")
	now := time.Now().In(loc)

	yearStr := c.Query("year", strconv.Itoa(now.Year()))
	monthStr := c.Query("month", strconv.Itoa(int(now.Month())))

	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ")
	}

	month, err := strconv.Atoi(monthStr)
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}

	data := h.calService.GetGoodDays(year, month)
	return utils.SuccessResponse(c, "Ngày tốt tháng "+monthStr+"/"+yearStr, data)
}

// GetSolarTerms handles GET /api/calendar/solar-terms/:year
func (h *CalendarHandler) GetSolarTerms(c *fiber.Ctx) error {
	yearStr := c.Params("year")
	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ (1900-2100)")
	}

	data := h.calService.GetSolarTerms(year)
	return utils.SuccessResponse(c, "24 tiết khí năm "+yearStr, data)
}

// GetFengshuiDirection handles GET /api/fengshui/direction/:date
func (h *CalendarHandler) GetFengshuiDirection(c *fiber.Ctx) error {
	dateStr := c.Params("date")
	t, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Định dạng ngày không hợp lệ (YYYY-MM-DD)")
	}

	data := h.calService.GetDate(t.Day(), int(t.Month()), t.Year(), 12)
	result := fiber.Map{
		"date":            dateStr,
		"huong_xuat_hanh": data.PhongThuy.HuongXuatHanh,
	}
	return utils.SuccessResponse(c, "Hướng xuất hành ngày "+dateStr, result)
}

// GetFengshuiHours handles GET /api/fengshui/hours/:date
func (h *CalendarHandler) GetFengshuiHours(c *fiber.Ctx) error {
	dateStr := c.Params("date")
	t, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Định dạng ngày không hợp lệ (YYYY-MM-DD)")
	}

	data := h.calService.GetDate(t.Day(), int(t.Month()), t.Year(), 12)
	result := fiber.Map{
		"date":          dateStr,
		"gio_hoang_dao": data.GioHoangDao,
	}
	return utils.SuccessResponse(c, "Giờ hoàng đạo ngày "+dateStr, result)
}

// GetFengshuiActivities handles GET /api/fengshui/activities/:date
func (h *CalendarHandler) GetFengshuiActivities(c *fiber.Ctx) error {
	dateStr := c.Params("date")
	t, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Định dạng ngày không hợp lệ (YYYY-MM-DD)")
	}

	data := h.calService.GetDate(t.Day(), int(t.Month()), t.Year(), 12)
	result := fiber.Map{
		"date":       dateStr,
		"viec_nen":   data.PhongThuy.ViecNen,
		"viec_khong": data.PhongThuy.ViecKhong,
		"truc_ngay":  data.PhongThuy.TrucNgay,
		"chi_so":     data.PhongThuy.ChiSoNgay,
	}
	return utils.SuccessResponse(c, "Việc nên/không nên ngày "+dateStr, result)
}
