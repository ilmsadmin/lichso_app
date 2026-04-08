package com.lichso.app.ui.screen.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ══════════════════════════════════════════
// Colors matching HTML mockup
// ══════════════════════════════════════════
private val EditNoteBlue = Color(0xFF1565C0)
private val EditTaskGreen = Color(0xFF2E7D32)
private val EditRemindOrange = Color(0xFFE65100)

enum class EditItemType { NOTE, TASK, REMIND }

// ══════════════════════════════════════════
// Data class for checklist items
// ══════════════════════════════════════════
data class ChecklistItem(val text: String, val isDone: Boolean)

// ══════════════════════════════════════════
// Data class for attached date info
// ══════════════════════════════════════════
data class AttachedDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val lunarInfo: String = "",
    val holiday: String = ""
)

// ══════════════════════════════════════════
// Note/Task/Remind Edit Screen
// (matching screen-note-task-edit.html)
// ══════════════════════════════════════════

@Composable
fun NoteTaskEditScreen(
    initialType: EditItemType = EditItemType.NOTE,
    editNote: NoteEntity? = null,
    editTask: TaskEntity? = null,
    editReminder: ReminderEntity? = null,
    prefillNote: NoteEntity? = null,
    prefillReminder: ReminderEntity? = null,
    attachedDate: AttachedDate? = null,
    onBackClick: () -> Unit = {},
    onSaveNote: (NoteEntity) -> Unit = {},
    onSaveTask: (TaskEntity) -> Unit = {},
    onSaveReminder: (ReminderEntity) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val c = LichSoThemeColors.current
    var selectedType by remember { mutableStateOf(initialType) }
    val isEditing = editNote != null || editTask != null || editReminder != null

    // Merge: edit values take priority, then prefill, then defaults
    val noteSource = editNote ?: prefillNote
    val reminderSource = editReminder ?: prefillReminder

    // ── Attached date state ──
    var currentAttachedDate by remember { mutableStateOf(attachedDate) }

    // ── Note state ──
    var noteTitle by remember { mutableStateOf(noteSource?.title ?: "") }
    var noteContent by remember { mutableStateOf(noteSource?.content ?: "") }
    var noteColorIndex by remember { mutableIntStateOf(noteSource?.colorIndex ?: 0) }
    var isPinned by remember { mutableStateOf(noteSource?.isPinned ?: false) }
    var noteChecklistItems by remember {
        mutableStateOf<List<ChecklistItem>>(
            if (noteSource != null && noteSource.content.contains("\n")) {
                val lines = noteSource.content.split("\n")
                val checkItems = lines.filter { it.startsWith("[x] ") || it.startsWith("[ ] ") }
                if (checkItems.isNotEmpty()) {
                    checkItems.map {
                        val done = it.startsWith("[x] ")
                        val text = it.removePrefix("[x] ").removePrefix("[ ] ")
                        ChecklistItem(text = text, isDone = done)
                    }
                } else emptyList()
            } else emptyList()
        )
    }

    // ── Labels state ──
    val allLabels = remember {
        listOf("Gia đình", "Quan trọng", "Công việc", "Cá nhân", "Học tập", "Sức khỏe")
    }
    var selectedLabels by remember {
        val initialLabels = when {
            noteSource != null && noteSource.labels.isNotBlank() -> noteSource.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            editTask != null && editTask.labels.isNotBlank() -> editTask.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            reminderSource != null && reminderSource.labels.isNotBlank() -> reminderSource.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            else -> emptySet()
        }
        mutableStateOf(initialLabels)
    }

    // ── Task state ──
    var taskTitle by remember { mutableStateOf(editTask?.title ?: "") }
    var taskDescription by remember { mutableStateOf(editTask?.description ?: "") }
    var taskPriority by remember { mutableIntStateOf(editTask?.priority ?: 1) }
    var taskDueDate by remember { mutableStateOf(editTask?.dueDate) }
    var taskDueTime by remember { mutableStateOf(editTask?.dueTime ?: "") }
    var taskReminderEnabled by remember { mutableStateOf(editTask?.hasReminder ?: false) }
    var taskChecklistItems by remember {
        mutableStateOf(
            if (editTask != null && editTask.description.contains("\n")) {
                editTask.description.split("\n").filter { it.isNotBlank() }.map {
                    val done = it.startsWith("[x] ")
                    val text = it.removePrefix("[x] ").removePrefix("[ ] ")
                    ChecklistItem(text = text, isDone = done)
                }
            } else {
                listOf(ChecklistItem("", false))
            }
        )
    }

    // ── Reminder state ──
    var reminderTitle by remember { mutableStateOf(reminderSource?.title ?: "") }
    var reminderDate by remember {
        mutableStateOf(
            reminderSource?.triggerTime?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
            } ?: ""
        )
    }
    var reminderTime by remember {
        mutableStateOf(
            reminderSource?.triggerTime?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
            } ?: "07:00"
        )
    }
    var reminderRepeatType by remember { mutableIntStateOf(reminderSource?.repeatType ?: 0) }
    var reminderEnabled by remember { mutableStateOf(reminderSource?.isEnabled ?: true) }
    var reminderUseLunar by remember { mutableStateOf(reminderSource?.useLunar ?: false) }
    var reminderAdvanceDays by remember { mutableIntStateOf(reminderSource?.advanceDays ?: 0) }
    var reminderCategory by remember { mutableIntStateOf(reminderSource?.category ?: 0) }
    var reminderNotes by remember { mutableStateOf(reminderSource?.subtitle ?: "") }

    val topTitle = when {
        isEditing && selectedType == EditItemType.NOTE -> "Sửa ghi chú"
        isEditing && selectedType == EditItemType.TASK -> "Sửa công việc"
        isEditing && selectedType == EditItemType.REMIND -> "Sửa nhắc nhở"
        selectedType == EditItemType.NOTE -> "Thêm ghi chú"
        selectedType == EditItemType.TASK -> "Thêm công việc"
        else -> "Thêm nhắc nhở"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ═══ TOP BAR ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = c.textPrimary)
            }
            Text(
                topTitle,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.weight(1f)
            )

            // Pin (note only)
            AnimatedVisibility(visible = selectedType == EditItemType.NOTE) {
                IconButton(onClick = { isPinned = !isPinned }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Ghim",
                        tint = if (isPinned) c.primary else c.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Delete button (when editing)
            if (isEditing) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Xóa", tint = c.textSecondary, modifier = Modifier.size(22.dp))
                }
            }

            // Save button
            Button(
                onClick = {
                    when (selectedType) {
                        EditItemType.NOTE -> {
                            val finalColor = noteColorIndex
                            val checklistStr = if (noteChecklistItems.isNotEmpty()) {
                                noteChecklistItems.filter { it.text.isNotBlank() }
                                    .joinToString("\n") {
                                        if (it.isDone) "[x] ${it.text}" else "[ ] ${it.text}"
                                    }
                            } else ""
                            val finalContent = if (checklistStr.isNotBlank()) {
                                if (noteContent.isNotBlank()) "$noteContent\n$checklistStr" else checklistStr
                            } else noteContent
                            val labelsStr = selectedLabels.joinToString(",")
                            val note = editNote?.copy(
                                title = noteTitle,
                                content = finalContent,
                                colorIndex = finalColor,
                                isPinned = isPinned,
                                labels = labelsStr,
                                updatedAt = System.currentTimeMillis()
                            ) ?: NoteEntity(
                                title = noteTitle,
                                content = finalContent,
                                colorIndex = finalColor,
                                isPinned = isPinned,
                                labels = labelsStr
                            )
                            onSaveNote(note)
                        }
                        EditItemType.TASK -> {
                            val desc = taskChecklistItems
                                .filter { it.text.isNotBlank() }
                                .joinToString("\n") {
                                    if (it.isDone) "[x] ${it.text}" else "[ ] ${it.text}"
                                }
                            val labelsStr = selectedLabels.joinToString(",")
                            val task = editTask?.copy(
                                title = taskTitle,
                                description = if (desc.isNotBlank()) desc else taskDescription,
                                priority = taskPriority,
                                dueDate = taskDueDate,
                                dueTime = taskDueTime.ifEmpty { null },
                                labels = labelsStr,
                                hasReminder = taskReminderEnabled,
                                updatedAt = System.currentTimeMillis()
                            ) ?: TaskEntity(
                                title = taskTitle,
                                description = if (desc.isNotBlank()) desc else taskDescription,
                                priority = taskPriority,
                                dueDate = taskDueDate,
                                dueTime = taskDueTime.ifEmpty { null },
                                labels = labelsStr,
                                hasReminder = taskReminderEnabled
                            )
                            onSaveTask(task)
                        }
                        EditItemType.REMIND -> {
                            val trigger = parseDateTimeToMillis(reminderDate, reminderTime)
                            val labelsStr = selectedLabels.joinToString(",")
                            val reminder = editReminder?.copy(
                                title = reminderTitle,
                                subtitle = reminderNotes,
                                triggerTime = trigger,
                                repeatType = reminderRepeatType,
                                isEnabled = reminderEnabled,
                                useLunar = reminderUseLunar,
                                advanceDays = reminderAdvanceDays,
                                category = reminderCategory,
                                labels = labelsStr
                            ) ?: ReminderEntity(
                                title = reminderTitle,
                                subtitle = reminderNotes,
                                triggerTime = trigger,
                                repeatType = reminderRepeatType,
                                isEnabled = reminderEnabled,
                                useLunar = reminderUseLunar,
                                advanceDays = reminderAdvanceDays,
                                category = reminderCategory,
                                labels = labelsStr
                            )
                            onSaveReminder(reminder)
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Text("Lưu", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            }
        }

        // ═══ TYPE SELECTOR (always visible) ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TypeChip(
                icon = Icons.AutoMirrored.Outlined.StickyNote2,
                label = "Ghi chú",
                isActive = selectedType == EditItemType.NOTE,
                activeColor = EditNoteBlue,
                modifier = Modifier.weight(1f),
                onClick = { selectedType = EditItemType.NOTE }
            )
            TypeChip(
                icon = Icons.Outlined.Checklist,
                label = "Công việc",
                isActive = selectedType == EditItemType.TASK,
                activeColor = EditTaskGreen,
                modifier = Modifier.weight(1f),
                onClick = { selectedType = EditItemType.TASK }
            )
            TypeChip(
                icon = Icons.Outlined.Alarm,
                label = "Nhắc nhở",
                isActive = selectedType == EditItemType.REMIND,
                activeColor = EditRemindOrange,
                modifier = Modifier.weight(1f),
                onClick = { selectedType = EditItemType.REMIND }
            )
        }

        // ═══ DATE ATTACHMENT (if set) ═══
        AnimatedVisibility(visible = currentAttachedDate != null) {
            currentAttachedDate?.let { dateInfo ->
                DateAttachmentCard(
                    date = dateInfo,
                    onRemove = { currentAttachedDate = null }
                )
            }
        }

        // ═══ SCROLLABLE CONTENT ═══
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ── Shared Title ──
            val currentTitle = when (selectedType) {
                EditItemType.NOTE -> noteTitle
                EditItemType.TASK -> taskTitle
                EditItemType.REMIND -> reminderTitle
            }
            val titlePlaceholder = when (selectedType) {
                EditItemType.NOTE -> "Tiêu đề..."
                EditItemType.TASK -> "Tên công việc..."
                EditItemType.REMIND -> "Nhắc tôi về..."
            }
            BasicTextField(
                value = currentTitle,
                onValueChange = { newVal ->
                    when (selectedType) {
                        EditItemType.NOTE -> noteTitle = newVal
                        EditItemType.TASK -> taskTitle = newVal
                        EditItemType.REMIND -> reminderTitle = newVal
                    }
                },
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    lineHeight = 30.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                decorationBox = { innerTextField ->
                    if (currentTitle.isEmpty()) {
                        Text(
                            titlePlaceholder,
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.outlineVariant)
                        )
                    }
                    innerTextField()
                }
            )

            // ── Labels row ──
            LabelRow(
                allLabels = allLabels,
                selectedLabels = selectedLabels,
                onToggleLabel = { label ->
                    selectedLabels = if (label in selectedLabels)
                        selectedLabels - label
                    else
                        selectedLabels + label
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Panel content ──
            when (selectedType) {
                EditItemType.NOTE -> NoteEditPanel(
                    content = noteContent,
                    checklistItems = noteChecklistItems,
                    onContentChange = { noteContent = it },
                    onChecklistChange = { noteChecklistItems = it }
                )
                EditItemType.TASK -> TaskEditPanel(
                    description = taskDescription,
                    priority = taskPriority,
                    dueDate = taskDueDate,
                    dueTime = taskDueTime,
                    checklistItems = taskChecklistItems,
                    reminderEnabled = taskReminderEnabled,
                    onDescriptionChange = { taskDescription = it },
                    onPriorityChange = { taskPriority = it },
                    onDueDateChange = { taskDueDate = it },
                    onDueTimeChange = { taskDueTime = it },
                    onChecklistChange = { taskChecklistItems = it },
                    onReminderEnabledChange = { taskReminderEnabled = it }
                )
                EditItemType.REMIND -> ReminderEditPanel(
                    date = reminderDate,
                    time = reminderTime,
                    repeatType = reminderRepeatType,
                    isEnabled = reminderEnabled,
                    useLunar = reminderUseLunar,
                    advanceDays = reminderAdvanceDays,
                    category = reminderCategory,
                    notes = reminderNotes,
                    onDateChange = { reminderDate = it },
                    onTimeChange = { reminderTime = it },
                    onRepeatChange = { reminderRepeatType = it },
                    onEnabledChange = { reminderEnabled = it },
                    onUseLunarChange = { reminderUseLunar = it },
                    onAdvanceDaysChange = { reminderAdvanceDays = it },
                    onCategoryChange = { reminderCategory = it },
                    onNotesChange = { reminderNotes = it }
                )
            }
        }

        // ═══ BOTTOM TOOLBAR (Note only) ═══
        AnimatedVisibility(visible = selectedType == EditItemType.NOTE) {
            NoteColorToolbar(
                selectedColorIndex = noteColorIndex,
                onColorSelected = { noteColorIndex = it },
                hasChecklist = noteChecklistItems.isNotEmpty(),
                onToggleChecklist = {
                    noteChecklistItems = if (noteChecklistItems.isEmpty()) {
                        listOf(ChecklistItem("", false))
                    } else {
                        emptyList()
                    }
                },
                onInsertBold = {
                    noteContent = if (noteContent.isNotBlank()) "$noteContent\n**in đậm**" else "**in đậm**"
                },
                onInsertList = {
                    noteContent = if (noteContent.isNotBlank()) "$noteContent\n• " else "• "
                },
                onAttachDate = {
                    if (currentAttachedDate == null) {
                        val cal = Calendar.getInstance()
                        currentAttachedDate = AttachedDate(
                            day = cal.get(Calendar.DAY_OF_MONTH),
                            month = cal.get(Calendar.MONTH) + 1,
                            year = cal.get(Calendar.YEAR)
                        )
                    } else {
                        currentAttachedDate = null
                    }
                },
                hasAttachedDate = currentAttachedDate != null
            )
        }
    }
}

