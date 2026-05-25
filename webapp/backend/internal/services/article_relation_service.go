package services

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.uber.org/zap"
)

// ArticleRelationService handles article relation business logic.
type ArticleRelationService struct {
	relationRepo *repositories.ArticleRelationRepository
	articleRepo  *repositories.ArticleRepository
	logger       *zap.Logger
}

// NewArticleRelationService creates a new ArticleRelationService.
func NewArticleRelationService(
	relationRepo *repositories.ArticleRelationRepository,
	articleRepo *repositories.ArticleRepository,
	logger *zap.Logger,
) *ArticleRelationService {
	return &ArticleRelationService{
		relationRepo: relationRepo,
		articleRepo:  articleRepo,
		logger:       logger,
	}
}

// Create creates a new article relation.
func (s *ArticleRelationService) Create(sourceArticleID uuid.UUID, createdBy uuid.UUID, req *dto.CreateArticleRelationRequest) (*dto.ArticleRelationResponse, error) {
	targetID, err := uuid.Parse(req.TargetArticleID)
	if err != nil {
		return nil, fmt.Errorf("invalid target article ID")
	}

	// Check source article exists
	_, err = s.articleRepo.GetByID(sourceArticleID)
	if err != nil {
		return nil, fmt.Errorf("source article not found")
	}

	// Check target article exists
	_, err = s.articleRepo.GetByID(targetID)
	if err != nil {
		return nil, fmt.Errorf("target article not found")
	}

	// Check for duplicate
	exists, err := s.relationRepo.Exists(sourceArticleID, targetID, req.RelationType)
	if err != nil {
		return nil, fmt.Errorf("failed to check existing relation: %w", err)
	}
	if exists {
		return nil, fmt.Errorf("relation already exists")
	}

	relation := &models.ArticleRelation{
		SourceArticleID: sourceArticleID,
		TargetArticleID: targetID,
		RelationType:    req.RelationType,
		SortOrder:       req.SortOrder,
		IsBidirectional: req.IsBidirectional,
		CreatedBy:       &createdBy,
	}

	if err := s.relationRepo.Create(relation); err != nil {
		s.logger.Error("Failed to create article relation", zap.Error(err))
		return nil, fmt.Errorf("failed to create relation: %w", err)
	}

	// Re-fetch with preloads
	relation, _ = s.relationRepo.GetByID(relation.ID)
	return s.toRelationResponse(relation), nil
}

// BatchCreate creates multiple article relations at once.
func (s *ArticleRelationService) BatchCreate(sourceArticleID uuid.UUID, createdBy uuid.UUID, req *dto.BatchCreateArticleRelationsRequest) ([]dto.ArticleRelationResponse, error) {
	var results []dto.ArticleRelationResponse
	for _, r := range req.Relations {
		result, err := s.Create(sourceArticleID, createdBy, &r)
		if err != nil {
			s.logger.Warn("Skipping relation in batch", zap.Error(err))
			continue
		}
		results = append(results, *result)
	}
	return results, nil
}

// GetRelations returns all relations for an article.
func (s *ArticleRelationService) GetRelations(articleID uuid.UUID, relationType string) ([]dto.ArticleRelationResponse, error) {
	relations, err := s.relationRepo.GetBySourceArticle(articleID, relationType)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch relations: %w", err)
	}

	results := make([]dto.ArticleRelationResponse, len(relations))
	for i, r := range relations {
		results[i] = *s.toRelationResponse(&r)
	}
	return results, nil
}

