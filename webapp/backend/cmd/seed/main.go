package main

import (
	"fmt"
	"log"
	"os"
	"time"

	"github.com/google/uuid"
	"github.com/zplus/lichso/internal/config"
	"github.com/zplus/lichso/internal/models"
	"github.com/zplus/lichso/internal/repositories"
	"github.com/zplus/lichso/internal/services"
	"go.uber.org/zap"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func main() {
	fmt.Println("🌱 Zplus Base - Database Seeder")
	fmt.Println("================================")

	// Load config
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("❌ Failed to load config: %v", err)
	}

	// Connect to PostgreSQL
	db, err := gorm.Open(postgres.Open(cfg.Database.DSN()), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		log.Fatalf("❌ Failed to connect to PostgreSQL: %v", err)
	}
	fmt.Println("✅ Connected to PostgreSQL")

	// Check command
	command := "all"
	if len(os.Args) > 1 {
		command = os.Args[1]
	}

	switch command {
	case "all":
		seedRoles(db)
		seedPermissions(db)
		seedRolePermissions(db)
		seedUsers(db)
		seedContentData(db)
		seedAppReviews(db)
	case "roles":
		seedRoles(db)
	case "permissions":
		seedPermissions(db)
	case "role-permissions":
		seedRolePermissions(db)
	case "users":
		seedUsers(db)
	case "content":
		seedContentData(db)
	case "categories":
		seedArticleCategories(db)
	case "quotes":
		seedQuotes(db)
	case "famous-people":
		seedFamousPeople(db)
	case "events":
		seedEvents(db)
	case "festivals":
		seedFolkFestivals(db)
	case "content-permissions":
		seedContentPermissions(db)
	case "articles":
		seedAllArticles(db)
	case "ai-prompts":
		seedAIPrompts(db)
	case "app-reviews":
		seedAppReviews(db)
	case "fresh":
		freshSeed(db)
	default:
		fmt.Printf("❌ Unknown command: %s\n", command)
		printUsage()
		os.Exit(1)
	}

	fmt.Println("\n✅ Seeding completed!")

	// Suppress unused import warning
	_ = zap.NewNop()
}

func printUsage() {
	fmt.Print(`
Usage:
  go run cmd/seed/main.go [command]

Commands:
  all              Seed everything (default)
  roles            Seed roles only
  permissions      Seed permissions only
  role-permissions Seed role-permission mappings only
  users            Seed users only
  content          Seed all content data (categories, quotes, people, events, festivals)
  categories       Seed article categories only
  quotes           Seed quotes only
  famous-people    Seed famous people only
  events           Seed events only
  festivals        Seed folk festivals only
  content-permissions Seed content permissions and role assignments
  articles         Seed all articles (~170 SEO articles)
  ai-prompts       Seed AI prompt templates
  app-reviews      Seed sample app reviews
  fresh            Clean and re-seed everything
`)
}

// seedAIPrompts seeds the default AI prompt templates using the service layer
func seedAIPrompts(db *gorm.DB) {
	fmt.Println("\n🤖 Seeding AI Prompt Templates...")
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	repo := repositories.NewAIPromptTemplateRepository(db)
	services.SeedDefaultPromptTemplates(repo, logger)
	fmt.Println("✅ AI prompt templates seeded")
}

// freshSeed cleans all data and re-seeds
func freshSeed(db *gorm.DB) {
	fmt.Println("\n🔄 Fresh seed: cleaning existing data...")

	// Delete in reverse dependency order
	db.Exec("DELETE FROM article_tag_relations")
	db.Exec("DELETE FROM articles")
	db.Exec("DELETE FROM article_tags")
	db.Exec("DELETE FROM article_categories")
	db.Exec("DELETE FROM quotes")
	db.Exec("DELETE FROM famous_people")
	db.Exec("DELETE FROM events")
	db.Exec("DELETE FROM folk_festivals")
	db.Exec("DELETE FROM role_permissions")
	db.Exec("DELETE FROM user_roles")
	db.Exec("DELETE FROM refresh_tokens")
	db.Exec("DELETE FROM users")
	db.Exec("DELETE FROM permissions")
	db.Exec("DELETE FROM roles")

	fmt.Println("✅ Data cleaned")

	seedRoles(db)
	seedPermissions(db)
	seedRolePermissions(db)
	seedUsers(db)
	seedContentData(db)
	seedAppReviews(db)
}

