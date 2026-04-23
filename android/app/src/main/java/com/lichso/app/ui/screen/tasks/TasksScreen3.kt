package com.lichso.app.ui.screen.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
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
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import java.util.Calendar

// ══════════════════════════════════════════
// Color constants matching HTML mockup
// ══════════════════════════════════════════
private val NoteBlue = Color(0xFF1565C0)
private val TaskGreen = Color(0xFF2E7D32)
private val RemindOrange = Color(0xFFE65100)

private val NoteBlueBg = Color(0xFFE3F2FD)
private val TaskGreenBg = Color(0xFFE8F5E9)
private val RemindOrangeBg = Color(0xFFFFF3E0)

enum class NTRTab { REMINDERS, NOTES, TASKS }

// ══════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════

@Composable
fun TasksScreen3(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onEditVisibilityChanged: (Boolean) -> Unit = {},
    viewModel: TasksViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(NTRTab.REMINDERS) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterLabel by remember { mutableStateOf("") }
    var filterPriority by remember { mutableIntStateOf(-1) } // -1 = all

    val activeColor = when (selectedTab) {
        NTRTab.NOTES -> NoteBlue
        NTRTab.TASKS -> TaskGreen
        NTRTab.REMINDERS -> RemindOrange
    }

    // Determine if we should show the edit screen
    val showEditScreen = state.showAddNoteDialog || state.showAddTaskDialog || state.showAddReminderDialog
            || state.editingNote != null || state.editingTask != null || state.editingReminder != null

    // Notify parent when edit screen visibility changes (to hide bottom bar)
    LaunchedEffect(showEditScreen) {
        onEditVisibilityChanged(showEditScreen)
    }

    val editInitialType = when {
        state.showAddNoteDialog || state.editingNote != null -> EditItemType.NOTE
        state.showAddTaskDialog || state.editingTask != null -> EditItemType.TASK
        state.showAddReminderDialog || state.editingReminder != null -> EditItemType.REMIND
        else -> EditItemType.NOTE
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ═══ LIST SCREEN ═══
        Column(modifier = Modifier.fillMaxSize().background(c.bg).imePadding()) {
        // ═══ TOP BAR ═══
        NTRTopBar(
            selectedTab = selectedTab,
            onMenuClick = onMenuClick,
            onAddClick = {
                when (selectedTab) {
                    NTRTab.NOTES -> viewModel.showAddNote(true)
                    NTRTab.TASKS -> viewModel.showAddTask(true)
                    NTRTab.REMINDERS -> viewModel.showAddReminder(true)
                }
            }
        )

        // ═══ STATS ROW ═══
        StatsRow(
            noteCount = state.noteCount,
            taskPending = state.taskCount,
            taskTotal = state.tasks.size,
            reminderCount = state.reminderCount
        )

        // ═══ TABS ═══
        TabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        // ═══ SEARCH ═══
        SearchRow(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onFilterClick = { showFilterSheet = true },
            hasActiveFilter = filterLabel.isNotBlank() || filterPriority >= 0
        )

        // ═══ CONTENT ═══
        when (selectedTab) {
            NTRTab.NOTES -> NoteListV3(
                notes = state.notes.filterBySearch(searchQuery).filterByLabel(filterLabel),
                viewModel = viewModel,
                onAddClick = { viewModel.showAddNote(true) }
            )
            NTRTab.TASKS -> TaskListV3(
                tasks = state.tasks.filterTaskBySearch(searchQuery).filterTaskByLabelAndPriority(filterLabel, filterPriority),
                viewModel = viewModel,
                onAddClick = { viewModel.showAddTask(true) }
            )
            NTRTab.REMINDERS -> ReminderListV3(
                reminders = state.reminders.filterReminderBySearch(searchQuery).filterReminderByLabel(filterLabel),
                viewModel = viewModel,
                onAddClick = { viewModel.showAddReminder(true) }
            )
        }
    }

        // ═══ FILTER BOTTOM SHEET ═══
        if (showFilterSheet) {
            NTRFilterSheet(
                selectedTab = selectedTab,
                currentLabel = filterLabel,
                currentPriority = filterPriority,
                allLabels = remember(state.notes, state.tasks, state.reminders) {
                    val noteLabels = state.notes.flatMap { it.labels.split(",").map { l -> l.trim() } }
                    val taskLabels = state.tasks.flatMap { it.labels.split(",").map { l -> l.trim() } }
                    val reminderLabels = state.reminders.flatMap { it.labels.split(",").map { l -> l.trim() } }
                    (noteLabels + taskLabels + reminderLabels).filter { it.isNotBlank() }.distinct().sorted()
                },
                onApply = { label, priority ->
                    filterLabel = label
                    filterPriority = priority
                    showFilterSheet = false
                },
                onReset = {
                    filterLabel = ""
                    filterPriority = -1
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        // ═══ EDIT SCREEN OVERLAY ═══
        AnimatedVisibility(
            visible = showEditScreen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            // Use key to force recomposition when editing entity changes
            val editKey = remember(state.editingNote, state.editingTask, state.editingReminder,
                state.showAddNoteDialog, state.showAddTaskDialog, state.showAddReminderDialog) {
                "${state.editingNote?.id}_${state.editingTask?.id}_${state.editingReminder?.id}_${state.showAddNoteDialog}_${state.showAddTaskDialog}_${state.showAddReminderDialog}"
            }
            key(editKey) {
                NoteTaskEditScreen(
                    initialType = editInitialType,
                    editNote = state.editingNote,
                    editTask = state.editingTask,
                    editReminder = state.editingReminder,
                    onBackClick = { viewModel.dismissAllEditing() },
                    onSaveNote = { note ->
                        if (state.editingNote != null) {
                            viewModel.updateNote(note)
                        } else {
                            viewModel.insertNote(note)
                        }
                        viewModel.dismissAllEditing()
                    },
                    onSaveTask = { task ->
                        if (state.editingTask != null) {
                            viewModel.updateTask(task)
                        } else {
                            viewModel.insertTask(task)
                        }
                        viewModel.dismissAllEditing()
                    },
                    onSaveReminder = { reminder ->
                        if (state.editingReminder != null) {
                            viewModel.updateReminder(reminder)
                        } else {
                            viewModel.insertReminder(reminder)
                        }
                        viewModel.dismissAllEditing()
                    },
                    onDelete = {
                        state.editingNote?.let { viewModel.deleteNote(it) }
                        state.editingTask?.let { viewModel.deleteTask(it) }
                        state.editingReminder?.let { viewModel.deleteReminder(it) }
                        viewModel.dismissAllEditing()
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// Top Bar
// ══════════════════════════════════════════

@Composable
private fun NTRTopBar(
    selectedTab: NTRTab,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit
) {
    AppTopBar(
        title = "Ghi chú & Việc cần làm",
        onBackClick = onMenuClick,
        leadingIcon = Icons.Filled.Menu
    )
}

// ══════════════════════════════════════════
// Stats Row
// ══════════════════════════════════════════

@Composable
private fun StatsRow(noteCount: Int, taskPending: Int, taskTotal: Int, reminderCount: Int) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            value = "$reminderCount",
            label = "Nhắc nhở",
            color = RemindOrange,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            value = "$noteCount",
            label = "Ghi chú",
            color = NoteBlue,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            value = "$taskPending",
            valueSuffix = "/$taskTotal",
            label = "Công việc",
            color = TaskGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    value: String,
    valueSuffix: String = "",
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            )
            if (valueSuffix.isNotEmpty()) {
                Text(
                    valueSuffix,
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            label,
            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = c.textTertiary)
        )
    }
}

// ══════════════════════════════════════════
// Tab Row (matching HTML mockup)
// ══════════════════════════════════════════

@Composable
private fun TabRow(selectedTab: NTRTab, onTabSelected: (NTRTab) -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
    ) {
        TabButton(
            icon = Icons.Outlined.Alarm,
            label = "Nhắc nhở",
            isActive = selectedTab == NTRTab.REMINDERS,
            activeColor = RemindOrange,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(NTRTab.REMINDERS) }
        )
        TabButton(
            icon = Icons.AutoMirrored.Outlined.StickyNote2,
            label = "Ghi chú",
            isActive = selectedTab == NTRTab.NOTES,
            activeColor = NoteBlue,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(NTRTab.NOTES) }
        )
        TabButton(
            icon = Icons.Outlined.Checklist,
            label = "Công việc",
            isActive = selectedTab == NTRTab.TASKS,
            activeColor = TaskGreen,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(NTRTab.TASKS) }
        )
    }
}

@Composable
private fun TabButton(
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
            .background(if (isActive) activeColor else Color.Transparent)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isActive) Color.White else c.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else c.textSecondary
                )
            )
        }
    }
}

