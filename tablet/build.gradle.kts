import java.util.Properties

plugins {
    // --- Android base ---
    alias(libs.plugins.android.application)     // Plugin principal de apps Android

    // --- Kotlin ---
    alias(libs.plugins.kotlin.android)          // Kotlin para Android
    alias(libs.plugins.kotlin.compose)          // Compose habilitado
    alias(libs.plugins.kotlin.serialization)    // Serialización JSON moderna

    // --- Procesador de anotaciones moderno ---
    alias(libs.plugins.ksp)                     // KSP (más rápido que kapt)

    // --- Inyección de dependencias ---
    alias(libs.plugins.hilt.android)            // Hilt para DI

    // --- Firebase ---
    alias(libs.plugins.google.services)         // Necesario para Auth/Firestore
}

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {

    namespace = "com.ajpr00.tablet"
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
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

        isCoreLibraryDesugaringEnabled = true   // Permite usar APIs modernas en Android viejo
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }

    buildFeatures {
        compose = true       // Activamos Jetpack Compose
        buildConfig = true   // Para generar BuildConfig.java
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {

    // ---------------------------------------------------------
    // 🧩 MÓDULOS DEL PROYECTO
    // ---------------------------------------------------------
    implementation(project(":ui_common"))          // Componentes UI compartidos
    implementation(project(":presentation_common")) // Lógica de presentación compartida
    implementation(project(":core"))               // Modelos y dominio
    implementation(project(":data"))               // Repositorios, DAOs, etc.


    // ---------------------------------------------------------
    // 🧱 CORE ANDROID
    // ---------------------------------------------------------
    implementation(libs.androidx.core.ktx)         // Extensiones modernas de Android
    implementation(libs.androidx.lifecycle.runtime.ktx) // Ciclo de vida moderno
    implementation(libs.androidx.activity.compose) // Actividades basadas en Compose


    // ---------------------------------------------------------
    // 🎨 COMPOSE UI
    // ---------------------------------------------------------
    implementation(platform(libs.androidx.compose.bom)) // BOM: controla versiones automáticamente

    implementation(libs.androidx.ui)                     // UI base
    implementation(libs.androidx.ui.graphics)            // Gráficos
    implementation(libs.androidx.ui.tooling.preview)     // Previews en Android Studio

    implementation(libs.androidx.material3)              // Material 3
    implementation(libs.androidx.material.icons.extended)// Iconos extra

    implementation(libs.androidx.compose.foundation.layout) // Layouts básicos


    // ---------------------------------------------------------
    // 💾 DATASTORE (Preferencias modernas)
    // ---------------------------------------------------------
    implementation(libs.androidx.datastore.preferences)  // Reemplazo de SharedPreferences


    // ---------------------------------------------------------
    // 🗄️ ROOM (Base de datos local)
    // ---------------------------------------------------------
    implementation(libs.androidx.room.runtime)           // Motor de Room
    implementation(libs.androidx.room.ktx)               // Corrutinas + helpers
    ksp(libs.androidx.room.compiler)                     // Genera DAOs y DB (KSP)


    // ---------------------------------------------------------
    // 🎬 MEDIA3 (ExoPlayer moderno)
    // ---------------------------------------------------------
    implementation(libs.media3.exoplayer)                // Reproductor
    implementation(libs.media3.ui)                       // UI del reproductor
    implementation(libs.media3.common)                   // Utilidades comunes


    // ---------------------------------------------------------
    // 🖼️ COIL (Carga de imágenes)
    // ---------------------------------------------------------
    implementation(libs.coil.compose)                    // Carga imágenes rápido y fácil


    // ---------------------------------------------------------
    // 🧩 HILT (Inyección de dependencias)
    // ---------------------------------------------------------
    implementation(libs.hilt)                            // Hilt runtime
    implementation(libs.hilt.navigation.compose)         // Navegación con Hilt
    ksp(libs.hilt.compiler)                              // Genera código DI


    // ---------------------------------------------------------
    // 🧭 NAVEGACIÓN
    // ---------------------------------------------------------
    implementation(libs.navigation.compose)              // Navegación declarativa


    // ---------------------------------------------------------
    // 🔤 FUENTES
    // ---------------------------------------------------------
    implementation(libs.androidx.ui.text.google.fonts)   // Google Fonts en Compose


    // ---------------------------------------------------------
    // 🧮 DESUGARING
    // ---------------------------------------------------------
    coreLibraryDesugaring(libs.desugar.jdk.libs)         // APIs modernas en Android viejo


    // ---------------------------------------------------------
    // 🌐 SERVIDOR LOCAL (NanoHTTPD)
    // ---------------------------------------------------------
    implementation(libs.nanohttpd)                       // Servidor HTTP ligero para LAN


    // ---------------------------------------------------------
    //
    // ---------------------------------------------------------
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.zxing.core)

    // Mapper Gson
    implementation(libs.retrofit.converter.gson)
}
