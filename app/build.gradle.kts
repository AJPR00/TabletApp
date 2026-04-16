import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.ajpr00.visumloop.tablet"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("C:/keystores/keyVisumloop.jks")
            storePassword = "Resistence00"
            keyAlias = "keyVisumloop"
            keyPassword = "Resistence00"
        }
    }

    defaultConfig {
        applicationId = "com.ajpr00.visumloop.tablet"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // OAuth y FirebaseUI
        buildConfigField("String","GOOGLE_CLIENT_ID","\"${properties["GOOGLE_CLIENT_ID"]}\"")
        buildConfigField("String","GOOGLE_CLIENT_SECRET","\"${properties["GOOGLE_CLIENT_SECRET"]}\"")
        buildConfigField("String","GOOGLE_REDIRECT_URI","\"https://app-visumlopp.firebaseapp.com/__/auth/handler\"")
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
    }


    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    // AndroidX Core + Lifecycle + Activity Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.compose.foundation.layout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // FirebaseUI Auth
    implementation(libs.firebase.ui.auth)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Google Drive API
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client)
    implementation(libs.google.http.client.gson)
    implementation(libs.google.api.client.android)

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Facebook
    implementation(libs.facebook.login)

    // Google Identity Services
    implementation(libs.google.identity)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // FTP
    implementation(libs.commons.net)

    // Desugar
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
