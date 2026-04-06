package com.lichso.app.ui.screen.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.ai.AiTemplates
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.ui.screen.calendar.DayActionsViewModel
import com.lichso.app.ui.theme.*
import java.util.Calendar

enum class TaskTab2 { TASKS, NOTES, REMINDERS, BOOKMARKS }

@Composable
fun TasksScreen2(viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dayActionsViewModel: DayActionsViewModel = hiltViewModel()
    val dayActionsState by dayActionsViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(TaskTab2.TASKS) }
    val c = LichSoThemeColors.current
    var aiInput by remember { mutableStateOf("") }
    var showAiBar by remember { mutableStateOf(false) }

    // Show AI message as snackbar
    LaunchedEffect(state.aiMessage, state.aiError) {
        val msg = state.aiMessage ?: state.aiError
        if (msg != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.dismissAiMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ghi chú & Việc làm", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, color = c.gold2))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // AI Templates button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(c.teal.copy(alpha = 0.12f), CircleShape)
                            .clip(CircleShape)
                            .clickable { viewModel.toggleAiTemplates() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Dashboard, null, tint = c.teal, modifier = Modifier.size(16.dp))
                    }
                    // AI input toggle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (showAiBar) c.gold.copy(alpha = 0.2f) else c.gold.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { showAiBar = !showAiBar },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, null, tint = c.gold2, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // AI Input Bar
            AnimatedVisibility(
                visible = showAiBar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AiInputBar(
                    input = aiInput,
                    onInputChange = { aiInput = it },
                    isProcessing = state.isAiProcessing,
                    onSend = {
                        viewModel.processAiCommand(aiInput)
                        aiInput = ""
                    }
                )
            }

            // AI Templates Panel
            AnimatedVisibility(
                visible = state.showAiTemplates,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AiTemplatesPanel(
                    onTemplateClick = { template ->
                        viewModel.executeTemplate(template)
                        viewModel.toggleAiTemplates()
                    }
                )
            }

            // AI Message Banner
            AnimatedVisibility(
                visible = state.aiMessage != null || state.aiError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AiMessageBanner(
                    message = state.aiMessage ?: state.aiError ?: "",
                    isError = state.aiError != null,
                    onDismiss = { viewModel.dismissAiMessage() }
                )
            }

            // Quick Stats
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard2("${state.taskCount}", "Việc cần làm", c.teal2, Modifier.weight(1f))
                StatCard2("${state.reminderCount}", "Nhắc nhở", c.gold2, Modifier.weight(1f))
                StatCard2("${state.noteCount}", "Ghi chú", c.textSecondary, Modifier.weight(1f))
                StatCard2("${dayActionsState.bookmarkCount}", "Đánh dấu", Color(0xFFC62828), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TabBtn2("Việc làm", "◈", selectedTab == TaskTab2.TASKS, Modifier.weight(1f)) { selectedTab = TaskTab2.TASKS }
                TabBtn2("Ghi chú", "◇", selectedTab == TaskTab2.NOTES, Modifier.weight(1f)) { selectedTab = TaskTab2.NOTES }
                TabBtn2("Nhắc nhở", "◷", selectedTab == TaskTab2.REMINDERS, Modifier.weight(1f)) { selectedTab = TaskTab2.REMINDERS }
                TabBtn2("Đánh dấu", "★", selectedTab == TaskTab2.BOOKMARKS, Modifier.weight(1f)) { selectedTab = TaskTab2.BOOKMARKS }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                TaskTab2.TASKS -> TaskListContent2(state.tasks, viewModel)
                TaskTab2.NOTES -> NoteListContent2(state.notes, viewModel)
                TaskTab2.REMINDERS -> ReminderListContent2(state.reminders, viewModel)
                TaskTab2.BOOKMARKS -> BookmarkListContent(dayActionsState.allBookmarks, dayActionsViewModel)
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // FAB
        if (selectedTab != TaskTab2.BOOKMARKS) {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        TaskTab2.TASKS -> viewModel.showAddTask(true)
                        TaskTab2.NOTES -> viewModel.showAddNote(true)
                        TaskTab2.REMINDERS -> viewModel.showAddReminder(true)
                        else -> {}
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 96.dp),
                containerColor = c.gold,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color(0xFF1A1500))
            }
        }
    }

    // ── Add Dialogs ──
    if (state.showAddTaskDialog) AddTaskDialog2(viewModel)
    if (state.showAddNoteDialog) AddNoteDialog2(viewModel)
    if (state.showAddReminderDialog) AddReminderDialog2(viewModel)

    // ── Edit Dialogs ──
    state.editingTask?.let { EditTaskDialog(task = it, viewModel = viewModel) }
    state.editingNote?.let { EditNoteDialog(note = it, viewModel = viewModel) }
    state.editingReminder?.let { EditReminderDialog(reminder = it, viewModel = viewModel) }

    // ── Delete Confirmations ──
    state.deletingTask?.let { task ->
        DeleteConfirmDialog(
            title = "Xoá công việc?",
            message = "\"${task.title}\" sẽ bị xoá vĩnh viễn.",
            onConfirm = { viewModel.confirmDeleteTask() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
    state.deletingNote?.let { note ->
        DeleteConfirmDialog(
            title = "Xoá ghi chú?",
            message = "\"${note.title}\" sẽ bị xoá vĩnh viễn.",
            onConfirm = { viewModel.confirmDeleteNote() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
    state.deletingReminder?.let { reminder ->
        DeleteConfirmDialog(
            title = "Xoá nhắc nhở?",
            message = "\"${reminder.title}\" sẽ bị xoá vĩnh viễn.",
            onConfirm = { viewModel.confirmDeleteReminder() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
}

// ═══════════════════════════════════════════
// Delete Confirm Dialog
// ═══════════════════════════════════════════

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(c.red2.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = c.red2, modifier = Modifier.size(22.dp))
            }
        },
        title = { Text(title, color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = { Text(message, color = c.textSecondary, fontSize = 13.sp, lineHeight = 18.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = c.red2),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Xoá", color = Color.White, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

@Composable
private fun TaskListContent2(tasks: List<TaskEntity>, viewModel: TasksViewModel) {
    if (tasks.isEmpty()) {
        EmptyState2("◈", "Chưa có việc làm nào", "Nhấn + để thêm việc mới")
    } else {
        val pending = tasks.filter { !it.isDone }
        val done = tasks.filter { it.isDone }

        if (pending.isNotEmpty()) {
            SectionLabel2("CẦN LÀM · ${pending.size}")
            Spacer(modifier = Modifier.height(7.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pending.forEach { task -> TaskRow2(task, viewModel) }
            }
        }
        if (done.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            SectionLabel2("ĐÃ XONG · ${done.size}")
            Spacer(modifier = Modifier.height(7.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                done.forEach { task -> TaskRow2(task, viewModel) }
            }
        }
    }
}

@Composable
private fun TaskRow2(task: TaskEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .clickable { viewModel.startEditTask(task) }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .padding(top = 1.dp)
                .background(if (task.isDone) c.teal else Color.Transparent, RoundedCornerShape(5.dp))
                .border(1.8.dp, if (task.isDone) c.teal else c.textQuaternary, RoundedCornerShape(5.dp))
                .clickable { viewModel.toggleTask(task) },
            contentAlignment = Alignment.Center
        ) {
            if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = TextStyle(
                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = if (task.isDone) c.textTertiary else c.textPrimary,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                ),
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary, lineHeight = 15.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                if (task.dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Outlined.CalendarToday, null, tint = c.textTertiary, modifier = Modifier.size(11.dp))
                        Text(viewModel.formatDateFull(task.dueDate), style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                    }
                }
                val label = viewModel.getPriorityLabel(task.priority)
                val (priColor, priBg) = when (task.priority) {
                    2 -> c.red2 to Color(0x24D94F3B)
                    1 -> c.gold to Color(0x1FE8C84A)
                    else -> c.teal2 to Color(0x1F4ABEAA)
                }
                Box(modifier = Modifier.background(priBg, RoundedCornerShape(20.dp)).padding(horizontal = 7.dp, vertical = 1.dp)) {
                    Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = priColor))
                }
            }
        }

        Icon(
            Icons.Outlined.Close, contentDescription = "Xoá", tint = c.textQuaternary,
            modifier = Modifier.size(16.dp).clickable { viewModel.requestDeleteTask(task) }
        )
    }
}

@Composable
private fun NoteListContent2(notes: List<NoteEntity>, viewModel: TasksViewModel) {
    if (notes.isEmpty()) {
        EmptyState2("◇", "Chưa có ghi chú nào", "Nhấn + để thêm ghi chú mới")
    } else {
        SectionLabel2("GHI CHÚ · ${notes.size}")
        Spacer(modifier = Modifier.height(7.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            notes.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { note ->
                        NoteCard2(note, viewModel, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NoteCard2(note: NoteEntity, viewModel: TasksViewModel, modifier: Modifier) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    val color = noteColors.getOrElse(note.colorIndex) { c.noteGold }

    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(10.dp))
            .clickable { viewModel.startEditNote(note) }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(note.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)), modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.Close, contentDescription = "Xoá", tint = Color(0x61000000),
                modifier = Modifier.size(14.dp).clickable { viewModel.requestDeleteNote(note) }
            )
        }
        if (note.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(note.content, style = TextStyle(fontSize = 11.sp, color = Color(0x7A000000), lineHeight = 16.sp), maxLines = 3)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(viewModel.formatDate(note.updatedAt), style = TextStyle(fontSize = 10.sp, color = Color(0x61000000)))
    }
}

@Composable
private fun ReminderListContent2(reminders: List<ReminderEntity>, viewModel: TasksViewModel) {
    if (reminders.isEmpty()) {
        EmptyState2("◷", "Chưa có nhắc nhở nào", "Nhấn + để thêm nhắc nhở mới")
    } else {
        SectionLabel2("NHẮC NHỞ · ${reminders.size}")
        Spacer(modifier = Modifier.height(7.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            reminders.forEach { reminder -> ReminderRow2(reminder, viewModel) }
        }
    }
}

@Composable
private fun ReminderRow2(reminder: ReminderEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val cal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val timeStr = String.format("%02d:%02d", if (hour > 12) hour - 12 else if (hour == 0) 12 else hour, minute)
    val amPm = if (hour >= 12) "CH" else "SA"
    val repeatLabel = getRepeatLabel(reminder.repeatType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .clickable { viewModel.startEditReminder(reminder) }
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 46.dp)) {
            Text(timeStr, style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.gold2))
            Text(amPm, style = TextStyle(fontSize = 10.sp, color = c.textTertiary))
        }
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(c.border))
        Column(modifier = Modifier.weight(1f)) {
            Text(reminder.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.textPrimary), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$repeatLabel · ${viewModel.formatDate(reminder.triggerTime)}", style = TextStyle(fontSize = 11.sp, color = c.textTertiary), modifier = Modifier.padding(top = 2.dp))
        }
        Icon(
            Icons.Outlined.Close, contentDescription = "Xoá", tint = c.textQuaternary,
            modifier = Modifier.size(14.dp).clickable { viewModel.requestDeleteReminder(reminder) }
        )
        Switch(
            checked = reminder.isEnabled,
            onCheckedChange = { viewModel.toggleReminder(reminder) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, checkedTrackColor = c.teal,
                uncheckedThumbColor = Color.White, uncheckedTrackColor = c.surface2
            ),
            modifier = Modifier.height(22.dp)
        )
    }
}

private fun getRepeatLabel(repeatType: Int): String = when (repeatType) {
    1 -> "Hàng ngày"
    2 -> "Hàng tuần"
    3 -> "Hàng tháng"
    4 -> "Hàng tháng (âm)"
    else -> "Một lần"
}

@Composable
private fun BookmarkListContent(bookmarks: List<BookmarkEntity>, viewModel: DayActionsViewModel) {
    val c = LichSoThemeColors.current
    if (bookmarks.isEmpty()) {
        EmptyState2("★", "Chưa có ngày nào được đánh dấu", "Vào chi tiết ngày trong lịch để đánh dấu")
    } else {
        SectionLabel2("NGÀY ĐÃ ĐÁNH DẤU · ${bookmarks.size}")
        Spacer(modifier = Modifier.height(7.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            bookmarks.forEach { bookmark -> BookmarkRow(bookmark, viewModel) }
        }
    }
}

@Composable
private fun BookmarkRow(bookmark: BookmarkEntity, viewModel: DayActionsViewModel) {
    val c = LichSoThemeColors.current
    val dateStr = "${"%02d".format(bookmark.solarDay)}/${"%02d".format(bookmark.solarMonth)}/${bookmark.solarYear}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Bookmark icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Bookmark,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                bookmark.label.ifEmpty { dateStr },
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.textPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                dateStr,
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (bookmark.note.isNotBlank()) {
                Text(
                    bookmark.note,
                    style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        Icon(
            Icons.Outlined.Close,
            contentDescription = "Xoá",
            tint = c.textQuaternary,
            modifier = Modifier
                .size(16.dp)
                .clickable { viewModel.removeBookmark(bookmark) }
        )
    }
}

@Composable
private fun AddTaskDialog2(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog2(
            initialDate = dueDate ?: System.currentTimeMillis(),
            onDateSelected = { dueDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = { viewModel.showAddTask(false) },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.teal.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.AddTask, null, tint = c.teal, modifier = Modifier.size(18.dp)) }
                Text("Thêm công việc", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tên công việc")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Nhập tên việc cần làm...", color = c.textQuaternary, fontSize = 13.sp) },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Mô tả")
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    placeholder = { Text("Chi tiết công việc...", color = c.textQuaternary, fontSize = 13.sp) },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp, lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )

                FieldLabel("Ngày đến hạn")
                DateFieldRow(
                    dateText = if (dueDate != null) viewModel.formatDateFull(dueDate!!) else null,
                    onClick = { showDatePicker = true },
                    onClear = { dueDate = null }
                )

                FieldLabel("Mức ưu tiên")
                PrioritySelector(priority) { priority = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.addTask(title, priority, dueDate, description); viewModel.showAddTask(false) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Thêm", color = if (title.isNotBlank()) Color(0xFF1A1500) else c.textQuaternary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.showAddTask(false) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

@Composable
private fun AddNoteDialog2(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.showAddNote(false) },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.gold.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.AutoMirrored.Outlined.StickyNote2, null, tint = c.gold, modifier = Modifier.size(18.dp)) }
                Text("Thêm ghi chú", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tiêu đề")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Tiêu đề ghi chú...", color = c.textQuaternary, fontSize = 13.sp) },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Nội dung")
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    placeholder = { Text("Viết ghi chú...", color = c.textQuaternary, fontSize = 13.sp) },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp, lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 8
                )

                FieldLabel("Màu sắc")
                ColorSelector(noteColors, colorIndex) { colorIndex = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.addNote(title, content, colorIndex); viewModel.showAddNote(false) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Thêm", color = if (title.isNotBlank()) Color(0xFF1A1500) else c.textQuaternary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.showAddNote(false) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

@Composable
private fun AddReminderDialog2(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("08") }
    var minute by remember { mutableStateOf("00") }
    var repeatType by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.showAddReminder(false) },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.teal.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Notifications, null, tint = c.teal, modifier = Modifier.size(18.dp)) }
                Text("Thêm nhắc nhở", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tiêu đề")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Nhắc tôi về...", color = c.textQuaternary, fontSize = 13.sp) },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Thời gian")
                TimeInput(hour, minute, { hour = it }, { minute = it })

                FieldLabel("Lặp lại")
                RepeatSelector(repeatType) { repeatType = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    val h = hour.toIntOrNull() ?: 8
                    val m = minute.toIntOrNull() ?: 0
                    cal.set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23))
                    cal.set(Calendar.MINUTE, m.coerceIn(0, 59))
                    cal.set(Calendar.SECOND, 0)
                    viewModel.addReminder(title, cal.timeInMillis, repeatType)
                    viewModel.showAddReminder(false)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Thêm", color = if (title.isNotBlank()) Color(0xFF1A1500) else c.textQuaternary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.showAddReminder(false) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