// ══════════════════════════════════════════
// Date Attachment Card
// ══════════════════════════════════════════

@Composable
private fun DateAttachmentCard(
    date: AttachedDate,
    onRemove: () -> Unit
) {
    val c = LichSoThemeColors.current
    val monthNames = listOf("", "Th1","Th2","Th3","Th4","Th5","Th6","Th7","Th8","Th9","Th10","Th11","Th12")
    val dayOfWeek = remember(date) {
        val cal = Calendar.getInstance().apply { set(date.year, date.month - 1, date.day) }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Thứ Hai"
            Calendar.TUESDAY -> "Thứ Ba"
            Calendar.WEDNESDAY -> "Thứ Tư"
            Calendar.THURSDAY -> "Thứ Năm"
            Calendar.FRIDAY -> "Thứ Sáu"
            Calendar.SATURDAY -> "Thứ Bảy"
            Calendar.SUNDAY -> "Chủ Nhật"
            else -> ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .background(
                Brush.linearGradient(listOf(c.surfaceContainer, c.surfaceContainerHigh)),
                RoundedCornerShape(14.dp)
            )
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Calendar icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Brush.linearGradient(
                        if (c.isDark) listOf(Color(0xFF7F1D1D), Color(0xFF5D1212))
                        else listOf(c.primary, Color(0xFFC62828))
                    ),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${date.day}",
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 18.sp)
                )
                Text(
                    monthNames.getOrElse(date.month) { "" },
                    style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$dayOfWeek, ${"%02d".format(date.day)}/${"%02d".format(date.month)}/${date.year}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            )
            val subtitle = buildString {
                if (date.lunarInfo.isNotEmpty()) append(date.lunarInfo)
                if (date.holiday.isNotEmpty()) {
                    if (isNotEmpty()) append(" · ")
                    append(date.holiday)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(c.textPrimary.copy(alpha = 0.05f), CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "Xóa ngày", tint = c.textTertiary, modifier = Modifier.size(16.dp))
        }
    }
}

