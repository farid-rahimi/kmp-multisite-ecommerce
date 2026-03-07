package com.solutionium.shared.util

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual fun currentLanguageCode(): String {
    val tag = (NSLocale.preferredLanguages.firstOrNull() as? String)
        ?: NSLocale.currentLocale.localeIdentifier
    return tag
        .substringBefore('-')
        .substringBefore('_')
        .lowercase()
}
