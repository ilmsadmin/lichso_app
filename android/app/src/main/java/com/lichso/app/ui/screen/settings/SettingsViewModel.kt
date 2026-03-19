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
    val NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
    val LUNAR_BADGE = booleanPreferencesKey("lunar_badge")
    val GIO_DAI_CAT = booleanPreferencesKey("gio_dai_cat")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val LANGUAGE = stringPreferencesKey("language")
    val CALENDAR_STYLE = stringPreferencesKey("calendar_style")
    val WEEK_START = stringPreferencesKey("week_start")
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
    val darkModeEnabled: Boolean = true,
    val language: String = "Tiếng Việt",
    val calendarStyle: String = "Lưới tháng",
    val weekStart: String = "Thứ Hai",
    val cacheSize: String = "Đang tính...",
    // Dialogs
    val showLanguageDialog: Boolean = false,
    val showCalendarStyleDialog: Boolean = false,
    val showWeekStartDialog: Boolean = false,
    val showClearCacheDialog: Boolean = false,
    // Feedback
    val toastMessage: String? = null,
    // Navigation events (consumed once)
    val openUrlEvent: String? = null,
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
                        darkModeEnabled = prefs[SettingsKeys.DARK_MODE] ?: true,
                        language = prefs[SettingsKeys.LANGUAGE] ?: "Tiếng Việt",
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
                _uiState.update { it.copy(toastMessage = "Đã bật thông báo nhắc nhở") }
            } else {
                // Huỷ tất cả alarms
                db.reminderDao().getAllReminders().first().forEach { scheduler.cancel(it.id) }
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
            GioDaiCatWorker.schedule(context)
            _uiState.update { it.copy(toastMessage = "Sẽ nhận thông báo giờ hoàng đạo lúc 6h sáng") }
        } else {
            GioDaiCatWorker.cancel(context)
            _uiState.update { it.copy(toastMessage = "Đã tắt thông báo giờ hoàng đạo") }
        }
    }

    fun setDarkMode(value: Boolean) = savePref(SettingsKeys.DARK_MODE, value)

    fun setLanguage(value: String) {
        savePrefString(SettingsKeys.LANGUAGE, value)
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun setCalendarStyle(value: String) {
        savePrefString(SettingsKeys.CALENDAR_STYLE, value)
        _uiState.update { it.copy(showCalendarStyleDialog = false) }
    }

    fun setWeekStart(value: String) {
        savePrefString(SettingsKeys.WEEK_START, value)
        _uiState.update { it.copy(showWeekStartDialog = false) }
    }

    // ═══ Dialog visibility ═══

    fun showLanguageDialog() = _uiState.update { it.copy(showLanguageDialog = true) }
    fun hideLanguageDialog() = _uiState.update { it.copy(showLanguageDialog = false) }
    fun showCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = true) }
    fun hideCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = false) }
    fun showWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = true) }
    fun hideWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = false) }
    fun showClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = true) }
    fun hideClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = false) }

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
        _uiState.update { it.copy(openUrlEvent = "file:///android_asset/privacy_policy.html") }
    }

    fun openHelp() {
        _uiState.update { it.copy(toastMessage = "Hướng dẫn sử dụng sẽ được cập nhật sớm") }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun consumeOpenUrl() {
        _uiState.update { it.copy(openUrlEvent = null) }
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
}