// ══════════════════════════════════════════
// Label Row
// ══════════════════════════════════════════

@Composable
private fun LabelRow(
    allLabels: List<String>,
    selectedLabels: Set<String>,
    onToggleLabel: (String) -> Unit
) {
    val c = LichSoThemeColors.current
    val labelIcons = mapOf(
        "Gia đình" to Icons.Outlined.Group,
        "Quan trọng" to Icons.Outlined.PriorityHigh,
        "Công việc" to Icons.Outlined.Work,
        "Cá nhân" to Icons.Outlined.Person,
        "Học tập" to Icons.Outlined.School,
        "Sức khỏe" to Icons.Outlined.FavoriteBorder
    )
    val labelColors = mapOf(
        "Quan trọng" to c.primary,
        "Gia đình" to Color(0xFF7B1FA2)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        allLabels.forEach { label ->
            val isSelected = label in selectedLabels
            val accentColor = labelColors[label] ?: Color(0xFF7B1FA2)
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.08f) else c.surfaceContainer,
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) accentColor.copy(alpha = 0.2f) else c.outlineVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleLabel(label) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        labelIcons[label] ?: Icons.AutoMirrored.Outlined.Label,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else c.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        label,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) accentColor else c.textSecondary
                        )
                    )
                }
            }
        }

        // "Add label" chip
        Box(
            modifier = Modifier
                .border(1.dp, c.outlineVariant, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .clickable { /* TODO: add custom label */ }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(14.dp))
                Text(
                    "Thêm nhãn",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.textTertiary)
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// Type Selector Chip
// ══════════════════════════════════════════

@Composable
private fun TypeChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = modifier
            .background(
                if (isActive) activeColor else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.5.dp,
                if (isActive) activeColor else c.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isActive) Color.White else c.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else c.textSecondary
                )
            )
        }
    }
}

