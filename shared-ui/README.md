# shared-ui module (KMP Compose UI)

Shared Compose UI module consumed by both Android and iOS.

## Responsibilities

- Shared screens and components
- Shared app navigation (tabs + nested flows)
- Shared theme and typography
- iOS bootstrap/bridge for initializing shared runtime

## Implemented Shared Screens

- Home
- Category + All Items
- Cart
- Account
- Address
- Orders list
- Product list/detail
- Reviews
- Checkout and related states

## Navigation

`navigation/SharedShopRoot.kt` holds shared navigation with:

- Bottom tabs: Home, Category, Cart, Account
- Independent stack per tab
- Shared deep links from stories/banners
- Checkout flow routes

## iOS Bridge

`iosMain/bootstrap/IosKoinBridge.kt` initializes shared Koin for iOS with:

- brand (`SITE_A` / `SITE_B`)
- base URL
- consumer key/secret
- brand-specific login endpoint path

## Useful Commands

```bash
./gradlew :shared-ui:compileAndroidMain
./gradlew :shared-ui:compileKotlinIosSimulatorArm64
./gradlew :shared-ui:test
```
