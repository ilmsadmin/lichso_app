package models

import (
	"time"

	"github.com/google/uuid"
)

// ============================================
// AI Generation Log — tracks every AI API call
// ============================================

// AIGenerationLog records each call made to the OpenRouter API
type AIGenerationLog struct {
	ID               uint64     `gorm:"primaryKey;autoIncrement"         json:"id"`
	UserID           *uuid.UUID `gorm:"type:uuid;index"                  json:"user_id,omitempty"`
	GenerationType   string     `gorm:"size:50;not null"                 json:"generation_type"` // "article" | "horoscope" | "chat"
	ModelUsed        string     `gorm:"size:100;not null"                json:"model_used"`
	PromptTokens     int        `gorm:"default:0"                        json:"prompt_tokens"`
	CompletionTokens int        `gorm:"default:0"                        json:"completion_tokens"`
	TotalTokens      int        `gorm:"default:0"                        json:"total_tokens"`
	CostUSD          float64    `gorm:"type:decimal(10,6);default:0"     json:"cost_usd"`
	DurationMs       int        `gorm:"default:0"                        json:"duration_ms"`
	Status           string     `gorm:"size:20;default:'success'"        json:"status"` // "success" | "error" | "timeout"
	ErrorMessage     string     `gorm:"type:text"                        json:"error_message,omitempty"`
	Metadata         JSONB      `gorm:"type:jsonb;default:'{}'"          json:"metadata,omitempty"`
	CreatedAt        time.Time  `                                        json:"created_at"`
}

func (AIGenerationLog) TableName() string { return "ai_generation_logs" }

// ============================================
// AI Horoscope Session — one tử vi reading
// ============================================

// AIHoroscopeSession stores each AI horoscope reading request & result
type AIHoroscopeSession struct {
	ID          uint64     `gorm:"primaryKey;autoIncrement"     json:"id"`
	UserID      *uuid.UUID `gorm:"type:uuid;index"              json:"user_id,omitempty"`
	SessionKey  string     `gorm:"size:64;index"                json:"session_key"` // SHA-256 of input for cache lookup
	BirthYear   int        `gorm:"not null"                     json:"birth_year"`
	BirthMonth  int        `gorm:"not null"                     json:"birth_month"`
	BirthDay    int        `gorm:"not null"                     json:"birth_day"`
	BirthHour   *int       `                                    json:"birth_hour,omitempty"`
	Gender      string     `gorm:"size:10;not null"             json:"gender"` // "male" | "female"
	ReadingType string     `gorm:"size:30;not null"             json:"reading_type"`
	Depth       string     `gorm:"size:20;default:'standard'"   json:"depth"`
	TargetYear  *int       `                                    json:"target_year,omitempty"`
	TargetMonth *int       `                                    json:"target_month,omitempty"`
	Question    string     `gorm:"type:text"                    json:"question,omitempty"`
	// Calculated data (backend)
	BatTu   JSONB `gorm:"type:jsonb"                   json:"bat_tu,omitempty"`
	NguHanh JSONB `gorm:"type:jsonb"                   json:"ngu_hanh,omitempty"`
	// AI result
	AIResult   string    `gorm:"type:text"                    json:"ai_result,omitempty"`
	ModelUsed  string    `gorm:"size:100"                     json:"model_used,omitempty"`
	TokensUsed int       `gorm:"default:0"                    json:"tokens_used"`
	CostUSD    float64   `gorm:"type:decimal(10,6);default:0" json:"cost_usd"`
	IPAddress  string    `gorm:"size:45"                      json:"ip_address,omitempty"`
	CreatedAt  time.Time `                                    json:"created_at"`
}

func (AIHoroscopeSession) TableName() string { return "ai_horoscope_sessions" }

// ============================================
// AI Prompt Template — reusable prompts
// ============================================

// AIPromptTemplate stores system & user prompt templates with placeholders
type AIPromptTemplate struct {
	ID           uint64    `gorm:"primaryKey;autoIncrement"    json:"id"`
	Name         string    `gorm:"size:200;uniqueIndex;not null" json:"name"`
	Type         string    `gorm:"size:50;not null"            json:"type"` // "article" | "horoscope" | "chat"
	SystemPrompt string    `gorm:"type:text;not null"          json:"system_prompt"`
	UserPrompt   string    `gorm:"type:text;not null"          json:"user_prompt"`
	Model        string    `gorm:"size:100"                    json:"model,omitempty"`
	MaxTokens    int       `gorm:"default:2048"                json:"max_tokens"`
	Temperature  float64   `gorm:"type:decimal(3,2);default:0.7" json:"temperature"`
	IsActive     bool      `gorm:"default:true"                json:"is_active"`
	CreatedBy    *uint64   `                                   json:"created_by,omitempty"`
	CreatedAt    time.Time `                                   json:"created_at"`
	UpdatedAt    time.Time `                                   json:"updated_at"`
}

func (AIPromptTemplate) TableName() string { return "ai_prompt_templates" }

// ============================================
// AI Chat Session — multi-turn conversation
// ============================================

// AIChatSession holds a multi-turn chat conversation for a user
type AIChatSession struct {
	ID            uint64     `gorm:"primaryKey;autoIncrement"    json:"id"`
	UserID        uuid.UUID  `gorm:"type:uuid;index;not null"    json:"user_id"`
	SessionUUID   string     `gorm:"size:36;uniqueIndex;not null" json:"session_uuid"`
	Title         string     `gorm:"size:200"                    json:"title,omitempty"`
	Context       JSONB      `gorm:"type:jsonb;default:'{}'"     json:"context,omitempty"`
	Messages      JSONB      `gorm:"type:jsonb;default:'[]'"     json:"messages,omitempty"`
	TotalTokens   int        `gorm:"default:0"                   json:"total_tokens"`
	TotalCost     float64    `gorm:"type:decimal(10,4);default:0" json:"total_cost"`
	IsActive      bool       `gorm:"default:true"                json:"is_active"`
	LastMessageAt *time.Time `                                   json:"last_message_at,omitempty"`
	CreatedAt     time.Time  `                                   json:"created_at"`
}

func (AIChatSession) TableName() string { return "ai_chat_sessions" }

// ============================================
// AI Usage Quota — rate limiting per user/day
// ============================================

// AIUsageQuota tracks how many times a user has used AI in a given period
type AIUsageQuota struct {
	ID         uint64     `gorm:"primaryKey;autoIncrement"       json:"id"`
	UserID     *uuid.UUID `gorm:"type:uuid;index"                json:"user_id,omitempty"` // nil = guest (tracked by IP)
	IPAddress  string     `gorm:"size:45;index"                  json:"ip_address,omitempty"`
	QuotaType  string     `gorm:"size:50;not null"               json:"quota_type"` // "horoscope_daily"
	PeriodKey  string     `gorm:"size:20;not null"               json:"period_key"` // "2026-03-12"
	UsedCount  int        `gorm:"default:0"                      json:"used_count"`
	LimitCount int        `gorm:"not null"                       json:"limit_count"`
	ResetAt    time.Time  `                                      json:"reset_at"`
	CreatedAt  time.Time  `                                      json:"created_at"`
	UpdatedAt  time.Time  `                                      json:"updated_at"`
}

func (AIUsageQuota) TableName() string { return "ai_usage_quotas" }
