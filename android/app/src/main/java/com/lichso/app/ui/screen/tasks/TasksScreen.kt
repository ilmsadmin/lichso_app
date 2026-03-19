package com.lichso.app.ui.screen.tasks

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.ui.theme.*
import java.util.Calendar

enum class TaskTab { TASKS, NOTES, REMINDERS }

@Composable
fun TasksScreen(viewModel: TasksViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(TaskTab.TASKS) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ghi chú & Việc làm", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, color = c.gold2))
            }

            // Quick Stats
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("${state.taskCount}", "Việc cần làm", c.teal2, Modifier.weight(1f))
                StatCard("${state.reminderCount}", "Nhắc nhở", c.gold2, Modifier.weight(1f))
                StatCard("${state.noteCount}", "Ghi chú", c.textSecondary, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TabBtn("Việc làm", Icons.Outlined.CheckCircle, selectedTab == TaskTab.TASKS, Modifier.weight(1f)) { selectedTab = TaskTab.TASKS }
                TabBtn("Ghi chú", Icons.Outlined.Edit, selectedTab == TaskTab.NOTES, Modifier.weight(1f)) { selectedTab = TaskTab.NOTES }
                TabBtn("Nhắc nhở", Icons.Outlined.NotificationsActive, selectedTab == TaskTab.REMINDERS, Modifier.weight(1f)) { selectedTab = TaskTab.REMINDERS }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                TaskTab.TASKS -> TaskListContent(state.tasks, viewModel)
                TaskTab.NOTES -> NoteListContent(state.notes, viewModel, noteColors)
                TaskTab.REMINDERS -> ReminderListContent(state.reminders, viewModel)
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // FAB — positioned at bottom-end (right side)
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    TaskTab.TASKS -> viewModel.showAddTask(true)
                    TaskTab.NOTES -> viewModel.showAddNote(true)
                    TaskTab.REMINDERS -> viewModel.showAddReminder(true)
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
            containerColor = c.gold,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm", tint = if (c.isDark) Color(0xFF1A1500) else Color.White)
        }
    }

    // Dialogs
    if (state.showAddTaskDialog) AddTaskDialog(viewModel)
    if (state.showAddNoteDialog) AddNoteDialog(viewModel)
    if (state.showAddReminderDialog) AddReminderDialog(viewModel)
}

// ==================== TASKS ====================

