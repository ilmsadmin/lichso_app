package services

import (
	"errors"
	"fmt"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"go.uber.org/zap"
)

// SurveyService handles survey business logic.
type SurveyService struct {
	repo   *repositories.SurveyRepository
	logger *zap.Logger
}

// NewSurveyService creates a new SurveyService.
func NewSurveyService(repo *repositories.SurveyRepository, logger *zap.Logger) *SurveyService {
	return &SurveyService{repo: repo, logger: logger}
}

// Create creates a new survey.
func (s *SurveyService) Create(req *dto.CreateSurveyRequest, actorID *uuid.UUID) (*dto.SurveyResponseDTO, error) {
	questions := make(models.SurveyQuestions, len(req.Questions))
	for i, q := range req.Questions {
		questions[i] = models.SurveyQuestion{
			Title:    q.Title,
			Type:     q.Type,
			Options:  q.Options,
			Required: q.Required,
		}
	}

	survey := &models.Survey{
		Title:       req.Title,
		Description: req.Description,
		Questions:   questions,
	}

	if req.IsActive != nil {
		survey.IsActive = *req.IsActive
	} else {
		survey.IsActive = true
	}

	survey.CreatedBy = actorID

	if err := s.repo.Create(survey); err != nil {
		s.logger.Error("Failed to create survey", zap.Error(err))
		return nil, fmt.Errorf("failed to create survey: %w", err)
	}

	return toSurveyResponse(survey), nil
}

// GetByID returns a survey by ID.
func (s *SurveyService) GetByID(id uuid.UUID) (*dto.SurveyResponseDTO, error) {
	survey, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("survey not found: %w", err)
	}
	return toSurveyResponse(survey), nil
}

// GetLatestActive returns the latest active survey for the client.
func (s *SurveyService) GetLatestActive() (*dto.SurveyResponseDTO, error) {
	survey, err := s.repo.GetLatestActive()
	if err != nil {
		return nil, fmt.Errorf("failed to fetch active survey: %w", err)
	}
	if survey == nil {
		return nil, nil // no active survey
	}
	return toSurveyResponse(survey), nil
}

// ListAll returns all surveys for admin.
func (s *SurveyService) ListAll(page, pageSize int, search string) ([]dto.SurveyResponseDTO, int64, error) {
	surveys, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch surveys: %w", err)
	}

	result := make([]dto.SurveyResponseDTO, len(surveys))
	for i, sv := range surveys {
		result[i] = *toSurveyResponse(&sv)
	}
	return result, total, nil
}

// Update updates a survey.
func (s *SurveyService) Update(id uuid.UUID, req *dto.UpdateSurveyRequest) (*dto.SurveyResponseDTO, error) {
	survey, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("survey not found: %w", err)
	}

	if req.Title != nil {
		survey.Title = *req.Title
	}
	if req.Description != nil {
		survey.Description = *req.Description
	}
	if req.Questions != nil {
		questions := make(models.SurveyQuestions, len(req.Questions))
		for i, q := range req.Questions {
			questions[i] = models.SurveyQuestion{
				Title:    q.Title,
				Type:     q.Type,
				Options:  q.Options,
				Required: q.Required,
			}
		}
		survey.Questions = questions
	}
	if req.IsActive != nil {
		survey.IsActive = *req.IsActive
	}

	if err := s.repo.Update(survey); err != nil {
		s.logger.Error("Failed to update survey", zap.Error(err))
		return nil, fmt.Errorf("failed to update survey: %w", err)
	}

	return toSurveyResponse(survey), nil
}

// ToggleActive toggles a survey's active status.
func (s *SurveyService) ToggleActive(id uuid.UUID) (*dto.SurveyResponseDTO, error) {
	survey, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("survey not found: %w", err)
	}

	survey.IsActive = !survey.IsActive
	if err := s.repo.Update(survey); err != nil {
		s.logger.Error("Failed to toggle survey status", zap.Error(err))
		return nil, fmt.Errorf("failed to toggle survey status: %w", err)
	}

	return toSurveyResponse(survey), nil
}

// Delete soft-deletes a survey.
func (s *SurveyService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete survey", zap.Error(err))
		return fmt.Errorf("failed to delete survey: %w", err)
	}
	return nil
}

