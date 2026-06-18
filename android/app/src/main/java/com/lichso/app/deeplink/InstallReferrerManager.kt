package com.lichso.app.deeplink

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Deferred deep linking qua Google Play Install Referrer.
 *
 * Khi user cài app từ một quảng cáo có gắn `referrer`, Play Store giữ chuỗi đó và
 * trả lại qua API ở lần mở đầu tiên. Ta đọc 1 lần duy nhất, suy ra route đích
 * ([CampaignRoutes.fromReferrer]) và lưu vào DataStore để Main tiêu thụ — nhờ vậy
 * ad "tử vi" mở thẳng màn AI thay vì rơi vào Home chung chung.
 *
 * API chỉ nên gọi 1 lần/đời cài đặt (flag [SettingsKeys.INSTALL_REFERRER_PROCESSED]).
 */
object InstallReferrerManager {

    /** Gọi ở startup (LichSoApp). No-op nếu đã xử lý trước đó. */
    suspend fun fetchAndStoreIfNeeded(context: Context) {
        val processed = context.safeSettingsData.first()[SettingsKeys.INSTALL_REFERRER_PROCESSED] ?: false
        if (processed) return

        val referrer = runCatching { readReferrer(context) }.getOrNull()
        val route = CampaignRoutes.fromReferrer(referrer)

        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.INSTALL_REFERRER_PROCESSED] = true
            if (route != null) prefs[SettingsKeys.PENDING_DEFERRED_ROUTE] = route
        }
    }

    /**
     * Đọc & xóa route deferred đang chờ (tiêu thụ 1 lần). Main gọi ở lần vào đầu.
     * Trả về null nếu không có hoặc route không còn hợp lệ.
     */
    suspend fun consumePendingRoute(context: Context): String? {
        val pending = context.safeSettingsData.first()[SettingsKeys.PENDING_DEFERRED_ROUTE]
        if (pending != null) {
            context.settingsDataStore.edit { it.remove(SettingsKeys.PENDING_DEFERRED_ROUTE) }
        }
        return pending?.takeIf { it in CampaignRoutes.valid }
    }

    /** Kết nối Play Install Referrer service, lấy chuỗi referrer rồi đóng. */
    private suspend fun readReferrer(context: Context): String? =
        suspendCancellableCoroutine { cont ->
            val client = InstallReferrerClient.newBuilder(context).build()
            cont.invokeOnCancellation { runCatching { client.endConnection() } }
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    val referrer = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        runCatching { client.installReferrer.installReferrer }.getOrNull()
                    } else null
                    runCatching { client.endConnection() }
                    if (cont.isActive) cont.resume(referrer)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    if (cont.isActive) cont.resume(null)
                }
            })
        }
}
