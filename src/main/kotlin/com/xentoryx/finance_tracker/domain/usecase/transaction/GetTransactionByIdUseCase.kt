package com.xentoryx.finance_tracker.domain.usecase.transaction

import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.repository.transaction.TransactionRepository
import com.xentoryx.finance_tracker.exception.NotFoundException
import java.util.UUID

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: UUID, userId: UUID): Transaction {
        val tx = transactionRepository.findById(id)
            ?: throw NotFoundException("Transaction not found")

        if (tx.userId != userId)
            throw NotFoundException("Transaction not found")

        return tx
    }
}
