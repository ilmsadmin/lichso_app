package com.lichso.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.backendTokenDataStore by preferencesDataStore(name = "backend_tokens")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val BACKEND_USER_ID = stringPreferencesKey("backend_user_id")
    private val USER_ROLES = stringPreferencesKey("user_roles")           // comma-separated
    private val USER_PERMISSIONS = stringPreferencesKey("user_permissions") // comma-separated
    private val FCM_TOKEN = stringPreferencesKey("fcm_token")

    suspend fun getAccessToken(): String? =
        context.backendTokenDataStore.data.map { it[ACCESS_TOKEN] }.firstOrNull()

    suspend fun getRefreshToken(): String? =
        context.backendTokenDataStore.data.map { it[REFRESH_TOKEN] }.firstOrNull()

    suspend fun getUserId(): String? =
        context.backendTokenDataStore.data.map { it[BACKEND_USER_ID] }.firstOrNull()

    suspend fun getUserRoles(): List<String> =
        context.backendTokenDataStore.data
            .map { it[USER_ROLES]?.split(",")?.filter { s -> s.isNotEmpty() } ?: emptyList() }
            .firstOrNull() ?: emptyList()

    suspend fun getUserPermissions(): List<String> =
        context.backendTokenDataStore.data
            .map { it[USER_PERMISSIONS]?.split(",")?.filter { s -> s.isNotEmpty() } ?: emptyList() }
            .firstOrNull() ?: emptyList()

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.backendTokenDataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUserSession(userId: String, roles: List<String>, permissions: List<String>) {
        context.backendTokenDataStore.edit { prefs ->
            prefs[BACKEND_USER_ID] = userId
            prefs[USER_ROLES] = roles.joinToString(",")
            prefs[USER_PERMISSIONS] = permissions.joinToString(",")
        }
    }

    suspend fun clearTokens() {
        context.backendTokenDataStore.edit { it.clear() }
    }

    suspend fun saveFcmToken(token: String) {
        context.backendTokenDataStore.edit { prefs -> prefs[FCM_TOKEN] = token }
    }

    suspend fun getFcmToken(): String? =
        context.backendTokenDataStore.data.map { it[FCM_TOKEN] }.firstOrNull()

    suspend fun clearFcmToken() {
        context.backendTokenDataStore.edit { prefs -> prefs.remove(FCM_TOKEN) }
    }
}
