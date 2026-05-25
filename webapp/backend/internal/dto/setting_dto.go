package dto

// UpdateSettingRequest represents a single setting update
type UpdateSettingRequest struct {
	Key   string      `json:"key" validate:"required"`
	Value interface{} `json:"value" validate:"required"`
}

// UpdateSettingsGroupRequest represents a batch settings update for a group
type UpdateSettingsGroupRequest struct {
	Group    string                 `json:"group" validate:"required"`
	Settings []UpdateSettingRequest `json:"settings" validate:"required,min=1,dive"`
}

// CreateSettingRequest represents a request to create a new setting
type CreateSettingRequest struct {
	Key         string      `json:"key" validate:"required,min=1,max=100"`
	Value       interface{} `json:"value" validate:"required"`
	Group       string      `json:"group" validate:"required,min=1,max=50"`
	Description string      `json:"description,omitempty"`
}
