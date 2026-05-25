package utils

import (
	"fmt"
	"net/http"
)

// AppError represents a custom application error
type AppError struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Detail  string `json:"detail,omitempty"`
}

// Error implements the error interface
func (e *AppError) Error() string {
	if e.Detail != "" {
		return fmt.Sprintf("[%d] %s: %s", e.Code, e.Message, e.Detail)
	}
	return fmt.Sprintf("[%d] %s", e.Code, e.Message)
}

// StatusCode returns the HTTP status code for this error
func (e *AppError) StatusCode() int {
	return e.Code
}

// NewAppError creates a new AppError
func NewAppError(code int, message string, detail ...string) *AppError {
	err := &AppError{
		Code:    code,
		Message: message,
	}
	if len(detail) > 0 {
		err.Detail = detail[0]
	}
	return err
}

// ============================================
// Pre-defined errors
// ============================================

// Authentication errors
var (
	ErrInvalidCredentials  = &AppError{Code: http.StatusUnauthorized, Message: "Invalid email or password"}
	ErrTokenExpired        = &AppError{Code: http.StatusUnauthorized, Message: "Token has expired"}
	ErrTokenInvalid        = &AppError{Code: http.StatusUnauthorized, Message: "Invalid token"}
	ErrTokenBlacklisted    = &AppError{Code: http.StatusUnauthorized, Message: "Token has been revoked"}
	ErrUnauthorized        = &AppError{Code: http.StatusUnauthorized, Message: "Unauthorized"}
	ErrRefreshTokenExpired = &AppError{Code: http.StatusUnauthorized, Message: "Refresh token has expired"}
)

// Authorization errors
var (
	ErrForbidden        = &AppError{Code: http.StatusForbidden, Message: "You don't have permission to access this resource"}
	ErrInsufficientRole = &AppError{Code: http.StatusForbidden, Message: "Insufficient role privileges"}
)

// Resource errors
var (
	ErrUserNotFound       = &AppError{Code: http.StatusNotFound, Message: "User not found"}
	ErrRoleNotFound       = &AppError{Code: http.StatusNotFound, Message: "Role not found"}
	ErrPermissionNotFound = &AppError{Code: http.StatusNotFound, Message: "Permission not found"}
	ErrResourceNotFound   = &AppError{Code: http.StatusNotFound, Message: "Resource not found"}
)

// Conflict errors
var (
	ErrEmailExists    = &AppError{Code: http.StatusConflict, Message: "Email already exists"}
	ErrRoleExists     = &AppError{Code: http.StatusConflict, Message: "Role already exists"}
	ErrResourceExists = &AppError{Code: http.StatusConflict, Message: "Resource already exists"}
)

// Validation errors
var (
	ErrInvalidInput     = &AppError{Code: http.StatusBadRequest, Message: "Invalid input"}
	ErrInvalidJSON      = &AppError{Code: http.StatusBadRequest, Message: "Invalid JSON format"}
	ErrValidationFailed = &AppError{Code: http.StatusUnprocessableEntity, Message: "Validation failed"}
)

// System errors
var (
	ErrInternal     = &AppError{Code: http.StatusInternalServerError, Message: "Internal server error"}
	ErrDatabaseFail = &AppError{Code: http.StatusInternalServerError, Message: "Database operation failed"}
	ErrCacheFail    = &AppError{Code: http.StatusInternalServerError, Message: "Cache operation failed"}
)

// Rate limit errors
var (
	ErrTooManyRequests = &AppError{Code: http.StatusTooManyRequests, Message: "Too many requests, please try again later"}
)

// Business logic errors
var (
	ErrCannotDeleteSystemRole = &AppError{Code: http.StatusBadRequest, Message: "Cannot delete system role"}
	ErrCannotDeleteSelf       = &AppError{Code: http.StatusBadRequest, Message: "Cannot delete your own account"}
	ErrPasswordMismatch       = &AppError{Code: http.StatusBadRequest, Message: "Current password is incorrect"}
	ErrUserInactive           = &AppError{Code: http.StatusForbidden, Message: "User account is inactive"}
	ErrGoogleTokenInvalid     = &AppError{Code: http.StatusUnauthorized, Message: "Invalid Google ID token"}
	ErrGoogleNotConfigured    = &AppError{Code: http.StatusBadRequest, Message: "Google login is not configured"}
)

// IsAppError checks if an error is an AppError and returns it
func IsAppError(err error) (*AppError, bool) {
	appErr, ok := err.(*AppError)
	return appErr, ok
}
