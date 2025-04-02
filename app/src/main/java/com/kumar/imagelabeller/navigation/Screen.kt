package com.kumar.imagelabeller.navigation

sealed class Screen(val route: String) {

    data object BankingScreen : Screen(route = "banking")

    data object ImageLabellingScreen : Screen(route = "image_labelling")

    data object RestaurantScreen : Screen(route = "restaurant")

    data object GPSSpeechScreen : Screen(route = "gps_speech")

    data object CallScreen : Screen(route = "call")

    data object MoreFeatures: Screen(route = "more")
}