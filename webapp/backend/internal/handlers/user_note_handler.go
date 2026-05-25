package handlers

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ============================================
// User Note Handler
// ============================================

// UserNoteHandler handles user note HTTP requests.
type UserNoteHandler struct {
	service   *services.UserNoteService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewUserNoteHandler creates a new UserNoteHandler.
func NewUserNoteHandler(service *services.UserNoteService, validator *validators.Validator, logger *zap.Logger) *UserNoteHandler {
	return &UserNoteHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/notes
func (h *UserNoteHandler) Create(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateUserNoteRequest
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

	return utils.CreatedResponse(c, "Đã tạo ghi chú", result)
}

// GetByID handles GET /api/notes/:id
func (h *UserNoteHandler) GetByID(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid note ID")
	}

	result, err := h.service.GetByID(id, userID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết ghi chú", result)
}

// GetByDate handles GET /api/notes/date/:date
func (h *UserNoteHandler) GetByDate(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	dateStr := c.Params("date")
	results, err := h.service.GetByDate(userID, dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Ghi chú theo ngày", results)
}

// GetByDateRange handles GET /api/notes/range?start_date=...&end_date=...
func (h *UserNoteHandler) GetByDateRange(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	startDate := c.Query("start_date")
	endDate := c.Query("end_date")

	if startDate == "" || endDate == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "start_date and end_date are required")
	}

	results, err := h.service.GetByDateRange(userID, startDate, endDate)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Ghi chú theo khoảng thời gian", results)
}

// List handles GET /api/notes
func (h *UserNoteHandler) List(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	results, total, err := h.service.List(userID, page, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách ghi chú", results, page, limit, total)
}

// Update handles PUT /api/notes/:id
func (h *UserNoteHandler) Update(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid note ID")
	}

	var req dto.UpdateUserNoteRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật ghi chú", result)
}

// Delete handles DELETE /api/notes/:id
func (h *UserNoteHandler) Delete(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid note ID")
	}

	if err := h.service.Delete(id, userID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa ghi chú", nil)
}

// ============================================
// User Countdown Handler
// ============================================

// UserCountdownHandler handles user countdown HTTP requests.
type UserCountdownHandler struct {
	service   *services.UserCountdownService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewUserCountdownHandler creates a new UserCountdownHandler.
func NewUserCountdownHandler(service *services.UserCountdownService, validator *validators.Validator, logger *zap.Logger) *UserCountdownHandler {
	return &UserCountdownHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/countdowns
func (h *UserCountdownHandler) Create(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateUserCountdownRequest
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

	return utils.CreatedResponse(c, "Đã tạo đếm ngược", result)
}

// GetByID handles GET /api/countdowns/:id
func (h *UserCountdownHandler) GetByID(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid countdown ID")
	}

	result, err := h.service.GetByID(id, userID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}

	return utils.SuccessResponse(c, "Chi tiết đếm ngược", result)
}

// GetActive handles GET /api/countdowns/active
func (h *UserCountdownHandler) GetActive(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	results, err := h.service.GetActive(userID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Đếm ngược đang hoạt động", results)
}

// GetUpcoming handles GET /api/countdowns/upcoming
func (h *UserCountdownHandler) GetUpcoming(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	days, _ := strconv.Atoi(c.Query("days", "30"))

	results, err := h.service.GetUpcoming(userID, days)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Đếm ngược sắp tới", results)
}

// List handles GET /api/countdowns
func (h *UserCountdownHandler) List(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	results, total, err := h.service.List(userID, page, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách đếm ngược", results, page, limit, total)
}

// Update handles PUT /api/countdowns/:id
func (h *UserCountdownHandler) Update(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid countdown ID")
	}

	var req dto.UpdateUserCountdownRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(id, userID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật đếm ngược", result)
}

// Delete handles DELETE /api/countdowns/:id
func (h *UserCountdownHandler) Delete(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid countdown ID")
	}

	if err := h.service.Delete(id, userID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa đếm ngược", nil)
}
