package repositories

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

// ArticleRepository handles article database operations.
type ArticleRepository struct {
	db *gorm.DB
}

// NewArticleRepository creates a new ArticleRepository.
func NewArticleRepository(db *gorm.DB) *ArticleRepository {
	return &ArticleRepository{db: db}
}

// Create creates a new article.
func (r *ArticleRepository) Create(article *models.Article) error {
	return r.db.Create(article).Error
}

// GetByID returns an article by ID with preloaded associations.
func (r *ArticleRepository) GetByID(id uuid.UUID) (*models.Article, error) {
	var article models.Article
	err := r.db.Preload("Category").Preload("Author").Preload("Tags").
		Where("id = ?", id).First(&article).Error
	return &article, err
}

// GetBySlug returns an article by slug with preloaded associations.
func (r *ArticleRepository) GetBySlug(slug string) (*models.Article, error) {
	var article models.Article
	err := r.db.Preload("Category").Preload("Author").Preload("Tags").
		Where("slug = ? AND is_active = ?", slug, true).First(&article).Error
	return &article, err
}

// List returns paginated articles with optional filters.
func (r *ArticleRepository) List(page, pageSize int, status string, search string, categoryIDs []uuid.UUID, isFeatured *bool) ([]models.Article, int64, error) {
	var articles []models.Article
	var total int64

	query := r.db.Model(&models.Article{}).Where("is_active = ?", true)

	if status != "" {
		query = query.Where("status = ?", status)
	}
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR excerpt ILIKE ?)", searchPattern, searchPattern)
	}
	if len(categoryIDs) > 0 {
		query = query.Where("category_id IN ?", categoryIDs)
	}
	if isFeatured != nil {
		query = query.Where("is_featured = ?", *isFeatured)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Preload("Category").Preload("Tags").
		Order("published_at DESC NULLS LAST, created_at DESC").
		Offset(offset).Limit(pageSize).Find(&articles).Error

	return articles, total, err
}

// ListAll returns all articles (for admin) with pagination.
func (r *ArticleRepository) ListAll(page, pageSize int, status string, search string, categoryID *uuid.UUID) ([]models.Article, int64, error) {
	var articles []models.Article
	var total int64

	query := r.db.Model(&models.Article{})
	if status != "" {
		query = query.Where("status = ?", status)
	}
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR excerpt ILIKE ?)", searchPattern, searchPattern)
	}
	if categoryID != nil {
		query = query.Where("category_id = ?", *categoryID)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Preload("Category").Preload("Author").Preload("Tags").
		Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&articles).Error

	return articles, total, err
}

// Update updates an article.
func (r *ArticleRepository) Update(article *models.Article) error {
	return r.db.Save(article).Error
}

// Delete soft-deletes an article.
func (r *ArticleRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.Article{}).Error
}

// IncrementViewCount increments the view count of an article.
func (r *ArticleRepository) IncrementViewCount(id uuid.UUID) error {
	return r.db.Model(&models.Article{}).Where("id = ?", id).
		UpdateColumn("view_count", gorm.Expr("view_count + 1")).Error
}

// UpdateTags replaces the tags for an article.
func (r *ArticleRepository) UpdateTags(article *models.Article, tags []models.ArticleTag) error {
	if err := r.db.Model(article).Association("Tags").Replace(tags); err != nil {
		return err
	}
	return nil
}

// Search performs a full-text search on articles.
func (r *ArticleRepository) Search(query string, page, pageSize int) ([]models.Article, int64, error) {
	var articles []models.Article
	var total int64

	dbQuery := r.db.Model(&models.Article{}).
		Where("is_active = ? AND status = ?", true, "published").
		Where("to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(excerpt, '') || ' ' || coalesce(content, '')) @@ plainto_tsquery('simple', ?)", query)

	if err := dbQuery.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := dbQuery.Preload("Category").Preload("Tags").
		Order("published_at DESC").
		Offset(offset).Limit(pageSize).Find(&articles).Error

	return articles, total, err
}

// SlugExists checks if a slug already exists.
func (r *ArticleRepository) SlugExists(slug string, excludeID *uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.Article{}).Where("slug = ?", slug)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Count(&count).Error
	return count > 0, err
}

