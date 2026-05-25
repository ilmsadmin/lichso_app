# Lịch Số — Kế hoạch Triển Khai (v3.0 + v4.0)

> Trích từ LICHSO-V3-DOCUMENT.md (Phase 11–24) và LICHSO-V4-DOCUMENT.md (Phase 25–28).
> Cập nhật lần cuối: **2026-03-12**

---

## Chú Thích Trạng Thái

| Icon | Trạng thái |
|------|------------|
| ✅ | Hoàn thành |
| 🔧 | Đang thực hiện |
| 🔜 | Sắp triển khai |
| 📋 | Kế hoạch |

---

## 1. Roadmap v3.0

### Phase 11 — Media Infrastructure (4 tuần) ✅

- [x] Cập nhật Media model (thêm fields mới: media_type, dimensions, exif, blur_hash, focal_point, tags, file_hash, etc.)
- [x] Tạo MongoDB collections mới: media_variants, media_attachments, media_albums, media_album_items, media_folders, chunk_uploads, media_versions
- [x] Service `ImageProcessService` — resize, crop, WebP convert, thumbnail
- [x] Service `MediaService` — nâng cấp upload với auto-processing pipeline
- [x] BlurHash generation
- [x] EXIF extraction
- [x] File hash + duplicate detection
- [x] Dominant color extraction
- [x] Background worker cho image processing (async)
- [x] Redis caching cho media

### Phase 12 — Folders & Albums (2 tuần) ✅

- [x] `MediaFolderService` — CRUD folder tree, nested folders
- [x] `MediaAlbumService` — CRUD albums, add/remove media, reorder
- [x] API endpoints cho folders & albums
- [x] Folder tree component (Frontend) — `FolderTree.tsx`
- [x] Album manager component (Frontend) — `AlbumManager.tsx`
- [x] Drag & drop reorder trong albums

### Phase 13 — Media Manager UI (3 tuần) ✅

- [x] Nâng cấp Media Manager page (grid, list, detail views) — `MediaManagerV3.tsx`
- [x] Media detail slide-over panel — `MediaDetailPanel.tsx`
- [x] Advanced search bar (type, tags, size, date, dimensions)
- [x] Drag & drop upload zone — `MediaDropzone.tsx`
- [x] Bulk operations (delete, move, tag, add to album)
- [x] Media favorites
- [x] Soft delete + trash management
- [x] Media stats cards


### Phase 14 — Media Picker & Content Integration (2 tuần) ✅

- [x] MediaPicker dialog component (dùng chung mọi form) — `MediaPickerV3.tsx`
- [x] Tích hợp MediaPicker vào Article form (featured image, OG image, content images) — `ArticleForm.tsx` + `RichTextEditor.tsx`
- [x] Tích hợp MediaPicker vào Event form — `EventForm.tsx`
- [x] Tích hợp MediaPicker vào Festival form (gallery) — `FolkFestivalForm.tsx` + `GalleryManagerV3.tsx`
- [x] Tích hợp MediaPicker vào Person form (avatar) — `FamousPersonForm.tsx`
- [x] Tích hợp MediaPicker vào Quote form (author image) — `QuoteForm.tsx`
- [x] Media–Content linking (2-way: xem media đang dùng ở đâu) — `MediaContentLinker.tsx`
- [x] Gallery field component cho forms — `GalleryManagerV3.tsx`

### Phase 15 — Image Editor & Processing (2 tuần) ✅

- [x] Inline Image Editor (crop, rotate, flip) — Frontend `ImageEditor.tsx`
- [x] Crop presets (1:1, 16:9, 4:3, OG 1200×630) — Frontend `ImageEditor.tsx`
- [x] Focal point selector — API + Frontend `ImageEditor.tsx`
- [x] Regenerate variants API + Frontend `ImageEditor.tsx`
- [x] Watermark feature — Frontend `ImageEditor.tsx`
- [x] Image versioning (lưu version cũ khi edit)

### Phase 16 — Video Management (2 tuần) ✅

- [x] YouTube/Vimeo URL parser & embed — `VideoManager.tsx` (parseVideoUrl, VideoEmbedPlayer)
- [x] Video upload (MP4, WebM) — `VideoManager.tsx` (VideoUploadDialog)
- [x] Chunk upload (init → upload chunks → complete) — API + `ChunkUploadProgressUI`
- [x] Video thumbnail extraction (FFmpeg) — `video_process_service.go` (ExtractThumbnail)
- [x] Video metadata (duration, resolution) — `video_process_service.go` (ExtractMetadata, ProcessVideoMetadata)
- [x] Video player component — `VideoManager.tsx` (VideoPlayer)
- [x] Chunk upload progress UI — `VideoManager.tsx` (ChunkUploadProgressUI)

