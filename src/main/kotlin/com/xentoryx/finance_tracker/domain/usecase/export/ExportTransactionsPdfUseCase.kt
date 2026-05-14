package com.xentoryx.finance_tracker.domain.usecase.export

import com.xentoryx.finance_tracker.domain.repository.account.AccountRepository
import com.xentoryx.finance_tracker.domain.repository.auth.UserRepository
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.export.PdfExportService
import java.time.LocalDate
import java.util.UUID

class ExportTransactionsPdfUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val pdfExportService: PdfExportService
) {
    suspend operator fun invoke(
        userId: UUID,
        from: LocalDate,
        to: LocalDate
    ): ByteArray {

        if (from.isAfter(to))
            throw IllegalArgumentException("'from' date must not be after 'to' date")

        val user         = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val transactions = transactionRepository.findByUserIdAndDateRange(userId, from, to)

        // Category id → name map
        val categories   = categoryRepository.findAllByUserId(userId)
        val categoryNames = categories.associate { it.id to it.name }

        // Account id → name map
        val accounts     = accountRepository.findAllByUserId(userId)
        val accountNames = accounts.associate { it.id to it.name }

        return pdfExportService.generateTransactionReport(
            userName      = user.fullName,
            currencyCode  = user.currencyCode,
            transactions  = transactions,
            categoryNames = categoryNames,
            accountNames  = accountNames,
            from          = from,
            to            = to
        )
    }
}