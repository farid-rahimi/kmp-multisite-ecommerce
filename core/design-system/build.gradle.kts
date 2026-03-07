plugins {
    id("com.s.gradle.android.library")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.core.designsystem"
}

dependencies {

    implementation(libs.androidx.material3)

    api(libs.icons.extended)
    implementation(libs.androidx.core)

}