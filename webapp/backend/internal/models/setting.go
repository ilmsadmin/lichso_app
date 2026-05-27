package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// Setting represents an application setting stored in MongoDB
type Setting struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Key         string             `bson:"key" json:"key"`
	Value       interface{}        `bson:"value" json:"value"`
	Group       string             `bson:"group" json:"group"`
	Description string             `bson:"description,omitempty" json:"description,omitempty"`
	UpdatedBy   string             `bson:"updated_by,omitempty" json:"updated_by,omitempty"`
	UpdatedAt   time.Time          `bson:"updated_at" json:"updated_at"`
}

// CollectionNameSettings returns the MongoDB collection name for settings
func (Setting) CollectionName() string {
	return "app_settings"
}

// SettingResponse represents the setting data returned in API responses
type SettingResponse struct {
	ID          string      `json:"id"`
	Key         string      `json:"key"`
	Value       interface{} `json:"value"`
	Group       string      `json:"group"`
	Description string      `json:"description,omitempty"`
	UpdatedBy   string      `json:"updated_by,omitempty"`
	UpdatedAt   time.Time   `json:"updated_at"`
}

// ToResponse converts Setting to SettingResponse
func (s *Setting) ToResponse() SettingResponse {
	return SettingResponse{
		ID:          s.ID.Hex(),
		Key:         s.Key,
		Value:       s.Value,
		Group:       s.Group,
		Description: s.Description,
		UpdatedBy:   s.UpdatedBy,
		UpdatedAt:   s.UpdatedAt,
	}
}

// Setting groups
const (
	SettingGroupGeneral  = "general"
	SettingGroupEmail    = "email"
	SettingGroupSecurity = "security"
	SettingGroupAI       = "ai"
)

// Known setting keys
const (
	SettingKeySiteName           = "site_name"
	SettingKeySiteDescription    = "site_description"
	SettingKeySiteURL            = "site_url"
	SettingKeyMaintenanceMode    = "maintenance_mode"
	SettingKeySMTPHost           = "smtp_host"
	SettingKeySMTPPort           = "smtp_port"
	SettingKeySMTPUser           = "smtp_user"
	SettingKeySMTPPassword       = "smtp_password"
	SettingKeyFromEmail          = "from_email"
	SettingKeyFromName           = "from_name"
	SettingKeyEnableRegistration = "enable_registration"
	SettingKeyRequireEmailVerify = "require_email_verification"
	SettingKeyMaxLoginAttempts   = "max_login_attempts"
	SettingKeyLockoutDuration    = "lockout_duration"
	SettingKeySessionTimeout     = "session_timeout"
	SettingKeyEnableTwoFactor    = "enable_two_factor"

	// AI Settings keys
	SettingKeyOpenRouterAPIKey       = "openrouter_api_key"
	SettingKeyOpenRouterBaseURL      = "openrouter_base_url"
	SettingKeyAIArticleModel         = "ai_article_model"
	SettingKeyAIHoroscopeModel       = "ai_horoscope_model"
	SettingKeyAIChatModel            = "ai_chat_model"
	SettingKeyAIMaxTokensArticle     = "ai_max_tokens_article"
	SettingKeyAIMaxTokensHoroscope   = "ai_max_tokens_horoscope"
	SettingKeyAIMaxTokensChat        = "ai_max_tokens_chat"
	SettingKeyAIRateHoroscopeGuest   = "ai_rate_horoscope_guest"
	SettingKeyAIRateHoroscopeFree    = "ai_rate_horoscope_free"
	SettingKeyAIRateHoroscopePremium = "ai_rate_horoscope_premium"
	SettingKeyAIMonthlyBudgetCap     = "ai_monthly_budget_cap"
	SettingKeyAIEnabled              = "ai_enabled"

	// Banner Settings keys
	SettingKeyUseServerBanners = "use_server_banners"
)
