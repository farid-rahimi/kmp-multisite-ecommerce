package com.solutionium.shared.data.user

import com.solutionium.shared.data.api.woo.getApiModule
import com.solutionium.shared.data.database.databaseModule
import com.solutionium.shared.data.local.localModule
import org.koin.dsl.module

fun getUserDataModules() = setOf(userDataModule, databaseModule, localModule) + getApiModule()

val userDataModule = module {
    single<WooUserRepository> { WooUserRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    single<StoryViewRepository> { StoryViewRepositoryImpl(get()) }
}