// ═══════════════════════════════════════════
// EDIT Dialogs
// ═══════════════════════════════════════════

@Composable
private fun EditTaskDialog(task: TaskEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var priority by remember(task.id) { mutableIntStateOf(task.priority) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog2(
            initialDate = dueDate ?: System.currentTimeMillis(),
            onDateSelected = { dueDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = { viewModel.dismissEditTask() },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.teal.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Edit, null, tint = c.teal, modifier = Modifier.size(18.dp)) }
                Text("Sửa công việc", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tên công việc")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Mô tả")
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp, lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )

                FieldLabel("Ngày đến hạn")
                DateFieldRow(
                    dateText = if (dueDate != null) viewModel.formatDateFull(dueDate!!) else null,
                    onClick = { showDatePicker = true },
                    onClear = { dueDate = null }
                )

                FieldLabel("Mức ưu tiên")
                PrioritySelector(priority) { priority = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateTask(task.copy(title = title, description = description, priority = priority, dueDate = dueDate))
                    viewModel.dismissEditTask()
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Lưu", color = Color(0xFF1A1500), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.dismissEditTask() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

@Composable
private fun EditNoteDialog(note: NoteEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }
    var colorIndex by remember(note.id) { mutableIntStateOf(note.colorIndex) }

    AlertDialog(
        onDismissRequest = { viewModel.dismissEditNote() },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.gold.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Edit, null, tint = c.gold, modifier = Modifier.size(18.dp)) }
                Text("Sửa ghi chú", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tiêu đề")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Nội dung")
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp, lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    maxLines = 10
                )

                FieldLabel("Màu sắc")
                ColorSelector(noteColors, colorIndex) { colorIndex = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateNote(note.copy(title = title, content = content, colorIndex = colorIndex))
                    viewModel.dismissEditNote()
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Lưu", color = Color(0xFF1A1500), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.dismissEditNote() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

@Composable
private fun EditReminderDialog(reminder: ReminderEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember(reminder.id) { mutableStateOf(reminder.title) }
    val cal = remember(reminder.id) { Calendar.getInstance().apply { timeInMillis = reminder.triggerTime } }
    var hour by remember(reminder.id) { mutableStateOf(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))) }
    var minute by remember(reminder.id) { mutableStateOf(String.format("%02d", cal.get(Calendar.MINUTE))) }
    var repeatType by remember(reminder.id) { mutableIntStateOf(reminder.repeatType) }

    AlertDialog(
        onDismissRequest = { viewModel.dismissEditReminder() },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(c.teal.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Edit, null, tint = c.teal, modifier = Modifier.size(18.dp)) }
                Text("Sửa nhắc nhở", color = c.gold2, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldLabel("Tiêu đề")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    colors = fieldColors(),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FieldLabel("Thời gian")
                TimeInput(hour, minute, { hour = it }, { minute = it })

                FieldLabel("Lặp lại")
                RepeatSelector(repeatType) { repeatType = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    val h = hour.toIntOrNull() ?: 8
                    val m = minute.toIntOrNull() ?: 0
                    newCal.set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23))
                    newCal.set(Calendar.MINUTE, m.coerceIn(0, 59))
                    newCal.set(Calendar.SECOND, 0)
                    viewModel.updateReminder(reminder.copy(title = title, triggerTime = newCal.timeInMillis, repeatType = repeatType))
                    viewModel.dismissEditReminder()
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = c.gold, disabledContainerColor = c.surface),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Lưu", color = Color(0xFF1A1500), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.dismissEditReminder() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, c.border)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

