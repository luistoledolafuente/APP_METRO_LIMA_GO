import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.tecsup.metrolimago" // O 'com.tecsup.metrolimago'
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tecsup.metrolimago" // O 'com.tecsup.metrolimago'
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- CÓDIGO PARA LA CLAVE DE API ---
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }
        val mapsApiKey = properties.getProperty("MAPS_API_KEY", "")

        // Esta línea CREA el archivo BuildConfig
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        // --- FIN CÓDIGO API ---
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
        buildConfig = true // <-- ¡AQUÍ ESTÁ EL ARREGLO DE LA TERMINAL!
    }

    // --- ¡¡AQUÍ ESTÁ EL ARREGLO IMPORTANTE!! ---
    // El bloque 'composeOptions' que causaba el conflicto ha sido ELIMINADO.
}

dependencies {
    // Versiones (puedes ajustarlas)
    val roomVersion = "2.6.1"
    val lifecycleVersion = "2.8.3"
    val navigationVersion = "2.7.7"
    val composeBomVersion = "2024.06.00"

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("io.coil-kt:coil-compose:2.6.0") // Para las imágenes

    // Esta librería provee los temas XML base de Material 3 (ej. Theme.Material3.DayNight)
    implementation("com.google.android.material:material:1.12.0")

    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended") // Para todos los íconos

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$navigationVersion")

    // Room (Base de Datos)
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.maps.android:maps-compose-utils:4.3.3") // Para Polylines
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // (Test, etc.)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
