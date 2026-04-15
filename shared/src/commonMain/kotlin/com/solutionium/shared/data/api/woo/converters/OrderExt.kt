package com.solutionium.shared.data.api.woo.converters

//import com.solutionium.data.api.woo.BuildConfig.APP_VERSION_NAME
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.FeeLine
import com.solutionium.shared.data.model.LineItem
import com.solutionium.shared.data.model.Metadata
import com.solutionium.shared.data.model.NewOrderData
import com.solutionium.shared.data.model.Order

//import com.solutionium.data.network.BuildConfig.BASE_URL
//import com.solutionium.shared.BuildKonfig.BASE_URL
import com.solutionium.shared.data.network.common.WooAddress
import com.solutionium.shared.data.network.common.WooFeeLine
import com.solutionium.shared.data.network.request.CouponLine
import com.solutionium.shared.data.network.request.LineItemRequest
import com.solutionium.shared.data.network.request.OrderMetadata
import com.solutionium.shared.data.network.request.OrderRequest
import com.solutionium.shared.data.network.request.ShippingLine
import com.solutionium.shared.data.network.response.WooLineItem
import com.solutionium.shared.data.network.response.WooOrderResponse

fun NewOrderData.toRequestBody(vatRate: Double = 0.0): OrderRequest =
    OrderRequest(
        paymentMethod = paymentMethod,
        paymentMethodTitle = paymentMethodTitle,
        setPaid = setPaid,
        billing = billing.toRequestBody(),
        shipping = shipping.toRequestBody(),
        lineItems = cartItems.map { it.toRequestBody(vatRate = vatRate) },
        shippingLines = listOf(
            ShippingLine(
                methodID = shippingMethod.id.toString(),
                methodTitle = shippingMethod.title,
                total = shippingMethod.cost
            )
        ),
        feeLines = feeLines?.map { it.toWooFeeLine() },
        status = status,
        currency = currency,
        customerID = customerID,
        customerNote = customerNote,
        createdVia = "mobile_app_ver:todo", //$APP_VERSION_NAME
        transactionID = null,
        couponLines = coupon?.map { CouponLine(it) },
        metaData = metaData.map { it.toRequestBody() },
    )

fun Address.toRequestBody(): WooAddress =
    WooAddress(
        firstName = firstName,
        lastName = lastName,
        company = company,
        address1 = address1,
        address2 = address2,
        city = city,
        state = state,
        postcode = postcode,
        country = country,
        email = email,
        phone = phone
    )

fun CartItem.toRequestBody(vatRate: Double = 0.0): LineItemRequest =
    LineItemRequest(
        productID = productId,
        quantity = quantity,
        variationID = if (isDecant || variationId <= 0) null else variationId,
        name = if (isDecant) name else null,
        // Woo expects line subtotal/total as pre-tax values.
        // We keep app prices tax-inclusive, so convert gross -> net when forcing line amounts.
        // Force line amounts for any discounted line (normal sale, app offer, decant).
        subTotal = if (hasLineDiscount()) {
            val grossLineSubTotal = baseGrossUnitPrice() * quantity
            calculateNetFromGross(grossLineSubTotal, vatRate).toString()
        } else null,
        total = if (hasLineDiscount()) {
            val grossLineTotal = currentPrice * quantity
            calculateNetFromGross(grossLineTotal, vatRate).toString()
        } else null,
        metaData = if (isDecant) listOf(
            OrderMetadata(
                key = "is_dec",
                value = "y"
            ),
            OrderMetadata(
                key = "d_vol",
                value = decVol ?: ""
            ),
        ) else null,
        //taxClass = "",
        //metaData = null
    )

private fun CartItem.hasLineDiscount(): Boolean {
    if (isDecant) return true
    if (appOffer > 0.0) return true
    val regular = regularPrice ?: return false
    return regular > 0.0 && regular > currentPrice
}

private fun CartItem.baseGrossUnitPrice(): Double {
    val regular = regularPrice
    if (regular != null && regular > 0.0 && regular > currentPrice) {
        return regular
    }
    if (appOffer > 0.0 && appOffer < 100.0) {
        return currentPrice / (1.0 - (appOffer / 100.0))
    }
    return currentPrice
}

fun Metadata.toRequestBody(): OrderMetadata =
    OrderMetadata(
        key = key,
        value = value
    )

fun WooOrderResponse.toModel(baseUrl: String): Order = Order(
    id = id,
    orderNumber = number,
    total = total,
    status = status,
    dateCreated = dateCreated,
    currency = currency,
    subtotal = lineItems.sumOf { it.subtotal.toDoubleOrNull() ?: 0.0 }.toStableMoneyString(),
    shippingTotal = shippingTotal,
    totalTax = totalTax,
    discountTotal = discountTotal,
    feeTotal = feeLines.orEmpty().sumOf { it.total.toDoubleOrNull() ?: 0.0 }.toStableMoneyString(),
    datePaid = datePaid,
    dateCompleted = dateCompleted,
    paymentMethod = paymentMethod,
    paymentMethodTitle = paymentMethodTitle,
    shippingMethodTitle = shippingLines.firstOrNull()?.methodTitle,
    customerNote = customerNote.takeIf { it.isNotBlank() },
    billingAddress = billing.toModel(),
    shippingAddress = shipping.toModel(),
    orderKey = orderKey, //orderKey
    paymentUrl = paymentUrl
        ?.takeIf { it.isNotBlank() }
        ?: "${baseUrl.ensureTrailingSlash()}checkout/order-pay/${id}/?pay_for_order=true&key=${orderKey}",
    lineItems = lineItems.map { it.toModel() }
)

fun WooLineItem.toModel() = LineItem (
    id = id,
    name = name,
    productId = productID,
    quantity = quantity,
    total = total,
    totalTax = totalTax,
    subTotal = subtotal,
    subTotalTax = subtotalTax,
    imageUrl = image?.src

)

fun FeeLine.toWooFeeLine() = WooFeeLine(
    name = name,
    total = total.toString(),
    metaData = metadata.map { it.toRequestBody() }
)

private fun WooAddress.toModel() = Address(
    id = null,
    title = null,
    firstName = firstName,
    lastName = lastName,
    company = company,
    address1 = address1,
    address2 = address2,
    city = city,
    state = state,
    postcode = postcode,
    country = country,
    email = email,
    phone = phone,
    isDefault = false,
)

private fun Double.toStableMoneyString(): String = toString()

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"

private fun calculateNetFromGross(gross: Double, vatRatePercent: Double): Double {
    val safeRate = vatRatePercent.coerceAtLeast(0.0)
    if (safeRate == 0.0) return gross
    // Project uses fractional VAT (e.g. 0.05 for 5%).
    // Keep compatibility if a whole percent slips in (e.g. 5.0).
    val normalizedRate = if (safeRate > 1.0) safeRate / 100.0 else safeRate
    return gross / (1.0 + normalizedRate)
}
