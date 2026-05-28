package services

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/dto"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/utils"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type googleIDTokenClaims struct {
	Aud   string `json:"aud"`
	Azp   string `json:"azp"`
	Iss   string `json:"iss"`
	Sub   string `json:"sub"`
	Email string `json:"email"`
	Exp   int64  `json:"exp"`
}

func inspectGoogleIDToken(idToken string) (*googleIDTokenClaims, error) {
	parts := strings.Split(idToken, ".")
	if len(parts) < 2 {
		return nil, errors.New("invalid JWT format")
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return nil, err
	}
	var claims googleIDTokenClaims
	if err := json.Unmarshal(payload, &claims); err != nil {
		return nil, err
	}
	return &claims, nil
}

func parseClientMetadata(rawUA string) (string, map[string]interface{}) {
	ua := strings.TrimSpace(rawUA)
	metadata := map[string]interface{}{}

	parts := strings.SplitN(ua, "| client ", 2)
	if len(parts) == 2 {
		ua = strings.TrimSpace(parts[0])
		tags := strings.Split(parts[1], ";")
		for _, tag := range tags {
			kv := strings.SplitN(strings.TrimSpace(tag), "=", 2)
			if len(kv) != 2 {
				continue
			}
			key := strings.TrimSpace(kv[0])
			val := strings.TrimSpace(kv[1])
			if val == "" {
				continue
			}
			switch key {
			case "platform":
				metadata["platform"] = strings.ToLower(val)
			case "app":
				metadata["app_version"] = val
			case "device":
				metadata["device_name"] = val
			case "os":
				metadata["os_version"] = val
			}
		}
	}

	uaLower := strings.ToLower(ua)
	if _, ok := metadata["platform"]; !ok {
		switch {
		case strings.Contains(uaLower, "android"):
			metadata["platform"] = "android"
		case strings.Contains(uaLower, "iphone"), strings.Contains(uaLower, "ipad"), strings.Contains(uaLower, "ios"):
			metadata["platform"] = "ios"
		default:
			metadata["platform"] = "web"
		}
	}

	if strings.Contains(uaLower, "lichso-android/") {
		metadata["app"] = "lichso-android"
	}
	if strings.Contains(uaLower, "lichso-ios/") {
		metadata["app"] = "lichso-ios"
	}

	return ua, metadata
}

// AuthService handles authentication business logic
type AuthService struct {
	userRepo     *repositories.UserRepository
	cacheService *CacheService
	emailService *EmailService
	cfg          *config.Config
	logger       *zap.Logger
	mongoDB      *mongo.Database
}

// NewAuthService creates a new AuthService
func NewAuthService(
	userRepo *repositories.UserRepository,
	cacheService *CacheService,
	cfg *config.Config,
	logger *zap.Logger,
	mongoDB *mongo.Database,
) *AuthService {
	return &AuthService{
		userRepo:     userRepo,
		cacheService: cacheService,
		cfg:          cfg,
		logger:       logger,
		mongoDB:      mongoDB,
	}
}

// SetEmailService sets the email service (called after initialization to avoid circular deps)
func (s *AuthService) SetEmailService(emailService *EmailService) {
	s.emailService = emailService
}

