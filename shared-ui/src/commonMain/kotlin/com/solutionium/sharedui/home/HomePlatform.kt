package com.solutionium.sharedui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.BannerItem
import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.sharedui.common.component.BannerSlider

@Composable
fun PlatformHeaderLogo(
    url: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        AsyncImage(
            model = url,
            contentDescription = "Logo",
            modifier = modifier,
        )
    }
}

@Composable
fun PlatformBannerSlider(
    modifier: Modifier = Modifier,
    items: List<BannerItem>,
    onBannerClick: (item: BannerItem) -> Unit,
) {
    BannerSlider(
        modifier = modifier,
        items = items,
        onBannerClick = onBannerClick,
    )
}

@Composable
fun PlatformContactSupportDialog(
    contactInfo: ContactInfo?,
    onDismiss: () -> Unit,
) {
    if (contactInfo == null) return

    val uriHandler = LocalUriHandler.current
    val contactRows = listOf(
        "WhatsApp" to contactInfo.whatsapp,
        "Telegram" to contactInfo.telegram,
        "Instagram" to contactInfo.instagram,
        "Call Us" to if (contactInfo.call.isNotBlank()) "tel:${contactInfo.call}" else "",
        "Email" to if (contactInfo.email.isNotBlank()) "mailto:${contactInfo.email}" else "",
    ).filter { it.second.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contact Support") },
        text = {
            Column {
                contactRows.forEach { (label, url) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { runCatching { uriHandler.openUri(url) } },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
