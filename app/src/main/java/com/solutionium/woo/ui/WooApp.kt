package com.solutionium.woo.ui

import androidx.compose.runtime.Composable
import com.solutionium.sharedui.navigation.SharedShopRoot

@Composable
fun WooApp(
    paymentReturnStatus: String? = null,
    paymentReturnOrderId: Int? = null,
    onPaymentReturnConsumed: () -> Unit = {},
) {
    SharedShopRoot(
        paymentReturnStatus = paymentReturnStatus,
        paymentReturnOrderId = paymentReturnOrderId,
        onPaymentReturnConsumed = onPaymentReturnConsumed,
    )
}
