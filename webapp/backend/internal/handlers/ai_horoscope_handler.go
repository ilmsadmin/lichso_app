package handlers

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"strings"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ============================================
// AIHoroscopeHandler
// ============================================

// AIHoroscopeHandler handles AI horoscope reading endpoints
type AIHoroscopeHandler struct {
	service   *services.AIHoroscopeService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewAIHoroscopeHandler creates a new AIHoroscopeHandler
func NewAIHoroscopeHandler(svc *services.AIHoroscopeService, validator *validators.Validator, logger *zap.Logger) *AIHoroscopeHandler {
	return &AIHoroscopeHandler{service: svc, validator: validator, logger: logger}
}

// ReadHoroscope handles POST /api/v4/ai/horoscope/read
// Supports both non-streaming JSON and SSE streaming (when ?stream=true or req.Stream=true)
func (h *AIHoroscopeHandler) ReadHoroscope(c *fiber.Ctx) error {
	var req dto.HoroscopeAIRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	userID, ipAddress, role := extractUserContext(c)

	// SSE streaming
	if req.Stream || c.Query("stream") == "true" {
		c.Set("Content-Type", "text/event-stream")
		c.Set("Cache-Control", "no-cache")
		c.Set("Connection", "keep-alive")
		c.Set("X-Accel-Buffering", "no")

		c.Context().SetBodyStreamWriter(func(w *bufio.Writer) {
			onChunk := func(delta string) error {
				chunk, _ := json.Marshal(fiber.Map{"delta": delta})
				_, err := fmt.Fprintf(w, "data: %s\n\n", chunk)
				if err != nil {
					return err
				}
				return w.Flush()
			}

			// Use context.Background() instead of c.Context() because this callback
			// runs asynchronously after Fiber's handler has returned, making
			// c.Context() (fasthttp.RequestCtx) invalid as a Go context.
			ctx := context.Background()
			result, err := h.service.ReadHoroscopeStream(ctx, req, userID, ipAddress, role, onChunk)
			if err != nil {
				errChunk, _ := json.Marshal(fiber.Map{"error": err.Error()})
				fmt.Fprintf(w, "data: %s\n\n", errChunk)
				w.Flush()
				return
			}

			// Send final summary event
			done, _ := json.Marshal(fiber.Map{
				"done":            true,
				"session_id":      result.SessionID,
				"quota_remaining": result.QuotaRemaining,
				"tokens_used":     result.TokensUsed,
				"bat_tu":          result.BatTu,
				"ngu_hanh":        result.NguHanhBalance,
			})
			fmt.Fprintf(w, "data: %s\n\n", done)
			w.Flush()
		})
		return nil
	}

	// Non-streaming
	result, err := h.service.ReadHoroscope(c.Context(), req, userID, ipAddress, role)
	if err != nil {
		if strings.HasPrefix(err.Error(), "quota_exceeded") {
			return utils.ErrorResponse(c, fiber.StatusTooManyRequests, err.Error())
		}
		h.logger.Error("ReadHoroscope error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi tạo tử vi AI")
	}

	return utils.SuccessResponse(c, "Tử vi AI", result)
}

// GetHistory handles GET /api/v4/ai/horoscope/history
func (h *AIHoroscopeHandler) GetHistory(c *fiber.Ctx) error {
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}
	userUUID, err := uuid.Parse(userIDStr)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Thông tin người dùng không hợp lệ")
	}

	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 10)

	sessions, total, err := h.service.GetHistory(&userUUID, page, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy lịch sử")
	}

	return utils.SuccessResponse(c, "Lịch sử tử vi AI", fiber.Map{
		"data":  sessions,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

// GetQuota handles GET /api/v4/ai/horoscope/quota
func (h *AIHoroscopeHandler) GetQuota(c *fiber.Ctx) error {
	userID, ipAddress, role := extractUserContext(c)
	quota, err := h.service.GetQuota(userID, ipAddress, role)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi kiểm tra quota")
	}
	return utils.SuccessResponse(c, "Quota tử vi AI", quota)
}

// ============================================
// Shared helper
// ============================================

// extractUserContext parses user_id (uuid ptr), IP, and role from Fiber context
func extractUserContext(c *fiber.Ctx) (userID *uuid.UUID, ipAddress, role string) {
	ipAddress = c.IP()
	role = "guest"

	userIDStr, ok := c.Locals("user_id").(string)
	if ok && userIDStr != "" {
		if u, err := uuid.Parse(userIDStr); err == nil {
			userID = &u
		}
		roles, _ := c.Locals("user_roles").([]string)
		for _, r := range roles {
			if r == "premium" {
				role = "premium"
				break
			}
			if r == "user" || r == "free" {
				role = "free"
			}
		}
		if role == "guest" {
			role = "free"
		}
	}
	return
}
