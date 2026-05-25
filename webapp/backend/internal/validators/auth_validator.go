package validators

import (
	"fmt"
	"strings"

	"github.com/go-playground/validator/v10"
)

// Validator wraps the go-playground validator
type Validator struct {
	validate *validator.Validate
}

// NewValidator creates a new Validator instance
func NewValidator() *Validator {
	v := validator.New()
	return &Validator{validate: v}
}

// ValidateStruct validates a struct and returns field-level errors
func (v *Validator) ValidateStruct(s interface{}) map[string]string {
	err := v.validate.Struct(s)
	if err == nil {
		return nil
	}

	errors := make(map[string]string)
	for _, err := range err.(validator.ValidationErrors) {
		field := toSnakeCase(err.Field())
		errors[field] = formatValidationError(err)
	}
	return errors
}

// formatValidationError returns a human-readable validation error message
func formatValidationError(fe validator.FieldError) string {
	field := toSnakeCase(fe.Field())

	switch fe.Tag() {
	case "required":
		return fmt.Sprintf("%s is required", field)
	case "email":
		return "Invalid email format"
	case "min":
		return fmt.Sprintf("%s must be at least %s characters", field, fe.Param())
	case "max":
		return fmt.Sprintf("%s must be at most %s characters", field, fe.Param())
	case "eqfield":
		return fmt.Sprintf("%s must match %s", field, toSnakeCase(fe.Param()))
	case "oneof":
		return fmt.Sprintf("%s must be one of: %s", field, fe.Param())
	case "uuid":
		return fmt.Sprintf("%s must be a valid UUID", field)
	default:
		return fmt.Sprintf("%s is not valid", field)
	}
}

// toSnakeCase converts PascalCase/camelCase to snake_case
func toSnakeCase(s string) string {
	var result strings.Builder
	for i, r := range s {
		if i > 0 && r >= 'A' && r <= 'Z' {
			result.WriteRune('_')
		}
		result.WriteRune(r)
	}
	return strings.ToLower(result.String())
}
