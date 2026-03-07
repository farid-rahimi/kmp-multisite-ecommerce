plugins {
    id("com.s.gradle.android.domain")
}

android {
    namespace = "com.solutionium.domain.favorite"
}

dependencies {


    implementation(project(":domain:user"))

}