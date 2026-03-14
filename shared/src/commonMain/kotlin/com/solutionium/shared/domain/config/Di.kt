package com.solutionium.shared.domain.config

import com.solutionium.shared.data.config.appConfigDataModule
import com.solutionium.shared.domain.config.impl.GetPrivacyPolicyUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetSearchTabsUseCaseImpl
import com.solutionium.shared.domain.config.impl.ForcedEnabledPaymentUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetAppImagesImpl
import com.solutionium.shared.domain.config.impl.GetBACSDetailsUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetContactInfoUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetHeaderLogoUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetStoriesUseCaseImpl
import com.solutionium.shared.domain.config.impl.GetVersionsUseCaseImpl
import com.solutionium.shared.domain.config.impl.HomeBannersUseCaseImpl
import com.solutionium.shared.domain.config.impl.InstallmentPriceEnabledUseCaseImpl
import com.solutionium.shared.domain.config.impl.PaymentMethodDiscountUseCaseImpl
import com.solutionium.shared.domain.config.impl.ReviewCriteriaUseCaseImpl
import com.solutionium.shared.domain.config.impl.WalletEnabledUseCaseImpl
import org.koin.dsl.module

fun getConfigDomainModules() = setOf(configDomainModule, appConfigDataModule)


val configDomainModule = module {
    factory<PaymentMethodDiscountUseCase> { PaymentMethodDiscountUseCaseImpl(get()) }
    factory<GetAppImages> { GetAppImagesImpl(get()) }
    factory<HomeBannersUseCase> { HomeBannersUseCaseImpl(get()) }
    factory<GetStoriesUseCase> { GetStoriesUseCaseImpl(get()) }
    factory<GetHeaderLogoUseCase> { GetHeaderLogoUseCaseImpl(get()) }
    factory<GetBACSDetailsUseCase> { GetBACSDetailsUseCaseImpl(get()) }
    factory<ReviewCriteriaUseCase> { ReviewCriteriaUseCaseImpl(get(), get(), get()) }
    factory<GetPrivacyPolicyUseCase> { GetPrivacyPolicyUseCaseImpl(get()) }
    factory<ForcedEnabledPaymentUseCase> { ForcedEnabledPaymentUseCaseImpl(get()) }
    factory<GetVersionsUseCase> { GetVersionsUseCaseImpl(get()) }
    factory<GetContactInfoUseCase> { GetContactInfoUseCaseImpl(get()) }
    factory<GetSearchTabsUseCase> { GetSearchTabsUseCaseImpl(get()) }
    factory<InstallmentPriceEnabledUseCase> { InstallmentPriceEnabledUseCaseImpl(get()) }
    factory<WalletEnabledUseCase> { WalletEnabledUseCaseImpl(get()) }
}
