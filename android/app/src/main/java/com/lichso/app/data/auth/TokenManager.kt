package com.lichso.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val master = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "backend_tokens_enc",
            master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * In-memory cache — tránh disk I/O trên OkHttp interceptor thread.
     * Warm on first access, updated on save/clear.
     */
    @Volatile var cachedAccessToken: String? = null
        private set
    @Volatile var cachedDeviceId: String = ""
        private set

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cachedAccessToken = getAccessToken()
                cachedDeviceId = getInstallationId()
            } catch (_: Exception) { /* prefs chưa sẵn sàng */ }
        }
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("access_token", null)
    }

    suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("refresh_token", null)
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        prefs.getString("backend_user_id", null)
    }

    suspend fun getUserRoles(): List<String> = withContext(Dispatchers.IO) {
        prefs.getString("user_roles", null)
            ?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun getUserPermissions(): List<String> = withContext(Dispatchers.IO) {
        prefs.getString("user_permissions", null)
            ?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        cachedAccessToken = accessToken
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .apply()
        }
    }

    suspend fun saveUserSession(userId: String, roles: List<String>, permissions: List<String>) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("backend_user_id", userId)
                .putString("user_roles", roles.joinToString(","))
                .putString("user_permissions", permissions.joinToString(","))
                .apply()
        }
    }

    suspend fun clearTokens() {
        cachedAccessToken = null
        withContext(Dispatchers.IO) {
            prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .remove("backend_user_id")
                .remove("user_roles")
                .remove("user_permissions")
                .apply()
        }
    }

    suspend fun getInstallationId(): String {
        val existing = withContext(Dispatchers.IO) { prefs.getString("installation_id", null) }
        if (!existing.isNullOrBlank()) {
            cachedDeviceId = existing
            return existing
        }

        val created = UUID.randomUUID().toString()
        withContext(Dispatchers.IO) {
            prefs.edit().putString("installation_id", created).apply()
        }
        val result = withContext(Dispatchers.IO) {
            prefs.getString("installation_id", null)
        } ?: created
        cachedDeviceId = result
        return result
    }

    suspend fun saveFcmToken(token: String) {
        withContext(Dispatchers.IO) { prefs.edit().putString("fcm_token", token).apply() }
    }

    suspend fun getFcmToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("fcm_token", null)
    }

    suspend fun clearFcmToken() {
        withContext(Dispatchers.IO) { prefs.edit().remove("fcm_token").apply() }
    }
}