// Login authenticates a user and returns tokens
func (s *AuthService) Login(req *dto.LoginRequest, ipAddress, userAgent string) (*dto.LoginResponse, error) {
	// Find user by email
	user, err := s.userRepo.FindByEmail(req.Email)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			s.logActivity("", req.Email, models.ActionLoginFailed, models.ModuleAuth,
				"Login failed: user not found", models.StatusFailure, ipAddress, userAgent)
			return nil, utils.ErrInvalidCredentials
		}
		return nil, utils.ErrDatabaseFail
	}

	// Check if user is active
	if !user.IsActive {
		s.logActivity(user.ID.String(), user.Email, models.ActionLoginFailed, models.ModuleAuth,
			"Login failed: account inactive", models.StatusFailure, ipAddress, userAgent)
		return nil, utils.ErrUserInactive
	}

	// Verify password
	if !utils.VerifyPassword(user.Password, req.Password) {
		s.logActivity(user.ID.String(), user.Email, models.ActionLoginFailed, models.ModuleAuth,
			"Login failed: wrong password", models.StatusFailure, ipAddress, userAgent)
		return nil, utils.ErrInvalidCredentials
	}

	// Get role names
	roleNames := make([]string, len(user.Roles))
	for i, role := range user.Roles {
		roleNames[i] = role.Name
	}

	// Get permissions
	permissions, err := s.userRepo.GetUserPermissions(user.ID)
	if err != nil {
		s.logger.Error("Failed to get user permissions", zap.Error(err))
		permissions = []string{}
	}

	// Generate token pair
	jwtCfg := &utils.JWTConfig{
		Secret:             s.cfg.JWT.Secret,
		AccessTokenExpiry:  s.cfg.JWT.AccessTokenExpiry,
		RefreshTokenExpiry: s.cfg.JWT.RefreshTokenExpiry,
		Issuer:             s.cfg.JWT.Issuer,
	}

	tokenPair, _, err := utils.GenerateTokenPair(jwtCfg, user.ID, user.Email, roleNames)
	if err != nil {
		s.logger.Error("Failed to generate tokens", zap.Error(err))
		return nil, utils.ErrInternal
	}

	// Save refresh token to database
	refreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     tokenPair.RefreshToken,
		UserAgent: userAgent,
		IPAddress: ipAddress,
		ExpiresAt: time.Now().Add(s.cfg.JWT.RefreshTokenExpiry),
	}

	if err := s.userRepo.SaveRefreshToken(refreshToken); err != nil {
		s.logger.Error("Failed to save refresh token", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Update last login
	_ = s.userRepo.UpdateLastLogin(user.ID)

	// Cache permissions
	_ = s.cacheService.CacheUserPermissions(user.ID, permissions)

	// Log activity
	s.logActivity(user.ID.String(), user.Email, models.ActionUserLogin, models.ModuleAuth,
		"User logged in successfully", models.StatusSuccess, ipAddress, userAgent)

	return &dto.LoginResponse{
		AccessToken:  tokenPair.AccessToken,
		RefreshToken: tokenPair.RefreshToken,
		ExpiresIn:    tokenPair.ExpiresIn,
		TokenType:    tokenPair.TokenType,
		User:         dto.ToAuthUserResponse(user, permissions),
	}, nil
}

// Register creates a new user account
func (s *AuthService) Register(req *dto.RegisterRequest, ipAddress, userAgent string) (*models.UserResponse, error) {
	// Check if email exists
	exists, err := s.userRepo.EmailExists(req.Email)
	if err != nil {
		return nil, utils.ErrDatabaseFail
	}
	if exists {
		return nil, utils.ErrEmailExists
	}

	// Hash password
	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		s.logger.Error("Failed to hash password", zap.Error(err))
		return nil, utils.ErrInternal
	}

	// Create user
	user := &models.User{
		ID:        uuid.New(),
		Email:     req.Email,
		Password:  hashedPassword,
		FirstName: req.FirstName,
		LastName:  req.LastName,
		IsActive:  true,
	}

	if err := s.userRepo.Create(user); err != nil {
		s.logger.Error("Failed to create user", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Assign default viewer role
	var viewerRole models.Role
	s.userRepo.FindRoleByName(models.RoleViewer, &viewerRole)
	if viewerRole.ID != uuid.Nil {
		_ = s.userRepo.AssignRole(user.ID, viewerRole.ID, nil)
		user.Roles = []models.Role{viewerRole}
	}

	// Log activity
	s.logActivity(user.ID.String(), user.Email, models.ActionUserCreate, models.ModuleAuth,
		"New user registered", models.StatusSuccess, ipAddress, userAgent)

	// Send welcome email
	if s.emailService != nil && s.emailService.IsEnabled() {
		go func() {
			if err := s.emailService.SendWelcomeEmail(user.Email, user.FullName()); err != nil {
				s.logger.Error("Failed to send welcome email",
					zap.String("email", user.Email),
					zap.Error(err),
				)
			}
		}()
	}

	resp := user.ToResponse()
	return &resp, nil
}

// RefreshToken rotates refresh tokens and returns new token pair
func (s *AuthService) RefreshToken(req *dto.RefreshTokenRequest, ipAddress, userAgent string) (*dto.RefreshTokenResponse, error) {
	// Find refresh token in database
	storedToken, err := s.userRepo.FindRefreshToken(req.RefreshToken)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, utils.ErrRefreshTokenExpired
		}
		return nil, utils.ErrDatabaseFail
	}

	// Check if token is valid
	if !storedToken.IsValid() {
		return nil, utils.ErrRefreshTokenExpired
	}

	// Validate the JWT itself
	claims, err := utils.ValidateToken(req.RefreshToken, s.cfg.JWT.Secret)
	if err != nil {
		// Revoke the stored token since JWT is invalid
		_ = s.userRepo.RevokeRefreshToken(storedToken.ID)
		return nil, utils.ErrRefreshTokenExpired
	}

	// Verify the token type
	if claims.Type != utils.RefreshToken {
		return nil, utils.ErrTokenInvalid
	}

	// Revoke old refresh token (rotation)
	_ = s.userRepo.RevokeRefreshToken(storedToken.ID)

	// Get user with roles
	user, err := s.userRepo.FindByID(storedToken.UserID)
	if err != nil {
		return nil, utils.ErrUserNotFound
	}

	if !user.IsActive {
		return nil, utils.ErrUserInactive
	}

	// Get role names
	roleNames := make([]string, len(user.Roles))
	for i, role := range user.Roles {
		roleNames[i] = role.Name
	}

	// Generate new token pair
	jwtCfg := &utils.JWTConfig{
		Secret:             s.cfg.JWT.Secret,
		AccessTokenExpiry:  s.cfg.JWT.AccessTokenExpiry,
		RefreshTokenExpiry: s.cfg.JWT.RefreshTokenExpiry,
		Issuer:             s.cfg.JWT.Issuer,
	}

	tokenPair, _, err := utils.GenerateTokenPair(jwtCfg, user.ID, user.Email, roleNames)
	if err != nil {
		return nil, utils.ErrInternal
	}

	// Save new refresh token
	newRefreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     tokenPair.RefreshToken,
		UserAgent: userAgent,
		IPAddress: ipAddress,
		ExpiresAt: time.Now().Add(s.cfg.JWT.RefreshTokenExpiry),
	}

	if err := s.userRepo.SaveRefreshToken(newRefreshToken); err != nil {
		return nil, utils.ErrDatabaseFail
	}

	// Refresh permission cache
	permissions, _ := s.userRepo.GetUserPermissions(user.ID)
	_ = s.cacheService.CacheUserPermissions(user.ID, permissions)

	return &dto.RefreshTokenResponse{
		AccessToken:  tokenPair.AccessToken,
		RefreshToken: tokenPair.RefreshToken,
		ExpiresIn:    tokenPair.ExpiresIn,
	}, nil
}