// ══════════════════════════════════════════
// NOTE EDIT PANEL
// ══════════════════════════════════════════

@Composable
private fun NoteEditPanel(
    content: String,
    checklistItems: List<ChecklistItem>,
    onContentChange: (String) -> Unit,
    onChecklistChange: (List<ChecklistItem>) -> Unit
) {
    val c = LichSoThemeColors.current

    // Body textarea
    BasicTextField(
        value = content,
        onValueChange = onContentChange,
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = c.textPrimary,
            lineHeight = 26.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        decorationBox = { innerTextField ->
            if (content.isEmpty() && checklistItems.isEmpty()) {
                Text(
                    "Viết ghi chú...",
                    style = TextStyle(fontSize = 15.sp, color = c.textTertiary)
                )
            }
            innerTextField()
        }
    )

    // Checklist (optional, toggled from toolbar)
    if (checklistItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        checklistItems.forEachIndexed { index, item ->
            ChecklistRow(
                item = item,
                accentColor = EditTaskGreen,
                onToggle = {
                    val updated = checklistItems.toMutableList()
                    updated[index] = item.copy(isDone = !item.isDone)
                    onChecklistChange(updated)
                },
                onTextChange = { newText ->
                    val updated = checklistItems.toMutableList()
                    updated[index] = item.copy(text = newText)
                    onChecklistChange(updated)
                },
                onRemove = if (checklistItems.size > 1) {
                    { onChecklistChange(checklistItems.toMutableList().also { it.removeAt(index) }) }
                } else null
            )
        }

        // Add item
        Row(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .clickable { onChecklistChange(checklistItems + ChecklistItem("", false)) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.AddCircle, null, tint = EditTaskGreen, modifier = Modifier.size(20.dp))
            Text("Thêm mục", style = TextStyle(fontSize = 13.sp, color = c.textTertiary))
        }
    }

    // Extra note area
    if (checklistItems.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = "",
            onValueChange = { /* second text area for additional notes */ },
            textStyle = TextStyle(fontSize = 15.sp, color = c.textPrimary, lineHeight = 26.sp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            decorationBox = { innerTextField ->
                innerTextField()
            }
        )
    }
}

