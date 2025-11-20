import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    androidTarget()

    // --- IOS DESATIVADO TEMPORARIAMENTE ---
    /*
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    */

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Dependências do Firebase (Agora o Gradle só vai tentar resolver para Android)
            implementation("dev.gitlive:firebase-core:1.13.1")
            implementation("dev.gitlive:firebase-auth:1.13.1")
            implementation("dev.gitlive:firebase-firestore:1.13.1")
            implementation("dev.gitlive:firebase-storage:1.13.1")
        }

        /*
        iosMain.dependencies {
            // Dependências iOS comentadas
        }
        */
    }
}

android {
    namespace = "com.example.eventify.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}