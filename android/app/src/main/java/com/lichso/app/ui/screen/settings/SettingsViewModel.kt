package com.lichso.app.ui.screen.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val showClearCacheDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
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
        calculateCacheSize()
    }

    fun setNotifyEnabled(value: Boolean) = savePref(SettingsKeys.NOTIFY_ENABLED, value)
    fun setLunarBadge(value: Boolean) = savePref(SettingsKeys.LUNAR_BADGE, value)
    fun setGioDaiCat(value: Boolean) = savePref(SettingsKeys.GIO_DAI_CAT, value)
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

    fun showLanguageDialog() = _uiState.update { it.copy(showLanguageDialog = true) }
    fun hideLanguageDialog() = _uiState.update { it.copy(showLanguageDialog = false) }
    fun showCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = true) }
    fun hideCalendarStyleDialog() = _uiState.update { it.copy(showCalendarStyleDialog = false) }
    fun showWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = true) }
    fun hideWeekStartDialog() = _uiState.update { it.copy(showWeekStartDialog = false) }
    fun showClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = true) }
    fun hideClearCacheDialog() = _uiState.update { it.copy(showClearCacheDialog = false) }

    fun clearCache() {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                _uiState.update { it.copy(cacheSize = "0 KB", showClearCacheDialog = false) }
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
