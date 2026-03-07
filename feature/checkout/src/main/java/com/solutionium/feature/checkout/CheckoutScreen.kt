package com.solutionium.feature.checkout

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solutionium.sharedui.common.component.CartItemCard
import com.solutionium.sharedui.common.component.CenteredCircularProgress
import com.solutionium.sharedui.common.component.FormattedPriceV2
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.ShippingMethod
import com.solutionium.shared.domain.checkout.CouponError
import com.solutionium.shared.domain.checkout.CouponErrorType
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue


@Composable
fun CheckoutScreen(
    onBack: () -> Unit,    // Assuming this is handled by a TopAppBar elsewhere or system back
    onAddEditAddressClick: (addressId: Int?) -> Unit,
    onContinueShopping: () -> Unit,
    paymentReturnStatus: String?, // "success" or "failed" from the payment gateway redirect
    paymentReturnOrderId: Int?, // Order ID returned from the payment gateway
    viewModel: CheckoutViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(paymentReturnStatus) {
        if (paymentReturnStatus != null) {
            // A deep link was used to open the screen.
            // Trigger the final verification.
            viewModel.verifyOrderStatusAfterPayment()
        }
    }

    // This effect launches the browser when we get a payment URL
    LaunchedEffect(state.placeOrderStatus) {
        val status = state.placeOrderStatus
        if (status is PlaceOrderStatus.AwaitingPayment) {
            val intent = Intent(Intent.ACTION_VIEW, status.paymentUrl.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle case where no browser is available
                viewModel.resetOrderStatus() // Or go to a specific error state
            }
        }
    }

    // This effect listens for the app coming back to the foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // App has resumed, check if we were waiting for payment.
                if (state.placeOrderStatus is PlaceOrderStatus.AwaitingPayment) {
                    viewModel.verifyOrderStatusAfterPayment()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    when (val status = state.placeOrderStatus) {
        is PlaceOrderStatus.Idle -> {
            CheckoutFormScreen(
                state = state,
                viewModel = viewModel,
                onAddEditAddressClick = onAddEditAddressClick,
            )
        }

        is PlaceOrderStatus.InProgress -> {
            PlacingOrderScreen()
        }
        // NEW SCREEN to show while user is in the browser
        is PlaceOrderStatus.AwaitingPayment -> {
            AwaitingPaymentScreen(
                onVerifyPaymentClicked = { viewModel.verifyOrderStatusAfterPayment() }
            )
        }

        is PlaceOrderStatus.Success -> {
            OrderSuccessfulScreen(
                orderId = status.orderId,
                orderTotal = status.orderTotal,
                onContinueShopping = onContinueShopping
            )
        }

        is PlaceOrderStatus.BACSSuccess -> {
            OrderSuccessfulBACSScreen(
                orderId = status.orderId,
                orderTotal = status.orderTotal,
                bacsDetails = status.bacsDetails,
                onContinueShopping = onContinueShopping
            )
        }

        is PlaceOrderStatus.Failed -> {
            OrderFailedScreen(
                errorMessage = status.errorMessage,
                canRetry = status.canRetry,
                onTryAgain = {
                    if (status.canRetry) {
                        // This is now for retrying the *status check*, not placing the order again
                        viewModel.verifyOrderStatusAfterPayment()
                    } else {
                        // If cannot retry, just reset the flow
                        viewModel.resetOrderStatus()
                    }
                },
                onGoBackToCart = {
                    viewModel.resetOrderStatus()
                    onBack()
                }
            )
        }


    }

}


@Composable
fun CheckoutFormScreen(
    state: CheckoutUiState,
    viewModel: CheckoutViewModel,
    onAddEditAddressClick: (addressId: Int?) -> Unit,
) {

    var showAllItems by remember { mutableStateOf(false) }
    var isSummaryExpanded by rememberSaveable { mutableStateOf(false) }

    // --- AUTOMATIC EXPANSION LOGIC ---
    // This will run whenever total, shippingCost, or totalDiscount changes
    LaunchedEffect(state.total, state.shippingCost, state.totalDiscount) {
        // Expand the summary card to show the user the price has changed
        isSummaryExpanded = true
    }

    val lazyListState = rememberLazyListState()
    LaunchedEffect(lazyListState) {
        val scrollThreshold = 30
        var previousScrollOffset = 0

        snapshotFlow {
            Pair(
                lazyListState.firstVisibleItemIndex * 1000 + lazyListState.firstVisibleItemScrollOffset,
                lazyListState.canScrollForward
            )
        }.distinctUntilChanged().collect { (currentScrollOffset, canScrollForward) ->
            if (!canScrollForward) {
                isSummaryExpanded = true
            } else {
                val scrollDelta = currentScrollOffset - previousScrollOffset
                if (scrollDelta > scrollThreshold) {
                    isSummaryExpanded = false
                }
            }
            previousScrollOffset = currentScrollOffset
        }
    }
//    val isScrolledToEnd = remember {
//        derivedStateOf {
//            val layoutInfo = lazyListState.layoutInfo
//            if (layoutInfo.totalItemsCount == 0) return@derivedStateOf false
//            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
//            lastVisibleItem.index + 1 == layoutInfo.totalItemsCount
//        }
//    }
//
//    LaunchedEffect(lazyListState) {
//        var previousOffset = 0
//        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
//            .collect { currentOffset ->
//                if (previousOffset < currentOffset) {
//                    // User is scrolling DOWN
//                    isSummaryExpanded = false // Collapse the summary
//                } else if (previousOffset > currentOffset) {
//                    // User is scrolling UP
//                    // DO NOTHING, as per the new requirement.
//                }
//                previousOffset = currentOffset
//            }
//    }
//
//    LaunchedEffect(isScrolledToEnd.value) {
//        if (isScrolledToEnd.value) {
//            isSummaryExpanded = true
//        }
//    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { // Sticky button goes into the bottomBar slot
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                //.background(Color.White),
                shadowElevation = 24.dp, // Gives a nice lift from the content
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Column(
                    // Optional: for background and elevation
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                    //shadowElevation = 8.dp,
                    //color = MaterialTheme.colorScheme.surface
                ) {
                    OrderSummaryCard(
                        state = state,
                        isInstallmentVisible = state.isInstallment,
                        isExpanded = isSummaryExpanded,
                        onToggleExpand = { isSummaryExpanded = !isSummaryExpanded }
                    ) // Encapsulate summary in a card
                    Button(
                        onClick = viewModel::confirmOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = state.selectedShippingMethod != null &&
                                state.selectedPaymentGateway != null &&
                                state.shippingAddress != null &&
                                state.placeOrderStatus == PlaceOrderStatus.Idle // Use new state
                    ) {
                        Text(
                            stringResource(R.string.confirm_order_button),
//                            state.selectedPaymentGateway?.title
//                                ?: stringResource(R.string.confirm_order_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { padding -> // Content of the Scaffold
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = 0.dp)
                .padding(horizontal = 16.dp), // Additional horizontal padding for content
            verticalArrangement = Arrangement.spacedBy(24.dp),
            //horizontalAlignment = CenterHorizontally,
        ) {


            item { Spacer(modifier = Modifier.height(8.dp)) }
//            item {
//                Spacer(modifier = Modifier.height(8.dp))
//                SectionTitle("Review Your Order")
//            }

            if (state.error != null) {
                item {
                    ErrorDisplay(errorMessage = stringResource( state.error.messageResId) )
                }
            }


            // --- Cart Items Section ---
            item {
                ReviewOrderSection(
                    cartItems = state.cartItems,
                    showAllItems = showAllItems,
                    onToggleShowAll = { showAllItems = !showAllItems }
                )
            }

            item {
                CouponSection(
                    appliedCoupons = state.appliedCoupons,
                    isLoading = state.isApplyingCoupon,
                    //error = state.couponError,
                    onApplyCoupon = viewModel::applyCoupon,
                    onRemoveCoupon = viewModel::removeCoupon,
                    hasError = state.couponError != null,
                    errorText = {
                        if (state.couponError != null)
                            MapCouponErrorToText(state.couponError)
                    }
                )
            }

            item {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }


            item {
                AddressSection(
                    selectedAddress = state.shippingAddress,
                    availableAddresses = state.availableAddresses,
                    isAddressListExpanded = state.isAddressListExpanded,
                    isLoading = state.isLoadingAddress,
                    onToggleAddressList = viewModel::onToggleAddressList,
                    onAddressSelected = viewModel::onAddressSelected,
                    onAddAddress = { onAddEditAddressClick(null) },
                    onEditAddress = { addressId -> onAddEditAddressClick(addressId) }
                )
            }


            item {
                ShippingMethodSection(
                    methods =
                        if (state.freeShippingByCouponIsActive) listOf(state.freeShippingMethodByCoupon!!)
                        else if (state.freeShippingByMinOrderIsActive) listOf(state.freeShippingMethodByMinOrder!!)
                        else state.shippingMethods,
                    selectedMethod = state.selectedShippingMethod,
                    onMethodSelected = viewModel::selectShipping,
                    subTotal = state.subTotal,
                    isLoading = state.isLoadingShippingMethods
                )
            }

            // --- Wallet Payment Section ---
            item {
                WalletPaymentSection(
                    walletBalance = state.userWallet?.balance ?: 0.0,
                    useWallet = state.useWallet,
                    onUseWalletChange = viewModel::onUseWalletChange,
                    orderTotal = state.paidByWallet
                )
            }

            item {
                // Only show other payment methods if the wallet doesn't cover the full total,
                // or if the user hasn't opted to use the wallet.
                val showPaymentGateways = state.total > 0 || !state.useWallet

                if (showPaymentGateways) {
                    // Animate the appearance/disappearance of this section
                    AnimatedVisibility(
                        visible = state.total > 0,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            PaymentMethodSection(
                                gateways = state.paymentGateways,
                                selectedGateway = state.selectedPaymentGateway,
                                onGatewaySelected = viewModel::selectPaymentGateway,
                                hasDiscount = { viewModel.paymentDiscountAmount(it) },
                                isLoading = state.isLoadingPaymentGateways,
                                // Let the section know if it's for a partial payment
                                showAsRemainingPayment = state.useWallet
                            )
                        }
                    }
                }
            }
            // --- Payment Method Section ---
//            item {
//                PaymentMethodSection(
//                    gateways = state.paymentGateways,
//                    selectedGateway = state.selectedPaymentGateway,
//                    onGatewaySelected = viewModel::selectPaymentGateway,
//                    hasDiscount = { viewModel.paymentMethodHasDiscount(it) * state.subTotal / 100 },
//                    isLoading = state.isLoadingPaymentGateways
//                )
//            }

            item {
                Spacer(modifier = Modifier.height(300.dp))
            }

        }
    }
}