// Logout blacklists the access token and revokes refresh tokens
func (s *AuthService) Logout(userID uuid.UUID, tokenJTI string, tokenTTL time.Duration) error {
	// Blacklist access token in Redis
	if err := s.cacheService.BlacklistToken(tokenJTI, tokenTTL); err != nil {
		s.logger.Error("Failed to blacklist token", zap.Error(err))
	}

	// Revoke all refresh tokens for user
	if err := s.userRepo.RevokeAllUserRefreshTokens(userID); err != nil {
		s.logger.Error("Failed to revoke refresh tokens", zap.Error(err))
	}

	// Clear session cache
	_ = s.cacheService.ClearUserSession(userID)

	return nil
}

// ForgotPassword generates a password reset token
func (s *AuthService) ForgotPassword(req *dto.ForgotPasswordRequest) error {
	user, err := s.userRepo.FindByEmail(req.Email)
	if err != nil {
		// Don't reveal whether email exists - silently return
		return nil
	}

	// Generate reset token
	resetToken, err := utils.GenerateRandomToken(32)
	if err != nil {
		return utils.ErrInternal
	}

	// Store in Redis with TTL
	if err := s.cacheService.StorePasswordResetToken(resetToken, user.ID); err != nil {
		return utils.ErrCacheFail
	}

	// Send email with reset link
	if s.emailService != nil && s.emailService.IsEnabled() {
		go func() {
			if err := s.emailService.SendPasswordResetEmail(user.Email, user.FullName(), resetToken); err != nil {
				s.logger.Error("Failed to send password reset email",
					zap.String("email", user.Email),
					zap.Error(err),
				)
			}
		}()
	} else {
		s.logger.Info("Password reset token generated (email not configured)",
			zap.String("email", user.Email),
			zap.String("reset_token", resetToken),
		)
	}

	return nil
}

