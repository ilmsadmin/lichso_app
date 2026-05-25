package utils

import (
	"math"

	"github.com/gofiber/fiber/v2"
)

// APIResponse represents the standard API response structure
type APIResponse struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   interface{} `json:"error,omitempty"`
	Meta    *Meta       `json:"meta,omitempty"`
}

// Meta represents pagination metadata
type Meta struct {
	Page       int   `json:"page"`
	Limit      int   `json:"limit"`
	Total      int64 `json:"total"`
	TotalPages int   `json:"total_pages"`
}

// ErrorDetail represents detailed error information
type ErrorDetail struct {
	Code   int               `json:"code"`
	Detail string            `json:"detail,omitempty"`
	Fields map[string]string `json:"fields,omitempty"`
}

// SuccessResponse sends a successful JSON response
func SuccessResponse(c *fiber.Ctx, message string, data interface{}) error {
	return c.Status(fiber.StatusOK).JSON(APIResponse{
		Success: true,
		Message: message,
		Data:    data,
	})
}

// CreatedResponse sends a 201 created JSON response
func CreatedResponse(c *fiber.Ctx, message string, data interface{}) error {
	return c.Status(fiber.StatusCreated).JSON(APIResponse{
		Success: true,
		Message: message,
		Data:    data,
	})
}

// NoContentResponse sends a 204 no content response
func NoContentResponse(c *fiber.Ctx) error {
	return c.SendStatus(fiber.StatusNoContent)
}

// PaginatedResponse sends a paginated JSON response
func PaginatedResponse(c *fiber.Ctx, message string, data interface{}, page, limit int, total int64) error {
	totalPages := int(math.Ceil(float64(total) / float64(limit)))
	return c.Status(fiber.StatusOK).JSON(APIResponse{
		Success: true,
		Message: message,
		Data:    data,
		Meta: &Meta{
			Page:       page,
			Limit:      limit,
			Total:      total,
			TotalPages: totalPages,
		},
	})
}

// ErrorResponse sends an error JSON response
func ErrorResponse(c *fiber.Ctx, statusCode int, message string) error {
	return c.Status(statusCode).JSON(APIResponse{
		Success: false,
		Message: message,
		Error: ErrorDetail{
			Code:   statusCode,
			Detail: message,
		},
	})
}

// ErrorResponseWithDetail sends an error JSON response with additional detail
func ErrorResponseWithDetail(c *fiber.Ctx, statusCode int, message, detail string) error {
	return c.Status(statusCode).JSON(APIResponse{
		Success: false,
		Message: message,
		Error: ErrorDetail{
			Code:   statusCode,
			Detail: detail,
		},
	})
}

// ValidationErrorResponse sends a validation error response with field-level errors
func ValidationErrorResponse(c *fiber.Ctx, fields map[string]string) error {
	return c.Status(fiber.StatusUnprocessableEntity).JSON(APIResponse{
		Success: false,
		Message: "Validation failed",
		Error: ErrorDetail{
			Code:   fiber.StatusUnprocessableEntity,
			Detail: "One or more fields failed validation",
			Fields: fields,
		},
	})
}

// InternalErrorResponse sends a 500 internal server error response
func InternalErrorResponse(c *fiber.Ctx) error {
	return ErrorResponse(c, fiber.StatusInternalServerError, "Internal server error")
}

// UnauthorizedResponse sends a 401 unauthorized response
func UnauthorizedResponse(c *fiber.Ctx, message string) error {
	if message == "" {
		message = "Unauthorized"
	}
	return ErrorResponse(c, fiber.StatusUnauthorized, message)
}

// ForbiddenResponse sends a 403 forbidden response
func ForbiddenResponse(c *fiber.Ctx, message string) error {
	if message == "" {
		message = "You don't have permission to access this resource"
	}
	return ErrorResponse(c, fiber.StatusForbidden, message)
}

// NotFoundResponse sends a 404 not found response
func NotFoundResponse(c *fiber.Ctx, message string) error {
	if message == "" {
		message = "Resource not found"
	}
	return ErrorResponse(c, fiber.StatusNotFound, message)
}

// ConflictResponse sends a 409 conflict response
func ConflictResponse(c *fiber.Ctx, message string) error {
	if message == "" {
		message = "Resource already exists"
	}
	return ErrorResponse(c, fiber.StatusConflict, message)
}

// TooManyRequestsResponse sends a 429 too many requests response
func TooManyRequestsResponse(c *fiber.Ctx) error {
	return ErrorResponse(c, fiber.StatusTooManyRequests, "Too many requests, please try again later")
}
