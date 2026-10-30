plugins {
 alias(libs.plugins.spotless)
 alias(libs.plugins.android.application) apply false
 alias(libs.plugins.android.library) apply false
 alias(libs.plugins.kotlin.android) apply false
 alias(libs.plugins.kotlin.jvm) apply false
 alias(libs.plugins.kotlin.compose) apply false
 alias(libs.plugins.kotlin.serialization) apply false
 alias(libs.plugins.ksp) apply false
 alias(libs.plugins.hilt) apply false
}

spotless {
 kotlin {
  target("**/src/**/*.kt")
  targetExclude("**/build/**")
  ktfmt("0.54").kotlinlangStyle()
 }
}
