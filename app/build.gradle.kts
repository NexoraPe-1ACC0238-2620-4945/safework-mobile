plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// No historical/default production host. Configure published HTTPS explicitly.
val publishedApiUrl = providers.gradleProperty("safework.apiUrl").orElse("")
val localApiUrl = providers.gradleProperty("safework.localApiUrl").orElse("http://127.0.0.1:18082/")
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.nexorape.safework"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexorape.safework"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        debug { buildConfigField("String", "API_URL", quoted(localApiUrl.get())) }
        release { buildConfigField("String", "API_URL", quoted(publishedApiUrl.get())) }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":business"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.gson)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
