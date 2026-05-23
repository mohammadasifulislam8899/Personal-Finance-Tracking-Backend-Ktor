Set-Location "C:\Users\Mohammad Asif\IdeaProjects\Personal Finance Tracker"
$base = "src\main\kotlin\com\xentoryx\finance_tracker"

# ── 1. Route files থেকে duplicate private userId() সরাও ──────────
$files = @(
    "presentation\routes\accountRoutes.kt",
    "presentation\routes\budgetRoutes.kt",
    "presentation\routes\categoryRoutes.kt",
    "presentation\routes\dashboardRoutes.kt",
    "presentation\routes\exportRoutes.kt",
    "presentation\routes\recurringTransactionRoutes.kt"
)

$files | ForEach-Object {
    $p = "$base\$_"
    $c = Get-Content $p -Raw
    $c = $c -replace '(?s)\r?\nprivate fun io\.ktor\.server\.application\.ApplicationCall\.userId\(\): UUID\? \{\r?\n    val raw = principal<JWTPrincipal>\(\)\r?\n        \?\.payload\?\.getClaim\("userId"\)\?\.asString\(\) \?: return null\r?\n    return runCatching \{ UUID\.fromString\(raw\) \}\.getOrNull\(\)\r?\n\}', ''
    Set-Content $p $c -Encoding UTF8
    Write-Host "Cleaned userId() from $_"
}

$txPath = "$base\presentation\routes\transactionRoutes.kt"
$c = Get-Content $txPath -Raw
$c = $c -replace '(?s)\r?\n// Extension.*?private fun ApplicationCall\.userId\(\): UUID\? \{\r?\n    val raw = principal<JWTPrincipal>\(\)\r?\n        \?\.payload\?\.getClaim\("userId"\)\?\.asString\(\) \?: return null\r?\n    return runCatching \{ UUID\.fromString\(raw\) \}\.getOrNull\(\)\r?\n\}', ''
Set-Content $txPath $c -Encoding UTF8
Write-Host "Cleaned userId() from transactionRoutes.kt"

# ── 2. transactionRoutes.kt — runCatching সরাও ──────────────────
$c = Get-Content $txPath -Raw
$c = $c -replace '(?s)// .*?CHANGE.*?\r?\n                val tx = runCatching \{ createTransactionUseCase\(userId, req\) \}.*?else -> throw e\r?\n                        \}\r?\n                    \}', '                val tx = createTransactionUseCase(userId, req)'
$c = $c -replace '(?s)// .*?CHANGE.*?\r?\n                val tx = runCatching \{ updateTransactionUseCase\(id, userId, req\) \}.*?else -> throw e\r?\n                        \}\r?\n                    \}', '                val tx = updateTransactionUseCase(id, userId, req)'
Set-Content $txPath $c -Encoding UTF8
Write-Host "Fixed runCatching in transactionRoutes.kt"

# ── 3. Account UseCases ──────────────────────────────────────────
$p = "$base\domain\usecase\account"

$content = "package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.model.AccountType
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.CreateAccountRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateAccountRequest): Account {

        if (req.name.isBlank())
            throw ValidationException(`"Account name cannot be empty`")

        val type = runCatching { AccountType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type. Must be BANK, CASH, MOBILE, CREDIT_CARD or OTHER`") }

        val balance = BigDecimal.valueOf(req.initialBalance ?: 0.0)
        if (balance < BigDecimal.ZERO)
            throw ValidationException(`"Initial balance cannot be negative`")

        return accountRepository.create(
            Account(
                id           = UUID.randomUUID(),
                userId       = userId,
                name         = req.name.trim(),
                type         = type,
                balance      = balance,
                currencyCode = req.currencyCode?.uppercase()?.trim() ?: `"BDT`",
                isActive     = true,
                createdAt    = LocalDateTime.now()
            )
        )
    }
}"
Set-Content "$p\CreateAccountUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = accountRepository.softDelete(id, userId)
        if (!deleted) throw NotFoundException(`"Account not found`")
    }
}"
Set-Content "$p\DeleteAccountUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetAccountsUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(userId: UUID): List<Account> {
        return accountRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): Account {
        val account = accountRepository.findById(id)
            ?: throw NotFoundException(`"Account not found`")

        if (account.userId != userId)
            throw NotFoundException(`"Account not found`")

        return account
    }
}"
Set-Content "$p\GetAccountsUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.account

import com.xentoryx.finance_tracker.domain.model.Account
import com.xentoryx.finance_tracker.domain.model.AccountType
import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateAccountRequest
import java.util.UUID

class UpdateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateAccountRequest): Account {

