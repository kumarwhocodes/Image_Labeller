package com.kumar.imagelabeller.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kumar.imagelabeller.screens.ImageLabelScreen

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

        }
        composable(route = Screen.GPSSpeechScreen.route) {

        }
        composable(route = Screen.BankingScreen.route) {

        }
        composable(route = Screen.RestaurantScreen.route) {

        }
    }
}