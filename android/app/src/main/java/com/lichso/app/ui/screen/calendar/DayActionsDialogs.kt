package com.lichso.app.ui.screen.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.theme.LichSoThemeColors

// ══════════════════════════════════════════
// BOOKMARK LABEL DIALOG
// ══════════════════════════════════════════

@Composable
fun BookmarkLabelDialog(
    day: Int,
    month: Int,
    year: Int,
    existingLabel: String = "",
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    var label by remember { mutableStateOf(existingLabel.ifEmpty { "Ngày ${"%02d".format(day)}/${"%02d".format(month)}/${year}" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BookmarkAdd, null, tint = Color(0xFFC62828), modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text(
                "Đánh dấu ngày",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ngày ${"%02d".format(day)}/${"%02d".format(month)}/${year}",
                    style = TextStyle(fontSize = 14.sp, color = c.textSecondary)
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nhãn ghi nhớ", fontSize = 12.sp) },
                    placeholder = { Text("VD: Sinh nhật, Kỷ niệm...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 15.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(label) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Lưu", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

// ══════════════════════════════════════════
// ADD NOTE FOR DAY DIALOG
// ══════════════════════════════════════════

@Composable
fun AddNoteForDayDialog(
    day: Int,
    month: Int,
    year: Int,
    onAdd: (title: String, content: String, colorIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }

    val noteColors = listOf(
        Color(0xFFFFF9C4), // gold
        Color(0xFFB2DFDB), // teal
        Color(0xFFFFCCBC), // orange
        Color(0xFFE1BEE7), // purple
        Color(0xFFC8E6C9), // green
        Color(0xFFFFCDD2)  // red
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.NoteAdd, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text(
                "Thêm ghi chú",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ngày ${"%02d".format(day)}/${"%02d".format(month)}/${year}",
                    style = TextStyle(fontSize = 13.sp, color = c.textSecondary)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 15.sp)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Nội dung", fontSize = 12.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 14.sp)
                )
                // Color picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    noteColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color, CircleShape)
                                .then(
                                    if (index == selectedColor)
                                        Modifier.border(2.dp, c.primary, CircleShape)
                                    else Modifier.border(1.dp, c.outlineVariant, CircleShape)
                                )
                                .clickable { selectedColor = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == selectedColor) {
                                Icon(Icons.Filled.Check, null, tint = c.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, content, selectedColor) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Thêm", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

// ══════════════════════════════════════════
// ADD REMINDER FOR DAY DIALOG
// ══════════════════════════════════════════

@Composable
fun AddReminderForDayDialog(
    day: Int,
    month: Int,
    year: Int,
    onAdd: (title: String, hour: Int, minute: Int, repeatType: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    var title by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("08") }
    var minuteText by remember { mutableStateOf("00") }
    var repeatType by remember { mutableIntStateOf(0) }

    val repeatOptions = listOf("Một lần", "Hàng ngày", "Hàng tuần", "Hàng tháng")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFF8E1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.NotificationAdd, null, tint = Color(0xFFF57F17), modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text(
                "Đặt nhắc nhở",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Ngày ${"%02d".format(day)}/${"%02d".format(month)}/${year}",
                    style = TextStyle(fontSize = 13.sp, color = c.textSecondary)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề nhắc nhở", fontSize = 12.sp) },
                    placeholder = { Text("VD: Cúng rằm, Họp gia đình...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 15.sp)
                )
                // Time input
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = {
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) hourText = it
                        },
                        label = { Text("Giờ", fontSize = 11.sp) },
                        placeholder = { Text("08", fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Text(":", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = {
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) minuteText = it
                        },
                        label = { Text("Phút", fontSize = 11.sp) },
                        placeholder = { Text("00", fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
                // Repeat type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeatOptions.forEachIndexed { index, label ->
                        val isSelected = repeatType == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFFF57F17).copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFF57F17) else c.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { repeatType = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                label,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFF57F17) else c.textSecondary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 8
                    val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    if (title.isNotBlank()) onAdd(title, h, m, repeatType)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Đặt nhắc", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Huỷ", color = c.textSecondary) }
        }
    )
}

// ══════════════════════════════════════════
// DAY ACTIONS BAR — shows inside DayDetail
// ══════════════════════════════════════════

@Composable
fun DayActionsBar(
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onBookmarkLongClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bookmark
        ActionButton(
            icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            label = if (isBookmarked) "Đã lưu" else "Đánh dấu",
            tint = if (isBookmarked) Color(0xFFC62828) else c.textSecondary,
            onClick = onBookmarkClick
        )

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(c.outlineVariant)
        )

        // Add Note
        ActionButton(
            icon = Icons.Filled.NoteAdd,
            label = "Ghi chú",
            tint = Color(0xFF2E7D32),
            onClick = onAddNoteClick
        )

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(c.outlineVariant)
        )

        // Add Reminder
        ActionButton(
            icon = Icons.Filled.NotificationAdd,
            label = "Nhắc nhở",
            tint = Color(0xFFF57F17),
            onClick = onAddReminderClick
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = tint)
        )
    }
}
