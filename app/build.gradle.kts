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

    flavorDimensions += "site"
    productFlavors {
        create("siteA") {
            dimension = "site"
            applicationId = "com.solutionium.woo"
            androidResources {
                localeFilters += listOf("en", "fa")
            }
            buildConfigField("String", "SITE_BRAND", "\"SITE_A\"")
            buildConfigField("String", "API_BASE_URL", "\"https://qeshminora.com/\"")
            buildConfigField("String", "API_SITE_HOST", "\"qeshminora.com\"")
            buildConfigField("String", "API_CONSUMER_KEY", "\"${System.getenv("WOO_SITE_A_CONSUMER_KEY") ?: System.getenv("WOO_CONSUMER_KEY") ?: "fallback_key"}\"")
            buildConfigField("String", "API_CONSUMER_SECRET", "\"${System.getenv("WOO_SITE_A_CONSUMER_SECRET") ?: System.getenv("WOO_CONSUMER_SECRET") ?: "fallback_secret"}\"")
        }
        create("siteB") {
            dimension = "site"
            applicationId = "ae.leparfum.shop"
            androidResources {
                localeFilters += listOf("en", "ar")
            }
            buildConfigField("String", "SITE_BRAND", "\"SITE_B\"")

            buildConfigField("String", "API_BASE_URL", "\"https://leparfum.ae/\"")
            buildConfigField("String", "API_SITE_HOST", "\"leparfum.ae\"")
            buildConfigField("String", "API_CONSUMER_KEY", "\"${System.getenv("LEPARFUM_CK") ?: "fallback_key"}\"")
            buildConfigField("String", "API_CONSUMER_SECRET", "\"${System.getenv("LEPARFUM_CS") ?: "fallback_secret"}\"")
        }
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

    buildFeatures {
        buildConfig = true
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
    implementation(project(":feature:orders"))
    implementation(project(":shared"))



    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging) // For Cloud Messaging
    implementation(libs.firebase.analytics) // Recommended for tracking
    implementation(libs.firebase.crashlytics)


    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose.navigation)
    //implementation(libs.androidx.browser)

}
