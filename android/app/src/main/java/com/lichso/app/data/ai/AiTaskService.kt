package com.lichso.app.data.ai

import com.lichso.app.data.remote.ChatMessage
import com.lichso.app.data.remote.OpenRouterApi
import com.google.gson.Gson
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Service xử lý ngôn ngữ tự nhiên → thao tác CRUD task/note/reminder.
 *
 * Trả về AiActionResult chứa:
 * - action: create_task, create_note, create_reminder, edit_task, edit_note, delete_task, bulk_create, suggest, general
 * - parsed data fields
 * - friendly message cho user
 */
@Singleton
class AiTaskService @Inject constructor(
    private val openRouterApi: OpenRouterApi
) {
    private val gson = Gson()

    companion object {
        private val SYSTEM_PROMPT = """
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
- edit_task: sửa task (cần id trong context)
- edit_note: sửa note (cần id trong context)
- delete_task: xoá task (cần id trong context)
- bulk_create: tạo nhiều items cùng lúc
- suggest: gợi ý khi không rõ ý định
- general: trả lời chung

Format JSON:
{
  "action": "create_task",
  "items": [
    {
      "type": "task|note|reminder",
      "title": "...",
      "description": "...",
      "priority": 1,
      "dueDate": "2026-03-20",
      "time": "08:00",
      "repeatType": 0,
      "colorIndex": 0
    }
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
""".trimIndent()
    }

    data class AiActionResult(
        val action: String,
        val items: List<AiItem>,
        val message: String,
        val rawJson: String? = null
    )

    data class AiItem(
        val type: String = "task",       // task, note, reminder
        val title: String = "",
        val description: String = "",
        val priority: Int = 1,
        val dueDate: String? = null,     // "yyyy-MM-dd"
        val time: String? = null,        // "HH:mm"
        val repeatType: Int = 0,
        val colorIndex: Int = 0
    )

    /**
     * Gửi prompt tự nhiên → AI parse → trả về action result
     * @param userInput Câu lệnh ngôn ngữ tự nhiên
     * @param existingContext Context hiện tại (VD: danh sách task đang có)
     */
    suspend fun processCommand(
        userInput: String,
        existingContext: String = ""
    ): AiActionResult {
        // Kiểm tra shortcut trước khi gọi AI (offline fallback)
        val quickResult = tryQuickParse(userInput)
        if (quickResult != null) return quickResult

        val contextMsg = if (existingContext.isNotBlank()) {
            "Dữ liệu hiện tại của người dùng:\n$existingContext"
        } else ""

        val result = openRouterApi.chat(
            userMessage = userInput,
            contextInfo = contextMsg,
            memoryContext = "",
            history = listOf(ChatMessage("system", SYSTEM_PROMPT))
        )

        return result.fold(
            onSuccess = { response -> parseAiResponse(response) },
            onFailure = { error ->
                // Fallback: thử parse offline
                tryQuickParse(userInput) ?: AiActionResult(
                    action = "general",
                    items = emptyList(),
                    message = "Không thể kết nối AI. Hãy thử dùng các nút bên dưới để tạo nhanh."
                )
            }
        )
    }

    private fun parseAiResponse(response: String): AiActionResult {
        return try {
            // Loại bỏ markdown code block nếu AI trả về
            val cleanJson = response
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            val jsonObj = JsonParser.parseString(cleanJson).asJsonObject
            val action = jsonObj.get("action")?.asString ?: "general"
            val message = jsonObj.get("message")?.asString ?: "Đã xử lý yêu cầu!"

            val items = mutableListOf<AiItem>()
            jsonObj.getAsJsonArray("items")?.forEach { element ->
                val obj = element.asJsonObject
                items.add(
                    AiItem(
                        type = obj.get("type")?.asString ?: "task",
                        title = obj.get("title")?.asString ?: "",
                        description = obj.get("description")?.asString ?: "",
                        priority = obj.get("priority")?.asInt ?: 1,
                        dueDate = obj.get("dueDate")?.asString,
                        time = obj.get("time")?.asString,
                        repeatType = obj.get("repeatType")?.asInt ?: 0,
                        colorIndex = obj.get("colorIndex")?.asInt ?: 0
                    )
                )
            }

            AiActionResult(action = action, items = items, message = message, rawJson = cleanJson)
        } catch (e: Exception) {
            // Nếu parse JSON fail, coi như general response
            AiActionResult(
                action = "general",
                items = emptyList(),
                message = response.take(300)
            )
        }
    }

    /**
     * Quick parse offline — không cần gọi AI cho các lệnh đơn giản
     */
    private fun tryQuickParse(input: String): AiActionResult? {
        val lower = input.lowercase().trim()

        // Pattern: "nhắc [tôi/mình] ... lúc HH:mm"
        val reminderPattern = Regex("nhắc\\s+(?:tôi|mình)?\\s*(.+?)\\s+(?:lúc|vào)\\s+(\\d{1,2})[h:]?(\\d{0,2})", RegexOption.IGNORE_CASE)
        reminderPattern.find(input)?.let { match ->
            val title = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val hour = match.groupValues[2]
            val min = match.groupValues[3].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            val isDaily = lower.contains("hàng ngày") || lower.contains("mỗi ngày") || lower.contains("daily")
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = if (isDaily) 1 else 0)),
                message = "Đã tạo nhắc nhở \"$title\" lúc $time${if (isDaily) " hàng ngày" else ""}! ⏰"
            )
        }

        // Pattern: "nhắc [tôi/mình] ... HHh" (without lúc/vào)
        val reminderShort = Regex("nhắc\\s+(?:tôi|mình)?\\s*(.+?)\\s+(\\d{1,2})\\s*[hg]\\s*(\\d{0,2})", RegexOption.IGNORE_CASE)
        reminderShort.find(input)?.let { match ->
            val title = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val hour = match.groupValues[2]
            val min = match.groupValues[3].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            val isDaily = lower.contains("hàng ngày") || lower.contains("mỗi ngày")
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = if (isDaily) 1 else 0)),
                message = "Đã tạo nhắc nhở \"$title\" lúc $time${if (isDaily) " hàng ngày" else ""}! ⏰"
            )
        }

        // Pattern: "nhắc [tôi/mình] ..." (no time — use default 08:00)
        val reminderNoTime = Regex("^nhắc\\s+(?:tôi|mình)?\\s+(.{3,})$", RegexOption.IGNORE_CASE)
        if (!lower.contains("lúc") && !Regex("\\d+\\s*[hg:]").containsMatchIn(lower)) {
            reminderNoTime.find(input)?.let { match ->
                val title = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
                return AiActionResult(
                    action = "create_reminder",
                    items = listOf(AiItem(type = "reminder", title = title, time = "08:00", repeatType = 0)),
                    message = "Đã tạo nhắc nhở \"$title\" lúc 08:00! ⏰"
                )
            }
        }

        // Pattern: "ghi chú: ..." or "note: ..."
        val notePattern = Regex("(?:ghi chú|note|ghi lại|ghi nhớ)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        notePattern.find(input)?.let { match ->
            val content = match.groupValues[1].trim()
            val title = content.take(50).let { if (it.length < content.length) "$it..." else it }
            return AiActionResult(
                action = "create_note",
                items = listOf(AiItem(type = "note", title = title, description = content)),
                message = "Đã tạo ghi chú \"$title\"! 📝"
            )
        }

        // Pattern: "tạo task: ..." or "thêm việc: ..."
        val taskPattern = Regex("(?:tạo|thêm|add)\\s+(?:task|việc|công việc)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        taskPattern.find(input)?.let { match ->
            val title = match.groupValues[1].trim()
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = 1)),
                message = "Đã tạo công việc \"$title\"! ✅"
            )
        }

        // Pattern: "tạo checklist ..." or "checklist: ..."  — split by commas
        val checklistPattern = Regex("(?:tạo\\s+)?(?:checklist|danh sách)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        checklistPattern.find(input)?.let { match ->
            val raw = match.groupValues[1].trim()
            // Tách theo dấu phẩy hoặc dấu xuống dòng
            val items = raw.split(Regex("[,،;\\n]+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (items.isNotEmpty()) {
                val taskItems = items.map { item ->
                    AiItem(type = "task", title = item.replaceFirstChar { it.uppercase() }, priority = 1)
                }
                return AiActionResult(
                    action = "bulk_create",
                    items = taskItems,
                    message = "Đã tạo ${taskItems.size} việc trong checklist! ✅"
                )
            }
        }

        // Pattern: "kế hoạch ngày" / "kế hoạch hôm nay" — predefined daily plan
        if (lower.contains("kế hoạch ngày") || lower.contains("kế hoạch hôm nay") || lower.contains("plan ngày")) {
            val dailyTasks = listOf(
                AiItem(type = "task", title = "Kiểm tra email & tin nhắn", priority = 1),
                AiItem(type = "task", title = "Hoàn thành task quan trọng nhất", priority = 2),
                AiItem(type = "task", title = "Họp/trao đổi công việc", priority = 1),
                AiItem(type = "task", title = "Review & cập nhật tiến độ", priority = 1),
                AiItem(type = "task", title = "Lên kế hoạch cho ngày mai", priority = 0),
            )
            return AiActionResult(
                action = "bulk_create",
                items = dailyTasks,
                message = "Đã tạo kế hoạch ngày với 5 công việc! 📋"
            )
        }

        // Pattern: simple "tạo ..." — catch-all for task creation
        val simpleCreate = Regex("^(?:tạo|thêm|add|làm)\\s+(.{3,})$", RegexOption.IGNORE_CASE)
        simpleCreate.find(input)?.let { match ->
            val title = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            // Detect priority from keywords
            val priority = when {
                lower.contains("quan trọng") || lower.contains("gấp") || lower.contains("khẩn") -> 2
                lower.contains("thấp") || lower.contains("nhẹ") -> 0
                else -> 1
            }
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = priority)),
                message = "Đã tạo công việc \"$title\"! ✅"
            )
        }

        return null // Cần gọi AI
    }
}
