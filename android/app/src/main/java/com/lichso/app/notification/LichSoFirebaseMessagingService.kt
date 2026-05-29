package com.lichso.app.notification

import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lichso.app.BuildConfig
import com.lichso.app.data.auth.TokenManager
import com.lichso.app.data.remote.LichSoApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles FCM token refresh and incoming push messages from the Lịch Số server.
 *
 * Token lifecycle:
 *  - onNewToken: called when FCM assigns a new token (first install, token rotation).
 *    Saves locally and registers with backend immediately.
 *  - LichSoApp.onCreate: also registers on every cold start to cover missed refreshes.
 */
@AndroidEntryPoint
class LichSoFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var api: LichSoApi
    @Inject lateinit var tokenManager: TokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        serviceScope.launch {
            tokenManager.saveFcmToken(token)
            registerWithBackend(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.messageId}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: return

        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        val clickAction = message.notification?.clickAction
            ?: message.data["click_action"]

        // Save to in-app notification DB + show system notification
        NotificationHelper.showPushNotification(applicationContext, title, body, clickAction)
    }

    private suspend fun registerWithBackend(fcmToken: String) {
        val authToken = tokenManager.getAccessToken()
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val result = api.registerDeviceToken(
            fcmToken = fcmToken,
            appVersion = BuildConfig.VERSION_NAME,
            deviceId = deviceId,
            deviceName = deviceName,
            authToken = authToken,
        )

        if (result.isSuccess) {
            Log.d(TAG, "Device token registered with backend")
        } else {
            Log.w(TAG, "Failed to register device token: ${result.exceptionOrNull()?.message}")
        }
    }

    companion object {
        private const val TAG = "LichSoFCM"
    }
}
