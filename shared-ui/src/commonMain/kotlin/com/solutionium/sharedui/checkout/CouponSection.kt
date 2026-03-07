package com.solutionium.sharedui.checkout

import com.solutionium.sharedui.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.solutionium.shared.data.model.Coupon
import kotlin.text.isNotBlank
import kotlin.text.uppercase

@Composable
fun CouponSection(
    appliedCoupons: List<Coupon>,
    isLoading: Boolean,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: (Coupon) -> Unit,
    hasError: Boolean = false,
    errorText: @Composable () -> Unit,
) {
    var couponCode by remember { mutableStateOf("") }

    Column {
        SectionTitle(stringResource(Res.string.discount_section_title))

        // Input field for applying a new coupon
        OutlinedTextField(
            value = couponCode,
            onValueChange = { couponCode = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.enter_coupon_code)) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            isError = hasError,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (couponCode.isNotBlank()) {
                        onApplyCoupon(couponCode)
                        // Clear the field after applying
                        couponCode = ""
                    }
                } // Call the apply action when the keyboard "Go" is pressed
            ),
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = {
                            if (couponCode.isNotBlank()) {
                                onApplyCoupon(couponCode)
                                // Clear the field after applying
                                couponCode = ""
                            }
                        },
                        enabled = couponCode.isNotBlank()
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Apply Coupon")
                    }
                }
            }
        )

        errorText()
        // Display error message if any
//        if (error != null) {
//            Text(
//                text = error,
//                color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }

        // List of successfully applied coupons
        if (appliedCoupons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            appliedCoupons.forEach { coupon ->
                AppliedCouponChip(coupon = coupon, onRemove = { onRemoveCoupon(coupon) })
            }
        }
    }
}

@Composable
fun AppliedCouponChip(coupon: Coupon, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = coupon.code.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Icon(
            imageVector = Icons.Filled.Cancel,
            contentDescription = "Remove coupon ${coupon.code}",
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onRemove),
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}
