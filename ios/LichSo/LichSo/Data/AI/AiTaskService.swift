import Foundation

// ═══════════════════════════════════════════
// AiTaskService — xử lý ngôn ngữ tự nhiên → thao tác CRUD trên ItemEntity.
// Port từ Android `AiTaskService.kt` (bản hành vi): gọi model qua
// OpenRouterService.complete với system prompt JSON, parse {action, items[], message}.
//
// Lưu ý: bản này LƯỢC BỎ phần tryQuickParse (regex tiếng Việt cục bộ) của Android để
// gọn — luôn đi qua AI. Có thể bổ sung offline-fallback sau.
// ═══════════════════════════════════════════

struct AiItem {
    var type: String = "task"          // task | note | reminder
    var title: String = ""
    var description: String = ""
    var priority: Int = 1
    var dueDate: String? = nil         // "yyyy-MM-dd"
    var time: String? = nil            // "HH:mm"
    var repeatType: Int = 0
    var colorIndex: Int = 0
}

struct AiActionResult {
    var action: String
    var items: [AiItem]
    var message: String
}

enum AiTaskService {
    private static let systemPrompt = """
    Bạn là trợ lý quản lý công việc thông minh trong app "Lịch Số".
    Nhiệm vụ: phân tích yêu cầu của người dùng và trả về JSON action.

    Quy tắc:
    1. LUÔN trả về JSON thuần (không markdown, không ```)
    2. Phân tích ý định: tạo/sửa/xoá task, note, reminder
    3. Nếu không rõ, trả action "suggest" kèm gợi ý
    4. Trích xuất: title, description, priority (0=Thấp, 1=Vừa, 2=Cao), dueDate (yyyy-MM-dd), time (HH:mm), repeatType (0=Một lần, 1=Hàng ngày, 2=Hàng tuần, 3=Hàng tháng)
    5. Cho message thân thiện bằng tiếng Việt

    Các action hợp lệ:
    - create_task: tạo công việc
    - create_note: tạo ghi chú
    - create_reminder: tạo nhắc nhở
    - edit_task: sửa task (mô tả "old_title:<tên cũ>", title=tên mới)
    - edit_note: sửa note
    - edit_task_priority: đổi mức ưu tiên task (title=tên, priority=mức mới)
    - delete_task / delete_note / delete_reminder: xoá (title=keyword cần khớp)
    - mark_done: đánh dấu việc đã xong (title=keyword)
    - bulk_create: tạo nhiều items cùng lúc
    - stats_all / stats_tasks / stats_notes / stats_reminders: thống kê
    - suggest: gợi ý khi không rõ ý định
    - general: trả lời chung

    Format JSON:
    {
      "action": "create_task",
      "items": [
        { "type": "task|note|reminder", "title": "...", "description": "...",
          "priority": 1, "dueDate": "2026-03-20", "time": "08:00",
          "repeatType": 0, "colorIndex": 0 }
      ],
      "message": "Đã tạo công việc ... cho bạn! ✨"
    }

    Ví dụ:
    - User: "nhắc tôi uống thuốc lúc 8h sáng hàng ngày"
      → {"action":"create_reminder","items":[{"type":"reminder","title":"Uống thuốc","time":"08:00","repeatType":1}],"message":"Đã tạo nhắc nhở uống thuốc lúc 8:00 sáng hàng ngày! 💊"}
    - User: "tạo checklist đi chợ: mua rau, mua thịt, mua trái cây"
      → {"action":"bulk_create","items":[{"type":"task","title":"Mua rau","priority":1},{"type":"task","title":"Mua thịt","priority":1},{"type":"task","title":"Mua trái cây","priority":1}],"message":"Đã tạo 3 việc trong checklist đi chợ! 🛒"}
    - User: "ghi chú: công thức nấu phở"
      → {"action":"create_note","items":[{"type":"note","title":"Công thức nấu phở","description":"","colorIndex":0}],"message":"Đã tạo ghi chú 'Công thức nấu phở'! 📝"}
    """

    /// Gửi câu lệnh tự nhiên → trả về action result đã parse.
    static func processCommand(_ userInput: String, existingContext: String = "") async -> AiActionResult {
        let contextMsg = existingContext.isEmpty ? "" : "Dữ liệu hiện tại của người dùng:\n\(existingContext)"
        let result = await OpenRouterService.shared.complete(
            systemPrompt: systemPrompt,
            userMessage: userInput,
            contextInfo: contextMsg
        )
        switch result {
        case .success(let response):
            return parse(response)
        case .failure:
            return AiActionResult(
                action: "general", items: [],
                message: "Không thể kết nối AI. Vui lòng thử lại hoặc dùng nút + để tạo nhanh."
            )
        }
    }

    // ── Parse JSON từ phản hồi model ──
    private static func parse(_ response: String) -> AiActionResult {
        let clean = response
            .replacingOccurrences(of: "```json", with: "")
            .replacingOccurrences(of: "```", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)

        guard let data = clean.data(using: .utf8),
              let obj = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            // Không phải JSON → coi như câu trả lời chung
            return AiActionResult(action: "general", items: [], message: clean.isEmpty ? "Đã xử lý yêu cầu!" : clean)
        }

        let action = obj["action"] as? String ?? "general"
        let message = obj["message"] as? String ?? "Đã xử lý yêu cầu!"
        var items: [AiItem] = []
        if let arr = obj["items"] as? [[String: Any]] {
            for o in arr {
                items.append(AiItem(
                    type: o["type"] as? String ?? "task",
                    title: o["title"] as? String ?? "",
                    description: o["description"] as? String ?? "",
                    priority: intValue(o["priority"]) ?? 1,
                    dueDate: o["dueDate"] as? String,
                    time: o["time"] as? String,
                    repeatType: intValue(o["repeatType"]) ?? 0,
                    colorIndex: intValue(o["colorIndex"]) ?? 0
                ))
            }
        }
        return AiActionResult(action: action, items: items, message: message)
    }

    private static func intValue(_ any: Any?) -> Int? {
        if let i = any as? Int { return i }
        if let d = any as? Double { return Int(d) }
        if let s = any as? String { return Int(s) }
        return nil
    }
}
