package com.lichso.app.ui.screen.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.domain.model.*
import com.lichso.app.ui.screen.home.HomeUiState
import com.lichso.app.ui.screen.home.HomeViewModel
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoDialog
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    onGoodDaysClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onEditVisibilityChanged: (Boolean) -> Unit = {},
    onAskAiClick: (day: Int, month: Int, year: Int) -> Unit = { _, _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDayDetail by remember { mutableStateOf(false) }

    // Day Actions ViewModel (bookmark, note, reminder)
    val dayActionsViewModel: DayActionsViewModel = hiltViewModel()
    val dayActionsState by dayActionsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Toast feedback
    LaunchedEffect(dayActionsState.toastMessage) {
        dayActionsState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            dayActionsViewModel.consumeToast()
        }
    }

    // Load month bookmarks when month changes
    LaunchedEffect(uiState.currentMonth, uiState.currentYear) {
        dayActionsViewModel.loadMonthBookmarks(uiState.currentMonth, uiState.currentYear)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ═══ CALENDAR VIEW (always composed, hidden when detail shown) ═══
        CalendarContent(
            uiState = uiState,
            viewModel = viewModel,
            monthBookmarks = dayActionsState.monthBookmarks,
            onGoodDaysClick = onGoodDaysClick,
            onSearchClick = onSearchClick,
            onMenuClick = onMenuClick,
            onDayClick = { day ->
                viewModel.selectDay(day.solarDay, day.solarMonth, day.solarYear)
                dayActionsViewModel.selectDate(day.solarDay, day.solarMonth, day.solarYear)
                showDayDetail = true
            }
        )

        // ═══ FULL-SCREEN DAY DETAIL (slide up) ═══
        AnimatedVisibility(
            visible = showDayDetail && uiState.dayInfo != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            uiState.dayInfo?.let { info ->
                DayDetailScreen(
                    dayInfo = info,
                    onBackClick = { showDayDetail = false },
                    onShareClick = {
                        val sb = StringBuilder()
                        sb.appendLine("📅 ${info.dayOfWeek}, ${info.solar.dd}/${info.solar.mm}/${info.solar.yy}")
                        sb.appendLine("🌙 Âm lịch: ${info.lunar.day}/${info.lunar.month}/${info.lunar.year}" +
                                if (info.lunar.leap == 1) " (Nhuận)" else "")
                        sb.appendLine("🔮 Ngày ${info.dayCanChi}")
                        sb.appendLine("📆 Tháng ${info.monthCanChi}, Năm ${info.yearCanChi}")
                        sb.appendLine()
                        // Day rating
                        sb.appendLine("⭐ Đánh giá: ${info.dayRating.label} (${info.dayRating.percent}%)")
                        // Holidays
                        info.solarHoliday?.let { sb.appendLine("🎉 $it") }
                        info.lunarHoliday?.let { sb.appendLine("🏮 $it") }
                        // Activities
                        if (info.activities.nenLam.isNotEmpty()) {
                            sb.appendLine()
                            sb.appendLine("✅ Nên làm: ${info.activities.nenLam.take(5).joinToString(", ")}")
                        }
                        if (info.activities.khongNen.isNotEmpty()) {
                            sb.appendLine("❌ Không nên: ${info.activities.khongNen.take(5).joinToString(", ")}")
                        }
                        // Hướng
                        sb.appendLine()
                        sb.appendLine("💰 Thần Tài: ${info.huong.thanTai}")
                        sb.appendLine("😊 Hỷ Thần: ${info.huong.hyThan}")
                        sb.appendLine()
                        sb.appendLine("— Lịch Số · Lịch Vạn Niên")

                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ thông tin ngày"))
                    },
                    onBookmarkClick = { dayActionsViewModel.toggleBookmark() },
                    onAskAiClick = {
                        onAskAiClick(info.solar.dd, info.solar.mm, info.solar.yy)
                    },
                    isBookmarked = dayActionsState.isBookmarked,
                    onAddNoteClick = { dayActionsViewModel.showAddNoteForDay() },
                    onAddReminderClick = { dayActionsViewModel.showAddReminderForDay() },
                    onBookmarkLongClick = { dayActionsViewModel.showBookmarkLabel() },
                    dayNotes = dayActionsState.dayNotes,
                    dayTasks = dayActionsState.dayTasks,
                    dayReminders = dayActionsState.dayReminders
                )
            }
        }
    }

    // ═══ DAY ACTION DIALOGS ═══

    if (dayActionsState.showBookmarkLabelDialog) {
        BookmarkLabelDialog(
            day = dayActionsState.selectedDay,
            month = dayActionsState.selectedMonth,
            year = dayActionsState.selectedYear,
            existingLabel = dayActionsState.currentBookmark?.label ?: "",
            onSave = { label -> dayActionsViewModel.bookmarkWithLabel(label) },
            onDismiss = { dayActionsViewModel.hideBookmarkLabel() }
        )
    }

    // ═══ FULL-SCREEN NOTE/REMINDER EDIT (consistent with TasksScreen3) ═══
    val showNoteOrReminderEdit = dayActionsState.showAddNoteDialog || dayActionsState.showAddReminderDialog

    // Notify parent when edit screen visibility changes (to hide bottom bar)
    LaunchedEffect(showNoteOrReminderEdit) {
        onEditVisibilityChanged(showNoteOrReminderEdit)
    }

    if (showNoteOrReminderEdit) {
        val editInitialType = if (dayActionsState.showAddNoteDialog)
            com.lichso.app.ui.screen.tasks.EditItemType.NOTE
        else
            com.lichso.app.ui.screen.tasks.EditItemType.REMIND

        // Build attached date from current day info
        val attachedDate = com.lichso.app.ui.screen.tasks.AttachedDate(
            day = dayActionsState.selectedDay,
            month = dayActionsState.selectedMonth,
            year = dayActionsState.selectedYear,
            lunarInfo = uiState.dayInfo?.let {
                "${it.lunar.day}/${it.lunar.month} Âm lịch"
            } ?: "",
            holiday = uiState.dayInfo?.let {
                it.lunarHoliday ?: it.solarHoliday ?: ""
            } ?: ""
        )

        Box(modifier = Modifier.fillMaxSize()) {
            com.lichso.app.ui.screen.tasks.NoteTaskEditScreen(
                initialType = editInitialType,
                attachedDate = attachedDate,
                onBackClick = {
                    dayActionsViewModel.hideAddNote()
                    dayActionsViewModel.hideAddReminder()
                },
                onSaveNote = { note ->
                    dayActionsViewModel.addNoteForDay(note.title, note.content, note.colorIndex)
                },
                onSaveTask = { task ->
                    // Tasks are also supported from here
                    dayActionsViewModel.addNoteForDay(task.title, task.description, 0)
                },
                onSaveReminder = { reminder ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = reminder.triggerTime
                    dayActionsViewModel.addReminderForDay(
                        reminder.title,
                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                        cal.get(java.util.Calendar.MINUTE),
                        reminder.repeatType
                    )
                },
                onDelete = {
                    dayActionsViewModel.hideAddNote()
                    dayActionsViewModel.hideAddReminder()
                }
            )
        }
    }
}

