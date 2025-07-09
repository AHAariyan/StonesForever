package com.hady.stonesforever.domain.Repository

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser

interface GoogleAuthRepository {
    suspend fun signIn(activityContext: Context): FirebaseUser?
    suspend fun signOut(): Unit
    fun getCurrentUser(): FirebaseUser?
}

