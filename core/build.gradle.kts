plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.dokka)
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    // -------------------------
    //  OkHttp
    // -------------------------

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.serialization.json)
    testImplementation("junit:junit:4.13.2")
}
