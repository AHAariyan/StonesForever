package com.hady.stonesforever.data

import android.app.Activity
import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hady.stonesforever.common.WEB_CLIENT_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): String /* idToken */ {
        val googleOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(true)
                .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val result = credentialManager.getCredential(request, context as Activity)
        val cred = result.credential
        require(cred is CustomCredential &&
                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        )

        val idToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
        return idToken
    }

    fun firebaseSignIn(idToken: String): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return FirebaseAuth.getInstance().signInWithCredential(credential)
    }

    fun getCurrentUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser
}
