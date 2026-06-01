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

// ArticleService handles article business logic.
type ArticleService struct {
	articleRepo  *repositories.ArticleRepository
	categoryRepo *repositories.ArticleCategoryRepository
	tagRepo      *repositories.ArticleTagRepository
	logger       *zap.Logger
}

// NewArticleService creates a new ArticleService.
func NewArticleService(
	articleRepo *repositories.ArticleRepository,
	categoryRepo *repositories.ArticleCategoryRepository,
	tagRepo *repositories.ArticleTagRepository,
	logger *zap.Logger,
) *ArticleService {
	return &ArticleService{
		articleRepo:  articleRepo,
		categoryRepo: categoryRepo,
		tagRepo:      tagRepo,
		logger:       logger,
	}
}

// Create creates a new article.
func (s *ArticleService) Create(authorID uuid.UUID, req *dto.CreateArticleRequest) (*dto.ArticleResponse, error) {
	slug := req.Slug
	if slug == "" {
		slug = utils.GenerateSlug(req.Title)
	}

	// Ensure slug is unique
	exists, err := s.articleRepo.SlugExists(slug, nil)
	if err != nil {
		s.logger.Error("Failed to check slug", zap.Error(err))
		return nil, fmt.Errorf("failed to check slug: %w", err)
	}
	if exists {
		slug = fmt.Sprintf("%s-%d", slug, time.Now().UnixMilli())
	}

	status := req.Status
	if status == "" {
		status = models.ArticleStatusDraft
	}

	article := &models.Article{
		Title:           req.Title,
		Slug:            slug,
		Excerpt:         req.Excerpt,
		Content:         req.Content,
		FeaturedImage:   req.FeaturedImage,
		AuthorID:        &authorID,
		Status:          status,
		MetaTitle:       req.MetaTitle,
		MetaDescription: req.MetaDescription,
		OGImage:         req.OGImage,
		IsFeatured:      req.IsFeatured,
		ReadingTime:     utils.CalculateReadingTime(req.Content),
	}

	// Set category
	if req.CategoryID != "" {
		catID, err := uuid.Parse(req.CategoryID)
		if err == nil {
			article.CategoryID = &catID
		}
	}

	// Set published_at if status is published
	if status == models.ArticleStatusPublished {
		now := time.Now()
		article.PublishedAt = &now
	}

	if err := s.articleRepo.Create(article); err != nil {
		s.logger.Error("Failed to create article", zap.Error(err))
		return nil, fmt.Errorf("failed to create article: %w", err)
	}

	// Set tags
	if len(req.TagIDs) > 0 {
		tagUUIDs := make([]uuid.UUID, 0, len(req.TagIDs))
		for _, idStr := range req.TagIDs {
			tagID, err := uuid.Parse(idStr)
			if err == nil {
				tagUUIDs = append(tagUUIDs, tagID)
			}
		}
		if len(tagUUIDs) > 0 {
			tags, err := s.tagRepo.GetByIDs(tagUUIDs)
			if err == nil {
				_ = s.articleRepo.UpdateTags(article, tags)
			}
		}
	}

	// Re-fetch with associations
	article, _ = s.articleRepo.GetByID(article.ID)

	return toArticleResponse(article), nil
}

// GetByID returns an article by ID.
func (s *ArticleService) GetByID(id uuid.UUID) (*dto.ArticleResponse, error) {
	article, err := s.articleRepo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("article not found: %w", err)
	}
	return toArticleResponse(article), nil
}

// GetBySlug returns an article by slug (public).
func (s *ArticleService) GetBySlug(slug string) (*dto.ArticleResponse, error) {
	article, err := s.articleRepo.GetBySlug(slug)
	if err != nil {
		return nil, fmt.Errorf("article not found: %w", err)
	}

	// Increment view count
	_ = s.articleRepo.IncrementViewCount(article.ID)

	return toArticleResponse(article), nil
}

// List returns paginated articles (public).
func (s *ArticleService) List(page, pageSize int, search string, categoryIDs []uuid.UUID, isFeatured *bool) ([]dto.ArticleListResponse, int64, error) {
	articles, total, err := s.articleRepo.List(page, pageSize, models.ArticleStatusPublished, search, categoryIDs, isFeatured)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch articles: %w", err)
	}

	result := make([]dto.ArticleListResponse, len(articles))
	for i, a := range articles {
		result[i] = *toArticleListResponse(&a)
	}
	return result, total, nil
}

