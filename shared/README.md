# shared module (KMP data/domain/viewmodel)

Kotlin Multiplatform business logic module used by both Android and iOS.

## Responsibilities

- Network layer (Ktor clients + API sources)
- Local persistence (Room + SQLite)
- Repositories
- Domain use cases
- Shared ViewModels
- Dependency injection modules (Koin)

## Why This Module Is Backend-Agnostic

This module is designed so backend providers can change without rewriting the app UI:

- `domain/*` defines use-case contracts and flow orchestration
- repository interfaces abstract data sources
- concrete backend behavior is isolated in data implementations
- DI modules decide which backend implementation is active

Current production adapter is WooCommerce, but the architecture supports additional adapters (custom REST, Shopify-like APIs, headless commerce gateways, etc.).

## Package Structure

- `data/`
  - `api/woo` remote sources and converters
  - (extensible) add `api/<provider>` for other commerce backends
  - `network` clients, request/response models
  - `database` entities/dao/module
  - repositories (`cart`, `products`, `orders`, `user`, `config`, ...)
- `domain/`
  - use cases by feature (`cart`, `checkout`, `products`, `order`, `user`, ...)
- `viewmodel/`
  - shared viewmodels and DI modules

## Main Technologies

- Kotlin Multiplatform
- Ktor
- Koin
- Room + SQLite
- Paging 3 (common/runtime)
- Kotlinx Serialization
- Kotlinx Datetime
- Multiplatform Settings

## Build Notes

- Uses BuildKonfig for runtime config defaults.
- iOS and Android targets are configured in `shared/build.gradle.kts`.

## Useful Commands

```bash
./gradlew :shared:compileKotlinAndroid
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:testDebugUnitTest
```
