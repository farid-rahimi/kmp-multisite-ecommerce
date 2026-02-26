plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.review"
}

dependencies {
    implementation(project(":shared-ui"))
    implementation(project(":domain:config"))
    implementation(project(":domain:user"))
    implementation(project(":domain:review"))

    implementation(libs.paging.compose)
    implementation(libs.androidx.material3)

}
