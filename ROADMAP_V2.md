# 🚀 Lịch Số v2.0 – Roadmap & Feature Proposal

> **Mục tiêu**: Biến Lịch Số từ một ứng dụng lịch âm dương truyền thống thành một **người bạn đồng hành tâm linh – phong thủy hàng ngày**, khiến người dùng **mở app mỗi sáng** và **gắn bó dài hạn**.
>
> 🎯 **Triết lý v2.0**: *"Free-to-use, earn-to-unlock"* — KHÔNG bán Premium, mọi tính năng xịn đều mở khoá bằng **Điểm Công Đức** kiếm từ hành vi sử dụng app.

- **Phiên bản**: 2.0
- **Ngày soạn thảo**: 23/04/2026
- **Trạng thái**: Proposal / Planning
- **Nền tảng**: Android (Kotlin/Compose) + iOS (SwiftUI)

---

## 📋 Mục lục

1. [Daily Engagement – Kéo người dùng mở app mỗi ngày](#-1-daily-engagement)
2. [AI Tương tác cá nhân hóa](#-2-ai-tương-tác-cá-nhân-hóa)
3. [Gamification](#-3-gamification)
4. [Lịch & Nhắc nhở thông minh](#-4-lịch--nhắc-nhở-thông-minh)
5. [UI/UX Wow Factors](#-5-uiux-wow-factors)
6. [Utility thiết thực & Monetization](#-6-utility-thiết-thực--monetization)
7. [Roadmap triển khai](#-7-roadmap-triển-khai)
8. [KPIs & Success Metrics](#-8-kpis--success-metrics)
9. [3 tính năng KILLER](#-9-3-tính-năng-killer)

> ⚠️ **Ghi chú**: Nhóm tính năng **Social & Family** (Cây gia phả sync, Lịch giỗ chung, Cầu an) **tạm hoãn** ở v2.0, sẽ xem xét lại ở v2.5 hoặc v3.0 khi có đủ backend infrastructure.

---

## 🌅 1. Daily Engagement

> Mục tiêu: DAU/MAU > 40%, lý do **chính đáng** để mở app mỗi sáng.

### 1.1 ⭐ Vận mệnh hôm nay (Daily Fortune Card)
Màn hình chào buổi sáng với thẻ bài xoay 3D đẹp mắt, cá nhân hóa theo tuổi/mệnh người dùng.

**Nội dung thẻ:**
- 🕐 Giờ hoàng đạo / hắc đạo trong ngày
- 🎨 Màu may mắn
- 🔢 Số may mắn (dùng cho lô đề, vé số – tuỳ văn hóa)
- 🧭 Hướng xuất hành
- 💼 Việc nên làm / nên tránh
- 💑 Tuổi xung – tuổi hợp hôm nay

**Hook**: Push notification lúc 7:00 AM — *"Hôm nay vận bạn thế nào? 🔮 Mở thẻ ngay!"*

**Technical**:
- Dữ liệu generate offline từ thuật toán can chi + bảng tra
- Cache 1 ngày, invalidate sau 00:00
- Animation: `rotation3D` + `hapticFeedback`

---

### 1.2 📿 Lời chúc ngày mới từ AI
Mỗi sáng 1 câu chúc cá nhân hóa dựa trên:
- Tử vi của user
- Tâm trạng hôm qua (từ mood tracker)
- Sự kiện sắp tới trong lịch

**Viral hook**: Nút **"Chia sẻ lời chúc"** → export ảnh đẹp có logo Lịch Số → share Zalo/Facebook.

---

### 1.3 🎴 Rút quẻ Kinh Dịch / Xăm hàng ngày
- Animation **lắc ống xăm** với haptic + âm thanh
- Rút được 1 trong 64 quẻ Kinh Dịch hoặc 100 lá xăm
- **Giới hạn 1 lần/ngày** → tạo thói quen quay lại
- Lưu lịch sử quẻ, có thể xem lại
- Giải nghĩa bằng AI chi tiết (Premium)

---

## 🧠 2. AI Tương tác cá nhân hóa

### 2.1 💬 AI Chat Master – "Thầy Số"
Chatbot phong thủy 24/7 dạng ChatGPT, chuyên sâu:

| Chủ đề | Ví dụ |
|--------|-------|
| Chọn ngày tốt | "Cho tôi ngày đẹp động thổ tháng 6/2026, tuổi Tân Dậu" |
| Đặt tên con | "Đặt tên con trai, họ Nguyễn, sinh 12/05/2026 lúc 8h" |
| Giải mộng | "Đêm qua tôi mơ thấy rắn trắng" |
| Xem tướng | Upload selfie → AI phân tích |
| Tư vấn phong thủy | "Bàn làm việc tôi nên quay hướng nào?" |

**Tech stack**:
- GPT-4o / Gemini 1.5 Pro
- System prompt chuyên biệt + RAG từ kho tri thức tử vi, kinh dịch
- Rate limit: 5 tin nhắn/ngày (Free), unlimited (Premium)

---

### 2.2 🏠 AI Phong Thủy Nhà ở (AR)
- Chụp ảnh phòng → AI phân tích phong thủy
- Gợi ý đặt bàn thờ, giường, bếp theo **tuổi + mệnh**
- Overlay AR: hiển thị hướng tốt/xấu ngay trên camera
- **Premium feature** → $4.99/lượt hoặc gói tháng

---

### 2.3 👶 AI Đặt tên con / Tên thương hiệu
- Input: ngày giờ sinh + họ + giới tính
- Output: 10 gợi ý tên hợp mệnh
- Kèm: giải nghĩa Hán–Việt, ngũ hành, số nét, ý nghĩa phong thủy
- Export PDF đẹp để gửi ông bà duyệt 😄

---

## 👨‍👩‍👧 ~~3. Social & Family~~ *(Tạm hoãn – dự kiến v2.5/v3.0)*

> 💤 Nhóm tính năng này **tạm bỏ ở v2.0** do cần backend sync phức tạp (Firebase Auth + Firestore + quản lý nhóm gia đình) và chưa phải ưu tiên cao nhất cho mục tiêu tăng DAU. Sẽ tái khởi động khi:
> - Đã có base user đủ lớn (> 100k MAU)
> - Đã validated được retention từ các tính năng v2.0
> - Backend infrastructure đã sẵn sàng
>
> Các ý tưởng lưu trữ: Cây gia phả sync 3D, Lịch giỗ chung gia đình (sync), Gửi lời cầu an / bàn thờ online.

---

## 🎯 3. Gamification – Điểm & Mở khoá tính năng

> 🎁 **Triết lý cốt lõi của Lịch Số v2.0**:
> **KHÔNG** bán Premium bằng tiền. Mọi tính năng "cao cấp" đều **mở khoá bằng ĐIỂM CÔNG ĐỨC** — thứ user kiếm được qua **sự gắn bó hàng ngày**.
>
> Nguyên tắc:
> - **Càng dùng → càng được nhiều** (reward sự kiên trì, không reward ví tiền)
> - **Công bằng**: ai cũng có thể unlock được nếu dùng đều
> - **Tạo áp lực tích cực**: "Hôm nay chưa mở thẻ hôm nay à? Mất streak mất hết đó!"

---

### 3.1 🏆 Hệ thống điểm Công Đức – Cấu trúc kép

Điểm chia làm **2 loại**:

| Loại | Ký hiệu | Mục đích | Reset? |
|------|---------|----------|--------|
| **🟡 Điểm ngày** | ⚡ Công đức ngày | Mở khoá tính năng **trong ngày hôm nay** | Reset 00:00 mỗi ngày |
| **🔴 Điểm vĩnh viễn** | ☯️ Công đức tích lũy | Mở khoá tính năng **vĩnh viễn** | Không bao giờ reset |

> 💡 Mọi hành động đều cộng cả 2 loại điểm cùng lúc (hệ số khác nhau).

---

### 3.2 📊 Bảng tính điểm

| Hành động | ⚡ Điểm ngày | ☯️ Điểm vĩnh viễn | Giới hạn |
|-----------|------------:|------------------:|----------|
| Mở app lần đầu trong ngày (check-in) | +10 | +5 | 1 lần/ngày |
| Xem Daily Fortune Card | +5 | +2 | 1 lần/ngày |
| Rút quẻ Kinh Dịch / xăm | +15 | +5 | 1 lần/ngày |
| Truy cập màn Lịch vạn niên | +3 | +1 | 3 lần/ngày |
| Truy cập màn Văn khấn | +5 | +2 | 3 lần/ngày |
| Đọc hết 1 bài văn khấn | +20 | +10 | 5 lần/ngày |
| Chat với AI Thầy Số (1 tin) | +2 | +1 | 10 lần/ngày |
| Tạo nhắc nhở mới | +5 | +3 | 5 lần/ngày |
| Hoàn thành nhắc nhở | +10 | +5 | không giới hạn |
| Chia sẻ quẻ / lời chúc lên MXH | +30 | +20 | 3 lần/ngày |
| Mời bạn cài app (qua link) | — | **+200** | không giới hạn |
| Bạn mời cài app thành công | — | **+500** | không giới hạn |
| Đánh giá 5★ trên Store | — | **+1,000** | 1 lần |
| Milestone streak 7 ngày | — | +300 | tự động |
| Milestone streak 30 ngày | — | +1,500 | tự động |
| Milestone streak 100 ngày | — | +10,000 | tự động |
| Milestone streak 365 ngày | — | +50,000 💎 | tự động |

**Trung bình 1 user active**: ~**50–80 điểm ngày** / ngày, ~**30–50 điểm vĩnh viễn** / ngày.

---

### 3.3 🔓 Tính năng mở khoá theo NGÀY (⚡ Daily Unlocks)

> User phải "làm nóng" app mỗi ngày mới dùng được. **Mỗi 00:00 khoá lại.**

| Tính năng | Điểm cần | Chi phí (điểm ngày) | Hành vi cần |
|-----------|---------:|--------------------:|-------------|
| 🔍 Xem tử vi chi tiết hôm nay | 20 ⚡ | tiêu 20 | Check-in + xem Fortune card |
| 📿 Xem văn khấn đầy đủ (1 bài) | 30 ⚡ | tiêu 30 | Vào màn Văn khấn 2 lần |
| 🤖 Mở khoá AI Thầy Số (10 tin trong ngày) | 40 ⚡ | tiêu 40 | Rút quẻ + check-in + xem lịch |
| 🎨 Dùng theme hôm nay | 25 ⚡ | tiêu 25 | Mở app + Fortune card |
| 🧭 Xem giờ hoàng đạo chi tiết (tất cả 12 giờ) | 15 ⚡ | tiêu 15 | Chỉ cần check-in |
| 📊 Xem biểu đồ vận hạn tuần | 50 ⚡ | tiêu 50 | Làm gần hết actions trong ngày |
| 🔮 Xem lá số tử vi hôm nay | 60 ⚡ | tiêu 60 | Rất tích cực hôm nay |

**Cơ chế UI**:
- Tính năng bị khoá sẽ có **🔒 + thanh tiến trình điểm**: "Còn 10 ⚡ nữa! Vào màn Lịch vạn niên +3 ⚡"
- Gợi ý **đường ngắn nhất** để đủ điểm: *"Rút quẻ hôm nay (+15 ⚡) để mở khoá!"*
- Khi unlock: animation **pháo hoa 🎆** + haptic + âm thanh chuông chùa

---

### 3.4 🌟 Tính năng mở khoá VĨNH VIỄN (☯️ Permanent Unlocks)

> Đạt mốc điểm tích lũy → **unlock vĩnh viễn, không bao giờ khoá lại.**

| Cấp bậc | Điểm cần ☯️ | Unlock vĩnh viễn |
|---------|-----------:|------------------|
| 🥉 **Sơ cơ** | 500 | ✅ Bỏ quảng cáo vĩnh viễn<br>✅ Theme "Trăng Rằm" (dark mode xịn) |
| 🥈 **Tu tập** | 2,000 | ✅ AI Thầy Số 20 tin/ngày (không cần unlock daily)<br>✅ Export ảnh quẻ có watermark tên user |
| 🥇 **Thông thạo** | 5,000 | ✅ Giờ hoàng đạo chi tiết vĩnh viễn<br>✅ 5 theme Premium (Cổ điển, Minimalism, Sakura...) |
| 💠 **Đạo sĩ** | 15,000 | ✅ AI Phong thuỷ AR (1 lần/tuần)<br>✅ Công cụ chọn ngày cưới / động thổ |
| 🌟 **Chân nhân** | 40,000 | ✅ AI Thầy Số **unlimited**<br>✅ Đặt tên con / thương hiệu<br>✅ Export PDF đẹp |
| 👑 **Thiên sư** | 100,000 | ✅ **Tất cả** tính năng Premium vĩnh viễn<br>✅ Huy hiệu 👑 cạnh tên<br>✅ Avatar frame rồng vàng |

**Ước lượng thời gian đạt mốc** (user active bình thường ~40 điểm/ngày):
- Sơ cơ: ~13 ngày
- Tu tập: ~50 ngày (~1.5 tháng)
- Thông thạo: ~4 tháng
- Đạo sĩ: ~1 năm
- Chân nhân: ~2–3 năm (hoặc nhanh hơn nếu mời bạn, chia sẻ)
- Thiên sư: hardcore fan / viral master

---

### 3.5 🔥 Streak – Chuỗi ngày (Boost engine)

Streak **nhân hệ số điểm**:

| Streak | Hệ số điểm ngày | Hệ số điểm vĩnh viễn |
|--------|----------------:|---------------------:|
| 1–6 ngày | x1.0 | x1.0 |
| 7–29 ngày | **x1.5** | x1.2 |
| 30–99 ngày | **x2.0** | x1.5 |
| 100–364 ngày | **x3.0** | x2.0 |
| 365+ ngày | **x5.0** 🔥 | x3.0 |

**Cơ chế giữ streak**:
- **Freeze streak**: 1 lần miễn phí/tháng, hoặc tốn **100 ☯️** để mua freeze
- **Thông báo nhắc 20:00**: *"Bạn chưa check-in hôm nay! Streak 🔥 42 ngày sắp bay!"*
- **Streak Freeze as Gift**: user có thể tặng freeze cho người khác (viral!)

**Milestone streak** – thưởng nóng:
- 7 ngày: +300 ☯️ + badge "Người kiên trì"
- 30 ngày: +1,500 ☯️ + theme "Tháng Đầu"
- 100 ngày: +10,000 ☯️ + avatar frame bạc
- 365 ngày: +50,000 ☯️ 💎 + avatar frame vàng + badge "Thiên mệnh giả"

---

### 3.6 📈 Thiết kế "Daily Loop" tối ưu

**Mục tiêu**: mỗi ngày user đi qua flow này để tối đa điểm (và tối đa engagement):

```
07:00 ── Push notification Fortune Card
  │
  ├─► Mở app (Check-in +10⚡)
  ├─► Xem Fortune Card (+5⚡)
  ├─► Rút quẻ hôm nay (+15⚡)  ← peak engagement
  │
12:00 ── Optional: truy cập các màn
  ├─► Lịch vạn niên (+3⚡)
  ├─► Văn khấn +  đọc 1 bài (+5 + 20⚡)
  │
18:00 ── Unlock tính năng cần
  ├─► Đủ 40⚡ → unlock AI Thầy Số
  ├─► Chat với AI (+2⚡ x nhiều tin)
  │
21:00 ── Share hook
  ├─► Chia sẻ quẻ lên Facebook (+30⚡)
  │
22:00 ── Streak reminder
  └─► "Streak 42 🔥 bạn đã giữ được!"
```

Tổng điểm ngày trung bình: **~80–120 ⚡** → đủ unlock hầu hết daily features + dư ~40 điểm để **tích sang điểm vĩnh viễn**.

---

### 3.7 🎴 Bộ sưu tập Thẻ 12 Con Giáp / Cung Hoàng Đạo

- Mở 1 thẻ ngẫu nhiên mỗi ngày (cần tiêu **20 ⚡**)
- Rare cards: **Rồng Vàng, Hổ Bạch Kim, Phụng Hoàng Đỏ** (<1% rate)
- Đủ bộ 12 con giáp → **+5,000 ☯️** + theme "Thập Nhị Chi"
- Trade card với bạn bè (viral social feature, v2.5)

---

## 📅 4. Lịch & Nhắc nhở thông minh

### 4.1 ⏰ Smart Reminder theo ngữ cảnh
- *"Mai là mùng 1, nhớ mua hoa cúng"* 🌸
- *"Hôm nay rằm, 5h chiều thắp hương nhé"* 🕯️
- *"Còn 3 ngày nữa giỗ bà nội – gợi ý mâm cúng"*
- *"Tuần sau có ngày Dương Công Kỵ – tránh xuất hành"*

### 4.2 🌙 Chuyển đổi âm–dương siêu nhanh
- Widget home có **ô nhập nhanh** ngày → ra kết quả
- Shortcut Siri / Google Assistant:
  - *"Hey Siri, hôm nay ngày âm mấy?"*
  - *"OK Google, 15/08 âm là ngày nào dương?"*
- Quick Settings tile (Android)

### 4.3 📸 OCR quét lịch giấy cũ
- Chụp tờ lịch bloc → tự động thêm sự kiện
- Quét **giấy giỗ của ông bà** (viết tay) → import vào ghi chú cá nhân
- ML Kit / Vision OCR + custom parser tiếng Việt

---

## 🎨 5. UI/UX Wow Factors

### 5.1 🌌 Theme theo mùa / 24 tiết khí
- Giao diện **tự đổi theo 24 tiết khí** (Lập Xuân, Thanh Minh, Đại Thử, Đông Chí...)
- Theme Tết đặc biệt: mai đào, pháo hoa, lì xì đỏ
- Dark mode "Đêm Rằm" với ánh trăng thật

### 5.2 🔔 Live Activity / Dynamic Island (iOS) & Ongoing Notification (Android)
- Đếm ngược đến **giờ hoàng đạo** tiếp theo
- Hiển thị ngày âm ngay trên lock screen
- Dynamic Island: icon 12 con giáp xoay

### 5.3 🏮 Widget động theo 12 canh giờ
- Widget tự đổi màu / ảnh nền theo **Tý, Sửu, Dần...**
- Widget "Hôm nay" với hiệu ứng **parallax**
- Widget lock screen (iOS 16+) & Material You (Android 12+)

---

## 💰 6. Utility thiết thực & Mô hình doanh thu

> 💡 **Lưu ý quan trọng**: Lịch Số v2.0 **KHÔNG** bán Premium bằng tiền. Tất cả tính năng đều unlock qua **hệ thống điểm Công Đức** (mục 3).
>
> Doanh thu đến từ: **Quảng cáo + Affiliate + Dịch vụ bên thứ 3**.

### 6.1 🛒 Chợ Tâm Linh (Marketplace – Affiliate)
- Đặt **hoa cúng, trái cây, vàng mã** online (affiliate 10–15% với shop địa phương)
- Đặt **thầy cúng, thầy xem ngày** tại nhà (commission booking)
- Booking **chùa/đền nổi tiếng** (lễ hội, cầu an đầu năm)
- **User trả tiền cho shop/thầy, không trả cho app** → giữ triết lý "free-to-use"

### 6.2 📖 Kho văn khấn có audio
- Nghe sư thầy đọc văn khấn (offline download)
- **Karaoke mode**: chữ chạy theo giọng đọc
- Hơn 100 bài văn khấn: gia tiên, thần tài, thổ công, chùa, đền...
- **Hoàn toàn miễn phí** — dùng điểm ⚡ để unlock trong ngày

### 6.3 🗺️ Bản đồ Chùa / Đền gần bạn
- Lịch lễ hội, giờ mở cửa, giá vé
- Check-in điểm tâm linh → **+50 ☯️ / địa điểm** (boost điểm vĩnh viễn!)
- Review, ảnh cộng đồng
- Doanh thu: **sponsored listing** từ chùa/đền lớn

### 6.4 💍 Công cụ chọn ngày (cưới, động thổ, khai trương)
- So sánh **3–5 ngày đẹp nhất**
- Xuất **PDF đẹp** gửi gia đình / đối tác
- Unlock vĩnh viễn ở mốc **Đạo sĩ (15,000 ☯️)** hoặc dùng daily (150 ⚡/lần)

### 6.5 � Mô hình quảng cáo
- **Banner ad** ở cuối list (không chen giữa flow quan trọng)
- **Interstitial ad** sau khi xem quẻ xong (có nút đóng rõ ràng)
- **Rewarded ad**: xem quảng cáo 30s → **+20 ⚡ điểm ngày** (user chủ động)
- Đạt mốc **Sơ cơ (500 ☯️)** → **tắt hẳn quảng cáo vĩnh viễn** ✨

### 6.6 🎁 Nguồn doanh thu tổng hợp
| Nguồn | % ước tính |
|-------|-----------:|
| Affiliate chợ tâm linh | 40% |
| Rewarded ads + Banner | 25% |
| Booking thầy / chùa | 20% |
| Sponsored content (theme brand, chùa VIP) | 10% |
| Commission công cụ chọn ngày (nếu người dùng muốn thầy thật tư vấn) | 5% |

---

## 🗺️ 7. Roadmap triển khai

### Phase 1 – MVP v2.0 (2 tháng) – *Tăng DAU ngay*
- [ ] Daily Fortune Card + Push 7AM
- [ ] Rút quẻ Kinh Dịch / Xăm
- [ ] **Hệ thống điểm ⚡/☯️ + Streak + Daily Unlock UI** 🔑
- [ ] AI Chat "Thầy Số" (gated bằng điểm ngày)
- [ ] Theme mới theo mùa

### Phase 2 – Growth (2 tháng) – *Tăng engagement*
- [ ] Share lời chúc / quẻ lên MXH (viral hook, +30 ⚡)
- [ ] Bộ sưu tập thẻ 12 con giáp (đốt điểm ngày)
- [ ] Smart Reminder theo ngữ cảnh
- [ ] Kho văn khấn có audio + Karaoke mode
- [ ] **Milestone vĩnh viễn + Badges + Avatar frames** 🔑

### Phase 3 – Doanh thu (3 tháng) – *Affiliate & Ads*
- [ ] Rewarded ads (xem quảng cáo +20 ⚡)
- [ ] Chợ tâm linh (affiliate)
- [ ] Công cụ chọn ngày (unlock bằng điểm)
- [ ] Booking thầy cúng / chùa
- [ ] AI Phong thủy AR (unlock mốc Đạo sĩ)

### Phase 4 – Delight (2 tháng) – *Retention dài hạn*
- [ ] Theme 24 tiết khí động (unlock theo điểm)
- [ ] Live Activity / Dynamic Island
- [ ] Widget động 12 canh giờ
- [ ] OCR quét lịch giấy
- [ ] Streak Freeze as Gift (viral)

> 🚧 **Hoãn sang v2.5 / v3.0**: Cây gia phả sync, Lịch giỗ chung gia đình, Cầu an online.

---

## 📊 8. KPIs & Success Metrics

| Metric | Hiện tại (v1) | Mục tiêu v2 |
|--------|---------------|-------------|
| **DAU / MAU** | ~20% | **> 40%** |
| **Retention D7** | ~15% | **> 35%** |
| **Retention D30** | ~8% | **> 20%** |
| **Session / ngày** | 1.2 | **> 2.5** |
| **Avg session time** | 2 phút | **> 4 phút** |
| **Tỉ lệ đạt Sơ cơ (500☯️)** | N/A | **> 25%** user D30 |
| **Tỉ lệ đạt Tu tập (2,000☯️)** | N/A | **> 8%** user D90 |
| **ARPU (ads + affiliate)** | N/A | **> 8k VND/tháng** |
| **App Store rating** | 4.5 | **> 4.7** |
| **Viral K-factor** | < 0.1 | **> 0.3** |
| **Streak trung bình** | N/A | **> 12 ngày** |
| **% user giữ streak > 7 ngày** | N/A | **> 30%** |

---

## 🎯 9. 3 tính năng KILLER

Nếu chỉ được chọn 3 tính năng để làm ngay, ưu tiên:

### 🥇 1. Daily Fortune Card + Notification 7AM
**Lý do**: Tạo **thói quen mở app mỗi sáng** như đọc báo. Đây là "thanh xuân" của app.

### 🥈 2. AI "Thầy Số" Chat 24/7
**Lý do**: **Khác biệt rõ rệt** so với đối thủ (Lịch Vạn Niên, Lịch Việt truyền thống chỉ tra cứu). Biến app thành **"chuyên gia phong thủy bỏ túi"**.

### 🥉 3. Rút quẻ Kinh Dịch / Xăm hàng ngày + Streak
**Lý do**: **Giới hạn 1 lần/ngày** + **chuỗi ngày liên tiếp** → tạo cơ chế quay lại cực mạnh, không cần backend phức tạp, ship nhanh trong 1–2 tuần.

### 🎁 Bonus killer: **Hệ thống Điểm Công Đức (⚡ + ☯️)**
**Lý do**: Đây là **xương sống** gắn kết 3 tính năng killer trên. Không có hệ thống điểm này, các tính năng khác chỉ là "đồ chơi 1 lần". Có hệ thống điểm:
- User **buộc phải quay lại** để unlock daily features
- User **được thưởng** cho lòng kiên trì → cảm giác công bằng, không bị "bóc lột"
- Tạo được **sự khác biệt marketing** rõ rệt: *"App lịch âm duy nhất mà bạn không bao giờ phải trả tiền"*

---

## 📌 Ghi chú triển khai

- **Kiến trúc**: tiếp tục dùng **Compose + Hilt + Room** (Android), **SwiftUI + SwiftData** (iOS)
- **Backend mới cần**: Cloud Functions (AI proxy, billing), Firebase Analytics, Remote Config. *(Firebase Auth + Firestore sync gia đình hoãn sang v2.5+)*
- **AI**: OpenAI GPT-4o-mini cho chat (rẻ), Gemini Vision cho AR/xem tướng
- **Analytics**: Firebase Analytics + Amplitude (funnel retention)
- **A/B Test**: Remote Config cho copy notification, thứ tự feature trên Home

---

## 🔗 Tham khảo

- 📐 [docs/POINTS_ENGINE_DATA_MODEL.md](./docs/POINTS_ENGINE_DATA_MODEL.md) – Room entities, DAOs, Use Cases, DI
- 🎨 [docs/POINTS_ENGINE_UI_MOCKUP.md](./docs/POINTS_ENGINE_UI_MOCKUP.md) – Compose mockup, design tokens
- [WIDGET_IMPLEMENTATION.md](./WIDGET_IMPLEMENTATION.md)
- [DOCUMENT.md](./DOCUMENT.md)
- [CHANGELOG.md](./CHANGELOG.md)

---

> 💡 **Next step**: Chọn 1 tính năng trong **Phase 1** để prototype. Đề xuất bắt đầu với **Hệ thống Điểm Công Đức + Daily Fortune Card** song song — đây là nền móng cho mọi thứ còn lại. Có thể ship trong 3 tuần.