// seedRoles creates default system roles
func seedRoles(db *gorm.DB) {
	fmt.Println("\n📋 Seeding Roles...")

	roles := []models.Role{
		{
			Name:        models.RoleSuperAdmin,
			DisplayName: "Super Administrator",
			Description: "Toàn quyền hệ thống, không thể xóa",
			IsSystem:    true,
			Level:       0,
		},
		{
			Name:        models.RoleAdmin,
			DisplayName: "Administrator",
			Description: "Quản trị hệ thống",
			IsSystem:    true,
			Level:       1,
		},
		{
			Name:        models.RoleEditor,
			DisplayName: "Editor",
			Description: "Chỉnh sửa nội dung",
			IsSystem:    true,
			Level:       2,
		},
		{
			Name:        models.RoleViewer,
			DisplayName: "Viewer",
			Description: "Chỉ xem",
			IsSystem:    true,
			Level:       3,
		},
	}

	for _, role := range roles {
		var existing models.Role
		result := db.Where("name = ?", role.Name).First(&existing)
		if result.Error == nil {
			fmt.Printf("   ⏭️  Role '%s' already exists, skipping\n", role.Name)
			continue
		}

		if err := db.Create(&role).Error; err != nil {
			fmt.Printf("   ❌ Failed to create role '%s': %v\n", role.Name, err)
			continue
		}
		fmt.Printf("   ✅ Created role: %s (%s)\n", role.Name, role.DisplayName)
	}
}

// seedPermissions creates default permissions
func seedPermissions(db *gorm.DB) {
	fmt.Println("\n🔑 Seeding Permissions...")

	permissions := []models.Permission{
		// User Management
		{Name: "users.create", DisplayName: "Tạo người dùng", Module: "users", Action: "create", Description: "Tạo tài khoản người dùng mới"},
		{Name: "users.read", DisplayName: "Xem người dùng", Module: "users", Action: "read", Description: "Xem danh sách và chi tiết người dùng"},
		{Name: "users.update", DisplayName: "Cập nhật người dùng", Module: "users", Action: "update", Description: "Chỉnh sửa thông tin người dùng"},
		{Name: "users.delete", DisplayName: "Xóa người dùng", Module: "users", Action: "delete", Description: "Xóa tài khoản người dùng"},
		{Name: "users.export", DisplayName: "Xuất dữ liệu người dùng", Module: "users", Action: "export", Description: "Xuất danh sách người dùng ra file"},

		// Role Management
		{Name: "roles.create", DisplayName: "Tạo vai trò", Module: "roles", Action: "create", Description: "Tạo vai trò mới"},
		{Name: "roles.read", DisplayName: "Xem vai trò", Module: "roles", Action: "read", Description: "Xem danh sách và chi tiết vai trò"},
		{Name: "roles.update", DisplayName: "Cập nhật vai trò", Module: "roles", Action: "update", Description: "Chỉnh sửa thông tin vai trò"},
		{Name: "roles.delete", DisplayName: "Xóa vai trò", Module: "roles", Action: "delete", Description: "Xóa vai trò (không phải system role)"},
		{Name: "roles.assign", DisplayName: "Gán vai trò cho user", Module: "roles", Action: "assign", Description: "Gán hoặc bỏ vai trò cho người dùng"},

		// Permission Management
		{Name: "permissions.read", DisplayName: "Xem quyền", Module: "permissions", Action: "read", Description: "Xem danh sách quyền"},
		{Name: "permissions.assign", DisplayName: "Gán quyền cho role", Module: "permissions", Action: "assign", Description: "Gán hoặc bỏ quyền cho vai trò"},

		// Settings
		{Name: "settings.read", DisplayName: "Xem cài đặt", Module: "settings", Action: "read", Description: "Xem cài đặt hệ thống"},
		{Name: "settings.update", DisplayName: "Cập nhật cài đặt", Module: "settings", Action: "update", Description: "Thay đổi cài đặt hệ thống"},

		// Dashboard
		{Name: "dashboard.read", DisplayName: "Xem dashboard", Module: "dashboard", Action: "read", Description: "Truy cập trang dashboard"},
		{Name: "dashboard.stats", DisplayName: "Xem thống kê", Module: "dashboard", Action: "stats", Description: "Xem thống kê chi tiết"},

		// Activity Logs
		{Name: "logs.read", DisplayName: "Xem nhật ký hoạt động", Module: "logs", Action: "read", Description: "Xem nhật ký hoạt động hệ thống"},
		{Name: "logs.export", DisplayName: "Xuất nhật ký", Module: "logs", Action: "export", Description: "Xuất nhật ký ra file"},
	}

	for _, perm := range permissions {
		var existing models.Permission
		result := db.Where("name = ?", perm.Name).First(&existing)
		if result.Error == nil {
			fmt.Printf("   ⏭️  Permission '%s' already exists, skipping\n", perm.Name)
			continue
		}

		if err := db.Create(&perm).Error; err != nil {
			fmt.Printf("   ❌ Failed to create permission '%s': %v\n", perm.Name, err)
			continue
		}
		fmt.Printf("   ✅ Created permission: %s\n", perm.Name)
	}
}

