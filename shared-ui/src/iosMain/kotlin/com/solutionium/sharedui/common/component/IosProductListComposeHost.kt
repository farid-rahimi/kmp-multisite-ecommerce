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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import platform.UIKit.UIViewController

class IosProductListComposeHost(
    private val initialTabIndex: Int = 0,
    private val showBottomBar: Boolean = true,
    private val lockTabToInitial: Boolean = false,
    private val onCartCountChanged: ((Int) -> Unit)? = null,
    private val onBottomBarVisibilityChanged: ((Int) -> Unit)? = null,
) {
    private val paymentReturnEvent = MutableStateFlow(PaymentReturnEvent())

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
        val paymentReturn by paymentReturnEvent.collectAsState()

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            WooTheme(brand = IosRuntimeConfig.brand, languageCode = currentLanguage) {
                SharedShopRoot(
                    paymentReturnStatus = paymentReturn.status,
                    paymentReturnOrderId = paymentReturn.orderId,
                    onPaymentReturnConsumed = {
                        val consumedEventId = paymentReturn.eventId
                        if (consumedEventId != 0L) {
                            paymentReturnEvent.update { current ->
                                if (current.eventId == consumedEventId) PaymentReturnEvent() else current
                            }
                        }
                    },
                    onCartCountChanged = onCartCountChanged,
                    onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                    initialTabIndex = initialTabIndex,
                    showBottomBar = showBottomBar,
                    lockTabToInitial = lockTabToInitial,
                )
            }
        }
    }

    fun viewController(): UIViewController = controller

    fun onPaymentReturn(status: String, orderId: Int, eventId: Long) {
        if (status.isBlank() || eventId <= 0L) return
        paymentReturnEvent.value = PaymentReturnEvent(
            status = status.trim().lowercase(),
            orderId = orderId.takeIf { it > 0 },
            eventId = eventId,
        )
    }
}

private data class PaymentReturnEvent(
    val status: String? = null,
    val orderId: Int? = null,
    val eventId: Long = 0L,
)

private fun isRtlLanguage(languageCode: String): Boolean {
    return languageCode == "fa" || languageCode == "ar"
}

private fun defaultLanguageForBrand(brand: WooBrand): String {
    return "en"
}
