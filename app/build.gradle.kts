import io.netty.util.internal.PlatformDependent.javaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val javaVersion = JavaVersion.VERSION_17

android {
    namespace = "com.mcards.sdk.fm.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mcards.sdk.fm.demo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        addManifestPlaceholders(mapOf("auth0Domain" to "@string/auth0_domain",
            "auth0Scheme" to "com.mcards.sdk.fm.demo"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
    }
}


dependencies {
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    implementation(platform(libs.sdk.bom))
    implementation(libs.sdk.auth)
    implementation(libs.sdk.cards)
    implementation(libs.sdk.fm)
}
