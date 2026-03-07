package com.solutionium.feature.account


import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.sharedui.common.DateHelper
import com.solutionium.sharedui.common.component.ContactSupportDialog
import com.solutionium.sharedui.common.component.FormattedPriceV2
import com.solutionium.sharedui.common.component.LanguageSelectionScreen
import com.solutionium.sharedui.common.component.OrderSummaryCard
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.Transaction
import com.solutionium.shared.data.model.Type
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.viewmodel.AccountStage
import com.solutionium.shared.viewmodel.AccountViewModel


@Composable
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
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }
//    val phoneNumber by viewModel.phoneNumber.collectAsState()
//    val otp by viewModel.otp.collectAsState()
//    val name by viewModel.name.collectAsState()
//    val email by viewModel.email.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState() // <-- Collect the refreshing state


    if (state.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onLogoutDismissed, // User clicks outside or back button
            title = { Text(stringResource(R.string.logout_confirmation_title)) },
            text = { Text(stringResource(R.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onLogoutConfirmed) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onLogoutDismissed) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.showContactSupportDialog) {
        ContactSupportDialog(
            contactInfo = state.contactInfo,
            onDismiss = { viewModel.dismissContactSupport() }
        )
    }

    // Snackbar host state for displaying errors
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine if we are in a substate that should handle back press internally
    val inSubScreenState = when (state.stage) {
        AccountStage.EditProfile,
        AccountStage.ViewWalletTransactions, // <-- ADD THIS
            // For OTP or NewUserDetails, you might also want custom back handling
            // to go to LoggedOut or OtpVerification respectively, if not just exiting.
        AccountStage.OtpVerification -> true // Example: back from OTP goes to PhoneLogin (LoggedOut state)
        AccountStage.NewUserDetailsInput -> true // Example: back from NewUser goes to OTP
        AccountStage.ChangeLanguage -> true
        else -> false
    }

    // Intercept back press if in a sub-screen state
    BackHandler(enabled = inSubScreenState) {
        viewModel.onNavigateBack(onBack) // ViewModel decides which state to go back to
    }


    LaunchedEffect(state.message) {
        if (state.message != null) {
            snackbarHostState.showSnackbar(
                message = state.message ?: "",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.stage != AccountStage.ChangeLanguage)
                        Text(stringResource(id = R.string.feature_account_title))
                },
                actions = {
                    if (state.stage != AccountStage.ChangeLanguage) {
                        var menuExpanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Language / زبان") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onNavigateToLanguage() // Navigate to the language screen
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.contact_support)) },
                                onClick = {
                                    viewModel.showContactSupport()
                                    // also close the dropdown menu
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (state.stage == AccountStage.ChangeLanguage) {
                        IconButton(onClick = { viewModel.onNavigateBack(onBack) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
//            AnimatedContent(
//                targetState = state,
//                transitionSpec = {
//                    // Add your desired animations, e.g., slide or fade
//                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
//                        animationSpec = tween(
//                            300
//                        )
//                    )
//                    //slideInVertically { width -> width } togetherWith slideOutHorizontally { width -> -width }
//                },
//                label = "AccountScreenContent"
//            ) { targetState ->
                when (state.stage) {
//                    AccountStage.Loading -> CircularProgressIndicator(
//                        modifier = Modifier.align(
//                            Alignment.Center
//                        )
//                    )

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
                        privacyPolicyContent = state.privacyPolicy

                    )

                    AccountStage.OtpVerification -> OtpVerificationScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        phoneNumber = state.phoneNumber ?: "",
                        otp = state.otp ?: "",
                        onOtpChange = viewModel::onOtpChange,
                        onVerifyOtp = viewModel::verifyOtp,
                        onRequestNewOtp = { viewModel.requestOtp() }
                    )

                    AccountStage.NewUserDetailsInput -> EditProfileSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        isNewUser = true,
                        userDetails = state.userDetails ?: UserDetails(),
                        onNavigateBack = null,
                        onSaveChanges = viewModel::submitNewUserDetails,
                        validationErrors = state.validationErrors
                    )

                    AccountStage.LoggedIn -> UserAccountScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        user = state.userDetails,
                        walletBalance = state.userWallet?.balance
                            ?: 0.0, // Pass the wallet info
                        isLoadingWalletBalance = state.isLoadingWallet,
                        onWalletClick = viewModel::onNavigateToWalletHistory, // VM changes state
                        latestOrder = state.latestOrder, // This was a summary, real orders in ViewOrders
                        isLoadingLatestOrder = state.isLoadingLatestOrder,
                        onLogout = viewModel::onLogoutClicked,
                        onOrderClick = onOrderClick, // VM changes state
                        onEditProfile = viewModel::onNavigateToEditProfile, // VM changes state
                        onManageAddresses = onAddressClick,
                        onFavoriteClick = { title ->
                            viewModel.onMyFavoritesClicked {
                                if (it.isNotEmpty())
                                    onFavoriteClick(title, it)
                                else {
                                    // show snackbar
                                }
                            }
                        },
                        onOrdersClick = onOrdersClick
                    )
                    // --- New Sub-Screens ---
                    AccountStage.EditProfile -> EditProfileSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        isLoading = state.isLoading,
                        userDetails = state.userDetails ?: UserDetails(),
                        onNavigateBack = viewModel::onNavigateBackToAccount,
                        onSaveChanges = viewModel::submitNewUserDetails,
                        validationErrors = state.validationErrors
                    )

                    AccountStage.ViewWalletTransactions -> ViewWalletTransactionsSubScreen(
                        modifier = Modifier.fillMaxSize(),
                        userWallet = state.userWallet,
                        onNavigateBack = viewModel::onNavigateBackToAccount
                    )


                    AccountStage.Error -> {}


                    //}
                    AccountStage.ChangeLanguage -> LanguageSelectionScreen(
                        currentLang = state.currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                    )
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
    onOrdersClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(user?.displayName ?: "", style = MaterialTheme.typography.headlineMedium)
            Text(
                "${user?.firstName ?: ""} ${user?.lastName ?: ""}",
                style = MaterialTheme.typography.titleSmall
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
                onWalletClick = onWalletClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                stringResource(R.string.latest_order_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            if (isLoadingLatestOrder)
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            else if (latestOrder == null) {
                Text(
                    stringResource(R.string.no_orders_yet),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                OrderSummaryCard(
                    latestOrder,
                    onClick = { onOrderClick(latestOrder.id) }
                )
            }
        }



        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            AccountActionItem(
                text = stringResource(R.string.order_history),
                icon = Icons.AutoMirrored.Filled.ListAlt, // Or Receipt
                onClick = onOrdersClick
            )
            HorizontalDivider()
        }

        item {
            AccountActionItem(
                text = stringResource(R.string.manage_addresses),
                icon = Icons.Default.LocationOn, // Or Home
                onClick = onManageAddresses
            )
            HorizontalDivider()
        }
        item {
            val favoriteTitle: String = stringResource(R.string.my_favorites)
            AccountActionItem(
                text = favoriteTitle,
                icon = Icons.Default.Favorite, // Or Home
                onClick = { onFavoriteClick(favoriteTitle) }
            )
            HorizontalDivider()
        }


        item {
            AccountActionItem(
                text = stringResource(R.string.edit_profile),
                icon = Icons.Default.Person,
                onClick = onEditProfile
            )
            HorizontalDivider()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {

                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else {

                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // For bottom padding
        }
    }
}

@Composable
fun WalletBalanceCard(
    balance: Double,
    isLoading: Boolean = false,
    onWalletClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onWalletClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    stringResource(R.string.wallet_balance_title),
                    style = MaterialTheme.typography.titleMedium
                )
                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                else
                    FormattedPriceV2(balance.toLong())
//                Text(
//                    "$ $balance ",
//                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
//                )
            }
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = "Wallet",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun AccountActionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f)) // Pushes arrow to the end
            //Icon(Icons.AutoMirrored.Outlined., contentDescription = "Navigate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWalletTransactionsSubScreen(
    modifier: Modifier = Modifier,
    userWallet: UserWallet?,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wallet_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (userWallet == null || userWallet.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_transactions_yet),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
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
    val amountColor =
        if (isCredit) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.error
    val backgroundColor =
        if (isCredit) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.errorContainer

    val prefix =
        if (isCredit) stringResource(R.string.credit_prefix) else stringResource(R.string.debit_prefix)

    val formattedDate = remember(transaction.date) {
        DateHelper.convertDateStringToJalali(transaction.date)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon for the transaction type
        Icon(
            imageVector = if (isCredit) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
            contentDescription = transaction.type.name,
            tint = amountColor,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.7f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Description and Date
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$prefix ${transaction.details}",
                style = MaterialTheme.typography.titleSmall,
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate, // Assuming date is pre-formatted
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        FormattedPriceV2(
            transaction.amount.toLong(),
            mainStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            ),

            )
        // Amount
//        Text(
//            text = "$sign${transaction.amount}",
//            color = amountColor,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
    }
}