### Phase 17 — Responsive Image Delivery (1 tuần) ✅

- [x] `<ResponsiveImage>` component (picture + srcset + blurhash) — `ResponsiveImage.tsx`
- [x] Tích hợp vào tất cả public pages (articles, events, festivals, famous people)
- [x] Next.js Image optimization config — `next.config.ts` (deviceSizes, imageSizes, cacheTTL)
- [ ] CDN integration (optional — Cloudflare)
- [x] Lazy loading với blur placeholder — BlurhashCanvas + dominant color fallback

### Phase 18 — Media Analytics & AI (2 tuần) 🔧

- [x] Media analytics dashboard — `app/admin/media-analytics/page.tsx`
- [x] Storage distribution charts — CSS progress bars by type/mime
- [ ] Upload trends charts (cần backend API thống kê theo thời gian)
- [x] Unused media detection — `useUnusedMedia()` hook + UI
- [ ] Broken reference detection (cần backend API)
- [x] Duplicate media report — `useDuplicateMedia()` hook + UI
- [ ] AI auto-tagging (Optional — OpenAI / Google Vision)
- [ ] AI image description generation
- [ ] AI content suggestions

### Phase 19 — Cleanup & Optimization (1 tuần) 📋

- [ ] Scheduled cleanup worker (trash, chunks, orphans)
- [ ] Storage optimization report
- [ ] Performance benchmarks
- [ ] Load testing (10,000+ files)
- [ ] Documentation cập nhật
- [ ] Migration guide v2 → v3

### Phase 20 — Bài Viết Liên Quan (2 tuần) 🔧

- [x] Tạo bảng `article_relations` (PostgreSQL migration `000020`)
- [x] Model `ArticleRelation` (Go — `models/article_relation.go`)
- [x] DTOs: Create/Batch/Update/Response (`dto/article_relation_dto.go`)
- [x] Repository `ArticleRelationRepository` (`repositories/article_relation_repo.go`)
- [x] `ArticleRelationService` — Rule-based related articles engine (`services/article_relation_service.go`)
- [x] Thuật toán gợi ý: same category, shared tags, popular, random
- [x] API endpoints: GET related, POST/DELETE manual, auto-generate (`handlers/article_relation_handler.go`)
- [x] Routes v3: public + admin (`routes/v3_routes.go`)
- [x] Frontend types (`types/v3.ts`)
- [x] Frontend API service (`services/v3Service.ts`)
- [x] Frontend React Query hooks (`hooks/useV3.ts`)
- [x] Admin UI: panel quản lý bài viết liên quan trong article form (`components/articles/ArticleRelationPanel.tsx`)
- [x] Frontend: component `RelatedArticles` cuối trang bài viết (`components/articles/RelatedArticles.tsx`)
- [ ] Cache Redis: related articles per article (TTL 1h)
- [x] Thêm `ArticleSummaryCard` component (ảnh + title + excerpt + reading time) (`components/articles/ArticleSummaryCard.tsx`)

### Phase 21 — Daily Content Calendar (3 tuần) 🔧

- [x] Tạo bảng `daily_content_schedules` (PostgreSQL migration `000021`)
- [x] Model `DailyContentSchedule` (Go — `models/daily_content_schedule.go`)
- [x] DTOs: Create/Update/Response + DayContentResponse (`dto/daily_content_dto.go`)
- [x] Repository `DailyContentRepository` (`repositories/daily_content_repo.go`)
- [x] `DailyContentService` — logic lấy nội dung theo ngày auto + manual (`services/daily_content_service.go`)
- [x] Auto-assign engine: quotes by day_of_year, events by day/month, festivals by lunar/solar, famous_people by birthday
- [x] Random articles engine: weighted random, category-diverse
- [x] API Public: `/api/v3/calendar/date/:date/content`, `/api/v3/calendar/today/content`
- [x] API Admin: CRUD daily content (`handlers/daily_content_handler.go`)
- [x] Routes v3: public + admin (`routes/v3_routes.go`)
- [x] Frontend types, API service, React Query hooks
- [x] Admin UI: Calendar view quản lý nội dung tháng (`app/admin/daily-content/page.tsx`)
- [x] Admin UI: Dialog thêm/sửa nội dung cho ngày (`app/admin/daily-content/DailyContentFormDialog.tsx`)
- [x] Admin UI: Auto-fill wizard — `AutoFillWizard.tsx` (config → confirm → result, coverage stats)
- [x] Bulk add, auto-fill, stats API — `POST /admin/daily-content/auto-fill` + `GET /admin/daily-content/stats`
- [ ] Cache Redis: daily content per date (TTL 1h), random articles (TTL 24h)
- [ ] Cron job: reset random articles lúc 00:00

