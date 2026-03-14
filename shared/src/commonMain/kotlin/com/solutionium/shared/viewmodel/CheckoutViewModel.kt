package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.Coupon
import com.solutionium.shared.data.model.FeeLine
import com.solutionium.shared.data.model.Metadata
import com.solutionium.shared.data.model.NewOrderData
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.ShippingMethod
import com.solutionium.shared.data.model.getMobileReturnEnabledMeta
import com.solutionium.shared.data.model.getMobileReturnExpiresMeta
import com.solutionium.shared.data.model.getMobileReturnSchemeMeta
import com.solutionium.shared.data.model.getPartialPaymentAmount
import com.solutionium.shared.data.model.getPaymentRedirectUrl
import com.solutionium.shared.data.model.getWalletPartialPaymentMeta
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.domain.cart.ClearCartUseCase
import com.solutionium.shared.domain.cart.ObserveCartUseCase
import com.solutionium.shared.domain.checkout.ApplyCouponUseCase
import com.solutionium.shared.domain.checkout.CreateOrderUseCase
import com.solutionium.shared.domain.checkout.GetOrderStatusUseCase
import com.solutionium.shared.domain.checkout.GetPaymentGatewaysUseCase
import com.solutionium.shared.domain.checkout.GetShippingMethodsUseCase
import com.solutionium.shared.domain.config.ForcedEnabledPaymentUseCase
import com.solutionium.shared.domain.config.GetBACSDetailsUseCase
import com.solutionium.shared.domain.config.PaymentMethodDiscountUseCase
import com.solutionium.shared.domain.config.WalletEnabledUseCase
import com.solutionium.shared.domain.user.GetUserWalletUseCase
import com.solutionium.shared.domain.user.LoadAddressesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val observeCartUseCase: ObserveCartUseCase,
    private val getShippingMethodsUseCase: GetShippingMethodsUseCase,
    private val getForcedEnabledPayment: ForcedEnabledPaymentUseCase,
    private val getPaymentGatewaysUseCase: GetPaymentGatewaysUseCase,
    private val loadAddressesUseCase: LoadAddressesUseCase,
    private val applyCouponUseCase: ApplyCouponUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getOrderStatusUseCase: GetOrderStatusUseCase,
    private val paymentMethodDiscountUseCase: PaymentMethodDiscountUseCase,
    private val getBACSDetails: GetBACSDetailsUseCase,
    private val getUserWalletUseCase: GetUserWalletUseCase,
    private val walletEnabledUseCase: WalletEnabledUseCase,
    private val networkConfigProvider: NetworkConfigProvider,
    private val paymentUnsuccessMessage: (String) -> String = { status ->
        "Payment was not successful. Status: $status"
    },
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state: MutableStateFlow<CheckoutUiState> =
        MutableStateFlow(CheckoutUiState())
    val state = _state.asStateFlow()

    private var verificationJob: Job? = null

    init {
        initCheckout()
    }

    private fun initCheckout() {
        observeCart()
        observeAddress()
        loadShippingMethods()
        loadPaymentGateways()
        loadPaymentDiscount()
        loadWalletFeature()
    }

    private fun loadWalletFeature() {
        scope.launch {
            val enabled = walletEnabledUseCase()
            _state.update { it.copy(walletEnabled = enabled) }
            if (enabled) {
                loadUserWallet()
            } else {
                _state.update {
                    it.copy(
                        userWallet = null,
                        loadingWallet = false,
                        useWallet = false,
                        paidByWallet = 0.0,
                    )
                }
                recalculateTotals()
            }
        }
    }

    private fun loadUserWallet() {
        scope.launch {
            _state.update { it.copy(loadingWallet = true) }
            getUserWalletUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(userWallet = result.data, loadingWallet = false) }
                    }

                    is Result.Failure -> _state.update { it.copy(loadingWallet = false) }
                }
            }
        }
    }

    private fun observeCart() {
        scope.launch {
            observeCartUseCase().collect { items ->
                val subTotalPrice = items.sumOf { it.currentPrice * it.quantity }
                _state.update { it.copy(cartItems = items, subTotal = subTotalPrice) }
                recalculateTotals()
            }
        }
    }

    private fun observeAddress() {
        scope.launch {
            loadAddressesUseCase().collect { addresses ->
                val defaultAddress = addresses.find { address -> address.isDefault }
                _state.update {
                    it.copy(
                        shippingAddress = defaultAddress,
                        availableAddresses = addresses,
                    )
                }
            }
        }
    }



    private fun loadPaymentGateways() =
        scope.launch {
            _state.update { it.copy(isLoadingPaymentGateways = true) }
            val forcedEnabledList = getForcedEnabledPayment()

            getPaymentGatewaysUseCase(forcedEnabledList).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(paymentGateways = result.data) }
                        if (_state.value.selectedPaymentGateway == null && result.data.isNotEmpty()) {
                            selectPaymentGateway(result.data.first())
                        }
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(error = CheckoutError.GeneralLoadingError()) }
                    }
                }
            }.also {
                _state.update { it.copy(isLoadingPaymentGateways = false) }
            }
        }

    private fun loadPaymentDiscount() {
        scope.launch {
            val discounts = paymentMethodDiscountUseCase()
            _state.update { it.copy(paymentMethodDiscounts = discounts) }
            if (_state.value.paymentGateways.isNotEmpty() && discounts.isNotEmpty()) {
                selectPaymentGateway(_state.value.paymentGateways.first())
            }
        }
    }

    private fun loadShippingMethods() =
        scope.launch {
            _state.update { it.copy(isLoadingShippingMethods = true) }

            getShippingMethodsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val freeShippingMethod = result.data.find { it.isFreeShippingByCoupon() }
                        val filteredMethods =
                            result.data.filter { !it.isFreeShippingByCoupon() && !it.isFreeShippingByMinOrder() }
                        val freeShippingByMinOrder =
                            result.data.find { it.isFreeShippingByMinOrder() }

                        _state.update { state ->
                            state.copy(
                                shippingMethods = filteredMethods,
                                freeShippingMethodByCoupon = freeShippingMethod,
                                freeShippingMethodByMinOrder = freeShippingByMinOrder,
                            )
                        }
                        if (_state.value.selectedShippingMethod == null && filteredMethods.isNotEmpty()) {
                            selectShipping(filteredMethods.first())
                        }
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(error = CheckoutError.GeneralLoadingError()) }
                    }
                }
            }.also {
                _state.update { it.copy(isLoadingShippingMethods = false) }
            }
        }

    fun selectShipping(method: ShippingMethod) {
        if (_state.value.selectedShippingMethod == method) return

        val shippingCost = method.calculateShippingCost(_state.value.subTotal)
        _state.update {
            it.copy(
                selectedShippingMethod = method,
                shippingCost = shippingCost,
            )
        }
        recalculateTotals()
    }

    fun selectPaymentGateway(gateway: PaymentGateway) {
        val isInstallment = paymentMethodIsInstallment(gateway.id)
        _state.update {
            it.copy(
                selectedPaymentGateway = gateway,
                isInstallment = isInstallment,
            )
        }
        recalculateTotals()
    }

    fun paymentDiscountAmount(methodId: String): Double {
        val discountPercent = paymentMethodHasDiscount(methodId)
        return (discountPercent / 100.0) * (_state.value.subTotal - _state.value.totalDiscount - _state.value.paidByWallet)
    }

    private fun paymentMethodHasDiscount(methodId: String): Double {
        return _state.value.paymentMethodDiscounts[methodId] ?: 0.0
    }

    private fun paymentMethodIsInstallment(methodId: String): Boolean {
        return listOf("WC_Gateway_SnappPay", "WC_Gateway_TorobPay").contains(methodId)
    }

    fun applyCoupon(code: String) {
        scope.launch {
            _state.update { it.copy(isApplyingCoupon = true, couponError = null) }

            when (
                val result = applyCouponUseCase(
                    code,
                    state.value.appliedCoupons,
                    state.value.cartItems,
                    state.value.subTotal,
                )
            ) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isApplyingCoupon = false,
                            appliedCoupons = (it.appliedCoupons + result.data).distinctBy { c -> c.code },
                        )
                    }

                    if (result.data.freeShipping) {
                        _state.value.freeShippingMethodByCoupon?.let {
                            _state.update { currentState -> currentState.copy(freeShippingByCouponIsActive = true) }
                            selectShipping(it)
                        }
                    }
                    recalculateTotals()
                }

                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            isApplyingCoupon = false,
                            couponError = result.error,
                        )
                    }
                }
            }
        }
    }

    fun removeCoupon(coupon: Coupon) {
        scope.launch {
            val updatedCoupons = state.value.appliedCoupons.filter { it.id != coupon.id }
            _state.update { it.copy(appliedCoupons = updatedCoupons, couponError = null) }

            if (coupon.freeShipping && _state.value.freeShippingByCouponIsActive) {
                _state.update { it.copy(freeShippingByCouponIsActive = false) }
                selectShipping(_state.value.shippingMethods.first())
            }

            recalculateTotals()
        }
    }

    private fun recalculateTotals() {
        val currentState = _state.value
        val subtotal = currentState.cartItems.sumOf { it.currentPrice * it.quantity }
        var totalDiscount = 0.0
        val fees = _state.value.fees
        val discountAmount =
            paymentDiscountAmount(methodId = state.value.selectedPaymentGateway?.id ?: "none")
        if (discountAmount > 0.0) {
            fees[FeeKeys.PAYMENT_DISCOUNT] = -1 * discountAmount
        } else {
            fees.remove(FeeKeys.PAYMENT_DISCOUNT)
        }

        currentState.appliedCoupons.forEach { coupon ->
            when (coupon.discountType) {
                "percent" -> {
                    val discountValue = coupon.amount
                    totalDiscount += (subtotal * (discountValue / 100.0))
                }

                "fixed_cart" -> {
                    val discountValue = coupon.amount
                    totalDiscount += discountValue
                }

                "fixed_product" -> {
                    val discountValue = coupon.amount
                    totalDiscount += discountValue
                }
            }
        }

        totalDiscount = totalDiscount.coerceAtMost(subtotal)

        var totalFees = 0.0
        fees.values.forEach { fee ->
            totalFees += fee
        }

        val hasFreeShippingCoupon = currentState.appliedCoupons.any { it.freeShipping }
        val freeShippingByProductShippingClass =
            _state.value.cartItems.any { it.shippingClass == "free-shipping" }
        var shippingFee =
            if (hasFreeShippingCoupon || freeShippingByProductShippingClass) 0.0 else (currentState.shippingCost)

        _state.value.freeShippingMethodByMinOrder?.let {
            if (it.isEligibleForMinOrderAmount(subTotal = subtotal)) {
                _state.update { state -> state.copy(freeShippingByMinOrderIsActive = true) }
                selectShipping(it)
                shippingFee = 0.0
            } else {
                _state.update { state -> state.copy(freeShippingByMinOrderIsActive = false) }
            }
        }

        var finalTotal = (subtotal - totalDiscount + shippingFee + totalFees).coerceAtLeast(0.0)

        var walletPaymentAmount = 0.0
        if (_state.value.useWallet) {
            val balance = _state.value.userWallet?.balance ?: 0.0
            walletPaymentAmount = balance.coerceAtMost(finalTotal)
            finalTotal -= walletPaymentAmount
        }

        _state.update {
            it.copy(
                subTotal = subtotal,
                totalDiscount = totalDiscount,
                shippingCost = shippingFee,
                fees = fees,
                paidByWallet = walletPaymentAmount,
                totalFees = totalFees,
                total = finalTotal,
            )
        }
    }

    fun confirmOrder() {
        scope.launch {
            val currentState = _state.value
            val cartItems: List<CartItem> = currentState.cartItems
            val shippingAddress = currentState.shippingAddress
            val paymentGateway = currentState.selectedPaymentGateway
            val shippingMethod = currentState.selectedShippingMethod
            val couponCodes = if (currentState.appliedCoupons.isNotEmpty()) {
                currentState.appliedCoupons.map { it.code }
            } else {
                null
            }

            val totalWalletPayment = currentState.useWallet && currentState.total == 0.0

            val feeLines = currentState.fees.map { (key, value) ->
                FeeLine(
                    name = key,
                    total = value,
                )
            }.toMutableList()

            val metadata: MutableList<Metadata> = emptyList<Metadata>().toMutableList()
            if (currentState.useWallet) {
                feeLines.add(
                    FeeLine(
                        name = "via_wallet",
                        total = -1 * currentState.paidByWallet,
                        metadata = listOf(getWalletPartialPaymentMeta()),
                    ),
                )

                metadata.add(getPartialPaymentAmount(currentState.paidByWallet.toString()))
            }

            val paymentReturnScheme = networkConfigProvider.get().paymentReturnScheme
            metadata.add(getPaymentRedirectUrl(paymentReturnScheme))
            metadata.add(getMobileReturnEnabledMeta())
            metadata.add(getMobileReturnSchemeMeta(paymentReturnScheme))
            metadata.add(getMobileReturnExpiresMeta())

            if (cartItems.isEmpty()) {
                _state.update { it.copy(error = CheckoutError.EmptyCart()) }
                return@launch
            }
            if (shippingAddress == null) {
                _state.update { it.copy(error = CheckoutError.AddressNotSelected()) }
                return@launch
            }
            if (shippingMethod == null) {
                _state.update { it.copy(error = CheckoutError.ShippingMethodNotSelected()) }
                return@launch
            }
            if (paymentGateway == null) {
                _state.update { it.copy(error = CheckoutError.PaymentMethodNotSelected()) }
                return@launch
            }

            var status = if (paymentGateway.id == "bacs" && !totalWalletPayment) "on-hold" else null
            if (totalWalletPayment || paymentGateway.id == "cod") {
                status = "processing"
            }

            val orderData = NewOrderData(
                coupon = couponCodes,
                cartItems = cartItems,
                shipping = shippingAddress,
                paymentMethod = if (totalWalletPayment) "wallet" else paymentGateway.id,
                paymentMethodTitle = if (totalWalletPayment) "Wallet" else paymentGateway.title,
                shippingMethod = shippingMethod.copy(cost = currentState.shippingCost.toString()),
                billing = shippingAddress,
                feeLines = feeLines.ifEmpty { null },
                metaData = metadata,
                status = status,
            )

            _state.update { it.copy(placeOrderStatus = PlaceOrderStatus.InProgress) }

            createOrderUseCase(orderData).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val orderResponse = result.data

                        if (totalWalletPayment) {
                            clearCartUseCase()
                            _state.update {
                                it.copy(
                                    placeOrderStatus = PlaceOrderStatus.Success(
                                        orderId = orderResponse.id,
                                        orderTotal = currentState.paidByWallet.toString(),
                                    ),
                                )
                            }
                        } else if (paymentGateway.id == "bacs") {
                            clearCartUseCase()
                            val bacsDetails = getBACSDetails()
                            _state.update {
                                it.copy(
                                    placeOrderStatus = PlaceOrderStatus.BACSSuccess(
                                        orderId = orderResponse.id,
                                        orderTotal = orderResponse.total,
                                        bacsDetails = bacsDetails,
                                    ),
                                )
                            }
                        } else if (paymentGateway.id == "cod") {
                            clearCartUseCase()
                            _state.update {
                                it.copy(
                                    placeOrderStatus = PlaceOrderStatus.Success(
                                        orderId = orderResponse.id,
                                        orderTotal = orderResponse.total,
                                    ),
                                )
                            }
                        } else if (!orderResponse.paymentUrl.isNullOrBlank()) {
                            _state.update {
                                it.copy(
                                    placeOrderStatus = PlaceOrderStatus.AwaitingPayment(
                                        paymentUrl = orderResponse.paymentUrl,
                                        orderId = orderResponse.id,
                                    ),
                                )
                            }
                        }
                    }

                    is Result.Failure -> {
                        _state.update {
                            it.copy(
                                placeOrderStatus = PlaceOrderStatus.Failed(
                                    errorMessage = result.error.toString(),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    fun verifyOrderStatusAfterPayment() {
        if (verificationJob?.isActive == true) return

        val currentState = _state.value.placeOrderStatus
        if (currentState !is PlaceOrderStatus.AwaitingPayment) return

        val orderId = currentState.orderId

        verificationJob = scope.launch {
            delay(1000)

            when (val result = getOrderStatusUseCase(orderId)) {
                is Result.Success -> {
                    val orderStatus = result.data.status
                    if (orderStatus == "completed" || orderStatus == "processing") {
                        clearCartUseCase()
                        _state.update {
                            it.copy(
                                placeOrderStatus = PlaceOrderStatus.Success(
                                    orderId = result.data.id,
                                    orderTotal = result.data.total,
                                ),
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                placeOrderStatus = PlaceOrderStatus.Failed(
                                    errorMessage = paymentUnsuccessMessage(orderStatus),
                                    canRetry = false,
                                ),
                            )
                        }
                    }
                }

                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            placeOrderStatus = PlaceOrderStatus.Failed(
                                errorMessage = result.error.toString(),
                                canRetry = false,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun resetOrderStatus() {
        _state.update { it.copy(placeOrderStatus = PlaceOrderStatus.Idle) }
    }

    fun onAddressSelected(address: com.solutionium.shared.data.model.Address) {
        _state.update { it.copy(shippingAddress = address, isAddressListExpanded = false) }
    }

    fun onToggleAddressList() {
        _state.update { it.copy(isAddressListExpanded = !it.isAddressListExpanded) }
    }

    fun onUseWalletChange(useWallet: Boolean) {
        if (!_state.value.walletEnabled) return
        _state.update { it.copy(useWallet = useWallet) }
        recalculateTotals()
    }

    fun clear() {
        scope.cancel()
    }
}