// ═══════════════════════════════════════════
// AI Components
// ═══════════════════════════════════════════

@Composable
private fun AiInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    isProcessing: Boolean,
    onSend: () -> Unit
) {
    val c = LichSoThemeColors.current
    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.bg2, RoundedCornerShape(14.dp))
                .border(1.dp, c.gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AutoAwesome, null,
                tint = c.gold.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 10.dp).size(18.dp)
            )
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "Hỏi AI: \"tạo checklist đi chợ\"...",
                        style = TextStyle(fontSize = 13.sp, color = c.textQuaternary)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    cursorColor = c.gold,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                textStyle = TextStyle(color = c.textPrimary, fontSize = 13.sp),
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isProcessing
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (input.isNotBlank() && !isProcessing) c.gold else c.surface,
                        RoundedCornerShape(10.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = input.isNotBlank() && !isProcessing) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = c.gold,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.ArrowUpward, null,
                        tint = if (input.isNotBlank()) Color(0xFF1A1500) else c.textQuaternary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        // Quick suggestion chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AiSuggestionChip("📝 Ghi chú nhanh") { onInputChange("ghi chú: ") }
            AiSuggestionChip("✅ Tạo checklist") { onInputChange("tạo checklist: ") }
            AiSuggestionChip("⏰ Nhắc nhở") { onInputChange("nhắc tôi ") }
            AiSuggestionChip("📋 Kế hoạch ngày") { onInputChange("tạo kế hoạch ngày hôm nay") }
        }
    }
}

