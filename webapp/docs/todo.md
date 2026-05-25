# 📋 LichSo V2 - Todo List

> Tracking tất cả tasks cho lichso.vn version 2.0
> Cập nhật: Tháng 7, 2025

---

## Phase 6: Content Infrastructure (Backend) ✅

### 6.1 Database Migrations | Phase 7: Admin Panel Frontend | ✅ Hoàn thành | 100% |
- [x] `000011` - Article Categories table (up + down)
- [x] `000012` - Article Tags table (up + down)
- [x] `000013` - Articles table with FTS index (up + down)
- [x] `000014` - Article-Tag relations table (up + down)
- [x] `000015` - Quotes table (up + down)
- [x] `000016` - Famous People table (up + down)
- [x] `000017` - Events table (up + down)
- [x] `000018` - Folk Festivals table (up + down)

### 6.2 GORM Models ✅
- [x] `models/article.go` - Article + ArticleTagRelation
- [x] `models/article_category.go` - ArticleCategory (self-referencing tree)
- [x] `models/article_tag.go` - ArticleTag (many2many with articles)
- [x] `models/quote.go` - Quote (pq.StringArray for tags)
- [x] `models/famous_person.go` - FamousPerson
- [x] `models/event.go` - Event
- [x] `models/folk_festival.go` - FolkFestival

### 6.3 DTOs (Request/Response) ✅
- [x] `dto/article_dto.go` - Article, Category, Tag DTOs
- [x] `dto/quote_dto.go` - Quote DTOs
- [x] `dto/famous_people_dto.go` - Famous Person DTOs
- [x] `dto/event_dto.go` - Event DTOs
- [x] `dto/festival_dto.go` - Folk Festival DTOs

### 6.4 Repositories ✅
- [x] `repositories/article_repo.go` - Article + Category + Tag repos
- [x] `repositories/quote_repo.go` - Quote repo (GetByDayOfYear, GetRandom)
- [x] `repositories/famous_person_repo.go` - Famous Person repo (GetByBirthday)
- [x] `repositories/event_repo.go` - Event repo (GetByDate)
- [x] `repositories/folk_festival_repo.go` - Folk Festival repo (GetByLunarDate, GetBySolarDate)

### 6.5 Services ✅
- [x] `services/article_service.go` - Article + Category + Tag services
- [x] `services/quote_service.go` - Quote service (GetToday, GetRandom)
- [x] `services/famous_person_service.go` - Famous Person service
- [x] `services/event_service.go` - Event service
- [x] `services/folk_festival_service.go` - Folk Festival service

### 6.6 Handlers ✅
- [x] `handlers/article_handler.go` - Article + Category + Tag handlers
- [x] `handlers/quote_handler.go` - Quote handler
- [x] `handlers/famous_person_handler.go` - Famous Person handler
- [x] `handlers/event_handler.go` - Event handler
- [x] `handlers/folk_festival_handler.go` - Folk Festival handler

### 6.7 Routes ✅
- [x] `routes/content_routes.go` - Public content routes (no auth)
- [x] `routes/admin_content_routes.go` - Admin content routes (auth + permissions)

### 6.8 Utilities ✅
- [x] `utils/slug.go` - Vietnamese-aware slug generator + reading time calculator

### 6.9 Wiring ✅
- [x] Wire all new components into `cmd/server/main.go`
- [x] Project compiles with `go build ./...` ✅

---

## Phase 7: Admin Panel (Frontend) �

### 7.1 Admin Layout & Navigation
- [x] Admin sidebar with content management sections
- [x] Dashboard content stats widget (total articles, quotes, events, etc.)
- [x] Breadcrumb navigation labels for all content routes

### 7.2 Article Management
- [x] Article list page (DataTable with search, filter by status/category)
- [x] Article create/edit page (form with category, tags, SEO fields)
- [x] Article preview mode
- [x] Article category management (inline CRUD with Dialog)
- [x] Article tag management (inline CRUD with Dialog)
- [x] Featured image upload (file upload integration)
- [x] SEO fields (meta title, description)
- [x] Schedule publishing
- [x] Rich text editor (TipTap) integration

### 7.3 Quote Management
- [x] Quote list page (DataTable with search, filter by is_active)
- [x] Quote create/edit form
- [x] Assign quote to day of year
- [x] Bulk import quotes (CSV)

### 7.4 Famous People Management
- [x] Famous Person list page (filter by is_vietnamese)
- [x] Famous Person create/edit form
- [x] Link to article
- [x] Image upload

### 7.5 Event Management
- [x] Event list page (filter by type, importance)
- [x] Event create/edit form
- [x] Link to article
- [x] Bulk import events

### 7.6 Folk Festival Management
- [x] Festival list page (filter by type)
- [x] Festival create/edit form
- [x] Gallery image management
- [x] Link to article

---

## Phase 8: Public Content Pages (Frontend) ✅

