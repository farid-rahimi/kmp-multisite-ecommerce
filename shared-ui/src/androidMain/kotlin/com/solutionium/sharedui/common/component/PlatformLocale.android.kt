package com.solutionium.sharedui.common.component

import java.util.Locale

actual fun defaultLocaleTag(): String = Locale.getDefault().toLanguageTag()