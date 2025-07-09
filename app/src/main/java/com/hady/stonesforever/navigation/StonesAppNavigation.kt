package com.hady.stonesforever.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser

@Composable
internal fun StonesNavigationGraph(
    modifier: Modifier = Modifier,
    isAuthenticated: Boolean = false
) {
    val navController = rememberNavController()

    val startDestination = if (isAuthenticated) HomeBaseRoute else AuthBaseRoute

    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        authScreen(
            onSignedIn = {
                //navController.navigateToHomeScreen()
            }
        )
        homeScreen()
    }
}