// ListAll returns all articles (admin).
func (s *ArticleService) ListAll(page, pageSize int, status string, search string, categoryID *uuid.UUID) ([]dto.ArticleResponse, int64, error) {
	articles, total, err := s.articleRepo.ListAll(page, pageSize, status, search, categoryID)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch articles: %w", err)
	}

	result := make([]dto.ArticleResponse, len(articles))
	for i, a := range articles {
		result[i] = *toArticleResponse(&a)
	}
	return result, total, nil
}

// ListByTag returns paginated published articles for a given tag ID.
func (s *ArticleService) ListByTag(tagID uuid.UUID, page, pageSize int) ([]dto.ArticleListResponse, int64, error) {
	articles, total, err := s.articleRepo.ListByTag(tagID, page, pageSize)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch articles by tag: %w", err)
	}
	result := make([]dto.ArticleListResponse, len(articles))
	for i, a := range articles {
		result[i] = *toArticleListResponse(&a)
	}
	return result, total, nil
}

// Search performs full-text search on articles.
func (s *ArticleService) Search(query string, page, pageSize int) ([]dto.ArticleListResponse, int64, error) {
	articles, total, err := s.articleRepo.Search(query, page, pageSize)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to search articles: %w", err)
	}

	result := make([]dto.ArticleListResponse, len(articles))
	for i, a := range articles {
		result[i] = *toArticleListResponse(&a)
	}
	return result, total, nil
}

// GetRandomArticles returns random published articles.
func (s *ArticleService) GetRandomArticles(limit int) ([]dto.ArticleListResponse, error) {
	articles, err := s.articleRepo.GetRandomPublished(limit)
	if err != nil {
		return nil, fmt.Errorf("failed to get random articles: %w", err)
	}

	result := make([]dto.ArticleListResponse, len(articles))
	for i, a := range articles {
		result[i] = *toArticleListResponse(&a)
	}
	return result, nil
}

// Update updates an article.
func (s *ArticleService) Update(id uuid.UUID, req *dto.UpdateArticleRequest) (*dto.ArticleResponse, error) {
	article, err := s.articleRepo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("article not found: %w", err)
	}

	if req.Title != nil {
		article.Title = *req.Title
	}
	if req.Slug != nil {
		exists, err := s.articleRepo.SlugExists(*req.Slug, &id)
		if err == nil && !exists {
			article.Slug = *req.Slug
		}
	}
	if req.Excerpt != nil {
		article.Excerpt = *req.Excerpt
	}
	if req.Content != nil {
		article.Content = *req.Content
		article.ReadingTime = utils.CalculateReadingTime(*req.Content)
	}
	if req.FeaturedImage != nil {
		article.FeaturedImage = *req.FeaturedImage
	}
	if req.CategoryID != nil {
		catID, err := uuid.Parse(*req.CategoryID)
		if err == nil {
			article.CategoryID = &catID
		}
	}
	if req.Status != nil {
		article.Status = *req.Status
		if *req.Status == models.ArticleStatusPublished && article.PublishedAt == nil {
			now := time.Now()
			article.PublishedAt = &now
		}
	}
	if req.MetaTitle != nil {
		article.MetaTitle = *req.MetaTitle
	}
	if req.MetaDescription != nil {
		article.MetaDescription = *req.MetaDescription
	}
	if req.OGImage != nil {
		article.OGImage = *req.OGImage
	}
	if req.IsFeatured != nil {
		article.IsFeatured = *req.IsFeatured
	}
	if req.IsActive != nil {
		article.IsActive = *req.IsActive
	}

	if err := s.articleRepo.Update(article); err != nil {
		s.logger.Error("Failed to update article", zap.Error(err))
		return nil, fmt.Errorf("failed to update article: %w", err)
	}

	// Update tags if provided
	if req.TagIDs != nil {
		tagUUIDs := make([]uuid.UUID, 0, len(req.TagIDs))
		for _, idStr := range req.TagIDs {
			tagID, err := uuid.Parse(idStr)
			if err == nil {
				tagUUIDs = append(tagUUIDs, tagID)
			}
		}
		tags, err := s.tagRepo.GetByIDs(tagUUIDs)
		if err == nil {
			_ = s.articleRepo.UpdateTags(article, tags)
		}
	}

	article, _ = s.articleRepo.GetByID(id)
	return toArticleResponse(article), nil
}

