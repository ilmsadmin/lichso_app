package com.lichso.app.ui.screen.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.auth.AuthRepository
import com.lichso.app.data.auth.UserInfo
import com.lichso.app.data.local.LichSoDatabase
import com.lichso.app.notification.DailyNotificationWorker
import com.lichso.app.notification.FestivalReminderWorker
import com.lichso.app.notification.GioDaiCatWorker
import com.lichso.app.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// DataStore extension
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "lichso_settings")

object SettingsKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
    val LUNAR_BADGE = booleanPreferencesKey("lunar_badge")
    val GIO_DAI_CAT = booleanPreferencesKey("gio_dai_cat")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val CALENDAR_STYLE = stringPreferencesKey("calendar_style")
    val WEEK_START = stringPreferencesKey("week_start")
    val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
    val FESTIVAL_ENABLED = booleanPreferencesKey("festival_enabled")
    val QUOTE_ENABLED = booleanPreferencesKey("quote_enabled")
    val FESTIVAL_REMINDER = booleanPreferencesKey("festival_reminder")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val TEMP_UNIT = stringPreferencesKey("temp_unit")
    val LOCATION_NAME = stringPreferencesKey("location_name")
    // In-App Review tracking
    val APP_OPEN_COUNT = intPreferencesKey("app_open_count")
    val LAST_REVIEW_PROMPT_TIME = longPreferencesKey("last_review_prompt_time")
}