// ResetPassword resets a user's password using a reset token
func (s *AuthService) ResetPassword(req *dto.ResetPasswordRequest, ipAddress, userAgent string) error {
	// Get user ID from reset token
	userID, err := s.cacheService.GetPasswordResetUserID(req.Token)
	if err != nil {
		return utils.ErrTokenInvalid
	}

	// Hash new password
	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		return utils.ErrInternal
	}

	// Update password
	if err := s.userRepo.UpdatePassword(userID, hashedPassword); err != nil {
		return utils.ErrDatabaseFail
	}

	// Delete reset token
	_ = s.cacheService.DeletePasswordResetToken(req.Token)

	// Revoke all refresh tokens (force re-login)
	_ = s.userRepo.RevokeAllUserRefreshTokens(userID)

	// Log activity
	s.logActivity(userID.String(), "", models.ActionUserPasswordReset, models.ModuleAuth,
		"Password reset successfully", models.StatusSuccess, ipAddress, userAgent)

	return nil
}

// ChangePassword changes a user's password after verifying the current one
func (s *AuthService) ChangePassword(userID uuid.UUID, req *dto.ChangePasswordRequest, ipAddress, userAgent string) error {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return utils.ErrUserNotFound
	}

	// Verify current password
	if !utils.VerifyPassword(user.Password, req.CurrentPassword) {
		return utils.ErrPasswordMismatch
	}

	// Hash new password
	hashedPassword, err := utils.HashPassword(req.NewPassword)
	if err != nil {
		return utils.ErrInternal
	}

	// Update password
	if err := s.userRepo.UpdatePassword(userID, hashedPassword); err != nil {
		return utils.ErrDatabaseFail
	}

	// Log activity
	s.logActivity(userID.String(), user.Email, models.ActionUserPasswordChange, models.ModuleAuth,
		"Password changed successfully", models.StatusSuccess, ipAddress, userAgent)

	return nil
}

