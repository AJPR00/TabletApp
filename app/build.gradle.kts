plugins {
    alias(libs.plugins.android.application)   // Plugin de aplicación Android
    alias(libs.plugins.kotlin.android)        // Plugin de Kotlin para Android
    alias(libs.plugins.kotlin.compose)        // Plugin Compose para habilitar soporte en Kotlin
    alias(libs.plugins.kotlin.kapt)           // KAPT (procesador de anotaciones, necesario para Room/Hilt)
    alias(libs.plugins.hilt.android)          // Plugin de Hilt para inyección de dependencias
}

android {
    namespace = "com.ajpr00.tabletapp"
    // Define el paquete base de tu aplicación. Es el identificador usado en el código y en el AndroidManifest.

    compileSdk = 36
    // Versión del SDK de Android con la que se compila tu app.
    // Debe ser lo más reciente posible para acceder a nuevas APIs.

    defaultConfig {
        applicationId = "com.ajpr00.tabletapp"
        // Identificador único de tu aplicación en Google Play. Ejemplo: com.whatsapp

        minSdk = 23
        // Versión mínima de Android que soporta tu app (Android 6.0 Marshmallow).
        // Dispositivos con una versión menor no podrán instalarla.

        //noinspection OldTargetApi
        targetSdk = 35
        // Versión de Android para la que tu app está optimizada.
        // Google Play exige que esté actualizado a la última versión estable.

        versionCode = 1
        // Número interno de versión (entero). Se incrementa en cada release para distinguir builds.

        versionName = "1.0"
        // Nombre visible de la versión (string). Ejemplo: "1.0.0-beta".

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Runner usado para ejecutar tests instrumentados en dispositivos/emuladores.
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Controla si se minifica/obfusca el código en la build de producción.
            // false = no se aplica ProGuard/R8.

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Archivos de reglas ProGuard para optimizar y obfuscar el código en release.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        // Versión de Java usada para compilar el código fuente.

        targetCompatibility = JavaVersion.VERSION_11
        // Versión de Java objetivo para la salida del bytecode.
        // Garantiza compatibilidad con Java 11.
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            // Configura el compilador de Kotlin para generar bytecode compatible con JVM 11.
            // Asegura que Kotlin y Java estén alineados.
        }
    }

    buildFeatures {
        compose = true
        // Activa el soporte de Jetpack Compose en tu proyecto.
        // Sin esto, no puedes usar Composables.
    }
}


tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    // Esto le dice a Gradle: “Cuando compiles con Java, muestra advertencias sobre APIs obsoletas.”
}

dependencies {
    // Core Android + Kotlin
    implementation(libs.androidx.core.ktx)                  // Extensiones Kotlin para Android
    implementation(libs.androidx.lifecycle.runtime.ktx)     // Ciclo de vida + coroutines
    implementation(libs.androidx.activity.compose)          // Integración Activity con Compose

    // Compose
    implementation(platform(libs.androidx.compose.bom))     // BOM para alinear versiones Compose
    implementation(libs.androidx.ui)                        // Core de Compose UI
    implementation(libs.androidx.ui.graphics)               // Gráficos en Compose
    implementation(libs.androidx.ui.tooling.preview)        // Preview en Compose
    implementation(libs.androidx.material3)                 // Material Design 3
    implementation(libs.androidx.material.icons.extended)   // Iconos extendidos de Material

    // 🎯 Google Fonts para Compose
    implementation(libs.androidx.ui.text.google.fonts)      // Permite usar GoogleFont.Provider y descargar tipografías dinámicas

    // Room (Base de datos)
    implementation(libs.androidx.room.runtime)              // Runtime de Room
    implementation(libs.androidx.room.ktx)                  // Extensiones Kotlin para Room
    kapt(libs.androidx.room.compiler)                       // Compiler de Room (procesador de anotaciones)

    // Media3 (ExoPlayer y dependencias)
    implementation(libs.media3.exoplayer)                   // Reproductor ExoPlayer
    implementation(libs.media3.ui)                          // UI de Media3
    implementation(libs.media3.common)                      // Utilidades comunes de Media3

    // Coil (carga de imágenes)
    implementation(libs.coil.compose)                       // Coil + Compose
    implementation(libs.coil.gif)                           // Carga de GIFs con Coil


    // Hilt (Inyección de dependencias)
    implementation(libs.hilt)                               // Hilt Android
    implementation(libs.hilt.navigation.compose)            // Integración Hilt + Navigation Compose
    kapt(libs.hilt.compiler)                                // Compiler de Hilt (procesador de anotaciones)

    // Navigation Compose
    implementation(libs.navigation.compose)                 // Navegación en Compose

    // Serialización Kotlin
    implementation(libs.kotlinx.serialization.json)         // Serialización JSON con Kotlinx

    // Testing
    testImplementation(libs.junit)                          // JUnit para tests unitarios
    androidTestImplementation(libs.androidx.junit)          // Extensión JUnit para Android
    androidTestImplementation(libs.androidx.espresso.core)  // Testing UI con Espresso
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM para tests Compose
    androidTestImplementation(libs.androidx.ui.test.junit4) // Testing Compose con JUnit4
    debugImplementation(libs.androidx.ui.tooling)           // Herramientas de preview en debug
    debugImplementation(libs.androidx.ui.test.manifest)     // Manifest para tests Compose
}
