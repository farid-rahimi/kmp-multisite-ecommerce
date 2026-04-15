package com.solutionium.shared.domain.user

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.user.WooUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface RequestDeleteAccountOtpUseCase {
    suspend operator fun invoke(): Flow<Result<Unit, GeneralError>>
}

internal class RequestDeleteAccountOtpUseCaseImpl(
    private val userRepository: WooUserRepository,
) : RequestDeleteAccountOtpUseCase {
    override suspend fun invoke(): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.requestDeleteAccountOtp())
    }
}

interface DeleteAccountWithPasswordUseCase {
    suspend operator fun invoke(password: String): Flow<Result<Unit, GeneralError>>
}

internal class DeleteAccountWithPasswordUseCaseImpl(
    private val userRepository: WooUserRepository,
) : DeleteAccountWithPasswordUseCase {
    override suspend fun invoke(password: String): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.deleteAccountWithPassword(password))
    }
}

interface DeleteAccountWithOtpUseCase {
    suspend operator fun invoke(otp: String): Flow<Result<Unit, GeneralError>>
}

internal class DeleteAccountWithOtpUseCaseImpl(
    private val userRepository: WooUserRepository,
) : DeleteAccountWithOtpUseCase {
    override suspend fun invoke(otp: String): Flow<Result<Unit, GeneralError>> = flow {
        emit(userRepository.deleteAccountWithOtp(otp))
    }
}

interface ClearLocalUserDataUseCase {
    suspend operator fun invoke()
}

internal class ClearLocalUserDataUseCaseImpl(
    private val userRepository: WooUserRepository,
) : ClearLocalUserDataUseCase {
    override suspend fun invoke() {
        userRepository.clearLocalUserData()
    }
}
