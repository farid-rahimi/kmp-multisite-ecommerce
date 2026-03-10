package com.solutionium.shared.domain.user

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.user.WooUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface VerifyPasswordResetOtpUseCase {
    operator fun invoke(email: String, otp: String): Flow<Result<Unit, GeneralError>>
}

class VerifyPasswordResetOtpUseCaseImpl(
    private val userRepository: WooUserRepository,
) : VerifyPasswordResetOtpUseCase {
    override fun invoke(email: String, otp: String): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.verifyPasswordResetOtp(email, otp))
    }
}
