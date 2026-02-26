plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.category"
}

dependencies {
    implementation(project(":domain:woo-products"))
    implementation(project(":domain:config"))
    implementation(project(":domain:user"))

    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)


}