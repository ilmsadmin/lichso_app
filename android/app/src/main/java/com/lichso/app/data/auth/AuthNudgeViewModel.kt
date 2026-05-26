package com.lichso.app.data.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthNudgeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val isGuestUser: StateFlow<Boolean> = authRepository.currentUser
        .map { it == null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser.value == null)

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    fun signInWithGoogle(activityContext: Context) {
        if (_isSigningIn.value) return
        viewModelScope.launch {
            _isSigningIn.value = true
            authRepository.signInWithGoogle(activityContext)
            _isSigningIn.value = false
        }
    }
}
