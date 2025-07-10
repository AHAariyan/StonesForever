package com.hady.stonesforever.presentation.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.play.integrity.internal.ac
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.data.drive.DriveSignInHelper
import com.hady.stonesforever.presentation.viewmodel.AuthUiState
import com.hady.stonesforever.presentation.viewmodel.AuthViewModel

@Composable
internal fun AuthScreenRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignedIn: (FirebaseUser) -> Unit
) {
    AuthScreen(viewModel = viewModel, onSignedIn = onSignedIn)
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onSignedIn: (FirebaseUser) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // 👇 Drive Sign-In Client
    val driveSignInClient = remember { DriveSignInHelper.getDriveSignInClient(context) }

    // 👇 Launcher for Drive OAuth Sign-In
    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("DriveAuth", "Drive Sign-In success: ${account.email}")
            viewModel.setDriveAccount(account) // You'll use this in Step 5

            // ✅ List files right after Drive account is set
            viewModel.listDriveFiles(
                onSuccess = { files ->
                    Log.d("DriveFiles", "Fetched ${files.size} files from Drive")
                    files.forEach {
                        Log.d("DriveFiles", "${it.name} (${it.mimeType})")
                    }
                },
                onError = { error ->
                    Log.e("DriveFiles", "Error listing files: ${error.message}", error)
                }
            )
        } catch (e: ApiException) {
            Log.e("DriveAuth", "Drive Sign-In failed", e)
        }
    }

    when (authState) {
        is AuthUiState.Idle -> {
            SignInButton(
                onClick = {
                    activity?.let {
                        viewModel.signInWithGoogle(activityContext = it)
                    }
                }
            )
        }

        is AuthUiState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        is AuthUiState.Success -> {
            val user = (authState as AuthUiState.Success).user
            onSignedIn(user)

            // 🚀 Launch Drive Sign-In immediately after Firebase Sign-In
            LaunchedEffect(Unit) {
                driveSignInLauncher.launch(driveSignInClient.signInIntent)
            }
        }

        is AuthUiState.NotSignedIn -> {
            SignInButton(
                onClick = {
                    activity?.let {
                        viewModel.signInWithGoogle(activityContext = it)
                    }
                }
            )
        }

        is AuthUiState.Error -> {
            val message = (authState as AuthUiState.Error).message
            ErrorMessage(
                message = message,
                onRetry = {
                    activity?.let {
                        viewModel.signInWithGoogle(activityContext = it)
                    }
                }
            )
        }
    }
}


@Composable
fun SignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("Sign in with Google")
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

