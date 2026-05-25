package main

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/utils"
	"gorm.io/gorm"
)

// ArticleSeed is a simplified struct for defining seed articles.
type ArticleSeed struct {
	Title           string
	Slug            string
	Excerpt         string
	Content         string
	CategorySlug    string
	Status          string
	MetaTitle       string
	MetaDescription string
	ReadingTime     int
	IsFeatured      bool
	TagNames        []string
}

// seedArticleTags creates article tags and returns a map of name->ID.
func seedArticleTags(db *gorm.DB) map[string]uuid.UUID {
	fmt.Println("\n🏷️  Seeding Article Tags...")

	tagDefs := []struct {
		Name        string
		Slug        string
		Description string
	}{
		// Tử vi
		{"Tử vi", "tu-vi", "Bài viết về tử vi, vận mệnh"},
		{"Cung hoàng đạo", "cung-hoang-dao", "12 cung hoàng đạo"},
		{"Con giáp", "con-giap", "12 con giáp"},
		{"Bạch Dương", "bach-duong", "Cung Bạch Dương"},
		{"Kim Ngưu", "kim-nguu", "Cung Kim Ngưu"},
		{"Song Tử", "song-tu", "Cung Song Tử"},
		{"Cự Giải", "cu-giai", "Cung Cự Giải"},
		{"Sư Tử", "su-tu", "Cung Sư Tử"},
		{"Xử Nữ", "xu-nu", "Cung Xử Nữ"},
		{"Thiên Bình", "thien-binh", "Cung Thiên Bình"},
		{"Bọ Cạp", "bo-cap", "Cung Bọ Cạp"},
		{"Nhân Mã", "nhan-ma", "Cung Nhân Mã"},
		{"Ma Kết", "ma-ket", "Cung Ma Kết"},
		{"Bảo Bình", "bao-binh", "Cung Bảo Bình"},
		{"Song Ngư", "song-ngu", "Cung Song Ngư"},
		{"Tử vi 2026", "tu-vi-2026", "Tử vi năm 2026"},
		{"Tình yêu", "tinh-yeu", "Tử vi tình yêu"},
		{"Sự nghiệp", "su-nghiep", "Tử vi sự nghiệp"},
		// Nhân vật lịch sử
		{"Nhân vật lịch sử", "nhan-vat-lich-su", "Nhân vật lịch sử nổi tiếng"},
		{"Triều đại", "trieu-dai", "Các triều đại Việt Nam"},
		{"Anh hùng dân tộc", "anh-hung-dan-toc", "Anh hùng dân tộc Việt Nam"},
		{"Danh nhân văn hóa", "danh-nhan-van-hoa", "Danh nhân văn hóa Việt Nam"},
		{"Lịch sử Việt Nam", "lich-su-viet-nam", "Lịch sử Việt Nam"},
		{"Lịch sử thế giới", "lich-su-the-gioi", "Lịch sử thế giới"},
		// Lễ hội
		{"Lễ hội", "le-hoi-tag", "Các lễ hội truyền thống"},
		{"Tết Nguyên Đán", "tet-nguyen-dan", "Tết cổ truyền Việt Nam"},
		{"Lễ hội dân gian", "le-hoi-dan-gian", "Lễ hội dân gian"},
		{"Lễ hội tôn giáo", "le-hoi-ton-giao", "Lễ hội tôn giáo"},
		{"Phong tục", "phong-tuc", "Phong tục tập quán"},
		// Thần số học
		{"Thần số học", "than-so-hoc", "Thần số học - Numerology"},
		{"Số chủ đạo", "so-chu-dao", "Số chủ đạo đường đời"},
		{"Con số may mắn", "con-so-may-man", "Con số may mắn"},
		{"Numerology", "numerology", "Numerology - Khoa học về con số"},
		// Tướng số
		{"Tướng số", "tuong-so", "Nhân tướng học"},
		{"Nhân tướng học", "nhan-tuong-hoc", "Xem tướng mặt"},
		{"Xem tướng tay", "xem-tuong-tay", "Xem chỉ tay"},
		{"Nốt ruồi", "not-ruoi", "Tướng nốt ruồi"},
		{"Tướng mặt", "tuong-mat", "Xem tướng khuôn mặt"},
		// Phong thủy
		{"Phong thủy", "phong-thuy-tag", "Kiến thức phong thủy"},
		{"Phong thủy nhà ở", "phong-thuy-nha-o", "Phong thủy nhà ở"},
		{"Ngày tốt xấu", "ngay-tot-xau", "Xem ngày tốt xấu"},
		{"Ngũ hành", "ngu-hanh", "Ngũ hành Kim Mộc Thủy Hỏa Thổ"},
		{"Tuổi", "tuoi", "Phong thủy theo tuổi"},
		{"Cây phong thủy", "cay-phong-thuy", "Cây cảnh phong thủy"},
		// Âm lịch
		{"Âm lịch", "am-lich-tag", "Kiến thức âm lịch"},
		{"Tiết khí", "tiet-khi", "24 tiết khí trong năm"},
		{"Ngày rằm", "ngay-ram", "Ngày rằm hàng tháng"},
		{"Lịch vạn niên", "lich-van-nien", "Lịch vạn niên"},
		// Lịch sử
		{"Sự kiện lịch sử", "su-kien-lich-su", "Sự kiện lịch sử nổi bật"},
		{"Chiến tranh", "chien-tranh", "Các cuộc chiến tranh"},
		{"Khởi nghĩa", "khoi-nghia", "Các cuộc khởi nghĩa"},
		// Chung
		{"Văn hóa Việt Nam", "van-hoa-viet-nam", "Văn hóa truyền thống Việt Nam"},
		{"Truyền thống", "truyen-thong", "Giá trị truyền thống"},
		{"Tâm linh", "tam-linh", "Tâm linh, tín ngưỡng"},
	}

	tagMap := make(map[string]uuid.UUID)
	created := 0
	for _, td := range tagDefs {
		var existing models.ArticleTag
		if db.Where("slug = ?", td.Slug).First(&existing).Error == nil {
			tagMap[td.Name] = existing.ID
			continue
		}
		tag := models.ArticleTag{
			Name:        td.Name,
			Slug:        td.Slug,
			Description: td.Description,
		}
		if err := db.Create(&tag).Error; err != nil {
			fmt.Printf("   ❌ Failed to create tag '%s': %v\n", td.Name, err)
			continue
		}
		tagMap[td.Name] = tag.ID
		created++
	}
	fmt.Printf("   ✅ Created %d tags (skipped %d existing)\n", created, len(tagDefs)-created)
	return tagMap
}

