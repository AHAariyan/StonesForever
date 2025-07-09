package com.hady.stonesforever.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.firebase.auth.FirebaseUser
import com.hady.stonesforever.presentation.ui.AuthScreen
import com.hady.stonesforever.presentation.ui.AuthScreenRoute

@Composable
fun NavController.navigateToAuthScreen(navOptions: NavOptions?= null) =
    navigate(route = AuthRoute, navOptions = navOptions)


fun NavGraphBuilder.authScreen(
    onSignedIn: (FirebaseUser) -> Unit
) {
    navigation<AuthBaseRoute>(startDestination = AuthRoute) {
        composable <AuthRoute> {
            AuthScreenRoute(
                onSignedIn = onSignedIn
            )
        }
    }
}