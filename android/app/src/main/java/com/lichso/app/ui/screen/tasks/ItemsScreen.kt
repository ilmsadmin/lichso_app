package com.lichso.app.ui.screen.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.ItemEntity
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoEmptyState
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.ui.theme.screenBackground
import com.lichso.app.util.stripHtml

private val filterLabels = listOf(
    ItemFilter.ALL to "Tất cả",
    ItemFilter.TASKS to "Cần làm",
    ItemFilter.REMINDERS to "Có nhắc",
    ItemFilter.NOTES to "Ghi chú",
    ItemFilter.PINNED to "Đã ghim",
)

@Composable
fun ItemsScreen(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onEditVisibilityChanged: (Boolean) -> Unit = {},
    viewModel: ItemsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ── Editor lồng (full-screen) ──
    var editing by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ItemEntity?>(null) }
    // Khi ô nhập AI được focus → ẩn thanh điều hướng dưới để thanh nhập nằm sát
    // ngay trên bàn phím (không bị che, không hở khoảng trống).
    // Chỉ ẩn thanh điều hướng khi MỞ EDITOR full-screen — KHÔNG ẩn khi chỉ focus ô AI
    // (giữ nguyên bottom menu, tránh giật nội dung). Bàn phím che ô AI được xử lý bằng
    // imePadding() trên thanh AI bên dưới.
    LaunchedEffect(editing) { onEditVisibilityChanged(editing) }

    // Back khi editor đang mở → đóng editor (thay vì pop route rời khỏi màn tasks).
    BackHandler(enabled = editing) { editing = false }
    // An toàn: khi màn rời composition (vd back pop route), trả lại thanh điều hướng.
    DisposableEffect(Unit) { onDispose { onEditVisibilityChanged(false) } }

    // Xác nhận xoá (hiển thị ở cả màn list lẫn sau khi đóng editor)
    DeleteConfirm(state.deletingItem, viewModel)

    if (editing) {
        ItemEditScreen(
            initialItem = editingItem,
            allExistingTags = state.allTags,
            onBackClick = { editing = false },
            onSave = { item -> viewModel.saveItem(item); editing = false },
            onDelete = editingItem?.let { item -> { viewModel.requestDelete(item); editing = false } },
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().screenBackground(c.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Sổ tay",
                subtitle = "${state.pendingTaskCount} việc · ${state.reminderCount} nhắc · ${state.totalCount} mục",
                leadingIcon = Icons.Filled.Menu,
                onBackClick = onMenuClick,
                actions = {
                    HeaderIconButton(
                        icon = Icons.Filled.Add,
                        contentDescription = "Tạo mục mới",
                        onClick = { editingItem = null; editing = true },
                    )
                },
            )

            // Tìm kiếm
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo tiêu đề, mô tả, tag…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterLabels.forEach { (f, label) ->
                    FilterChipItem(label = label, selected = state.filter == f) { viewModel.setFilter(f) }
                }
            }

            // Danh sách các Tags của Sổ tay
            if (state.allTags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChipItem(
                        label = "Tất cả tag",
                        selected = state.selectedTag == null
                    ) {
                        viewModel.setSelectedTag(null)
                    }
                    state.allTags.forEach { tag ->
                        TagChipItem(
                            label = tag,
                            selected = state.selectedTag == tag
                        ) {
                            if (state.selectedTag == tag) viewModel.setSelectedTag(null)
                            else viewModel.setSelectedTag(tag)
                        }
                    }
                }
            }

            // AI message
            state.aiMessage?.let { msg ->
                AiCard(text = msg, isError = false, onDismiss = viewModel::dismissAiMessage)
            }
            state.aiError?.let { msg ->
                AiCard(text = msg, isError = true, onDismiss = viewModel::dismissAiMessage)
            }

            val list = state.visibleItems
            if (list.isEmpty()) {
                LichSoEmptyState(
                    title = "Chưa có mục nào",
                    message = "Tạo ghi chú, việc cần làm hoặc nhắc nhở — tất cả trong một.",
                    actionText = "Tạo mục mới",
                    onAction = { editingItem = null; editing = true },
                    icon = Icons.Outlined.NoteAdd,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(list, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            viewModel = viewModel,
                            onClick = { editingItem = item; editing = true },
                            onToggleDone = { viewModel.toggleDone(item) },
                        )
                    }
                }
            }
        }

        // ── Thanh lệnh AI (gọn, 1 dòng) ──
        AiCommandBar(
            isProcessing = state.isAiProcessing,
            onSend = viewModel::processAiCommand,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        )
    }
}

