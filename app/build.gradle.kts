import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.prontocheck"
    compileSdk = 36

    // Cargar local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.example.prontocheck"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Leer de local.properties o dejar vacío si no existe
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
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

        buildFeatures {
            compose = false
            viewBinding = true // <--- AGREGA ESTA LÍNEA
            buildConfig = true
        }
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    // Core y UI base
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Arquitectura MVVM (Lifecycle, ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // <- esta es clave

    // Para usar el delegado "by viewModels()" en la Activity
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Networking (Retrofit + Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Corrutinas (Para llamadas asíncronas)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing (Opcional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")



        // CameraX core library usando la versión más estable
        val camerax_version = "1.3.1"
        implementation("androidx.camera:camera-core:$camerax_version")
        implementation("androidx.camera:camera-camera2:$camerax_version")
        implementation("androidx.camera:camera-lifecycle:$camerax_version")
        implementation("androidx.camera:camera-view:$camerax_version")
        implementation("androidx.camera:camera-extensions:$camerax_version")




    // ML Kit - Detección de rostros
    implementation ("com.google.mlkit:face-detection:16.1.5")

    // TENSORFLOW LITE (Necesario para FaceNetHelper)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

        // SERVICIOS DE UBICACIÓN (Para solucionar el error de 'location')
        implementation("com.google.android.gms:play-services-location:21.1.0")

        // Añade esta línea específicamente:
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}