// ListByTag returns paginated published articles that have the given tag.
func (r *ArticleRepository) ListByTag(tagID uuid.UUID, page, pageSize int) ([]models.Article, int64, error) {
	var articles []models.Article
	var total int64

	subQuery := r.db.Table("article_tag_relations").
		Select("article_id").
		Where("tag_id = ?", tagID)

	query := r.db.Model(&models.Article{}).
		Where("is_active = ? AND status = ?", true, "published").
		Where("id IN (?)", subQuery)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Preload("Category").Preload("Tags").
		Order("published_at DESC NULLS LAST, created_at DESC").
		Offset(offset).Limit(pageSize).Find(&articles).Error

	return articles, total, err
}

// GetRandomPublished returns random published articles.
func (r *ArticleRepository) GetRandomPublished(limit int) ([]models.Article, error) {
	var articles []models.Article
	err := r.db.Preload("Category").Preload("Tags").
		Where("is_active = ? AND status = ?", true, "published").
		Order("RANDOM()").
		Limit(limit).
		Find(&articles).Error
	return articles, err
}

// ListLowQuality returns all articles (admin scope) whose content word count is below the threshold.
// These articles are considered "chưa chất lượng" and eligible for AI rewriting.
// Optionally filter by status; when status is empty, all statuses are included.
func (r *ArticleRepository) ListLowQuality(maxWords int, page, pageSize int, status string, search string, categoryID *uuid.UUID) ([]models.Article, int64, error) {
	var articles []models.Article
	var total int64

	// Use PostgreSQL's array_length(regexp_split_to_array(...)) to count words server-side
	query := r.db.Model(&models.Article{}).
		Where("array_length(regexp_split_to_array(COALESCE(content, ''), '\\s+'), 1) < ?", maxWords)

	if status != "" {
		query = query.Where("status = ?", status)
	}
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(title ILIKE ? OR excerpt ILIKE ?)", searchPattern, searchPattern)
	}
	if categoryID != nil {
		query = query.Where("category_id = ?", *categoryID)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Preload("Category").Preload("Author").Preload("Tags").
		Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&articles).Error

	return articles, total, err
}

// CountLowQuality returns the count of articles with fewer than maxWords words.
// When status is empty, all statuses are counted.
func (r *ArticleRepository) CountLowQuality(maxWords int, status string) (int64, error) {
	var count int64
	query := r.db.Model(&models.Article{}).
		Where("array_length(regexp_split_to_array(COALESCE(content, ''), '\\s+'), 1) < ?", maxWords)
	if status != "" {
		query = query.Where("status = ?", status)
	}
	err := query.Count(&count).Error
	return count, err
}

// ============================================
// Article Category Repository
// ============================================

// ArticleCategoryRepository handles article category database operations.
type ArticleCategoryRepository struct {
	db *gorm.DB
}

// NewArticleCategoryRepository creates a new ArticleCategoryRepository.
func NewArticleCategoryRepository(db *gorm.DB) *ArticleCategoryRepository {
	return &ArticleCategoryRepository{db: db}
}

// Create creates a new article category.
func (r *ArticleCategoryRepository) Create(category *models.ArticleCategory) error {
	return r.db.Create(category).Error
}

// GetByID returns a category by ID.
func (r *ArticleCategoryRepository) GetByID(id uuid.UUID) (*models.ArticleCategory, error) {
	var category models.ArticleCategory
	err := r.db.Where("id = ?", id).First(&category).Error
	return &category, err
}

// GetBySlug returns a category by slug.
func (r *ArticleCategoryRepository) GetBySlug(slug string) (*models.ArticleCategory, error) {
	var category models.ArticleCategory
	err := r.db.Where("slug = ? AND is_active = ?", slug, true).First(&category).Error
	return &category, err
}

// GetDescendantIDs returns active child category IDs for a category tree.
func (r *ArticleCategoryRepository) GetDescendantIDs(parentID uuid.UUID) ([]uuid.UUID, error) {
	var ids []uuid.UUID
	err := r.db.Raw(`
		WITH RECURSIVE category_tree AS (
			SELECT id FROM article_categories
			WHERE parent_id = ? AND is_active = TRUE AND deleted_at IS NULL
			UNION ALL
			SELECT c.id FROM article_categories c
			INNER JOIN category_tree ct ON c.parent_id = ct.id
			WHERE c.is_active = TRUE AND c.deleted_at IS NULL
		)
		SELECT id FROM category_tree
	`, parentID).Scan(&ids).Error
	return ids, err
}

