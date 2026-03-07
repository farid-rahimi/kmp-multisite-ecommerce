plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.home"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":shared-ui"))

    api(libs.androidx.material3)
}
