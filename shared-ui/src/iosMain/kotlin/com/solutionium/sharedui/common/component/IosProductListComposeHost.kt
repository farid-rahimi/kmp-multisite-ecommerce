package com.solutionium.sharedui.common.component

import androidx.compose.ui.window.ComposeUIViewController
import com.solutionium.sharedui.bootstrap.IosKoinBridge
import com.solutionium.sharedui.bootstrap.IosRuntimeConfig
import com.solutionium.sharedui.designsystem.theme.WooTheme
import com.solutionium.sharedui.navigation.SharedShopRoot
import platform.UIKit.UIViewController

class IosProductListComposeHost {
    init {
        IosKoinBridge().initKoinDefault()
    }

    private val controller = ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        },
    ) {
        WooTheme(brand = IosRuntimeConfig.brand) {
            SharedShopRoot()
        }
    }

    fun viewController(): UIViewController = controller
}