// ══════════════════════════════════════════
// Search Row
// ══════════════════════════════════════════

@Composable
private fun SearchRow(query: String, onQueryChange: (String) -> Unit, onFilterClick: () -> Unit = {}, hasActiveFilter: Boolean = false) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(c.surfaceContainer, RoundedCornerShape(12.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(fontSize = 13.sp, color = c.textPrimary),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Tìm kiếm...", style = TextStyle(fontSize = 13.sp, color = c.textTertiary))
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Xóa",
                    tint = c.textTertiary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (hasActiveFilter) c.primary.copy(alpha = 0.15f) else c.surfaceContainer,
                    RoundedCornerShape(10.dp)
                )
                .border(
                    1.dp,
                    if (hasActiveFilter) c.primary else c.outlineVariant,
                    RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Tune, contentDescription = "Lọc", tint = if (hasActiveFilter) c.primary else c.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// ══════════════════════════════════════════
// NOTE LIST (matching screen-notes.html)
// ══════════════════════════════════════════

@Composable
private fun NoteListV3(notes: List<NoteEntity>, viewModel: TasksViewModel, onAddClick: () -> Unit) {
    val c = LichSoThemeColors.current

    if (notes.isEmpty()) {
        EmptyStateV3(
            icon = Icons.AutoMirrored.Outlined.StickyNote2,
            title = "Chưa có ghi chú nào",
            subtitle = "Tạo ghi chú đầu tiên của bạn",
            onAddClick = onAddClick,
            addLabel = "Thêm ghi chú",
            addColor = NoteBlue
        )
    } else {
        val pinnedNotes = notes.filter { it.isPinned }
        val otherNotes = notes.filter { !it.isPinned }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // Inline add card
            item { AddNewCard(label = "Thêm ghi chú mới", color = NoteBlue, icon = Icons.Outlined.EditNote, onClick = onAddClick) }

            if (pinnedNotes.isNotEmpty()) {
                item { SectionDivider("📌 Đã ghim") }
                items(pinnedNotes, key = { it.id }) { note ->
                    NoteCardV3(note = note, viewModel = viewModel, isPinned = true)
                }
            }

            if (otherNotes.isNotEmpty()) {
                item { SectionDivider("Gần đây") }
                items(otherNotes, key = { it.id }) { note ->
                    NoteCardV3(note = note, viewModel = viewModel, isPinned = false)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NoteCardV3(note: NoteEntity, viewModel: TasksViewModel, isPinned: Boolean) {
    val c = LichSoThemeColors.current
    val noteColors = listOf(c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen, c.noteRed)
    val stripColor = noteColors.getOrElse(note.colorIndex) { NoteBlue }

    // Parse checklist info from content
    val checklistInfo = remember(note.content) {
        val lines = note.content.split("\n")
        val checkItems = lines.filter { it.startsWith("[x] ") || it.startsWith("[ ] ") }
        if (checkItems.isNotEmpty()) {
            val done = checkItems.count { it.startsWith("[x] ") }
            Pair(done, checkItems.size)
        } else null
    }

    // Parse labels
    val labels = remember(note.labels) {
        if (note.labels.isNotBlank()) note.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }
        else emptyList()
    }

    // Display content (exclude checklist lines)
    val displayContent = remember(note.content) {
        note.content.split("\n")
            .filter { !it.startsWith("[x] ") && !it.startsWith("[ ] ") }
            .joinToString("\n")
            .trim()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { viewModel.startEditNote(note) }
    ) {
        // Left color strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(stripColor)
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // Header: title + pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    note.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isPinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Ghim",
                        tint = c.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Body preview
            if (displayContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    displayContent,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = c.textSecondary,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Checklist progress
            if (checklistInfo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckBox,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${checklistInfo.first}/${checklistInfo.second} mục",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                    )
                }
            }

            // Footer: labels + chips + time
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date chip
                InfoChip(
                    icon = Icons.Outlined.CalendarToday,
                    text = viewModel.formatDate(note.updatedAt),
                    bgColor = c.primary.copy(alpha = 0.08f),
                    textColor = c.primary
                )

                // Label chips (show first 2)
                labels.take(2).forEach { label ->
                    val labelColor = when (label) {
                        "Quan trọng" -> c.primary
                        "Gia đình" -> Color(0xFF7B1FA2)
                        "Công việc" -> NoteBlue
                        "Học tập" -> Color(0xFF00695C)
                        "Sức khỏe" -> Color(0xFFC62828)
                        else -> Color(0xFF7B1FA2)
                    }
                    InfoChip(
                        icon = Icons.AutoMirrored.Outlined.Label,
                        text = label,
                        bgColor = labelColor.copy(alpha = 0.08f),
                        textColor = labelColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Relative time
                Text(
                    getRelativeTime(note.updatedAt),
                    style = TextStyle(fontSize = 10.sp, color = c.textTertiary)
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// TASK LIST (matching screen-notes.html)
// ══════════════════════════════════════════

@Composable
private fun TaskListV3(tasks: List<TaskEntity>, viewModel: TasksViewModel, onAddClick: () -> Unit) {
    if (tasks.isEmpty()) {
        EmptyStateV3(
            icon = Icons.Outlined.Checklist,
            title = "Chưa có việc làm nào",
            subtitle = "Tạo công việc đầu tiên của bạn",
            onAddClick = onAddClick,
            addLabel = "Thêm công việc",
            addColor = TaskGreen
        )
    } else {
        val pending = tasks.filter { !it.isDone }
        val done = tasks.filter { it.isDone }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            item { AddNewCard(label = "Thêm công việc mới", color = TaskGreen, icon = Icons.Outlined.AddTask, onClick = onAddClick) }

            if (pending.isNotEmpty()) {
                item { SectionDivider("Đang làm") }
                items(pending, key = { it.id }) { task ->
                    TaskCardV3(task = task, viewModel = viewModel)
                }
            }
            if (done.isNotEmpty()) {
                item { SectionDivider("✅ Đã xong") }
                items(done, key = { it.id }) { task ->
                    TaskCardV3(task = task, viewModel = viewModel)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TaskCardV3(task: TaskEntity, viewModel: TasksViewModel) {
    val c = LichSoThemeColors.current
    val isDone = task.isDone
    val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && !isDone

    // Parse checklist from description
    val checklistInfo = remember(task.description) {
        val lines = task.description.split("\n")
        val checkItems = lines.filter { it.startsWith("[x] ") || it.startsWith("[ ] ") }
        if (checkItems.isNotEmpty()) {
            val done = checkItems.count { it.startsWith("[x] ") }
            Pair(done, checkItems.size)
        } else null
    }

    // Plain description (non-checklist)
    val plainDescription = remember(task.description) {
        task.description.split("\n")
            .filter { !it.startsWith("[x] ") && !it.startsWith("[ ] ") }
            .joinToString("\n")
            .trim()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .then(if (isDone) Modifier.graphicsLayer { alpha = 0.5f } else Modifier)
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { viewModel.startEditTask(task) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    if (isDone) TaskGreen else Color.Transparent,
                    RoundedCornerShape(7.dp)
                )
                .border(2.dp, TaskGreen, RoundedCornerShape(7.dp))
                .clip(RoundedCornerShape(7.dp))
                .clickable { viewModel.toggleTask(task) },
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) c.textTertiary else c.textPrimary,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (plainDescription.isNotBlank() && !isDone) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    plainDescription,
                    style = TextStyle(fontSize = 11.sp, color = c.textSecondary, lineHeight = 16.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date chip
                if (task.dueDate != null) {
                    if (isDone) {
                        InfoChip(
                            icon = Icons.Outlined.CheckCircle,
                            text = "Xong ${viewModel.formatDate(task.updatedAt)}",
                            bgColor = TaskGreenBg,
                            textColor = TaskGreen
                        )
                    } else if (isOverdue) {
                        InfoChip(
                            icon = Icons.Outlined.Warning,
                            text = "Quá hạn",
                            bgColor = Color(0xFFFFEBEE),
                            textColor = Color(0xFFC62828)
                        )
                    } else {
                        val dateText = buildString {
                            append(viewModel.formatDate(task.dueDate))
                            if (!task.dueTime.isNullOrEmpty()) {
                                append(" · ${task.dueTime}")
                            }
                        }
                        InfoChip(
                            icon = Icons.Outlined.CalendarToday,
                            text = dateText,
                            bgColor = TaskGreenBg,
                            textColor = TaskGreen
                        )
                    }
                }

                // Checklist progress
                if (checklistInfo != null && !isDone) {
                    InfoChip(
                        icon = Icons.Outlined.CheckBox,
                        text = "${checklistInfo.first}/${checklistInfo.second}",
                        bgColor = NoteBlueBg,
                        textColor = NoteBlue
                    )
                }

                // Priority dot
                if (!isDone) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (task.priority) {
                                    2 -> Color(0xFFC62828)
                                    1 -> Color(0xFFF57F17)
                                    else -> TaskGreen
                                },
                                CircleShape
                            )
                    )
                }

                // Label chips
                if (task.labels.isNotBlank() && !isDone) {
                    task.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(1).forEach { label ->
                        val labelColor = when (label) {
                            "Quan trọng" -> Color(0xFFC62828)
                            "Gia đình" -> Color(0xFF7B1FA2)
                            else -> NoteBlue
                        }
                        InfoChip(
                            icon = Icons.AutoMirrored.Outlined.Label,
                            text = label,
                            bgColor = labelColor.copy(alpha = 0.08f),
                            textColor = labelColor
                        )
                    }
                }
            }
        }

        // Right side: time info + progress bar
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (task.dueDate != null && !isDone) {
                val daysLeft = getDaysUntil(task.dueDate)
                Text(
                    if (isOverdue) "Quá hạn" else "Còn $daysLeft ngày",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = if (isOverdue) Color(0xFFC62828) else c.textTertiary
                    )
                )
            }
            // Checklist progress bar
            if (checklistInfo != null && checklistInfo.second > 0 && !isDone) {
                val progress = checklistInfo.first.toFloat() / checklistInfo.second.toFloat()
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(c.outlineVariant, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(TaskGreen, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// REMINDER LIST (matching screen-notes.html)
// ══════════════════════════════════════════

@Composable
private fun ReminderListV3(reminders: List<ReminderEntity>, viewModel: TasksViewModel, onAddClick: () -> Unit) {
    if (reminders.isEmpty()) {
        EmptyStateV3(
            icon = Icons.Outlined.Alarm,
            title = "Chưa có nhắc nhở nào",
            subtitle = "Tạo nhắc nhở đầu tiên của bạn",
            onAddClick = onAddClick,
            addLabel = "Thêm nhắc nhở",
            addColor = RemindOrange
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            item { AddNewCard(label = "Thêm nhắc nhở mới", color = RemindOrange, icon = Icons.Outlined.AlarmAdd, onClick = onAddClick) }

            // ── Phân loại reminder ──
            // Reminder Once (repeatType==0) sau khi đã đến giờ trigger thì coi
            // như "đã nhắc" — vì AlarmManager đã fire xong, scheduler không
            // schedule lại (xem ReminderScheduler.schedule), nhưng `isEnabled`
            // trong DB vẫn = true. Nếu để chung "Sắp tới" sẽ gây hiểu nhầm
            // (user thấy reminder 11:40 vẫn còn ở danh sách dù đã thông báo).
            //
            // Reminder lặp (Daily/Weekly/Monthly/Yearly) thì triggerTime gốc
            // dù đã qua vẫn coi là "Sắp tới" — vì lần fire kế tiếp được tính
            // động trong ReminderScheduler.
            val now = System.currentTimeMillis()
            val (enabled, disabled) = reminders.partition { it.isEnabled }
            val (alreadyFired, upcoming) = enabled.partition { r ->
                r.repeatType == 0 && r.triggerTime <= now
            }

            if (upcoming.isNotEmpty()) {
                item { SectionDivider("Sắp tới") }
                items(upcoming, key = { it.id }) { reminder ->
                    ReminderCardV3(reminder = reminder, viewModel = viewModel)
                }
            }
            if (alreadyFired.isNotEmpty()) {
                item { SectionDivider("Đã nhắc") }
                items(alreadyFired, key = { "fired_${it.id}" }) { reminder ->
                    ReminderCardV3(reminder = reminder, viewModel = viewModel, alreadyFired = true)
                }
            }
            if (disabled.isNotEmpty()) {
                item { SectionDivider("Đã tắt") }
                items(disabled, key = { it.id }) { reminder ->
                    ReminderCardV3(reminder = reminder, viewModel = viewModel)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ReminderCardV3(
    reminder: ReminderEntity,
    viewModel: TasksViewModel,
    alreadyFired: Boolean = false
) {
    val c = LichSoThemeColors.current
    val typeInfo = getReminderTypeInfo(reminder)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { viewModel.startEditReminder(reminder) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(typeInfo.bgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                typeInfo.icon,
                contentDescription = null,
                tint = typeInfo.iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Show date + time info
            Spacer(modifier = Modifier.height(2.dp))
            val dateTimeText = buildString {
                append(viewModel.formatDateFull(reminder.triggerTime))
                append(" · ")
                append(viewModel.formatTime(reminder.triggerTime))
                if (reminder.subtitle.isNotBlank()) {
                    append(" · ")
                    append(reminder.subtitle)
                }
            }
            Text(
                dateTimeText,
                style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxLines = 2,
                overflow = FlowRowOverflow.Clip
            ) {
                // Time until
                val daysLeft = getDaysUntil(reminder.triggerTime)
                if (alreadyFired) {
                    InfoChip(
                        icon = Icons.Outlined.CheckCircle,
                        text = "Đã nhắc",
                        bgColor = Color(0xFFE8F5E9),
                        textColor = Color(0xFF2E7D32)
                    )
                } else if (reminder.isEnabled && daysLeft >= 0) {
                    InfoChip(
                        icon = Icons.Outlined.Schedule,
                        text = if (daysLeft == 0L) "Hôm nay" else "Còn $daysLeft ngày",
                        bgColor = TaskGreenBg,
                        textColor = TaskGreen
                    )
                } else if (reminder.isEnabled && daysLeft < 0) {
                    InfoChip(
                        icon = Icons.Outlined.Warning,
                        text = "Đã qua",
                        bgColor = Color(0xFFFFEBEE),
                        textColor = Color(0xFFC62828)
                    )
                }

                // Repeat chip
                val repeatLabel = getRepeatLabelV3(reminder.repeatType)
                if (repeatLabel != null) {
                    InfoChip(
                        icon = Icons.Outlined.Replay,
                        text = repeatLabel,
                        bgColor = NoteBlueBg,
                        textColor = NoteBlue
                    )
                }

                // Lunar chip
                if (reminder.useLunar) {
                    InfoChip(
                        icon = Icons.Outlined.DarkMode,
                        text = "Âm lịch",
                        bgColor = Color(0xFFFFF8E1),
                        textColor = Color(0xFFF57F17)
                    )
                }

                // Advance days chip
                if (reminder.advanceDays > 0) {
                    InfoChip(
                        icon = Icons.Outlined.NotificationsActive,
                        text = "Trước ${reminder.advanceDays} ngày",
                        bgColor = RemindOrangeBg,
                        textColor = RemindOrange
                    )
                }

                // Label chips
                if (reminder.labels.isNotBlank()) {
                    reminder.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(1).forEach { label ->
                        val labelColor = when (label) {
                            "Quan trọng" -> Color(0xFFC62828)
                            "Gia đình" -> Color(0xFF7B1FA2)
                            else -> NoteBlue
                        }
                        InfoChip(
                            icon = Icons.AutoMirrored.Outlined.Label,
                            text = label,
                            bgColor = labelColor.copy(alpha = 0.08f),
                            textColor = labelColor
                        )
                    }
                }
            }
        }

        // Toggle
        Switch(
            checked = reminder.isEnabled,
            onCheckedChange = { viewModel.toggleReminder(reminder) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RemindOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.outlineVariant
            ),
            modifier = Modifier.height(22.dp)
        )
    }
}

// ══════════════════════════════════════════
// Shared Components
// ══════════════════════════════════════════

@Composable
private fun SectionDivider(text: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = c.textTertiary,
                letterSpacing = 0.6.sp
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(c.outlineVariant)
        )
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(11.dp))
        Text(
            text,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        )
    }
}

@Composable
private fun EmptyStateV3(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onAddClick: (() -> Unit)? = null,
    addLabel: String = "Thêm mới",
    addColor: Color = NoteBlue
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = c.textQuaternary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = TextStyle(fontSize = 13.sp, color = c.textTertiary)
        )
        if (onAddClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .background(addColor, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddClick() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(
                    addLabel,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun AddNewCard(
    label: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 4.dp)
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color),
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.Add, null, tint = color, modifier = Modifier.size(20.dp))
    }
}

// ══════════════════════════════════════════
// Helper functions
// ══════════════════════════════════════════

private data class ReminderTypeInfo(
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

private fun getReminderTypeInfo(reminder: ReminderEntity): ReminderTypeInfo {
    // Use the category field first
    return when (reminder.category) {
        0 -> ReminderTypeInfo(Icons.Outlined.Celebration, Color(0xFFE65100), Color(0xFFFFF3E0))  // Holiday
        1 -> ReminderTypeInfo(Icons.Outlined.Cake, Color(0xFF7B1FA2), Color(0xFFF3E5F5))          // Birthday
        2 -> ReminderTypeInfo(Icons.Outlined.DarkMode, Color(0xFFF57F17), Color(0xFFFFF8E1))      // Lunar
        3 -> ReminderTypeInfo(Icons.AutoMirrored.Outlined.EventNote, Color(0xFF1565C0), Color(0xFFE3F2FD))     // Personal
        4 -> ReminderTypeInfo(Icons.Outlined.LocalFireDepartment, Color(0xFFC62828), Color(0xFFFFEBEE))  // Memorial (Ngày giỗ)
        else -> {
            // Fallback: auto-detect by title keywords
            val title = reminder.title.lowercase()
            when {
                title.contains("sinh nhật") || title.contains("birthday") ->
                    ReminderTypeInfo(Icons.Outlined.Cake, Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                title.contains("giỗ") ->
                    ReminderTypeInfo(Icons.Outlined.LocalFireDepartment, Color(0xFFC62828), Color(0xFFFFEBEE))
                title.contains("lễ") || title.contains("tết") ->
                    ReminderTypeInfo(Icons.Outlined.Celebration, Color(0xFFE65100), Color(0xFFFFF3E0))
                title.contains("rằm") || title.contains("mùng") ->
                    ReminderTypeInfo(Icons.Outlined.DarkMode, Color(0xFFF57F17), Color(0xFFFFF8E1))
                else ->
                    ReminderTypeInfo(Icons.AutoMirrored.Outlined.EventNote, Color(0xFF1565C0), Color(0xFFE3F2FD))
            }
        }
    }
}

private fun getRepeatLabelV3(repeatType: Int): String? = when (repeatType) {
    1 -> "Hàng ngày"
    2 -> "Hàng tuần"
    3 -> "Hàng tháng"
    4 -> "Hàng tháng (âm)"
    5 -> "Hàng năm"
    else -> null // One-time, no label needed
}

private fun getDaysUntil(targetMillis: Long): Long {
    val now = System.currentTimeMillis()
    val diff = targetMillis - now
    return diff / 86400_000L
}

private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3600_000
    val days = diff / 86400_000

    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "${minutes} phút trước"
        hours < 24 -> "${hours} giờ trước"
        days == 1L -> "Hôm qua"
        days < 7 -> "${days} ngày trước"
        days < 30 -> "${days / 7} tuần trước"
        else -> "${days / 30} tháng trước"
    }
}

// Filter helpers
private fun List<NoteEntity>.filterBySearch(query: String): List<NoteEntity> {
    if (query.isBlank()) return this
    val q = query.lowercase()
    return filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
}

private fun List<NoteEntity>.filterByLabel(label: String): List<NoteEntity> {
    if (label.isBlank()) return this
    val l = label.lowercase()
    return filter { it.labels.lowercase().contains(l) }
}

private fun List<TaskEntity>.filterTaskBySearch(query: String): List<TaskEntity> {
    if (query.isBlank()) return this
    val q = query.lowercase()
    return filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
}

private fun List<TaskEntity>.filterTaskByLabelAndPriority(label: String, priority: Int): List<TaskEntity> {
    var result = this
    if (label.isNotBlank()) {
        val l = label.lowercase()
        result = result.filter { it.labels.lowercase().contains(l) }
    }
    if (priority >= 0) {
        result = result.filter { it.priority == priority }
    }
    return result
}

private fun List<ReminderEntity>.filterReminderBySearch(query: String): List<ReminderEntity> {
    if (query.isBlank()) return this
    val q = query.lowercase()
    return filter { it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) }
}

private fun List<ReminderEntity>.filterReminderByLabel(label: String): List<ReminderEntity> {
    if (label.isBlank()) return this
    val l = label.lowercase()
    return filter { it.labels.lowercase().contains(l) }
}

// ══════════════════════════════════════════
// FILTER BOTTOM SHEET
// ══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NTRFilterSheet(
    selectedTab: NTRTab,
    currentLabel: String,
    currentPriority: Int,
    allLabels: List<String>,
    onApply: (label: String, priority: Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val sheetState = rememberModalBottomSheetState()
    var label by remember { mutableStateOf(currentLabel) }
    var priority by remember { mutableIntStateOf(currentPriority) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(
                "Bộ lọc",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Label filter
            Text(
                "Nhãn",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (allLabels.isEmpty()) {
                Text(
                    "Chưa có nhãn nào",
                    style = TextStyle(fontSize = 12.sp, color = c.outline),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                // Wrap labels in a flow
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allLabels.forEach { l ->
                        val isSelected = l == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) c.primary else c.surfaceContainer,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) c.primary else c.outlineVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { label = if (isSelected) "" else l }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                l,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else c.textSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Priority filter (only for Tasks tab)
            if (selectedTab == NTRTab.TASKS) {
                Text(
                    "Độ ưu tiên",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary),
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    val priorities = listOf(-1 to "Tất cả", 0 to "Thấp", 1 to "TB", 2 to "Cao")
                    priorities.forEach { (p, lbl) ->
                        val isSelected = priority == p
                        val chipColor = when (p) {
                            0 -> TaskGreen
                            1 -> RemindOrange
                            2 -> Color(0xFFC62828)
                            else -> c.primary
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) chipColor else c.surfaceContainer,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) chipColor else c.outlineVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { priority = p }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                lbl,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else c.textSecondary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, c.outlineVariant)
                ) {
                    Text("Đặt lại", color = c.textSecondary)
                }
                Button(
                    onClick = { onApply(label, priority) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    Text("Áp dụng", color = Color.White)
                }
            }
        }
    }
}
