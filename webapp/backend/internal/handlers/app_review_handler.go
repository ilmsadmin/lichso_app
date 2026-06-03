package handlers

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
)

// AppReviewHandler serves mobile review submission and admin management endpoints.
type AppReviewHandler struct {
	service   *services.AppReviewService
	validator *validators.Validator
}

func NewAppReviewHandler(service *services.AppReviewService, validator *validators.Validator) *AppReviewHandler {
	return &AppReviewHandler{service: service, validator: validator}
}

// Submit handles POST /api/app-reviews.
func (h *AppReviewHandler) Submit(c *fiber.Ctx) error {
	var req dto.SubmitAppReviewRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	platform := clientPlatform(c)
	if platform == "" {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Không xác định được nền tảng thiết bị")
	}

	var userID *uuid.UUID
	if userIDStr, ok := c.Locals("user_id").(string); ok && userIDStr != "" {
		if parsed, err := uuid.Parse(userIDStr); err == nil {
			userID = &parsed
		}
	}

	result, err := h.service.Submit(
		c.UserContext(),
		&req,
		userID,
		platform,
		c.Get("X-App-Version"),
		c.Get("X-Device-ID"),
		c.Get("X-Device-Name"),
		c.Get("X-OS-Version"),
	)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}

	return utils.CreatedResponse(c, "Đã ghi nhận đánh giá ứng dụng", result)
}

// List handles GET /api/admin/app-reviews.
func (h *AppReviewHandler) List(c *fiber.Ctx) error {
	pagination := utils.ParsePagination(c)
	stars, _ := strconv.Atoi(c.Query("stars", "0"))

	result, total, err := h.service.List(c.UserContext(), dto.AppReviewListParams{
		Page:      pagination.Page,
		Limit:     pagination.Limit,
		Search:    c.Query("search"),
		Status:    c.Query("status"),
		Platform:  c.Query("platform"),
		Stars:     stars,
		SortBy:    c.Query("sort_by", "created_at"),
		SortOrder: c.Query("sort_order", "desc"),
	})
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, err.Error())
	}

	return utils.PaginatedResponse(c, "Danh sách đánh giá ứng dụng", result, pagination.Page, pagination.Limit, total)
}

// GetByID handles GET /api/admin/app-reviews/:id.
func (h *AppReviewHandler) GetByID(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid review ID")
	}

	result, err := h.service.GetByID(c.UserContext(), id)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusNotFound, err.Error())
	}
	return utils.SuccessResponse(c, "Chi tiết đánh giá ứng dụng", result)
}

// Update handles PATCH /api/admin/app-reviews/:id.
func (h *AppReviewHandler) Update(c *fiber.Ctx) error {
	id, err := uuid.Parse(c.Params("id"))
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid review ID")
	}

	var req dto.UpdateAppReviewRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Invalid request body")
	}
	if errors := h.validator.ValidateStruct(&req); errors != nil {
		return utils.ValidationErrorResponse(c, errors)
	}

	result, err := h.service.Update(c.UserContext(), id, &req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, err.Error())
	}
	return utils.SuccessResponse(c, "Đã cập nhật đánh giá ứng dụng", result)
}
