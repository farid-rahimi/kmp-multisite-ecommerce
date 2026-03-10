package com.solutionium.sharedui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.enter_otp
import com.solutionium.sharedui.resources.otp_sent_to
import com.solutionium.sharedui.resources.resend_otp
import com.solutionium.sharedui.resources.resend_otp_in_s
import com.solutionium.sharedui.resources.submit
import com.solutionium.sharedui.resources.verify_otp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    phoneNumber: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onRequestNewOtp: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var remainingTime by remember { mutableIntStateOf(60) }
    val canResend by remember(remainingTime) { derivedStateOf { remainingTime == 0 } }

    LaunchedEffect(isLoading) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime--
        }
    }

    LaunchedEffect(otp) {
        if (otp.length == 4) {
            onVerifyOtp()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.verify_otp)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(Res.string.otp_sent_to, phoneNumber),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = onOtpChange,
                label = { Text(stringResource(Res.string.enter_otp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onVerifyOtp,
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 4 && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(Res.string.submit))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    remainingTime = 60
                    onRequestNewOtp()
                },
                enabled = canResend && !isLoading,
            ) {
                Text(
                    if (canResend) {
                        stringResource(Res.string.resend_otp)
                    } else {
                        stringResource(Res.string.resend_otp_in_s, remainingTime)
                    },
                )
            }
        }
    }
}
