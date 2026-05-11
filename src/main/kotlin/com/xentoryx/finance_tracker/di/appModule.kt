package com.xentoryx.finance_tracker.di

import com.xentoryx.finance_tracker.data.repository.*
import com.xentoryx.finance_tracker.domain.repository.auth.*
import com.xentoryx.finance_tracker.domain.usecase.auth.*
import com.xentoryx.finance_tracker.infrastructure.email.EmailService
import com.xentoryx.finance_tracker.security.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.koin.dsl.module

fun appModule(application: Application) = module {

    single<ApplicationEnvironment> { application.environment }

    single<R2dbcDatabase> {
        val config = get<ApplicationEnvironment>().config
        R2dbcDatabase.connect(
            url = config.property("db.url").getString(),
            user = config.property("db.user").getString(),
            password = config.property("db.password").getString(),
            databaseConfig = R2dbcDatabaseConfig {
                defaultMaxAttempts = 3
                defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED
            }
        )
    }

    single { JwtService(environment = get()) }
    single { EmailService(environment = get()) }

    single<UserRepository> { UserRepositoryImpl(get()) }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl(get()) }
    single<OtpRepository> { OtpRepositoryImpl(get()) }
    single<PasswordResetRepository> { PasswordResetRepositoryImpl(get()) }

    single { RegisterUseCase(get(), get()) }
    single { LoginUseCase(get()) }
    single { VerifyEmailUseCase(get(), get()) }
    single { ResendOtpUseCase(get(), get()) }
    single { RefreshTokenUseCase(get(), get()) }
    single { ForgotPasswordUseCase(get(), get()) }
    single { ResetPasswordUseCase(get(), get()) }
    single { GetUserProfileUseCase(get()) }
    single { LogoutUseCase(get()) }
}
