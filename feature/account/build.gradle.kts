plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.account"
}

dependencies {
    implementation(project(":core:design-system"))
    implementation(project(":shared-ui"))
    implementation(project(":domain:user"))
    implementation(project(":domain:favorite"))
    implementation(project(":domain:order"))
    implementation(project(":domain:config"))
    implementation(libs.androidx.material3)


}
