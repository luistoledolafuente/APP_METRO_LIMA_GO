plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")

}

android {
    namespace = "com.tecsup.metrolimago"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tecsup.metrolimago"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Definimos nuestras versiones aquí ---
    val roomVersion = "2.6.1"
    val navVersion = "2.7.7"
    val lifecycleVersion = "2.8.3"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- DEPENDENCIAS CLAVE PARA NUESTRO PROYECTO ---

    // 1. ViewModel (Para conectar UI y Lógica)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // 2. Navigation (Para movernos entre pantallas)
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // 3. Room (Base de Datos)
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Para coroutines
    kapt("androidx.room:room-compiler:$roomVersion") // "kapt" es el procesador de anotaciones

    implementation("androidx.compose.material:material-icons-extended")

    // --- NUEVAS DEPENDENCIAS PARA GOOGLE MAPS ---
    // El SDK de Google Maps para Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
    // El SDK de Google Play Services (necesario para el mapa)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Para pedir y gestionar permisos de localización
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    // Para utilidades del mapa, como dibujar Polylines
    implementation("com.google.maps.android:maps-compose-utils:4.3.3")
}