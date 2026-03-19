package com.lichso.app.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserInfo(
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val uid: String
)

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "AuthRepository"
    private val firebaseAuth = FirebaseAuth.getInstance()

    // TODO: Replace with your actual Web Client ID from Firebase Console
    // Firebase Console → Project Settings → General → Web client (auto created by Google Service)
    private val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // Restore Firebase user on init
        firebaseAuth.currentUser?.let { user ->
            _currentUser.value = UserInfo(
                displayName = user.displayName ?: "Người dùng",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                uid = user.uid
            )
        }
    }

    /**
     * Sign in with Google using Credential Manager API.
     * Must be called from an Activity context.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<UserInfo> {
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
                        val userInfo = UserInfo(
                            displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Người dùng",
                            email = user.email ?: googleIdTokenCredential.id,
                            photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                            uid = user.uid
                        )
                        _currentUser.value = userInfo
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

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null
}
