.PHONY: help up down restart rebuild logs logs-api logs-web clean migrate migrate-down seed dev-api dev-web test-api test-web build-prod deploy-prod ps health

# ============================================
# HELP
# ============================================
help: ## Show this help message
	@echo ""
	@echo "  🚀 Lịch Số - Available Commands"
	@echo "  ====================================="
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""

# ============================================
# DOCKER COMMANDS
# ============================================
up: ## Start all services in background
	docker-compose up -d
	@echo "✅ All services started!"
	@echo "   Frontend: http://localhost:3000"
	@echo "   Backend:  http://localhost:8080"
	@echo "   Nginx:    http://localhost"

down: ## Stop all services
	docker-compose down
	@echo "✅ All services stopped!"

restart: ## Restart all services
	docker-compose restart
	@echo "✅ All services restarted!"

rebuild: ## Rebuild and start all services
	docker-compose up -d --build
	@echo "✅ All services rebuilt and started!"

logs: ## View all service logs (follow mode)
	docker-compose logs -f

logs-api: ## View API logs only
	docker-compose logs -f api

logs-web: ## View frontend logs only
	docker-compose logs -f web

logs-db: ## View database logs
	docker-compose logs -f postgres mongodb redis

clean: ## Stop services and remove all volumes (⚠️ deletes data)
	docker-compose down -v
	@echo "⚠️  All services stopped and volumes removed!"

ps: ## Show running services status
	docker-compose ps

# ============================================
# DATABASE COMMANDS
# ============================================
migrate: ## Run database migrations (up)
	cd backend && go run cmd/migrate/main.go up

migrate-down: ## Rollback last migration
	cd backend && go run cmd/migrate/main.go down

migrate-fresh: ## Reset and re-run all migrations
	cd backend && go run cmd/migrate/main.go fresh

migrate-status: ## Show migration status
	cd backend && go run cmd/migrate/main.go status

seed: ## Seed database with default data
	cd backend && go run ./cmd/seed/ all

seed-fresh: ## Clean and re-seed database
	cd backend && go run ./cmd/seed/ fresh

db-shell: ## Open PostgreSQL shell
	docker-compose exec postgres psql -U zplus_user -d zplus_lichso_db

mongo-shell: ## Open MongoDB shell
	docker-compose exec mongodb mongosh -u zplus_user -p zplus_secret zplus_lichso_logs

redis-cli: ## Open Redis CLI
	docker-compose exec redis redis-cli

# ============================================
# DEVELOPMENT (LOCAL)
# ============================================
dev-api: ## Run backend API with hot reload (local)
	cd backend && air -c .air.toml

dev-web: ## Run frontend dev server (local)
	cd frontend && npm run dev

install-api: ## Install backend Go dependencies
	cd backend && go mod download && go mod tidy

install-web: ## Install frontend dependencies
	cd frontend && npm install

# ============================================
# TESTING
# ============================================
test-api: ## Run backend tests
	cd backend && go test -v ./...

test-api-cover: ## Run backend tests with coverage
	cd backend && go test -v -coverprofile=coverage.out ./... && go tool cover -html=coverage.out -o coverage.html

test-web: ## Run frontend tests
	cd frontend && npm test

lint-api: ## Run Go linter
	cd backend && golangci-lint run ./...

lint-web: ## Run frontend linter
	cd frontend && npm run lint

format-web: ## Format frontend code with Prettier
	cd frontend && npm run format

format-check: ## Check frontend formatting
	cd frontend && npm run format:check

ci: ## Run all CI checks (lint + test)
	@echo "🔍 Running CI checks..."
	@echo ""
	@echo "=== Backend Tests ==="
	cd backend && go test -count=1 ./...
	@echo ""
	@echo "=== Frontend Tests ==="
	cd frontend && npx vitest run
	@echo ""
	@echo "=== Frontend Lint ==="
	cd frontend && npm run lint
	@echo ""
	@echo "✅ All CI checks passed!"

# ============================================
# PRODUCTION
# ============================================
build-prod: ## Build for production
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml build
	@echo "✅ Production build complete!"

deploy-prod: ## Deploy to production
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
	@echo "✅ Production deployed!"

stop-prod: ## Stop production services
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
	@echo "✅ Production stopped!"

logs-prod: ## View production logs
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

backup: ## Backup databases
	@bash scripts/backup.sh manual

backup-daily: ## Run daily backup
	@bash scripts/backup.sh daily

restore: ## Restore database (usage: make restore DB=postgres FILE=backups/...)
	@bash scripts/restore.sh $(DB) $(FILE)

# ============================================
# UTILITIES
# ============================================
health: ## Check health of all services
	@bash scripts/health-check.sh

health-json: ## Check health (JSON output)
	@bash scripts/health-check.sh --json

setup: ## Initial project setup (copy env files)
	@echo "📋 Setting up environment files..."
	@test -f .env || cp .env.example .env && echo "  ✅ Created .env"
	@test -f backend/.env || cp backend/.env.example backend/.env && echo "  ✅ Created backend/.env"
	@test -f frontend/.env.local || cp frontend/.env.example frontend/.env.local && echo "  ✅ Created frontend/.env.local"
	@echo ""
	@echo "✅ Setup complete! Run 'make up' to start services."
