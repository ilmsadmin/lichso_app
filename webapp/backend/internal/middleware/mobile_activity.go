package middleware

import (
	"context"
	"crypto/sha1"
	"encoding/hex"
	"strings"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/redis/go-redis/v9"
	"github.com/zplus/lichso/internal/models"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
)

const mobileGuestActiveAction = "app.mobile_active_guest"

func detectPlatform(userAgent, headerPlatform string) string {
	platform := strings.ToLower(strings.TrimSpace(headerPlatform))
	if platform == "android" || platform == "ios" {
		return platform
	}

	ua := strings.ToLower(userAgent)
	switch {
	case strings.Contains(ua, "android"):
		return "android"
	case strings.Contains(ua, "iphone"), strings.Contains(ua, "ipad"), strings.Contains(ua, "ios"):
		return "ios"
	default:
		return ""
	}
}

func dayKeyAndTTL(now time.Time) (string, time.Duration) {
	loc, err := time.LoadLocation("Asia/Ho_Chi_Minh")
	if err != nil {
		loc = time.FixedZone("UTC+7", 7*60*60)
	}
	localNow := now.In(loc)
	key := localNow.Format("2006-01-02")

	nextDay := time.Date(localNow.Year(), localNow.Month(), localNow.Day()+1, 0, 0, 0, 0, loc)
	ttl := time.Until(nextDay)
	if ttl <= 0 {
		ttl = 24 * time.Hour
	}
	return key, ttl
}

func fingerprint(ip, platform, appVersion, deviceName, osVersion, userAgent string) string {
	raw := strings.Join([]string{
		strings.TrimSpace(ip),
		strings.TrimSpace(platform),
		strings.TrimSpace(appVersion),
		strings.TrimSpace(deviceName),
		strings.TrimSpace(osVersion),
		strings.TrimSpace(userAgent),
	}, "|")
	sum := sha1.Sum([]byte(raw))
	return hex.EncodeToString(sum[:])
}

// MobileGuestActivityTracker logs one guest mobile active event per device per day.
func MobileGuestActivityTracker(redisClient *redis.Client, mongoDB *mongo.Database, logger *zap.Logger) fiber.Handler {
	collection := mongoDB.Collection(models.ActivityLog{}.CollectionName())

	return func(c *fiber.Ctx) error {
		err := c.Next()

		// Only track API requests.
		if !strings.HasPrefix(c.Path(), "/api/") {
			return err
		}

		// Track only non-authenticated (guest) traffic.
		if strings.TrimSpace(c.Get("Authorization")) != "" {
			return err
		}

		userAgent := strings.TrimSpace(c.Get("User-Agent"))
		platform := detectPlatform(userAgent, c.Get("X-Client-Platform"))
		if platform == "" {
			return err
		}

		appVersion := strings.TrimSpace(c.Get("X-App-Version"))
		deviceName := strings.TrimSpace(c.Get("X-Device-Name"))
		osVersion := strings.TrimSpace(c.Get("X-OS-Version"))
		ipAddress := strings.TrimSpace(c.IP())
		if ipAddress == "" {
			ipAddress = "unknown"
		}

		day, ttl := dayKeyAndTTL(time.Now())
		fp := fingerprint(ipAddress, platform, appVersion, deviceName, osVersion, userAgent)
		redisKey := "mobile:guest:active:" + day + ":" + fp

		ctx := context.Background()
		locked, lockErr := redisClient.SetNX(ctx, redisKey, "1", ttl).Result()
		if lockErr != nil {
			logger.Warn("mobile activity tracker redis error", zap.Error(lockErr))
			return err
		}
		if !locked {
			return err
		}

		log := models.NewActivityLog("guest-mobile", "guest@mobile", mobileGuestActiveAction, models.ModuleSystem, "Guest mobile app active").
			WithStatus(models.StatusSuccess).
			WithIPAndAgent(ipAddress, userAgent).
			WithMetadata(map[string]interface{}{
				"platform":    platform,
				"app_version": appVersion,
				"device_name": deviceName,
				"os_version":  osVersion,
				"path":        c.Path(),
				"method":      c.Method(),
				"day":         day,
				"fingerprint": fp,
			})

		if _, insertErr := collection.InsertOne(ctx, log); insertErr != nil {
			logger.Warn("mobile activity tracker mongo insert failed", zap.Error(insertErr))
		}

		return err
	}
}

