plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.product.detail"
}

dependencies {
    implementation(project(":shared-ui"))

    implementation(project(":domain:woo-products"))
    implementation(project(":domain:favorite"))
    implementation(project(":domain:cart"))
    implementation(project(":domain:config"))
    implementation(project(":domain:user"))
    implementation(project(":domain:review"))


    implementation(project(":data:model"))


    implementation(libs.androidx.material3)
    implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

}
