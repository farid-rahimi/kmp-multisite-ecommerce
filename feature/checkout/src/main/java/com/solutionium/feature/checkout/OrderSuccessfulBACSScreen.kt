package com.solutionium.feature.checkout

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.solutionium.sharedui.common.component.FormattedPriceV2
import com.solutionium.shared.data.model.BACSDetails
import java.net.URLEncoder

// In a new file, e.g., OrderSuccessfulScreen.kt
@Composable
fun OrderSuccessfulBACSScreen(
    orderId: Int,
    orderTotal: String,
    bacsDetails: BACSDetails?,
    onContinueShopping: () -> Unit
) {

    val bankAccountNumber = bacsDetails?.cardNumber ?: ""
    val whatsappNumber = bacsDetails?.contactNumber ?: "" // Use international format
    val ibanNumber = bacsDetails?.ibanNumber ?: ""
    val accountHolder = bacsDetails?.accountHolder ?: ""

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Make the screen scrollable,
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center
    ) {
        // You can replace this with a Lottie animation of a checkmark
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Action Required",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.order_on_hold),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.bacs_instruction_message),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Order Details Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.bacs_bank_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Account Number with Copy Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    SelectionContainer{
                        Text(
                            text = bankAccountNumber,
                            letterSpacing = 3.sp,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            //fontWeight = FontWeight.Monospace
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Account Number",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(bankAccountNumber))
                            // Optional: Show a toast message
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.bacs_iban_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer {
                        Text(
                            text = ibanNumber,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp),
                            //fontWeight = FontWeight.Monospace
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Account Number",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(ibanNumber))
                            // Optional: Show a toast message
                        }
                    )
                }
                // Important Note
                Text(
                    text = stringResource(R.string.bacs_reference_note, accountHolder),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // --- Order Summary ---
        OrderSummary(orderId, orderTotal)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Action Buttons ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // WhatsApp Button
            val whatsappMessage = stringResource(R.string.whatsapp_message, orderId)
            Button(
                onClick = {
                    val encodedMessage = URLEncoder.encode(whatsappMessage, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "https://api.whatsapp.com/send?phone=$whatsappNumber&text=$encodedMessage".toUri()
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Whatsapp, contentDescription = "WhatsApp")
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.contact_via_whatsapp))
            }
            // Continue Shopping Button
            OutlinedButton(
                onClick = onContinueShopping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.continue_shopping))
            }
        }
    }
}

@Composable
private fun OrderSummary(orderId: Int, orderTotal: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.order_number), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "$orderId",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.order_total), style = MaterialTheme.typography.bodyLarge)
                FormattedPriceV2(orderTotal.toLong())
            }
        }
    }
}