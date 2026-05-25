# 🚀 Lịch Số

> **Lịch Số** — Go Fiber + Next.js + PostgreSQL + MongoDB + Redis

## 📋 Tổng Quan

Lịch Số là dự án được phát triển dựa trên nền tảng Zplus Base, sử dụng kiến trúc monorepo với Clean Architecture pattern. Dự án bao gồm hệ thống authentication (JWT), RBAC (Role-Based Access Control), user management, và admin dashboard.

## 🏗️ Tech Stack

| Layer | Công nghệ |
|-------|-----------|
| **Backend** | Go 1.22+, Fiber v2, GORM, Zap Logger |
| **Frontend** | Next.js 14+, TypeScript, Tailwind CSS, shadcn/ui |
| **Database** | PostgreSQL 16, MongoDB 7, Redis 7 |
| **Infrastructure** | Docker, Docker Compose, Nginx |

## 📁 Cấu Trúc Dự Án

```
Zplus_Lichso/
├── backend/                 # Go Fiber API Server
│   ├── cmd/                 # Application entry points
│   ├── internal/            # Private application code
│   ├── migrations/          # Database migrations
│   ├── go.mod
│   └── Dockerfile
├── frontend/                # Next.js Application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── Dockerfile
├── docker/                  # Docker configurations
│   ├── postgres/            # PostgreSQL init scripts
│   ├── mongodb/             # MongoDB init scripts
│   ├── redis/               # Redis configuration
│   └── nginx/               # Nginx reverse proxy
├── docs/                    # Documentation
├── scripts/                 # Utility scripts
├── docker-compose.yml       # Docker orchestration
├── Makefile                 # Common commands
└── README.md
```

## 🚀 Quick Start

### Yêu cầu

- [Docker](https://www.docker.com/) >= 24.0
- [Docker Compose](https://docs.docker.com/compose/) >= 2.0
- [Go](https://golang.org/) >= 1.22 (cho dev local)
- [Node.js](https://nodejs.org/) >= 20 (cho dev local)
- [pnpm](https://pnpm.io/) >= 8 (cho dev local)

### 1. Clone & Setup

```bash
git clone <repository-url> Zplus_Lichso
cd Zplus_Lichso

# Copy environment files
cp .env.example .env
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env.local
```

### 2. Start với Docker

```bash
# Start tất cả services
make up

# Xem logs
make logs

# Chạy migrations
make migrate

# Seed data mặc định
make seed
```

### 3. Development (Local)

```bash
# Backend (Go Fiber + Air hot reload)
make dev-api

# Frontend (Next.js dev server)
make dev-web
```

### 4. Truy cập

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:3001 |
| **Backend API** | http://localhost:8080 |
| **Nginx (Proxy)** | http://localhost |

## 📖 Documentation

Chi tiết documentation nằm trong thư mục `docs/`:

- [01 - Architecture](docs/01-architecture.md)
- [02 - Getting Started](docs/02-getting-started.md)
- [03 - Backend Structure](docs/03-backend-structure.md)
- [04 - Frontend Structure](docs/04-frontend-structure.md)
- [05 - Database Design](docs/05-database-design.md)
- [06 - Auth System](docs/06-auth-system.md)
- [07 - API Reference](docs/07-api-reference.md)
- [08 - Docker Deployment](docs/08-docker-deployment.md)
- [09 - Coding Conventions](docs/09-coding-conventions.md)
- [10 - Environment Variables](docs/10-environment-variables.md)

## 🔑 Default Accounts

| Role | Email | Password |
|------|-------|----------|
| Super Admin | admin@zplus.dev | Admin@123 |
| Editor | editor@zplus.dev | Editor@123 |
| Viewer | viewer@zplus.dev | Viewer@123 |

> ⚠️ **Đổi mật khẩu mặc định trước khi deploy production!**

## 📝 Makefile Commands

```bash
make help          # Hiển thị tất cả commands
make up            # Start all services
make down          # Stop all services
make restart       # Restart all services
make rebuild       # Rebuild & start
make logs          # View all logs
make clean         # Stop & remove all data
make migrate       # Run migrations
make seed          # Seed default data
make dev-api       # Run backend dev
make dev-web       # Run frontend dev
make test-api      # Run backend tests
make test-web      # Run frontend tests
```

## 📄 License

MIT License - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.
