# 📱 LỊCH SỐ — Tài Liệu Version 3

> **Phiên bản tài liệu:** 1.0  
> **Ngày tạo:** 25/05/2026  
> **Phạm vi:** Android v3.0 — Tích hợp lichso.vn API + Quiz Engine  
> **Trạng thái:** Thiết kế — Chưa bắt đầu phát triển  
> **Phụ thuộc:** Backend lichso.vn (Go/Fiber) — đang chạy production

---

## 📋 MỤC LỤC

1. [Tổng quan Version 3](#1-tổng-quan-version-3)
2. [Kiến trúc tích hợp API](#2-kiến-trúc-tích-hợp-api)
3. [Module Content Feed](#3-module-content-feed)
4. [Module Quiz Engine](#4-module-quiz-engine)
5. [Backend: Endpoint mới cần xây dựng](#5-backend-endpoint-mới-cần-xây-dựng)
6. [Màn hình & Navigation](#6-màn-hình--navigation)
7. [Tích hợp với Points Engine V2](#7-tích-hợp-với-points-engine-v2)
8. [Mô hình dữ liệu Android](#8-mô-hình-dữ-liệu-android)
9. [Chiến lược Cache & Offline](#9-chiến-lược-cache--offline)
10. [Lộ trình phát triển](#10-lộ-trình-phát-triển)

---

## 1. TỔNG QUAN VERSION 3

### Mục tiêu

Version 3 biến Lịch Số từ **công cụ lịch** thành **nền tảng kiến thức văn hóa Việt Nam**, với hai trụ cột chính:

| Trụ cột | Mô tả | Nguồn dữ liệu |
|---|---|---|
| **Content Feed** | Bài viết, sự kiện, nhân vật lịch sử, lễ hội | lichso.vn API (REST) |
| **Quiz Engine** | Câu đố văn hóa + AI giải thích + Leaderboard | lichso.vn API (mới) |

### Triết lý thiết kế

- **Context-aware content**: Nội dung gắn với ngày hiện tại (hôm nay năm xưa, lễ hội gần nhất, nhân vật sinh nhật hôm nay)
- **Learn by playing**: Quiz tích hợp Points Engine — chơi có thưởng, sai có AI giải thích
- **Offline-first**: Cache 7 ngày, đọc báo được khi không có mạng
- **Single source of truth**: Toàn bộ nội dung từ lichso.vn — không hardcode

### Những gì KHÔNG thay đổi ở V3

- Points Engine V2 (giữ nguyên, chỉ bổ sung action mới)
- Navigation bottom bar (5 tab)
- Design System (màu sắc, typography)
- Tất cả tính năng V2 hiện có

---

## 2. KIẾN TRÚC TÍCH HỢP API

### 2.1 Base URL & Auth

```
Base URL: https://api.lichso.vn/v1   (hoặc theo config production)
Auth: Bearer JWT Token
```

**Chiến lược auth cho Android:**
- Guest mode (không đăng nhập): Dùng anonymous API key trong header `X-App-Key` — cho phép đọc public endpoints
- Logged-in mode: JWT từ `/auth/login` hoặc `/auth/google` — đồng bộ tài khoản, leaderboard cá nhân
- Token refresh: Tự động gọi `/auth/refresh` khi nhận 401

### 2.2 Sơ đồ kiến trúc

```
lichso.vn REST API
        │
        ▼
   RetrofitClient          ← OkHttp + Gson, singleton
        │
   ┌────┴────────────────────────────────┐
   │                                     │
ContentApiService              QuizApiService
   │                                     │
ContentRepository              QuizRepository
   │                                     │
   └────────────┬────────────────────────┘
                │
           Room Database (cache)
                │
   ┌────────────┴────────────────────────┐
   │                                     │
ContentViewModel              QuizViewModel
   │                                     │
NewsFeedScreen            QuizSessionScreen
ArticleDetailScreen        QuizResultScreen
TodayInHistoryScreen       LeaderboardScreen
FestivalDetailScreen       QuizHomeScreen
```

### 2.3 Retrofit Services

```kotlin
// ContentApiService.kt
interface ContentApiService {
    // Articles
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null,
        @Query("tag") tag: String? = null,
    ): ArticleListResponse

    @GET("articles/search")
    suspend fun searchArticles(@Query("q") query: String): ArticleListResponse

    @GET("articles/{id}")
    suspend fun getArticle(@Path("id") id: Long): ArticleResponse

    // Events (Ngày này năm xưa)
    @GET("events/date/{month}/{day}")
    suspend fun getEventsByDate(
        @Path("month") month: Int,
        @Path("day") day: Int,
    ): EventListResponse

    // Famous People (Nhân vật sinh nhật hôm nay)
    @GET("famous-people/birthday/{month}/{day}")
    suspend fun getFamousPeopleByBirthday(
        @Path("month") month: Int,
        @Path("day") day: Int,
    ): FamousPersonListResponse

    // Festivals
    @GET("festivals/lunar/{month}/{day}")
    suspend fun getFestivalsByLunarDate(
        @Path("month") month: Int,
        @Path("day") day: Int,
    ): FestivalListResponse

    // Daily Content
    @GET("day-content/today")
    suspend fun getTodayContent(): DayContentResponse

    @GET("day-content/{date}")
    suspend fun getDayContent(@Path("date") date: String): DayContentResponse

    // Quotes
    @GET("quotes/today")
    suspend fun getTodayQuote(): QuoteResponse

    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteResponse
}

// QuizApiService.kt  (endpoint mới — xem mục 5)
interface QuizApiService {
    @GET("quiz/questions/daily")
    suspend fun getDailyQuestions(@Query("date") date: String): QuizSetResponse

    @GET("quiz/questions")
    suspend fun getQuestions(
        @Query("category") category: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10,
    ): QuizSetResponse

    @POST("quiz/sessions")
    suspend fun startSession(@Body body: StartSessionBody): QuizSessionResponse

    @POST("quiz/sessions/{id}/submit")
    suspend fun submitAnswer(
        @Path("id") sessionId: String,
        @Body body: SubmitAnswerBody,
    ): AnswerResultResponse

    @POST("quiz/sessions/{id}/finish")
    suspend fun finishSession(@Path("id") sessionId: String): SessionResultResponse

    @GET("quiz/leaderboard")
    suspend fun getLeaderboard(
        @Query("period") period: String = "weekly", // daily | weekly | alltime
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 50,
    ): LeaderboardResponse

    @GET("quiz/leaderboard/me")
    suspend fun getMyRank(@Query("period") period: String = "weekly"): MyRankResponse
}
```

---

## 3. MODULE CONTENT FEED

### 3.1 Màn hình chính: KnowledgeFeedScreen

Tab mới trong bottom navigation (hoặc tích hợp vào HomeScreen dưới dạng section). Hiển thị nội dung được curate theo ngày hiện tại.

**Layout:**
```
┌─────────────────────────────────────┐
│  📰 Khám Phá Hôm Nay   [Tìm kiếm]  │
├─────────────────────────────────────┤
│  ▶ Ngày này năm xưa                 │
│  ┌───────────────────────────────┐  │
│  │ Sự kiện 1  │  Sự kiện 2      │  │  ← Horizontal scroll
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│  🎂 Nhân vật sinh nhật hôm nay      │
│  ┌─────────┐  ┌─────────┐          │
│  │ Avatar  │  │ Avatar  │          │
│  │ Tên     │  │ Tên     │          │
│  └─────────┘  └─────────┘          │
├─────────────────────────────────────┤
│  🏮 Lễ hội gần nhất                 │
│  ┌───────────────────────────────┐  │
│  │  [ảnh]  Tên lễ hội            │  │
│  │         Còn X ngày • Địa điểm │  │
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│  📚 Bài viết mới nhất               │
│  ┌───────────────────────────────┐  │
│  │ [thumb] Tiêu đề bài viết      │  │
│  │         Danh mục • Thời gian  │  │  ← Vertical list
│  └───────────────────────────────┘  │
│  ...                                │
└─────────────────────────────────────┘
```

### 3.2 ArticleDetailScreen

- Native render (Markdown → Compose) hoặc WebView với custom header/footer
- Nút "Hỏi AI về bài này" → gọi ChatViewModel với context là tóm tắt bài viết
- Bookmark bài viết → lưu local Room
- Chia sẻ → Share Intent
- Trao điểm: +1☯ khi đọc đủ (scroll > 80%)

### 3.3 FestivalDetailScreen

- Thông tin lễ hội: tên, ngày âm/dương, địa điểm, ý nghĩa, nghi lễ
- Countdown đến ngày lễ hội
- Nút "Xem văn khấn liên quan" → navigate PrayersScreen với filter
- Nút "Thêm vào lịch" → tạo Reminder

### 3.4 FamousPersonDetailScreen

- Avatar, tiểu sử, timeline sự kiện cuộc đời
- Nút "Hỏi AI thêm về [tên]" → ChatViewModel
- Related articles từ API

### 3.5 Tích hợp vào HomeScreen hiện tại

Thêm các section vào HomeScreen dưới dạng card nhỏ (không tạo tab mới, giảm cognitive load):

```kotlin
// Thêm vào HomeScreen sau section hiện tại
TodayEventsSection(events = todayEvents)        // "Ngày này năm xưa"
BirthdayFiguresSection(people = birthdayPeople) // Nếu có nhân vật sinh nhật
UpcomingFestivalBanner(festival = nextFestival) // Banner lễ hội gần nhất
```

---

## 4. MODULE QUIZ ENGINE

### 4.1 Tổng quan gameplay (Approach A + C)

**Approach A — Daily Quiz tích hợp Points:**
- Mỗi ngày 5 câu hỏi mới (reset 00:00)
- Đúng: +3⚡ Daily Points per câu → tối đa +15⚡/ngày từ quiz
- All correct (5/5): +5☯ Permanent bonus
- Streak quiz: 7 ngày liên tiếp quiz đủ → badge "Học giả"

**Approach C — AI giải thích:**
- Sau mỗi câu (đúng hoặc sai): hiển thị card giải thích ngắn (2-3 dòng)
- Nút "Tìm hiểu thêm" → gọi AI với context câu hỏi + đáp án đúng
- Nút "Xem bài viết liên quan" → ArticleDetailScreen nếu API trả về `article_id`

### 4.2 Luồng Quiz

```
QuizHomeScreen
    │
    ├── [Bộ đề hàng ngày]  →  QuizSessionScreen (5 câu cố định theo ngày)
    │                               │
    ├── [Chọn chủ đề]      →       │ (mỗi câu 30 giây)
    │   ├── Lịch sử VN              │
    │   ├── Nhân vật lịch sử        ▼
    │   ├── Lễ hội & Phong tục  QuizResultScreen
    │   ├── Văn hóa dân gian        │
    │   └── Địa danh lịch sử        ├── Xem giải thích AI
    │                               ├── Chia sẻ kết quả
    └── [Bảng xếp hạng]    →  LeaderboardScreen
```

### 4.3 QuizSessionScreen

**Layout một câu hỏi:**
```
┌─────────────────────────────────────┐
│  Câu 3/5     ████████░░  [⏱ 22s]   │
├─────────────────────────────────────┤
│                                     │
│  [Icon chủ đề]                      │
│                                     │
│  Trận Bạch Đằng năm 938 do ai       │
│  lãnh đạo chống quân Nam Hán?       │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  A.  Lý Thường Kiệt           │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  B.  Ngô Quyền                │  │  ← Đúng
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  C.  Trần Hưng Đạo            │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  D.  Đinh Bộ Lĩnh             │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

**Sau khi chọn đáp án:**
- Animate màu đáp án đúng (xanh) / sai (đỏ) — 500ms
- Hiện card giải thích ngắn (từ `explanation` field của API)
- Nút "Tiếp theo" → câu tiếp
- Timer dừng

### 4.4 QuizResultScreen

```
┌─────────────────────────────────────┐
│          🎉 Kết quả hôm nay         │
│                                     │
│              4 / 5                  │
│         ████████████████░░          │
│                                     │
│   +12⚡  +3☯   🏅 Huy chương mới   │
│                                     │
│  ┌─ Chi tiết ─────────────────────┐ │
│  │ ✅ Câu 1: Đúng                 │ │
│  │ ✅ Câu 2: Đúng                 │ │
│  │ ❌ Câu 3: Sai → [Xem AI giải] │ │
│  │ ✅ Câu 4: Đúng                 │ │
│  │ ✅ Câu 5: Đúng                 │ │
│  └────────────────────────────────┘ │
│                                     │
│  [Chia sẻ kết quả]  [Chơi thêm]    │
└─────────────────────────────────────┘
```

### 4.5 LeaderboardScreen

```
┌─────────────────────────────────────┐
│  🏆 Bảng Xếp Hạng                  │
│  [Tuần này ▼]  [Tháng]  [Toàn thời] │
├─────────────────────────────────────┤
│  ── Vị trí của bạn ──               │
│  #47  Bạn  ████  320 điểm          │
├─────────────────────────────────────┤
│  🥇 1.  Nguyễn A     980 điểm      │
│  🥈 2.  Trần B       875 điểm      │
│  🥉 3.  Lê C         820 điểm      │
│     4.  Phạm D       755 điểm      │
│     5.  Hoàng E      710 điểm      │
│     ...                            │
└─────────────────────────────────────┘
```

**Filter:** Tuần này / Tháng này / Toàn thời gian  
**Tab phụ:** Theo chủ đề (Lịch sử VN, Lễ hội, Nhân vật...)

### 4.6 AI Giải thích câu hỏi

Khi user bấm "Xem AI giải thích" sau câu sai:

```kotlin
val prompt = buildString {
    append("Câu hỏi: ${question.content}\n")
    append("Đáp án đúng: ${question.correctAnswer}\n")
    append("Người dùng đã chọn: ${userAnswer}\n\n")
    append("Hãy giải thích ngắn gọn (3-5 câu) tại sao đáp án đúng là ")
    append("'${question.correctAnswer}', cung cấp thêm bối cảnh lịch sử thú vị. ")
    append("Dùng tiếng Việt, giọng văn thân thiện.")
}
```

→ Mở sheet dialog (không navigate sang ChatScreen) để giữ context quiz.

---

## 5. BACKEND: ENDPOINT MỚI CẦN XÂY DỰNG

API lichso.vn hiện tại **chưa có quiz system**. Cần bổ sung các endpoint sau vào backend Go/Fiber:

### 5.1 Models cần tạo

```sql
-- Câu hỏi quiz
CREATE TABLE quiz_questions (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,                    -- Nội dung câu hỏi
    option_a    VARCHAR(500) NOT NULL,
    option_b    VARCHAR(500) NOT NULL,
    option_c    VARCHAR(500) NOT NULL,
    option_d    VARCHAR(500) NOT NULL,
    correct     CHAR(1) NOT NULL CHECK (correct IN ('a','b','c','d')),
    explanation TEXT,                             -- Giải thích đáp án
    category    VARCHAR(100) NOT NULL,            -- history_vn | figures | festivals | folklore | landmarks
    difficulty  VARCHAR(20) DEFAULT 'medium',     -- easy | medium | hard
    article_id  BIGINT REFERENCES articles(id),   -- Liên kết bài viết liên quan (nullable)
    event_id    BIGINT REFERENCES events(id),     -- Liên kết sự kiện (nullable)
    person_id   BIGINT REFERENCES famous_people(id),
    active      BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Ngày quiz hàng ngày (admin curate)
CREATE TABLE quiz_daily_sets (
    id           BIGSERIAL PRIMARY KEY,
    date         DATE UNIQUE NOT NULL,
    question_ids BIGINT[] NOT NULL,              -- 5 câu hỏi
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Phiên quiz của user
CREATE TABLE quiz_sessions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(20) NOT NULL,           -- daily | topic
    category     VARCHAR(100),
    question_ids BIGINT[] NOT NULL,
    answers      JSONB DEFAULT '[]',             -- [{q_id, chosen, correct, time_ms}]
    score        INT DEFAULT 0,
    total        INT NOT NULL,
    completed    BOOLEAN DEFAULT FALSE,
    started_at   TIMESTAMP DEFAULT NOW(),
    finished_at  TIMESTAMP
);

-- Điểm quiz tổng hợp (cho leaderboard)
CREATE TABLE quiz_scores (
    user_id     BIGINT PRIMARY KEY REFERENCES users(id),
    total_score INT DEFAULT 0,
    week_score  INT DEFAULT 0,                   -- reset mỗi tuần
    month_score INT DEFAULT 0,                   -- reset mỗi tháng
    best_streak INT DEFAULT 0,
    cur_streak  INT DEFAULT 0,
    last_quiz   DATE,
    updated_at  TIMESTAMP DEFAULT NOW()
);
```

### 5.2 Endpoints cần thêm

```
PUBLIC (X-App-Key):
  GET  /quiz/questions/daily?date=YYYY-MM-DD    → 5 câu hàng ngày
  GET  /quiz/questions?category=&difficulty=&limit=   → câu theo filter
  GET  /quiz/leaderboard?period=weekly&limit=50  → bảng xếp hạng public

AUTH REQUIRED (JWT):
  POST /quiz/sessions                            → bắt đầu phiên
  POST /quiz/sessions/:id/submit                 → nộp câu trả lời
  POST /quiz/sessions/:id/finish                 → kết thúc, tính điểm
  GET  /quiz/sessions/:id                        → xem kết quả phiên
  GET  /quiz/history                             → lịch sử các phiên
  GET  /quiz/leaderboard/me?period=weekly        → vị trí của tôi

ADMIN (RBAC):
  GET    /admin/quiz/questions                   → quản lý câu hỏi
  POST   /admin/quiz/questions                   → tạo câu hỏi
  PUT    /admin/quiz/questions/:id               → sửa câu hỏi
  DELETE /admin/quiz/questions/:id               → xóa câu hỏi
  POST   /admin/quiz/questions/bulk-import       → import hàng loạt (CSV/JSON)
  POST   /admin/quiz/daily-sets                  → set câu hỏi ngày
  GET    /admin/quiz/daily-sets                  → xem lịch câu hỏi
  POST   /admin/quiz/daily-sets/auto-fill        → AI tự sinh bộ đề
```

### 5.3 Gợi ý AI tự sinh câu hỏi (cho admin)

Tận dụng OpenRouter đã có sẵn để admin bấm "Auto-fill" → AI đọc articles/events/famous_people → sinh câu hỏi trắc nghiệm tự động:

```
Prompt template:
"Dựa trên bài viết sau, hãy tạo 3 câu hỏi trắc nghiệm 4 đáp án về [nội dung bài].
Mỗi câu phải có: nội dung câu hỏi, 4 đáp án (A/B/C/D), đáp án đúng, giải thích ngắn.
Độ khó: [easy/medium/hard]. Format: JSON array."
```

---

## 6. MÀN HÌNH & NAVIGATION

### 6.1 Các màn hình mới

| Route | Màn hình | Mô tả |
|---|---|---|
| `knowledge_feed` | KnowledgeFeedScreen | Feed nội dung theo ngày |
| `article/{id}` | ArticleDetailScreen | Chi tiết bài viết |
| `festival/{id}` | FestivalDetailScreen | Chi tiết lễ hội |
| `famous_person/{id}` | FamousPersonDetailScreen | Chi tiết nhân vật |
| `quiz_home` | QuizHomeScreen | Trang chính quiz |
| `quiz_session` | QuizSessionScreen | Đang chơi quiz |
| `quiz_result` | QuizResultScreen | Kết quả quiz |
| `leaderboard` | LeaderboardScreen | Bảng xếp hạng |

### 6.2 Entry points vào V3 features

**Từ HomeScreen:**
- Section "Ngày này năm xưa" card → `article/{id}` hoặc inline expand
- Banner "Lễ hội gần nhất" → `festival/{id}`
- Card "Quiz hàng ngày" (mới) → `quiz_home`

**Từ ToolsScreen:**
- Tool card "Quiz Lịch sử" → `quiz_home`
- Tool card "Khám phá kiến thức" → `knowledge_feed`

**Từ ThisDayInHistoryScreen:**
- Upgrade: Thay data hardcode bằng API `/events/date/{month}/{day}`
- Liên kết sang `famous_person/{id}` từ nhân vật được nhắc đến

### 6.3 Navigation trong LichSoMainScreen

```kotlin
// Thêm vào when(currentRoute) block:
"knowledge_feed" -> KnowledgeFeedScreen(
    onBackClick = { currentRoute = "home" },
    onArticleClick = { id -> currentRoute = "article/$id" },
    onFestivalClick = { id -> currentRoute = "festival/$id" },
    onPersonClick = { id -> currentRoute = "famous_person/$id" },
)
"quiz_home" -> QuizHomeScreen(
    onBackClick = { currentRoute = "tools" },
    onStartDaily = { currentRoute = "quiz_session" },
    onStartTopic = { category -> 
        quizCategory = category
        currentRoute = "quiz_session"
    },
    onLeaderboard = { currentRoute = "leaderboard" },
)
"quiz_session" -> QuizSessionScreen(
    onBackClick = { currentRoute = "quiz_home" },
    onFinished = { currentRoute = "quiz_result" },
    onAskAi = { prompt ->
        initialAiMessage = prompt
        currentRoute = "chat"
    },
    category = quizCategory,
)
"quiz_result" -> QuizResultScreen(
    onBackClick = { currentRoute = "quiz_home" },
    onShareClick = { /* Share Intent */ },
)
"leaderboard" -> LeaderboardScreen(
    onBackClick = { currentRoute = "quiz_home" },
)
```

---

## 7. TÍCH HỢP VỚI POINTS ENGINE V2

### 7.1 ActionType mới cần thêm

```kotlin
// Thêm vào ActionType.kt
QUIZ_CORRECT_ANSWER(
    dailyPoints = 3,
    permanentPoints = 0,
    dailyCap = 5,                // max 5 câu đúng/ngày = max 15⚡
    description = "Trả lời đúng câu quiz"
),
QUIZ_PERFECT_SCORE(
    dailyPoints = 0,
    permanentPoints = 5,
    dailyCap = 1,
    description = "Đạt điểm tuyệt đối (5/5) trong quiz ngày"
),
QUIZ_DAILY_STREAK_7(
    dailyPoints = 0,
    permanentPoints = 10,
    dailyCap = null,             // 1 lần duy nhất per milestone
    description = "7 ngày liên tiếp làm quiz"
),
READ_ARTICLE_FULL(
    dailyPoints = 1,
    permanentPoints = 0,
    dailyCap = 3,                // max 3 bài/ngày
    description = "Đọc xong bài viết (scroll > 80%)"
),
VIEW_FESTIVAL_DETAIL(
    dailyPoints = 1,
    permanentPoints = 0,
    dailyCap = 2,
    description = "Xem chi tiết lễ hội"
),
```

### 7.2 Quiz Streak riêng

Bên cạnh Login Streak hiện tại, thêm Quiz Streak:
- Lưu `lastQuizDate` trong Room local
- Nếu quiz đủ 5 câu hôm nay → streak tăng
- Hiển thị trong ProfileScreen dưới dạng stat riêng: "🎯 Streak quiz: 5 ngày"

### 7.3 Huy chương (Badge) mới

| Badge | Điều kiện | Unlock |
|---|---|---|
| 🎓 Học giả | 7 ngày quiz liên tiếp | Theme "Học giả" |
| 📜 Sử gia | 30 ngày quiz liên tiếp | Border vàng |
| 🏆 Quán quân | Top 10 leaderboard tuần | Crown badge |
| 📚 Mọt sách | Đọc 50 bài viết | Theme "Thư viện" |
| 🎯 Thần cung | 10 lần đạt 5/5 quiz | Avatar frame đặc biệt |

---

## 8. MÔ HÌNH DỮ LIỆU ANDROID

### 8.1 Room Entities (cache local)

```kotlin
// Article cache
@Entity(tableName = "articles_cache")
data class ArticleEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val slug: String,
    val summary: String?,
    val content: String?,           // Nullable — chỉ load khi xem chi tiết
    val thumbnailUrl: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val publishedAt: Long,          // epoch ms
    val cachedAt: Long,             // để biết cache bao giờ hết hạn
)

// Event cache (Ngày này năm xưa)
@Entity(tableName = "events_cache")
data class EventEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String?,
    val month: Int,
    val day: Int,
    val year: Int?,
    val imageUrl: String?,
    val articleId: Long?,
    val cachedAt: Long,
)

// Quiz question cache
@Entity(tableName = "quiz_questions_cache")
data class QuizQuestionEntity(
    @PrimaryKey val id: Long,
    val content: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correct: String,            // "a" | "b" | "c" | "d"
    val explanation: String?,
    val category: String,
    val difficulty: String,
    val articleId: Long?,
    val cachedAt: Long,
)

// Quiz session (local — sync lên server khi hoàn thành)
@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey val id: String,     // UUID
    val sessionType: String,
    val category: String?,
    val questionIds: String,        // JSON array
    val answers: String,            // JSON array [{qId, chosen, correct, timeMs}]
    val score: Int,
    val total: Int,
    val completed: Boolean,
    val startedAt: Long,
    val finishedAt: Long?,
    val synced: Boolean = false,    // đã sync lên server chưa
)

// Leaderboard cache
@Entity(tableName = "leaderboard_cache")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val period: String,             // daily | weekly | alltime
    val rank: Int,
    val userId: Long,
    val userName: String,
    val avatarUrl: String?,
    val score: Int,
    val cachedAt: Long,
)
```

### 8.2 Repository Pattern

```kotlin
class ContentRepository @Inject constructor(
    private val apiService: ContentApiService,
    private val dao: ContentDao,
    private val clock: Clock,
) {
    // Stale-while-revalidate: trả cache ngay, fetch ngầm nếu > 6 giờ
    fun observeTodayEvents(): Flow<List<EventEntity>> = flow {
        val today = LocalDate.now()
        val cached = dao.getEvents(today.monthValue, today.dayOfMonth)
        if (cached.isNotEmpty()) emit(cached)
        
        if (cached.isEmpty() || isCacheStale(cached.first().cachedAt, hours = 6)) {
            val remote = apiService.getEventsByDate(today.monthValue, today.dayOfMonth)
            dao.insertEvents(remote.data.map { it.toEntity() })
            emit(dao.getEvents(today.monthValue, today.dayOfMonth))
        }
    }

    suspend fun getArticle(id: Long): ArticleEntity {
        val cached = dao.getArticle(id)
        if (cached?.content != null && !isCacheStale(cached.cachedAt, hours = 24)) {
            return cached
        }
        val remote = apiService.getArticle(id)
        val entity = remote.data.toEntity()
        dao.insertArticle(entity)
        return entity
    }
}
```

---

## 9. CHIẾN LƯỢC CACHE & OFFLINE

### 9.1 TTL (Time To Live)

| Loại dữ liệu | TTL | Lý do |
|---|---|---|
| Articles list | 2 giờ | Cập nhật thường xuyên |
| Article detail | 24 giờ | Nội dung ít thay đổi |
| Events by date | 7 ngày | Lịch sử không đổi |
| Famous people | 7 ngày | Ít thay đổi |
| Festivals | 7 ngày | Lịch cố định hàng năm |
| Quiz daily set | 1 ngày | Reset mỗi ngày |
| Quiz questions | 3 ngày | Pool câu hỏi ổn định |
| Leaderboard | 15 phút | Real-time quan trọng |
| Daily quote | 1 ngày | Thay mỗi ngày |

### 9.2 Prefetch strategy

Khi app mở (trong background `WorkManager`):
1. Fetch `day-content/today` → cache nội dung ngày hôm nay
2. Fetch `quiz/questions/daily?date=today` → cache câu hỏi quiz ngày
3. Fetch `events/date/{month}/{day}` cho 3 ngày tới → ready offline

### 9.3 Offline fallback

- Nếu không có mạng + cache còn hạn → hiển thị bình thường
- Nếu không có mạng + cache hết hạn → hiển thị banner "Đang dùng dữ liệu cũ" + thời gian cache
- Quiz: cho phép chơi offline với câu hỏi đã cache → sync kết quả khi có mạng

---

## 10. LỘ TRÌNH PHÁT TRIỂN

### Phase 1 — Backend Foundation (2-3 tuần)

Backend (Go/Fiber):
- [ ] Tạo migration `quiz_questions`, `quiz_daily_sets`, `quiz_sessions`, `quiz_scores`
- [ ] CRUD endpoints cho quiz questions (public read + admin write)
- [ ] Endpoint daily quiz set + logic pick ngẫu nhiên nếu admin chưa set
- [ ] Session endpoints (start/submit/finish) với score calculation
- [ ] Leaderboard endpoint với Redis cache
- [ ] Admin bulk-import câu hỏi (CSV upload)
- [ ] AI auto-generate câu hỏi từ articles có sẵn

### Phase 2 — Android Content Feed (1-2 tuần)

Android:
- [ ] `ContentApiService` + Retrofit setup
- [ ] Room entities + DAOs cho articles, events, famous people, festivals
- [ ] `ContentRepository` với stale-while-revalidate
- [ ] `ContentViewModel`
- [ ] `KnowledgeFeedScreen` (layout theo mockup mục 3.1)
- [ ] `ArticleDetailScreen` (Markdown render)
- [ ] Upgrade `ThisDayInHistoryScreen` → dùng API thay hardcode
- [ ] Thêm sections vào `HomeScreen`

### Phase 3 — Android Quiz Engine (2-3 tuần)

Android:
- [ ] `QuizApiService` + `QuizRepository`
- [ ] Room entities cho quiz questions, sessions
- [ ] `QuizViewModel` (state machine: LOADING → QUESTION → RESULT → FINISHED)
- [ ] `QuizHomeScreen`
- [ ] `QuizSessionScreen` + timer countdown + answer animation
- [ ] `QuizResultScreen` + điểm Points
- [ ] `LeaderboardScreen`
- [ ] AI explanation sheet dialog
- [ ] Thêm `ActionType` quiz vào Points Engine
- [ ] Wire vào `LichSoMainScreen` routes

### Phase 4 — Polish & Gamification (1 tuần)

- [ ] Quiz streak tracking + badge mới
- [ ] Share quiz result (custom card)
- [ ] `FestivalDetailScreen`, `FamousPersonDetailScreen`
- [ ] WorkManager prefetch
- [ ] Offline fallback UI
- [ ] Thêm quiz tool card vào `ToolsScreen`
- [ ] Analytics events cho quiz (`quiz_started`, `quiz_completed`, `quiz_perfect_score`)

### Ước tính tổng

| Phase | Backend | Android | Tổng |
|---|---|---|---|
| Phase 1 | 2-3 tuần | — | 2-3 tuần |
| Phase 2 | — | 1-2 tuần | 1-2 tuần |
| Phase 3 | — | 2-3 tuần | 2-3 tuần |
| Phase 4 | — | 1 tuần | 1 tuần |
| **Tổng** | **2-3 tuần** | **4-6 tuần** | **~6-8 tuần** |

---

## PHỤ LỤC A — Danh mục Quiz Categories

| Category ID | Tên hiển thị | Icon | Ví dụ |
|---|---|---|---|
| `history_vn` | Lịch sử Việt Nam | 🏛️ | Các triều đại, chiến tranh, sự kiện |
| `figures` | Nhân vật lịch sử | 👤 | Vua, danh nhân, anh hùng |
| `festivals` | Lễ hội & Phong tục | 🏮 | Tết, Trung Thu, lễ truyền thống |
| `folklore` | Văn hóa dân gian | 📜 | Tục ngữ, ca dao, truyền thuyết |
| `landmarks` | Địa danh lịch sử | 🗺️ | Đền, thành, di tích |
| `cuisine` | Ẩm thực truyền thống | 🍜 | Nguồn gốc, ý nghĩa món ăn |

---

## PHỤ LỤC B — API Response Format mẫu

```json
// GET /quiz/questions/daily?date=2026-05-25
{
  "date": "2026-05-25",
  "questions": [
    {
      "id": 142,
      "content": "Trận Bạch Đằng năm 938 do ai lãnh đạo chống quân Nam Hán?",
      "option_a": "Lý Thường Kiệt",
      "option_b": "Ngô Quyền",
      "option_c": "Trần Hưng Đạo",
      "option_d": "Đinh Bộ Lĩnh",
      "category": "history_vn",
      "difficulty": "easy",
      "article_id": 58
    }
  ]
}

// POST /quiz/sessions/{id}/submit  — Response
{
  "question_id": 142,
  "chosen": "b",
  "correct": "b",
  "is_correct": true,
  "explanation": "Ngô Quyền (898–944) đã lãnh đạo quân dân Việt đánh tan quân Nam Hán trên sông Bạch Đằng năm 938, chấm dứt 1000 năm Bắc thuộc.",
  "article_id": 58,
  "points_earned": 3
}

// GET /quiz/leaderboard?period=weekly
{
  "period": "weekly",
  "generated_at": "2026-05-25T10:00:00Z",
  "entries": [
    { "rank": 1, "user_id": 1001, "display_name": "Nguyễn A", "avatar_url": "...", "score": 980 },
    { "rank": 2, "user_id": 1042, "display_name": "Trần B",   "avatar_url": null,  "score": 875 }
  ]
}
```

---

*Tài liệu này là thiết kế phân tích. Trước khi bắt đầu Phase 1, cần confirm:*
*1. Base URL production của lichso.vn API*
*2. X-App-Key cho anonymous Android client*
*3. Quyết định về auth flow (login bắt buộc để quiz hay guest được chơi nhưng không lưu leaderboard)*
