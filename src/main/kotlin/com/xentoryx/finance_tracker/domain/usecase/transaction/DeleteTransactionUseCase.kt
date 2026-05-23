package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID) {
        val deleted = transactionRepository.delete(id, userId)
        if (!deleted) throw NotFoundException("Transaction not found")
    }
}
