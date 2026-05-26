# Tài liệu chức năng Quiz và định hướng chiến lược

Ngày cập nhật: 26/05/2026
Phạm vi: Android app, backend Go Fiber, admin web CMS, API và gameplay quiz.

## 1. Tóm tắt điều hành

Quiz nên được xem là một trụ cột chiến lược của app Lịch Số, không chỉ là một màn chơi phụ. Đây là nơi có thể tạo thói quen dùng app hằng ngày, chuyển người dùng khách thành người dùng đăng nhập, tái sử dụng nội dung lịch sử - văn hoá trong app, và tạo vòng lặp cạnh tranh nhẹ qua điểm, streak, bảng xếp hạng, trợ giúp, chia sẻ kết quả.

Hiện tại hệ thống quiz đã có các phần nền tảng quan trọng:

- Có câu hỏi từ backend, gồm nội dung, 4 đáp án, đáp án đúng, giải thích, danh mục, độ khó và gợi ý.
- Có chế độ quiz hằng ngày và quiz theo chủ đề.
- Có phiên chơi trên server cho người dùng đăng nhập, có hỗ trợ chơi khách và đồng bộ sau khi đăng nhập.
- Có bảng xếp hạng theo tuần, tháng và toàn thời gian.
- Android đã có gameplay với timer, chọn đáp án, giải thích, kết quả, review câu trả lời.
- Android hiện đã có trợ giúp bằng điểm ngày: 50/50, gợi ý, thêm thời gian. Định hướng mới là chuyển sang ví điểm chung `App Points`.
- Admin web đã có form để người soạn nội dung nhập gợi ý, hoặc tạo gợi ý bằng AI rồi duyệt lại.

Điểm cần làm tiếp nếu quiz trở thành tính năng chiến lược:

- Thiết kế lại hệ thống điểm thành một ví điểm chung cho toàn app, dùng thống nhất ở quiz và các tiện ích khác.
- Đưa xác thực đáp án và chấm điểm quan trọng về server để chống gian lận.
- Ghi nhận việc dùng trợ giúp trong phiên chơi để tính điểm công bằng hơn.
- Bắt buộc mỗi câu hỏi có gợi ý, giải thích, nguồn tham khảo và trạng thái kiểm duyệt nội dung.
- Thiết kế lại vòng lặp giữ chân: daily challenge 15 câu, streak, mùa giải, huy hiệu, danh hiệu theo chủ đề, hiệu ứng chúc mừng, chia sẻ kết quả, thử thách bạn bè.

## 2. Mục tiêu sản phẩm của Quiz

Quiz nên phục vụ 4 mục tiêu chính.

### 2.1. Tạo thói quen dùng app mỗi ngày

Quiz hằng ngày là lý do rõ ràng để người dùng mở app. Mỗi ngày 15 câu là đủ dài để tạo cảm giác “một bài thi nhỏ”, nhưng vẫn đủ ngắn để hoàn thành trong vài phút. Nếu kết hợp tiến trình, streak, danh hiệu và phần thưởng điểm chung, quiz sẽ tạo cảm giác “không muốn bỏ lỡ”.

### 2.2. Biến nội dung lịch sử thành trải nghiệm tương tác

Người dùng đọc lịch, xem ngày tốt xấu, tử vi hoặc nội dung văn hoá xong có thể được dẫn sang câu hỏi liên quan. Như vậy quiz không đứng riêng, mà trở thành lớp tương tác trên toàn bộ hệ sinh thái nội dung.

### 2.3. Tăng đăng nhập và giữ chân

Người chơi khách có thể chơi ngay. Nhưng muốn lưu điểm, vào bảng xếp hạng, giữ streak, nhận huy hiệu, đồng bộ nhiều thiết bị thì cần đăng nhập. Đây là động lực đăng nhập tự nhiên, không ép.

### 2.4. Tạo nền cho social và monetization nhẹ

Khi có điểm, bảng xếp hạng, thành tích và trợ giúp, app có thể mở rộng sang chia sẻ kết quả, đấu bạn bè, event theo mùa, vật phẩm trợ giúp, nhiệm vụ ngày, và phần thưởng đăng nhập.

## 3. Hiện trạng kiến trúc Quiz

### 3.1. Backend

Backend quản lý các thực thể chính:

- `quiz_questions`: câu hỏi, đáp án A/B/C/D, đáp án đúng, gợi ý, giải thích, danh mục, độ khó, trạng thái active.
- `quiz_daily_sets`: bộ câu hỏi theo ngày.
- `quiz_sessions`: phiên chơi của user hoặc phiên đồng bộ từ khách.
- `quiz_scores`: điểm tổng, điểm tuần, điểm tháng, streak.

Các chức năng chính ở backend:

- Lấy câu hỏi quiz hằng ngày.
- Lấy câu hỏi theo danh mục và độ khó.
- Tạo phiên chơi cho user đăng nhập.
- Nhận câu trả lời từng câu.
- Kết thúc phiên chơi và cộng điểm.
- Đồng bộ phiên chơi khách sau khi đăng nhập.
- Lấy bảng xếp hạng và thứ hạng cá nhân.
- Sinh câu hỏi bằng AI cho admin, gồm cả gợi ý và giải thích.

### 3.2. Android app

Android đang có các phần chính:

- `QuizHomeScreen`: màn hình vào quiz, quiz hôm nay, luật chơi, danh mục, bảng xếp hạng.
- `QuizSessionScreen`: màn hình gameplay, timer, trợ giúp, câu hỏi, đáp án, giải thích.
- `QuizResultScreen`: kết quả sau phiên chơi, điểm, số câu đúng, review câu trả lời.
- `LeaderboardScreen`: bảng xếp hạng theo kỳ.
- `QuizViewModel`: điều phối tải câu hỏi, tạo phiên, timer, chọn đáp án, trợ giúp, lưu/khôi phục phiên.
- `QuizRepository`: gọi API quiz từ server.
- `PointsRepository`: hiện lấy và tiêu điểm ngày để mua trợ giúp; nên được thay bằng ví điểm chung khi nâng cấp.

### 3.3. Admin web CMS

Admin quiz hiện đã hỗ trợ:

- Tạo/sửa câu hỏi.
- Nhập nội dung câu hỏi, 4 đáp án, đáp án đúng, danh mục, độ khó.
- Nhập trường `hint` cho gợi ý người chơi.
- Nhập `explanation` cho giải thích sau khi trả lời.
- Tạo câu hỏi bằng AI, trong đó AI có thể trả về `hint` và `explanation`.
- Xem preview gợi ý ở danh sách câu hỏi.