// In CheckoutScreen.kt
@Composable
fun WalletPaymentSection(
    walletBalance: Double,
    useWallet: Boolean,
    onUseWalletChange: (Boolean) -> Unit,
    orderTotal: Double
) {
    Column {
        SectionTitle(stringResource(R.string.wallet_title))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { if (walletBalance > 0) onUseWalletChange(!useWallet) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.use_wallet_balance),
                    style = MaterialTheme.typography.bodyLarge
                )
                FormattedPriceV2(
                    amount = walletBalance.toLong(),
                    mainStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Switch(
                checked = useWallet,
                onCheckedChange = onUseWalletChange,
                enabled = walletBalance > 0
            )
        }
        if (useWallet) {
            val amountToUse = minOf(walletBalance, orderTotal)
            Row {
                Text(
                    text = stringResource(
                        R.string.wallet_deduction_message
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
                FormattedPriceV2(amountToUse.toLong())
            }

        }
    }
}



@Composable
fun PaymentMethodSection(
    gateways: List<PaymentGateway>,
    selectedGateway: PaymentGateway?,
    onGatewaySelected: (PaymentGateway) -> Unit,
    hasDiscount: (methodId: String) -> Double,
    isLoading: Boolean,
    showAsRemainingPayment: Boolean // New parameter
) {

    val title = if (showAsRemainingPayment) {
        stringResource(R.string.pay_remaining_amount_title)
    } else {
        stringResource(R.string.payment_methods_section_title)
    }

    SectionTitle(title)
    Spacer(modifier = Modifier.height(8.dp))
    if (isLoading) {
        CenteredCircularProgress()
    } else if (gateways.isEmpty()) {
        Text(
            stringResource(R.string.no_payment_methods_available),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        gateways.filter { it.id != "wallet" }.forEach { gateway ->

            SelectableOptionRow(
                text = gateway.title,
                isSelected = gateway == selectedGateway,
                secondaryText = {
                    val discount = hasDiscount(gateway.id)
                    if (discount > 0) {

                        Row {
                            FormattedPriceV2(
                                discount.toLong(),
                                currency = "",
                                mainStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(
                                        0xFF0A7F52
                                    )
                                )
                            )
                            Text(stringResource(R.string.discount), color = Color(0xFF0A7F52))
                        }
//                        Text(
//                            stringResource(R.string.discount, "%${discount.toInt()}"),
//                            color = Color(0xFF0A7F52),
//                        ) // Green color for free shipping
                    } else {
                        Text(gateway.description, style = MaterialTheme.typography.labelSmall)
                    }
                }, // No secondary text for payment gateways currently
                onClick = { onGatewaySelected(gateway) }
            )
        }
    }
}

@Composable
fun ShippingMethodSection(
    methods: List<ShippingMethod>,
    selectedMethod: ShippingMethod?,
    onMethodSelected: (ShippingMethod) -> Unit,
    subTotal: Double,
    isLoading: Boolean
) {
    SectionTitle(stringResource(R.string.shipping_methods_section_title))
    Spacer(modifier = Modifier.height(8.dp))
    if (isLoading) {
        CenteredCircularProgress()
    } else if (methods.isEmpty()) {
        Text(
            stringResource(R.string.no_shipping_methods_available),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        methods.forEach { method ->
            val shippingCost = method.calculateShippingCost(subTotal).toLong()
            SelectableOptionRow(
                text = method.title,
                // You might want to display method.cost here too
                // e.g., "${method.title} (${FormattedPriceV2(method.cost, "USD")})"
                secondaryText = {
                    if (shippingCost > 0)
                        FormattedPriceV2(
                            amount = shippingCost, // Assuming cost is in dollars, convert to cents
                            currency = "ت" // Replace with your actual currency
                        )
//                    else
//                        Text(
//                            stringResource(R.string.free_shipping),
//                            color = Color(0xFF0A7F52),
//                        ) // Green color for free shipping
                }, // FormattedPriceV2 should ideally return AnnotatedString
                isSelected = method == selectedMethod,
                onClick = { onMethodSelected(method) }
            )

        }
    }
}


@Composable
fun ReviewOrderSection(
    cartItems: List<CartItem>,
    showAllItems: Boolean,
    onToggleShowAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle(stringResource(R.string.review_order_section_title))

        val itemsToShow = if (showAllItems) cartItems else cartItems.take(3)
        itemsToShow.forEach { cartItem ->
            CartItemCard(cartItem = cartItem) // Assuming this has its own consistent styling
        }

        val remainingItemCount = cartItems.size - itemsToShow.size
        if (remainingItemCount > 0) {
            TextButton(onClick = onToggleShowAll, modifier = Modifier.align(CenterHorizontally)) {
                Text(
                    stringResource(
                        R.string.more_item,
                        remainingItemCount
                    )
                )
            }
        } else if (cartItems.size > 3) {
            TextButton(onClick = onToggleShowAll, modifier = Modifier.align(CenterHorizontally)) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.show_less)
                )
                Text(stringResource(R.string.show_less))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(bottom = 4.dp) // Add vertical padding to section titles
    )
}

