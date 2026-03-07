import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.solutionium.build"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "com.s.gradle.android.application.compose"
            implementationClass = "com.s.gradle.plugins.ApplicationComposePlugin"
        }
        register("androidApplication") {
            id = "com.s.gradle.android.application"
            implementationClass = "com.s.gradle.plugins.ApplicationPlugin"
        }
        register("androidLibraryCompose") {
            id = "com.s.gradle.android.library.compose"
            implementationClass = "com.s.gradle.plugins.LibraryComposePlugin"
        }
        register("androidLibrary") {
            id = "com.s.gradle.android.library"
            implementationClass = "com.s.gradle.plugins.LibraryPlugin"
        }
        register("androidFeature") {
            id = "com.s.gradle.android.feature"
            implementationClass = "com.s.gradle.plugins.FeaturePlugin"
        }

        register("androidDomain") {
            id = "com.s.gradle.android.domain"
            implementationClass = "com.s.gradle.plugins.DomainPlugin"
        }
        register("androidKoin") {
            id = "com.s.gradle.android.koin"
            implementationClass = "com.s.gradle.plugins.KoinPlugin"
        }
        register("jvmLibrary") {
            id = "com.s.gradle.jvm.library"
            implementationClass = "com.s.gradle.plugins.LibraryJvmPlugin"
        }
    }
}
