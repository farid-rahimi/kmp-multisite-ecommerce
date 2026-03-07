package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.domain.config.GetContactInfoUseCase
import com.solutionium.shared.domain.config.GetPrivacyPolicyUseCase
import com.solutionium.shared.domain.favorite.ObserveFavoritesUseCase
import com.solutionium.shared.domain.order.GetLatestOrderUseCase
import com.solutionium.shared.domain.user.CheckLoginUserUseCase
import com.solutionium.shared.domain.user.EditUserDetailsUseCase
import com.solutionium.shared.domain.user.GetCurrentUserUseCase
import com.solutionium.shared.domain.user.GetUserWalletUseCase
import com.solutionium.shared.domain.user.LoginByUserPassUseCase
import com.solutionium.shared.domain.user.LoginOrRegisterUseCase
import com.solutionium.shared.domain.user.LogoutUseCase
import com.solutionium.shared.domain.user.ObserveLanguageUseCase
import com.solutionium.shared.domain.user.SendOtpUseCase
import com.solutionium.shared.domain.user.SetLanguageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AccountViewModel(
    private val checkLoginUserUseCase: CheckLoginUserUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val loginOrRegisterUseCase: LoginOrRegisterUseCase,
    private val loginByUserPassUseCase: LoginByUserPassUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val editUserDetailsUseCase: EditUserDetailsUseCase,
    private val getUserWalletUseCase: GetUserWalletUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val latestOrderUseCase: GetLatestOrderUseCase,
    private val seLanguageUseCase: SetLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val getPrivacyPolicyUseCase: GetPrivacyPolicyUseCase,
    private val getContactInfoUseCase: GetContactInfoUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state: MutableStateFlow<AccountUIState> = MutableStateFlow(AccountUIState())
    val state: StateFlow<AccountUIState> = _state.asStateFlow()


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchData()
        observeLanguage()
    }

    private fun fetchData() {
        checkLoginStatus()
        fetchPrivacyPolicy()
        getContactInfo()
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            // Re-run all the initial data fetching logic
            fetchData()
            // A small delay can make the UI feel smoother
            delay(600)
            _isRefreshing.value = false
        }
    }

    private fun fetchPrivacyPolicy() {
        scope.launch {
            val policy = getPrivacyPolicyUseCase()
            _state.update { it.copy(privacyPolicy = policy) }
        }
    }

    private fun getContactInfo() {
        scope.launch {
            val contactInfo = getContactInfoUseCase()
            _state.update { it.copy(contactInfo = contactInfo) }
        }
    }

    private fun observeLanguage() {
        scope.launch {
            observeLanguageUseCase().collect { langCode ->
                _state.update { it.copy(currentLanguage = langCode ?: "fa") }
            }
        }
    }

    // Unified back navigation logic
    fun onNavigateBack(parentBackHandler: () -> Unit) {
        val currentStage = _state.value.stage
        when (currentStage) {
            AccountStage.EditProfile,
            AccountStage.NewUserDetailsInput -> {
                _state.update { it.copy(stage = AccountStage.LoggedIn) }
                checkLoginStatus()
            }

            AccountStage.OtpVerification -> {
                _state.update { it.copy(stage = AccountStage.LoggedOut) }
            }

            AccountStage.Error -> {
                checkLoginStatus()
            }

            AccountStage.ViewWalletTransactions -> {
                _state.update { it.copy(stage = AccountStage.LoggedIn) }
                checkLoginStatus()
            }

            AccountStage.LoggedIn -> parentBackHandler()
            AccountStage.LoggedOut -> parentBackHandler()
            AccountStage.ChangeLanguage -> {
                if (_state.value.lastStage != null) {
                    _state.update { it.copy(stage = _state.value.lastStage!!, lastStage = null) }
                } else {
                    parentBackHandler()
                }
            }
        }
    }

    fun onNavigateToEditProfile() {
        _state.update { it.copy(stage = AccountStage.EditProfile) }

    }


    fun onNavigateBackToAccount() {
        _state.update { it.copy(stage = AccountStage.LoggedIn) }

    }

    fun onNavigateToWalletHistory() {
        _state.update { it.copy(stage = AccountStage.ViewWalletTransactions) }

    }


    private fun checkLoginStatus() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val isLoggedIn = checkLoginUserUseCase().first() // Assume not logged in initially
            if (isLoggedIn) {
                _state.update { it.copy(stage = AccountStage.LoggedIn, isLoading = false) }
                fetchUserDetailsAndOrders()
            } else {
                _state.update { it.copy(stage = AccountStage.LoggedOut, isLoading = false) }
            }
        }
    }

    fun onPhoneNumberChange(newNumber: String) {
        _state.update { it.copy(phoneNumber = newNumber) }
        //_phoneNumber.value = newNumber.filter { it.isDigit() }.take(11) // Basic validation
    }

    fun onOtpChange(newOtp: String) {
        _state.update { it.copy(otp = newOtp) }
        //_otp.value = newOtp.filter { it.isDigit() }.take(4) // Basic validation
    }

    fun onUsernameChange(newUsername: String) {
        _state.update { it.copy(username = newUsername) }
    }

    fun onPasswordChange(newPassword: String) {
        _state.update { it.copy(password = newPassword) }
    }

    fun requestOtp() {
        val phoneNumber = _state.value.phoneNumber ?: "0"
        if (phoneNumber.length < 11) {
            _state.update { it.copy(message = "Please enter a valid 11-digit phone number.") }

            return
        }

        scope.launch {
            //_screenState.value = AccountUiState.Loading
            _state.update { it.copy(otp = "", isLoading = true) }

            sendOtpUseCase(phoneNumber).collect { result ->

                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                stage = AccountStage.OtpVerification,
                                isLoading = false,
                                message = null
                            )
                        }
                        //_screenState.value = AccountUiState.OtpVerification(_phoneNumber.value)
                    }

                    is Result.Failure -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                message = "Failed to send OTP. Please try again."
                            )
                        }

                    }
                }

            }
        }
    }

    fun verifyOtp() {
        val otp = _state.value.otp ?: ""
        if (otp.length < 4) {
            //val currentState = _screenState.value
            _state.update { it.copy(message = "Please enter a valid 4-digit OTP.") }

            return
        }
        scope.launch {
            val phoneNumber = _state.value.phoneNumber ?: "0"
            _state.update { it.copy(isLoading = true) }
            //_screenState.value = AccountUiState.Loading
            //delay(500) // Simulate API call to verify OTP

            loginOrRegisterUseCase(phoneNumber, otp).collect { result ->

                when (result) {
                    is Result.Success -> {
                        val isRegister = result.data == ActionType.REGISTER
                        if (isRegister)
                            _state.update {
                                it.copy(
                                    stage = AccountStage.NewUserDetailsInput,
                                    isLoading = false
                                )
                            }
                        else {

                            _state.update {
                                it.copy(
                                    otp = "",
                                    stage = AccountStage.LoggedIn,
                                    isLoading = false
                                )
                            }
                            checkLoginStatus()

                        }
                        updateFTMToken() // To Save FCM Token
                    }

                    is Result.Failure -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                message = "Otp Verification Failed"
                            )
                        }

                    }
                }

            }

        }
    }

    // Inside AccountViewModel.kt
