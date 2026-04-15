package com.solutionium.sharedui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.sharedui.common.component.FormattedPriceV3
import com.solutionium.sharedui.common.component.LanguageSelectionScreen
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.sharedui.common.component.platformPrimaryButtonShape
import com.solutionium.sharedui.common.component.platformUsesCupertinoChrome
import com.solutionium.sharedui.designsystem.theme.LocalWooBrand
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.home.PlatformContactSupportDialog
import com.solutionium.sharedui.orders.OrderSummaryCard
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.account_settings
import com.solutionium.sharedui.resources.cancel
import com.solutionium.sharedui.resources.contact_support
import com.solutionium.sharedui.resources.credit_prefix
import com.solutionium.sharedui.resources.debit_prefix
import com.solutionium.sharedui.resources.edit_profile
import com.solutionium.sharedui.resources.feature_account_title
import com.solutionium.sharedui.resources.latest_order_title
import com.solutionium.sharedui.resources.language_menu
import com.solutionium.sharedui.resources.logout
import com.solutionium.sharedui.resources.logout_confirmation_message
import com.solutionium.sharedui.resources.logout_confirmation_title
import com.solutionium.sharedui.resources.manage_addresses
import com.solutionium.sharedui.resources.my_favorites
import com.solutionium.sharedui.resources.no_orders_yet
import com.solutionium.sharedui.resources.no_transactions_yet
import com.solutionium.sharedui.resources.order_history
import com.solutionium.sharedui.resources.wallet_balance_title
import com.solutionium.sharedui.resources.wallet_history
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.Transaction
import com.solutionium.shared.data.model.Type
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.viewmodel.AccountStage
import com.solutionium.shared.viewmodel.AccountViewModel
import com.solutionium.shared.viewmodel.PasswordResetStage
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun AccountScreen(
    onAddressClick: () -> Unit,
    onFavoriteClick: (title: String, ids: String) -> Unit,
    onOrdersClick: () -> Unit,
    onOrderClick: (orderId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    onBack: () -> Unit,
    clearOnDispose: Boolean = true,
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val brand = LocalWooBrand.current

    val hasInnerAccountStage =
        state.stage != AccountStage.LoggedIn && state.stage != AccountStage.LoggedOut
    val hasPasswordResetFlow =
        state.stage == AccountStage.LoggedOut && state.passwordResetStage != PasswordResetStage.Idle
    val hideOuterTopBar = state.stage == AccountStage.LoggedOut ||
        state.stage == AccountStage.OtpVerification ||
        state.stage == AccountStage.EditProfile ||
        state.stage == AccountStage.NewUserDetailsInput ||
        state.stage == AccountStage.AccountSettings

    BackHandler(enabled = hasInnerAccountStage || hasPasswordResetFlow) {
        when {
            hasPasswordResetFlow -> viewModel.cancelPasswordReset()
            else -> viewModel.onNavigateBack(onBack)
        }
    }

    DisposableEffect(viewModel, clearOnDispose) {
        onDispose {
            if (clearOnDispose) viewModel.clear()
        }
    }

    if (state.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onLogoutDismissed,
            title = { Text(stringResource(Res.string.logout_confirmation_title)) },
            text = { Text(stringResource(Res.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onLogoutConfirmed) {
                    Text(stringResource(Res.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onLogoutDismissed) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    if (state.showContactSupportDialog) {
        PlatformContactSupportDialog(
            contactInfo = state.contactInfo,
            onDismiss = { viewModel.dismissContactSupport() },
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        if (state.message != null && state.stage != AccountStage.LoggedOut) {
            snackbarHostState.showSnackbar(
                message = state.message ?: "",
                duration = SnackbarDuration.Short,
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!hideOuterTopBar) {
                PlatformTopBar(
                    title = {
                        if (state.stage != AccountStage.ChangeLanguage && state.stage != AccountStage.LoggedOut) {
                            Text(
                                text = stringResource(Res.string.feature_account_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    },
                    actions = {
                        if (state.stage != AccountStage.ChangeLanguage) {
                            var menuExpanded by remember { mutableStateOf(false) }

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
                                        viewModel.onNavigateToLanguage()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.contact_support)) },
                                    onClick = {
                                        viewModel.showContactSupport()
                                        menuExpanded = false
                                    },
                                )
                                if (state.stage == AccountStage.LoggedIn) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.account_settings)) },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.onNavigateToAccountSettings()
                                        },
                                    )
                                }
                            }
                        }
                    },
                    onBack = if (state.stage == AccountStage.ChangeLanguage) {
                        { viewModel.onNavigateBack(onBack) }
                    } else {
                        null
                    },
                )
            }

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingValues(0.dp)),
                contentAlignment = Alignment.Center,
            ) {
                when (state.stage) {
                    AccountStage.LoggedOut -> PhoneLoginScreen(
                        modifier = Modifier.fillMaxSize(),
                        brand = brand,
                        phoneNumber = state.phoneNumber ?: "",
                        username = state.username,
                        isLoading = state.isLoading,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        onUsernameChange = viewModel::onUsernameChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onRequestOtp = viewModel::requestOtp,
                        onPasswordLogin = viewModel::loginWithPassword,
                        onPasswordSignup = { name, email, phone, password ->
                            viewModel.signupWithPassword(
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                requireEmailOtp = brand == WooBrand.SiteB,
                            )
                        },
                        signupEmailOtpStage = state.signupEmailOtpStage,
                        onRequestSignupEmailOtp = viewModel::requestSignupEmailOtp,
                        onVerifySignupEmailOtp = viewModel::verifySignupEmailOtp,
                        onResetSignupEmailVerification = viewModel::resetSignupEmailVerification,
                        passwordResetStage = state.passwordResetStage,
                        passwordResetEmail = state.passwordResetEmail,
                        passwordResetOtp = state.passwordResetOtp,
                        onRequestPasswordResetOtp = viewModel::requestPasswordResetOtp,
                        onVerifyPasswordResetOtp = viewModel::verifyPasswordResetOtp,
                        onResetPasswordByOtp = viewModel::resetPasswordByOtp,
                        onCancelPasswordReset = viewModel::cancelPasswordReset,
                        onStartPasswordReset = viewModel::startPasswordReset,
                        errorMessage = state.message,
                        messageType = state.messageType,
                        onDismissError = viewModel::clearMessage,
                        privacyPolicyContent = state.privacyPolicy,
                        onLanguageClick = viewModel::onNavigateToLanguage,
                        onSupportClick = viewModel::showContactSupport,
                    )

                    AccountStage.OtpVerification -> OtpVerificationScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        phoneNumber = state.phoneNumber ?: "",
                        otp = state.otp ?: "",
                        onOtpChange = viewModel::onOtpChange,
                        onVerifyOtp = viewModel::verifyOtp,
                        onRequestNewOtp = { viewModel.requestOtp() },
                        onNavigateBack = { viewModel.onNavigateBack(onBack) },
                    )

                    AccountStage.NewUserDetailsInput -> EditProfileSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        isNewUser = true,
                        userDetails = state.userDetails ?: UserDetails(),
                        onNavigateBack = { viewModel.onNavigateBack(onBack) },
                        onSaveChanges = viewModel::submitNewUserDetails,
                        validationErrors = state.validationErrors,
                    )

                    AccountStage.LoggedIn -> UserAccountScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        user = state.userDetails,
                        showWallet = state.walletEnabled,
                        walletBalance = state.userWallet?.balance ?: 0.0,
                        isLoadingWalletBalance = state.isLoadingWallet,
                        onWalletClick = viewModel::onNavigateToWalletHistory,
                        latestOrder = state.latestOrder,
                        isLoadingLatestOrder = state.isLoadingLatestOrder,
                        onOrderClick = onOrderClick,
                        onEditProfile = viewModel::onNavigateToEditProfile,
                        onManageAddresses = onAddressClick,
                        onFavoriteClick = { title ->
                            viewModel.onMyFavoritesClicked {
                                onFavoriteClick(title, it)
                            }
                        },
                        onOrdersClick = onOrdersClick,
                    )

                    AccountStage.EditProfile -> EditProfileSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        userDetails = state.userDetails ?: UserDetails(),
                        onNavigateBack = viewModel::onNavigateBackToAccount,
                        onSaveChanges = viewModel::submitNewUserDetails,
                        validationErrors = state.validationErrors,
                    )

                    AccountStage.ViewWalletTransactions -> ViewWalletTransactionsSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        userWallet = state.userWallet,
                        onNavigateBack = viewModel::onNavigateBackToAccount,
                    )

                    AccountStage.AccountSettings -> AccountSettingsSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isDeleting = state.isDeletingAccount,
                        isRequestingOtp = state.isRequestingOtp,
                        otpRequested = state.deleteAccountOtpRequested,
                        onNavigateBack = viewModel::onNavigateBackToAccount,
                        onRequestOtp = viewModel::requestDeleteAccountOtp,
                        onDeleteWithPassword = viewModel::deleteAccountWithPassword,
                        onDeleteWithOtp = viewModel::deleteAccountWithOtp,
                        onLogout = viewModel::onLogoutClicked,
                    )

                    AccountStage.ChangeLanguage -> LanguageSelectionScreen(
                        currentLang = state.currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                    )

                    AccountStage.Error -> Unit
                }
            }
        }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (platformUsesCupertinoChrome()) 92.dp else 12.dp),
        )
    }
}