@Composable
private fun AiSuggestionChip(text: String, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(c.bg3, RoundedCornerShape(20.dp))
            .border(1.dp, c.border, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
    }
}

@Composable
private fun AiMessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val bg = if (isError) c.red2.copy(alpha = 0.1f) else c.teal.copy(alpha = 0.1f)
    val borderColor = if (isError) c.red2.copy(alpha = 0.3f) else c.teal.copy(alpha = 0.3f)
    val textColor = if (isError) c.red2 else c.teal2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircleOutline,
            null, tint = textColor, modifier = Modifier.size(18.dp).padding(top = 1.dp)
        )
        Text(
            message,
            style = TextStyle(fontSize = 12.5.sp, color = textColor, lineHeight = 18.sp),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Outlined.Close, null, tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp).clickable(onClick = onDismiss)
        )
    }
}

@Composable
private fun AiTemplatesPanel(
    onTemplateClick: (AiTemplates.QuickTemplate) -> Unit
) {
    val c = LichSoThemeColors.current
    var selectedCategory by remember { mutableStateOf<AiTemplates.TemplateCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp)
            .background(c.bg2, RoundedCornerShape(14.dp))
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.Dashboard, null, tint = c.teal, modifier = Modifier.size(16.dp))
            Text(
                "Mẫu nhanh",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.gold2)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isAll = selectedCategory == null
            Box(
                modifier = Modifier
                    .background(if (isAll) c.goldDim else c.bg3, RoundedCornerShape(20.dp))
                    .border(1.dp, if (isAll) c.gold.copy(alpha = 0.38f) else c.border, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { selectedCategory = null }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text("Tất cả", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isAll) c.gold2 else c.textTertiary))
            }
            AiTemplates.TemplateCategory.entries.forEach { cat ->
                val isActive = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .background(if (isActive) c.goldDim else c.bg3, RoundedCornerShape(20.dp))
                        .border(1.dp, if (isActive) c.gold.copy(alpha = 0.38f) else c.border, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(cat.label, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isActive) c.gold2 else c.textTertiary))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Template list
        val templates = if (selectedCategory != null) {
            AiTemplates.getByCategory(selectedCategory!!)
        } else {
            AiTemplates.quickTemplates
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            templates.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface, RoundedCornerShape(10.dp))
                        .border(1.dp, c.border, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTemplateClick(template) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(template.icon, fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            template.title,
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                        )
                        Text(
                            template.subtitle,
                            style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = c.textQuaternary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Shared Components
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog2(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { state.selectedDateMillis?.let(onDateSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = c.gold),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Chọn", color = Color(0xFF1A1500), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, c.border)) {
                Text("Huỷ", color = c.textSecondary)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = c.bg2)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = c.bg2,
                titleContentColor = c.gold2,
                headlineContentColor = c.textPrimary,
                weekdayContentColor = c.textTertiary,
                dayContentColor = c.textPrimary,
                selectedDayContainerColor = c.gold,
                selectedDayContentColor = Color(0xFF1A1500),
                todayContentColor = c.teal,
                todayDateBorderColor = c.teal,
                navigationContentColor = c.textSecondary
            )
        )
    }
}

