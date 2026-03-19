package com.lichso.app.data.ai

/**
 * Notion-style templates cho Task/Note/Reminder
 * Người dùng chọn template → AI tự động tạo items phù hợp
 */
object AiTemplates {

    data class QuickTemplate(
        val id: String,
        val icon: String,
        val title: String,
        val subtitle: String,
        val category: TemplateCategory,
        val prompt: String  // Prompt gửi tới AI hoặc quick-create
    )

    enum class TemplateCategory(val label: String) {
        WORK("Công việc"),
        PERSONAL("Cá nhân"),
        STUDY("Học tập"),
        HEALTH("Sức khoẻ"),
        TRAVEL("Du lịch"),
        EVENT("Sự kiện"),
        FINANCE("Tài chính"),
        PARENTING("Gia đình"),
        CREATIVE("Sáng tạo"),
        SPIRITUAL("Tâm linh")
    }

    val quickTemplates = listOf(
        // ══════════════════════════════════
        // ── Công việc (WORK) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "daily_standup",
            icon = "☀",
            title = "Kế hoạch ngày",
            subtitle = "Checklist công việc trong ngày",
            category = TemplateCategory.WORK,
            prompt = "Tạo checklist kế hoạch ngày hôm nay gồm: kiểm tra email, họp team, hoàn thành task quan trọng, review code, cập nhật tiến độ"
        ),
        QuickTemplate(
            id = "weekly_plan",
            icon = "📋",
            title = "Kế hoạch tuần",
            subtitle = "Lên lịch công việc cả tuần",
            category = TemplateCategory.WORK,
            prompt = "Tạo kế hoạch tuần gồm các task: Thứ 2 - Lên kế hoạch tuần, Thứ 3 - Họp team, Thứ 4 - Hoàn thành task chính, Thứ 5 - Review & sửa lỗi, Thứ 6 - Tổng kết tuần"
        ),
        QuickTemplate(
            id = "meeting_notes",
            icon = "🗒",
            title = "Ghi chú cuộc họp",
            subtitle = "Template ghi chú meeting",
            category = TemplateCategory.WORK,
            prompt = "Tạo ghi chú cuộc họp với nội dung template: Chủ đề, Người tham gia, Nội dung thảo luận, Kết luận, Việc cần làm tiếp theo"
        ),
        QuickTemplate(
            id = "project_tasks",
            icon = "🚀",
            title = "Dự án mới",
            subtitle = "Checklist khởi động dự án",
            category = TemplateCategory.WORK,
            prompt = "Tạo checklist dự án mới: Phân tích yêu cầu (Cao), Thiết kế hệ thống (Cao), Lên timeline (Vừa), Phân công nhiệm vụ (Vừa), Setup môi trường (Thấp), Kick-off meeting (Cao)"
        ),
        QuickTemplate(
            id = "email_followup",
            icon = "📧",
            title = "Follow-up email",
            subtitle = "Nhắc nhở theo dõi email",
            category = TemplateCategory.WORK,
            prompt = "nhắc tôi kiểm tra email quan trọng lúc 9h sáng hàng ngày"
        ),
        QuickTemplate(
            id = "presentation_prep",
            icon = "📊",
            title = "Chuẩn bị thuyết trình",
            subtitle = "Checklist chuẩn bị presentation",
            category = TemplateCategory.WORK,
            prompt = "Tạo checklist chuẩn bị thuyết trình: Nghiên cứu nội dung (Cao), Tạo slide PowerPoint (Cao), Chuẩn bị dữ liệu minh hoạ (Vừa), Tập trình bày 2-3 lần (Cao), Kiểm tra thiết bị (Thấp), Đến sớm 15 phút (Vừa)"
        ),
        QuickTemplate(
            id = "monthly_review",
            icon = "📈",
            title = "Review tháng",
            subtitle = "Tổng kết & đánh giá tháng",
            category = TemplateCategory.WORK,
            prompt = "Tạo ghi chú review tháng với nội dung: Mục tiêu đã đặt, Kết quả đạt được, Bài học rút ra, Khó khăn gặp phải, Mục tiêu tháng tới"
        ),

