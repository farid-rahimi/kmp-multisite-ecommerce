package com.solutionium.shared.data.config

import com.solutionium.shared.data.config.impl.AppConfigRepositoryImpl
import org.koin.dsl.module

val appConfigDataModule = module {
    single<AppConfigRepository> { AppConfigRepositoryImpl(get(), get()) }
}
