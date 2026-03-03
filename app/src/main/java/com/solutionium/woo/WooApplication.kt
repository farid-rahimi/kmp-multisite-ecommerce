package com.solutionium.woo

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class WooApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WooApplication)
            modules(allModules) // From AppModule.kt
        }
    }

//    override fun attachBaseContext(base: Context) {
//        // This is the most reliable place to force the app's locale.
//        // It runs before any activity or other component is created.
//        val localeToSet = Locale("fa")
//        Locale.setDefault(localeToSet)
//
//        val config = Configuration(base.resources.configuration)
//        config.setLocale(localeToSet)
//        config.setLayoutDirection(localeToSet)
//
//        val updatedContext = base.createConfigurationContext(config)
//        super.attachBaseContext(updatedContext)
//    }
}
