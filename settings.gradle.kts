pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Woo"
include(":app")
include(":domain:woo-products")
include(":feature:product-detail")
include(":feature:home")
include(":domain:woo-categories")
include(":feature:product-list")
include(":core:design-system")
include(":core:ui:common")
include(":feature:category")
include(":feature:cart")
include(":feature:account")
include(":feature:checkout")
include(":feature:orders")
include(":domain:cart")
include(":domain:checkout")
include(":domain:user")
include(":feature:address")
include(":domain:favorite")
include(":domain:config")
include(":domain:order")
include(":feature:login")
include(":domain:review")
include(":feature:review")
include(":shared")
include(":shared-ui")
