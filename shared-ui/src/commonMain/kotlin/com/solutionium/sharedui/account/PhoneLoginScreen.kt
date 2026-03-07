package com.solutionium.sharedui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.close
import com.solutionium.sharedui.resources.login
import com.solutionium.sharedui.resources.login_or_signup
import com.solutionium.sharedui.resources.login_with_otp_instead
import com.solutionium.sharedui.resources.login_with_password
import com.solutionium.sharedui.resources.password
import com.solutionium.sharedui.resources.phone_number
import com.solutionium.sharedui.resources.privacy_policy
import com.solutionium.sharedui.resources.request_otp
import com.solutionium.sharedui.resources.username
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneLoginScreen(
    modifier: Modifier = Modifier,
    phoneNumber: String,
    username: String,
    isLoading: Boolean = false,
    onPhoneNumberChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onPasswordLogin: () -> Unit,
    privacyPolicyContent: String,
) {
    var loginWithPassword by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        if (showPrivacyDialog) {
            PrivacyPolicyDialog(content = privacyPolicyContent, onDismiss = { showPrivacyDialog = false })
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                if (loginWithPassword) stringResource(Res.string.login) else stringResource(Res.string.login_or_signup),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = !loginWithPassword,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
            ) {
                PhoneInputSection(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = onPhoneNumberChange,
                    onRequestOtp = onRequestOtp,
                    isLoading = isLoading,
                )
            }

            AnimatedVisibility(
                visible = loginWithPassword,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
            ) {
                PasswordInputSection(
                    username = username,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordLogin = onPasswordLogin,
                    isLoading = isLoading,
                )
            }

            TextButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.privacy_policy),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        TextButton(
            onClick = { loginWithPassword = !loginWithPassword },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            val text = if (loginWithPassword) {
                stringResource(Res.string.login_with_otp_instead)
            } else {
                stringResource(Res.string.login_with_password)
            }
            Text(text)
        }
    }
}

@Composable
fun PrivacyPolicyDialog(content: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.privacy_policy))
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        },
    )
}

@Composable
private fun PhoneInputSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    isLoading: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text(stringResource(Res.string.phone_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.length >= 11 && !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(Res.string.request_otp))
            }
        }
    }
}

@Composable
private fun PasswordInputSection(
    username: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordLogin: () -> Unit,
    isLoading: Boolean,
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(Res.string.username)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                onPasswordChange(it)
            },
            label = { Text(stringResource(Res.string.password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onPasswordLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(Res.string.login))
            }
        }
    }
}
