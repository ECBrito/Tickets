import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // --- CÂMARA (CameraX) ---
            implementation("androidx.camera:camera-core:1.3.0")
            implementation("androidx.camera:camera-camera2:1.3.0")
            implementation("androidx.camera:camera-lifecycle:1.3.0")
            implementation("androidx.camera:camera-view:1.3.0")

            // --- ML KIT (Leitura de Código de Barras) ---
            implementation("com.google.mlkit:barcode-scanning:17.2.0")

            // --- PERMISSÕES (Accompanist) ---
            implementation("com.google.accompanist:accompanist-permissions:0.34.0")

            // --- UTILITÁRIOS (Para evitar conflitos de Futures) ---
            implementation("com.google.guava:guava:31.1-android")
            implementation("com.google.android.gms:play-services-location:21.1.0")
        }

        commonMain.dependencies {
            // Google Maps
            implementation("com.google.maps.android:maps-compose:4.3.3")
            implementation("com.google.android.gms:play-services-maps:18.2.0")

            // MVVM & Lifecycle
            implementation("dev.icerock.moko:mvvm-core:0.16.1")
            implementation("dev.icerock.moko:mvvm-flow:0.16.1")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

            // Utils
            implementation("com.google.zxing:core:3.5.2")
            implementation("androidx.compose.material:material-icons-extended:1.6.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation(libs.io.coil.compose)

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Navigation & Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            // Firebase KMP
            implementation("dev.gitlive:firebase-auth:2.1.0")

            // Shared Module
            implementation(projects.shared)

            // FIREBASE Android Nativo (BOM)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.7.1"))
            implementation("com.google.firebase:firebase-auth-ktx")
            implementation("com.google.firebase:firebase-firestore-ktx")

            // --- FIREBASE CLOUD MESSAGING (FCM) ---
            implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")

            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.7.2")) // Tenta uma versão recente
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.eventify"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.eventify"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.play.services.location)
    debugImplementation(compose.uiTooling)
}