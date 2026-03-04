package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.order.getOrderDomainModules
import org.koin.dsl.module

fun getOrderListModules() = setOf(orderListModule) + getOrderDomainModules()

val orderListModule = module {
    factory { OrderListViewModel(get()) }
}