@Composable
private fun DateFieldRow(dateText: String?, onClick: () -> Unit, onClear: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface, RoundedCornerShape(8.dp))
            .border(1.dp, c.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CalendarToday, null, tint = c.gold, modifier = Modifier.size(16.dp))
            Text(
                dateText ?: "Chọn ngày...",
                style = TextStyle(fontSize = 13.sp, color = if (dateText != null) c.textPrimary else c.textQuaternary)
            )
        }
        if (dateText != null) {
            Icon(
                Icons.Outlined.Close, null, tint = c.textQuaternary,
                modifier = Modifier.size(14.dp).clickable(onClick = onClear)
            )
        }
    }
}

@Composable
private fun PrioritySelector(selected: Int, onSelect: (Int) -> Unit) {
    val c = LichSoThemeColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0 to "Thấp", 1 to "Vừa", 2 to "Cao").forEach { (p, label) ->
            val isSelected = selected == p
            val (color, bg) = when (p) {
                2 -> c.red2 to Color(0x24D94F3B)
                1 -> c.gold to Color(0x1FE8C84A)
                else -> c.teal2 to Color(0x1F4ABEAA)
            }
            Box(
                modifier = Modifier
                    .background(if (isSelected) bg else c.bg3, RoundedCornerShape(20.dp))
                    .border(1.dp, if (isSelected) color else c.border, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(p) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) color else c.textTertiary))
            }
        }
    }
}

