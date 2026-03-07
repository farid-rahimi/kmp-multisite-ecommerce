package com.solutionium.shared.data.local

import platform.Foundation.NSUserDefaults

actual fun applyPlatformLanguage(languageCode: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
    defaults.setObject(languageCode, forKey = "AppleLocale")
    defaults.synchronize()
}
