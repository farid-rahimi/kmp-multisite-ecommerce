package com.solutionium.sharedui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.sharedui.common.LoginPromptDialog
import com.solutionium.sharedui.common.component.CartItemCard
import com.solutionium.sharedui.common.component.InfoBox
import com.solutionium.sharedui.common.component.PriceView2
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.all_items_updated_msg
import com.solutionium.sharedui.resources.cart_updated_items_title
import com.solutionium.sharedui.resources.cart_validation_multiple_issues
import com.solutionium.sharedui.resources.cart_validation_network_error
import com.solutionium.sharedui.resources.cart_validation_out_of_stock_short
import com.solutionium.sharedui.resources.cart_validation_price_changed
import com.solutionium.sharedui.resources.cart_validation_regular_price_changed
import com.solutionium.sharedui.resources.cart_validation_stock_changed
import com.solutionium.sharedui.resources.confirm_updates
import com.solutionium.sharedui.resources.empty_cart
import com.solutionium.sharedui.resources.proceed_to_checkout
import com.solutionium.sharedui.resources.review_the_changes_before_proceeding
import com.solutionium.sharedui.resources.validating_cart_items
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.ChangeType
import com.solutionium.shared.data.model.ValidationInfo
import com.solutionium.shared.viewmodel.CartScreenUiState
import com.solutionium.shared.viewmodel.CartViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onCheckoutClick: () -> Unit,
    onProductClick: (id: Int) -> Unit,
    onNavigateToAccount: () -> Unit,
) {
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    val state by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    if (state.showLoginPrompt) {
        LoginPromptDialog(
            onDismiss = viewModel::dismissLoginPrompt,
            onConfirm = {
                viewModel.dismissLoginPrompt()
                onNavigateToAccount()
            },
        )
    }

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        CartScreenContent(
            state = state,
            onCheckoutClick = {
                viewModel.onCheckoutClick(onNavigateToCheckout = onCheckoutClick)
            },
            onProductClick = onProductClick,
            onRemove = { viewModel.removeItem(it) },
            onIncreaseQuantity = { viewModel.increaseQuantity(it) },
            onDecreaseQuantity = { viewModel.decreaseQuantity(it) },
            onConfirmValidation = { viewModel.confirmCartValidation() },
            infoMapper = { validationInfo ->
                validationInfoMessage(validationInfo)
            },
        )
    }
}

@Composable
fun CartScreenContent(
    state: CartScreenUiState,
    onProductClick: (id: Int) -> Unit,
    onCheckoutClick: () -> Unit,
    onRemove: (CartItem) -> Unit,
    onIncreaseQuantity: (CartItem) -> Unit,
    onDecreaseQuantity: (CartItem) -> Unit,
    onConfirmValidation: () -> Unit,
    infoMapper: @Composable (ValidationInfo?) -> String?,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.cartItems.isNotEmpty()) {
            ValidationSummary(
                summaryKey = state.validationSummaryKey,
                summaryCount = state.validationSummaryCount,
                error = state.validationError,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (state.isPerformingValidation) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.validating_cart_items),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else if (state.cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.empty_cart),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(
                        state.cartItems,
                        key = { intArrayOf(it.productId, it.variationId) },
                    ) { item ->
                        CartItemCard(
                            cartItem = item,
                            validationMessage = infoMapper(item.validationInfo),
                            onProductClick = { onProductClick(item.productId) },
                            discountedPrice = state::discountedPrice,
                            onRemove = { onRemove(item) },
                            onIncreaseQuantity = { onIncreaseQuantity(item) },
                            onDecreaseQuantity = { onDecreaseQuantity(item) },
                        )
                    }
                }
            }
        }

        if (!state.isPerformingValidation && state.cartItems.isNotEmpty()) {
            CartBottomBar(
                totalPrice = state.totalPrice,
                hasAttentionItems = state.hasAttentionItems,
                discountedPrice = state::discountedPrice,
                onConfirmValidation = onConfirmValidation,
                onCheckoutClick = onCheckoutClick,
            )
        }
    }
}

@Composable
fun CartBottomBar(
    modifier: Modifier = Modifier,
    totalPrice: Double,
    hasAttentionItems: Boolean,
    discountedPrice: (Double?) -> Double? = { null },
    onConfirmValidation: () -> Unit,
    onCheckoutClick: () -> Unit,
) {
    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (hasAttentionItems) {
                Text(
                    stringResource(Res.string.review_the_changes_before_proceeding),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "قسطی",
                            fontSize = 11.sp,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(" x 4 ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        PriceView2(totalPrice / 4, false, null, magnifier = 1.5)
                    }
                    discountedPrice(totalPrice)?.let {
                        Row {
                            Text(
                                text = "نقدی",
                                fontSize = 11.sp,
                                color = Color.Gray,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            PriceView2(it, false, null, magnifier = 1.0)
                        }
                    }
                }

                Button(
                    onClick = if (hasAttentionItems) onConfirmValidation else onCheckoutClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 140.dp),
                ) {
                    Text(
                        if (hasAttentionItems) stringResource(Res.string.confirm_updates)
                        else stringResource(Res.string.proceed_to_checkout),
                    )
                }
            }
        }
    }
}

@Composable
fun ValidationSummary(summaryKey: String?, summaryCount: Int?, error: String?) {
    val message = when (summaryKey) {
        "cart_updated_items_title" -> stringResource(Res.string.cart_updated_items_title, (summaryCount ?: 0).toString())
        "all_items_updated_msg" -> stringResource(Res.string.all_items_updated_msg)
        else -> null
    }

    if (message != null) {
        val isError = error != null
        InfoBox(
            message = message,
            icon = if (isError) Icons.Default.Warning else Icons.Outlined.Info,
            color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun validationInfoMessage(validationInfo: ValidationInfo?): String? {
    if (validationInfo == null) return null

    return when (validationInfo.type) {
        ChangeType.PRICE_CHANGED -> stringResource(
            Res.string.cart_validation_price_changed,
            validationInfo.values.getOrNull(0) ?: "",
            validationInfo.values.getOrNull(1) ?: "",
        )

        ChangeType.REGULAR_PRICE_CHANGED -> stringResource(
            Res.string.cart_validation_regular_price_changed,
            validationInfo.values.getOrNull(0) ?: "",
            validationInfo.values.getOrNull(1) ?: "",
        )

        ChangeType.STOCK_CHANGED -> stringResource(
            Res.string.cart_validation_stock_changed,
            validationInfo.values.getOrNull(0) ?: "",
            validationInfo.values.getOrNull(1) ?: "0",
        )

        ChangeType.OUT_OF_STOCK,
        ChangeType.NOT_AVAILABLE,
        -> stringResource(Res.string.cart_validation_out_of_stock_short)

        ChangeType.MULTIPLE_ISSUES -> stringResource(Res.string.cart_validation_multiple_issues)
        ChangeType.NETWORK_ERROR -> stringResource(Res.string.cart_validation_network_error)
    }
}