// SubmitResponse stores a client response.
func (s *SurveyService) SubmitResponse(surveyID uuid.UUID, req *dto.SubmitSurveyResponseRequest, userID *uuid.UUID) error {
	survey, err := s.repo.GetByID(surveyID)
	if err != nil {
		return fmt.Errorf("survey not found: %w", err)
	}

	if !survey.IsActive {
		return errors.New("survey is no longer active")
	}

	// Check if already submitted
	already, err := s.repo.HasSubmitted(surveyID, userID, req.DeviceID)
	if err != nil {
		s.logger.Warn("Failed to check prior survey submissions", zap.Error(err))
	}
	if already {
		return errors.New("you have already submitted a response for this survey")
	}

	// Transform answers
	answers := make(models.UserAnswers, len(req.Answers))
	for i, a := range req.Answers {
		answers[i] = models.UserAnswer{
			QuestionIndex:   a.QuestionIndex,
			SelectedOptions: a.SelectedOptions,
			TextAnswer:      a.TextAnswer,
		}
	}

	// Validate required fields
	for idx, q := range survey.Questions {
		if q.Required {
			answered := false
			for _, ans := range answers {
				if ans.QuestionIndex == idx {
					if q.Type == "text" && ans.TextAnswer != "" {
						answered = true
					} else if (q.Type == "single_choice" || q.Type == "multiple_choice") && len(ans.SelectedOptions) > 0 {
						answered = true
					}
					break
				}
			}
			if !answered {
				return fmt.Errorf("question '%s' is required", q.Title)
			}
		}
	}

	resp := &models.SurveyResponse{
		SurveyID: surveyID,
		UserID:   userID,
		DeviceID: req.DeviceID,
		Answers:  answers,
	}

	if err := s.repo.CreateResponse(resp); err != nil {
		s.logger.Error("Failed to save survey response", zap.Error(err))
		return fmt.Errorf("failed to save response: %w", err)
	}

	return nil
}

// GetSurveyStats aggregates survey statistics for admin.
func (s *SurveyService) GetSurveyStats(id uuid.UUID) (*dto.SurveyStatsResponse, error) {
	survey, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("survey not found: %w", err)
	}

	responses, err := s.repo.ListResponsesBySurveyID(id)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch survey responses: %w", err)
	}

	totalResponses := len(responses)
	qStats := make([]dto.QuestionStatsDTO, len(survey.Questions))

	// Pre-initialize stats structure based on questions
	for i, q := range survey.Questions {
		stats := dto.QuestionStatsDTO{
			QuestionIndex: i,
			QuestionTitle: q.Title,
			Type:          q.Type,
			TotalAnswers:  0,
		}

		if q.Type == "single_choice" || q.Type == "multiple_choice" {
			stats.OptionCounts = make([]dto.StatsOptionCount, len(q.Options))
			for j, opt := range q.Options {
				stats.OptionCounts[j] = dto.StatsOptionCount{
					Option: opt,
					Count:  0,
					Pct:    0.0,
				}
			}
		} else {
			stats.TextAnswers = make([]string, 0)
		}

		qStats[i] = stats
	}

	// Aggregate response answers
	for _, resp := range responses {
		for _, ans := range resp.Answers {
			idx := ans.QuestionIndex
			if idx < 0 || idx >= len(qStats) {
				continue
			}

			qType := qStats[idx].Type
			if qType == "text" {
				if ans.TextAnswer != "" {
					qStats[idx].TotalAnswers++
					// Cap text responses to latest 500 for safety
					if len(qStats[idx].TextAnswers) < 500 {
						qStats[idx].TextAnswers = append(qStats[idx].TextAnswers, ans.TextAnswer)
					}
				}
			} else if qType == "single_choice" || qType == "multiple_choice" {
				if len(ans.SelectedOptions) > 0 {
					qStats[idx].TotalAnswers++
					for _, opt := range ans.SelectedOptions {
						for oIdx := range qStats[idx].OptionCounts {
							if qStats[idx].OptionCounts[oIdx].Option == opt {
								qStats[idx].OptionCounts[oIdx].Count++
							}
						}
					}
				}
			}
		}
	}

	// Compute percentages for options
	for i := range qStats {
		qType := qStats[i].Type
		if qType == "single_choice" || qType == "multiple_choice" {
			totalForQuestion := qStats[i].TotalAnswers
			if totalForQuestion > 0 {
				for oIdx := range qStats[i].OptionCounts {
					count := qStats[i].OptionCounts[oIdx].Count
					qStats[i].OptionCounts[oIdx].Pct = float64(count) / float64(totalForQuestion) * 100.0
				}
			}
		}
	}

	return &dto.SurveyStatsResponse{
		SurveyID:       survey.ID.String(),
		SurveyTitle:    survey.Title,
		TotalResponses: totalResponses,
		QuestionStats:  qStats,
	}, nil
}

func toSurveyResponse(sv *models.Survey) *dto.SurveyResponseDTO {
	questions := make([]dto.SurveyQuestionDTO, len(sv.Questions))
	for i, q := range sv.Questions {
		questions[i] = dto.SurveyQuestionDTO{
			Title:    q.Title,
			Type:     q.Type,
			Options:  q.Options,
			Required: q.Required,
		}
	}

	var creatorIDStr *string
	if sv.CreatedBy != nil {
		s := sv.CreatedBy.String()
		creatorIDStr = &s
	}

	return &dto.SurveyResponseDTO{
		ID:          sv.ID.String(),
		Title:       sv.Title,
		Description: sv.Description,
		Questions:   questions,
		IsActive:    sv.IsActive,
		CreatedBy:   creatorIDStr,
		CreatedAt:   sv.CreatedAt,
		UpdatedAt:   sv.UpdatedAt,
	}
}
