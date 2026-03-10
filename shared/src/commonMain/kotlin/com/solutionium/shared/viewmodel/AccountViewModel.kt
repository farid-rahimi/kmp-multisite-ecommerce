package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.domain.config.GetContactInfoUseCase
import com.solutionium.shared.domain.config.GetPrivacyPolicyUseCase
import com.solutionium.shared.domain.config.WalletEnabledUseCase
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
import com.solutionium.shared.domain.user.RequestPasswordResetOtpUseCase
import com.solutionium.shared.domain.user.ResetPasswordByOtpUseCase
import com.solutionium.shared.domain.user.SendOtpUseCase
import com.solutionium.shared.domain.user.SetLanguageUseCase
import com.solutionium.shared.domain.user.SignupByUserPassUseCase
import com.solutionium.shared.domain.user.VerifyPasswordResetOtpUseCase
import com.solutionium.shared.util.PhoneNumberFormatter
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
    private val signupByUserPassUseCase: SignupByUserPassUseCase,
    private val requestPasswordResetOtpUseCase: RequestPasswordResetOtpUseCase,
    private val verifyPasswordResetOtpUseCase: VerifyPasswordResetOtpUseCase,
    private val resetPasswordByOtpUseCase: ResetPasswordByOtpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val editUserDetailsUseCase: EditUserDetailsUseCase,
    private val getUserWalletUseCase: GetUserWalletUseCase,
    private val walletEnabledUseCase: WalletEnabledUseCase,
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
        if (_state.value.walletEnabled) {
            _state.update { it.copy(stage = AccountStage.ViewWalletTransactions) }
        }

    }


    private fun checkLoginStatus() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val isLoggedIn = checkLoginUserUseCase().first() // Assume not logged in initially
            if (isLoggedIn) {
                _state.update { it.copy(stage = AccountStage.LoggedIn, isLoading = false) }
                fetchUserDetailsAndOrders()
            } else {
                _state.update {
                    it.copy(
                        stage = AccountStage.LoggedOut,
                        isLoading = false,
                        passwordResetStage = PasswordResetStage.Idle,
                        passwordResetEmail = "",
                        passwordResetOtp = "",
                    )
                }
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
            setErrorMessage("Please enter a valid 11-digit phone number.")

            return
        }

        scope.launch {
            //_screenState.value = AccountUiState.Loading
            _state.update { it.copy(otp = "", isLoading = true, message = null, messageType = null) }

            sendOtpUseCase(phoneNumber).collect { result ->

                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                stage = AccountStage.OtpVerification,
                                isLoading = false,
                                message = null,
                                messageType = null,
                            )
                        }
                        //_screenState.value = AccountUiState.OtpVerification(_phoneNumber.value)
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage("Failed to send OTP. Please try again.")

                    }
                }

            }
        }
    }

    fun verifyOtp() {
        val otp = _state.value.otp ?: ""
        if (otp.length < 4) {
            //val currentState = _screenState.value
            setErrorMessage("Please enter a valid 4-digit OTP.")

            return
        }
        scope.launch {
            val phoneNumber = _state.value.phoneNumber ?: "0"
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }
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
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage("Otp Verification Failed")

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
            setErrorMessage("Username and password cannot be empty.")
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }

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
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage(toUserFriendlyMessage(result.error, "We could not sign you in. Please try again."))
                    }
                }
            }
        }
    }

    fun signupWithPassword(
        name: String,
        email: String,
        phone: String,
        password: String,
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val normalizedPhone = PhoneNumberFormatter.normalizeFromRaw(trimmedPhone)

        if (
            trimmedName.isBlank() ||
            trimmedEmail.isBlank() ||
            trimmedPhone.isBlank() ||
            password.isBlank()
        ) {
            setErrorMessage("Please fill all signup fields.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            setErrorMessage("Please enter a valid email address.")
            return
        }
        if (!PhoneNumberFormatter.isCanonical(normalizedPhone)) {
            setErrorMessage("Please enter phone in valid format like +971551112222.")
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }

            signupByUserPassUseCase(
                name = trimmedName,
                email = trimmedEmail,
                phone = normalizedPhone,
                password = password,
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                stage = AccountStage.LoggedIn,
                                isLoading = false,
                            )
                        }
                        fetchUserDetailsAndOrders()
                        updateFTMToken()
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage(toUserFriendlyMessage(result.error, "We could not create your account. Please try again."))
                    }
                }
            }
        }
    }

    fun requestPasswordResetOtp(email: String) {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            setErrorMessage("Please enter a valid email address.")
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }
            requestPasswordResetOtpUseCase(trimmedEmail).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                passwordResetEmail = trimmedEmail,
                                passwordResetStage = PasswordResetStage.OtpSent,
                            )
                        }
                        setSuccessMessage("Verification code sent to your email.")
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage(toUserFriendlyMessage(result.error, "Unable to send verification code."))
                    }
                }
            }
        }
    }

    fun verifyPasswordResetOtp(email: String, otp: String) {
        val trimmedEmail = email.trim()
        val trimmedOtp = otp.trim()
        if (trimmedEmail.isBlank() || trimmedOtp.isBlank()) {
            setErrorMessage("Please enter email and verification code.")
            return
        }
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }
            verifyPasswordResetOtpUseCase(trimmedEmail, trimmedOtp).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                passwordResetEmail = trimmedEmail,
                                passwordResetOtp = trimmedOtp,
                                passwordResetStage = PasswordResetStage.OtpVerified,
                            )
                        }
                        setSuccessMessage("Code verified. Set a new password.")
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage(toUserFriendlyMessage(result.error, "Invalid verification code."))
                    }
                }
            }
        }
    }

    fun resetPasswordByOtp(email: String, otp: String, newPassword: String) {
        val trimmedEmail = email.trim()
        val trimmedOtp = otp.trim()
        if (trimmedEmail.isBlank() || trimmedOtp.isBlank() || newPassword.isBlank()) {
            setErrorMessage("Please complete all password reset fields.")
            return
        }
        if (newPassword.length < 6) {
            setErrorMessage("Password must be at least 6 characters.")
            return
        }
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, messageType = null) }
            resetPasswordByOtpUseCase(trimmedEmail, trimmedOtp, newPassword).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                passwordResetStage = PasswordResetStage.Idle,
                                passwordResetEmail = "",
                                passwordResetOtp = "",
                            )
                        }
                        setSuccessMessage("Password updated successfully. Please sign in.")
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(isLoading = false) }
                        setErrorMessage(toUserFriendlyMessage(result.error, "Unable to reset password."))
                    }
                }
            }
        }
    }

    fun cancelPasswordReset() {
        _state.update {
            it.copy(
                passwordResetStage = PasswordResetStage.Idle,
                passwordResetEmail = "",
                passwordResetOtp = "",
                message = null,
                messageType = null,
            )
        }
    }

    fun startPasswordReset() {
        _state.update {
            it.copy(
                passwordResetStage = PasswordResetStage.EmailInput,
                passwordResetEmail = "",
                passwordResetOtp = "",
                message = null,
                messageType = null,
            )
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
        val emailBlank = userDetails.email.isBlank()
        val emailInvalid = userDetails.email.isNotBlank() && !isValidEmail(userDetails.email)
        val phoneInvalid = !PhoneNumberFormatter.isCanonical(userDetails.phoneNumber)

        if (firstNameBlank || emailBlank || emailInvalid || phoneInvalid) {
            _state.update {
                it.copy(
                    validationErrors = FieldErrors(
                        firstNameErrorKey = if (firstNameBlank) AccountValidationErrorKeys.FIELD_REQUIRED else null,
                        lastNameErrorKey = null,
                        emailErrorKey = when {
                            emailBlank -> AccountValidationErrorKeys.FIELD_REQUIRED
                            emailInvalid -> AccountValidationErrorKeys.INVALID_EMAIL
                            else -> null
                        },
                        phoneErrorKey = if (phoneInvalid) AccountValidationErrorKeys.INVALID_PHONE else null,
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
        _state.update { it.copy(message = null, messageType = null) }

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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = toUserFriendlyMessage(result.error, "We could not save your profile changes."),
                        )
                    }
                }
            }

        }
    }


    private fun fetchUserDetailsAndOrders() {
        scope.launch {
            val isWalletEnabled = walletEnabledUseCase()
            _state.update {
                it.copy(
                    isLoading = true,
                    isLoadingWallet = isWalletEnabled,
                    walletEnabled = isWalletEnabled,
                    isLoadingLatestOrder = true,
                )
            }
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


            if (isWalletEnabled) {
                getUserWalletUseCase().collect { walletResult ->
                    when (walletResult) {
                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    isLoadingWallet = false,
                                    userWallet = walletResult.data
                                )
                            }
                        }

                        is Result.Failure -> {
                            _state.update { it.copy(userWallet = null, isLoadingWallet = false) }
                            setErrorMessage(
                                toUserFriendlyMessage(
                                    walletResult.error,
                                    "We could not load wallet information.",
                                ),
                            )
                        }
                    }
                }
            } else {
                _state.update { it.copy(userWallet = null, isLoadingWallet = false, stage = AccountStage.LoggedIn) }
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
                // Send non-existent ID to force empty result instead of loading full catalog.
                navigateToProductList("0")
                return@launch
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

    fun clearMessage() {
        _state.update { it.copy(message = null, messageType = null) }
    }

    private fun setErrorMessage(text: String) {
        _state.update { it.copy(message = text, messageType = AccountMessageType.Error) }
    }

    private fun setSuccessMessage(text: String) {
        _state.update { it.copy(message = text, messageType = AccountMessageType.Success) }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val atIndex = email.indexOf('@')
        if (atIndex <= 0 || atIndex == email.lastIndex) return false
        val domain = email.substring(atIndex + 1)
        return domain.contains('.') && !domain.startsWith('.') && !domain.endsWith('.')
    }

    private fun toUserFriendlyMessage(error: GeneralError, fallback: String): String =
        when (error) {
            is GeneralError.ApiError -> {
                val apiMessage = error.message?.trim().orEmpty()
                if (apiMessage.isNotEmpty()) {
                    apiMessage
                } else {
                    when (error.status) {
                        400 -> "The request is invalid. Please review your input and try again."
                        401 -> "Your credentials are incorrect. Please try again."
                        403 -> "Access was denied. Please check your permissions and try again."
                        404 -> "The service is currently unavailable. Please try again later."
                        409 -> "An account with this information already exists."
                        in 500..599 -> "The server is currently unavailable. Please try again in a moment."
                        else -> fallback
                    }
                }
            }

            is GeneralError.NetworkError -> "No internet connection. Please check your network and try again."
            is GeneralError.UnknownError -> error.error.message?.takeIf { it.isNotBlank() } ?: fallback
        }
}
