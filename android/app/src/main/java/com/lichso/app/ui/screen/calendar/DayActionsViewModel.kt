package com.lichso.app.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.BookmarkDao
import com.lichso.app.data.local.dao.NoteDao
import com.lichso.app.data.local.dao.ReminderDao
import com.lichso.app.data.local.dao.TaskDao
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.data.local.entity.TaskEntity
import com.lichso.app.util.ReviewHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DayActionsUiState(
    // Currently selected date
    val selectedDay: Int = 0,
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,

    // Bookmark state for selected date
    val isBookmarked: Boolean = false,
    val currentBookmark: BookmarkEntity? = null,

    // Bookmarks for the month (for calendar dots)
    val monthBookmarks: List<BookmarkEntity> = emptyList(),

    // All bookmarks
    val allBookmarks: List<BookmarkEntity> = emptyList(),
    val bookmarkCount: Int = 0,

    // Dialogs
    val showAddNoteDialog: Boolean = false,
    val showAddReminderDialog: Boolean = false,
    val showBookmarkLabelDialog: Boolean = false,

    // Notes / Tasks / Reminders for the selected day
    val dayNotes: List<NoteEntity> = emptyList(),
    val dayTasks: List<TaskEntity> = emptyList(),
    val dayReminders: List<ReminderEntity> = emptyList(),

    // Toast
    val toastMessage: String? = null
)