## 4. Kiểm tra riêng phần Hint

Kết luận: Android đã lấy được hint từ server về, nếu backend trả trường `hint` trong JSON.

Luồng hiện tại:

1. Người soạn nội dung nhập gợi ý trong admin web.
2. Backend lưu vào cột `quiz_questions.hint`.
3. Backend map sang DTO public qua trường `QuizQuestionPublic.Hint`.
4. API trả về JSON có trường `hint` nếu gợi ý không rỗng.
5. Android model `QuizQuestion` có `val hint: String? = null`.
6. Khi user bấm trợ giúp `Hint`, `QuizViewModel.useAssist()` đọc `question.hint`.
7. Nếu hint rỗng, app báo câu này chưa có gợi ý và không trừ điểm.
8. Nếu hint có nội dung, app hiện trừ 8 điểm ngày rồi lưu vào `state.hints`; phiên bản mới nên trừ 8 `App Points`.
9. `QuizSessionScreen` đọc `state.hints[currentQ.id]` và hiển thị qua `HintCard`.

Điều này cũng xử lý lỗi font trước đó, vì app không còn tự sinh gợi ý bằng cách biến đổi chuỗi câu hỏi. Nội dung gợi ý đến từ backend/CMS, nên người soạn nội dung kiểm soát câu chữ và dấu tiếng Việt.

Lưu ý vận hành:

- Với câu hỏi cũ chưa có `hint`, API sẽ không trả trường `hint` hoặc trả rỗng.
- Cần backfill gợi ý cho toàn bộ câu hỏi active.
- Cần quy định gợi ý không được lộ đáp án trực tiếp.
- Nên thêm kiểm tra ở admin: câu hỏi active phải có `hint` và `explanation` trước khi publish.

## 5. Gameplay hiện tại

### 5.1. Quiz hằng ngày

Luật hiện tại:

- Mỗi ngày có một bộ câu hỏi riêng.
- Android hiện đang giới hạn quiz hằng ngày còn 5 câu.
- Mỗi câu có timer 30 giây.
- Nếu hết giờ, app tự nộp câu trả lời rỗng.
- Sau khi trả lời, app hiển thị đúng/sai và giải thích.
- Sau khoảng 3,5 giây, app tự chuyển câu tiếp theo.
- Phiên quiz hằng ngày đang được lưu local để khôi phục nếu app bị đóng giữa chừng.
- Phiên hằng ngày chỉ được khôi phục trong đúng ngày hiện tại.

Định hướng mới:

- Nâng quiz hằng ngày lên 15 câu.
- Chia 15 câu thành 3 chặng, mỗi chặng 5 câu để người chơi không bị cảm giác quá dài.
- Sau câu 5 và câu 10, hiển thị milestone nhỏ: số câu đúng, streak trong phiên, danh hiệu tạm thời.
- Sau câu 15, hiển thị màn chúc mừng lớn với điểm, danh hiệu, huy hiệu mới nếu có, số điểm chung kiếm được và lời mời chia sẻ.
- Nếu người chơi thoát giữa chừng, cho phép tiếp tục trong ngày nhưng không cho chơi lại cùng bộ daily để farm điểm.

### 5.2. Quiz theo chủ đề

Luật hiện tại:

- Người dùng chọn danh mục/chủ đề.
- App tạo phiên topic quiz từ backend.
- Backend đang giới hạn khoảng 10 câu cho một phiên topic.
- Cách trả lời, timer, giải thích và kết quả giống daily quiz.

### 5.3. Người dùng khách và người dùng đăng nhập

Người dùng khách:

- Có thể chơi ngay không cần đăng nhập.
- App tự chấm đúng/sai dựa trên dữ liệu câu hỏi nhận từ server.
- Kết quả có thể lưu local dưới dạng phiên chờ đồng bộ.
- Khi đăng nhập, app có thể đồng bộ phiên khách lên server.

Người dùng đăng nhập:

- App tạo phiên chơi trên backend.
- Mỗi câu trả lời được gửi lên backend.
- Khi kết thúc, backend chấm và cập nhật điểm, streak, bảng xếp hạng.
- User có thể có thứ hạng cá nhân.

### 5.4. Trợ giúp bằng điểm chung

App hiện có 3 trợ giúp.

#### 50/50

- Giá hiện tại: 12 điểm ngày. Đề xuất mới: 12 `App Points`.
- Tác dụng: ẩn 2 đáp án sai, giữ lại đáp án đúng và 1 đáp án nhiễu.
- Mỗi câu chỉ dùng được một lần.
- Cần có thông tin đáp án đúng ở client để xác định đáp án nào được giữ.

#### Gợi ý

- Giá hiện tại: 8 điểm ngày. Đề xuất mới: 8 `App Points`.
- Tác dụng: mở gợi ý do backend/CMS cung cấp.
- Mỗi câu chỉ dùng được một lần.
- Nếu câu hỏi chưa có gợi ý, app không trừ điểm.
- Gợi ý được lưu vào state để vẫn hiển thị khi màn hình recomposition hoặc khôi phục phiên.

#### Thêm thời gian

- Giá hiện tại: 10 điểm ngày. Đề xuất mới: 10 `App Points`.
- Tác dụng: cộng thêm 15 giây.
- Timer được giới hạn tối đa 45 giây.
- Mỗi câu chỉ dùng được một lần.

### 5.5. Điểm hiện tại

Backend hiện đang chấm:

- Mỗi câu đúng: 3 điểm.
- Hoàn thành hoàn hảo: cộng thêm 5 điểm.
- Điểm được cộng vào tổng, tuần, tháng.
- Leaderboard dùng điểm đã tích lũy.

Điểm ngày trong app hiện tại:

- Là loại điểm tích lũy hằng ngày trong app.
- Được dùng để mua trợ giúp trong quiz.
- Hiện app tiêu điểm qua `PointsRepository.spendDaily()` bằng key duy nhất theo loại trợ giúp, câu hỏi và phiên chơi.
- Định hướng mới là chuyển loại điểm này thành `App Points`, một ví điểm chung toàn app.

Điểm cần làm rõ trong sản phẩm:

- Điểm ngày hiện tại nên được nâng cấp thành ví điểm chung toàn app.
- Điểm chung có thể kiếm từ nhiều hoạt động và tiêu ở nhiều tiện ích.
- Điểm xếp hạng quiz nên là chỉ số thành tích, không phải số dư có thể tiêu.
- XP hoặc level nên phản ánh tiến trình dài hạn, cũng không phải số dư có thể tiêu.

