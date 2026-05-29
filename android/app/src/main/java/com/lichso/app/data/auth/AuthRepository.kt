package com.lichso.app.data.auth

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.lichso.app.BuildConfig
import com.lichso.app.analytics.Analytics
import com.lichso.app.data.remote.LichSoApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserInfo(
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val uid: String,
    val backendId: String = "",
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
)

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: LichSoApi,
    private val tokenManager: TokenManager,
) {
    private val tag = "AuthRepository"
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Web Client ID from Firebase Console, loaded from local.properties via BuildConfig.
    // Add GOOGLE_WEB_CLIENT_ID=<your-id>.apps.googleusercontent.com to local.properties.
    private val webClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // Restore session on startup — load stored roles/permissions from DataStore
        firebaseAuth.currentUser?.let { user ->
            Analytics.setUserId(user.uid)
            CoroutineScope(Dispatchers.IO).launch {
                val backendId = tokenManager.getUserId() ?: ""
                val roles = tokenManager.getUserRoles()
                val permissions = tokenManager.getUserPermissions()
                _currentUser.value = UserInfo(
                    displayName = user.displayName ?: "Người dùng",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    uid = user.uid,
                    backendId = backendId,
                    roles = roles,
                    permissions = permissions,
                )
            }
        }
    }

    /**
     * Sign in with Google using Credential Manager API.
     * Must be called from an Activity context.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<UserInfo> {
        if (webClientId.isBlank()) {
            return Result.failure(
                IllegalStateException("GOOGLE_WEB_CLIENT_ID is not configured. Add it to local.properties.")
            )
        }

        return try {
            val credentialManager = CredentialManager.create(activityContext)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext,
            )

            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "Sign-in cancelled by user")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(tag, "Sign-in failed: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): Result<UserInfo> {
        val credential = result.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // Sign in to Firebase with the Google credential
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user

                    if (user != null) {
                        // Exchange Google token for backend JWT (required for app session)
                        val loginResponse = api.loginWithGoogle(idToken).getOrElse { error ->
                            firebaseAuth.signOut()
                            tokenManager.clearTokens()
                            return Result.failure(
                                IllegalStateException(
                                    "Đăng nhập máy chủ thất bại: ${error.message ?: "unknown error"}"
                                )
                            )
                        }

                        tokenManager.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
                        val backendId = loginResponse.user.id
                        val roles = loginResponse.user.roles
                        val permissions = loginResponse.user.permissions
                        tokenManager.saveUserSession(backendId, roles, permissions)
                        Log.d(tag, "Backend auth OK — user=${loginResponse.user.email} roles=$roles")

                        // Re-register FCM token with backend now that we have the auth token,
                        // so the device is linked to this user account.
                        tokenManager.getFcmToken()?.let { fcmToken ->
                            val deviceId = Settings.Secure.getString(
                                context.contentResolver, Settings.Secure.ANDROID_ID
                            ) ?: ""
                            api.registerDeviceToken(
                                fcmToken = fcmToken,
                                appVersion = BuildConfig.VERSION_NAME,
                                deviceId = deviceId,
                                authToken = loginResponse.accessToken,
                            )
                            Log.d(tag, "FCM token linked to user account")
                        }

                        val userInfo = UserInfo(
                            displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Người dùng",
                            email = user.email ?: googleIdTokenCredential.id,
                            photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                            uid = user.uid,
                            backendId = backendId,
                            roles = roles,
                            permissions = permissions,
                        )
                        _currentUser.value = userInfo
                        Analytics.setUserId(user.uid)
                        Analytics.logEvent("login", mapOf("method" to "google"))
                        Result.success(userInfo)
                    } else {
                        Result.failure(Exception("Firebase auth returned null user"))
                    }
                } else {
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
            else -> Result.failure(Exception("Unexpected credential type"))
        }
    }

    suspend fun getBackendToken(): String? {
        val access = tokenManager.getAccessToken() ?: return null
        return access
    }

    suspend fun refreshBackendTokenIfNeeded(): String? {
        val access = tokenManager.getAccessToken()
        if (access != null) {
            val stillValid = api.getMe(access).isSuccess
            if (stillValid) return access
        }
        val refresh = tokenManager.getRefreshToken() ?: return null
        return api.refreshBackendToken(refresh).getOrNull()?.also { response ->
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
        }?.accessToken ?: run {
            tokenManager.clearTokens()
            null
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
        Analytics.setUserId(null)
        Analytics.logEvent("logout")
    }

    suspend fun signOutAndClearTokens() {
        // Notify backend to revoke tokens and write activity log
        tokenManager.getAccessToken()?.let { token ->
            try { api.logout(token) } catch (_: Exception) {}
        }
        signOut()
        tokenManager.clearTokens()
    }

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun hasPermission(permission: String): Boolean {
        val user = _currentUser.value ?: return false
        if ("super_admin" in user.roles) return true
        return permission in user.permissions
    }

    fun getUserRoles(): List<String> = _currentUser.value?.roles ?: emptyList()
}
