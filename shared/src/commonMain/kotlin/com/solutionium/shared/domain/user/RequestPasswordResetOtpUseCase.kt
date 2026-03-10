package com.solutionium.shared.domain.user

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.user.WooUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface RequestPasswordResetOtpUseCase {
    operator fun invoke(email: String): Flow<Result<Unit, GeneralError>>
}

class RequestPasswordResetOtpUseCaseImpl(
    private val userRepository: WooUserRepository,
) : RequestPasswordResetOtpUseCase {
    override fun invoke(email: String): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.requestPasswordResetOtp(email))
    }
}