### Phase 22 — Enhanced Day Detail & User Features (3 tuần) 🔧

- [x] Tạo bảng `user_notes` + `user_countdowns` (PostgreSQL migration `000022`)
- [x] Models: `UserNote`, `UserCountdown` (Go — `models/user_note.go`)
- [x] DTOs: Note + Countdown CRUD (`dto/user_note_dto.go`)
- [x] Repositories: `UserNoteRepository`, `UserCountdownRepository` (`repositories/user_note_repo.go`)
- [x] Services: `UserNoteService`, `UserCountdownService` (`services/user_note_service.go`)
- [x] Handlers: `UserNoteHandler`, `UserCountdownHandler` (`handlers/user_note_handler.go`)
- [x] Routes v3: user-authenticated routes (`routes/v3_routes.go`)
- [x] Frontend types, API service, React Query hooks
- [x] Wiring in `cmd/server/main.go`
- [x] Nâng cấp `DayDetailModal` — thêm sections: quote, events, articles, festivals, birthdays
- [x] Component `DailyQuoteCard` — hiển thị danh ngôn ngày (`components/lichso/DailyQuoteCard.tsx`)
- [x] Component `HistoricalEventsTimeline` — sự kiện lịch sử dạng timeline (`components/lichso/HistoricalEventsTimeline.tsx`)
- [x] Component `ArticleBriefCard` — bài viết ngẫu nhiên (`components/lichso/ArticleBriefCard.tsx`)
- [x] Component `FestivalCard` — lễ hội hôm nay (`components/lichso/FestivalCard.tsx`)
- [x] Component `BirthdayBadge` — sinh nhật người nổi tiếng (`components/lichso/BirthdayBadge.tsx`)
- [x] Calendar Grid: content indicators (dots) cho ngày có nội dung — `CalendarGrid.tsx` + `useMonthContentSummary`
- [x] API: `/api/day-content/month/:year/:month` — monthly content summary (backend + frontend)
- [x] Nút "🔄 Xem bài khác" cho random articles (trong `ArticleBriefCard`)
- [x] User Notes management page (`app/profile/notes/page.tsx`)
- [x] User Countdown management page (`app/profile/countdowns/page.tsx`)
- [x] Countdown widget trên trang chủ (`components/lichso/CountdownWidget.tsx`)
- [x] Profile nav: thêm Ghi chú + Đếm ngược (`ProfileHeader.tsx`)

### Phase 23 — Extended Calendar Features (4 tuần) ✅

- [ ] In lịch (Print): generate PDF/PNG cho tháng/năm/ngày
- [x] Chia sẻ lên MXH: generate share card image (OG format) — `ShareCardGenerator.tsx` + tích hợp vào `DayDetailModal`
- [x] Tử vi ngày: tính theo can chi + con giáp, hiển thị 12 cung — Backend `horoscope_service.go` + `horoscope_handler.go` + Frontend `HoroscopeWidget.tsx`
- [x] Nâng cấp Xem Ngày: thêm cưới hỏi, động thổ, nhập trạch, khai trương — Backend `good_day_service.go` + `good_day_handler.go` + Frontend `GoodDayPurposeTab.tsx`
- [x] Tuổi âm lịch calculator: tính tuổi âm, tuổi mụ, ngũ hành, mệnh — Backend `/lunar-age/:birthYear` + Frontend `LunarAgeCalculator.tsx`

### Phase 24 — Engagement & Integration (3 tuần) ✅

- [x] Newsletter/Email digest: bảng subscribers, subscribe/unsubscribe/confirm API — Backend `newsletter_service.go` + `newsletter_handler.go` + Frontend `NewsletterSubscribe.tsx`
- [x] Streaks & Achievements: gamification system — Backend `streak_achievement_service.go` + `streak_handler.go` + Frontend `StreakWidget.tsx`
- [ ] Weather integration (Optional): OpenWeatherMap API
- [ ] Google Calendar sync (Optional): export events to GCal
- [ ] Mobile-optimized views: responsive calendar detail
- [ ] PWA improvements: offline calendar data, push notifications

