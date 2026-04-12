package com.lichso.app.ui.screen.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.ai.AiTaskService
import com.lichso.app.data.local.AiMemoryStore
import com.lichso.app.data.local.dao.ChatMessageDao
import com.lichso.app.data.local.dao.NoteDao
import com.lichso.app.data.local.dao.ReminderDao
import com.lichso.app.data.local.dao.TaskDao
import com.lichso.app.data.local.entity.ChatMessageEntity
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.data.remote.ChatMessage
import com.lichso.app.data.remote.OpenRouterApi
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.notification.ReminderScheduler
import com.lichso.app.ui.screen.profile.ProfileKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

/**
 * Thông tin profile tóm tắt để AI sử dụng.
 */
data class UserProfileSummary(
    val displayName: String = "",
    val gender: String = "",
    val birthDay: Int = 0,
    val birthMonth: Int = 0,
    val birthYear: Int = 0,
    val birthHour: Int = -1,
    val birthMinute: Int = -1,
    val lunarBirthDay: Int = 0,
    val lunarBirthMonth: Int = 0,
    val lunarBirthYear: Int = 0,
    val lunarLeap: Int = 0,
    val yearCanChi: String = "",
    val menh: String = "",
    val nguHanh: String = "",
    val conGiap: String = "",
    val cung: String = ""
) {
    /** Profile được coi là đầy đủ khi có tên + ngày sinh + giới tính */
    val isComplete: Boolean
        get() = displayName.isNotBlank() && displayName != "Người dùng"
                && birthDay > 0 && birthMonth > 0 && birthYear > 0

    /** Danh sách các trường còn thiếu */
    val missingFields: List<String>
        get() = buildList {
            if (displayName.isBlank() || displayName == "Người dùng") add("Tên hiển thị")
            if (birthDay <= 0 || birthMonth <= 0 || birthYear <= 0) add("Ngày tháng năm sinh")
            if (gender.isBlank()) add("Giới tính")
        }
}

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isTyping: Boolean = false,
    val profileSummary: UserProfileSummary = UserProfileSummary()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatMessageDao: ChatMessageDao,
    private val dayInfoProvider: DayInfoProvider,
    private val openRouterApi: OpenRouterApi,
    private val aiMemoryStore: AiMemoryStore,
    private val aiTaskService: AiTaskService,
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Collect profile data from DataStore
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val name = prefs[ProfileKeys.DISPLAY_NAME] ?: "Người dùng"
                val gender = prefs[ProfileKeys.GENDER] ?: ""
                val bDay = prefs[ProfileKeys.BIRTH_DAY] ?: 0
                val bMonth = prefs[ProfileKeys.BIRTH_MONTH] ?: 0
                val bYear = prefs[ProfileKeys.BIRTH_YEAR] ?: 0
                val bHour = prefs[ProfileKeys.BIRTH_HOUR] ?: -1
                val bMin = prefs[ProfileKeys.BIRTH_MINUTE] ?: -1

                // Tính toán thông tin phong thủy nếu có ngày sinh
                val summary = if (bYear > 0 && bMonth > 0 && bDay > 0) {
                    try {
                        val lunar = com.lichso.app.util.LunarCalendarUtil.convertSolar2Lunar(bDay, bMonth, bYear)
                        val lunarYear = lunar.lunarYear
                        val yearCanChi = com.lichso.app.util.CanChiCalculator.getYearCanChi(lunarYear)
                        val chiIndex = (lunarYear + 8) % 12
                        val conGiapNames = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
                        val conGiap = conGiapNames[chiIndex]

                        UserProfileSummary(
                            displayName = name,
                            gender = gender,
                            birthDay = bDay,
                            birthMonth = bMonth,
                            birthYear = bYear,
                            birthHour = bHour,
                            birthMinute = bMin,
                            lunarBirthDay = lunar.lunarDay,
                            lunarBirthMonth = lunar.lunarMonth,
                            lunarBirthYear = lunar.lunarYear,
                            lunarLeap = lunar.lunarLeap,
                            yearCanChi = yearCanChi,
                            conGiap = conGiap
                        )
                    } catch (_: Exception) {
                        UserProfileSummary(displayName = name, gender = gender, birthDay = bDay, birthMonth = bMonth, birthYear = bYear, birthHour = bHour, birthMinute = bMin)
                    }
                } else {
                    UserProfileSummary(displayName = name, gender = gender, birthDay = bDay, birthMonth = bMonth, birthYear = bYear, birthHour = bHour, birthMinute = bMin)
                }

                _uiState.update { it.copy(profileSummary = summary) }
            }
        }

        viewModelScope.launch {
            chatMessageDao.getAllMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
                if (messages.isEmpty()) {
                    val greeting = buildGreetingMessage()
                    chatMessageDao.insert(
                        ChatMessageEntity(
                            content = greeting,
                            isUser = false
                        )
                    )
                }
            }
        }
    }

    private suspend fun buildGreetingMessage(): String {
        val userName = aiMemoryStore.getUserName()
        val aiName = aiMemoryStore.getAiName()
        val i = ChatIcons
        val profile = _uiState.value.profileSummary

        val selfName = aiName ?: "Lịch Số AI"

        return if (profile.isComplete) {
            "Chào ${profile.displayName}! Tôi là $selfName — trợ lý phong thủy & lịch vạn niên của bạn.\n\n" +
                    "Tôi đã có thông tin cá nhân của bạn (tuổi ${profile.yearCanChi}, con giáp ${profile.conGiap}). " +
                    "Mọi phân tích phong thủy sẽ được cá nhân hóa theo tuổi & mệnh của bạn.\n\n" +
                    "Bạn muốn hỏi gì hôm nay?"
        } else if (userName != null) {
            "Chào $userName! Tôi là $selfName — trợ lý phong thủy & lịch vạn niên của bạn.\n\n" +
                    "${i.ARROW} Để tôi phân tích chính xác hơn (tử vi, hợp tuổi, hướng xuất hành...), hãy cập nhật hồ sơ cá nhân nhé!\n\n" +
                    "Bạn muốn hỏi gì hôm nay?"
        } else {
            "Xin chào! Tôi là $selfName — trợ lý phong thủy & lịch vạn niên thông minh.\n\n" +
                    "Tôi được hỗ trợ bởi AI tiên tiến, có thể giúp bạn:\n" +
                    "${i.ARROW} Xem ngày tốt/xấu cho công việc\n" +
                    "${i.ARROW} Tra cứu can chi, tiết khí, giờ hoàng đạo\n" +
                    "${i.ARROW} Gợi ý ngày cưới, khai trương, động thổ\n" +
                    "${i.ARROW} Tạo/sửa/xoá task, ghi chú, nhắc nhở\n" +
                    "${i.ARROW} Thống kê & báo cáo tình trạng công việc\n" +
                    "${i.ARROW} Giải đáp phong thủy chi tiết\n\n" +
                    "${i.SPARKLE} Hãy cập nhật hồ sơ cá nhân (tên, ngày sinh, giới tính) để tôi phân tích phong thủy chính xác theo tuổi & mệnh của bạn!"
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        // Prevent sending while already processing (rate limiting)
        if (_uiState.value.isTyping) return
        viewModelScope.launch {
            chatMessageDao.insert(ChatMessageEntity(content = text.trim(), isUser = true))
            _uiState.update { it.copy(isTyping = true) }

            // Check if this is a task/note/reminder command
            val lower = text.lowercase()
            val isActionCommand = lower.let { l ->
                l.contains("tạo") || l.contains("thêm") || l.contains("nhắc") ||
                l.contains("ghi chú") || l.contains("checklist") || l.contains("todo") ||
                l.contains("task") || l.contains("note") || l.contains("reminder") ||
                l.contains("kế hoạch") || l.contains("danh sách") ||
                l.contains("xoá") || l.contains("xóa") || l.contains("delete") || l.contains("remove") ||
                l.contains("sửa") || l.contains("edit") || l.contains("update") || l.contains("đổi") || l.contains("cập nhật") ||
                l.contains("xong") || l.contains("hoàn thành") || l.contains("done") || l.contains("đánh dấu") ||
                l.contains("xem task") || l.contains("xem note") || l.contains("xem nhắc") || l.contains("xem việc") ||
                l.contains("liệt kê") || l.contains("list") || l.contains("bao nhiêu") ||
                l.contains("alarm") || l.contains("báo thức") || l.contains("memo") ||
                l.contains("cần làm") || l.contains("phải làm") ||
                l.contains("plan") || l.contains("shopping") || l.contains("mua ") ||
                l.contains("thống kê") || l.contains("thong ke") || l.contains("statistics") ||
                l.contains("tổng hợp") || l.contains("tong hop") || l.contains("báo cáo") || l.contains("bao cao") ||
                l.contains("report") || l.contains("tổng kết") || l.contains("tong ket") || l.contains("summary") ||
                l.contains("tổng quan") || l.contains("tong quan") || l.contains("overview") ||
                l.contains("tình trạng") || l.contains("tinh trang") || l.contains("trạng thái") ||
                l.contains("đếm") || l.contains("count") || l.contains("số lượng") ||
                l.startsWith("nhắc") || l.startsWith("ghi ") || l.startsWith("note") || l.startsWith("todo")
            }

            if (isActionCommand) {
                try {
                    val result = aiTaskService.processCommand(text.trim())
                    val isStatsAction = result.action.startsWith("stats_")
                    val actionHandled = result.action != "general" && result.action != "suggest" && !isStatsAction

                    // Handle statistics separately — need to query real data
                    if (isStatsAction) {
                        val statsResponse = buildStatsResponse(result.action)
                        chatMessageDao.insert(ChatMessageEntity(content = statsResponse, isUser = false))
                        _uiState.update { it.copy(isTyping = false) }
                        return@launch
                    }
                    
                    if (actionHandled) {
                        // Execute the action (create/edit/delete/mark_done/view)
                        executeAiAction(result)
                        
                        val suffix = when (result.action) {
                            "view_date", "convert_date", "list_tasks", "list_notes", "list_reminders", "count_tasks",
                            "delete_all_tasks", "delete_all_notes" -> ""
                            "delete_task", "delete_note", "delete_reminder" -> "\n\n🗑️ Đã xử lý xong!"
                            "edit_task", "edit_note", "edit_reminder", "edit_task_priority" -> "\n\n✏️ Đã cập nhật!"
                            "mark_done" -> "\n\n✅ Đã đánh dấu hoàn thành!"
                            else -> "\n\n✨ Đã thực hiện xong! Bạn có thể xem ở tab \"Ghi chú & Việc làm\"."
                        }
                        val response = "${result.message}$suffix"
                        chatMessageDao.insert(ChatMessageEntity(content = response, isUser = false))
                        _uiState.update { it.copy(isTyping = false) }
                        return@launch
                    }
                    
                    // "suggest" action — return the suggestion message
                    if (result.action == "suggest") {
                        chatMessageDao.insert(ChatMessageEntity(content = result.message, isUser = false))
                        _uiState.update { it.copy(isTyping = false) }
                        return@launch
                    }
                } catch (_: Exception) {
                    // Fall through to normal chat
                }
            }

            val contextInfo = buildTodayContext()
            val memoryContext = aiMemoryStore.getMemoryContext()
            val profileContext = buildProfileContext()

            val history = _uiState.value.messages.takeLast(10).map { msg ->
                ChatMessage(
                    role = if (msg.isUser) "user" else "assistant",
                    content = msg.content
                )
            }

            val result = openRouterApi.chat(
                userMessage = text.trim(),
                contextInfo = contextInfo,
                memoryContext = memoryContext,
                profileContext = profileContext,
                history = history
            )

            val response = result.getOrElse { error ->
                val errMsg = error.message ?: ""
                val hint = when {
                    errMsg.contains("401") || errMsg.contains("User not found") ->
                        "${ChatIcons.WARNING} API key không hợp lệ hoặc đã hết hạn. Vui lòng cập nhật API key trong cài đặt.\n\n"
                    errMsg.contains("429") ->
                        "${ChatIcons.WARNING} Đã vượt giới hạn gọi AI. Thử lại sau vài phút.\n\n"
                    errMsg.contains("timeout") || errMsg.contains("Timeout") ->
                        "${ChatIcons.WARNING} Kết nối AI bị timeout. Kiểm tra mạng và thử lại.\n\n"
                    else ->
                        "${ChatIcons.WARNING} Không thể kết nối AI. Đang dùng trả lời cục bộ.\n\n"
                }
                hint + generateLocalResponse(text.trim())
            }

            chatMessageDao.insert(ChatMessageEntity(content = response, isUser = false))
            _uiState.update { it.copy(isTyping = false) }

            // Parse and save memory from the conversation
            aiMemoryStore.parseAndSaveFromAiResponse(text.trim(), response)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatMessageDao.clearAll()
        }
    }

    private suspend fun executeAiAction(result: AiTaskService.AiActionResult) {
        result.items.forEach { item ->
            when (item.type) {
                "task" -> {
                    val dueDate = item.dueDate?.let {
                        try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
                        } catch (_: Exception) { null }
                    }
                    taskDao.insert(
                        TaskEntity(
                            title = item.title,
                            description = item.description,
                            priority = item.priority,
                            dueDate = dueDate
                        )
                    )
                }
                "note" -> {
                    noteDao.insert(
                        NoteEntity(
                            title = item.title,
                            content = item.description,
                            colorIndex = item.colorIndex
                        )
                    )
                }
                "reminder" -> {
                    val cal = Calendar.getInstance()
                    item.time?.let { timeStr ->
                        try {
                            val parts = timeStr.split(":")
                            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt().coerceIn(0, 23))
                            cal.set(Calendar.MINUTE, parts.getOrElse(1) { "0" }.toInt().coerceIn(0, 59))
                            cal.set(Calendar.SECOND, 0)
                        } catch (_: Exception) { }
                    }
                    val entity = ReminderEntity(
                            title = item.title,
                            triggerTime = cal.timeInMillis,
                            repeatType = item.repeatType
                        )
                    val id = reminderDao.insert(entity)
                    ReminderScheduler(context).schedule(entity.copy(id = id))
                }
                "date_query" -> {
                    // Date queries don't create anything — message only
                }
            }
        }

        // Handle delete/edit/mark_done by searching existing items
        when (result.action) {
            "delete_task" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    val tasks = _uiState.value.messages // We don't have task list here directly
                    // Try to find and delete via taskDao
                    try {
                        taskDao.getAllTasks().first().find { t ->
                            t.title.lowercase().contains(keyword)
                        }?.let { found ->
                            taskDao.delete(found)
                        }
                    } catch (_: Exception) {}
                }
            }
            "delete_note" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    try {
                        noteDao.getAllNotes().first().find { n ->
                            n.title.lowercase().contains(keyword)
                        }?.let { found ->
                            noteDao.delete(found)
                        }
                    } catch (_: Exception) {}
                }
            }
            "delete_reminder" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    try {
                        reminderDao.getAllReminders().first().find { r ->
                            r.title.lowercase().contains(keyword)
                        }?.let { found ->
                            reminderDao.delete(found)
                            ReminderScheduler(context).cancel(found.id)
                        }
                    } catch (_: Exception) {}
                }
            }
            "edit_task" -> {
                result.items.forEach { item ->
                    val oldTitle = item.description.removePrefix("old_title:").lowercase()
                    try {
                        taskDao.getAllTasks().first().find { t ->
                            t.title.lowercase().contains(oldTitle)
                        }?.let { found ->
                            taskDao.update(found.copy(title = item.title, updatedAt = System.currentTimeMillis()))
                        }
                    } catch (_: Exception) {}
                }
            }
            "edit_note" -> {
                result.items.forEach { item ->
                    val oldTitle = item.description.removePrefix("old_title:").lowercase()
                    try {
                        noteDao.getAllNotes().first().find { n ->
                            n.title.lowercase().contains(oldTitle)
                        }?.let { found ->
                            noteDao.update(found.copy(title = item.title, updatedAt = System.currentTimeMillis()))
                        }
                    } catch (_: Exception) {}
                }
            }
            "edit_task_priority" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    try {
                        taskDao.getAllTasks().first().find { t ->
                            t.title.lowercase().contains(keyword)
                        }?.let { found ->
                            taskDao.update(found.copy(priority = item.priority, updatedAt = System.currentTimeMillis()))
                        }
                    } catch (_: Exception) {}
                }
            }
            "edit_reminder" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    try {
                        reminderDao.getAllReminders().first().find { r ->
                            r.title.lowercase().contains(keyword)
                        }?.let { found ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = found.triggerTime
                            item.time?.let { timeStr ->
                                val parts = timeStr.split(":")
                                cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt().coerceIn(0, 23))
                                cal.set(Calendar.MINUTE, parts.getOrElse(1) { "0" }.toInt().coerceIn(0, 59))
                            }
                            reminderDao.update(found.copy(triggerTime = cal.timeInMillis))
                        }
                    } catch (_: Exception) {}
                }
            }
            "mark_done" -> {
                result.items.forEach { item ->
                    val keyword = item.title.lowercase()
                    try {
                        taskDao.getAllTasks().first().find { t ->
                            t.title.lowercase().contains(keyword) && !t.isDone
                        }?.let { found ->
                            taskDao.toggleDone(found.id, true)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    // ════════════════════════════════════════════════════════
    // STATISTICS / THỐNG KÊ
    // ════════════════════════════════════════════════════════

    private suspend fun buildStatsResponse(action: String): String {
        val i = ChatIcons
        val sb = StringBuilder()
        val tasks = try { taskDao.getAllTasks().first() } catch (_: Exception) { emptyList() }
        val notes = try { noteDao.getAllNotes().first() } catch (_: Exception) { emptyList() }
        val reminders = try { reminderDao.getAllReminders().first() } catch (_: Exception) { emptyList() }

        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        when (action) {
            "stats_tasks", "stats_all" -> {
                val totalTasks = tasks.size
                val doneTasks = tasks.count { it.isDone }
                val pendingTasks = totalTasks - doneTasks
                val highPriority = tasks.count { it.priority == 2 && !it.isDone }
                val medPriority = tasks.count { it.priority == 1 && !it.isDone }
                val lowPriority = tasks.count { it.priority == 0 && !it.isDone }
                val todayTasks = tasks.filter { t ->
                    t.dueDate?.let { sdfDate.format(Date(it)) == todayStr } == true
                }
                val overdueTasks = tasks.filter { t ->
                    !t.isDone && t.dueDate != null && t.dueDate < System.currentTimeMillis() &&
                            sdfDate.format(Date(t.dueDate)) != todayStr
                }

                sb.appendLine("${i.STAR} THỐNG KÊ CÔNG VIỆC")
                sb.appendLine("${i.SECTION}")
                sb.appendLine("${i.BULLET} Tổng cộng: $totalTasks task")
                sb.appendLine("${i.CHECK} Hoàn thành: $doneTasks ✅")
                sb.appendLine("${i.CROSS} Chưa xong: $pendingTasks")
                if (totalTasks > 0) {
                    val percent = (doneTasks * 100) / totalTasks
                    sb.appendLine("${i.SPARKLE} Tiến độ: $percent%")
                }
                sb.appendLine()
                sb.appendLine("${i.ARROW} Ưu tiên (chưa xong):")
                sb.appendLine("   🔴 Cao: $highPriority")
                sb.appendLine("   🟡 Vừa: $medPriority")
                sb.appendLine("   🟢 Thấp: $lowPriority")
                if (todayTasks.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("${i.CALENDAR} Hôm nay: ${todayTasks.size} task")
                    todayTasks.take(5).forEach { t ->
                        val status = if (t.isDone) "✅" else "⬜"
                        sb.appendLine("   $status ${t.title}")
                    }
                }
                if (overdueTasks.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("${i.WARNING} Quá hạn: ${overdueTasks.size} task")
                    overdueTasks.take(5).forEach { t ->
                        val date = t.dueDate?.let { sdfDate.format(Date(it)) } ?: ""
                        sb.appendLine("   ${i.CROSS} ${t.title} ($date)")
                    }
                }
                if (pendingTasks > 0 && totalTasks > 0) {
                    sb.appendLine()
                    val topPending = tasks.filter { !it.isDone }.sortedByDescending { it.priority }.take(3)
                    sb.appendLine("${i.INFO} Top việc cần làm:")
                    topPending.forEach { t ->
                        val pLabel = when (t.priority) { 2 -> "🔴"; 1 -> "🟡"; else -> "🟢" }
                        sb.appendLine("   $pLabel ${t.title}")
                    }
                }
            }
        }

        when (action) {
            "stats_notes", "stats_all" -> {
                if (action == "stats_all" && sb.isNotEmpty()) sb.appendLine()
                val totalNotes = notes.size
                val colorDistribution = notes.groupBy { it.colorIndex }
                val recentNotes = notes.sortedByDescending { it.updatedAt }.take(5)

                sb.appendLine("${i.INFO} THỐNG KÊ GHI CHÚ")
                sb.appendLine("${i.SECTION}")
                sb.appendLine("${i.BULLET} Tổng cộng: $totalNotes ghi chú 📝")
                if (recentNotes.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("${i.ARROW} Gần đây nhất:")
                    recentNotes.forEach { n ->
                        val date = sdfDate.format(Date(n.updatedAt))
                        sb.appendLine("   ${i.BULLET} ${n.title} ($date)")
                    }
                }
            }
        }

        when (action) {
            "stats_reminders", "stats_all" -> {
                if (action == "stats_all" && sb.isNotEmpty()) sb.appendLine()
                val totalReminders = reminders.size
                val enabledReminders = reminders.count { it.isEnabled }
                val disabledReminders = totalReminders - enabledReminders
                val dailyReminders = reminders.count { it.repeatType == 1 }
                val weeklyReminders = reminders.count { it.repeatType == 2 }
                val monthlyReminders = reminders.count { it.repeatType == 3 }
                val onceReminders = reminders.count { it.repeatType == 0 }
                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

                sb.appendLine("${i.CLOCK} THỐNG KÊ NHẮC NHỞ")
                sb.appendLine("${i.SECTION}")
                sb.appendLine("${i.BULLET} Tổng cộng: $totalReminders nhắc nhở ⏰")
                sb.appendLine("${i.CHECK} Đang bật: $enabledReminders")
                sb.appendLine("${i.CROSS} Đã tắt: $disabledReminders")
                sb.appendLine()
                sb.appendLine("${i.ARROW} Theo loại:")
                sb.appendLine("   ${i.BULLET} Một lần: $onceReminders")
                sb.appendLine("   ${i.BULLET} Hàng ngày: $dailyReminders")
                sb.appendLine("   ${i.BULLET} Hàng tuần: $weeklyReminders")
                sb.appendLine("   ${i.BULLET} Hàng tháng: $monthlyReminders")
                if (enabledReminders > 0) {
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Nhắc nhở đang hoạt động:")
                    reminders.filter { it.isEnabled }.take(5).forEach { r ->
                        val time = sdfTime.format(Date(r.triggerTime))
                        val repeat = when (r.repeatType) {
                            1 -> "hàng ngày"; 2 -> "hàng tuần"; 3 -> "hàng tháng"; else -> "một lần"
                        }
                        sb.appendLine("   ${i.SPARKLE} ${r.title} — $time ($repeat)")
                    }
                }
            }
        }

        // Summary footer
        if (action == "stats_all") {
            sb.appendLine()
            sb.appendLine("${i.SECTION}")
            val totalItems = tasks.size + notes.size + reminders.size
            sb.appendLine("${i.FORTUNE} Tổng cộng bạn có $totalItems mục: ${tasks.size} task, ${notes.size} ghi chú, ${reminders.size} nhắc nhở")
            val pendingCount = tasks.count { !it.isDone }
            if (pendingCount > 0) {
                sb.appendLine("${i.ARROW} Bạn còn $pendingCount công việc chưa hoàn thành. Cố lên! 💪")
            } else if (tasks.isNotEmpty()) {
                sb.appendLine("${i.SPARKLE} Tuyệt vời! Bạn đã hoàn thành tất cả công việc! 🎉")
            }
        }

        return sb.toString().trim()
    }

    private suspend fun buildTodayContext(): String {
        return try {
            val today = LocalDate.now()
            val info = dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
            val sb = StringBuilder()
            sb.appendLine("Ngày: ${info.solar.dd}/${info.solar.mm}/${info.solar.yy} (${info.dayOfWeek})")
            sb.appendLine("Âm lịch: ${info.lunar.day}/${info.lunar.month} năm ${info.yearCanChi}")
            sb.appendLine("Can chi ngày: ${info.dayCanChi}")
            sb.appendLine("Can chi tháng: ${info.monthCanChi}")
            sb.appendLine("Can chi năm: ${info.yearCanChi}")
            sb.appendLine("Ngày xấu: ${info.activities.isXauDay}")
            if (info.activities.isNguyetKy) sb.appendLine("Nguyệt Kỵ: Có")
            if (info.activities.isTamNuong) sb.appendLine("Tam Nương: Có")
            sb.appendLine("Nên: ${info.activities.nenLam.joinToString(", ")}")
            sb.appendLine("Không nên: ${info.activities.khongNen.joinToString(", ")}")
            sb.appendLine("Giờ hoàng đạo: ${info.gioHoangDao.joinToString(", ") { "${it.name} (${it.time})" }}")
            sb.appendLine("Hướng Thần Tài: ${info.huong.thanTai}")
            sb.appendLine("Hướng Hỷ Thần: ${info.huong.hyThan}")
            sb.appendLine("Hướng Hắc Thần: ${info.huong.hungThan}")
            if (info.tietKhi.currentName != null) sb.appendLine("Tiết khí: ${info.tietKhi.currentName}")
            sb.appendLine("Pha trăng: ${info.moonPhase.name} (ngày ${info.moonPhase.age})")
            if (info.lunarHoliday != null) sb.appendLine("Ngày lễ âm: ${info.lunarHoliday}")
            if (info.solarHoliday != null) sb.appendLine("Ngày lễ dương: ${info.solarHoliday}")

            // Thêm context task/note/reminder để AI hiểu tình trạng người dùng
            try {
                val tasks = taskDao.getAllTasks().first()
                val notes = noteDao.getAllNotes().first()
                val reminders = reminderDao.getAllReminders().first()
                val pendingTasks = tasks.count { !it.isDone }
                val doneTasks = tasks.count { it.isDone }
                sb.appendLine()
                sb.appendLine("=== THÔNG TIN TASK/GHI CHÚ ===")
                sb.appendLine("Tổng task: ${tasks.size} (xong: $doneTasks, chưa xong: $pendingTasks)")
                sb.appendLine("Tổng ghi chú: ${notes.size}")
                sb.appendLine("Tổng nhắc nhở: ${reminders.size} (đang bật: ${reminders.count { it.isEnabled }})")
                if (pendingTasks > 0) {
                    sb.appendLine("Task chưa xong: ${tasks.filter { !it.isDone }.take(5).joinToString(", ") { it.title }}")
                }
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val todayTasks = tasks.filter { t ->
                    t.dueDate?.let { sdfDate.format(Date(it)) == todayStr } == true
                }
                if (todayTasks.isNotEmpty()) {
                    sb.appendLine("Task hôm nay: ${todayTasks.joinToString(", ") { "${it.title}${if (it.isDone) " ✅" else ""}" }}")
                }
            } catch (_: Exception) { }

            sb.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildProfileContext(): String {
        val profile = _uiState.value.profileSummary
        val sb = StringBuilder()
        sb.appendLine("=== THÔNG TIN CÁ NHÂN NGƯỜI DÙNG (BẮT BUỘC SỬ DỤNG, TUYỆT ĐỐI KHÔNG HỎI LẠI) ===")
        if (profile.displayName.isNotBlank() && profile.displayName != "Người dùng") {
            sb.appendLine("Tên: ${profile.displayName}")
        }
        if (profile.gender.isNotBlank()) {
            sb.appendLine("Giới tính: ${profile.gender}")
        }
        if (profile.birthYear > 0 && profile.birthMonth > 0 && profile.birthDay > 0) {
            sb.appendLine("Ngày sinh dương lịch: ${profile.birthDay}/${profile.birthMonth}/${profile.birthYear}")
        }
        if (profile.lunarBirthYear > 0 && profile.lunarBirthMonth > 0 && profile.lunarBirthDay > 0) {
            val leapNote = if (profile.lunarLeap != 0) " (tháng nhuận)" else ""
            sb.appendLine("Ngày sinh âm lịch: ${profile.lunarBirthDay}/${profile.lunarBirthMonth}/${profile.lunarBirthYear}$leapNote")
        }
        if (profile.birthHour >= 0 && profile.birthMinute >= 0) {
            sb.appendLine("Giờ sinh: ${profile.birthHour}h${String.format("%02d", profile.birthMinute)}")
        }
        if (profile.yearCanChi.isNotBlank()) sb.appendLine("Tuổi Can Chi: ${profile.yearCanChi}")
        if (profile.conGiap.isNotBlank()) sb.appendLine("Con giáp: ${profile.conGiap}")
        if (profile.menh.isNotBlank()) sb.appendLine("Mệnh: ${profile.menh}")
        if (profile.nguHanh.isNotBlank()) sb.appendLine("Ngũ hành nạp âm: ${profile.nguHanh}")
        if (profile.cung.isNotBlank()) sb.appendLine("Cung: ${profile.cung}")
        sb.appendLine("→ Tất cả thông tin trên đã được người dùng cung cấp. SỬ DỤNG TRỰC TIẾP, KHÔNG HỎI LẠI.")
        return sb.toString().trim()
    }

    private fun generateLocalResponse(query: String): String {
        val today = LocalDate.now()
        val info = dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
        val q = query.lowercase()
        val i = ChatIcons

        return when {
            // ── Hôm nay / ngày tốt ──
            q.contains("hôm nay") || q.contains("ngày tốt") || q.contains("hom nay") || q.contains("today") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} Hôm nay — ${info.solar.dd}/${info.solar.mm}/${info.solar.yy}")
                sb.appendLine("${i.LUNAR} Âm lịch: ${info.lunar.day}/${info.lunar.month} ${info.yearCanChi}")
                sb.appendLine("${i.CANCHI} Ngày: ${info.dayCanChi} · ${info.dayOfWeek}")
                sb.appendLine()
                if (info.activities.isXauDay) {
                    sb.appendLine("${i.WARNING} Hôm nay là ngày xấu.")
                }
                if (info.activities.isNguyetKy) sb.appendLine("${i.WARNING} Nguyệt Kỵ")
                if (info.activities.isTamNuong) sb.appendLine("${i.WARNING} Tam Nương")
                sb.appendLine("${i.CHECK} Nên: ${info.activities.nenLam.take(5).joinToString(", ")}")
                sb.appendLine("${i.CROSS} Không nên: ${info.activities.khongNen.take(5).joinToString(", ")}")
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.COMPASS} Hướng Thần Tài: ${info.huong.thanTai}")
                sb.appendLine("${i.JOY} Hướng Hỷ Thần: ${info.huong.hyThan}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Giờ hoàng đạo hôm nay")
                sb.appendLine("📌 Xem tử vi hôm nay")
                sb.appendLine("📌 Ngày mai thế nào")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Giờ hoàng đạo ──
            q.contains("giờ hoàng đạo") || q.contains("gio hoang dao") || q.contains("giờ tốt") || q.contains("gio tot") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CLOCK} GIỜ HOÀNG ĐẠO — ${info.dayOfWeek} ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine("${i.CANCHI} Ngày: ${info.dayCanChi}")
                sb.appendLine()
                info.gioHoangDao.forEach { sb.appendLine("   ${i.SPARKLE} ${it.name} (${it.time})") }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Hướng xuất hành tốt hôm nay")
                sb.appendLine("📌 Ngày mai giờ nào tốt")
                sb.appendLine("📌 Đánh giá tổng quan ngày")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Hướng xuất hành ──
            q.contains("hướng") || q.contains("huong") || q.contains("xuất hành") || q.contains("xuat hanh") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.COMPASS} HƯỚNG TỐT — ${info.dayOfWeek} ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                sb.appendLine("${i.FORTUNE} Thần Tài: ${info.huong.thanTai}")
                sb.appendLine("${i.JOY} Hỷ Thần: ${info.huong.hyThan}")
                sb.appendLine("${i.WARNING} Hắc Thần (tránh): ${info.huong.hungThan}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Giờ hoàng đạo hôm nay")
                sb.appendLine("📌 Hướng nhà hợp mệnh tôi")
                sb.appendLine("📌 Can chi hôm nay")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Can chi ──
            q.contains("can chi") || q.contains("canchi") || q.contains("thiên can") || q.contains("địa chi") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CANCHI} CAN CHI HÔM NAY")
                sb.appendLine("${i.STAR} Năm: ${info.yearCanChi}")
                sb.appendLine("${i.CALENDAR} Tháng: ${info.monthCanChi}")
                sb.appendLine("${i.INFO} Ngày: ${info.dayCanChi}")
                sb.appendLine("${i.CLOCK} Giờ hiện tại: ${info.hourCanChi}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Phân tích bát tự tứ trụ của tôi")
                sb.appendLine("📌 Ngũ hành ngày hôm nay")
                sb.appendLine("📌 Giờ hoàng đạo hôm nay")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Bát tự / Tứ trụ ──
            q.contains("bát tự") || q.contains("bat tu") || q.contains("tứ trụ") || q.contains("tu tru") || q.contains("4 trụ") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                if (profile.isComplete && profile.birthYear > 0) {
                    sb.appendLine("${i.SPARKLE} BÁT TỰ TỨ TRỤ — ${profile.displayName}")
                    sb.appendLine()
                    sb.appendLine("${i.CALENDAR} Ngày sinh: ${profile.birthDay}/${profile.birthMonth}/${profile.birthYear}")
                    if (profile.birthHour >= 0) sb.appendLine("${i.CLOCK} Giờ sinh: ${profile.birthHour}h${profile.birthMinute}")
                    sb.appendLine("${i.CANCHI} Năm: ${profile.yearCanChi}")
                    if (profile.menh.isNotBlank()) sb.appendLine("${i.STAR} Mệnh: ${profile.menh}")
                    if (profile.nguHanh.isNotBlank()) sb.appendLine("${i.INFO} Ngũ hành nạp âm: ${profile.nguHanh}")
                    if (profile.conGiap.isNotBlank()) sb.appendLine("${i.FORTUNE} Con giáp: ${profile.conGiap}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Để xem phân tích bát tự chi tiết (thiên can, địa chi, tàng can, dụng thần...), hãy kết nối AI trực tuyến.")
                } else {
                    sb.appendLine("${i.SPARKLE} BÁT TỰ TỨ TRỤ")
                    sb.appendLine()
                    sb.appendLine("${i.WARNING} Để phân tích bát tự, tôi cần biết thêm:")
                    if (profile.birthYear <= 0) sb.appendLine("${i.ARROW} Ngày sinh dương lịch (ngày/tháng/năm)")
                    if (profile.birthHour < 0) sb.appendLine("${i.ARROW} Giờ sinh (quan trọng — xác định trụ Giờ)")
                    if (profile.gender.isBlank()) sb.appendLine("${i.ARROW} Giới tính (ảnh hưởng đại vận)")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Hãy cập nhật hồ sơ cá nhân hoặc cho tôi biết ngày giờ sinh của bạn!")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Xem vận hạn năm nay")
                sb.appendLine("📌 Phân tích ngũ hành của tôi")
                sb.appendLine("📌 Xem hợp tuổi vợ chồng")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Tử vi ──
            q.contains("tử vi") || q.contains("tu vi") || q.contains("lá số") || q.contains("la so") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.STAR} TỬ VI")
                sb.appendLine()
                if (profile.isComplete && profile.birthYear > 0) {
                    sb.appendLine("${i.CALENDAR} ${profile.displayName} — sinh ${profile.birthDay}/${profile.birthMonth}/${profile.birthYear}")
                    if (profile.yearCanChi.isNotBlank()) sb.appendLine("${i.CANCHI} Tuổi: ${profile.yearCanChi}")
                    if (profile.conGiap.isNotBlank()) sb.appendLine("${i.FORTUNE} Con giáp: ${profile.conGiap}")
                    if (profile.menh.isNotBlank()) sb.appendLine("${i.SPARKLE} Mệnh: ${profile.menh}")
                    if (profile.cung.isNotBlank()) sb.appendLine("${i.INFO} Cung: ${profile.cung}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Để xem tử vi trọn bộ 12 cung (Mệnh, Thân, Quan Lộc, Tài Bạch, Phu Thê...), hãy kết nối AI trực tuyến để phân tích chi tiết.")
                } else {
                    sb.appendLine("${i.WARNING} Để lập lá số tử vi, tôi cần biết thêm:")
                    if (profile.birthYear <= 0) sb.appendLine("${i.ARROW} Ngày sinh dương lịch đầy đủ")
                    if (profile.birthHour < 0) sb.appendLine("${i.ARROW} Giờ sinh chính xác")
                    if (profile.gender.isBlank()) sb.appendLine("${i.ARROW} Giới tính")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Hãy cập nhật hồ sơ cá nhân để tôi phân tích tử vi cho bạn!")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Vận hạn năm nay của tôi")
                sb.appendLine("📌 Phân tích bát tự tứ trụ")
                sb.appendLine("📌 Con giáp năm nay vận thế sao")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Vận mệnh / Vận hạn / Lưu niên ──
            q.contains("vận mệnh") || q.contains("van menh") || q.contains("vận hạn") || q.contains("van han") ||
            q.contains("lưu niên") || q.contains("luu nien") || q.contains("đại vận") || q.contains("dai van") ||
            q.contains("vận năm") || q.contains("van nam") || q.contains("năm nay") && (q.contains("vận") || q.contains("sao")) -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.SPARKLE} VẬN HẠN ${today.year}")
                sb.appendLine()
                if (profile.isComplete && profile.birthYear > 0) {
                    val tuoi = today.year - profile.birthYear
                    sb.appendLine("${i.CALENDAR} ${profile.displayName} — $tuoi tuổi")
                    if (profile.yearCanChi.isNotBlank()) sb.appendLine("${i.CANCHI} Tuổi: ${profile.yearCanChi}")
                    if (profile.conGiap.isNotBlank()) sb.appendLine("${i.FORTUNE} Con giáp: ${profile.conGiap}")
                    if (profile.menh.isNotBlank()) sb.appendLine("${i.STAR} Mệnh: ${profile.menh}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Kết nối AI trực tuyến để xem phân tích vận hạn chi tiết: đại vận, tiểu vận, lưu niên ${today.year}, sao chiếu mệnh, cát hung...")
                } else {
                    sb.appendLine("${i.WARNING} Để phân tích vận hạn, tôi cần biết ngày giờ sinh của bạn.")
                    sb.appendLine("${i.ARROW} Hãy cập nhật hồ sơ cá nhân!")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Phân tích bát tự tứ trụ")
                sb.appendLine("📌 Tử vi năm nay chi tiết")
                sb.appendLine("📌 Hướng xuất hành tốt hôm nay")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Ngũ hành ──
            q.contains("ngũ hành") || q.contains("ngu hanh") || q.contains("kim mộc") || q.contains("thủy hỏa") || q.contains("mệnh") && !q.contains("vận") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.SPARKLE} NGŨ HÀNH & MỆNH")
                sb.appendLine()
                if (profile.isComplete && profile.birthYear > 0) {
                    sb.appendLine("${i.CALENDAR} ${profile.displayName}")
                    if (profile.yearCanChi.isNotBlank()) sb.appendLine("${i.CANCHI} Tuổi: ${profile.yearCanChi}")
                    if (profile.menh.isNotBlank()) sb.appendLine("${i.STAR} Mệnh: ${profile.menh}")
                    if (profile.nguHanh.isNotBlank()) sb.appendLine("${i.INFO} Ngũ hành nạp âm: ${profile.nguHanh}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Kết nối AI trực tuyến để xem chi tiết: hợp/kỵ màu sắc, hướng, số, nghề nghiệp phù hợp với mệnh...")
                } else {
                    sb.appendLine("${i.WARNING} Để phân tích ngũ hành, tôi cần biết năm sinh của bạn.")
                    sb.appendLine("${i.ARROW} Hãy cập nhật hồ sơ cá nhân!")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Màu sắc hợp mệnh của tôi")
                sb.appendLine("📌 Hướng nhà tốt theo mệnh")
                sb.appendLine("📌 Phân tích bát tự tứ trụ")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Hợp tuổi ──
            q.contains("hợp tuổi") || q.contains("hop tuoi") || q.contains("xung tuổi") || q.contains("xung tuoi") ||
            q.contains("tam hợp") || q.contains("lục hợp") || q.contains("tứ hành xung") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.JOY} HỢP TUỔI")
                sb.appendLine()
                if (profile.isComplete && profile.conGiap.isNotBlank()) {
                    sb.appendLine("${i.CALENDAR} ${profile.displayName} — tuổi ${profile.yearCanChi}")
                    sb.appendLine("${i.FORTUNE} Con giáp: ${profile.conGiap}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Kết nối AI trực tuyến để phân tích hợp tuổi chi tiết: tam hợp, lục hợp, tứ hành xung, hợp vợ chồng, hợp làm ăn...")
                } else {
                    sb.appendLine("${i.WARNING} Để xem hợp tuổi, tôi cần biết năm sinh của bạn.")
                    sb.appendLine("${i.ARROW} Hãy cập nhật hồ sơ cá nhân!")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Bạn cũng có thể hỏi: \"Tuổi Tý hợp với tuổi nào?\"")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Xem tử vi hôm nay")
                sb.appendLine("📌 Ngày cưới tốt tháng này")
                sb.appendLine("📌 Phân tích ngũ hành vợ chồng")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Con giáp / 12 con giáp ──
            q.contains("con giáp") || q.contains("con giap") || q.contains("12 con") || q.contains("tuổi tý") || q.contains("tuổi sửu") ||
            q.contains("tuổi dần") || q.contains("tuổi mão") || q.contains("tuổi thìn") || q.contains("tuổi tỵ") ||
            q.contains("tuổi ngọ") || q.contains("tuổi mùi") || q.contains("tuổi thân") || q.contains("tuổi dậu") ||
            q.contains("tuổi tuất") || q.contains("tuổi hợi") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.FORTUNE} VẬN THẾ CON GIÁP ${today.year}")
                sb.appendLine()
                if (profile.conGiap.isNotBlank()) {
                    sb.appendLine("${i.STAR} Con giáp: ${profile.conGiap}")
                    if (profile.yearCanChi.isNotBlank()) sb.appendLine("${i.CANCHI} Tuổi: ${profile.yearCanChi}")
                }
                sb.appendLine()
                sb.appendLine("${i.INFO} Kết nối AI trực tuyến để xem vận thế ${today.year} chi tiết: tài lộc, sự nghiệp, tình duyên, sức khỏe, quý nhân...")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Vận hạn năm nay chi tiết")
                sb.appendLine("📌 Hợp tuổi của tôi")
                sb.appendLine("📌 Tử vi tháng này")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Phong thủy nhà / hướng nhà ──
            q.contains("phong thủy nhà") || q.contains("phong thuy nha") || q.contains("hướng nhà") || q.contains("huong nha") ||
            q.contains("bàn thờ") || q.contains("ban tho") || q.contains("phòng ngủ") || q.contains("phong ngu") -> {
                val profile = _uiState.value.profileSummary
                val sb = StringBuilder()
                sb.appendLine("${i.COMPASS} PHONG THỦY NHÀ CỬA")
                sb.appendLine()
                if (profile.menh.isNotBlank()) {
                    sb.appendLine("${i.STAR} Mệnh: ${profile.menh}")
                    sb.appendLine()
                    sb.appendLine("${i.INFO} Kết nối AI trực tuyến để được tư vấn chi tiết: hướng nhà tốt, vị trí bàn thờ, phòng ngủ, bếp theo mệnh ${profile.menh}...")
                } else {
                    sb.appendLine("${i.WARNING} Cần biết mệnh của bạn để tư vấn phong thủy chính xác.")
                    sb.appendLine("${i.ARROW} Hãy cập nhật hồ sơ cá nhân (ngày sinh)!")
                }
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Hướng bàn làm việc theo mệnh")
                sb.appendLine("📌 Màu sơn nhà hợp mệnh")
                sb.appendLine("📌 Ngày tốt động thổ tháng này")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Đặt tên / chọn tên ──
            q.contains("đặt tên") || q.contains("dat ten") || q.contains("chọn tên") || q.contains("chon ten") ||
            q.contains("tên con") || q.contains("ten con") || q.contains("tên đẹp") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.SPARKLE} GỢI Ý ĐẶT TÊN")
                sb.appendLine()
                sb.appendLine("${i.INFO} Để gợi ý tên phù hợp, tôi cần biết:")
                sb.appendLine("${i.ARROW} Năm sinh của bé (hoặc năm dự sinh)")
                sb.appendLine("${i.ARROW} Giới tính")
                sb.appendLine("${i.ARROW} Họ của gia đình")
                sb.appendLine()
                sb.appendLine("${i.INFO} Kết nối AI trực tuyến để được gợi ý tên đẹp theo ngũ hành, mệnh, ý nghĩa và phong thủy.")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Ngũ hành mệnh năm nay")
                sb.appendLine("📌 Xem hợp tuổi bố mẹ - con")
                sb.appendLine("📌 Ngày tốt đầy tháng bé")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Tiết khí ──
            q.contains("tiết khí") || q.contains("tiet khi") || q.contains("solar term") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.SPARKLE} TIẾT KHÍ")
                if (info.tietKhi.currentName != null) {
                    sb.appendLine("${i.STAR} Tiết khí hiện tại: ${info.tietKhi.currentName}")
                }
                if (info.tietKhi.nextName != null) {
                    sb.appendLine("${i.ARROW} Tiết khí tiếp: ${info.tietKhi.nextName} (${info.tietKhi.nextDd}/${info.tietKhi.nextMm})")
                    sb.appendLine("${i.CLOCK} Còn ${info.tietKhi.daysUntilNext} ngày")
                }
                sb.toString().trim()
            }

            // ── Trực / Sao ──
            q.contains("trực") || q.contains("truc ngay") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.INFO} TRỰC NGÀY: ${info.trucNgay.name}")
                sb.appendLine("${i.STAR} Đánh giá: ${info.trucNgay.rating}")
                sb.toString().trim()
            }
            q.contains("sao chiếu") || q.contains("sao chieu") || q.contains("nhị thập bát tú") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.STAR} SAO CHIẾU: ${info.saoChieu.name}")
                sb.appendLine("${i.INFO} Đánh giá: ${info.saoChieu.rating}")
                sb.toString().trim()
            }

            // ── Pha trăng ──
            q.contains("trăng") || q.contains("moon") || q.contains("pha trăng") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.LUNAR} PHA TRĂNG HÔM NAY")
                sb.appendLine("${info.moonPhase.icon} ${info.moonPhase.name}")
                sb.appendLine("${i.INFO} Ngày trăng: ${info.moonPhase.age.toInt()}")
                if (info.isRam) sb.appendLine("${i.STAR} Hôm nay là ngày Rằm!")
                if (info.isMung1) sb.appendLine("${i.STAR} Hôm nay là ngày Mùng 1!")
                sb.toString().trim()
            }

            // ── Ngày lễ ──
            q.contains("lễ") || q.contains("holiday") || q.contains("ngày nghỉ") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} NGÀY LỄ")
                if (info.solarHoliday != null) sb.appendLine("${i.STAR} Dương lịch: ${info.solarHoliday}")
                if (info.lunarHoliday != null) sb.appendLine("${i.LUNAR} Âm lịch: ${info.lunarHoliday}")
                if (info.solarHoliday == null && info.lunarHoliday == null) {
                    sb.appendLine("${i.INFO} Hôm nay không có ngày lễ đặc biệt.")
                }
                sb.toString().trim()
            }

            // ── Âm lịch hôm nay ──
            q.contains("âm lịch") || q.contains("am lich") || q.contains("lunar") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.LUNAR} ÂM LỊCH HÔM NAY")
                sb.appendLine("${i.CALENDAR} Dương: ${info.solar.dd}/${info.solar.mm}/${info.solar.yy}")
                sb.appendLine("${i.LUNAR} Âm: ${info.lunar.day}/${info.lunar.month} năm ${info.yearCanChi}")
                if (info.lunar.leap == 1) sb.appendLine("${i.INFO} Tháng nhuận")
                if (info.isRam) sb.appendLine("${i.STAR} Ngày Rằm")
                if (info.isMung1) sb.appendLine("${i.STAR} Ngày Mùng 1")
                sb.toString().trim()
            }

            // ── Ngày mai ──
            q.contains("ngày mai") || q.contains("tomorrow") -> {
                val tmr = today.plusDays(1)
                val tmrInfo = dayInfoProvider.getDayInfo(tmr.dayOfMonth, tmr.monthValue, tmr.year)
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} Ngày mai — ${tmrInfo.solar.dd}/${tmrInfo.solar.mm}/${tmrInfo.solar.yy}")
                sb.appendLine("${i.LUNAR} Âm lịch: ${tmrInfo.lunar.day}/${tmrInfo.lunar.month}")
                sb.appendLine("${i.CANCHI} ${tmrInfo.dayCanChi} · ${tmrInfo.dayOfWeek}")
                if (tmrInfo.activities.isXauDay) sb.appendLine("${i.WARNING} Ngày xấu")
                sb.appendLine("${i.CHECK} Nên: ${tmrInfo.activities.nenLam.take(3).joinToString(", ")}")
                sb.appendLine("${i.CROSS} Không nên: ${tmrInfo.activities.khongNen.take(3).joinToString(", ")}")
                sb.appendLine("${i.CLOCK} Giờ tốt: ${tmrInfo.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.toString().trim()
            }

            // ── Cưới hỏi ──
            q.contains("cưới") || q.contains("hỏi") || q.contains("kết hôn") || q.contains("wedding") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.JOY} NGÀY CƯỚI HỎI — ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                val isGood = info.activities.nenLam.any { it.contains("cưới") || it.contains("hỏi") || it.contains("gả") }
                val isBad = info.activities.khongNen.any { it.contains("cưới") || it.contains("hỏi") || it.contains("gả") }
                when {
                    isGood -> sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP cho cưới hỏi!")
                    isBad -> sb.appendLine("${i.CROSS} Hôm nay KHÔNG PHÙ HỢP cho cưới hỏi.")
                    else -> sb.appendLine("${i.INFO} Hôm nay không đặc biệt tốt/xấu cho cưới hỏi.")
                }
                if (info.activities.isNguyetKy) sb.appendLine("${i.WARNING} Nguyệt Kỵ — nên tránh")
                if (info.activities.isTamNuong) sb.appendLine("${i.WARNING} Tam Nương — nên tránh")
                sb.appendLine()
                sb.appendLine("${i.COMPASS} Hướng Hỷ Thần: ${info.huong.hyThan}")
                sb.appendLine("${i.CLOCK} Giờ tốt: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Xem hợp tuổi vợ chồng")
                sb.appendLine("📌 Ngày cưới tốt tháng sau")
                sb.appendLine("📌 Hướng rước dâu tốt")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Khai trương ──
            q.contains("khai trương") || q.contains("mở hàng") || q.contains("kinh doanh") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.FORTUNE} KHAI TRƯƠNG — ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                val isGood = info.activities.nenLam.any { it.contains("khai trương") || it.contains("mở hàng") || it.contains("giao dịch") }
                when {
                    isGood -> sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP khai trương!")
                    else -> sb.appendLine("${i.INFO} Xem xét thêm trước khi khai trương hôm nay.")
                }
                sb.appendLine("${i.FORTUNE} Hướng Thần Tài: ${info.huong.thanTai}")
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Hướng Thần Tài hôm nay")
                sb.appendLine("📌 Ngày khai trương tốt tháng này")
                sb.appendLine("📌 Phong thủy cửa hàng theo mệnh")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Động thổ / xây nhà ──
            q.contains("động thổ") || q.contains("xây nhà") || q.contains("xây dựng") || q.contains("nhập trạch") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.INFO} ĐỘNG THỔ / XÂY DỰNG — ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                val isGood = info.activities.nenLam.any { it.contains("động thổ") || it.contains("xây dựng") || it.contains("sửa nhà") }
                when {
                    isGood -> sb.appendLine("${i.CHECK} Hôm nay PHÙ HỢP cho động thổ!")
                    else -> sb.appendLine("${i.INFO} Xem thêm chi tiết ngày trước khi quyết định.")
                }
                sb.appendLine("${i.CANCHI} Trực: ${info.trucNgay.name} (${info.trucNgay.rating})")
                sb.appendLine("${i.STAR} Sao: ${info.saoChieu.name} (${info.saoChieu.rating})")
                sb.appendLine("${i.COMPASS} Tránh hướng: ${info.huong.hungThan}")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Phong thủy nhà theo mệnh")
                sb.appendLine("📌 Hướng nhà tốt cho tuổi tôi")
                sb.appendLine("📌 Ngày nhập trạch tốt")
                sb.appendLine("~~~")
                sb.toString().trim()
            }

            // ── Đánh giá ngày ──
            q.contains("đánh giá") || q.contains("rating") || q.contains("điểm") || q.contains("tốt xấu") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.STAR} ĐÁNH GIÁ NGÀY — ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                sb.appendLine("${i.INFO} Tổng: ${info.dayRating.label} (${info.dayRating.percent}%)")
                sb.appendLine("${i.CANCHI} Trực: ${info.trucNgay.name} (${info.trucNgay.rating})")
                sb.appendLine("${i.STAR} Sao chiếu: ${info.saoChieu.name} (${info.saoChieu.rating})")
                if (info.activities.isXauDay) sb.appendLine("${i.WARNING} Ngày xấu")
                if (info.activities.isNguyetKy) sb.appendLine("${i.WARNING} Nguyệt Kỵ")
                if (info.activities.isTamNuong) sb.appendLine("${i.WARNING} Tam Nương")
                sb.toString().trim()
            }

            // ── Ngày cụ thể dd/mm ──
            q.matches(Regex(".*ngày\\s+\\d{1,2}/\\d{1,2}.*")) -> {
                val dateMatch = Regex("(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?").find(q)
                if (dateMatch != null) {
                    val dd = dateMatch.groupValues[1].toInt()
                    val mm = dateMatch.groupValues[2].toInt()
                    val yy = dateMatch.groupValues[3].let {
                        if (it.isBlank()) today.year else if (it.length == 2) 2000 + it.toInt() else it.toInt()
                    }
                    try {
                        val dateInfo = dayInfoProvider.getDayInfo(dd, mm, yy)
                        val sb = StringBuilder()
                        sb.appendLine("${i.CALENDAR} $dd/$mm/$yy · ${dateInfo.dayOfWeek}")
                        sb.appendLine("${i.LUNAR} Âm: ${dateInfo.lunar.day}/${dateInfo.lunar.month} ${dateInfo.yearCanChi}")
                        sb.appendLine("${i.CANCHI} ${dateInfo.dayCanChi}")
                        sb.appendLine()
                        if (dateInfo.activities.isXauDay) sb.appendLine("${i.WARNING} Ngày xấu")
                        sb.appendLine("${i.CHECK} Nên: ${dateInfo.activities.nenLam.take(4).joinToString(", ")}")
                        sb.appendLine("${i.CROSS} Không nên: ${dateInfo.activities.khongNen.take(4).joinToString(", ")}")
                        sb.appendLine("${i.CLOCK} Giờ tốt: ${dateInfo.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                        sb.toString().trim()
                    } catch (_: Exception) {
                        "${i.WARNING} Không thể tra cứu ngày $dd/$mm/$yy. Vui lòng kiểm tra lại."
                    }
                } else {
                    "${i.INFO} Không nhận diện được ngày. Hãy thử: \"ngày 25/12\""
                }
            }

            // ── Xin chào / giới thiệu ──
            q.matches(Regex("^(xin chào|chào|hi|hello|hey|alo).*")) -> {
                "${i.SPARKLE} Xin chào! Tôi là Lịch Số AI — trợ lý phong thuỷ & lịch vạn niên.\n\n" +
                "Bạn có thể hỏi về:\n" +
                "${i.ARROW} Ngày tốt/xấu hôm nay\n" +
                "${i.ARROW} Giờ hoàng đạo, hướng xuất hành\n" +
                "${i.ARROW} Can chi, tiết khí\n" +
                "${i.ARROW} Bát tự, tử vi, vận mệnh\n" +
                "${i.ARROW} Hợp tuổi, ngũ hành, phong thủy\n" +
                "${i.ARROW} Ngày cưới hỏi, khai trương\n" +
                "${i.ARROW} Tạo task, ghi chú, nhắc nhở\n\n" +
                "~~~gợi ý\n" +
                "📌 Xem tử vi hôm nay\n" +
                "📌 Phân tích bát tự của tôi\n" +
                "📌 Vận hạn năm nay\n" +
                "~~~"
            }

            // ── Cảm ơn ──
            q.matches(Regex("^(cảm ơn|cám ơn|thanks|thank you|tks|cam on).*")) -> {
                "Không có gì ạ! ${i.SPARKLE} Tôi luôn sẵn sàng hỗ trợ bạn. Hãy hỏi bất cứ khi nào nhé!"
            }

            // ── Trợ giúp / hướng dẫn ──
            q.matches(Regex(".*(?:giúp|help|hướng dẫn|guide|trợ giúp|làm gì được|biết gì).*")) -> {
                val sb = StringBuilder()
                sb.appendLine("${i.SPARKLE} TÔI CÓ THỂ GIÚP BẠN:")
                sb.appendLine()
                sb.appendLine("${i.CALENDAR} Lịch & Phong thuỷ:")
                sb.appendLine("${i.ARROW} \"Hôm nay ngày tốt không?\"")
                sb.appendLine("${i.ARROW} \"Giờ hoàng đạo\"")
                sb.appendLine("${i.ARROW} \"Hướng xuất hành\"")
                sb.appendLine("${i.ARROW} \"Ngày 25/12 thế nào?\"")
                sb.appendLine("${i.ARROW} \"Can chi hôm nay\"")
                sb.appendLine()
                sb.appendLine("${i.CHECK} Quản lý công việc:")
                sb.appendLine("${i.ARROW} \"Tạo task: Họp team lúc 2h\"")
                sb.appendLine("${i.ARROW} \"Ghi chú: Số điện thoại bác sĩ\"")
                sb.appendLine("${i.ARROW} \"Nhắc tôi uống thuốc lúc 8h sáng hàng ngày\"")
                sb.appendLine("${i.ARROW} \"Checklist: mua rau, mua thịt, mua cá\"")
                sb.appendLine("${i.ARROW} \"Xong task Họp team\"")
                sb.appendLine("${i.ARROW} \"Xoá task Họp team\"")
                sb.appendLine("${i.ARROW} \"Sửa task Họp team thành Họp client\"")
                sb.appendLine()
                sb.appendLine("${i.FORTUNE} Thống kê & Báo cáo:")
                sb.appendLine("${i.ARROW} \"Thống kê\" — tổng quan tất cả")
                sb.appendLine("${i.ARROW} \"Thống kê task\" — chi tiết công việc")
                sb.appendLine("${i.ARROW} \"Thống kê nhắc nhở\" — danh sách nhắc nhở")
                sb.appendLine("${i.ARROW} \"Báo cáo\" — tổng hợp đầy đủ")
                sb.appendLine()
                sb.appendLine("${i.INFO} Kế hoạch mẫu:")
                sb.appendLine("${i.ARROW} \"Kế hoạch ngày\" / \"Kế hoạch tuần\"")
                sb.appendLine("${i.ARROW} \"Kế hoạch tập gym\" / \"Kế hoạch du lịch\"")
                sb.toString().trim()
            }

            // ── Default ──
            else -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} ${info.solar.dd}/${info.solar.mm}/${info.solar.yy} · ${info.dayCanChi}")
                sb.appendLine("${i.LUNAR} Âm: ${info.lunar.day}/${info.lunar.month} ${info.yearCanChi}")
                sb.appendLine()
                sb.appendLine("Tôi chưa hiểu rõ câu hỏi. Bạn có thể thử:")
                sb.appendLine("${i.ARROW} Hỏi về ngày tốt/xấu, giờ hoàng đạo")
                sb.appendLine("${i.ARROW} Tạo task/ghi chú/nhắc nhở")
                sb.appendLine("${i.ARROW} Xem ngày cụ thể: \"ngày 25/12\"")
                sb.appendLine("${i.ARROW} Gõ \"giúp\" để xem tất cả chức năng")
                sb.appendLine()
                sb.appendLine("~~~gợi ý")
                sb.appendLine("📌 Hôm nay ngày tốt không")
                sb.appendLine("📌 Phân tích bát tự của tôi")
                sb.appendLine("📌 Xem tử vi hôm nay")
                sb.appendLine("~~~")
                sb.toString().trim()
            }
        }
    }
}
