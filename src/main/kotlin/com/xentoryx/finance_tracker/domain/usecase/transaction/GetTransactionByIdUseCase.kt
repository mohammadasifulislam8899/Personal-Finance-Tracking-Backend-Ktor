package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import java.util.UUID

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID): Transaction {
        val tx = transactionRepository.findById(id)
            ?: throw IllegalArgumentException("Transaction not found")

        if (tx.userId != userId)
            throw IllegalArgumentException("Transaction not found")

        return tx
    }
}