// ... (after verifyOtp function)
    fun loginWithPassword() {
        val username = _state.value.username
        val password = _state.value.password

        // --- Basic Validation ---
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(message = "Username and password cannot be empty.") }
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }

            // --- Call the Use Case ---
            loginByUserPassUseCase(username, password).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Login was successful
                        _state.update {
                            it.copy(
                                // Reset fields after successful login
                                username = "",
                                password = "",
                                stage = AccountStage.LoggedIn,
                                isLoading = false
                            )
                        }
                        // Fetch user details now that we are logged in
                        fetchUserDetailsAndOrders()
                        updateFTMToken()
                    }

                    is Result.Failure -> {
                        // Login failed, show an error
                        _state.update {
                            it.copy(
                                isLoading = false,
                                message = result.error.toString() // Or a more user-friendly message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateFTMToken() {
        scope.launch {

            editUserDetailsUseCase(userDetails = UserDetails())

        }
    }


    fun submitNewUserDetails(userDetails: UserDetails) {

        // --- VALIDATION LOGIC START ---
        _state.update { it.copy(validationErrors = FieldErrors()) }
        val firstNameBlank = userDetails.firstName.isBlank()
        val lastNameBlank = userDetails.lastName.isBlank()
        val emailInvalid = userDetails.email.isNotBlank() && !isValidEmail(userDetails.email)

        if (firstNameBlank || lastNameBlank || emailInvalid) {
            _state.update {
                it.copy(
                    validationErrors = FieldErrors(
                        firstNameErrorKey = if (firstNameBlank) AccountValidationErrorKeys.FIELD_REQUIRED else null,
                        lastNameErrorKey = if (lastNameBlank) AccountValidationErrorKeys.FIELD_REQUIRED else null,
                        emailErrorKey = if (emailInvalid) AccountValidationErrorKeys.INVALID_EMAIL else null,
                    )
                )
            }
            return // Stop execution
        }

        if (userDetails.displayName.isBlank()) {
            userDetails.displayName = userDetails.firstName
            //_state.update { it.copy(message = "Display name cannot be empty.") }
            //return // Stop execution if validation fails
        }

        // Clear any previous error messages if validation passes
        _state.update { it.copy(message = null) }

        // --- VALIDATION LOGIC END ---


        scope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = editUserDetailsUseCase(userDetails)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            stage = AccountStage.LoggedIn,
                            userDetails = result.data,
                            isLoading = false
                        )
                    }
                    checkLoginStatus()
                }

                is Result.Failure -> {
                    _state.update { it.copy(isLoading = false, message = result.error.toString()) }
                }
            }

        }
    }


    private fun fetchUserDetailsAndOrders() {
        scope.launch {
            _state.update { it.copy(isLoading = true, isLoadingWallet = true, isLoadingLatestOrder = true) }
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(userDetails = result.data, isLoading = false) }
                    }

                    is Result.Failure -> {
                        when (result.error) {
                            is GeneralError.ApiError -> logout()
                            GeneralError.NetworkError -> {}
                            is GeneralError.UnknownError -> {}
                        }


                    }
                }
            }



            getUserWalletUseCase().collect { walletResult ->// This should be a suspending function call


                when (walletResult) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoadingWallet = false,
                                userWallet = walletResult.data
                            )
                        }

                        // Both user details and wallet loaded successfully
                    }

                    is Result.Failure -> {
                        _state.update {
                            it.copy(
                                userWallet = null,
                                isLoadingWallet = false,
                                message = walletResult.error.toString()
                            )
                        }
                    }
                }
            }


            when (val latestOrderResult = latestOrderUseCase()) {
                is Result.Success -> {
                    if (latestOrderResult.data.isNotEmpty()) {
                        _state.update { it.copy(latestOrder = latestOrderResult.data.first()) }
                    }
                    _state.update { it.copy(isLoadingLatestOrder = false) }
                }

                is Result.Failure -> {
                    _state.update { it.copy(isLoadingLatestOrder = false) }
                }


            }
        }
    }

    fun onLogoutClicked() {
        _state.update { it.copy(showLogoutConfirmDialog = true) }
    }

    fun onLogoutDismissed() {
        _state.update { it.copy(showLogoutConfirmDialog = false) }
    }

    fun onLogoutConfirmed() {
        _state.update { it.copy(showLogoutConfirmDialog = false) } // Hide dialog first
        logout() // Call your existing logout function
    }


    private fun logout() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            logoutUseCase().collect { _ ->

                _state.update {
                    it.copy(
                        userDetails = null,
                        userWallet = null,
                        stage = AccountStage.LoggedOut,
                        isLoading = false
                    )
                }

            }

        }
    }

    // --- Navigation Actions ---

    // In AccountViewModel.kt
    fun onMyFavoritesClicked(navigateToProductList: (String) -> Unit) {
        scope.launch {
            // This suspends until the IDs are fetched from the database
            val result = observeFavoritesUseCase.getSnapshot()
            if (result.isEmpty()) {
                // Optional: Show a toast "You have no favorites"
                navigateToProductList("")
            }
            val idsString = result.joinToString(separator = ",")
            navigateToProductList(idsString)

        }
    }

    fun onNavigateToLanguage() {

        //_state.update { it.copy(lastStage = it.stage) }
        _state.update { it.copy(lastStage = it.stage, stage = AccountStage.ChangeLanguage) }
    }

    fun showContactSupport() {
        _state.update { it.copy(showContactSupportDialog = true) }
    }

    fun dismissContactSupport() {
        _state.update { it.copy(showContactSupportDialog = false) }
    }

    fun setLanguage(langCode: String) {
        scope.launch {
            seLanguageUseCase(langCode)
        }
    }

    fun clear() {
        scope.cancel()
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val atIndex = email.indexOf('@')
        if (atIndex <= 0 || atIndex == email.lastIndex) return false
        val domain = email.substring(atIndex + 1)
        return domain.contains('.') && !domain.startsWith('.') && !domain.endsWith('.')
    }
}
