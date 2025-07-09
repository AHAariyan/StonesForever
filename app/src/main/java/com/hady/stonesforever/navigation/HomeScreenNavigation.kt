package com.hady.stonesforever.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.hady.stonesforever.presentation.ui.HomeScreen
import com.hady.stonesforever.presentation.ui.HomeScreenRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) = navigate(route = HomeRoute, navOptions = navOptions)

fun NavGraphBuilder.homeScreen() {
    navigation<HomeBaseRoute>(startDestination = HomeRoute){
        composable<HomeRoute> {
            HomeScreenRoute()
        }
    }
}