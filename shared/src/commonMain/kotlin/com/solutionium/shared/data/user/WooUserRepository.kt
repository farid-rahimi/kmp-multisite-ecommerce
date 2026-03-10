package com.solutionium.shared.data.user

import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.data.model.WalletConfig
import kotlinx.coroutines.flow.Flow

interface WooUserRepository {

    suspend fun sendOtp(phoneNumber: String): Result<Unit, GeneralError>

    suspend fun loginOrRegister(phoneNumber: String, otp: String): Result<ActionType, GeneralError>
    suspend fun loginUserPass(user: String, pass: String): Result<ActionType, GeneralError>
    suspend fun signupUserPass(
        name: String,
        email: String,
        phone: String,
        pass: String,
    ): Result<ActionType, GeneralError>
    suspend fun requestPasswordResetOtp(email: String): Result<Unit, GeneralError>
    suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Unit, GeneralError>
    suspend fun resetPasswordByOtp(
        email: String,
        otp: String,
        newPassword: String,
    ): Result<Unit, GeneralError>


    suspend fun getMe(): Result<UserDetails, GeneralError>

    suspend fun updateUserProfile(userDetails: UserDetails): Result<UserDetails, GeneralError>

    suspend fun getUserWallet(): Result<UserWallet, GeneralError>
    suspend fun getWalletConfig(): Result<WalletConfig, GeneralError>

    suspend fun logout(): Boolean
    fun getCurrentUserId(): String?
    fun isLoggedIn(): Flow<Boolean>

    suspend fun saveAddress(address: Address)
    fun getAddresses(): Flow<List<Address>>
    fun getAddressById(id: Int): Flow<Address?>
    suspend fun deleteAddress(address: Address)

    suspend fun setDefaultAddress(addressId: Int)
    fun isSuperUser(): Flow<Boolean>

    fun getLanguage(): Flow<String?>
    suspend fun setLanguage(languageCode: String)

}
