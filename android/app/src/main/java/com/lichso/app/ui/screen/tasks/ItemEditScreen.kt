package com.lichso.app.ui.screen.tasks

import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.data.local.entity.ItemEntity
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoDeleteButton
import com.lichso.app.ui.components.LichSoNullableDatePickerField
import com.lichso.app.ui.components.LichSoPrimaryButton
import com.lichso.app.ui.components.LichSoTextField
import com.lichso.app.ui.components.LichSoRichTextEditor
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.ui.theme.screenBackground
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

/** Ngày gắn kèm khi tạo item từ màn lịch (chỉ hiển thị ngữ cảnh). */
data class AttachedDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val lunarInfo: String = "",
    val holiday: String = "",
)

private val repeatLabels = listOf(
    "Một lần", "Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng tháng (Âm)", "Hàng năm"
)
private val advanceOptions = listOf(0 to "Đúng ngày", 1 to "Trước 1 ngày", 3 to "Trước 3 ngày", 7 to "Trước 7 ngày")

/** Bảng màu cho item (index 0..5). */
@Composable
private fun itemColors(): List<Color> {
    val c = LichSoThemeColors.current
    return listOf(c.textTertiary, c.noteGold, c.noteTeal, c.noteOrange, c.notePurple, c.noteGreen)
}

/**
 * Trình soạn thảo hợp nhất cho một [ItemEntity]: title + mô tả + tags, kèm các
 * năng lực bật/tắt: Việc cần làm (priority, hạn) và Nhắc nhở (giờ, lặp, âm lịch).
 */
