plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
   // alias(libs.plugins.google.services) apply false uncomment after adding google play
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.google.services)


}

android {
    namespace = "com.agriflow.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.agriflow.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"https://api.agriflowhub.xyz/api/\"")

        //ksp {
          //  arg("room.schemaLocation", "$projectDir/schemas")
        //}
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        compose = true
        buildConfig = true
    }
    //kotlinOptions {
      // jvmTarget = "11"
    //}
}

dependencies {

    implementation(libs.androidx.compose.material3)
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.coil.compose)


    // Compose
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    //implementation(libs.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.location)

    // Testing
    //testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")

    // Android Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.androidx.espresso.core)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)


}
