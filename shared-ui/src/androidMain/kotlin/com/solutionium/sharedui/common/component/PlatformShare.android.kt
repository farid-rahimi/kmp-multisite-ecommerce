package com.solutionium.sharedui.common.component

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformShareAction(): (title: String, url: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { title, url ->
            val normalizedUrl = url.trim()
            if (normalizedUrl.isNotBlank()) {
                val normalizedTitle = title.trim()
                val shareText = if (normalizedTitle.isNotBlank()) {
                    "$normalizedTitle\n$normalizedUrl"
                } else {
                    normalizedUrl
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, normalizedTitle)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooser = Intent.createChooser(intent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        }
    }
}
