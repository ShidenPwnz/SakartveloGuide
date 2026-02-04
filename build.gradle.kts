// PROJECT-LEVEL BUILD.GRADLE.KTS
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.devtools.ksp) apply false

    // ARCHITECT'S FIX: Define the Google Services plugin version here
    id("com.google.gms.google-services") version "4.4.2" apply false
}