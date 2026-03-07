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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.sharedui.common.component.LanguageSelectionScreen
import com.solutionium.sharedui.home.PlatformContactSupportDialog
import com.solutionium.sharedui.resources.Res
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
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountScreen(
    onAddressClick: () -> Unit,
    onFavoriteClick: (title: String, ids: String) -> Unit,
    onOrdersClick: () -> Unit,
    onOrderClick: (orderId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
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
        if (state.message != null) {
            snackbarHostState.showSnackbar(
                message = state.message ?: "",
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.stage != AccountStage.ChangeLanguage) {
                        Text(stringResource(Res.string.feature_account_title))
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
                        }
                    }
                },
                navigationIcon = {
                    if (state.stage == AccountStage.ChangeLanguage) {
                        IconButton(onClick = { viewModel.onNavigateBack(onBack) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                when (state.stage) {
                    AccountStage.LoggedOut -> PhoneLoginScreen(
                        modifier = Modifier.fillMaxSize(),
                        phoneNumber = state.phoneNumber ?: "",
                        username = state.username,
                        isLoading = state.isLoading,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        onUsernameChange = viewModel::onUsernameChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onRequestOtp = viewModel::requestOtp,
                        onPasswordLogin = viewModel::loginWithPassword,
                        privacyPolicyContent = state.privacyPolicy,
                    )

                    AccountStage.OtpVerification -> OtpVerificationScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        phoneNumber = state.phoneNumber ?: "",
                        otp = state.otp ?: "",
                        onOtpChange = viewModel::onOtpChange,
                        onVerifyOtp = viewModel::verifyOtp,
                        onRequestNewOtp = { viewModel.requestOtp() },
                    )

                    AccountStage.NewUserDetailsInput -> EditProfileSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        isNewUser = true,
                        userDetails = state.userDetails ?: UserDetails(),
                        onNavigateBack = null,
                        onSaveChanges = viewModel::submitNewUserDetails,
                        validationErrors = state.validationErrors,
                    )

                    AccountStage.LoggedIn -> UserAccountScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        user = state.userDetails,
                        walletBalance = state.userWallet?.balance ?: 0.0,
                        isLoadingWalletBalance = state.isLoadingWallet,
                        onWalletClick = viewModel::onNavigateToWalletHistory,
                        latestOrder = state.latestOrder,
                        isLoadingLatestOrder = state.isLoadingLatestOrder,
                        onLogout = viewModel::onLogoutClicked,
                        onOrderClick = onOrderClick,
                        onEditProfile = viewModel::onNavigateToEditProfile,
                        onManageAddresses = onAddressClick,
                        onFavoriteClick = { title ->
                            viewModel.onMyFavoritesClicked {
                                if (it.isNotEmpty()) {
                                    onFavoriteClick(title, it)
                                }
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

                    AccountStage.ChangeLanguage -> LanguageSelectionScreen(
                        currentLang = state.currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                    )

                    AccountStage.Error -> Unit
                }
            }
        }
    }
}

@Composable
fun UserAccountScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    user: UserDetails?,
    walletBalance: Double,
    isLoadingWalletBalance: Boolean = false,
    onWalletClick: () -> Unit,
    latestOrder: Order?,
    isLoadingLatestOrder: Boolean = false,
    onOrderClick: (orderId: Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onManageAddresses: () -> Unit,
    onFavoriteClick: (favoriteTitle: String) -> Unit,
    onOrdersClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(user?.displayName ?: "", style = MaterialTheme.typography.headlineMedium)
            Text(
                "${user?.firstName ?: ""} ${user?.lastName ?: ""}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(user?.email ?: "", style = MaterialTheme.typography.titleMedium)
            if (user?.isSuperUser == true) {
                Text("Super User", style = MaterialTheme.typography.titleSmall)
            } else {
                Text(user?.phoneNumber ?: "", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            WalletBalanceCard(
                balance = walletBalance,
                isLoading = isLoadingWalletBalance,
                onWalletClick = onWalletClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                stringResource(Res.string.latest_order_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            if (isLoadingLatestOrder) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            } else if (latestOrder == null) {
                Text(
                    stringResource(Res.string.no_orders_yet),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                SimpleOrderSummaryCard(latestOrder, onClick = { onOrderClick(latestOrder.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            AccountActionItem(
                text = stringResource(Res.string.order_history),
                icon = Icons.AutoMirrored.Filled.ListAlt,
                onClick = onOrdersClick,
            )
            HorizontalDivider()
        }

        item {
            AccountActionItem(
                text = stringResource(Res.string.manage_addresses),
                icon = Icons.Default.LocationOn,
                onClick = onManageAddresses,
            )
            HorizontalDivider()
        }

        item {
            val favoriteTitle: String = stringResource(Res.string.my_favorites)
            AccountActionItem(
                text = favoriteTitle,
                icon = Icons.Default.Favorite,
                onClick = { onFavoriteClick(favoriteTitle) },
            )
            HorizontalDivider()
        }

        item {
            AccountActionItem(
                text = stringResource(Res.string.edit_profile),
                icon = Icons.Default.Person,
                onClick = onEditProfile,
            )
            HorizontalDivider()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(Res.string.logout), color = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onWalletClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    Text(
                        text = balance.toLong().toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = "Wallet",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
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
                .padding(vertical = 8.dp),
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
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
            TopAppBar(
                title = { Text(stringResource(Res.string.wallet_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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

@Composable
private fun SimpleOrderSummaryCard(
    order: Order,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order #${order.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = order.status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
