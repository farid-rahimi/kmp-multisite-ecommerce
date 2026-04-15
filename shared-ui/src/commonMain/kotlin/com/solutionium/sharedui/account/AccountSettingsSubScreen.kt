package com.solutionium.sharedui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.account_settings
import com.solutionium.sharedui.resources.account_delete_confirm_action
import com.solutionium.sharedui.resources.account_delete_confirm_body
import com.solutionium.sharedui.resources.account_delete_confirm_title
import com.solutionium.sharedui.resources.account_delete_data_orders
import com.solutionium.sharedui.resources.account_delete_data_profile
import com.solutionium.sharedui.resources.account_delete_data_saved_items
import com.solutionium.sharedui.resources.account_delete_forgot_password
import com.solutionium.sharedui.resources.account_delete_hold_confirm
import com.solutionium.sharedui.resources.account_delete_hold_hint
import com.solutionium.sharedui.resources.account_delete_irreversible
import com.solutionium.sharedui.resources.account_delete_otp_label
import com.solutionium.sharedui.resources.account_delete_password_label
import com.solutionium.sharedui.resources.account_delete_requesting_otp
import com.solutionium.sharedui.resources.account_delete_send_otp
import com.solutionium.sharedui.resources.account_delete_title
import com.solutionium.sharedui.resources.account_delete_warning
import com.solutionium.sharedui.resources.account_logout_hint
import com.solutionium.sharedui.resources.account_settings_hint
import com.solutionium.sharedui.resources.cancel
import com.solutionium.sharedui.resources.logout
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountSettingsSubScreen(
    modifier: Modifier = Modifier,
    isDeleting: Boolean,
    isRequestingOtp: Boolean,
    otpRequested: Boolean,
    onNavigateBack: () -> Unit,
    onRequestOtp: () -> Unit,
    onDeleteWithPassword: (String) -> Unit,
    onDeleteWithOtp: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var showDeleteWarning by remember { mutableStateOf(false) }
    var showDeleteForm by remember { mutableStateOf(false) }
    var deleteWithOtp by remember(otpRequested) { mutableStateOf(otpRequested) }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var holdHint by remember { mutableStateOf<String?>(null) }
    val holdHintText = stringResource(Res.string.account_delete_hold_hint)
    val titleText = if (showDeleteForm) {
        stringResource(Res.string.account_delete_title)
    } else {
        stringResource(Res.string.account_settings)
    }

    fun resetDeleteFlow() {
        showDeleteWarning = false
        showDeleteForm = false
        deleteWithOtp = false
        password = ""
        otp = ""
        holdHint = null
    }

    LaunchedEffect(otpRequested) {
        if (otpRequested) {
            deleteWithOtp = true
        }
    }

    BackHandler(enabled = showDeleteForm) {
        resetDeleteFlow()
    }

    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            title = { Text(stringResource(Res.string.account_delete_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.account_delete_confirm_body))
                    Text("• ${stringResource(Res.string.account_delete_data_profile)}")
                    Text("• ${stringResource(Res.string.account_delete_data_saved_items)}")
                    Text("• ${stringResource(Res.string.account_delete_data_orders)}")
                    Text(
                        stringResource(Res.string.account_delete_irreversible),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteWarning = false
                        showDeleteForm = true
                    },
                ) {
                    Text(stringResource(Res.string.account_delete_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarning = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PlatformTopBar(
                title = {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                onBack = {
                    if (showDeleteForm) {
                        resetDeleteFlow()
                    } else {
                        onNavigateBack()
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!showDeleteForm) {
                Text(
                    text = stringResource(Res.string.account_settings_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FilledTonalButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(999.dp),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(Res.string.logout),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(Res.string.account_logout_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.account_delete_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(Res.string.account_delete_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                FilledTonalButton(
                    onClick = { showDeleteWarning = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(999.dp),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(Res.string.account_delete_confirm_action),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                if (!deleteWithOtp) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(Res.string.account_delete_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HoldToConfirmButton(
                        text = stringResource(Res.string.account_delete_hold_confirm),
                        onHoldComplete = {
                            holdHint = null
                            onDeleteWithPassword(password)
                        },
                        onTapHint = { holdHint = holdHintText },
                        enabled = password.isNotBlank() && !isDeleting,
                        loading = isDeleting,
                    )
                    TextButton(
                        onClick = {
                            if (!isRequestingOtp) {
                                onRequestOtp()
                            }
                        },
                        enabled = !isRequestingOtp && !isDeleting,
                    ) {
                        if (isRequestingOtp) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text(
                                text = " ${stringResource(Res.string.account_delete_requesting_otp)}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        } else {
                            Text(stringResource(Res.string.account_delete_forgot_password))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = deleteWithOtp,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text(stringResource(Res.string.account_delete_otp_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HoldToConfirmButton(
                            text = stringResource(Res.string.account_delete_send_otp),
                            onHoldComplete = {
                                holdHint = null
                                onDeleteWithOtp(otp)
                            },
                            onTapHint = { holdHint = holdHintText },
                            enabled = otp.isNotBlank() && !isDeleting,
                            loading = isDeleting,
                        )
                    }
                }
            }

            if (!holdHint.isNullOrBlank()) {
                Text(
                    text = holdHint.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HoldToConfirmButton(
    text: String,
    onHoldComplete: () -> Unit,
    onTapHint: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var progress by remember { mutableStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed, enabled, loading) {
        if (!enabled || loading) {
            progress = 0f
            completed = false
            return@LaunchedEffect
        }

        if (isPressed) {
            completed = false
            progress = 0f
            val steps = 24
            val delayMillis = 50L
            repeat(steps) { idx ->
                if (!isPressed || !enabled || loading) {
                    progress = 0f
                    return@LaunchedEffect
                }
                progress = (idx + 1f) / steps.toFloat()
                delay(delayMillis)
            }
            if (!completed) {
                completed = true
                progress = 1f
                onHoldComplete()
                progress = 0f
            }
        } else {
            progress = 0f
            completed = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            )
            TextButton(
                onClick = onTapHint,
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                } else {
                    Text(text = text, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
