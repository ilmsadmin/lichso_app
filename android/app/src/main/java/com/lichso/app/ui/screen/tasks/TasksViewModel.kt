package com.lichso.app.ui.screen.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.ai.AiTaskService
import com.lichso.app.data.ai.AiTemplates
import com.lichso.app.data.local.dao.NoteDao
import com.lichso.app.data.local.dao.ReminderDao
import com.lichso.app.data.local.dao.TaskDao
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val taskCount: Int = 0,
    val reminderCount: Int = 0,
    val noteCount: Int = 0,
    val showAddTaskDialog: Boolean = false,
    val showAddNoteDialog: Boolean = false,
    val showAddReminderDialog: Boolean = false,
    // Edit
    val editingTask: TaskEntity? = null,
    val editingNote: NoteEntity? = null,
    val editingReminder: ReminderEntity? = null,
    // Delete confirmation
    val deletingTask: TaskEntity? = null,
    val deletingNote: NoteEntity? = null,
    val deletingReminder: ReminderEntity? = null,
    // AI
    val isAiProcessing: Boolean = false,
    val aiMessage: String? = null,
    val showAiTemplates: Boolean = false,
    val aiError: String? = null
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao,
    private val aiTaskService: AiTaskService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskDao.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(tasks = tasks, taskCount = tasks.count { t -> !t.isDone }) }
            }
        }
        viewModelScope.launch {
            noteDao.getAllNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes, noteCount = notes.size) }
            }
        }
        viewModelScope.launch {
            reminderDao.getAllReminders().collect { reminders ->
                _uiState.update { it.copy(reminders = reminders, reminderCount = reminders.count { r -> r.isEnabled }) }
            }
        }
    }

    // ---- Tasks ----
    fun addTask(title: String, priority: Int, dueDate: Long? = null, description: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.toggleDone(task.id, !task.isDone)
        }
    }

    fun requestDeleteTask(task: TaskEntity) {
        _uiState.update { it.copy(deletingTask = task) }
    }

    fun confirmDeleteTask() {
        val task = _uiState.value.deletingTask ?: return
        viewModelScope.launch { taskDao.delete(task) }
        _uiState.update { it.copy(deletingTask = null) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deletingTask = null, deletingNote = null, deletingReminder = null) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }

    fun showAddTask(show: Boolean) {
        _uiState.update { it.copy(showAddTaskDialog = show) }
    }

    fun startEditTask(task: TaskEntity) {
        _uiState.update { it.copy(editingTask = task) }
    }

    fun dismissEditTask() {
        _uiState.update { it.copy(editingTask = null) }
    }

    // ---- Notes ----
    fun addNote(title: String, content: String, colorIndex: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            noteDao.insert(NoteEntity(title = title, content = content, colorIndex = colorIndex))
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun requestDeleteNote(note: NoteEntity) {
        _uiState.update { it.copy(deletingNote = note) }
    }

    fun confirmDeleteNote() {
        val note = _uiState.value.deletingNote ?: return
        viewModelScope.launch { noteDao.delete(note) }
        _uiState.update { it.copy(deletingNote = null) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }

    fun showAddNote(show: Boolean) {
        _uiState.update { it.copy(showAddNoteDialog = show) }
    }

    fun startEditNote(note: NoteEntity) {
        _uiState.update { it.copy(editingNote = note) }
    }

    fun dismissEditNote() {
        _uiState.update { it.copy(editingNote = null) }
    }

    // ---- Reminders ----
    fun addReminder(title: String, triggerTime: Long, repeatType: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            reminderDao.insert(
                ReminderEntity(title = title, triggerTime = triggerTime, repeatType = repeatType)
            )
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.update(reminder)
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.toggleEnabled(reminder.id, !reminder.isEnabled)
        }
    }

    fun requestDeleteReminder(reminder: ReminderEntity) {
        _uiState.update { it.copy(deletingReminder = reminder) }
    }

    fun confirmDeleteReminder() {
        val reminder = _uiState.value.deletingReminder ?: return
        viewModelScope.launch { reminderDao.delete(reminder) }
        _uiState.update { it.copy(deletingReminder = null) }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.delete(reminder)
        }
    }

    fun showAddReminder(show: Boolean) {
        _uiState.update { it.copy(showAddReminderDialog = show) }
    }

    fun startEditReminder(reminder: ReminderEntity) {
        _uiState.update { it.copy(editingReminder = reminder) }
    }

    fun dismissEditReminder() {
        _uiState.update { it.copy(editingReminder = null) }
    }

    // ---- Helpers ----
    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateFull(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun getPriorityLabel(priority: Int): String = when (priority) {
        2 -> "Cao"
        1 -> "Vừa"
        else -> "Thấp"
    }

    // ════════════════════════════
    // AI Functions
    // ════════════════════════════

    fun processAiCommand(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiProcessing = true, aiMessage = null, aiError = null) }

            try {
                val context = buildExistingContext()
                val result = aiTaskService.processCommand(input, context)

                // Execute actions based on AI result
                when (result.action) {
                    "create_task", "bulk_create" -> {
                        result.items.filter { it.type == "task" }.forEach { item ->
                            val dueDate = item.dueDate?.let { parseDateToMillis(it) }
                            taskDao.insert(
                                TaskEntity(
                                    title = item.title,
                                    description = item.description,
                                    priority = item.priority,
                                    dueDate = dueDate
                                )
                            )
                        }
                        result.items.filter { it.type == "note" }.forEach { item ->
                            noteDao.insert(
                                NoteEntity(
                                    title = item.title,
                                    content = item.description,
                                    colorIndex = item.colorIndex
                                )
                            )
                        }
                        result.items.filter { it.type == "reminder" }.forEach { item ->
                            val triggerTime = parseTimeToMillis(item.time)
                            reminderDao.insert(
                                ReminderEntity(
                                    title = item.title,
                                    triggerTime = triggerTime,
                                    repeatType = item.repeatType
                                )
                            )
                        }
                    }
                    "create_note" -> {
                        result.items.forEach { item ->
                            noteDao.insert(
                                NoteEntity(
                                    title = item.title,
                                    content = item.description,
                                    colorIndex = item.colorIndex
                                )
                            )
                        }
                    }
                    "create_reminder" -> {
                        result.items.forEach { item ->
                            val triggerTime = parseTimeToMillis(item.time)
                            reminderDao.insert(
                                ReminderEntity(
                                    title = item.title,
                                    triggerTime = triggerTime,
                                    repeatType = item.repeatType
                                )
                            )
                        }
                    }
                    "delete_task" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.tasks.find { t ->
                                t.title.lowercase().contains(keyword)
                            }?.let { found -> taskDao.delete(found) }
                        }
                    }
                    "delete_note" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.notes.find { n ->
                                n.title.lowercase().contains(keyword)
                            }?.let { found -> noteDao.delete(found) }
                        }
                    }
                    "delete_reminder" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.reminders.find { r ->
                                r.title.lowercase().contains(keyword)
                            }?.let { found -> reminderDao.delete(found) }
                        }
                    }
                    "edit_task" -> {
                        result.items.forEach { item ->
                            val oldTitle = item.description.removePrefix("old_title:").lowercase()
                            _uiState.value.tasks.find { t ->
                                t.title.lowercase().contains(oldTitle)
                            }?.let { found ->
                                taskDao.update(found.copy(title = item.title, updatedAt = System.currentTimeMillis()))
                            }
                        }
                    }
                    "edit_note" -> {
                        result.items.forEach { item ->
                            val oldTitle = item.description.removePrefix("old_title:").lowercase()
                            _uiState.value.notes.find { n ->
                                n.title.lowercase().contains(oldTitle)
                            }?.let { found ->
                                noteDao.update(found.copy(title = item.title, updatedAt = System.currentTimeMillis()))
                            }
                        }
                    }
                    "edit_task_priority" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.tasks.find { t ->
                                t.title.lowercase().contains(keyword)
                            }?.let { found ->
                                taskDao.update(found.copy(priority = item.priority, updatedAt = System.currentTimeMillis()))
                            }
                        }
                    }
                    "edit_reminder" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.reminders.find { r ->
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
                        }
                    }
                    "mark_done" -> {
                        result.items.forEach { item ->
                            val keyword = item.title.lowercase()
                            _uiState.value.tasks.find { t ->
                                t.title.lowercase().contains(keyword) && !t.isDone
                            }?.let { found ->
                                taskDao.toggleDone(found.id, true)
                            }
                        }
                    }
                    "stats_all", "stats_tasks", "stats_notes", "stats_reminders" -> {
                        // Build stats message from current state
                        val statsMsg = buildQuickStats(result.action)
                        _uiState.update { it.copy(isAiProcessing = false, aiMessage = statsMsg) }
                        return@launch
                    }
                }

                _uiState.update { it.copy(isAiProcessing = false, aiMessage = result.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiProcessing = false,
                        aiError = "Có lỗi xảy ra: ${e.message}"
                    )
                }
            }
        }
    }

    fun executeTemplate(template: AiTemplates.QuickTemplate) {
        processAiCommand(template.prompt)
    }

    fun toggleAiTemplates() {
        _uiState.update { it.copy(showAiTemplates = !it.showAiTemplates) }
    }

    fun dismissAiMessage() {
        _uiState.update { it.copy(aiMessage = null, aiError = null) }
    }

    private fun buildExistingContext(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        if (state.tasks.isNotEmpty()) {
            sb.appendLine("Tasks hiện tại (${state.tasks.size}):")
            state.tasks.take(10).forEach { t ->
                sb.appendLine("- [id:${t.id}] ${t.title} (${if (t.isDone) "done" else "pending"}, priority:${t.priority})")
            }
        }
        if (state.notes.isNotEmpty()) {
            sb.appendLine("Notes hiện tại (${state.notes.size}):")
            state.notes.take(5).forEach { n ->
                sb.appendLine("- [id:${n.id}] ${n.title}")
            }
        }
        if (state.reminders.isNotEmpty()) {
            sb.appendLine("Reminders hiện tại (${state.reminders.size}):")
            state.reminders.take(5).forEach { r ->
                sb.appendLine("- [id:${r.id}] ${r.title} (${if (r.isEnabled) "on" else "off"})")
            }
        }
        return sb.toString()
    }

    private fun buildQuickStats(action: String): String {
        val state = _uiState.value
        val sb = StringBuilder()

        when (action) {
            "stats_tasks", "stats_all" -> {
                val total = state.tasks.size
                val done = state.tasks.count { it.isDone }
                val pending = total - done
                val high = state.tasks.count { it.priority == 2 && !it.isDone }
                val med = state.tasks.count { it.priority == 1 && !it.isDone }
                val low = state.tasks.count { it.priority == 0 && !it.isDone }
                sb.appendLine("✦ THỐNG KÊ CÔNG VIỆC")
                sb.appendLine("── Tổng: $total | ✅ Xong: $done | ⬜ Chưa xong: $pending")
                if (total > 0) sb.appendLine("⟡ Tiến độ: ${(done * 100) / total}%")
                sb.appendLine("› Ưu tiên chưa xong: 🔴 Cao: $high · 🟡 Vừa: $med · 🟢 Thấp: $low")
            }
        }
        when (action) {
            "stats_notes", "stats_all" -> {
                if (sb.isNotEmpty()) sb.appendLine()
                sb.appendLine("◇ THỐNG KÊ GHI CHÚ")
                sb.appendLine("── Tổng: ${state.notes.size} ghi chú 📝")
            }
        }
        when (action) {
            "stats_reminders", "stats_all" -> {
                if (sb.isNotEmpty()) sb.appendLine()
                val enabled = state.reminders.count { it.isEnabled }
                val disabled = state.reminders.size - enabled
                sb.appendLine("◷ THỐNG KÊ NHẮC NHỞ")
                sb.appendLine("── Tổng: ${state.reminders.size} | ✅ Bật: $enabled | ⬜ Tắt: $disabled")
            }
        }
        if (action == "stats_all") {
            sb.appendLine()
            val total = state.tasks.size + state.notes.size + state.reminders.size
            sb.appendLine("⊛ Tổng cộng: $total mục")
        }
        return sb.toString().trim()
    }

    private fun parseDateToMillis(dateStr: String): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateStr)?.time
        } catch (e: Exception) { null }
    }

    private fun parseTimeToMillis(timeStr: String?): Long {
        val cal = Calendar.getInstance()
        if (timeStr != null) {
            try {
                val parts = timeStr.split(":")
                cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt().coerceIn(0, 23))
                cal.set(Calendar.MINUTE, parts.getOrElse(1) { "0" }.toInt().coerceIn(0, 59))
                cal.set(Calendar.SECOND, 0)
            } catch (_: Exception) { }
        }
        return cal.timeInMillis
    }
}