@Composable
private fun CalendarContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    monthBookmarks: List<com.lichso.app.data.local.entity.BookmarkEntity> = emptyList(),
    onGoodDaysClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onDayClick: (CalendarDay) -> Unit
) {
    val c = LichSoThemeColors.current
    var showSearchDialog by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        AppTopBar(
            title = "Lịch tháng",
            subtitle = "Âm lịch · ${uiState.dayInfo?.yearCanChi ?: ""}",
            onBackClick = onMenuClick,
            leadingIcon = Icons.Filled.Menu,
            actions = {
                HeaderIconButton(
                    icon = Icons.Filled.Search,
                    contentDescription = "Tìm kiếm",
                    onClick = { onSearchClick() }
                )
                HeaderIconButton(
                    icon = Icons.Filled.EventAvailable,
                    contentDescription = "Ngày tốt",
                    onClick = onGoodDaysClick
                )
            }
        )

        // ═══ MONTH SELECTOR ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.prevMonth() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Tháng trước", tint = c.textPrimary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showMonthYearPicker = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Tháng ${uiState.currentMonth}, ${uiState.currentYear}",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Chọn tháng",
                        tint = c.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "Tháng ${uiState.dayInfo?.lunar?.month ?: ""} Âm lịch",
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Tháng sau", tint = c.textPrimary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ═══ CALENDAR GRID (with swipe left/right to change month) ═══
        val swipeThreshold = 80f
        var swipeCumulativeDrag by remember { mutableStateOf(0f) }

        CalendarGrid(
            days = uiState.calendarDays,
            selectedDate = uiState.selectedDate,
            showLunarBadge = uiState.showLunarBadge,
            showHoangDao = uiState.showHoangDao,
            weekStartSunday = uiState.weekStartSunday,
            bookmarkedDays = monthBookmarks.map { it.solarDay to it.solarMonth }.toSet(),
            onDayClick = onDayClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { swipeCumulativeDrag = 0f },
                        onDragEnd = {
                            if (swipeCumulativeDrag > swipeThreshold) {
                                viewModel.prevMonth()
                            } else if (swipeCumulativeDrag < -swipeThreshold) {
                                viewModel.nextMonth()
                            }
                            swipeCumulativeDrag = 0f
                        },
                        onDragCancel = { swipeCumulativeDrag = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeCumulativeDrag += dragAmount
                        }
                    )
                }
        )

        // ═══ SELECTED DAY DETAIL PANEL (inline, matching HTML) ═══
        uiState.dayInfo?.let { info ->
            SelectedDayDetailPanel(
                dayInfo = info,
                upcomingEvents = uiState.upcomingEvents,
                onTap = {
                    // Find the matching CalendarDay for current selection to trigger full detail
                    val matchingDay = uiState.calendarDays.find {
                        it.solarDay == uiState.selectedDate.dayOfMonth &&
                        it.solarMonth == uiState.selectedDate.monthValue &&
                        it.solarYear == uiState.selectedDate.year &&
                        it.isCurrentMonth
                    }
                    matchingDay?.let { onDayClick(it) }
                }
            )
        }
    }

    // ═══ SEARCH / JUMP DATE DIALOG ═══
    if (showSearchDialog) {
        JumpToDateDialog(
            currentYear = uiState.currentYear,
            currentMonth = uiState.currentMonth,
            onDismiss = { showSearchDialog = false },
            onConfirm = { year, month, day ->
                viewModel.goToDate(year, month, day)
                showSearchDialog = false
            }
        )
    }

    // ═══ MONTH/YEAR PICKER DIALOG ═══
    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentMonth = uiState.currentMonth,
            currentYear = uiState.currentYear,
            onDismiss = { showMonthYearPicker = false },
            onMonthYearSelected = { year, month ->
                viewModel.goToMonth(year, month)
                showMonthYearPicker = false
            }
        )
    }
}

