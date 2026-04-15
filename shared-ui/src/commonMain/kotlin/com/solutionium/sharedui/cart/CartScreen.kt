package com.solutionium.sharedui.cart

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.ChangeType
import com.solutionium.shared.data.model.ValidationInfo
import com.solutionium.shared.viewmodel.CartScreenUiState
import com.solutionium.shared.viewmodel.CartViewModel
import com.solutionium.sharedui.common.component.CartItemCard
import com.solutionium.sharedui.common.component.InfoBox
import com.solutionium.sharedui.common.component.PriceView2
import com.solutionium.sharedui.common.component.platformPrimaryButtonShape
import com.solutionium.sharedui.common.component.platformUsesCupertinoChrome
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
import com.solutionium.sharedui.resources.full_pay
import com.solutionium.sharedui.resources.installment_pay
import com.solutionium.sharedui.resources.proceed_to_checkout
import com.solutionium.sharedui.resources.review_the_changes_before_proceeding
import com.solutionium.sharedui.resources.validating_cart_items
import org.jetbrains.compose.resources.stringResource

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onCheckoutClick: () -> Unit,
    onProductClick: (id: Int) -> Unit,
    onRequireAuth: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(state.showLoginPrompt) {
        if (state.showLoginPrompt) {
            viewModel.dismissLoginPrompt()
            onRequireAuth()
        }
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
        Spacer(Modifier.height(30.dp))
        if (state.cartItems.isNotEmpty()) {
            ValidationSummary(
                summaryKey = state.validationSummaryKey,
                summaryCount = state.validationSummaryCount,
                error = state.validationError,
            )
        }

        Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
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
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                    ) {
                        Text(
                            text = stringResource(Res.string.empty_cart),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
                            showInstallmentPrice = state.installmentPriceEnabled,
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
                showInstallmentPrice = state.installmentPriceEnabled,
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
    showInstallmentPrice: Boolean = false,
    onConfirmValidation: () -> Unit,
    onCheckoutClick: () -> Unit,
) {
    val platformBottomPadding = if (platformUsesCupertinoChrome()) 80.dp else 0.dp

    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            if (hasAttentionItems) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = stringResource(Res.string.review_the_changes_before_proceeding),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    if (showInstallmentPrice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(Res.string.installment_pay),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = " x 4 ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            PriceView2(totalPrice / 4, false, null, magnifier = 1.5)
                        }
                        discountedPrice(totalPrice)?.let {
                            Row {
                                Text(
                                    text = stringResource(Res.string.full_pay),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                PriceView2(it, false, null, magnifier = 1.0)
                            }
                        }
                    } else {
                        PriceView2(totalPrice, false, null, magnifier = 1.2)
                    }
                }

                Button(
                    onClick = if (hasAttentionItems) onConfirmValidation else onCheckoutClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
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
            Spacer(modifier = Modifier.height(platformBottomPadding))
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
