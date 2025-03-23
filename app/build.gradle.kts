plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.womensafetyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.womensafetyapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
// Google Maps SDK for Android
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    // Location Services
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    // Material Components (optional, if you’re using Material design)
    implementation ("com.google.android.material:material:1.9.0")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.15.0")
    implementation(libs.firebase.database)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")

    implementation ("com.google.code.gson:gson:2.8.8")  // Add Gson library for JSON conversion
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}