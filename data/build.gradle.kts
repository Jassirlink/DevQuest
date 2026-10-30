plugins {
 alias(libs.plugins.android.library)
 alias(libs.plugins.kotlin.android)
 alias(libs.plugins.kotlin.serialization)
 alias(libs.plugins.ksp)
 alias(libs.plugins.hilt)
}
android {
 testOptions { unitTests.isIncludeAndroidResources = true }
 namespace = "com.devquest.data"
 compileSdk = 36
 defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 testImplementation(libs.robolectric)
 testImplementation(libs.android.test)
 androidTestImplementation(libs.coroutines.test)
 implementation(libs.coroutines)
 testImplementation(libs.junit)
 testImplementation(libs.coroutines.test)
 implementation(project(":domain"))
 implementation(project(":core:model"))
 implementation(project(":core:common"))
 implementation(project(":core:database"))
 implementation(project(":core:datastore"))
 implementation(libs.serialization)
 implementation(libs.room.ktx)
 androidTestImplementation(libs.room.testing)
 implementation(libs.hilt)
 ksp(libs.hilt.compiler)
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.android.runner)
}

tasks.withType<Test>().configureEach {
 val testHome = layout.buildDirectory.dir("test-home").get().asFile
 doFirst { testHome.mkdirs() }
 systemProperty("user.home", testHome.absolutePath)
 systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
}