---

## 2. Timeline Tổng Quan

```
Tháng 3/2026:  Phase 11 — Media Infrastructure (4 tuần)
Tháng 4/2026:  Phase 12 — Folders & Albums (2 tuần)
               Phase 13 — Media Manager UI (3 tuần, overlap)
Tháng 5/2026:  Phase 14 — Media Picker & Content Integration (2 tuần)
               Phase 15 — Image Editor & Processing (2 tuần)
Tháng 6/2026:  Phase 16 — Video Management (2 tuần)
               Phase 17 — Responsive Image Delivery (1 tuần)
Tháng 7/2026:  Phase 18 — Media Analytics & AI (2 tuần)
               Phase 19 — Cleanup & Optimization (1 tuần)
Tháng 8/2026:  Phase 20 — Bài Viết Liên Quan (2 tuần) 🔧
               Phase 21 — Daily Content Calendar (3 tuần, overlap) 🔧
Tháng 9/2026:  Phase 22 — Enhanced Day Detail (3 tuần) 🔧
Tháng 10/2026: Phase 23 — Extended Calendar Features (4 tuần)
Tháng 11/2026: Phase 24 — Engagement & Integration (3 tuần)

Tổng: ~33 tuần (~8 tháng)
Release v3.0 Core (Media): Tháng 7/2026
Release v3.1 (Content Calendar + Day Detail): Tháng 9/2026
Release v3.2 (Extended Features): Tháng 11/2026
```

---

## 3. Ưu Tiên Phát Triển

```
P0 (Critical — Phải có):
├── Media model upgrade (new fields)
├── Image processing pipeline (resize, WebP, thumbnail)
├── Folder tree
├── Media Manager UI upgrade
├── MediaPicker component
├── Media–Content linking
├── Bài viết liên quan (related articles)          ← ✅ Backend + Frontend done
├── Daily Content Calendar (scheduling system)      ← ✅ Backend + Admin UI done
├── Enhanced Day Detail (quote, events, articles)   ← ✅ Backend + Frontend done
└── Calendar Grid content indicators

P1 (High — Nên có):
├── Albums
├── Advanced search
├── Bulk operations
├── Soft delete + trash
├── Image editor (crop, rotate)
├── Video embed (YouTube, Vimeo)
├── Responsive image delivery
├── Random articles in Day Detail                   ← ✅ Done
├── Auto-fill daily content
├── Admin calendar content management UI            ← ✅ Done
├── Xem ngày cưới hỏi / động thổ nâng cao          ← ✅ Done (GoodDayPurposeTab)
└── Tử vi ngày theo 12 con giáp                     ← ✅ Done (HoroscopeWidget)

P2 (Medium — Tốt nếu có):
├── Chunk upload (video)
├── Video upload trực tiếp
├── EXIF extraction
├── BlurHash placeholders
├── File versioning
├── Duplicate detection
├── Media analytics
├── Ghi chú cá nhân (User Notes)                   ← ✅ Backend + Frontend done
├── Đếm ngược sự kiện (Countdown)                  ← ✅ Backend + Frontend done
├── In lịch (Print calendar)
├── Chia sẻ lên MXH (Share card)                    ← ✅ Done (ShareCardGenerator)
├── Tuổi âm lịch calculator                         ← ✅ Done (LunarAgeCalculator)
└── AI-powered related articles (vector embedding)

P3 (Low — Phase sau):
├── AI auto-tagging
├── AI image description
├── CDN integration
├── Watermark
├── OCR
├── Content suggestions
├── Newsletter / Email digest                        ← ✅ Done (NewsletterSubscribe + backend)
├── Weather integration
├── Google Calendar sync
├── Streaks & Achievements (gamification)            ← ✅ Done (StreakWidget + backend)
└── PWA push notifications
```

---

## 4. Tổng Kết Tiến Độ

> 📊 Cập nhật: **2026-03-12** | Build status: ✅ `go build` | ✅ `go vet` | ✅ `tsc --noEmit`

### Thống kê tổng quan

