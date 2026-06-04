plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // KSP
    alias(libs.plugins.ksp)

    // Hilt
    alias(libs.plugins.hilt.android)

    // Firebase
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ajpr00.visumloop.mobile"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.ajpr00.visumloop.mobile"

        minSdk = 23
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    // Modules
    implementation(project(":ui_common"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":presentation_common"))

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.compose.foundation.layout)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)

    // Mapper Gson
    implementation(libs.retrofit.converter.gson)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Genera DAOs, entidades y código de base de datos


    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    // Coil
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)

    ksp(libs.hilt.compiler)

    // Navigation
    implementation(libs.navigation.compose)

    // Fonts
    implementation(libs.androidx.ui.text.google.fonts)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // --- TEST UNITARIOS ---
    testImplementation(libs.junit)

    // --- TEST INSTRUMENTADOS ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- COMPOSE UI TESTING ---
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Necesario para createComposeRule()
    debugImplementation(libs.androidx.ui.test.manifest)

    // LectorQR
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.zxing.core)

}