## 6. Luật chơi đề xuất phiên bản rõ ràng hơn

### 6.1. Hệ thống điểm chung toàn app

Nên thiết kế một ví điểm chung cho toàn app, tạm gọi là `App Points`.

`App Points` là số dư có thể kiếm và tiêu trong toàn bộ hệ sinh thái Lịch Số:

- Dùng trong quiz để mua trợ giúp.
- Dùng ở tiện ích khác để mở thêm lượt xem, phân tích nâng cao, nội dung đặc biệt hoặc tính năng phụ.
- Kiếm từ các hoạt động lành mạnh trong app: mở app, xem lịch, đọc nội dung, hoàn thành quiz, giữ streak, chia sẻ kết quả, hoàn thành nhiệm vụ.

Cần phân biệt rõ:

- `App Points`: ví điểm chung, có số dư, có thể cộng/trừ, dùng để mua quyền lợi.
- `Quiz Score`: điểm thành tích trong phiên quiz, dùng cho bảng xếp hạng, không bị tiêu hao.
- `XP`: kinh nghiệm dài hạn, dùng lên cấp, mở huy hiệu/danh hiệu, không bị tiêu hao.

Nguyên tắc quan trọng:

- Dùng gợi ý trong quiz sẽ trừ `App Points`, không trừ trực tiếp `Quiz Score`.
- Nhưng việc dùng gợi ý vẫn nên làm giảm điểm thành tích quiz bằng penalty để leaderboard công bằng.
- `App Points` nên do backend quản lý khi user đăng nhập.
- Với user khách, app có thể lưu tạm local và đồng bộ sau khi đăng nhập, nhưng cần giới hạn để tránh gian lận.

### 6.2. Luật Daily Quiz đề xuất

- 15 câu mỗi ngày.
- 30 giây mỗi câu.
- Chia thành 3 chặng, mỗi chặng 5 câu.
- Có 3 trợ giúp: 50/50, gợi ý, thêm thời gian.
- Mỗi loại trợ giúp chỉ dùng 1 lần mỗi câu.
- Hoàn thành quiz được `App Points` và XP.
- Chuỗi ngày chơi liên tiếp tạo streak.
- Bảng xếp hạng daily/weekly ưu tiên điểm quiz, thời gian và số trợ giúp đã dùng.
- Câu hỏi nên được phân bố theo category để có thể xét danh hiệu theo năng lực từng mảng.

Đề xuất phân bổ 15 câu:

- 5 câu dễ để tạo đà.
- 7 câu trung bình để phân hoá năng lực.
- 3 câu khó để tạo cảm giác chinh phục.

Đề xuất nhịp trải nghiệm:

- Câu 1-5: khởi động, tốc độ nhanh, ít áp lực.
- Câu 6-10: tăng độ khó, bắt đầu gợi ý danh hiệu tạm thời.
- Câu 11-15: thử thách, nhiều điểm thành tích hơn, có hiệu ứng cao trào.

Nếu muốn cá nhân hoá sâu hơn, daily set có thể gồm:

- 3 câu văn hoá/phong tục.
- 3 câu lịch sử.
- 3 câu nhân vật.
- 3 câu sự kiện/ngày tháng.
- 3 câu địa danh/triều đại/tổng hợp.

### 6.3. Luật Topic Quiz đề xuất

- 10-15 câu mỗi phiên tuỳ mục tiêu.
- Không giới hạn số lần chơi.
- Điểm topic có thể không ảnh hưởng mạnh đến leaderboard chính để tránh farm điểm.
- Dùng để luyện tập, mở danh hiệu theo chủ đề, ôn câu sai.
- Mỗi category cần có progress riêng để xét danh hiệu lâu dài.

### 6.4. Luật Event Quiz đề xuất

- Mở theo dịp đặc biệt: Tết, Giỗ Tổ Hùng Vương, 30/4, 2/9, Trung Thu, các ngày lịch sử.
- Có bộ câu hỏi riêng, giao diện riêng, huy hiệu riêng.
- Có bảng xếp hạng sự kiện theo thời gian ngắn.
- Đây là loại nội dung dễ truyền thông và push notification.

## 7. Hệ thống điểm và công thức điểm đề xuất

Backend hiện đang dùng công thức đơn giản 3 điểm/câu đúng và +5 nếu hoàn hảo. Công thức này dễ hiểu nhưng chưa đủ hấp dẫn nếu quiz là tính năng chiến lược.

Đề xuất công thức v2 cần tách 3 lớp:

- `App Points`: điểm ví chung kiếm/tiêu trong toàn app.
- `Quiz Score`: điểm thành tích trong phiên quiz và leaderboard.
- `XP`: kinh nghiệm dài hạn để lên cấp/huy hiệu.

### 7.1. Quiz Score cho bảng xếp hạng

- Đúng câu: 100 điểm.
- Bonus tốc độ: 0 đến 50 điểm, dựa trên thời gian còn lại.
- Bonus không dùng trợ giúp: +20 điểm/câu.
- Hệ số độ khó: easy x1.0, medium x1.25, hard x1.5.
- Penalty dùng trợ giúp: Hint -20, 50/50 -30, Extra Time -15.
- Bonus hoàn hảo cả phiên 15/15: +300 điểm.
- Bonus mốc 5 câu đúng liên tiếp: +50 điểm.
- Bonus mốc 10 câu đúng liên tiếp: +120 điểm.
- Bonus streak daily: +5% đến +20%, có giới hạn trần.

Ví dụ:

```text
QuestionScore = (BaseCorrect + SpeedBonus + NoAssistBonus - AssistPenalty) * DifficultyMultiplier
SessionScore = Sum(QuestionScore) + PerfectBonus + ComboBonus + StreakBonus
```

Nguyên tắc cân bằng:

- Dùng trợ giúp vẫn đáng dùng khi người chơi bí, vì đúng câu vẫn có điểm cao hơn sai.
- Người chơi giỏi, nhanh và không dùng trợ giúp có lợi thế rõ ràng.
- Người chơi mới vẫn có đường học thông qua hint và 50/50.
- Leaderboard công bằng hơn vì trợ giúp không còn là lợi thế miễn phí.

### 7.2. App Points kiếm được từ quiz

`App Points` là điểm ví chung. Hoàn thành daily quiz 15 câu nên thưởng theo công thức dễ hiểu:

- Vào chơi daily quiz: +5 điểm, chỉ nhận một lần/ngày.
- Hoàn thành 15 câu: +20 điểm.
- Mỗi câu đúng: +2 điểm.
- Đúng 10/15 trở lên: +10 điểm.
- Đúng 15/15: +30 điểm.
- Giữ streak 3 ngày: +10 điểm.
- Giữ streak 7 ngày: +30 điểm.
- Giữ streak 30 ngày: +150 điểm.

Ví dụ người chơi đúng 12/15 và hoàn thành quiz:

```text
AppPointsEarned = 5 + 20 + (12 * 2) + 10 = 59 điểm
```

Nếu người chơi dùng gợi ý hoặc trợ giúp, số điểm ví bị trừ theo giá trợ giúp. Điểm thưởng cuối phiên vẫn được cộng, nhưng net balance sẽ phản ánh cả kiếm và tiêu.

Ví dụ người chơi đúng 12/15, dùng 1 gợi ý giá 8 điểm và 1 lần 50/50 giá 12 điểm:

```text
Earned = 59
Spent = 8 + 12
NetAppPoints = +39
```

Điều này giúp người chơi thấy rõ: gợi ý trừ vào ví điểm chung, không trừ vào số câu đúng. Nhưng vì trợ giúp có penalty trong `Quiz Score`, leaderboard vẫn công bằng.

### 7.3. XP và level

XP không tiêu hao, dùng cho cấp độ và mở danh hiệu:

- Hoàn thành daily quiz: +50 XP.
- Mỗi câu đúng: +5 XP.
- Đúng câu khó: +5 XP thêm.
- Đúng 15/15: +100 XP.
- Hoàn thành quiz theo chủ đề: +30 XP.

XP giúp app có tiến trình dài hạn, không gây lo lắng vì người chơi không bị mất XP khi dùng trợ giúp.

### 7.4. Điểm trừ khi dùng gợi ý nằm ở đâu

Khi người chơi dùng gợi ý:

- Trừ `App Points` trong ví điểm chung.
- Ghi transaction vào ledger điểm chung.
- Ghi assist usage vào phiên quiz.
- Áp dụng penalty vào `Quiz Score` của câu đó.
- Không trừ XP.
- Không trừ số câu đúng.

Ví dụ transaction:

```text
type = spend
source = quiz_assist
assist_type = hint
amount = -8
session_id = ...
question_id = ...
```

Ví dụ scoring:

```text
App Points: -8 điểm
Quiz Score: -20 penalty trong câu đó
XP: không đổi
```

Như vậy người chơi hiểu rõ: “Mình dùng điểm chung để mua gợi ý, còn điểm thi đua bị giảm nhẹ vì đã dùng trợ giúp”.

## 8. Hệ thống điểm chung toàn app

### 8.1. Tên gọi đề xuất

Nên dùng một tên thân thiện thay vì chỉ gọi là điểm. Một vài lựa chọn:

- `Xu Lịch Số`: dễ hiểu, có cảm giác ví chung.
- `Điểm Lịch Số`: rõ ràng, trung tính.
- `Lộc Điểm`: có màu sắc văn hoá nhưng cần cân nhắc vì hơi thiên may mắn.
- `Tinh Hoa`: giàu chất học tập, hợp với app lịch sử/văn hoá.

Khuyến nghị: dùng `Điểm Lịch Số` ở giai đoạn đầu vì rõ nghĩa nhất. Sau này nếu muốn gamification đậm hơn có thể đổi tên marketing thành `Xu Lịch Số`.

### 8.2. Cách kiếm điểm chung

Các nguồn kiếm điểm nên thống nhất toàn app:

- Mở app mỗi ngày: +5 điểm, một lần/ngày.
- Xem lịch hôm nay: +3 điểm, một lần/ngày.
- Đọc xong một nội dung lịch sử/văn hoá: +5 điểm, tối đa 3 lần/ngày.
- Hoàn thành daily quiz 15 câu: +20 điểm.
- Mỗi câu quiz đúng: +2 điểm.
- Đúng 10/15 trong daily quiz: +10 điểm.
- Đúng 15/15 trong daily quiz: +30 điểm.
- Giữ streak 3 ngày: +10 điểm.
- Giữ streak 7 ngày: +30 điểm.
- Giữ streak 30 ngày: +150 điểm.
- Chia sẻ kết quả quiz: +5 điểm, tối đa 1 lần/ngày.
- Hoàn thành nhiệm vụ ngày: +20 điểm.
- Đăng nhập lần đầu: +50 điểm.

Nên có giới hạn kiếm điểm mỗi ngày để tránh farm:

- Người dùng khách: tối đa 50 điểm/ngày, lưu tạm local.
- Người dùng đăng nhập: tối đa 150 điểm/ngày từ hoạt động thường.
- Điểm thưởng event có thể vượt trần nhưng phải được backend ghi nhận.

### 8.3. Cách tiêu điểm chung

Trong quiz:

- Gợi ý: 8 điểm.
- Thêm 15 giây: 10 điểm.
- 50/50: 12 điểm.
- Gợi ý cấp 2 sau này: 15 điểm.
- Đổi câu hỏi trong practice: 15 điểm, không dùng trong daily leaderboard.

Trong tiện ích khác:

- Mở thêm lượt xem phân tích nâng cao.
- Mở nội dung chuyên sâu.
- Lưu thêm mẫu/cấu hình cá nhân.
- Tạo thêm lượt AI giải thích nếu có.
- Mở theme/huy hiệu/trang trí hồ sơ.

Nguyên tắc tiêu điểm:

- Mọi tiêu điểm phải có transaction ledger.
- Mọi giao dịch tiêu điểm phải idempotent để tránh trừ 2 lần khi mạng lỗi.
- Nếu tính năng lỗi hoặc thiếu dữ liệu, hoàn điểm tự động.
- Với quiz, nếu câu hỏi không có hint thì không trừ điểm khi bấm gợi ý.

### 8.4. Ledger điểm chung

Nên có bảng điểm chung toàn app:

```text
point_wallets
- id
- user_id
- balance
- lifetime_earned
- lifetime_spent
- created_at
- updated_at

point_transactions
- id
- user_id
- amount
- direction
- source
- source_id
- idempotency_key
- metadata
- created_at
```

Ví dụ `source`:

- `daily_checkin`
- `view_today_calendar`
- `read_article`
- `quiz_daily_complete`
- `quiz_correct_answer`
- `quiz_assist_hint`
- `quiz_assist_fifty_fifty`
- `quiz_assist_extra_time`
- `event_reward`
- `refund`

### 8.5. Đồng bộ với khách và đăng nhập