// getCategoryID looks up category by slug and returns its UUID.
func getCategoryID(db *gorm.DB, slug string) *uuid.UUID {
	var cat models.ArticleCategory
	if err := db.Where("slug = ?", slug).First(&cat).Error; err != nil {
		return nil
	}
	return &cat.ID
}

// getAdminUserID returns the first admin user ID for article author.
func getAdminUserID(db *gorm.DB) *uuid.UUID {
	var user models.User
	if err := db.Where("email = ?", "admin@zplus.dev").First(&user).Error; err != nil {
		return nil
	}
	return &user.ID
}

// createSeedArticles inserts articles from a list of ArticleSeed, assigning category and tags.
func createSeedArticles(db *gorm.DB, seeds []ArticleSeed, tagMap map[string]uuid.UUID, authorID *uuid.UUID) int {
	created := 0
	now := time.Now()

	for _, s := range seeds {
		slug := s.Slug
		if slug == "" {
			slug = utils.GenerateSlug(s.Title)
		}

		var existing models.Article
		if db.Where("slug = ?", slug).First(&existing).Error == nil {
			continue // already exists
		}

		catID := getCategoryID(db, s.CategorySlug)
		status := s.Status
		if status == "" {
			status = models.ArticleStatusPublished
		}

		article := models.Article{
			Title:           s.Title,
			Slug:            slug,
			Excerpt:         s.Excerpt,
			Content:         s.Content,
			CategoryID:      catID,
			AuthorID:        authorID,
			Status:          status,
			PublishedAt:     &now,
			MetaTitle:       s.MetaTitle,
			MetaDescription: s.MetaDescription,
			ReadingTime:     s.ReadingTime,
			IsFeatured:      s.IsFeatured,
			IsActive:        true,
		}

		if err := db.Create(&article).Error; err != nil {
			fmt.Printf("   ❌ Failed: %s - %v\n", s.Title, err)
			continue
		}

		// Assign tags
		for _, tagName := range s.TagNames {
			if tagID, ok := tagMap[tagName]; ok {
				rel := models.ArticleTagRelation{
					ArticleID: article.ID,
					TagID:     tagID,
				}
				db.Create(&rel)
			}
		}

		created++
	}
	return created
}

// seedAllArticles orchestrates seeding of all article content.
func seedAllArticles(db *gorm.DB) {
	fmt.Println("\n📝 Seeding Articles...")

	// Add new categories if needed
	seedExtraCategories(db)

	// Seed tags
	tagMap := seedArticleTags(db)

	// Get admin author
	authorID := getAdminUserID(db)

	// Seed each topic
	total := 0

	n := seedTuViArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Tử vi: %d bài\n", n)
	total += n

	n = seedNhanVatArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Nhân vật lịch sử: %d bài\n", n)
	total += n

	n = seedLeHoiArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Lễ hội: %d bài\n", n)
	total += n

	n = seedThanSoHocArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Thần số học: %d bài\n", n)
	total += n

	n = seedTuongSoArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Tướng số: %d bài\n", n)
	total += n

	n = seedPhongThuyArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Phong thủy: %d bài\n", n)
	total += n

	n = seedAmLichArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Âm lịch: %d bài\n", n)
	total += n

	n = seedLichSuArticles(db, tagMap, authorID)
	fmt.Printf("   ✅ Lịch sử: %d bài\n", n)
	total += n

	n = seedBatch1Articles(db, tagMap, authorID)
	fmt.Printf("   ✅ Batch 1 (1 bài/chủ đề): %d bài\n", n)
	total += n

	n = seedBatch2Articles(db, tagMap, authorID)
	fmt.Printf("   ✅ Batch 2 (1 bài/chủ đề): %d bài\n", n)
	total += n

	fmt.Printf("\n   📊 Tổng cộng: %d bài viết mới được tạo\n", total)
}

// seedExtraCategories adds categories for Thần số học and Tướng số if not exist.
func seedExtraCategories(db *gorm.DB) {
	extras := []models.ArticleCategory{
		{Name: "Thần số học", Slug: "than-so-hoc", Description: "Thần số học, ý nghĩa các con số, số chủ đạo", Icon: "🔢", SortOrder: 9, IsActive: true},
		{Name: "Tướng số", Slug: "tuong-so", Description: "Nhân tướng học, xem tướng mặt, tay, nốt ruồi", Icon: "🔮", SortOrder: 10, IsActive: true},
	}
	for _, cat := range extras {
		var existing models.ArticleCategory
		if db.Where("slug = ?", cat.Slug).First(&existing).Error == nil {
			continue
		}
		if err := db.Create(&cat).Error; err != nil {
			fmt.Printf("   ❌ Failed to create category '%s': %v\n", cat.Name, err)
		} else {
			fmt.Printf("   ✅ Created category: %s %s\n", cat.Icon, cat.Name)
		}
	}
}
