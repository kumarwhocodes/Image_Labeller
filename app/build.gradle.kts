import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Dagger Hilt
    id("dagger.hilt.android.plugin")
    // KSP
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.kumar.imagelabeller"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kumar.imagelabeller"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", getBaseUrlInCIEnvironment())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.navigation.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // okHttp Dependency
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    val cameraxversion = "1.3.0"
    implementation ("androidx.camera:camera-core:$cameraxversion")
    implementation ("androidx.camera:camera-camera2:$cameraxversion")
    implementation ("androidx.camera:camera-lifecycle:$cameraxversion")
    implementation ("androidx.camera:camera-view:$cameraxversion")

    implementation ("com.google.mlkit:image-labeling:17.0.7")

    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.2")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.4.2")
    implementation ("org.tensorflow:tensorflow-lite-task-vision:0.4.2")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.google.android.gms:play-services-auth:20.6.0")

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth-ktx")

    //dagger-hilt
    val hiltVersion = "2.48"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")  // CORRECT DEPENDENCY
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // coroutines
    val coroutineVersion = "1.3.9"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutineVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutineVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${coroutineVersion}")

    //coroutine lifecycle scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    implementation (libs.androidx.material.icons.extended)
}

fun getBaseUrlInCIEnvironment(): String {
    val propFile = rootProject.file("local.properties")
    val properties = Properties()
    properties.load(FileInputStream(propFile))
    return properties.getProperty("BASE_URL")
}