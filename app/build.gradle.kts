import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.serialization)


}

android {
    namespace = "com.hady.stonesforever"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hady.stonesforever"
        minSdk = 33
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
    packaging {
        resources {
            // For Kotlin DSL (build.gradle.kts):
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES" // <-- Add this line!
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp ("com.google.dagger:hilt-compiler:2.56.2")

    // Hilt Core
    implementation(libs.hilt.android)
    //ksp(libs.hilt.compiler)

    // Hilt for Android lifecycle components
    implementation(libs.androidx.hilt.navigation.compose)
    //ksp(libs.androidx.hilt.compiler)

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Google Sign-In & Drive API
    implementation(libs.bundles.google.drive.api)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.kotlinx.serialization.json)

    // Core Credential Manager library
    implementation("androidx.credentials:credentials:1.5.0")
// Or the latest stable version
// Google Identity integration for Credential Manager (this contains GetAuthorizationOption)
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
// Or the latest stable version
// If you're also using the original Google Sign-In for other parts, keep this:
    implementation("com.google.android.gms:play-services-auth:21.3.0")
// Or the latest stable version
// Google API Client for Android (for Drive service)
    implementation("com.google.api-client:google-api-client-android:2.8.0")
// Or the latest stable version
    implementation("com.google.apis:google-api-services-drive:v3-rev20250701-2.0.0")
// Or the latest stable version
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")


    implementation("org.apache.poi:poi:5.4.1")

}