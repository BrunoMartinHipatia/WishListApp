plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.example.wishlistapp"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.steam.wishlistapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }



        buildTypes {
            debug {
                buildConfigField("String", "STEAM_API_BASE_URL", "\"http://api.steampowered.com/ISteamApps/GetAppList/v0002/\"")
                buildConfigField("String", "STEAM_API_KEY", "\"B9713D51AF83EC3366A2B662C6D0EC38\"")
            }
            release {
                buildConfigField("String", "STEAM_API_BASE_URL", "\"http://api.steampowered.com/ISteamApps/GetAppList/v0002/\"")
                buildConfigField("String", "STEAM_API_KEY", "\"B9713D51AF83EC3366A2B662C6D0EC38\"")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

        implementation(libs.kotlinx.metadata.jvm)


    val room_version = "2.6.1"
    implementation(libs.accompanist.swiperefresh) // O la última versión estable
    implementation(libs.material3)

    implementation(libs.androidx.room.runtime)
    implementation ("androidx.compose.foundation:foundation:1.5.0")
    implementation(libs.google.firebase.storage.ktx)

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    kapt(libs.androidx.room.compiler)

    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor(libs.androidx.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.material3.v140alpha10)
    // GSON


    implementation(libs.converter.gson)


    // coroutine

    implementation (libs.androidx.lifecycle.viewmodel.compose)
    //room
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.coil.compose)
    implementation (libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.firebase.storage)

    implementation(libs.firebase.database)

    implementation (libs.firebase.firestore)
    implementation (libs.firebase.auth)
    implementation (libs.firebase.core)
}