package com.hady.stonesforever.domain.use_cases

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class SignInWithGoogleUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(): FirebaseUser? {
        return repository.signIn()
    }
}

class SignOutUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: GoogleAuthRepository
) {
    operator fun invoke(): FirebaseUser? = authRepository.getCurrentUser()
}
