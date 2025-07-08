package com.hady.stonesforever.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hady.stonesforever.presentation.viewmodel.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val user by viewModel.user.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            it.data?.let { intent ->
                viewModel.handleSignInResult(intent)
            }
        }
    )

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("User: ${user?.displayName ?: "Not signed in"}")
        Button(onClick = {
            viewModel.requestGoogleSignIn()
        }) {
            Text("Sign In with Google")
        }

        LaunchedEffect(viewModel.intentSender) {
            viewModel.intentSender?.let { intentSender ->
                launcher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )
            }
        }
    }
}
