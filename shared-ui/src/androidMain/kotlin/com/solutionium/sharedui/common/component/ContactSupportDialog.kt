package com.solutionium.sharedui.common.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.solutionium.core.ui.common.R
import com.solutionium.shared.data.model.ContactInfo

@Composable
fun ContactSupportDialog(
    contactInfo: ContactInfo?,
    onDismiss: () -> Unit
) {
    if (contactInfo == null) return

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.contact_support)) },
        text = {
            Column {

                if (contactInfo.whatsapp.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    ContactRow(
                        text = stringResource(R.string.whatsapp),
                        icon = Icons.AutoMirrored.Filled.Chat,

                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                contactInfo.whatsapp.toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }
                if (contactInfo.telegram.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    ContactRow(
                        text = stringResource(R.string.telegram),
                        //icon = ImageVector.vectorResource(R.drawable.ic_telegram), // Add this drawable
                        icon = Icons.AutoMirrored.Filled.Chat,

                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                contactInfo.telegram.toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }

                if (contactInfo.instagram.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    ContactRow(
                        text = stringResource(R.string.instagram),
                        //icon = ImageVector.vectorResource(R.drawable.ic_instagram), // Add this drawable
                        icon = Icons.AutoMirrored.Filled.Chat,

                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                contactInfo.instagram.toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }

                if (contactInfo.call.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    ContactRow(
                        text = stringResource(R.string.call_us),
                        icon = Icons.Default.Phone,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                "tel:${contactInfo.call}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }

                if (contactInfo.email.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    ContactRow(
                        text = stringResource(R.string.email),
                        icon = Icons.Default.Email,
                        onClick = {
                            val intent =
                                Intent(Intent.ACTION_SENDTO, "mailto:${contactInfo.email}".toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }
        },
        confirmButton = {}, // No confirm button needed
//        dismissButton = {
//
//            TextButton(onClick = onDismiss) {
//                Text(stringResource(R.string.back))
//            }
//
//        }

    )
}

@Composable
private fun ContactRow(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null)
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}