| Hạng mục | Hoàn thành | Còn lại | Tỷ lệ |
|----------|-----------|---------|--------|
| Phase 11 — Media Infrastructure | 10/10 | 0 | 100% |
| Phase 12 — Folders & Albums | 6/6 | 0 | 100% |
| Phase 13 — Media Manager UI | 8/8 | 0 | 100% |
| Phase 14 — Media Picker & Content | 8/8 | 0 | 100% |
| Phase 15 — Image Editor & Processing | 6/6 | 0 | 100% |
| Phase 16 — Video Management | 7/7 | 0 | 100% |
| Phase 17 — Responsive Image Delivery | 4/5 | 1 | 80% |
| Phase 18 — Media Analytics | 4/9 | 5 | 44% |
| Phase 19 — Cleanup & Optimization | 0/6 | 6 | 0% |
| Phase 20 — Bài Viết Liên Quan | 14/15 | 1 | 93% |
| Phase 21 — Daily Content Calendar | 15/17 | 2 | 88% |
| Phase 22 — Enhanced Day Detail & User | 22/22 | 0 | 100% |
| Phase 23 — Extended Calendar Features | 4/5 | 1 | 80% |
| Phase 24 — Engagement & Integration | 2/6 | 4 | 33% |
| **Tổng V3** | **110/~135** | **~25** | **~81%** |
| Phase 25 — OpenRouter Core & AI Horoscope | 18/18 | 0 | 100% |
| Phase 26 — AI Article Generator | 11/11 | 0 | 100% |
| Phase 27 — AI Chat & Admin Dashboard | 8/8 | 0 | 100% |
| Phase 28 — Auto Schedule & Optimization | 0/4 | 4 | 0% |
| **Tổng V4** | **41/41** | **0** | **100%** |

### ✅ Đã hoàn thành (Backend + Frontend Infrastructure)

#### Backend — Database Migrations
| Migration | File | Trạng thái |
|-----------|------|-----------|
| `000020` — article_relations | `migrations/000020_create_article_relations_table.up.sql` | ✅ Created |
| `000021` — daily_content_schedules | `migrations/000021_create_daily_content_schedules_table.up.sql` | ✅ Created |
| `000022` — user_notes + user_countdowns | `migrations/000022_create_user_notes_and_countdowns_tables.up.sql` | ✅ Created |
| `000015` — newsletter_subscribers | `migrations/000015_create_newsletter_subscribers_table.up.sql` | ✅ Created |
| `000016` — user_streaks + user_achievements | `migrations/000016_create_streaks_achievements_tables.up.sql` | ✅ Created |

#### Backend — Models (Go / GORM)
| Model | File | Trạng thái |
|-------|------|-----------|
| ArticleRelation | `internal/models/article_relation.go` | ✅ |
| DailyContentSchedule | `internal/models/daily_content_schedule.go` | ✅ |
| UserNote + UserCountdown | `internal/models/user_note.go` | ✅ |
| NewsletterSubscriber + JSONB type | `internal/models/newsletter_subscriber.go` | ✅ |
| UserStreak + UserAchievement | `internal/models/user_streak.go` | ✅ |

#### Backend — DTOs
| DTO | File | Trạng thái |
|-----|------|-----------|
| Create/Batch/Update/Response ArticleRelation | `internal/dto/article_relation_dto.go` | ✅ |
| Create/Update/Response DailyContent + DayContentResponse | `internal/dto/daily_content_dto.go` | ✅ |
| Note + Countdown CRUD | `internal/dto/user_note_dto.go` | ✅ |

#### Backend — Repositories
| Repository | File | Trạng thái |
|-----------|------|-----------|
| ArticleRelationRepository | `internal/repositories/article_relation_repo.go` | ✅ |
| DailyContentRepository | `internal/repositories/daily_content_repo.go` | ✅ |
| UserNoteRepository + UserCountdownRepository | `internal/repositories/user_note_repo.go` | ✅ |
| NewsletterRepository | `internal/repositories/newsletter_repo.go` | ✅ |
| StreakRepository | `internal/repositories/streak_repo.go` | ✅ |

#### Backend — Services
| Service | File | Trạng thái |
|---------|------|-----------|
| ArticleRelationService (manual + auto: category, tags, random) | `internal/services/article_relation_service.go` | ✅ |
| DailyContentService (auto-assign + manual scheduling) | `internal/services/daily_content_service.go` | ✅ |
| UserNoteService + UserCountdownService | `internal/services/user_note_service.go` | ✅ |
| VideoProcessService (FFmpeg thumbnail + metadata) | `internal/services/video_process_service.go` | ✅ |
| HoroscopeService (12 zodiac, can chi scoring) | `internal/services/horoscope_service.go` | ✅ |
| GoodDayService (7 purposes, lunar age calc) | `internal/services/good_day_service.go` | ✅ |
| NewsletterService (subscribe, confirm, prefs) | `internal/services/newsletter_service.go` | ✅ |
| StreakAchievementService (visit, auto-unlock) | `internal/services/streak_achievement_service.go` | ✅ |

