package com.solutionium.sharedui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.all
import com.solutionium.sharedui.resources.awaiting
import com.solutionium.sharedui.resources.cancelled
import com.solutionium.sharedui.resources.completed
import com.solutionium.sharedui.resources.failed
import com.solutionium.sharedui.resources.my_orders
import com.solutionium.sharedui.resources.on_hold
import com.solutionium.sharedui.resources.pending
import com.solutionium.sharedui.resources.processing
import com.solutionium.sharedui.resources.refunded
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.viewmodel.OrderListStatusFilter
import com.solutionium.shared.viewmodel.OrderListViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OrderListScreen(
    onOrderClick: (orderId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: OrderListViewModel,
) {
    val orders = viewModel.pagedList.collectAsLazyPagingItems()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    val isRefreshing = orders.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.my_orders)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OrderFilterChips(
                selectedStatus = selectedStatus,
                onStatusSelected = viewModel::onFilterChange,
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { orders.refresh() },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (orders.loadState.refresh is LoadState.Loading) {
                        items(5) { OrderSummaryCardPlaceholder() }
                    }

                    items(orders.itemCount) { index ->
                        orders[index]?.let { order ->
                            SimpleOrderSummaryCard(
                                order = order,
                                onClick = { onOrderClick(order.id) },
                            )
                        }
                    }

                    if (orders.loadState.append is LoadState.Loading) {
                        item { OrderSummaryCardPlaceholder() }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderFilterChips(
    selectedStatus: OrderListStatusFilter,
    onStatusSelected: (OrderListStatusFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(OrderListStatusFilter.entries.toTypedArray()) { status ->
            FilterChip(
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                selected = status == selectedStatus,
                onClick = { onStatusSelected(status) },
                label = { Text(stringResource(status.titleRes())) },
            )
        }
    }
}

@Composable
private fun SimpleOrderSummaryCard(
    order: Order,
    onClick: () -> Unit,
) {
    Card(onClick = onClick) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Order #${order.id}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = order.status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = order.total,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun OrderSummaryCardPlaceholder() {
    Card {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator()
        }
    }
}

private fun OrderListStatusFilter.titleRes(): StringResource = when (this) {
    OrderListStatusFilter.ALL -> Res.string.all
    OrderListStatusFilter.PROCESSING -> Res.string.processing
    OrderListStatusFilter.AWAITING -> Res.string.awaiting
    OrderListStatusFilter.ON_HOLD -> Res.string.on_hold
    OrderListStatusFilter.COMPLETED -> Res.string.completed
    OrderListStatusFilter.CANCELLED -> Res.string.cancelled
    OrderListStatusFilter.FAILED -> Res.string.failed
    OrderListStatusFilter.REFUNDED -> Res.string.refunded
    OrderListStatusFilter.PENDING -> Res.string.pending
}