@Composable
fun SelectableOptionRow(
    text: String,
    secondaryText: @Composable () -> Unit, // For shipping cost, for example
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(
            alpha = 0.2f
        )
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.3f
        )

    Surface(
        // Use Surface for better click feedback and elevation/border
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Rounded corners for each option
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        // tonalElevation = if (isSelected) 2.dp else 0.dp // Subtle elevation for selected
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Consistent padding
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface
                )
                secondaryText() // Display secondary text if provided

            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp)) // Space between options
}

@Composable
fun OrderSummaryCard(
    modifier: Modifier = Modifier,
    state: CheckoutUiState,
    isExpanded: Boolean, // <-- Add this
    isInstallmentVisible: Boolean = false,
    onToggleExpand: () -> Unit // <-- Add this
) {
    //var isExpanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .animateContentSize() // Animate the size change on expand/collapse
            //verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple effect for the whole row
                        onClick = onToggleExpand
                    )
                    .padding(vertical = 2.dp), // Consistent vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {


                // The "Total" label is now part of the header
                Text(
                    text = stringResource(R.string.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )



                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse summary" else "Expand summary",
                    modifier = Modifier.padding(2.dp),
                )
                Spacer(
                    modifier = Modifier.weight(1f),
                )

                // The total amount is also always visible
                FormattedPriceV2(
                    amount = state.total.toLong(),
                    mainStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                // The expand/collapse icon

            }

            // Due Today Row for installment payment.
            if (isInstallmentVisible)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // No ripple effect for the whole row
                            onClick = onToggleExpand
                        )
                        .padding(vertical = 0.dp), // Consistent vertical padding
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    // The "Total" label is now part of the header
                    Text(
                        text = stringResource(R.string.due_today),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Spacer(
                        modifier = Modifier.weight(1f),
                    )

                    // The total amount is also always visible
                    FormattedPriceV2(
                        amount = state.total.toLong() / 4,
                        mainStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    // The expand/collapse icon

                }



            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp) // Padding at the bottom when expanded
                ) {

                    SummaryRow(
                        stringResource(R.string.subtotal),
                        valueComposable = { FormattedPriceV2(state.subTotal.toLong()) }) // Pass Long or adapt
                    SummaryRow(
                        label = stringResource(R.string.shipping),
                        valueComposable = {
                            if (state.shippingCost > 0) FormattedPriceV2(state.shippingCost.toLong())
                            else Text(
                                stringResource(R.string.free),
                                color = Color(0xFF0A7F52)
                            ) // Green color for free shipping
                        }
                    )
                    if (state.fees.isNotEmpty()) {
                        state.fees.forEach { (feeName, feeAmount) ->
                            SummaryRow(
                                label = if (feeName == FeeKeys.PAYMENT_DISCOUNT ) stringResource(R.string.payment_discount) else feeName,
                                valueComposable = {
                                    FormattedPriceV2(
                                        //amount = feeAmount.toLong().times(-1L), // Assuming feeAmount is in smallest unit
                                        amount = feeAmount.toLong().absoluteValue,
                                        mainStyle = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (feeAmount < 0) Color(0xFF0A7F52) else Color.DarkGray
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (state.useWallet) {
                        SummaryRow(stringResource(R.string.paid_by_wallet), valueComposable = {
                            FormattedPriceV2(
                                state.paidByWallet.toLong(),
                                mainStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A7F52)
                                )
                            )
                        })
                    }

                    if (state.totalDiscount > 0) {
                        SummaryRow(stringResource(R.string.discounts), valueComposable = {
                            FormattedPriceV2(
                                state.totalDiscount.toLong(),
                                mainStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A7F52)
                                )
                            )
                        })
                    }
//                    HorizontalDivider(
//                        modifier = Modifier.padding(vertical = 4.dp),
//                        thickness = 0.5.dp,
//                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
//                    )
                }

            }
