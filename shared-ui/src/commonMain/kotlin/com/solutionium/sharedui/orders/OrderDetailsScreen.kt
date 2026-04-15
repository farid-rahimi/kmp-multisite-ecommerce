package com.solutionium.sharedui.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.LineItem
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.viewmodel.OrderDetailsViewModel
import com.solutionium.sharedui.common.component.FormattedPriceV3
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.sharedui.common.component.platformPrimaryButtonShape
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.discount
import com.solutionium.sharedui.resources.free
import com.solutionium.sharedui.resources.includes_vat
import com.solutionium.sharedui.resources.my_orders
import com.solutionium.sharedui.resources.payment_methods_section_title
import com.solutionium.sharedui.resources.shipping
import com.solutionium.sharedui.resources.shipping_address_section_title
import com.solutionium.sharedui.resources.subtotal
import com.solutionium.sharedui.resources.total
import com.solutionium.sharedui.resources.vat
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onBack: () -> Unit,
    viewModel: OrderDetailsViewModel,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            PlatformTopBar(
                title = { Text(stringResource(Res.string.my_orders)) },
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = viewModel::retry,
                        shape = platformPrimaryButtonShape(),
                    ) {
                        Text("Try again")
                    }
                }
            }

            state.order != null -> {
                OrderDetailsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    order = state.order!!,
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    modifier: Modifier = Modifier,
    order: Order,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeaderSection(order = order)
        }

        item {
            SectionCard(title = "Items") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    order.lineItems.forEach { item ->
                        OrderItemRow(item = item)
                    }
                }
            }
        }

        item {
            SectionCard(title = "Price Summary") {
                val shippingAmount = order.shippingTotal.toMoneyDouble()
                val taxAmount = order.totalTax.toMoneyDouble()
                val discountAmount = order.discountTotal.toMoneyDouble()
                val feeAmount = order.feeTotal.toMoneyDouble()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceRow(title = stringResource(Res.string.subtotal), amount = order.subtotal)
                    if (shippingAmount.isZeroMoney()) {
                        LabelValueRow(
                            title = stringResource(Res.string.shipping),
                            value = stringResource(Res.string.free),
                        )
                    } else {
                        PriceRow(title = stringResource(Res.string.shipping), amount = order.shippingTotal)
                    }
                    if (!taxAmount.isZeroMoney()) {
                        PriceRow(title = stringResource(Res.string.vat), amount = order.totalTax)
                    }
                    if (!discountAmount.isZeroMoney()) {
                        PriceRow(title = stringResource(Res.string.discount), amount = (-discountAmount).toString())
                    }
                    if (!feeAmount.isZeroMoney()) {
                        PriceRow(title = "Fees", amount = order.feeTotal)
                    }
                    HorizontalDivider()
                    PriceRow(
                        title = stringResource(Res.string.total),
                        amount = order.total,
                        emphasized = true,
                    )
                }
            }
        }

        item {
            SectionCard(title = "Address") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddressBlock(
                        title = "Billing Address",
                        address = order.billingAddress,
                    )
                    AddressBlock(
                        title = stringResource(Res.string.shipping_address_section_title),
                        address = order.shippingAddress,
                    )
                }
            }
        }

        item {
            SectionCard(title = stringResource(Res.string.payment_methods_section_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ValueLine(label = "Method", value = order.paymentMethodTitle)
                    ValueLine(label = "Status", value = beautifiedStatus(order.status))
                    order.datePaid?.let { ValueLine(label = "Paid at", value = beautifiedOrderDate(it)) }
                    order.shippingMethodTitle?.takeIf { it.isNotBlank() }?.let {
                        ValueLine(label = "Shipping Method", value = it)
                    }
                }
            }
        }

        order.customerNote?.takeIf { it.isNotBlank() }?.let { note ->
            item {
                SectionCard(title = "Note") {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(order: Order) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Order #${order.orderNumber.ifBlank { order.id.toString() }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = beautifiedStatus(order.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = beautifiedOrderDate(order.dateCreated),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FormattedPriceV3(
                amount = order.total.toDoubleOrNull() ?: 0.0,
                mainStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                smallDigitsSpanStyle = MaterialTheme.typography.titleMedium.toSpanStyle().copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun OrderItemRow(item: LineItem) {
    val itemTotalBeforeTax = item.total.toDoubleOrNull() ?: 0.0
    val taxAmount = item.totalTax.toDoubleOrNull()
        ?: item.subTotalTax.toDoubleOrNull()
        ?: 0.0
    val itemTotalAfterTax = itemTotalBeforeTax + taxAmount
    val unitPriceAfterTax = if (item.quantity > 0) itemTotalAfterTax / item.quantity else itemTotalAfterTax

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Qty: ${item.quantity} ×",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FormattedPriceV3(
                    amount = unitPriceAfterTax,
                    mainStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    smallDigitsSpanStyle = MaterialTheme.typography.labelSmall.toSpanStyle().copy(fontWeight = FontWeight.Medium),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            FormattedPriceV3(
                amount = itemTotalAfterTax,
                mainStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                smallDigitsSpanStyle = MaterialTheme.typography.bodySmall.toSpanStyle().copy(fontWeight = FontWeight.SemiBold),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.includes_vat),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FormattedPriceV3(
                    amount = taxAmount,
                    mainStyle = MaterialTheme.typography.labelSmall,
                    smallDigitsSpanStyle = MaterialTheme.typography.labelSmall.toSpanStyle(),
                )
            }
        }
    }
}

@Composable
private fun LabelValueRow(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PriceRow(
    title: String,
    amount: String,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        )
        FormattedPriceV3(
            amount = amount.toDoubleOrNull() ?: 0.0,
            mainStyle = if (emphasized) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            smallDigitsSpanStyle = MaterialTheme.typography.bodySmall.toSpanStyle().copy(
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
            ),
        )
    }
}

private fun String.toMoneyDouble(): Double = toDoubleOrNull() ?: 0.0

private fun Double.isZeroMoney(): Boolean = kotlin.math.abs(this) < 0.0001

@Composable
private fun AddressBlock(
    title: String,
    address: Address?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        if (address == null) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        val lines = buildList {
            val fullName = listOf(address.firstName, address.lastName).filter { it.isNotBlank() }.joinToString(" ")
            if (fullName.isNotBlank()) add(fullName)
            if (address.address1.isNotBlank()) add(address.address1)
            address.address2?.takeIf { it.isNotBlank() }?.let(::add)
            add(listOf(address.city, address.state, address.postcode).filter { it.isNotBlank() }.joinToString(", "))
            if (address.country.isNotBlank()) add(address.country)
            address.phone?.takeIf { it.isNotBlank() }?.let { add("Phone: $it") }
            address.email?.takeIf { it.isNotBlank() }?.let { add("Email: $it") }
        }

        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ValueLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
