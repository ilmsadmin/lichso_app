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
}