//            SummaryRow(
//                label = stringResource(R.string.total),
//                valueComposable = {
//                    FormattedPriceV2(
//                        amount = state.total.toLong(), // Assuming state.total is Long in smallest unit
//                        mainStyle = TextStyle(
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        ),
//
//                    )
//                },
//                labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
//            )
        }
    }
}


@Composable
fun SummaryRow(
    label: String,
    value: Any,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
) {
    // Overload or generic way to display value, assuming FormattedPriceV2 returns AnnotatedString
    // This simple version assumes 'value' is an AnnotatedString from FormattedPriceV2 or similar
    Row {
        Text(label, style = labelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        if (value is AnnotatedString) { // Adapt if FormattedPriceV2 returns something else
            Text(value, style = valueStyle, color = MaterialTheme.colorScheme.onSurface)
        } else {
            Text(value.toString(), style = valueStyle, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// For cases where value is a composable itself (like the styled total)
@Composable
fun SummaryRow(
    label: String,
    valueComposable: @Composable () -> Unit,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = labelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        valueComposable()
    }
}


@Composable
fun ErrorDisplay(errorMessage: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun AddressDropdown(
    addresses: List<Address>,
    onAddressSelected: (Address) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = true, // Controlled by the caller
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(0.9f) // Adjust width as needed
    ) {
        addresses.forEach { address ->
            DropdownMenuItem(
                text = {
                    Text(
                        address.toDisplayString(),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { onAddressSelected(address) }
            )
        }
    }
}

// Helper extension function for displaying the address neatly
fun Address.toDisplayString(): String {
    // This creates a multi-line, detailed string representation of the address.
    return listOfNotNull(
        title,
        "$firstName $lastName",
        "${state}, $city ${postcode}".trim(),
        address1,
        address2?.takeIf { it.isNotBlank() }, // Only include address2 if it's not empty
        country,
        "$phone"
    ).joinToString("\n")
}


// In CheckoutScreen.kt - Replace your existing AddressSection with this one
@Composable
fun AddressSection(
    selectedAddress: Address?,
    availableAddresses: List<Address>,
    isAddressListExpanded: Boolean,
    isLoading: Boolean,
    onToggleAddressList: () -> Unit,
    onAddressSelected: (Address) -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // --- Section Title with inline "Change" button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                stringResource(R.string.shipping_address_section_title),
                modifier = Modifier.weight(1f)
            )
            if (selectedAddress != null && availableAddresses.size > 1) {
                TextButton(onClick = onToggleAddressList) {
                    Text(selectedAddress.title ?: stringResource(R.string.change))
                    Icon(
                        imageVector = if (isAddressListExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.change)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CenteredCircularProgress(modifier = Modifier.padding(vertical = 24.dp))
        } else if (selectedAddress != null) {
            // --- Display Selected Address ---
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                    //.height(120.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                // Dropdown needs to be anchored to the card
                Box {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedAddress.firstName} ${selectedAddress.lastName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onEditAddress(selectedAddress.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Address")
                            }
                        }
                        Text(
                            text = "${selectedAddress.state}, ${selectedAddress.city}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "${selectedAddress.address1}, ${selectedAddress.address2}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(
                                    R.string.postal_code,
                                    selectedAddress.postcode
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.phone, selectedAddress.phone ?: ""),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        // Use the updated toDisplayString() for full details
//                        Text(
//                            text = selectedAddress.toDisplayString(),
//                            style = MaterialTheme.typography.bodyLarge,
//                            lineHeight = 22.sp // Improve readability for multi-line text
//                        )
                    }

                    // The Dropdown Menu for address selection
                    if (isAddressListExpanded) {
                        AddressDropdown(
                            addresses = availableAddresses.filter { it.id != selectedAddress.id }, // Show other addresses
                            onAddressSelected = onAddressSelected,
                            onDismissRequest = onToggleAddressList
                        )
                    }


                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- "Add New Address" Button - Always Visible ---
        TextButton(
            onClick = onAddAddress,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_new_address))
        }
    }
}


@Composable
fun AddressDisplayCard(
    address: Address,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Keep it subtle
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${address.firstName} ${address.lastName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = address.address1 + (address.address2?.let { "\n$it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${address.city}, ${address.state} ${address.postcode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Phone: ${address.phone}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MapCouponErrorToText(error: CouponError) {

    when (error.errorType) {
        CouponErrorType.Expired -> Text(stringResource(R.string.coupon_error_expired))
        CouponErrorType.AlreadyApplied -> Text(stringResource(R.string.coupon_error_already_applied))
        CouponErrorType.NotExist -> Text(stringResource(R.string.coupon_error_not_exist))
        CouponErrorType.MinSpend -> Text(
            stringResource(
                R.string.coupon_error_min_spend,
                error.arg ?: ""
            )
        )

        CouponErrorType.MaxSpend -> Text(
            stringResource(
                R.string.coupon_error_max_spend,
                error.arg ?: ""
            )
        )

        CouponErrorType.IndividualUse -> Text(stringResource(R.string.coupon_error_individual_use))
        CouponErrorType.IndividualAlready -> Text(stringResource(R.string.coupon_error_individual_use_already_applied))
        CouponErrorType.UsageLimit -> Text(stringResource(R.string.coupon_error_usage_limit))
        CouponErrorType.Include -> Text(stringResource(R.string.coupon_error_includes))
        CouponErrorType.Exclude -> Text(stringResource(R.string.coupon_error_excludes))
        CouponErrorType.OnSalesLimit -> Text(stringResource(R.string.coupon_error_on_sale_items))
    }

}



