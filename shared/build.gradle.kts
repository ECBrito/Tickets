import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // 1. PLUGIN DE SERIALIZAÇÃO (CRUCIAL PARA O FIREBASE/KMP)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    // CORREÇÃO: Usamos o simples androidTarget() apenas para registar o target.
    androidTarget()

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

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            // 2. SERIALIZAÇÃO (RUNTIME)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // 3. FIREBASE KMP DEPENDÊNCIAS
            val firebaseVersion = "1.13.1"
            implementation("dev.gitlive:firebase-core:$firebaseVersion")
            implementation("dev.gitlive:firebase-auth:$firebaseVersion")
            implementation("dev.gitlive:firebase-firestore:$firebaseVersion") // Base de Dados na Nuvem
            implementation("dev.gitlive:firebase-storage:$firebaseVersion")     // Upload de Imagens
        }
    }
}

android {
    namespace = "com.example.eventify.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // CORREÇÃO: Definimos o JVM Target aqui, no bloco 'android', onde o plugin espera.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}