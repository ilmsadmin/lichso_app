package services

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// ============================================
// AIChatService
// ============================================

// chatMessage is a single message in the messages JSONB array
type chatMessage struct {
	Role      string `json:"role"`
	Content   string `json:"content"`
	CreatedAt string `json:"created_at"`
}

// AIChatService manages multi-turn AI chat sessions
type AIChatService struct {
	openRouter *OpenRouterService
	chatRepo   *repositories.AIChatRepository
	logRepo    *repositories.AILogRepository
	promptRepo *repositories.AIPromptTemplateRepository
	cfg        *config.AIConfig
	logger     *zap.Logger
}

// NewAIChatService creates a new AIChatService
func NewAIChatService(
	openRouter *OpenRouterService,
	chatRepo *repositories.AIChatRepository,
	logRepo *repositories.AILogRepository,
	promptRepo *repositories.AIPromptTemplateRepository,
	cfg *config.AIConfig,
	logger *zap.Logger,
) *AIChatService {
	return &AIChatService{
		openRouter: openRouter,
		chatRepo:   chatRepo,
		logRepo:    logRepo,
		promptRepo: promptRepo,
		cfg:        cfg,
		logger:     logger,
	}
}

// ============================================
// Public API
// ============================================

// CreateSession opens a new chat session for a user
func (s *AIChatService) CreateSession(userID uuid.UUID, req dto.AIChatCreateRequest) (*dto.AIChatSessionResponse, error) {
	title := req.Title
	if title == "" {
		title = "Cuộc trò chuyện mới"
	}

	contextMap := models.JSONB{}
	if req.Context != nil {
		for k, v := range req.Context {
			contextMap[k] = v
		}
	}

	session := &models.AIChatSession{
		UserID:      userID,
		SessionUUID: uuid.New().String(),
		Title:       title,
		Context:     contextMap,
		Messages:    models.JSONB{"messages": []interface{}{}},
		IsActive:    true,
	}

	if err := s.chatRepo.Create(session); err != nil {
		return nil, fmt.Errorf("create chat session: %w", err)
	}

	return s.sessionToResponse(session, nil), nil
}

// GetSession returns a chat session with full message history
func (s *AIChatService) GetSession(sessionUUID string, userID uuid.UUID) (*dto.AIChatSessionResponse, error) {
	session, err := s.chatRepo.GetByUUID(sessionUUID)
	if err != nil {
		return nil, fmt.Errorf("session not found")
	}

	if session.UserID != userID {
		return nil, fmt.Errorf("forbidden")
	}

	messages, _ := s.extractMessages(session)
	return s.sessionToResponse(session, messages), nil
}

// ListSessions returns all active sessions for a user
func (s *AIChatService) ListSessions(userID uuid.UUID, page, limit int) ([]dto.AIChatSessionResponse, int64, error) {
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 50 {
		limit = 20
	}

	sessions, total, err := s.chatRepo.ListByUser(userID, page, limit)
	if err != nil {
		return nil, 0, err
	}

	var result []dto.AIChatSessionResponse
	for _, sess := range sessions {
		result = append(result, *s.sessionToResponse(&sess, nil))
	}
	return result, total, nil
}

// DeleteSession soft-deletes a session
func (s *AIChatService) DeleteSession(sessionUUID string, userID uuid.UUID) error {
	session, err := s.chatRepo.GetByUUID(sessionUUID)
	if err != nil {
		return fmt.Errorf("session not found")
	}
	if session.UserID != userID {
		return fmt.Errorf("forbidden")
	}
	return s.chatRepo.SoftDelete(sessionUUID, userID)
}