@Composable
private fun ItemRow(
    item: ItemEntity,
    viewModel: ItemsViewModel,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val colors = listOf(c.textTertiary, c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen)
    val accent = colors.getOrElse(item.colorIndex) { c.textTertiary }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (item.isTask) {
            Icon(
                if (item.isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (item.isDone) "Đã xong" else "Chưa xong",
                tint = if (item.isDone) c.goodGreen else accent,
                modifier = Modifier.size(24.dp).clip(CircleShape).clickable(onClick = onToggleDone),
            )
        } else {
            Box(modifier = Modifier.padding(top = 4.dp).size(12.dp).clip(CircleShape).background(accent))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                    ),
                )
                if (item.isPinned) {
                    Icon(Icons.Filled.PushPin, contentDescription = "Đã ghim", tint = c.primary, modifier = Modifier.size(15.dp))
                }
            }
            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    item.description.stripHtml(), maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                )
            }
            // Badges
            val badges = buildList {
                if (item.isTask) add(Triple(Icons.Outlined.Flag, viewModel.getPriorityLabel(item.priority), null as (() -> Unit)?))
                item.dueDate?.let { if (item.isTask) add(Triple(Icons.Outlined.Event, viewModel.formatDate(it), null)) }
                if (item.hasReminder) item.reminderAt?.let {
                    add(Triple(Icons.Outlined.Notifications, viewModel.formatDateTime(it), null))
                }
                item.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3).forEach { tag ->
                    add(Triple(Icons.Outlined.Label, tag, { viewModel.setSelectedTag(tag) }))
                }
            }
            if (badges.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    badges.forEach { (icon, label, onClick) -> Badge(icon, label, onClick) }
                }
            }
        }
    }
}

@Composable
private fun Badge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val c = LichSoThemeColors.current
    Row(
		modifier = Modifier
			.clip(RoundedCornerShape(8.dp))
			.background(c.surfaceContainerHigh)
			.let {
				if (onClick != null) it.clickable(onClick = onClick) else it
			}
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icon, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(12.dp))
        Text(label, style = TextStyle(fontSize = 11.sp, color = c.textSecondary), maxLines = 1)
    }
}

@Composable
private fun TagChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) c.primary.copy(alpha = 0.15f) else c.surfaceContainerHigh)
            .border(
                width = 1.dp,
                color = if (selected) c.primary else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Label,
            contentDescription = null,
            tint = if (selected) c.primary else c.textTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) c.primary else c.textSecondary
            )
        )
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) c.primary else c.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) Color.White else c.textSecondary,
            ),
        )
    }
}

@Composable
private fun AiCommandBar(
    isProcessing: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LichSoThemeColors.current
    var text by remember { mutableStateOf("") }
    val canSend = text.isNotBlank() && !isProcessing

    // Nâng thanh nhập áp SÁT mép bàn phím. Vùng nội dung đã chừa sẵn chỗ cho thanh
    // điều hướng dưới (≈80dp + nav inset, xem bottomBarTotalHeight ở LichSoMainScreen),
    // nên chỉ nâng phần bàn phím vượt quá khoảng đã chừa đó (tránh hở như imePadding).
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navBottom = WindowInsets.navigationBars.getBottom(density)
    val reservedPx = with(density) { 80.dp.toPx() } + navBottom
    val keyboardLift = with(density) { (imeBottom - reservedPx).coerceAtLeast(0f).toDp() }

    Row(
        modifier = modifier
            .background(c.bg)
            .padding(bottom = keyboardLift)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.surfaceContainerHigh)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(22.dp))
            .padding(start = 14.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = c.gold, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
            cursorBrush = SolidColor(c.primary),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text("Hỏi AI tạo nhanh…", style = TextStyle(fontSize = 14.sp, color = c.textTertiary))
                }
                inner()
            },
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (canSend) c.primary else c.outlineVariant)
                .clickable(enabled = canSend) { onSend(text); text = "" },
            contentAlignment = Alignment.Center,
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AiCard(text: String, isError: Boolean, onDismiss: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isError) c.red.copy(alpha = 0.12f) else c.primaryContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = if (isError) c.red else c.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 13.sp, color = c.textPrimary))
        Icon(
            Icons.Filled.Close, contentDescription = "Đóng",
            tint = c.textTertiary,
            modifier = Modifier.size(18.dp).clickable(onClick = onDismiss),
        )
    }
}

@Composable
private fun DeleteConfirm(item: ItemEntity?, viewModel: ItemsViewModel) {
    if (item == null) return
    AlertDialog(
        onDismissRequest = viewModel::cancelDelete,
        title = { Text("Xoá mục này?") },
        text = { Text("“${item.title}” sẽ bị xoá vĩnh viễn.") },
        confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("Xoá") } },
        dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("Huỷ") } },
    )
}