@Composable
private fun ColorSelector(colors: List<Color>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEachIndexed { idx, color ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color, CircleShape)
                    .then(
                        if (selected == idx)
                            Modifier.border(2.5.dp, Color.White, CircleShape)
                        else
                            Modifier.border(1.dp, Color.Transparent, CircleShape)
                    )
                    .clip(CircleShape)
                    .clickable { onSelect(idx) }
            )
        }
    }
}

@Composable
private fun TimeInput(
    hour: String, minute: String,
    onHourChange: (String) -> Unit, onMinuteChange: (String) -> Unit
) {
    val c = LichSoThemeColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = hour,
            onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) onHourChange(it) },
            colors = fieldColors(),
            textStyle = TextStyle(color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
            modifier = Modifier.width(60.dp),
            singleLine = true
        )
        Text(":", style = TextStyle(fontSize = 22.sp, color = c.gold, fontWeight = FontWeight.Bold))
        OutlinedTextField(
            value = minute,
            onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) onMinuteChange(it) },
            colors = fieldColors(),
            textStyle = TextStyle(color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
            modifier = Modifier.width(60.dp),
            singleLine = true
        )
    }
}

@Composable
private fun RepeatSelector(selected: Int, onSelect: (Int) -> Unit) {
    val c = LichSoThemeColors.current
    val options = listOf(0 to "Một lần", 1 to "Hàng ngày", 2 to "Hàng tuần", 3 to "Hàng tháng")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (type, label) ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .background(if (isSelected) c.goldDim else c.bg3, RoundedCornerShape(20.dp))
                    .border(1.dp, if (isSelected) c.gold.copy(alpha = 0.38f) else c.border, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(type) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) c.gold2 else c.textTertiary))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.textTertiary, letterSpacing = 0.5.sp))
}

@Composable
private fun fieldColors(): TextFieldColors {
    val c = LichSoThemeColors.current
    return OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = c.border,
        focusedBorderColor = c.gold,
        cursorColor = c.gold,
        unfocusedContainerColor = c.surface,
        focusedContainerColor = c.surface
    )
}

// ═══════════════════════════════════════════
// Base Components
// ═══════════════════════════════════════════

@Composable
private fun StatCard2(value: String, label: String, color: Color, modifier: Modifier) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color))
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, style = TextStyle(fontSize = 10.sp, color = c.textTertiary))
    }
}

@Composable
private fun TabBtn2(label: String, icon: String, isActive: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    val bg = if (isActive) c.goldDim else c.bg3
    val borderColor = if (isActive) c.gold.copy(alpha = 0.38f) else c.border
    val textColor = if (isActive) c.gold2 else c.textTertiary

    Box(
        modifier = modifier
            .height(34.dp)
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(icon, fontSize = 14.sp)
            Text(label, style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = textColor))
        }
    }
}

@Composable
private fun SectionLabel2(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text,
        style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.sp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun EmptyState2(icon: String, title: String, subtitle: String) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
    }
}
