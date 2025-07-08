package com.hady.stonesforever

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.hady.stonesforever.presentation.ui.AuthScreen
import com.hady.stonesforever.presentation.viewmodel.AuthViewModel
import com.hady.stonesforever.ui.theme.StonesForeverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StonesForeverTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StonesForeverApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun StonesForeverApp(modifier: Modifier = Modifier) {
    val viewModel: AuthViewModel = hiltViewModel()
    AuthScreen(viewModel = viewModel)
}