plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.cart"
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(project(":domain:cart"))
    implementation(project(":domain:config"))
    implementation(project(":domain:user"))


}