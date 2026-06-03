package main

import (
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/models"
	"gorm.io/gorm"
)

func seedAppReviews(db *gorm.DB) {
	fmt.Println("\n⭐ Seeding App Reviews...")

	type reviewSeed struct {
		UserEmail  string
		Platform   string
		AppVersion string
		DeviceID   string
		DeviceName string
		OSVersion  string
		Stars      int
		ReviewText string
		ReviewFlow string
		Status     string
		AdminNote  string
		CreatedAt  time.Time
	}

	now := time.Now()
	seeds := []reviewSeed{
		{UserEmail: "admin@zplus.dev", Platform: models.PlatformIOS, AppVersion: "2.3.1", DeviceID: "ios-admin-001", DeviceName: "iPhone 15 Pro", OSVersion: "18.1", Stars: 5, ReviewText: "Giao diện đẹp, widget rất hữu ích mỗi sáng.", ReviewFlow: models.AppReviewFlowHighRatingPrompt, Status: models.AppReviewStatusReviewed, AdminNote: "Đã ghi nhận phản hồi tích cực về widget.", CreatedAt: now.Add(-2 * time.Hour)},
		{UserEmail: "editor@zplus.dev", Platform: models.PlatformAndroid, AppVersion: "2.3.1", DeviceID: "android-editor-001", DeviceName: "Samsung Galaxy S24", OSVersion: "14", Stars: 2, ReviewText: "Thông báo nhắc việc đôi lúc đến muộn khoảng vài phút.", ReviewFlow: models.AppReviewFlowLowRatingFeedback, Status: models.AppReviewStatusNew, CreatedAt: now.Add(-4 * time.Hour)},
		{UserEmail: "viewer@zplus.dev", Platform: models.PlatformAndroid, AppVersion: "2.3.0", DeviceID: "android-viewer-001", DeviceName: "Xiaomi 14", OSVersion: "14", Stars: 4, ReviewText: "Tra cứu lịch âm nhanh, mong có thêm chủ đề màu sáng hơn.", ReviewFlow: models.AppReviewFlowHighRatingPrompt, Status: models.AppReviewStatusReviewed, CreatedAt: now.Add(-8 * time.Hour)},
		{Platform: models.PlatformIOS, AppVersion: "2.3.1", DeviceID: "guest-ios-001", DeviceName: "iPhone 13", OSVersion: "17.6", Stars: 1, ReviewText: "App bị lag lúc mở màn xem ngày tốt.", ReviewFlow: models.AppReviewFlowLowRatingFeedback, Status: models.AppReviewStatusNew, CreatedAt: now.Add(-12 * time.Hour)},
		{Platform: models.PlatformAndroid, AppVersion: "2.3.1", DeviceID: "guest-android-002", DeviceName: "OPPO Reno11", OSVersion: "14", Stars: 3, ReviewText: "Nội dung hay nhưng font hơi nhỏ ở màn hình bài viết.", ReviewFlow: models.AppReviewFlowLowRatingFeedback, Status: models.AppReviewStatusReviewed, AdminNote: "Chuyển team mobile kiểm tra typography bài viết.", CreatedAt: now.Add(-18 * time.Hour)},
		{Platform: models.PlatformIOS, AppVersion: "2.2.9", DeviceID: "guest-ios-003", DeviceName: "iPad Air", OSVersion: "17.5", Stars: 5, ReviewText: "Dùng trên iPad rất ổn, phần gia phả tiện cho gia đình.", ReviewFlow: models.AppReviewFlowHighRatingPrompt, Status: models.AppReviewStatusResolved, AdminNote: "Phản hồi tốt, không cần xử lý.", CreatedAt: now.Add(-26 * time.Hour)},
		{Platform: models.PlatformAndroid, AppVersion: "2.3.1", DeviceID: "guest-android-004", DeviceName: "Pixel 8", OSVersion: "15", Stars: 4, ReviewText: "AI trả lời nhanh, mong có thêm lịch sử chat dài hơn.", ReviewFlow: models.AppReviewFlowHighRatingPrompt, Status: models.AppReviewStatusReviewed, CreatedAt: now.Add(-36 * time.Hour)},
		{Platform: models.PlatformIOS, AppVersion: "2.3.0", DeviceID: "guest-ios-005", DeviceName: "iPhone 12", OSVersion: "18.0", Stars: 2, ReviewText: "Mở app lần đầu hơi khó hiểu, onboarding nên ngắn hơn.", ReviewFlow: models.AppReviewFlowLowRatingFeedback, Status: models.AppReviewStatusNew, CreatedAt: now.Add(-48 * time.Hour)},
		{Platform: models.PlatformAndroid, AppVersion: "2.2.8", DeviceID: "guest-android-006", DeviceName: "Vivo V30", OSVersion: "14", Stars: 5, ReviewText: "Xem ngày và giờ hoàng đạo rất tiện cho ba mẹ.", ReviewFlow: models.AppReviewFlowHighRatingPrompt, Status: models.AppReviewStatusResolved, CreatedAt: now.Add(-72 * time.Hour)},
		{Platform: models.PlatformIOS, AppVersion: "2.3.1", DeviceID: "guest-ios-007", DeviceName: "iPhone 14", OSVersion: "18.1", Stars: 3, ReviewText: "Widget đẹp nhưng đôi lúc chưa cập nhật ngay sau nửa đêm.", ReviewFlow: models.AppReviewFlowLowRatingFeedback, Status: models.AppReviewStatusReviewed, AdminNote: "Liên quan lịch refresh widget.", CreatedAt: now.Add(-96 * time.Hour)},
	}

	for _, item := range seeds {
		var userID *uuid.UUID
		if item.UserEmail != "" {
			var user models.User
			if err := db.Where("email = ?", item.UserEmail).First(&user).Error; err == nil {
				userID = &user.ID
			}
		}

		review := models.AppReview{
			UserID:     userID,
			Platform:   item.Platform,
			AppVersion: item.AppVersion,
			DeviceID:   item.DeviceID,
			DeviceName: item.DeviceName,
			OSVersion:  item.OSVersion,
			Stars:      item.Stars,
			ReviewText: item.ReviewText,
			ReviewFlow: item.ReviewFlow,
			Status:     item.Status,
			AdminNote:  item.AdminNote,
			CreatedAt:  item.CreatedAt,
			UpdatedAt:  item.CreatedAt,
		}

		var existing models.AppReview
		result := db.Where("device_id = ? AND created_at = ?", review.DeviceID, review.CreatedAt).First(&existing)
		if result.Error == nil {
			fmt.Printf("   ⏭️  App review '%s @ %s' already exists, skipping\n", review.DeviceID, review.CreatedAt.Format(time.RFC3339))
			continue
		}

		if err := db.Create(&review).Error; err != nil {
			fmt.Printf("   ❌ Failed to seed app review '%s': %v\n", review.DeviceID, err)
			continue
		}
		fmt.Printf("   ✅ Seeded app review: %s (%d★)\n", review.DeviceName, review.Stars)
	}
}