// ══════════════════════════════════════════
// TASK EDIT PANEL
// ══════════════════════════════════════════

@Composable
private fun TaskEditPanel(
    description: String,
    priority: Int,
    dueDate: Long?,
    dueTime: String,
    checklistItems: List<ChecklistItem>,
    reminderEnabled: Boolean,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onDueTimeChange: (String) -> Unit,
    onChecklistChange: (List<ChecklistItem>) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    // ── Checklist section ──
    EditSectionTitle(icon = Icons.Outlined.Checklist, text = "Các bước thực hiện")

    Spacer(modifier = Modifier.height(8.dp))

    checklistItems.forEachIndexed { index, item ->
        ChecklistRow(
            item = item,
            accentColor = EditTaskGreen,
            onToggle = {
                val updated = checklistItems.toMutableList()
                updated[index] = item.copy(isDone = !item.isDone)
                onChecklistChange(updated)
            },
            onTextChange = { newText ->
                val updated = checklistItems.toMutableList()
                updated[index] = item.copy(text = newText)
                onChecklistChange(updated)
            },
            onRemove = if (checklistItems.size > 1) {
                { onChecklistChange(checklistItems.toMutableList().also { it.removeAt(index) }) }
            } else null
        )
    }

    // Add step
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clickable { onChecklistChange(checklistItems + ChecklistItem("", false)) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.AddCircle, null, tint = EditTaskGreen, modifier = Modifier.size(20.dp))
        Text("Thêm bước", style = TextStyle(fontSize = 13.sp, color = c.textTertiary))
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Details section ──
    EditSectionTitle(icon = Icons.Outlined.Tune, text = "Chi tiết công việc")

    Spacer(modifier = Modifier.height(8.dp))

    // Due date + time
    FormRow(
        icon = Icons.Outlined.CalendarToday,
        label = "Hạn chót",
        trailingIcon = Icons.Default.ChevronRight,
        onClick = {
            val cal = Calendar.getInstance()
            if (dueDate != null) cal.timeInMillis = dueDate
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                    onDueDateChange(newCal.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    ) {
        Text(
            if (dueDate != null) {
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dueDate))
                val timeStr = if (dueTime.isNotEmpty()) " · $dueTime" else ""
                "$dateStr$timeStr"
            } else {
                "Chọn ngày..."
            },
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dueDate != null) c.textPrimary else c.textTertiary
            )
        )
    }

    // Priority
    FormRow(
        icon = Icons.Outlined.Flag,
        label = "Ưu tiên"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PriorityChip("Cao", 2, priority == 2, Color(0xFFC62828)) { onPriorityChange(2) }
            PriorityChip("Trung bình", 1, priority == 1, Color(0xFFF57F17)) { onPriorityChange(1) }
            PriorityChip("Thấp", 0, priority == 0, EditTaskGreen) { onPriorityChange(0) }
        }
    }

    // Reminder toggle
    FormRowWithToggle(
        icon = Icons.Outlined.Alarm,
        label = "Nhắc nhở",
        subtitle = if (reminderEnabled && dueDate != null) {
            "Trước 1 ngày"
        } else null,
        isOn = reminderEnabled,
        onToggle = onReminderEnabledChange,
        toggleColor = EditRemindOrange
    )

    // Notes
    FormRow(
        icon = Icons.AutoMirrored.Outlined.StickyNote2,
        label = "Ghi chú thêm"
    ) {
        BasicTextField(
            value = description,
            onValueChange = onDescriptionChange,
            textStyle = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(top = 4.dp),
            decorationBox = { inner ->
                if (description.isEmpty()) {
                    Text("Thêm ghi chú cho công việc...", style = TextStyle(fontSize = 13.sp, color = c.textTertiary))
                }
                inner()
            }
        )
    }
}

