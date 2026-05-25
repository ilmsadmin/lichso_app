package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ArticleRelationRepository handles article relation database operations.
type ArticleRelationRepository struct {
	db *gorm.DB
}

// NewArticleRelationRepository creates a new ArticleRelationRepository.
func NewArticleRelationRepository(db *gorm.DB) *ArticleRelationRepository {
	return &ArticleRelationRepository{db: db}
}

// Create creates a new article relation.
func (r *ArticleRelationRepository) Create(relation *models.ArticleRelation) error {
	return r.db.Create(relation).Error
}

// GetByID returns a relation by ID.
func (r *ArticleRelationRepository) GetByID(id uuid.UUID) (*models.ArticleRelation, error) {
	var relation models.ArticleRelation
	err := r.db.Preload("TargetArticle").Preload("TargetArticle.Category").
		Where("id = ?", id).First(&relation).Error
	return &relation, err
}

// GetBySourceArticle returns all relations for a source article.
func (r *ArticleRelationRepository) GetBySourceArticle(articleID uuid.UUID, relationType string) ([]models.ArticleRelation, error) {
	var relations []models.ArticleRelation
	query := r.db.Preload("TargetArticle").Preload("TargetArticle.Category").Preload("TargetArticle.Tags").
		Where("source_article_id = ?", articleID)

	if relationType != "" {
		query = query.Where("relation_type = ?", relationType)
	}

	err := query.Order("sort_order ASC, created_at DESC").Find(&relations).Error
	return relations, err
}

// GetAllRelated returns all related articles (both directions for bidirectional relations).
func (r *ArticleRelationRepository) GetAllRelated(articleID uuid.UUID, relationType string) ([]models.Article, error) {
	var articles []models.Article

	subQuery := r.db.Model(&models.ArticleRelation{}).Select("target_article_id").
		Where("source_article_id = ?", articleID)

	bidirectionalSubQuery := r.db.Model(&models.ArticleRelation{}).Select("source_article_id").
		Where("target_article_id = ? AND is_bidirectional = true", articleID)

	if relationType != "" {
		subQuery = subQuery.Where("relation_type = ?", relationType)
		bidirectionalSubQuery = bidirectionalSubQuery.Where("relation_type = ?", relationType)
	}

	err := r.db.Preload("Category").Preload("Tags").
		Where("id IN (?) OR id IN (?)", subQuery, bidirectionalSubQuery).
		Where("status = ? AND is_active = true", models.ArticleStatusPublished).
		Order("created_at DESC").
		Find(&articles).Error

	return articles, err
}

// GetRandomArticles returns random published articles, optionally excluding some IDs.
func (r *ArticleRelationRepository) GetRandomArticles(excludeIDs []uuid.UUID, limit int) ([]models.Article, error) {
	var articles []models.Article
	query := r.db.Preload("Category").Preload("Tags").
		Where("status = ? AND is_active = true", models.ArticleStatusPublished)

	if len(excludeIDs) > 0 {
		query = query.Where("id NOT IN ?", excludeIDs)
	}

	err := query.Order("RANDOM()").Limit(limit).Find(&articles).Error
	return articles, err
}

// GetBySameCategory returns articles in the same category, excluding the source article.
func (r *ArticleRelationRepository) GetBySameCategory(articleID uuid.UUID, categoryID uuid.UUID, limit int) ([]models.Article, error) {
	var articles []models.Article
	err := r.db.Preload("Category").Preload("Tags").
		Where("id != ? AND category_id = ? AND status = ? AND is_active = true",
			articleID, categoryID, models.ArticleStatusPublished).
		Order("published_at DESC").
		Limit(limit).
		Find(&articles).Error
	return articles, err
}

// GetBySameTags returns articles sharing tags with the source article.
func (r *ArticleRelationRepository) GetBySameTags(articleID uuid.UUID, tagIDs []uuid.UUID, limit int) ([]models.Article, error) {
	if len(tagIDs) == 0 {
		return nil, nil
	}

	var articles []models.Article
	err := r.db.Preload("Category").Preload("Tags").
		Where("id != ? AND status = ? AND is_active = true",
			articleID, models.ArticleStatusPublished).
		Where("id IN (?)",
			r.db.Model(&models.ArticleTagRelation{}).Select("article_id").
				Where("tag_id IN ?", tagIDs)).
		Order("view_count DESC").
		Limit(limit).
		Find(&articles).Error
	return articles, err
}

// Delete deletes a relation.
func (r *ArticleRelationRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.ArticleRelation{}).Error
}

// DeleteByArticle deletes all relations for a specific article.
func (r *ArticleRelationRepository) DeleteByArticle(articleID uuid.UUID) error {
	return r.db.Where("source_article_id = ? OR target_article_id = ?", articleID, articleID).
		Delete(&models.ArticleRelation{}).Error
}

// Exists checks if a relation already exists.
func (r *ArticleRelationRepository) Exists(sourceID, targetID uuid.UUID, relationType string) (bool, error) {
	var count int64
	err := r.db.Model(&models.ArticleRelation{}).
		Where("source_article_id = ? AND target_article_id = ? AND relation_type = ?",
			sourceID, targetID, relationType).
		Count(&count).Error
	return count > 0, err
}