Người dùng khách:

- Có thể kiếm điểm tạm.
- Có thể dùng điểm tạm cho quiz.
- Có giới hạn thấp hơn user đăng nhập.
- Khi đăng nhập, app gửi pending transactions lên backend.

Backend:

- Deduplicate bằng `idempotency_key`.
- Chỉ nhận transaction hợp lệ trong giới hạn.
- Có thể không nhận các transaction đáng ngờ.
- Trả lại balance chính thức sau sync.

### 8.6. Vì sao không dùng trực tiếp Quiz Score làm ví điểm

Không nên dùng `Quiz Score` làm điểm tiêu dùng vì:

- Người chơi sẽ sợ dùng gợi ý vì thấy mất thành tích.
- Leaderboard sẽ bị lẫn giữa năng lực và chi tiêu.
- Các tiện ích ngoài quiz không có lý do dùng `Quiz Score`.
- Khó cân bằng nếu vừa là điểm xếp hạng vừa là tiền tệ.

Thiết kế đúng là:

- `App Points`: tiền tệ mềm toàn app.
- `Quiz Score`: thành tích cạnh tranh.
- `XP`: tiến trình dài hạn.

## 9. Danh hiệu, huy hiệu và hiệu ứng chúc mừng

### 9.1. Hiệu ứng chúc mừng trong phiên quiz

Quiz 15 câu nên có nhiều lớp phản hồi tích cực:

- Trả lời đúng: hiệu ứng sáng nhẹ quanh đáp án, âm thanh nhỏ nếu app có sound setting, text động như “Chính xác”.
- Trả lời đúng liên tiếp 3 câu: badge nhỏ “Chuỗi 3”.
- Trả lời đúng liên tiếp 5 câu: hiệu ứng milestone mạnh hơn, rung nhẹ, chip “Đang vào guồng”.
- Hoàn thành chặng 5 câu: màn mini summary 2-3 giây.
- Hoàn thành 15 câu: màn chúc mừng lớn có confetti, danh hiệu phiên, điểm kiếm được, streak, nút chia sẻ.

Không nên lạm dụng hiệu ứng:

- Người chơi sai vẫn cần phản hồi nhẹ nhàng, không làm họ nản.
- Có setting tắt âm thanh/rung.
- Hiệu ứng phải ngắn để không làm chậm nhịp chơi.

### 9.2. Danh hiệu theo kết quả phiên daily

Danh hiệu phiên dựa trên số câu đúng:

- 15/15: `Trạng Nguyên Lịch Số`
- 14/15: `Bảng Nhãn Tri Thức`
- 13/15: `Thám Hoa Sử Việt`
- 11-12/15: `Sĩ Tử Uyên Bác`
- 9-10/15: `Người Giữ Mạch Sử`
- 6-8/15: `Học Giả Tập Sự`
- 0-5/15: `Tân Binh Lịch Số`

Các danh hiệu này hiển thị ở kết quả phiên và có thể chia sẻ. Danh hiệu cao nhất đạt được trong tuần có thể lưu vào hồ sơ.

### 9.3. Danh hiệu theo category

Vì câu hỏi đã được phân loại, app nên theo dõi năng lực theo từng category. Mỗi category có progress riêng:

```text
category_mastery
- user_id
- category
- answered_count
- correct_count
- correct_rate
- current_streak
- best_streak
- mastery_level
- title
```

Điều kiện mở danh hiệu nên dựa trên cả số lượng câu và tỷ lệ đúng để tránh may mắn:

- Cấp 1: trả lời đúng ít nhất 10 câu trong category, tỷ lệ đúng >= 60%.
- Cấp 2: trả lời đúng ít nhất 30 câu, tỷ lệ đúng >= 70%.
- Cấp 3: trả lời đúng ít nhất 60 câu, tỷ lệ đúng >= 80%.
- Cấp 4: trả lời đúng ít nhất 100 câu, tỷ lệ đúng >= 85%.
- Cấp 5: trả lời đúng ít nhất 200 câu, tỷ lệ đúng >= 90%.

Đề xuất danh hiệu theo category:

- `history`: Sử Sinh, Sử Gia, Quốc Sử Quán, Bậc Thầy Sử Việt, Đại Sử Gia.
- `culture`: Người Am Tường Phong Tục, Nhà Văn Hoá, Người Giữ Nếp Xưa, Bậc Thầy Văn Hoá, Quốc Hồn Quốc Tuý.
- `festival`: Người Rành Lễ Tết, Sứ Giả Mùa Lễ, Chuyên Gia Lễ Hội, Bậc Thầy Phong Tục, Lễ Quan Lịch Số.
- `dynasty`: Sĩ Tử Triều Đại, Người Thuộc Long Mạch, Chuyên Gia Vương Triều, Bậc Thầy Triều Đại, Quốc Triều Thông Giám.
- `figure`: Người Kể Chuyện Danh Nhân, Tri Kỷ Anh Hùng, Chuyên Gia Nhân Vật, Bậc Thầy Danh Nhân, Danh Nhân Ký Sự.
- `geography`: Người Theo Dấu Địa Danh, Lữ Khách Sử Việt, Chuyên Gia Địa Danh, Bậc Thầy Non Sông, Sơn Hà Ký.

Nếu category trong DB đang dùng tên khác, cần map sang bộ title tương ứng trong cấu hình backend.

### 9.4. Huy hiệu theo hành vi

Ngoài danh hiệu năng lực, nên có huy hiệu hành vi:

- `Không Bỏ Ngày Nào`: hoàn thành daily 7 ngày liên tiếp.
- `Một Mạch 15 Câu`: hoàn thành 15 câu không thoát.
- `Không Cần Trợ Giúp`: đạt 12/15 trở lên không dùng trợ giúp.
- `Cú Lội Ngược Dòng`: sai 3 câu đầu nhưng kết thúc >= 10/15.
- `Tốc Chiến`: hoàn thành daily dưới 5 phút và >= 12/15.
- `Người Ham Học`: đọc giải thích của toàn bộ câu sai.

### 9.5. Lưu danh hiệu

Nên tách:

- `Session Title`: danh hiệu của một phiên chơi, có thể chia sẻ nhưng không nhất thiết lưu vĩnh viễn.
- `Category Title`: danh hiệu lâu dài theo năng lực từng category.
- `Season Title`: danh hiệu đạt được trong tuần/tháng/event.
- `Badge`: huy hiệu vĩnh viễn khi đạt điều kiện.

