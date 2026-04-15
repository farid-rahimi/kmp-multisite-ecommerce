package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.UserAccess
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.data.model.WalletConfig
import com.solutionium.shared.data.model.Result


interface WooUserRemoteSource {

    suspend fun sendOtp(phoneNumber: String): Result<Unit, GeneralError>

    suspend fun loginOrRegister(phoneNumber: String, otp: String): Result<UserAccess, GeneralError>
    suspend fun signupUserPass(
        name: String,
        email: String,
        phone: String,
        pass: String,
        requireEmailOtp: Boolean = false,
    ): Result<UserAccess, GeneralError>
    suspend fun requestPasswordResetOtp(email: String, mode: String = "reset"): Result<Unit, GeneralError>
    suspend fun verifyPasswordResetOtp(email: String, otp: String, mode: String = "reset"): Result<Unit, GeneralError>
    suspend fun resetPasswordByOtp(
        email: String,
        otp: String,
        newPassword: String,
    ): Result<Unit, GeneralError>
    suspend fun requestDeleteAccountOtp(token: String?): Result<Unit, GeneralError>
    suspend fun deleteAccountWithPassword(token: String?, password: String): Result<Unit, GeneralError>
    suspend fun deleteAccountWithOtp(token: String?, otp: String): Result<Unit, GeneralError>

    suspend fun logout(token: String?): Result<Boolean, GeneralError>
    suspend fun getMe(token: String?): Result<UserDetails, GeneralError>
    suspend fun updateUserProfile(token: String?, userId: String, userDetails: UserDetails): Result<UserDetails, GeneralError>

    suspend fun getUserWallet(token: String?): Result<UserWallet, GeneralError>
    suspend fun getWalletConfig(): Result<WalletConfig, GeneralError>
    suspend fun loginUserPass(user: String, pass: String): Result<UserAccess, GeneralError>
}