### 8.1 Calendar Integration
- [x] Show "Quote of the Day" on calendar view (QuoteOfTheDayWidget)
- [x] Show famous birthdays on date cells (CalendarDateContent)
- [x] Show events/national days on date cells (CalendarDateContent)
- [x] Show folk festivals on date cells (lunar/solar) (CalendarDateContent)
- [x] Date detail panel with all content for selected date (DateContentPanel)

### 8.2 Content Pages
- [x] `/bai-viet` - Article listing page
- [x] `/bai-viet/[slug]` - Article detail page
- [x] `/cau-noi-noi-tieng` - Famous quotes page
- [x] `/nguoi-noi-tieng` - Famous people page
- [x] `/nguoi-noi-tieng/[id]` - Famous person detail
- [x] `/su-kien` - Events page
- [x] `/su-kien/[slug]` - Event detail
- [x] `/le-hoi` - Folk festivals page
- [x] `/le-hoi/[slug]` - Festival detail
- [x] `/ngay-nay-trong-lich-su` - "Today in History" page

### 8.3 SEO & Metadata
- [x] Dynamic `<title>` and `<meta>` tags per page
- [x] Open Graph images
- [x] JSON-LD structured data (via Next.js metadata)
- [x] Sitemap.xml generation
- [x] robots.txt

---

## Phase 9: Data Seeding & Content ✅

### 9.1 Seed Data Scripts
- [x] Seed article categories (Lịch sử, Văn hóa, Phong thủy, etc.)
- [x] Seed initial quotes (~75 Vietnamese & international)
- [x] Seed famous Vietnamese historical figures
- [x] Seed famous international figures
- [x] Seed Vietnamese national holidays & events
- [x] Seed world days (UN, UNESCO, etc.)
- [x] Seed Vietnamese folk festivals (Tết Nguyên Đán, Trung Thu, etc.)

### 9.2 Content Permissions
- [x] Add `content.read`, `content.create`, `content.update`, `content.delete`, `content.publish` permissions to seed
- [x] Assign content permissions to admin/editor roles

---

## Phase 10: Testing & Optimization 🔲

### 10.1 Backend Tests
- [ ] Repository unit tests
- [ ] Service unit tests
- [ ] Handler integration tests
- [ ] API endpoint tests (content CRUD)

### 10.2 Frontend Tests
- [ ] Component tests for content pages
- [ ] API service mock tests
- [ ] E2E tests for critical flows

### 10.3 Performance
- [ ] Redis caching for popular content (quote of day, featured articles)
- [ ] CDN for images
- [ ] Database query optimization
- [ ] Lazy loading for article content

---

## Tổng quan tiến độ

| Phase | Trạng thái | Tiến độ |
|-------|-----------|---------|
| Phase 6: Backend Infrastructure | ✅ Hoàn thành | 100% |
| Phase 7: Admin Panel Frontend | � Đang thực hiện | ~75% |
| Phase 8: Public Content Pages | ✅ Hoàn thành | 100% |
| Phase 9: Data Seeding | ✅ Hoàn thành | 100% |
| Phase 10: Testing | 🔲 Chưa bắt đầu | 0% |

---

## Files Created (Phase 6)

### Migrations (16 files)
```
backend/migrations/000011_create_article_categories_table.{up,down}.sql
backend/migrations/000012_create_article_tags_table.{up,down}.sql
backend/migrations/000013_create_articles_table.{up,down}.sql
backend/migrations/000014_create_article_tag_relations_table.{up,down}.sql
backend/migrations/000015_create_quotes_table.{up,down}.sql
backend/migrations/000016_create_famous_people_table.{up,down}.sql
backend/migrations/000017_create_events_table.{up,down}.sql
backend/migrations/000018_create_folk_festivals_table.{up,down}.sql
```

### Models (7 files)
```
backend/internal/models/article.go
backend/internal/models/article_category.go
backend/internal/models/article_tag.go
backend/internal/models/quote.go
backend/internal/models/famous_person.go
backend/internal/models/event.go
backend/internal/models/folk_festival.go
```

### DTOs (5 files)
```
backend/internal/dto/article_dto.go
backend/internal/dto/quote_dto.go
backend/internal/dto/famous_people_dto.go
backend/internal/dto/event_dto.go
backend/internal/dto/festival_dto.go
```

### Repositories (5 files)
```
backend/internal/repositories/article_repo.go
backend/internal/repositories/quote_repo.go
backend/internal/repositories/famous_person_repo.go
backend/internal/repositories/event_repo.go
backend/internal/repositories/folk_festival_repo.go
```

### Services (4 files)
```
backend/internal/services/article_service.go
backend/internal/services/quote_service.go
backend/internal/services/famous_person_service.go
backend/internal/services/event_service.go
backend/internal/services/folk_festival_service.go
```

### Handlers (5 files)
```
backend/internal/handlers/article_handler.go
backend/internal/handlers/quote_handler.go
backend/internal/handlers/famous_person_handler.go
backend/internal/handlers/event_handler.go
backend/internal/handlers/folk_festival_handler.go
```

### Routes (2 files)
```
backend/internal/routes/content_routes.go
backend/internal/routes/admin_content_routes.go
```

### Utilities (1 file)
```
backend/internal/utils/slug.go
```