@HiltViewModel
class DayActionsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bookmarkDao: BookmarkDao,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayActionsUiState())
    val uiState: StateFlow<DayActionsUiState> = _uiState.asStateFlow()

    init {
        // Collect all bookmarks count
        viewModelScope.launch {
            bookmarkDao.getCount().collect { count ->
                _uiState.update { it.copy(bookmarkCount = count) }
            }
        }
        viewModelScope.launch {
            bookmarkDao.getAllBookmarks().collect { bookmarks ->
                _uiState.update { it.copy(allBookmarks = bookmarks) }
            }
        }
    }

    /**
     * Called when user selects a date in calendar or day detail
     */
    fun selectDate(day: Int, month: Int, year: Int) {
        _uiState.update {
            it.copy(selectedDay = day, selectedMonth = month, selectedYear = year)
        }
        // Load bookmark for this date
        viewModelScope.launch {
            bookmarkDao.getBookmarkForDate(day, month, year).collect { bookmark ->
                _uiState.update {
                    it.copy(
                        isBookmarked = bookmark != null,
                        currentBookmark = bookmark
                    )
                }
            }
        }
        // Load notes for this date (matched by title prefix "[dd/MM/yyyy]")
        viewModelScope.launch {
            val datePrefix = "[${"%02d".format(day)}/${"%02d".format(month)}/${year}]"
            noteDao.getNotesForDate(datePrefix).collect { notes ->
                _uiState.update { it.copy(dayNotes = notes) }
            }
        }
        // Load tasks for this date (matched by dueDate)
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            taskDao.getTasksForDate(startOfDay).collect { tasks ->
                _uiState.update { it.copy(dayTasks = tasks) }
            }
        }
        // Load reminders for this date (matched by triggerTime range)
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
            reminderDao.getRemindersForDateRange(startOfDay, endOfDay).collect { reminders ->
                _uiState.update { it.copy(dayReminders = reminders) }
            }
        }
    }

    /**
     * Load bookmarks for the month (for calendar dots)
     */
    fun loadMonthBookmarks(month: Int, year: Int) {
        viewModelScope.launch {
            bookmarkDao.getBookmarksForMonth(month, year).collect { bookmarks ->
                _uiState.update { it.copy(monthBookmarks = bookmarks) }
            }
        }
    }

    // ═══ Bookmark Actions ═══

    fun toggleBookmark() {
        val state = _uiState.value
        val day = state.selectedDay
        val month = state.selectedMonth
        val year = state.selectedYear
        if (day == 0) return

        viewModelScope.launch {
            if (state.isBookmarked) {
                bookmarkDao.deleteByDate(day, month, year)
                _uiState.update { it.copy(toastMessage = "Đã bỏ đánh dấu ngày ${"%02d".format(day)}/${"%02d".format(month)}") }
            } else {
                bookmarkDao.insert(
                    BookmarkEntity(
                        solarDay = day,
                        solarMonth = month,
                        solarYear = year,
                        label = "Ngày ${"%02d".format(day)}/${"%02d".format(month)}/${year}"
                    )
                )
                _uiState.update { it.copy(toastMessage = "Đã đánh dấu ngày ${"%02d".format(day)}/${"%02d".format(month)}") }
                // Happy action: user bookmarked a day → trigger smart rating
                ReviewHelper.triggerAfterAction(appContext)
            }
        }
    }

    fun bookmarkWithLabel(label: String) {
        val state = _uiState.value
        val day = state.selectedDay
        val month = state.selectedMonth
        val year = state.selectedYear
        if (day == 0) return

        viewModelScope.launch {
            val existing = bookmarkDao.getBookmarkForDateSync(day, month, year)
            if (existing != null) {
                bookmarkDao.update(existing.copy(label = label))
            } else {
                bookmarkDao.insert(
                    BookmarkEntity(
                        solarDay = day,
                        solarMonth = month,
                        solarYear = year,
                        label = label
                    )
                )
            }
            _uiState.update {
                it.copy(
                    showBookmarkLabelDialog = false,
                    toastMessage = "Đã lưu \"$label\""
                )
            }
            // Happy action: user saved a labelled bookmark
            ReviewHelper.triggerAfterAction(appContext, weight = 2)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.delete(bookmark)
            _uiState.update { it.copy(toastMessage = "Đã xóa đánh dấu") }
        }
    }

    // ═══ Note Actions (for specific day) ═══

    fun showAddNoteForDay() {
        _uiState.update { it.copy(showAddNoteDialog = true) }
    }

    fun hideAddNote() {
        _uiState.update { it.copy(showAddNoteDialog = false) }
    }

    fun addNoteForDay(title: String, content: String, colorIndex: Int = 0) {
        val state = _uiState.value
        if (title.isBlank()) return

        val datePrefix = "${"%02d".format(state.selectedDay)}/${"%02d".format(state.selectedMonth)}/${state.selectedYear}"
        viewModelScope.launch {
            noteDao.insert(
                NoteEntity(
                    title = "[$datePrefix] $title",
                    content = content,
                    colorIndex = colorIndex
                )
            )
            _uiState.update {
                it.copy(
                    showAddNoteDialog = false,
                    toastMessage = "Đã thêm ghi chú cho ngày ${"%02d".format(state.selectedDay)}/${"%02d".format(state.selectedMonth)}"
                )
            }
        }
    }

    // ═══ Reminder Actions (for specific day) ═══

    fun showAddReminderForDay() {
        _uiState.update { it.copy(showAddReminderDialog = true) }
    }

    fun hideAddReminder() {
        _uiState.update { it.copy(showAddReminderDialog = false) }
    }

    fun addReminderForDay(title: String, hour: Int, minute: Int, repeatType: Int = 0) {
        val state = _uiState.value
        if (title.isBlank()) return

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.selectedYear)
            set(Calendar.MONTH, state.selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, state.selectedDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        viewModelScope.launch {
            reminderDao.insert(
                ReminderEntity(
                    title = title,
                    subtitle = "Ngày ${"%02d".format(state.selectedDay)}/${"%02d".format(state.selectedMonth)}/${state.selectedYear}",
                    triggerTime = cal.timeInMillis,
                    repeatType = repeatType
                )
            )
            _uiState.update {
                it.copy(
                    showAddReminderDialog = false,
                    toastMessage = "Đã đặt nhắc nhở \"$title\" lúc ${"%02d".format(hour)}:${"%02d".format(minute)}"
                )
            }
        }
    }

    // ═══ Bookmark Label Dialog ═══

    fun showBookmarkLabel() {
        _uiState.update { it.copy(showBookmarkLabelDialog = true) }
    }

    fun hideBookmarkLabel() {
        _uiState.update { it.copy(showBookmarkLabelDialog = false) }
    }

    // ═══ Toast ═══

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