// GetRelatedArticles returns all related articles for display (public API).
// Combines manual relations + auto-discovered (same category, same tags, random).
func (s *ArticleRelationService) GetRelatedArticles(articleID uuid.UUID, limit int) (*dto.RelatedArticlesResponse, error) {
	if limit <= 0 {
		limit = 10
	}

	response := &dto.RelatedArticlesResponse{}
	var excludeIDs []uuid.UUID
	excludeIDs = append(excludeIDs, articleID)

	// 1. Get manually defined "related" relations
	relatedArticles, err := s.relationRepo.GetAllRelated(articleID, models.RelationTypeRelated)
	if err == nil && len(relatedArticles) > 0 {
		for _, a := range relatedArticles {
			response.Related = append(response.Related, *toRelationArticleListResponse(&a))
			excludeIDs = append(excludeIDs, a.ID)
		}
	}

	// 2. Get series relations
	seriesArticles, err := s.relationRepo.GetAllRelated(articleID, models.RelationTypeSeries)
	if err == nil && len(seriesArticles) > 0 {
		for _, a := range seriesArticles {
			response.Series = append(response.Series, *toRelationArticleListResponse(&a))
			excludeIDs = append(excludeIDs, a.ID)
		}
	}

	// 3. Get reference relations
	refArticles, err := s.relationRepo.GetAllRelated(articleID, models.RelationTypeReference)
	if err == nil && len(refArticles) > 0 {
		for _, a := range refArticles {
			response.References = append(response.References, *toRelationArticleListResponse(&a))
			excludeIDs = append(excludeIDs, a.ID)
		}
	}

	// 4. If we don't have enough related articles, auto-discover by category and tags
	totalManual := len(response.Related) + len(response.Series) + len(response.References)
	remaining := limit - totalManual
	if remaining > 0 {
		article, err := s.articleRepo.GetByID(articleID)
		if err == nil {
			// Try same category first
			if article.CategoryID != nil {
				catArticles, err := s.relationRepo.GetBySameCategory(articleID, *article.CategoryID, remaining)
				if err == nil {
					for _, a := range catArticles {
						if !containsUUID(excludeIDs, a.ID) {
							response.Related = append(response.Related, *toRelationArticleListResponse(&a))
							excludeIDs = append(excludeIDs, a.ID)
							remaining--
						}
					}
				}
			}

			// Try same tags
			if remaining > 0 && len(article.Tags) > 0 {
				tagIDs := make([]uuid.UUID, len(article.Tags))
				for i, t := range article.Tags {
					tagIDs[i] = t.ID
				}
				tagArticles, err := s.relationRepo.GetBySameTags(articleID, tagIDs, remaining)
				if err == nil {
					for _, a := range tagArticles {
						if !containsUUID(excludeIDs, a.ID) {
							response.Related = append(response.Related, *toRelationArticleListResponse(&a))
							excludeIDs = append(excludeIDs, a.ID)
							remaining--
						}
					}
				}
			}
		}
	}

	// 5. Fill with random articles
	randomCount := limit - (len(response.Related) + len(response.Series) + len(response.References))
	if randomCount > 0 {
		randomArticles, err := s.relationRepo.GetRandomArticles(excludeIDs, randomCount)
		if err == nil {
			for _, a := range randomArticles {
				response.RandomPicks = append(response.RandomPicks, *toRelationArticleListResponse(&a))
			}
		}
	}

	return response, nil
}

// Update updates an article relation.
func (s *ArticleRelationService) Update(id uuid.UUID, req *dto.UpdateArticleRelationRequest) (*dto.ArticleRelationResponse, error) {
	relation, err := s.relationRepo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("relation not found")
	}

	if req.RelationType != nil {
		relation.RelationType = *req.RelationType
	}
	if req.SortOrder != nil {
		relation.SortOrder = *req.SortOrder
	}
	if req.IsBidirectional != nil {
		relation.IsBidirectional = *req.IsBidirectional
	}

	if err := s.relationRepo.Create(relation); err != nil {
		return nil, fmt.Errorf("failed to update relation: %w", err)
	}

	relation, _ = s.relationRepo.GetByID(id)
	return s.toRelationResponse(relation), nil
}

// Delete deletes an article relation.
func (s *ArticleRelationService) Delete(id uuid.UUID) error {
	if err := s.relationRepo.Delete(id); err != nil {
		s.logger.Error("Failed to delete article relation", zap.Error(err))
		return fmt.Errorf("failed to delete relation: %w", err)
	}
	return nil
}

// ============================================
// Helper functions
// ============================================

func (s *ArticleRelationService) toRelationResponse(r *models.ArticleRelation) *dto.ArticleRelationResponse {
	resp := &dto.ArticleRelationResponse{
		ID:              r.ID.String(),
		SourceArticleID: r.SourceArticleID.String(),
		TargetArticleID: r.TargetArticleID.String(),
		RelationType:    r.RelationType,
		SortOrder:       r.SortOrder,
		IsBidirectional: r.IsBidirectional,
		CreatedAt:       r.CreatedAt.Format(time.RFC3339),
	}

	if r.TargetArticle != nil {
		resp.TargetArticle = toRelationArticleListResponse(r.TargetArticle)
	}

	return resp
}

func toRelationArticleListResponse(a *models.Article) *dto.ArticleListResponse {
	resp := &dto.ArticleListResponse{
		ID:            a.ID.String(),
		Title:         a.Title,
		Slug:          a.Slug,
		Excerpt:       a.Excerpt,
		FeaturedImage: utils.NormalizeUploadURL(a.FeaturedImage),
		Status:        a.Status,
		ViewCount:     a.ViewCount,
		ReadingTime:   a.ReadingTime,
		IsFeatured:    a.IsFeatured,
		CreatedAt:     a.CreatedAt.Format(time.RFC3339),
	}

	if a.PublishedAt != nil {
		resp.PublishedAt = a.PublishedAt.Format(time.RFC3339)
	}
	if a.Category != nil {
		resp.Category = &dto.ArticleCategoryResponse{
			ID:   a.Category.ID.String(),
			Name: a.Category.Name,
			Slug: a.Category.Slug,
		}
	}
	if len(a.Tags) > 0 {
		resp.Tags = make([]dto.ArticleTagResponse, len(a.Tags))
		for i, t := range a.Tags {
			resp.Tags[i] = dto.ArticleTagResponse{
				ID:   t.ID.String(),
				Name: t.Name,
				Slug: t.Slug,
			}
		}
	}

	return resp
}

func containsUUID(slice []uuid.UUID, id uuid.UUID) bool {
	for _, v := range slice {
		if v == id {
			return true
		}
	}
	return false
}
