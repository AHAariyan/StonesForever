package com.hady.stonesforever

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hady.stonesforever.navigation.StonesNavigationGraph
import com.hady.stonesforever.presentation.ui.AuthScreen
import com.hady.stonesforever.presentation.viewmodel.AuthUiState
import com.hady.stonesforever.presentation.viewmodel.AuthViewModel
import com.hady.stonesforever.ui.theme.StonesForeverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
    val authState by viewModel.authState.collectAsState()

    Scaffold(
        topBar = { AppBar() }
    ) { paddingValues ->

        Log.d("", "StonesForeverApp: $paddingValues")
        Column(
            modifier = modifier
                .fillMaxSize().padding(top = 64.dp)
        ) {
            when (authState) {
                is AuthUiState.Idle,
                is AuthUiState.Loading -> {
                    // ✅ Show splash or loader
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AuthUiState.Success -> {
                    StonesNavigationGraph(
                        isAuthenticated = true
                    )
                }

                is AuthUiState.Error -> {
                    StonesNavigationGraph(
                        isAuthenticated = false
                    )
                }

                AuthUiState.NotSignedIn -> {
                    StonesNavigationGraph(
                        isAuthenticated = false
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(0.8f)
        ),
        title = { Text(text = stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onPrimary) },
        navigationIcon = {
//            Image(
//                painter = painterResource(youTubeIcon),
//                contentDescription = null,
//                modifier = Modifier.size(width = 36.dp, height = 26.dp)
//            )

        }
    )
}
