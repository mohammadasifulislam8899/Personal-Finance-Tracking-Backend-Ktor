package com.xentoryx.finance_tracker.di

import com.xentoryx.finance_tracker.data.repository.account.AccountRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.auth.OtpRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.auth.PasswordResetRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.auth.RefreshTokenRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.auth.UserRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.transaction.TransactionRepositoryImpl
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.domain.repository.auth.*
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.domain.usecase.account.CreateAccountUseCase
import com.xentoryx.finance_tracker.domain.usecase.account.DeleteAccountUseCase
import com.xentoryx.finance_tracker.domain.usecase.account.GetAccountsUseCase
import com.xentoryx.finance_tracker.domain.usecase.account.UpdateAccountUseCase
import com.xentoryx.finance_tracker.domain.usecase.auth.*
import com.xentoryx.finance_tracker.domain.usecase.transaction.CreateTransactionUseCase
import com.xentoryx.finance_tracker.domain.usecase.transaction.DeleteTransactionUseCase
import com.xentoryx.finance_tracker.domain.usecase.transaction.GetTransactionByIdUseCase
import com.xentoryx.finance_tracker.domain.usecase.transaction.GetTransactionsUseCase
import com.xentoryx.finance_tracker.domain.usecase.transaction.UpdateTransactionUseCase
import com.xentoryx.finance_tracker.data.repository.*
import com.xentoryx.finance_tracker.data.repository.budget.BudgetRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.catgory.CategoryRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.dashboard.DashboardRepositoryImpl
import com.xentoryx.finance_tracker.data.repository.recurring.RecurringTransactionRepositoryImpl
import com.xentoryx.finance_tracker.domain.repository.auth.*
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.domain.repository.dashboard.DashboardRepository
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.domain.usecase.auth.*
import com.xentoryx.finance_tracker.domain.usecase.budget.CreateBudgetUseCase
import com.xentoryx.finance_tracker.domain.usecase.budget.DeleteBudgetUseCase
import com.xentoryx.finance_tracker.domain.usecase.budget.GetBudgetsUseCase
import com.xentoryx.finance_tracker.domain.usecase.budget.UpdateBudgetUseCase
import com.xentoryx.finance_tracker.domain.usecase.category.CreateCategoryUseCase
import com.xentoryx.finance_tracker.domain.usecase.category.DeleteCategoryUseCase
import com.xentoryx.finance_tracker.domain.usecase.category.GetCategoriesUseCase
import com.xentoryx.finance_tracker.domain.usecase.category.UpdateCategoryUseCase
import com.xentoryx.finance_tracker.domain.usecase.dashboard.GetDashboardUseCase
import com.xentoryx.finance_tracker.domain.usecase.dashboard.GetMonthlyTrendUseCase
import com.xentoryx.finance_tracker.domain.usecase.export.ExportTransactionsPdfUseCase
import com.xentoryx.finance_tracker.domain.usecase.recurring.CreateRecurringTransactionUseCase
import com.xentoryx.finance_tracker.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.xentoryx.finance_tracker.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.xentoryx.finance_tracker.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import com.xentoryx.finance_tracker.domain.usecase.recurring.UpdateRecurringTransactionUseCase
import com.xentoryx.finance_tracker.export.PdfExportService
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
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }

    single { CreateBudgetUseCase(get(), get()) }
    single { GetBudgetsUseCase(get(), get(), get()) }
    single { UpdateBudgetUseCase(get()) }
    single { DeleteBudgetUseCase(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }

    single { GetDashboardUseCase(get(), get(), get()) }
    single { GetMonthlyTrendUseCase(get()) }
    single<AccountRepository> { AccountRepositoryImpl(get()) }

    single { CreateAccountUseCase(get()) }
    single { GetAccountsUseCase(get()) }
    single { UpdateAccountUseCase(get()) }
    single { DeleteAccountUseCase(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }

    single { CreateTransactionUseCase(get(), get()) }
    single { GetTransactionsUseCase(get()) }
    single { GetTransactionByIdUseCase(get()) }
    single { UpdateTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }

    single { CreateCategoryUseCase(get()) }
    single { GetCategoriesUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }
    single { JwtService(environment = get()) }
    single { EmailService(environment = get()) }

    single<UserRepository> { UserRepositoryImpl(get()) }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl(get()) }
    single<OtpRepository> { OtpRepositoryImpl(get()) }
    single<PasswordResetRepository> { PasswordResetRepositoryImpl(get()) }
    single<RecurringTransactionRepository> { RecurringTransactionRepositoryImpl(get()) }
    single { PdfExportService() }

    single {
        ExportTransactionsPdfUseCase(get(), get(), get(), get(), get())
    }
    single { CreateRecurringTransactionUseCase(get()) }
    single { GetRecurringTransactionsUseCase(get()) }
    single { UpdateRecurringTransactionUseCase(get()) }
    single { DeleteRecurringTransactionUseCase(get()) }
    single { ProcessRecurringTransactionsUseCase(get(), get()) }
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


