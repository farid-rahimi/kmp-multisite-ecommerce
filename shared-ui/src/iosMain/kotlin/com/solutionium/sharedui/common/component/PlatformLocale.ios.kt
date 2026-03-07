package com.solutionium.sharedui.common.component

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual fun defaultLocaleTag(): String = NSLocale.currentLocale.localeIdentifier
