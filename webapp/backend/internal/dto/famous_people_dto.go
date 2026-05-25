package dto

// ============================================
// Famous People DTOs
// ============================================

// CreateFamousPersonRequest represents a request to create a famous person.
type CreateFamousPersonRequest struct {
	Name         string   `json:"name" validate:"required,max=255"`
	OriginalName string   `json:"original_name" validate:"omitempty,max=255"`
	BirthDate    string   `json:"birth_date"` // YYYY-MM-DD
	BirthDay     *int     `json:"birth_day" validate:"omitempty,min=1,max=31"`
	BirthMonth   *int     `json:"birth_month" validate:"omitempty,min=1,max=12"`
	BirthYear    *int     `json:"birth_year"`
	DeathDate    string   `json:"death_date"` // YYYY-MM-DD
	Nationality  string   `json:"nationality" validate:"omitempty,max=100"`
	Occupation   string   `json:"occupation" validate:"omitempty,max=500"`
	Category     string   `json:"category" validate:"required,oneof=chinh_tri khoa_hoc nghe_thuat am_nhac the_thao van_hoc lich_su dien_anh kinh_doanh khac"`
	ShortBio     string   `json:"short_bio"`
	ImageURL     string   `json:"image_url" validate:"omitempty,max=500"`
	ArticleID    string   `json:"article_id" validate:"omitempty,uuid"`
	IsVietnamese bool     `json:"is_vietnamese"`
	Tags         []string `json:"tags"`
}

// UpdateFamousPersonRequest represents a request to update a famous person.
type UpdateFamousPersonRequest struct {
	Name         *string  `json:"name" validate:"omitempty,max=255"`
	OriginalName *string  `json:"original_name" validate:"omitempty,max=255"`
	BirthDate    *string  `json:"birth_date"`
	BirthDay     *int     `json:"birth_day" validate:"omitempty,min=1,max=31"`
	BirthMonth   *int     `json:"birth_month" validate:"omitempty,min=1,max=12"`
	BirthYear    *int     `json:"birth_year"`
	DeathDate    *string  `json:"death_date"`
	Nationality  *string  `json:"nationality" validate:"omitempty,max=100"`
	Occupation   *string  `json:"occupation" validate:"omitempty,max=500"`
	Category     *string  `json:"category" validate:"omitempty,oneof=chinh_tri khoa_hoc nghe_thuat am_nhac the_thao van_hoc lich_su dien_anh kinh_doanh khac"`
	ShortBio     *string  `json:"short_bio"`
	ImageURL     *string  `json:"image_url" validate:"omitempty,max=500"`
	ArticleID    *string  `json:"article_id" validate:"omitempty,uuid"`
	IsVietnamese *bool    `json:"is_vietnamese"`
	Tags         []string `json:"tags"`
	IsActive     *bool    `json:"is_active"`
}

// FamousPersonResponse represents a famous person in API responses.
type FamousPersonResponse struct {
	ID           string   `json:"id"`
	Name         string   `json:"name"`
	OriginalName string   `json:"original_name,omitempty"`
	BirthDate    string   `json:"birth_date,omitempty"`
	BirthDay     *int     `json:"birth_day,omitempty"`
	BirthMonth   *int     `json:"birth_month,omitempty"`
	BirthYear    *int     `json:"birth_year,omitempty"`
	DeathDate    string   `json:"death_date,omitempty"`
	Nationality  string   `json:"nationality,omitempty"`
	Occupation   string   `json:"occupation,omitempty"`
	Category     string   `json:"category"`
	ShortBio     string   `json:"short_bio,omitempty"`
	ImageURL     string   `json:"image_url,omitempty"`
	ArticleID    string   `json:"article_id,omitempty"`
	IsVietnamese bool     `json:"is_vietnamese"`
	Tags         []string `json:"tags,omitempty"`
	IsActive     bool     `json:"is_active"`
	CreatedAt    string   `json:"created_at"`
}
