import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // Plugin do SQLDelight
    id("app.cash.sqldelight") version "2.0.2"
}

kotlin {
    // Configuração do Android Target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11" // Forma mais estável de definir o target
            }
        }
    }

    // Configuração do iOS Target
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    // Dependências
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            // SQLDelight
            implementation("app.cash.sqldelight:runtime:2.0.2")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
        }

        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:2.0.2")
        }

        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.0.2")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.eventify.db")
        }
    }
}