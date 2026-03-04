package com.solutionium.shared.viewmodel

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.solutionium.shared.data.model.FilterCriterion
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.OrderFilterKey
import com.solutionium.shared.domain.order.GetOrderListPagingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class OrderListViewModel(
    private val getOrdersUseCase: GetOrderListPagingUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _selectedStatus = MutableStateFlow(OrderListStatusFilter.ALL)
    val selectedStatus = _selectedStatus.asStateFlow()

    val pagedList: Flow<PagingData<Order>> =
        _selectedStatus
            .flatMapLatest { statusFilter ->
                val filters: MutableList<FilterCriterion> = emptyList<FilterCriterion>().toMutableList()
                if (statusFilter != OrderListStatusFilter.ALL) {
                    filters.add(FilterCriterion(OrderFilterKey.STATUS.apiKey, statusFilter.key))
                }
                getOrdersUseCase(filters)
            }
            .cachedIn(scope)

    fun onFilterChange(newStatus: OrderListStatusFilter) {
        _selectedStatus.update { newStatus }
    }

    fun clear() {
        scope.cancel()
    }
}
