package com.hady.stonesforever.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.data.model.UserData
import com.hady.stonesforever.domain.use_cases.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val useCase: SignInWithGoogleUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private var _pendingIntentSender: IntentSender? = null
    val intentSender: IntentSender? get() = _pendingIntentSender

    fun requestGoogleSignIn() = viewModelScope.launch {
        try {
            _pendingIntentSender = useCase.beginSignIn(context)
        } catch (e: Exception) {
            Log.e("Auth", "Begin sign-in failed", e)
        }
    }

    fun handleSignInResult(intent: Intent) = viewModelScope.launch {
        try {
            _user.value = useCase.handleIntent(context, intent)
        } catch (e: Exception) {
            Log.e("Auth", "Failed to sign in", e)
        }
    }
}
