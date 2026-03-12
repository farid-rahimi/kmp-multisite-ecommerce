package com.solutionium.shared.domain.order

import com.solutionium.shared.data.orders.orderDataModule
import org.koin.dsl.module


fun getOrderDomainModules() = setOf(orderDomainModule, orderDataModule)


val orderDomainModule = module {
    factory<GetOrderListPagingUseCase> { GetOrderListPagingUseCaseImpl(get()) }
    factory<GetLatestOrderUseCase> { GetLatestOrderUseCaseImpl(get()) }
    factory<GetOrderByIdUseCase> { GetOrderByIdUseCaseImpl(get()) }
}
