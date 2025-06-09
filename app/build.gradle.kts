plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.furniturestore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.furniturestore"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.firebase:firebase-auth:22.1.1")

    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