// seedRolePermissions creates default role-permission mappings
func seedRolePermissions(db *gorm.DB) {
	fmt.Println("\n🔗 Seeding Role-Permission Mappings...")

	// Define role → permission mappings
	rolePermissions := map[string][]string{
		models.RoleSuperAdmin: {
			// Super admin gets ALL permissions
			"users.create", "users.read", "users.update", "users.delete", "users.export",
			"roles.create", "roles.read", "roles.update", "roles.delete", "roles.assign",
			"permissions.read", "permissions.assign",
			"settings.read", "settings.update",
			"dashboard.read", "dashboard.stats",
			"logs.read", "logs.export",
		},
		models.RoleAdmin: {
			"users.create", "users.read", "users.update", "users.delete", "users.export",
			"roles.read", "roles.assign",
			"permissions.read",
			"settings.read", "settings.update",
			"dashboard.read", "dashboard.stats",
			"logs.read", "logs.export",
		},
		models.RoleEditor: {
			"users.read",
			"dashboard.read",
		},
		models.RoleViewer: {
			"users.read",
			"dashboard.read",
		},
	}

	for roleName, permNames := range rolePermissions {
		// Find role
		var role models.Role
		if err := db.Where("name = ?", roleName).First(&role).Error; err != nil {
			fmt.Printf("   ❌ Role '%s' not found: %v\n", roleName, err)
			continue
		}

		assignedCount := 0
		for _, permName := range permNames {
			// Find permission
			var perm models.Permission
			if err := db.Where("name = ?", permName).First(&perm).Error; err != nil {
				fmt.Printf("   ❌ Permission '%s' not found: %v\n", permName, err)
				continue
			}

			// Check if mapping already exists
			var existing models.RolePermission
			result := db.Where("role_id = ? AND permission_id = ?", role.ID, perm.ID).First(&existing)
			if result.Error == nil {
				continue // Already exists
			}

			// Create mapping
			rp := models.RolePermission{
				RoleID:       role.ID,
				PermissionID: perm.ID,
			}
			if err := db.Create(&rp).Error; err != nil {
				fmt.Printf("   ❌ Failed to assign '%s' to '%s': %v\n", permName, roleName, err)
				continue
			}
			assignedCount++
		}

		if assignedCount > 0 {
			fmt.Printf("   ✅ Assigned %d permissions to role: %s\n", assignedCount, roleName)
		} else {
			fmt.Printf("   ⏭️  Role '%s' already has all permissions assigned\n", roleName)
		}
	}
}

// seedUsers creates default users
func seedUsers(db *gorm.DB) {
	fmt.Println("\n👤 Seeding Users...")

	type userSeed struct {
		Email     string
		Password  string
		FirstName string
		LastName  string
		IsActive  bool
		RoleName  string
	}

	users := []userSeed{
		{
			Email:     "admin@zplus.dev",
			Password:  "Admin@123",
			FirstName: "Super",
			LastName:  "Admin",
			IsActive:  true,
			RoleName:  models.RoleSuperAdmin,
		},
		{
			Email:     "editor@zplus.dev",
			Password:  "Editor@123",
			FirstName: "Test",
			LastName:  "Editor",
			IsActive:  true,
			RoleName:  models.RoleEditor,
		},
		{
			Email:     "viewer@zplus.dev",
			Password:  "Viewer@123",
			FirstName: "Test",
			LastName:  "Viewer",
			IsActive:  true,
			RoleName:  models.RoleViewer,
		},
	}

	for _, u := range users {
		// Check if user already exists
		var existing models.User
		result := db.Where("email = ?", u.Email).First(&existing)
		if result.Error == nil {
			fmt.Printf("   ⏭️  User '%s' already exists, skipping\n", u.Email)
			continue
		}

		// Hash password
		hashedPassword, err := bcrypt.GenerateFromPassword([]byte(u.Password), bcrypt.DefaultCost)
		if err != nil {
			fmt.Printf("   ❌ Failed to hash password for '%s': %v\n", u.Email, err)
			continue
		}

		now := time.Now()
		user := models.User{
			ID:        uuid.New(),
			Email:     u.Email,
			Password:  string(hashedPassword),
			FirstName: u.FirstName,
			LastName:  u.LastName,
			IsActive:  u.IsActive,
			LastLogin: &now,
		}

		if err := db.Create(&user).Error; err != nil {
			fmt.Printf("   ❌ Failed to create user '%s': %v\n", u.Email, err)
			continue
		}

		// Assign role
		var role models.Role
		if err := db.Where("name = ?", u.RoleName).First(&role).Error; err != nil {
			fmt.Printf("   ⚠️  Role '%s' not found for user '%s'\n", u.RoleName, u.Email)
		} else {
			userRole := models.UserRole{
				UserID: user.ID,
				RoleID: role.ID,
			}
			if err := db.Create(&userRole).Error; err != nil {
				fmt.Printf("   ⚠️  Failed to assign role '%s' to user '%s': %v\n", u.RoleName, u.Email, err)
			}
		}

		fmt.Printf("   ✅ Created user: %s (role: %s, password: %s)\n", u.Email, u.RoleName, u.Password)
	}
}
