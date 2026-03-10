package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.UserAccess
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.model.UserWallet
import com.solutionium.shared.data.model.WalletConfig
import com.solutionium.shared.data.api.woo.WooUserRemoteSource
import com.solutionium.shared.data.api.woo.converters.toEditUserRequest
import com.solutionium.shared.data.api.woo.converters.toUserAccess
import com.solutionium.shared.data.api.woo.converters.toUserDetails
import com.solutionium.shared.data.api.woo.converters.toUserWallet
import com.solutionium.shared.data.api.woo.converters.toWalletConfig
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.network.adapter.NetworkResponse
import com.solutionium.shared.data.network.clients.DigitsClient
import com.solutionium.shared.data.network.clients.UserClient
import com.solutionium.shared.data.model.Result


internal class WooUserRemoteSourceImpl(
    private val authService: DigitsClient,
    private val userService: UserClient
) : WooUserRemoteSource {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit, GeneralError> =
        when (val result = authService.sendOTP(
            mapOf(
                "mobileNo" to phoneNumber,
                "countrycode" to "+98",
                "type" to "login"
            )
        )) {
            is NetworkResponse.Success -> {
                val digitsResponse = result.body
                if (digitsResponse?.code == 1) {
                    Result.Success(Unit)
                } else {
                    Result.Failure(GeneralError.UnknownError(Throwable(digitsResponse?.code.toString())))
                }
            }

            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.message,
                        errorResponse.code.toString(),
                        400
                    )
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun loginOrRegister(
        phoneNumber: String,
        otp: String
    ): Result<UserAccess, GeneralError> =
        when (val result = authService.oneClick(
            mapOf(
                "mobileNo" to phoneNumber,
                "countrycode" to "+98",
                "otp" to otp,
            )
        )) {
            is NetworkResponse.Success -> {
                val digitsResponse = result.body
                if (digitsResponse?.success == true) {
                    Result.Success(digitsResponse.data.toUserAccess())
                } else {
                    Result.Failure(GeneralError.UnknownError(Throwable(digitsResponse?.data.toString())))
                }
            }

            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        400
                    )
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun loginUserPass(
        user: String,
        pass: String
    ): Result<UserAccess, GeneralError> =
        when (val result = authService.loginUser(
            //user = user.toRequestBody(MultipartBody.FORM), password = pass.toRequestBody(MultipartBody.FORM)
            user = user,
            password = pass
        )
        ) {
            is NetworkResponse.Success -> {
                val digitsResponse = result.body
                if (digitsResponse?.success == true) {
                    Result.Success(digitsResponse.data.toUserAccess())
                } else {
                    Result.Failure(GeneralError.UnknownError(Throwable(digitsResponse?.data.toString())))
                }
            }

            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        400
                    )
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun signupUserPass(
        name: String,
        email: String,
        phone: String,
        pass: String,
    ): Result<UserAccess, GeneralError> =
        when (
            val result = authService.registerUser(
                name = name,
                email = email,
                phone = phone,
                password = pass,
            )
        ) {
            is NetworkResponse.Success -> {
                val response = result.body
                if (response?.success == true) {
                    Result.Success(response.data.toUserAccess())
                } else {
                    Result.Failure(GeneralError.UnknownError(Throwable(response?.data.toString())))
                }
            }

            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        result.code,
                    ),
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun requestPasswordResetOtp(email: String): Result<Unit, GeneralError> =
        when (val result = authService.requestPasswordResetOtp(email)) {
            is NetworkResponse.Success -> Result.Success(Unit)
            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        result.code,
                    ),
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Unit, GeneralError> =
        when (val result = authService.verifyPasswordResetOtp(email, otp)) {
            is NetworkResponse.Success -> Result.Success(Unit)
            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        result.code,
                    ),
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun resetPasswordByOtp(
        email: String,
        otp: String,
        newPassword: String,
    ): Result<Unit, GeneralError> =
        when (val result = authService.resetPasswordByOtp(email, otp, newPassword)) {
            is NetworkResponse.Success -> Result.Success(Unit)
            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(
                    GeneralError.ApiError(
                        errorResponse.data?.msg,
                        errorResponse.data?.code,
                        result.code,
                    ),
                )
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }

    override suspend fun logout(token: String?): Result<Boolean, GeneralError> =
        when (val result = authService.logout("Bearer $token")) {
            is NetworkResponse.Success -> {
                val response = result.body
                if (response?.success == true) {
                    Result.Success(true)
                } else {
                    Result.Failure(GeneralError.UnknownError(Throwable("Logout failed")))
                }
            }
            is NetworkResponse.ApiError -> {
                Result.Failure(GeneralError.UnknownError(Throwable("Logout failed")))
            }
            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))

        }

    override suspend fun getMe(token: String?): Result<UserDetails, GeneralError> =
        handleNetworkResponse(
            networkCall = { userService.getMe("Bearer $token") },
            mapper = { response -> response.toUserDetails() }
        )

    override suspend fun updateUserProfile(
        token: String?,
        userId: String,
        userDetails: UserDetails
    ): Result<UserDetails, GeneralError> =
        handleNetworkResponse(
            networkCall = {
                userService.updateUser(
                    userId = userId,
                    userData = userDetails.toEditUserRequest(),
                    token = "Bearer $token"
                )
            },
            mapper = { response -> response.toUserDetails() }
        )



    override suspend fun getUserWallet(token: String?): Result<UserWallet, GeneralError> =
        handleNetworkResponse(
            networkCall = { userService.getUserWallet("Bearer $token") },
            mapper = { response -> response.toUserWallet() }
        )


    override suspend fun getWalletConfig(): Result<WalletConfig, GeneralError> =
        handleNetworkResponse(
            networkCall = { userService.getWalletConfig() },
            mapper = { response -> response.toWalletConfig() }
        )


}
