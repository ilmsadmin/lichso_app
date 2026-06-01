package dto

import "time"

// SurveyQuestionDTO represents a question inside survey requests.
type SurveyQuestionDTO struct {
	Title    string   `json:"title" validate:"required"`
	Type     string   `json:"type" validate:"required,oneof=single_choice multiple_choice text"`
	Options  []string `json:"options"`
	Required bool     `json:"required"`
}

// CreateSurveyRequest represents a request to create a survey.
type CreateSurveyRequest struct {
	Title       string              `json:"title" validate:"required,max=255"`
	Description string              `json:"description"`
	Questions   []SurveyQuestionDTO `json:"questions" validate:"required,dive"`
	IsActive    *bool               `json:"is_active"`
}

// UpdateSurveyRequest represents a request to update a survey.
type UpdateSurveyRequest struct {
	Title       *string             `json:"title" validate:"omitempty,max=255"`
	Description *string             `json:"description"`
	Questions   []SurveyQuestionDTO `json:"questions" validate:"omitempty,dive"`
	IsActive    *bool               `json:"is_active"`
}

// SurveyResponseDTO represents a survey in API responses.
type SurveyResponseDTO struct {
	ID          string              `json:"id"`
	Title       string              `json:"title"`
	Description string              `json:"description,omitempty"`
	Questions   []SurveyQuestionDTO `json:"questions"`
	IsActive    bool                `json:"is_active"`
	CreatedBy   *string             `json:"created_by,omitempty"`
	CreatedAt   time.Time           `json:"created_at"`
	UpdatedAt   time.Time           `json:"updated_at"`
}

// UserAnswerDTO represents an answer in a survey response submission.
type UserAnswerDTO struct {
	QuestionIndex   int      `json:"question_index"`
	SelectedOptions []string `json:"selected_options"`
	TextAnswer      string   `json:"text_answer"`
}

// SubmitSurveyResponseRequest represents a request to submit a survey response.
type SubmitSurveyResponseRequest struct {
	Answers  []UserAnswerDTO `json:"answers" validate:"required,dive"`
	DeviceID string          `json:"device_id" validate:"omitempty,max=255"`
}

// StatsOptionCount represents choice stats.
type StatsOptionCount struct {
	Option string  `json:"option"`
	Count  int     `json:"count"`
	Pct    float64 `json:"percentage"`
}

// QuestionStatsDTO represents aggregated statistics for a single question.
type QuestionStatsDTO struct {
	QuestionIndex int                `json:"question_index"`
	QuestionTitle string             `json:"question_title"`
	Type          string             `json:"type"`
	TotalAnswers  int                `json:"total_answers"`
	OptionCounts  []StatsOptionCount `json:"option_counts,omitempty"`
	TextAnswers   []string           `json:"text_answers,omitempty"`
}

// SurveyStatsResponse represents aggregated survey statistics.
type SurveyStatsResponse struct {
	SurveyID       string             `json:"survey_id"`
	SurveyTitle    string             `json:"survey_title"`
	TotalResponses int                `json:"total_responses"`
	QuestionStats  []QuestionStatsDTO `json:"question_stats"`
}
