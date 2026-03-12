package com.solutionium.sharedui.bootstrap

import com.solutionium.shared.data.api.woo.getApiModule
import com.solutionium.shared.data.local.applyPlatformLanguage
import com.solutionium.shared.data.local.iosLocalModule
import com.solutionium.shared.data.network.NetworkConfig
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.viewmodel.getAccountModules
import com.solutionium.shared.viewmodel.getAddressModules
import com.solutionium.shared.viewmodel.getCartModules
import com.solutionium.shared.viewmodel.getCategoryModules
import com.solutionium.shared.viewmodel.getCheckoutModules
import com.solutionium.shared.viewmodel.getHomeModules
import com.solutionium.shared.viewmodel.getOrderListModules
import com.solutionium.shared.viewmodel.getProductDetailModules
import com.solutionium.shared.viewmodel.getProductListModules
import com.solutionium.shared.viewmodel.getReviewModules
import com.solutionium.shared.viewmodel.iosAppModule
import com.solutionium.sharedui.designsystem.theme.WooBrand
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

object IosRuntimeConfig {
    var brand: WooBrand = WooBrand.SiteA
}

class IosKoinBridge {
    fun initKoin(
        siteBrand: String,
        baseUrl: String,
        consumerKey: String,
        consumerSecret: String,
        paymentReturnScheme: String,
    ) {
        IosRuntimeConfig.brand = if (siteBrand.uppercase() == "SITE_B") WooBrand.SiteB else WooBrand.SiteA
        val defaultLanguage = "en"
        val savedLanguage = NSUserDefaults.standardUserDefaults.stringForKey("app_language")
        applyPlatformLanguage(savedLanguage ?: defaultLanguage)

        val networkConfigModule = module {
            single<NetworkConfigProvider> {
                NetworkConfigProvider {
                    NetworkConfig(
                        baseUrl = baseUrl,
                        consumerKey = consumerKey,
                        consumerSecret = consumerSecret,
                        paymentReturnScheme = paymentReturnScheme,
                        passwordLoginPath = if (siteBrand.uppercase() == "SITE_B") {
                            "wp-json/woo-mobile-auth/v1/login_user"
                        } else {
                            "wp-json/digits/v1/login_user"
                        },
                        passwordRegisterPath = if (siteBrand.uppercase() == "SITE_B") {
                            "wp-json/woo-mobile-auth/v1/register_user"
                        } else {
                            "wp-json/digits/v1/register_user"
                        },
                        enableNetworkLogs = true,
                    )
                }
            }
        }

        runCatching {
            startKoin {
                modules(
                    (
                        getAccountModules() +
                            getAddressModules() +
                            getCartModules() +
                            getCheckoutModules() +
                            getHomeModules() +
                            getCategoryModules() +
                            getOrderListModules() +
                            getProductListModules() +
                            getProductDetailModules() +
                            getReviewModules() +
                            getApiModule() +
                            setOf(iosLocalModule, iosAppModule, networkConfigModule)
                        ).toList(),
                )
            }
        }.onFailure { throwable ->
            println("IosKoinBridge initKoin failed: ${throwable.message}")
            throwable.printStackTrace()
        }
    }

    fun initKoinDefault() {
        initKoin(
            siteBrand = "SITE_A",
            baseUrl = "https://qeshminora.com/",
            consumerKey = "fallback_key",
            consumerSecret = "fallback_secret",
            paymentReturnScheme = "solutioniuma",
        )
    }
}
