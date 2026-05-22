package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import com.xentoryx.finance_tracker.exception.ValidationException
import com.xentoryx.finance_tracker.presentation.dto.request.CreateTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class CreateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateTransactionRequest): Transaction {

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException("Amount must be greater than zero")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException("Invalid type. Must be INCOME, EXPENSE or TRANSFER") }

        if (type == TransactionType.TRANSFER && req.transferToAccountId == null)
            throw ValidationException("transferToAccountId is required for TRANSFER")

        if (type == TransactionType.TRANSFER && req.transferToAccountId == req.accountId)
            throw ValidationException("Source and destination accounts must be different")

        val date = runCatching { LocalDate.parse(req.transactionDate) }
            .getOrElse { throw ValidationException("Invalid date format. Use yyyy-MM-dd") }

        val categoryId = runCatching { UUID.fromString(req.categoryId) }
            .getOrElse { throw ValidationException("Invalid category id") }

        // FIX: category validation moved from Repository to UseCase
        categoryRepository.findById(categoryId)
            ?: throw NotFoundException("Category not found: $categoryId")

        return transactionRepository.create(
            Transaction(
                id                  = UUID.randomUUID(),
                userId              = userId,
                accountId           = UUID.fromString(req.accountId),
                categoryId          = categoryId,
                transferToAccountId = req.transferToAccountId?.let { UUID.fromString(it) },
                amount              = amount,
                type                = type,
                note                = req.note?.trim(),
                transactionDate     = date,
                createdAt           = LocalDateTime.now()
            )
        )
    }
}
