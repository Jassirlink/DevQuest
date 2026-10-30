plugins {
 alias(libs.plugins.kotlin.jvm)
 alias(libs.plugins.kotlin.serialization)
}
kotlin { jvmToolchain(17) }
dependencies {
 implementation(libs.coroutines)
 testImplementation(libs.junit)
 testImplementation(libs.coroutines.test)
 api(libs.serialization)
}
