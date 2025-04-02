package com.kumar.imagelabeller.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

sealed class BottomNavOptions(
    val route: String,
    val labelOfIcon: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val onOptionClicked: (NavController) -> Unit,
) {

    data object ImageLabellingOption : BottomNavOptions(
        route = Screen.ImageLabellingScreen.route,
        labelOfIcon = "Image Labelling",
        unselectedIcon = Icons.Outlined.ImageSearch,
        selectedIcon = Icons.Filled.ImageSearch,
        onOptionClicked = {
            it.navigate(Screen.ImageLabellingScreen.route) {
                popUpTo(it.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }

        })

    data object MoreFeaturesOption : BottomNavOptions(
        route = Screen.MoreFeatures.route,
        labelOfIcon = "More Features",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
        onOptionClicked = {
            it.navigate(Screen.MoreFeatures.route) {
                popUpTo(it.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        })

    companion object {
        val bottomNavOptions = listOf(
            ImageLabellingOption,
            MoreFeaturesOption
        )
    }

}