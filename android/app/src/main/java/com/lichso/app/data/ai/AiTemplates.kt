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
        EVENT("Sự kiện")
    }

    val quickTemplates = listOf(
        // ── Công việc ──
        QuickTemplate(
            id = "daily_standup",
            icon = "☀",
            title = "Kế hoạch ngày",
            subtitle = "Tạo checklist công việc trong ngày",
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

        // ── Cá nhân ──
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

        // ── Học tập ──
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

        // ── Sức khoẻ ──
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

        // ── Du lịch ──
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

        // ── Sự kiện ──
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
    )

    fun getByCategory(category: TemplateCategory): List<QuickTemplate> {
        return quickTemplates.filter { it.category == category }
    }

    fun getById(id: String): QuickTemplate? {
        return quickTemplates.find { it.id == id }
    }
}
