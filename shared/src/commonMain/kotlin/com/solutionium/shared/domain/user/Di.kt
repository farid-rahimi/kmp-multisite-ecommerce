package com.solutionium.shared.domain.user

import com.solutionium.shared.data.user.getUserDataModules
import org.koin.dsl.module

fun getUserDomainModules() = setOf(userDomainModule) + getUserDataModules()




val userDomainModule = module {
    factory<SendOtpUseCase> { SendOtpUseCaseImpl(get()) }
    factory<CheckLoginUserUseCase> { CheckLoginUserUseCaseImpl(get()) }
    factory<CheckSuperUserUseCase> { CheckSuperUserUseCaseImpl(get()) }
    factory<LoginOrRegisterUseCase> { LoginOrRegisterUseCaseImpl(get()) }
    factory<LoginByUserPassUseCase> { LoginByUserPassUseCaseImpl(get()) }
    factory<SignupByUserPassUseCase> { SignupByUserPassUseCaseImpl(get()) }
    factory<RequestPasswordResetOtpUseCase> { RequestPasswordResetOtpUseCaseImpl(get()) }
    factory<VerifyPasswordResetOtpUseCase> { VerifyPasswordResetOtpUseCaseImpl(get()) }
    factory<ResetPasswordByOtpUseCase> { ResetPasswordByOtpUseCaseImpl(get()) }
    factory<LogoutUseCase> { LogoutUseCaseImpl(get()) }
    factory<GetCurrentUserUseCase> { GetCurrentUserUseCaseImpl(get()) }
    factory<LoadAddressesUseCase> { LoadAddressesUseCaseImpl(get()) }
    factory<SaveAddressUseCase> { SaveAddressUseCaseImpl(get()) }
    factory<DeleteAddressUseCase> { DeleteAddressUseCaseImpl(get()) }
    factory<SetDefaultAddressUseCase> { SetDefaultAddressUseCaseImpl(get()) }
    factory<GetUserWalletUseCase> { GetUserWalletUseCaseImpl(get()) }
    factory<GetWalletConfigUseCase> { GetWalletConfigUseCaseImpl(get()) }
    factory<GetAllStoryViewUseCase> { GetAllStoryViewUseCaseImpl(get()) }
    factory<AddStoryViewUseCase> { AddStoryViewUseCaseImpl(get()) }
    factory<EditUserDetailsUseCase> { EditUserDetailsUseCaseImpl(get()) }
    factory<SetLanguageUseCase> { SetLanguageUseCaseImpl(get()) }
    factory<ObserveLanguageUseCase> { ObserveLanguageUseCaseImpl(get()) }
}