Người dùng nên có một danh hiệu đại diện trên hồ sơ, được chọn từ các danh hiệu đã mở.

## 10. Rủi ro và vấn đề kỹ thuật hiện tại

### 10.1. Client đang nhận đáp án đúng

API public hiện trả cả `correct` và `correct_answer`. Điều này giúp app chấm offline và hiển thị nhanh, nhưng nếu quiz có leaderboard chiến lược thì đây là rủi ro gian lận lớn.

Đề xuất:

- Với phiên có xếp hạng, client không nên nhận đáp án đúng trước khi trả lời.
- Backend nên xác thực câu trả lời và trả result từng câu sau khi submit.
- Với chế độ practice/offline, có thể giữ cơ chế cũ hoặc tạo endpoint riêng ít cạnh tranh hơn.

### 10.2. Điểm ví chung tiêu ở client có thể bị sửa

Nếu điểm ví chung được lưu local, người dùng kỹ thuật cao có thể can thiệp. Nếu trợ giúp ảnh hưởng leaderboard hoặc quyền lợi toàn app, server cần là nguồn sự thật cho số dư điểm và lịch sử tiêu điểm.

Đề xuất:

- Giai đoạn đầu: giữ local cho tốc độ triển khai.
- Giai đoạn chiến lược: chuyển tiêu điểm quiz và các tiện ích khác lên backend.
- Server ghi transaction: user, session, question, assist type, cost, timestamp.

### 10.3. Chưa ghi nhận trợ giúp trong session server

Hiện assist chủ yếu là state local. Backend chưa dùng assist usage để tính điểm hoặc phân bảng xếp hạng.

Đề xuất:

- Thêm trường assist usage vào `quiz_sessions.answers` hoặc bảng riêng `quiz_assist_usages`.
- Submit answer gửi kèm danh sách trợ giúp đã dùng cho câu đó.
- Backend áp dụng penalty hoặc tách leaderboard.

### 10.4. Nội dung gợi ý chưa đầy đủ cho câu hỏi cũ

Các câu hỏi trước khi có cột `hint` có thể đang trống gợi ý.

Đề xuất:

- Chạy job backfill hint bằng AI cho toàn bộ câu hỏi active.
- Đưa vào admin trạng thái: thiếu hint, thiếu explanation, thiếu source.
- Không publish câu hỏi mới nếu thiếu hint hoặc explanation.

### 10.5. Chưa có nguồn tham khảo và kiểm duyệt nội dung

Quiz lịch sử/văn hoá rất nhạy về độ chính xác. Nếu sai, người dùng mất niềm tin nhanh.

Đề xuất thêm trường:

- `source_title`
- `source_url`
- `source_note`
- `review_status`
- `reviewed_by`
- `reviewed_at`
- `version`

### 10.6. Thời gian trả lời cần đo chính xác hơn

Hiện app tính thời gian dựa trên timer còn lại. Khi có trợ giúp thêm thời gian, chỉ số `timeMs` có thể chưa phản ánh đúng thời gian thực tế người chơi đã dùng.

Đề xuất:

- Lưu `questionStartedAtMs` cho từng câu.
- Khi submit, tính `elapsedMs = now - questionStartedAtMs`.
- Backend nhận `elapsed_ms` nhưng vẫn nên kiểm tra hợp lý bằng server time.

### 10.7. Phiên đăng nhập có thể fallback sang guest

Nếu tạo phiên server lỗi, app có thể fallback sang chơi khách. Điều này tốt cho trải nghiệm, nhưng có thể gây hiểu nhầm nếu user nghĩ điểm đã lưu server.

Đề xuất:

- Hiển thị thông báo rõ: “Mất kết nối, phiên này sẽ lưu tạm và đồng bộ khi có mạng”.
- Khi sync thành công, báo lại cho user.

## 11. Đề xuất làm Quiz hấp dẫn hơn

### 11.1. Daily Challenge như một nghi thức mỗi ngày

Thiết kế daily quiz thành một nghi thức ngắn:

- Mỗi ngày 15 câu, hoàn thành trong 5-7 phút.
- Chia 15 câu thành 3 chặng, mỗi chặng 5 câu để người chơi luôn thấy tiến độ rõ ràng.
- Chủ đề gắn với ngày hôm nay: âm lịch, sự kiện lịch sử, lễ tết, nhân vật, văn hoá.
- Có streak theo ngày.
- Có huy hiệu cho mốc 3, 7, 14, 30 ngày.
- Có câu “đặc biệt hôm nay” liên kết sang bài viết hoặc lịch ngày.

### 11.2. Mùa giải và bảng xếp hạng tuần

Leaderboard dài hạn dễ làm người mới nản. Nên có mùa giải ngắn:

- Bảng xếp hạng tuần là mặc định.
- Mỗi tuần reset cạnh tranh.
- Cuối tuần trao danh hiệu: Trạng Nguyên Tuần, Bảng Nhãn, Thám Hoa.
- Lưu danh hiệu vào hồ sơ.

### 11.3. Huy hiệu và bộ sưu tập lịch sử

Thay vì chỉ điểm, app nên cho người chơi sưu tập:

- Huy hiệu theo chủ đề: Văn hoá, Triều đại, Nhân vật, Địa danh, Lễ Tết.
- Thẻ sưu tập: nhân vật lịch sử, sự kiện, di sản, phong tục.
- Mỗi lần hoàn thành quiz hoặc streak có cơ hội mở thẻ.
- Thẻ có thể dẫn sang nội dung học thêm.

### 11.4. Ôn lại câu sai

Đây là tính năng vừa giáo dục vừa giữ chân:

- Sau mỗi phiên, lưu danh sách câu sai.
- Có màn “Ôn lại câu sai”.
- Sau 1 ngày, 3 ngày, 7 ngày nhắc ôn lại bằng spaced repetition.
- Nếu trả lời đúng lại, đánh dấu đã nắm vững.

### 11.5. Chế độ sinh tồn

Một mode dễ gây nghiện:

- Người chơi trả lời liên tục đến khi sai 3 câu hoặc hết thời gian.
- Câu hỏi khó dần.
- Có leaderboard riêng theo chuỗi đúng dài nhất.
- Trợ giúp giới hạn rất ít.

### 11.6. Thử thách bạn bè

Tính xã hội nhẹ, không cần real-time phức tạp ban đầu:

- User chơi một bộ 5 câu ngắn hoặc một bộ 15 câu đầy đủ.
- Chia sẻ link/mã thách đấu.
- Bạn bè chơi cùng bộ câu hỏi.
- So sánh điểm, thời gian, số trợ giúp đã dùng.

