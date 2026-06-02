package com.lichso.app.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.BookmarkDao
import com.lichso.app.data.local.dao.ItemDao
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.data.local.entity.ItemEntity
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.feature.points.domain.AwardPointsUseCase
import com.lichso.app.notification.NotificationScheduler
import com.lichso.app.util.SmartRatingManager
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

    // Items gắn với ngày đang chọn (gộp ghi chú + việc + nhắc)
    val dayItems: List<ItemEntity> = emptyList(),

    // Toast
    val toastMessage: String? = null
) {
    val dayNotes: List<ItemEntity> get() = dayItems.filter { !it.isTask && !it.hasReminder }
    val dayTasks: List<ItemEntity> get() = dayItems.filter { it.isTask }
    val dayReminders: List<ItemEntity> get() = dayItems.filter { it.hasReminder }
}

@HiltViewModel
class DayActionsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bookmarkDao: BookmarkDao,
    private val itemDao: ItemDao,
    private val awardPointsUseCase: AwardPointsUseCase,
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
        // Award xem chi tiết 1 ngày (cap 8 lần/ngày)
        viewModelScope.launch {
            runCatching { awardPointsUseCase(ActionType.VIEW_DAY_DETAIL) }
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
        // Load tất cả item gắn với ngày này (theo dueDate trong [startOfDay, endOfDay))
        viewModelScope.launch {
            val startOfDay = startOfDayMillis(day, month, year)
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
            itemDao.getItemsForDateRange(startOfDay, endOfDay).collect { items ->
                _uiState.update { it.copy(dayItems = items) }
            }
        }
    }

    private fun startOfDayMillis(day: Int, month: Int, year: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

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
                SmartRatingManager.recordHappyAction(appContext)
                runCatching { awardPointsUseCase(ActionType.ADD_BOOKMARK) }
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
                runCatching { awardPointsUseCase(ActionType.ADD_BOOKMARK) }
            }
            _uiState.update {
                it.copy(
                    showBookmarkLabelDialog = false,
                    toastMessage = "Đã lưu \"$label\""
                )
            }
            // Happy action: user saved a labelled bookmark
            SmartRatingManager.recordHappyAction(appContext, weight = 2)
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

        val startOfDay = startOfDayMillis(state.selectedDay, state.selectedMonth, state.selectedYear)
        viewModelScope.launch {
            itemDao.insert(
                ItemEntity(
                    title = title,
                    description = content,
                    colorIndex = colorIndex,
                    dueDate = startOfDay,
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
            val entity = ItemEntity(
                title = title,
                description = "Ngày ${"%02d".format(state.selectedDay)}/${"%02d".format(state.selectedMonth)}/${state.selectedYear}",
                hasReminder = true,
                reminderAt = cal.timeInMillis,
                repeatType = repeatType,
                dueDate = startOfDayMillis(state.selectedDay, state.selectedMonth, state.selectedYear),
            )
            val id = itemDao.insert(entity)
            NotificationScheduler.scheduleReminder(appContext, entity.copy(id = id))
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
