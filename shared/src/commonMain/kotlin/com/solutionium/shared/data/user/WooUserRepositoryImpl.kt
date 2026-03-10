package com.solutionium.shared.data.user

import com.solutionium.shared.data.api.woo.WooUserRemoteSource
import com.solutionium.shared.data.database.dao.AddressDao
import com.solutionium.shared.data.local.AppPreferences
import com.solutionium.shared.data.local.TokenStore
import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.data.model.WalletConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WooUserRepositoryImpl(
    private val wooUserRemoteSource: WooUserRemoteSource,
    private val tokenStore: TokenStore,
    private val appPreferences: AppPreferences,
    private val addressDao: AddressDao
) : WooUserRepository {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit, GeneralError> =
        wooUserRemoteSource.sendOtp(phoneNumber)

    override suspend fun loginOrRegister(
        phoneNumber: String,
        otp: String
    ): Result<ActionType, GeneralError> {

        return when (val result = wooUserRemoteSource.loginOrRegister(phoneNumber, otp)) {
            is Result.Success -> {
                tokenStore.saveToken(result.data.token) // <-- SAVING THE TOKEN
                tokenStore.saveUserId(result.data.userId)
                Result.Success(result.data.action)
            }

            is Result.Failure -> {
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun loginUserPass(user: String, pass: String): Result<ActionType, GeneralError> {
        return when (val result = wooUserRemoteSource.loginUserPass(user, pass)) {
            is Result.Success -> {
                tokenStore.saveToken(result.data.token) // <-- SAVING THE TOKEN
                tokenStore.saveUserId(result.data.userId)
                Result.Success(result.data.action)
            }

            is Result.Failure -> {
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun signupUserPass(
        name: String,
        email: String,
        phone: String,
        pass: String,
    ): Result<ActionType, GeneralError> {
        return when (val result = wooUserRemoteSource.signupUserPass(name, email, phone, pass)) {
            is Result.Success -> {
                tokenStore.saveToken(result.data.token)
                tokenStore.saveUserId(result.data.userId)
                Result.Success(result.data.action)
            }

            is Result.Failure -> {
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun requestPasswordResetOtp(email: String): Result<Unit, GeneralError> =
        wooUserRemoteSource.requestPasswordResetOtp(email)

    override suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Unit, GeneralError> =
        wooUserRemoteSource.verifyPasswordResetOtp(email, otp)

    override suspend fun resetPasswordByOtp(
        email: String,
        otp: String,
        newPassword: String,
    ): Result<Unit, GeneralError> = wooUserRemoteSource.resetPasswordByOtp(email, otp, newPassword)

    override suspend fun getMe(): Result<UserDetails, GeneralError> {

        if (tokenStore.getToken() == null) {
            return Result.Failure(GeneralError.UnknownError(Throwable("Token is null")))
        }

        return when (val result = wooUserRemoteSource.getMe(tokenStore.getToken())) {

            is Result.Success -> {
                tokenStore.saveSuperUser(isSuperUser = result.data.isSuperUser)
                Result.Success(result.data)
            }

            is Result.Failure -> {
                if (result.error is GeneralError.ApiError && (result.error as GeneralError.ApiError).code == "rest_not_logged_in") {
                    tokenStore.clearToken() // <-- CLEARING THE TOKEN
                }
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun updateUserProfile(userDetails: UserDetails): Result<UserDetails, GeneralError> {

        if (tokenStore.getToken() == null) {
            return Result.Failure(GeneralError.UnknownError(Throwable("Token is null")))
        }

        userDetails.fcmToken = appPreferences.getFcmToken().orEmpty()

        return when (val result = wooUserRemoteSource.updateUserProfile(
            token = tokenStore.getToken(),
            userId = tokenStore.getUserId() ?: "",
            userDetails = userDetails
        )) {

            is Result.Success -> {
                Result.Success(result.data)
            }

            is Result.Failure -> {
                if (result.error is GeneralError.ApiError && (result.error as GeneralError.ApiError).code == "rest_not_logged_in") {
                    tokenStore.clearToken() // <-- CLEARING THE TOKEN
                }
                Result.Failure(result.error)
            }
        }
    }

    override suspend fun getUserWallet(): Result<UserWallet, GeneralError> =
        wooUserRemoteSource.getUserWallet(tokenStore.getToken())


    override suspend fun getWalletConfig(): Result<WalletConfig, GeneralError> =
        wooUserRemoteSource.getWalletConfig()


    override suspend fun logout(): Boolean {
        return when (wooUserRemoteSource.logout(tokenStore.getToken())) {
            is Result.Success -> {
                tokenStore.clearToken() // <-- CLEARING THE TOKEN
                true
            }

            is Result.Failure -> {
                tokenStore.clearToken()
                false
            }
        }
    }

    override fun getCurrentUserId(): String? {
        return tokenStore.getUserId()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenStore.observeToken().map { token ->
            token != null
        }
    }
    override fun isSuperUser(): Flow<Boolean> {
        return tokenStore.observeSuperUser().map { isSuperUser ->
            isSuperUser ?: false
        }
    }

    override fun getLanguage(): Flow<String?> = appPreferences.language()

    override suspend fun setLanguage(languageCode: String) {
        appPreferences.setLanguage(languageCode)
    }

    override suspend fun saveAddress(address: Address) {
        val savedAddressId = addressDao.insertAddress(address.toEntity(tokenStore.getUserId() ?: "0"))
        setDefaultAddress(savedAddressId.toInt())
    }

    override fun getAddresses(): Flow<List<Address>> {
        return addressDao.getAllAddress(tokenStore.getUserId() ?: "0").map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getAddressById(id: Int): Flow<Address?> {
        return addressDao.getAddressById(id).map { it.toModel() }
    }

    override suspend fun deleteAddress(address: Address) {
        addressDao.deleteAddress(address.toEntity(tokenStore.getUserId() ?: "0"))
        if (address.isDefault) {
            addressDao.setFirstAddressAsDefaultForUser(tokenStore.getUserId() ?: "0")
        }
    }

    override suspend fun setDefaultAddress(addressId: Int) {
        addressDao.updateAllDefaultAddress(tokenStore.getUserId() ?: "0",false)
        addressDao.updateDefaultAddress(addressId, true)
    }




}
