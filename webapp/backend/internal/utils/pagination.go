package utils

import (
	"strconv"

	"github.com/gofiber/fiber/v2"
)

// PaginationQuery represents parsed pagination parameters from query string
type PaginationQuery struct {
	Page      int    `json:"page"`
	Limit     int    `json:"limit"`
	Search    string `json:"search"`
	SortBy    string `json:"sort_by"`
	SortOrder string `json:"sort_order"`
	Status    string `json:"status"`     // "active", "inactive", "all"
	Role      string `json:"role"`       // filter by role name
	HasDevice string `json:"has_device"` // "yes", "no", "" = all
}

// DefaultPage is the default page number
const DefaultPage = 1

// DefaultLimit is the default number of items per page
const DefaultLimit = 20

// MaxLimit is the maximum allowed items per page
const MaxLimit = 100

// ParsePagination extracts and validates pagination parameters from the request
func ParsePagination(c *fiber.Ctx) PaginationQuery {
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))

	if page < 1 {
		page = DefaultPage
	}
	if limit < 1 {
		limit = DefaultLimit
	}
	if limit > MaxLimit {
		limit = MaxLimit
	}

	sortBy := c.Query("sort_by", "created_at")
	sortOrder := c.Query("sort_order", "desc")
	if sortOrder != "asc" && sortOrder != "desc" {
		sortOrder = "desc"
	}

	// Validate sort_by to prevent SQL injection
	allowedSortFields := map[string]bool{
		"created_at": true,
		"updated_at": true,
		"email":      true,
		"first_name": true,
		"last_name":  true,
		"is_active":  true,
	}
	if !allowedSortFields[sortBy] {
		sortBy = "created_at"
	}

	return PaginationQuery{
		Page:      page,
		Limit:     limit,
		Search:    c.Query("search", ""),
		SortBy:    sortBy,
		SortOrder: sortOrder,
		Status:    c.Query("status", "all"),
		Role:      c.Query("role", ""),
		HasDevice: c.Query("has_device", ""),
	}
}

// Offset calculates the SQL offset from page and limit
func (p *PaginationQuery) Offset() int {
	return (p.Page - 1) * p.Limit
}
