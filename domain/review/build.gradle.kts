plugins {
    id("com.s.gradle.android.domain")
}

android {
    namespace = "com.solutionium.domain.review"
}

dependencies {

    api(libs.paging.runtime)

}