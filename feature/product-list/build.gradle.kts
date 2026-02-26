plugins {
    id("com.s.gradle.android.feature")
    id("com.s.gradle.android.library.compose")
}

android {
    namespace = "com.solutionium.feature.product.list"
}

dependencies {

    implementation(project(":domain:woo-products"))
    implementation(project(":domain:cart"))
    implementation(project(":domain:favorite"))
    implementation(project(":domain:config"))
    implementation(project(":domain:user"))




    implementation(libs.paging.compose)
    api(libs.androidx.material3)

}