### 11.7. Chia sẻ kết quả đẹp

Sau mỗi daily quiz, tạo card chia sẻ:

- Điểm hôm nay.
- Số câu đúng.
- Streak.
- Danh hiệu vui.
- Không lộ đáp án.

Nên có style văn hoá Việt, dùng màu/hoa văn riêng thay vì card generic.

### 11.8. Event theo lịch Việt Nam

Quiz có lợi thế lớn vì app gắn với lịch. Nên tận dụng:

- Tết Nguyên Đán: phong tục, lịch âm, kiêng kỵ, sự tích.
- Giỗ Tổ Hùng Vương: thời Hùng Vương, truyền thuyết, địa danh.
- 30/4 và 2/9: lịch sử hiện đại.
- Trung Thu: văn hoá dân gian.
- Rằm tháng Giêng, Vu Lan, Tết Đoan Ngọ.

Mỗi event có landing nhỏ, quiz riêng, huy hiệu riêng, bảng xếp hạng riêng.

### 11.9. Cá nhân hoá độ khó

Sau vài phiên, app có thể tự điều chỉnh:

- Người chơi hay sai chủ đề nào thì gợi ý luyện tập chủ đề đó.
- Người chơi quá giỏi thì tăng tỷ lệ câu medium/hard.
- Người mới được nhiều câu easy hơn để không nản.
- Hệ thống đề xuất bài đọc trước khi quiz.

### 11.10. “Học trước, đấu sau”

Liên kết nội dung và quiz:

- Sau khi đọc một bài lịch sử/văn hoá, hiện CTA “Làm 3 câu kiểm tra nhanh”.
- Nếu trả lời đúng, nhận XP và thẻ sưu tập.
- Nếu sai, gợi ý đoạn nội dung liên quan để đọc lại.

## 12. Content pipeline đề xuất

### 12.1. Chuẩn câu hỏi

Mỗi câu hỏi nên có đầy đủ:

- Nội dung câu hỏi rõ ràng, không mơ hồ.
- 4 đáp án có độ dài tương đối cân bằng.
- Chỉ có 1 đáp án đúng.
- Gợi ý không lộ đáp án trực tiếp.
- Giải thích sau khi trả lời.
- Nguồn tham khảo.
- Danh mục.
- Độ khó.
- Trạng thái kiểm duyệt.

### 12.2. Quy trình tạo câu hỏi

Quy trình đề xuất:

1. AI tạo bản nháp câu hỏi, đáp án, hint, explanation.
2. Người soạn nội dung chỉnh câu chữ.
3. Người kiểm duyệt xác minh đáp án và nguồn.
4. Câu hỏi được publish.
5. Sau khi người dùng chơi, hệ thống ghi nhận tỷ lệ đúng/sai và báo cáo lỗi.
6. Câu hỏi có vấn đề được đưa về trạng thái cần xem lại.

### 12.3. Chỉ số chất lượng câu hỏi

Nên theo dõi:

- Tỷ lệ trả lời đúng.
- Thời gian trung bình để trả lời.
- Tỷ lệ dùng hint.
- Tỷ lệ dùng 50/50.
- Đáp án sai phổ biến nhất.
- Tỷ lệ report câu hỏi.
- Tỷ lệ người chơi thoát ở câu đó.

Các chỉ số này giúp phát hiện câu quá dễ, quá khó, gây hiểu nhầm hoặc sai kiến thức.

## 13. API và dữ liệu nên cải tiến

### 13.1. API session cạnh tranh

Đề xuất endpoint cho competitive quiz:

```text
POST /api/v1/quiz/sessions
GET  /api/v1/quiz/sessions/{id}/questions
POST /api/v1/quiz/sessions/{id}/answers
POST /api/v1/quiz/sessions/{id}/assists
POST /api/v1/quiz/sessions/{id}/finish
```

Với competitive mode, response câu hỏi không nên chứa đáp án đúng. Sau khi submit answer, server trả về:

```json
{
  "is_correct": true,
  "correct": "c",
  "correct_answer": "Tết Nguyên Đán",
  "explanation": "...",
  "score_delta": 128
}
```

### 13.2. Bảng assist usage

Có thể thêm bảng:

```text
quiz_assist_usages
- id
- session_id
- user_id
- question_id
- assist_type
- cost_app_points
- created_at
```

Lợi ích:

- Chấm điểm công bằng.
- Chống dùng trợ giúp nhiều lần.
- Analytics biết người chơi bí ở đâu.
- Có thể hoàn điểm nếu lỗi mạng hoặc câu hỏi thiếu hint.

### 13.3. API điểm chung toàn app

Đề xuất endpoint:

```text
GET  /api/v1/points/wallet
GET  /api/v1/points/transactions
POST /api/v1/points/earn
POST /api/v1/points/spend
POST /api/v1/points/sync-guest
```

Trong đó:

- `earn` dùng cho các hoạt động hợp lệ như daily check-in, đọc nội dung, hoàn thành quiz.
- `spend` dùng cho quiz assist và tiện ích khác.
- `sync-guest` nhận các transaction local có `idempotency_key`, backend kiểm tra rồi hợp nhất.
- App không tự cộng/trừ số dư cuối cùng khi đã đăng nhập; backend trả balance chính thức.

Ví dụ spend gợi ý:

```json
{
  "source": "quiz_assist_hint",
  "amount": 8,
  "idempotency_key": "quiz_session_123_question_456_hint",
  "metadata": {
    "session_id": 123,
    "question_id": 456,
    "assist_type": "hint"
  }
}
```

### 13.4. Bảng danh hiệu category

Nên thêm bảng hoặc materialized view:

```text
quiz_category_masteries
- id
- user_id
- category
- answered_count
- correct_count
- correct_rate
- current_streak
- best_streak
- mastery_level
- title
- last_unlocked_at
- created_at
- updated_at

user_badges
- id
- user_id
- badge_key
- source
- unlocked_at
- metadata
```

Khi user hoàn thành một câu hỏi, backend cập nhật category mastery tương ứng. Khi vượt điều kiện danh hiệu, backend trả về unlock event để Android hiển thị hiệu ứng chúc mừng.

### 13.5. Trạng thái nội dung

Thêm vào `quiz_questions`:

```text
source_title
source_url
source_note
review_status
reviewed_by
reviewed_at
published_at
version
```

Các trạng thái gợi ý:

- `draft`
- `needs_review`
- `approved`
- `published`
- `archived`

## 14. Roadmap đề xuất

