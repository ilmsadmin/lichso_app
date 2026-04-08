package com.lichso.app.ui.screen.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.domain.model.*
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.ui.components.HeaderIconButton

// ══════════════════════════════════════════════════════════════
// Màn hình Chi tiết Ngày — Full-screen (based on v2 HTML mock)
// ══════════════════════════════════════════════════════════════

@Composable
fun DayDetailScreen(
    dayInfo: DayInfo,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onAskAiClick: () -> Unit = {},
    isBookmarked: Boolean = false,
    onAddNoteClick: () -> Unit = {},
    onAddReminderClick: () -> Unit = {},
    onBookmarkLongClick: () -> Unit = {},
    dayNotes: List<NoteEntity> = emptyList(),
    dayTasks: List<TaskEntity> = emptyList(),
    dayReminders: List<ReminderEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ HERO HEADER ═══
        DayDetailHero(
            dayInfo = dayInfo,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onBookmarkClick = onBookmarkClick,
            isBookmarked = isBookmarked
        )

        // ═══ SCROLLABLE CONTENT ═══
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // ── Day Actions Bar (Bookmark, Note, Reminder) ──
            DayActionsBar(
                isBookmarked = isBookmarked,
                onBookmarkClick = onBookmarkClick,
                onBookmarkLongClick = onBookmarkLongClick,
                onAddNoteClick = onAddNoteClick,
                onAddReminderClick = onAddReminderClick
            )

            // ── Notes / Tasks / Reminders for this day ──
            if (dayNotes.isNotEmpty() || dayTasks.isNotEmpty() || dayReminders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(icon = Icons.Filled.EditNote, title = "Ghi chú & Nhắc nhở")
                Spacer(modifier = Modifier.height(10.dp))
                DayNoteTaskReminderSection(
                    notes = dayNotes,
                    tasks = dayTasks,
                    reminders = dayReminders,
                    onAddNoteClick = onAddNoteClick,
                    onAddReminderClick = onAddReminderClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Info Grid (2×2) ──
            InfoGrid(dayInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Giờ Hoàng Đạo ──
            SectionTitle(icon = Icons.Filled.Schedule, title = "Giờ hoàng đạo")
            Spacer(modifier = Modifier.height(10.dp))
            HoursGrid(dayInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Việc nên / kỵ ──
            SectionTitle(icon = Icons.Filled.Checklist, title = "Việc nên / kỵ")
            Spacer(modifier = Modifier.height(10.dp))
            ActivitiesSection(dayInfo.activities)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Sự kiện & Ghi chú ──
            SectionTitle(icon = Icons.Filled.Event, title = "Sự kiện & Ghi chú")
            Spacer(modifier = Modifier.height(10.dp))
            EventsSection(dayInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Quote ──
            QuoteCard()

            Spacer(modifier = Modifier.height(16.dp))

            // ── Ask AI Button ──
            AskAiButton(onClick = onAskAiClick)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ══════════════════════════════════════════
// HERO HEADER
// ══════════════════════════════════════════
@Composable
private fun DayDetailHero(
    dayInfo: DayInfo,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    isBookmarked: Boolean = false
) {
    val c = LichSoThemeColors.current
    val primary = c.primary
    val deepRed = c.deepRed
    val heroGradient = if (c.isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF5D1212), Color(0xFF7F1D1D), Color(0xFF4A1010))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(primary, Color(0xFFD32F2F), deepRed)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Nav row ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    onClick = onBackClick
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HeaderIconButton(
                        icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Đã lưu" else "Lưu",
                        onClick = onBookmarkClick
                    )
                    HeaderIconButton(
                        icon = Icons.Filled.Share,
                        contentDescription = "Chia sẻ",
                        onClick = onShareClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Big date number ───
            Text(
                text = "%02d".format(dayInfo.solar.dd),
                style = TextStyle(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 80.sp
                )
            )

            // ─── Weekday ───
            Text(
                text = dayInfo.dayOfWeek,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )
            )

            // ─── Month / Year ───
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tháng ${dayInfo.solar.mm}, ${dayInfo.solar.yy}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )

            // ─── Lunar info row ───
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Lunar chip
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🌙", fontSize = 16.sp)
                        Text(
                            text = "${dayInfo.lunar.day} tháng ${dayInfo.lunar.month} Âm · ${dayInfo.dayCanChi}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Hoàng đạo / Hắc đạo chip
                val isGoodDay = !dayInfo.activities.isXauDay
                Box(
                    modifier = Modifier
                        .background(
                            if (isGoodDay) Color(0xFF4CAF50).copy(alpha = 0.25f)
                            else Color(0xFFF44336).copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isGoodDay) "✦ Hoàng Đạo" else "✦ Hắc Đạo",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoodDay) Color(0xFFA5D6A7) else Color(0xFFEF9A9A)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ══════════════════════════════════════════
// SECTION TITLE
// ══════════════════════════════════════════
@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = c.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary
            )
        )
    }
}

// ══════════════════════════════════════════
// GHI CHÚ & NHẮC NHỞ CỦA NGÀY
// ══════════════════════════════════════════
@Composable
private fun DayNoteTaskReminderSection(
    notes: List<NoteEntity>,
    tasks: List<TaskEntity>,
    reminders: List<ReminderEntity>,
    onAddNoteClick: () -> Unit = {},
    onAddReminderClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // ── Notes ──
        notes.forEach { note ->
            val noteColors = listOf(
                Color(0xFFD4A017), // Gold
                Color(0xFF00897B), // Teal
                Color(0xFFE65100), // Orange
                Color(0xFF7B1FA2), // Purple
                Color(0xFF2E7D32), // Green
            )
            val accentColor = noteColors.getOrElse(note.colorIndex) { noteColors[0] }
            val bgColor = if (c.isDark) accentColor.copy(alpha = 0.12f) else accentColor.copy(alpha = 0.08f)
            val borderColor = if (c.isDark) accentColor.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.25f)

            // Strip date prefix from title for display
            val displayTitle = note.title.replace(Regex("^\\[\\d{2}/\\d{2}/\\d{4}]\\s*"), "")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.StickyNote2,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle.ifBlank { "Ghi chú" },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = note.content,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = c.textSecondary,
                                lineHeight = 17.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ── Tasks ──
        tasks.forEach { task ->
            val taskColor = Color(0xFF2E7D32) // TaskGreen
            val bgColor = if (c.isDark) taskColor.copy(alpha = 0.12f) else taskColor.copy(alpha = 0.08f)
            val borderColor = if (c.isDark) taskColor.copy(alpha = 0.3f) else taskColor.copy(alpha = 0.25f)

            val priorityLabel = when (task.priority) {
                2 -> "Cao"
                1 -> "TB"
                else -> "Thấp"
            }
            val priorityColor = when (task.priority) {
                2 -> Color(0xFFE53935)
                1 -> Color(0xFFFFA726)
                else -> c.textTertiary
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(taskColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (task.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = taskColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = task.title.ifBlank { "Công việc" },
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // Priority badge
                        Box(
                            modifier = Modifier
                                .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = priorityLabel,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor
                                )
                            )
                        }
                    }
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = c.textSecondary,
                                lineHeight = 17.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (task.dueTime != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = c.textTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = task.dueTime,
                                style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                            )
                        }
                    }
                }
            }
        }

        // ── Reminders ──
        reminders.forEach { reminder ->
            val remindColor = Color(0xFFE65100) // RemindOrange
            val bgColor = if (c.isDark) remindColor.copy(alpha = 0.12f) else remindColor.copy(alpha = 0.08f)
            val borderColor = if (c.isDark) remindColor.copy(alpha = 0.3f) else remindColor.copy(alpha = 0.25f)

            val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
            val timeStr = timeFormat.format(Date(reminder.triggerTime))

            val repeatLabel = when (reminder.repeatType) {
                1 -> "Hàng ngày"
                2 -> "Hàng tuần"
                3 -> "Hàng tháng"
                4 -> "Âm lịch"
                else -> "Một lần"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(remindColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = remindColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title.ifBlank { "Nhắc nhở" },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = remindColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = timeStr,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = remindColor
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(c.surfaceContainerHigh, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = repeatLabel,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = c.textTertiary
                                )
                            )
                        }
                        if (!reminder.isEnabled) {
                            Text(
                                text = "Đã tắt",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = c.textTertiary
                                )
                            )
                        }
                    }
                    if (reminder.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reminder.subtitle,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = c.textTertiary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// INFO GRID (2×2)
// ══════════════════════════════════════════
@Composable
private fun InfoGrid(dayInfo: DayInfo) {
    val c = LichSoThemeColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(
                icon = Icons.Filled.AutoAwesome,
                label = "Can chi",
                value = "Ngày ${dayInfo.dayCanChi}",
                valueColor = c.primary,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Filled.Star,
                label = "Trực",
                value = dayInfo.trucNgay.name,
                valueColor = when (dayInfo.trucNgay.rating) {
                    "Tốt" -> c.goodGreen; "Xấu" -> c.badRed; else -> c.textPrimary
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(
                icon = Icons.Filled.Shield,
                label = "Sao tốt",
                value = dayInfo.saoChieu.name,
                valueColor = when (dayInfo.saoChieu.rating) {
                    "Tốt" -> c.goodGreen; "Xấu" -> c.badRed; else -> c.textPrimary
                },
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Filled.Explore,
                label = "Hướng tốt",
                value = dayInfo.huong.thanTai,
                valueColor = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = c.outline, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.outline)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        )
    }
}

// ══════════════════════════════════════════
// GIỜ HOÀNG ĐẠO GRID
// ══════════════════════════════════════════
private data class HourEntry(
    val name: String,
    val time: String,
    val type: HourType
)

private enum class HourType { GOOD, BAD, NEUTRAL }

@Composable
private fun HoursGrid(dayInfo: DayInfo) {
    val c = LichSoThemeColors.current
    val hoangDaoNames = dayInfo.gioHoangDao.map { it.name }.toSet()

    // 12 giờ can chi cố định
    val allHours = listOf(
        "Tý" to "23h - 1h",
        "Sửu" to "1h - 3h",
        "Dần" to "3h - 5h",
        "Mão" to "5h - 7h",
        "Thìn" to "7h - 9h",
        "Tỵ" to "9h - 11h",
        "Ngọ" to "11h - 13h",
        "Mùi" to "13h - 15h",
        "Thân" to "15h - 17h",
        "Dậu" to "17h - 19h",
        "Tuất" to "19h - 21h",
        "Hợi" to "21h - 23h"
    )

    // Xác định giờ tốt/xấu/trung bình
    val hours = allHours.map { (name, time) ->
        val isGood = hoangDaoNames.contains(name)
        HourEntry(name, time, if (isGood) HourType.GOOD else HourType.NEUTRAL)
    }

    // Layout: 4 columns × 3 rows (FlowRow-style)
    val rows = hours.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { hour ->
                    HourChip(
                        hour = hour,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if < 4 items
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HourChip(hour: HourEntry, modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current

    val bgColor = when (hour.type) {
        HourType.GOOD -> if (c.isDark) Color(0xFF1B3A2F) else Color(0xFFE8F5E9)
        HourType.BAD -> if (c.isDark) Color(0xFF3A1B1B) else Color(0xFFFFEBEE)
        HourType.NEUTRAL -> c.surfaceContainer
    }
    val borderColor = when (hour.type) {
        HourType.GOOD -> if (c.isDark) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color(0xFF81C784)
        HourType.BAD -> if (c.isDark) Color(0xFFE57373).copy(alpha = 0.4f) else Color(0xFFEF9A9A)
        HourType.NEUTRAL -> c.outlineVariant
    }
    val textColor = when (hour.type) {
        HourType.GOOD -> c.goodGreen
        HourType.BAD -> c.badRed
        HourType.NEUTRAL -> c.textPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = hour.name,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
        Text(
            text = hour.time,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.7f)
            )
        )
    }
}

// ══════════════════════════════════════════
// ACTIVITIES (Nên làm / Nên tránh)
// ══════════════════════════════════════════
@Composable
private fun ActivitiesSection(activities: DayActivitiesInfo) {
    val c = LichSoThemeColors.current

    // ── Việc nên làm ──
    if (activities.nenLam.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = c.goodGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Việc nên làm",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.goodGreen)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ActivityTagsFlow(
            items = activities.nenLam,
            isGood = true
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    // ── Việc nên tránh ──
    if (activities.khongNen.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                Icons.Filled.Cancel,
                contentDescription = null,
                tint = c.badRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Việc nên tránh",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.badRed)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ActivityTagsFlow(
            items = activities.khongNen,
            isGood = false
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTagsFlow(items: List<String>, isGood: Boolean) {
    val c = LichSoThemeColors.current

    val bgColor = if (isGood) {
        if (c.isDark) Color(0xFF1B3A2F) else Color(0xFFE8F5E9)
    } else {
        if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF3E0)
    }
    val textColor = if (isGood) c.goodGreen else Color(0xFFE65100)
    val borderColor = if (isGood) {
        if (c.isDark) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color(0xFFC8E6C9)
    } else {
        if (c.isDark) Color(0xFFFF9800).copy(alpha = 0.3f) else Color(0xFFFFCC80)
    }

    val iconMap = mapOf(
        "Khai trương" to Icons.Filled.Store,
        "Xuất hành" to Icons.Filled.FlightTakeoff,
        "Cưới hỏi" to Icons.Filled.Favorite,
        "Ký kết" to Icons.Filled.RealEstateAgent,
        "Nhập trạch" to Icons.Filled.Home,
        "Giao dịch" to Icons.Filled.LocalShipping,
        "Kiện tụng" to Icons.Filled.Gavel,
        "Đào đất" to Icons.Filled.Construction,
        "Đào giếng" to Icons.Filled.WaterDrop,
        "Cầu phúc" to Icons.Filled.VolunteerActivism,
        "Cúng tế" to Icons.Filled.TempleHindu,
        "Dọn nhà" to Icons.Filled.CleaningServices,
        "An táng" to Icons.Filled.Yard,
        "Động thổ" to Icons.Filled.Foundation,
        "Xây dựng" to Icons.Filled.Architecture,
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        items.forEach { item ->
            val icon = iconMap[item]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = item,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// EVENTS SECTION
// ══════════════════════════════════════════
@Composable
private fun EventsSection(dayInfo: DayInfo) {
    val c = LichSoThemeColors.current

    // ── Holiday event ──
    val holiday = dayInfo.solarHoliday ?: dayInfo.lunarHoliday
    if (holiday != null) {
        EventCard(
            icon = Icons.Filled.Celebration,
            iconBg = if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF3E0),
            iconTint = Color(0xFFE65100),
            title = holiday,
            description = "Ngày lễ / Sự kiện đặc biệt"
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    // ── "Ngày này năm xưa" placeholder ──
    EventCard(
        icon = Icons.Filled.HistoryEdu,
        iconBg = if (c.isDark) Color(0xFF1B3A2F) else Color(0xFFE8F5E9),
        iconTint = c.goodGreen,
        title = "Ngày này năm xưa",
        description = "%02d/%02d — Khám phá sự kiện lịch sử ngày này.".format(
            dayInfo.solar.dd, dayInfo.solar.mm
        )
    )
}

@Composable
private fun EventCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = c.textSecondary,
                    lineHeight = 17.sp
                )
            )
        }
    }
}

// ══════════════════════════════════════════
// QUOTE CARD
// ══════════════════════════════════════════
@Composable
private fun QuoteCard() {
    val c = LichSoThemeColors.current

    val quoteBg = if (c.isDark) {
        Brush.linearGradient(listOf(Color(0xFF2A2510), Color(0xFF2E2812)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7)))
    }
    val quoteBorder = if (c.isDark) Color(0xFFFFE082).copy(alpha = 0.2f) else Color(0xFFFFE082)

    // Lấy ngẫu nhiên từ thư viện ca dao, tục ngữ, thành ngữ, câu nói nổi tiếng
    val (quoteText, quoteAuthor) = remember {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        com.lichso.app.data.VietnameseQuotes.ofDay(dayOfYear)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(quoteBg, RoundedCornerShape(16.dp))
            .border(1.dp, quoteBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u201C$quoteText\u201D",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = c.textSecondary,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "— $quoteAuthor",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = c.outline
            )
        )
    }
}

// ══════════════════════════════════════════
// ASK AI BUTTON
// ══════════════════════════════════════════
@Composable
private fun AskAiButton(onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    val btnGradient = if (c.isDark) {
        Brush.linearGradient(listOf(Color(0xFF7F1D1D), Color(0xFF5D1212)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFFC62828)))
    }
    val gold = Color(0xFFD4A017)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                btnGradient,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = gold,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Hỏi AI về ngày này",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}
