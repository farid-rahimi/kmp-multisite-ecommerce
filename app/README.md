# app module (Android host)

Android application host module.

## Responsibilities

- Android entry point (`Application`, `MainActivity`)
- Flavor configuration (`siteA`, `siteB`)
- Android-only integrations:
  - Firebase Messaging
  - Firebase Analytics
  - Firebase Crashlytics
- Wiring shared modules:
  - `:shared`
  - `:shared-ui`

## Flavor Config

Defined in `app/build.gradle.kts`:

- `siteA`
  - App ID: `com.solutionium.woo`
  - Languages: `en`, `fa`
  - API host: `qeshminora.com`
- `siteB`
  - App ID: `ae.leparfum.shop`
  - Languages: `en`, `ar`
  - API host: `leparfum.ae`

## Firebase Files

- `app/src/siteA/google-services.json`
- `app/src/siteB/google-services.json`

If either flavor is compiled in CI, its corresponding file must exist.

## Useful Commands

```bash
./gradlew :app:assembleSiteADebug
./gradlew :app:assembleSiteBDebug
./gradlew :app:testSiteADebugUnitTest
./gradlew :app:compileSiteADebugAndroidTestKotlin
```
