package com.lichso.app.data.ai

import com.lichso.app.data.remote.ChatMessage
import com.lichso.app.data.remote.OpenRouterApi
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
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
- stats_all: thống kê tổng hợp tất cả task/note/reminder
- stats_tasks: thống kê chỉ task
- stats_notes: thống kê chỉ note
- stats_reminders: thống kê chỉ reminder
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

    // ════════════════════════════════════════════════════════════════
    // OFFLINE SMART PARSER — Xử lý thông minh không cần internet
    // ════════════════════════════════════════════════════════════════

    /**
     * Quick parse offline — không cần gọi AI cho các lệnh phổ biến.
     * Bao gồm: tạo/sửa/xoá task, note, reminder, xem ngày, đổi ngày, checklist, kế hoạch...
     */
    private fun tryQuickParse(input: String): AiActionResult? {
        val lower = input.lowercase().trim()

        // ── 0. THỐNG KÊ (statistics) ─────────────────────────
        tryParseStats(lower)?.let { return it }

        // ── 1. XOÁ (delete) ──────────────────────────────────
        tryParseDelete(input, lower)?.let { return it }

        // ── 2. SỬA (edit/update) ─────────────────────────────
        tryParseEdit(input, lower)?.let { return it }

        // ── 3. ĐÁNH DẤU HOÀN THÀNH ──────────────────────────
        tryParseMarkDone(input, lower)?.let { return it }

        // ── 4. LIỆT KÊ (list/xem) ───────────────────────────
        tryParseList(lower)?.let { return it }

        // ── 5. TẠO NHẮC NHỞ (reminder) ──────────────────────
        tryParseReminder(input, lower)?.let { return it }

        // ── 6. TẠO GHI CHÚ (note) ───────────────────────────
        tryParseNote(input, lower)?.let { return it }

        // ── 7. TẠO TASK ─────────────────────────────────────
        tryParseTask(input, lower)?.let { return it }

        // ── 8. CHECKLIST / BULK ──────────────────────────────
        tryParseChecklist(input, lower)?.let { return it }

        // ── 9. KẾ HOẠCH MẪU ─────────────────────────────────
        tryParsePlanTemplates(lower)?.let { return it }

        // ── 10. XEM NGÀY / ĐỔI NGÀY ─────────────────────────
        tryParseDateQuery(input, lower)?.let { return it }

        // ── 11. CATCH-ALL TẠO ────────────────────────────────
        tryParseSimpleCreate(input, lower)?.let { return it }

        return null // Cần gọi AI
    }

    // ────────────────────────────────────────────────────────
    // 0. THỐNG KÊ (Statistics/Summary)
    // ────────────────────────────────────────────────────────

    private fun tryParseStats(lower: String): AiActionResult? {
        val statsKeywords = listOf(
            "thống kê", "thong ke", "statistics", "tổng hợp", "tong hop",
            "báo cáo", "bao cao", "report", "tổng kết", "tong ket",
            "summary", "bao nhiêu task", "bao nhiêu ghi chú", "bao nhiêu nhắc",
            "đếm", "count", "tình trạng", "tinh trang", "trạng thái",
            "overview", "tổng quan", "tong quan"
        )

        val isStats = statsKeywords.any { lower.contains(it) } ||
                lower.matches(Regex(".*(?:có bao nhiêu|mấy cái|tổng cộng|tất cả|số lượng).*(?:task|note|nhắc|ghi chú|việc|công việc|reminder).*")) ||
                lower.matches(Regex(".*(?:task|note|nhắc|ghi chú|việc|công việc|reminder).*(?:có bao nhiêu|mấy cái|tổng cộng|tất cả|số lượng).*"))

        if (!isStats) return null

        // Determine what type of stats they want
        val wantTask = lower.contains("task") || lower.contains("việc") || lower.contains("công việc") || lower.contains("todo")
        val wantNote = lower.contains("note") || lower.contains("ghi chú")
        val wantReminder = lower.contains("nhắc") || lower.contains("reminder") || lower.contains("alarm") || lower.contains("báo thức")

        val statsType = when {
            wantTask && !wantNote && !wantReminder -> "stats_tasks"
            wantNote && !wantTask && !wantReminder -> "stats_notes"
            wantReminder && !wantTask && !wantNote -> "stats_reminders"
            else -> "stats_all"
        }

        return AiActionResult(
            action = statsType,
            items = emptyList(),
            message = "" // ChatViewModel will build the real message from actual data
        )
    }

    // ────────────────────────────────────────────────────────
    // 1. XOÁ
    // ────────────────────────────────────────────────────────

    private fun tryParseDelete(input: String, lower: String): AiActionResult? {
        // "xoá task/việc ..." 
        val deleteTaskP = Regex("(?:xoá|xóa|xoa|delete|remove|bỏ)\\s+(?:task|việc|công việc)\\s*[:\\s]*(.+)", RegexOption.IGNORE_CASE)
        deleteTaskP.find(input)?.let { match ->
            val keyword = match.groupValues[1].trim()
            return AiActionResult(
                action = "delete_task",
                items = listOf(AiItem(type = "task", title = keyword)),
                message = "Đã yêu cầu xoá task \"$keyword\". Tìm và xoá task có tiêu đề phù hợp. 🗑️"
            )
        }

        // "xoá note/ghi chú ..."
        val deleteNoteP = Regex("(?:xoá|xóa|xoa|delete|remove|bỏ)\\s+(?:note|ghi chú|ghi chu)\\s*[:\\s]*(.+)", RegexOption.IGNORE_CASE)
        deleteNoteP.find(input)?.let { match ->
            val keyword = match.groupValues[1].trim()
            return AiActionResult(
                action = "delete_note",
                items = listOf(AiItem(type = "note", title = keyword)),
                message = "Đã yêu cầu xoá ghi chú \"$keyword\". 🗑️"
            )
        }

        // "xoá nhắc nhở/reminder ..."
        val deleteReminderP = Regex("(?:xoá|xóa|xoa|delete|remove|bỏ|tắt)\\s+(?:nhắc nhở|nhắc|reminder|alarm)\\s*[:\\s]*(.+)", RegexOption.IGNORE_CASE)
        deleteReminderP.find(input)?.let { match ->
            val keyword = match.groupValues[1].trim()
            return AiActionResult(
                action = "delete_reminder",
                items = listOf(AiItem(type = "reminder", title = keyword)),
                message = "Đã yêu cầu xoá nhắc nhở \"$keyword\". 🗑️"
            )
        }

        // Generic "xoá tất cả task/note/reminder"
        if (lower.matches(Regex(".*(?:xoá|xóa|xoa)\\s+(?:tất cả|hết|all)\\s+(?:task|việc|công việc).*"))) {
            return AiActionResult(
                action = "delete_all_tasks",
                items = emptyList(),
                message = "⚠️ Bạn muốn xoá tất cả task? Hãy vào tab \"Ghi chú & Việc làm\" và xoá thủ công để tránh nhầm lẫn."
            )
        }
        if (lower.matches(Regex(".*(?:xoá|xóa|xoa)\\s+(?:tất cả|hết|all)\\s+(?:note|ghi chú).*"))) {
            return AiActionResult(
                action = "delete_all_notes",
                items = emptyList(),
                message = "⚠️ Bạn muốn xoá tất cả ghi chú? Hãy vào tab \"Ghi chú & Việc làm\" và xoá thủ công để tránh nhầm lẫn."
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 2. SỬA
    // ────────────────────────────────────────────────────────

    private fun tryParseEdit(input: String, lower: String): AiActionResult? {
        // "sửa task/việc ... thành ..."
        val editTaskP = Regex("(?:sửa|edit|update|đổi|cập nhật|rename)\\s+(?:task|việc|công việc)\\s+[\"']?(.+?)[\"']?\\s+(?:thành|thành|sang|→|->)\\s+[\"']?(.+?)[\"']?\\s*$", RegexOption.IGNORE_CASE)
        editTaskP.find(input)?.let { match ->
            val oldTitle = match.groupValues[1].trim()
            val newTitle = match.groupValues[2].trim()
            return AiActionResult(
                action = "edit_task",
                items = listOf(AiItem(type = "task", title = newTitle, description = "old_title:$oldTitle")),
                message = "Đã yêu cầu đổi task \"$oldTitle\" → \"$newTitle\". ✏️"
            )
        }

        // "sửa note/ghi chú ... thành ..."
        val editNoteP = Regex("(?:sửa|edit|update|đổi|cập nhật|rename)\\s+(?:note|ghi chú|ghi chu)\\s+[\"']?(.+?)[\"']?\\s+(?:thành|sang|→|->)\\s+[\"']?(.+?)[\"']?\\s*$", RegexOption.IGNORE_CASE)
        editNoteP.find(input)?.let { match ->
            val oldTitle = match.groupValues[1].trim()
            val newTitle = match.groupValues[2].trim()
            return AiActionResult(
                action = "edit_note",
                items = listOf(AiItem(type = "note", title = newTitle, description = "old_title:$oldTitle")),
                message = "Đã yêu cầu đổi ghi chú \"$oldTitle\" → \"$newTitle\". ✏️"
            )
        }

        // "đổi priority/ưu tiên task ... thành cao/vừa/thấp"
        val editPriorityP = Regex("(?:đổi|sửa|set)\\s+(?:priority|ưu tiên|mức)\\s+(?:task|việc)?\\s*[\"']?(.+?)[\"']?\\s+(?:thành|sang|→|->|=)\\s+(cao|vừa|thấp|high|medium|low)", RegexOption.IGNORE_CASE)
        editPriorityP.find(input)?.let { match ->
            val taskTitle = match.groupValues[1].trim()
            val priorityStr = match.groupValues[2].lowercase()
            val priority = when {
                priorityStr.contains("cao") || priorityStr.contains("high") -> 2
                priorityStr.contains("thấp") || priorityStr.contains("low") -> 0
                else -> 1
            }
            return AiActionResult(
                action = "edit_task_priority",
                items = listOf(AiItem(type = "task", title = taskTitle, priority = priority)),
                message = "Đã yêu cầu đổi ưu tiên task \"$taskTitle\" → ${if (priority == 2) "Cao" else if (priority == 0) "Thấp" else "Vừa"}. ✏️"
            )
        }

        // "đổi thời gian nhắc nhở ... thành HH:mm"
        val editReminderTimeP = Regex("(?:đổi|sửa|chuyển)\\s+(?:thời gian|giờ|time)\\s+(?:nhắc nhở|nhắc|reminder)\\s*[\"']?(.+?)[\"']?\\s+(?:thành|sang|→|->|lúc)\\s+(\\d{1,2})[h:](\\d{0,2})", RegexOption.IGNORE_CASE)
        editReminderTimeP.find(input)?.let { match ->
            val title = match.groupValues[1].trim()
            val hour = match.groupValues[2]
            val min = match.groupValues[3].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            return AiActionResult(
                action = "edit_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time)),
                message = "Đã yêu cầu đổi giờ nhắc nhở \"$title\" → $time. ✏️"
            )
        }

        // Generic "sửa/edit ..." (without specific type → suggest)
        val genericEdit = Regex("^(?:sửa|edit|update|cập nhật)\\s+(.{3,})$", RegexOption.IGNORE_CASE)
        if (!lower.contains("thành") && !lower.contains("sang")) {
            genericEdit.find(input)?.let { match ->
                val keyword = match.groupValues[1].trim()
                return AiActionResult(
                    action = "suggest",
                    items = emptyList(),
                    message = "Bạn muốn sửa \"$keyword\"?\n\n" +
                            "› Sửa task: \"sửa task $keyword thành ...\"\n" +
                            "› Sửa note: \"sửa note $keyword thành ...\"\n" +
                            "› Sửa nhắc nhở: \"đổi giờ nhắc nhở $keyword lúc 9h\"\n\n" +
                            "Hoặc vào tab \"Ghi chú & Việc làm\" để sửa trực tiếp. ✏️"
                )
            }
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 3. ĐÁNH DẤU HOÀN THÀNH
    // ────────────────────────────────────────────────────────

    private fun tryParseMarkDone(input: String, lower: String): AiActionResult? {
        // "xong/hoàn thành/done task ..."
        val doneP = Regex("(?:xong|hoàn thành|done|finish|đã xong|đã làm|completed)\\s+(?:task|việc|công việc)?\\s*[:\\s]*[\"']?(.+?)[\"']?\\s*$", RegexOption.IGNORE_CASE)
        doneP.find(input)?.let { match ->
            val keyword = match.groupValues[1].trim()
            return AiActionResult(
                action = "mark_done",
                items = listOf(AiItem(type = "task", title = keyword)),
                message = "Đã yêu cầu đánh dấu hoàn thành \"$keyword\". ✅"
            )
        }

        // "đánh dấu ... hoàn thành"
        val markP = Regex("(?:đánh dấu|mark)\\s+[\"']?(.+?)[\"']?\\s+(?:hoàn thành|xong|done|completed)", RegexOption.IGNORE_CASE)
        markP.find(input)?.let { match ->
            val keyword = match.groupValues[1].trim()
            return AiActionResult(
                action = "mark_done",
                items = listOf(AiItem(type = "task", title = keyword)),
                message = "Đã yêu cầu đánh dấu hoàn thành \"$keyword\". ✅"
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 4. LIỆT KÊ / XEM
    // ────────────────────────────────────────────────────────

    private fun tryParseList(lower: String): AiActionResult? {
        // "xem task / liệt kê việc / list tasks"
        if (lower.matches(Regex(".*(?:xem|liệt kê|list|hiển thị|show|danh sách)\\s+(?:task|việc|công việc|todo).*"))) {
            return AiActionResult(
                action = "list_tasks",
                items = emptyList(),
                message = "📋 Hãy chuyển sang tab \"Ghi chú & Việc làm\" để xem danh sách task đầy đủ."
            )
        }

        // "xem note / liệt kê ghi chú"
        if (lower.matches(Regex(".*(?:xem|liệt kê|list|hiển thị|show|danh sách)\\s+(?:note|ghi chú|ghi chu).*"))) {
            return AiActionResult(
                action = "list_notes",
                items = emptyList(),
                message = "📝 Hãy chuyển sang tab \"Ghi chú & Việc làm\" để xem danh sách ghi chú đầy đủ."
            )
        }

        // "xem nhắc nhở / list reminders"
        if (lower.matches(Regex(".*(?:xem|liệt kê|list|hiển thị|show|danh sách)\\s+(?:nhắc nhở|nhắc|reminder|alarm).*"))) {
            return AiActionResult(
                action = "list_reminders",
                items = emptyList(),
                message = "⏰ Hãy chuyển sang tab \"Ghi chú & Việc làm\" để xem danh sách nhắc nhở đầy đủ."
            )
        }

        // "tôi có bao nhiêu task/việc"
        if (lower.matches(Regex(".*(?:bao nhiêu|mấy|count|số lượng)\\s+(?:task|việc|công việc).*"))) {
            return AiActionResult(
                action = "count_tasks",
                items = emptyList(),
                message = "📊 Hãy chuyển sang tab \"Ghi chú & Việc làm\" để xem số lượng task hiện tại."
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 5. TẠO NHẮC NHỞ
    // ────────────────────────────────────────────────────────

    private fun tryParseReminder(input: String, lower: String): AiActionResult? {
        val isDaily = lower.contains("hàng ngày") || lower.contains("mỗi ngày") || lower.contains("daily")
        val isWeekly = lower.contains("hàng tuần") || lower.contains("mỗi tuần") || lower.contains("weekly")
        val isMonthly = lower.contains("hàng tháng") || lower.contains("mỗi tháng") || lower.contains("monthly")
        val repeatType = when {
            isDaily -> 1
            isWeekly -> 2
            isMonthly -> 3
            else -> 0
        }
        val repeatLabel = when (repeatType) {
            1 -> " hàng ngày"
            2 -> " hàng tuần"
            3 -> " hàng tháng"
            else -> ""
        }

        // "nhắc [tôi/mình] ... lúc/vào HH:mm"
        val reminderPattern = Regex("nhắc\\s+(?:tôi|mình)?\\s*(.+?)\\s+(?:lúc|vào)\\s+(\\d{1,2})[h:]?(\\d{0,2})", RegexOption.IGNORE_CASE)
        reminderPattern.find(input)?.let { match ->
            val title = cleanReminderTitle(match.groupValues[1].trim())
            val hour = match.groupValues[2]
            val min = match.groupValues[3].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = repeatType)),
                message = "Đã tạo nhắc nhở \"$title\" lúc $time$repeatLabel! ⏰"
            )
        }

        // "nhắc [tôi/mình] ... HHh[mm]"
        val reminderShort = Regex("nhắc\\s+(?:tôi|mình)?\\s*(.+?)\\s+(\\d{1,2})\\s*[hg]\\s*(\\d{0,2})", RegexOption.IGNORE_CASE)
        reminderShort.find(input)?.let { match ->
            val title = cleanReminderTitle(match.groupValues[1].trim())
            val hour = match.groupValues[2]
            val min = match.groupValues[3].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = repeatType)),
                message = "Đã tạo nhắc nhở \"$title\" lúc $time$repeatLabel! ⏰"
            )
        }

        // "nhắc ... sáng/chiều/tối" (without specific time)
        val reminderPeriod = Regex("nhắc\\s+(?:tôi|mình)?\\s*(.+?)\\s+(sáng sớm|sáng|trưa|chiều|tối|khuya)", RegexOption.IGNORE_CASE)
        reminderPeriod.find(input)?.let { match ->
            val title = cleanReminderTitle(match.groupValues[1].trim())
            val period = match.groupValues[2].lowercase()
            val time = when {
                period.contains("sớm") -> "06:00"
                period.contains("sáng") -> "08:00"
                period.contains("trưa") -> "12:00"
                period.contains("chiều") -> "14:00"
                period.contains("tối") -> "19:00"
                period.contains("khuya") -> "22:00"
                else -> "08:00"
            }
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = repeatType)),
                message = "Đã tạo nhắc nhở \"$title\" lúc $time ($period)$repeatLabel! ⏰"
            )
        }

        // "nhắc [tôi/mình] ..." (no time — use default 08:00)
        val reminderNoTime = Regex("^nhắc\\s+(?:tôi|mình)?\\s+(.{3,})$", RegexOption.IGNORE_CASE)
        if (!lower.contains("lúc") && !lower.contains("vào") && !Regex("\\d+\\s*[hg:]").containsMatchIn(lower)
            && !lower.contains("sáng") && !lower.contains("chiều") && !lower.contains("tối") && !lower.contains("trưa")) {
            reminderNoTime.find(input)?.let { match ->
                val rawTitle = match.groupValues[1].trim()
                val title = cleanReminderTitle(rawTitle)
                return AiActionResult(
                    action = "create_reminder",
                    items = listOf(AiItem(type = "reminder", title = title, time = "08:00", repeatType = repeatType)),
                    message = "Đã tạo nhắc nhở \"$title\" lúc 08:00$repeatLabel! ⏰"
                )
            }
        }

        // "đặt alarm/báo thức lúc HH:mm"
        val alarmP = Regex("(?:đặt|set)\\s+(?:alarm|báo thức|chuông)\\s*(?:lúc|vào)?\\s*(\\d{1,2})[h:](\\d{0,2})\\s*(.*)", RegexOption.IGNORE_CASE)
        alarmP.find(input)?.let { match ->
            val hour = match.groupValues[1]
            val min = match.groupValues[2].ifBlank { "00" }
            val time = "${hour.padStart(2, '0')}:${min.padStart(2, '0')}"
            val title = match.groupValues[3].trim().ifBlank { "Báo thức" }
            return AiActionResult(
                action = "create_reminder",
                items = listOf(AiItem(type = "reminder", title = title, time = time, repeatType = repeatType)),
                message = "Đã đặt báo thức lúc $time$repeatLabel! ⏰"
            )
        }

        return null
    }

    /** Loại bỏ repeat keywords khỏi title nhắc nhở */
    private fun cleanReminderTitle(title: String): String {
        return title
            .replace(Regex("\\s*(hàng ngày|mỗi ngày|hàng tuần|mỗi tuần|hàng tháng|mỗi tháng|daily|weekly|monthly)\\s*", RegexOption.IGNORE_CASE), " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
    }

    // ────────────────────────────────────────────────────────
    // 6. TẠO GHI CHÚ
    // ────────────────────────────────────────────────────────

    private fun tryParseNote(input: String, lower: String): AiActionResult? {
        // "ghi chú: ..." / "note: ..." / "ghi lại: ..." / "ghi nhớ: ..."
        val notePattern = Regex("(?:ghi chú|note|ghi lại|ghi nhớ|memo|viết|jot)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        notePattern.find(input)?.let { match ->
            val content = match.groupValues[1].trim()
            val title = content.take(50).let { if (it.length < content.length) "$it..." else it }
            return AiActionResult(
                action = "create_note",
                items = listOf(AiItem(type = "note", title = title, description = content)),
                message = "Đã tạo ghi chú \"$title\"! 📝"
            )
        }

        // "tạo note/ghi chú: ..."
        val createNoteP = Regex("(?:tạo|thêm|add)\\s+(?:note|ghi chú|ghi chu)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        createNoteP.find(input)?.let { match ->
            val content = match.groupValues[1].trim()
            val title = content.take(50).let { if (it.length < content.length) "$it..." else it }
            return AiActionResult(
                action = "create_note",
                items = listOf(AiItem(type = "note", title = title, description = content)),
                message = "Đã tạo ghi chú \"$title\"! 📝"
            )
        }

        // "tạo note/ghi chú tiêu đề: ... nội dung: ..."
        val noteWithBody = Regex("(?:tạo|thêm)?\\s*(?:note|ghi chú)\\s*(?:tiêu đề|title)?[:\\s]+(.+?)\\s+(?:nội dung|content|body)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        noteWithBody.find(input)?.let { match ->
            val title = match.groupValues[1].trim()
            val body = match.groupValues[2].trim()
            return AiActionResult(
                action = "create_note",
                items = listOf(AiItem(type = "note", title = title, description = body)),
                message = "Đã tạo ghi chú \"$title\"! 📝"
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 7. TẠO TASK
    // ────────────────────────────────────────────────────────

    private fun tryParseTask(input: String, lower: String): AiActionResult? {
        // Detect priority
        val priority = when {
            lower.contains("quan trọng") || lower.contains("gấp") || lower.contains("khẩn") || lower.contains("urgent") || lower.contains("high") -> 2
            lower.contains("thấp") || lower.contains("nhẹ") || lower.contains("low") -> 0
            else -> 1
        }

        // Detect due date
        val dueDate = parseDateFromText(lower)

        // "tạo task: ..." / "thêm việc: ..." / "add task: ..."
        val taskPattern = Regex("(?:tạo|thêm|add)\\s+(?:task|việc|công việc)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        taskPattern.find(input)?.let { match ->
            val rawTitle = match.groupValues[1].trim()
            val title = cleanDateKeywords(rawTitle)
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = priority, dueDate = dueDate)),
                message = "Đã tạo công việc \"$title\"${if (dueDate != null) " (hạn: $dueDate)" else ""}! ✅"
            )
        }

        // "todo: ..."
        val todoPattern = Regex("(?:todo|to-do|to do)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        todoPattern.find(input)?.let { match ->
            val title = cleanDateKeywords(match.groupValues[1].trim())
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = priority, dueDate = dueDate)),
                message = "Đã tạo task \"$title\"! ✅"
            )
        }

        // "cần làm ..." / "phải làm ..."
        val needDoP = Regex("^(?:cần|phải|nên)\\s+(?:làm|hoàn thành|xong)\\s+(.{3,})", RegexOption.IGNORE_CASE)
        needDoP.find(input)?.let { match ->
            val title = cleanDateKeywords(match.groupValues[1].trim()).replaceFirstChar { it.uppercase() }
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = priority, dueDate = dueDate)),
                message = "Đã tạo task \"$title\"! ✅"
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 8. CHECKLIST / BULK
    // ────────────────────────────────────────────────────────

    private fun tryParseChecklist(input: String, lower: String): AiActionResult? {
        // "checklist: a, b, c" / "danh sách: ..."
        val checklistPattern = Regex("(?:tạo\\s+)?(?:checklist|danh sách|list)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        checklistPattern.find(input)?.let { match ->
            val raw = match.groupValues[1].trim()
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

        // "mua ... , ... , ..." (shopping list pattern)
        val shoppingP = Regex("(?:đi )?(?:mua|shopping)[:\\s]+(.+)", RegexOption.IGNORE_CASE)
        shoppingP.find(input)?.let { match ->
            val raw = match.groupValues[1].trim()
            val items = raw.split(Regex("[,،;\\n]+|\\s+và\\s+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (items.size > 1) {
                val taskItems = items.map { item ->
                    AiItem(type = "task", title = "Mua ${item.lowercase()}", priority = 1)
                }
                return AiActionResult(
                    action = "bulk_create",
                    items = taskItems,
                    message = "Đã tạo danh sách mua sắm ${taskItems.size} món! 🛒"
                )
            }
        }

        // Multi-line input with "- " or "• " bullets
        if (input.contains("\n") && (input.contains("- ") || input.contains("• "))) {
            val items = input.lines()
                .map { it.trimStart('-', '•', '✓', '☐', ' ', '\t') }
                .filter { it.isNotBlank() && it.length >= 2 }
            if (items.size >= 2) {
                val taskItems = items.map { item ->
                    AiItem(type = "task", title = item.replaceFirstChar { it.uppercase() }, priority = 1)
                }
                return AiActionResult(
                    action = "bulk_create",
                    items = taskItems,
                    message = "Đã tạo ${taskItems.size} task từ danh sách! ✅"
                )
            }
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 9. KẾ HOẠCH MẪU
    // ────────────────────────────────────────────────────────

    private fun tryParsePlanTemplates(lower: String): AiActionResult? {
        // "kế hoạch ngày" / "kế hoạch hôm nay"
        if (lower.contains("kế hoạch ngày") || lower.contains("kế hoạch hôm nay") || lower.contains("plan ngày") || lower.contains("daily plan")) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Kiểm tra email & tin nhắn", priority = 1),
                    AiItem(type = "task", title = "Hoàn thành task quan trọng nhất", priority = 2),
                    AiItem(type = "task", title = "Họp/trao đổi công việc", priority = 1),
                    AiItem(type = "task", title = "Review & cập nhật tiến độ", priority = 1),
                    AiItem(type = "task", title = "Lên kế hoạch cho ngày mai", priority = 0),
                ),
                message = "Đã tạo kế hoạch ngày với 5 công việc! 📋"
            )
        }

        // "kế hoạch tuần"
        if (lower.contains("kế hoạch tuần") || lower.contains("weekly plan") || lower.contains("plan tuần")) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Lên mục tiêu tuần", priority = 2),
                    AiItem(type = "task", title = "Review kết quả tuần trước", priority = 1),
                    AiItem(type = "task", title = "Họp đầu tuần", priority = 1),
                    AiItem(type = "task", title = "Hoàn thành 3 task ưu tiên", priority = 2),
                    AiItem(type = "task", title = "Dọn dẹp & sắp xếp workspace", priority = 0),
                    AiItem(type = "task", title = "Học/đọc sách 30 phút", priority = 0),
                    AiItem(type = "task", title = "Tổng kết & báo cáo cuối tuần", priority = 1),
                ),
                message = "Đã tạo kế hoạch tuần với 7 công việc! 📋"
            )
        }

        // "kế hoạch tập thể dục / gym / workout"
        if (lower.matches(Regex(".*(?:kế hoạch|plan|lịch)\\s+(?:tập|gym|workout|thể dục|exercise).*"))) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Khởi động 10 phút", priority = 1),
                    AiItem(type = "task", title = "Cardio 20 phút", priority = 2),
                    AiItem(type = "task", title = "Bài tập sức mạnh", priority = 2),
                    AiItem(type = "task", title = "Stretching & giãn cơ", priority = 1),
                    AiItem(type = "reminder", title = "Uống đủ nước", time = "07:00", repeatType = 1),
                ),
                message = "Đã tạo kế hoạch tập luyện! 💪"
            )
        }

        // "kế hoạch du lịch / trip"
        if (lower.matches(Regex(".*(?:kế hoạch|plan|lịch|chuẩn bị)\\s+(?:du lịch|trip|đi chơi|đi du lịch|vacation).*"))) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Đặt vé máy bay/xe", priority = 2),
                    AiItem(type = "task", title = "Đặt khách sạn/homestay", priority = 2),
                    AiItem(type = "task", title = "Lên lịch trình chi tiết", priority = 1),
                    AiItem(type = "task", title = "Chuẩn bị hành lý", priority = 1),
                    AiItem(type = "task", title = "Đổi tiền / chuẩn bị tài chính", priority = 1),
                    AiItem(type = "task", title = "Kiểm tra passport & giấy tờ", priority = 2),
                    AiItem(type = "note", title = "Danh sách đồ mang theo", description = "Quần áo, sạc pin, thuốc, giấy tờ, tiền mặt..."),
                ),
                message = "Đã tạo kế hoạch du lịch với 7 mục! 🧳"
            )
        }

        // "kế hoạch học tập / study plan"
        if (lower.matches(Regex(".*(?:kế hoạch|plan|lịch)\\s+(?:học|study|ôn thi|ôn bài|revision).*"))) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Xem lại bài cũ", priority = 1),
                    AiItem(type = "task", title = "Học bài mới", priority = 2),
                    AiItem(type = "task", title = "Làm bài tập / thực hành", priority = 2),
                    AiItem(type = "task", title = "Ghi chép tóm tắt", priority = 1),
                    AiItem(type = "task", title = "Ôn tập & kiểm tra kiến thức", priority = 1),
                    AiItem(type = "reminder", title = "Giải lao 5 phút", time = "10:00", repeatType = 0),
                ),
                message = "Đã tạo kế hoạch học tập! 📚"
            )
        }

        // "kế hoạch nấu ăn / cooking"
        if (lower.matches(Regex(".*(?:kế hoạch|plan|chuẩn bị)\\s+(?:nấu ăn|cooking|bếp|meal).*"))) {
            return AiActionResult(
                action = "bulk_create",
                items = listOf(
                    AiItem(type = "task", title = "Lên thực đơn trong tuần", priority = 1),
                    AiItem(type = "task", title = "Đi chợ mua nguyên liệu", priority = 2),
                    AiItem(type = "task", title = "Sơ chế & chuẩn bị", priority = 1),
                    AiItem(type = "task", title = "Nấu món chính", priority = 2),
                    AiItem(type = "task", title = "Dọn dẹp bếp", priority = 0),
                ),
                message = "Đã tạo kế hoạch nấu ăn! 🍳"
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 10. XEM NGÀY / ĐỔI NGÀY
    // ────────────────────────────────────────────────────────

    private fun tryParseDateQuery(input: String, lower: String): AiActionResult? {
        // "xem ngày dd/mm" hoặc "ngày dd/mm/yyyy" hoặc "xem ngày dd-mm-yyyy"
        val viewDateP = Regex("(?:xem|tra|check)?\\s*ngày\\s+(\\d{1,2})[/\\-.](\\d{1,2})(?:[/\\-.](\\d{2,4}))?", RegexOption.IGNORE_CASE)
        viewDateP.find(input)?.let { match ->
            val dd = match.groupValues[1].toInt()
            val mm = match.groupValues[2].toInt()
            val yy = match.groupValues[3].let {
                if (it.isBlank()) LocalDate.now().year
                else if (it.length == 2) 2000 + it.toInt()
                else it.toInt()
            }
            val dateStr = "$dd/$mm/$yy"
            return AiActionResult(
                action = "view_date",
                items = listOf(AiItem(type = "date_query", title = dateStr, dueDate = "${yy}-${mm.toString().padStart(2, '0')}-${dd.toString().padStart(2, '0')}")),
                message = "📅 Xem thông tin ngày $dateStr — hãy chuyển sang tab Lịch và chọn ngày $dd tháng $mm."
            )
        }

        // "ngày mai / ngày kia / hôm qua"
        if (lower.matches(Regex(".*(?:xem|tra)?\\s*(?:ngày mai|tomorrow).*"))) {
            val tomorrow = LocalDate.now().plusDays(1)
            val dateStr = "${tomorrow.dayOfMonth}/${tomorrow.monthValue}/${tomorrow.year}"
            return AiActionResult(
                action = "view_date",
                items = listOf(AiItem(type = "date_query", title = "Ngày mai ($dateStr)", dueDate = tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE))),
                message = "📅 Ngày mai: $dateStr — chuyển sang tab Lịch để xem chi tiết."
            )
        }
        if (lower.matches(Regex(".*(?:xem|tra)?\\s*(?:ngày kia|ngày mốt).*"))) {
            val dayAfter = LocalDate.now().plusDays(2)
            val dateStr = "${dayAfter.dayOfMonth}/${dayAfter.monthValue}/${dayAfter.year}"
            return AiActionResult(
                action = "view_date",
                items = listOf(AiItem(type = "date_query", title = "Ngày kia ($dateStr)", dueDate = dayAfter.format(DateTimeFormatter.ISO_LOCAL_DATE))),
                message = "📅 Ngày kia: $dateStr — chuyển sang tab Lịch để xem chi tiết."
            )
        }
        if (lower.matches(Regex(".*(?:xem|tra)?\\s*(?:hôm qua|yesterday).*"))) {
            val yesterday = LocalDate.now().minusDays(1)
            val dateStr = "${yesterday.dayOfMonth}/${yesterday.monthValue}/${yesterday.year}"
            return AiActionResult(
                action = "view_date",
                items = listOf(AiItem(type = "date_query", title = "Hôm qua ($dateStr)", dueDate = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE))),
                message = "📅 Hôm qua: $dateStr — chuyển sang tab Lịch để xem chi tiết."
            )
        }

        // "đổi ngày dd/mm sang âm lịch" / "âm lịch ngày dd/mm"
        if (lower.matches(Regex(".*(?:đổi|chuyển|convert|quy đổi|sang)\\s+.*(?:âm lịch|âm|lunar).*")) ||
            lower.matches(Regex(".*(?:âm lịch|lunar)\\s+(?:ngày|của).*"))) {
            return AiActionResult(
                action = "convert_date",
                items = emptyList(),
                message = "📅 Để xem ngày âm lịch, hãy chuyển sang tab Lịch — mỗi ngày đều hiển thị cả ngày dương và âm lịch tương ứng."
            )
        }

        // "cuối tuần này / weekend"
        if (lower.matches(Regex(".*(?:cuối tuần|weekend|thứ 7|thứ bảy|chủ nhật).*(?:xem|làm gì|kế hoạch)?.*"))) {
            val today = LocalDate.now()
            val daysUntilSat = (6 - today.dayOfWeek.value % 7 + 7) % 7
            val saturday = today.plusDays(daysUntilSat.toLong())
            val sunday = saturday.plusDays(1)
            return AiActionResult(
                action = "view_date",
                items = listOf(
                    AiItem(type = "date_query", title = "Thứ 7: ${saturday.dayOfMonth}/${saturday.monthValue}", dueDate = saturday.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    AiItem(type = "date_query", title = "Chủ nhật: ${sunday.dayOfMonth}/${sunday.monthValue}", dueDate = sunday.format(DateTimeFormatter.ISO_LOCAL_DATE))
                ),
                message = "📅 Cuối tuần:\n› Thứ 7: ${saturday.dayOfMonth}/${saturday.monthValue}\n› Chủ nhật: ${sunday.dayOfMonth}/${sunday.monthValue}\n\nChuyển sang tab Lịch để xem chi tiết phong thuỷ."
            )
        }

        return null
    }

    // ────────────────────────────────────────────────────────
    // 11. CATCH-ALL TẠO
    // ────────────────────────────────────────────────────────

    private fun tryParseSimpleCreate(input: String, lower: String): AiActionResult? {
        val simpleCreate = Regex("^(?:tạo|thêm|add|làm)\\s+(.{3,})$", RegexOption.IGNORE_CASE)
        simpleCreate.find(input)?.let { match ->
            val title = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val priority = when {
                lower.contains("quan trọng") || lower.contains("gấp") || lower.contains("khẩn") -> 2
                lower.contains("thấp") || lower.contains("nhẹ") -> 0
                else -> 1
            }
            val dueDate = parseDateFromText(lower)
            return AiActionResult(
                action = "create_task",
                items = listOf(AiItem(type = "task", title = title, priority = priority, dueDate = dueDate)),
                message = "Đã tạo công việc \"$title\"${if (dueDate != null) " (hạn: $dueDate)" else ""}! ✅"
            )
        }
        return null
    }

    // ════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ════════════════════════════════════════════════════════

    /** Trích xuất ngày từ text: "ngày mai", "hôm nay", "dd/mm", "thứ X tới" */
    private fun parseDateFromText(lower: String): String? {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        if (lower.contains("hôm nay") || lower.contains("today")) {
            return today.format(fmt)
        }
        if (lower.contains("ngày mai") || lower.contains("tomorrow")) {
            return today.plusDays(1).format(fmt)
        }
        if (lower.contains("ngày kia") || lower.contains("ngày mốt")) {
            return today.plusDays(2).format(fmt)
        }
        if (lower.contains("tuần sau") || lower.contains("next week")) {
            return today.plusWeeks(1).format(fmt)
        }
        if (lower.contains("tháng sau") || lower.contains("next month")) {
            return today.plusMonths(1).format(fmt)
        }

        // "thứ X" → next occurrence
        val thuMap = mapOf(
            "thứ 2" to 1, "thứ hai" to 1,
            "thứ 3" to 2, "thứ ba" to 2,
            "thứ 4" to 3, "thứ tư" to 3,
            "thứ 5" to 4, "thứ năm" to 4,
            "thứ 6" to 5, "thứ sáu" to 5,
            "thứ 7" to 6, "thứ bảy" to 6,
            "chủ nhật" to 7
        )
        for ((keyword, dayOfWeek) in thuMap) {
            if (lower.contains(keyword)) {
                val currentDow = today.dayOfWeek.value // Mon=1..Sun=7
                val diff = (dayOfWeek - currentDow + 7) % 7
                val targetDate = if (diff == 0) today.plusDays(7) else today.plusDays(diff.toLong())
                return targetDate.format(fmt)
            }
        }

        // "ngày dd/mm"
        val dateInText = Regex("ngày\\s+(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?").find(lower)
        if (dateInText != null) {
            val dd = dateInText.groupValues[1].toInt()
            val mm = dateInText.groupValues[2].toInt()
            val yy = dateInText.groupValues[3].let {
                if (it.isBlank()) today.year
                else if (it.length == 2) 2000 + it.toInt()
                else it.toInt()
            }
            return LocalDate.of(yy, mm, dd).format(fmt)
        }

        return null
    }

    /** Loại bỏ các keyword ngày khỏi title task */
    private fun cleanDateKeywords(title: String): String {
        return title
            .replace(Regex("\\s*(hôm nay|ngày mai|ngày kia|ngày mốt|tuần sau|tháng sau|today|tomorrow)\\s*", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s*(thứ \\d|thứ hai|thứ ba|thứ tư|thứ năm|thứ sáu|thứ bảy|chủ nhật)\\s*", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s*(ngày \\d{1,2}/\\d{1,2}(?:/\\d{2,4})?)\\s*", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("\\s*(quan trọng|gấp|khẩn|urgent|thấp|nhẹ|low|high)\\s*", RegexOption.IGNORE_CASE), " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
    }
}
