package com.lichso.app.ui.screen.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.analytics.Analytics
import com.lichso.app.data.ai.AiTaskService
import com.lichso.app.data.ai.AiTemplates
import com.lichso.app.data.local.dao.ItemDao
import com.lichso.app.data.local.entity.ItemEntity
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.feature.points.domain.AwardPointsUseCase
import com.lichso.app.notification.NotificationScheduler
import com.lichso.app.util.SmartRatingManager
import com.lichso.app.util.stripHtml
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/** Bộ lọc cho danh sách hợp nhất. */
enum class ItemFilter { ALL, TASKS, REMINDERS, NOTES, PINNED }

data class ItemsUiState(
    val items: List<ItemEntity> = emptyList(),
    val filter: ItemFilter = ItemFilter.ALL,
    val query: String = "",
    val pendingTaskCount: Int = 0,
    val reminderCount: Int = 0,
    val totalCount: Int = 0,
    val deletingItem: ItemEntity? = null,
    val selectedTag: String? = null,
    // AI
    val isAiProcessing: Boolean = false,
    val aiMessage: String? = null,
    val showAiTemplates: Boolean = false,
    val aiError: String? = null,
) {
    /** Danh sách tất cả các tags duy nhất từ các item. */
    val allTags: List<String>
        get() = items.flatMap { item ->
            item.tags.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }.distinct().sorted()
    /** Danh sách đã áp bộ lọc + tìm kiếm (ghim luôn lên đầu trong từng nhóm). */
    val visibleItems: List<ItemEntity>
        get() {
            val q = query.trim().lowercase()
            return items.filter { item ->
                val matchFilter = when (filter) {
                    ItemFilter.ALL -> true
                    ItemFilter.TASKS -> item.isTask
                    ItemFilter.REMINDERS -> item.hasReminder
                    ItemFilter.NOTES -> !item.isTask && !item.hasReminder
                    ItemFilter.PINNED -> item.isPinned
                }
				val matchTag = selectedTag == null ||
					item.tags.split(",").map { it.trim().lowercase() }.contains(selectedTag.lowercase())

				val matchQuery = q.isBlank() ||
                    item.title.lowercase().contains(q) ||
                    item.description.stripHtml().lowercase().contains(q) ||
                    item.tags.lowercase().contains(q)
                matchFilter && matchTag && matchQuery
            }
        }
}

