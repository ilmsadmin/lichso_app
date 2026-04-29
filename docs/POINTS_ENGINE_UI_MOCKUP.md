# 🎨 UI Mockup – Daily Unlock Card & Progress Bar

> **Scope**: Thiết kế UI cho hệ thống điểm Công Đức trên **màn Home** và các điểm chạm unlock trong app.
> **Design system**: Material 3 (Android) / SF Symbols (iOS), tone màu truyền thống **đỏ son + vàng kim + đen mực**.
> **File**: Mockup dạng ASCII + token + Compose snippet (production-ready).

---

## 🎨 Design tokens

```kotlin
object PointsTheme {
    // Colors
    val DailyPoint       = Color(0xFFFFB300)   // ⚡ vàng điện
    val DailyPointLight  = Color(0xFFFFE082)
    val PermanentPoint   = Color(0xFFC62828)   // ☯️ đỏ son
    val PermanentLight   = Color(0xFFFFCDD2)
    val LockGray         = Color(0xFF9E9E9E)
    val UnlockedGreen    = Color(0xFF2E7D32)
    val StreakFire       = Color(0xFFFF5722)
    val GoldAccent       = Color(0xFFD4AF37)   // viền huy hiệu

    // Elevation
    val CardElevation = 4.dp
    val DialogElevation = 12.dp

    // Shapes
    val CardShape = RoundedCornerShape(20.dp)
    val ChipShape = RoundedCornerShape(50)
    val ProgressShape = RoundedCornerShape(10.dp)

    // Typography
    val PointNumber = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        fontWeight = FontWeight.Black
    )
}
```

---

## 📱 1. Home Header – Points Dashboard

Vị trí: **trên cùng màn Home**, ngay dưới status bar.

```
┌──────────────────────────────────────────────────────────┐
│  ┌────┐  Chào buổi sáng, Minh                    🔔 ⚙️   │
│  │ 👤 │  Thứ 4, 23/04/2026 • Mùng 7/3 Âm lịch            │
│  └────┘                                                   │
│                                                           │
│  ╔═══════════════════════════════════════════════════╗   │
│  ║  ⚡ 47  /  ☯️ 1,284   🔥 42 ngày                  ║   │
│  ║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║   │
│  ║  Tu tập  ▓▓▓▓▓▓▓▓▓▓░░░░░░  64%  → Thông thạo    ║   │
│  ║                                   còn 716 ☯️       ║   │
│  ╚═══════════════════════════════════════════════════╝   │
└──────────────────────────────────────────────────────────┘
```

### Spec chi tiết

| Element | Spec |
|---------|------|
| Avatar | 48×48 dp, frame theo rank (Sơ cơ=đồng, Tu tập=bạc, Thông thạo=vàng...) |
| ⚡ chip | `DailyPoint` bg, shape pill, icon tia chớp + số lớn |
| ☯️ chip | `PermanentPoint` bg, icon âm dương xoay nhẹ |
| 🔥 streak | Icon lửa animated nếu ≥7 ngày, nhảy vỗ khi streak tăng |
| Rank progress | `LinearProgressIndicator` height 10dp, gradient vàng→đỏ |
| Next rank label | Caption nhỏ, tap → mở bottom sheet list unlocks |

### Compose snippet

```kotlin
@Composable
fun PointsHeader(
    balance: PointsBalance,
    streak: StreakState,
    onTapRank: () -> Unit
) {
    Surface(
        shape = PointsTheme.CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = PointsTheme.CardElevation
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DailyPointChip(points = balance.daily)
                Spacer(Modifier.width(8.dp))
                PermanentPointChip(points = balance.permanent)
                Spacer(Modifier.weight(1f))
                StreakBadge(streak = streak)
            }
            Spacer(Modifier.height(12.dp))
            RankProgressBar(
                currentRank = balance.rank,
                nextRank = balance.nextRank,
                progress = balance.progressToNextRank,
                onClick = onTapRank
            )
        }
    }
}
```

---

## 🎴 2. Daily Unlock Card – Trạng thái LOCKED

Khi user vào 1 màn cần unlock (ví dụ AI Thầy Số), thấy **card khoá** full-width:

```
┌──────────────────────────────────────────────────────────┐
│                                                           │
│                       🔒                                  │
│                                                           │
│             AI Thầy Số – Chat 10 tin/ngày                 │
│         Hỏi đáp phong thuỷ, tử vi, kinh dịch              │
│                                                           │
│   ┌─────────────────────────────────────────────────┐    │
│   │  Cần:  40 ⚡       Bạn có:  30 ⚡              │    │
│   │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │    │
│   │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░    75%         │    │
│   └─────────────────────────────────────────────────┘    │
│                                                           │
│   💡 Còn 10 ⚡ nữa là đủ!                                 │
│                                                           │
│   ┌────────────────────┐  ┌────────────────────┐         │
│   │ 🎴 Rút quẻ        │  │ 📿 Đọc văn khấn    │         │
│   │ +15 ⚡            │  │ +20 ⚡            │         │
│   └────────────────────┘  └────────────────────┘         │
│                                                           │
│   Hoặc  ▶️  Xem quảng cáo 30s (+20 ⚡)                    │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

### Spec

| Element | Behavior |
|---------|----------|
| 🔒 icon | 64dp, màu `LockGray`, animated (shake nhẹ 1s/lần) |
| Tên tính năng | Headline medium, bold |
| Mô tả | Body small, 2 dòng max |
| Progress bar | `LinearProgressIndicator` với `indicatorColor = DailyPoint`, shimmer khi đang tính |
| Suggestion cards | `OutlinedCard` 2 cái cạnh nhau, tap → deeplink mở màn tương ứng + haptic |
| Rewarded ad button | `TextButton` với icon ▶️, hiện khi user có thể xem ad |

### Compose snippet

```kotlin
@Composable
fun DailyUnlockLockedCard(
    state: DailyUnlockState,
    onSuggestionTap: (ActionSuggestion) -> Unit,
    onWatchAd: () -> Unit
) {
    val progress = (state.currentDaily.toFloat() / state.cost).coerceIn(0f, 1f)
    Card(
        shape = PointsTheme.CardShape,
        elevation = CardDefaults.cardElevation(PointsTheme.CardElevation),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LockIcon()
            Spacer(Modifier.height(12.dp))
            Text(state.key.label, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cần: ${state.cost} ⚡   Bạn có: ${state.currentDaily} ⚡",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp)
                    .clip(PointsTheme.ProgressShape),
                color = PointsTheme.DailyPoint,
                trackColor = PointsTheme.DailyPointLight,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "💡 Còn ${(state.cost - state.currentDaily).coerceAtLeast(0)} ⚡ nữa!",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.shortestPath) { suggestion ->
                    SuggestionChip(suggestion, onClick = { onSuggestionTap(suggestion) })
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onWatchAd) {
                Icon(Icons.Default.PlayCircle, null)
                Spacer(Modifier.width(4.dp))
                Text("Xem quảng cáo 30s (+20 ⚡)")
            }
        }
    }
}
```

---

## 🎉 3. Daily Unlock Card – Trạng thái READY TO UNLOCK

Khi user đủ điểm, card chuyển sang màu rực:

```
┌──────────────────────────────────────────────────────────┐
│   ✨                                                 ✨   │
│                      🔓                                  │
│                                                           │
│             AI Thầy Số – Chat 10 tin/ngày                 │
│                                                           │
│   ┌─────────────────────────────────────────────────┐    │
│   │  Bạn có:  50 ⚡  →  Tiêu  40 ⚡                 │    │
│   │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  100% ✅  │    │
│   └─────────────────────────────────────────────────┘    │
│                                                           │
│   ┌────────────────────────────────────────────────┐     │
│   │          🎆  MỞ KHOÁ NGAY  🎆                  │     │
│   └────────────────────────────────────────────────┘     │
│                                                           │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

- Background: gradient `DailyPoint` → `GoldAccent`
- CTA button: `FilledTonalButton` to, haptic medium khi tap
- Khi tap → animation **pháo hoa 🎆** full-screen 1.5s + chuông chùa + card flip sang nội dung thật

---

## ✅ 4. Daily Unlock Card – Trạng thái UNLOCKED (hôm nay)

```
┌──────────────────────────────────────────────────────────┐
│  ✅ Đã mở khoá hôm nay — còn 8h 32 phút              ⋯  │
│  AI Thầy Số (10 tin/ngày)                                │
│  ▓▓▓▓▓▓▓▓░░ 3/10 tin đã dùng                            │
└──────────────────────────────────────────────────────────┘
```

- Header compact, không chắn nội dung tính năng bên dưới
- Countdown đến 00:00 (màu xanh → cam khi <1h)

---

## 🔥 5. Streak Badge – Animated

```
┌──────────┐        ┌──────────┐        ┌──────────┐
│   🔥     │        │   🔥🔥   │        │  🔥🔥🔥  │
│    42    │        │    42    │        │    42    │
│   ngày   │        │   ngày   │        │   ngày   │
│  x2.0    │        │  x2.0    │        │  x2.0    │
└──────────┘        └──────────┘        └──────────┘
   Tier 30+           Tier 100+           Tier 365+
```

### Spec
- **Size**: 72×72dp, tap → mở `StreakDetailSheet`
- **Animation**: Lottie flame (loop), tăng tốc theo tier
- **Missed today** state: chuyển grayscale + chấm đỏ cảnh báo 🔴
- **Milestone hit**: phóng to 1.5x + confetti + haptic heavy

---

## 🎯 6. Rank Progress Bar – Interactive

```
┌──────────────────────────────────────────────────────────┐
│  🥈 Tu tập                          → 🥇 Thông thạo      │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░  64%                    │
│  1,284 ☯️                                 5,000 ☯️        │
│  ─────────────────────────────────────────────────       │
│  Đã mở khoá 👇                                           │
│  ✅ AI 20 tin/ngày   ✅ Export watermark                 │
│  ─────────────────────────────────────────────────       │
│  Tiếp theo 🔒                                            │
│  🔒 Giờ hoàng đạo vĩnh viễn                              │
│  🔒 5 Theme Premium                                       │
└──────────────────────────────────────────────────────────┘
```

