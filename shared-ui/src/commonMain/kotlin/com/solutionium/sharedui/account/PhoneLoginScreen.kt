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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.shared.util.PhoneNumberFormatter
import com.solutionium.shared.viewmodel.AccountMessageType
import com.solutionium.shared.viewmodel.PasswordResetStage
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.back
import com.solutionium.sharedui.resources.close
import com.solutionium.sharedui.resources.contact_support
import com.solutionium.sharedui.resources.country_code
import com.solutionium.sharedui.resources.create_account
import com.solutionium.sharedui.resources.email
import com.solutionium.sharedui.resources.enter_otp
import com.solutionium.sharedui.resources.forgot_password
import com.solutionium.sharedui.resources.full_name
import com.solutionium.sharedui.resources.language_menu
import com.solutionium.sharedui.resources.login
import com.solutionium.sharedui.resources.login_identifier
import com.solutionium.sharedui.resources.login_or_signup
import com.solutionium.sharedui.resources.login_with_otp_instead
import com.solutionium.sharedui.resources.login_with_password
import com.solutionium.sharedui.resources.password
import com.solutionium.sharedui.resources.phone_number
import com.solutionium.sharedui.resources.phone_number_optional
import com.solutionium.sharedui.resources.privacy_policy
import com.solutionium.sharedui.resources.request_otp
import com.solutionium.sharedui.resources.reset_password
import com.solutionium.sharedui.resources.signup
import com.solutionium.sharedui.resources.verification_code_sent_to_email
import com.solutionium.sharedui.resources.verify_otp
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
    signupEmailOtpStage: PasswordResetStage,
    onRequestSignupEmailOtp: (String) -> Unit,
    onVerifySignupEmailOtp: (String, String) -> Unit,
    onResetSignupEmailVerification: () -> Unit,
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
            PlatformTopBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
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
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            if (showPrivacyDialog) {
                PrivacyPolicyDialog(content = privacyPolicyContent, onDismiss = { showPrivacyDialog = false })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(if (brand == WooBrand.SiteB) Alignment.TopCenter else Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(top = if (brand == WooBrand.SiteB) 24.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (brand == WooBrand.SiteB) Arrangement.Top else Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                val authPanelColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                    ErrorMessageCard(
                        message = errorMessage.orEmpty(),
                        messageType = messageType ?: AccountMessageType.Error,
                        onDismiss = onDismissError,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                AuthModeTabs(
                    modifier = Modifier.fillMaxWidth(),
                    selectedLeft = when (brand) {
                        WooBrand.SiteA -> loginWithPassword
                        WooBrand.SiteB -> siteBAuthMode == SiteBAuthMode.Login
                    },
                    leftText = stringResource(Res.string.login),
                    rightText = stringResource(Res.string.signup),
                    selectedColor = authPanelColor,
                    unselectedColor = MaterialTheme.colorScheme.background,
                    onLeftClick = {
                        when (brand) {
                            WooBrand.SiteA -> loginWithPassword = true
                            WooBrand.SiteB -> {
                                siteBAuthMode = SiteBAuthMode.Login
                                onResetSignupEmailVerification()
                            }
                        }
                    },
                    onRightClick = {
                        when (brand) {
                            WooBrand.SiteA -> loginWithPassword = false
                            WooBrand.SiteB -> {
                                siteBAuthMode = SiteBAuthMode.Signup
                                onCancelPasswordReset()
                                onResetSignupEmailVerification()
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = authPanelColor,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                            AnimatedVisibility(
                                visible = brand == WooBrand.SiteA && !loginWithPassword,
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
                                visible = brand == WooBrand.SiteB && siteBAuthMode == SiteBAuthMode.Signup,
                                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(),
                            ) {
                                SignupInputSection(
                                    isLoading = isLoading,
                                    otpStage = signupEmailOtpStage,
                                    onRequestEmailOtp = onRequestSignupEmailOtp,
                                    onVerifyEmailOtp = onVerifySignupEmailOtp,
                                    onResetEmailVerification = onResetSignupEmailVerification,
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
                    }
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
        }
    }
}

@Composable
private fun AuthModeTabs(
    leftText: String,
    rightText: String,
    selectedLeft: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    unselectedColor: androidx.compose.ui.graphics.Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Button(
            onClick = onLeftClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLeft) selectedColor else unselectedColor,
                contentColor = if (selectedLeft) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            ),
            elevation = null,
        ) {
            Text(
                text = leftText,
                fontWeight = if (selectedLeft) FontWeight.Bold else FontWeight.Normal,
            )
        }
        Button(
            onClick = onRightClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!selectedLeft) selectedColor else unselectedColor,
                contentColor = if (!selectedLeft) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            ),
            elevation = null,
        ) {
            Text(
                text = rightText,
                fontWeight = if (!selectedLeft) FontWeight.Bold else FontWeight.Normal,
            )
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
    otpStage: PasswordResetStage,
    onRequestEmailOtp: (String) -> Unit,
    onVerifyEmailOtp: (String, String) -> Unit,
    onResetEmailVerification: () -> Unit,
    onSignup: (name: String, email: String, phone: String, password: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+971") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var signupStep by remember { mutableStateOf(SiteBSignupStep.Form) }
    var accountCreationTriggered by remember { mutableStateOf(false) }
    var awaitingOtpSend by remember { mutableStateOf(false) }

    val hasPhone = phone.isNotBlank()
    val normalizedPhone = if (hasPhone) PhoneNumberFormatter.normalize(countryCode, phone) else ""
    val isPhoneValid = !hasPhone || PhoneNumberFormatter.isValid(countryCode, phone)

    LaunchedEffect(otpStage, signupStep, isLoading) {
        if (signupStep == SiteBSignupStep.Otp && otpStage == PasswordResetStage.OtpVerified && !accountCreationTriggered && !isLoading) {
            accountCreationTriggered = true
            onSignup(name, email, normalizedPhone, password)
        }
    }
    LaunchedEffect(otpStage, awaitingOtpSend, isLoading) {
        if (awaitingOtpSend && otpStage == PasswordResetStage.OtpSent && !isLoading) {
            signupStep = SiteBSignupStep.Otp
            awaitingOtpSend = false
        } else if (awaitingOtpSend && !isLoading && otpStage != PasswordResetStage.OtpSent) {
            // Request finished but OTP was not sent (likely API error): stay on form.
            awaitingOtpSend = false
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (signupStep) {
            SiteBSignupStep.Form -> {
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
                Button(
                    onClick = {
                        onResetEmailVerification()
                        otp = ""
                        accountCreationTriggered = false
                        awaitingOtpSend = true
                        onRequestEmailOtp(email)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        isPhoneValid &&
                        password.isNotBlank() &&
                        !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(Res.string.create_account))
                }
            }

            SiteBSignupStep.Otp -> {
                Text(
                    text = stringResource(Res.string.verification_code_sent_to_email),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(Res.string.enter_otp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVerifyEmailOtp(email, otp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && otp.length == 4 && !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text(stringResource(Res.string.verify_otp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        signupStep = SiteBSignupStep.Form
                        onResetEmailVerification()
                        otp = ""
                        accountCreationTriggered = false
                        awaitingOtpSend = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.back))
                }
            }
        }
    }
}

private enum class SiteBSignupStep {
    Form,
    Otp,
}

private enum class SiteBAuthMode {
    Login,
    Signup,
}
