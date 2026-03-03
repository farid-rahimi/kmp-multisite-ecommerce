package com.solutionium.woo


import com.solutionium.shared.data.local.localModule
import com.solutionium.shared.data.network.NetworkConfig
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.feature.address.getAddressModules
import com.solutionium.feature.checkout.getCheckoutModules
import com.solutionium.feature.orders.getOrdersModules
import com.solutionium.shared.data.local.androidLocalModule
import com.solutionium.shared.viewmodel.AppVersionProvider
import com.solutionium.shared.viewmodel.getAccountModules
import com.solutionium.shared.viewmodel.getCartModules
import com.solutionium.shared.viewmodel.getCategoryModules
import com.solutionium.shared.viewmodel.getHomeModules
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
                consumerKey = "ck_92e3c10aef47e3f8a9bc4cc525c5ad52ae20ada2",//BuildConfig.API_CONSUMER_KEY,
                consumerSecret = "cs_3ec93d75322dc29891a5df67b756cc8bb5fa95dd",//BuildConfig.API_CONSUMER_SECRET,
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
    getOrdersModules() +
    getProductDetailModules() +
    getProductListModules() +
    getReviewModules()
).toList()