### Giai đoạn 1: Củng cố nền tảng hiện tại

Mục tiêu: quiz ổn định, rõ luật, nội dung đầy đủ.

Việc nên làm:

- Backfill hint cho toàn bộ câu hỏi active.
- Bắt buộc admin nhập hint và explanation khi publish.
- Thêm nguồn tham khảo và trạng thái kiểm duyệt.
- Ghi assist usage vào backend.
- Nâng daily quiz từ 5 câu lên 15 câu.
- Hiển thị rõ `App Points` dùng để mua trợ giúp.
- Tạo ledger điểm chung toàn app ở backend.
- Sửa đo thời gian trả lời theo elapsed time thực tế.

### Giai đoạn 2: Công bằng leaderboard

Mục tiêu: quiz có thể cạnh tranh nghiêm túc.

Việc nên làm:

- Không trả đáp án đúng trước khi user submit trong session xếp hạng.
- Backend chấm điểm authoritative.
- Áp dụng công thức điểm v2.
- Tách leaderboard có trợ giúp và không trợ giúp, hoặc trừ điểm trợ giúp.
- Chống replay submit và validate session state.

### Giai đoạn 3: Retention loop

Mục tiêu: tạo thói quen hằng ngày.

Việc nên làm:

- Streak và phần thưởng streak.
- Huy hiệu daily, weekly, category.
- Danh hiệu theo category dựa trên tỷ lệ đúng và số câu đã trả lời.
- Hiệu ứng chúc mừng theo milestone 5/10/15 câu.
- Card chia sẻ kết quả.
- Push notification daily challenge.
- Ôn lại câu sai.
- Weekly season leaderboard.

### Giai đoạn 4: Social và event

Mục tiêu: tăng lan truyền và mùa vụ.

Việc nên làm:

- Challenge bạn bè bằng link.
- Event quiz theo lịch Việt Nam.
- Bộ sưu tập thẻ lịch sử/văn hoá.
- Danh hiệu theo mùa.
- Trang hồ sơ thành tích.

### Giai đoạn 5: Cá nhân hoá và học tập

Mục tiêu: biến quiz thành engine học lịch sử/văn hoá.

Việc nên làm:

- Adaptive difficulty.
- Đề xuất bài đọc theo câu sai.
- Spaced repetition.
- Phân tích năng lực theo chủ đề.
- AI giải thích thêm theo từng câu, nhưng phải dựa trên nguồn đã duyệt.

## 15. KPI nên theo dõi

Các chỉ số sản phẩm:

- Quiz start rate: tỷ lệ user mở quiz.
- Daily quiz completion rate: tỷ lệ hoàn thành quiz hằng ngày.
- D1/D7/D30 retention của user có chơi quiz so với không chơi quiz.
- Login conversion sau khi chơi quiz khách.
- Leaderboard participation rate.
- Share rate sau kết quả.
- Streak retention: tỷ lệ giữ streak 3/7/14 ngày.
- Assist usage rate theo loại trợ giúp.
- Hint availability rate: tỷ lệ câu active có hint.
- App Points earned/spent per user per day.
- Tỷ lệ tiêu điểm cho quiz assist so với tiện ích khác.
- Tỷ lệ mở danh hiệu theo category.
- Question report rate.
- Average session length.

Các chỉ số nội dung:

- Tỷ lệ đúng theo câu hỏi.
- Tỷ lệ đúng theo danh mục.
- Tỷ lệ dùng hint theo câu.
- Tỷ lệ dùng 50/50 theo câu.
- Thời gian trung bình trả lời.
- Tỷ lệ thoát giữa phiên.

## 16. Quyết định sản phẩm đề xuất

Nên chọn hướng:

- Quiz daily là chế độ chính, dễ chơi, có thưởng, có streak.
- Quiz daily nên dùng bộ 15 câu/ngày.
- Topic quiz là chế độ luyện tập và khám phá nội dung.
- Leaderboard chính ưu tiên weekly, không phải all-time.
- `App Points` là ví điểm chung toàn app, dùng mua trợ giúp và quyền lợi ở các tiện ích.
- `Quiz Score` dùng cho leaderboard, không phải số dư để tiêu.
- `XP` dùng cho level/huy hiệu, không tiêu hao.
- Gợi ý luôn do backend/CMS cung cấp, không tự sinh ở client.
- Competitive quiz phải được server chấm và không lộ đáp án trước khi submit.
- Mỗi câu hỏi publish phải có hint, explanation và nguồn tham khảo.

## 17. Checklist triển khai gần nhất

Ưu tiên kỹ thuật:

- Backfill hint cho câu hỏi cũ.
- Thêm validation admin: active question cần hint và explanation.
- Ghi assist usage vào backend.
- Tạo `point_wallets` và `point_transactions` cho điểm chung toàn app.
- Chuyển tiêu điểm trợ giúp quiz sang ledger backend khi user đã đăng nhập.
- Cập nhật daily quiz lên 15 câu.
- Chuyển competitive session sang server-side answer reveal.
- Cập nhật scoring v2 hoặc ít nhất ghi thêm `time_ms` và `assist_used` để chuẩn bị.
- Đồng bộ tiêu điểm khách lên backend sau khi đăng nhập.

Ưu tiên sản phẩm:

- Viết lại rules trong UI cho dễ hiểu.
- Làm result card có thể chia sẻ.
- Thêm streak/huy hiệu daily.
- Thêm danh hiệu phiên và danh hiệu theo category.
- Thêm hiệu ứng chúc mừng ở mốc 5/10/15 câu.
- Thêm “Ôn lại câu sai”.
- Chuẩn bị event quiz theo lịch Việt Nam.

## 18. Kết luận

Quiz hiện đã có nền khá tốt: backend có câu hỏi, session, điểm, leaderboard; Android có gameplay, timer, result, trợ giúp; admin có nhập hint và AI draft. Điểm mạnh nhất để phát triển tiếp là biến quiz thành vòng lặp hằng ngày 15 câu gắn với lịch Việt Nam, danh hiệu theo năng lực, ví điểm chung toàn app và nội dung văn hoá/lịch sử.

Nếu muốn biến quiz thành phần chiến lược của app, bước quan trọng nhất không chỉ là thêm nhiều câu hỏi, mà là chuẩn hoá luật chơi 15 câu/ngày, chuẩn hoá `App Points`, bảo vệ leaderboard, nâng chất lượng nội dung, và xây vòng lặp retention gồm daily challenge, streak, danh hiệu, huy hiệu, chia sẻ và event theo mùa.
