plugins {
    id("com.s.gradle.android.domain")
}

android {
    namespace = "com.solutionium.domain.order"
}

dependencies {


    api(libs.paging.runtime)


}