package com.solutionium.sharedui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberPlatformShareAction(): (title: String, url: String) -> Unit {
    return remember {
        { title, url ->
            val normalizedUrl = url.trim()
            if (normalizedUrl.isNotBlank()) {
                val normalizedTitle = title.trim()
                val shareText = if (normalizedTitle.isNotBlank()) {
                    "$normalizedTitle\n$normalizedUrl"
                } else {
                    normalizedUrl
                }

                val presenter = topMostViewController()
                if (presenter != null) {
                    val controller = UIActivityViewController(
                        activityItems = listOf(shareText),
                        applicationActivities = null
                    )
                    presenter.presentViewController(controller, animated = true, completion = null)
                }
            }
        }
    }
}

private fun topMostViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    var controller = app.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
