package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestPaginationQuery_Offset(t *testing.T) {
	tests := []struct {
		name       string
		page       int
		limit      int
		wantOffset int
	}{
		{"page 1, limit 10", 1, 10, 0},
		{"page 2, limit 10", 2, 10, 10},
		{"page 3, limit 20", 3, 20, 40},
		{"page 1, limit 50", 1, 50, 0},
		{"page 5, limit 10", 5, 10, 40},
		{"page 10, limit 100", 10, 100, 900},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			pq := PaginationQuery{
				Page:  tt.page,
				Limit: tt.limit,
			}
			assert.Equal(t, tt.wantOffset, pq.Offset())
		})
	}
}

func TestPaginationQuery_Defaults(t *testing.T) {
	assert.Equal(t, 1, DefaultPage)
	assert.Equal(t, 20, DefaultLimit)
	assert.Equal(t, 100, MaxLimit)
}
