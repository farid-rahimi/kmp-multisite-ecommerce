package com.solutionium.shared.viewmodel

import org.koin.dsl.module

val iosAppModule = module {
    factory<AppVersionProvider> {
        AppVersionProvider {
            "2.4"
        }
    }
}
