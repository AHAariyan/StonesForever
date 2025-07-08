package com.hady.stonesforever.domain.Repository

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser

interface GoogleAuthRepository {
    suspend fun beginSignIn(context: Context): IntentSender
    suspend fun getGoogleIdTokenFromIntent(context: Context, intent: Intent): String
    suspend fun firebaseSignInWithGoogleIdToken(idToken: String): FirebaseUser?
}