data class SettingsUiState(
    // Account
    val user: UserInfo? = null,
    val isSigningIn: Boolean = false,
    val signInError: String? = null,
    val showSignOutDialog: Boolean = false,
    // Settings
    val notifyEnabled: Boolean = true,
    val lunarBadgeEnabled: Boolean = true,
    val gioDaiCatEnabled: Boolean = false,
    val themeMode: String = "system", // "light", "dark", "system"
    val festivalEnabled: Boolean = true,
    val quoteEnabled: Boolean = true,
    val festivalReminderEnabled: Boolean = true,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val tempUnit: String = "°C",
    val locationName: String = "Hà Nội",
    val calendarStyle: String = "Lưới tháng",
    val weekStart: String = "Thứ Hai",
    val cacheSize: String = "Đang tính...",
    // Dialogs
    val showClearCacheDialog: Boolean = false,
    val showCalendarStyleDialog: Boolean = false,
    val showWeekStartDialog: Boolean = false,
    val showPrivacyPolicyDialog: Boolean = false,
    val showTempUnitDialog: Boolean = false,
    val showLocationDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showThemeModeDialog: Boolean = false,
    // Feedback
    val toastMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Collect DataStore prefs
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        notifyEnabled = prefs[SettingsKeys.NOTIFY_ENABLED] ?: true,
                        lunarBadgeEnabled = prefs[SettingsKeys.LUNAR_BADGE] ?: true,
                        gioDaiCatEnabled = prefs[SettingsKeys.GIO_DAI_CAT] ?: false,
                        themeMode = prefs[SettingsKeys.THEME_MODE] ?: "system",
                        festivalEnabled = prefs[SettingsKeys.FESTIVAL_ENABLED] ?: true,
                        quoteEnabled = prefs[SettingsKeys.QUOTE_ENABLED] ?: true,
                        festivalReminderEnabled = prefs[SettingsKeys.FESTIVAL_REMINDER] ?: true,
                        reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 7,
                        reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0,
                        tempUnit = prefs[SettingsKeys.TEMP_UNIT] ?: "°C",
                        locationName = prefs[SettingsKeys.LOCATION_NAME] ?: "Hà Nội",
                        calendarStyle = prefs[SettingsKeys.CALENDAR_STYLE] ?: "Lưới tháng",
                        weekStart = prefs[SettingsKeys.WEEK_START] ?: "Thứ Hai"
                    )
                }
            }
        }
        // Collect auth user
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
        calculateCacheSize()
    }

    // ═══ Auth ═══

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, signInError = null) }
            val result = authRepository.signInWithGoogle(activityContext)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSigningIn = false, toastMessage = "Đăng nhập thành công") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSigningIn = false, signInError = e.message) }
                }
            )
        }
    }

    fun showSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = true) }
    fun hideSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = false) }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(showSignOutDialog = false, toastMessage = "Đã đăng xuất") }
    }

    // ═══ Settings toggles ═══

    fun setNotifyEnabled(value: Boolean) {
        savePref(SettingsKeys.NOTIFY_ENABLED, value)
        viewModelScope.launch {
            val db = LichSoDatabase.getInstance(context)
            val scheduler = ReminderScheduler(context)
            if (value) {
                // Reschedule tất cả reminders đang enabled
                db.reminderDao().getEnabledReminders().first().forEach { scheduler.schedule(it) }
                // Schedule daily notification worker
                val state = _uiState.value
                DailyNotificationWorker.schedule(context, state.reminderHour, state.reminderMinute)
                _uiState.update { it.copy(toastMessage = "Đã bật thông báo nhắc nhở") }
            } else {
                // Huỷ tất cả alarms
                db.reminderDao().getAllReminders().first().forEach { scheduler.cancel(it.id) }
                // Cancel daily notification worker
                DailyNotificationWorker.cancel(context)
                _uiState.update { it.copy(toastMessage = "Đã tắt thông báo nhắc nhở") }
            }
        }
    }

    fun setLunarBadge(value: Boolean) {
        savePref(SettingsKeys.LUNAR_BADGE, value)
        _uiState.update {
            it.copy(toastMessage = if (value) "Đã bật hiển thị lịch âm" else "Đã tắt hiển thị lịch âm")
        }
    }

    fun setGioDaiCat(value: Boolean) {
        savePref(SettingsKeys.GIO_DAI_CAT, value)
        if (value) {
            val state = _uiState.value
            GioDaiCatWorker.schedule(context, state.reminderHour, state.reminderMinute)
            _uiState.update { it.copy(toastMessage = "Sẽ nhận thông báo giờ hoàng đạo mỗi ngày") }
        } else {
            GioDaiCatWorker.cancel(context)
            _uiState.update { it.copy(toastMessage = "Đã tắt thông báo giờ hoàng đạo") }
        }
    }

    fun setThemeMode(value: String) {
        savePrefString(SettingsKeys.THEME_MODE, value)
        val label = when (value) {
            "light" -> "Sáng"
            "dark" -> "Tối"
            else -> "Theo hệ thống"
        }
        _uiState.update {
            it.copy(showThemeModeDialog = false, toastMessage = "Giao diện: $label")
        }
    }

    fun setFestivalEnabled(value: Boolean) {
        savePref(SettingsKeys.FESTIVAL_ENABLED, value)
        _uiState.update {
            it.copy(toastMessage = if (value) "Đã bật hiển thị ngày lễ" else "Đã tắt hiển thị ngày lễ")
        }
    }

    fun setQuoteEnabled(value: Boolean) {
        savePref(SettingsKeys.QUOTE_ENABLED, value)
        _uiState.update {
            it.copy(toastMessage = if (value) "Đã bật câu danh ngôn" else "Đã tắt câu danh ngôn")
        }
    }

    fun setFestivalReminder(value: Boolean) {
        savePref(SettingsKeys.FESTIVAL_REMINDER, value)
        if (value) {
            FestivalReminderWorker.schedule(context)
            _uiState.update { it.copy(toastMessage = "Đã bật nhắc ngày lễ") }
        } else {
            FestivalReminderWorker.cancel(context)
            _uiState.update { it.copy(toastMessage = "Đã tắt nhắc ngày lễ") }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[SettingsKeys.REMINDER_HOUR] = hour
                it[SettingsKeys.REMINDER_MINUTE] = minute
            }
        }
        // Reschedule daily notification worker with the new time
        if (_uiState.value.notifyEnabled) {
            DailyNotificationWorker.schedule(context, hour, minute)
        }
        // Also reschedule giờ đại cát with updated time if enabled
        if (_uiState.value.gioDaiCatEnabled) {
            GioDaiCatWorker.schedule(context, hour, minute)
        }
        _uiState.update {
            it.copy(
                showTimePickerDialog = false,
                toastMessage = "Đã đặt giờ nhắc nhở: ${String.format("%02d:%02d", hour, minute)}"
            )
        }
    }

    fun setTempUnit(value: String) {
        savePrefString(SettingsKeys.TEMP_UNIT, value)
        _uiState.update {
            it.copy(
                showTempUnitDialog = false,
                toastMessage = "Đã chuyển sang $value"
            )
        }
    }

    fun setLocationName(value: String) {
        savePrefString(SettingsKeys.LOCATION_NAME, value)
        _uiState.update {
            it.copy(
                showLocationDialog = false,
                toastMessage = "Đã chọn vị trí: $value"
            )
        }
    }

    fun setCalendarStyle(value: String) {
        savePrefString(SettingsKeys.CALENDAR_STYLE, value)
        _uiState.update { it.copy(showCalendarStyleDialog = false) }
    }

    fun setWeekStart(value: String) {
        savePrefString(SettingsKeys.WEEK_START, value)
        _uiState.update {
            it.copy(
                showWeekStartDialog = false,
                toastMessage = "Ngày bắt đầu tuần: $value"
            )
        }
    }

    // ═══ Dialog visibility ═══

    fun showCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = true) }
    fun hideCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = false) }
    fun showWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = true) }
    fun hideWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = false) }
    fun showClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = true) }
    fun hideClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = false) }
    fun showTempUnitDialog() = _uiState.update { it.copy(showTempUnitDialog = true) }
    fun hideTempUnitDialog() = _uiState.update { it.copy(showTempUnitDialog = false) }
    fun showLocationDialog() = _uiState.update { it.copy(showLocationDialog = true) }
    fun hideLocationDialog() = _uiState.update { it.copy(showLocationDialog = false) }
    fun showTimePickerDialog() = _uiState.update { it.copy(showTimePickerDialog = true) }
    fun hideTimePickerDialog() = _uiState.update { it.copy(showTimePickerDialog = false) }
    fun showThemeModeDialog() = _uiState.update { it.copy(showThemeModeDialog = true) }
    fun hideThemeModeDialog() = _uiState.update { it.copy(showThemeModeDialog = false) }

    // ═══ Cache ═══

    fun clearCache() {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                _uiState.update { it.copy(cacheSize = "0 KB", showClearCacheDialog = false, toastMessage = "Đã xoá cache") }
            } catch (_: Exception) {
                _uiState.update { it.copy(showClearCacheDialog = false) }
            }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val size = context.cacheDir.walkTopDown().sumOf { it.length() }
                val sizeStr = when {
                    size < 1024 -> "$size B"
                    size < 1024 * 1024 -> "${size / 1024} KB"
                    else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
                }
                _uiState.update { it.copy(cacheSize = sizeStr) }
            } catch (_: Exception) {
                _uiState.update { it.copy(cacheSize = "N/A") }
            }
        }
    }

    // ═══ Actions ═══

    fun backupData() {
        _uiState.update { it.copy(toastMessage = "Tính năng sao lưu sẽ có trong bản cập nhật tiếp theo") }
    }

    fun restoreData() {
        _uiState.update { it.copy(toastMessage = "Tính năng khôi phục sẽ có trong bản cập nhật tiếp theo") }
    }

    fun rateApp() {
        _uiState.update { it.copy(toastMessage = "Tính năng đánh giá sẽ có khi ứng dụng lên Store") }
    }

    fun shareApp() {
        _uiState.update { it.copy(toastMessage = "Tính năng chia sẻ sẽ có khi ứng dụng lên Store") }
    }

    fun openPrivacyPolicy() {
        _uiState.update { it.copy(showPrivacyPolicyDialog = true) }
    }

    fun dismissPrivacyPolicy() {
        _uiState.update { it.copy(showPrivacyPolicyDialog = false) }
    }

    fun openHelp() {
        _uiState.update { it.copy(toastMessage = "Hướng dẫn sử dụng sẽ được cập nhật sớm") }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // ═══ Helpers ═══

    private fun savePref(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[key] = value }
        }
    }

    private fun savePrefString(key: Preferences.Key<String>, value: String) {
        viewModelScope.launch {
            dataStore.edit { it[key] = value }
        }
    }

    private fun savePrefInt(key: Preferences.Key<Int>, value: Int) {
        viewModelScope.launch {
            dataStore.edit { it[key] = value }
        }
    }
}
