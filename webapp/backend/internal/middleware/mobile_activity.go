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
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

const (
	mobileGuestActiveAction = "app.mobile_active_guest"
	mobileUserActiveAction  = "app.mobile_active_user"
)

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

// MobileActivityTracker logs one mobile active event per device/user per day.
func MobileActivityTracker(redisClient *redis.Client, mongoDB *mongo.Database, pgDB *gorm.DB, jwtSecret string, logger *zap.Logger) fiber.Handler {
	collection := mongoDB.Collection(models.ActivityLog{}.CollectionName())

	return func(c *fiber.Ctx) error {
		err := c.Next()

		// Only track API requests.
		if !strings.HasPrefix(c.Path(), "/api/") {
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
		deviceID := strings.TrimSpace(c.Get("X-Device-ID"))
		ipAddress := strings.TrimSpace(c.IP())
		if ipAddress == "" {
			ipAddress = "unknown"
		}

		day, ttl := dayKeyAndTTL(time.Now())
		fp := fingerprint(ipAddress, platform, appVersion, deviceName, osVersion, userAgent)
		ctx := context.Background()

		// Extract token from Authorization header if present
		var token string
		authHeader := c.Get("Authorization")
		if authHeader != "" {
			parts := strings.SplitN(authHeader, " ", 2)
			if len(parts) == 2 && strings.EqualFold(parts[0], "bearer") {
				token = parts[1]
			}
		}

		// Fallback to token query param (useful for WS)
		if token == "" {
			token = c.Query("token")
		}

		var loggedInUser *utils.JWTClaims
		if token != "" {
			if claims, valErr := utils.ValidateToken(token, jwtSecret); valErr == nil && claims.Type == utils.AccessToken {
				loggedInUser = claims
			}
		}

		if loggedInUser != nil {
			// Logged in user activity
			userIDStr := loggedInUser.UserID.String()
			redisKey := "mobile:user:active:" + day + ":" + userIDStr

			locked, lockErr := redisClient.SetNX(ctx, redisKey, "1", ttl).Result()
			if lockErr != nil {
				logger.Warn("mobile activity tracker redis error (user)", zap.Error(lockErr))
				return err
			}
			if !locked {
				return err
			}

			// Check if new or returning user
			userType := "Returning user"
			var dbUser models.User
			if dbErr := pgDB.Select("created_at").First(&dbUser, "id = ?", loggedInUser.UserID).Error; dbErr == nil {
				if time.Since(dbUser.CreatedAt) < 24*time.Hour {
					userType = "New user"
				}
			}

			log := models.NewActivityLog(userIDStr, loggedInUser.Email, mobileUserActiveAction, models.ModuleSystem, userType+" mobile app active").
				WithStatus(models.StatusSuccess).
				WithIPAndAgent(ipAddress, userAgent).
				WithMetadata(map[string]interface{}{
					"platform":    platform,
					"app_version": appVersion,
					"device_name": deviceName,
					"os_version":  osVersion,
					"device_id":   deviceID,
					"path":        c.Path(),
					"method":      c.Method(),
					"day":         day,
					"fingerprint": fp,
					"user_type":   userType,
				})

			if _, insertErr := collection.InsertOne(ctx, log); insertErr != nil {
				logger.Warn("mobile activity tracker mongo insert failed (user)", zap.Error(insertErr))
			}
		} else {
			// Guest user activity
			redisKey := "mobile:guest:active:" + day + ":" + fp

			locked, lockErr := redisClient.SetNX(ctx, redisKey, "1", ttl).Result()
			if lockErr != nil {
				logger.Warn("mobile activity tracker redis error (guest)", zap.Error(lockErr))
				return err
			}
			if !locked {
				return err
			}

			// Distinguish new or returning guest using a permanent Redis set/key
			deviceKey := fp
			if deviceID != "" {
				deviceKey = deviceID
			}

			redisSeenKey := "device:seen:" + deviceKey
			isNewDevice, seenErr := redisClient.SetNX(ctx, redisSeenKey, "1", 0).Result()
			guestType := "Returning guest"
			if seenErr == nil && isNewDevice {
				guestType = "New guest"
			}

			log := models.NewActivityLog("guest-mobile", "guest@mobile", mobileGuestActiveAction, models.ModuleSystem, guestType+" mobile app active").
				WithStatus(models.StatusSuccess).
				WithIPAndAgent(ipAddress, userAgent).
				WithMetadata(map[string]interface{}{
					"platform":    platform,
					"app_version": appVersion,
					"device_name": deviceName,
					"os_version":  osVersion,
					"device_id":   deviceID,
					"path":        c.Path(),
					"method":      c.Method(),
					"day":         day,
					"fingerprint": fp,
					"guest_type":  guestType,
				})

			if _, insertErr := collection.InsertOne(ctx, log); insertErr != nil {
				logger.Warn("mobile activity tracker mongo insert failed (guest)", zap.Error(insertErr))
			}
		}

		return err
	}
}