// Delete soft-deletes an article.
func (s *ArticleService) Delete(id uuid.UUID) error {
	if err := s.articleRepo.Delete(id); err != nil {
		s.logger.Error("Failed to delete article", zap.Error(err))
		return fmt.Errorf("failed to delete article: %w", err)
	}
	return nil
}

// ResolveCategoryID resolves either a category UUID or slug into an ID.
func (s *ArticleService) ResolveCategoryID(value string) (*uuid.UUID, error) {
	if value == "" {
		return nil, nil
	}
	if id, err := uuid.Parse(value); err == nil {
		return &id, nil
	}
	category, err := s.categoryRepo.GetBySlug(value)
	if err != nil {
		return nil, fmt.Errorf("category not found: %w", err)
	}
	return &category.ID, nil
}

// ResolveCategoryTreeIDs resolves a category UUID or slug and includes its active descendants.
func (s *ArticleService) ResolveCategoryTreeIDs(value string) ([]uuid.UUID, error) {
	categoryID, err := s.ResolveCategoryID(value)
	if err != nil || categoryID == nil {
		return nil, err
	}

	ids := []uuid.UUID{*categoryID}
	descendantIDs, err := s.categoryRepo.GetDescendantIDs(*categoryID)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch child categories: %w", err)
	}
	ids = append(ids, descendantIDs...)
	return ids, nil
}

// ============================================
// Helper functions
// ============================================

func toArticleResponse(a *models.Article) *dto.ArticleResponse {
	resp := &dto.ArticleResponse{
		ID:              a.ID.String(),
		Title:           a.Title,
		Slug:            a.Slug,
		Excerpt:         a.Excerpt,
		Content:         utils.NormalizeContentURLs(a.Content),
		FeaturedImage:   utils.NormalizeUploadURL(a.FeaturedImage),
		Status:          a.Status,
		MetaTitle:       a.MetaTitle,
		MetaDescription: a.MetaDescription,
		OGImage:         utils.NormalizeUploadURL(a.OGImage),
		ViewCount:       a.ViewCount,
		ReadingTime:     a.ReadingTime,
		IsFeatured:      a.IsFeatured,
		IsActive:        a.IsActive,
		CreatedAt:       a.CreatedAt.Format(time.RFC3339),
		UpdatedAt:       a.UpdatedAt.Format(time.RFC3339),
	}

	if a.CategoryID != nil {
		resp.CategoryID = a.CategoryID.String()
	}
	if a.AuthorID != nil {
		resp.AuthorID = a.AuthorID.String()
	}
	if a.PublishedAt != nil {
		resp.PublishedAt = a.PublishedAt.Format(time.RFC3339)
	}
	if a.Category != nil {
		resp.Category = toArticleCategoryResponse(a.Category)
	}
	if len(a.Tags) > 0 {
		resp.Tags = make([]dto.ArticleTagResponse, len(a.Tags))
		for i, t := range a.Tags {
			resp.Tags[i] = *toArticleTagResponseFromModel(&t)
		}
	}

	return resp
}

func toArticleListResponse(a *models.Article) *dto.ArticleListResponse {
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
		resp.Category = toArticleCategoryResponse(a.Category)
	}
	if len(a.Tags) > 0 {
		resp.Tags = make([]dto.ArticleTagResponse, len(a.Tags))
		for i, t := range a.Tags {
			resp.Tags[i] = *toArticleTagResponseFromModel(&t)
		}
	}

	return resp
}

func toArticleCategoryResponse(c *models.ArticleCategory) *dto.ArticleCategoryResponse {
	resp := &dto.ArticleCategoryResponse{
		ID:          c.ID.String(),
		Name:        c.Name,
		Slug:        c.Slug,
		Description: c.Description,
		Icon:        c.Icon,
		ImageURL:    utils.NormalizeUploadURL(c.ImageURL),
		SortOrder:   c.SortOrder,
		IsActive:    c.IsActive,
		CreatedAt:   c.CreatedAt.Format(time.RFC3339),
	}
	if c.ParentID != nil {
		resp.ParentID = c.ParentID.String()
	}
	if len(c.Children) > 0 {
		resp.Children = make([]dto.ArticleCategoryResponse, len(c.Children))
		for i, child := range c.Children {
			resp.Children[i] = *toArticleCategoryResponse(&child)
		}
	}
	return resp
}