@Composable
fun ItemEditScreen(
    initialItem: ItemEntity? = null,
    attachedDate: AttachedDate? = null,
    allExistingTags: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onSave: (ItemEntity) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val colors = itemColors()

	var title by remember { mutableStateOf(initialItem?.title ?: "") }
	val richTextState = rememberRichTextState()

	// Set initial rich text HTML content
	LaunchedEffect(initialItem) {
		initialItem?.description?.let {
			richTextState.setHtml(it)
		}
	}

	var tags by remember { mutableStateOf(initialItem?.tags ?: "") }

    var isTask by remember { mutableStateOf(initialItem?.isTask ?: false) }
    var priority by remember { mutableIntStateOf(initialItem?.priority ?: 1) }
    var dueDate by remember { mutableStateOf(initialItem?.dueDate?.toLocalDate()) }

    var hasReminder by remember { mutableStateOf(initialItem?.hasReminder ?: false) }

    // Xin quyền thông báo (Android 13+) đúng lúc user bật "Nhắc nhở" lần đầu —
    // contextual permission, thay cho việc hỏi ngay khi mở app.
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user chọn cho/không — vẫn lưu được nhắc nhở bình thường */ }
    fun requestNotifPermIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val initCal = remember {
        Calendar.getInstance().apply { initialItem?.reminderAt?.let { timeInMillis = it } }
    }
    var reminderDate by remember { mutableStateOf(initialItem?.reminderAt?.toLocalDate() ?: LocalDate.now()) }
    var reminderHour by remember { mutableIntStateOf(initCal.get(Calendar.HOUR_OF_DAY)) }
    var reminderMinute by remember { mutableIntStateOf(initCal.get(Calendar.MINUTE)) }
    var repeatType by remember { mutableIntStateOf(initialItem?.repeatType ?: 0) }
    var useLunar by remember { mutableStateOf(initialItem?.useLunar ?: false) }
    var advanceDays by remember { mutableIntStateOf(initialItem?.advanceDays ?: 0) }

    var isPinned by remember { mutableStateOf(initialItem?.isPinned ?: false) }
    var colorIndex by remember { mutableIntStateOf(initialItem?.colorIndex ?: 0) }

    fun buildItem(): ItemEntity {
        val reminderAt = if (hasReminder) {
            Calendar.getInstance().apply {
                set(reminderDate.year, reminderDate.monthValue - 1, reminderDate.dayOfMonth, reminderHour, reminderMinute, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else null
        return (initialItem ?: ItemEntity(title = "")).copy(
            title = title.trim(),
            description = richTextState.toHtml().trim(),
            tags = tags.trim(),
            isTask = isTask,
            priority = priority,
            dueDate = if (isTask) dueDate?.toEpochMillis() else null,
            hasReminder = hasReminder,
            reminderAt = reminderAt,
            repeatType = repeatType,
            useLunar = useLunar,
            advanceDays = advanceDays,
            reminderEnabled = if (hasReminder) (initialItem?.reminderEnabled ?: true) else true,
            isPinned = isPinned,
            colorIndex = colorIndex,
        )
    }

    val isExisting = initialItem != null && initialItem.id != 0L
    Column(modifier = Modifier.fillMaxSize().screenBackground(c.bg)) {
        AppTopBar(
            title = if (isExisting) "Chỉnh sửa" else "Tạo mục mới",
            onBackClick = onBackClick,
            actions = {
                if (onDelete != null && isExisting) {
                    LichSoDeleteButton(onClick = onDelete)
                    Spacer(Modifier.width(8.dp))
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            attachedDate?.let { d ->
                Text(
                    "Gắn với ngày ${d.day}/${d.month}/${d.year}" +
                        (if (d.lunarInfo.isNotBlank()) " · ${d.lunarInfo}" else ""),
                    style = TextStyle(fontSize = 12.sp, color = c.gold, fontWeight = FontWeight.SemiBold),
                )
            }

            LichSoTextField(value = title, onValueChange = { title = it }, label = "Tiêu đề")
            LichSoRichTextEditor(
                state = richTextState,
                label = "Mô tả",
                placeholder = "Nhập mô tả chi tiết..."
            )
            LichSoTextField(
                value = tags, onValueChange = { tags = it },
                label = "Tags (phân cách bằng dấu phẩy)", leadingIcon = Icons.Outlined.Label,
            )

            if (allExistingTags.isNotEmpty()) {
                val currentTagsList = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Chọn nhanh tag đã có:",
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary, fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allExistingTags.forEach { tag ->
                            val isSelected = currentTagsList.any { it.equals(tag, ignoreCase = true) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) c.primary.copy(alpha = 0.15f) else c.surfaceContainerHigh)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) c.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            tags = currentTagsList
                                                .filter { !it.equals(tag, ignoreCase = true) }
                                                .joinToString(", ")
                                        } else {
                                            tags = (currentTagsList + tag).joinToString(", ")
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = if (isSelected) c.primary else c.textSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── Màu & ghim ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                colors.forEachIndexed { idx, col ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(col.copy(alpha = if (idx == 0) 0.4f else 1f))
                            .border(
                                width = if (colorIndex == idx) 3.dp else 0.dp,
                                color = c.textPrimary, shape = CircleShape,
                            )
                            .clickable { colorIndex = idx },
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Ghim",
                        tint = if (isPinned) c.primary else c.textTertiary,
                    )
                }
            }

            // ── Việc cần làm ──
            SectionToggle(
                title = "Việc cần làm",
                icon = Icons.Outlined.CheckCircle,
                checked = isTask,
                onCheckedChange = { isTask = it },
            )
            if (isTask) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mức ưu tiên", style = TextStyle(fontSize = 13.sp, color = c.textSecondary, fontWeight = FontWeight.SemiBold))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Thấp", 1 to "Vừa", 2 to "Cao").forEach { (p, label) ->
                            ChoiceChip(label = label, selected = priority == p) { priority = p }
                        }
                    }
                    LichSoNullableDatePickerField(
                        label = "Hạn hoàn thành", date = dueDate,
                        onDateChange = { dueDate = it }, placeholder = "Không đặt hạn",
                    )
                }
            }

            // ── Nhắc nhở ──
            SectionToggle(
                title = "Nhắc nhở",
                icon = Icons.Outlined.Notifications,
                checked = hasReminder,
                onCheckedChange = {
                    hasReminder = it
                    if (it) requestNotifPermIfNeeded()
                },
            )
            if (hasReminder) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LichSoNullableDatePickerField(
                        label = "Ngày nhắc", date = reminderDate,
                        onDateChange = { reminderDate = it }, placeholder = "Chọn ngày",
                    )
                    // Giờ nhắc
                    Text("Giờ nhắc", style = TextStyle(fontSize = 13.sp, color = c.textSecondary, fontWeight = FontWeight.SemiBold))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(c.surfaceContainer)
                            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> reminderHour = h; reminderMinute = m },
                                    reminderHour, reminderMinute, true,
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                        Text(
                            "%02d:%02d".format(reminderHour, reminderMinute),
                            style = TextStyle(fontSize = 15.sp, color = c.textPrimary, fontWeight = FontWeight.SemiBold),
                        )
                    }
                    // Lặp lại
                    Text("Lặp lại", style = TextStyle(fontSize = 13.sp, color = c.textSecondary, fontWeight = FontWeight.SemiBold))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeatLabels.forEachIndexed { idx, label ->
                            ChoiceChip(label = label, selected = repeatType == idx) {
                                repeatType = idx
                                if (idx == 4) useLunar = true
                            }
                        }
                    }
                    // Âm lịch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useLunar, onCheckedChange = { useLunar = it })
                        Text("Theo Âm lịch", style = TextStyle(fontSize = 14.sp, color = c.textPrimary))
                    }
                    // Nhắc trước
                    Text("Nhắc trước", style = TextStyle(fontSize = 13.sp, color = c.textSecondary, fontWeight = FontWeight.SemiBold))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        advanceOptions.forEach { (days, label) ->
                            ChoiceChip(label = label, selected = advanceDays == days) { advanceDays = days }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            LichSoPrimaryButton(
                text = "Lưu",
                onClick = {
                    val item = buildItem()
                    if (item.title.isNotBlank() || item.description.isNotBlank()) onSave(item)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() || richTextState.toHtml().isNotBlank(),
                icon = Icons.Filled.Check,
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SectionToggle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, modifier = Modifier.weight(1f), style = TextStyle(fontSize = 15.sp, color = c.textPrimary, fontWeight = FontWeight.SemiBold))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) c.primaryContainer else c.surfaceContainerHigh)
            .border(1.dp, if (selected) c.primary else c.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 13.sp,
                color = if (selected) c.primary else c.textSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
    }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
