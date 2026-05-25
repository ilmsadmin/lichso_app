package services

import (
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
)

// NewPromptTemplateFromRequest converts a dto request to a model for creation
func NewPromptTemplateFromRequest(req dto.AIPromptTemplateRequest) *models.AIPromptTemplate {
	maxTokens := req.MaxTokens
	if maxTokens == 0 {
		maxTokens = 2048
	}
	temp := req.Temperature
	if temp == 0 {
		temp = 0.7
	}
	return &models.AIPromptTemplate{
		Name:         req.Name,
		Type:         req.Type,
		SystemPrompt: req.SystemPrompt,
		UserPrompt:   req.UserPrompt,
		Model:        req.Model,
		MaxTokens:    maxTokens,
		Temperature:  temp,
		IsActive:     req.IsActive,
	}
}
