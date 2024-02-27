plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.karag.civilprotectionapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.karag.civilprotectionapp"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    dataBinding {
        enable = true
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-firestore:24.10.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation ("androidx.activity:activity:1.7.0")
    implementation ("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.firebaseui:firebase-ui-storage:7.2.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation ("com.google.android.material:material:1.5.0")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.activity:activity:1.2.0")
    implementation ("androidx.fragment:fragment:1.3.0")

}