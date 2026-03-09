plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    val frameworkBaseName = "sharedUIKit"

    androidLibrary {
        namespace = "com.solutionium.core.ui.common"
        compileSdk = 36
        minSdk = 24
        androidResources {
            enable = true
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = frameworkBaseName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = frameworkBaseName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(libs.compose.ui.backhandler)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(libs.paging.compose)
                implementation(libs.koin.compose)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
                implementation(project(":shared"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.accompanist.pager)
                implementation(libs.androidx.activity.compose)
                implementation(libs.coil.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.ktor.client.okhttp)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.solutionium.sharedui.resources"
}