        // ══════════════════════════════════
        // ── Cá nhân (PERSONAL) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "shopping_list",
            icon = "🛒",
            title = "Đi chợ / Mua sắm",
            subtitle = "Danh sách đồ cần mua",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo checklist đi chợ: Rau xanh, Thịt/cá, Trái cây, Gia vị, Đồ uống, Đồ gia dụng"
        ),
        QuickTemplate(
            id = "house_chores",
            icon = "🏠",
            title = "Việc nhà",
            subtitle = "Checklist dọn dẹp nhà cửa",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo checklist việc nhà: Quét/lau nhà, Giặt đồ, Rửa chén, Dọn phòng ngủ, Đổ rác, Tưới cây"
        ),
        QuickTemplate(
            id = "morning_routine",
            icon = "🌅",
            title = "Thói quen buổi sáng",
            subtitle = "Nhắc nhở routine hàng ngày",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo nhắc nhở buổi sáng hàng ngày: Thức dậy lúc 6:00, Tập thể dục lúc 6:15, Ăn sáng lúc 7:00, Đọc sách lúc 7:30"
        ),
        QuickTemplate(
            id = "evening_routine",
            icon = "🌙",
            title = "Thói quen buổi tối",
            subtitle = "Routine trước khi ngủ",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo nhắc nhở buổi tối hàng ngày: Chuẩn bị đồ ngày mai lúc 20:00, Đọc sách/thiền lúc 21:00, Tắt điện thoại lúc 22:00, Đi ngủ lúc 22:30"
        ),
        QuickTemplate(
            id = "self_care",
            icon = "🧖",
            title = "Chăm sóc bản thân",
            subtitle = "Nhắc nhở skincare & wellness",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo nhắc nhở chăm sóc bản thân hàng ngày: Rửa mặt & dưỡng da sáng lúc 6:30, Uống 2 lít nước lúc 8:00, Giãn cơ/yoga lúc 17:30, Skincare tối lúc 21:30"
        ),
        QuickTemplate(
            id = "moving_checklist",
            icon = "📦",
            title = "Chuyển nhà",
            subtitle = "Checklist đồ dùng khi chuyển nhà",
            category = TemplateCategory.PERSONAL,
            prompt = "Tạo checklist chuyển nhà: Đóng gói đồ phòng bếp (Cao), Đóng gói quần áo (Cao), Đồ điện tử & sạc (Cao), Giấy tờ quan trọng (Cao), Đồ phòng tắm (Vừa), Đổi địa chỉ đăng ký (Vừa), Lắp internet mới (Vừa), Dọn dẹp nhà cũ (Thấp)"
        ),

        // ══════════════════════════════════
        // ── Học tập (STUDY) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "study_plan",
            icon = "📚",
            title = "Kế hoạch học tập",
            subtitle = "Lịch học và ôn bài",
            category = TemplateCategory.STUDY,
            prompt = "Tạo kế hoạch học tập: Đọc tài liệu chương mới (Cao), Làm bài tập (Cao), Ôn bài cũ (Vừa), Ghi chú tóm tắt (Vừa), Luyện đề (Cao)"
        ),
        QuickTemplate(
            id = "exam_prep",
            icon = "✏",
            title = "Ôn thi",
            subtitle = "Checklist chuẩn bị thi",
            category = TemplateCategory.STUDY,
            prompt = "Tạo checklist ôn thi: Tổng hợp kiến thức trọng tâm (Cao), Làm đề thi thử (Cao), Ôn lại chỗ yếu (Vừa), Chuẩn bị dụng cụ thi (Thấp), Ngủ sớm trước ngày thi (Cao)"
        ),
        QuickTemplate(
            id = "language_learning",
            icon = "🌍",
            title = "Học ngoại ngữ",
            subtitle = "Lịch học ngôn ngữ hàng ngày",
            category = TemplateCategory.STUDY,
            prompt = "Tạo nhắc nhở học ngoại ngữ hàng ngày: Học từ vựng mới lúc 7:00, Nghe podcast/video lúc 12:30, Luyện nói/viết lúc 19:00, Flashcard ôn tập lúc 21:00"
        ),
        QuickTemplate(
            id = "reading_list",
            icon = "📖",
            title = "Danh sách đọc sách",
            subtitle = "Ghi chú sách cần đọc",
            category = TemplateCategory.STUDY,
            prompt = "Tạo ghi chú danh sách đọc sách với nội dung: Sách đang đọc: ..., Sách sắp đọc: ..., Sách đã đọc (note ngắn): ..., Mục tiêu: đọc 2 quyển/tháng"
        ),
        QuickTemplate(
            id = "course_notes",
            icon = "🎓",
            title = "Ghi chú khoá học",
            subtitle = "Template ghi chú bài giảng",
            category = TemplateCategory.STUDY,
            prompt = "Tạo ghi chú khoá học với nội dung: Tên khoá học, Giảng viên, Bài học hôm nay, Kiến thức chính, Câu hỏi cần tìm hiểu thêm, Bài tập về nhà"
        ),

        // ══════════════════════════════════
        // ── Sức khoẻ (HEALTH) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "health_daily",
            icon = "💪",
            title = "Sức khoẻ hàng ngày",
            subtitle = "Nhắc nhở chăm sóc sức khoẻ",
            category = TemplateCategory.HEALTH,
            prompt = "Tạo nhắc nhở sức khoẻ hàng ngày: Uống nước lúc 8:00, Tập thể dục lúc 17:00, Uống vitamin lúc 12:00, Đi ngủ sớm lúc 22:00"
        ),
        QuickTemplate(
            id = "workout_plan",
            icon = "🏃",
            title = "Lịch tập gym",
            subtitle = "Kế hoạch tập luyện trong tuần",
            category = TemplateCategory.HEALTH,
            prompt = "Tạo kế hoạch tập gym tuần: Thứ 2 - Chest & Triceps, Thứ 3 - Back & Biceps, Thứ 4 - Nghỉ, Thứ 5 - Legs & Shoulders, Thứ 6 - Cardio & Abs, Thứ 7 - Nghỉ"
        ),
        QuickTemplate(
            id = "medicine_reminder",
            icon = "💊",
            title = "Nhắc uống thuốc",
            subtitle = "Lịch uống thuốc đúng giờ",
            category = TemplateCategory.HEALTH,
            prompt = "Tạo nhắc nhở uống thuốc hàng ngày: Uống thuốc sáng lúc 7:00, Uống thuốc trưa lúc 12:00, Uống thuốc tối lúc 19:00"
        ),
        QuickTemplate(
            id = "meal_plan",
            icon = "🥗",
            title = "Thực đơn ăn kiêng",
            subtitle = "Kế hoạch ăn uống lành mạnh",
            category = TemplateCategory.HEALTH,
            prompt = "Tạo ghi chú thực đơn tuần với nội dung: Thứ 2: Bữa sáng - Yến mạch trái cây / Bữa trưa - Salad gà / Bữa tối - Cá hấp rau, Thứ 3: ..., Mục tiêu calo: 1800/ngày, Uống 2L nước/ngày"
        ),
        QuickTemplate(
            id = "mental_health",
            icon = "🧠",
            title = "Sức khoẻ tinh thần",
            subtitle = "Nhắc nhở mindfulness & thư giãn",
            category = TemplateCategory.HEALTH,
            prompt = "Tạo nhắc nhở sức khoẻ tinh thần hàng ngày: Thiền 10 phút lúc 6:30, Viết nhật ký biết ơn lúc 7:00, Giải lao 5 phút giữa giờ làm lúc 10:30, Hít thở sâu 3 phút lúc 15:00, Không dùng điện thoại 1h trước ngủ lúc 21:00"
        ),

        // ══════════════════════════════════
        // ── Du lịch (TRAVEL) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "travel_checklist",
            icon = "✈",
            title = "Chuẩn bị du lịch",
            subtitle = "Checklist trước chuyến đi",
            category = TemplateCategory.TRAVEL,
            prompt = "Tạo checklist chuẩn bị du lịch: Đặt vé máy bay (Cao), Đặt khách sạn (Cao), Chuẩn bị hành lý (Vừa), Đổi tiền (Vừa), Mua bảo hiểm du lịch (Vừa), Sạc đầy pin thiết bị (Thấp), In vé & giấy tờ (Cao)"
        ),
        QuickTemplate(
            id = "packing_list",
            icon = "🧳",
            title = "Danh sách hành lý",
            subtitle = "Đồ cần mang theo",
            category = TemplateCategory.TRAVEL,
            prompt = "Tạo checklist hành lý: Quần áo, Đồ vệ sinh cá nhân, Sạc điện thoại & adapter, Giấy tờ tuỳ thân, Thuốc men, Tiền mặt, Bản đồ/app du lịch"
        ),
        QuickTemplate(
            id = "travel_diary",
            icon = "📸",
            title = "Nhật ký du lịch",
            subtitle = "Ghi chú trải nghiệm chuyến đi",
            category = TemplateCategory.TRAVEL,
            prompt = "Tạo ghi chú nhật ký du lịch với nội dung: Ngày, Địa điểm, Trải nghiệm nổi bật, Món ăn đặc sắc, Chi phí, Ảnh/kỷ niệm"
        ),

        // ══════════════════════════════════
        // ── Sự kiện (EVENT) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "birthday_party",
            icon = "🎂",
            title = "Tổ chức sinh nhật",
            subtitle = "Checklist chuẩn bị tiệc",
            category = TemplateCategory.EVENT,
            prompt = "Tạo checklist sinh nhật: Đặt bánh sinh nhật (Cao), Mua đồ trang trí (Vừa), Gửi lời mời (Cao), Chuẩn bị quà (Vừa), Đặt nhà hàng/phòng (Cao), Chuẩn bị playlist nhạc (Thấp)"
        ),
        QuickTemplate(
            id = "wedding_prep",
            icon = "💒",
            title = "Chuẩn bị đám cưới",
            subtitle = "Checklist tổ chức lễ cưới",
            category = TemplateCategory.EVENT,
            prompt = "Tạo checklist đám cưới: Chọn ngày cưới tốt (Cao), Đặt nhà hàng (Cao), Chụp ảnh cưới (Cao), In thiệp mời (Vừa), Đặt xe hoa (Vừa), Thuê MC & ban nhạc (Vừa), Chuẩn bị tráp lễ (Cao)"
        ),
        QuickTemplate(
            id = "housewarming",
            icon = "🏡",
            title = "Tân gia / Nhập trạch",
            subtitle = "Checklist lễ tân gia",
            category = TemplateCategory.EVENT,
            prompt = "Tạo checklist tân gia: Chọn ngày tốt nhập trạch (Cao), Chuẩn bị lễ cúng (Cao), Mời gia đình & bạn bè (Vừa), Dọn dẹp & trang trí nhà (Cao), Đặt tiệc/nấu ăn (Vừa), Chuẩn bị phong bao (Thấp)"
        ),
        QuickTemplate(
            id = "new_year_prep",
            icon = "🎊",
            title = "Chuẩn bị Tết",
            subtitle = "Checklist trước Tết Nguyên Đán",
            category = TemplateCategory.EVENT,
            prompt = "Tạo checklist chuẩn bị Tết: Dọn dẹp nhà cửa (Cao), Mua hoa & cây cảnh (Vừa), Gói bánh chưng/bánh tét (Vừa), Mua sắm quần áo mới (Vừa), Sắm đồ lễ cúng ông Táo (Cao), Chuẩn bị lì xì (Vừa), Mua mứt & bánh kẹo (Thấp), Đi chúc Tết gia đình (Cao)"
        ),
        QuickTemplate(
            id = "death_anniversary",
            icon = "🕯",
            title = "Chuẩn bị giỗ",
            subtitle = "Checklist cúng giỗ gia tiên",
            category = TemplateCategory.EVENT,
            prompt = "Tạo checklist chuẩn bị giỗ: Mua hoa & nhang đèn (Cao), Chuẩn bị mâm cỗ (Cao), Mời họ hàng (Vừa), Dọn bàn thờ (Cao), Chuẩn bị trái cây (Vừa), Nấu các món truyền thống (Cao)"
        ),

        // ══════════════════════════════════
        // ── Tài chính (FINANCE) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "budget_monthly",
            icon = "💰",
            title = "Ngân sách tháng",
            subtitle = "Ghi chú quản lý chi tiêu",
            category = TemplateCategory.FINANCE,
            prompt = "Tạo ghi chú ngân sách tháng với nội dung: Thu nhập: ..., Chi phí cố định (nhà/điện/nước/internet): ..., Ăn uống: ..., Di chuyển: ..., Giải trí: ..., Tiết kiệm: ..., Dự phòng: ..."
        ),
        QuickTemplate(
            id = "bill_reminders",
            icon = "🧾",
            title = "Nhắc thanh toán hoá đơn",
            subtitle = "Nhắc nhở đóng tiền hàng tháng",
            category = TemplateCategory.FINANCE,
            prompt = "Tạo nhắc nhở thanh toán hàng tháng: Tiền nhà lúc 9:00, Tiền điện lúc 9:00, Tiền nước lúc 9:00, Tiền internet lúc 9:00, Tiền bảo hiểm lúc 9:00"
        ),
        QuickTemplate(
            id = "savings_goal",
            icon = "🎯",
            title = "Mục tiêu tiết kiệm",
            subtitle = "Theo dõi tiến độ tiết kiệm",
            category = TemplateCategory.FINANCE,
            prompt = "Tạo ghi chú mục tiêu tiết kiệm với nội dung: Mục tiêu: ..., Số tiền cần: ..., Thời hạn: ..., Tiết kiệm mỗi tháng: ..., Tiến độ hiện tại: 0%"
        ),

        // ══════════════════════════════════
        // ── Gia đình (PARENTING) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "kids_schedule",
            icon = "👶",
            title = "Lịch con nhỏ",
            subtitle = "Nhắc nhở chăm con hàng ngày",
            category = TemplateCategory.PARENTING,
            prompt = "Tạo nhắc nhở chăm con hàng ngày: Cho ăn sáng lúc 7:00, Đưa đi học lúc 7:30, Đón con lúc 16:30, Cho tắm lúc 18:00, Đọc truyện cho con lúc 20:00, Cho ngủ lúc 20:30"
        ),
        QuickTemplate(
            id = "school_prep",
            icon = "🎒",
            title = "Chuẩn bị đi học",
            subtitle = "Checklist cho con đi học",
            category = TemplateCategory.PARENTING,
            prompt = "Tạo checklist chuẩn bị đi học: Kiểm tra bài tập về nhà (Cao), Chuẩn bị cặp sách (Vừa), Đồ ăn trưa/snack (Vừa), Đồng phục/quần áo (Thấp), Bình nước (Thấp)"
        ),
        QuickTemplate(
            id = "family_activity",
            icon = "👨‍👩‍👧‍👦",
            title = "Hoạt động gia đình",
            subtitle = "Ý tưởng cuối tuần gia đình",
            category = TemplateCategory.PARENTING,
            prompt = "Tạo ghi chú ý tưởng hoạt động gia đình với nội dung: Đi công viên, Nấu ăn cùng nhau, Xem phim gia đình, Picnic, Đi bơi, Thăm ông bà, Chơi board game, Đi siêu thị cùng"
        ),

        // ══════════════════════════════════
        // ── Sáng tạo (CREATIVE) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "content_calendar",
            icon = "📱",
            title = "Lịch đăng bài",
            subtitle = "Content plan mạng xã hội",
            category = TemplateCategory.CREATIVE,
            prompt = "Tạo kế hoạch content tuần: Thứ 2 - Đăng bài chia sẻ kiến thức (Cao), Thứ 3 - Story/Reel ngắn (Vừa), Thứ 4 - Tương tác cộng đồng (Thấp), Thứ 5 - Đăng bài sản phẩm (Cao), Thứ 6 - Behind the scenes (Vừa), Thứ 7 - Tổng kết tuần (Thấp)"
        ),
        QuickTemplate(
            id = "blog_post",
            icon = "✍",
            title = "Viết bài blog",
            subtitle = "Template lên ý tưởng bài viết",
            category = TemplateCategory.CREATIVE,
            prompt = "Tạo ghi chú template bài blog với nội dung: Tiêu đề, Đối tượng đọc, Outline (3-5 mục), Từ khoá SEO, Tài liệu tham khảo, Deadline xuất bản"
        ),
        QuickTemplate(
            id = "video_production",
            icon = "🎬",
            title = "Sản xuất video",
            subtitle = "Checklist quay & edit video",
            category = TemplateCategory.CREATIVE,
            prompt = "Tạo checklist sản xuất video: Lên ý tưởng & kịch bản (Cao), Chuẩn bị thiết bị (Vừa), Quay hình (Cao), Edit & hậu kỳ (Cao), Thêm nhạc nền & phụ đề (Vừa), Upload & đăng bài (Vừa), Trả lời comment (Thấp)"
        ),

        // ══════════════════════════════════
        // ── Tâm linh (SPIRITUAL) ──
        // ══════════════════════════════════
        QuickTemplate(
            id = "full_moon_prayer",
            icon = "🌕",
            title = "Cúng Rằm",
            subtitle = "Checklist chuẩn bị cúng Rằm",
            category = TemplateCategory.SPIRITUAL,
            prompt = "Tạo checklist cúng Rằm: Mua hoa tươi (Cao), Mua trái cây 5 loại (Cao), Chuẩn bị nhang đèn (Vừa), Nấu cơm chay/mặn (Cao), Dọn bàn thờ sạch sẽ (Cao), Thắp nhang đúng giờ (Vừa)"
        ),
        QuickTemplate(
            id = "first_lunar",
            icon = "🌑",
            title = "Cúng Mùng 1",
            subtitle = "Checklist cúng đầu tháng",
            category = TemplateCategory.SPIRITUAL,
            prompt = "Tạo checklist cúng Mùng 1: Dọn bàn thờ (Cao), Thay nước & hoa mới (Vừa), Chuẩn bị lễ vật (Cao), Thắp nhang cầu bình an (Cao), Phóng sinh (Thấp)"
        ),
        QuickTemplate(
            id = "meditation_routine",
            icon = "🧘",
            title = "Thiền & tĩnh tâm",
            subtitle = "Nhắc nhở thiền hàng ngày",
            category = TemplateCategory.SPIRITUAL,
            prompt = "Tạo nhắc nhở thiền hàng ngày: Thiền buổi sáng 15 phút lúc 5:30, Hít thở sâu lúc 12:00, Thiền buổi tối 10 phút lúc 21:00"
        ),
    )

    fun getByCategory(category: TemplateCategory): List<QuickTemplate> {
        return quickTemplates.filter { it.category == category }
    }

    fun getById(id: String): QuickTemplate? {
        return quickTemplates.find { it.id == id }
    }
}
