package com.solutionium.shared.data.local

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

val iosLocalModule = module {
    single<Settings>(named("EncPrefs")) {
        val userDefaults = NSUserDefaults.standardUserDefaults
        NSUserDefaultsSettings(userDefaults)
    }

    single<Settings>(named("AppPrefs")) {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}
