package com.solutionium.shared.util

import java.util.Locale

actual fun currentLanguageCode(): String {
    return Locale.getDefault().language.lowercase()
}