// SendMessage appends a user message, calls AI, appends assistant reply, and returns the reply.
// When onChunk is non-nil the AI response is streamed via that callback.
func (s *AIChatService) SendMessage(ctx context.Context, sessionUUID string, userID uuid.UUID, req dto.AIChatMessageRequest, onChunk func(string) error) (*dto.AIChatMessageResponse, error) {
	session, err := s.chatRepo.GetByUUID(sessionUUID)
	if err != nil {
		return nil, fmt.Errorf("session not found")
	}
	if session.UserID != userID {
		return nil, fmt.Errorf("forbidden")
	}

	// Extract history
	history, err := s.extractMessages(session)
	if err != nil {
		history = nil
	}

	// Append user message to history
	userMsg := chatMessage{
		Role:      "user",
		Content:   req.Content,
		CreatedAt: time.Now().Format(time.RFC3339),
	}
	history = append(history, userMsg)

	// Build messages for OpenRouter (include system prompt)
	systemPrompt := s.buildSystemPrompt()
	orMessages := []OpenRouterMessage{{Role: "system", Content: systemPrompt}}
	for _, m := range history {
		orMessages = append(orMessages, OpenRouterMessage{Role: m.Role, Content: m.Content})
	}

	model := s.cfg.DefaultChatModel
	start := time.Now()

	var assistantContent string
	var promptT, completionT, totalT int
	var costUSD float64

	if onChunk != nil {
		// Streaming
		var sb strings.Builder
		usage, err := s.openRouter.Stream(ctx, model, orMessages, s.cfg.MaxTokensChat, func(delta string) error {
			sb.WriteString(delta)
			return onChunk(delta)
		})
		if err != nil {
			return nil, fmt.Errorf("AI stream error: %w", err)
		}
		assistantContent = sb.String()
		if usage != nil {
			promptT, completionT, totalT = usage.PromptTokens, usage.CompletionTokens, usage.TotalTokens
			costUSD = estimateCost(model, promptT, completionT)
		}
	} else {
		// Non-streaming
		resp, err := s.openRouter.Complete(ctx, model, orMessages, s.cfg.MaxTokensChat, 0.7)
		if err != nil {
			return nil, fmt.Errorf("AI error: %w", err)
		}
		assistantContent = resp.Content()
		promptT, completionT, totalT = resp.Usage.PromptTokens, resp.Usage.CompletionTokens, resp.Usage.TotalTokens
		costUSD = estimateCost(model, promptT, completionT)
	}

	durationMs := int(time.Since(start).Milliseconds())

	_ = s.logRepo.Create(&models.AIGenerationLog{
		UserID:           nil, // chat service uses uint64 userID — omit for now
		GenerationType:   "chat",
		ModelUsed:        model,
		PromptTokens:     promptT,
		CompletionTokens: completionT,
		TotalTokens:      totalT,
		CostUSD:          costUSD,
		DurationMs:       durationMs,
		Status:           "success",
	})

	// Append assistant message
	now := time.Now()
	assistantMsg := chatMessage{
		Role:      "assistant",
		Content:   assistantContent,
		CreatedAt: now.Format(time.RFC3339),
	}
	history = append(history, assistantMsg)

	// Persist updated messages
	msgSlice := make([]interface{}, len(history))
	for i, m := range history {
		msgSlice[i] = m
	}
	session.Messages = models.JSONB{"messages": msgSlice}
	session.TotalTokens += totalT
	session.TotalCost += costUSD
	session.LastMessageAt = &now

	// Auto-generate title from first message
	if session.Title == "Cuộc trò chuyện mới" && len(history) <= 2 {
		session.Title = truncateTitle(req.Content, 80)
	}

	if err := s.chatRepo.Save(session); err != nil {
		s.logger.Error("Failed to save chat session", zap.Error(err))
	}

	return &dto.AIChatMessageResponse{
		Role:       "assistant",
		Content:    assistantContent,
		CreatedAt:  now.Format(time.RFC3339),
		TokensUsed: totalT,
	}, nil
}

// ============================================
// Helpers
// ============================================

// buildSystemPrompt returns the system prompt for the chat model
func (s *AIChatService) buildSystemPrompt() string {
	if tpl, err := s.promptRepo.GetActiveByType("chat"); err == nil {
		return tpl.SystemPrompt
	}
	return `Bạn là chuyên gia tư vấn phong thuỷ, tử vi và tâm linh người Việt.
Trả lời bằng tiếng Việt, chân thành, hữu ích và dễ hiểu.
Nếu câu hỏi không liên quan đến phong thuỷ / tâm linh, hãy lịch sự từ chối.`
}

// extractMessages parses the JSONB messages field into chatMessage slice
func (s *AIChatService) extractMessages(session *models.AIChatSession) ([]chatMessage, error) {
	raw, ok := session.Messages["messages"]
	if !ok {
		return nil, nil
	}

	// Re-marshal then unmarshal to []chatMessage
	b, err := json.Marshal(raw)
	if err != nil {
		return nil, err
	}
	var msgs []chatMessage
	if err := json.Unmarshal(b, &msgs); err != nil {
		return nil, err
	}
	return msgs, nil
}

// sessionToResponse converts a model to a DTO
func (s *AIChatService) sessionToResponse(session *models.AIChatSession, messages []chatMessage) *dto.AIChatSessionResponse {
	var msgResponses []dto.AIChatMessageResponse
	for _, m := range messages {
		msgResponses = append(msgResponses, dto.AIChatMessageResponse{
			Role:      m.Role,
			Content:   m.Content,
			CreatedAt: m.CreatedAt,
		})
	}

	var lastMsgAt *string
	if session.LastMessageAt != nil {
		t := session.LastMessageAt.Format(time.RFC3339)
		lastMsgAt = &t
	}

	return &dto.AIChatSessionResponse{
		SessionUUID:   session.SessionUUID,
		Title:         session.Title,
		Messages:      msgResponses,
		TotalTokens:   session.TotalTokens,
		TotalCost:     session.TotalCost,
		LastMessageAt: lastMsgAt,
		CreatedAt:     session.CreatedAt.Format(time.RFC3339),
	}
}

// truncateTitle shortens a string to maxLen characters
func truncateTitle(s string, maxLen int) string {
	if len(s) <= maxLen {
		return s
	}
	return s[:maxLen] + "..."
}
