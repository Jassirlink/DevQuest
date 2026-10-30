plugins {
 alias(libs.plugins.android.application)
 alias(libs.plugins.kotlin.android)
 alias(libs.plugins.kotlin.compose)
 alias(libs.plugins.kotlin.serialization)
 alias(libs.plugins.ksp)
 alias(libs.plugins.hilt)
}
android {
 testOptions { unitTests.isIncludeAndroidResources = true }
 namespace = "com.devquest.app"
 compileSdk = 36
 defaultConfig { minSdk = 26; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; applicationId = "com.devquest.android"; targetSdk = 36; versionCode = 1; versionName = "1.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose = true }
}
dependencies {
 testImplementation(platform(libs.compose.bom))
 testImplementation(libs.compose.test)
 testImplementation(libs.robolectric)
 testImplementation(libs.android.test)
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
 implementation(project(":core:motion"))
 implementation(project(":core:model"))
 implementation(project(":core:database"))
 implementation(project(":core:datastore"))
 implementation(project(":core:common"))
 implementation(project(":domain"))
 implementation(project(":data"))
 implementation(project(":feature:onboarding"))
 implementation(project(":feature:home"))
 implementation(project(":feature:learn"))
 implementation(project(":feature:lesson"))
 implementation(project(":feature:forge"))
 implementation(project(":feature:practice"))
 implementation(project(":feature:progress"))
 implementation(project(":feature:profile"))
 implementation(libs.activity)
 implementation(libs.navigation)
 implementation(libs.viewmodel)
 implementation(libs.viewmodel.savedstate)
 implementation(libs.serialization)
 debugImplementation(libs.compose.tooling)
 androidTestImplementation(platform(libs.compose.bom))
 androidTestImplementation(libs.compose.test)
 debugImplementation(libs.compose.test.manifest)
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
