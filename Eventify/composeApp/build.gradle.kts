import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.compose.material:material-icons-extended:1.6.0")
            // A dependência debugImplementation foi movida para o bloco 'dependencies' no final do arquivo.
        }

        commonMain.dependencies {
            implementation("com.google.maps.android:maps-compose:4.3.3")
            implementation("com.google.android.gms:play-services-maps:18.2.0")
            implementation("androidx.compose.material:material-icons-extended")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // DEPENDÊNCIAS DE UI DO EVENTIFY:
            implementation(libs.androidx.navigation.compose)
            implementation(libs.io.coil.compose)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// LOCAL CORRETO PARA DEPENDÊNCIAS ESPECÍFICAS DE CONFIGURAÇÃO ANDROID (DEBUG, RELEASE)
dependencies {
    // Movido de androidMain.dependencies para resolver 'Unresolved reference: debugImplementation'
    debugImplementation(compose.uiTooling)
}