func toArticleTagResponseFromModel(t *models.ArticleTag) *dto.ArticleTagResponse {
	return &dto.ArticleTagResponse{
		ID:           t.ID.String(),
		Name:         t.Name,
		Slug:         t.Slug,
		Description:  t.Description,
		ArticleCount: t.ArticleCount,
		CreatedAt:    t.CreatedAt.Format(time.RFC3339),
	}
}

// ============================================
// Article Category Service
// ============================================

// ArticleCategoryService handles article category business logic.
type ArticleCategoryService struct {
	repo   *repositories.ArticleCategoryRepository
	logger *zap.Logger
}

// NewArticleCategoryService creates a new ArticleCategoryService.
func NewArticleCategoryService(repo *repositories.ArticleCategoryRepository, logger *zap.Logger) *ArticleCategoryService {
	return &ArticleCategoryService{repo: repo, logger: logger}
}

// Create creates a new article category.
func (s *ArticleCategoryService) Create(req *dto.CreateArticleCategoryRequest) (*dto.ArticleCategoryResponse, error) {
	slug := req.Slug
	if slug == "" {
		slug = utils.GenerateSlug(req.Name)
	}

	exists, err := s.repo.SlugExists(slug, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to check slug: %w", err)
	}
	if exists {
		return nil, fmt.Errorf("slug '%s' already exists", slug)
	}

	category := &models.ArticleCategory{
		Name:        req.Name,
		Slug:        slug,
		Description: req.Description,
		Icon:        req.Icon,
		ImageURL:    req.ImageURL,
		SortOrder:   req.SortOrder,
	}

	if req.ParentID != "" {
		parentID, err := uuid.Parse(req.ParentID)
		if err == nil {
			category.ParentID = &parentID
		}
	}

	if err := s.repo.Create(category); err != nil {
		s.logger.Error("Failed to create category", zap.Error(err))
		return nil, fmt.Errorf("failed to create category: %w", err)
	}

	return toArticleCategoryResponse(category), nil
}

// GetByID returns a category by ID.
func (s *ArticleCategoryService) GetByID(id uuid.UUID) (*dto.ArticleCategoryResponse, error) {
	category, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("category not found: %w", err)
	}
	return toArticleCategoryResponse(category), nil
}

// GetBySlug returns a category by slug.
func (s *ArticleCategoryService) GetBySlug(slug string) (*dto.ArticleCategoryResponse, error) {
	category, err := s.repo.GetBySlug(slug)
	if err != nil {
		return nil, fmt.Errorf("category not found: %w", err)
	}
	return toArticleCategoryResponse(category), nil
}

// List returns all active categories.
func (s *ArticleCategoryService) List() ([]dto.ArticleCategoryResponse, error) {
	categories, err := s.repo.List()
	if err != nil {
		return nil, fmt.Errorf("failed to fetch categories: %w", err)
	}

	result := make([]dto.ArticleCategoryResponse, len(categories))
	for i, c := range categories {
		result[i] = *toArticleCategoryResponse(&c)
	}
	return result, nil
}

// ListAll returns all categories (admin) with pagination and search.
func (s *ArticleCategoryService) ListAll(page, pageSize int, search string) ([]dto.ArticleCategoryResponse, int64, error) {
	categories, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch categories: %w", err)
	}

	result := make([]dto.ArticleCategoryResponse, len(categories))
	for i, c := range categories {
		result[i] = *toArticleCategoryResponse(&c)
	}
	return result, total, nil
}

// Update updates a category.
func (s *ArticleCategoryService) Update(id uuid.UUID, req *dto.UpdateArticleCategoryRequest) (*dto.ArticleCategoryResponse, error) {
	category, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("category not found: %w", err)
	}

	if req.Name != nil {
		category.Name = *req.Name
	}
	if req.Slug != nil {
		exists, err := s.repo.SlugExists(*req.Slug, &id)
		if err == nil && !exists {
			category.Slug = *req.Slug
		}
	}
	if req.Description != nil {
		category.Description = *req.Description
	}
	if req.ParentID != nil {
		parentID, err := uuid.Parse(*req.ParentID)
		if err == nil {
			category.ParentID = &parentID
		}
	}
	if req.Icon != nil {
		category.Icon = *req.Icon
	}
	if req.ImageURL != nil {
		category.ImageURL = *req.ImageURL
	}
	if req.SortOrder != nil {
		category.SortOrder = *req.SortOrder
	}
	if req.IsActive != nil {
		category.IsActive = *req.IsActive
	}

	if err := s.repo.Update(category); err != nil {
		s.logger.Error("Failed to update category", zap.Error(err))
		return nil, fmt.Errorf("failed to update category: %w", err)
	}

	return toArticleCategoryResponse(category), nil
}

