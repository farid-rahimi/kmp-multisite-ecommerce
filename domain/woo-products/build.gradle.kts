plugins {
    id("com.s.gradle.android.domain")
}

android {
    namespace = "com.solutionium.domain.woo.products"
}

dependencies {


    api(libs.paging.runtime)

}