// ═══ Jump to Date Dialog ═══

@Composable
private fun JumpToDateDialog(
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit
) {
    val c = LichSoThemeColors.current
    var dayText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("$currentMonth") }
    var yearText by remember { mutableStateOf("$currentYear") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LichSoDialog(
        onDismiss = onDismiss,
        title = "Đi đến ngày",
        icon = Icons.Filled.Search,
        iconTint = c.primary,
        iconBgColor = c.primary.copy(alpha = 0.12f),
        confirmText = "Đi đến",
        confirmColor = c.primary,
        onConfirm = {
            val year = yearText.toIntOrNull()
            val month = monthText.toIntOrNull()
            val day = dayText.toIntOrNull()
            when {
                year == null || year < 1900 || year > 2100 -> errorMessage = "Năm không hợp lệ (1900-2100)"
                month == null || month < 1 || month > 12 -> errorMessage = "Tháng không hợp lệ (1-12)"
                day == null || day < 1 || day > 31 -> errorMessage = "Ngày không hợp lệ"
                else -> {
                    try {
                        java.time.LocalDate.of(year, month, day)
                        onConfirm(year, month, day)
                    } catch (_: Exception) {
                        errorMessage = "Ngày không hợp lệ cho tháng $month/$year"
                    }
                }
            }
        },
    ) {
        Text(
            "Nhập ngày muốn xem:",
            style = TextStyle(fontSize = 14.sp, color = c.textTertiary)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dayText,
                onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) { dayText = it; errorMessage = null } },
                label = { Text("Ngày", fontSize = 12.sp) },
                placeholder = { Text("1-31", fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            )
            OutlinedTextField(
                value = monthText,
                onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) { monthText = it; errorMessage = null } },
                label = { Text("Tháng", fontSize = 12.sp) },
                placeholder = { Text("1-12", fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            )
            OutlinedTextField(
                value = yearText,
                onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) { yearText = it; errorMessage = null } },
                label = { Text("Năm", fontSize = 12.sp) },
                placeholder = { Text("2025", fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1.2f),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            )
        }
        errorMessage?.let {
            Text(it, style = TextStyle(fontSize = 12.sp, color = Color(0xFFE53935)))
        }
    }
}