// GoogleLogin authenticates a user via Google ID token and returns tokens
func (s *AuthService) GoogleLogin(req *dto.GoogleLoginRequest, ipAddress, userAgent string) (*dto.LoginResponse, error) {
	// Check if Google login is configured
	if s.cfg.Google.ClientID == "" {
		return nil, utils.ErrGoogleNotConfigured
	}

	// Verify Google ID token — accept web client ID, Android Firebase client ID, and iOS native client ID
	allowedIDs := make([]string, 0, 8)
	appendClientIDs := func(raw string) {
		for _, id := range strings.Split(raw, ",") {
			trimmed := strings.TrimSpace(id)
			if trimmed != "" {
				allowedIDs = append(allowedIDs, trimmed)
			}
		}
	}
	appendClientIDs(s.cfg.Google.ClientID)
	appendClientIDs(s.cfg.Google.AndroidClientID)
	appendClientIDs(s.cfg.Google.IOSClientID)

	if claims, err := inspectGoogleIDToken(req.IDToken); err == nil {
		s.logger.Info("Google login token received",
			zap.String("aud", claims.Aud),
			zap.String("azp", claims.Azp),
			zap.String("iss", claims.Iss),
			zap.String("email", claims.Email),
			zap.String("sub", claims.Sub),
			zap.Int64("exp", claims.Exp),
		)
	} else {
		s.logger.Warn("Failed to decode Google ID token payload", zap.Error(err))
	}

	googleUser, err := utils.VerifyGoogleIDToken(req.IDToken, allowedIDs...)
	if err != nil {
		s.logger.Error("Failed to verify Google ID token",
			zap.Error(err),
			zap.Int("allowed_client_ids_count", len(allowedIDs)),
			zap.Strings("allowed_client_ids", allowedIDs),
		)
		s.logActivity("", "", models.ActionLoginFailed, models.ModuleAuth,
			"Google login failed: invalid id token", models.StatusFailure, ipAddress, userAgent)
		return nil, utils.ErrGoogleTokenInvalid
	}

	// Try to find existing user by provider
	user, err := s.userRepo.FindByProvider(models.ProviderGoogle, googleUser.Sub)
	if err != nil {
		// User not found by provider, check by email
		user, err = s.userRepo.FindByEmail(googleUser.Email)
		if err != nil {
			// No existing user, create a new one
			user = &models.User{
				ID:         uuid.New(),
				Email:      googleUser.Email,
				FirstName:  googleUser.GivenName,
				LastName:   googleUser.FamilyName,
				Avatar:     googleUser.Picture,
				Provider:   models.ProviderGoogle,
				ProviderID: googleUser.Sub,
				IsActive:   true,
			}

			if err := s.userRepo.Create(user); err != nil {
				s.logger.Error("Failed to create Google user", zap.Error(err))
				return nil, utils.ErrDatabaseFail
			}

			// Assign default viewer role
			var viewerRole models.Role
			s.userRepo.FindRoleByName(models.RoleViewer, &viewerRole)
			if viewerRole.ID != uuid.Nil {
				_ = s.userRepo.AssignRole(user.ID, viewerRole.ID, nil)
				user.Roles = []models.Role{viewerRole}
			}

			s.logActivity(user.ID.String(), user.Email, models.ActionUserCreate, models.ModuleAuth,
				"New user registered via Google", models.StatusSuccess, ipAddress, userAgent)
		} else {
			// User exists with same email but different provider — link Google provider
			user.Provider = models.ProviderGoogle
			user.ProviderID = googleUser.Sub
			// Always sync latest avatar from Google
			if googleUser.Picture != "" {
				user.Avatar = googleUser.Picture
			}
			if err := s.userRepo.Update(user); err != nil {
				s.logger.Error("Failed to link Google provider to existing user", zap.Error(err))
				return nil, utils.ErrDatabaseFail
			}
		}
	} else {
		// User found by provider — sync latest avatar from Google if changed
		if googleUser.Picture != "" && user.Avatar != googleUser.Picture {
			user.Avatar = googleUser.Picture
			if err := s.userRepo.Update(user); err != nil {
				s.logger.Error("Failed to update Google user avatar", zap.Error(err))
				// Non-fatal: continue login even if avatar update fails
			}
		}
	}

	// Check if user is active
	if !user.IsActive {
		s.logActivity(user.ID.String(), user.Email, models.ActionLoginFailed, models.ModuleAuth,
			"Google login failed: account inactive", models.StatusFailure, ipAddress, userAgent)
		return nil, utils.ErrUserInactive
	}

	// Get role names
	roleNames := make([]string, len(user.Roles))
	for i, role := range user.Roles {
		roleNames[i] = role.Name
	}

	// Get permissions
	permissions, err := s.userRepo.GetUserPermissions(user.ID)
	if err != nil {
		s.logger.Error("Failed to get user permissions", zap.Error(err))
		permissions = []string{}
	}

	// Generate token pair
	jwtCfg := &utils.JWTConfig{
		Secret:             s.cfg.JWT.Secret,
		AccessTokenExpiry:  s.cfg.JWT.AccessTokenExpiry,
		RefreshTokenExpiry: s.cfg.JWT.RefreshTokenExpiry,
		Issuer:             s.cfg.JWT.Issuer,
	}

	tokenPair, _, err := utils.GenerateTokenPair(jwtCfg, user.ID, user.Email, roleNames)
	if err != nil {
		s.logger.Error("Failed to generate tokens", zap.Error(err))
		return nil, utils.ErrInternal
	}

	// Save refresh token to database
	refreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     tokenPair.RefreshToken,
		UserAgent: userAgent,
		IPAddress: ipAddress,
		ExpiresAt: time.Now().Add(s.cfg.JWT.RefreshTokenExpiry),
	}

	if err := s.userRepo.SaveRefreshToken(refreshToken); err != nil {
		s.logger.Error("Failed to save refresh token", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	// Update last login
	_ = s.userRepo.UpdateLastLogin(user.ID)

	// Cache permissions
	_ = s.cacheService.CacheUserPermissions(user.ID, permissions)

	// Log activity
	s.logActivity(user.ID.String(), user.Email, models.ActionUserLogin, models.ModuleAuth,
		"User logged in via Google", models.StatusSuccess, ipAddress, userAgent)

	return &dto.LoginResponse{
		AccessToken:  tokenPair.AccessToken,
		RefreshToken: tokenPair.RefreshToken,
		ExpiresIn:    tokenPair.ExpiresIn,
		TokenType:    tokenPair.TokenType,
		User:         dto.ToAuthUserResponse(user, permissions),
	}, nil
}

// GetMe returns the current user's information
func (s *AuthService) GetMe(userID uuid.UUID) (*dto.GetMeResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return nil, utils.ErrUserNotFound
	}

	permissions, _ := s.userRepo.GetUserPermissions(user.ID)

	return &dto.GetMeResponse{
		User: dto.ToAuthUserResponse(user, permissions),
	}, nil
}

