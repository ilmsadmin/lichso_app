package validators

import (
	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/dto"
)

// ValidateCreateUser performs business-level validation on create user request
func ValidateCreateUser(req *dto.CreateUserRequest) map[string]string {
	errors := make(map[string]string)

	if req.Email == "" {
		errors["email"] = "email is required"
	}
	if req.Password == "" {
		errors["password"] = "password is required"
	} else if len(req.Password) < 8 {
		errors["password"] = "password must be at least 8 characters"
	}
	if req.FirstName == "" {
		errors["first_name"] = "first_name is required"
	}
	if req.LastName == "" {
		errors["last_name"] = "last_name is required"
	}

	if len(errors) > 0 {
		return errors
	}
	return nil
}

// ValidateUserRoles validates that role IDs are not empty UUIDs
func ValidateUserRoles(roleIDs []uuid.UUID) map[string]string {
	for i, id := range roleIDs {
		if id == uuid.Nil {
			return map[string]string{
				"role_ids": "role_ids contains an invalid UUID at index " + string(rune('0'+i)),
			}
		}
	}
	return nil
}
