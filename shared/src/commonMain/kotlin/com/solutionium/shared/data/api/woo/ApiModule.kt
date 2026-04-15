package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.api.woo.impl.WooCategoryRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooCheckoutRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooConfigRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooCouponRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooFavoriteRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooOrderRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooProductsRemoteSourceImpl
import com.solutionium.shared.data.api.woo.impl.WooUserRemoteSourceImpl
import com.solutionium.shared.data.network.getNetworkDataModules
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getApiModule() = setOf(apiModule) + getNetworkDataModules()


val apiModule = module {
    single<WooProductsRemoteSource> {
        WooProductsRemoteSourceImpl(
            productApi = get(named("WooAuthProductClient")),
            publicProductApi = get(named("WooPublicProductClient")),
            networkConfigProvider = get(),
            tokenStore = get(),
        )
    }
    single<WooCategoryRemoteSource> { WooCategoryRemoteSourceImpl(get()) }
    single<WooCheckoutRemoteSource> { WooCheckoutRemoteSourceImpl(get(), get(), get()) }
    single<WooUserRemoteSource> { WooUserRemoteSourceImpl(get(), get()) }
    single<WooOrderRemoteSource> { WooOrderRemoteSourceImpl(get(), get()) }
    single<WooCouponRemoteSource> { WooCouponRemoteSourceImpl(get()) }
    single<WooFavoriteRemoteSource> { WooFavoriteRemoteSourceImpl(get(), get()) }
    single<WooConfigRemoteSource> { WooConfigRemoteSourceImpl(get()) }
}
