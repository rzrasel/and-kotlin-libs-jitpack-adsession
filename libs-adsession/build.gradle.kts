plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //|-----------------|KOTLIN PLUGIN SUPPORT|------------------|
    //|Kotlin serialization plugin via version catalog alias|----|
    alias(libs.plugins.jetbrains.kotlin.serialization)
    //id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    //kotlin("plugin.serialization") version "2.0.21"
    id("maven-publish")
}

android {
    namespace = "com.rzrasel.adsession"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_20.toString()
    }
    publishing {
        // REQUIRED FOR JITPACK
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    /*implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)*/
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("com.google.code.gson:gson:2.13.2")
    //|JSON Serialization dependency|----------------------------|
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    //
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    //
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
}
publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
                groupId = "com.github.rzrasel"
                artifactId = "libs-adsession"
                version = "1.0.0"
            }
        }
    }
}