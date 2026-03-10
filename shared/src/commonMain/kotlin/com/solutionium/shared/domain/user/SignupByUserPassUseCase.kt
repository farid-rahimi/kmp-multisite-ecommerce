package com.solutionium.shared.domain.user

import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.user.WooUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface SignupByUserPassUseCase {
    operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Flow<Result<ActionType, GeneralError>>
}

class SignupByUserPassUseCaseImpl(
    private val userRepository: WooUserRepository,
) : SignupByUserPassUseCase {
    override fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): Flow<Result<ActionType, GeneralError>> = flow {
        emit(userRepository.signupUserPass(name, email, phone, password))
    }
}