@Composable
fun UserAccountScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    user: UserDetails?,
    showWallet: Boolean = false,
    walletBalance: Double,
    isLoadingWalletBalance: Boolean = false,
    onWalletClick: () -> Unit,
    latestOrder: Order?,
    isLoadingLatestOrder: Boolean = false,
    onOrderClick: (orderId: Int) -> Unit,
    onEditProfile: () -> Unit,
    onManageAddresses: () -> Unit,
    onFavoriteClick: (favoriteTitle: String) -> Unit,
    onOrdersClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AccountProfileCard(user = user)
        }

        if (showWallet) {
            item {
                WalletBalanceCard(
                    balance = walletBalance,
                    isLoading = isLoadingWalletBalance,
                    onWalletClick = onWalletClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Text(
                stringResource(Res.string.latest_order_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
            )
        }

        item {
            if (isLoadingLatestOrder) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (latestOrder == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardDefaults.shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = stringResource(Res.string.no_orders_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                OrderSummaryCard(
                    order = latestOrder,
                    onClick = { onOrderClick(latestOrder.id) },
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AccountActionItem(
                        text = stringResource(Res.string.order_history),
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        onClick = onOrdersClick,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    AccountActionItem(
                        text = stringResource(Res.string.manage_addresses),
                        icon = Icons.Default.LocationOn,
                        onClick = onManageAddresses,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    val favoriteTitle: String = stringResource(Res.string.my_favorites)
                    AccountActionItem(
                        text = favoriteTitle,
                        icon = Icons.Default.Favorite,
                        onClick = { onFavoriteClick(favoriteTitle) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                    AccountActionItem(
                        text = stringResource(Res.string.edit_profile),
                        icon = Icons.Default.Person,
                        onClick = onEditProfile,
                    )
                }
            }
        }

    }
}

@Composable
private fun AccountProfileCard(user: UserDetails?) {
    val primaryName = when {
        !user?.displayName.isNullOrBlank() -> user?.displayName.orEmpty()
        else -> listOf(user?.firstName.orEmpty(), user?.lastName.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }.ifBlank { "-" }
    val secondaryInfo = user?.email
        ?.takeIf { it.isNotBlank() }
        ?: user?.phoneNumber
            ?.takeIf { it.isNotBlank() }
            ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = primaryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryInfo.isNotBlank()) {
                    Text(
                        text = secondaryInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun WalletBalanceCard(
    balance: Double,
    isLoading: Boolean = false,
    onWalletClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onWalletClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    stringResource(Res.string.wallet_balance_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    FormattedPriceV3(
                        amount = balance,
                        mainStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        smallDigitsSpanStyle = MaterialTheme.typography.titleMedium.toSpanStyle().copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun AccountActionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWalletTransactionsSubScreen(
    modifier: Modifier = Modifier,
    userWallet: UserWallet?,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PlatformTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.wallet_history),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        if (userWallet == null || userWallet.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.no_transactions_yet),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(userWallet.transactions) { transaction ->
                    WalletTransactionItem(transaction = transaction)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun WalletTransactionItem(transaction: Transaction) {
    val isCredit = transaction.type == Type.Credit
    val amountColor = if (isCredit) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.error
    val backgroundColor = if (isCredit) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val prefix = if (isCredit) {
        stringResource(Res.string.credit_prefix)
    } else {
        stringResource(Res.string.debit_prefix)
    }

    val formattedDate = transaction.date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isCredit) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
            contentDescription = transaction.type.name,
            tint = amountColor,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.7f),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$prefix ${transaction.details}",
                style = MaterialTheme.typography.titleSmall,
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = transaction.amount.toLong().toString(),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
            ),
        )
    }
}