// ═══ Month/Year Picker Dialog ═══

private enum class PickerMode { MONTH, YEAR }

@Composable
private fun MonthYearPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onMonthYearSelected: (year: Int, month: Int) -> Unit
) {
    val c = LichSoThemeColors.current
    var pickerMode by remember { mutableStateOf(PickerMode.MONTH) }
    var pickerYear by remember { mutableStateOf(currentYear) }
    val today = java.time.LocalDate.now()

    val monthNames = listOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.bg,
        shape = RoundedCornerShape(24.dp),
        title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = pickerMode,
                    transitionSpec = {
                        fadeIn(tween(200)) + slideInVertically { if (targetState == PickerMode.YEAR) -it / 4 else it / 4 } togetherWith
                        fadeOut(tween(200)) + slideOutVertically { if (targetState == PickerMode.YEAR) it / 4 else -it / 4 }
                    },
                    label = "picker_mode"
                ) { mode ->
                    when (mode) {
                        PickerMode.MONTH -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Year header — clickable to switch to year picker
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { pickerMode = PickerMode.YEAR }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Năm $pickerYear",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Chọn năm",
                                        tint = c.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 12 months in 3x4 grid
                                for (row in 0 until 4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (col in 0 until 3) {
                                            val month = row * 3 + col + 1
                                            val isSelected = month == currentMonth && pickerYear == currentYear
                                            val isCurrent = month == today.monthValue && pickerYear == today.year

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> c.primary
                                                            isCurrent -> c.primary.copy(alpha = 0.12f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .then(
                                                        if (isCurrent && !isSelected) Modifier.border(1.dp, c.primary, RoundedCornerShape(12.dp))
                                                        else Modifier
                                                    )
                                                    .clickable { onMonthYearSelected(pickerYear, month) }
                                                    .padding(vertical = 14.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    monthNames[month - 1],
                                                    style = TextStyle(
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isCurrent -> c.primary
                                                            else -> c.textPrimary
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    if (row < 3) Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        PickerMode.YEAR -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Back to month view
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { pickerMode = PickerMode.MONTH }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowDropUp,
                                        contentDescription = "Quay lại",
                                        tint = c.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Chọn năm",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = c.primary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 16 years in 4x4 grid centered around pickerYear
                                val startYear = pickerYear - 7
                                for (row in 0 until 4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (col in 0 until 4) {
                                            val year = startYear + row * 4 + col
                                            val isSelected = year == currentYear
                                            val isCurrent = year == today.year

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> c.primary
                                                            isCurrent -> c.primary.copy(alpha = 0.12f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .then(
                                                        if (isCurrent && !isSelected) Modifier.border(1.dp, c.primary, RoundedCornerShape(12.dp))
                                                        else Modifier
                                                    )
                                                    .clickable {
                                                        pickerYear = year
                                                        pickerMode = PickerMode.MONTH
                                                    }
                                                    .padding(vertical = 14.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "$year",
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isCurrent -> c.primary
                                                            else -> c.textPrimary
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    if (row < 3) Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Navigation: Previous/Next 16 years
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { pickerYear -= 16 }) {
                                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                                        Text("${startYear - 16}–${startYear - 1}", style = TextStyle(fontSize = 12.sp, color = c.primary))
                                    }
                                    TextButton(onClick = { pickerYear += 16 }) {
                                        Text("${startYear + 16}–${startYear + 31}", style = TextStyle(fontSize = 12.sp, color = c.primary))
                                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = c.textTertiary)
            }
        }
    )
}

// ═══ Selected Day Detail Panel (inline, matches HTML day-detail) ═══

@Composable
private fun SelectedDayDetailPanel(
    dayInfo: DayInfo,
    upcomingEvents: List<UpcomingEvent>,
    onTap: () -> Unit
) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .drawBehind {
                drawLine(
                    color = Color(0xFFD8C2BF),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header: Date + Lunar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${dayInfo.dayOfWeek}, ${"%02d".format(dayInfo.solar.dd)}/${"%02d".format(dayInfo.solar.mm)}/${dayInfo.solar.yy}",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            )
            Text(
                "${if (dayInfo.isRam) "Rằm" else if (dayInfo.isMung1) "Mùng 1" else "Mùng ${dayInfo.lunar.day}"}/${dayInfo.lunar.month} Âm",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.primary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Detail items
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Hoang dao / xau day rating item
            val rating = dayInfo.dayRating
            if (rating.label.isNotEmpty()) {
                val isGood = rating.label == "Rất tốt" || rating.label == "Tốt"
                val itemColor = if (isGood) Color(0xFF2E7D32) else Color(0xFFE65100)
                val icon = if (isGood) Icons.Filled.Verified else Icons.Filled.Warning
                val activitiesHint = if (dayInfo.activities.nenLam.isNotEmpty()) {
                    " — ${dayInfo.activities.nenLam.take(2).joinToString(", ")}"
                } else ""
                DetailChip(
                    icon = icon,
                    text = "Ngày ${rating.label}$activitiesHint",
                    color = itemColor
                )
            }

            // Holidays (solar or lunar)
            dayInfo.solarHoliday?.let { holiday ->
                DetailChip(
                    icon = Icons.Filled.Celebration,
                    text = holiday,
                    color = Color(0xFFE65100)
                )
            }
            dayInfo.lunarHoliday?.let { holiday ->
                DetailChip(
                    icon = Icons.Filled.Celebration,
                    text = holiday,
                    color = Color(0xFFE65100)
                )
            }

            // Nearby upcoming events (from the upcoming events list)
            val nearbyEvent = upcomingEvents.firstOrNull { it.timeLabel != "Hôm nay" }
            nearbyEvent?.let { event ->
                DetailChip(
                    icon = Icons.Filled.HistoryEdu,
                    text = "Gần ${event.title} (${event.timeLabel})",
                    color = Color(0xFF1565C0)
                )
            }

            // If nothing to show, display can chi
            if (rating.label.isEmpty() && dayInfo.solarHoliday == null && dayInfo.lunarHoliday == null && nearbyEvent == null) {
                DetailChip(
                    icon = Icons.Filled.Info,
                    text = "Ngày ${dayInfo.dayCanChi} · ${dayInfo.trucNgay.name}",
                    color = c.outline
                )
            }
        }

        // Hint to see full detail
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Nhấn để xem chi tiết ›",
            style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(
            text,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══ Calendar Grid ═══

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDate: java.time.LocalDate,
    showLunarBadge: Boolean,
    showHoangDao: Boolean,
    weekStartSunday: Boolean,
    bookmarkedDays: Set<Pair<Int, Int>> = emptySet(),
    onDayClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    Column(modifier = modifier) {
        // Weekday header — adapts to week start setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            val weekDays = if (weekStartSunday) {
                listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
            } else {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            }
            weekDays.forEachIndexed { index, label ->
                val isWeekend = if (weekStartSunday) {
                    index == 0 || index == 6  // CN, T7
                } else {
                    index >= 5  // T7, CN
                }
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWeekend) c.primary else c.outline,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Day cells grid
        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                week.forEach { day ->
                    val isSelected = day.isCurrentMonth &&
                            day.solarDay == selectedDate.dayOfMonth &&
                            day.solarMonth == selectedDate.monthValue &&
                            day.solarYear == selectedDate.year &&
                            !day.isToday

                    DayCell(
                        day = day,
                        isSelected = isSelected,
                        showLunarBadge = showLunarBadge,
                        showHoangDao = showHoangDao,
                        isBookmarked = (day.solarDay to day.solarMonth) in bookmarkedDays,
                        onClick = { if (day.isCurrentMonth) onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    showLunarBadge: Boolean,
    showHoangDao: Boolean,
    isBookmarked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    val bgColor = when {
        day.isToday -> c.primary
        isSelected -> c.primaryContainer
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(1.dp)
        ) {
            val textColor = when {
                day.isToday -> Color.White
                !day.isCurrentMonth -> c.outlineVariant
                day.isHoliday || day.isSunday -> c.primary
                isSelected -> c.primary
                day.isSaturday -> c.primary
                else -> c.textPrimary
            }
            val fontWeight = when {
                day.isToday -> FontWeight.Bold
                day.isHoliday -> FontWeight.Bold
                isSelected -> FontWeight.SemiBold
                else -> FontWeight.SemiBold
            }

            Text(
                text = "${day.solarDay}",
                style = TextStyle(fontSize = 16.sp, fontWeight = fontWeight, color = textColor, lineHeight = 16.sp)
            )
            if (showLunarBadge) {
                Text(
                    text = day.lunarDisplayText,
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = if (day.isToday) Color.White.copy(alpha = 0.7f)
                        else if (day.isCurrentMonth) c.outline
                        else c.outlineVariant,
                        lineHeight = 9.sp
                    )
                )
            }
        }

        // Hoang dao day rating indicator
        if (showHoangDao && day.isCurrentMonth && day.dayRatingLabel.isNotEmpty()) {
            val ratingColor = when (day.dayRatingLabel) {
                "Rất tốt" -> c.goodGreen
                "Tốt" -> c.gold
                "Xấu" -> Color(0xFFE53935)
                else -> Color.Transparent // "Trung bình" — no indicator
            }
            if (ratingColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 4.dp, start = 4.dp)
                        .size(6.dp)
                        .background(ratingColor, CircleShape)
                )
            }
        }

        // Green dot for good day
        if (day.hasEvent && day.isCurrentMonth && !day.isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(6.dp)
                    .background(c.goodGreen, CircleShape)
            )
        }

        // Event dot
        if (day.isHoliday && day.isCurrentMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .background(c.gold, CircleShape)
            )
        }

        // Bookmark indicator
        if (isBookmarked && day.isCurrentMonth) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 1.dp)
                    .size(5.dp)
                    .background(Color(0xFFC62828), CircleShape)
            )
        }
    }
}
