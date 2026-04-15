package com.solutionium.woo.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.navigation.SharedShopRoot

@Composable
fun WooApp(
    paymentReturnStatus: String? = null,
    paymentReturnOrderId: Int? = null,
    onPaymentReturnConsumed: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            //.padding(bottom = 56.dp)
    ) {
        SharedShopRoot(
            paymentReturnStatus = paymentReturnStatus,
            paymentReturnOrderId = paymentReturnOrderId,
            onPaymentReturnConsumed = onPaymentReturnConsumed,
        )
    }
}
