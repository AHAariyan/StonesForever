package com.hady.stonesforever.data.Repository

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.play.integrity.internal.a
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GoogleAuthRepositoryImpl(
    private val context: Context,
    private val webClientId: String
) : GoogleAuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(activityContext: Context): FirebaseUser? = withContext(Dispatchers.IO) {
        try {
            // 1. Build the Google sign-in option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false) // set true if you want to show only previously used accounts
                .build()

            // 2. Create Credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 3. Launch Credential Manager
            val result = credentialManager.getCredential(activityContext, request)

            // 4. Get credential from result
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                return@withContext authResult.user
            } else {
                Log.w(TAG, "Credential is not a Google ID Token type!")
                return@withContext null
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential error: ${e.localizedMessage}", e)
            return@withContext null
        }
    }

    override suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        firebaseAuth.signOut()

        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Failed to clear credential state: ${e.localizedMessage}")
        }
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    companion object {
        private const val TAG = "GoogleAuthRepository"
    }
}