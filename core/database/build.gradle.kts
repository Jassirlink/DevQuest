plugins {
 alias(libs.plugins.android.library)
 alias(libs.plugins.kotlin.android)
 alias(libs.plugins.ksp)
}
android {
 namespace = "com.devquest.core.database"
 compileSdk = 36
 defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
 implementation(libs.coroutines)
 testImplementation(libs.junit)
 testImplementation(libs.coroutines.test)
 api(libs.room.runtime)
 implementation(libs.room.ktx)
 ksp(libs.room.compiler)
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.android.runner)
}
