package com.solutionium.shared.data.favorite

import com.solutionium.shared.data.api.woo.getApiModule
import com.solutionium.shared.data.database.databaseModule
import org.koin.dsl.module

fun getFavoriteDataModules() = setOf(favoriteDataModule, databaseModule) + getApiModule()

val favoriteDataModule = module {
    single<FavoriteRepository> { FavoriteRepositoryImpl(get(), get(), get(), get()) }
}
