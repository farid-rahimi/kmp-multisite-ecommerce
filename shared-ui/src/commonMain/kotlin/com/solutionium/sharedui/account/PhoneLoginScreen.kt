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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.close
import com.solutionium.sharedui.resources.contact_support
import com.solutionium.sharedui.resources.create_account
import com.solutionium.sharedui.resources.country_code
import com.solutionium.sharedui.resources.email
import com.solutionium.sharedui.resources.forgot_password
import com.solutionium.sharedui.resources.full_name
import com.solutionium.sharedui.resources.language_menu
import com.solutionium.sharedui.resources.login
import com.solutionium.sharedui.resources.login_or_signup
import com.solutionium.sharedui.resources.login_with_otp_instead
import com.solutionium.sharedui.resources.login_with_password
import com.solutionium.sharedui.resources.login_identifier
import com.solutionium.sharedui.resources.phone_number_optional
import com.solutionium.sharedui.resources.reset_password
import com.solutionium.sharedui.resources.password
import com.solutionium.sharedui.resources.phone_number
import com.solutionium.sharedui.resources.privacy_policy
import com.solutionium.sharedui.resources.request_otp
import com.solutionium.sharedui.resources.signup
import com.solutionium.sharedui.resources.username
import com.solutionium.shared.viewmodel.AccountMessageType
import com.solutionium.sharedui.resources.verify_otp
import com.solutionium.shared.viewmodel.PasswordResetStage
import com.solutionium.shared.util.PhoneNumberFormatter
import com.solutionium.sharedui.resources.back
import com.solutionium.sharedui.resources.enter_otp
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(
    modifier: Modifier = Modifier,
    brand: WooBrand,
    phoneNumber: String,
    username: String,
    isLoading: Boolean = false,
    onPhoneNumberChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onPasswordLogin: () -> Unit,
    onPasswordSignup: (name: String, email: String, phone: String, password: String) -> Unit,
    passwordResetStage: PasswordResetStage,
    passwordResetEmail: String,
    passwordResetOtp: String,
    onRequestPasswordResetOtp: (String) -> Unit,
    onVerifyPasswordResetOtp: (String, String) -> Unit,
    onResetPasswordByOtp: (String, String, String) -> Unit,
    onCancelPasswordReset: () -> Unit,
    onStartPasswordReset: () -> Unit,
    errorMessage: String?,
    messageType: AccountMessageType?,
    onDismissError: () -> Unit,
    privacyPolicyContent: String,
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onSupportClick: () -> Unit,
) {
    var loginWithPassword by remember { mutableStateOf(false) }
    var siteBAuthMode by remember { mutableStateOf(SiteBAuthMode.Login) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val screenTitle = when {
        brand == WooBrand.SiteB && passwordResetStage != PasswordResetStage.Idle -> stringResource(Res.string.forgot_password)
        brand == WooBrand.SiteA && loginWithPassword -> stringResource(Res.string.login)
        brand == WooBrand.SiteB && siteBAuthMode == SiteBAuthMode.Login -> stringResource(Res.string.login)
        brand == WooBrand.SiteB && siteBAuthMode == SiteBAuthMode.Signup -> stringResource(Res.string.create_account)
        else -> stringResource(Res.string.login_or_signup)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.language_menu)) },
                            onClick = {
                                menuExpanded = false
                                onLanguageClick()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.contact_support)) },
                            onClick = {
                                menuExpanded = false
                                onSupportClick()
                            },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
        ) {
            if (showPrivacyDialog) {
                PrivacyPolicyDialog(content = privacyPolicyContent, onDismiss = { showPrivacyDialog = false })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(if (brand == WooBrand.SiteB) Alignment.TopCenter else Alignment.Center)
                    .padding(top = if (brand == WooBrand.SiteB) 24.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (brand == WooBrand.SiteB) Arrangement.Top else Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                    ErrorMessageCard(
                        message = errorMessage.orEmpty(),
                        messageType = messageType ?: AccountMessageType.Error,
                        onDismiss = onDismissError,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

            if (brand == WooBrand.SiteA) {
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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { siteBAuthMode = SiteBAuthMode.Login },
                        modifier = Modifier.weight(1f),
                        colors = if (siteBAuthMode == SiteBAuthMode.Login) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    ) {
                        Text(stringResource(Res.string.login))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            siteBAuthMode = SiteBAuthMode.Signup
                            onCancelPasswordReset()
                        },
                        modifier = Modifier.weight(1f),
                        colors = if (siteBAuthMode == SiteBAuthMode.Signup) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    ) {
                        Text(stringResource(Res.string.signup))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            AnimatedVisibility(
                visible = brand == WooBrand.SiteB && siteBAuthMode == SiteBAuthMode.Signup,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
            ) {
                SignupInputSection(
                    isLoading = isLoading,
                    onSignup = onPasswordSignup,
                )
            }

            AnimatedVisibility(
                visible = (brand == WooBrand.SiteA && loginWithPassword) ||
                    (brand == WooBrand.SiteB && siteBAuthMode == SiteBAuthMode.Login && passwordResetStage == PasswordResetStage.Idle),
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
            ) {
                PasswordInputSection(
                    username = username,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordLogin = onPasswordLogin,
                    isLoading = isLoading,
                    showForgotPassword = brand == WooBrand.SiteB,
                    onForgotPasswordClick = onStartPasswordReset,
                )
            }

            AnimatedVisibility(
                visible = brand == WooBrand.SiteB &&
                    siteBAuthMode == SiteBAuthMode.Login &&
                    passwordResetStage != PasswordResetStage.Idle,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
            ) {
                ForgotPasswordSection(
                    stage = passwordResetStage,
                    prefEmail = passwordResetEmail,
                    prefOtp = passwordResetOtp,
                    isLoading = isLoading,
                    onRequestOtp = onRequestPasswordResetOtp,
                    onVerifyOtp = onVerifyPasswordResetOtp,
                    onResetPassword = onResetPasswordByOtp,
                    onCancel = onCancelPasswordReset,
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

            if (brand == WooBrand.SiteA) {
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
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    messageType: AccountMessageType,
    onDismiss: () -> Unit,
) {
    val containerColor = when (messageType) {
        AccountMessageType.Error -> MaterialTheme.colorScheme.errorContainer
        AccountMessageType.Success -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when (messageType) {
        AccountMessageType.Error -> MaterialTheme.colorScheme.onErrorContainer
        AccountMessageType.Success -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (messageType == AccountMessageType.Success) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = if (messageType == AccountMessageType.Success) "Success" else "Error",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
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
    showForgotPassword: Boolean,
    onForgotPasswordClick: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(Res.string.login_identifier)) },
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
        if (showForgotPassword) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onForgotPasswordClick) {
                Text(stringResource(Res.string.forgot_password))
            }
        }
    }
}

@Composable
private fun ForgotPasswordSection(
    stage: PasswordResetStage,
    prefEmail: String,
    prefOtp: String,
    isLoading: Boolean,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onResetPassword: (String, String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var email by remember(prefEmail) { mutableStateOf(prefEmail) }
    var otp by remember(prefOtp) { mutableStateOf(prefOtp) }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = stage == PasswordResetStage.EmailInput,
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (stage) {
            PasswordResetStage.EmailInput -> {
                Button(
                    onClick = { onRequestOtp(email) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(Res.string.request_otp))
                }
            }

            PasswordResetStage.OtpSent -> {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text(stringResource(Res.string.enter_otp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVerifyOtp(email, otp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && otp.isNotBlank() && !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(Res.string.verify_otp))
                }
            }

            PasswordResetStage.OtpVerified -> {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(Res.string.password)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "New Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onResetPassword(email, otp, newPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && otp.isNotBlank() && newPassword.isNotBlank() && !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(Res.string.reset_password))
                }
            }

            PasswordResetStage.Idle -> Unit
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCancel) {
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
private fun SignupInputSection(
    isLoading: Boolean,
    onSignup: (name: String, email: String, phone: String, password: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+971") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(Res.string.full_name)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = countryCode,
                onValueChange = {
                    countryCode = PhoneNumberFormatter.sanitizeCountryCode(it)
                    phone = phone.take(PhoneNumberFormatter.maxLocalInputDigits(countryCode))
                },
                label = { Text(stringResource(Res.string.country_code)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(0.35f),
                singleLine = true,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter(Char::isDigit).take(PhoneNumberFormatter.maxLocalInputDigits(countryCode))
                },
                label = { Text(stringResource(Res.string.phone_number_optional)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") },
                modifier = Modifier.weight(0.65f),
                singleLine = true,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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
        val normalizedPhone = PhoneNumberFormatter.normalize(countryCode, phone)
        Button(
            onClick = {
                onSignup(name, email, normalizedPhone, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() &&
                email.isNotBlank() &&
                countryCode.isNotBlank() &&
                phone.isNotBlank() &&
                PhoneNumberFormatter.isCanonical(normalizedPhone) &&
                password.isNotBlank() &&
                !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(Res.string.create_account))
            }
        }
    }
}

private enum class SiteBAuthMode {
    Login,
    Signup,
}
