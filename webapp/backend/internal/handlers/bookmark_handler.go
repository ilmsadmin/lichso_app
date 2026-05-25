package handlers

import (
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// BookmarkHandler handles bookmark-related HTTP requests.
type BookmarkHandler struct {
	service   *services.BookmarkService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewBookmarkHandler creates a new BookmarkHandler.
func NewBookmarkHandler(service *services.BookmarkService, validator *validators.Validator, logger *zap.Logger) *BookmarkHandler {
	return &BookmarkHandler{service: service, validator: validator, logger: logger}
}

// Create handles POST /api/bookmarks
func (h *BookmarkHandler) Create(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	var req dto.CreateBookmarkRequest
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

	return utils.SuccessResponse(c, "Đã lưu bookmark", result)
}

// GetAll handles GET /api/bookmarks
func (h *BookmarkHandler) GetAll(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	result, err := h.service.GetByUser(userID)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Danh sách bookmark", result)
}

// GetByMonth handles GET /api/bookmarks/month/:year/:month
func (h *BookmarkHandler) GetByMonth(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	year, err := c.ParamsInt("year")
	if err != nil || year < 1900 || year > 2100 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Năm không hợp lệ")
	}
	month, err := c.ParamsInt("month")
	if err != nil || month < 1 || month > 12 {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Tháng không hợp lệ")
	}

	result, err := h.service.GetByMonth(userID, year, month)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.SuccessResponse(c, "Bookmark tháng", result)
}

// GetByDate handles GET /api/bookmarks/date/:date
func (h *BookmarkHandler) GetByDate(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	dateStr := c.Params("date")
	result, err := h.service.GetByDate(userID, dateStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Bookmark ngày", result)
}

// Update handles PUT /api/bookmarks/:id
func (h *BookmarkHandler) Update(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	bookmarkID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid bookmark ID")
	}

	var req dto.UpdateBookmarkRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}

	result, err := h.service.Update(userID, bookmarkID, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã cập nhật bookmark", result)
}

// Delete handles DELETE /api/bookmarks/:id
func (h *BookmarkHandler) Delete(c *fiber.Ctx) error {
	userID, err := getUserID(c)
	if err != nil {
		return utils.UnauthorizedResponse(c, "Invalid user")
	}

	bookmarkID, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid bookmark ID")
	}

	if err := h.service.Delete(userID, bookmarkID); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.SuccessResponse(c, "Đã xóa bookmark", nil)
}

// getUserID extracts the user ID from the request context.
func getUserID(c *fiber.Ctx) (uuid.UUID, error) {
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return uuid.Nil, fiber.ErrUnauthorized
	}
	return uuid.Parse(userIDStr)
}