// ══════════════════════════════════════════
// REMINDER EDIT PANEL
// ══════════════════════════════════════════

@Composable
private fun ReminderEditPanel(
    date: String,
    time: String,
    repeatType: Int,
    isEnabled: Boolean,
    useLunar: Boolean,
    advanceDays: Int,
    category: Int,
    notes: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRepeatChange: (Int) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onUseLunarChange: (Boolean) -> Unit,
    onAdvanceDaysChange: (Int) -> Unit,
    onCategoryChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    // Settings section
    EditSectionTitle(icon = Icons.Outlined.Alarm, text = "Cài đặt nhắc nhở")

    Spacer(modifier = Modifier.height(8.dp))

    // Date
    FormRow(
        icon = Icons.Outlined.CalendarToday,
        label = "Ngày nhắc",
        trailingIcon = Icons.Default.ChevronRight,
        onClick = {
            val cal = Calendar.getInstance()
            if (date.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.parse(date)?.let { cal.time = it }
                } catch (_: Exception) {}
            }
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    onDateChange("%02d/%02d/%04d".format(d, m + 1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    ) {
        Text(
            if (date.isNotEmpty()) date else "Chọn ngày...",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (date.isNotEmpty()) c.textPrimary else c.textTertiary
            )
        )
    }

    // Time
    FormRow(
        icon = Icons.Outlined.Schedule,
        label = "Giờ nhắc",
        trailingIcon = Icons.Default.ChevronRight,
        onClick = {
            val parts = time.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            TimePickerDialog(context, { _, hour, minute ->
                onTimeChange("%02d:%02d".format(hour, minute))
            }, h, m, true).show()
        }
    ) {
        Text(
            time.ifEmpty { "07:00" },
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
        )
    }

    // Advance days (nhắc trước)
    FormRow(
        icon = Icons.Outlined.NotificationsActive,
        label = "Nhắc trước"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SelectableChip("Đúng ngày", advanceDays == 0, c.primary) { onAdvanceDaysChange(0) }
            SelectableChip("1 ngày", advanceDays == 1, c.primary) { onAdvanceDaysChange(1) }
            SelectableChip("3 ngày", advanceDays == 3, c.primary) { onAdvanceDaysChange(3) }
            SelectableChip("1 tuần", advanceDays == 7, c.primary) { onAdvanceDaysChange(7) }
        }
    }

    // Repeat
    FormRow(
        icon = Icons.Outlined.Replay,
        label = "Lặp lại"
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            RepeatChip("Không", 0, repeatType == 0) { onRepeatChange(0) }
            RepeatChip("Hàng năm", 5, repeatType == 5) { onRepeatChange(5) }
            RepeatChip("Hàng tháng", 3, repeatType == 3) { onRepeatChange(3) }
            RepeatChip("Hàng tuần", 2, repeatType == 2) { onRepeatChange(2) }
        }
    }

    // Lunar calendar toggle
    FormRowWithToggle(
        icon = Icons.Outlined.DarkMode,
        label = "Theo Âm lịch",
        subtitle = "Nhắc theo ngày Âm lịch thay vì Dương lịch",
        isOn = useLunar,
        onToggle = onUseLunarChange,
        toggleColor = EditRemindOrange
    )

    // Category
    FormRow(
        icon = Icons.Outlined.Category,
        label = "Loại nhắc nhở"
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            CategoryChip("🎉 Ngày lễ", 0, category == 0) { onCategoryChange(0) }
            CategoryChip("🎂 Sinh nhật", 1, category == 1) { onCategoryChange(1) }
            CategoryChip("🌙 Âm lịch", 2, category == 2) { onCategoryChange(2) }
            CategoryChip("📝 Cá nhân", 3, category == 3) { onCategoryChange(3) }
            CategoryChip("🕯️ Ngày giỗ", 4, category == 4) { onCategoryChange(4) }
        }
    }

    // Notes
    FormRow(
        icon = Icons.AutoMirrored.Outlined.StickyNote2,
        label = "Ghi chú",
        showDivider = false
    ) {
        BasicTextField(
            value = notes,
            onValueChange = onNotesChange,
            textStyle = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(top = 4.dp),
            decorationBox = { inner ->
                if (notes.isEmpty()) {
                    Text("Thêm ghi chú...", style = TextStyle(fontSize = 13.sp, color = c.textTertiary))
                }
                inner()
            }
        )
    }
}

