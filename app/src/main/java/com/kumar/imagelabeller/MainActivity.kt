package com.kumar.imagelabeller

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.kumar.imagelabeller.navigation.BottomNavBar
import com.kumar.imagelabeller.navigation.BottomNavOptions
import com.kumar.imagelabeller.navigation.MainNavGraph
import com.kumar.imagelabeller.ui.theme.ImageLabellerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startApp()
            } else {
                Log.e("CameraPermission", "Camera permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startApp()
        } else {
            cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun startApp() {
        setContent {
            ImageLabellerTheme(dynamicColor = true) {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            navController = navController,
                            bottomMenu = BottomNavOptions.bottomNavOptions
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}