### Modified (1 file)
```
backend/cmd/server/main.go (wiring V2 components)
```

**Tổng: 46 files mới + 1 file sửa đổi**

---

## Files Created (Phase 7)

### TypeScript Types (5 files)
```
frontend/src/types/article.ts
frontend/src/types/quote.ts
frontend/src/types/famousPerson.ts
frontend/src/types/event.ts
frontend/src/types/festival.ts
```

### API Services (5 files)
```
frontend/src/services/articleService.ts
frontend/src/services/quoteService.ts
frontend/src/services/famousPersonService.ts
frontend/src/services/eventService.ts
frontend/src/services/festivalService.ts
```

### React Query Hooks (6 files)
```
frontend/src/hooks/useArticles.ts
frontend/src/hooks/useQuotes.ts
frontend/src/hooks/useFamousPeople.ts
frontend/src/hooks/useEvents.ts
frontend/src/hooks/useFolkFestivals.ts
frontend/src/hooks/useContentStats.ts
```

### Components (10 files)
```
frontend/src/components/articles/ArticleTable.tsx
frontend/src/components/articles/ArticleForm.tsx
frontend/src/components/quotes/QuoteTable.tsx
frontend/src/components/quotes/QuoteForm.tsx
frontend/src/components/famous-people/FamousPersonTable.tsx
frontend/src/components/famous-people/FamousPersonForm.tsx
frontend/src/components/events/EventTable.tsx
frontend/src/components/events/EventForm.tsx
frontend/src/components/festivals/FolkFestivalTable.tsx
frontend/src/components/festivals/FolkFestivalForm.tsx
```

### Admin Pages (17 files)
```
frontend/src/app/admin/articles/page.tsx
frontend/src/app/admin/articles/create/page.tsx
frontend/src/app/admin/articles/[id]/edit/page.tsx
frontend/src/app/admin/categories/page.tsx
frontend/src/app/admin/tags/page.tsx
frontend/src/app/admin/quotes/page.tsx
frontend/src/app/admin/quotes/create/page.tsx
frontend/src/app/admin/quotes/[id]/edit/page.tsx
frontend/src/app/admin/famous-people/page.tsx
frontend/src/app/admin/famous-people/create/page.tsx
frontend/src/app/admin/famous-people/[id]/edit/page.tsx
frontend/src/app/admin/events/page.tsx
frontend/src/app/admin/events/create/page.tsx
frontend/src/app/admin/events/[id]/edit/page.tsx
frontend/src/app/admin/festivals/page.tsx
frontend/src/app/admin/festivals/create/page.tsx
frontend/src/app/admin/festivals/[id]/edit/page.tsx
```

### Modified (4 files)
```
frontend/src/lib/constants.ts (new content routes)
frontend/src/components/layouts/AdminSidebar.tsx (content nav items)
frontend/src/components/layouts/AdminSidebarMobile.tsx (content nav items)
frontend/src/components/shared/BreadcrumbNav.tsx (content route labels)
frontend/src/app/admin/page.tsx (content stats dashboard widget)
```

**Tổng Phase 7: 43 files mới + 5 files sửa đổi**

## Files Created (Phase 9)

### Seed Data (1 file)
```
backend/cmd/seed/content_seed.go
```

### Modified (1 file)
```
backend/cmd/seed/main.go (added content seed commands, updated fresh seed, updated usage)
```

### Seed Data Summary
- **8 Article Categories**: Lịch sử, Văn hóa, Phong thủy, Lễ hội, Nhân vật lịch sử, Âm lịch, Tử vi, Tin tức
- **~75 Quotes**: Tục ngữ Việt Nam, Ca dao, Hồ Chí Minh, Nguyễn Trãi, Einstein, Gandhi, Jobs, Mandela, Khổng Tử...
- **22 Famous People**: 12 nhân vật Việt Nam (HCM, Võ Nguyên Giáp, Nguyễn Du, Hai Bà Trưng...) + 10 quốc tế (Einstein, Gandhi, da Vinci, Marie Curie...)
- **27 Events**: 16 ngày lễ Việt Nam + 8 ngày quốc tế (UN/UNESCO) + 3 sự kiện lịch sử
- **14 Folk Festivals**: Tết Nguyên Đán, Trung Thu, Đoan Ngọ, Vu Lan, Chùa Hương, Hội Lim, Đền Trần...
- **5 Content Permissions**: content.read, content.create, content.update, content.delete, content.publish
- **Role Assignments**: SuperAdmin/Admin (full), Editor (read/create/update/publish), Viewer (read)

### New Seed Commands
```
make seed                          # Seed everything including content
make seed-fresh                    # Clean all & re-seed
go run cmd/seed/main.go content    # Seed all content data
go run cmd/seed/main.go categories # Seed categories only
go run cmd/seed/main.go quotes     # Seed quotes only
go run cmd/seed/main.go famous-people
go run cmd/seed/main.go events
go run cmd/seed/main.go festivals
go run cmd/seed/main.go content-permissions
```

**Tổng Phase 9: 1 file mới + 1 file sửa đổi**
