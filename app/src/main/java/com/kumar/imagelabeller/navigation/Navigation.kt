package com.kumar.imagelabeller.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kumar.imagelabeller.screens.BankingScreen
import com.kumar.imagelabeller.screens.CallScreen
import com.kumar.imagelabeller.screens.GPSSpeechScreen
import com.kumar.imagelabeller.screens.ImageLabelScreen
import com.kumar.imagelabeller.screens.MoreFeaturesScreen
import com.kumar.imagelabeller.screens.RestaurantScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ImageLabellingScreen.route
    ) {
        composable(route = Screen.ImageLabellingScreen.route) {
            ImageLabelScreen()
        }
        composable(route = Screen.CallScreen.route) {
            CallScreen(navController)
        }
        composable(route = Screen.GPSSpeechScreen.route) {
            GPSSpeechScreen(navController)
        }
        composable(route = Screen.BankingScreen.route) {
            BankingScreen(navController)
        }
        composable(route = Screen.RestaurantScreen.route) {
            RestaurantScreen(navController)
        }
        composable(route = Screen.MoreFeatures.route) {
            MoreFeaturesScreen(navController)
        }
    }
}