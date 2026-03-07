package com.solutionium.sharedui.common.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.ComposeUIViewController
import com.solutionium.shared.data.local.AppPreferences
import com.solutionium.sharedui.bootstrap.IosRuntimeConfig
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.designsystem.theme.WooTheme
import com.solutionium.sharedui.navigation.SharedShopRoot
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

class IosProductListComposeHost {
    private val controller = ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        },
    ) {
        val appPreferences = koinInject<AppPreferences>()
        val selectedLanguage by appPreferences
            .language()
            .collectAsState(initial = appPreferences.getLanguage() ?: defaultLanguageForBrand(IosRuntimeConfig.brand))
        val currentLanguage = selectedLanguage ?: defaultLanguageForBrand(IosRuntimeConfig.brand)
        val layoutDirection = if (isRtlLanguage(currentLanguage)) LayoutDirection.Rtl else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            WooTheme(brand = IosRuntimeConfig.brand) {
                SharedShopRoot()
            }
        }
    }

    fun viewController(): UIViewController = controller
}

private fun isRtlLanguage(languageCode: String): Boolean {
    return languageCode == "fa" || languageCode == "ar"
}

private fun defaultLanguageForBrand(brand: WooBrand): String {
    return if (brand == WooBrand.SiteB) "ar" else "fa"
}
