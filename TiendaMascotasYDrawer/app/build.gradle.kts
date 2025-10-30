
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tiendamascotas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tiendamascotas"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    // AndroidX Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Navigation Drawer - IMPORTANTE
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // CoordinatorLayout - IMPORTANTE para el AppBar
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Volley para peticiones HTTP
    implementation("com.android.volley:volley:1.2.1")

    // Picasso para cargar imágenes
    implementation("com.squareup.picasso:picasso:2.8")

    // Fragment
    implementation("androidx.fragment:fragment:1.6.2")
}