        if (req.name.isBlank())
            throw ValidationException(`"Account name cannot be empty`")

        val type = runCatching { AccountType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type. Must be BANK, CASH, MOBILE, CREDIT_CARD or OTHER`") }

        val existing = accountRepository.findById(id)
            ?: throw NotFoundException(`"Account not found`")

        if (existing.userId != userId)
            throw NotFoundException(`"Account not found`")

        return accountRepository.update(
            existing.copy(
                name         = req.name.trim(),
                type         = type,
                currencyCode = req.currencyCode?.uppercase()?.trim() ?: existing.currencyCode
            )
        )
    }
}"
Set-Content "$p\UpdateAccountUseCase.kt" $content -Encoding UTF8
Write-Host "Fixed Account UseCases"

# ── 4. Budget UseCases ───────────────────────────────────────────
$p = "$base\domain\usecase\budget"

$content = "package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.model.BudgetPeriod
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.ConflictException
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.CreateBudgetRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class CreateBudgetUseCase(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateBudgetRequest): Budget {

        val limit = BigDecimal.valueOf(req.amountLimit)
        if (limit <= BigDecimal.ZERO)
            throw ValidationException(`"Budget limit must be greater than zero`")

        val period = runCatching { BudgetPeriod.valueOf(req.period.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid period. Must be WEEKLY, MONTHLY or YEARLY`") }

        val categoryId = runCatching { UUID.fromString(req.categoryId) }
            .getOrElse { throw ValidationException(`"Invalid category id`") }

        categoryRepository.findById(categoryId)
            ?: throw NotFoundException(`"Category not found`")

        budgetRepository.findByUserIdAndCategory(userId, categoryId)?.let {
            throw ConflictException(`"Budget already exists for this category`")
        }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid startDate format. Use yyyy-MM-dd`") }
        } ?: LocalDate.now().withDayOfMonth(1)

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid endDate format. Use yyyy-MM-dd`") }
        } ?: when (period) {
            BudgetPeriod.WEEKLY  -> startDate.plusWeeks(1).minusDays(1)
            BudgetPeriod.MONTHLY -> startDate.withDayOfMonth(startDate.lengthOfMonth())
            BudgetPeriod.YEARLY  -> startDate.withDayOfYear(startDate.lengthOfYear())
        }

        if (startDate.isAfter(endDate))
            throw ValidationException(`"startDate must not be after endDate`")

        return budgetRepository.create(
            Budget(
                id          = UUID.randomUUID(),
                userId      = userId,
                categoryId  = categoryId,
                amountLimit = limit,
                period      = period,
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}"
Set-Content "$p\CreateBudgetUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.model.Budget
import com.xentoryx.finance_tracker.domain.model.BudgetPeriod
import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateBudgetRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateBudgetUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateBudgetRequest): Budget {

        val existing = budgetRepository.findById(id)
            ?: throw NotFoundException(`"Budget not found`")

        if (existing.userId != userId)
            throw NotFoundException(`"Budget not found`")

        val limit = BigDecimal.valueOf(req.amountLimit)
        if (limit <= BigDecimal.ZERO)
            throw ValidationException(`"Budget limit must be greater than zero`")

        val period = runCatching { BudgetPeriod.valueOf(req.period.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid period. Must be WEEKLY, MONTHLY or YEARLY`") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid startDate format`") }
        } ?: existing.startDate

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid endDate format`") }
        } ?: existing.endDate

        if (startDate.isAfter(endDate))
            throw ValidationException(`"startDate must not be after endDate`")

        return budgetRepository.update(
            existing.copy(
                amountLimit = limit,
                period      = period,
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}"
Set-Content "$p\UpdateBudgetUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.budget

import com.xentoryx.finance_tracker.domain.repository.budget.BudgetRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteBudgetUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = budgetRepository.delete(id, userId)
        if (!deleted) throw NotFoundException(`"Budget not found`")
    }
}"
Set-Content "$p\DeleteBudgetUseCase.kt" $content -Encoding UTF8
Write-Host "Fixed Budget UseCases"

# ── 5. Category UseCases ─────────────────────────────────────────
$p = "$base\domain\usecase\category"

$content = "package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.model.CategoryType
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.ConflictException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.CreateCategoryRequest
import java.util.UUID

class CreateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateCategoryRequest): Category {

        if (req.name.isBlank())
            throw ValidationException(`"Category name cannot be empty`")

        val type = runCatching { CategoryType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type. Must be INCOME or EXPENSE`") }

        if (categoryRepository.existsByName(userId, req.name.trim(), type.name))
            throw ConflictException(`"Category `'\`${req.name}`' already exists`")

        return categoryRepository.create(
            Category(
                id       = UUID.randomUUID(),
                userId   = userId,
                parentId = req.parentId?.let { UUID.fromString(it) },
                name     = req.name.trim(),
                type     = type,
                icon     = req.icon?.trim(),
                color    = req.color?.trim(),
                isSystem = false
            )
        )
    }
}"
Set-Content "$p\CreateCategoryUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import java.util.UUID

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val existing = categoryRepository.findById(id)
            ?: throw NotFoundException(`"Category not found`")

        if (existing.isSystem)
            throw ValidationException(`"System categories cannot be deleted`")

        val deleted = categoryRepository.delete(id, userId)
        if (!deleted) throw NotFoundException(`"Category not found`")
    }
}"
Set-Content "$p\DeleteCategoryUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID): List<Category> {
        return categoryRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): Category {
        val category = categoryRepository.findById(id)
            ?: throw NotFoundException(`"Category not found`")

        if (!category.isSystem && category.userId != userId)
            throw NotFoundException(`"Category not found`")

        return category
    }
}"
Set-Content "$p\GetCategoriesUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateCategoryRequest
import java.util.UUID

class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateCategoryRequest): Category {

        if (req.name.isBlank())
            throw ValidationException(`"Category name cannot be empty`")

        val existing = categoryRepository.findById(id)
            ?: throw NotFoundException(`"Category not found`")

        if (existing.isSystem)
            throw ValidationException(`"System categories cannot be modified`")

        if (existing.userId != userId)
            throw NotFoundException(`"Category not found`")

        return categoryRepository.update(
            existing.copy(
                name  = req.name.trim(),
                icon  = req req.icon?.trim() ?: existing.icon,
                color = req.color?.trim() ?: existing.color
            )
        )
    }
}"
Set-Content "$p\UpdateCategoryUseCase.kt" $content -Encoding UTF8
Write-Host "Fixed Category UseCases"

# ── 6. Transaction UseCases ──────────────────────────────────────
$p = "$base\domain\usecase\transaction"

$content = "package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = transactionRepository.delete(id, userId)
        if (!deleted) throw NotFoundException(`"Transaction not found`")
    }
}"
Set-Content "$p\DeleteTransactionUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID): Transaction {
        val tx = transactionRepository.findById(id)
            ?: throw NotFoundException(`"Transaction not found`")

        if (tx.userId != userId)
            throw NotFoundException(`"Transaction not found`")

        return tx
    }
}"
Set-Content "$p\GetTransactionByIdUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.ValidationException
import java.time.LocalDate
import java.util.UUID

class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(userId: UUID, page: Int, limit: Int): List<Transaction> {
        val safePage  = if (page < 1) 1 else page
        val safeLimit = if (limit < 1 || limit > 100) 20 else limit
        val offset    = ((safePage - 1) * safeLimit).toLong()
        return transactionRepository.findByUserId(userId, safeLimit, offset)
    }

    suspend operator fun invoke(userId: UUID, from: LocalDate, to: LocalDate): List<Transaction> {
        if (from.isAfter(to))
            throw ValidationException(`"'from' date must not be after 'to' date`")
        return transactionRepository.findByUserIdAndDateRange(userId, from, to)
    }
}"
Set-Content "$p\GetTransactionsUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateTransactionRequest): Transaction {

        val existing = transactionRepository.findById(id)
            ?: throw NotFoundException(`"Transaction not found`")

        if (existing.userId != userId)
            throw NotFoundException(`"Transaction not found`")

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException(`"Amount must be greater than zero`")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type. Must be INCOME, EXPENSE or TRANSFER`") }

        if (type == TransactionType.TRANSFER && req.transferToAccountId == null)
            throw ValidationException(`"transferToAccountId is required for TRANSFER`")

        val date = runCatching { LocalDate.parse(req.transactionDate) }
            .getOrElse { throw ValidationException(`"Invalid date format. Use yyyy-MM-dd`") }

        return transactionRepository.update(
            existing.copy(
                accountId           = UUID.fromString(req.accountId),
                categoryId          = UUID.fromString(req.categoryId),
                transferToAccountId = req.transferToAccountId?.let { UUID.fromString(it) },
                amount              = amount,
                type                = type,
                note                = req.note?.trim(),
                transactionDate     = date
            )
        )
    }
}"
Set-Content "$p\UpdateTransactionUseCase.kt" $content -Encoding UTF8
Write-Host "Fixed Transaction UseCases"

# ── 7. Recurring UseCases ────────────────────────────────────────
$p = "$base\domain\usecase\recurring"

$content = "package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.CreateRecurringTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class CreateRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateRecurringTransactionRequest): RecurringTransaction {

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException(`"Amount must be greater than zero`")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type. Must be INCOME or EXPENSE`") }

        if (type == TransactionType.TRANSFER)
            throw ValidationException(`"TRANSFER type not supported for recurring transactions`")

        val frequency = runCatching { RecurringFrequency.valueOf(req.frequency.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid frequency. Must be DAILY, WEEKLY, MONTHLY or YEARLY`") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid startDate format. Use yyyy-MM-dd`") }
        } ?: LocalDate.now()

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid endDate format. Use yyyy-MM-dd`") }
        }

        if (endDate != null && startDate.isAfter(endDate))
            throw ValidationException(`"startDate must not be after endDate`")

        return recurringRepository.create(
            RecurringTransaction(
                id          = UUID.randomUUID(),
                userId      = userId,
                accountId   = UUID.fromString(req.accountId),
                categoryId  = UUID.fromString(req.categoryId),
                amount      = amount,
                type        = type,
                frequency   = frequency,
                note        = req.note?.trim(),
                startDate   = startDate,
                endDate     = endDate,
                nextRunDate = startDate,
                isActive    = true,
                createdAt   = LocalDateTime.now()
            )
        )
    }
}"
Set-Content "$p\CreateRecurringTransactionUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = recurringRepository.delete(id, userId)
        if (!deleted) throw NotFoundException(`"Recurring transaction not found`")
    }
}"
Set-Content "$p\DeleteRecurringTransactionUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetRecurringTransactionsUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(userId: UUID): List<RecurringTransaction> {
        return recurringRepository.findAllByUserId(userId)
    }

    suspend operator fun invoke(id: UUID, userId: UUID): RecurringTransaction {
        val recurring = recurringRepository.findById(id)
            ?: throw NotFoundException(`"Recurring transaction not found`")

        if (recurring.userId != userId)
            throw NotFoundException(`"Recurring transaction not found`")

        return recurring
    }
}"
Set-Content "$p\GetRecurringTransactionsUseCase.kt" $content -Encoding UTF8

$content = "package com.xentoryx.finance_tracker.domain.usecase.recurring

import com.xentoryx.finance_tracker.domain.model.RecurringFrequency
import com.xentoryx.finance_tracker.domain.model.RecurringTransaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.recurring.RecurringTransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateRecurringTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID, req: UpdateRecurringTransactionRequest): RecurringTransaction {

        val existing = recurringRepository.findById(id)
            ?: throw NotFoundException(`"Recurring transaction not found`")

        if (existing.userId != userId)
            throw NotFoundException(`"Recurring transaction not found`")

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException(`"Amount must be greater than zero`")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid type`") }

        val frequency = runCatching { RecurringFrequency.valueOf(req.frequency.uppercase()) }
            .getOrElse { throw ValidationException(`"Invalid frequency`") }

        val startDate = req.startDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid startDate format`") }
        } ?: existing.startDate

        val endDate = req.endDate?.let {
            runCatching { LocalDate.parse(it) }
                .getOrElse { throw ValidationException(`"Invalid endDate format`") }
        }

        return recurringRepository.update(
            existing.copy(
                accountId   = UUID.fromString(req.accountId),
                categoryId  = UUID.fromString(req.categoryId),
                amount      = amount,
                type        = type,
                frequency   = frequency,
                note        = req.note?.trim(),
                startDate   = startDate,
                endDate     = endDate
            )
        )
    }
}"
Set-Content "$p\UpdateRecurringTransactionUseCase.kt" $content -Encoding UTF8
Write-Host "Fixed Recurring UseCases"

# ── 8. appModule.kt duplicate import fix ────────────────────────
$modulePath = "src\main\kotlin\com\xentoryx\finance_tracker\di\appModule.kt"
$mc = Get-Content $modulePath -Raw
$mc = $mc -replace "(?m)^(import com\.xentoryx\.finance_tracker\.domain\.repository\.auth\.\*)(\r?\n\1)+", '$1'
$mc = $mc -replace "(?m)^(import com\.xentoryx\.finance_tracker\.domain\.usecase\.auth\.\*)(\r?\n\1)+", '$1'
Set-Content $modulePath $mc -Encoding UTF8
Write-Host "Fixed appModule.kt duplicate imports"

# ── 9. Git commit ────────────────────────────────────────────────
git add .
git commit -m "fix: complete exception migration and route cleanup

- Remove duplicate private userId() from all 7 route files
- Remove runCatching try-catch from transactionRoutes POST and PUT
- Migrate Account UseCases to ValidationException/NotFoundException
- Migrate Budget UseCases to ValidationException/NotFoundException/ConflictException
- Migrate Category UseCases to ValidationException/NotFoundException/ConflictException
- Migrate Transaction UseCases to ValidationException/NotFoundException
- Migrate Recurring UseCases to ValidationException/NotFoundException
- Remove duplicate wildcard imports from appModule.kt"

Write-Host ""
Write-Host "All fixes applied and committed!" -ForegroundColor Green
