// NO FICHEIRO build.gradle.kts (RAIZ DO PROJETO)

plugins {
    // O alias é suficiente para carregar o plugin sem o aplicar na raiz
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    // Plugin do Google Services (Firebase)
    id("com.google.gms.google-services") version "4.4.1" apply false

}

allprojects {
    repositories {
        // 1. Repositórios Padrão (Prioridade Alta)
        google()
        mavenCentral()

        // 2. Repositórios Específicos para Bibliotecas KMP (GitLive, etc.)
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://jitpack.io") }

        // 3. Repositórios de Fallback
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
    }
}