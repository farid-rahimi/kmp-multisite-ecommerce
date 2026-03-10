package com.solutionium.shared.domain.user

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.user.WooUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ResetPasswordByOtpUseCase {
    operator fun invoke(email: String, otp: String, newPassword: String): Flow<Result<Unit, GeneralError>>
}

class ResetPasswordByOtpUseCaseImpl(
    private val userRepository: WooUserRepository,
) : ResetPasswordByOtpUseCase {
    override fun invoke(email: String, otp: String, newPassword: String): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.resetPasswordByOtp(email, otp, newPassword))
    }
}