// UpdateProfile updates the current user's first/last name
func (s *AuthService) UpdateProfile(userID uuid.UUID, req *dto.UpdateProfileRequest) (*dto.GetMeResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return nil, utils.ErrUserNotFound
	}

	user.FirstName = req.FirstName
	user.LastName = req.LastName

	if err := s.userRepo.Update(user); err != nil {
		s.logger.Error("Failed to update user profile", zap.Error(err))
		return nil, utils.ErrDatabaseFail
	}

	permissions, _ := s.userRepo.GetUserPermissions(user.ID)

	return &dto.GetMeResponse{
		User: dto.ToAuthUserResponse(user, permissions),
	}, nil
}

// logActivity logs an activity to MongoDB
func (s *AuthService) logActivity(userID, email, action, module, description, status, ip, ua string) {
	cleanUA, metadata := parseClientMetadata(ua)

	log := models.NewActivityLog(userID, email, action, module, description).
		WithStatus(status).
		WithIPAndAgent(ip, cleanUA)
	if len(metadata) > 0 {
		log = log.WithMetadata(metadata)
	}

	collection := s.mongoDB.Collection(models.ActivityLog{}.CollectionName())
	_, err := collection.InsertOne(context.Background(), log)
	if err != nil {
		s.logger.Error("Failed to log activity",
			zap.String("action", action),
			zap.Error(err),
		)
	}
}
