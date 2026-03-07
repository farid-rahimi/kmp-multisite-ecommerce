plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.checkout"
}

dependencies {
    implementation(project(":domain:cart"))
    implementation(project(":domain:checkout"))
    implementation(project(":domain:user"))
    implementation(project(":domain:config"))
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

}