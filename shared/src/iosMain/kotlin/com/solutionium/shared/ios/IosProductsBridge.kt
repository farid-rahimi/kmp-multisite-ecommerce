package com.solutionium.shared.ios

import com.solutionium.shared.data.api.woo.getApiModule
import com.solutionium.shared.data.model.ProductListType
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.products.GetProductsListUseCase
import com.solutionium.shared.domain.products.getProductsDomainModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

data class IosProductItem(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val regularPrice: Double? = null,
    val onSale: Boolean = false,
    val salePercent: Int = 0,
    val stockCount: Int = 0,
    val stockStatus: String = "instock",
    val manageStock: Boolean = false,
    val featureText: String? = null,
)

private fun ensureKoinStarted() {
    runCatching {
        startKoin {
            modules(
                (getProductsDomainModules() + getApiModule()).toList(),
            )
        }
    }
}

class IosProductsBridge {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun loadProducts(
        onResult: (List<IosProductItem>) -> Unit,
        onError: (String) -> Unit,
    ) {
        scope.launch {
            try {
                ensureKoinStarted()
                val useCase: GetProductsListUseCase = KoinPlatform.getKoin().get()
                val result = useCase(ProductListType.New).first()

                when (result) {
                    is Result.Success -> onResult(
                        result.data.map {
                            val salePercent = when {
                                it.appOffer > 0.0 -> it.appOffer.toInt()
                                it.onSale && it.regularPrice != null && it.regularPrice > it.price ->
                                    (((it.regularPrice - it.price) / it.regularPrice) * 100).toInt()
                                else -> 0
                            }

                            IosProductItem(
                                id = it.id,
                                name = it.name,
                                price = it.price,
                                imageUrl = it.imageUrl,
                                regularPrice = it.regularPrice,
                                onSale = it.onSale || it.appOffer > 0.0,
                                salePercent = salePercent,
                                stockCount = it.stock,
                                stockStatus = it.stockStatus,
                                manageStock = it.manageStock,
                                featureText = it.features().firstOrNull()?.name?.replace('_', ' '),
                            )
                        }
                    )

                    is Result.Failure -> onError("Failed to load products")
                }
            } catch (t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}
