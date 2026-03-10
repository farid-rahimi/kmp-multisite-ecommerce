package com.solutionium.woo


import com.solutionium.shared.data.local.localModule
import com.solutionium.shared.data.network.NetworkConfig
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.data.local.androidLocalModule
import com.solutionium.shared.viewmodel.AppVersionProvider
import com.solutionium.shared.viewmodel.getAccountModules
import com.solutionium.shared.viewmodel.getAddressModules
import com.solutionium.shared.viewmodel.getCartModules
import com.solutionium.shared.viewmodel.getCategoryModules
import com.solutionium.shared.viewmodel.getCheckoutModules
import com.solutionium.shared.viewmodel.getHomeModules
import com.solutionium.shared.viewmodel.getOrderListModules
import com.solutionium.shared.viewmodel.getProductListModules
import com.solutionium.shared.viewmodel.getProductDetailModules
import com.solutionium.shared.viewmodel.getReviewModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    viewModel { MainViewModel(get()) }
    single<NetworkConfigProvider> {
        NetworkConfigProvider {
            NetworkConfig(
                baseUrl = BuildConfig.API_BASE_URL,
                consumerKey = BuildConfig.API_CONSUMER_KEY,
                consumerSecret = BuildConfig.API_CONSUMER_SECRET,
                passwordLoginPath = when (BuildConfig.SITE_BRAND) {
                    "SITE_B" -> "wp-json/woo-mobile-auth/v1/login_user"
                    else -> "wp-json/digits/v1/login_user"
                },
                passwordRegisterPath = when (BuildConfig.SITE_BRAND) {
                    "SITE_B" -> "wp-json/woo-mobile-auth/v1/register_user"
                    else -> "wp-json/digits/v1/register_user"
                },
                enableNetworkLogs = true,
            )
        }
    }
    factory<AppVersionProvider> {
        AppVersionProvider {
            runCatching {
                val context = androidContext()
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull()
        }
    }
}

val allModules = (
    setOf(androidLocalModule, localModule, appModule) +
    getAccountModules() +
    getAddressModules() +
    getCartModules() +
    getCategoryModules() +
    getCheckoutModules() +
    getHomeModules() +
    getOrderListModules() +
    getProductDetailModules() +
    getProductListModules() +
    getReviewModules()
).toList()
