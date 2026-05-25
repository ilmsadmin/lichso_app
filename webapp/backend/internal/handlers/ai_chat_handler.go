package handlers

import (
	"bufio"
	"encoding/json"
	"fmt"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/services"
	"github.com/zplus/lichso/internal/utils"
	"github.com/zplus/lichso/internal/validators"
	"go.uber.org/zap"
)

// ============================================
// AIChatHandler
// ============================================

// AIChatHandler handles AI chat session endpoints
type AIChatHandler struct {
	service   *services.AIChatService
	validator *validators.Validator
	logger    *zap.Logger
}

// NewAIChatHandler creates a new AIChatHandler
func NewAIChatHandler(svc *services.AIChatService, validator *validators.Validator, logger *zap.Logger) *AIChatHandler {
	return &AIChatHandler{service: svc, validator: validator, logger: logger}
}

// requireUserID extracts user UUID from auth locals
func (h *AIChatHandler) requireUserID(c *fiber.Ctx) (uuid.UUID, error) {
	userIDStr, ok := c.Locals("user_id").(string)
	if !ok || userIDStr == "" {
		return uuid.Nil, fmt.Errorf("unauthorized")
	}
	u, err := uuid.Parse(userIDStr)
	if err != nil {
		return uuid.Nil, fmt.Errorf("invalid user id")
	}
	return u, nil
}

// CreateSession handles POST /api/v4/ai/chat/sessions
func (h *AIChatHandler) CreateSession(c *fiber.Ctx) error {
	userID, err := h.requireUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}

	var req dto.AIChatCreateRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}

	session, err := h.service.CreateSession(userID, req)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi tạo phiên chat")
	}
	return utils.CreatedResponse(c, "Tạo phiên chat thành công", session)
}

// ListSessions handles GET /api/v4/ai/chat/sessions
func (h *AIChatHandler) ListSessions(c *fiber.Ctx) error {
	userID, err := h.requireUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}

	page := c.QueryInt("page", 1)
	limit := c.QueryInt("limit", 20)

	sessions, total, err := h.service.ListSessions(userID, page, limit)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi lấy danh sách phiên chat")
	}

	return utils.SuccessResponse(c, "Danh sách phiên chat", fiber.Map{
		"data":  sessions,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

// GetSession handles GET /api/v4/ai/chat/sessions/:uuid
func (h *AIChatHandler) GetSession(c *fiber.Ctx) error {
	userID, err := h.requireUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}

	sessionUUID := c.Params("uuid")
	session, err := h.service.GetSession(sessionUUID, userID)
	if err != nil {
		if err.Error() == "forbidden" {
			return utils.ErrorResponse(c, fiber.StatusForbidden, "Không có quyền truy cập")
		}
		return utils.ErrorResponse(c, fiber.StatusNotFound, "Không tìm thấy phiên chat")
	}

	return utils.SuccessResponse(c, "Chi tiết phiên chat", session)
}

// DeleteSession handles DELETE /api/v4/ai/chat/sessions/:uuid
func (h *AIChatHandler) DeleteSession(c *fiber.Ctx) error {
	userID, err := h.requireUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}

	sessionUUID := c.Params("uuid")
	if err := h.service.DeleteSession(sessionUUID, userID); err != nil {
		if err.Error() == "forbidden" {
			return utils.ErrorResponse(c, fiber.StatusForbidden, "Không có quyền truy cập")
		}
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi xoá phiên chat")
	}

	return utils.SuccessResponse(c, "Đã xoá phiên chat", nil)
}

// SendMessage handles POST /api/v4/ai/chat/sessions/:uuid/messages
// Supports SSE streaming when ?stream=true
func (h *AIChatHandler) SendMessage(c *fiber.Ctx) error {
	userID, err := h.requireUserID(c)
	if err != nil {
		return utils.ErrorResponse(c, fiber.StatusUnauthorized, "Cần đăng nhập")
	}

	var req dto.AIChatMessageRequest
	if err := c.BodyParser(&req); err != nil {
		return utils.ErrorResponse(c, fiber.StatusBadRequest, "Dữ liệu không hợp lệ")
	}
	if errs := h.validator.ValidateStruct(&req); errs != nil {
		return c.Status(fiber.StatusUnprocessableEntity).JSON(fiber.Map{"errors": errs})
	}

	sessionUUID := c.Params("uuid")
	stream := req.Stream || c.QueryBool("stream", false)

	if stream {
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

			result, err := h.service.SendMessage(c.Context(), sessionUUID, userID, req, onChunk)
			if err != nil {
				errChunk, _ := json.Marshal(fiber.Map{"error": err.Error()})
				fmt.Fprintf(w, "data: %s\n\n", errChunk)
				w.Flush()
				return
			}

			done, _ := json.Marshal(fiber.Map{
				"done":        true,
				"tokens_used": result.TokensUsed,
				"created_at":  result.CreatedAt,
			})
			fmt.Fprintf(w, "data: %s\n\n", done)
			w.Flush()
		})
		return nil
	}

	result, err := h.service.SendMessage(c.Context(), sessionUUID, userID, req, nil)
	if err != nil {
		if err.Error() == "forbidden" {
			return utils.ErrorResponse(c, fiber.StatusForbidden, "Không có quyền truy cập")
		}
		if err.Error() == "session not found" {
			return utils.ErrorResponse(c, fiber.StatusNotFound, "Không tìm thấy phiên chat")
		}
		h.logger.Error("SendMessage error", zap.Error(err))
		return utils.ErrorResponse(c, fiber.StatusInternalServerError, "Lỗi gửi tin nhắn")
	}

	return utils.SuccessResponse(c, "Tin nhắn đã gửi", result)
}
