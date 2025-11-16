import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.lombok)
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.project.lumina.client"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.project.lumina.client"
        minSdk = 28
        targetSdk = 36
<<<<<<< HEAD
        versionCode = 7
        versionName = "4.1.6"
=======
        versionCode = 6
        versionName = "4.1.5"
>>>>>>> 7ecab2664e9649c6e702bacb60cb53c1d0039246

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }
    signingConfigs {
        create("shared") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true

            storeFile = rootDir.resolve("buildKey.jks")
            keyAlias = "UntrustedKey"
            storePassword = "123456"
            keyPassword = "123456"
        }
    }
    packaging {
        jniLibs.useLegacyPackaging = true
        resources.excludes.addAll(
            setOf(
                "DebugProbesKt.bin"
            )
        )
        resources.pickFirsts.addAll(
            setOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/DEPENDENCIES"
            )
        )
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("shared")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    composeCompiler {
        includeTraceMarkers = false
        includeSourceInformation = false
        generateFunctionKeyMetaClasses = false
        featureFlags = setOf(
            ComposeFeatureFlag.OptimizeNonSkippingGroups,
            ComposeFeatureFlag.PausableComposition
        )
    }
}

dependencies {
    implementation(libs.leveldb)
    //implementation("com.github.CloudburstMC:NBT:master-SNAPSHOT")
    implementation(libs.ui.graphics)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.window)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    debugImplementation(platform(libs.log4j.bom))
    debugImplementation(libs.log4j.api)
    debugImplementation(libs.log4j.core)
    implementation(libs.bundles.netty)
    implementation(libs.expiringmap)
    implementation(libs.network.common)
    implementation(platform(libs.fastutil.bom))
    implementation(libs.fastutil.long.common)
    implementation(libs.fastutil.long.obj.maps)
    implementation(libs.fastutil.int.obj.maps)
    implementation(libs.fastutil.obj.int.maps)
    implementation(libs.jose4j)
    implementation(libs.math)
    implementation(libs.nbt)
    implementation(libs.snappy)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.http.client)
    implementation(libs.bcprov)
    implementation(libs.okhttp)
    api(libs.net.raphimc.minecraftauth)
    implementation("com.amplitude:analytics-android:1.+")
    implementation("com.github.SmartToolFactory:Compose-Colorful-Sliders:1.2.2")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.airbnb.android:lottie-compose:6.0.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation(project(":animatedux"))
    implementation(project(":Pixie"))
    implementation(project(":Lunaris"))
    implementation(project(":SSC"))
    implementation(project(":TablerIcons"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation(libs.kotlinx.serialization.json.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.activity:activity-compose:1.8.0")
}