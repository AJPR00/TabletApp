import java.util.Properties

plugins {
    alias(libs.plugins.android.library) // Convierte este módulo en una librería Android (necesario para Room, Hilt, Firebase…)
    alias(libs.plugins.kotlin.android)  // Activa Kotlin para Android (permite usar coroutines, AndroidX, etc.)

    alias(libs.plugins.ksp)             // KSP: procesa anotaciones (Room, Hilt, Serialization…)
    alias(libs.plugins.hilt.android)    // Plugin de Hilt para generar código de inyección de dependencias
}

// Cargamos variables privadas desde local.properties (client_id, secrets…)
val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.ajpr00.data" // Nombre del paquete base del módulo
    compileSdk = 36               // Versión del SDK con el que compila este módulo

    defaultConfig {
        minSdk = 23               // Mínima versión de Android soportada

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro") // Reglas ProGuard para apps que consuman esta librería

        // Variables que se generan en BuildConfig.java
        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${properties["GOOGLE_CLIENT_ID"]}\""
        )

        buildConfigField(
            "String",
            "GOOGLE_CLIENT_SECRET",
            "\"${properties["GOOGLE_CLIENT_SECRET"]}\""
        )

        buildConfigField(
            "String",
            "GOOGLE_REDIRECT_URI",
            "\"https://app-visumlopp.firebaseapp.com/__/auth/handler\""
        )

        buildConfigField(
            "String",
            "AES_KEY_HEX",
            "\"${properties["AES_KEY"]}\""
        )

    }

    buildFeatures {
        buildConfig = true // Activa BuildConfig.java para poder usar constantes en tiempo de compilación
    }

    buildTypes {
        release {
            isMinifyEnabled = false // No ofusca en release (puedes activarlo más adelante)

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Reglas base de Google
                "proguard-rules.pro"                                     // Reglas personalizadas
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Nivel de Java permitido en Android
        targetCompatibility = JavaVersion.VERSION_11 // Bytecode generado compatible con Android
        isCoreLibraryDesugaringEnabled = true        // Permite usar APIs modernas de Java en Android
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        // Kotlin genera bytecode compatible con Android (Java 11)
    }

    packaging {
        // Evita conflictos de licencias duplicadas (muy común con Firebase + Google APIs)
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {

    implementation(project(":core")) // Importa el módulo core (Kotlin puro)

    // -------------------------
    // Firebase
    // -------------------------
    implementation(platform(libs.firebase.bom)) // BOM: asegura versiones compatibles entre sí
    implementation(libs.firebase.firestore)    // Base de datos en la nube
    implementation(libs.firebase.auth)         // Autenticación Firebase

    // -------------------------
    // Google Drive API
    // -------------------------
    implementation(libs.google.api.services.drive)   // Cliente oficial de Google Drive
    implementation(libs.google.api.client.android)   // Cliente HTTP para Android

    // -------------------------
    // Google Identity / Credentials
    // -------------------------
    implementation(libs.androidx.credentials) // Nuevo sistema de credenciales de Android
    implementation(libs.google.identity)      // Login con Google moderno

    // -------------------------
    // Facebook Login
    // -------------------------
    implementation(libs.facebook.login)

    // -------------------------
    // AndroidX
    // -------------------------
    implementation(libs.androidx.core.ktx)           // Extensiones Kotlin para Android
    implementation(libs.androidx.lifecycle.runtime.ktx) // Coroutines + lifecycle

    // -------------------------
    // Room (Base de datos local)
    // -------------------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Genera DAOs, entidades y código de base de datos

    // -------------------------
    // Hilt (Inyección de dependencias)
    // -------------------------
    implementation(libs.hilt)
    ksp(libs.hilt.compiler) // Genera código de Hilt

    // -------------------------
    // Retrofit + OkHttp (Networking)
    // -------------------------
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // -------------------------
    // Kotlin Serialization
    // -------------------------
    implementation(libs.kotlinx.serialization.json)

    // -------------------------
    // DataStore (Reemplazo moderno de SharedPreferences)
    // -------------------------
    implementation(libs.androidx.datastore.preferences)

    // -------------------------
    // FTP (Apache Commons Net)
    // -------------------------
    implementation(libs.commons.net)

    // -------------------------
    // Media3 (ExoPlayer moderno)
    // -------------------------
    implementation(libs.media3.common)

    // -------------------------
    // Desugaring (APIs modernas de Java)
    // -------------------------
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // -------------------------
    // Testing
    // -------------------------
    testImplementation(libs.junit)

    // Localizador Lan
    implementation(libs.jmdns)
}

// Exporta los esquemas de Room (útil para migraciones y debugging)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
