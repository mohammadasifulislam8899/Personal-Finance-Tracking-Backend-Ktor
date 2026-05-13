package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.presentation.dto.request.UpdateTransactionRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        id: UUID,
        userId: UUID,
        req: UpdateTransactionRequest
    ): Transaction {

        // Ownership check
        val existing = transactionRepository.findById(id)
            ?: throw IllegalArgumentException("Transaction not found")
        if (existing.userId != userId)
            throw IllegalArgumentException("Transaction not found")

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw IllegalArgumentException("Amount must be greater than zero")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid type. Must be INCOME, EXPENSE or TRANSFER") }

        if (type == TransactionType.TRANSFER && req.transferToAccountId == null)
            throw IllegalArgumentException("transferToAccountId is required for TRANSFER")

        val date = runCatching { LocalDate.parse(req.transactionDate) }
            .getOrElse { throw IllegalArgumentException("Invalid date format. Use yyyy-MM-dd") }

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
}