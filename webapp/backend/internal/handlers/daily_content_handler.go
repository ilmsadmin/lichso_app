package handlers

import (
	"strconv"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// DailyContentHandler handles daily content schedule HTTP requests.
type DailyContentHandler struct {
	service   *services.DailyContentService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewDailyContentHandler creates a new DailyContentHandler.
func NewDailyContentHandler(service *services.DailyContentService, validator *validators.Validator, logger *zap.Logger) *DailyContentHandler {
	return &DailyContentHandler{service: service, validator: validator, logger: logger}
}

// ============================================
// Public Routes
// ============================================

// GetContentForDate handles GET /api/day-content/:date (public)
func (h *DailyContentHandler) GetContentForDate(c *fiber.Ctx) error {
	dateStr := c.Params("date")
	if dateStr == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Thiếu tham số ngày (format: YYYY-MM-DD)")
	}

	date, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Định dạng ngày không hợp lệ (format: YYYY-MM-DD)")
	}

	result, err := h.service.GetContentForDate(date)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Nội dung ngày "+dateStr, result)
}

// GetContentToday handles GET /api/day-content/today (public)
func (h *DailyContentHandler) GetContentToday(c *fiber.Ctx) error {
	loc, _ := time.LoadLocation("Asia/Ho_Chi_Minh")
	now := time.Now().In(loc)

	result, err := h.service.GetContentForDate(now)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Nội dung hôm nay", result)
}

// GetMonthContentSummary handles GET /api/day-content/month/:year/:month (public)
func (h *DailyContentHandler) GetMonthContentSummary(c *fiber.Ctx) error {
	year, err := strconv.Atoi(c.Params("year"))
	if err != nil || year < 1900 || year > 2200 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ (1900-2200)")
	}

	month, err := strconv.Atoi(c.Params("month"))
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ (1-12)")
	}

	result, err := h.service.GetMonthContentSummary(year, month)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Tổng hợp nội dung tháng", result)
}

// ============================================
// Admin Routes
// ============================================

// Create handles POST /api/admin/daily-content
func (h *DailyContentHandler) Create(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateDailyContentRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Create(userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã tạo lịch nội dung", result)
}

// GetByID handles GET /api/admin/daily-content/:id
func (h *DailyContentHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid schedule ID")
	}

	result, err := h.service.GetByID(id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết lịch nội dung", result)
}

// List handles GET /api/admin/daily-content
func (h *DailyContentHandler) List(c *fiber.Ctx) error {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))
	contentType := c.Query("content_type", "")

	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	var results []dto.DailyContentResponse
	var total int64
	var err error

	if contentType != "" {
		results, total, err = h.service.ListByType(contentType, page, limit)
	} else {
		results, total, err = h.service.List(page, limit)
	}

	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách lịch nội dung", results, page, limit, total)
}

// Update handles PUT /api/admin/daily-content/:id
func (h *DailyContentHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid schedule ID")
	}

	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.UpdateDailyContentRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật lịch nội dung", result)
}

// Delete handles DELETE /api/admin/daily-content/:id
func (h *DailyContentHandler) Delete(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid schedule ID")
	}

	if err := h.service.Delete(id); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa lịch nội dung", nil)
}

// GetStats handles GET /api/admin/daily-content/stats
func (h *DailyContentHandler) GetStats(c *fiber.Ctx) error {
	year, _ := strconv.Atoi(c.Query("year", strconv.Itoa(time.Now().Year())))
	if year < 1900 || year > 2200 {
		year = time.Now().Year()
	}

	result, err := h.service.GetStats(year)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Thống kê nội dung ngày", result)
}

// AutoFill handles POST /api/admin/daily-content/auto-fill
func (h *DailyContentHandler) AutoFill(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.AutoFillRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.AutoFill(userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã tự động điền nội dung", result)
}
