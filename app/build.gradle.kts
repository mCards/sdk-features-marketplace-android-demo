plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.mcards.sdk.fm.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mcards.sdk.fm.demo"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)

    implementation(platform(libs.sdk.bom))
    implementation(libs.sdk.auth)
    implementation(libs.sdk.cards)
    implementation(libs.sdk.fm)
}
