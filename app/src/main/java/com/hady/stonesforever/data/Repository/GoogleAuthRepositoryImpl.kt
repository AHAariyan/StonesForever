package com.hady.stonesforever.data.Repository

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository
import kotlinx.coroutines.tasks.await

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val webClientId: String
) : GoogleAuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override suspend fun beginSignIn(context: Context): IntentSender {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        val oneTapClient = Identity.getSignInClient(context)
        val result = oneTapClient.beginSignIn(signInRequest).await()
        return result.pendingIntent.intentSender
    }

    override suspend fun getGoogleIdTokenFromIntent(context: Context, intent: Intent): String {
        val oneTapClient = Identity.getSignInClient(context)
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        return credential.googleIdToken
            ?: throw Exception("Google ID token was null")
    }

    override suspend fun firebaseSignInWithGoogleIdToken(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return authResult.user
    }
}
