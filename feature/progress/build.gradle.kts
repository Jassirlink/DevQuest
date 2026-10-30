plugins {
 alias(libs.plugins.android.library)
 alias(libs.plugins.kotlin.android)
 alias(libs.plugins.kotlin.compose)
}
android {
 namespace = "com.devquest.feature.progress"
 compileSdk = 36
 defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose = true }
}
dependencies {
 implementation(libs.coroutines)
 testImplementation(libs.junit)
 testImplementation(libs.coroutines.test)
 implementation(platform(libs.compose.bom))
 implementation(libs.compose.ui)
 implementation(libs.compose.foundation)
 implementation(libs.compose.material)
 implementation(libs.compose.preview)
 implementation(libs.lifecycle)
 implementation(project(":core:model"))
 implementation(project(":core:designsystem"))
 implementation(project(":domain"))
 implementation(project(":core:common"))
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.android.runner)
}
