import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // Plugin de Serialização
    kotlin("plugin.serialization") version "1.9.22"
    // Plugin do SQLDelight
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)  // ← MUDOU DE 11 PARA 17
                }
            }
        }
    }

    // --- IOS COMPLETAMENTE REMOVIDO PARA EVITAR ERROS DE SYNC ---
    // Quando tiveres um Mac, podes descomentar isto.
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
            // Utilitários Kotlin
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            // Serialização
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Firebase KMP (GitLive)
            implementation("dev.gitlive:firebase-app:2.1.0")
            implementation("dev.gitlive:firebase-auth:2.1.0")
            implementation("dev.gitlive:firebase-firestore:2.1.0")
            implementation("dev.gitlive:firebase-storage:2.1.0")

            // SQLDelight - Runtime comum
            implementation("app.cash.sqldelight:runtime:2.0.1")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
        }

        androidMain.dependencies {
            // SQLDelight - Driver Android
            implementation("app.cash.sqldelight:android-driver:2.0.1")
        }

        iosMain.dependencies {
            // SQLDelight - Driver iOS
            implementation("app.cash.sqldelight:native-driver:2.0.1")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.eventify.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // ← MUDOU DE 11 PARA 17
        targetCompatibility = JavaVersion.VERSION_17  // ← MUDOU DE 11 PARA 17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

// Configuração do SQLDelight
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.eventify.db")
        }
    }
}