// ══════════════════════════════════════════
// Shared Edit Components
// ══════════════════════════════════════════

@Composable
private fun EditSectionTitle(icon: ImageVector, text: String) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(16.dp))
        Text(
            text,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = c.textSecondary
            )
        )
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    accentColor: Color,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    if (item.isDone) accentColor else Color.Transparent,
                    RoundedCornerShape(7.dp)
                )
                .border(
                    2.dp,
                    if (item.isDone) accentColor else c.outlineVariant,
                    RoundedCornerShape(7.dp)
                )
                .clip(RoundedCornerShape(7.dp))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (item.isDone) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        // Text
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = if (item.isDone) c.textTertiary else c.textPrimary,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                lineHeight = 22.sp
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
            decorationBox = { inner ->
                if (item.text.isEmpty()) {
                    Text("Nhập mục...", style = TextStyle(fontSize = 14.sp, color = c.textTertiary))
                }
                inner()
            }
        )

        // Remove
        if (onRemove != null) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Xóa",
                tint = c.textQuaternary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun FormRow(
    icon: ImageVector,
    label: String,
    trailingIcon: ImageVector? = null,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(c.surfaceContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
        if (trailingIcon != null) {
            Icon(
                trailingIcon,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    if (showDivider) {
        HorizontalDivider(thickness = 0.5.dp, color = c.outlineVariant)
    }
}

@Composable
private fun FormRowWithToggle(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    toggleColor: Color
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(c.surfaceContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Custom toggle
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(24.dp)
                .background(
                    if (isOn) toggleColor else c.outlineVariant,
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggle(!isOn) }
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(if (isOn) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(Color.White, CircleShape)
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = c.outlineVariant)
}

@Composable
private fun PriorityChip(label: String, value: Int, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(
                if (isActive) activeColor.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isActive) activeColor else c.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) activeColor else c.textTertiary
            )
        )
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(
                if (isActive) activeColor.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isActive) activeColor else c.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) activeColor else c.textTertiary
            )
        )
    }
}