// List returns all active categories as a tree structure.
func (r *ArticleCategoryRepository) List() ([]models.ArticleCategory, error) {
	var categories []models.ArticleCategory
	err := r.db.Where("is_active = ? AND parent_id IS NULL", true).
		Preload("Children").
		Order("sort_order ASC, name ASC").Find(&categories).Error
	return categories, err
}

// ListAll returns all categories (for admin) with pagination and search.
func (r *ArticleCategoryRepository) ListAll(page, pageSize int, search string) ([]models.ArticleCategory, int64, error) {
	var categories []models.ArticleCategory
	var total int64

	query := r.db.Model(&models.ArticleCategory{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(name ILIKE ? OR slug ILIKE ? OR description ILIKE ?)", searchPattern, searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Preload("Children").
		Order("sort_order ASC, name ASC").
		Offset(offset).Limit(pageSize).Find(&categories).Error

	return categories, total, err
}

// Update updates a category.
func (r *ArticleCategoryRepository) Update(category *models.ArticleCategory) error {
	return r.db.Save(category).Error
}

// Delete soft-deletes a category.
func (r *ArticleCategoryRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.ArticleCategory{}).Error
}

// SlugExists checks if a category slug already exists.
func (r *ArticleCategoryRepository) SlugExists(slug string, excludeID *uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.ArticleCategory{}).Where("slug = ?", slug)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Count(&count).Error
	return count > 0, err
}

// ============================================
// Article Tag Repository
// ============================================

// ArticleTagRepository handles article tag database operations.
type ArticleTagRepository struct {
	db *gorm.DB
}

// NewArticleTagRepository creates a new ArticleTagRepository.
func NewArticleTagRepository(db *gorm.DB) *ArticleTagRepository {
	return &ArticleTagRepository{db: db}
}

// Create creates a new article tag.
func (r *ArticleTagRepository) Create(tag *models.ArticleTag) error {
	return r.db.Create(tag).Error
}

// GetByID returns a tag by ID.
func (r *ArticleTagRepository) GetByID(id uuid.UUID) (*models.ArticleTag, error) {
	var tag models.ArticleTag
	err := r.db.Where("id = ?", id).First(&tag).Error
	return &tag, err
}

// GetByIDs returns tags by a list of IDs.
func (r *ArticleTagRepository) GetByIDs(ids []uuid.UUID) ([]models.ArticleTag, error) {
	var tags []models.ArticleTag
	err := r.db.Where("id IN ?", ids).Find(&tags).Error
	return tags, err
}

// GetBySlug returns a tag by slug.
func (r *ArticleTagRepository) GetBySlug(slug string) (*models.ArticleTag, error) {
	var tag models.ArticleTag
	err := r.db.Where("slug = ?", slug).First(&tag).Error
	return &tag, err
}

// List returns all tags ordered by article count.
func (r *ArticleTagRepository) List() ([]models.ArticleTag, error) {
	var tags []models.ArticleTag
	err := r.db.Order("article_count DESC, name ASC").Find(&tags).Error
	return tags, err
}

// ListAll returns all tags (for admin) with pagination and search.
func (r *ArticleTagRepository) ListAll(page, pageSize int, search string) ([]models.ArticleTag, int64, error) {
	var tags []models.ArticleTag
	var total int64

	query := r.db.Model(&models.ArticleTag{})
	if search != "" {
		searchPattern := "%" + search + "%"
		query = query.Where("(name ILIKE ? OR slug ILIKE ?)", searchPattern, searchPattern)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	err := query.Order("created_at DESC").
		Offset(offset).Limit(pageSize).Find(&tags).Error

	return tags, total, err
}

// Update updates a tag.
func (r *ArticleTagRepository) Update(tag *models.ArticleTag) error {
	return r.db.Save(tag).Error
}

// Delete soft-deletes a tag.
func (r *ArticleTagRepository) Delete(id uuid.UUID) error {
	return r.db.Where("id = ?", id).Delete(&models.ArticleTag{}).Error
}

// SlugExists checks if a tag slug already exists.
func (r *ArticleTagRepository) SlugExists(slug string, excludeID *uuid.UUID) (bool, error) {
	var count int64
	query := r.db.Model(&models.ArticleTag{}).Where("slug = ?", slug)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Count(&count).Error
	return count > 0, err
}
