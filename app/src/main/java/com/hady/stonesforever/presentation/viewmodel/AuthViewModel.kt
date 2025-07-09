package com.hady.stonesforever.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.integrity.internal.ac
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.data.model.UserData
import com.hady.stonesforever.domain.use_cases.GetCurrentUserUseCase
import com.hady.stonesforever.domain.use_cases.SignInWithGoogleUseCase
import com.hady.stonesforever.domain.use_cases.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCases: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private var _intentSender: IntentSender? = null
    val intentSender: IntentSender?
        get() = _intentSender

    init {
        checkIfUserIsSignedIn()
    }

    fun checkIfUserIsSignedIn() {
        val user = getCurrentUserUseCase()
        _authState.value = if (user != null) {
            AuthUiState.Success(user)
        } else {
            AuthUiState.NotSignedIn
        }
    }


    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val user = signInUseCases(activityContext = activityContext)
                _authState.value = if (user != null) {
                    AuthUiState.Success(user)
                } else {
                    AuthUiState.Error("Something went wrong.")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                signOutUseCase()
                _authState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Sign out failed")
            }
        }
    }

    fun setIntentSender(intentSender: IntentSender) {
        _intentSender = intentSender
    }

    fun clearError() {
        if (_authState.value is AuthUiState.Error) {
            _authState.value = AuthUiState.Idle
        }
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object NotSignedIn : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