@Composable
private fun TaskListContent(tasks: List<TaskEntity>, viewModel: TasksViewModel) {
    if (tasks.isEmpty()) {
        EmptyState(Icons.Outlined.CheckCircle, "Chưa có việc làm nào", "Nhấn + để thêm việc mới")
    } else {
        val pending = tasks.filter { !it.isDone }
        val done = tasks.filter { it.isDone }

        if (pending.isNotEmpty()) {
            SectionLabel("CẦN LÀM · ${pending.size}")
            Spacer(modifier = Modifier.height(7.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pending.forEach { task -> TaskRow(task, viewModel) }
            }
        }
        if (done.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            SectionLabel("ĐÃ XONG · ${done.size}")
            Spacer(modifier = Modifier.height(7.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                done.forEach { task -> TaskRow(task, viewModel) }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox
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
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                if (task.dueDate != null) {
                    Text(viewModel.formatDate(task.dueDate), style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                }
                val label = viewModel.getPriorityLabel(task.priority)
                val (priColor, priBg) = when (task.priority) {
                    2 -> c.red2 to c.red.copy(alpha = 0.14f)
                    1 -> c.gold to c.goldDim
                    else -> c.teal2 to c.tealDim
                }
                Box(modifier = Modifier.background(priBg, RoundedCornerShape(20.dp)).padding(horizontal = 7.dp, vertical = 1.dp)) {
                    Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = priColor))
                }
            }
        }

        // Delete
        Icon(
            Icons.Outlined.Close, contentDescription = "Xoá", tint = c.textQuaternary,
            modifier = Modifier.size(16.dp).clickable { viewModel.deleteTask(task) }
        )
    }
}

// ==================== NOTES ====================

@Composable
private fun NoteListContent(notes: List<NoteEntity>, viewModel: TasksViewModel, noteColors: List<Color>) {
    if (notes.isEmpty()) {
        EmptyState(Icons.Outlined.Edit, "Chưa có ghi chú nào", "Nhấn + để thêm ghi chú mới")
    } else {
        SectionLabel("GHI CHÚ · ${notes.size}")
        Spacer(modifier = Modifier.height(7.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            notes.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { note ->
                        NoteCard(note, viewModel, noteColors, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, viewModel: TasksViewModel, noteColors: List<Color>, modifier: Modifier) {
    val c = LichSoThemeColors.current
    val color = noteColors.getOrElse(note.colorIndex) { c.noteGold }

    Column(
        modifier = modifier
            .background(color, RoundedCornerShape(10.dp))
            .clickable { }
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
                modifier = Modifier.size(14.dp).clickable { viewModel.deleteNote(note) }
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

// ==================== REMINDERS ====================

@Composable
private fun ReminderListContent(reminders: List<ReminderEntity>, viewModel: TasksViewModel) {
    if (reminders.isEmpty()) {
        EmptyState(Icons.Outlined.NotificationsActive, "Chưa có nhắc nhở nào", "Nhấn + để thêm nhắc nhở mới")
    } else {
        SectionLabel("NHẮC NHỞ · ${reminders.size}")
        Spacer(modifier = Modifier.height(7.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            reminders.forEach { reminder -> ReminderRow(reminder, viewModel) }
        }
    }
}

@Composable
private fun ReminderRow(reminder: ReminderEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val cal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val timeStr = String.format("%02d:%02d", if (hour > 12) hour - 12 else if (hour == 0) 12 else hour, minute)
    val amPm = if (hour >= 12) "CH" else "SA"
    val repeatLabel = when (reminder.repeatType) {
        1 -> "Hàng ngày"
        2 -> "Hàng tuần"
        3 -> "Hàng tháng"
        4 -> "Hàng tháng (âm)"
        else -> "Một lần"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
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

// ==================== DIALOGS ====================

@Composable
private fun AddTaskDialog(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = { viewModel.showAddTask(false) },
        containerColor = c.bg2,
        title = { Text("Thêm việc mới", color = c.gold2, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Tên công việc...", color = c.textQuaternary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                        cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                    ),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Mức ưu tiên", style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Thấp", 1 to "Vừa", 2 to "Cao").forEach { (p, label) ->
                        val selected = priority == p
                        val (color, bg) = when (p) {
                            2 -> c.red2 to c.red.copy(alpha = 0.14f)
                            1 -> c.gold to c.goldDim
                            else -> c.teal2 to c.tealDim
                        }
                        Box(
                            modifier = Modifier
                                .background(if (selected) bg else c.bg3, RoundedCornerShape(20.dp))
                                .border(1.dp, if (selected) color else c.border, RoundedCornerShape(20.dp))
                                .clickable { priority = p }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) color else c.textTertiary))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.addTask(title, priority)
                viewModel.showAddTask(false)
            }) { Text("Thêm", color = c.gold) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showAddTask(false) }) { Text("Huỷ", color = c.textTertiary) }
        }
    )
}

@Composable
private fun AddNoteDialog(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.showAddNote(false) },
        containerColor = c.bg2,
        title = { Text("Thêm ghi chú", color = c.gold2, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Tiêu đề...", color = c.textQuaternary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                        cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                    ),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    placeholder = { Text("Nội dung...", color = c.textQuaternary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                        cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                    ),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(), minLines = 3
                )
                Text("Màu sắc", style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    noteColors.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(color, CircleShape)
                                .then(
                                    if (colorIndex == idx) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier.border(1.dp, Color.Transparent, CircleShape)
                                )
                                .clickable { colorIndex = idx }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.addNote(title, content, colorIndex)
                viewModel.showAddNote(false)
            }) { Text("Thêm", color = c.gold) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showAddNote(false) }) { Text("Huỷ", color = c.textTertiary) }
        }
    )
}

@Composable
private fun AddReminderDialog(viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("08") }
    var minute by remember { mutableStateOf("00") }
    var repeatType by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.showAddReminder(false) },
        containerColor = c.bg2,
        title = { Text("Thêm nhắc nhở", color = c.gold2, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("Tiêu đề nhắc nhở...", color = c.textQuaternary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                        cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                    ),
                    textStyle = TextStyle(color = c.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Thời gian", style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hour, onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) hour = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                            cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                        ),
                        textStyle = TextStyle(color = c.textPrimary, fontSize = 16.sp),
                        modifier = Modifier.width(60.dp)
                    )
                    Text(":", style = TextStyle(fontSize = 18.sp, color = c.textSecondary, fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = minute, onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) minute = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = c.border, focusedBorderColor = c.gold,
                            cursorColor = c.gold, unfocusedContainerColor = c.surface, focusedContainerColor = c.surface
                        ),
                        textStyle = TextStyle(color = c.textPrimary, fontSize = 16.sp),
                        modifier = Modifier.width(60.dp)
                    )
                }
                Text("Lặp lại", style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Một lần", 1 to "Hàng ngày", 2 to "Hàng tuần", 3 to "Hàng tháng").forEach { (rt, label) ->
                        val selected = repeatType == rt
                        Box(
                            modifier = Modifier
                                .background(if (selected) c.goldDim else c.bg3, RoundedCornerShape(20.dp))
                                .border(1.dp, if (selected) c.gold else c.border, RoundedCornerShape(20.dp))
                                .clickable { repeatType = rt }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(label, style = TextStyle(fontSize = 11.sp, color = if (selected) c.gold2 else c.textTertiary))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance()
                val h = hour.toIntOrNull() ?: 8
                val m = minute.toIntOrNull() ?: 0
                cal.set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23))
                cal.set(Calendar.MINUTE, m.coerceIn(0, 59))
                cal.set(Calendar.SECOND, 0)
                viewModel.addReminder(title, cal.timeInMillis, repeatType)
                viewModel.showAddReminder(false)
            }) { Text("Thêm", color = c.gold) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showAddReminder(false) }) { Text("Huỷ", color = c.textTertiary) }
        }
    )
}

// ==================== SHARED COMPONENTS ====================

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier) {
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
private fun TabBtn(label: String, icon: ImageVector, isActive: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
            Text(label, style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = textColor))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text,
        style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.sp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = c.textQuaternary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
    }
}