#### Backend — Handlers (API Controllers)
| Handler | File | Trạng thái |
|---------|------|-----------|
| ArticleRelationHandler (Create/Batch/Get/Update/Delete) | `internal/handlers/article_relation_handler.go` | ✅ |
| DailyContentHandler (GetForDate/Today + Admin CRUD) | `internal/handlers/daily_content_handler.go` | ✅ |
| UserNoteHandler + UserCountdownHandler | `internal/handlers/user_note_handler.go` | ✅ |
| HoroscopeHandler (today, by date, all zodiac, by birth year) | `internal/handlers/horoscope_handler.go` | ✅ |
| GoodDayHandler (purpose-based, lunar age) | `internal/handlers/good_day_handler.go` | ✅ |
| NewsletterHandler (subscribe, unsubscribe, confirm, admin) | `internal/handlers/newsletter_handler.go` | ✅ |
| StreakHandler (visit, profile, achievements, leaderboard) | `internal/handlers/streak_handler.go` | ✅ |

#### Backend — Routes & Wiring
| Component | File | Trạng thái |
|-----------|------|-----------|
| V3 Routes (Content + Admin + User) | `internal/routes/v3_routes.go` | ✅ |
| Main.go DI wiring (all V3 repos/services/handlers) | `cmd/server/main.go` | ✅ |

#### Frontend — Infrastructure
| Component | File | Trạng thái |
|-----------|------|-----------|
| TypeScript types (all V3 interfaces) | `src/types/v3.ts` | ✅ |
| API service (axios, all V3 endpoints) | `src/services/v3Service.ts` | ✅ |
| React Query hooks (cache invalidation + toast) | `src/hooks/useV3.ts` | ✅ |

#### Build Verification
| Check | Command | Trạng thái |
|-------|---------|-----------|
| Go build | `go build ./...` | ✅ Pass (2026-03-10) |
| Go vet | `go vet ./...` | ✅ Pass (2026-03-09) |
| TypeScript | `npx tsc --noEmit` | ✅ Pass (2026-03-10) |

### ✅ Đã hoàn thành — Frontend UI Components (2026-07-23)

#### Public Components
| Component | File | Mô tả |
|-----------|------|-------|
| ArticleSummaryCard | `components/articles/ArticleSummaryCard.tsx` | Card bài viết (ảnh + title + excerpt + reading time) |
| RelatedArticles | `components/articles/RelatedArticles.tsx` | Bài viết liên quan cuối trang bài viết |
| DailyQuoteCard | `components/lichso/DailyQuoteCard.tsx` | Danh ngôn ngày trong DayDetailModal |
| HistoricalEventsTimeline | `components/lichso/HistoricalEventsTimeline.tsx` | Timeline sự kiện lịch sử |
| ArticleBriefCard | `components/lichso/ArticleBriefCard.tsx` | Bài viết ngẫu nhiên + nút "Xem bài khác" |
| FestivalCard | `components/lichso/FestivalCard.tsx` | Lễ hội dân gian |
| BirthdayBadge | `components/lichso/BirthdayBadge.tsx` | Sinh nhật người nổi tiếng |
| CountdownWidget | `components/lichso/CountdownWidget.tsx` | Widget đếm ngược trên trang chủ |
| HoroscopeWidget | `components/lichso/HoroscopeWidget.tsx` | Tử vi 12 con giáp hàng ngày (tích hợp trang /tu-vi) |
| GoodDayPurposeTab | `components/lichso/GoodDayPurposeTab.tsx` | Xem ngày tốt theo mục đích (tích hợp trang /ngay-tot) |
| LunarAgeCalculator | `components/lichso/LunarAgeCalculator.tsx` | Tra cứu tuổi âm lịch (tích hợp trang /tu-vi) |
| StreakWidget | `components/lichso/StreakWidget.tsx` | Chuỗi hoạt động & thành tựu (tích hợp profile) |
| NewsletterSubscribe | `components/lichso/NewsletterSubscribe.tsx` | Đăng ký nhận bản tin (tích hợp footer layout) |
| ShareCardGenerator | `components/lichso/ShareCardGenerator.tsx` | Tạo ảnh chia sẻ ngày (tích hợp DayDetailModal) |

