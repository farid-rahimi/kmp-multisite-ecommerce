# shared module (KMP data/domain/viewmodel)

Kotlin Multiplatform business logic module used by both Android and iOS.

## Responsibilities

- Network layer (Ktor clients + API sources)
- Local persistence (Room + SQLite)
- Repositories
- Domain use cases
- Shared ViewModels
- Dependency injection modules (Koin)

## Package Structure

- `data/`
  - `api/woo` remote sources and converters
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
