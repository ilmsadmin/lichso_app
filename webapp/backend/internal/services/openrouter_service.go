package services

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// ============================================
// OpenRouter Types
// ============================================

// OpenRouterMessage is a single chat message (role + content)
type OpenRouterMessage struct {
	Role    string `json:"role"` // "system" | "user" | "assistant"
	Content string `json:"content"`
}

// openRouterRequest is the body sent to the OpenRouter completions endpoint
type openRouterRequest struct {
	Model       string              `json:"model"`
	Messages    []OpenRouterMessage `json:"messages"`
	Stream      bool                `json:"stream,omitempty"`
	MaxTokens   int                 `json:"max_tokens,omitempty"`
	Temperature float64             `json:"temperature,omitempty"`
}

// OpenRouterUsage tracks token usage for a completion call
type OpenRouterUsage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

// OpenRouterResponse is the non-streaming response from OpenRouter
type OpenRouterResponse struct {
	ID      string `json:"id"`
	Choices []struct {
		Message struct {
			Content string `json:"content"`
		} `json:"message"`
		FinishReason string `json:"finish_reason"`
	} `json:"choices"`
	Usage OpenRouterUsage `json:"usage"`
}

// Content returns the first choice's content or empty string
func (r *OpenRouterResponse) Content() string {
	if len(r.Choices) == 0 {
		return ""
	}
	return r.Choices[0].Message.Content
}

// ============================================
// OpenRouterService
// ============================================

// OpenRouterService wraps the OpenRouter.ai HTTP API
type OpenRouterService struct {
	cfg         *config.AIConfig
	httpClient  *http.Client
	logger      *zap.Logger
	settingRepo *repositories.SettingRepository // optional: load key from DB at runtime
}

// NewOpenRouterService creates a new OpenRouterService
func NewOpenRouterService(cfg *config.AIConfig, logger *zap.Logger) *OpenRouterService {
	return &OpenRouterService{
		cfg: cfg,
		httpClient: &http.Client{
			Timeout: 120 * time.Second,
		},
		logger: logger,
	}
}

// SetSettingRepo injects the setting repository so the service can load
// the OpenRouter API key from DB at runtime (overrides env config).
func (s *OpenRouterService) SetSettingRepo(repo *repositories.SettingRepository) {
	s.settingRepo = repo
}

// resolveAPIKey returns the API key: DB setting takes priority over env config.
func (s *OpenRouterService) resolveAPIKey() string {
	if s.settingRepo != nil {
		if setting, err := s.settingRepo.FindByKey(context.Background(), "openrouter_api_key"); err == nil {
			if v, ok := setting.Value.(string); ok && v != "" {
				return v
			}
		}
	}
	return s.cfg.OpenRouterAPIKey
}

// resolveBaseURL returns the base URL: DB setting takes priority over env config.
func (s *OpenRouterService) resolveBaseURL() string {
	if s.settingRepo != nil {
		if setting, err := s.settingRepo.FindByKey(context.Background(), "openrouter_base_url"); err == nil {
			if v, ok := setting.Value.(string); ok && v != "" {
				return v
			}
		}
	}
	if s.cfg.OpenRouterBaseURL != "" {
		return s.cfg.OpenRouterBaseURL
	}
	return "https://openrouter.ai/api/v1"
}

// IsConfigured returns true when an API key is set
func (s *OpenRouterService) IsConfigured() bool {
	return s.resolveAPIKey() != ""
}

// Complete calls the completions endpoint without streaming and returns the full response
func (s *OpenRouterService) Complete(ctx context.Context, model string, messages []OpenRouterMessage, maxTokens int, temperature float64) (*OpenRouterResponse, error) {
	if !s.IsConfigured() {
		return nil, fmt.Errorf("OpenRouter API key is not configured")
	}

	reqBody := openRouterRequest{
		Model:       model,
		Messages:    messages,
		MaxTokens:   maxTokens,
		Temperature: temperature,
	}

	body, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("marshal request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.resolveBaseURL()+"/chat/completions", bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("create request: %w", err)
	}
	s.setHeaders(req)

	start := time.Now()
	resp, err := s.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("do request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		raw, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("openrouter status %d: %s", resp.StatusCode, string(raw))
	}

	var result OpenRouterResponse
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, fmt.Errorf("decode response: %w", err)
	}

	s.logger.Debug("OpenRouter complete",
		zap.String("model", model),
		zap.Int("total_tokens", result.Usage.TotalTokens),
		zap.Duration("duration", time.Since(start)),
	)

	return &result, nil
}

// StreamChunk is called for each delta token during streaming.
// Returning a non-nil error aborts the stream.
type StreamChunk struct {
	Delta string
	Done  bool
}

// Stream calls the completions endpoint with streaming enabled.
// onChunk is invoked for every token delta; returning an error stops the stream.
// The final OpenRouterUsage is returned after the stream completes.
func (s *OpenRouterService) Stream(ctx context.Context, model string, messages []OpenRouterMessage, maxTokens int, onChunk func(delta string) error) (*OpenRouterUsage, error) {
	if !s.IsConfigured() {
		return nil, fmt.Errorf("OpenRouter API key is not configured")
	}

	reqBody := openRouterRequest{
		Model:     model,
		Messages:  messages,
		MaxTokens: maxTokens,
		Stream:    true,
	}

	body, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("marshal request: %w", err)
	}

	// Guard against nil context (e.g. from fasthttp async callbacks)
	if ctx == nil {
		ctx = context.Background()
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.resolveBaseURL()+"/chat/completions", bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("create request: %w", err)
	}
	s.setHeaders(req)

	resp, err := s.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("do request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		raw, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("openrouter status %d: %s", resp.StatusCode, string(raw))
	}

	usage := &OpenRouterUsage{}
	scanner := bufio.NewScanner(resp.Body)

	for scanner.Scan() {
		line := scanner.Text()

		if !strings.HasPrefix(line, "data: ") {
			continue
		}

		data := strings.TrimPrefix(line, "data: ")
		if data == "[DONE]" {
			break
		}

		var chunk struct {
			Choices []struct {
				Delta struct {
					Content string `json:"content"`
				} `json:"delta"`
			} `json:"choices"`
			Usage *OpenRouterUsage `json:"usage"`
		}

		if err := json.Unmarshal([]byte(data), &chunk); err != nil {
			continue // skip malformed chunks
		}

		if len(chunk.Choices) > 0 {
			delta := chunk.Choices[0].Delta.Content
			if delta != "" {
				if err := onChunk(delta); err != nil {
					return usage, err // caller cancelled
				}
			}
		}

		if chunk.Usage != nil {
			*usage = *chunk.Usage
		}
	}

	if err := scanner.Err(); err != nil {
		return usage, fmt.Errorf("reading stream: %w", err)
	}

	return usage, nil
}

// setHeaders adds authentication and identification headers
func (s *OpenRouterService) setHeaders(req *http.Request) {
	req.Header.Set("Authorization", "Bearer "+s.resolveAPIKey())
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("HTTP-Referer", s.cfg.SiteURL)
	req.Header.Set("X-Title", s.cfg.SiteName)
}
