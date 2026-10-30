plugins {
 alias(libs.plugins.android.library)
 alias(libs.plugins.kotlin.android)
}
android {
 namespace = "com.devquest.core.datastore"
 compileSdk = 36
 defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation(libs.coroutines)
 testImplementation(libs.junit)
 testImplementation(libs.coroutines.test)
 implementation(project(":core:model"))
 implementation(libs.datastore)
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.android.runner)
}