@Composable
private fun RepeatChip(label: String, value: Int, isActive: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(
                if (isActive) EditRemindOrange.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isActive) EditRemindOrange else c.outlineVariant,
                RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) EditRemindOrange else c.textTertiary
            )
        )
    }
}

@Composable
private fun CategoryChip(label: String, value: Int, isActive: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(
                if (isActive) c.primary.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isActive) c.primary else c.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) c.primary else c.textTertiary
            )
        )
    }
}

// ══════════════════════════════════════════
// Note Bottom Toolbar
// ══════════════════════════════════════════

@Composable
private fun NoteColorToolbar(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    hasChecklist: Boolean,
    onToggleChecklist: () -> Unit,
    onInsertBold: () -> Unit = {},
    onInsertList: () -> Unit = {},
    onAttachDate: () -> Unit = {},
    hasAttachedDate: Boolean = false
) {
    val c = LichSoThemeColors.current
    val colors = listOf(
        Color(0xFFFFF8F0),  // Default
        Color(0xFFFFE082),  // Gold
        Color(0xFFEF9A9A),  // Red
        Color(0xFF90CAF9),  // Blue
        Color(0xFFA5D6A7),  // Green
        Color(0xFFCE93D8),  // Purple
        Color(0xFFFFCC80),  // Orange
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = c.outlineVariant)

        // Color dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Màu nền",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.textTertiary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color, CircleShape)
                        .then(
                            if (selectedColorIndex == index)
                                Modifier.border(2.dp, c.textPrimary, CircleShape)
                            else if (index == 0)
                                Modifier.border(1.dp, c.outlineVariant, CircleShape)
                            else Modifier
                        )
                        .clip(CircleShape)
                        .clickable { onColorSelected(index) }
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = c.outlineVariant)

        // Bottom tool row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolButton(
                icon = Icons.Outlined.CheckBox,
                isActive = hasChecklist,
                onClick = onToggleChecklist
            )
            ToolButton(
                icon = Icons.Outlined.CalendarMonth,
                isActive = hasAttachedDate,
                onClick = onAttachDate
            )
            ToolButton(
                icon = Icons.Outlined.FormatBold,
                onClick = onInsertBold
            )
            ToolButton(
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                onClick = onInsertList
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Sửa lúc ${SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())}",
                    style = TextStyle(fontSize = 10.sp, color = c.textTertiary, lineHeight = 14.sp)
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                if (isActive)
                    Modifier.background(c.primaryContainer, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isActive) c.primary else c.textSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ══════════════════════════════════════════
// Utility
// ══════════════════════════════════════════

private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.parse("$dateStr $timeStr")?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val cal = Calendar.getInstance()
            val time = sdf.parse(timeStr)
            if (time != null) {
                val timeCal = Calendar.getInstance().apply { this.time = time }
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
            }
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
