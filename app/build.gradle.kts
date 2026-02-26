plugins {
    id("com.s.gradle.android.application")
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "com.solutionium.woo"

    defaultConfig {
        applicationId = "com.solutionium.woo"
        versionCode = 15
        versionName = "2.4"
        //resourceConfigurations.addAll(listOf("fa", "en"))
    }

    rootProject.extra.set("versionName", defaultConfig.versionName)

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    bundle {
        language {
            // This tells Google Play to NEVER strip the following language resources.
            // All other languages will be stripped, which is what we want.
            enableSplit = false
        }
    }


    buildTypes {
//        getByName("release") {
//            isMinifyEnabled = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android.txt"),
//                "proguard-rules.pro"
//            )
//        }
//        getByName("debug") {
//            isMinifyEnabled = true
//            // It's crucial to use the same ProGuard files as your release build
//            // to ensure you are testing the exact same rules.
//            proguardFiles(
//                getDefaultProguardFile("proguard-android.txt"),
//                "proguard-rules.pro"
//            )
//        }
    }
}

dependencies {

    implementation(project(":shared-ui"))
    implementation(project(":feature:product-detail"))
    implementation(project(":feature:home"))
    implementation(project(":feature:product-list"))
    implementation(project(":feature:category"))
    implementation(project(":feature:cart"))
    implementation(project(":feature:checkout"))
    implementation(project(":feature:account"))
    implementation(project(":feature:address"))
    implementation(project(":feature:orders"))
    implementation(project(":feature:review"))
    implementation(project(":shared"))



    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging) // For Cloud Messaging
    implementation(libs.firebase.analytics) // Recommended for tracking
    implementation(libs.firebase.crashlytics)


    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose.navigation)
    //implementation(libs.androidx.browser)

}