// Delete soft-deletes a category.
func (s *ArticleCategoryService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete category", zap.Error(err))
		return fmt.Errorf("failed to delete category: %w", err)
	}
	return nil
}

// ============================================
// Article Tag Service
// ============================================

// ArticleTagService handles article tag business logic.
type ArticleTagService struct {
	repo   *repositories.ArticleTagRepository
	logger *zap.Logger
}

// NewArticleTagService creates a new ArticleTagService.
func NewArticleTagService(repo *repositories.ArticleTagRepository, logger *zap.Logger) *ArticleTagService {
	return &ArticleTagService{repo: repo, logger: logger}
}

// Create creates a new article tag.
func (s *ArticleTagService) Create(req *dto.CreateArticleTagRequest) (*dto.ArticleTagResponse, error) {
	slug := req.Slug
	if slug == "" {
		slug = utils.GenerateSlug(req.Name)
	}

	exists, err := s.repo.SlugExists(slug, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to check slug: %w", err)
	}
	if exists {
		return nil, fmt.Errorf("slug '%s' already exists", slug)
	}

	tag := &models.ArticleTag{
		Name:        req.Name,
		Slug:        slug,
		Description: req.Description,
	}

	if err := s.repo.Create(tag); err != nil {
		s.logger.Error("Failed to create tag", zap.Error(err))
		return nil, fmt.Errorf("failed to create tag: %w", err)
	}

	return toArticleTagResponseFromModel(tag), nil
}

// GetByID returns a tag by ID.
func (s *ArticleTagService) GetByID(id uuid.UUID) (*dto.ArticleTagResponse, error) {
	tag, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("tag not found: %w", err)
	}
	return toArticleTagResponseFromModel(tag), nil
}

// GetBySlug returns a tag by slug.
func (s *ArticleTagService) GetBySlug(slug string) (*dto.ArticleTagResponse, error) {
	tag, err := s.repo.GetBySlug(slug)
	if err != nil {
		return nil, fmt.Errorf("tag not found: %w", err)
	}
	return toArticleTagResponseFromModel(tag), nil
}

// List returns all tags.
func (s *ArticleTagService) List() ([]dto.ArticleTagResponse, error) {
	tags, err := s.repo.List()
	if err != nil {
		return nil, fmt.Errorf("failed to fetch tags: %w", err)
	}

	result := make([]dto.ArticleTagResponse, len(tags))
	for i, t := range tags {
		result[i] = *toArticleTagResponseFromModel(&t)
	}
	return result, nil
}

// ListAll returns all tags (admin) with pagination and search.
func (s *ArticleTagService) ListAll(page, pageSize int, search string) ([]dto.ArticleTagResponse, int64, error) {
	tags, total, err := s.repo.ListAll(page, pageSize, search)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to fetch tags: %w", err)
	}

	result := make([]dto.ArticleTagResponse, len(tags))
	for i, t := range tags {
		result[i] = *toArticleTagResponseFromModel(&t)
	}
	return result, total, nil
}

// Update updates a tag.
func (s *ArticleTagService) Update(id uuid.UUID, req *dto.UpdateArticleTagRequest) (*dto.ArticleTagResponse, error) {
	tag, err := s.repo.GetByID(id)
	if err != nil {
		return nil, fmt.Errorf("tag not found: %w", err)
	}

	if req.Name != nil {
		tag.Name = *req.Name
	}
	if req.Slug != nil {
		exists, err := s.repo.SlugExists(*req.Slug, &id)
		if err == nil && !exists {
			tag.Slug = *req.Slug
		}
	}
	if req.Description != nil {
		tag.Description = *req.Description
	}

	if err := s.repo.Update(tag); err != nil {
		s.logger.Error("Failed to update tag", zap.Error(err))
		return nil, fmt.Errorf("failed to update tag: %w", err)
	}

	return toArticleTagResponseFromModel(tag), nil
}

// Delete soft-deletes a tag.
func (s *ArticleTagService) Delete(id uuid.UUID) error {
	if err := s.repo.Delete(id); err != nil {
		s.logger.Error("Failed to delete tag", zap.Error(err))
		return fmt.Errorf("failed to delete tag: %w", err)
	}
	return nil
}
