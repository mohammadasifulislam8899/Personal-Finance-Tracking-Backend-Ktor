package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import java.util.UUID

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = transactionRepository.delete(id, userId)
        if (!deleted) throw IllegalArgumentException("Transaction not found")
    }
}