plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.login"
}

dependencies {
    implementation(project(":core:design-system"))
    implementation(project(":domain:user"))
    implementation(libs.androidx.material3)

}