Tap → bottom sheet full list các rank + unlocks.

---

## 🎊 7. Rank-Up Celebration Dialog

Full-screen modal khi user vừa đạt rank mới:

```
╔══════════════════════════════════════════════════════════╗
║                                                           ║
║              🎆 ✨ 🎆 ✨ 🎆 ✨ 🎆                         ║
║                                                           ║
║                       🥇                                  ║
║                                                           ║
║              CHÚC MỪNG BẠN ĐÃ ĐẠT                         ║
║                                                           ║
║                   THÔNG THẠO                              ║
║                                                           ║
║   Bạn đã kiên trì 142 ngày cùng Lịch Số 🙏               ║
║                                                           ║
║   Phần thưởng vĩnh viễn:                                  ║
║   ✨ Giờ hoàng đạo chi tiết vĩnh viễn                    ║
║   ✨ 5 Theme Premium (Cổ điển, Minimalism, Sakura...)    ║
║                                                           ║
║   ┌────────────────────┐  ┌────────────────────┐         ║
║   │  Chia sẻ 📣        │  │  Tiếp tục ▶️      │         ║
║   └────────────────────┘  └────────────────────┘         ║
║                                                           ║
╚══════════════════════════════════════════════════════════╝
```

- Animation: particles vàng rơi + trống chùa âm thanh
- Nút "Chia sẻ" → export ảnh thành tựu (+20 ☯️ nếu share thật)

---

## 📊 8. Points Earning Feedback (Toast/Snackbar)

Mỗi action cộng điểm → toast nhỏ ở góc trên:

```
┌─────────────────────────────────┐
│  ⚡ +15   ☯️ +5     x2.0 🔥     │
│  Rút quẻ Kinh Dịch              │
└─────────────────────────────────┘
```

- Xuất hiện 2.5s, tự mờ dần
- Nếu bị cap: `💤 Đã đạt giới hạn hôm nay cho hành động này`
- Nếu rank up: chuyển thành full-screen dialog (#7)

---

## 🎲 9. Action Suggestion Chip

```
┌──────────────────┐     ┌──────────────────┐
│ 🎴 Rút quẻ      │     │ 📿 Văn khấn     │
│ +15 ⚡           │     │ +20 ⚡           │
└──────────────────┘     └──────────────────┘
```

- Card 2 dòng, tap → deeplink + haptic light
- Max 3 suggestions (top 3 theo `UnlockSuggester`)

---

## 🗺️ 10. Điểm chạm UI toàn app

| Vị trí | Component | Mục đích |
|--------|-----------|----------|
| Home top | `PointsHeader` | Always-visible balance + streak |
| Home middle | `DailyFortuneCard` | Trigger check-in, award điểm |
| Home bottom | Widget "Quick Actions" (Rút quẻ, Văn khấn) | Kiếm điểm nhanh |
| Màn AI chat (khoá) | `DailyUnlockLockedCard` | Convert điểm → unlock |
| Màn Tử vi (khoá) | `DailyUnlockLockedCard` | Convert điểm → unlock |
| Màn Profile | `RankProgressBar` + list unlocks | Hiển thị thành tựu |
| Bất kỳ action | `PointsEarnedToast` | Feedback tức thời |
| Khi lên rank | `RankUpDialog` | Celebration moment |
| 20:00 nếu chưa check-in | `StreakWarningNotification` | Giữ streak |

---

## 📐 Grid & Spacing

- Page padding: **16dp**
- Inner card padding: **16–24dp**
- Gap between cards: **12dp**
- Corner radius: **20dp (card) / 50 (pill) / 10 (progress)**
- Progress bar height: **10–12dp**
- Touch target min: **48×48dp**

---

## 🎨 Dark mode palette

| Token | Light | Dark |
|-------|-------|------|
| `DailyPoint` | `#FFB300` | `#FFD54F` |
| `PermanentPoint` | `#C62828` | `#EF5350` |
| Card surface | `#FFFBF0` | `#1C1B1F` |
| Lock gray | `#9E9E9E` | `#616161` |

---

## 🧪 Accessibility

- Mọi icon có `contentDescription`
- Progress bar có `semantics { stateDescription = "..." }`
- Streak badge đọc được: *"Chuỗi 42 ngày, cấp Kiên tâm, nhân hệ số 2x"*
- Support font scale 200%
- Màu sắc pass WCAG AA (contrast ≥ 4.5:1)

---

## 🚀 Implementation order (Phase 1)

1. `PointsHeader` (balance + streak + rank bar) — 2 ngày
2. `DailyUnlockLockedCard` + `PointsEarnedToast` — 3 ngày
3. `StreakBadge` + animation — 2 ngày
4. `RankUpDialog` + celebration — 2 ngày
5. `ActionSuggestionChip` + deeplink — 1 ngày
6. `RankDetailBottomSheet` (list all ranks/unlocks) — 2 ngày
7. Dark mode + a11y pass — 2 ngày

**Tổng Phase 1 UI**: ~2 tuần cho 1 dev, parallel với backend `PointsEngine`.
