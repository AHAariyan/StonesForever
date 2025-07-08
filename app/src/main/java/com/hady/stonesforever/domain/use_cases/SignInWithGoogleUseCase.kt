package com.hady.stonesforever.domain.use_cases

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository

class SignInWithGoogleUseCase(private val repo: GoogleAuthRepository) {

    suspend fun beginSignIn(context: Context): IntentSender =
        repo.beginSignIn(context)

    suspend fun handleIntent(context: Context, intent: Intent): FirebaseUser {
        val idToken = repo.getGoogleIdTokenFromIntent(context, intent)
        return repo.firebaseSignInWithGoogleIdToken(idToken)
            ?: throw Exception("Firebase user is null")
    }
}
