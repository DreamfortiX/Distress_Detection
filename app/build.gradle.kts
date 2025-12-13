plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    aaptOptions {
        noCompress += "tflite"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    // AndroidX core
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.activity:activity-ktx:1.8.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Room database
    implementation ("androidx.room:room-runtime:2.7.0-alpha07")
    implementation ("androidx.room:room-ktx:2.7.0-alpha07")
    kapt ("androidx.room:room-compiler:2.7.0-alpha07")

    // TensorFlow Lite
    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // Networking
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Image loading
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    // CameraX dependencies
    implementation(libs.androidx.activity.v190)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")


    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
// Or if using AndroidX with newer version:
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

}
