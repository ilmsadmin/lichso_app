# 📜 Lịch Số v4.0 — Tài Liệu Giai Đoạn 4: Tích Hợp AI

> **Lịch Số (曆數) v4.0** — Nền tảng văn hoá truyền thống được trang bị trí tuệ nhân tạo: Tự động viết bài và Xem tử vi cá nhân hoá theo yêu cầu người dùng

---

## 📋 Mục Lục

1. [Tổng Quan Giai Đoạn 4](#1-tổng-quan-giai-đoạn-4)
2. [Kiến Trúc AI Integration](#2-kiến-trúc-ai-integration)
3. [Tính Năng 1 — AI Viết Bài Tự Động](#3-tính-năng-1--ai-viết-bài-tự-động)
4. [Tính Năng 2 — AI Xem Tử Vi Theo Yêu Cầu](#4-tính-năng-2--ai-xem-tử-vi-theo-yêu-cầu)
5. [OpenRouter.ai Integration](#5-openrouterai-integration)
6. [Thiết Kế Cơ Sở Dữ Liệu v4.0](#6-thiết-kế-cơ-sở-dữ-liệu-v40)
7. [API Endpoints v4.0](#7-api-endpoints-v40)
8. [Backend Implementation](#8-backend-implementation)
9. [Frontend Implementation](#9-frontend-implementation)
10. [Prompt Engineering](#10-prompt-engineering)
11. [Quản Lý Chi Phí & Rate Limiting](#11-quản-lý-chi-phí--rate-limiting)
12. [Cấu Trúc Mã Nguồn Mới](#12-cấu-trúc-mã-nguồn-mới)
13. [Kế Hoạch Triển Khai — Roadmap Phase 25–28](#13-kế-hoạch-triển-khai--roadmap-phase-2528)

---

## 1. Tổng Quan Giai Đoạn 4

### 1.1 Tầm Nhìn

**Lịch Số v4.0** đưa trí tuệ nhân tạo vào trung tâm của trải nghiệm người dùng, biến nền tảng văn hoá truyền thống thành một **trợ lý thông minh** có khả năng:

- 🤖 **AI Viết Bài Tự Động** — Tự động tạo nội dung bài viết chất lượng cao về các chủ đề văn hoá, phong thuỷ, lịch sử theo lịch trình hoặc theo yêu cầu admin
- 🔮 **AI Xem Tử Vi Cá Nhân** — Người dùng nhập thông tin bát tự, AI phân tích và trả về luận giải tử vi chi tiết, sâu sắc bằng ngôn ngữ tự nhiên
- 💬 **AI Chat Tư Vấn** — Chatbot tư vấn phong thuỷ, chọn ngày tốt theo yêu cầu
- 📊 **AI Content Analytics** — Phân tích nội dung đang thiếu, đề xuất chủ đề viết bài

**Nền tảng AI**: [OpenRouter.ai](https://openrouter.ai) — Gateway tổng hợp, cho phép sử dụng nhiều model AI (GPT-4o, Claude 3.5 Sonnet, Gemini 1.5 Pro, DeepSeek, Llama 3.1...) qua 1 API duy nhất.

### 1.2 So Sánh v3.0 vs v4.0

| Khía cạnh | v3.0 (Hiện tại) | v4.0 (Mới) |
|-----------|-----------------|-------------|
| **Viết bài** | Thủ công 100% | + AI draft tự động + admin review |
| **Tử vi** | Rule-based theo can chi | + AI luận giải ngôn ngữ tự nhiên |
| **Gợi ý nội dung** | Không có | + AI đề xuất chủ đề theo xu hướng |
| **Tương tác** | Trang tĩnh + CRUD | + AI chat tư vấn phong thuỷ |
| **Cá nhân hoá** | Tuổi, ngày tốt, tử vi cơ bản | + AI tạo tử vi chi tiết theo bát tự |
| **Tốc độ tạo bài** | Vài giờ/bài (viết tay) | Vài phút/bài (AI + review) |
| **Chi phí content** | Cao (nhân lực) | Thấp hơn (AI + editorial QC) |

### 1.3 Mục Tiêu v4.0

| Mục tiêu | KPI |
|-----------|-----|
| **Tốc độ tạo bài** | Tạo draft bài 1000+ từ trong < 30 giây |
| **Chất lượng AI** | > 80% draft được duyệt sau chỉnh sửa nhỏ |
| **Tử vi AI** | Luận giải chi tiết 500+ từ/request |
| **Độ chính xác phong thuỷ** | Tham chiếu đúng hệ thống can chi, ngũ hành |
| **Ngân sách AI** | < $50/tháng cho 1,000 bài viết + 5,000 tử vi |
| **Uptime** | 99.5% (fallback model khi primary down) |
| **Response time** | < 5s cho tử vi, < 30s cho bài viết đầy đủ |

---

## 2. Kiến Trúc AI Integration

### 2.1 Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        LỊCH SỐ v4.0 — AI LAYER                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                        FRONTEND (Next.js)                        │    │
│  │   ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐    │    │
│  │   │  AI Article  │  │  AI Horoscope│  │   AI Chat         │    │    │
│  │   │  Generator   │  │  (Tử Vi AI)  │  │  (Phong Thuỷ)     │    │    │
│  │   │  (Admin)     │  │  (Public)    │  │  (Public)         │    │    │
│  │   └──────┬───────┘  └──────┬───────┘  └────────┬──────────┘    │    │
│  └──────────┼─────────────────┼────────────────────┼───────────────┘    │
│             │                 │                    │                     │
│  ┌──────────▼─────────────────▼────────────────────▼───────────────┐    │
│  │                     BACKEND (Go Fiber)                           │    │
│  │   ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐    │    │
│  │   │  AI Article  │  │  AI Horoscope│  │   AI Chat         │    │    │
│  │   │  Handler     │  │  Handler     │  │   Handler         │    │    │
│  │   └──────┬───────┘  └──────┬───────┘  └────────┬──────────┘    │    │
│  │          │                 │                    │                │    │
│  │   ┌──────▼─────────────────▼────────────────────▼──────────┐   │    │
│  │   │              OpenRouter Service (Core)                  │   │    │
│  │   │  • Model selection & fallback                           │   │    │
│  │   │  • Prompt template management                           │   │    │
│  │   │  • Streaming SSE support                                │   │    │
│  │   │  • Token counting & cost tracking                       │   │    │
│  │   │  • Rate limiting per user/day                           │   │    │
│  │   │  • Response caching (Redis)                             │   │    │
│  │   └──────────────────────┬──────────────────────────────────┘   │    │
│  └─────────────────────────┼────────────────────────────────────────┘    │
│                             │                                             │
│  ┌──────────────────────────▼────────────────────────────────────────┐   │
│  │                   OPENROUTER.AI GATEWAY                           │   │
│  │   ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐    │    │
│  │   │  GPT-4o      │  │  Claude 3.5  │  │  DeepSeek-V3      │    │    │
│  │   │  (Premium)   │  │  Sonnet      │  │  (Low Cost)       │    │    │
│  │   └──────────────┘  └──────────────┘  └───────────────────┘    │    │
│  │   ┌──────────────┐  ┌──────────────┐                            │    │
│  │   │  Gemini 1.5  │  │  Llama 3.1   │                            │    │
│  │   │  Pro         │  │  70B         │                            │    │
│  │   └──────────────┘  └──────────────┘                            │    │
│  └───────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐     │
│  │                    DATA LAYER                                    │     │
│  │   PostgreSQL: ai_articles, ai_horoscopes, ai_chat_sessions      │     │
│  │   Redis: AI response cache, rate limit counters                  │     │
│  │   MongoDB: AI usage logs, prompt history                         │     │
│  └─────────────────────────────────────────────────────────────────┘     │
└───────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Model Selection Strategy

| Use Case | Primary Model | Fallback Model | Lý do |
|----------|--------------|----------------|-------|
| **Viết bài dài** (1000+ từ) | `deepseek/deepseek-chat` | `meta-llama/llama-3.1-70b-instruct` | Chi phí thấp, chất lượng tốt với tiếng Việt |
| **Tử vi chi tiết** | `anthropic/claude-3.5-sonnet` | `openai/gpt-4o-mini` | Lập luận sâu, ngữ nghĩa phong phú |
| **AI Chat nhanh** | `openai/gpt-4o-mini` | `google/gemini-flash-1.5` | Latency thấp, giá rẻ |
| **Phân tích nội dung** | `google/gemini-pro-1.5` | `openai/gpt-4o` | Context window lớn |
| **Tiêu đề & tóm tắt** | `deepseek/deepseek-chat` | `meta-llama/llama-3.1-8b-instruct` | Nhanh, rẻ |

### 2.3 Flow Viết Bài AI

```
Admin chọn chủ đề / cấu hình
         │
         ▼
  ┌─────────────────┐
  │  Gather Context  │  ← Lấy dữ liệu từ DB: tags, category, ngày âm
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐
  │  Build Prompt   │  ← Prompt template + context injection
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐     ┌──────────────────┐
  │  OpenRouter API │────▶│  Stream Response  │  ← SSE streaming về Frontend
  └────────┬────────┘     └──────────────────┘
           │
           ▼
  ┌─────────────────┐
  │  Post-process   │  ← Format Markdown, extract metadata, SEO fields
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐
  │  Save as Draft  │  ← Lưu vào PostgreSQL, status = "ai_draft"
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐
  │  Admin Review   │  ← Admin chỉnh sửa, duyệt, publish
  └─────────────────┘
```

### 2.4 Flow Tử Vi AI

```
User nhập: Năm sinh, Tháng, Ngày, Giờ, Giới tính, Câu hỏi
         │
         ▼
  ┌─────────────────────┐
  │  Tính Bát Tự        │  ← Backend tính Can Chi từ dữ liệu đầu vào
  │  (Four Pillars)     │
  └──────────┬──────────┘
             │
             ▼
  ┌─────────────────────┐
  │  Build Horoscope    │  ← Truyền bát tự + câu hỏi + context vào prompt
  │  Prompt             │
  └──────────┬──────────┘
             │
             ▼
  ┌─────────────────────┐     ┌──────────────────────┐
  │  OpenRouter API     │────▶│  Stream to User      │  ← Real-time typing effect
  └──────────┬──────────┘     └──────────────────────┘
             │
             ▼
  ┌─────────────────────┐
  │  Cache & Save       │  ← Redis cache 24h, DB lưu lịch sử (nếu đăng nhập)
  └─────────────────────┘
```

---

## 3. Tính Năng 1 — AI Viết Bài Tự Động

### 3.1 Tổng Quan Tính Năng

Hệ thống AI viết bài cho phép admin tạo nội dung bài viết chất lượng cao về các chủ đề:
- Văn hoá truyền thống Việt Nam
- Phong thuỷ & ngũ hành
- Lịch sử & nhân vật lịch sử
- Tiết khí & lễ hội dân gian
- Ngày tốt & kiêng kỵ
- Tử vi & mệnh lý học

### 3.2 Chế Độ Tạo Bài

| Chế độ | Mô tả | Sử dụng khi |
|--------|--------|-------------|
| **Quick Draft** | Tạo nháp nhanh (~500 từ) trong 5–10 giây | Admin cần ý tưởng ban đầu |
| **Full Article** | Bài viết hoàn chỉnh (~1500–3000 từ) với streaming | Bài đăng chính thức |
| **SEO Optimized** | Bài viết với tiêu đề H1/H2/H3, meta description, từ khoá | Bài cần SEO tốt |
| **Series Article** | Tạo loạt bài theo series (3–10 bài liên quan) | Chuyên đề chuyên sâu |
| **Auto Schedule** | Tự động tạo bài theo lịch (ngày lễ, tiết khí sắp đến) | Content calendar tự động |

### 3.3 Cấu Hình Tạo Bài

```typescript
interface AIArticleConfig {
  // Nội dung cơ bản
  topic: string;                   // Chủ đề bài viết
  category_id: number;             // Danh mục
  tags: string[];                  // Tags gợi ý
  target_length: 'short' | 'medium' | 'long';  // Độ dài (~500/1500/3000 từ)

  // Phong cách viết
  writing_style: 'academic'        // Học thuật, trang trọng
                | 'popular'        // Phổ thông, dễ đọc
                | 'storytelling'   // Kể chuyện, hấp dẫn
                | 'listicle';      // Dạng danh sách

  // Ngữ cảnh bổ sung
  reference_date?: string;         // Ngày tham chiếu (dương lịch, ISO)
  lunar_context?: boolean;         // Thêm thông tin âm lịch vào context
  related_article_ids?: number[];  // Tham khảo bài viết đã có

  // SEO
  target_keyword?: string;         // Từ khoá SEO chính
  generate_seo?: boolean;          // Tự động tạo meta title, description, slug

  // Model
  model?: string;                  // Override model mặc định
  language: 'vi' | 'en';          // Ngôn ngữ bài viết (mặc định: 'vi')
}
```

### 3.4 Output Bài Viết AI

Sau khi AI tạo xong, hệ thống tự động điền vào form bài viết:

| Field | AI Tạo | Ghi chú |
|-------|--------|---------|
| `title` | ✅ | Tiêu đề SEO-friendly |
| `content` | ✅ | Nội dung Markdown/HTML đầy đủ |
| `excerpt` | ✅ | Tóm tắt 2–3 câu |
| `meta_title` | ✅ | Tiêu đề SEO (< 60 ký tự) |
| `meta_description` | ✅ | Mô tả SEO (< 160 ký tự) |
| `slug` | ✅ | URL slug tối ưu |
| `suggested_tags` | ✅ | 5–10 tags gợi ý |
| `reading_time` | ✅ | Thời gian đọc ước tính |
| `status` | Auto | `"ai_draft"` — chờ admin duyệt |
| `ai_model` | Auto | Model đã dùng |
| `ai_tokens_used` | Auto | Số token tiêu thụ |
| `ai_cost_usd` | Auto | Chi phí ước tính |

### 3.5 Trạng Thái Bài Viết AI

```
ai_draft ──▶ pending_review ──▶ published
    │                │
    │                └──▶ rejected ──▶ (xoá hoặc viết lại)
    │
    └──▶ (admin chỉnh sửa trực tiếp) ──▶ published
```

| Status | Mô tả |
|--------|--------|
| `ai_draft` | AI vừa tạo xong, chưa có người xem |
| `pending_review` | Admin đang review |
| `published` | Đã đăng công khai |
| `rejected` | Bị từ chối, cần viết lại |

### 3.6 Admin UI — AI Article Generator

**Trang**: `app/admin/ai-articles/page.tsx`

Bố cục giao diện:
```
┌─────────────────────────────────────────────────────────────────┐
│  🤖 AI Article Generator                        [+ Tạo Bài Mới] │
├─────────────────────────────────────────────────────────────────┤
│  Tabs: [Tạo Bài] [Danh Sách Draft] [Đã Duyệt] [Cài Đặt AI]    │
├─────────────────────────────────────────────────────────────────┤
│  TAB: TẠO BÀI                                                   │
│  ┌──────────────────────────┐  ┌──────────────────────────────┐ │
│  │  Chủ Đề & Cấu Hình       │  │  Preview & Edit              │ │
│  │                          │  │                              │ │
│  │  Chủ đề: [____________]  │  │  (Streaming text sẽ hiện     │ │
│  │  Danh mục: [▼ Dropdown]  │  │   ở đây khi AI đang viết)   │ │
│  │  Phong cách: [▼ Select]  │  │                              │ │
│  │  Độ dài: ○ Ngắn ● Trung  │  │  [Markdown Editor]          │ │
│  │          ○ Dài           │  │                              │ │
│  │  Tags: [tag1] [tag2] +   │  │  ─────────────────────────   │ │
│  │  Từ khoá SEO: [______]   │  │  SEO Preview:                │ │
│  │  Model AI: [▼ Auto]      │  │  Title: ...                  │ │
│  │                          │  │  Desc: ...                   │ │
│  │  [🤖 Tạo Bài Viết]       │  │  Slug: ...                   │ │
│  │  [⚡ Quick Draft]         │  │                              │ │
│  └──────────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. Tính Năng 2 — AI Xem Tử Vi Theo Yêu Cầu

### 4.1 Tổng Quan

Hệ thống tử vi AI kết hợp:
1. **Backend tính toán** — Tính chính xác Bát Tự (Tứ Trụ), Can Chi, Ngũ Hành từ ngày sinh
2. **AI luận giải** — Sử dụng Claude/GPT để diễn giải bát tự thành văn bản tự nhiên, sâu sắc

Kết quả vượt xa rule-based hiện tại — AI có thể trả lời câu hỏi cụ thể như:
- "Năm nay tôi có hợp làm kinh doanh không?"
- "Tôi và người sinh năm Tân Mão có hợp nhau không?"
- "Mệnh tôi hợp màu gì, hướng nào?"
- "Công việc năm 2026 của tôi như thế nào?"

### 4.2 Loại Tử Vi AI

| Loại | Mô tả | Chi phí token |
|------|--------|--------------|
| **Tổng quan mệnh** | Phân tích toàn diện bát tự, tính cách, sự nghiệp, tình duyên | ~2000 tokens |
| **Tử vi năm** | Luận giải vận hạn năm hiện tại theo lưu niên, thái tuế | ~1500 tokens |
| **Tử vi tháng** | Vận hạn tháng theo tiểu vận, lưu tháng | ~1000 tokens |
| **Hỏi & Đáp** | Trả lời câu hỏi cụ thể của user về cuộc sống | ~800 tokens |
| **Xem hợp tuổi** | Luận giải mức độ hợp nhau giữa 2 người | ~1200 tokens |
| **Chọn ngày tốt AI** | AI đề xuất ngày tốt nhất cho sự kiện cụ thể | ~800 tokens |

### 4.3 Input Form Tử Vi

```typescript
interface HoroscopeAIRequest {
  // Thông tin người dùng
  birth_year: number;       // Năm sinh dương lịch
  birth_month: number;      // Tháng sinh (1–12)
  birth_day: number;        // Ngày sinh (1–31)
  birth_hour?: number;      // Giờ sinh (0–23) — optional, ảnh hưởng trụ giờ
  gender: 'male' | 'female';

  // Loại xem
  reading_type: 'overview'      // Tổng quan mệnh cục
               | 'yearly'       // Tử vi năm
               | 'monthly'      // Tử vi tháng
               | 'question'     // Hỏi & đáp tự do
               | 'compatibility' // Hợp tuổi
               | 'choose_date'; // Chọn ngày tốt

  // Tham số theo loại
  target_year?: number;         // Cho yearly/monthly
  target_month?: number;        // Cho monthly
  question?: string;            // Câu hỏi cụ thể (tối đa 500 ký tự)
  partner_birth_year?: number;  // Cho compatibility

  // Tuỳ chọn
  depth: 'brief' | 'standard' | 'detailed';  // Độ chi tiết
  language: 'vi' | 'en';
}
```

### 4.4 Trang Tử Vi AI — Giao Diện Người Dùng

**Trang**: `app/(home)/tu-vi-ai/page.tsx`

```
┌────────────────────────────────────────────────────────────────┐
│  🔮 Tử Vi AI — Luận Giải Theo Yêu Cầu                         │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Nhập Thông Tin                                         │   │
│  │                                                         │   │
│  │  Năm sinh: [1990 ▼]  Tháng: [03 ▼]  Ngày: [15 ▼]      │   │
│  │  Giờ sinh: [08:00 ▼] (tuỳ chọn)    Giới tính: ● Nam ○ Nữ│  │
│  │                                                         │   │
│  │  Loại xem: [● Tổng Quan] [○ Tử Vi Năm] [○ Hỏi & Đáp]  │   │
│  │                                                         │   │
│  │  Câu hỏi (tuỳ chọn):                                   │   │
│  │  [Ví dụ: Năm 2026 tôi có nên thay đổi công việc?___]   │   │
│  │                                                         │   │
│  │  Độ chi tiết: ○ Tóm tắt  ● Tiêu chuẩn  ○ Chi tiết     │   │
│  │                                                         │   │
│  │              [🔮 Xem Tử Vi AI]  (3 lượt/ngày miễn phí) │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  📜 Kết Quả Luận Giải                                   │   │
│  │                                                         │   │
│  │  Bát Tự: [Canh Ngọ] [Quý Mão] [Mậu Tuất] [Giáp Thìn]  │   │
│  │  Ngũ Hành: Kim–Mộc–Thổ (Vượng Kim, Thiếu Hỏa)         │   │
│  │                                                         │   │
│  │  ┌─ AI Luận Giải ─────────────────────────────────┐    │   │
│  │  │  (Text streaming từ AI, hiệu ứng đánh máy)     │    │   │
│  │  │                                                 │    │   │
│  │  │  Mệnh cục của bạn thuộc Kim cục, với...        │    │   │
│  │  │  ▌ (con trỏ nhấp nháy)                         │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  │                                                         │   │
│  │  [💾 Lưu Kết Quả]  [📤 Chia Sẻ]  [🔄 Xem Lại]         │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

### 4.5 Hạn Mức Sử Dụng

| Loại tài khoản | Tử vi / ngày | Bài viết AI / tháng | Ghi chú |
|----------------|-------------|---------------------|---------|
| **Khách (chưa đăng nhập)** | 1 lượt | Không | Chỉ xem tổng quan brief |
| **Thành viên miễn phí** | 3 lượt | Không | Xem standard |
| **Thành viên Premium** | 20 lượt | Không | Xem detailed, lưu lịch sử |
| **Admin** | Không giới hạn | Không giới hạn | Full access |

---

## 5. OpenRouter.ai Integration

### 5.1 Tại Sao OpenRouter

| Tiêu chí | OpenRouter | Direct API |
|----------|------------|-----------|
| **Model đa dạng** | 200+ models từ 1 API | Phải tích hợp từng provider |
| **Fallback tự động** | ✅ Có | ❌ Phải tự code |
| **Chi phí** | Cạnh tranh, thường rẻ hơn | Phụ thuộc provider |
| **Quản lý API key** | 1 key cho tất cả | Nhiều keys |
| **Rate limiting** | Tổng hợp từ nhiều nguồn | Giới hạn của từng provider |
| **Streaming** | ✅ SSE support | Tuỳ provider |
| **Usage tracking** | Dashboard tổng hợp | Phải tự implement |

### 5.2 Cấu Hình API

```go
// backend/internal/config/ai_config.go

type AIConfig struct {
    OpenRouterAPIKey   string  `env:"OPENROUTER_API_KEY" required:"true"`
    OpenRouterBaseURL  string  `env:"OPENROUTER_BASE_URL" default:"https://openrouter.ai/api/v1"`
    SiteURL            string  `env:"SITE_URL" default:"https://lichso.vn"`
    SiteName           string  `env:"SITE_NAME" default:"Lịch Số"`

    // Model mặc định
    DefaultArticleModel   string `env:"AI_ARTICLE_MODEL" default:"deepseek/deepseek-chat"`
    DefaultHoroscopeModel string `env:"AI_HOROSCOPE_MODEL" default:"anthropic/claude-3.5-sonnet"`
    DefaultChatModel      string `env:"AI_CHAT_MODEL" default:"openai/gpt-4o-mini"`

    // Giới hạn
    MaxTokensArticle   int `env:"AI_MAX_TOKENS_ARTICLE" default:"4096"`
    MaxTokensHoroscope int `env:"AI_MAX_TOKENS_HOROSCOPE" default:"2048"`
    MaxTokensChat      int `env:"AI_MAX_TOKENS_CHAT" default:"1024"`

    // Rate limiting
    HoroscopeRateLimitGuest  int `env:"AI_RATE_HOROSCOPE_GUEST" default:"1"`   // per day
    HoroscopeRateLimitFree   int `env:"AI_RATE_HOROSCOPE_FREE" default:"3"`    // per day
    HoroscopeRateLimitPremium int `env:"AI_RATE_HOROSCOPE_PREMIUM" default:"20"` // per day
}
```

### 5.3 OpenRouter Request Format

```go
// OpenRouter tương thích OpenAI Chat Completion API
type OpenRouterRequest struct {
    Model    string            `json:"model"`
    Messages []ChatMessage     `json:"messages"`
    Stream   bool              `json:"stream,omitempty"`
    MaxTokens int              `json:"max_tokens,omitempty"`
    Temperature float64        `json:"temperature,omitempty"`

    // OpenRouter-specific headers (truyền qua HTTP headers)
    // HTTP-Referer: https://lichso.vn
    // X-Title: Lịch Số
}

type ChatMessage struct {
    Role    string `json:"role"`    // "system" | "user" | "assistant"
    Content string `json:"content"`
}
```

### 5.4 Danh Sách Model Khuyến Nghị

```
Viết bài tiếng Việt (giá tốt):
├── deepseek/deepseek-chat          → ~$0.14/1M tokens, tiếng Việt tốt
├── meta-llama/llama-3.1-70b-instruct → Free tier available
└── google/gemini-flash-1.5         → Nhanh, rẻ

Tử vi & lập luận sâu (chất lượng cao):
├── anthropic/claude-3.5-sonnet     → Tốt nhất cho phân tích phức tạp
├── openai/gpt-4o                   → Cân bằng chất lượng/giá
└── openai/gpt-4o-mini              → Nhanh, rẻ hơn

Chat hỏi đáp nhanh:
├── openai/gpt-4o-mini              → Latency thấp
├── google/gemini-flash-1.5         → Rẻ nhất
└── meta-llama/llama-3.1-8b-instruct → Free, nhanh
```

---

## 6. Thiết Kế Cơ Sở Dữ Liệu v4.0

### 6.1 Migration mới

**File**: `migrations/000025_create_ai_tables.up.sql`

```sql
-- ===================================================
-- AI Article Generation Logs
-- ===================================================
CREATE TABLE ai_generation_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    generation_type VARCHAR(50) NOT NULL,    -- 'article', 'horoscope', 'chat'
    model_used      VARCHAR(100) NOT NULL,
    prompt_tokens   INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens    INT DEFAULT 0,
    cost_usd        DECIMAL(10, 6) DEFAULT 0,
    duration_ms     INT DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'success', -- 'success', 'error', 'timeout'
    error_message   TEXT,
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_ai_logs_user_id ON ai_generation_logs(user_id);
CREATE INDEX idx_ai_logs_type ON ai_generation_logs(generation_type);
CREATE INDEX idx_ai_logs_created_at ON ai_generation_logs(created_at);

-- ===================================================
-- AI Horoscope Sessions (lịch sử xem tử vi)
-- ===================================================
CREATE TABLE ai_horoscope_sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_key     VARCHAR(64),             -- hash của input, dùng để cache
    birth_year      INT NOT NULL,
    birth_month     INT NOT NULL,
    birth_day       INT NOT NULL,
    birth_hour      INT,
    gender          VARCHAR(10) NOT NULL,
    reading_type    VARCHAR(30) NOT NULL,
    depth           VARCHAR(20) DEFAULT 'standard',
    target_year     INT,
    target_month    INT,
    question        TEXT,
    -- Kết quả tính toán (backend)
    bat_tu          JSONB,                   -- Tứ trụ bát tự
    ngu_hanh        JSONB,                   -- Ngũ hành phân tích
    -- Kết quả AI
    ai_result       TEXT,                    -- Nội dung luận giải từ AI
    model_used      VARCHAR(100),
    tokens_used     INT DEFAULT 0,
    cost_usd        DECIMAL(10, 6) DEFAULT 0,
    -- Metadata
    ip_address      INET,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_horoscope_user_id ON ai_horoscope_sessions(user_id);
CREATE INDEX idx_horoscope_session_key ON ai_horoscope_sessions(session_key);
CREATE INDEX idx_horoscope_created_at ON ai_horoscope_sessions(created_at);

-- ===================================================
-- AI Article Drafts (bài viết do AI tạo ra)
-- ===================================================
ALTER TABLE articles
    ADD COLUMN IF NOT EXISTS ai_generated     BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ai_model         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ai_prompt_id     BIGINT,
    ADD COLUMN IF NOT EXISTS ai_tokens_used   INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS ai_cost_usd      DECIMAL(10, 6) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS ai_generation_id BIGINT REFERENCES ai_generation_logs(id);

-- ===================================================
-- AI Prompt Templates
-- ===================================================
CREATE TABLE ai_prompt_templates (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL UNIQUE,
    type        VARCHAR(50) NOT NULL,        -- 'article', 'horoscope', 'chat'
    system_prompt TEXT NOT NULL,             -- System message
    user_prompt  TEXT NOT NULL,              -- User message template (với {{placeholders}})
    model        VARCHAR(100),               -- Model mặc định cho template này
    max_tokens   INT DEFAULT 2048,
    temperature  DECIMAL(3,2) DEFAULT 0.7,
    is_active    BOOLEAN DEFAULT TRUE,
    created_by   BIGINT REFERENCES users(id),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ===================================================
-- AI Chat Sessions
-- ===================================================
CREATE TABLE ai_chat_sessions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_uuid UUID DEFAULT gen_random_uuid() UNIQUE,
    title       VARCHAR(200),
    context     JSONB DEFAULT '{}',          -- Thông tin ngữ cảnh (bát tự, ngày...)
    messages    JSONB DEFAULT '[]',          -- Lịch sử chat [{role, content, created_at}]
    total_tokens INT DEFAULT 0,
    total_cost   DECIMAL(10, 4) DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    last_message_at TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_chat_sessions_user_id ON ai_chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_uuid ON ai_chat_sessions(session_uuid);

-- ===================================================
-- Rate Limiting (AI Usage)
-- ===================================================
CREATE TABLE ai_usage_quotas (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
    quota_type  VARCHAR(50) NOT NULL,        -- 'horoscope_daily', 'article_monthly'
    period_key  VARCHAR(20) NOT NULL,        -- '2026-03-12' hoặc '2026-03'
    used_count  INT DEFAULT 0,
    limit_count INT NOT NULL,
    reset_at    TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, quota_type, period_key)
);
```

### 6.2 Model Go — Cấu Trúc

```go
// backend/internal/models/ai_models.go

type AIGenerationLog struct {
    ID               uint64    `gorm:"primaryKey;autoIncrement"`
    UserID           *uint64   `gorm:"index"`
    GenerationType   string    `gorm:"size:50;not null"`
    ModelUsed        string    `gorm:"size:100;not null"`
    PromptTokens     int       `gorm:"default:0"`
    CompletionTokens int       `gorm:"default:0"`
    TotalTokens      int       `gorm:"default:0"`
    CostUSD          float64   `gorm:"type:decimal(10,6);default:0"`
    DurationMs       int       `gorm:"default:0"`
    Status           string    `gorm:"size:20;default:'success'"`
    ErrorMessage     string    `gorm:"type:text"`
    Metadata         datatypes.JSON
    CreatedAt        time.Time
}

type AIHoroscopeSession struct {
    ID           uint64    `gorm:"primaryKey;autoIncrement"`
    UserID       *uint64   `gorm:"index"`
    SessionKey   string    `gorm:"size:64;index"`
    BirthYear    int
    BirthMonth   int
    BirthDay     int
    BirthHour    *int
    Gender       string    `gorm:"size:10"`
    ReadingType  string    `gorm:"size:30"`
    Depth        string    `gorm:"size:20;default:'standard'"`
    TargetYear   *int
    TargetMonth  *int
    Question     string    `gorm:"type:text"`
    BatTu        datatypes.JSON
    NguHanh      datatypes.JSON
    AIResult     string    `gorm:"type:text"`
    ModelUsed    string    `gorm:"size:100"`
    TokensUsed   int       `gorm:"default:0"`
    CostUSD      float64   `gorm:"type:decimal(10,6);default:0"`
    IPAddress    string    `gorm:"type:inet"`
    CreatedAt    time.Time
}

type AIPromptTemplate struct {
    ID           uint64    `gorm:"primaryKey;autoIncrement"`
    Name         string    `gorm:"size:200;uniqueIndex"`
    Type         string    `gorm:"size:50"`
    SystemPrompt string    `gorm:"type:text"`
    UserPrompt   string    `gorm:"type:text"`
    Model        string    `gorm:"size:100"`
    MaxTokens    int       `gorm:"default:2048"`
    Temperature  float64   `gorm:"type:decimal(3,2);default:0.7"`
    IsActive     bool      `gorm:"default:true"`
    CreatedBy    *uint64
    CreatedAt    time.Time
    UpdatedAt    time.Time
}

type AIChatSession struct {
    ID            uint64    `gorm:"primaryKey;autoIncrement"`
    UserID        uint64    `gorm:"index"`
    SessionUUID   string    `gorm:"type:uuid;uniqueIndex"`
    Title         string    `gorm:"size:200"`
    Context       datatypes.JSON
    Messages      datatypes.JSON
    TotalTokens   int       `gorm:"default:0"`
    TotalCost     float64   `gorm:"type:decimal(10,4);default:0"`
    IsActive      bool      `gorm:"default:true"`
    LastMessageAt *time.Time
    CreatedAt     time.Time
}
```

---

## 7. API Endpoints v4.0

### 7.1 AI Article Endpoints

```
POST   /api/v4/ai/articles/generate          Admin — Tạo bài viết AI (full, có streaming)
POST   /api/v4/ai/articles/quick-draft       Admin — Tạo nháp nhanh (không streaming)
GET    /api/v4/ai/articles/drafts            Admin — Danh sách bài AI draft
GET    /api/v4/ai/articles/drafts/:id        Admin — Chi tiết bài draft
PATCH  /api/v4/ai/articles/drafts/:id/review Admin — Duyệt hoặc từ chối
POST   /api/v4/ai/articles/schedule          Admin — Lên lịch tự động tạo bài
GET    /api/v4/ai/articles/topics/suggest    Admin — AI gợi ý chủ đề viết bài
```

### 7.2 AI Horoscope Endpoints

```
POST   /api/v4/ai/horoscope/read             Public — Xem tử vi AI (có streaming)
POST   /api/v4/ai/horoscope/quick            Public — Xem tử vi tóm tắt (không stream)
GET    /api/v4/ai/horoscope/history          User Auth — Lịch sử xem tử vi
GET    /api/v4/ai/horoscope/history/:id      User Auth — Chi tiết 1 lần xem
DELETE /api/v4/ai/horoscope/history/:id      User Auth — Xoá lịch sử
GET    /api/v4/ai/horoscope/quota            User Auth — Hạn mức còn lại hôm nay
```

### 7.3 AI Chat Endpoints

```
POST   /api/v4/ai/chat/sessions              User Auth — Tạo session chat mới
GET    /api/v4/ai/chat/sessions              User Auth — Danh sách sessions
GET    /api/v4/ai/chat/sessions/:uuid        User Auth — Lấy session + messages
POST   /api/v4/ai/chat/sessions/:uuid/message User Auth — Gửi tin nhắn (có streaming)
DELETE /api/v4/ai/chat/sessions/:uuid        User Auth — Xoá session
```

### 7.4 AI Admin Management

```
GET    /api/v4/admin/ai/stats                Admin — Thống kê usage tổng quan
GET    /api/v4/admin/ai/logs                 Admin — Log chi tiết (filter, pagination)
GET    /api/v4/admin/ai/cost-report          Admin — Báo cáo chi phí theo ngày/tháng
GET    /api/v4/admin/ai/prompts              Admin — Danh sách prompt templates
POST   /api/v4/admin/ai/prompts              Admin — Tạo prompt template
PUT    /api/v4/admin/ai/prompts/:id          Admin — Cập nhật prompt template
DELETE /api/v4/admin/ai/prompts/:id          Admin — Xoá prompt template
```

### 7.5 Request/Response Ví Dụ

#### POST `/api/v4/ai/horoscope/read`

**Request:**
```json
{
    "birth_year": 1990,
    "birth_month": 3,
    "birth_day": 15,
    "birth_hour": 8,
    "gender": "male",
    "reading_type": "yearly",
    "target_year": 2026,
    "question": "Năm 2026 tôi có nên thay đổi công việc không?",
    "depth": "standard"
}
```

**Response (non-streaming):**
```json
{
    "success": true,
    "data": {
        "session_id": 12345,
        "bat_tu": {
            "year_pillar":  { "heavenly_stem": "Canh", "earthly_branch": "Ngọ" },
            "month_pillar": { "heavenly_stem": "Ất",   "earthly_branch": "Mão" },
            "day_pillar":   { "heavenly_stem": "Mậu",  "earthly_branch": "Tuất" },
            "hour_pillar":  { "heavenly_stem": "Giáp", "earthly_branch": "Thìn" }
        },
        "ngu_hanh_balance": {
            "Kim": 2, "Mộc": 1, "Thủy": 1, "Hỏa": 0, "Thổ": 4
        },
        "ai_result": "## Tử Vi Năm Bính Ngọ 2026\n\n**Mệnh cục của bạn**: Thổ cục vượng...\n\n### Sự Nghiệp\nNăm 2026, Thái Tuế chiếu vào cung quan lộc...",
        "model_used": "anthropic/claude-3.5-sonnet",
        "tokens_used": 1842,
        "quota_remaining": 2
    }
}
```

**Streaming Response** (khi `stream=true` trong header):
```
data: {"delta": "## Tử Vi Năm Bính Ngọ 2026\n\n"}
data: {"delta": "**Mệnh cục của bạn**: Thổ cục vượng..."}
data: {"delta": "\n\n### Sự Nghiệp\n"}
...
data: [DONE]
```

---

## 8. Backend Implementation

### 8.1 Cấu Trúc File Mới

```
backend/internal/
├── services/
│   ├── openrouter_service.go         ← Core OpenRouter client
│   ├── ai_article_service.go         ← Viết bài AI
│   ├── ai_horoscope_service.go       ← Tử vi AI
│   ├── ai_chat_service.go            ← Chat AI
│   └── ai_prompt_service.go          ← Quản lý prompt templates
├── handlers/
│   ├── ai_article_handler.go         ← HTTP handlers viết bài
│   ├── ai_horoscope_handler.go       ← HTTP handlers tử vi
│   ├── ai_chat_handler.go            ← HTTP handlers chat
│   └── ai_admin_handler.go           ← Admin stats & management
├── dto/
│   ├── ai_article_dto.go
│   ├── ai_horoscope_dto.go
│   └── ai_chat_dto.go
├── models/
│   └── ai_models.go                  ← Tất cả AI models
├── repositories/
│   ├── ai_log_repo.go
│   ├── ai_horoscope_repo.go
│   └── ai_chat_repo.go
└── middleware/
    └── ai_rate_limit.go              ← Rate limiting cho AI endpoints
```

### 8.2 OpenRouter Service — Core

```go
// backend/internal/services/openrouter_service.go

package services

import (
    "bufio"
    "bytes"
    "encoding/json"
    "fmt"
    "net/http"
    "strings"
    "time"

    "github.com/lichso/backend/internal/config"
)

type OpenRouterService struct {
    cfg        *config.AIConfig
    httpClient *http.Client
}

type OpenRouterMessage struct {
    Role    string `json:"role"`
    Content string `json:"content"`
}

type OpenRouterRequest struct {
    Model       string              `json:"model"`
    Messages    []OpenRouterMessage `json:"messages"`
    Stream      bool                `json:"stream,omitempty"`
    MaxTokens   int                 `json:"max_tokens,omitempty"`
    Temperature float64             `json:"temperature,omitempty"`
}

type OpenRouterResponse struct {
    ID      string `json:"id"`
    Choices []struct {
        Message struct {
            Content string `json:"content"`
        } `json:"message"`
        FinishReason string `json:"finish_reason"`
    } `json:"choices"`
    Usage struct {
        PromptTokens     int `json:"prompt_tokens"`
        CompletionTokens int `json:"completion_tokens"`
        TotalTokens      int `json:"total_tokens"`
    } `json:"usage"`
}

func NewOpenRouterService(cfg *config.AIConfig) *OpenRouterService {
    return &OpenRouterService{
        cfg: cfg,
        httpClient: &http.Client{
            Timeout: 120 * time.Second,
        },
    }
}

// Complete — gọi API không streaming
func (s *OpenRouterService) Complete(model string, messages []OpenRouterMessage, maxTokens int, temperature float64) (*OpenRouterResponse, error) {
    reqBody := OpenRouterRequest{
        Model:       model,
        Messages:    messages,
        MaxTokens:   maxTokens,
        Temperature: temperature,
    }

    return s.doRequest(reqBody)
}

// Stream — gọi API có streaming, gọi callback cho mỗi chunk
func (s *OpenRouterService) Stream(model string, messages []OpenRouterMessage, maxTokens int, onChunk func(delta string) error) (usage *TokenUsage, err error) {
    reqBody := OpenRouterRequest{
        Model:     model,
        Messages:  messages,
        MaxTokens: maxTokens,
        Stream:    true,
    }

    body, _ := json.Marshal(reqBody)
    req, err := http.NewRequest("POST", s.cfg.OpenRouterBaseURL+"/chat/completions", bytes.NewReader(body))
    if err != nil {
        return nil, err
    }

    req.Header.Set("Authorization", "Bearer "+s.cfg.OpenRouterAPIKey)
    req.Header.Set("Content-Type", "application/json")
    req.Header.Set("HTTP-Referer", s.cfg.SiteURL)
    req.Header.Set("X-Title", s.cfg.SiteName)

    resp, err := s.httpClient.Do(req)
    if err != nil {
        return nil, err
    }
    defer resp.Body.Close()

    scanner := bufio.NewScanner(resp.Body)
    usage = &TokenUsage{}

    for scanner.Scan() {
        line := scanner.Text()
        if !strings.HasPrefix(line, "data: ") {
            continue
        }
        data := strings.TrimPrefix(line, "data: ")
        if data == "[DONE]" {
            break
        }

        var chunk struct {
            Choices []struct {
                Delta struct {
                    Content string `json:"content"`
                } `json:"delta"`
            } `json:"choices"`
            Usage *struct {
                TotalTokens int `json:"total_tokens"`
            } `json:"usage"`
        }

        if err := json.Unmarshal([]byte(data), &chunk); err != nil {
            continue
        }

        if len(chunk.Choices) > 0 {
            delta := chunk.Choices[0].Delta.Content
            if delta != "" {
                if err := onChunk(delta); err != nil {
                    return usage, err
                }
            }
        }

        if chunk.Usage != nil {
            usage.TotalTokens = chunk.Usage.TotalTokens
        }
    }

    return usage, scanner.Err()
}

type TokenUsage struct {
    PromptTokens     int
    CompletionTokens int
    TotalTokens      int
}
```

### 8.3 AI Article Service

```go
// backend/internal/services/ai_article_service.go

package services

import (
    "fmt"
    "strings"
    "time"
)

type AIArticleService struct {
    openrouter    *OpenRouterService
    articleRepo   ArticleRepository
    categoryRepo  ArticleCategoryRepository
    logRepo       AILogRepository
    cfg           *config.AIConfig
}

type GenerateArticleInput struct {
    Topic         string
    CategoryID    uint64
    Tags          []string
    TargetLength  string   // "short" | "medium" | "long"
    WritingStyle  string
    TargetKeyword string
    GenerateSEO   bool
    Model         string
    Language      string
    LunarContext  bool
    RefDate       string
}

func (s *AIArticleService) GenerateArticle(input GenerateArticleInput) (*ArticleDraft, error) {
    // 1. Lấy thông tin context
    category, _ := s.categoryRepo.GetByID(input.CategoryID)

    // 2. Tính âm lịch nếu cần
    lunarInfo := ""
    if input.LunarContext && input.RefDate != "" {
        lunarInfo = s.getLunarContext(input.RefDate)
    }

    // 3. Xác định độ dài
    wordCount := map[string]string{
        "short":  "khoảng 500–700 từ",
        "medium": "khoảng 1200–1500 từ",
        "long":   "khoảng 2500–3000 từ",
    }[input.TargetLength]

    // 4. Build prompt
    systemPrompt := s.buildArticleSystemPrompt(input.WritingStyle, input.Language)
    userPrompt := s.buildArticleUserPrompt(BuildArticlePromptInput{
        Topic:         input.Topic,
        Category:      category.Name,
        Tags:          strings.Join(input.Tags, ", "),
        WordCount:     wordCount,
        TargetKeyword: input.TargetKeyword,
        LunarInfo:     lunarInfo,
        GenerateSEO:   input.GenerateSEO,
    })

    model := input.Model
    if model == "" {
        model = s.cfg.DefaultArticleModel
    }

    // 5. Gọi API
    startTime := time.Now()
    resp, err := s.openrouter.Complete(model, []OpenRouterMessage{
        {Role: "system", Content: systemPrompt},
        {Role: "user", Content: userPrompt},
    }, s.cfg.MaxTokensArticle, 0.7)

    duration := int(time.Since(startTime).Milliseconds())

    // 6. Log usage
    s.logRepo.Create(&AIGenerationLog{
        GenerationType:   "article",
        ModelUsed:        model,
        TotalTokens:      resp.Usage.TotalTokens,
        DurationMs:       duration,
        Status:           "success",
    })

    // 7. Parse kết quả
    content := resp.Choices[0].Message.Content
    draft := s.parseArticleDraft(content, input)

    return draft, err
}

func (s *AIArticleService) buildArticleSystemPrompt(style, language string) string {
    styleGuide := map[string]string{
        "academic":     "Viết theo phong cách học thuật, trang trọng, có trích dẫn nguồn.",
        "popular":      "Viết theo phong cách phổ thông, dễ hiểu, gần gũi với đại chúng.",
        "storytelling": "Viết theo phong cách kể chuyện, hấp dẫn, có mở bài cuốn hút.",
        "listicle":     "Viết theo dạng danh sách có đánh số, rõ ràng, dễ đọc.",
    }[style]

    return fmt.Sprintf(`Bạn là chuyên gia biên soạn nội dung về văn hoá truyền thống Việt Nam, phong thuỷ, lịch sử và lịch vạn niên.
Nhiệm vụ của bạn là viết bài viết chất lượng cao bằng tiếng Việt cho trang web Lịch Số (lichso.vn).

Yêu cầu:
- %s
- Sử dụng tiếng Việt chuẩn, chính tả đúng, dấu câu đầy đủ
- Nội dung chính xác về văn hoá, lịch sử, phong thuỷ Việt Nam
- Cấu trúc bài: Mở bài hấp dẫn → Thân bài với H2/H3 → Kết luận
- Format Markdown (dùng ## cho H2, ### cho H3, **bold**, *italic*, > blockquote)
- Không bịa đặt thông tin, nếu không chắc hãy dùng câu như "Theo truyền thống..."`, styleGuide)
}
```

### 8.4 AI Horoscope Service

```go
// backend/internal/services/ai_horoscope_service.go

package services

type AIHoroscopeService struct {
    openrouter   *OpenRouterService
    horoscope    *HoroscopeService   // Service tính can chi hiện có
    logRepo      AILogRepository
    quotaRepo    AIQuotaRepository
    cacheService *RedisService
    cfg          *config.AIConfig
}

type HoroscopeAIInput struct {
    BirthYear   int
    BirthMonth  int
    BirthDay    int
    BirthHour   *int
    Gender      string
    ReadingType string
    TargetYear  *int
    TargetMonth *int
    Question    string
    Depth       string
    UserID      *uint64
    IPAddress   string
}

func (s *AIHoroscopeService) ReadHoroscope(input HoroscopeAIInput) (*HoroscopeAIResult, error) {
    // 1. Kiểm tra rate limit
    if err := s.checkQuota(input.UserID, input.IPAddress); err != nil {
        return nil, err
    }

    // 2. Tính Bát Tự từ ngày sinh (dùng HoroscopeService hiện có)
    batTu := s.horoscope.CalculateBatTu(input.BirthYear, input.BirthMonth, input.BirthDay, input.BirthHour)
    nguHanh := s.horoscope.AnalyzeNguHanh(batTu)

    // 3. Kiểm tra cache
    cacheKey := s.buildCacheKey(input, batTu)
    if cached, err := s.cacheService.Get(cacheKey); err == nil {
        return cached.(*HoroscopeAIResult), nil
    }

    // 4. Build prompt tử vi
    systemPrompt := s.buildHoroscopeSystemPrompt()
    userPrompt := s.buildHoroscopeUserPrompt(input, batTu, nguHanh)

    // 5. Gọi OpenRouter
    resp, err := s.openrouter.Complete(
        s.cfg.DefaultHoroscopeModel,
        []OpenRouterMessage{
            {Role: "system", Content: systemPrompt},
            {Role: "user", Content: userPrompt},
        },
        s.cfg.MaxTokensHoroscope,
        0.75,
    )
    if err != nil {
        return nil, err
    }

    // 6. Lưu kết quả
    result := &HoroscopeAIResult{
        BatTu:    batTu,
        NguHanh:  nguHanh,
        AIResult: resp.Choices[0].Message.Content,
        Model:    s.cfg.DefaultHoroscopeModel,
    }

    // Cache 24h
    s.cacheService.Set(cacheKey, result, 24*time.Hour)

    // Cập nhật quota
    s.quotaRepo.IncrementUsage(input.UserID, "horoscope_daily")

    return result, nil
}

func (s *AIHoroscopeService) buildHoroscopeSystemPrompt() string {
    return `Bạn là chuyên gia tử vi và mệnh lý học phương Đông, am hiểu sâu về:
- Tứ Trụ Bát Tự (四柱八字): Năm, Tháng, Ngày, Giờ sinh
- Ngũ Hành (五行): Kim, Mộc, Thuỷ, Hoả, Thổ và sự tương sinh/tương khắc
- Thiên Can Địa Chi (天干地支): 10 Thiên Can, 12 Địa Chi
- Lục Thập Hoa Giáp (六十花甲): 60 năm chu kỳ
- Đại vận, Tiểu vận và ảnh hưởng đến cuộc sống
- Phong thuỷ và vận mệnh theo triết học phương Đông Việt Nam

Khi luận giải:
- Dùng ngôn ngữ tiếng Việt tự nhiên, dễ hiểu, tránh quá nhiều thuật ngữ Hán-Việt
- Luận giải có chiều sâu, không chung chung
- Đưa ra lời khuyên thực tế, tích cực
- Phân tích dựa trên bát tự đã cung cấp, không bịa đặt
- Kết hợp giải thích lý do dựa trên ngũ hành và can chi`
}

func (s *AIHoroscopeService) buildHoroscopeUserPrompt(input HoroscopeAIInput, batTu *BatTu, nguHanh *NguHanhAnalysis) string {
    genderStr := "Nam"
    if input.Gender == "female" {
        genderStr = "Nữ"
    }

    depthGuide := map[string]string{
        "brief":    "Tóm tắt ngắn gọn trong 200–300 từ",
        "standard": "Phân tích trung bình 500–800 từ, chia thành các mục rõ ràng",
        "detailed": "Phân tích chi tiết 1200–1500 từ với đầy đủ các khía cạnh",
    }[input.Depth]

    prompt := fmt.Sprintf(`Thông tin người xem:
- Giới tính: %s
- Bát Tự (Tứ Trụ):
  * Trụ Năm: %s %s
  * Trụ Tháng: %s %s
  * Trụ Ngày: %s %s
  * Trụ Giờ: %s %s
- Ngũ Hành: Kim(%d) Mộc(%d) Thuỷ(%d) Hoả(%d) Thổ(%d)
- Hành vượng nhất: %s | Hành thiếu: %s

Yêu cầu: %s

`,
        genderStr,
        batTu.Year.HeavenlyStem, batTu.Year.EarthlyBranch,
        batTu.Month.HeavenlyStem, batTu.Month.EarthlyBranch,
        batTu.Day.HeavenlyStem, batTu.Day.EarthlyBranch,
        batTu.Hour.HeavenlyStem, batTu.Hour.EarthlyBranch,
        nguHanh.Kim, nguHanh.Moc, nguHanh.Thuy, nguHanh.Hoa, nguHanh.Tho,
        nguHanh.Strongest, nguHanh.Weakest,
        depthGuide,
    )

    // Thêm ngữ cảnh theo loại xem
    switch input.ReadingType {
    case "yearly":
        prompt += fmt.Sprintf("Hãy luận giải tử vi năm %d cho người này.", *input.TargetYear)
    case "question":
        prompt += fmt.Sprintf("Hãy trả lời câu hỏi sau dựa trên bát tự: \"%s\"", input.Question)
    case "overview":
        prompt += "Hãy phân tích tổng quan mệnh cục: tính cách, sự nghiệp, tình duyên, sức khoẻ và tài lộc."
    case "compatibility":
        prompt += fmt.Sprintf("Hãy luận giải mức độ hợp nhau giữa người này (năm sinh %d) và người kia (năm sinh %d).",
            input.BirthYear, *input.PartnerBirthYear)
    }

    return prompt
}
```

### 8.5 AI Rate Limit Middleware

```go
// backend/internal/middleware/ai_rate_limit.go

package middleware

import (
    "fmt"
    "github.com/gofiber/fiber/v2"
    "github.com/lichso/backend/internal/services"
)

func AIRateLimit(quotaService *services.AIQuotaService, quotaType string) fiber.Handler {
    return func(c *fiber.Ctx) error {
        userID := c.Locals("userID")
        ipAddr := c.IP()

        limit, used, err := quotaService.GetQuota(userID, ipAddr, quotaType)
        if err != nil {
            return c.Next() // Lỗi quota => cho qua (fail open)
        }

        if used >= limit {
            return c.Status(fiber.StatusTooManyRequests).JSON(fiber.Map{
                "success": false,
                "error":   "Bạn đã hết lượt sử dụng AI hôm nay.",
                "data": fiber.Map{
                    "limit":      limit,
                    "used":       used,
                    "reset_at":   "00:00 ngày mai",
                    "upgrade_url": "/premium",
                },
            })
        }

        // Truyền quota info vào context
        c.Locals("ai_quota_remaining", limit-used)
        return c.Next()
    }
}
```

---

## 9. Frontend Implementation

### 9.1 Cấu Trúc File Mới

```
frontend/src/
├── app/
│   ├── (home)/
│   │   ├── tu-vi-ai/
│   │   │   └── page.tsx                    ← Trang tử vi AI (public)
│   │   └── chat-phong-thuy/
│   │       └── page.tsx                    ← Chat tư vấn phong thuỷ
│   └── admin/
│       ├── ai-articles/
│       │   └── page.tsx                    ← Admin: tạo bài AI
│       └── ai-dashboard/
│           └── page.tsx                    ← Admin: thống kê AI
├── components/
│   ├── ai/
│   │   ├── AIArticleGenerator.tsx          ← Form tạo bài AI + streaming preview
│   │   ├── AIArticleDraftList.tsx          ← Danh sách bài draft
│   │   ├── AIHoroscopeForm.tsx             ← Form nhập thông tin tử vi
│   │   ├── AIHoroscopeResult.tsx           ← Hiển thị kết quả tử vi (streaming)
│   │   ├── AIStreamingText.tsx             ← Component chung: hiệu ứng text streaming
│   │   ├── AIChatWindow.tsx                ← Cửa sổ chat AI phong thuỷ
│   │   ├── AIUsageQuota.tsx                ← Hiển thị hạn mức còn lại
│   │   └── AIModelSelector.tsx             ← Chọn model AI (admin)
├── services/
│   └── aiService.ts                        ← API calls tới backend AI endpoints
├── hooks/
│   └── useAI.ts                            ← React Query hooks cho AI
└── types/
    └── ai.ts                               ← TypeScript types cho AI
```

### 9.2 AIStreamingText Component

```typescript
// frontend/src/components/ai/AIStreamingText.tsx
'use client';

import { useEffect, useRef, useState } from 'react';
import ReactMarkdown from 'react-markdown';

interface AIStreamingTextProps {
    streamUrl: string;
    payload: object;
    onComplete?: (fullText: string) => void;
    onError?: (error: string) => void;
    className?: string;
}

export function AIStreamingText({
    streamUrl,
    payload,
    onComplete,
    onError,
    className,
}: AIStreamingTextProps) {
    const [text, setText] = useState('');
    const [isStreaming, setIsStreaming] = useState(false);
    const [isDone, setIsDone] = useState(false);
    const fullTextRef = useRef('');
    const abortRef = useRef<AbortController | null>(null);

    useEffect(() => {
        const controller = new AbortController();
        abortRef.current = controller;

        const stream = async () => {
            setIsStreaming(true);
            setText('');
            fullTextRef.current = '';

            try {
                const res = await fetch(streamUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                        'X-Stream': 'true',
                    },
                    body: JSON.stringify(payload),
                    signal: controller.signal,
                });

                const reader = res.body!.getReader();
                const decoder = new TextDecoder();

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    const chunk = decoder.decode(value);
                    const lines = chunk.split('\n');

                    for (const line of lines) {
                        if (line.startsWith('data: ')) {
                            const data = line.slice(6);
                            if (data === '[DONE]') {
                                setIsDone(true);
                                onComplete?.(fullTextRef.current);
                                return;
                            }
                            try {
                                const parsed = JSON.parse(data);
                                const delta = parsed.delta || '';
                                fullTextRef.current += delta;
                                setText(fullTextRef.current);
                            } catch {}
                        }
                    }
                }
            } catch (err: unknown) {
                if ((err as Error).name !== 'AbortError') {
                    onError?.((err as Error).message);
                }
            } finally {
                setIsStreaming(false);
            }
        };

        stream();

        return () => controller.abort();
    }, [streamUrl, JSON.stringify(payload)]);

    return (
        <div className={className}>
            <div className="prose prose-sm max-w-none dark:prose-invert">
                <ReactMarkdown>{text}</ReactMarkdown>
                {isStreaming && !isDone && (
                    <span className="inline-block w-2 h-4 bg-primary animate-pulse ml-0.5" />
                )}
            </div>
        </div>
    );
}
```

### 9.3 AIHoroscopeForm Component

```typescript
// frontend/src/components/ai/AIHoroscopeForm.tsx
'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { AIStreamingText } from './AIStreamingText';
import { AIUsageQuota } from './AIUsageQuota';

export function AIHoroscopeForm() {
    const [formData, setFormData] = useState({
        birth_year: new Date().getFullYear() - 30,
        birth_month: 1,
        birth_day: 1,
        birth_hour: undefined as number | undefined,
        gender: 'male',
        reading_type: 'overview',
        target_year: new Date().getFullYear(),
        question: '',
        depth: 'standard',
    });

    const [isLoading, setIsLoading] = useState(false);
    const [showResult, setShowResult] = useState(false);
    const [batTuInfo, setBatTuInfo] = useState<BatTuInfo | null>(null);

    const handleSubmit = async () => {
        setIsLoading(true);
        setShowResult(true);
        // AIStreamingText sẽ tự fetch stream
    };

    return (
        <div className="space-y-6">
            {/* Form nhập liệu */}
            <div className="bg-amber-50 dark:bg-amber-950/20 rounded-xl p-6 border border-amber-200">
                <h3 className="font-semibold text-amber-900 dark:text-amber-100 mb-4">
                    🔮 Nhập Thông Tin Để Xem Tử Vi
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <YearSelect value={formData.birth_year} onChange={...} />
                    <MonthSelect value={formData.birth_month} onChange={...} />
                    <DaySelect value={formData.birth_day} onChange={...} />
                    <HourSelect value={formData.birth_hour} onChange={...} label="Giờ sinh (tuỳ chọn)" />
                </div>

                <div className="flex gap-4 mt-3">
                    <GenderToggle value={formData.gender} onChange={...} />
                    <ReadingTypeSelect value={formData.reading_type} onChange={...} />
                    <DepthSelect value={formData.depth} onChange={...} />
                </div>

                {formData.reading_type === 'question' && (
                    <Textarea
                        placeholder="Ví dụ: Năm 2026 tôi có nên thay đổi công việc không?"
                        value={formData.question}
                        onChange={e => setFormData({...formData, question: e.target.value})}
                        className="mt-3"
                        maxLength={500}
                    />
                )}

                <div className="flex items-center justify-between mt-4">
                    <AIUsageQuota type="horoscope_daily" />
                    <Button
                        onClick={handleSubmit}
                        disabled={isLoading}
                        className="bg-amber-600 hover:bg-amber-700"
                    >
                        🔮 Xem Tử Vi AI
                    </Button>
                </div>
            </div>

            {/* Kết quả */}
            {showResult && (
                <div className="bg-white dark:bg-gray-900 rounded-xl p-6 border shadow-sm">
                    {batTuInfo && <BatTuDisplay data={batTuInfo} />}

                    <AIStreamingText
                        streamUrl="/api/v4/ai/horoscope/read"
                        payload={{ ...formData, stream: true }}
                        onComplete={(text) => {
                            setIsLoading(false);
                            // Save to history if logged in
                        }}
                        className="mt-4"
                    />
                </div>
            )}
        </div>
    );
}
```

### 9.4 TypeScript Types

```typescript
// frontend/src/types/ai.ts

export interface AIHoroscopeRequest {
    birth_year: number;
    birth_month: number;
    birth_day: number;
    birth_hour?: number;
    gender: 'male' | 'female';
    reading_type: 'overview' | 'yearly' | 'monthly' | 'question' | 'compatibility' | 'choose_date';
    target_year?: number;
    target_month?: number;
    question?: string;
    partner_birth_year?: number;
    depth: 'brief' | 'standard' | 'detailed';
    stream?: boolean;
}

export interface BatTuPillar {
    heavenly_stem: string;    // Thiên Can
    earthly_branch: string;   // Địa Chi
    element: string;          // Ngũ Hành
}

export interface BatTuInfo {
    year_pillar: BatTuPillar;
    month_pillar: BatTuPillar;
    day_pillar: BatTuPillar;
    hour_pillar: BatTuPillar;
}

export interface NguHanhBalance {
    Kim: number;
    Moc: number;
    Thuy: number;
    Hoa: number;
    Tho: number;
    strongest: string;
    weakest: string;
}

export interface HoroscopeAIResponse {
    session_id: number;
    bat_tu: BatTuInfo;
    ngu_hanh_balance: NguHanhBalance;
    ai_result: string;
    model_used: string;
    tokens_used: number;
    quota_remaining: number;
}

export interface AIArticleConfig {
    topic: string;
    category_id: number;
    tags: string[];
    target_length: 'short' | 'medium' | 'long';
    writing_style: 'academic' | 'popular' | 'storytelling' | 'listicle';
    target_keyword?: string;
    generate_seo: boolean;
    model?: string;
    language: 'vi' | 'en';
    lunar_context: boolean;
    ref_date?: string;
}

export interface AIArticleDraft {
    id: number;
    title: string;
    content: string;
    excerpt: string;
    meta_title?: string;
    meta_description?: string;
    slug: string;
    suggested_tags: string[];
    reading_time: number;
    status: 'ai_draft' | 'pending_review' | 'published' | 'rejected';
    ai_model: string;
    ai_tokens_used: number;
    ai_cost_usd: number;
    created_at: string;
}

export interface AIUsageQuota {
    quota_type: string;
    used: number;
    limit: number;
    remaining: number;
    reset_at: string;
}

export interface AIStats {
    total_requests: number;
    total_tokens: number;
    total_cost_usd: number;
    articles_generated: number;
    horoscopes_read: number;
    chat_messages: number;
    cost_by_day: { date: string; cost: number }[];
    requests_by_model: { model: string; count: number }[];
}
```

---

## 10. Prompt Engineering

### 10.1 Prompt Viết Bài — Template Chính

```
SYSTEM:
Bạn là chuyên gia biên soạn nội dung về văn hoá truyền thống Việt Nam cho trang Lịch Số (lichso.vn).
Chuyên môn: Phong thuỷ, âm lịch, lịch vạn niên, lễ hội dân gian, nhân vật lịch sử.

Quy tắc viết:
1. Tiếng Việt chuẩn, đúng chính tả, dấu câu đầy đủ
2. Format Markdown: ## cho H2, ### cho H3, **bold**, *italic*
3. Độ dài: {{WORD_COUNT}}
4. Phong cách: {{WRITING_STYLE}}
5. Nội dung chính xác, không bịa đặt
6. Luôn có: mở bài, thân bài (chia section), kết luận

{{#IF GENERATE_SEO}}
Cuối bài thêm block JSON:
```json
{
  "seo": {
    "meta_title": "...",        // < 60 ký tự
    "meta_description": "...", // < 160 ký tự
    "slug": "...",             // URL-friendly, lowercase, dấu gạch nối
    "focus_keyword": "..."
  }
}
```
{{/IF}}

USER:
Viết bài về chủ đề: {{TOPIC}}
Danh mục: {{CATEGORY}}
Tags liên quan: {{TAGS}}
{{#IF KEYWORD}}Từ khoá SEO chính: {{KEYWORD}}{{/IF}}
{{#IF LUNAR_INFO}}Thông tin âm lịch tham khảo: {{LUNAR_INFO}}{{/IF}}
```

### 10.2 Prompt Tử Vi — Template

```
SYSTEM:
Bạn là thầy tử vi có hơn 30 năm kinh nghiệm nghiên cứu Tứ Trụ Bát Tự và mệnh lý học phương Đông.
Am hiểu: Thiên Can, Địa Chi, Ngũ Hành tương sinh tương khắc, Đại vận, Tiểu vận, Lưu niên.

Cách luận giải:
- Phân tích dựa trên Bát Tự được cung cấp, không suy đoán chung chung
- Dùng tiếng Việt dễ hiểu, giải thích thuật ngữ khi cần
- Lời khuyên thực tế, tích cực, định hướng hành động
- Cấu trúc rõ ràng với các mục: Mệnh Cục, Tính Cách, Sự Nghiệp, Tình Duyên, Sức Khoẻ, Lời Khuyên
- Không đưa ra tiên đoán bi quan tuyệt đối

USER:
Thông tin bát tự:
- Giới tính: {{GENDER}}
- Trụ Năm: {{YEAR_HS}} {{YEAR_EB}} ({{YEAR_ELEMENT}})
- Trụ Tháng: {{MONTH_HS}} {{MONTH_EB}} ({{MONTH_ELEMENT}})
- Trụ Ngày: {{DAY_HS}} {{DAY_EB}} ({{DAY_ELEMENT}})
- Trụ Giờ: {{HOUR_HS}} {{HOUR_EB}} ({{HOUR_ELEMENT}})

Cân bằng Ngũ Hành:
Kim({{KIM}}) Mộc({{MOC}}) Thuỷ({{THUY}}) Hoả({{HOA}}) Thổ({{THO}})
Vượng: {{STRONGEST}} | Thiếu: {{WEAKEST}}

Yêu cầu: {{READING_REQUEST}}
{{#IF QUESTION}}Câu hỏi cụ thể: {{QUESTION}}{{/IF}}

Luận giải {{DEPTH_GUIDE}}
```

### 10.3 Prompt Tối Ưu Theo Chủ Đề Bài Viết

| Chủ đề | Điểm nhấn Prompt | Model tốt nhất |
|--------|-----------------|----------------|
| **Tiết khí** | Thêm ngữ cảnh: ngày tiết khí, truyền thống dân gian VN | deepseek-chat |
| **Lễ hội** | Thêm: tỉnh/thành, tên lễ hội, ngày âm lịch | deepseek-chat |
| **Nhân vật lịch sử** | Yêu cầu: nguồn sử liệu, thời đại, công trạng | claude-3.5-sonnet |
| **Phong thuỷ** | Thêm: hệ thống ngũ hành, bát quái liên quan | claude-3.5-sonnet |
| **Ngày tốt/xấu** | Thêm: can chi ngày, sao chiếu, giờ hoàng đạo | gpt-4o-mini |

---

## 11. Quản Lý Chi Phí & Rate Limiting

### 11.1 Ước Tính Chi Phí

| Hoạt động | Token/lần | Model | Giá/1M tokens | Chi phí/lần |
|-----------|----------|-------|--------------|------------|
| Viết bài ngắn (500 từ) | ~1,500 tokens | deepseek-chat | $0.14 | ~$0.00021 |
| Viết bài trung (1500 từ) | ~3,000 tokens | deepseek-chat | $0.14 | ~$0.00042 |
| Viết bài dài (3000 từ) | ~5,000 tokens | deepseek-chat | $0.14 | ~$0.00070 |
| Tử vi brief | ~800 tokens | gpt-4o-mini | $0.15 | ~$0.00012 |
| Tử vi standard | ~1,500 tokens | claude-3.5-sonnet | $3.00 | ~$0.0045 |
| Tử vi detailed | ~2,500 tokens | claude-3.5-sonnet | $3.00 | ~$0.0075 |
| Chat message | ~500 tokens | gpt-4o-mini | $0.15 | ~$0.000075 |

**Ước tính ngân sách tháng:**
| Khối lượng | Ước tính |
|-----------|---------|
| 500 bài viết trung bình | ~$0.21 |
| 1,000 bài viết trung bình | ~$0.42 |
| 3,000 tử vi standard | ~$13.50 |
| 5,000 tử vi standard | ~$22.50 |
| 10,000 chat messages | ~$0.75 |
| **Tổng ~1,000 bài + 3,000 tử vi** | **~$14–25/tháng** |

### 11.2 Chiến Lược Tiết Kiệm Chi Phí

```
1. Cache thông minh:
   - Cache tử vi theo (birth_year, birth_month, birth_day, reading_type, target_year) — TTL 24h
   - Cùng bát tự + cùng loại xem → trả cache, không gọi AI lại

2. Model tiered:
   - Khách/free → model rẻ (gpt-4o-mini, deepseek) + depth=brief
   - Premium → model tốt (claude-3.5-sonnet) + depth=detailed

3. Rate limiting:
   - Khách: 1 lượt/ngày/IP
   - Free: 3 lượt/ngày/user
   - Premium: 20 lượt/ngày/user

4. Bài viết:
   - Chỉ admin được tạo bài AI (không giới hạn số lượng)
   - Budget cap: nếu chi phí tháng > $X, tắt model đắt, dùng model rẻ hơn

5. Streaming:
   - Dùng streaming SSE giúp user thấy kết quả ngay, giảm timeout
   - Không lưu request trùng lặp
```

### 11.3 Redis Cache Keys

```
ai:horoscope:{session_key_hash}       → TTL 24h
ai:article:topics:suggest:{date}      → TTL 6h
ai:quota:horoscope:{user_id}:{date}   → TTL đến 00:00 hôm sau
ai:quota:horoscope:ip:{ip}:{date}     → TTL đến 00:00 hôm sau
ai:chat:session:{session_uuid}        → TTL 30 ngày
```

---

## 12. Cấu Trúc Mã Nguồn Mới

### 12.1 Backend — Files Mới

```
backend/
├── internal/
│   ├── config/
│   │   └── ai_config.go                    ← AI configuration struct
│   ├── models/
│   │   └── ai_models.go                    ← AIGenerationLog, AIHoroscopeSession, etc.
│   ├── dto/
│   │   ├── ai_article_dto.go               ← Request/Response DTOs viết bài
│   │   ├── ai_horoscope_dto.go             ← Request/Response DTOs tử vi
│   │   └── ai_chat_dto.go                  ← Request/Response DTOs chat
│   ├── repositories/
│   │   ├── ai_log_repo.go                  ← CRUD AIGenerationLog
│   │   ├── ai_horoscope_repo.go            ← CRUD AIHoroscopeSession + quota
│   │   └── ai_chat_repo.go                 ← CRUD AIChatSession
│   ├── services/
│   │   ├── openrouter_service.go           ← OpenRouter HTTP client (Complete + Stream)
│   │   ├── ai_article_service.go           ← Viết bài AI + prompt builder
│   │   ├── ai_horoscope_service.go         ← Tử vi AI + rate limit check
│   │   ├── ai_chat_service.go              ← Chat session management
│   │   └── ai_prompt_service.go            ← CRUD prompt templates
│   ├── handlers/
│   │   ├── ai_article_handler.go           ← POST generate, GET drafts, PATCH review
│   │   ├── ai_horoscope_handler.go         ← POST read, GET history, GET quota
│   │   ├── ai_chat_handler.go              ← CRUD sessions, POST message (stream)
│   │   └── ai_admin_handler.go             ← Stats, logs, cost report, prompts
│   ├── middleware/
│   │   └── ai_rate_limit.go                ← Middleware kiểm tra AI quota
│   └── routes/
│       └── v4_routes.go                    ← Routes v4 AI endpoints
├── migrations/
│   ├── 000025_create_ai_tables.up.sql
│   └── 000025_create_ai_tables.down.sql
└── cmd/server/main.go                      ← Wiring DI mới
```

### 12.2 Frontend — Files Mới

```
frontend/src/
├── app/
│   ├── (home)/
│   │   ├── tu-vi-ai/
│   │   │   └── page.tsx                    ← /tu-vi-ai (Public)
│   │   └── chat-phong-thuy/
│   │       └── page.tsx                    ← /chat-phong-thuy (Auth required)
│   └── admin/
│       ├── ai-articles/
│       │   └── page.tsx                    ← /admin/ai-articles (Admin)
│       └── ai-dashboard/
│           └── page.tsx                    ← /admin/ai-dashboard (Admin)
├── components/
│   └── ai/
│       ├── AIArticleGenerator.tsx          ← Form + streaming preview tạo bài
│       ├── AIArticleDraftList.tsx          ← Table quản lý draft AI
│       ├── AIArticleReviewDialog.tsx       ← Dialog duyệt bài
│       ├── AIHoroscopeForm.tsx             ← Form nhập thông tin tử vi
│       ├── AIHoroscopeResult.tsx           ← Card hiển thị bát tự + AI result
│       ├── AIStreamingText.tsx             ← Reusable: SSE streaming text display
│       ├── AIChatWindow.tsx                ← Chat interface (bubble UI)
│       ├── AIChatMessage.tsx               ← Một tin nhắn chat
│       ├── AIUsageQuota.tsx                ← Badge "Còn X lượt hôm nay"
│       ├── AIModelSelector.tsx             ← Dropdown chọn model (admin only)
│       ├── AIStats.tsx                     ← Charts thống kê AI (admin)
│       └── AICostReport.tsx                ← Báo cáo chi phí (admin)
├── services/
│   └── aiService.ts                        ← axios API calls + SSE helpers
├── hooks/
│   └── useAI.ts                            ← React Query + useAI hooks
└── types/
    └── ai.ts                               ← TypeScript interfaces
```

### 12.3 Environment Variables Mới

```bash
# .env (thêm vào)

# OpenRouter AI
OPENROUTER_API_KEY=sk-or-v1-...
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
SITE_URL=https://lichso.vn
SITE_NAME=Lịch Số

# AI Model Defaults
AI_ARTICLE_MODEL=deepseek/deepseek-chat
AI_HOROSCOPE_MODEL=anthropic/claude-3.5-sonnet
AI_CHAT_MODEL=openai/gpt-4o-mini

# AI Token Limits
AI_MAX_TOKENS_ARTICLE=4096
AI_MAX_TOKENS_HOROSCOPE=2048
AI_MAX_TOKENS_CHAT=1024

# AI Rate Limits (per day)
AI_RATE_HOROSCOPE_GUEST=1
AI_RATE_HOROSCOPE_FREE=3
AI_RATE_HOROSCOPE_PREMIUM=20

# AI Budget Cap (USD/month) - tắt model đắt khi vượt ngưỡng
AI_MONTHLY_BUDGET_CAP=50
```

---

## 13. Kế Hoạch Triển Khai — Roadmap Phase 25–28

### Phase 25 — OpenRouter Core & AI Horoscope (3 tuần)

**Mục tiêu**: Tích hợp OpenRouter, triển khai tính năng tử vi AI

**Backend:**
- [ ] Tạo `AIConfig` và load từ environment
- [ ] Implement `OpenRouterService` (Complete + Stream)
- [ ] Migration `000025_create_ai_tables`
- [ ] Models: `AIGenerationLog`, `AIHoroscopeSession`, `AIUsageQuota`
- [ ] Repositories: `AILogRepo`, `AIHoroscopeRepo`
- [ ] `AIHoroscopeService` — tích hợp với `HoroscopeService` hiện có
- [ ] `AIRateLimit` middleware
- [ ] `AIHoroscopeHandler` với streaming SSE
- [ ] Routes v4: `/api/v4/ai/horoscope/*`
- [ ] Wiring trong `cmd/server/main.go`

**Frontend:**
- [ ] TypeScript types (`types/ai.ts`)
- [ ] `aiService.ts` — API calls + SSE fetch helper
- [ ] `useAI.ts` React Query hooks
- [ ] `AIStreamingText.tsx` — Reusable streaming component
- [ ] `AIHoroscopeForm.tsx` — Form nhập thông tin
- [ ] `AIHoroscopeResult.tsx` — Hiển thị bát tự + AI result
- [ ] `AIUsageQuota.tsx` — Badge hạn mức
- [ ] Trang `/tu-vi-ai` public
- [ ] Tích hợp vào trang `/tu-vi` hiện có (thêm tab "Tử Vi AI")

---

### Phase 26 — AI Article Generator (3 tuần)

**Mục tiêu**: Admin có thể tạo bài viết tự động bằng AI

**Backend:**
- [ ] `AIArticleService` — build prompt, gọi OpenRouter, parse kết quả
- [ ] `AIPromptService` — CRUD prompt templates
- [ ] `AIArticleHandler` — generate (stream), quick-draft, review
- [ ] `AIPromptTemplate` model + migration (thêm vào 000025 hoặc 000026)
- [ ] Cập nhật `articles` table (thêm ai_generated, ai_model, ai_tokens_used, ai_cost_usd)
- [ ] Routes v4: `/api/v4/ai/articles/*`
- [ ] Endpoint gợi ý chủ đề: `/api/v4/ai/articles/topics/suggest`

**Frontend:**
- [ ] `AIArticleGenerator.tsx` — Form cấu hình + streaming preview
- [ ] `AIModelSelector.tsx` — Dropdown chọn model (admin)
- [ ] `AIArticleDraftList.tsx` — Bảng danh sách bài draft
- [ ] `AIArticleReviewDialog.tsx` — Dialog review + duyệt bài
- [ ] Trang `/admin/ai-articles`
- [ ] Tích hợp nút "🤖 Tạo với AI" vào form bài viết hiện có

---

### Phase 27 — AI Chat & Admin Dashboard (2 tuần)

**Mục tiêu**: Chat tư vấn phong thuỷ + Dashboard thống kê AI

**Backend:**
- [ ] `AIChatService` — session management, message history, context injection
- [ ] `AIChatSession` model + repository
- [ ] `AIChatHandler` — CRUD sessions, stream message
- [ ] `AIAdminHandler` — stats, logs, cost report
- [ ] Routes v4: `/api/v4/ai/chat/*`, `/api/v4/admin/ai/*`

**Frontend:**
- [ ] `AIChatWindow.tsx` — Chat UI với lịch sử tin nhắn
- [ ] `AIChatMessage.tsx` — Bubble tin nhắn (user + AI)
- [ ] Trang `/chat-phong-thuy`
- [ ] `AIStats.tsx` — Charts tổng quan
- [ ] `AICostReport.tsx` — Báo cáo chi phí theo ngày/tháng
- [ ] Trang `/admin/ai-dashboard`
- [ ] Thêm "AI Dashboard" vào Admin Sidebar

---

### Phase 28 — Auto Content Schedule & Optimization (2 tuần)

**Mục tiêu**: Tự động tạo bài theo lịch, tối ưu chi phí

**Backend:**
- [ ] Cron job: tự động tạo bài viết theo ngày lễ/tiết khí sắp đến (7 ngày trước)
- [ ] Budget monitoring: alert khi chi phí tháng > 80% cap
- [ ] Model fallback tự động khi primary model down
- [ ] A/B testing prompt templates (tracking conversion rate)
- [ ] Redis pipeline: batch cache warming cho tử vi theo mùa

**Frontend:**
- [ ] `AIScheduleConfig.tsx` — Cài đặt lịch tự động tạo bài
- [ ] Budget alert banner trong Admin
- [ ] Prompt template editor trong Admin
- [ ] Thêm badge "✨ AI" cho bài viết do AI tạo (public display)

---

### Timeline Tổng Quan Giai Đoạn 4

```
Tháng 3/2026: Phase 25 — OpenRouter Core + AI Horoscope (3 tuần)
Tháng 4/2026: Phase 26 — AI Article Generator (3 tuần)
Tháng 4/2026: Phase 27 — AI Chat + Admin Dashboard (2 tuần, overlap)
Tháng 5/2026: Phase 28 — Auto Schedule + Optimization (2 tuần)

Tổng: ~10 tuần (~2.5 tháng)
Release v4.0 Alpha (Tử Vi AI): Cuối tháng 3/2026
Release v4.0 Beta (Viết Bài AI): Cuối tháng 4/2026
Release v4.0 Stable: Giữa tháng 5/2026
```

### Checklist Trước Khi Release

```
Backend:
□ go build ./... — PASS
□ go vet ./... — PASS
□ Unit tests cho OpenRouterService (mock HTTP)
□ Unit tests cho AIHoroscopeService
□ Integration test với OpenRouter sandbox
□ Rate limiting test (quota đúng theo user type)
□ Streaming SSE test
□ Error handling (API key invalid, timeout, model error)

Frontend:
□ npx tsc --noEmit — PASS
□ eslint — PASS
□ Streaming text render đúng Markdown
□ Loading states + skeleton UI
□ Error states (hết quota, API lỗi)
□ Mobile responsive (form tử vi, chat)
□ SEO: meta tags cho trang /tu-vi-ai

Security:
□ API key không expose ra Frontend
□ Rate limit không bypass qua IP spoofing
□ Input sanitization (question field)
□ Content moderation (lọc prompt injection)

Performance:
□ Streaming response < 1s first token
□ Cache hit rate > 60% cho tử vi
□ Redis quota check < 5ms
```

---

> 📌 **Cập nhật lần cuối**: 2026-03-12
> 📊 **Trạng thái**: Giai đoạn 4 — Lên kế hoạch, chưa bắt đầu triển khai
> 🔗 **Tài liệu liên quan**: [LICHSO-V3-DOCUMENT.md](./LICHSO-V3-DOCUMENT.md) | [Roadmap.md](./Roadmap.md)
