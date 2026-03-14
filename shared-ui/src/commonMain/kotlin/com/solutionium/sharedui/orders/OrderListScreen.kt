package com.solutionium.sharedui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.solutionium.shared.viewmodel.OrderListViewModel
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.my_orders
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OrderListScreen(
    onOrderClick: (orderId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: OrderListViewModel,
) {
    val orders = viewModel.pagedList.collectAsLazyPagingItems()

    val isRefreshing = orders.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            PlatformTopBar(
                title = { Text(stringResource(Res.string.my_orders)) },
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
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
                            OrderSummaryCard(
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