#### Enhanced Existing Components
| Component | File | Thay đổi |
|-----------|------|---------|
| DayDetailModal | `components/lichso/DayDetailModal.tsx` | Thêm V3 content: quote, events, festivals, birthdays, articles + ShareCardGenerator |
| ArticleForm | `components/articles/ArticleForm.tsx` | Thêm ArticleRelationPanel cho bài viết đang sửa |
| ProfileHeader | `components/layouts/ProfileHeader.tsx` | Thêm nav links: Ghi chú, Đếm ngược |
| AdminSidebar | `components/layouts/AdminSidebar.tsx` | Thêm "Nội dung ngày" menu item |
| Home page | `app/(home)/page.tsx` | Thêm CountdownWidget trong calendar panel |
| Home layout | `app/(home)/layout.tsx` | Thêm footer với NewsletterSubscribe |
| Article detail | `app/(home)/bai-viet/[slug]/page.tsx` | Thêm RelatedArticles component |
| Tử Vi page | `app/(home)/tu-vi/page.tsx` | Thêm HoroscopeWidget + LunarAgeCalculator |
| Ngày Tốt page | `app/(home)/ngay-tot/page.tsx` | Thêm GoodDayPurposeTab (xem ngày theo mục đích) |
| Profile page | `app/profile/page.tsx` | Thêm StreakWidget (chuỗi hoạt động & thành tựu) |

#### Admin Pages
| Page | File | Mô tả |
|------|------|-------|
| Daily Content CRUD | `app/admin/daily-content/page.tsx` | Quản lý nội dung ngày (table + filter + pagination) |
| Daily Content Form | `app/admin/daily-content/DailyContentFormDialog.tsx` | Dialog thêm/sửa nội dung ngày |
| Article Relations | `components/articles/ArticleRelationPanel.tsx` | Panel quản lý bài viết liên quan trong article form |

#### User Pages
| Page | File | Mô tả |
|------|------|-------|
| Notes | `app/profile/notes/page.tsx` | CRUD ghi chú cá nhân (ghim, màu sắc, grid layout) |
| Countdowns | `app/profile/countdowns/page.tsx` | CRUD đếm ngược (icon, color, recurring, live timer) |

### 🔜 Tiếp theo cần làm

#### Ưu tiên cao — Phase 23 còn lại
- [ ] In lịch (Print): generate PDF/PNG cho tháng/năm/ngày

#### Ưu tiên cao — Phase 24 còn lại
- [ ] Weather integration (Optional): OpenWeatherMap API
- [ ] Google Calendar sync (Optional): export events to GCal
- [ ] Mobile-optimized views: responsive calendar detail
- [ ] PWA improvements: offline calendar data, push notifications

#### Ưu tiên trung bình — Backend Enhancement
- [ ] Redis caching: related articles per article (TTL 1h)
- [ ] Redis caching: daily content per date (TTL 1h), random articles (TTL 24h)
- [ ] Cron job: reset random articles lúc 00:00

#### Ưu tiên trung bình — Phase 18 Media Analytics còn lại
- [ ] Upload trends charts (cần backend API thống kê theo thời gian)
- [ ] Broken reference detection (cần backend API)
- [ ] AI auto-tagging (Optional — OpenAI / Google Vision)
- [ ] AI image description generation
- [ ] AI content suggestions

#### Ưu tiên thấp — Phase 19 Cleanup
- [ ] Scheduled cleanup worker (trash, chunks, orphans)
- [ ] Storage optimization report
- [ ] Performance benchmarks
- [ ] Load testing (10,000+ files)
- [ ] Documentation cập nhật
- [ ] Migration guide v2 → v3

#### Ưu tiên thấp — DevOps
- [ ] Run database migrations trên server (000015, 000016, 000020, 000021, 000022)
- [ ] Seed data: auto-fill daily content cho 365 ngày
- [ ] Seed data: auto-generate related articles cho existing articles

---

## 2. Roadmap v4.0 — AI Integration

> Chi tiết đầy đủ: [LICHSO-V4-DOCUMENT.md](./LICHSO-V4-DOCUMENT.md)

### Phase 25 — OpenRouter Core & AI Horoscope (3 tuần) ✅

