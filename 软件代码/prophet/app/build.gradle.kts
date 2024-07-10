import com.google.devtools.ksp.gradle.model.Ksp

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //添加kapt
    id("kotlin-kapt").apply(true)
    id("dagger.hilt.android.plugin").apply(true)
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}


android {
    namespace = "com.example.prophet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.prophet"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.dagger.hilt.android)
    implementation(libs.hilt.navigation.compose)
//    implementation(libs.org.eclipse.paho.client.mqttv3)
//    implementation(libs.org.eclipse.paho.android.service)
    implementation("com.google.accompanist:accompanist-permissions:0.19.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.org.eclipse.paho.mqttv5.client)
    implementation(libs.org.eclipse.paho.android.service)
    implementation(libs.support.v4)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    kapt(libs.hilt.compiler)

}

dependencies {
    val room_version = "2.6.1"

    implementation(libs.room.runtime)
    annotationProcessor(libs.androidx.room.room.compiler4)

    // To use Kotlin annotation processing tool (kapt)
    kapt(libs.androidx.room.room.compiler4)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.room.ktx)

    // optional - RxJava2 support for Room
    implementation(libs.androidx.room.room.rxjava2)

    // optional - RxJava3 support for Room
    implementation(libs.androidx.room.room.rxjava3)

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.androidx.room.guava)

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)

    implementation(libs.room.runtime)
}