plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.jsegomez.store"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.jsegomez.store"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Mejora de seguridad 2 (OWASP MASVS-CODE/RESILIENCE): R8 elimina código sin uso y
            // ofusca nombres. NO convierte un secreto en seguro: solo dificulta la lectura casual.
            // Las reglas propias van en src/main/keepRules/. Guardar build/outputs/mapping/ de cada
            // release para poder leer fallos ofuscados.
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Room exporta aquí un JSON por cada versión del esquema (1.json, 2.json, 3.json...).
// Esa carpeta se SUBE a git: la prueba de migración necesita el JSON de la versión
// anterior para recrear una base antigua y comprobar que no se pierden datos.
// El plugin de Room también la expone como assets de androidTest automáticamente.
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Room: runtime = API que usamos en el código; compiler (vía KSP) = genera la implementación
    // de los DAO y de la base al compilar. Desde Room 2.7 las extensiones de corrutinas
    // (withTransaction, Flow, suspend) están dentro de room-runtime, por eso no existe room-ktx aquí.
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // WorkManager: ejecuta la actualización de la caché aunque la pantalla ya no exista.
    // DataStore: guarda la fecha del último refresh exitoso.
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    // Dispatchers.setMain: permite probar ViewModels (viewModelScope usa el hilo principal) en la JVM
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // MigrationTestHelper: lo usaremos para probar la migración 2 -> 3 con datos reales
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}