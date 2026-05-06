package com.lichso.app.feature.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoCard
import com.lichso.app.ui.components.LichSoCheckboxRow
import com.lichso.app.ui.components.LichSoDatePickerField
import com.lichso.app.ui.components.LichSoDeleteButton
import com.lichso.app.ui.components.LichSoEmptyState
import com.lichso.app.ui.components.LichSoHeaderActionButton
import com.lichso.app.ui.components.LichSoPrimaryButton
import com.lichso.app.ui.components.LichSoSecondaryButton
import com.lichso.app.ui.components.LichSoTextField
import com.lichso.app.ui.components.LichSoToggleChip
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun CountdownScreen(
    onBackClick: () -> Unit = {},
    viewModel: CountdownViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Đếm ngược sự kiện",
            subtitle = "Hiển thị trên Home / Widget",
            onBackClick = onBackClick,
            actions = {
                LichSoHeaderActionButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Thêm",
                    onClick = viewModel::openAddDialog,
                )
            }
        )

        if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LichSoEmptyState(
                    title = "Chưa có sự kiện nào",
                    message = "Thêm sự kiện để đếm ngược và hiển thị trên Home / Widget",
                    actionText = "Thêm sự kiện đầu tiên",
                    onAction = viewModel::openAddDialog,
                    icon = Icons.Outlined.Info,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { it.entity.id }) { item ->
                    CountdownRow(
                        item = item,
                        onToggleHome = { viewModel.toggleHome(item.entity) },
                        onToggleWidget = { viewModel.toggleWidget(item.entity) },
                        onDelete = { viewModel.delete(item.entity) },
                    )
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddCountdownDialog(
            state = state,
            onDismiss = { viewModel.closeAddDialog() },
            onTitle = viewModel::setTitle,
            onDate = viewModel::setDate,
            onNote = viewModel::setNote,
            onHome = viewModel::setShowOnHome,
            onWidget = viewModel::setShowOnWidget,
            onSave = viewModel::saveNewEvent,
        )
    }
}

@Composable
private fun CountdownRow(
    item: CountdownItemUi,
    onToggleHome: () -> Unit,
    onToggleWidget: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val target = LocalDate.ofEpochDay(item.entity.targetEpochDay)
    val dayText = when {
        item.daysLeft > 0 -> "Còn ${item.daysLeft} ngày"
        item.daysLeft == 0L -> "Hôm nay"
        else -> "Đã qua ${-item.daysLeft} ngày"
    }
    val dayColor = when {
        item.daysLeft > 0 -> Color(0xFF2E7D32)
        item.daysLeft == 0L -> Color(0xFFE65100)
        else -> Color(0xFF757575)
    }

    LichSoCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.entity.title,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                )
                Text(
                    target.format(FMT),
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                )
                if (item.entity.note.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.entity.note,
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                    )
                }
            }
            // Day count badge
            Column(
                modifier = Modifier
                    .background(dayColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    when {
                        item.daysLeft > 0 -> "${item.daysLeft}"
                        item.daysLeft == 0L -> "0"
                        else -> "${-item.daysLeft}"
                    },
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = dayColor),
                )
                Text(
                    when {
                        item.daysLeft > 0 -> "ngày nữa"
                        item.daysLeft == 0L -> "hôm nay"
                        else -> "ngày trước"
                    },
                    style = TextStyle(fontSize = 10.sp, color = dayColor, fontWeight = FontWeight.Medium),
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallToggleChip(
                label = "Home",
                icon = Icons.Outlined.Home,
                checked = item.entity.showOnHome,
                onClick = onToggleHome,
            )
            SmallToggleChip(
                label = "Widget",
                icon = Icons.Outlined.Widgets,
                checked = item.entity.showOnWidget,
                onClick = onToggleWidget,
            )
            Spacer(modifier = Modifier.weight(1f))
            LichSoDeleteButton(onClick = onDelete)
        }
    }
}

@Composable
private fun SmallToggleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onClick: () -> Unit,
) {
    LichSoToggleChip(label = label, icon = icon, checked = checked, onClick = onClick)
}

@Composable
private fun AddCountdownDialog(
    state: CountdownUiState,
    onDismiss: () -> Unit,
    onTitle: (String) -> Unit,
    onDate: (LocalDate) -> Unit,
    onNote: (String) -> Unit,
    onHome: (Boolean) -> Unit,
    onWidget: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sự kiện đếm ngược") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LichSoTextField(
                    value = state.titleInput,
                    onValueChange = onTitle,
                    label = "Tên sự kiện",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                LichSoDatePickerField(
                    label = "Ngày sự kiện",
                    date = state.dateInput,
                    onDateChange = onDate,
                    formatter = FMT,
                    icon = Icons.Filled.CalendarToday,
                )

                LichSoTextField(
                    value = state.noteInput,
                    onValueChange = onNote,
                    label = "Ghi chú (tuỳ chọn)",
                    modifier = Modifier.fillMaxWidth(),
                )

                LichSoCheckboxRow(checked = state.showOnHome, onCheckedChange = onHome, label = "Hiển thị ở Home")
                LichSoCheckboxRow(checked = state.showOnWidget, onCheckedChange = onWidget, label = "Hiển thị trên Widget")
            }
        },
        confirmButton = {
            LichSoPrimaryButton(text = "Lưu", onClick = onSave, enabled = state.titleInput.isNotBlank())
        },
        dismissButton = {
            LichSoSecondaryButton(text = "Hủy", onClick = onDismiss)
        }
    )
}
