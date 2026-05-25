# 📱 LỊCH SỐ — Tài Liệu Dự Án Chi Tiết

> **Phiên bản tài liệu:** 3.0  
> **Cập nhật:** 25/05/2026  
> **Nền tảng:** Android (Native — Jetpack Compose)  
> **Phiên bản ứng dụng hiện tại:** v2.0.4 (versionCode 33)  
> **Trạng thái:** Production — Đang phát hành

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Tầm nhìn & Mục tiêu](#2-tầm-nhìn--mục-tiêu)
3. [Đối tượng người dùng](#3-đối-tượng-người-dùng)
4. [Kiến trúc ứng dụng](#4-kiến-trúc-ứng-dụng)
5. [Cấu trúc màn hình & Navigation](#5-cấu-trúc-màn-hình--navigation)
6. [Chi tiết tính năng](#6-chi-tiết-tính-năng)
7. [Hệ thống Points & Gamification](#7-hệ-thống-points--gamification)
8. [Widget Màn Hình Chính](#8-widget-màn-hình-chính)
9. [Hệ thống Design System](#9-hệ-thống-design-system)
10. [Trợ lý AI — Kiến trúc & Tính năng](#10-trợ-lý-ai--kiến-trúc--tính-năng)
11. [Mô hình dữ liệu (Data Models)](#11-mô-hình-dữ-liệu-data-models)
12. [Thông báo & Nhắc nhở](#12-thông-báo--nhắc-nhở)
13. [Yêu cầu phi chức năng](#13-yêu-cầu-phi-chức-năng)
14. [Phụ lục kỹ thuật](#14-phụ-lục-kỹ-thuật)

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1. Giới thiệu

**Lịch Số** là ứng dụng Android kết hợp **Lịch Vạn Niên** truyền thống Việt Nam với **trợ lý AI phong thuỷ thông minh**, cung cấp trải nghiệm quản lý thời gian và văn hoá toàn diện:

- Lịch dương — âm lịch đồng bộ với hiệu ứng 3D page flip
- Tra cứu can chi, tiết khí, giờ hoàng đạo, hướng xuất hành, sao chiếu
- Thời tiết thực tế tích hợp ngay màn hình chính
- Quản lý công việc, ghi chú, nhắc nhở (hỗ trợ âm lịch)
- Trợ lý AI Phong Thuỷ chat tích hợp — tư vấn dựa trên lịch vạn niên
- Hệ thống AI Template cho các tác vụ lặp lại
- Các công cụ phong thuỷ: La bàn số, Thước Lỗ Ban, Bát Trạch
- Quẻ I Ching (Kinh Dịch) hàng ngày
- Cây gia phả số (Family Tree)
- Hệ thống điểm & danh hiệu gamification (Points v2)
- 7 loại widget cho màn hình chính Android

### 1.2. Định vị sản phẩm

| Thuộc tính | Giá trị |
|---|---|
| **Tên ứng dụng** | Lịch Số — Lịch Vạn Niên |
| **Nền tảng** | Android (API 26+, Android 8.0 Oreo trở lên) |
| **Ngôn ngữ chính** | Tiếng Việt (en support trong resource) |
| **Phong cách UI** | Material Design 3 — Red Gradient Headers + Gold/Teal accents |
| **Phân loại** | Productivity / Lifestyle / Calendar |
| **Phát triển bởi** | Zenix Labs |
| **versionCode** | 33 / versionName: 2.0.4 |

---

## 2. TẦM NHÌN & MỤC TIÊU

### 2.1. Tầm nhìn

Trở thành ứng dụng lịch **#1 Việt Nam** — kết hợp giá trị văn hóa truyền thống với công nghệ AI hiện đại, phục vụ hàng triệu người dùng Việt Nam trong cuộc sống hàng ngày.

### 2.2. Mục tiêu kinh doanh

| Chỉ số | Mục tiêu Q2/2026 | Mục tiêu cuối năm 2026 |
|---|---|---|
| Lượt cài đặt | 100,000 | 500,000 |
| DAU (Daily Active Users) | 30,000 | 150,000 |
| Tỷ lệ giữ chân (D7) | 45% | 55% |
| Đánh giá Google Play | ≥ 4.5⭐ | ≥ 4.6⭐ |

### 2.3. Mục tiêu kỹ thuật

- **Thời gian khởi động (Cold Start):** < 1.5 giây
- **Kích thước APK:** < 25MB (App Bundle với ABI/language splits)
- **Offline-first:** Toàn bộ tính năng lịch hoạt động offline
- **Hiệu năng cuộn lịch:** 60 FPS không drop frame
- **Thời gian phản hồi AI:** < 3 giây cho câu trả lời đầu tiên

---

## 3. ĐỐI TƯỢNG NGƯỜI DÙNG

### 3.1. Persona chính

#### Persona 1: Người dùng truyền thống (40–65 tuổi)
- **Nhu cầu:** Xem lịch âm, tra ngày tốt xấu, xem giờ hoàng đạo, văn khấn
- **Tần suất:** Hàng ngày, đặc biệt vào đầu tháng âm, ngày rằm
- **Kỳ vọng:** Giao diện rõ ràng, thông tin chính xác, văn khấn đầy đủ

#### Persona 2: Người dùng hiện đại (25–40 tuổi)
- **Nhu cầu:** Quản lý công việc kết hợp xem lịch truyền thống, AI tư vấn
- **Tần suất:** Hàng ngày
- **Kỳ vọng:** UI đẹp, tích hợp AI, widget tiện lợi, gamification thú vị

#### Persona 3: Doanh nhân / Kinh doanh
- **Nhu cầu:** Chọn ngày tốt cho giao dịch, ký hợp đồng, khai trương, nhập trạch
- **Tần suất:** Hàng tuần
- **Kỳ vọng:** Tư vấn chính xác, công cụ chọn ngày đại sự, nhắc nhở đúng lúc

#### Persona 4: Phụ nữ (20–40 tuổi)
- **Nhu cầu:** Theo dõi chu kỳ, lên kế hoạch sinh, lịch âm dương
- **Tần suất:** Hàng tháng
- **Kỳ vọng:** Cycle tracker, birth date planner, tư vấn phong thuỷ sức khoẻ

---

## 4. KIẾN TRÚC ỨNG DỤNG

### 4.1. Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │ Calendar  │ │  Tasks   │ │ AI Chat  │ │ Tools / Points│  │
│  │  Screen   │ │  Screen  │ │  Screen  │ │   Screens     │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───────┬───────┘  │
│       └────────────┴────────────┴───────────────┘           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          VIEWMODEL LAYER (MVVM + StateFlow)           │   │
│  │  HomeVM │ CalendarVM │ TasksVM │ ChatVM │ PointsVM   │   │
│  │  FamilyTreeVM │ PrayersVM │ BookmarksVM │ SettingsVM │   │
│  └────────────────────────┬─────────────────────────────┘   │
├───────────────────────────┼─────────────────────────────────┤
│                  DOMAIN LAYER                                │
│  ┌────────────────────────┴─────────────────────────────┐   │
│  │              CALCULATORS & USE CASES                  │   │
│  │  LunarCalendarUtil │ CanChiCalculator │ TietKhi       │   │
│  │  GioHoangDao │ DayActivity │ SaoChieu │ TrucNgay      │   │
│  │  FengShuiCalculators │ DatePickerScorer               │   │
│  │  DateMathLogic │ MoonPhaseCalculator                   │   │
│  │  Points UseCases │ StreakTier │ PermanentRank          │   │
│  └────────────────────────┬─────────────────────────────┘   │
├───────────────────────────┼─────────────────────────────────┤
│                   DATA LAYER                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │ Room DB       │ │  OkHttp      │ │  DataStore Prefs    │  │
│  │ (Local-first)│ │  (OpenRouter │ │  (Settings,          │  │
│  │              │ │  /Weather)   │ │   Profile, Auth)     │  │
│  └──────────────┘ └──────────────┘ └─────────────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │  WorkManager │ │   Firebase   │ │  AlarmManager        │  │
│  │  (Widget     │ │  (Auth +     │ │  (Exact reminders)   │  │
│  │   updates)   │ │   Analytics) │ │                      │  │
│  └──────────────┘ └──────────────┘ └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 4.2. Tech Stack thực tế

| Layer | Công nghệ | Phiên bản |
|---|---|---|
| **Language** | Kotlin | JVM 17 |
| **UI Framework** | Jetpack Compose | BOM 2025.04.00 |
| **Architecture** | MVVM + Clean Architecture | — |
| **DI** | Hilt (Dagger) | 2.55 |
| **Local DB** | Room | 2.6.1 |
| **Preferences** | DataStore Preferences | 1.1.2 |
| **Networking** | OkHttp | 4.12.0 |
| **Serialization** | Gson | 2.11.0 |
| **Image** | Coil | 2.7.0 |
| **Navigation** | Compose Navigation | 2.8.9 |
| **Async** | Kotlin Coroutines + StateFlow | — |
| **Background** | WorkManager | 2.10.0 |
| **AI** | OpenRouter API (multi-model) | — |
| **Auth** | Firebase Auth + Credential Manager | firebase-bom 33.7.0 |
| **Analytics** | Firebase Analytics | — |
| **In-App Update** | Play App Update | 2.1.0 |
| **In-App Review** | Play Review | 2.0.2 |
| **Badge** | ShortcutBadger | 1.1.22 |
| **Desugaring** | desugar_jdk_libs | 2.1.4 |
| **compileSdk / targetSdk** | 35 | — |
| **minSdk** | 26 (Android 8.0) | — |

### 4.3. Cấu trúc Package thực tế

```
com.lichso.app/
├── MainActivity.kt                    # Entry point, deep link handler
├── LichSoApp.kt                       # Application class
├── di/
│   └── AppModule.kt                   # Hilt DI modules
├── analytics/
│   └── Analytics.kt                   # Event logging
├── data/
│   ├── ai/
│   │   ├── AiTaskService.kt           # Background AI processing
│   │   ├── AiTemplates.kt             # AI prompt templates
│   │   └── AiMemoryStore.kt           # AI context memory
│   ├── auth/
│   │   └── AuthRepository.kt          # Firebase Auth + Google Sign-In
│   ├── local/
│   │   ├── LichSoDatabase.kt          # Room DB (v25+ migrations)
│   │   ├── AppBackupManager.kt        # JSON backup/restore
│   │   ├── FamilyTreeRepository.kt
│   │   ├── FamilyTreeExportImport.kt
│   │   ├── entity/
│   │   │   ├── Entities.kt            # Task, Note, Reminder, Bookmark, Chat, Countdown, WorldClock, Notification
│   │   │   ├── FamilyTreeEntities.kt  # FamilyMember, Relationship, MemorialDay, Photo
│   │   │   └── PointsEntities.kt      # ActionLog, PointsState, Unlock, DailyUnlock
│   │   └── dao/
│   │       ├── TaskDao.kt / NoteDao.kt / ReminderDao.kt
│   │       ├── BookmarkDao.kt / ChatMessageDao.kt
│   │       ├── CountdownEventDao.kt / WorldClockCityDao.kt
│   │       ├── NotificationDao.kt / StreakDao.kt
│   │       ├── FamilyTreeDao.kt
│   │       ├── PointsDao.kt / UnlockDao.kt
│   │       └── CycleDao.kt
│   ├── remote/
│   │   ├── OpenRouterApi.kt           # LLM API (GPT-4/Claude/Mistral...)
│   │   ├── WeatherApi.kt              # Weather fetching
│   │   └── WeatherRepository.kt
│   └── settings/
│       └── AppSettingsRepository.kt   # DataStore preferences
├── domain/
│   ├── model/
│   │   ├── DayInfo.kt                 # Full day info aggregate
│   │   ├── WeatherInfo.kt
│   │   ├── CityCoordinates.kt
│   │   └── HistoricalEvent.kt
│   ├── DayInfoProvider.kt             # Orchestrates all calculators
│   └── HistoricalEventProvider.kt    # "Ngày này năm xưa" data
├── feature/
│   ├── birthplanner/
│   │   └── BirthDatePlannerScreen.kt
│   ├── countdown/
│   │   ├── CountdownScreen.kt
│   │   └── CountdownViewModel.kt
│   ├── cycle/
│   │   ├── CycleTrackerScreen.kt
│   │   └── CycleTrackerViewModel.kt
│   ├── datepicker/
│   │   ├── DatePickerToolScreen.kt
│   │   ├── DatePickerToolViewModel.kt
│   │   └── DatePickerScorer.kt        # Day scoring algorithm
│   ├── datemath/
│   │   ├── DateMathScreen.kt
│   │   └── DateMathLogic.kt
│   ├── fengshui/
│   │   ├── CompassScreen.kt           # Digital compass + daily directions
│   │   ├── LoBanScreen.kt             # Thước Lỗ Ban
│   │   ├── BatTrachScreen.kt          # Bát trạch cung phi
│   │   └── FengShuiCalculators.kt     # Core algorithms
│   ├── points/
│   │   ├── domain/
│   │   │   ├── Models.kt              # PointsBalance, StreakState, LedgerEntry...
│   │   │   ├── ActionType.kt          # 38 action types
│   │   │   ├── PermanentRank.kt       # 7 ranks + unlocks
│   │   │   ├── StreakTier.kt          # 5 tiers + multipliers
│   │   │   ├── Zodiac.kt              # 27 collectible cards
│   │   │   ├── Oracle.kt              # I Ching / Kinh Dịch 20 quẻ
│   │   │   ├── DailyUnlockKey.kt      # 9 daily feature gates
│   │   │   ├── UseCases.kt            # Points logic use cases
│   │   │   └── Clock.kt               # Midnight reset clock
│   │   ├── data/
│   │   │   ├── PointsRepository.kt
│   │   │   └── ZodiacCollectionStore.kt
│   │   └── ui/
│   │       ├── PointsViewModel.kt
│   │       ├── PointsWidgets.kt       # PointsPill, StreakBadge, RankProgressCard
│   │       ├── OracleScreens.kt       # Draw + Result screens
│   │       ├── ZodiacCollectionScreen.kt
│   │       ├── DailyUnlockStoreScreen.kt
│   │       ├── LedgerScreen.kt
│   │       ├── StreakFreezeScreen.kt
│   │       ├── RankUpDialog.kt
│   │       └── PointsTutorialScreen.kt
│   ├── tietkhi/
│   │   ├── TietKhiScreen.kt
│   │   └── TietKhiCatalog.kt
│   └── worldclock/
│       ├── WorldClockScreen.kt
│       └── WorldClockViewModel.kt
├── notification/
│   ├── NotificationScheduler.kt       # AlarmManager scheduling
│   ├── NotificationReceiver.kt        # BroadcastReceiver
│   ├── NotificationHelper.kt          # Notification channel/builder
│   ├── BootReceiver.kt                # Reschedule after reboot
│   ├── SmartReminderProvider.kt       # Context-aware reminders
│   ├── PersonalHoroscopeHelper.kt     # Daily horoscope notifications
│   └── AppUpdateChecker.kt / AppIconBadgeManager.kt
├── quicksettings/
│   └── LichSoQuickSettingsTileService.kt
├── update/
│   └── InAppUpdateManager.kt          # Play Store in-app update
├── ui/
│   ├── LichSoMainScreen.kt            # Navigation host + drawer + bottom nav
│   ├── theme/
│   │   ├── Theme.kt / Color.kt / Type.kt
│   │   └── SolarTermPalette.kt        # Seasonal theme colors
│   ├── icons/
│   │   └── PrayerIcons.kt
│   └── screen/
│       ├── home/           # HomeScreen + HomeViewModel + WeatherDetailSheet
│       ├── calendar/       # CalendarScreen + DayDetailScreen + DayDetailOverlay + DayActionsViewModel + DayActionsDialogs
│       ├── tasks/          # TasksScreen3 + TasksViewModel + NoteTaskEditScreen
│       ├── chat/           # AIChatScreen + ChatViewModel + ChatIcons
│       ├── settings/       # SettingsScreen + SettingsViewModel
│       ├── tools/          # ToolsScreen + WidgetManagerScreen
│       ├── profile/        # ProfileScreen + ProfileViewModel + RankBadgeSection
│       ├── prayers/        # PrayersScreen + PrayersViewModel + PrayerIcons
│       ├── bookmarks/      # BookmarksScreen + BookmarksViewModel
│       ├── search/         # SearchScreen + SearchViewModel
│       ├── history/        # ThisDayInHistoryScreen + ThisDayInHistoryViewModel
│       ├── notifications/  # NotificationScreen + NotificationViewModel
│       ├── templates/      # TemplatesScreen + TemplatesViewModel
│       ├── gooddays/       # GoodDaysScreen
│       ├── familytree/     # FamilyTreeScreen + AddMemberScreen + MemberDetailScreen + MemorialDetailScreen + FamilyTreeViewModel + FamilySettingsScreen + PickMemberScreen
│       ├── onboarding/     # OnboardingScreen
│       └── splash/         # SplashScreen
├── util/
│   ├── LunarCalendarUtil.kt
│   ├── CanChiCalculator.kt
│   ├── TietKhiCalculator.kt
│   ├── GioHoangDaoCalculator.kt
│   ├── TrucNgayCalculator.kt
│   ├── SaoChieuCalculator.kt
│   ├── DayActivityCalculator.kt
│   ├── MoonPhaseCalculator.kt
│   ├── HolidayUtil.kt
│   └── SmartRatingManager.kt
├── widget/
│   ├── CalendarWidget.kt              # 2×2 day widget
│   ├── ClockWidget.kt                 # 4×2 clock+weather widget
│   ├── ClockWidget2.kt                # 4×2 light/dark variant
│   ├── MonthCalendarWidget.kt         # 4×4 month grid widget
│   ├── CountdownWidget.kt             # 3×2 countdown widget
│   ├── AiWidget.kt                    # 2×2 AI quick access widget
│   ├── CanhGioWidget.kt               # 4×1 earthly branch widget
│   ├── CalendarWidgetScheduler.kt     # Midnight update scheduler
│   ├── CalendarWidgetUpdateWorker.kt  # WorkManager worker
│   ├── DarkModeWidgetObserver.kt
│   ├── WidgetWeatherHelper.kt
│   └── WidgetPinHelper.kt
└── data/
    └── VietnameseQuotes.kt            # Daily quotes dataset
```

---

## 5. CẤU TRÚC MÀN HÌNH & NAVIGATION

### 5.1. Sơ đồ Navigation tổng quan

```
SplashScreen
    │
    ▼
OnboardingScreen (lần đầu)
    │
    ▼
LichSoMainScreen (Navigation Host)
├── Modal Drawer
│   ├── Trang Chủ (Home)
│   ├── Ngày Đã Lưu (Bookmarks)
│   ├── Ngày Này Năm Xưa (History)
│   ├── Cây Gia Phả (Family Tree)
│   ├── Cài Đặt (Settings)
│   ├── Đánh Giá / Chia Sẻ / Bảo Mật
│   └── Footer: Zenix Labs + versionName
│
├── Bottom Navigation (5 items)
│   ├── 📅 Lịch Tháng (Calendar)
│   ├── 📝 Ghi Chú (Tasks)
│   ├── [●] Today button (center, raised)
│   ├── 🙏 Văn Khấn (Prayers)
│   └── 🛠 Tiện Ích (Tools)
│
└── Screens (30+)
    ├── home             → HomeScreen
    ├── calendar         → CalendarScreen
    ├── day_detail/{date} → DayDetailScreen (slide-up)
    ├── tasks            → TasksScreen3
    ├── note_edit/{id}   → NoteTaskEditScreen
    ├── ai_chat          → AIChatScreen
    ├── prayers          → PrayersScreen
    ├── tools            → ToolsScreen
    ├── good_days        → GoodDaysScreen
    ├── date_picker_tool → DatePickerToolScreen
    ├── tiet_khi         → TietKhiScreen
    ├── date_math        → DateMathScreen
    ├── world_clock      → WorldClockScreen
    ├── birth_planner    → BirthDatePlannerScreen
    ├── cycle_tracker    → CycleTrackerScreen
    ├── countdown        → CountdownScreen
    ├── compass          → CompassScreen
    ├── lo_ban           → LoBanScreen
    ├── bat_trach        → BatTrachScreen
    ├── family_tree      → FamilyTreeScreen
    ├── add_member/{id}  → AddMemberScreen
    ├── member_detail/{id} → MemberDetailScreen
    ├── memorial_detail/{id} → MemorialDetailScreen
    ├── bookmarks        → BookmarksScreen
    ├── search           → SearchScreen
    ├── history          → ThisDayInHistoryScreen
    ├── notifications    → NotificationScreen
    ├── templates        → TemplatesScreen
    ├── profile          → ProfileScreen
    ├── settings         → SettingsScreen
    ├── widget_manager   → WidgetManagerScreen
    ├── points/ledger    → LedgerScreen
    ├── points/oracle    → OracleDrawScreen → OracleResultScreen
    ├── points/zodiac    → ZodiacCollectionScreen
    ├── points/store     → DailyUnlockStoreScreen
    ├── points/freeze    → StreakFreezeScreen
    └── points/tutorial  → PointsTutorialScreen
```

### 5.2. Deep Links

| URI | Destination |
|---|---|
| `lichso://streak-gift?token=xxx` | Streak freeze gift redemption |
| `lichso://ai_chat` | Mở AI Chat từ widget |
| `lichso://calendar` | Mở Calendar từ widget |
| Mỗi ActionType có `deeplink` riêng | Navigate trực tiếp đến feature |

---

## 6. CHI TIẾT TÍNH NĂNG

### 6.1. MÀN HÌNH HOME — Trang Chủ

Màn hình chính kết hợp lịch vạn niên + thời tiết + thông tin ngày, sử dụng hiệu ứng **3D Page Flip** để chuyển trang.

#### 6.1.1. Header

| Thành phần | Mô tả |
|---|---|
| **Menu icon** | Mở drawer navigation |
| **Weather Chip** | Hiển thị nhiệt độ thực + icon thời tiết, tap → WeatherDetailSheet |
| **Notifications icon** | Số thông báo chưa đọc |
| **Avatar** | Avatar người dùng (click → Profile) |
| **Settings icon** | Shortcut tới Settings |
| **Points Pill** | ⚡ daily + ☯ permanent, click → Ledger |
| **Streak Badge** | Số ngày streak + tier badge |

#### 6.1.2. WeatherDetailSheet (Bottom Sheet)

- Nhiệt độ thực / cảm giác / độ ẩm / gió / UV
- Chọn thành phố từ danh sách
- Lời khuyên Phong Thuỷ theo thời tiết
- Nút refresh dữ liệu thời tiết

#### 6.1.3. Smart Hint Banner

Banner contextual xuất hiện theo ngữ cảnh:
- Ngày đầu tháng âm
- Rằm / mùng một
- Ngày hoàng đạo / hắc đạo
- Tiết khí mới
- Ngày kỵ / ngày đặc biệt

#### 6.1.4. Khu vực ngày (3D Page Flip)

- **Vuốt dọc** để chuyển sang ngày/tháng khác (3D perspective, opacity fade)
- **Mini Calendar Strip**: 7 ô ngày trong tuần, hiện số dương + âm, click để chọn
- **Số ngày lớn**: Font serif, màu đỏ/gold theo trạng thái ngày
- **Lunar badge**: Ngày âm lịch + biểu tượng mặt trăng
- **Countdown Card**: Tối đa 2 sự kiện đếm ngược gần nhất
- **Quote**: Câu trích dẫn hàng ngày luân phiên

#### 6.1.5. Swipe Hint
- Animation hướng dẫn xuất hiện lần đầu, tự ẩn sau 4 giây

---

### 6.2. MÀN HÌNH CALENDAR — Lịch Tháng

#### 6.2.1. Calendar Grid

| Thuộc tính | Chi tiết |
|---|---|
| **Layout** | Grid 7 cột × 6 hàng |
| **Ô ngày** | Số dương + số âm nhỏ dưới |
| **Ngày hôm nay** | Background đỏ nổi bật |
| **Ngày được chọn** | Background nhạt |
| **Chủ nhật / Lễ** | Số đỏ |
| **Thứ 7** | Số teal |
| **Giờ hoàng đạo** | Chấm nhỏ góc trái trên |
| **Sự kiện / lễ** | Chấm nhỏ giữa dưới |
| **Bookmark** | Chấm đỏ góc phải trên |
| **Swipe ngang** | Chuyển tháng |

#### 6.2.2. Month/Year Navigation

- Nút `‹` / `›` chuyển tháng
- Tap vào tháng/năm → Month Picker (grid 3×4) hoặc Year Picker (grid 4×4)
- Jump to Date dialog: nhập tay ngày/tháng/năm

#### 6.2.3. Selected Day Panel (Inline)

Khi chọn ngày, hiện card nhỏ bên dưới grid:
- Ngày dương + âm lịch
- Điểm chất lượng ngày
- Sự kiện, ngày lễ gần
- Can Chi + Trực Ngày

#### 6.2.4. Day Detail Screen (Full-screen overlay)

Mở từ Calendar bằng tap vào selected day panel:

| Section | Nội dung |
|---|---|
| **Hero Header** | Red gradient + back/share/bookmark |
| **Day Actions Bar** | Bookmark toggle + Add Note + Add Reminder |
| **Info Grid 2×2** | Chất lượng ngày % | Hoa Văn | Can Chi | Mức độ hoạt động |
| **Giờ Hoàng Đạo** | 12 canh giờ tô màu theo tốt/xấu/trung |
| **Hoạt động** | Nên làm + Không nên làm |
| **Ghi chú / Tasks / Nhắc nhở** | Dữ liệu gắn với ngày đó |
| **Quote** | Câu trích dẫn |
| **Ask AI** | Button mở AI Chat với prompt ngày hôm đó |

---

### 6.3. MÀN HÌNH TASKS — Ghi Chú & Việc Làm

#### 6.3.1. 3 Tab chính

| Tab | Màu | Nội dung |
|---|---|---|
| **Nhắc nhở** | Orange (#E65100) | Danh sách reminders + toggle bật/tắt |
| **Ghi chú** | Blue (#1565C0) | Notes grid/list với màu sắc |
| **Công việc** | Green (#2E7D32) | Tasks với priority + checkbox |

#### 6.3.2. Tính năng chung

- **Stats row**: Badge đếm mỗi loại
- **Search bar**: Lọc real-time + clear
- **Filter sheet**: Lọc theo label & priority

#### 6.3.3. Màn hình chỉnh sửa (NoteTaskEditScreen)

**Note Editor:**
- Tiêu đề + nội dung (text area)
- Bộ chọn màu (6 màu preset)
- Ghim (pin) toggle
- Checklist trong note
- Label multi-select (Gia đình, Quan trọng, Công việc, ...)

**Task Editor:**
- Tiêu đề + mô tả
- Priority 1–5
- Ngày hết hạn + giờ
- Reminder toggle
- Checklist
- Labels

**Reminder Editor:**
- Tiêu đề + ngày + giờ (TimePicker dialog)
- Repeat type (Không / Hàng ngày / Hàng tuần / Hàng tháng / Hàng năm)
- Enabled toggle
- Dùng âm lịch toggle
- Báo trước N ngày
- Danh mục
- Ghi chú
- Labels

**Attached Date Info:** Hiển thị thông tin ngày + âm lịch + lễ khi tạo từ Calendar

---

### 6.4. MÀN HÌNH AI CHAT — Phong Thuỷ AI

#### 6.4.1. Giao diện chat

- **Header**: "Phong Thuỷ AI" + "Đang trực tuyến" + icon AI + nút xoá lịch sử
- **Bubbles**: User (phải, colored) / AI (trái, markdown rendered)
- **Typing indicator**: 3 chấm animation
- **Copy button**: Sao chép phản hồi AI
- **Auto-scroll**: Cuộn xuống khi có tin mới
- **Suggestion chips**: Gợi ý khi chưa có tin nhắn

#### 6.4.2. Profile Completion Banner

Hiển thị banner vàng nếu profile chưa đầy đủ (thiếu tên / ngày sinh / giới tính) với link tới màn Profile.

#### 6.4.3. Ngữ cảnh AI

AI nhận context đầy đủ:
- Tên, giới tính, ngày sinh, giờ sinh
- Ngày âm lịch, can chi, tuổi
- Cung phi bát trạch (nếu đã tính)
- Lịch sử chat (lưu trong DB)
- AI Memory Store cho context dài hạn

#### 6.4.4. Backend AI

- **OpenRouter API** — hỗ trợ nhiều model: GPT-4, Claude, Mistral, Llama, v.v.
- Model được cấu hình qua `AI_PROXY_BASE_URL` / `AI_PROXY_APP_ID` / `AI_PROXY_APP_SECRET`
- Tin nhắn được lưu vào `ChatMessageEntity` trong Room DB

---

### 6.5. MÀN HÌNH TOOLS — Tiện Ích

Grid các công cụ được phân thành 5 nhóm:

#### Nhóm 1: Lịch & Ngày Tốt
| Công cụ | Tính năng |
|---|---|
| Ngày tốt/xấu | Lọc ngày tốt theo mục đích trong tháng |
| Chọn ngày đại sự ⭐ | Top 5 ngày tốt nhất theo thuật toán scoring (unlock bằng điểm) |
| Xem tuổi hợp | Kiểm tra tương sinh/khắc giữa 2 năm sinh |
| Đổi ngày Âm/Dương | Chuyển đổi ngày âm lịch ↔ dương lịch |
| 24 Tiết Khí | Xem 24 tiết khí theo 4 mùa, ngày chính xác + ý nghĩa |
| Văn khấn | Thư viện văn khấn đầy đủ |

#### Nhóm 2: Phong Thuỷ & Nghi Lễ
| Công cụ | Tính năng |
|---|---|
| La bàn phong thủy 🆕 | La bàn số thực (sensor), hướng xuất hành hàng ngày |
| Thước Lỗ Ban 🆕 | Đánh giá kích thước theo 3 loại thước, 8 cung |
| Bát trạch 🆕 | Tính cung phi theo năm sinh + giới tính |
| Xông đất / Xuất hành / Sao hạn / Phi Tinh | AI tư vấn theo ngày |

#### Nhóm 3: Khám Phá & Giải Trí
| Công cụ | Tính năng |
|---|---|
| Cây gia phả | Quản lý phả hệ gia đình đầy đủ |
| Ngày này năm xưa | Sự kiện lịch sử theo ngày |
| Quay số | Rút quẻ Kinh Dịch hàng ngày |

#### Nhóm 4: Điểm & Thưởng
| Công cụ | Tính năng |
|---|---|
| Hành Động Hàng Ngày | Danh sách 38 hành động kiếm điểm |
| Kho Điểm Hàng Ngày | Mở khoá 9 tính năng cao cấp bằng điểm |
| Bộ Sưu Tập Tuổi | Thu thập 27 thẻ cung hoàng đạo |
| Streak Freeze | Bảo vệ streak bằng token, tặng qua deeplink |
| Hướng Dẫn Điểm | Tutorial + nhận 100☯ bonus |

#### Nhóm 5: Tiện Ích Khác
| Công cụ | Tính năng |
|---|---|
| Lịch Toán Ngày | Tính tuổi, khoảng cách ngày, cộng/trừ ngày |
| Đếm Ngược | Countdown events, hỗ trợ widget |
| Lên Kế Hoạch Sinh | Gợi ý ngày đẹp cho bà bầu |
| Theo Dõi Chu Kỳ | Tracker kinh nguyệt, dự đoán rụng trứng |
| Đồng Hồ Thế Giới | 100+ thành phố, converter múi giờ |
| Quản Lý Widget | Pin/hướng dẫn 7 loại widget |

---

### 6.6. PHONG THUỶ — La Bàn, Lỗ Ban, Bát Trạch

#### La Bàn Phong Thuỷ (CompassScreen)
- Sensor `TYPE_ROTATION_VECTOR` → heading thực 0–360°
- Hiển thị 8 hướng: Bắc/Đông Bắc/Đông/.../Tây Bắc
- Hướng ngày hôm nay: Thần Tài, Hỷ Thần, Hung Thần
- Nguồn dữ liệu: `DayInfoProvider`

#### Thước Lỗ Ban (LoBanScreen)
- **3 loại thước**: Đồ Gia Dụng (52.2cm), Bàn Thờ (42.9cm), Giường Tủ (38.8cm)
- Input: kích thước cm (text field + slider 0–200cm)
- Output: Tên cung (Tài Lộc/Bệnh Tật/Ly Tán/Nghĩa Đức/Quan Lộc/Kiếp Sát/Họa Hại/Bản Mệnh)
- Phân loại: Tốt / Xấu + màu hiển thị tương ứng
- Nút "Hỏi AI" để tư vấn thêm

#### Bát Trạch (BatTrachScreen)
- Input: Ngày sinh (dương) + Giới tính
- Tính: Năm âm lịch → digit sum → cung phi
- Output: Tên cung (Khảm/Ly/Cấn/Đoài/Càn/Khôn/Tốn/Chấn), nhóm Đông/Tây tứ mệnh
- Hướng tốt/xấu theo nhóm

---

### 6.7. CÂY GIA PHẢ (Family Tree)

#### Danh sách thành viên
- Nhóm theo thế hệ
- Avatar (tải ảnh từ máy)
- Tên, quan hệ, quê quán, năm sinh/mất, nghề nghiệp
- Đánh dấu "Bậc trên" và "Bản thân"

#### Chi tiết thành viên (MemberDetailScreen)
- Ảnh lớn, tên, quan hệ, thế hệ
- Can chi năm sinh
- Quan hệ (cha/mẹ/vợ/chồng/con) dạng clickable cards
- Gallery ảnh (nhiều ảnh)
- Chỉnh sửa / Xoá
- Link đến văn khấn cho người đã khuất

#### Thêm/sửa thành viên (AddMemberScreen)
- Avatar upload
- Tên, giới tính, quan hệ, quê quán, nghề nghiệp, ghi chú
- Ngày sinh âm lịch (ngày/tháng/năm)
- Ngày mất (tuỳ chọn)
- Thế hệ, là Bậc trên, là Bản thân
- Picker cha/mẹ, vợ/chồng từ danh sách

#### Ngày Giỗ (MemorialDetailScreen)
- Ngày mất dương + âm
- Ngày kỷ niệm, số năm đã qua
- Danh sách lễ vật (checklist)
- Ghi chú cúng giỗ
- Link tới văn khấn phù hợp

#### Cài đặt gia phả (FamilySettingsScreen)
- Tên gia tộc, quê quán
- Ảnh bìa gia phả
- Xuất / Nhập dữ liệu JSON

---

### 6.8. VĂN KHẤN (Prayers)

- Thư viện đầy đủ văn khấn truyền thống Việt Nam
- Tìm kiếm theo từ khoá
- Lọc theo danh mục (đám tang, lễ vật, giỗ, khai trương, ...)
- Card văn khấn nổi bật (featured)
- Chi tiết văn khấn:
  - Nội dung đầy đủ format truyền thống
  - Chia sẻ / Copy / Text-to-Speech (TTS tiếng Việt)
  - Gợi ý văn khấn liên quan

---

### 6.9. CÔNG CỤ NGÀY THÁNG

#### Ngày Tốt (GoodDaysScreen)
- Filter: Tháng này / Khai trương / Xây dựng / Cưới hỏi / Xuất hành
- Lọc chất lượng: Tốt / Xấu / Trung bình
- Card ngày: Dương + âm lịch, chất lượng, can chi, giờ tốt, hoạt động
- Expand → chi tiết đầy đủ + bát quái hướng

#### Chọn Ngày Đại Sự (DatePickerToolScreen)
- Chọn mục đích: Cưới Hỏi / Động Thổ / Khai Trương / Nhập Trạch / Xuất Hành / An Táng / Ký Kết
- Nhập năm sinh (tránh lục xung)
- Phạm vi: 30/60/90/180 ngày
- **Unlock**: 150⚡ mỗi lần dùng (hoặc Đạo Sĩ rank)
- Kết quả: Top 5 ngày điểm cao nhất với huy chương Vàng/Bạc/Đồng, phân tích chi tiết

**Thuật toán DatePickerScorer:**
| Yếu tố | Điểm |
|---|---|
| Điểm cơ bản (rating %) | 10–100 |
| Hoạt động khớp mục đích | +25 |
| Hoạt động xung đột mục đích | -30 |
| Trực Ngày (tốt) | bonus |
| Sao Chiếu (xấu) | penalty |
| Nguyệt Kỵ, Tam Nương | -15/-12 |
| Lục Xung với tuổi sinh | -25 |
| Tam Hợp | +10 |
| Nhiều giờ hoàng đạo | +bonus |

#### Máy Tính Ngày (DateMathScreen)
- **Tab Tuổi**: Tuổi tròn + tuổi mụ + can chi + vận hạn 6 năm + Kim Lâu/Tam Tai/Hoàng Ốc
- **Tab Khoảng Cách**: Số ngày/tuần, ngày làm việc vs cuối tuần, trừ ngày lễ Việt Nam
- **Tab Cộng/Trừ**: Cộng/trừ ngày/tuần/tháng/năm; preset tang lễ (3/7/49/100/1095 ngày)

#### 24 Tiết Khí (TietKhiScreen)
- Hero card tiết khí hiện tại + đếm ngược đến tiết khí tiếp theo
- 4 mùa × 6 tiết → card chi tiết: ngày chính xác, ý nghĩa, hoạt động, thực phẩm
- Màu gradient theo mùa

#### Đồng Hồ Thế Giới (WorldClockScreen)
- 100+ thành phố có sẵn, tìm kiếm theo tên/quốc gia
- Thêm/xoá thành phố
- Chọn thành phố cơ sở (base)
- Hiển thị: Giờ địa phương, ngày, offset GMT

#### Lên Kế Hoạch Sinh (BirthDatePlannerScreen)
- Input: Ngày dự sinh + phạm vi ±7/15/30 ngày
- Scoring: Ngày trăng tròn (+8), ngày 6–10/16–20 (+10), ngày xấu (-12)
- Top 10 kết quả + disclaimer y tế

#### Theo Dõi Chu Kỳ (CycleTrackerScreen)
- Input: Ngày bắt đầu chu kỳ gần nhất + độ dài chu kỳ + thời gian hành kinh
- Dự đoán: Chu kỳ tiếp theo, ngày rụng trứng, cửa sổ thụ thai (±4 ngày)
- Gradient màu theo số ngày còn lại
- Lịch sử ghi chú từng chu kỳ
- Disclaimer y tế

#### Đếm Ngược (CountdownScreen)
- Tạo sự kiện với tên + ngày mục tiêu + ghi chú
- Toggle hiển thị trên Home và Widget
- Real-time: "Còn X ngày" (xanh) / "Hôm nay" (cam) / "Đã qua X ngày" (xám)

---

### 6.10. MÀN HÌNH PHỤ TRỢ

#### Hồ Sơ (ProfileScreen)
- Avatar (chọn từ máy ảnh)
- Tên, giới tính, ngày sinh dương + giờ phút
- Cung hoàng đạo + Can chi (tính tự động)
- Stats: Điểm tổng, hạng, streak, lịch sử check-in
- Backup/Restore dữ liệu JSON đầy đủ
- Đăng xuất

#### Tìm Kiếm (SearchScreen)
- Quick actions: Chuyển đổi Âm/Dương, kiểm tra tuổi hợp, đến ngày
- Lunar Converter: Âm → Dương (ngày/tháng/năm)
- Zodiac Compatibility: 2 năm sinh → kết quả tương sinh/khắc
- Search results: Văn khấn, sự kiện lịch sử
- Recent searches history

#### Thông Báo (NotificationScreen)
- Grouped by date (Hôm nay / Hôm qua / ...)
- Card: icon theo type, tiêu đề, nội dung, thời gian tương đối
- Swipe-to-dismiss (red background)
- Mark as read / Delete all

#### Bookmarks (BookmarksScreen)
- Stats: Tổng, sắp tới, ngày tốt, lễ, cá nhân
- Danh sách bookmark với label + note + chất lượng
- Sort: Theo ngày tạo / chất lượng
- Filter: Theo danh mục
- Multi-select: Xoá hàng loạt / Export CSV
- Share: Text summary

#### Ngày Này Năm Xưa (ThisDayInHistoryScreen)
- Header xanh + ngày hiện tại + share
- Điều hướng ←→ xem ngày khác
- Timeline dọc với năm nổi bật, title, mô tả, badge (Việt Nam/Thế giới/Văn hoá/Khoa học), "X năm trước"
- Share tổng hợp sự kiện

#### Templates (TemplatesScreen)
- Kho template AI cho các tác vụ phổ biến
- Filter theo danh mục
- Mỗi template: Icon + tên + mô tả + prompt mẫu có placeholder
- Tap → điền placeholder → gửi AI

#### Settings (SettingsScreen)
- **Tài khoản**: Đăng nhập / thông tin
- **Giao diện**: Theme (Sáng/Tối/Hệ thống/Theo tiết khí), tuần bắt đầu (CN/T2)
- **Lịch**: Hiển thị âm lịch, giờ hoàng đạo, lễ tết, câu trích dẫn
- **Thông báo**: On/Off, nhắc lễ, giờ nhắc
- **Thời tiết**: Đơn vị (°C/°F), thành phố
- **Dữ liệu**: Tính cache size, xoá cache
- **Thông tin**: Privacy policy, version, Zenix Labs

#### Widget Manager (WidgetManagerScreen)
- Hero card: Trạng thái đã pin, số widget hỗ trợ
- Pin widget nhanh (requestPinAppWidget)
- Hướng dẫn cài widget thủ công
- Danh sách đầy đủ 7 widget với nút pin riêng

---

## 7. HỆ THỐNG POINTS & GAMIFICATION

### 7.1. Tổng quan

Hệ thống Points v2 gồm 2 loại điểm:
- **⚡ Daily Points**: Reset hàng ngày lúc 00:00, dùng để mở khoá tính năng ngày
- **☯ Permanent Points**: Không bao giờ mất, tích lũy → nâng hạng vĩnh viễn

### 7.2. 38 Hành động kiếm điểm (ActionType)

**Nguyên tắc thiết kế:**  
- ⚡ Daily Points: Giữ cao để người dùng đủ điểm dùng Daily Store mỗi ngày  
- ☯ Permanent: Chỉ trao cho hành động có ý nghĩa thực sự, view đơn giản = 0☯  
- Max ☯ hiển thị mục tiêu: ≤ 9.999 (không cần chữ K), Thiên sư đạt sau ~2 năm dùng đều

**Daily Engagement** — trao ☯ cho action quan trọng, 0☯ cho view thụ động
| Hành động | ⚡ | ☯ | Cap/ngày |
|---|---|---|---|
| Điểm danh (check-in) | 10 | 2 | 1 |
| Xem thẻ vận mệnh | 5 | 1 | 1 |
| Rút quẻ Kinh Dịch | 15 | 2 | 1 |
| Xem chi tiết 1 ngày | 3 | 0 | 8 |
| Xem tiết khí | 5 | 1 | 1 |
| Mở ledger/profile/store | 2–3 | 0 | 1 |
| Ngày này năm xưa | 5 | 1 | 1 |

**Screen Visits**
| Hành động | ⚡ | ☯ | Cap/ngày |
|---|---|---|---|
| Xem lịch vạn niên | 3 | 0 | 3 |
| Mở văn khấn | 5 | 1 | 1 |
| Xem tử vi/AI | 4 | 1 | 1 |
| Khám phá tiện ích | 3 | 0 | 3 |
| Đổi ngày Âm/Dương | 4 | 0 | 3 |
| Xem tuổi hợp | 5 | 1 | 2 |
| Chọn ngày tốt (tool) | 8 | 1 | 2 |

**Deep Engagement**
| Hành động | ⚡ | ☯ | Cap/ngày |
|---|---|---|---|
| Đọc hết 1 bài văn khấn | 20 | 2 | 3 |
| Chat với Thầy Số | 2 | 0 | 10 |
| Tạo nhắc nhở mới | 5 | 1 | 3 |
| Hoàn thành nhắc nhở | 10 | 1 | ∞ |
| Đánh dấu 1 ngày | 5 | 1 | 3 |
| Hoàn thành tutorial | 0 | **20** | 1 |
| Mở từ widget | 3 | 0 | 3 |

**Viral / Social**
| Hành động | ⚡ | ☯ | Cap |
|---|---|---|---|
| Chia sẻ mạng xã hội | 30 | 3 | 1/ngày |
| Mời bạn (đã gửi) | 0 | 15 | ∞ |
| Mời bạn (bạn cài app) | 0 | 50 | ∞ |
| Đánh giá 5★ | 0 | **100** | 1 |

**Location & Milestone**
| Hành động | ⚡ | ☯ | Cap |
|---|---|---|---|
| Check-in chùa/đền | 0 | 5 | 1/ngày |
| Streak 7 ngày | 0 | 5 | 1 lần |
| Streak 30 ngày | 0 | 30 | 1 lần |
| Streak 100 ngày | 0 | 150 | 1 lần |
| Streak 365 ngày | 0 | 500 | 1 lần |

### 7.3. Streak System

| Tier | Ngày tối thiểu | Multiplier ⚡ Daily | Multiplier ☯ Permanent |
|---|---|---|---|
| Tân thủ | 0 | ×1.0 | ×1.0 |
| Tu tập | 7 | ×1.5 | ×1.1 |
| Kiên tâm | 30 | ×2.0 | ×1.2 |
| Đại định | 100 | ×3.0 | ×1.4 |
| Thiên mệnh | 365 | ×5.0 | ×1.5 |

> ⚡ multiplier cao → khuyến khích dùng app hàng ngày, người dùng lâu năm mở được nhiều tính năng Daily hơn  
> ☯ multiplier cố ý thấp → tránh tích lũy số lớn, max ×1.5 ở năm thứ 2

**Streak Freeze**: Tối đa 5 token, mua 100☯/token, bảo vệ 1 ngày bỏ lỡ  
**Streak Gift**: Tạo deeplink `lichso://streak-gift?token=xxx` để tặng bạn

### 7.4. Hạng Vĩnh Viễn (PermanentRank)

| Hạng | Điểm ☯ | Tính năng mở khoá | Đạt sau (casual ~6☯/ngày) |
|---|---|---|---|
| Vô Danh | 0 | — | Ngay từ đầu |
| Sơ Cơ | 30 | Ẩn quảng cáo, Dark theme | ~5 ngày |
| Tu Tập | 100 | AI 20 tin/ngày, Export watermark | ~2.5 tuần |
| Thông Thạo | 300 | Giờ hoàng đạo chi tiết, Premium themes | ~7 tuần |
| Đạo Sĩ | 800 | Công cụ Chọn Ngày miễn phí | ~4.5 tháng |
| Chân Nhân | 2,000 | AI không giới hạn, Đặt tên con, PDF | ~11 tháng |
| Thiên Sư | 5,000 | Toàn bộ Premium, Huy hiệu Vương Miện, Khung Rồng Vàng | ~2.3 năm |

### 7.5. Daily Unlock Store (9 khoá mở hàng ngày)

| Khoá mở | Chi phí ⚡ |
|---|---|
| Tử vi chi tiết (Sao chiếu) | 20 |
| Toàn bộ văn khấn | 30 |
| AI Master 10 tin | 40 |
| Giao diện theo mùa | 25 |
| Giờ hoàng đạo đầy đủ (12 canh) | 15 |
| Biểu đồ vận tuần (7 ngày) | 50 |
| Tử vi tiết khí real-time | 60 |
| Rút thẻ cung hoàng đạo | 20 |
| Công cụ chọn ngày (1 lần) | 150 |

### 7.6. Quẻ Kinh Dịch (Oracle)

- 20 quẻ được biên soạn chi tiết
- Mỗi quẻ: Tên + Hán tự + Phụ đề + Tier (Thượng Thượng Cát → Hung)
- 4 dòng thơ + giải nghĩa + vận 4 phương diện (Sự nghiệp/Tài lộc/Tình duyên/Sức khoẻ)
- Hướng may mắn + giờ may mắn + lời khuyên
- Xác định theo ngày (deterministic seed: `epochDay × salt`) → cùng quẻ cả ngày
- Chia sẻ + "Hỏi AI giải quẻ sâu hơn"

### 7.7. Bộ Sưu Tập Cung (Zodiac Collection)

- 12 cung × 2–4 độ hiếm = **27 thẻ**
- Độ hiếm: COMMON (70%) / RARE (22%) / EPIC (7%) / LEGENDARY (<1%)
- Chi phí rút: 20⚡/lần
- Hoàn chỉnh bộ 12 cung → thưởng 50☯

---

## 8. WIDGET MÀN HÌNH CHÍNH

### 8.1. Danh sách widget (7 loại)

| Widget | Label | Kích thước | Nội dung |
|---|---|---|---|
| **CalendarWidget** | Lịch Vạn Niên | 2×2 | Số ngày lớn + thứ + tháng năm + âm lịch + can chi + rating |
| **ClockWidget** | Đồng Hồ Lịch Số | 4×2 | Analog clock + ngày + âm lịch + can chi + thời tiết (nhiệt độ + icon + min/max) |
| **ClockWidget2** | Đồng Hồ Sáng/Tối | 4×2 | Compact, hỗ trợ light/dark variant |
| **MonthCalendarWidget** | Lịch Tháng | 4×4 | Grid tháng 6×7 + nav Prev/Today/Next + âm lịch mỗi ô |
| **CountdownWidget** | Đếm ngược sự kiện | 3×2 | Liệt kê 3–5 sự kiện countdown tiếp theo |
| **AiWidget** | AI Tử Vi | 2×2 | Quick access AI Chat + snippet phản hồi gần nhất |
| **CanhGioWidget** | Canh Giờ 12 Địa Chi | 4×1 | Canh giờ hiện tại + ngũ hành + màu tốt/xấu |

### 8.2. Cơ chế cập nhật widget

- **Broadcast receivers**: `DATE_CHANGED`, `TIME_CHANGED`, `TIMEZONE_CHANGED`
- **Midnight alarm**: `CalendarWidgetScheduler.MidnightUpdateReceiver`
- **WorkManager**: `CalendarWidgetUpdateWorker` cho background updates
- **Dark mode**: Layout-night/ variants cho CalendarWidget, MonthCalendarWidget
- **MonthCalendarWidget partial update**: Cập nhật từng hàng để tránh giới hạn Binder transaction

---

## 9. HỆ THỐNG DESIGN SYSTEM

### 9.1. Bảng màu (Color Palette)

```
PRIMARY RED (Headers)
──────────────────────────────────────────
LichSo Red:   #C62828    ■  Gradient header chính
Deep Red:     #B71C1C    ■  Header gradient bottom

ACCENT - GOLD (Primary)
──────────────────────────────────────────
Gold:         #E8C84A    ■  Gold chính
Gold2:        #F5D96E    ■  Gold sáng (highlight)
Gold-dim:     rgba(232,200,74,.18)  ■  Gold mờ

ACCENT - TEAL (Secondary)
──────────────────────────────────────────
Teal:         #4ABEAA    ■  Teal chính
Teal2:        #62D4C0    ■  Teal sáng

DARK BACKGROUNDS
──────────────────────────────────────────
bg:           #0F0E0C    ■  Nền chính (gần đen ấm)
bg2:          #181610    ■  Nền card
surface:      #2E2B23    ■  Surface chính

TEXT
──────────────────────────────────────────
text:         #F0E8D0    ■  Text chính (kem ấm)
text2:        #B8AA88    ■  Text phụ
text3:        #7A6E52    ■  Text mờ

TASK COLORS
──────────────────────────────────────────
NoteBlue:     #1565C0    ■  Tab Ghi chú
TaskGreen:    #2E7D32    ■  Tab Công việc
RemindOrange: #E65100    ■  Tab Nhắc nhở
```

### 9.2. Typography

| Role | Font | Weight | Size |
|---|---|---|---|
| Page title | Serif | 400 | 22sp |
| Day number (hero) | Serif | 700 | 58sp |
| Section label | Default | 700 | 10.5sp (uppercase) |
| Body text | Default | 400 | 13sp |
| Calendar solar | Default | 500 | 14sp |
| Calendar lunar | Default | 400 | 9sp |
| Stat number | Serif | 700 | 22sp |

**Font Families:**
- **Be Vietnam Pro** (300–700): UI chính, tối ưu tiếng Việt
- **Noto Serif** (400, 700): Tiêu đề, số liệu, cảm giác truyền thống

### 9.3. Theming theo Tiết Khí

Setting "Theo tiết khí" tự động thay đổi palette màu dựa trên tiết khí hiện tại:
- `SolarTermPalette.kt` định nghĩa màu cho từng trong 24 tiết khí
- Mùa Xuân (Lập Xuân → Cốc Vũ): Xanh lá nhạt
- Mùa Hạ (Lập Hạ → Đại Thử): Đỏ cam nóng
- Mùa Thu (Lập Thu → Sương Giáng): Cam vàng ấm
- Mùa Đông (Lập Đông → Đại Hàn): Xanh lạnh

---

## 10. TRỢ LÝ AI — KIẾN TRÚC & TÍNH NĂNG

### 10.1. Kiến trúc AI

```
User Input
    │
    ▼
ChatViewModel
├── Xây dựng context (profile + lịch âm + can chi + zodiac + lịch sử)
├── AiMemoryStore (context dài hạn)
├── AiTaskService (background processing)
│
▼
OpenRouterApi
├── POST /chat/completions
├── Model: cấu hình qua proxy (GPT-4/Claude/Mistral/Llama)
├── Stream hoặc batch response
│
▼
Response Processing
├── Markdown rendering
├── Auto-task parsing (tạo note/reminder từ AI suggest)
├── Lưu vào ChatMessageEntity
└── Cập nhật PointsViewModel (daily points)
```

### 10.2. AI Context per Request

```kotlin
data class AiProfile(
    val name: String,
    val gender: String,
    val solarBirthDate: LocalDate,
    val lunarBirthDate: String,
    val birthHour: Int?,
    val zodiacSign: String,      // Zodiac Western
    val canChiYear: String,      // Năm Can Chi
    val canChiMonth: String,
    val canChiDay: String,
    val todayLunarInfo: String,  // Âm lịch hôm nay
    val aiMemory: String         // Long-term memory
)
```

### 10.3. AI Proxy Configuration

```
AI_PROXY_BASE_URL   = URL của proxy server
AI_PROXY_APP_ID     = App identifier
AI_PROXY_APP_SECRET = Signing secret
```

Không sử dụng Google Gemini trực tiếp — thông qua OpenRouter/proxy server cho phép lựa chọn model linh hoạt.

---

## 11. MÔ HÌNH DỮ LIỆU (DATA MODELS)

### 11.1. Room Database Entities

```kotlin
// ── CORE ENTITIES ──

@Entity data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Int = 2,          // 1-5
    val dueDate: Long? = null,       // epoch millis
    val dueTime: Long? = null,
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = false,
    val labels: String = "",         // JSON list
    val createdAt: Long = System.currentTimeMillis()
)

@Entity data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val labels: String = "",
    val checklistJson: String = "",  // Checklist items JSON
    val createdAt: Long,
    val updatedAt: Long
)

@Entity data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scheduledAt: Long,          // epoch millis
    val repeatType: String = "NONE",
    val isLunar: Boolean = false,
    val isEnabled: Boolean = true,
    val advanceDays: Int = 0,
    val category: String = "",
    val notes: String = "",
    val labels: String = ""
)

@Entity data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,              // LocalDate.toEpochDay()
    val label: String = "",
    val note: String = "",
    val category: String = "",
    val qualityScore: Int = 0,
    val createdAt: Long
)

@Entity data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,               // DAILY, HOLIDAY, AI, REMINDER, SYSTEM, GOOD_DAY
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long
)

@Entity data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,               // "user" | "bot"
    val content: String,
    val timestamp: Long
)

@Entity data class CountdownEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetEpochDay: Long,
    val notes: String = "",
    val showOnHome: Boolean = true,
    val showOnWidget: Boolean = true,
    val createdAt: Long
)

@Entity data class WorldClockCityEntity(
    @PrimaryKey val cityName: String,
    val timezone: String,           // e.g., "Asia/Ho_Chi_Minh"
    val country: String,
    val sortOrder: Int = 0
)

// ── FAMILY TREE ──

@Entity data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,
    val relation: String,
    val hometown: String = "",
    val occupation: String = "",
    val note: String = "",
    val lunarBirthDay: Int = 0,
    val lunarBirthMonth: Int = 0,
    val lunarBirthYear: Int = 0,
    val isDeceased: Boolean = false,
    val lunarDeathDay: Int = 0,
    val lunarDeathMonth: Int = 0,
    val lunarDeathYear: Int = 0,
    val generation: Int = 0,
    val isElder: Boolean = false,
    val isSelf: Boolean = false,
    val avatarPath: String = ""
)

// ── POINTS ENGINE ──

@Entity data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,
    val dailyAwarded: Int,
    val permanentAwarded: Int,
    val streakMultiplier: Float = 1.0f,
    val epochDay: Long,
    val timestamp: Long
)

@Entity data class PointsStateEntity(
    @PrimaryKey val id: Int = 0,    // singleton row
    val dailyBalance: Int = 0,
    val dailySpent: Int = 0,
    val permanentBalance: Int = 0,
    val currentRank: String = "VO_DANH",
    val streakCount: Int = 0,
    val longestStreak: Int = 0,
    val freezeTokens: Int = 0,
    val lastCheckInEpochDay: Long = 0
)
```

### 11.2. DataStore Preferences (AppSettings)

```kotlin
// Stored via DataStore Proto / Preferences
data class AppSettings(
    val themeMode: String = "DARK",           // LIGHT / DARK / SYSTEM / SEASONAL
    val weekStartDay: Int = Calendar.SUNDAY,
    val showLunarBadge: Boolean = true,
    val showGioHoangDao: Boolean = false,
    val showFestivals: Boolean = true,
    val showDailyQuote: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val festivalReminderEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val temperatureUnit: String = "CELSIUS",  // CELSIUS / FAHRENHEIT
    val weatherCity: String = "",
    val avatarPath: String = "",
    val userName: String = "",
    val userGender: String = "",
    val userBirthDateSolar: String = "",      // ISO date string
    val userBirthHour: Int = -1,
    val userBirthMinute: Int = -1
)
```

---

## 12. THÔNG BÁO & NHẮC NHỞ

### 12.1. Loại thông báo

| Loại | Trigger | Channel | Priority |
|---|---|---|---|
| **Nhắc nhở tùy chỉnh** | AlarmManager exact alarm | `reminder` | High |
| **Nhắc nhở ngày lễ âm** | Trước N ngày | `festival` | Default |
| **Thông báo ngày mới** | 8:00 sáng hàng ngày | `daily` | Default |
| **Tử vi cá nhân** | Theo cài đặt | `horoscope` | Default |

### 12.2. AlarmManager

- `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` cho exact reminders
- `BootReceiver` lắng nghe `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED` để reschedule
- `SmartReminderProvider`: Học giờ mở app → gợi ý thời điểm ít xâm phạm nhất

### 12.3. Quick Settings Tile

`LichSoQuickSettingsTileService` — Tile trong Quick Settings panel Android:
- Single-tap: Mở MainActivity
- Long-press: App Settings
- Hiển thị trạng thái app (điểm, streak)

### 12.4. In-App Update

`InAppUpdateManager`:
- Kiểm tra update qua Google Play App Update API
- Flexible update: Download ngầm, install khi user đồng ý
- Immediate update: Bắt buộc nếu update critical
- Fallback: Dialog chuyển tới Play Store

---

## 13. YÊU CẦU PHI CHỨC NĂNG

### 13.1. Hiệu năng

| Metric | Target |
|---|---|
| Cold start | < 1.5s |
| Warm start | < 500ms |
| Calendar scroll | 60 FPS |
| Memory usage | < 150MB |
| Battery drain | < 2%/giờ active |
| AI response (first token) | < 3s |

### 13.2. Khả năng tương thích

| Yêu cầu | Chi tiết |
|---|---|
| Android version | API 26+ (Android 8.0 Oreo) |
| compileSdk / targetSdk | 35 |
| Kích thước màn hình | 5"–7" (phone), tablet (adaptive) |
| Orientation | Portrait only |
| Dark/Light mode | Dark default, Light via Settings |
| Ngôn ngữ resources | vi + en |

### 13.3. Permissions

| Permission | Lý do |
|---|---|
| `POST_NOTIFICATIONS` | Nhắc nhở + thông báo ngày |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms sau reboot |
| `INTERNET` | Weather, AI Chat |
| `WAKE_LOCK` | Đảm bảo alarm chạy |
| `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` | Nhắc nhở đúng giờ |
| `ACCESS_COARSE_LOCATION` + `ACCESS_FINE_LOCATION` | Thời tiết theo vị trí |
| `BIND_QUICK_SETTINGS_TILE` | Quick Settings Tile |

### 13.4. Bảo mật

| Vấn đề | Giải pháp |
|---|---|
| API keys | BuildConfig field từ local.properties |
| AI proxy secret | BuildConfig, không commit vào git |
| Network security | `network_security_config.xml` |
| File sharing | `FileProvider` với `fileprovider` authority |
| Backup | `android:allowBackup="false"` (custom backup qua AppBackupManager) |
| ProGuard | Full obfuscation + native debug symbols trong AAB |

### 13.5. Offline Support

| Tính năng | Offline | Yêu cầu internet |
|---|---|---|
| Lịch dương/âm, can chi, tiết khí, giờ HD | ✅ Hoàn toàn | Không |
| Tasks, Notes, Reminders | ✅ Local-first | Không |
| Bookmarks, Family Tree | ✅ Local-first | Không |
| Widget (tất cả 7 loại) | ✅ | Không (weather có cache) |
| AI Chat | ❌ | Cần internet |
| Thời tiết | ⚡ Cache ~3h | Cần để refresh |

### 13.6. Build Configuration

| Setting | Giá trị |
|---|---|
| minSdk | 26 |
| compileSdk / targetSdk | 35 |
| JVM target | 17 |
| Core library desugaring | Enabled |
| Release: minify + shrink | Enabled |
| Debug: PNG compression | Disabled (faster builds) |
| App Bundle splits | language / density / abi |
| Room schema export | `$projectDir/schemas` |

---

## 14. PHỤ LỤC KỸ THUẬT

### 14.1. Thuật toán chuyển đổi Dương → Âm lịch

Sử dụng thuật toán Hồ Ngọc Đức (chuẩn cho lịch Việt Nam):

```
Input:  Solar date (dd/mm/yyyy)
Output: Lunar date (day, month, year, isLeap, canChi)

Các bước chính:
1. Tính Julian Day Number từ ngày dương lịch
2. Tính New Moon gần nhất (thuật toán Jean Meeus)
3. Xác định tháng âm lịch dựa trên Winter Solstice
4. Xử lý tháng nhuận (có năm có tháng nhuận)
5. Ánh xạ Can Chi (Thiên Can × Địa Chi theo chu kỳ 60)
```

### 14.2. Bảng Can Chi

**10 Thiên Can:** Giáp, Ất, Bính, Đinh, Mậu, Kỷ, Canh, Tân, Nhâm, Quý

**12 Địa Chi:** Tý, Sửu, Dần, Mão, Thìn, Tỵ, Ngọ, Mùi, Thân, Dậu, Tuất, Hợi

**Chu kỳ:** 60 tổ hợp (Lục Thập Hoa Giáp)

### 14.3. Giờ Hoàng Đạo

Tính dựa trên **Ngày Chi** theo bảng tra: Thanh Long, Minh Đường, Kim Quỹ, Thiên Đức, Ngọc Đường, Tư Mệnh.

12 canh giờ: Tý (23–01) · Sửu (01–03) · Dần (03–05) · Mão (05–07) · Thìn (07–09) · Tỵ (09–11) · Ngọ (11–13) · Mùi (13–15) · Thân (15–17) · Dậu (17–19) · Tuất (19–21) · Hợi (21–23)

### 14.4. Dependencies thực tế (build.gradle.kts)

```kotlin
// Desugaring
coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

// AndroidX Core
implementation("androidx.core:core-ktx:1.16.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
implementation("androidx.activity:activity-compose:1.10.1")

// Compose
implementation(platform("androidx.compose:compose-bom:2025.04.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.compose.animation:animation")
implementation("androidx.compose.foundation:foundation")
implementation("androidx.navigation:navigation-compose:2.8.9")

// Hilt DI
implementation("com.google.dagger:hilt-android:2.55")
ksp("com.google.dagger:hilt-compiler:2.55")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.1.2")

// Network (không dùng Retrofit — dùng OkHttp trực tiếp)
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.google.code.gson:gson:2.11.0")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.10.0")

// Auth (Credential Manager + Firebase)
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Image
implementation("io.coil-kt:coil-compose:2.7.0")
implementation("androidx.exifinterface:exifinterface:1.3.7")

// Play Services
implementation("com.google.android.play:review-ktx:2.0.2")
implementation("com.google.android.play:app-update-ktx:2.1.0")

// Badge
implementation("me.leolin:ShortcutBadger:1.1.22@aar")
```

**Lưu ý quan trọng:**
- Không dùng Retrofit (dùng OkHttp trực tiếp để gọi OpenRouter API)
- Không dùng Google Gemini SDK (AI qua proxy/OpenRouter)
- Không dùng Firebase Crashlytics / FCM (Firebase chỉ dùng Auth + Analytics)
- Không dùng Kotlinx Serialization (dùng Gson)
- KSP thay thế kapt cho Room + Hilt

### 14.5. Quy ước đặt tên

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Package | lowercase | `com.lichso.app.feature.fengshui` |
| Class | PascalCase | `LoBanScreen`, `FengShuiCalculators` |
| Composable | PascalCase | `@Composable fun HeroCard()` |
| ViewModel | PascalCase + VM | `HomeViewModel` |
| Repository | PascalCase + Repository | `FamilyTreeRepository` |
| Entity | PascalCase + Entity | `FamilyMemberEntity` |
| DAO | PascalCase + Dao | `FamilyTreeDao` |
| Constant | SCREAMING_SNAKE | `MAX_FREEZE_TOKENS` |
| Resource | snake_case | `ic_calendar`, `widget_calendar_info` |

---

> **Tài liệu này phản ánh trạng thái thực tế của codebase tại v2.0.4 (versionCode 33).**  
> **Phát triển bởi:** Zenix Labs  
> **Lần cập nhật cuối:** 25/05/2026  
> **Document version:** 3.0
