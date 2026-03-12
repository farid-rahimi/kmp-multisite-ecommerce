package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.BACSDetails
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.Coupon
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.ShippingMethod
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.domain.checkout.CouponError

sealed class PlaceOrderStatus {
    data object Idle : PlaceOrderStatus()
    data object InProgress : PlaceOrderStatus()
    data class AwaitingPayment(val paymentUrl: String, val orderId: Int) : PlaceOrderStatus()
    data class Success(val orderId: Int, val orderTotal: String) : PlaceOrderStatus()
    //data class CODSuccess(val orderId: Int, val orderTotal: String) : PlaceOrderStatus()

    data class BACSSuccess(
        val orderId: Int,
        val orderTotal: String,
        val bacsDetails: BACSDetails?,
    ) : PlaceOrderStatus()

    data class Failed(val errorMessage: String, val canRetry: Boolean = false) : PlaceOrderStatus()
}

data class CheckoutUiState(
    val isUserLoggedIn: Boolean? = null,
    val cartItems: List<CartItem> = emptyList(),
    val addressId: String? = null,
    var shippingAddress: Address? = null,
    val isLoadingAddress: Boolean = false,
    val subTotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val totalFees: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val total: Double = 0.0,
    val isInstallment: Boolean = false,
    val fees: MutableMap<String, Double> = emptyMap<String, Double>().toMutableMap(),
    val shippingMethods: List<ShippingMethod> = emptyList(),
    val freeShippingMethodByCoupon: ShippingMethod? = null,
    val freeShippingByCouponIsActive: Boolean = false,
    val freeShippingMethodByMinOrder: ShippingMethod? = null,
    val freeShippingByMinOrderIsActive: Boolean = false,
    val selectedShippingMethod: ShippingMethod? = null,
    val paymentGateways: List<PaymentGateway> = emptyList(),
    val selectedPaymentGateway: PaymentGateway? = null,
    val isLoadingShippingMethods: Boolean = false,
    val isLoadingPaymentGateways: Boolean = false,
    val message: String? = null,
    val error: CheckoutError? = null,
    val placeOrderStatus: PlaceOrderStatus = PlaceOrderStatus.Idle,
    val isApplyingCoupon: Boolean = false,
    val couponError: CouponError? = null,
    val appliedCoupons: List<Coupon> = emptyList(),
    val paymentMethodDiscounts: Map<String, Double> = emptyMap(),
    val availableAddresses: List<Address> = emptyList(),
    val isAddressListExpanded: Boolean = false,
    val walletEnabled: Boolean = false,
    val userWallet: UserWallet? = null,
    val loadingWallet: Boolean = false,
    val useWallet: Boolean = false,
    val paidByWallet: Double = 0.0,
)

sealed class CheckoutError(open val messageKey: String) {
    data class EmptyCart(
        override val messageKey: String = "empty_cart_error",
    ) : CheckoutError(messageKey)

    data class AddressNotSelected(
        override val messageKey: String = "error_address_not_selected",
    ) : CheckoutError(messageKey)

    data class ShippingMethodNotSelected(
        override val messageKey: String = "error_shipping_method_not_selected",
    ) : CheckoutError(messageKey)

    data class PaymentMethodNotSelected(
        override val messageKey: String = "error_payment_method_not_selected",
    ) : CheckoutError(messageKey)

    data class GeneralLoadingError(
        override val messageKey: String = "error_general_checkout",
    ) : CheckoutError(messageKey)
}

object FeeKeys {
    const val PAYMENT_DISCOUNT = "payment_discount"
}
