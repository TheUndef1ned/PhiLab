import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

/*
 * Release signing: CI passes PHILAB_* environment variables (from GitHub secrets),
 * locally the same values are read from keystore.properties (git-ignored, never committed).
 * Without them assembleRelease still works but produces an unsigned APK.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingValue(property: String, env: String): String? =
    System.getenv(env)?.takeIf { it.isNotBlank() } ?: keystoreProperties.getProperty(property)

val releaseStoreFile = signingValue("storeFile", "PHILAB_KEYSTORE_FILE")

android {
    namespace = "app.philab"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "app.philab"
        minSdk = 29
        targetSdk = 36
        // CI: versionCode = run number, versionName = tag without "v" (v1.2.0 → 1.2.0)
        versionCode = System.getenv("PHILAB_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("PHILAB_VERSION_NAME")?.removePrefix("v") ?: "1.0.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = signingValue("storePassword", "PHILAB_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "PHILAB_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "PHILAB_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        localeFilters += listOf("ru")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    //Logging
    implementation(libs.timber)
    //Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    //Dagger
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)
    //Room (DatabaseModule builds the database)
    implementation(libs.bundles.room)
    //Navigation
    implementation(libs.decompose)
    implementation(libs.decomposeExt)
    //MVIKotlin
    implementation(libs.bundles.mviKotlin)
    //Haze + Liquid Glass
    implementation(libs.bundles.haze)
    implementation(libs.backdrop)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
