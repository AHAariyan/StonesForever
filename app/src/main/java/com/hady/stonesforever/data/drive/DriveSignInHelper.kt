package com.hady.stonesforever.data.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

//object DriveSignInHelper {
//    fun getDriveSignInClient(context: Context): GoogleSignInClient {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE)) // Or DRIVE or DRIVE_APPDATA
//            .requestEmail()
//            .requestIdToken("167639734033-s24rie3nmu5jc87539j729gioi8pl79k.apps.googleusercontent.com") // Same as Firebase
//            .build()
//
//        return GoogleSignIn.getClient(context, gso)
//    }
//
//}

object DriveSignInHelper {
    fun getDriveSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE)) // Use DRIVE if you want full access
            .requestIdToken("167639734033-s24rie3nmu5jc87539j729gioi8pl79k.apps.googleusercontent.com") //Same as Firebase
            .build()

        return GoogleSignIn.getClient(context, gso)
    }
}

