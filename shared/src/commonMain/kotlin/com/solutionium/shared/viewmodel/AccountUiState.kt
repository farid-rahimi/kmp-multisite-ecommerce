package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet

data class AccountUIState(
    val lastStage: AccountStage? = null,

    val stage: AccountStage = AccountStage.LoggedOut,

    val isLoading: Boolean = false,
    val isRequestingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val isSubmittingUserDetails: Boolean = false,
    val isLoggingOut: Boolean = false,
    val phoneNumber: String? = null,
    val email: String? = null,
    val name: String? = null,
    val otp: String? = null,
    val username: String = "",
    val password: String = "",
    val userDetails: UserDetails? = null,
    val userWallet: UserWallet? = null,
    val isLoadingWallet: Boolean = false,
    val latestOrder: Order? = null,
    val isLoadingLatestOrder: Boolean = false,
    val message: String? = null,
    val validationErrors: FieldErrors = FieldErrors(),
    val showLogoutConfirmDialog: Boolean = false, // Add this new state
    val currentLanguage: String = "none",
    val privacyPolicy: String = "",

    val contactInfo: ContactInfo? = null,
    val showContactSupportDialog: Boolean = false
)

enum class AccountStage{
    Error,
    EditProfile,
    ViewWalletTransactions,
    LoggedIn,
    NewUserDetailsInput,
    OtpVerification,
    ChangeLanguage,
    LoggedOut
}

data class FieldErrors(
    val firstNameErrorKey: String? = null,
    val lastNameErrorKey: String? = null,
    val emailErrorKey: String? = null
)

object AccountValidationErrorKeys {
    const val FIELD_REQUIRED = "error_field_required"
    const val INVALID_EMAIL = "error_invalid_email"
}
