package com.lichso.app.data.settings

import android.content.Context
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository đơn giản để expose các setting quan trọng cho các screen khác đọc.
 */
@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Hiển thị ngày âm lịch trên ô calendar hay không */
    val lunarBadgeEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.LUNAR_BADGE] ?: true }

    /** Thông báo nhắc nhở có bật không */
    val notifyEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.NOTIFY_ENABLED] ?: true }

    /** Thông báo giờ đại cát có bật không */
    val gioDaiCatEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.GIO_DAI_CAT] ?: false }

    /** Chế độ giao diện: "light", "dark", "system" */
    val themeMode: Flow<String> =
        context.settingsDataStore.data.map { it[SettingsKeys.THEME_MODE] ?: "system" }

    /** Hiển thị ngày lễ / sự kiện */
    val festivalEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.FESTIVAL_ENABLED] ?: true }

    /** Hiển thị câu danh ngôn mỗi ngày */
    val quoteEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.QUOTE_ENABLED] ?: true }

    /** Nhắc nhở ngày lễ */
    val festivalReminderEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[SettingsKeys.FESTIVAL_REMINDER] ?: true }

    /** Đơn vị nhiệt độ (°C / °F) */
    val tempUnit: Flow<String> =
        context.settingsDataStore.data.map { it[SettingsKeys.TEMP_UNIT] ?: "°C" }

    /** Tên vị trí */
    val locationName: Flow<String> =
        context.settingsDataStore.data.map { it[SettingsKeys.LOCATION_NAME] ?: "Hà Nội" }

    /** Giờ nhắc nhở */
    val reminderHour: Flow<Int> =
        context.settingsDataStore.data.map { it[SettingsKeys.REMINDER_HOUR] ?: 7 }

    /** Phút nhắc nhở */
    val reminderMinute: Flow<Int> =
        context.settingsDataStore.data.map { it[SettingsKeys.REMINDER_MINUTE] ?: 0 }

    /** Ngày bắt đầu tuần */
    val weekStart: Flow<String> =
        context.settingsDataStore.data.map { it[SettingsKeys.WEEK_START] ?: "Thứ Hai" }

    /** Ngôn ngữ */
    val language: Flow<String> =
        context.settingsDataStore.data.map { it[SettingsKeys.LANGUAGE] ?: "Tiếng Việt" }
}