**Backend:**
- [x] `AIConfig` struct — load từ environment variables
- [x] `OpenRouterService` — HTTP client hoàn chỉnh (Complete + Stream SSE)
- [x] Migration `000025_create_ai_tables` (ai_generation_logs, ai_horoscope_sessions, ai_usage_quotas, ai_prompt_templates, ai_chat_sessions)
- [x] Models: `AIGenerationLog`, `AIHoroscopeSession`, `AIUsageQuota`, `AIPromptTemplate`, `AIChatSession`
- [x] Repositories: `AILogRepository`, `AIHoroscopeRepository`, `AIQuotaRepository`, `AIPromptTemplateRepository`, `AIChatRepository`
- [x] `AIHoroscopeService` — tích hợp bát tự calc + rate limit + cache + OpenRouter
- [x] `AIHoroscopeHandler` — POST read (stream SSE), GET history, GET quota
- [x] Routes `/api/v4/ai/horoscope/*`
- [x] Wiring DI trong `cmd/server/main.go`

**Frontend:**
- [x] `src/types/ai.ts` — TypeScript interfaces
- [x] `src/services/aiService.ts` — API calls + SSE fetch helper
- [x] `src/hooks/useAI.ts` — React Query hooks
- [x] `AIStreamingText.tsx` — Reusable SSE streaming text component
- [x] `AIHoroscopeForm.tsx` — Form nhập ngày sinh, loại xem, câu hỏi
- [x] `AIHoroscopeResult.tsx` — Hiển thị bát tự + AI result card
- [x] `AIUsageQuota.tsx` — Badge "Còn X lượt hôm nay"
- [x] Trang `/tu-vi-ai` (public route)
- [x] Tích hợp tab "Tử Vi AI" vào trang `/tu-vi` hiện có

### Phase 26 — AI Article Generator (3 tuần) ✅

**Backend:**
- [x] `AIArticleService` — prompt builder + OpenRouter call + draft parser
- [x] `AIPromptService` — CRUD prompt templates helper
- [x] `AIArticleHandler` — generate (stream), quick-draft, topic-suggest
- [x] Cập nhật `articles` table: thêm `ai_generated`, `ai_model`, `ai_tokens_used`, `ai_cost_usd` (migration 000025)
- [x] Routes `/api/v4/admin/ai/articles/*`

**Frontend:**
- [x] `AIArticleGenerator.tsx` — Form config + streaming preview + Quick Draft bar
- [x] `AIModelSelector.tsx` — Dropdown chọn AI model (admin)
- [x] Trang `/admin/ai-articles` — gợi ý chủ đề + generator
- [x] Admin Sidebar: menu item "AI Tạo Bài Viết"

### Phase 27 — AI Chat & Admin Dashboard (2 tuần) ✅

**Backend:**
- [x] `AIChatService` — session management, message history, context injection
- [x] `AIChatHandler` — CRUD sessions, POST message (stream SSE)
- [x] `AIAdminHandler` — stats, logs, cost report, prompt CRUD
- [x] Routes `/api/v4/ai/chat/*` + `/api/v4/admin/ai/*`

**Frontend:**
- [x] `AIChatWindow.tsx` + `AIChatMessage.tsx` — Chat bubble UI với SSE streaming
- [x] Trang `/chat-phong-thuy` (auth required)
- [x] Trang `/admin/ai-dashboard` — Stats, cost charts, prompt templates, AI logs
- [x] Admin Sidebar + Mobile: menu items "AI Dashboard" + "AI Tạo Bài Viết"

### Phase 28 — Auto Schedule & Optimization (2 tuần) 📋

- [ ] Cron job: tự động tạo bài viết trước 7 ngày cho ngày lễ/tiết khí
- [ ] Budget monitoring + auto-fallback model khi chi phí > 80% cap
- [ ] `AIScheduleConfig.tsx` — Admin UI lịch tự động tạo bài
- [ ] Prompt template editor trong Admin
- [ ] Badge "✨ AI Generated" cho bài viết AI (public)

---

## 3. Timeline Tổng Quan

```
[v3.0 — Đang hoàn thiện]
Tháng 3–9/2026: Phase 11–24 (Media, Content Calendar, Extended Features, Engagement) ✅/🔧

[v4.0 — AI Integration]
Tháng 3/2026: Phase 25 — OpenRouter Core + AI Tử Vi        📋
Tháng 4/2026: Phase 26 — AI Article Generator               📋
Tháng 4/2026: Phase 27 — AI Chat + Admin Dashboard          📋
Tháng 5/2026: Phase 28 — Auto Schedule + Optimization       📋

Release v4.0 Alpha (Tử Vi AI):   Cuối tháng 3/2026
Release v4.0 Beta (Viết Bài AI): Cuối tháng 4/2026
Release v4.0 Stable:             Giữa tháng 5/2026
```
