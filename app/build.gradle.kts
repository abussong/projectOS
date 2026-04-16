/*
 * ============================================================================
 * RemindMind - конфигурация модуля app
 * ============================================================================
 * Версия: 2.0
 * Авторы: Грехов М.В., Яньшина А.Ю.
 *
 * Описание: Настройки сборки приложения: версии SDK, зависимости,
 *           конфигурация Jetpack Compose.
 * ============================================================================
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("org.jetbrains.dokka") version "2.2.0"
}

android {
    namespace = "com.example.remindmind"
    compileSdk {
        version = release(36)       // Android 15 (API 36)
    }

    defaultConfig {
        applicationId = "com.example.remindmind"
        minSdk = 24                  // Android 7.0 (минимальная версия)
        targetSdk = 36               // Android 15 (целевая версия)
        versionCode = 2
        versionName = "2.1.1"          // Текущая версия приложения

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

    buildFeatures {
        compose = true               // Включение Jetpack Compose
    }
}

afterEvaluate {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
        dokkaSourceSets {
            configureEach {
                // Явно указываем sourceSet от Android
                sourceRoots.setFrom(
                    android.sourceSets.getByName("main").java.srcDirs("src/main/kotlin"),
                    "src/main/kotlin"
                )
            }
        }
    }
}
dependencies {
    //implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")


    // ==================== ХРАНЕНИЕ НАСТРОЕК ====================
    // DataStore Preferences - для сохранения выбранной темы
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ==================== РАБОТА С ДАТАМИ И ВРЕМЕНЕМ ====================
    // ThreeTenABP - бэкпорт java.time для Android
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.9")

    // ==================== JETPACK COMPOSE UI ====================
    // Базовые и расширенные иконки Material
    implementation("androidx.compose.material:material-icons-core:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // ViewModel и LiveData для Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose-android:2.10.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")

    // Системный контроллер для управления строкой состояния
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")

    // ==================== ANDROIDX БИБЛИОТЕКИ ====================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ==================== JETPACK COMPOSE BOM ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ==================== ДРУГИЕ ЗАВИСИМОСТИ ====================
    implementation(libs.places)

    // ==================== ТЕСТИРОВАНИЕ ====================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
