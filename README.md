# Woo Mobile (KMP)

A Kotlin Multiplatform (KMP) WooCommerce mobile app with shared business logic and shared Compose UI for Android and iOS.

## Overview

This repository contains:

- **Android app** with 2 product flavors (Site A / Site B)
- **iOS app** (Swift host + KMP frameworks)
- **Shared domain/data/viewmodels** in Kotlin Multiplatform
- **Shared Compose UI** reused by Android and iOS

The project is structured to keep feature behavior and navigation consistent across platforms while still allowing brand-specific configuration (API base URL, auth flow, theme, app id/bundle id, and language set).

## Key Features

- Multi-brand build variants:
  - **Site A**: `en`, `fa`
  - **Site B**: `en`, `ar`
- Shared bottom-tab navigation with independent tab stacks
- Home, Category, Cart, Account, Order List, Address, Product List/Detail, Review, Checkout flows in shared UI
- Dynamic home/category content from server config
- Story and banner deeplink handling
- WooCommerce integrations:
  - Products, categories, attributes, brands
  - Cart and checkout
  - Orders and reviews
  - User/profile/address
- Local caching (Room) for improved return navigation/performance
- Koin dependency injection across shared modules
- Firebase on Android (Messaging, Analytics, Crashlytics)

## Architecture

High-level layering:

- `shared`:
  - data layer (network, db, repositories)
  - domain layer (use cases)
  - viewmodels + DI modules
- `shared-ui`:
  - shared Compose screens/components/navigation
  - iOS bootstrap bridge
- `app`:
  - Android host app, flavor wiring, Firebase config
- `iosApp`:
  - Swift host app and Xcode targets/schemes

## Modules

- [`app`](./app/README.md) - Android host app, flavors, Firebase integration
- [`shared`](./shared/README.md) - KMP business logic/data/domain/viewmodels
- [`shared-ui`](./shared-ui/README.md) - KMP shared Compose UI and shared navigation
- [`core/design-system`](./core/design-system/README.md) - legacy Android-only design-system module
- [`core/ui/common`](./core/ui/common/README.md) - legacy Android-only UI common module

## Product Flavors (Android)

Defined in `app/build.gradle.kts` with flavor dimension `site`:

- `siteA`
  - `applicationId`: `com.solutionium.woo`
  - `localeFilters`: `en`, `fa`
  - Base URL: `https://qeshminora.com/`
- `siteB`
  - `applicationId`: `ae.leparfum.shop`
  - `localeFilters`: `en`, `ar`
  - Base URL: `https://leparfum.ae/`

Google services files are flavor-specific:

- `app/src/siteA/google-services.json`
- `app/src/siteB/google-services.json`

## Tech Stack

- **Kotlin** 2.2.x
- **Kotlin Multiplatform** (Android + iOS)
- **Compose Multiplatform**
- **Jetpack Compose** (Android)
- **Ktor** (networking)
- **Koin** (DI)
- **Room + SQLite** (local data/cache)
- **Paging 3**
- **Kotlinx Serialization**
- **Multiplatform Settings**
- **Firebase** (Android): Messaging, Analytics, Crashlytics
- **GitHub Actions** for CI

## Getting Started

### Prerequisites

- JDK 17
- Android Studio (latest stable)
- Xcode (for iOS builds)
- Cocoa/system tools required by your local KMP/iOS setup

### Environment Variables

Set secrets before build (especially for CI):

- Site A:
  - `WOO_SITE_A_CONSUMER_KEY`
  - `WOO_SITE_A_CONSUMER_SECRET`
- Site B:
  - `LEPARFUM_CK`
  - `LEPARFUM_CS`

Fallback keys exist in Gradle for local debug, but production builds should always use secure env vars.

### Android Build

```bash
./gradlew :app:assembleSiteADebug
./gradlew :app:assembleSiteBDebug
```

### iOS Build

1. Open `iosApp/QSolu.xcodeproj` in Xcode.
2. Select target/scheme (`QSolu` or `QSoluSiteB` if configured).
3. Run on simulator/device.

## Tests

Examples of useful commands:

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :app:testSiteADebugUnitTest
./gradlew :app:connectedSiteADebugAndroidTest
```

## CI

GitHub Actions workflow: `.github/workflows/ci.yml`

Current CI compiles:

- `:app:compileSiteADebugKotlin`
- `:app:compileSiteBDebugKotlin`
- `:shared-ui:compileAndroidMain`
- `:shared-ui:compileKotlinIosSimulatorArm64`

Ensure both flavor `google-services.json` files are present in repo or generated in CI before build.

## Notes

- Some old Android-only modules still exist under `core/*` as legacy/transition modules.
- Shared UI is the active direction for cross-platform feature work.

