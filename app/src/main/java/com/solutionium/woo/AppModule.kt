package com.solutionium.woo


import com.solutionium.shared.data.local.localModule
import com.solutionium.feature.account.getAccountModules
import com.solutionium.feature.address.getAddressModules
import com.solutionium.feature.cart.getCartFeatureModules
import com.solutionium.feature.checkout.getCheckoutModules
import com.solutionium.feature.orders.getOrdersModules
import com.solutionium.shared.data.local.androidLocalModule
import com.solutionium.shared.viewmodel.AppVersionProvider
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
    getCartFeatureModules() +
    getCategoryModules() +
    getCheckoutModules() +
    getHomeModules() +
    getOrdersModules() +
    getProductDetailModules() +
    getProductListModules() +
    getReviewModules()
).toList()
