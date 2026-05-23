package com.xentoryx.finance_tracker.domain.usecase.transaction

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
            ?: throw NotFoundException("Transaction not found")

        if (existing.userId != userId)
            throw NotFoundException("Transaction not found")

        val amount = BigDecimal.valueOf(req.amount)
        if (amount <= BigDecimal.ZERO)
            throw ValidationException("Amount must be greater than zero")

        val type = runCatching { TransactionType.valueOf(req.type.uppercase()) }
            .getOrElse { throw ValidationException("Invalid type. Must be INCOME, EXPENSE or TRANSFER") }

        if (type == TransactionType.TRANSFER && req.transferToAccountId == null)
            throw ValidationException("transferToAccountId is required for TRANSFER")

        val date = runCatching { LocalDate.parse(req.transactionDate) }
            .getOrElse { throw ValidationException("Invalid date format. Use yyyy-MM-dd") }

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