@HiltViewModel
class ItemsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val itemDao: ItemDao,
    private val aiTaskService: AiTaskService,
    private val awardPointsUseCase: AwardPointsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    private fun scheduleOrCancel(item: ItemEntity) {
        if (item.hasReminder && item.reminderEnabled) {
            NotificationScheduler.scheduleReminder(context, item)
        } else {
            NotificationScheduler.cancelReminder(context, item.id)
        }
    }

    init {
        viewModelScope.launch {
            itemDao.getAllItems().collect { list ->
                _uiState.update {
                    it.copy(
                        items = list,
                        pendingTaskCount = list.count { i -> i.isTask && !i.isDone },
                        reminderCount = list.count { i -> i.hasReminder && i.reminderEnabled },
                        totalCount = list.size,
                    )
                }
            }
        }
    }

    // ── Bộ lọc / tìm kiếm ──
    fun setFilter(filter: ItemFilter) = _uiState.update { it.copy(filter = filter) }
    fun setQuery(query: String) = _uiState.update { it.copy(query = query) }
    fun setSelectedTag(tag: String?) = _uiState.update { it.copy(selectedTag = tag) }

    // ── CRUD ──
    /** Tạo mới (id==0) hoặc cập nhật. Tự lên lịch / huỷ nhắc nhở. */
    fun saveItem(item: ItemEntity) {
        if (item.title.isBlank() && item.description.isBlank()) return
        viewModelScope.launch {
            val toSave = item.copy(
                title = item.title.ifBlank { item.description.stripHtml().take(50) },
                updatedAt = System.currentTimeMillis(),
            )
            val id = if (toSave.id == 0L) {
                val newId = itemDao.insert(toSave)
                SmartRatingManager.recordHappyAction(context)
                runCatching { awardPointsUseCase(ActionType.CREATE_REMINDER) }
                newId
            } else {
                itemDao.update(toSave)
                toSave.id
            }
            scheduleOrCancel(toSave.copy(id = id))
        }
    }

    fun toggleDone(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.toggleDone(item.id, !item.isDone)
            if (!item.isDone) {
                SmartRatingManager.recordHappyAction(context)
                runCatching { awardPointsUseCase(ActionType.COMPLETE_REMINDER) }
            }
        }
    }

    fun togglePin(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.update(item.copy(isPinned = !item.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleReminder(item: ItemEntity) {
        viewModelScope.launch {
            val newEnabled = !item.reminderEnabled
            itemDao.toggleReminderEnabled(item.id, newEnabled)
            scheduleOrCancel(item.copy(reminderEnabled = newEnabled))
        }
    }

    fun requestDelete(item: ItemEntity) = _uiState.update { it.copy(deletingItem = item) }
    fun cancelDelete() = _uiState.update { it.copy(deletingItem = null) }

    fun confirmDelete() {
        val item = _uiState.value.deletingItem ?: return
        viewModelScope.launch {
            itemDao.delete(item)
            if (item.hasReminder) NotificationScheduler.cancelReminder(context, item.id)
        }
        _uiState.update { it.copy(deletingItem = null) }
    }

    suspend fun getItem(id: Long): ItemEntity? = itemDao.getById(id)

    // ── Helpers ──
    fun formatDateTime(millis: Long): String =
        SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault()).format(Date(millis))

    fun formatDate(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))

    fun getPriorityLabel(priority: Int): String = when (priority) {
        2 -> "Cao"; 1 -> "Vừa"; else -> "Thấp"
    }

    // ════════════════════════════════════════════
    // AI — map kết quả AiTaskService → ItemEntity
    // ════════════════════════════════════════════

    private fun aiItemToEntity(it: AiTaskService.AiItem): ItemEntity = when (it.type) {
        "reminder" -> ItemEntity(
            title = it.title,
            description = it.description,
            hasReminder = true,
            reminderAt = parseTimeToMillis(it.time),
            repeatType = it.repeatType,
        )
        "task" -> ItemEntity(
            title = it.title,
            description = it.description,
            isTask = true,
            priority = it.priority,
            dueDate = it.dueDate?.let { d -> parseDateToMillis(d) },
        )
        else -> ItemEntity( // note
            title = it.title,
            description = it.description,
            colorIndex = it.colorIndex,
        )
    }

    fun processAiCommand(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiProcessing = true, aiMessage = null, aiError = null) }
            try {
                val ctx = buildExistingContext()
                val result = aiTaskService.processCommand(input, ctx)
                when (result.action) {
                    "create_task", "bulk_create", "create_note", "create_reminder" -> {
                        result.items.forEach { item ->
                            val entity = aiItemToEntity(item)
                            val id = itemDao.insert(entity)
                            if (entity.hasReminder) scheduleOrCancel(entity.copy(id = id))
                        }
                        runCatching { awardPointsUseCase(ActionType.CREATE_REMINDER) }
                    }
                    "delete_task", "delete_note", "delete_reminder" -> {
                        result.items.forEach { item ->
                            val kw = item.title.lowercase()
                            _uiState.value.items.find { it.title.lowercase().contains(kw) }?.let { found ->
                                itemDao.delete(found)
                                if (found.hasReminder) NotificationScheduler.cancelReminder(context, found.id)
                            }
                        }
                    }
                    "edit_task", "edit_note" -> {
                        result.items.forEach { item ->
                            val old = item.description.removePrefix("old_title:").lowercase()
                            _uiState.value.items.find { it.title.lowercase().contains(old) }?.let { found ->
                                itemDao.update(found.copy(title = item.title, updatedAt = System.currentTimeMillis()))
                            }
                        }
                    }
                    "edit_task_priority" -> {
                        result.items.forEach { item ->
                            val kw = item.title.lowercase()
                            _uiState.value.items.find { it.title.lowercase().contains(kw) }?.let { found ->
                                itemDao.update(found.copy(priority = item.priority, isTask = true, updatedAt = System.currentTimeMillis()))
                            }
                        }
                    }
                    "mark_done" -> {
                        result.items.forEach { item ->
                            val kw = item.title.lowercase()
                            _uiState.value.items.find { it.title.lowercase().contains(kw) && !it.isDone }?.let { found ->
                                itemDao.toggleDone(found.id, true)
                            }
                        }
                    }
                    "stats_all", "stats_tasks", "stats_notes", "stats_reminders" -> {
                        _uiState.update { it.copy(isAiProcessing = false, aiMessage = buildQuickStats(result.action)) }
                        return@launch
                    }
                }
                _uiState.update { it.copy(isAiProcessing = false, aiMessage = result.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiProcessing = false, aiError = "Có lỗi xảy ra: ${e.message}") }
            }
        }
    }

    fun executeTemplate(template: AiTemplates.QuickTemplate) = processAiCommand(template.prompt)
    fun toggleAiTemplates() = _uiState.update { it.copy(showAiTemplates = !it.showAiTemplates) }
    fun dismissAiMessage() = _uiState.update { it.copy(aiMessage = null, aiError = null) }

    private fun buildExistingContext(): String {
        val items = _uiState.value.items
        if (items.isEmpty()) return ""
        val sb = StringBuilder("Các mục hiện tại (${items.size}):\n")
        items.take(15).forEach { i ->
            val kind = when {
                i.isTask -> if (i.isDone) "việc-xong" else "việc"
                i.hasReminder -> "nhắc"
                else -> "ghi-chú"
            }
            sb.appendLine("- [id:${i.id}] ${i.title} ($kind)")
        }
        return sb.toString()
    }

    private fun buildQuickStats(action: String): String {
        val items = _uiState.value.items
        val tasks = items.filter { it.isTask }
        val reminders = items.filter { it.hasReminder }
        val notes = items.filter { !it.isTask && !it.hasReminder }
        val sb = StringBuilder()
        if (action == "stats_tasks" || action == "stats_all") {
            val done = tasks.count { it.isDone }
            sb.appendLine("✦ CÔNG VIỆC — Tổng ${tasks.size} · ✅ $done · ⬜ ${tasks.size - done}")
        }
        if (action == "stats_notes" || action == "stats_all") {
            sb.appendLine("◇ GHI CHÚ — ${notes.size} mục")
        }
        if (action == "stats_reminders" || action == "stats_all") {
            val on = reminders.count { it.reminderEnabled }
            sb.appendLine("◷ NHẮC NHỞ — Tổng ${reminders.size} · ✅ Bật $on")
        }
        if (action == "stats_all") sb.appendLine("\n⊛ Tổng cộng: ${items.size} mục")
        return sb.toString().trim()
    }

    private fun parseDateToMillis(dateStr: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time
    } catch (_: Exception) { null }

    private fun parseTimeToMillis(timeStr: String?): Long {
        val cal = Calendar.getInstance()
        if (timeStr != null) try {
            val parts = timeStr.split(":")
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt().coerceIn(0, 23))
            cal.set(Calendar.MINUTE, parts.getOrElse(1) { "0" }.toInt().coerceIn(0, 59))
            cal.set(Calendar.SECOND, 0)
        } catch (_: Exception) {}
        return cal.timeInMillis
    }
}
